package com.mkhokheli.pocketcodedweb

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.*
import org.junit.Test

class EditorHistoryTest {
    private fun value(text: String, cursor: Int = text.length) = TextFieldValue(text, TextRange(cursor))

    @Test fun undoRestoresSelectionAndRedoSurvivesCaretMovement() {
        val history = EditorHistory()
        val before = TextFieldValue("hello", TextRange(1, 4))
        val after = value("hio", 2)
        history.record(before, after)
        assertEquals(before, history.undo(after))
        history.record(before, before.copy(selection = TextRange(0)))
        assertTrue(history.canRedo)
        assertEquals(after, history.redo(before))
    }

    @Test fun editingAfterUndoDiscardsRedo() {
        val history = EditorHistory()
        history.record(value("a"), value("ab"))
        val undone = history.undo(value("ab"))!!
        history.record(undone, value("ac"))
        assertFalse(history.canRedo)
        assertEquals(value("a"), history.undo(value("ac")))
    }

    @Test fun staleHistoryNeverChangesExternallyReplacedText() {
        val history = EditorHistory()
        history.record(value("a"), value("ab"))
        assertNull(history.undo(value("external")))
        assertFalse(history.canUndo)
    }

    @Test fun oversizedEditsEstablishNewBaseline() {
        val history = EditorHistory(maxRetainedCharacters = 4)
        history.record(value("a"), value("ab"))
        history.record(value("ab"), value("huge replacement"))
        assertFalse(history.canUndo)
        history.record(value("huge replacement"), value("huge replacements"))
        assertEquals("huge replacement", history.undo(value("huge replacements"))!!.text)
    }

    @Test fun sessionsPreserveHistoryAndInvalidationRemovesIt() {
        val registry = EditorSessionRegistry()
        val session = registry.open("/file.js", "a")
        session.history.record(session.value, value("ab"))
        session.value = value("ab")
        assertSame(session, registry.open("/file.js", "a"))
        assertTrue(session.history.canUndo)
        registry.invalidate("/file.js")
        assertFalse(session.valid)
        assertFalse(registry.open("/file.js", "replacement").history.canUndo)
    }
}
