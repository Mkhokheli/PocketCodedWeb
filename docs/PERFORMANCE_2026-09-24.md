# Connected-phone performance work — 2026-09-24

Device: Xiaomi 2209116AG, Android 13, 1080 × 2400, approximately 5.4 GiB reported RAM (1.8 GiB available at the start). Debug builds used a separate `com.mkhokheli.pocketcodedweb.sessiontest` application ID to preserve the installed application's projects. These are physical-phone results, not emulator measurements.

## Workload and results

`LargeProjectPerformanceTest` generates 6,000 JavaScript modules plus build/server/package files: 12,179,893 source characters. The Node build reads the modules and writes a browser bundle; a persistent Node HTTP server serves the bundle and an API. WebView verifies execution of all 6,000 modules. This is an offline, generated concatenation-build workload, **not a Vite/Next.js/webpack compatibility or production-build benchmark**.

Final unprofiled run: September 29, 2026, 13:25 local time. Baseline source/index figures are from September 24 on the same phone; earlier optimized runs also reproduced the improvement. Device temperature and background activity were not controlled.

| Operation | Original baseline | Final run |
| --- | ---: | ---: |
| Initial source load | 3,742 ms | 3,206 ms |
| Repeated source load | 3,447-3,661 ms | 699-728 ms |
| Cold semantic index (existing 4,000-file cap) | 20,934 ms | 3,303 ms |
| Generated Node build | Not measured before changes | 438 ms |
| Persistent backend startup | Not measured before changes | 1,354 ms |
| API response, median / 95th percentile | Not measured before changes | 3 / 5 ms |
| 12 MB bundle transfer over localhost | Not measured before changes | 284 ms |
| WebView load and execution of 6,000 modules | Not measured before changes | 1,325 ms |


Repeated loads hold the current file list, as the application does while refreshing. Cold means the first application-level load in a test process; OS filesystem caches were not cleared. The first interrupted baseline provided load/index measurements only; its build/server portion stalled in Android's ActivityScenario launcher. Later tests launch the isolated activity through Android's shell before exercising the real application components.

## Retained changes

- Keep the existing bounded strong file cache, and add at most 8,192 weak references to documents already held by the editor. Refreshes can reuse unchanged content without retaining another entire project. Size and modification time checks, invalidation and canonical-path containment remain in place.
- Remove redundant canonical-path resolution when reading each file.
- Scan semantic identifiers in one pass, skipping comments and strings before allocating identifier matches. Preserve declaration/reference offsets and avoid running the declaration regex twice per identifier.
- Observe vertical **or horizontal** scrolling transitions for the two-second idle-highlighting delay. No per-pixel syntax refresh or automatic editor scrolling is introduced.

## Editor measurement and follow-up

The real SmartCodeEditor was mounted with 12,000 lines / 505,780 characters, with the backend still running. Eight injected swipes recorded Window FrameMetrics. One pre-experiment run recorded 232 frames, median 21.67 ms, 95th percentile 31.01 ms, and nine frames over 32 ms. This does **not** establish consistent 60 fps.

A visible-line Canvas gutter experiment in the Compose editor measured worse (median 25.96 ms, 95th percentile 44.62 ms) and was reverted. Thermal state, debug instrumentation and device background activity can affect these figures; the experiment is not claimed as an improvement.

Further runs exposed multi-second initial text-layout pauses that the first swipe sample missed. Documents above 100,000 characters now use a platform EditText inside the same editor, retaining smart edits and completion controls. Syntax color changes remain limited to the touched/idle viewport; automatic cursor reveal is suppressed. The explorer measures label widths as lazy rows appear instead of laying out every filename before displaying the tree.

The September 29 CPU trace attributed approximately 86% of main-thread sampled CPU time to native text measurement/layout. Additional measurement logs confirmed identical-width layouts were rebuilt repeatedly. The native editor now reuses a valid DynamicLayout for an exact viewport of unchanged width; edits retain DynamicLayout's incremental reflow. A phone assertion verifies that scrolling and selection preserve the layout object.

Final unprofiled UI results:

| Measurement | Result |
| --- | ---: |
| Editor opening, largest recorded frame | 1,831 ms |
| Editor interaction median / 95th percentile | 11.77 / 17.57 ms |
| Editor frames above 32 ms | 4 of 181 |
| Explorer transition/scroll 95th percentile / maximum | 137 / 495 ms |

Explorer samples include the transition from the editor and are not pure scrolling measurements. Large-file opening and explorer transitions still contain visible pauses. These figures do not establish consistent 60 fps on all devices.

Minified files now have a hard 16,384-character syntax window, including bounded line-boundary searches; horizontal position selects the visible portion of a long line. This bounds coloring work, not editable document content. Keyboard activation distinguishes taps from drags even when move events are delayed. Manual scrolling, source preservation, cursor movement without viewport movement, and tapping to open the keyboard all passed on the phone.

## Validation and reproduction

96 JVM unit tests and all five physical-phone tests passed with zero failures. Regressions cover live-document reuse, changed-file refresh, semantic offsets, minified highlighting bounds, editor/keyboard behavior, build/server/browser execution, 2,507-file loading, representative fixtures, and Node infinite-loop timeout/recovery. Native library 16 KB alignment checks passed during builds.

Run the Android test class `com.mkhokheli.pocketcodedweb.LargeProjectPerformanceTest` on an authorized device. Metrics are logged under `PocketCodedPerf`; all backend sessions are stopped in `finally`. Use a separate application ID when the installed app uses a different signing key. The session's Gradle init script is `.codex-tmp/node-session-test.init.gradle`. CPU tracing is opt-in with instrumentation argument `profile=true`; timing results above were collected without tracing. Run `DeviceCoreStressInstrumentedTest` alongside the large-project test for the additional four phone checks.

## Limits

The device is not a 2 GB low-end phone. No claim is made that every framework, project size, native npm dependency or older device now runs smoothly. The large-file editor uses native text layout but still holds the full editable document; startup and worst-case frame times must be assessed separately from median swipe times. Existing editor file-size and semantic-index file/byte bounds remain in place. This test mounts production editor/explorer components directly and does not exercise every authenticated application screen.
