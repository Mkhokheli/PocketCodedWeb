# Freemium and yearly billing

## Release configuration

The existing subscription product is `pocketcodedweb_premium_monthly`. Despite its
historical name, it now supports two auto-renewing base plans:

| Base plan | Billing period | Display order |
| --- | --- | --- |
| `monthly` | Monthly (`P1M`) | 1 |
| `yearly` | Yearly (`P1Y`) | 2 |

Lifetime remains the one-time product `pocketcodedweb_lifetime`, displayed third.
These are configuration requirements; this implementation does not create or
activate products in Play Console.

1. On the existing subscription, add an auto-renewing `yearly` base plan with
   a yearly billing period. Set prices and available regions in Play Console,
   then activate it. Keep the existing monthly base plan active.
2. Deploy the updated `functions/index.js` before offering annual purchases.
   `verifyPlayPurchase`, the existing subscription notification handler, and
   scheduled reconciliation use the existing subscription product. The verifier
   now accepts both base plans. Existing `monthlyExpiresAt` and token fields are
   retained for compatibility and store either subscription's expiry.
3. Ensure the existing Play service-account permissions, real-time subscription
   notifications, scheduled reconciliation, and App Check configuration remain
   configured as described in `functions/README.md`.
4. Publish the app to an internal testing track and test using Play license testers.
   A sideloaded debug APK is insufficient to validate actual product availability.
5. Confirm the privacy and terms pages are published at the existing Firebase
   Hosting domain before release. This change does not deploy hosting or functions.

The app selects regular base-plan offers (`offerId == null`), so no introductory
price is advertised. All displayed prices come directly from ProductDetails.
Unavailable annual pricing never falls back to a monthly/lifetime price.
The app's existing 14-day trial does not automatically begin a paid subscription.

## Access behavior

After the trial expires, users without Premium see a protected project named
`Scratchpad` under Existing Projects. It has no rename/delete menu and is hidden
during the trial and for Premium owners. Free users can open this persistent scratchpad,
edit its `index.html`, `style.css` and `script.js`, preview HTML with inline CSS and JavaScript, provide browser-requested program input, copy output,
use fullscreen editing, navigate the explorer and change themes.

The trial unlocks Premium features until its existing deadline. Expiry no longer
locks the app. Existing projects are preserved but locked behind Premium, including restored
project navigation. Tapping a locked project offers an upgrade instead of opening
it. The final-day trial popup warns that project access will require Premium.
Free scratchpad import/export and project mutations require Premium.

Premium gates cover projects/files, advanced tools, AI, terminal commands,
npm packages and development servers, expanded terminal/explorer and project search/replace.
The upgrade screen is a modal above the existing workspace, preserving editor
and running-program state while users inspect pricing.

## Release validation

- Purchase, cancel, restore and renew each subscription base plan.
- Exercise pending payment, payment failure, grace period, hold and expiry.
- Confirm a lifetime owner retains access after subscription expiry.
- Confirm unavailable yearly products do not disable other available plans.
- Confirm free program input works and output selection/copy never asks to upgrade.
- Check editor fullscreen, themes, scratchpad persistence and locked project access.
- Inspect all app themes, large fonts, landscape and disabled system animations.
- Confirm opening/dismissing Premium preserves a running program and editor text.

No live Play purchase or deployment is performed by the local build/tests.

The free Scratchpad contains `index.html`, `style.css` and `script.js`, linked for
preview. Missing starter files and asset references are added without replacing
existing user code. Browser prompt/alert interactions remain
available. Arbitrary terminal commands, npm, debugging and project scripts are Premium.

The hosting project is `pocketcodedweb-6887b`. Configure prices only in Play Console.
No cloud deployment, console activation, or live purchase is performed by local tests.

## Implementation validation (10 November 2026)

- Debug APK and Android instrumentation test APK build successfully.
- All 179 Android JVM unit tests pass.
- All 19 backend subscription tests pass, including yearly access, expiry,
  ownership, unsupported base plans, and lifetime/subscription coexistence.
- `FreemiumDeviceTest` covers free terminal input/output, yearly localized price
  selection, the final-day warning, and unavailable yearly pricing. It compiles,
  but execution is unverified: the connected phone rejected installation with
  `INSTALL_FAILED_USER_RESTRICTED`. No app data was cleared and no restriction
  was bypassed.
- After permitting debug APK installation on a test device, run:

  ```powershell
  .\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mkhokheli.pocketcodedweb.FreemiumDeviceTest
  ```

Actual Play pricing, purchase flow and renewals still require an internal-track
release, active console base plans and the deployed verification backend.
