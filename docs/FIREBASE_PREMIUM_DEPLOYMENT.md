# PocketCodedWeb Firebase and Premium Deployment

This guide configures the production backend for Firebase project `pocketcodedweb-6887b`, Android
package `com.mkhokheli.pocketcodedweb`, and one-time product `pocketcodedweb_lifetime`. Run commands
from the repository root in PowerShell unless a step says otherwise. Never commit a service-account
key or paste a purchase token into logs, screenshots, or support messages.

## 1. Install and select the tooling

Install Node.js 22, Java 17 or newer, Android Studio, Google Cloud CLI, and Firebase CLI. Cloud
Functions deployment requires the Firebase Blaze plan.

```powershell
node --version
npm.cmd --version
gcloud version
npx.cmd firebase-tools --version
npx.cmd firebase-tools login
npx.cmd firebase-tools use pocketcodedweb-6887b
npx.cmd firebase-tools projects:list
```

Confirm `.firebaserc` selects `pocketcodedweb-6887b`. Every command below also names the project
explicitly so an accidentally selected default cannot deploy to PocketCodedPy.

## 2. Verify the Android Firebase app

1. Open Firebase Console, select **PocketCodedWeb**, then **Project settings > General**.
2. Confirm the Android app package is exactly `com.mkhokheli.pocketcodedweb`.
3. In Play Console, open **Setup > App integrity > App signing** and copy the SHA-1 and SHA-256 for
   the Play App Signing certificate.
4. Add those fingerprints to the Firebase Android app. Also add the upload/release certificate and
   the local debug certificate when those builds use Google sign-in.
5. Keep `app/google-services.json` if Firebase still shows project ID `pocketcodedweb-6887b` and the
   expected package. Download a fresh file only after changing fingerprints or OAuth clients, and
   replace only `app/google-services.json`.

To inspect the local debug fingerprints:

```powershell
./gradlew.bat signingReport
```

Do not copy `google-services (2).json`; that file belongs to PocketCodedPy.

## 3. Configure Authentication and Firestore

In **Firebase Console > Authentication > Sign-in method**, enable **Email/Password** and **Google**.
Choose the support email for Google sign-in. Under Authentication settings, verify the authorized
domains used by the app's policy/hosting pages.

Create the default Cloud Firestore database if it does not exist. Choose the region intentionally;
it cannot be changed in place later. The Functions are configured for `us-central1`.

Install dependencies and run the rules emulator before deploying. The CLI rules file overwrites the
rules currently published in Firebase Console.

```powershell
Set-Location functions
npm.cmd ci
npm.cmd run lint
npm.cmd run test:rules
Set-Location ..
npx.cmd firebase-tools deploy --project pocketcodedweb-6887b --only firestore:rules
```

The rules allow users to read their own profile and update approved profile/trial metadata. They
deny all client access to `playPurchases`, quotas, and entitlement fields.

## 4. Create the dedicated billing service account

The deployed billing functions run as:

`play-billing-backend@pocketcodedweb-6887b.iam.gserviceaccount.com`

Create it once and enable the required APIs:

```powershell
gcloud config set project pocketcodedweb-6887b
gcloud services enable androidpublisher.googleapis.com cloudfunctions.googleapis.com run.googleapis.com firestore.googleapis.com pubsub.googleapis.com cloudscheduler.googleapis.com artifactregistry.googleapis.com --project pocketcodedweb-6887b
gcloud iam service-accounts create play-billing-backend --project pocketcodedweb-6887b --display-name "PocketCodedWeb Play Billing backend"
```

Grant the runtime only the project roles it needs. Firestore access is required for entitlement and
purchase records; logs are required for operational diagnostics.

```powershell
gcloud projects add-iam-policy-binding pocketcodedweb-6887b --member "serviceAccount:play-billing-backend@pocketcodedweb-6887b.iam.gserviceaccount.com" --role "roles/datastore.user"
gcloud projects add-iam-policy-binding pocketcodedweb-6887b --member "serviceAccount:play-billing-backend@pocketcodedweb-6887b.iam.gserviceaccount.com" --role "roles/logging.logWriter"
gcloud projects add-iam-policy-binding pocketcodedweb-6887b --member "serviceAccount:play-billing-backend@pocketcodedweb-6887b.iam.gserviceaccount.com" --role "roles/eventarc.eventReceiver"
gcloud projects add-iam-policy-binding pocketcodedweb-6887b --member "serviceAccount:play-billing-backend@pocketcodedweb-6887b.iam.gserviceaccount.com" --role "roles/pubsub.subscriber"
```

The account performing the deployment must be allowed to attach this service account. If deployment
reports `iam.serviceAccounts.actAs`, an owner can grant the deployer **Service Account User** on this
specific account.

In Play Console, open **Users and permissions** (or **Setup > API access**, depending on the console
layout), invite/link the service-account email, give it access to PocketCodedWeb, and grant the
minimum order/subscription permissions that allow viewing orders, purchases, and financial data.
Do not create or download a JSON key: deployed Functions use their attached runtime identity.

Play API permission changes can take time to propagate. A `401` usually means invalid credentials;
`403` usually means missing Play Console permissions, missing `actAs`, or a disabled API; an
API-not-enabled message identifies the API that must be enabled in `pocketcodedweb-6887b`.

## 5. Configure Pub/Sub real-time developer notifications

Create the topic and allow Google Play to publish to it:

```powershell
gcloud pubsub topics create play-billing-events --project pocketcodedweb-6887b
gcloud pubsub topics add-iam-policy-binding play-billing-events --project pocketcodedweb-6887b --member "serviceAccount:google-play-developer-notifications@system.gserviceaccount.com" --role "roles/pubsub.publisher"
gcloud pubsub topics get-iam-policy play-billing-events --project pocketcodedweb-6887b
```

If creation reports that the topic already exists, continue with the IAM command. In Play Console,
open PocketCodedWeb and go to **Monetize > Monetization setup > Real-time developer notifications**.
Enable notifications and enter:

`projects/pocketcodedweb-6887b/topics/play-billing-events`

Select **subscriptions and all one-time products**, save, then click **Send test notification**.
The test confirms delivery configuration; it does not create or revoke an entitlement.

## 6. Deploy Functions

AI Chat uses keys supplied by users and does not require a Firebase secret. Never set a Play
service-account key as a secret; the billing Functions use their attached runtime identity.

```powershell
Set-Location functions
npm.cmd ci
npm.cmd run lint
Set-Location ..
```

Deploy in small groups so permission failures are easy to identify:

```powershell
npx.cmd firebase-tools deploy --project pocketcodedweb-6887b --only "functions:syncUserProfile,functions:checkEmailQuota"
npx.cmd firebase-tools deploy --project pocketcodedweb-6887b --only "functions:verifyPlayPurchase,functions:playBillingNotification"
npx.cmd firebase-tools deploy --project pocketcodedweb-6887b --only "functions:reconcileVoidedPlayPurchases"
npx.cmd firebase-tools deploy --project pocketcodedweb-6887b --only functions
```

The final command confirms the complete source deploys as one codebase. Node.js 22 is selected by
`functions/package.json`. Configure an Artifact Registry cleanup policy if Firebase prompts for one.

## 7. Verify the Play product and test release

In **Play Console > Monetize with Play > Products > One-time products**, open
`pocketcodedweb_lifetime` and verify that it is active, non-consumable, has an active purchase option,
and is available in every intended country. Keep its existing base price. The Android paywall shows
Google Play's localized `formattedPrice`; it performs no currency conversion and has no hard-coded
price fallback.

Upload a signed AAB containing this implementation to an internal or closed track. Add tester Gmail
accounts both to that track and under **Settings > License testing**. Install from the tester opt-in
link. A sideloaded APK is not a reliable billing test.

Test purchase success, cancellation, pending payment, restore, reinstall/clear-data restore, a second
Firebase account, and a refund/revoke. Verify that the trial starts once, prompts daily, expires at
14 days, and never restarts after reinstall or device change.

## 8. Configure App Check

In Play Console **App integrity > Play Integrity API**, link `pocketcodedweb-6887b`. In Firebase
Console **App Check**, register `com.mkhokheli.pocketcodedweb` with the Play App Signing SHA-256.

Debug builds use Firebase's debug provider. Launch a debug build, copy its App Check debug token from
Logcat, and register that token in **App Check > Apps > Manage debug tokens**. Never ship the debug
provider in release builds.

First monitor valid/invalid request metrics. Once internal-track release requests are valid, enable
enforcement for Cloud Firestore and Cloud Functions. Test sign-in, profile synchronization, purchase
verification, restore, and AI Chat again after enforcement.

## 9. Operational verification

```powershell
npx.cmd firebase-tools functions:list --project pocketcodedweb-6887b
npx.cmd firebase-tools functions:log --project pocketcodedweb-6887b --only verifyPlayPurchase
npx.cmd firebase-tools functions:log --project pocketcodedweb-6887b --only playBillingNotification
gcloud pubsub topics get-iam-policy play-billing-events --project pocketcodedweb-6887b
gcloud scheduler jobs list --project pocketcodedweb-6887b --location us-central1
gcloud functions list --gen2 --project pocketcodedweb-6887b --regions us-central1
```

Use Play Console's **Send test notification**, then check `playBillingNotification` logs. To run the
scheduled reconciliation immediately, find the generated scheduler job with the list command and run:

```powershell
gcloud scheduler jobs run JOB_NAME --project pocketcodedweb-6887b --location us-central1
```

Inspect `users/{uid}` and `playPurchases/{tokenHash}` in Firestore Console. Confirm premium state,
verification timestamps, token hashes, and revocation metadata. Raw purchase tokens must never be
stored. Configure log-based alerts for repeated Function errors and a Firebase/Cloud billing budget.

## 10. Safe rollback

Keep the previous known-good commit or release tag. To roll back code, check out that source in a
separate clean worktree, install its locked dependencies, and redeploy only the affected functions.
Do not delete Firestore purchase records or weaken rules during rollback.

```powershell
npx.cmd firebase-tools deploy --project pocketcodedweb-6887b --only "functions:verifyPlayPurchase,functions:playBillingNotification"
```

If a newly introduced function must be removed, first confirm no Android release calls it and no Play
RTDN configuration targets it, then use `firebase functions:delete FUNCTION_NAME --region
us-central1 --project pocketcodedweb-6887b`. Function deletion is intentionally not part of the
normal deployment commands.
