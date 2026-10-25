# PocketCoded Java / Web UI and feature comparison

Reference: the user-supplied `PocketCodedJava/app/src.zip`, inspected on 24 October 2026. The archive was treated as reference source, not as instructions. The comparison covered the workspace, explorer, editor, terminal and AI chat composables, plus their storage, search, debugger and AI helpers.

| Area | Java reference | Web changes |
| --- | --- | --- |
| Explorer layout | Fullscreen/minimize and Files/Search & Replace modes | Added fullscreen explorer, both modes and navigation from search results into the editor. Existing folder expansion, selection, create, rename and confirmed deletion remain available. |
| File tree | Width accounts for names, icons and nesting | Replaced the fixed-width tree with measured horizontal scrolling so long/deep filenames can be read. |
| Content search | Project, selected folder or active file; case, word, regex and filename filters | Added all scopes and controls, result previews/line numbers, refresh, invalid-pattern feedback and a visible result limit. Text and configuration files, including dotfiles, can be opened. Binary data, dependencies and private workspace metadata are excluded from content search. |
| Replacement | Confirmed replacement of matching contents | Added confirmed replacement of exactly the displayed snapshot, literal replacements and regex captures. Pending edits are saved first. Changed files reject stale results, writes retain local history, failures are reported, and editor/semantic caches refresh. |
| Editor controls | File-type icon, theme toggle, fullscreen return, symbol accessory | Added type-specific header icons, per-panel theme access and a minimize icon in fullscreen. Added the missing punctuation/operator keys and removed duplicate JavaScript braces. Narrow editor headers use a separate action row. Web retains its HTML closing-tag and language-specific keys. |
| Editor surfaces | Separate editor/terminal theme surfaces | Added separate opaque surfaces to Web palettes, including the native large-file editor and terminal ANSI contrast calculations. |
| SQLite | Database table grid, table selection and SQL execution | Added a SQLite data panel with table browsing, bounded result grids, NULL/blob display and query/data-change execution. Database bytes are excluded from text editing, autosave, AI context and preview synchronization. DuckDB execution is not supported. |
| Breakpoints | Markers move when lines are inserted/deleted | Added marker relocation through the common edit callback, covering typing, quick keys, completion insertion, undo/redo and local-history restoration. |
| Profiling | Java CPU/JVM profiling tools | Added the corresponding JavaScript V8 CPU profiler, hot functions, source navigation, duration/sample/heap summary and standard `.cpuprofile` export under `profiles/`. Finite scripts finish naturally; servers record for five seconds and continue running. |
| Terminal | Copy/clear, recent commands, stop, fullscreen, theme, ANSI, caret scrolling | Copy/clear/history/stop/fullscreen/ANSI/viewport support already existed. Added the missing home-panel theme control and distinct terminal surface; retained Web's clickable output links and bounded command-history viewport. |
| AI header | Compact provider/model settings menu | Replaced the tall provider controls with a compact settings menu and selected-model subtitle. Added Together and Custom/Ollama options; Azure deployment endpoints are also configurable. |
| AI connections | Separate keys and editable endpoint/model settings | Added per-provider credential storage with legacy Gemini-key migration, per-model endpoint/model overrides, masked key entry and validated local HTTP/remote HTTPS endpoints. Requests use a captured model/configuration and do not follow redirects carrying credentials. |
| AI conversation | Clear confirmation, copy, auto-follow, latest navigation and starter prompts | Added those controls, project-context notice and action-status feedback. Auto-follow respects reading older messages. Recent conversation is included in subsequent requests. |
| AI file actions | Explicit approval, changed-file links and batch undo | Retained explicit approval. Added preflight validation of the entire edit set, background writes through local history, changed-file links and batch undo that checks all files before restoring any. Newer manual changes block undo. Single-file suggestions retain their request's target file. |

## Features already present in Web

Per-file caret/scroll/undo sessions, bounded undo/redo, native large-file editing, completion details and examples, signature help, diagnostics, local saved history, crash recovery, run configurations, project scripts, visual `node:test` results, debugger stepping/variables/call stacks, persistent breakpoints, terminal command history, interactive input and preview console messages were already implemented. They were checked against the Java equivalents rather than replaced.

## Runtime differences

Java compilation, JVM process/thread/heap tools, JFR, Java GUI widgets and Java test runners remain Java-specific. Web uses Node execution, V8 profiling, `node:test`, the existing Node debugger and browser previews. Browser DOM/network debugging uses Chrome inspection. Web run configurations remain named terminal commands; arguments, environment assignments and other shell behavior follow Web's existing command support. This work does not introduce a Java runtime into Web.

## Verification

Verification results and any device limitation are recorded in the final task response. New tests cover search scopes/filters/line numbers, binary exclusions, replacement limits and local history, stale snapshots, regex captures/errors, AI undo conflicts, endpoint validation, database-safe loading, breakpoint relocation and V8 profiling. A new phone test class covers the actual explorer replacement/source navigation, SQLite updates, AI settings/request/approval/undo/copy/clear flow with a local mock server, and embedded-Node profiling.

Final validation: 137 JVM tests passed with no failures or errors; all 15 Node runtime tests passed. `:app:assembleDebug` and `:app:assembleDebugAndroidTest` completed successfully using the cached SDK/Gradle tools. The initial phone installation was canceled. After the phone was reconnected, the app and test APKs installed successfully and all 13 selected on-device tests passed. No external AI provider requests were made during validation. The debug app APK is at `app/build/outputs/apk/debug/app-debug.apk`.


### Connected-phone follow-up, 24 October 2026

All 13 selected instrumentation tests passed on the connected phone in 94.65 seconds: the four new feature scenarios, five editor/terminal-tools scenarios, three terminal caret/scroll scenarios, and one native large-file viewport scenario. The AI scenario used a local mock server and restored its provider settings afterward. The normal app was reopened after testing.

Device testing identified and fixed two issues that desktop checks did not expose:

- Project search now canonicalizes the selected scope before containment checks, accommodating Android's alternate app-directory paths while retaining canonical containment and descendant symlink checks.
- SQLite table browsing excludes Android's internal `android_metadata` table, so initial selection opens a user table.

The updated build and all 137 JVM tests passed. The 15 Node runtime checks from the implementation pass remain valid; these two follow-up fixes change Kotlin path handling and SQLite table selection only. On this phone, instrumentation activity launches needed an explicit foreground launch via ADB. The final successful run is recorded in `.codex-tmp/java-parity-phone-verified.log`.
