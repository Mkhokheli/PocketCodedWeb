# PocketCodedWeb Firebase Functions

For the complete Windows/PowerShell deployment walkthrough, including Firebase Authentication,
Firestore, the dedicated Play Billing service account, Play Developer API access, RTDN, App Check,
verification, troubleshooting, and rollback, see
[`docs/FIREBASE_PREMIUM_DEPLOYMENT.md`](../docs/FIREBASE_PREMIUM_DEPLOYMENT.md).

This Functions codebase belongs to the Firebase project `pocketcodedweb-6887b`.

## Functions

- `syncUserProfile`: verifies Firebase Auth, creates/updates `users/{uid}`, and initializes the 14-day trial timestamp once.
- `checkEmailQuota`: tracks daily verification/reset-email quota counters in `emailDailyUsage`.
- `verifyPlayPurchase`: verifies and acknowledges one-time Premium purchases with Google Play.
- `playBillingNotification`: handles Google Play real-time developer notifications.
- `reconcileVoidedPlayPurchases`: revokes refunded/voided entitlements every six hours.

## Setup

Install dependencies from the `functions` directory:

```bash
cd functions
npm install
```

Run this in the same shell environment you use for `firebase deploy`. If you deploy from WSL, run
`npm install` from WSL so `functions/node_modules` exists where the Firebase CLI is looking.
Use Node 22 in that shell because `functions/package.json` targets the Node.js 22 Functions runtime.

Deploy Firestore rules and Functions:

```bash
firebase deploy --only firestore:rules,functions
```

App Check is enforced outside the Functions emulator. Register Play Integrity in Firebase App Check and register your debug token for local builds.

AI Chat is bring-your-own-key and calls the selected provider directly from the Android app. This
Functions codebase does not store or require a shared Gemini key.

Grant the Functions runtime service account Android Publisher API access in Play Console, create the
`play-billing-events` Pub/Sub topic, and select that topic under Play Console real-time developer notifications.

## Deploy Troubleshooting

If deploy fails with `Cannot determine backend specification. Timeout after 10000`, the Firebase CLI timed out while locally discovering the Functions manifest. This can happen when deploying from WSL against a project on `/mnt/c`.

```bash
cd /mnt/c/Users/User-PC/AndroidStudioProjects/PocketCodedWeb
nvm install 22
nvm use 22
cd functions
npm install
npm run lint
cd ..
FUNCTIONS_DISCOVERY_TIMEOUT=60 firebase deploy --only functions
```
