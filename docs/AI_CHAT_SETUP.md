# PocketCodedWeb AI Chat Setup

PocketCodedWeb includes an AI Chat screen backed by a Firebase Cloud Function quota proxy and
Gemini models. The Android app does not call Gemini directly; it sends an authenticated request to
the `aiChat` backend, and the backend enforces per-user limits before spending API quota.

## What the app does

- Adds an AI Chat fullscreen screen inside the IDE.
- Requires a signed-in, verified Firebase user before AI requests are enabled.
- Shows the selected model before each request.
- Sends a compact project context: active editor file, visible small Python files, project tree, and recent terminal output.
- Parses assistant suggestions into explicit approval actions:
  - `terminal` fenced block: show a **Run** command button.
  - `replace-active-file` fenced block: show a **Replace** active file button.
  - `PocketCodedWeb-actions` JSON block: show an **Apply** button for project file writes/deletes.
- Never runs terminal commands or overwrites code without a user tap.

## Copilot-style file edits

When the user asks AI Chat to create or update code, the model should return normal explanation text
plus a fenced JSON block:

```PocketCodedWeb-actions
[
  {
    "action": "write_file",
    "path": "main.py",
    "content": "print(\"Hello\")\n"
  },
  {
    "action": "delete_file",
    "path": "old_file.py"
  }
]
```

Supported actions:

- `write_file`: create or replace a file with complete final content.
- `delete_file`: delete a file.

The Android app validates every path before applying it. It rejects absolute paths, `..`,
metadata folders like `.git` and `.ssh`, virtual environment internals, folders, and files outside
the current project. The user must approve the suggested change before the app writes anything.

## Firebase / Google setup

1. In Firebase Console, keep Firebase Authentication enabled for **Email/Password** and **Google**.
2. In Firebase Console, register App Check for Android package `com.mkhokheli.PocketCodedWeb`.
3. Enable the Gemini Developer API for the Google/Firebase project.
4. Create a Gemini API key for the project.
5. Install the Firebase CLI and sign in:

   ```powershell
   npm install -g firebase-tools
   firebase login
   ```

6. Store the Gemini key as a Cloud Functions secret:

   ```powershell
   firebase functions:secrets:set GEMINI_API_KEY
   ```

7. Deploy the AI backend:

   ```powershell
   firebase deploy --only functions
   ```

8. Rebuild and install the Android app.

If the app shows an error mentioning `firebasevertexai.googleapis.com`, the AI Logic setup workflow
has not finished yet. Open:

`https://console.firebase.google.com/project/PocketCodedWeb/ailogic`

Then click **Get started** and choose the Gemini Developer API.

The Android app currently calls:

`https://us-central1-PocketCodedWeb.cloudfunctions.net/aiChat`

If the Firebase project ID or region changes, update `AI_CHAT_FUNCTION_URL` in
`app/src/main/java/com/mkhokheli/PocketCodedWeb/MainActivity.kt`.

## Model choices

The app currently offers:

- `gemini-3.5-flash` as the default fast free-tier model;
- `gemini-3.1-flash-lite` as the lightweight fallback;
- `gemini-2.5-pro` for deeper reasoning when available;
- `gemini-2.5-flash` as a stable Gemini 2.5 option.

Do not use `gemini-2.0-flash`; Firebase lists Gemini 2.0 Flash/Flash-Lite as shut down from
June 1, 2026.

If the AI Chat UI still shows **Gemini 2.0 Flash**, uninstall or update the app on the phone with
the latest APK. Current builds do not include Gemini 2.0 in the model switcher.

## App connection checks

1. Confirm the package name is still `com.mkhokheli.PocketCodedWeb`.
2. Keep the app's Google Play package and Firebase app connected.

## App Check setup

The Android app now installs Firebase App Check before using Firebase Auth, Firestore, or AI Logic:

- debug builds use `DebugAppCheckProviderFactory`;
- release builds use `PlayIntegrityAppCheckProviderFactory`.

In Firebase Console, open **Security > App Check** and register the Android app
`com.mkhokheli.PocketCodedWeb`.

For debug APK testing:

1. Install and open a debug build.
2. Trigger any Firebase-backed action, such as AI Chat.
3. Read the App Check debug token from Logcat.
4. Add that token in Firebase Console under **Security > App Check > Apps > Manage debug tokens**.

For Play Store release:

1. Register Play Integrity for the Android app in Firebase App Check.
2. Upload and install the app through Google Play internal testing or production.
3. Watch App Check metrics first.
4. Enforce App Check for Firebase AI Logic only after valid traffic appears.

Firebase says App Check tokens are sent once the client library is installed, but Firebase services
do not require valid tokens until enforcement is enabled in the Firebase console.

## Backend quotas and verified email behavior

The Android app gates AI Chat behind Firebase sign-in and email verification, so anonymous users
cannot use the feature. Google Sign-In accounts are treated as verified.

The Cloud Function also verifies:

- a Firebase Auth ID token;
- a Firebase App Check token;
- a verified email address;
- the requested model against an allowlist.

Default AI limits:

- Free users: 5 AI prompts per UTC day.
- Premium users: 50 AI prompts per UTC day.
- Custom override: create `aiEntitlements/{uid}` with `dailyLimit`, or set an Auth custom claim
  named `premium` or `aiPremium`.

The backend writes usage to `aiDailyUsage/{uid_YYYY-MM-DD}`. Firestore rules explicitly deny client
access to `aiDailyUsage` and `aiEntitlements`; only Admin SDK code such as Cloud Functions can read
or write those collections.

Firebase/Gemini quota is controlled by the Google/Firebase project, not by a user's email address.
With this backend proxy, Gemini usage still belongs to the `PocketCodedWeb` Google/Firebase project,
so any paid usage is billed to the project owner. Users do not pay Google directly unless you build
and enforce your own payment/entitlement flow.

To protect the project for larger launches:

- keep the API key only in Cloud Functions secrets;
- set budget alerts in Google Cloud Billing;
- start with low daily limits;
- enforce App Check after valid traffic appears;
- monitor `aiDailyUsage` and Gemini quota.

Do not ship private provider API keys directly inside the Android app.

## Play/Data safety note

The privacy policy has been updated to disclose that AI Chat can send prompts, code context, file
names, and terminal output to Firebase AI Logic / Gemini when users request AI assistance.

