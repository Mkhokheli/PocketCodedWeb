"use strict";

const fs = require("fs");
const path = require("path");
const { after, before, beforeEach, test } = require("node:test");
const {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} = require("@firebase/rules-unit-testing");
const {
  doc,
  getDoc,
  serverTimestamp,
  setDoc,
  updateDoc,
} = require("firebase/firestore");

let environment;

before(async () => {
  environment = await initializeTestEnvironment({
    projectId: "demo-pocketcodedweb",
    firestore: {
      rules: fs.readFileSync(path.join(__dirname, "..", "..", "firestore.rules"), "utf8"),
    },
  });
});

beforeEach(async () => environment.clearFirestore());
after(async () => environment.cleanup());

async function seedProfile(uid, data = {}) {
  await environment.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "users", uid), {
      uid,
      displayName: "Tester",
      trialStartedAt: new Date("2026-01-01T00:00:00Z"),
      isPremium: false,
      ...data,
    });
  });
}

test("an authenticated user can read only their own profile", async () => {
  await seedProfile("alice");
  await assertSucceeds(getDoc(doc(environment.authenticatedContext("alice").firestore(), "users", "alice")));
  await assertFails(getDoc(doc(environment.authenticatedContext("bob").firestore(), "users", "alice")));
  await assertFails(getDoc(doc(environment.unauthenticatedContext().firestore(), "users", "alice")));
});

test("a client can initialize trial time once with server time", async () => {
  const firestore = environment.authenticatedContext("alice").firestore();
  await assertSucceeds(setDoc(doc(firestore, "users", "alice"), {
    uid: "alice",
    displayName: "Alice",
    trialStartedAt: serverTimestamp(),
  }));
});

test("trial start is immutable after profile creation", async () => {
  await seedProfile("alice");
  const reference = doc(environment.authenticatedContext("alice").firestore(), "users", "alice");
  await assertFails(updateDoc(reference, { trialStartedAt: serverTimestamp() }));
});

test("clients cannot create or change premium entitlement fields", async () => {
  const firestore = environment.authenticatedContext("alice").firestore();
  const reference = doc(firestore, "users", "alice");
  await assertFails(setDoc(reference, {
    uid: "alice",
    displayName: "Alice",
    isPremium: true,
  }));
  await seedProfile("alice");
  await assertFails(updateDoc(reference, { isPremium: true }));
  await assertFails(updateDoc(reference, { premiumPurchaseTokenHash: "forged" }));
  await assertFails(updateDoc(reference, { lifetimePremium: true }));
  await assertFails(updateDoc(reference, { monthlyExpiresAt: new Date("2099-01-01") }));
  await assertFails(updateDoc(reference, { monthlyPurchaseTokenHash: "forged" }));
});

test("clients have no access to purchase records", async () => {
  const reference = doc(environment.authenticatedContext("alice").firestore(), "playPurchases", "token-hash");
  await assertFails(getDoc(reference));
  await assertFails(setDoc(reference, { uid: "alice", active: true }));
});
