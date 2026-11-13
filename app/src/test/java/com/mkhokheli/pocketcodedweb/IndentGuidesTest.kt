package com.mkhokheli.pocketcodedweb

import org.junit.Assert.*
import org.junit.Test

class IndentGuidesTest {
    @Test fun spacesTabsAndWhitespaceOnlyLinesHaveReferenceDepths() {
        assertEquals(1, indentationDepth("    code", 0, 8))
        assertEquals(2, indentationDepth("\t    code", 0, 9))
        assertEquals(0, indentationDepth("code", 0, 4))
        assertNull(indentationDepth(" \t \r\n", 0, 5))
    }

    @Test fun blankLinesJoinSurroundingBlockRails() {
        assertEquals(listOf(IndentGuideSegment(1, 1, 6), IndentGuideSegment(2, 2, 5)),
            indentationGuideSegments(listOf(0, 1, null, 2, null, 1, 0)))
    }

    @Test fun separateBlocksDoNotJoinAcrossAnOutdentedLine() {
        assertEquals(listOf(IndentGuideSegment(1, 0, 2), IndentGuideSegment(1, 3, 5)),
            indentationGuideSegments(listOf(1, null, 0, null, 1)))
    }

    @Test fun emptyOrBlankWindowsDoNotProduceSpuriousGuides() {
        assertTrue(indentationGuideSegments(emptyList()).isEmpty())
        assertTrue(indentationGuideSegments(listOf(null, null)).isEmpty())
    }

    @Test fun extremeIndentationIsBoundedAndRangesStayInsideTheWindow() {
        val segments = indentationGuideSegments(listOf(10_000, null, -1))
        assertEquals(64, segments.size)
        assertTrue(segments.all { it.firstLine == 0 && it.endLine == 2 && it.level in 1..64 })
        val source = " ".repeat(10_000) + "code"
        assertEquals(64, indentationDepth(source, 0, source.length))
    }
}
