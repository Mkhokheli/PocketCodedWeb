package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.mkhokheli.pocketcodedweb.ui.theme.*
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ThemeReadabilityDeviceTest {
    @get:Rule val compose = createComposeRule()

    private fun assertInk(label: String, surface: Color) {
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(label, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue("No rendered text for $label", layouts.isNotEmpty())
        val ink = layouts.first().layoutInput.style.color
        assertTrue("Low contrast for $label: ${textContrast(ink, surface)}", textContrast(ink, surface) >= 4.49f)
    }

    @Test fun actualFieldsButtonsAndDisabledTextStayReadableThroughAllThemes() {
        var theme by mutableStateOf(WebTheme.entries.first())
        compose.setContent {
            PocketCodedWebTheme(theme.isDark, theme.name, dynamicColor = false) {
                Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
                    Text("Body label")
                    TextButton(onClick = {}, colors = readableTextButtonColors()) { Text("Text action") }
                    Button(onClick = {}, colors = readableButtonColors()) { Text("Filled action") }
                    Button(onClick = {}, enabled = false, colors = readableButtonColors()) { Text("Unavailable action") }
                    OutlinedTextField(value = "Sample input", onValueChange = {}, label = { Text("Input label") }, colors = readableFieldColors())
                    OutlinedTextField(value = "Disabled input", onValueChange = {}, enabled = false, colors = readableFieldColors())
                    DropdownMenuItem(text = { Text("Unavailable menu item") }, onClick = {}, enabled = false, colors = readableMenuItemColors())
                }
            }
        }
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Body label").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        WebTheme.entries.forEach { next ->
            compose.runOnIdle { theme = next }
            compose.waitForIdle()
            val c = webThemeColorScheme(next.isDark, next.name)
            listOf("Body label", "Text action", "Sample input", "Disabled input", "Input label", "Unavailable menu item").forEach { assertInk(it, c.surface) }
            assertInk("Filled action", c.primary)
            assertInk("Unavailable action", c.surfaceVariant)
        }
    }

    @Test fun realExplorerEditorTerminalAndAiSettingsStayReadableAcrossThemeSwitches() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NativeNodeRuntime.initialize(context)
        val root = File(context.filesDir, "theme-readability-${System.nanoTime()}").apply {
            mkdirs(); File(this, "index.html").writeText("<h1>Theme test heading</h1>")
        }
        var theme by mutableStateOf(WebTheme.entries.first())
        try {
            compose.setContent { WebIdeApp(theme, {}, root, {}) }
            compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Fullscreen explorer").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
            WebTheme.entries.forEach { next ->
                compose.runOnIdle { theme = next }
                compose.waitForIdle()
                val p = webIdePalette(next)
                assertInk("EXPLORER", p.panel)
                assertInk("Code Editor - HTML", p.editorBackground)
                compose.onNodeWithText("Terminal Output").performScrollTo()
                assertInk("Terminal Output", p.terminalBackground)
                compose.onNodeWithContentDescription("Fullscreen explorer").performScrollTo().performClick()
                compose.onNodeWithText("Search & Replace").performClick()
                compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("heading")
                assertInk("heading", webThemeColorScheme(next.isDark, next.name).surface)
                compose.onNodeWithContentDescription("Minimize explorer").performClick()
                compose.onNodeWithContentDescription("Open AI chat").performScrollTo().performClick()
                assertInk("AI Chat", p.panel)
                compose.onNodeWithContentDescription("AI settings").performClick()
                assertInk("API settings", p.elevatedPanel)
                compose.onNodeWithText("API settings").performClick()
                assertInk("API endpoint URL", webThemeColorScheme(next.isDark, next.name).surface)
                compose.onNodeWithText("Cancel").performClick()
                compose.onNodeWithContentDescription("Close AI chat").performClick()
            }
        } finally { root.deleteRecursively() }
    }
}
