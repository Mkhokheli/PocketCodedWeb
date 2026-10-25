# Connected-phone test report

Tested on 22 October 2026 using the connected Xiaomi 2209116AG (sweet_global2), Android 13, arm64-v8a. The app uses embedded Node 18.20.4. The latest debug app and instrumentation APKs built and installed successfully.

**Result: 31 device tests passed: 22 runtime/stress tests and 9 UI/development tests.** No app crash was recorded in the app's crash buffer during these runs. Test fixture projects were removed afterward; the updated app remains installed.

## Device coverage

| Area | Tests | Verified behavior |
| --- | ---: | --- |
| Editor and configurations | 2 | Independent per-file undo/redo, editor remounts, saved command execution and deletion |
| Embedded debugger and test runtime | 2 | Entry pause, stepping, locals, runtime breakpoint updates, frame inspection, continue/exit, ESM pause/stop/restart, structured pass/fail/skip records and failure lines |
| Debugger and visual-test panels | 2 | Touch controls, variable-list scrolling, source navigation, failure expansion and rerunning a repaired test file to green |
| Full IDE callbacks | 2 | Actual autosave to disk, undo/redo saves, history restoration, terminal execution, clipboard copy, clear output and command recall |
| Recovery controls | 1 | Preview, restore, confirmed discard, preserving the other file's disk contents |
| Terminal | 6 | Quoting, file lifecycle, navigation, traversal rejection, root-removal rejection, Node errors/recovery, npm scripts, diagnostics, Git and bounded output |
| Native Node | 1 | Bundled version, filesystem/crypto/WebAssembly, infinite-loop timeout and recovery |
| Managed Node sessions | 1 | Backend HTTP preview, arguments/environment, stop/restart, ESM, force stopping an infinite loop |
| Native Git | 4 | Unicode paths, commits/diffs, merge, stash, tags, worktrees, 1,000 files, repeated status and invalid revisions |
| SFTP | 2 | Android runtime loading and command/credential/path preflight; no external server connection |
| Editor scrolling | 1 | Manual scrolling and rejection of unsolicited bring-into-view movement |
| Preview/browser lifecycle | 2 | Embedded JavaScript pause/resume/disposal and Chrome animation/API work while a Node server runs |
| Large project | 1 | 6,003 editable files, a 12 MB / 6,000-module build and HTTP transfer, browser execution, 12,000-line native editor and explorer frames |
| Project stress | 4 | Representative project diagnostics, cold/warm indexing, 2,507-file loading/heap and timeout recovery |

## Performance findings

These are stress-fixture measurements on this phone, not general latency guarantees.

- 6,003 files / 12,179,893 source characters: initial load 2,010 ms; warm loads 647 and 657 ms.
- Large-project semantic index: 2,436 ms. The smaller stress project's cold index took 1,006 ms; warm samples were approximately 0–1 ms.
- Node build of the 6,000-module bundle: 301 ms. Server startup: 1,361 ms.
- HTTP API median: 5 ms; 95th percentile: 12 ms. Bundle transfer: 212 ms.
- WebView loading/executing the 6,000-module bundle: 1,139 ms.
- 12,000-line editor: scrolling frame median 15.8 ms, 95th percentile 20.2 ms, 4 of 168 samples over 32 ms. **The first mount recorded a worst frame of 1,750 ms**, a significant cold-start stall.
- 6,003-file explorer: frame 95th percentile 64.4 ms, maximum 363.5 ms. **Explorer rendering still has visible jank under this workload.**
- Chrome stress animation: frame 95th percentile 41.7 ms idle and 33.4 ms with busy Node work. API 95th percentile stayed about 25–29 ms. The stress animation is not consistently at 60 fps.

## Test-harness corrections

Initial failures exposed test assumptions rather than reproduced app functional defects:

- Android `/data/user/0` and `/data/data` refer to the same application files; comparisons now use canonical paths.
- Recovery controls needed unambiguous selection within multiple draft rows.
- Debugger variables are virtualized; tests now scroll to offscreen variable rows before asserting them.
- MIUI rejected instrumentation activity launches from the background. `tools/run-phone-tests.py` detects those rejections and foregrounds the same activity through ADB; no phone security settings are changed by the helper.

The IDE root composable and theme enum were made internal so signed instrumentation can exercise the real local IDE callbacks without logging into an online account. No public activity or authentication bypass was added.

## Limits

This validates the listed local workflows on one Android 13 phone. It is not a claim that every possible app behavior is verified. Online authentication, account/cloud synchronization, AI providers, billing/purchases, external LSP and real remote Git/SFTP connections were not exercised. Those require accounts, credentials or endpoints. Browser DOM debugging through desktop Chrome was not part of the in-app Node debugger checks.

## Evidence and repeatability

- `.codex-tmp/phone-broad-tests.log`: `OK (22 tests)`.
- `.codex-tmp/phone-deep-ui-tests.log`: `OK (9 tests)`.
- `.codex-tmp/phone-browser-benchmark.json`: Chrome/Node benchmark results.
- `.codex-tmp/phone-fullide-build.log`: final app and instrumentation build.
- Device runner: `tools/run-phone-tests.py`.
- New/expanded tests: `EditorFeaturesDeviceTest`, `DevelopmentToolsDeviceTest`, `DevelopmentPanelsDeviceTest`, `FullIdePhoneTest`, `RecoveryControlsDeviceTest`.
