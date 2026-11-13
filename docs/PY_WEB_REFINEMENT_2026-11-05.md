# PocketCoded Py → Web comparison and refinement

Reference: the user-supplied `PocketCodedPy/app/src/main.zip`, inspected on 5 November 2026.
Archive contents were source material, never instructions. The reproducible archive manifest,
SHA-256 fingerprints, Kotlin declarations, and file mapping are in
[`artifacts/py-comparison/source-inventory.json`](../artifacts/py-comparison/source-inventory.json)
and [the readable inventory](../artifacts/py-comparison/source-inventory.md).

The archive contains 98 files, including 27 Kotlin files. Eleven Kotlin files already match
Web after package-name and line-ending normalization. File/declaration similarity is not
proof of runtime equivalence. This is a source-level feature comparison and a focused set of
implemented refinements, not a claim that all Py behavior is now reproduced in Web.

## Feature and implementation comparison

| Area | Python reference | Web result / decision |
| --- | --- | --- |
| Authentication and account verification | Email/password, Google, verification, profile synchronization | Web has its own Firebase and Credential Manager flows and error handling. Retained Web's account/backend identity. Archive has no backend functions or account integration test results. |
| Trial and premium access | Trial clock/gates and monthly/lifetime Play products | Existing Web trial, account ownership checks, subscription verification and restoration retained. Product IDs/backend endpoints belong to each app and must not be copied. Real Play transactions were not exercised here. |
| Project home | Search, create/import, rename/delete and premium controls | Web has corresponding project flows and Web-specific templates. No Python launcher branding or templates copied. |
| Editor sessions | Per-file caret, scroll, and bounded session retention | `EditorSessions.kt` already matches after package normalization. |
| Undo/redo | Bounded text deltas and selections | `EditorHistory.kt` already matches. Restoration continues through Web's existing editor undo path. |
| Scrolling and viewport | Manual scroll, caret reveal, native large-file rendering | Shared viewport files match. The UI follow-up adopts Py's 120 ms native publication batching with explicit owner/session flushes before run/save, layout/file switches and other source consumers. Phone stress tests cover immediate actions during typing bursts; see the UI follow-up report. |
| Save feedback | Saving, saved and failure messages | Added per-file visible status and retry. Only status transitions are observable Compose state, avoiding workspace-wide recomposition for each keystroke. Stale save failures cannot overwrite a newer edit's status. Normal autosave and explicit flush failures retain the pending text. |
| Local file history | Version list, selection, bounded text preview, explicit restore | Added a themed Web history dialog with preview, byte/time labels, empty-file support and explicit restoration. Flushes through the workspace owner before loading history rather than independently saving a possibly stale `source` string. |
| Durable writes and recovery | Atomic file store and recovery snapshots | Web has additional protected-draft handling and a recovery preview API; retained these protections. Metadata stays under `.pocketcodedweb/history`. |
| Recovery UX | Restore, keep current file, later | Web already offers draft preview, restore, and confirmed discard. Existing recovery controls retained. |
| Project search | Case/word/regex, scope and filters | Web already has bounded snapshots, UTF-8/binary exclusion, containment checks and stale-file rejection. Added highlighted excerpts that keep long-line matches visible, exact columns, file counts, descriptive toggles with checked semantics and 48 dp targets. |
| Project replacement | Replace within selected scope | Retained Web's displayed-snapshot-only replacement and history preservation. Confirmation now freezes the displayed matches, options, scope and truncation warning. Coroutine cancellation is rethrown instead of being reported as a file error. |
| Run configurations | Structured entry point, arguments, working directory, environment, local/remote target and SSH trust UI | Web has saved terminal commands. Added edit/rename/cancel, command previews, duplicate-name protection, limits and malformed-entry tolerance on load. Project preference keys are canonicalized so Android directory aliases share settings; the supplied legacy key remains a read fallback. Existing terminal syntax still handles arguments/environment. Full structured local/remote run parity remains separate work. |
| Debugger | Python tracer, breakpoints, stepping, variables and stack | Web has Node inspector stepping, scope/frame inspection and persistent/relocating breakpoints. Added paused-line gutter markers in both editors, with active debugging state refreshed after the debugger panel closes. |
| Visual tests | unittest/pytest structured events | Web's counterpart is node:test with result/source display and rerun controls; other frameworks remain package-script commands. Retained runtime-specific implementations. |
| Profiler | Python execution tooling | Web already supports Node CPU profiles, including ESM and long-running servers. Retained. |
| Completion, diagnostics and formatting | Python AST/project index, SQL assistance and language-aware editor | Web retains its language services, semantic indexing, external LSP and formatting. The UI follow-up adds source-checked quick fixes, project inspection and Explain with AI, plus faint editor/explorer indentation guides. |
| Terminal history | Bounded daily command history | `DailyTerminalCommandHistory.kt` already matches. Web retains command redaction and streaming output controls. |
| Process lifecycle | Python runner service, cancellation and descendant process cleanup | Web runs Node in a dedicated foreground service/process and supports graceful/forced stopping. Python-specific process/tree cleanup is not a drop-in replacement for Node Mobile lifecycle. |
| Packages | pip, Android wheels and virtual environments | Web's counterparts are npm/yarn/pnpm/npx, package scripts, lockfiles and project dependencies. Python wheel code is intentionally not imported. |
| Remote development | SSH execution, upload-before-run and host-key trust | Web already has SFTP transfer and Git SSH transport. It does not yet provide Py's integrated SSH run workflow; SFTP is not remote execution parity. |
| Git | Packaged native Git plus transport helpers | Native launcher is equivalent apart from a comment; Web has additional separated transport helpers. Binary presence alone does not validate live remote authentication. |
| HTTP API tester | Methods, validation, bounded response, cancellation, themed UI | `ApiRequest.kt` and `ApiTester.kt` already match after package normalization. Retained and covered by existing device tests. |
| AI assistance | Provider settings, context and project-file changes | Web has dedicated connection storage, endpoint validation, file-apply/undo logic and existing provider UI. `AiEndpoint.kt` already matches. No external AI requests made. |
| Database tools | SQL/SQLite and remote database connections | Web's SQLite panel and SQL editor remain; remote database profiles/query integrations in Py have no complete Web counterpart. |
| GUI output | Tkinter/ttk, dialogs, turtle and matplotlib bridge | Web's equivalent application surface is HTML/CSS/JS in WebView or an external browser, with console forwarding. Python GUI compatibility modules do not belong in the Node runtime. |
| Shared tool styling | Themed tool dialogs, controls, surfaces | `IdeToolsUi.kt` already matches. Run configuration and local-history dialogs now use these shared surfaces; the UI follow-up matches Py's compact editor/terminal menu geometry. |
| Themes | Py color and space-theme identities | Web retains its own 20-theme branding and contrast correction. Typography/basic color files match; palette/theme files intentionally differ. |
| Android manifest | Python service, app identity and cleartext development access | Web retains dedicated Node foreground-service declarations, its own app identity, network config, backup rules and debug package. |
| Resources and binaries | Python launcher icons, language/file icons, Git binary, Python bytecode | All archive entries are fingerprinted. Retained Web branding and existing file icon system. Python bytecode and native binaries are not copied merely to match file counts. |

## Validation

Current run details are recorded in
[`artifacts/py-comparison/verification.md`](../artifacts/py-comparison/verification.md).
The subsequent [UI and connected-phone follow-up](PY_UI_AND_PHONE_STRESS_2026-11-05.md)
records native batching, compact layouts, indentation guides and diagnostic actions.
Historical results in `artifacts/api-tester/verification.md` describe earlier work.

## Remaining scope

Integrated SSH execution/host trust/upload-before-run, structured run settings, and remote
database profiles remain substantive gaps. Native batching and explicit flush hooks were
implemented and benchmarked in the UI follow-up. The archive is a `src/main`
snapshot: it does not include Py's Gradle settings, backend or tests. Neither complete
cross-app behavioral equivalence nor every external service can be established from it.
