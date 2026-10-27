# Live Node terminal validation — 2026-10-27

PocketCoded Web's Node bootstrap already flushed its log file on a timer, but the
terminal displayed a command-time snapshot. Logs produced after startup required
`node logs` to become visible.

## Change

- The visible project's terminal now polls its managed Node session for new output.
- A bounded snapshot cursor avoids replaying startup snapshots or previously shown logs,
  including when the terminal switches between embedded and fullscreen views.
- Output follows stdout and stderr, preserves partial lines, and reports session exit.
- Polling pauses with the activity lifecycle, runs file/process checks off the UI thread,
  and explicitly dispatches UI state updates on Android's main thread.
- Only the matching project's session is consumed. Existing canonical project-boundary
  checks remain in place.

This applies to managed `node server.js`, `npm run ...`, and installed Node CLI sessions.
`node logs` still returns a full retained snapshot; `node stop` ends the session.
The browser JavaScript console and bounded `node -e` evaluator were not changed.

## Validation

- Android build and native 16 KB alignment validation passed.
- All **146 Android JVM unit tests** passed, including new cursor tests for incremental
  output, bounded-log trimming, reset, empty snapshots, and partial lines.
- All **15 host Node runtime/development tests** passed: CommonJS, ESM/top-level await,
  dependency loading, asynchronous failures, bounded logs, slow writes, exit flushing,
  debugger, test runner, and profiler behavior.
- Installed the debug app and test APK in place on the connected Xiaomi 2209116AG,
  Android 13. No app-data clearing or uninstall was performed.
- Final combined device run passed **2/2 tests in 36.333 seconds**:
  - The actual Compose terminal launched both `node server.cjs direct` and
    `npm run dev -- npm`. Delayed stdout fragments and stderr became visible before
    HTTP traffic and without a `node logs` command. Forty HTTP requests across the
    two launches produced live request logs. Startup output appeared once per launch;
    switching terminal views did not replay it; both servers stopped successfully.
  - Existing service tests verified npm arguments/environment, real HTTP responses,
    server preview, repeated stop/restart, ESM/top-level await, and killing a CPU loop.
- The final live UI test also passed separately in 25.723 seconds before the combined run.

Intermediate phone runs exposed test-fixture ordering and stop-message timing mistakes,
and an intermittent wrong-thread callback in the instrumentation coroutine dispatcher.
The fixture and explicit main-thread dispatch were corrected before the passing final runs.
MIUI's blocked ActivityScenario launch was handled by the repository's existing launch
assistance script, adapted locally for the debug package.

## User data and evidence

Before installation, the debug app's `files` and `shared_prefs` directories were backed
up to `PocketCodedPy/.codex-tmp/web-before-live-terminal.tar` (74240 bytes). All seven
regular files under the existing Web `p1` project matched that backup after testing.
Tests used separate temporary directories and restored their modified session preferences.

Local evidence is in the sibling PocketCodedPy workspace's `.codex-tmp` directory:
`web-live-build.log`, `web-live-main-thread.txt`, and `web-live-final-verified.txt`.
The last file records the final 2/2 device result. Earlier failure logs are retained there
for traceability.
