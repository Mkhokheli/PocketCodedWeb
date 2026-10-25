# PocketCodedWeb space themes

Inspired by [PocketCodedWeb on MKH-N Industries](https://mkhn-industries.com/products/pocketcoded-web/), inspected 25 October 2026. The page sky uses midnight `#070C17`, cyan `#67D2EF` and orbital blue `#789DF4`; product CSS also supplies green `#70E85D`. Supporting shades and light variants are app adaptations.

| Pair | Dark surface / accent | Light surface / accent |
| --- | --- | --- |
| Cyan Orbit | `#070C17` / `#67D2EF` | `#F2FBFE` / `#14677E` |
| Blue Horizon | `#090E1D` / `#789DF4` | `#F5F7FE` / `#3457A3` |
| Green Comet | `#071410` / `#8BDD91` | `#F5FCF6` / `#296738` |

All six are appended to the existing theme cycle. Existing saved themes remain valid. Shared definitions in `ui/theme/SpaceThemes.kt` supply the IDE, project/authentication screens and Material dialogs. Explorer/editor/terminal retain aligned surfaces and existing contrast correction. Static colors use the existing gradient, with no additional animations or image assets. Existing readability tests cover text, controls, syntax and ANSI terminal colors across all themes.
