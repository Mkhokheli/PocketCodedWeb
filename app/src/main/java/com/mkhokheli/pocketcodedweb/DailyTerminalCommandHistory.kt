package com.mkhokheli.pocketcodedweb

import androidx.compose.runtime.mutableStateListOf
import java.util.Calendar

/** In-memory command recall for one project and one local calendar day. */
internal class DailyTerminalCommandHistory(
    private val today: () -> Int = ::terminalHistoryLocalDate,
    private val maxEntries: Int = 100,
) {
    init { require(maxEntries > 0) }

    private val commands = mutableStateListOf<String>()
    private var date = today()

    /** A stable, observable list; callers can retain it while menu contents change. */
    val entries: List<String> = commands

    fun record(command: String) {
        refresh()
        if (command.isBlank() || commands.lastOrNull() == command) return
        commands.add(command)
        while (commands.size > maxEntries) commands.removeAt(0)
    }

    /** Call at local midnight, on resume, and before opening the recall menu. */
    fun refresh(): Boolean {
        val currentDate = today()
        if (currentDate == date) return false
        date = currentDate
        commands.clear()
        return true
    }
}

/** yyyyMMdd in the current default time zone; compatible with the app's Android 7 target. */
private fun terminalHistoryLocalDate(): Int = Calendar.getInstance().let { calendar ->
    calendar[Calendar.YEAR] * 10_000 + (calendar[Calendar.MONTH] + 1) * 100 + calendar[Calendar.DAY_OF_MONTH]
}
