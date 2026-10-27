# PocketCoded Web verification ? 27 October 2026

## Changes

- Monthly Premium (`pocketcodedweb_premium_monthly`, base plan `monthly`) above lifetime.
- Both INAPP and SUBS purchase discovery, backend verification, renewal/expiry handling,
  subscription management and restoration. Lifetime ownership survives monthly expiry.
- API tester in Editor tools, embedded in the existing editor card and following Web palettes.
- Refined 48 dp button targets, clear spacing, full-width URL, wrapping response actions.
- GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS, headers, UTF-8 body, response status/timing/headers,
  JSON formatting/copy, timeouts/cancellation, 1 MiB capture and a bounded preview.
- Node API example: `docs/examples/api-server.cjs`.
- FULL release native symbol metadata enabled; available symbols verified inside the bundle.
- Separate debug package `com.mkhokheli.pocketcodedweb.debug` for phone testing.

## Results

- 143 app unit tests: PASS.
- 14 backend lifecycle tests: PASS.
- 5 Firestore security tests in local `demo-pocketcodedweb` emulator: PASS. These include
  attempts to forge monthly expiry, lifetime ownership and subscription-token fields.
- 13 API transport tests on USB phone `1c93ec120013` (2209116AG, Android 13): PASS.
- 8 integrated API tester UI tests on the same phone: PASS, final run 65.885 seconds.
- All 20 Web themes, inline/fullscreen, copy, input validation, timeout/cancel/retry,
  large/empty responses, Back navigation, editor preservation and resizing during requests.
- User-written Node.js HTTP API ran in the app runtime and returned 200 from API tester:
  `usersNodeApiCanBeTestedInsideTheEditorCard`. Its test fixture uses the managed files
  directory required by Web, after an initial fixture in cache was correctly rejected.
- Build tasks: assembleDebug, assembleDebugAndroidTest, testDebugUnitTest, bundleRelease: PASS.
- Release native libraries passed existing 16 KB alignment validation.
- Seven `.dbg` files exist under `BUNDLE-METADATA/com.android.tools.build.debugsymbols/`.
  Already stripped third-party libraries cannot supply full symbols.
- Debug app remains installed on the phone. Play-installed package was not replaced.

## Deliverables and remaining store setup

`app/build/outputs/apk/debug/app-debug.apk`

`app/build/outputs/bundle/release/app-release.aab` ? unsigned local release bundle; sign with
existing upload credentials before distribution and use an unused version code.

Screenshots: `artifacts/api-tester/api-tester-tests/`.

Setup: `docs/API_TESTER_AND_MONTHLY_PREMIUM.md` and `functions/README.md`.
Release notes: `docs/RELEASE_NOTES_EN_GB.txt`.

Billing backend and Firestore rules deployed successfully to `pocketcodedweb-6887b` on
27 October 2026. Firebase confirmed successful updates for `verifyPlayPurchase`,
`playBillingNotification` and `reconcileVoidedPlayPurchases`, and successful creation of
`reconcileMonthlySubscriptions`, all in `us-central1`.
The user confirmed that subscription `pocketcodedweb_premium_monthly`, base plan `monthly`,
is active in 175 countries/regions. Configure/test RTDN and verify checkout/renewal/expiry
with license testers on a Play internal track before publishing the app.
Deployment log: `artifacts/api-tester/billing-deploy.txt`.
