"use strict";
const { test } = require("node:test");
const assert = require("node:assert/strict");
const vm = require("node:vm");
const fs = require("node:fs");
const crypto = require("node:crypto");
const hash = value => crypto.createHash("sha256").update(value).digest("hex");
function harness({ state = "SUBSCRIPTION_STATE_ACTIVE", expired = false, lifetime = false,
  owner = "user", existingOwner, newer = false } = {}) {
  const records = new Map();
  const token = "token";
  records.set("users/user", { isPremium: lifetime || newer, lifetimePremium: lifetime,
    monthlyPurchaseTokenHash: newer ? "new-token" : undefined,
    monthlyExpiresAt: newer ? { toMillis: () => Date.now() + 9999999 } : undefined });
  if (existingOwner) records.set(`playPurchases/${hash(token)}`, { uid: existingOwner });
  const snapshot = key => ({ exists: records.has(key), get: field => records.get(key)?.[field] });
  const db = { collection: name => ({ doc: id => ({ key: `${name}/${id}` }) }),
    runTransaction: async callback => {
      const writes = [];
      await callback({ get: async ref => snapshot(ref.key),
        set: (ref, data) => writes.push([ref.key, data]) });
      for (const [key, data] of writes) records.set(key, { ...records.get(key), ...data });
    } };
  const firestore = () => db;
  firestore.Timestamp = { fromMillis: value => ({ toMillis: () => value }) };
  firestore.FieldValue = { serverTimestamp: () => "now" };
  let acknowledged = false;
  const publisher = { purchases: { subscriptionsv2: { get: async () => ({ data: {
    subscriptionState: state, acknowledgementState: "ACKNOWLEDGEMENT_STATE_PENDING",
    externalAccountIdentifiers: { obfuscatedExternalAccountId: hash(owner) },
    lineItems: [{ productId: "pocketcodedweb_premium_monthly", offerDetails: { basePlanId: "monthly" },
      expiryTime: new Date(Date.now() + (expired ? -10000 : 100000)).toISOString() }],
  } }) }, subscriptions: { acknowledge: async () => { acknowledged = true; } } } };
  const context = { exports: {}, console, Buffer, process, require: name => {
    if (name === "crypto") return crypto;
    if (name === "firebase-admin") return { initializeApp() {}, firestore };
    if (name === "googleapis") return { google: { auth: { GoogleAuth: class {} }, androidpublisher: () => publisher } };
    if (name.endsWith("params")) return { defineSecret: () => ({}) };
    return { onRequest: (_, fn) => fn, onSchedule: (_, fn) => fn, onMessagePublished: (_, fn) => fn };
  } };
  vm.createContext(context);
  vm.runInContext(fs.readFileSync(__dirname + "/index.js", "utf8"), context);
  return { run: () => context.verifyAndStoreSubscription("user", token),
    user: () => records.get("users/user"), acknowledged: () => acknowledged,
    revokeLifetime: async () => {
      const token = "lifetime-token";
      records.set(`playPurchases/${hash(token)}`, { uid: "user", active: true });
      records.get("users/user").premiumPurchaseTokenHash = hash(token);
      await context.markPlayPurchaseVoided(token);
    } };
}
for (const state of ["SUBSCRIPTION_STATE_ACTIVE", "SUBSCRIPTION_STATE_IN_GRACE_PERIOD", "SUBSCRIPTION_STATE_CANCELED"]) {
  test(state + " retains paid access", async () => {
    const h = harness({ state }); assert.equal((await h.run()).active, true);
    assert.equal(h.user().isPremium, true); assert.equal(h.acknowledged(), true);
  });
}
for (const state of ["SUBSCRIPTION_STATE_ON_HOLD", "SUBSCRIPTION_STATE_PAUSED", "SUBSCRIPTION_STATE_EXPIRED", "SUBSCRIPTION_STATE_PENDING"]) {
  test(state + " grants no monthly access", async () => {
    const h = harness({ state }); assert.equal((await h.run()).active, false);
    assert.equal(h.user().isPremium, false); assert.equal(h.acknowledged(), false);
  });
}
test("expired paid period grants no access", async () => {
  const h = harness({ expired: true }); assert.equal((await h.run()).active, false);
});
test("expiry preserves lifetime", async () => {
  const h = harness({ expired: true, lifetime: true }); await h.run(); assert.equal(h.user().isPremium, true);
});
test("old expired token preserves newer subscription", async () => {
  const h = harness({ expired: true, newer: true }); await h.run(); assert.equal(h.user().isPremium, true);
});
test("rejects mismatched Play account", async () => {
  await assert.rejects(harness({ owner: "other" }).run());
});
test("rejects token assigned to another user", async () => {
  await assert.rejects(harness({ existingOwner: "other" }).run());
});

test("lifetime refund preserves a separately active monthly subscription", async () => {
  const h = harness({ lifetime: true, newer: true });
  await h.revokeLifetime();
  assert.equal(h.user().isPremium, true);
  assert.equal(h.user().lifetimePremium, false);
});
test("lifetime refund revokes access when there is no monthly subscription", async () => {
  const h = harness({ lifetime: true });
  await h.revokeLifetime();
  assert.equal(h.user().isPremium, false);
  assert.equal(h.user().lifetimePremium, false);
});
