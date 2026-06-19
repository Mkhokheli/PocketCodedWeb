package com.mkhokheli.pocketcodedpy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
fun PocketCodedPyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
