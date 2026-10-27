"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { onMessagePublished } = require("firebase-functions/v2/pubsub");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const crypto = require("crypto");

admin.initializeApp();

const REGION = "us-central1";
const APP_NAME = "PocketCodedWeb";
const VERIFICATION_DAILY_LIMIT = 1000;
const RESET_DAILY_LIMIT = 150;
const ANDROID_PACKAGE_NAME = "com.mkhokheli.pocketcodedweb";
const LIFETIME_PRODUCT_ID = "pocketcodedweb_lifetime";
const MONTHLY_PRODUCT_ID = "pocketcodedweb_premium_monthly";
const PLAY_BILLING_TOPIC = "play-billing-events";
const PLAY_BILLING_SERVICE_ACCOUNT =
  "play-billing-backend@pocketcodedweb-6887b.iam.gserviceaccount.com";
exports.checkEmailQuota = onRequest(
  {
    region: REGION,
    timeoutSeconds: 15,
    memory: "256MiB",
  },
  async (request, response) => {
    try {
      if (request.method !== "POST") {
        response.status(405).json({ error: "Use POST." });
        return;
      }

      await verifyFirebaseAppCheck(request);

      const body = typeof request.body === "object" && request.body !== null ? request.body : {};
      const type = String(body.type || "").trim().toLowerCase();

      if (type !== "verification" && type !== "reset") {
        response.status(400).json({ error: "Invalid email type." });
        return;
      }

      const limit = type === "verification" ? VERIFICATION_DAILY_LIMIT : RESET_DAILY_LIMIT;
      const firestore = admin.firestore();
      const day = new Date().toISOString().slice(0, 10);
      const quotaRef = firestore.collection("emailDailyUsage").doc(`${type}_${day}`);

      const result = await firestore.runTransaction(async (transaction) => {
        const snapshot = await transaction.get(quotaRef);
        const current = snapshot.exists ? Number(snapshot.get("count") || 0) : 0;
        if (current >= limit) {
          throw httpError(429, `The daily limit for ${type} emails has been reached. Please try again tomorrow.`);
        }
        const next = current + 1;
        transaction.set(
          quotaRef,
          {
            type,
            day,
            count: next,
            limit,
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          },
          { merge: true }
        );
        return { success: true, remaining: Math.max(0, limit - next) };
      });

      response.status(200).json(result);
    } catch (error) {
      sendError(response, error, "Email quota check failed.");
    }
  }
);

exports.syncUserProfile = onRequest(
  {
    region: REGION,
    timeoutSeconds: 15,
    memory: "256MiB",
  },
  async (request, response) => {
    try {
      if (request.method !== "POST") {
        response.status(405).json({ error: "Use POST." });
        return;
      }

      const auth = await verifyFirebaseAuth(request);
      await verifyFirebaseAppCheck(request);

      const body = typeof request.body === "object" && request.body !== null ? request.body : {};
      const suppliedName = String(body.displayName || "").trim();
      const snapshot = await syncUserProfileDocument(auth.uid, suppliedName || undefined);
      const trialStartedAt = snapshot.get("trialStartedAt");

      response.status(200).json({
        success: true,
        uid: auth.uid,
        trialStartedAtMillis: timestampMillis(trialStartedAt),
      });
    } catch (error) {
      sendError(response, error, "Could not sync user profile.");
    }
  }
);

exports.verifyPlayPurchase = onRequest(
  {
    region: REGION,
    timeoutSeconds: 30,
    memory: "256MiB",
    serviceAccount: PLAY_BILLING_SERVICE_ACCOUNT,
  },
  async (request, response) => {
    try {
      if (request.method !== "POST") {
        response.status(405).json({ error: "Use POST." });
        return;
      }
      const auth = await verifyFirebaseAuth(request);
      await verifyFirebaseAppCheck(request);
      const body = typeof request.body === "object" && request.body !== null ? request.body : {};
      const productId = String(body.productId || "").trim();
      const purchaseToken = String(body.purchaseToken || "").trim();
      if (![LIFETIME_PRODUCT_ID, MONTHLY_PRODUCT_ID].includes(productId) || !purchaseToken) {
        throw httpError(400, "A valid Premium purchase token is required.");
      }
      response.status(200).json(await verifyAndStorePlayPurchase(auth.uid, productId, purchaseToken));
    } catch (error) {
      console.error("verifyPlayPurchase failed", error);
      sendError(response, error, "Google Play could not verify this purchase.");
    }
  }
);

exports.playBillingNotification = onMessagePublished(
  {
    topic: PLAY_BILLING_TOPIC,
    region: REGION,
    retry: true,
    serviceAccount: PLAY_BILLING_SERVICE_ACCOUNT,
  },
  async (event) => {
    const encoded = event.data?.message?.data;
    if (!encoded) return;
    const notification = JSON.parse(Buffer.from(encoded, "base64").toString("utf8"));
    console.info("Play billing notification received", {
      messageId: event.data.message.messageId,
      packageName: notification.packageName || null,
      eventTimeMillis: notification.eventTimeMillis || null,
      isTest: Boolean(notification.testNotification),
      oneTimeNotificationType: notification.oneTimeProductNotification?.notificationType || null,
      hasVoidedPurchase: Boolean(notification.voidedPurchaseNotification),
    });
    if (notification.packageName !== ANDROID_PACKAGE_NAME) return;
    if (notification.testNotification) return;
    const subscription = notification.subscriptionNotification;
    if (subscription?.purchaseToken) {
      await reconcilePlayPurchase(subscription.purchaseToken, MONTHLY_PRODUCT_ID);
      return;
    }
    const oneTime = notification.oneTimeProductNotification;
    if (oneTime?.sku === LIFETIME_PRODUCT_ID && oneTime.purchaseToken) {
      await reconcilePlayPurchase(oneTime.purchaseToken);
      return;
    }
    const voided = notification.voidedPurchaseNotification;
    if (voided && Number(voided.productType) === 1 && voided.purchaseToken) {
      await reconcilePlayPurchase(voided.purchaseToken, MONTHLY_PRODUCT_ID);
      return;
    }
    if (voided && Number(voided.productType) === 2 && voided.purchaseToken) {
      await markPlayPurchaseVoided(voided.purchaseToken, {
        orderId: voided.orderId || null,
        refundType: Number(voided.refundType || 0),
        source: "rtdn",
      });
    }
  }
);

exports.reconcileVoidedPlayPurchases = onSchedule(
  {
    schedule: "every 6 hours",
    region: REGION,
    timeZone: "Etc/UTC",
    timeoutSeconds: 300,
    memory: "256MiB",
    serviceAccount: PLAY_BILLING_SERVICE_ACCOUNT,
    retryCount: 3,
  },
  async () => {
    const { google } = require("googleapis");
    const auth = new google.auth.GoogleAuth({
      scopes: ["https://www.googleapis.com/auth/androidpublisher"],
    });
    const publisher = google.androidpublisher({ version: "v3", auth });
    let token;
    let processed = 0;
    do {
      const result = await publisher.purchases.voidedpurchases.list({
        packageName: ANDROID_PACKAGE_NAME,
        type: 0,
        maxResults: 1000,
        token,
      });
      for (const purchase of result.data.voidedPurchases || []) {
        if (!purchase.purchaseToken) continue;
        await markPlayPurchaseVoided(purchase.purchaseToken, {
          orderId: purchase.orderId || null,
          voidedReason: Number(purchase.voidedReason || 0),
          voidedSource: Number(purchase.voidedSource || 0),
          voidedTimeMillis: purchase.voidedTimeMillis || null,
          source: "voided-purchases-api",
        });
        processed += 1;
      }
      token = result.data.tokenPagination?.nextPageToken || undefined;
    } while (token);
    console.info("Voided Play purchase reconciliation complete", { processed });
  }
);

async function verifyAndStorePlayPurchase(uid, productId, purchaseToken) {
  if (productId === MONTHLY_PRODUCT_ID) return verifyAndStoreSubscription(uid, purchaseToken);
  const tokenHash = sha256(purchaseToken);
  const firestore = admin.firestore();
  const purchaseRef = firestore.collection("playPurchases").doc(tokenHash);
  const previous = await purchaseRef.get();
  if (previous.exists && previous.get("voidedAt")) {
    const assignedUid = previous.get("uid") || null;
    if (assignedUid && assignedUid !== uid) {
      return { active: false, productId, verified: true };
    }
    await markPlayPurchaseVoided(purchaseToken, { source: "verification-of-voided-token" });
    return { active: false, productId, verified: true };
  }

  const purchase = await getPlayPurchase(productId, purchaseToken);
  const expectedAccountId = sha256(uid);
  const suppliedAccountId = purchase.obfuscatedExternalAccountId || null;
  let active = Number(purchase.purchaseState) === 0;
  let acknowledgementError = null;
  if (active && Number(purchase.acknowledgementState) === 0) {
    try {
      await acknowledgePlayPurchase(productId, purchaseToken);
      purchase.acknowledgementState = 1;
    } catch (error) {
      acknowledgementError = {
        status: Number(error.code || error.status || error.statusCode || 0) || null,
        message: error.message || "Google Play acknowledgement failed.",
      };
      console.error("Verified purchase acknowledgement will be retried", {
        purchaseTokenHash: tokenHash,
        status: acknowledgementError.status,
      });
    }
  }

  const userRef = firestore.collection("users").doc(uid);
  await firestore.runTransaction(async (transaction) => {
    const existing = await transaction.get(purchaseRef);
    const assignedUid = existing.exists ? existing.get("uid") : null;
    if (existing.exists && existing.get("voidedAt")) active = false;
    if (assignedUid && assignedUid !== uid) {
      throw httpError(409, "This Google Play purchase is already assigned to another PocketCodedWeb account.");
    }
    if (!assignedUid && suppliedAccountId && suppliedAccountId !== expectedAccountId) {
      throw httpError(409, "This purchase was made for another PocketCodedWeb account.");
    }
    const userSnapshot = await transaction.get(userRef);
    const monthlyActive = timestampMillis(userSnapshot.get("monthlyExpiresAt")) > Date.now();
    const now = admin.firestore.FieldValue.serverTimestamp();
    transaction.set(purchaseRef, {
      uid,
      productId,
      packageName: ANDROID_PACKAGE_NAME,
      purchaseTokenHash: tokenHash,
      orderId: purchase.orderId || null,
      purchaseState: Number(purchase.purchaseState),
      consumptionState: Number(purchase.consumptionState),
      acknowledgementState: Number(purchase.acknowledgementState),
      acknowledgementPending: active && Number(purchase.acknowledgementState) === 0,
      acknowledgementLastErrorStatus: acknowledgementError?.status || null,
      purchasedAt: purchase.purchaseTimeMillis
        ? admin.firestore.Timestamp.fromMillis(Number(purchase.purchaseTimeMillis))
        : null,
      active,
      verifiedAt: now,
      updatedAt: now,
    }, { merge: true });
    transaction.set(userRef, {
      isPremium: active || monthlyActive,
      lifetimePremium: active,
      premiumProductId: productId,
      premiumPurchaseTokenHash: tokenHash,
      premiumVerifiedAt: now,
      premiumRevokedAt: active ? null : now,
    }, { merge: true });
  });
  return {
    active,
    productId,
    verified: true,
    acknowledgementPending: active && Number(purchase.acknowledgementState) === 0,
  };
}

async function verifyAndStoreSubscription(uid, purchaseToken) {
  const { google } = require("googleapis");
  const auth = new google.auth.GoogleAuth({ scopes: ["https://www.googleapis.com/auth/androidpublisher"] });
  const publisher = google.androidpublisher({ version: "v3", auth });
  const { data: purchase } = await publisher.purchases.subscriptionsv2.get({
    packageName: ANDROID_PACKAGE_NAME, token: purchaseToken,
  });
  const line = (purchase.lineItems || []).find(item =>
    item.productId === MONTHLY_PRODUCT_ID && item.offerDetails?.basePlanId === "monthly");
  if (!line) throw httpError(400, "This token does not contain the monthly Premium plan.");
  const expiryMillis = Date.parse(line.expiryTime || "") || 0;
  const active = ["SUBSCRIPTION_STATE_ACTIVE", "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
    "SUBSCRIPTION_STATE_CANCELED"].includes(purchase.subscriptionState) && expiryMillis > Date.now();
  const firestore = admin.firestore();
  const tokenHash = sha256(purchaseToken);
  const ref = firestore.collection("playPurchases").doc(tokenHash);
  const userRef = firestore.collection("users").doc(uid);
  await firestore.runTransaction(async transaction => {
    const existing = await transaction.get(ref);
    const user = await transaction.get(userRef);
    if (existing.get("uid") && existing.get("uid") !== uid)
      throw httpError(409, "This subscription belongs to another PocketCodedPy account.");
    if (!existing.get("uid") && purchase.externalAccountIdentifiers?.obfuscatedExternalAccountId !== sha256(uid))
      throw httpError(409, "This subscription was purchased for another PocketCodedPy account.");
    const lifetime = user.get("lifetimePremium") === true ||
      (user.get("isPremium") === true && user.get("premiumProductId") === LIFETIME_PRODUCT_ID && user.get("lifetimePremium") !== false);
    const now = admin.firestore.FieldValue.serverTimestamp();
    transaction.set(ref, { uid, productId: MONTHLY_PRODUCT_ID, purchaseToken,
      purchaseTokenHash: tokenHash, active, subscriptionState: purchase.subscriptionState,
      expiresAt: admin.firestore.Timestamp.fromMillis(expiryMillis), updatedAt: now }, { merge: true });
    // A late notification for an old token cannot revoke a newer subscription.
    const currentExpiry = timestampMillis(user.get("monthlyExpiresAt"));
    if (user.get("monthlyPurchaseTokenHash") === tokenHash || !user.get("monthlyPurchaseTokenHash") || (active && expiryMillis >= currentExpiry)) {
      transaction.set(userRef, { isPremium: lifetime || active, lifetimePremium: lifetime,
        monthlyPurchaseTokenHash: tokenHash,
        monthlyExpiresAt: admin.firestore.Timestamp.fromMillis(active ? expiryMillis : 0),
        premiumVerifiedAt: now }, { merge: true });
    }
  });
  if (active && purchase.acknowledgementState === "ACKNOWLEDGEMENT_STATE_PENDING") {
    await publisher.purchases.subscriptions.acknowledge({ packageName: ANDROID_PACKAGE_NAME,
      subscriptionId: MONTHLY_PRODUCT_ID, token: purchaseToken, requestBody: {} });
  }
  return { active, verified: true, productId: MONTHLY_PRODUCT_ID, expiresAtMillis: expiryMillis };
}

// Revalidate renewals and expiry even when a Play notification is missed.
exports.reconcileMonthlySubscriptions = onSchedule({ schedule: "every 60 minutes", region: REGION,
  timeoutSeconds: 300, serviceAccount: PLAY_BILLING_SERVICE_ACCOUNT }, async () => {
  const records = await admin.firestore().collection("playPurchases")
    .where("productId", "==", MONTHLY_PRODUCT_ID).get();
  for (const record of records.docs) {
    if (record.get("purchaseToken") && record.get("uid"))
      await reconcilePlayPurchase(record.get("purchaseToken"), MONTHLY_PRODUCT_ID);
  }
});

async function reconcilePlayPurchase(purchaseToken, productId = LIFETIME_PRODUCT_ID) {
  const purchaseRef = admin.firestore().collection("playPurchases").doc(sha256(purchaseToken));
  const existing = await purchaseRef.get();
  if (!existing.exists || !existing.get("uid")) return;
  try {
    await verifyAndStorePlayPurchase(existing.get("uid"), productId, purchaseToken);
  } catch (error) {
    const status = Number(error.code || error.status || error.statusCode || 0);
    if (status === 404 || status === 410) {
      if (productId === MONTHLY_PRODUCT_ID) {
        const userRef = admin.firestore().collection("users").doc(existing.get("uid"));
        await admin.firestore().runTransaction(async transaction => {
          const user = await transaction.get(userRef);
          if (user.get("monthlyPurchaseTokenHash") === sha256(purchaseToken)) transaction.set(userRef, {
            isPremium: user.get("lifetimePremium") === true,
            monthlyExpiresAt: admin.firestore.Timestamp.fromMillis(0),
          }, { merge: true });
          transaction.set(purchaseRef, { active: false }, { merge: true });
        });
      } else await markPlayPurchaseVoided(purchaseToken, { source: "play-api-not-found" });
    }
    else throw error;
  }
}

async function markPlayPurchaseVoided(purchaseToken, details = {}) {
  const tokenHash = sha256(purchaseToken);
  const firestore = admin.firestore();
  const purchaseRef = firestore.collection("playPurchases").doc(tokenHash);
  await firestore.runTransaction(async (transaction) => {
    const purchase = await transaction.get(purchaseRef);
    const now = admin.firestore.FieldValue.serverTimestamp();
    if (!purchase.exists || !purchase.get("uid")) {
      transaction.set(purchaseRef, {
        active: false,
        purchaseState: 1,
        purchaseTokenHash: tokenHash,
        productId: LIFETIME_PRODUCT_ID,
        packageName: ANDROID_PACKAGE_NAME,
        voidedAt: now,
        revokedAt: now,
        voidedOrderId: details.orderId || null,
        voidedReason: details.voidedReason ?? null,
        voidedSource: details.voidedSource ?? null,
        voidedTimeMillis: details.voidedTimeMillis || null,
        voidedDetectionSource: details.source || "unknown",
        updatedAt: now,
      }, { merge: true });
      return;
    }
    const uid = purchase.get("uid");
    const userRef = firestore.collection("users").doc(uid);
    const user = await transaction.get(userRef);
    transaction.set(purchaseRef, {
      active: false,
      purchaseState: 1,
      voidedAt: now,
      revokedAt: now,
      voidedOrderId: details.orderId || null,
      voidedReason: details.voidedReason ?? null,
      voidedSource: details.voidedSource ?? null,
      voidedTimeMillis: details.voidedTimeMillis || null,
      voidedDetectionSource: details.source || "unknown",
      updatedAt: now,
    }, { merge: true });
    if (user.get("premiumPurchaseTokenHash") === tokenHash) {
      transaction.set(userRef, {
        isPremium: timestampMillis(user.get("monthlyExpiresAt")) > Date.now(),
        lifetimePremium: false,
        premiumVerifiedAt: now,
        premiumRevokedAt: now,
      }, { merge: true });
    }
  });
}

async function getPlayPurchase(productId, purchaseToken) {
  const { google } = require("googleapis");
  const auth = new google.auth.GoogleAuth({
    scopes: ["https://www.googleapis.com/auth/androidpublisher"],
  });
  const publisher = google.androidpublisher({ version: "v3", auth });
  const result = await publisher.purchases.products.get({
    packageName: ANDROID_PACKAGE_NAME,
    productId,
    token: purchaseToken,
  });
  return result.data;
}

async function acknowledgePlayPurchase(productId, purchaseToken) {
  const { google } = require("googleapis");
  const auth = new google.auth.GoogleAuth({
    scopes: ["https://www.googleapis.com/auth/androidpublisher"],
  });
  const publisher = google.androidpublisher({ version: "v3", auth });
  await publisher.purchases.products.acknowledge({
    packageName: ANDROID_PACKAGE_NAME,
    productId,
    token: purchaseToken,
    requestBody: {},
  });
}

function sha256(value) {
  return crypto.createHash("sha256").update(String(value), "utf8").digest("hex");
}

async function verifyFirebaseAuth(request) {
  const authorization = request.get("Authorization") || "";
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  if (!match) {
    throw httpError(401, "Sign in before using this service.");
  }
  return admin.auth().verifyIdToken(match[1]);
}

async function verifyFirebaseAppCheck(request) {
  const token = request.get("X-Firebase-AppCheck");

  if (!token) {
    if (process.env.FUNCTIONS_EMULATOR === "true") return;
    throw httpError(401, "App verification is required.");
  }

  try {
    await admin.appCheck().verifyToken(token);
  } catch (error) {
    console.warn("App Check verification failed:", error.message);
    throw httpError(401, "App verification failed.");
  }
}

async function syncUserProfileDocument(uid, suppliedName) {
  const firestore = admin.firestore();
  const userRecord = await admin.auth().getUser(uid);
  const profileRef = firestore.collection("users").doc(uid);

  await firestore.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(profileRef);
    const profile = buildUserProfileData(userRecord, suppliedName);
    const existingTrialStart = snapshot.exists ? snapshot.get("trialStartedAt") : null;
    if (!existingTrialStart) {
      const trialStartedAt = authCreationTimestamp(userRecord);
      profile.trialStartedAt = trialStartedAt;
      const trialStartedAtMillis = timestampMillis(trialStartedAt) || Date.now();
      profile.trialStartedAtMillis = trialStartedAtMillis;
      profile.trialEndsAtMillis = trialStartedAtMillis + 72 * 60 * 60 * 1000;
    }
    transaction.set(profileRef, profile, { merge: true });
  });

  return profileRef.get();
}

function buildUserProfileData(userRecord, suppliedName) {
  const displayName = suppliedName ||
    userRecord.displayName ||
    emailName(userRecord.email) ||
    `${APP_NAME} user`;
  return {
    app: APP_NAME,
    uid: userRecord.uid,
    displayName,
    email: userRecord.email || null,
    emailVerified: userRecord.emailVerified === true,
    providers: Array.isArray(userRecord.providerData)
      ? userRecord.providerData.map((provider) => provider.providerId).filter(Boolean)
      : [],
    lastSignInAt: admin.firestore.FieldValue.serverTimestamp(),
    lastSyncedAt: admin.firestore.FieldValue.serverTimestamp(),
  };
}

function authCreationTimestamp(userRecord) {
  const creationTime = userRecord.metadata && userRecord.metadata.creationTime;
  const createdAt = creationTime ? new Date(creationTime) : null;
  if (createdAt && !Number.isNaN(createdAt.getTime())) {
    return admin.firestore.Timestamp.fromDate(createdAt);
  }
  return admin.firestore.Timestamp.now();
}

function timestampMillis(value) {
  return value && typeof value.toMillis === "function" ? value.toMillis() : null;
}

function emailName(email) {
  return typeof email === "string" && email.includes("@") ? email.split("@")[0] : null;
}

function sendError(response, error, fallback) {
  const status = Number(error.status || error.statusCode || 500);
  response.status(status >= 400 && status < 600 ? status : 500).json({
    error: error.publicMessage || error.message || fallback,
  });
}

function httpError(status, publicMessage) {
  const error = new Error(publicMessage);
  error.status = status;
  error.publicMessage = publicMessage;
  return error;
}
