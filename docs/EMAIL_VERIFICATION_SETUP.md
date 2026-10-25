# PocketCodedWeb verification email branding

Firebase Authentication sends the verification message, so its sender name,
subject, and body are configured in the Firebase Console rather than in Android
layout code.

For the `pocketcodedweb-6887b` project:

1. Open **Firebase Console → Project settings → General → Public settings**.
2. Set the public-facing name to **PocketCodedWeb** and select the support email.
3. Open **Authentication → Templates → Email address verification**.
4. Set the sender name to **PocketCodedWeb**.
5. Use the subject **Verify your PocketCodedWeb email**.
6. Use this body:

   `Hello %DISPLAY_NAME%,`

   `Follow this link to verify your email address for your PocketCodedWeb account:`

   `%LINK%`

   `If you did not create this account, you can ignore this email.`

   `Thanks,`

   `The PocketCodedWeb team`

The default `firebaseapp.com` action-handler domain can only be replaced after
adding and verifying a custom domain in Firebase Authentication.
