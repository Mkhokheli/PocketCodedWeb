# PocketCodedWeb Firebase Functions

This Functions codebase belongs to the Firebase project `pocketcodedweb-6887b`.

## Functions

- `syncUserProfile`: verifies Firebase Auth, creates/updates `users/{uid}`, and initializes the 72 hour trial timestamp once.
- `checkEmailQuota`: tracks daily verification/reset-email quota counters in `emailDailyUsage`.
- `aiChat`: authenticated Gemini proxy with free, Premium, entitlement override, and global daily quotas.
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

Configure shared AI Chat once before deploying:

```bash
firebase functions:secrets:set GEMINI_API_KEY
```

Users may still supply a personal Gemini key; otherwise the app uses the quota-controlled backend.

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
