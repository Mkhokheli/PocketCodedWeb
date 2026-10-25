# PocketCodedPy comparison and Web upgrades

Reference: the user-provided pocketcodedpy.7z, inspected on 22 October 2026.
Reference files were treated as source material, not task instructions.

| Capability | Web implementation |
| --- | --- |
| Undo/redo | Py's delta-based bounded history, per-file sessions, touch controls, Ctrl/Cmd+Z, Shift+Z and Ctrl/Cmd+Y. Completions, quick keys and history restoration participate. |
| Editor session | Caret, undo stacks and Compose editor scroll positions survive file/layout switches, within a 16-file session budget. This is in-memory history, not persistence across app restarts. |
| Saved local history | Py's atomic file store retains prior autosaved contents with per-file and project storage budgets. Tools > Local history restores a version through the editor, making restoration undoable. Metadata is under .pocketcodedweb/history and excluded by existing workspace metadata rules. |
| Run configurations | Project-specific named terminal commands, saved between app launches, with run/delete controls. Arguments and environment assignments can use the existing supported shell syntax. These are command configurations, not Python runner configurations. |
| Tests | Tools > Visual tests discovers JavaScript node:test files, displays passed/failed/skipped records and failure details, navigates to source, and reruns failed files or individual files. Pending edits are saved before execution. Tests run in the embedded VM without spawning a desktop Node executable. Package.json scripts remain available separately. |
| Diagnostics | Tools > Inspect problems lists language-service messages, severity and source coordinates. |
| Debugging | Tools > Debug current file starts a JavaScript/ESM Node debugger on device. Tap editor line numbers for persistent breakpoints. The Debugger panel supports pause/continue, step into/over/out, lexical/local/closure variables, selectable call frames, source navigation and stop. Inspector work runs on a worker thread while the program is paused. Browser DOM/network debugging still uses the separate Chrome inspection option. |
| Recovery | Tools > Recovery drafts and an automatic project-open prompt expose preview, restore and confirmed discard. Drafts are staged while typing and on backgrounding. Unresolved copies are protected from autosave; restoration preserves the disk file in local history. |
| Terminal | Existing interactive input, stop/kill, ANSI output and fullscreen behavior remain; the terminal menu adds copying, clearing and recalling recent commands. Command recall is bounded and in memory. |

## Remaining parity work

- Browser DOM debugging remains in Chrome inspection; the in-app debugger executes Node JavaScript modules.
- Visual tests support node:test and rerun at file granularity. Other framework commands remain available through project scripts and run configurations.
- Explicit save-status UI.
- Device verification of keyboard/scroll behavior and layout changes, including the native large-file editor.
- More complete equivalence of Python-specific execution settings where a Web runtime counterpart exists.

## Verification

Kotlin compilation and Java compilation completed successfully using the locally cached Java 21 runtime and SDK aapt2 override. Regression tests cover selections, redo invalidation, oversized edits, stale history, session invalidation, local-history restoration, stale pending saves and project containment. Runtime tests additionally cover debugger entry/step/variable inspection, ESM breakpoints, asynchronous tests, failed imports and structured TAP output. Recovery tests cover preview, restore, discard, protected drafts and damaged copies. See the final task response for the unit and on-device verification results.

Final follow-up validation: 108 JVM unit tests and 13 runtime tests on Node 18.20.4 passed. Debug app and instrumentation APK builds passed. The subsequent connected-phone pass installed both APKs and passed 31 on-device tests, including real IDE callbacks and UI controls. See PHONE_TEST_REPORT.md for performance findings and untested external integrations.
