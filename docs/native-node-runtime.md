# Native Node runtime

PocketCoded Web bundles Node.js Mobile v18.20.4 as native shared libraries for `arm64-v8a`, `armeabi-v7a`, and `x86_64`. The 64-bit libraries must be rebuilt with 16 KB ELF alignment; the upstream v18.20.4 Android ZIP contains incompatible 4 KB libraries. The 32-bit library remains the upstream binary.

## 16 KB builds and validation

`tools/build-nodejs-mobile-16k.sh` builds the pinned upstream source on Linux/WSL using NDK r24 and `LDFLAGS=-Wl,-z,max-page-size=16384`, the change made by upstream [PR #154](https://github.com/nodejs-mobile/nodejs-mobile/pull/154). Source, NDK and original runtime archives have SHA-256 checks. Install gcc, g++, make, curl, unzip, rsync and Python 3.11 before running it:

```sh
bash tools/build-nodejs-mobile-16k.sh "$HOME/.cache/pocketcoded-node-16k" 4
```

The output is `dist/bin/<abi>/libnode.so`, matching headers in `dist/include/node`, `dist/LICENSE`, and `nodejs-mobile-v18.20.4-16k-android.zip`. Source and library provenance is recorded in `tools/nodejs-mobile-16k-build.json`. When intentionally rebuilding the runtime, validate the resulting archive and update the pinned checksum in `tools/prepare-nodejs-mobile.ps1`. Do not substitute the original upstream 4 KB ZIP.

Copy the validated ZIP to `.codex-tmp/nodejs-mobile-v18.20.4-16k-android.zip` in the Windows project, then import it with:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/prepare-nodejs-mobile.ps1
```

The import verifies the archive checksum, required files and 64-bit ELF alignment before replacing any library. Ordinary Gradle app builds reuse these prepared binaries and do not rebuild Node.js.

The app's NDK r29 builds the JNI bridge; CMake also declares 16 KB alignment explicitly. `verifyDebugNativeAlignment` and `verifyReleaseNativeAlignment` inspect every merged 64-bit native library, including dependencies, before APK/AAB packaging. They reject any LOAD segment with alignment below 16 KB or mismatched file/virtual-address offsets.

```sh
python tools/test_native_alignment.py
python tools/check-native-alignment.py app/src/main/jniLibs app/build/outputs/bundle/release/app-release.aab
```

ELF checks do not replace runtime testing. Run `NativeNodePageSizeTest` on a 16 KB emulator/device with instrumentation argument `expectedPageSize=16384`, and on a conventional device with `expectedPageSize=4096`. It covers loading Node, JavaScript execution, buffers, file I/O, SHA-256, WebAssembly memory and timeout recovery. Check the final signed bundle as well as the source libraries before uploading.

## Verified release 3.8.2 (version code 26)

Validation on 10 September 2026:

- Debug APK, instrumentation APK and release AAB built successfully; release lint checks passed.
- All 79 app unit tests and five native-alignment checker tests passed.
- All 11 packaged 64-bit libraries passed ELF alignment checks in both the debug APK and release AAB. The debug APK also passed `zipalign -P 16 -c 4`.
- `NativeNodePageSizeTest` passed on the connected physical device with 4 KB pages and the API 36 x86_64 emulator with confirmed 16 KB pages. Both runs covered JavaScript execution, buffers, file I/O, hashing, WebAssembly memory and timeout recovery.
- The emulator initially timed out during application startup, before the test ran. Precompiling both app and test packages with `cmd package compile -m speed -f <package>` resolved this; the final instrumentation result was `OK (1 test)`.

The generated `app/build/outputs/bundle/release/app-release.aab` is **unsigned**, with SHA-256 `850e241e04ed35e989e627b2479c5707af3c92268f10feddc79b876f1427b780`. Generate a signed release bundle with the existing upload key before submitting version 26 to Play, then run the alignment checker against that signed artifact. These checks establish native compatibility and smoke-test coverage; they do not constitute Play acceptance or exhaustive device testing.

## Runtime design

- The bounded Node host starts lazily for inline evaluation. Long-running file and package commands use a separate, non-exported `:nodeSession` Android service process. Each process starts Node only once; stopping a session terminates its process, so it can subsequently restart cleanly.
- The host listens only on `127.0.0.1` on an ephemeral port and requires a fresh 256-bit bearer token stored in app-private storage.
- Inline evaluations run in fresh V8 contexts with a hard execution timeout capped at 120 seconds. File/package sessions use Node's real CommonJS/ESM loader, process object, timers and event loop, without a job timeout. Neither path depends on `worker_threads` for isolation.
- Canonical root and working paths must remain below the app's private `filesDir`; traversal and imported paths outside that sandbox are rejected.
- Kotlin reports startup, transport, timeout, script, and native-load failures without crashing the IDE. The existing WebView runner remains available for browser-specific preview code.

This is isolation within the app's Android sandbox, not a security boundary equivalent to a VM or container. JavaScript executes with the app's permissions. Long-lived timers are available in file/package sessions. Do not expose the bounded worker port, token, or arbitrary execution endpoint to exported Android components.

## Packaging and Play decisions

- Publish an Android App Bundle. Play delivers only the matching ABI split, avoiding installation of all three native runtime libraries on one device.
- Keep runtime libraries bundled and immutable. PocketCoded Web must not download executable native code after installation.
- `minSdk` remains 24, matching the supported Android floor used by the pinned runtime build.
- The complete upstream license/third-party notice is packaged at `assets/licenses/nodejs-mobile-LICENSE.txt`. Any runtime upgrade must refresh both the pinned checksum and this notice.
- Native npm addons are ABI-specific and are not supported by default. Pure JavaScript/WASM dependencies are the reliable target; each native addon would require separate Android builds and license review.

## Current command surface

`node -e <code>` uses the bounded host. `node <file> [args...]` starts a managed session. `node status` and `node logs` show session state and recent output; `node stop` terminates the session, closing its sockets and timers even when JavaScript is stuck. One session runs at a time and may host multiple servers. The service runs in the foreground with an ongoing notification and Stop action to keep the backend available during browser preview. Its manifest declares the special-use foreground-service subtype for local development servers.

`npm run dev` and other package scripts resolve the nearest package.json, run Node entry points or installed package binaries, accept leading environment assignments, and forward arguments after `--`. Installed JavaScript CLIs also run through `npx`. Commands no longer silently substitute a static preview for a development server. Explicit `preview`, `lint`, and `format` scripts retain their built-in actions. Shell pipelines/operators, native desktop executables, and dependencies requiring a newer Node version are not emulated; package compatibility still depends on the bundled Node 18.20.4 Android runtime.

HTTP servers publish their actual listening URLs in session logs. **Browser localhost** saves pending edits, reuses the project's running session or starts its `dev`/`start` script, waits up to 30 seconds for an HTTP listener after startup, then opens the actual URL in the default browser. **In-app Live Preview** uses the same server startup flow. Projects without a server script use the built-in frontend preview. Startup failures show logs instead of silently substituting static content. You can also enter a Preview URL or run `preview http://127.0.0.1:3000/` directly. External server links accept HTTP/HTTPS, including LAN development servers; localhost always refers to the phone. Serving frontend and API routes from the same backend avoids cross-origin configuration.

Validation: `node --test tools/test-node-session.cjs` covers CommonJS, ESM/top-level await, asynchronous HTTP, argument/environment handling, failures and bounded logs. `NodeSessionDeviceTest` covers npm execution, HTTP responses, preview links, stop/restart, ESM, and stopping an infinite loop on Android.

The terminal displays **Running** and a red **Stop** action whenever the dedicated session process is alive, in both embedded and full-screen layouts. While stopping, the action becomes **Kill**; shutdown also automatically falls back to killing only this app's session process if it does not exit promptly. HTTP/HTTPS URLs in output are blue, underlined browser links, including localhost, loopback and IPv6 addresses.

Console output uses a bounded 128 Ki-character queue with disk flushes at most once per 200 ms during normal operation, plus a final flush on normal exit or JavaScript failure. Small writes are coalesced into blocks to reduce object allocation. Session status JSON is cached until its file changes. Terminal process observation runs off the UI thread once per second while a session is active, backs off while idle, and pauses when the activity is not visible; unchanged running state does not trigger recomposition or text parsing. ANSI parsing and link annotations are remembered across input edits, and line iteration avoids allocating a list for the whole terminal buffer.

At startup, phones marked low-RAM by Android, with up to 3 GiB of physical RAM, or with an app memory class of at most 128 MiB automatically use a smaller budget. Terminal history retains 96,000 characters, and layout/styles/links process only the latest 32,000 characters or 600 lines with a visible truncation notice. Node logs retain 64 Ki characters and flush every 500 ms; idle process checks back off to three seconds. Normal devices retain the larger budgets and use two-second idle checks. These are output/monitoring budgets, not restrictions on backend APIs or the JavaScript heap. Validate real-world responsiveness on physical low-end phones; these thresholds do not establish a measured frame-rate or battery improvement.
