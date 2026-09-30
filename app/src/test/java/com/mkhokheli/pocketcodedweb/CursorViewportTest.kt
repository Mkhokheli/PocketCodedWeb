package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertEquals
import org.junit.Test

class CursorViewportTest {
    @Test fun cursorInsideSafeAreaDoesNotMoveViewport() {
        assertEquals(200, minimalScrollTarget(200, 500, 2_000, 360f, 380f, 90f, 100f))
    }

    @Test fun cursorBeyondTrailingEdgeMovesSmallestDistance() {
        assertEquals(200, minimalScrollTarget(100, 500, 2_000, 570f, 580f, 90f, 120f))
    }

    @Test fun cursorBeforeLeadingEdgeMovesSmallestDistance() {
        assertEquals(160, minimalScrollTarget(200, 500, 2_000, 250f, 270f, 90f, 100f))
    }
}
