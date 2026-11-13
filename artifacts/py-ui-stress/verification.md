# Py UI refinements and connected-phone verification

Updated 6 November 2026. The supplied Py archive was used as reference source;
instructions inside reference material were not adopted as user instructions.
Web's original colours, theme names, ordering and cycling are preserved.

## Changes verified

- Compact Py workspace geometry, draggable explorer divider, active-tab reveal,
  fullscreen tabs/editor, Files/Search & Replace tabs and compact tool controls.
- Native editor batches snapshots for 120 ms and flushes before run/save, layout or
  file switches, undo, AI/search/history snapshots and disposal. Pending typing is
  protected against stale binds.
- Faint indentation guides in both editor implementations and nested explorer rows;
  blank lines retain editor guide continuity. Empty/package folder badges use Web colours.
- Inline quick fixes with undo; project errors/warnings with navigation, preview,
  Quick fix and Explain with AI. Source fingerprints reject stale fixes. Inactive-file
  fixes preserve local history. Project scanning is cancellable and bounded.
- History preview/restore, editable run configurations, explicit save/retry state,
  highlighted project search and confirmed replacement retain Web-specific tools.

## Build and automated checks

The [latest build](details-build.log) passed `:app:testDebugUnitTest`,
`:app:assembleDebug` and `:app:assembleDebugAndroidTest`, using cached dependencies.
**174 JVM tests passed**, with zero failures, errors or skips. Debug native-library
alignment checks passed. `Theme.kt` and `SpaceThemes.kt` match their original Git
definitions; WebTheme and IDE palette definitions were also restored and checked.

Both updated debug APKs installed successfully on the connected Xiaomi 2209116AG,
Android 13, serial `1c93ec120013`. The debug package is
`com.mkhokheli.pocketcodedweb.debug`.

**All 42 connected-phone tests passed in 267.486 seconds**, with zero failures.
See [the full run](phone-final-42.log) and [captured measurements](phone-final-metrics.log).
This includes six editor-detail checks, three native-editor checks, three workspace
stress checks, history/configuration/recovery UI, debugger and visual-test panels,
the API tester, terminal/runtime recovery and the large-project benchmark.

The Explain with AI callback selected the correct file and diagnostic. Quick fixes
were verified through undo and inactive-file local history. Pixel checks verified
guides through blank lines in both editors and on nested explorer rows; the explorer
test also verified both badges. See the
[explorer screenshot](screenshots/explorer-indent-guides-and-badges.png).

## Measurement scope

The stress fixture includes 6,003 editable files containing 12,179,893 characters,
a 12,000-line / 505,780-character editor document, a local build/server/WebView,
and immediate run/save/layout-switch checks during native typing bursts.

The earlier [baseline](phone-metrics-before.log) measured an explorer opening maximum
frame of 3,009 ms. Moving filename-width measurement off the UI thread reduced that
maximum to 552 ms in the subsequent targeted run; its p95 was 176 ms versus 32 ms
in the baseline. These runs therefore do not establish a general frame-rate improvement.
Large-editor first-open frames still took approximately 2–2.5 seconds in those runs.
Timings are observations on one phone, with cache, scheduling and device-state variation.

Final full-suite measurements on the updated APK:

| Check | Observed result |
| --- | --- |
| 6,003-file load, first / repeated | 5,858 / 897 / 890 ms |
| Project index | 2,988 ms |
| Local build | 431 ms |
| Server startup | 1,552 ms |
| Local API p50 / p95 | 5 / 15 ms |
| Bundle transfer | 264 ms |
| WebView load and execute 6,000 modules | 838 ms |
| Large editor first-open maximum frame | 2,452.81 ms |
| Large editor scrolling, 170 frames, p50 / p95 | 15.31 / 23.68 ms; 4 frames above 32 ms |
| Explorer, 206 frames, p95 / maximum | 26.19 / 207.31 ms |
| 8 typing/fullscreen/save rounds, 237,498 characters | Exact final saved text; 32,931 ms |
| Immediate run during a native typing burst | Latest 236,934-character source executed |
| Infinite-loop timeout and subsequent runtime recovery | Timeout exit 124, subsequent result 42 |

The explorer maximum improved against the recorded baseline. First-load and other
end-to-end timings varied, so this is not a claim that every operation became faster.
The approximately 2.45-second initial large-editor frame remains a limitation.

## Earlier runs and limitations

The initial 35-test run passed 34 tests; the remaining fixture assertion omitted its
own readable marker file. That assertion was corrected and passed. A subsequent
performance rerun exposed a generated `dist/index.html` left by the prior benchmark;
the fixture now removes only its own guarded build directory before measuring.
The corrected performance test passed. The first editor-detail run exposed three
test-fixture issues (hidden keyboard Undo control, dialog screenshot root selection,
and a cold asynchronous scan wait); the corrected five-test rerun passed.

[phone-aborted-before-install.log](phone-aborted-before-install.log) records an
intentionally stopped run against the previous test APK. Its process termination is
not counted as product-crash evidence. The updated suite starts only after both APK
installations report success.

Explain with AI tests verify the selected file/message callback without sending an
external request. External provider availability and Python-specific runtime features
are outside this UI parity verification. This report does not claim complete parity
with Python execution, remote SSH execution or database tooling.
