package com.mkhokheli.pocketcodedweb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** Keeps editing state when the same file moves between tabs and editor layouts. */
internal class EditorSession(code: String) {
    val history = EditorHistory()
    var textRevision = 0L
        private set
    var value = TextFieldValue(code, TextRange(code.length))
        set(updated) {
            if (field.text != updated.text) textRevision++
            field = updated
        }
    var externalCode = code
    var verticalScroll = 0
    var horizontalScroll = 0
    var followCursor = true
    var lastSourceJumpVersion: Long? = null
    var valid = true
        private set

    fun invalidate() {
        valid = false
        history.clear()
    }

    fun reconcile(code: String) {
        // A layout change can happen before the local draft reaches the parent.
        if (code != externalCode && code != value.text) {
            val updated = TextFieldValue(code, TextRange(value.selection.end.coerceAtMost(code.length)))
            history.record(value, updated)
            value = updated
        }
        externalCode = code
    }
}

internal class EditorSessionRegistry(private val maxSessions: Int = 16) {
    private val sessions = LinkedHashMap<String, EditorSession>(16, 0.75f, true)
    var generation by mutableIntStateOf(0)
        private set

    fun open(path: String, code: String): EditorSession {
        val session = sessions.getOrPut(path) { EditorSession(code) }
        session.reconcile(code)
        while (sessions.size > maxSessions.coerceAtLeast(1)) {
            sessions.remove(sessions.keys.first())
        }
        return session
    }

    /** A replaced/deleted file must never inherit the previous file's draft or undo stack. */
    fun invalidate(path: String) {
        sessions.remove(path)?.invalidate()
        generation++
    }

    fun invalidateUnder(folder: String) {
        val prefix = folder.replace('\\', '/').trimEnd('/')
        val iterator = sessions.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val path = entry.key.replace('\\', '/')
            if (path == prefix || path.startsWith("$prefix/")) {
                entry.value.invalidate()
                iterator.remove()
            }
        }
        generation++
    }
}

internal val LocalEditorSessions = staticCompositionLocalOf<EditorSessionRegistry?> { null }
