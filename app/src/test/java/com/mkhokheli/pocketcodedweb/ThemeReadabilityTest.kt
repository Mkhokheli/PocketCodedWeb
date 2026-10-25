package com.mkhokheli.pocketcodedweb

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import com.mkhokheli.pocketcodedweb.ui.theme.*
import org.junit.Assert.*
import org.junit.Test

class ThemeReadabilityTest {
    private fun readable(label: String, ink: Color, surface: Color) {
        assertTrue("$label contrast=${textContrast(ink, surface)}", textContrast(ink, surface) >= 4.49f)
    }

    @Test fun allIdeTextRolesStayReadableAcrossEveryThemeSurface() {
        WebTheme.entries.forEach { theme ->
            val p = webIdePalette(theme)
            val surfaces = listOf(p.backgroundTop, p.backgroundBottom, p.panel, p.elevatedPanel,
                p.editorBackground, p.terminalBackground, p.tab, p.selectedTab) +
                listOf(p.accent, p.error, p.warning, p.success, p.info).map { it.copy(alpha = 0.18f).compositeOver(p.panel) }
            val text = listOf(p.text, p.mutedText, p.subtleText, p.accent, p.error, p.warning, p.success, p.info, p.terminalPrompt)
            surfaces.forEachIndexed { surface, color -> text.forEachIndexed { role, ink -> readable("$theme surface=$surface role=$role", ink, color) } }
            readable("$theme run label", p.runText, p.run)
            readable("$theme accent label", p.onAccent, p.accent)
        }
    }

    @Test fun entryAndAuthenticationTextIsReadableInAllFourteenThemes() {
        WebTheme.entries.forEach { theme ->
            val p = entryPalette(theme.name, theme.isDark)
            listOf(p.background, p.panel, p.elevatedPanel).forEach { surface ->
                listOf(p.text, p.mutedText, p.accent, p.accentAlt, p.error).forEach { readable("Entry $theme", it, surface) }
            }
            readable("Auth $theme", readableContentColor(p.accent), p.accent)
        }
    }

    @Test fun materialControlsAndDialogContainersUseMatchingContentColors() {
        WebTheme.entries.forEach { theme ->
            val c = webThemeColorScheme(theme.isDark, theme.name)
            listOf(c.surface, c.surfaceVariant, c.surfaceContainer, c.surfaceContainerHigh, c.surfaceContainerHighest).forEach {
                readable("$theme body", c.onSurface, it)
                readable("$theme hint/disabled", c.onSurfaceVariant, it)
                readable("$theme text button", c.primary, it)
                readable("$theme error", c.error, it)
            }
            listOf(c.onPrimary to c.primary, c.onSecondary to c.secondary,
                c.onPrimaryContainer to c.primaryContainer, c.onSecondaryContainer to c.secondaryContainer,
                c.onErrorContainer to c.errorContainer, c.inverseOnSurface to c.inverseSurface).forEach {
                readable("$theme filled control", it.first, it.second)
            }
        }
    }

    @Test fun syntaxAndAllAnsiColorsAreReadableInEveryTheme() {
        val sources = mapOf("index.html" to "<!-- comment --><div id=\"value\">Hello</div>",
            "style.css" to "/* comment */ .box { color: #f00; width: 2px; }",
            "script.js" to "// comment\nconst value = true; console.log('hello', 42);",
            "data.json" to "{\"hello\": true, \"number\": 42}",
            "query.sql" to "SELECT name FROM users WHERE id = 2;",
            "main.py" to "# comment\ndef greet(): return 'hello'",
            "main.php" to "<?php // comment\necho 'hello'; ?>")
        WebTheme.entries.forEach { theme ->
            val p = webIdePalette(theme)
            sources.forEach { (name, source) ->
                val highlighted = highlightWebSource(name, source, p)
                assertSpans("$theme $name", highlighted, p.editorBackground)
                assertSpans("$theme selected $name", highlighted, p.accent.copy(alpha = 0.18f).compositeOver(p.editorBackground))
            }
            val ansi = (0..255).joinToString(" ") { "\u001B[38;5;${it}mcolor$it" } +
                "\u001B[38;2;0;0;0mblack \u001B[38;2;255;255;255mwhite\u001B[0m\nerror failed\nsuccessfully installed"
            assertSpans("$theme ANSI", parseAnsiTerminalOutput(ansi, "~$", p), p.terminalBackground)
            val link = linkifyTerminalOutput(AnnotatedString("http://localhost:3000"), p.terminalBackground)
                .getLinkAnnotations(0, 21).single().item as LinkAnnotation.Url
            readable("$theme terminal link", link.styles!!.style!!.color, p.terminalBackground)
        }
    }

    private fun assertSpans(label: String, text: AnnotatedString, background: Color) {
        text.indices.forEach { offset ->
            val style = text.spanStyles.filter { offset >= it.start && offset < it.end }
                .fold(SpanStyle()) { style, range -> style.merge(range.item) }
            if (style.color.isSpecified) readable(label, style.color,
                if (style.background.isSpecified) style.background.compositeOver(background) else background)
        }
    }

    @Test fun overlappingHighlightAndDiagnosticSpansPreserveTextAndAnnotations() {
        val original = AnnotatedString.Builder("abcdef").apply {
            addStyle(SpanStyle(color = Color(0xFFAAAAAA)), 0, 6)
            addStyle(SpanStyle(color = Color.Blue), 2, 4)
            addStyle(SpanStyle(background = Color.Red.copy(alpha = 0.2f)), 1, 5)
            addStringAnnotation("source", "unchanged", 0, 6)
        }.toAnnotatedString()
        val result = original.withReadableText(Color.White)
        assertEquals(original.text, result.text)
        assertEquals(original.getStringAnnotations(0, 6), result.getStringAnnotations(0, 6))
        assertSpans("overlap", result, Color.White)
    }
}
