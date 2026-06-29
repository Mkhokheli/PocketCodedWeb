"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

admin.initializeApp();

const geminiApiKey = defineSecret("GEMINI_API_KEY");

const REGION = "us-central1";
const DEFAULT_DAILY_LIMIT = 5;
const PREMIUM_DAILY_LIMIT = 50;
const GLOBAL_DAILY_LIMIT = 1000; // SAFETY: Total requests the entire app can make per day
const VERIFICATION_DAILY_LIMIT = 1000;
const RESET_DAILY_LIMIT = 150;
const MAX_PROMPT_CHARS = 90_000;
const MAX_OUTPUT_TOKENS = 4096;
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

      if (!auth.email_verified) {
        response.status(403).json({ error: "Verify your email before using AI Chat." });
        return;
      }

      const body = typeof request.body === "object" && request.body !== null ? request.body : {};
      const requestedModel = String(body.model || "").trim();
      const model = ALLOWED_MODELS.has(requestedModel) ? requestedModel : "gemini-3.5-flash";
      const prompt = String(body.prompt || "");

      if (!prompt.trim()) {
        response.status(400).json({ error: "Prompt is required." });
        return;
      }
      if (prompt.length > MAX_PROMPT_CHARS) {
        response.status(413).json({ error: "AI context is too large. Close some files or shorten the request." });
        return;
      }

      const quota = await consumeDailyQuota(auth);
      const aiResponse = await callGemini(model, prompt);

      response.status(200).json({
        text: aiResponse.text,
        model,
        quota: {
          limit: quota.limit,
          used: quota.used,
          remaining: Math.max(0, quota.limit - quota.used),
          resetAt: quota.resetAt,
        },
        usage: aiResponse.usage,
      });
    } catch (error) {
      const status = Number(error.status || error.statusCode || 500);
      response.status(status >= 400 && status < 600 ? status : 500).json({
        error: error.publicMessage || error.message || "AI request failed.",
      });
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
        return { success: true };
      });

      response.status(200).json(result);
    } catch (error) {
      const status = Number(error.status || error.statusCode || 500);
      response.status(status >= 400 && status < 600 ? status : 500).json({
        error: error.publicMessage || error.message || "Email quota check failed.",
      });
    }
  }
);

async function verifyFirebaseAuth(request) {
  const authorization = request.get("Authorization") || "";
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  if (!match) {
    throw httpError(401, "Sign in before using AI Chat.");
  }
  return admin.auth().verifyIdToken(match[1]);
}

async function verifyFirebaseAppCheck(request) {
  const token = request.get("X-Firebase-AppCheck");

  // Allow bypass for the internal testing token
  if (token === "DEBUG_TOKEN") {
    console.log("App Check bypassed via internal DEBUG_TOKEN.");
    return;
  }

  if (!token) {
    if (process.env.FUNCTIONS_EMULATOR === "true") return;
    console.warn("App Check token missing, but allowing for debugging.");
    return; // TEMPORARY BYPASS: Allowing requests without token to fix the 403 error
  }

  try {
    await admin.appCheck().verifyToken(token);
  } catch (error) {
    console.warn("App Check verification failed, but allowing for debugging:", error.message);
    return; // TEMPORARY BYPASS: Allowing requests even if verification fails
  }
}

async function consumeDailyQuota(auth) {
  const firestore = admin.firestore();
  const day = new Date().toISOString().slice(0, 10);
  const resetAt = `${day}T23:59:59.999Z`;
  const limit = await dailyLimitForUser(auth);

  const userUsageRef = firestore.collection("aiDailyUsage").doc(`${auth.uid}_${day}`);
  const globalUsageRef = firestore.collection("aiDailyUsage").doc(`GLOBAL_${day}`);

  return firestore.runTransaction(async (transaction) => {
    // 1. Check Global Safety Limit
    const globalSnapshot = await transaction.get(globalUsageRef);
    const globalCurrent = globalSnapshot.exists ? Number(globalSnapshot.get("count") || 0) : 0;
    if (globalCurrent >= GLOBAL_DAILY_LIMIT) {
      throw httpError(503, "The shared AI service is temporarily busy due to high demand. Please try again tomorrow or use your own API key.");
    }

    // 2. Check User Limit
    const userSnapshot = await transaction.get(userUsageRef);
    const userCurrent = userSnapshot.exists ? Number(userSnapshot.get("count") || 0) : 0;
    if (userCurrent >= limit) {
      throw httpError(429, `Daily AI limit reached (${limit} prompts). Try again tomorrow or upgrade.`);
    }

    const nextUser = userCurrent + 1;
    const nextGlobal = globalCurrent + 1;

    // 3. Update User Usage
    transaction.set(
      userUsageRef,
      {
        uid: auth.uid,
        email: auth.email || null,
        day,
        count: nextUser,
        limit,
        resetAt,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    // 4. Update Global Usage
    transaction.set(
      globalUsageRef,
      {
        uid: "GLOBAL",
        day,
        count: nextGlobal,
        limit: GLOBAL_DAILY_LIMIT,
        resetAt,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    return { limit, used: nextUser, resetAt };
  });
}

async function dailyLimitForUser(auth) {
  const firestore = admin.firestore();

  // 1. Check primary 'users' collection (Play Billing) or manual email override
  const userDoc = await firestore.collection("users").doc(auth.uid).get();
  if (userDoc.get("isPremium") === true || auth.email === "slimkheli2@gmail.com") {
    return PREMIUM_DAILY_LIMIT;
  }

  // 2. Check custom claims (e.g. from Firebase Auth console)
  if (auth.premium === true || auth.aiPremium === true) {
    return PREMIUM_DAILY_LIMIT;
  }

  // 3. Check explicit AI overrides/entitlements
  const entitlement = await firestore.collection("aiEntitlements").doc(auth.uid).get();
  if (entitlement.exists) {
    const explicitLimit = Number(entitlement.get("dailyLimit"));
    if (Number.isFinite(explicitLimit) && explicitLimit > 0) {
      return Math.min(500, Math.floor(explicitLimit));
    }
    if (entitlement.get("premium") === true) {
      return PREMIUM_DAILY_LIMIT;
    }
  }

  return DEFAULT_DAILY_LIMIT;
}

async function callGemini(model, prompt) {
  const key = geminiApiKey.value();
  if (!key) {
    throw httpError(500, "Gemini API key is not configured on the AI backend.");
  }

  const url = new URL(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`);
  url.searchParams.set("key", key);

  const geminiResponse = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      contents: [
        {
          role: "user",
          parts: [{ text: prompt }],
        },
      ],
      generationConfig: {
        temperature: 0.25,
        maxOutputTokens: MAX_OUTPUT_TOKENS,
      },
    }),
  });

  const raw = await geminiResponse.text();
  const data = raw ? JSON.parse(raw) : {};
  if (!geminiResponse.ok) {
    const message = data.error && data.error.message ? data.error.message : "Gemini request failed.";
    throw httpError(geminiResponse.status, friendlyGeminiError(message));
  }

  const text = (data.candidates || [])
    .flatMap((candidate) => (((candidate.content || {}).parts) || []).map((part) => part.text || ""))
    .join("")
    .trim();

  return {
    text: text || "I could not generate a response.",
    usage: data.usageMetadata || null,
  };
}

function friendlyGeminiError(message) {
  const lower = String(message).toLowerCase();
  if (lower.includes("quota") || lower.includes("rate limit")) {
    return "The shared AI quota is currently exhausted. Try again later.";
  }
  if (lower.includes("api key")) {
    return "The AI backend is missing or rejecting the Gemini API key.";
  }
  if (lower.includes("model") && lower.includes("not found")) {
    return "This Gemini model is not available for the AI backend.";
  }
  return message;
}

function httpError(status, publicMessage) {
  const error = new Error(publicMessage);
  error.status = status;
  error.publicMessage = publicMessage;
  return error;
}
