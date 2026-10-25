package com.mkhokheli.pocketcodedweb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.util.ArrayDeque

/**
 * Undo history for one open file. Keep this alongside the file's editor session, rather than
 * inside the editor tab, so navigating away does not discard its history.
 *
 * Entries retain only the replaced text and selections. The current document reference is
 * shared with the editor, allowing untracked external replacements to invalidate stale history.
 * Both stacks share the same entry and UTF-16 character budgets.
 */
class EditorHistory(
    private val maxEntries: Int = 200,
    private val maxRetainedCharacters: Int = 500_000,
) {
    init {
        require(maxEntries >= 0)
        require(maxRetainedCharacters >= 0)
    }

    private data class Edit(
        val start: Int,
        val removed: String,
        val inserted: String,
        val beforeSelection: TextRange,
        val afterSelection: TextRange,
    ) {
        val retainedCharacters: Long
            get() = removed.length.toLong() + inserted.length
    }

    private data class ChangeRange(val start: Int, val beforeEnd: Int, val afterEnd: Int)

    private val undoStack = ArrayDeque<Edit>()
    private val redoStack = ArrayDeque<Edit>()
    private var retainedCharacters = 0L
    private var expectedText: String? = null

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    /** Record one editor transaction, including shortcuts, completions and known external edits. */
    fun record(before: TextFieldValue, after: TextFieldValue) {
        if (expectedText != null && expectedText != before.text) clear()
        expectedText = after.text

        // Moving the caret must not consume history or discard a possible redo.
        if (before.text == after.text) return

        while (redoStack.isNotEmpty()) {
            retainedCharacters -= redoStack.removeLast().retainedCharacters
        }

        val (start, beforeEnd, afterEnd) = changeRange(before, after)
        val changedCharacters = (beforeEnd - start).toLong() + afterEnd - start
        if (maxEntries == 0 || changedCharacters > maxRetainedCharacters) {
            // A very large replace/paste establishes a new baseline. Do not allocate or retain
            // a delta larger than the budget, or allow older entries to cross this transaction.
            clearStacks()
            return
        }

        val edit = Edit(
            start = start,
            removed = before.text.substring(start, beforeEnd),
            inserted = after.text.substring(start, afterEnd),
            beforeSelection = before.selection,
            afterSelection = after.selection,
        )
        undoStack.addLast(edit)
        retainedCharacters += edit.retainedCharacters
        while (undoStack.size > maxEntries || retainedCharacters > maxRetainedCharacters) {
            retainedCharacters -= undoStack.removeFirst().retainedCharacters
        }
        updateAvailability()
    }

    fun undo(current: TextFieldValue): TextFieldValue? {
        if (!accepts(current)) return null
        val edit = undoStack.pollLast() ?: return null
        val text = current.text.replaceRange(
            edit.start,
            edit.start + edit.inserted.length,
            edit.removed,
        )
        redoStack.addLast(edit)
        expectedText = text
        updateAvailability()
        return TextFieldValue(text, edit.beforeSelection)
    }

    fun redo(current: TextFieldValue): TextFieldValue? {
        if (!accepts(current)) return null
        val edit = redoStack.pollLast() ?: return null
        val text = current.text.replaceRange(
            edit.start,
            edit.start + edit.removed.length,
            edit.inserted,
        )
        undoStack.addLast(edit)
        expectedText = text
        updateAvailability()
        return TextFieldValue(text, edit.afterSelection)
    }

    /** Forget history after an unknown reload, file replacement or session eviction. */
    fun clear() {
        clearStacks()
        expectedText = null
    }

    private fun accepts(current: TextFieldValue): Boolean {
        if (expectedText == null || expectedText == current.text) return true
        clearStacks()
        expectedText = current.text
        return false
    }

    private fun changeRange(before: TextFieldValue, after: TextFieldValue): ChangeRange {
        val delta = after.text.length - before.text.length
        val selection = before.selection
        val candidate = if (selection.collapsed && delta < 0) {
            if (after.selection.end < selection.end) {
                // Backspace, including deleting a code point or an entire word.
                ChangeRange(selection.end + delta, selection.end, selection.end + delta)
            } else {
                // Forward delete leaves the caret at the start of the removed text.
                ChangeRange(selection.end, selection.end - delta, selection.end)
            }
        } else {
            // Typing, paste, selected-text replacement, indentation and paired punctuation.
            ChangeRange(selection.min, selection.max, selection.max + delta)
        }
        if (candidate.start >= 0 &&
            candidate.beforeEnd in candidate.start..before.text.length &&
            candidate.afterEnd in candidate.start..after.text.length &&
            before.text.regionMatches(0, after.text, 0, candidate.start) &&
            before.text.regionMatches(
                candidate.beforeEnd,
                after.text,
                candidate.afterEnd,
                before.text.length - candidate.beforeEnd,
            )
        ) {
            // Let the platform compare the unchanged spans instead of walking a large file
            // character by character in Kotlin on every keystroke near its end.
            return trimChangeRange(before.text, after.text, candidate)
        }

        // Formatting, completions and external updates can change text away from the caret.
        // Verify the candidate above before trusting it, then use an exact general diff here.
        var start = 0
        val commonLength = minOf(before.text.length, after.text.length)
        while (start < commonLength && before.text[start] == after.text[start]) start++
        var beforeEnd = before.text.length
        var afterEnd = after.text.length
        while (beforeEnd > start && afterEnd > start &&
            before.text[beforeEnd - 1] == after.text[afterEnd - 1]
        ) {
            beforeEnd--
            afterEnd--
        }
        return ChangeRange(start, beforeEnd, afterEnd)
    }

    private fun trimChangeRange(before: String, after: String, range: ChangeRange): ChangeRange {
        // Selected-text replacement can leave part of the selection unchanged. Trim it so a
        // large selection does not consume the history budget for a tiny actual replacement.
        var start = range.start
        var beforeEnd = range.beforeEnd
        var afterEnd = range.afterEnd
        while (start < minOf(beforeEnd, afterEnd) && before[start] == after[start]) start++
        while (beforeEnd > start && afterEnd > start &&
            before[beforeEnd - 1] == after[afterEnd - 1]
        ) {
            beforeEnd--
            afterEnd--
        }
        return ChangeRange(start, beforeEnd, afterEnd)
    }

    private fun clearStacks() {
        undoStack.clear()
        redoStack.clear()
        retainedCharacters = 0L
        updateAvailability()
    }

    private fun updateAvailability() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }
}
