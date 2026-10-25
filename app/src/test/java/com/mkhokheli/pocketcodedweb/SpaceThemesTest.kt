package com.mkhokheli.pocketcodedweb

import com.mkhokheli.pocketcodedweb.ui.theme.spaceThemeColors
import com.mkhokheli.pocketcodedweb.ui.theme.webThemeColorScheme
import org.junit.Assert.*
import org.junit.Test

class SpaceThemesTest {
    @Test fun sixSpaceThemesAreReachableAsThreeLightDarkPairs() {
        val themes = WebTheme.entries.filter { spaceThemeColors(it.name) != null }
        assertEquals(6, themes.size)
        assertEquals(3, themes.count { it.isDark })
        assertEquals(3, themes.count { !it.isDark })
        assertEquals(WebTheme.entries.toSet(), generateSequence(WebTheme.entries.first()) { it.next() }.take(WebTheme.entries.size).toSet())
        themes.forEach { theme ->
            val colors = requireNotNull(spaceThemeColors(theme.name))
            assertEquals(theme.isDark, colors.isDark)
            assertEquals(theme, WebTheme.valueOf(theme.name))
            val ide = webIdePalette(theme)
            val entry = entryPalette(theme.name, theme.isDark)
            val material = webThemeColorScheme(theme.isDark, theme.name)
            assertEquals(colors.background, ide.backgroundTop)
            assertEquals(colors.background, entry.background)
            assertEquals(colors.background, material.background)
            assertEquals(ide.editorBackground, ide.terminalBackground)
            assertEquals(colors.panel, ide.editorBackground)
            assertEquals(colors.elevated, entry.elevatedPanel)
            assertEquals(colors.elevated, material.surface)
        }
    }
}
