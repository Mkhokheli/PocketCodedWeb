package com.mkhokheli.pocketcodedweb
import org.junit.Assert.assertEquals
import org.junit.Test
class TerminalLogCursorTest {
    @Test fun deliversDelayedOutputWithoutDuplicatingCommandSnapshot() {
        val cursor = TerminalLogCursor()
        assertEquals("startup\n", cursor.consume("startup\n"))
        assertEquals("", cursor.consume("startup\n"))
        assertEquals("listening\n", cursor.consume("startup\nlistening\n"))
        assertEquals("request /api\n", cursor.consume("startup\nlistening\nrequest /api\n"))
    }
    @Test fun handlesTrimmedLogsAndNewSessions() {
        val cursor = TerminalLogCursor()
        cursor.consume("old output\n" + "x".repeat(2048))
        assertEquals("new\n", cursor.consume("x".repeat(2048) + "new\n"))
        assertEquals("\n[Earlier Node output trimmed]\ncompletely new", cursor.consume("completely new"))
        cursor.reset()
        assertEquals("startup", cursor.consume("startup"))
    }
    @Test fun preservesPartialLinesAndEmptySnapshots() {
        val cursor = TerminalLogCursor()
        assertEquals("part", cursor.consume("part"))
        assertEquals("ial\n", cursor.consume("partial\n"))
        assertEquals("", cursor.consume(""))
        assertEquals("next", cursor.consume("next"))
    }
}
