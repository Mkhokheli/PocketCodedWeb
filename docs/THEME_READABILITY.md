# Theme text readability

The app's 14 light and dark themes share contrast checks for their text roles. Foreground colors retain their theme hue when possible and are adjusted toward black or white when their measured contrast against the actual surface falls below 4.5:1. Filled buttons choose their own contrasting label color.

The changes cover authentication and project screens, explorer/search, editor, terminal, SQLite, AI chat/settings, development panels, menus and dialogs. Material surface/container tokens follow the selected theme, including elevated dialogs, rather than retaining unrelated defaults from the system theme. Disabled text and input placeholders use readable muted colors instead of additional fading.

Editor highlighting corrects overlapping foreground and diagnostic-background spans while preserving annotations. Both Compose and native large-file editors use those colors, including a readable selection highlight. Terminal ANSI, true-color and link colors are checked against the terminal surface. The preview still displays the project's own HTML/CSS.

`ThemeReadabilityTest` covers all theme palettes, button labels, Material containers, authentication colors, syntax samples, all 256 ANSI colors, true-color extremes, links and overlapping annotation spans. `ThemeReadabilityDeviceTest` measures the text colors actually used by fields/buttons and switches the live IDE, explorer and AI settings through every theme.

The final app/test APK builds and all 142 JVM tests passed, including selection-highlight contrast. The updated debug app was installed on the connected phone. After the phone was unlocked, all 15 selected on-device tests passed in 134.247 seconds. This includes both rendered-text tests across all 14 themes and the explorer, SQLite, AI chat, profiler, editor, terminal and native-editor regressions. The earlier locked-screen attempts were infrastructure failures. The temporary stay-awake setting was restored to its original value of 0. Results: `.codex-tmp/theme-readability-unlocked.log`.
