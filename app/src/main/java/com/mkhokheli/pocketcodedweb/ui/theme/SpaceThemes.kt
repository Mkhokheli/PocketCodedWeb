package com.mkhokheli.pocketcodedweb.ui.theme

import androidx.compose.ui.graphics.Color

/** Static palettes inspired by https://mkhn-industries.com/products/pocketcoded-web/.
 * Web sky #070c17, core #67d2ef, glow #789df4; product green #70e85d.
 * Light variants darken accents for readable controls. Shared by IDE, entry and Material UI.
 */
internal data class SpaceThemeColors(
    val isDark: Boolean,
    val background: Color,
    val gradientEnd: Color,
    val panel: Color,
    val elevated: Color,
    val accent: Color,
    val secondary: Color,
    val border: Color,
    val run: Color,
)

private val spaceThemePalettes = mapOf(
    "CyanOrbitDark" to SpaceThemeColors(
        isDark = true,
        background = Color(0xFF070C17),
        gradientEnd = Color(0xFF0C2330),
        panel = Color(0xFF0B1826),
        elevated = Color(0xFF0F2130),
        accent = Color(0xFF67D2EF),
        secondary = Color(0xFF9DB8FF),
        border = Color(0xFF33566F),
        run = Color(0xFF155B71),
    ),
    "CyanOrbitLight" to SpaceThemeColors(
        isDark = false,
        background = Color(0xFFF2FBFE),
        gradientEnd = Color(0xFFDDEFF7),
        panel = Color(0xFFFFFFFF),
        elevated = Color(0xFFEAF6FC),
        accent = Color(0xFF14677E),
        secondary = Color(0xFF355DA3),
        border = Color(0xFF91B6C9),
        run = Color(0xFF14677E),
    ),
    "BlueHorizonDark" to SpaceThemeColors(
        isDark = true,
        background = Color(0xFF090E1D),
        gradientEnd = Color(0xFF192646),
        panel = Color(0xFF101A30),
        elevated = Color(0xFF16223B),
        accent = Color(0xFF789DF4),
        secondary = Color(0xFF7BDDF1),
        border = Color(0xFF3E577E),
        run = Color(0xFF395B9C),
    ),
    "BlueHorizonLight" to SpaceThemeColors(
        isDark = false,
        background = Color(0xFFF5F7FE),
        gradientEnd = Color(0xFFE1E8FC),
        panel = Color(0xFFFFFFFF),
        elevated = Color(0xFFEDF1FC),
        accent = Color(0xFF3457A3),
        secondary = Color(0xFF176A7D),
        border = Color(0xFF9BACD0),
        run = Color(0xFF3457A3),
    ),
    "GreenCometDark" to SpaceThemeColors(
        isDark = true,
        background = Color(0xFF071410),
        gradientEnd = Color(0xFF102C25),
        panel = Color(0xFF0C211C),
        elevated = Color(0xFF122D25),
        accent = Color(0xFF8BDD91),
        secondary = Color(0xFF67D2EF),
        border = Color(0xFF3B6953),
        run = Color(0xFF23613E),
    ),
    "GreenCometLight" to SpaceThemeColors(
        isDark = false,
        background = Color(0xFFF5FCF6),
        gradientEnd = Color(0xFFE0F0DE),
        panel = Color(0xFFFFFFFF),
        elevated = Color(0xFFEBF6EA),
        accent = Color(0xFF296738),
        secondary = Color(0xFF1B6572),
        border = Color(0xFF9ABA98),
        run = Color(0xFF296738),
    ),
)

internal fun spaceThemeColors(name: String?): SpaceThemeColors? = spaceThemePalettes[name]
