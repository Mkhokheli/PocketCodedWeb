# Py UI, Web themes, and connected-phone stress testing

The supplied Py `main.zip` is a source reference, not an instruction document.
The user's final constraint is to retain Web's colours and themes while bringing across
Py's UI, UX, individual features and optimisations.

## Implemented

- Retained Web's original 20 theme names, ordering, cycling, palette generation, Material
  colours and space palettes. `Theme.kt`, `SpaceThemes.kt`, the `WebTheme` enum and IDE
  palette functions were compared against their original definitions.
- Ported Py's 14 dp explorer divider, drag affordance and width constraints using its
  measured three-pane layout instead of the older weighted row and text grip.
- Ported lazy file tabs, stable item keys, active-tab reveal and the fullscreen tab strip.
- Ported Py's edge-to-edge fullscreen editor and square outer run control, preserving
  language-specific Web actions and colours.
- Matched compact explorer headers and overflow actions, plus the 28 dp Files/Search &
  Replace view tabs. File-tree structure now stays independent of saved file contents.
- Applied Py's authentication layout dimensions, floating labels, field/button sizes and
  form spacing while retaining Web authentication, profile and purchase callbacks.
- Added the explicit `Save now` tool action and matched Py's compact editor/terminal tool
  controls. Local history uses the reference row borders, selection controls and preview
  layout while retaining Web's undoable restoration path.
- Adopted Py's 120 ms native-editor publication debounce. Typing remains in `Editable`
  until a burst settles; the workspace synchronously flushes before run, save, search,
  history, AI snapshots, file/layout switches and background draft staging. Undo and
  shortcut operations derive their edit from the freshly flushed value.
- Protected pending native typing from stale binds, invalidated/deleted sessions and
  premature selection callbacks; flushes also run on view disposal and session eviction.
- After measuring a large explorer opening stall, moved filename-width measurement to a
  cancellable worker using its own default-font Paint and inherited letter spacing. The
  UI retains stable row widths and horizontal scrolling without shaping thousands of
  Compose paragraphs during its opening frame.
- Added faint indentation rails to both editor implementations, including continuity
  through blank lines, plus nested explorer rails and Py's `empty` / `pkg` folder badges.
  These use the existing Web palette; native rails draw from the visible Editable lines.
- Added undoable inline quick fixes and a project errors/warnings dialog with source
  navigation, previews, Quick fix and Explain with AI. Fixes validate the source snapshot
  before editing; inactive-file fixes retain local history. AI explanations use the
  selected file and diagnostic with the existing Web provider/model configuration.
- Project inspection prioritises the active draft, runs off the UI thread, supports
  cancellation and bounds file, byte and diagnostic counts. Dependency/build directories,
  binary files and paths outside the project are excluded.

## Evidence

The 6 November follow-up passed 174 JVM tests and all 42 connected-phone tests on
the updated APK. Both APK installations succeeded before that final phone run.

See [verification and measured results](../artifacts/py-ui-stress/verification.md),
[latest build output](../artifacts/py-ui-stress/details-build.log),
[initial 35-test phone run](../artifacts/py-ui-stress/phone-stress.log), and
[targeted rerun](../artifacts/py-ui-stress/phone-optimised-rerun.log).

The initial phone run passed 34/35. The remaining assertion expected 2,507 editable
files but omitted the fixture's readable `.fixture-v2` marker. The assertion now includes
the marker explicitly and independently verifies that the oversized file stays excluded.
That was a fixture expectation correction, not a change to which user files are editable.

Benchmarks are observations on one connected Android 13 phone, not universal performance
guarantees. Rendering still includes one-time editor and font setup; steady scrolling and
opening-frame measurements are reported separately. No speed-up percentage is claimed
from the noisy end-to-end measurements.
