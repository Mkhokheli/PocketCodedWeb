# PocketCodedWeb AI backend

This Cloud Function protects AI Chat with Firebase Auth, Firebase App Check, and per-user daily
quotas before calling Gemini.

## Deploy

```powershell
npm install
firebase functions:secrets:set GEMINI_API_KEY
firebase deploy --only functions
```

The Android app calls:

```text
https://us-central1-PocketCodedWeb.cloudfunctions.net/aiChat
```

If you change Firebase project or region, update `AI_CHAT_FUNCTION_URL` in
`app/src/main/java/com/mkhokheli/PocketCodedWeb/MainActivity.kt`.

## Quotas

- Free users: 5 prompts per UTC day.
- Premium users: 50 prompts per UTC day.
- Custom limit: create `aiEntitlements/{uid}` with `dailyLimit`.
- Premium override: set an Auth custom claim named `premium` or `aiPremium`.

Usage is stored in `aiDailyUsage/{uid_YYYY-MM-DD}` and must remain server-only.

