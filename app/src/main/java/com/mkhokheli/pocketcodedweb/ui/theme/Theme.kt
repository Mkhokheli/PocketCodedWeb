package com.mkhokheli.pocketcodedweb.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF77A7FF),
    onPrimary = Color(0xFF07162C),
    primaryContainer = Color(0xFF223E64),
    onPrimaryContainer = Color(0xFFEAF1FF),
    secondary = Color(0xFF7DE3FF),
    onSecondary = Color(0xFF06222A),
    background = Color(0xFF0B1730),
    onBackground = Color.White,
    surface = Color(0xFF142B4D),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF102440),
    onSurfaceVariant = Color(0xFFD8DEEB),
    outline = Color(0xFF557095),
    error = Color(0xFFFF7B7B),
    onError = Color(0xFF3B0808)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF315ECD),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7EEFF),
    onPrimaryContainer = Color(0xFF152B62),
    secondary = Color(0xFF075E73),
    onSecondary = Color.White,
    background = Color(0xFFE9EEFF),
    onBackground = Color(0xFF151A24),
    surface = Color.White,
    onSurface = Color(0xFF151A24),
    surfaceVariant = Color(0xFFF2F5FC),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF6B7A90),
    error = Color(0xFFB42318),
    onError = Color.White
)

@Composable
fun PocketCodedWebTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val themed = webThemeColors(themeName)
    val accent = themed?.accent ?: baseColorScheme.primary
    val background = themed?.background ?: baseColorScheme.background
    val surface = themed?.surface ?: baseColorScheme.surface
    val surfaceVariant = themed?.surfaceVariant ?: baseColorScheme.surfaceVariant
    val outline = themed?.outline ?: baseColorScheme.outline
    val onSurface = readableOn(surface)
    val colorScheme = baseColorScheme.copy(
        primary = accent,
        onPrimary = readableOn(accent),
        primaryContainer = accent.copy(alpha = if (darkTheme) 0.24f else 0.14f).compositeOver(surface),
        onPrimaryContainer = onSurface,
        secondary = accent,
        onSecondary = readableOn(accent),
        secondaryContainer = accent.copy(alpha = if (darkTheme) 0.18f else 0.10f).compositeOver(surface),
        onSecondaryContainer = onSurface,
        background = background,
        onBackground = readableOn(background),
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurface.copy(alpha = 0.76f),
        surfaceTint = accent,
        outline = outline,
        outlineVariant = outline.copy(alpha = 0.55f),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private data class WebThemeColors(
    val accent: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val outline: Color,
)

private fun webThemeColors(name: String?): WebThemeColors? = when (name) {
    "CyberpunkElectricDark" -> WebThemeColors(Color(0xFFFF2D75), Color(0xFF070617), Color(0xFF0A0C24), Color(0xFF1A1033), Color(0xFF8F355E))
    "CyberpunkElectricLight" -> WebThemeColors(Color(0xFFFF2D75), Color(0xFFFFFBF8), Color.White, Color(0xFFFFE7F0), Color(0xFFFFC4DC))
    "ForestNightDark" -> WebThemeColors(Color(0xFF22C55E), Color(0xFF031611), Color(0xFF042018), Color(0xFF0E3B2B), Color(0xFF247448))
    "ForestNightLight" -> WebThemeColors(Color(0xFF22C55E), Color(0xFFF7FFF9), Color.White, Color(0xFFECFDF5), Color(0xFFD1FAE5))
    "SunsetDuskDark" -> WebThemeColors(Color(0xFFFB923C), Color(0xFF1E0F0B), Color(0xFF180F18), Color(0xFF3A2118), Color(0xFF8C5128))
    "SunsetDuskLight" -> WebThemeColors(Color(0xFFFB923C), Color(0xFFFFFDF9), Color.White, Color(0xFFFFF0D0), Color(0xFFFED7AA))
    "ArcticAuroraDark" -> WebThemeColors(Color(0xFF06B6D4), Color(0xFF031A24), Color(0xFF052536), Color(0xFF0A4054), Color(0xFF26768A))
    "ArcticAuroraLight" -> WebThemeColors(Color(0xFF06B6D4), Color(0xFFF7FEFF), Color.White, Color(0xFFF0FDFF), Color(0xFFBAE6FD))
    "VioletHazeDark" -> WebThemeColors(Color(0xFFA78BFA), Color(0xFF1A123A), Color(0xFF201540), Color(0xFF3B2868), Color(0xFF7056A8))
    "VioletHazeLight" -> WebThemeColors(Color(0xFFA78BFA), Color(0xFFFAF5FF), Color.White, Color(0xFFEDE9FE), Color(0xFFDDD6FE))
    "MonochromeProDark" -> WebThemeColors(Color(0xFF64748B), Color(0xFF111827), Color(0xFF111827), Color(0xFF273244), Color(0xFF566477))
    "MonochromeProLight" -> WebThemeColors(Color(0xFF64748B), Color.White, Color.White, Color(0xFFF3F4F6), Color(0xFFE5E7EB))
    "VSCodeDefaultDark" -> WebThemeColors(Color(0xFF007ACC), Color(0xFF1E1E1E), Color(0xFF252526), Color(0xFF2D2D30), Color(0xFF3C3C3C))
    "VSCodeDefaultLight" -> WebThemeColors(Color(0xFF007ACC), Color.White, Color.White, Color(0xFFF3F3F3), Color(0xFFE5E5E5))
    else -> null
}

private fun readableOn(background: Color): Color =
    if (background.luminance() > 0.179f) Color(0xFF111318) else Color.White
