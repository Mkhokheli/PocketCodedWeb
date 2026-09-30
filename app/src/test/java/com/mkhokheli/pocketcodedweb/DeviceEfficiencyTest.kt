package com.mkhokheli.pocketcodedweb

import org.junit.Assert.*
import org.junit.Test

class DeviceEfficiencyTest {
    private val gb = 1024L * 1024 * 1024

    @Test fun constrainedDevicesUseSmallerBudgets() {
        listOf(
            efficiencyBudget(true, 8 * gb, 256),
            efficiencyBudget(false, 3 * gb, 256),
            efficiencyBudget(false, 8 * gb, 128),
        ).forEach {
            assertEquals(32_000, it.terminalRenderChars)
            assertEquals(600, it.terminalRenderLines)
            assertEquals(64 * 1024, it.nodeLogChars)
            assertEquals(500, it.logFlushMs)
        }
        assertEquals(DeviceEfficiencyBudget(), efficiencyBudget(false, 4 * gb, 256))
        assertEquals(DeviceEfficiencyBudget(), efficiencyBudget(false, 0, 0))
    }

    @Test fun smallOutputIsUnchangedAndRecentLinesAreKept() {
        val small = "first\nsecond"
        assertSame(small, terminalRenderTail(small, 100, 600))
        val result = terminalRenderTail("old\nsecond\nlast", 100, 2)
        assertTrue(result.endsWith("second\nlast"))
        assertFalse(result.contains("old"))
    }

    @Test fun largeSingleLinesAndManyShortLinesAreBounded() {
        val huge = terminalRenderTail("x".repeat(200_000), 32_000, 600)
        assertTrue(huge.length < 32_100)
        val many = terminalRenderTail("a\n".repeat(40_000), 32_000, 600)
        assertTrue(many.count { it == '\n' } <= 601)
        assertTrue(many.endsWith("a\n"))
    }

    @Test fun renderTailDoesNotSplitSurrogatePairs() {
        val result = terminalRenderTail("abc\uD83D\uDE00xyz", 4, 10)
        assertTrue(result.endsWith("xyz"))
        assertFalse(result.contains('\uDE00'))
    }
}
