package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TerminalCursorDeviceTest {
    @get:Rule val compose = createComposeRule()
    private val palette = WebPalette(
        backgroundTop = Color.Black, backgroundBottom = Color.Black, panel = Color.Black,
        elevatedPanel = Color.DarkGray, tab = Color.DarkGray, selectedTab = Color.Gray,
        text = Color.White, mutedText = Color.LightGray, subtleText = Color.Gray,
        border = Color.Gray, strongBorder = Color.Gray, accent = Color.Cyan,
        run = Color.Green, runText = Color.Black, terminalPrompt = Color.Green,
        divider = Color.Gray, error = Color.Red, warning = Color.Yellow, success = Color.Green, info = Color.Cyan,
    )

    @Test fun focusedMultilineInputStaysVisibleAfterOutputPromptAndViewportChanges() {
        var output by mutableStateOf("Ready")
        var input by mutableStateOf("")
        var prompt by mutableStateOf("project >")
        var height by mutableStateOf(240.dp)
        compose.setContent {
            MaterialTheme {
                TerminalBody(output, input, palette, prompt, false, true, false,
                    Modifier.width(240.dp).height(height), {}, { input = it }, {})
            }
        }
        compose.onNodeWithTag("terminal-input").performClick()
        compose.onNodeWithTag("terminal-input").performTextReplacement("first line\n" + "x".repeat(100))
        compose.runOnIdle { output = (1..300).joinToString("\n") { "output $it" } }
        compose.waitForIdle()
        assertCaretVisible()

        compose.runOnIdle { height = 130.dp; prompt = "a much longer project prompt >" }
        compose.waitForIdle()
        assertCaretVisible()
    }

    @Test fun appendedOutputPreservesAnUnfocusedManualViewport() {
        var output by mutableStateOf((1..300).joinToString("\n") { "output $it" })
        compose.setContent {
            MaterialTheme {
                TerminalBody(output, "", palette, "project >", false, true, false,
                    Modifier.width(240.dp).height(240.dp), {}, {}, {})
            }
        }
        val viewport = compose.onNodeWithTag("terminal-vertical-scroll")
        viewport.performTouchInput { swipeUp() }
        compose.waitForIdle()
        fun position() = viewport.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
        val manualPosition = position()
        assertTrue("The fixture scrolls through earlier output", manualPosition > 0f)
        compose.runOnIdle { output += "\n" + (301..400).joinToString("\n") { "output $it" } }
        compose.waitForIdle()
        assertEquals("Background output must preserve manual browsing", manualPosition, position(), 1f)
    }

    @Test fun asynchronousInputPromptsRevealTheNewInputWithoutAFirstKeystroke() {
        var output by mutableStateOf("Starting")
        var awaiting by mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                TerminalBody(output, "", palette, "project >", true, false, awaiting,
                    Modifier.width(240.dp).height(180.dp), {}, {}, {})
            }
        }
        compose.onNodeWithTag("terminal-input").assertDoesNotExist()
        compose.runOnIdle {
            output = (1..300).joinToString("\n") { "output $it" } + "\nEnter a value: "
            awaiting = true
        }
        compose.waitForIdle()
        // Await the asynchronous prompt-focus effect rather than assuming
        // waitForIdle also waits for its short layout delay.
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("terminal-input").fetchSemanticsNodes().singleOrNull()
                ?.let { it.config[SemanticsProperties.Focused] } == true
        }
        compose.waitForIdle()
        compose.onNodeWithTag("terminal-input").assertIsFocused()
        assertCaretVisible()
    }

    private fun assertCaretVisible() {
        val input = compose.onNodeWithTag("terminal-input")
        val layouts = mutableListOf<TextLayoutResult>()
        input.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(layouts)) }
        val node = input.fetchSemanticsNode()
        val selection = node.config[SemanticsProperties.TextSelectionRange]
        val caret = layouts.single().getCursorRect(selection.end)
        val horizontal = compose.onNodeWithTag("terminal-horizontal-scroll").fetchSemanticsNode().boundsInRoot
        val vertical = compose.onNodeWithTag("terminal-vertical-scroll").fetchSemanticsNode().boundsInRoot
        assertTrue("Caret remains inside the horizontal terminal viewport",
            node.positionInRoot.x + caret.left >= horizontal.left - 1f &&
                node.positionInRoot.x + caret.right <= horizontal.right + 1f)
        assertTrue("Caret remains inside the vertical terminal viewport",
            node.positionInRoot.y + caret.top >= vertical.top - 1f &&
                node.positionInRoot.y + caret.bottom <= vertical.bottom + 1f)
    }
}
