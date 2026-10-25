# Connected phone IDE audit — 10 September 2026

Device: Xiaomi model 2209116AG (`sweet`), Android 13. Installed PocketCodedWeb version 3.8.2 (26), debuggable build. Tests target the installed app; the source search-icon correction has not been installed during this audit.

## Automated device results

Existing installed Android instrumentation suites: **15 passed, 1 failed across 16 tests**.

| Suite | Result | Coverage |
| --- | --- | --- |
| NativeGitDeviceTest | 4/4 passed | Commit/diff with unusual paths; merge, stash, tags and worktrees; 1,000-file index and repeated status; invalid repository/revision handling |
| DeviceCoreStressInstrumentedTest | 4/4 passed | Representative project diagnostics; large-project loading; semantic index cache; infinite-loop timeout and Node recovery |
| DeviceTerminalInstrumentedTest | 4/5 passed | File lifecycle and quoting; sandbox boundaries; Node execution and recovery; compatibility commands and malformed inputs |
| SftpDeviceTest | 2/2 passed | Android runtime/preflight and invalid command rejection; no successful server connection or transfer tested |
| NativeNodePageSizeTest | 1/1 passed | Node 18.20.4 filesystem, crypto, WebAssembly memory, arithmetic, timeout and recovery on this phone |

The terminal Git lifecycle test fails at commit with `Author identity unknown`. Its fixture does not configure `user.name` and `user.email`; the native Git tests do configure a local identity and pass. This is a test setup gap and a first-use configuration requirement, not evidence that configured Git commits fail. The subsequent large-output assertions in that failing test were not reached.

Measured core timings (not end-to-end UI timings):

- Load 2,507 editable files: 846 ms.
- Cold semantic index, 2,506 files: 1,265 ms.
- Twenty cached index calls: each below the millisecond timer's resolution.
- Small representative project load and diagnostics: 23–87 ms.
- Infinite loop stopped at the configured 750 ms limit; subsequent execution returned 42.

Test fixtures were created as separate projects: BeginnerPortfolio, JuniorDashboard, EnterpriseConsole, BrokenLab, StressLab and TerminalQualification. Project0 was not edited.

## Source-level compatibility limits

These are findings from the workspace source, not additional device test passes:

- npm/pnpm/yarn/npx are built-in compatibility commands. Help explicitly says native Node CLIs, postinstall scripts and binary tools are not executed.
- Package installation has a 160-package cap, dependency depth limit of 7 and a 30 MB package size limit. Conflicting nested dependency versions are skipped with warnings.
- The current `npm ci` implementation removes the lockfile and installs again; reproducible lockfile-based desktop npm semantics should not be assumed.
- Editable files are limited to 1,000,000 bytes. Semantic indexing is capped at 4,000 files and 24,000,000 total bytes.
- Console capture exists. A breakpoint/step-through debugger has not been verified.

## Interactive validation

The JuniorDashboard editor was visible during user interaction. In portrait with Explorer open, the Explorer occupies most of the workspace and leaves a narrow code pane. The editor heading wraps over multiple lines, and code and completion content are clipped. This is a confirmed usability concern in the observed layout; fullscreen/Explorer-toggle affordances are present, but their behavior was not exercised by automation.

Evidence: `../.codex-tmp/audit-editor-portrait.png` and the captured UI hierarchy. One hierarchy showed the JavaScript editor text bounds at x=874–1079 on the 1080-pixel-wide display, leaving approximately 205 physical pixels for editing.

Still unverified: project creation/search interactions, actual editor input/save/reopen, keyboard and touch usability, landscape layout, rendered preview and console, actual package download/import, and lifecycle recovery. ADB taps repeatedly failed with an `INJECT_EVENTS` permission error, including after reconnecting and after the user enabled the phone's separate USB debugging security option. `persist.security.adbinput` reports `1`, but input injection remains rejected. This device-control limitation is distinct from an app failure.

An app rendering snapshot reported 12 janky frames out of 12 during startup. This sample is too small and affected by startup/testing to establish steady-state scrolling or typing performance. A representative interactive frame trace remains necessary.

No full mobile-readiness or desktop-equivalence claim is supported yet. Remote Git authentication, real SFTP transfers, and broad framework/build compatibility require further tests.

## Fixes prepared after the audit

- At the user's request, the original side-by-side Explorer/editor layout has been restored, including the 50/50 initial split, original resize range and Explorer visibility toggle. The separate mobile Explorer dialog was removed.
- The project title now takes only the available toolbar space and truncates instead of pushing action buttons offscreen.
- The hollow search-icon correction is included in the rebuilt app.
- Terminal qualification explicitly installs native Git and sets a repository-local test author, removing dependence on test order or the user's Git settings.
- `npm ci` now fails explicitly without changing the manifest, lockfile or installed dependencies. True locked installs remain unsupported. A device regression test checks preservation of all three.

Validation: debug app and Android test APK builds succeeded; all three `TerminalTextUtilsTest` unit tests passed. Android native libraries passed the build's 16 KB ELF alignment check. The phone disconnected during this work, so these new builds have not been installed and the updated device tests and visual layout still require a connected-device run. Earlier 15/16 results above refer to the pre-fix installed build.
