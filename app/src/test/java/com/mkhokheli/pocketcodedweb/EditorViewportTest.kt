package com.mkhokheli.pocketcodedweb

import org.junit.Assert.*
import org.junit.Test

class EditorViewportTest {
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
