package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyTerminalCommandHistoryTest {
    @Test fun refreshingTheSameDayPreservesRecallEntries() {
        val history = DailyTerminalCommandHistory(today = { 20261022 })
        history.record("pwd")
        history.record("npm test")
        assertFalse(history.refresh())
        assertEquals(listOf("pwd", "npm test"), history.entries)
    }

    @Test fun midnightRefreshClearsAnAlreadyRetainedMenuList() {
        var date = 20261022
        val history = DailyTerminalCommandHistory(today = { date })
        val menuEntries = history.entries
        history.record("npm test")
        assertEquals(listOf("npm test"), menuEntries)

        date = 20261023
        assertTrue(history.refresh())
        assertSame(menuEntries, history.entries)
        assertTrue(menuEntries.isEmpty())
        assertFalse(history.refresh())
    }

    @Test fun firstCommandAfterMidnightReplacesYesterdayBeforeDeduplication() {
        var date = 20261022
        val history = DailyTerminalCommandHistory(today = { date })
        history.record("pwd")
        history.record("npm test")
        date = 20261023
        history.record("npm test")
        assertEquals(listOf("npm test"), history.entries)
    }

    @Test fun blankCommandsAreIgnoredAndOnlyConsecutiveExactDuplicatesAreRemoved() {
        val history = DailyTerminalCommandHistory(today = { 20261022 })
        listOf("", " \t\n", "pwd", "pwd", "npm test", "pwd", " pwd ").forEach(history::record)
        assertEquals(listOf("pwd", "npm test", "pwd", " pwd "), history.entries)
    }

    @Test fun recallRetainsOnlyTheMostRecentHundredCommands() {
        val history = DailyTerminalCommandHistory(today = { 20261022 })
        (1..105).forEach { history.record("command $it") }
        assertEquals((6..105).map { "command $it" }, history.entries)
    }

    @Test fun yearAndBackwardLocalDateChangesAlsoStartANewHistory() {
        var date = 20261231
        val history = DailyTerminalCommandHistory(today = { date })
        history.record("last year")
        date = 20270101
        assertTrue(history.refresh())
        history.record("new year")
        date = 20261231 // A time-zone or clock change can move the local date backward.
        assertTrue(history.refresh())
        assertTrue(history.entries.isEmpty())
    }

    @Test fun evenABlankSubmissionRefreshesTheDate() {
        var date = 20261022
        val history = DailyTerminalCommandHistory(today = { date })
        history.record("pwd")
        date = 20261023
        history.record(" ")
        assertTrue(history.entries.isEmpty())
    }
}
