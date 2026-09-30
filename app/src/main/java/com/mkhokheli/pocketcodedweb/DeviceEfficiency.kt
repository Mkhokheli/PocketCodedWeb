package com.mkhokheli.pocketcodedweb

import android.app.ActivityManager
import android.content.Context

internal data class DeviceEfficiencyBudget(
        val terminalHistoryChars: Int = 200_000,
        val terminalRenderChars: Int = 200_000,
        val terminalRenderLines: Int = Int.MAX_VALUE,
        val nodeLogChars: Int = 128 * 1024,
        val logFlushMs: Int = 200,
        val nodeStatusPollMs: Long = 2_000,
        val idlePollMs: Long = 2_000,
)

internal fun efficiencyBudget(
        lowRam: Boolean,
        totalRamBytes: Long,
        memoryClassMb: Int
): DeviceEfficiencyBudget {
    val constrained =
            lowRam || totalRamBytes in 1..(3L * 1024 * 1024 * 1024) || memoryClassMb in 1..128
    return if (constrained)
            DeviceEfficiencyBudget(
                    terminalHistoryChars = 96_000,
                    terminalRenderChars = 32_000,
                    terminalRenderLines = 600,
                    nodeLogChars = 64 * 1024,
                    logFlushMs = 500,
                    nodeStatusPollMs = 3_000,
                    idlePollMs = 3_000,
            )
    else DeviceEfficiencyBudget()
}

internal object DeviceEfficiency {
    @Volatile
    var budget = DeviceEfficiencyBudget()
        private set
    @Volatile private var initialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo().also(manager::getMemoryInfo)
        budget = efficiencyBudget(manager.isLowRamDevice, memory.totalMem, manager.memoryClass)
        initialized = true
    }
}

/** Bound layout work by characters AND lines; many tiny lines can be as costly as long output. */
internal fun terminalRenderTail(text: String, maxChars: Int, maxLines: Int): String {
    require(maxChars > 0 && maxLines > 0)
    if (text.length <= maxChars && maxLines == Int.MAX_VALUE) return text
    var start = (text.length - maxChars).coerceAtLeast(0)
    var lines = 0
    for (index in text.lastIndex downTo start) {
        if (text[index] == '\n' && ++lines >= maxLines) {
            start = index + 1
            break
        }
    }
    if (start == 0) return text
    if (text[start - 1] != '\n') {
        val newline = text.indexOf('\n', start)
        if (newline >= 0 && newline < text.lastIndex) start = newline + 1
    }
    // Avoid cutting a supplementary Unicode character in half on a single long line.
    if (start < text.length && text[start].isLowSurrogate() && text[start - 1].isHighSurrogate())
            start++
    return "[Showing recent output to keep the terminal responsive]\n" + text.substring(start)
}
