package com.mkhokheli.pocketcodedweb

/** Consume bounded log snapshots without replaying output already displayed. */
internal class TerminalLogCursor {
    private var previous = ""
    fun reset() { previous = "" }
    fun consume(snapshot: String): String {
        if (snapshot == previous) return ""
        val addition = when {
            snapshot.startsWith(previous) -> snapshot.substring(previous.length)
            snapshot.isEmpty() -> ""
            else -> {
                // Snapshots retain a suffix when the runtime trims its bounded log.
                val overlap = minOf(previous.length, snapshot.length)
                val marker = previous.takeLast(minOf(overlap, 1024))
                val end = if (marker.isEmpty()) -1 else snapshot.lastIndexOf(marker)
                if (end >= 0) snapshot.substring(end + marker.length)
                else "\n[Earlier Node output trimmed]\n" + snapshot
            }
        }
        previous = snapshot
        return addition
    }
}
