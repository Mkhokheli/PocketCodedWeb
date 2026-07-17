"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { onMessagePublished } = require("firebase-functions/v2/pubsub");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const crypto = require("crypto");

admin.initializeApp();
const geminiApiKey = defineSecret("GEMINI_API_KEY");

const REGION = "us-central1";
const APP_NAME = "PocketCodedWeb";
const VERIFICATION_DAILY_LIMIT = 1000;
const RESET_DAILY_LIMIT = 150;
const ANDROID_PACKAGE_NAME = "com.mkhokheli.pocketcodedweb";
const LIFETIME_PRODUCT_ID = "pocketcodedweb_lifetime";
const PLAY_BILLING_TOPIC = "play-billing-events";
const DEFAULT_DAILY_LIMIT = 5;
const PREMIUM_DAILY_LIMIT = 50;
const GLOBAL_DAILY_LIMIT = 1000;
const MAX_PROMPT_CHARS = 90000;
const ALLOWED_MODELS = new Set([
  "gemini-3.5-flash",
  "gemini-3.1-flash-lite",
  "gemini-2.5-pro",
  "gemini-2.5-flash",
]);

exports.aiChat = onRequest(
  {
    region: REGION,
    timeoutSeconds: 90,
    memory: "512MiB",
    secrets: [geminiApiKey],
  },
  async (request, response) => {
    try {
      if (request.method !== "POST") {
        response.status(405).json({ error: "Use POST." });
        return;
      }
      const auth = await verifyFirebaseAuth(request);
      await verifyFirebaseAppCheck(request);
      if (!auth.email_verified) throw httpError(403, "Verify your email before using AI Chat.");
      const body = typeof request.body === "object" && request.body !== null ? request.body : {};
      const requestedModel = String(body.model || "").trim();
      const model = ALLOWED_MODELS.has(requestedModel) ? requestedModel : "gemini-3.5-flash";
      const prompt = String(body.prompt || "");
      if (!prompt.trim()) throw httpError(400, "Prompt is required.");
      if (prompt.length > MAX_PROMPT_CHARS) throw httpError(413, "AI context is too large.");
      const quota = await consumeDailyQuota(auth);
      const result = await callGemini(model, prompt);
      response.status(200).json({
        text: result.text,
        model,
        usage: result.usage,
        quota: { ...quota, remaining: Math.max(0, quota.limit - quota.used) },
      });
    } catch (error) {
      sendError(response, error, "AI request failed.");
    }
  }
);

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
      if (productId !== LIFETIME_PRODUCT_ID || !purchaseToken) {
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
  { topic: PLAY_BILLING_TOPIC, region: REGION, retry: true },
  async (event) => {
    const encoded = event.data?.message?.data;
    if (!encoded) return;
    const notification = JSON.parse(Buffer.from(encoded, "base64").toString("utf8"));
    if (notification.packageName !== ANDROID_PACKAGE_NAME || notification.testNotification) return;
    const oneTime = notification.oneTimeProductNotification;
    if (oneTime?.sku === LIFETIME_PRODUCT_ID && oneTime.purchaseToken) {
      await reconcilePlayPurchase(oneTime.purchaseToken);
      return;
    }
    const voided = notification.voidedPurchaseNotification;
    if (voided?.purchaseToken) {
      await markPlayPurchaseVoided(voided.purchaseToken, "rtdn");
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
    retryCount: 3,
  },
  async () => {
    const { google } = require("googleapis");
    const auth = new google.auth.GoogleAuth({
      scopes: ["https://www.googleapis.com/auth/androidpublisher"],
    });
    const publisher = google.androidpublisher({ version: "v3", auth });
    let token;
    do {
      const result = await publisher.purchases.voidedpurchases.list({
        packageName: ANDROID_PACKAGE_NAME,
        type: 0,
        maxResults: 1000,
        token,
      });
      for (const purchase of result.data.voidedPurchases || []) {
        if (purchase.purchaseToken) await markPlayPurchaseVoided(purchase.purchaseToken, "voided-api");
      }
      token = result.data.tokenPagination?.nextPageToken || undefined;
    } while (token);
  }
);

async function verifyAndStorePlayPurchase(uid, productId, purchaseToken) {
  const purchase = await getPlayPurchase(productId, purchaseToken);
  const tokenHash = sha256(purchaseToken);
  const expectedAccountId = sha256(uid);
  const suppliedAccountId = purchase.obfuscatedExternalAccountId || null;
  if (suppliedAccountId && suppliedAccountId !== expectedAccountId) {
    throw httpError(409, "This purchase was made for another PocketCodedWeb account.");
  }

  const active = Number(purchase.purchaseState) === 0;
  if (active && Number(purchase.acknowledgementState) === 0) {
    await acknowledgePlayPurchase(productId, purchaseToken);
    purchase.acknowledgementState = 1;
  }

  const firestore = admin.firestore();
  const purchaseRef = firestore.collection("playPurchases").doc(tokenHash);
  const userRef = firestore.collection("users").doc(uid);
  await firestore.runTransaction(async (transaction) => {
    const existing = await transaction.get(purchaseRef);
    const assignedUid = existing.exists ? existing.get("uid") : null;
    if (assignedUid && assignedUid !== uid) {
      throw httpError(409, "This Google Play purchase is already assigned to another PocketCodedWeb account.");
    }
    if (existing.exists && existing.get("voidedAt")) {
      throw httpError(410, "This Google Play purchase has been refunded or revoked.");
    }
    const now = admin.firestore.FieldValue.serverTimestamp();
    transaction.set(purchaseRef, {
      uid,
      productId,
      packageName: ANDROID_PACKAGE_NAME,
      purchaseTokenHash: tokenHash,
      orderId: purchase.orderId || null,
      purchaseState: Number(purchase.purchaseState),
      acknowledgementState: Number(purchase.acknowledgementState),
      purchasedAt: purchase.purchaseTimeMillis
        ? admin.firestore.Timestamp.fromMillis(Number(purchase.purchaseTimeMillis))
        : null,
      active,
      verifiedAt: now,
      updatedAt: now,
    }, { merge: true });
    transaction.set(userRef, {
      isPremium: active,
      premiumProductId: productId,
      premiumPurchaseTokenHash: tokenHash,
      premiumVerifiedAt: now,
      premiumRevokedAt: active ? null : now,
    }, { merge: true });
  });
  return { active, productId, verified: true };
}

async function reconcilePlayPurchase(purchaseToken) {
  const purchaseRef = admin.firestore().collection("playPurchases").doc(sha256(purchaseToken));
  const existing = await purchaseRef.get();
  if (!existing.exists || !existing.get("uid")) return;
  try {
    await verifyAndStorePlayPurchase(existing.get("uid"), LIFETIME_PRODUCT_ID, purchaseToken);
  } catch (error) {
    const status = Number(error.code || error.status || error.statusCode || 0);
    if (status === 404 || status === 410) await markPlayPurchaseVoided(purchaseToken, "play-api");
    else throw error;
  }
}

async function markPlayPurchaseVoided(purchaseToken, source) {
  const tokenHash = sha256(purchaseToken);
  const firestore = admin.firestore();
  const purchaseRef = firestore.collection("playPurchases").doc(tokenHash);
  await firestore.runTransaction(async (transaction) => {
    const purchase = await transaction.get(purchaseRef);
    const uid = purchase.exists ? purchase.get("uid") : null;
    const userRef = uid ? firestore.collection("users").doc(uid) : null;
    const user = userRef ? await transaction.get(userRef) : null;
    const now = admin.firestore.FieldValue.serverTimestamp();
    transaction.set(purchaseRef, {
      active: false,
      purchaseState: 1,
      voidedAt: now,
      revokedAt: now,
      voidedDetectionSource: source,
      updatedAt: now,
    }, { merge: true });
    if (userRef && user?.get("premiumPurchaseTokenHash") === tokenHash) {
      transaction.set(userRef, {
        isPremium: false,
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

async function consumeDailyQuota(auth) {
  const firestore = admin.firestore();
  const day = new Date().toISOString().slice(0, 10);
  const resetAt = `${day}T23:59:59.999Z`;
  const limit = await dailyLimitForUser(auth);
  const userRef = firestore.collection("aiDailyUsage").doc(`${auth.uid}_${day}`);
  const globalRef = firestore.collection("aiDailyUsage").doc(`GLOBAL_${day}`);
  return firestore.runTransaction(async (transaction) => {
    const globalSnapshot = await transaction.get(globalRef);
    const userSnapshot = await transaction.get(userRef);
    const globalCurrent = globalSnapshot.exists ? Number(globalSnapshot.get("count") || 0) : 0;
    const userCurrent = userSnapshot.exists ? Number(userSnapshot.get("count") || 0) : 0;
    if (globalCurrent >= GLOBAL_DAILY_LIMIT) {
      throw httpError(503, "The shared AI service is temporarily busy. Try again tomorrow or use your own API key.");
    }
    if (userCurrent >= limit) {
      throw httpError(429, `Daily AI limit reached (${limit} prompts).`);
    }
    const now = admin.firestore.FieldValue.serverTimestamp();
    transaction.set(userRef, {
      uid: auth.uid,
      day,
      count: userCurrent + 1,
      limit,
      resetAt,
      updatedAt: now,
    }, { merge: true });
    transaction.set(globalRef, {
      uid: "GLOBAL",
      day,
      count: globalCurrent + 1,
      limit: GLOBAL_DAILY_LIMIT,
      resetAt,
      updatedAt: now,
    }, { merge: true });
    return { limit, used: userCurrent + 1, resetAt };
  });
}

async function dailyLimitForUser(auth) {
  const firestore = admin.firestore();
  const [user, entitlement] = await Promise.all([
    firestore.collection("users").doc(auth.uid).get(),
    firestore.collection("aiEntitlements").doc(auth.uid).get(),
  ]);
  if (user.get("isPremium") === true || auth.premium === true || auth.aiPremium === true) {
    return PREMIUM_DAILY_LIMIT;
  }
  if (entitlement.exists) {
    const explicit = Number(entitlement.get("dailyLimit"));
    if (Number.isFinite(explicit) && explicit > 0) return Math.min(500, Math.floor(explicit));
    if (entitlement.get("premium") === true) return PREMIUM_DAILY_LIMIT;
  }
  return DEFAULT_DAILY_LIMIT;
}

async function callGemini(model, prompt) {
  const key = geminiApiKey.value();
  if (!key) throw httpError(500, "Gemini API key is not configured on the backend.");
  const url = new URL(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`);
  url.searchParams.set("key", key);
  const upstream = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      contents: [{ role: "user", parts: [{ text: prompt }] }],
      generationConfig: { temperature: 0.25, maxOutputTokens: 4096 },
    }),
  });
  const raw = await upstream.text();
  const data = raw ? JSON.parse(raw) : {};
  if (!upstream.ok) {
    throw httpError(upstream.status, data.error?.message || "Gemini request failed.");
  }
  const text = (data.candidates || [])
    .flatMap((candidate) => candidate.content?.parts || [])
    .map((part) => part.text || "")
    .join("")
    .trim();
  return { text: text || "I could not generate a response.", usage: data.usageMetadata || null };
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
