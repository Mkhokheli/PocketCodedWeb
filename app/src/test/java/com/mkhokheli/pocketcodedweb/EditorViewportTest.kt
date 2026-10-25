package com.mkhokheli.pocketcodedweb

import org.junit.Assert.*
import org.junit.Test

class EditorViewportTest {
    @Test fun caretRevealMovesOnlyTheDistanceNeededForTheVisibleEditor() {
        assertEquals(200, editorCursorScrollTarget(200, 300, 2_000, 300f, 320f, 12f))
        assertEquals(232, editorCursorScrollTarget(200, 300, 2_000, 500f, 520f, 12f))
        assertEquals(168, editorCursorScrollTarget(200, 300, 2_000, 180f, 200f, 12f))
        assertEquals(212, editorCursorScrollTarget(200, 300, 2_000, 510f, 512f, 0f))
    }

    @Test fun keyboardAndCompletionResizeStillFitsTheCaretInTinyViewports() {
        assertEquals(20, editorCursorScrollTarget(0, 40, 1_000, 30f, 50f, 30f))
        assertEquals(50, editorCursorScrollTarget(0, 20, 1_000, 50f, 70f, 30f))
        assertEquals(1_000, editorCursorScrollTarget(0, 100, 1_000, 2_000f, 2_020f, 12f))
        assertEquals(0, editorCursorScrollTarget(0, 0, -1, 50f, 70f, 12f))
    }

    @Test fun breakpointDotsReplaceOnlyTheirOwnNumeralSlots() {
        val numbers = (1..12).joinToString("\n")
        val masked = maskBreakpointLineNumbers(numbers, setOf(2, 10, 12))
        assertEquals(numbers.length, masked.length)
        assertEquals(listOf("1", " ", "3", "4", "5", "6", "7", "8", "9", "  ", "11", "  "), masked.lines())
        assertSame(numbers, maskBreakpointLineNumbers(numbers, emptySet()))
    }

    @Test fun minifiedBundlesHaveBoundedHighlightWorkAtTheVisibleOffset() {
        val source = "x".repeat(1_000_000)
        val window = editorHighlightWindow(source, 500_000, 500_100)
        assertTrue(window.start <= 500_000)
        assertTrue(window.end >= 500_100)
        assertTrue(window.end - window.start <= EDITOR_HIGHLIGHT_MAX_CHARS)
        assertTrue(window.start > 490_000)
    }

    @Test fun highlightingFollowsVisibleCodeFarFromTheCursor() {
        val source = "const answer = 42;\n".repeat(10_000)
        val start = source.indexOf('\n', 90_000) + 1
        val end = source.indexOf('\n', start + 8_000)
        val window = editorHighlightWindow(source, start, end)
        assertTrue(window.start <= start)
        assertTrue(window.end >= end)
        assertTrue(window.start > 80_000)
        assertEquals('\n', source[window.start - 1])
        assertEquals('\n', source[window.end])
    }

    @Test fun emptyAndShortenedFilesHaveValidRanges() {
        assertEquals(EditorHighlightWindow(0, 0), editorHighlightWindow("", 500, 900))
        assertEquals(EditorHighlightWindow(0, 4), editorHighlightWindow("\nabc", 500, 900))
        assertEquals(EditorHighlightWindow(0, 4), editorHighlightWindow("\nabc", 0, 0))
    }

    @Test fun focusAndKeyboardRevealRequestsNeverMoveTheEditor() {
        assertEquals(0f, ManualEditorBringIntoViewSpec.calculateScrollDistance(-200f, 20f, 400f))
        assertEquals(0f, ManualEditorBringIntoViewSpec.calculateScrollDistance(900f, 20f, 400f))
    }
}
