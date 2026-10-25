package com.mkhokheli.pocketcodedweb

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class TerminalViewportTest {
    @Test fun growingOutputKeepsTheFocusedInputCaretInsideTheTerminal() {
        assertEquals(TerminalScrollTargets(0, 732), target(row = Offset(0f, 1_000f)))
        assertEquals(TerminalScrollTargets(0, 1_232), target(vertical = 732, row = Offset(0f, 1_500f)))
    }

    @Test fun measuredPromptAndFieldOffsetsRevealLongInputHorizontally() {
        assertEquals(TerminalScrollTargets(446, 0), target(
            row = Offset.Zero, field = Offset(180f, 2f), caret = Rect(650f, 0f, 654f, 20f)))
    }

    @Test fun multilineSelectionRevealsTheSelectedLineRatherThanAlwaysJumpingToTheBottom() {
        assertEquals(TerminalScrollTargets(0, 998), target(vertical = 1_400,
            row = Offset(0f, 1_000f), field = Offset(100f, 10f), caret = Rect(0f, 0f, 2f, 20f)))
    }

    @Test fun keyboardResizeRevealsTheCaretUsingTheNewViewport() {
        assertEquals(TerminalScrollTargets(0, 882), target(viewportHeight = 150, row = Offset(0f, 1_000f)))
    }

    @Test fun visibleCaretsDoNotMoveManualScrollAndTargetsStayWithinContent() {
        assertEquals(TerminalScrollTargets(50, 900), target(horizontal = 50, vertical = 900,
            row = Offset(0f, 1_000f)))
        assertEquals(TerminalScrollTargets(0, 20), target(row = Offset(0f, 1_000f), verticalMax = 20))
    }

    private fun target(
        horizontal: Int = 0,
        vertical: Int = 0,
        viewportHeight: Int = 300,
        verticalMax: Int = 4_000,
        row: Offset,
        field: Offset = Offset(100f, 0f),
        caret: Rect = Rect(0f, 0f, 2f, 20f),
    ) = terminalCursorScrollTargets(horizontal, vertical, 400, viewportHeight, 4_000, verticalMax,
        row, field, caret, 12f)
}
