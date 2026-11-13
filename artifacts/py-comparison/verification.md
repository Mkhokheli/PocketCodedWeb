# PocketCoded Web refinement verification — 5 November 2026

Reference: `PocketCodedPy/app/src/main.zip`.
SHA-256: `423de48ffb9da11e545c8ec30f3a007b7f7f976ab0ed94d6a18b5228ce251104`.

## Completed

- Inventory: all 98 archive files fingerprinted; 27 Kotlin files mapped with declaration lists.
  Eleven Kotlin files match Web after package and line-ending normalization.
- `:app:testDebugUnitTest`: 152 tests, zero failures/errors/skips.
- `:app:assembleDebug`: passed.
- `:app:assembleDebugAndroidTest`: passed, including three new refinement UI tests.
- Debug native libraries: existing 16 KB ELF alignment check passed.
- `node tools/test-development-tools.cjs`: 7 passed.
- `node tools/test-node-session.cjs`: 8 passed.

Build output: [build-verification.log](build-verification.log).
Feature decisions and remaining gaps:
[comparison report](../../docs/PY_WEB_REFINEMENT_2026-11-05.md).

## Device verification

Connected device: `1c93ec120013`, model `2209116AG`, Android 13.
Installation initially encountered the phone's USB-install restriction. After the user
enabled installation, both debug APKs installed successfully. The final corrected app
and test APKs are installed.

- Initial device run: 19 of 20 tests passed in 134.675 seconds. The new configuration
  edit test exposed mismatched project preference keys for Android directory aliases.
- Fixed the store to canonicalize project keys while retaining the supplied legacy
  key as a read fallback.
- Targeted rerun: **4 of 4 passed** in 24.727 seconds, including all three new UI tests
  and the existing saved-command execution/deletion test. The initially failing edit,
  rename and persistence scenario now passes.
- Across the initial run and corrected rerun, all 20 selected scenarios have passed:
  8 API tester scenarios, 5 editor/terminal scenarios, native viewport preservation,
  2 development-panel scenarios, 3 new refinement scenarios and recovery controls.

Logs: [initial run](phone-verification.log), [corrected rerun](phone-refinements-rerun.log).
Seven screenshots from the API-tester theme/layout regression are in `screenshots/`.
The light-theme request form was visually inspected. These screenshots do not represent
the new history or run-configuration dialogs; those were checked by instrumentation.

## Deliverables

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`

Final app APK SHA-256:
`b679f873f83d86d698a672789047b0a3e4ab64cdf74d2d02cb66035dd63391c1`.

The app APK uses `com.mkhokheli.pocketcodedweb.debug`. No production release, backend
deployment, Play checkout, live AI provider request or external SSH/database connection
was performed. Structural matching is not a claim of complete behavioral parity.
