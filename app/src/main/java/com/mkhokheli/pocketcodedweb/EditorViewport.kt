package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.relocation.BringIntoViewModifierNode

internal const val EDITOR_SCROLL_HIGHLIGHT_IDLE_MS = 2_000L
internal const val EDITOR_HIGHLIGHT_MAX_CHARS = 16_384

internal data class EditorHighlightWindow(val start: Int, val end: Int)

/** Reserve a little context while still fitting the caret in a very short keyboard viewport. */
internal fun editorCursorScrollTarget(
        current: Int,
        viewportSize: Int,
        contentMax: Int,
        cursorStart: Float,
        cursorEnd: Float,
        preferredMargin: Float,
): Int {
    if (viewportSize <= 0) return current.coerceIn(0, contentMax.coerceAtLeast(0))
    val caretSize = (cursorEnd - cursorStart).coerceAtLeast(0f)
    val margin = preferredMargin.coerceIn(0f, ((viewportSize - caretSize) / 2f).coerceAtLeast(0f))
    return minimalScrollTarget(current, viewportSize, contentMax.coerceAtLeast(0),
            cursorStart, cursorEnd, margin, margin)
}

/** Leave the numeral's existing slot empty when its breakpoint dot is drawn over the gutter. */
internal fun maskBreakpointLineNumbers(numbers: String, breakpoints: Set<Int>): String {
    if (breakpoints.isEmpty()) return numbers
    val result = StringBuilder(numbers)
    var line = 1
    for (offset in numbers.indices) {
        if (numbers[offset] == '\n') line++
        else if (line in breakpoints) result.setCharAt(offset, ' ')
    }
    return result.toString()
}

internal fun editorHighlightWindow(
        source: String,
        visibleStart: Int,
        visibleEnd: Int,
        contextChars: Int = 2_000
): EditorHighlightWindow {
    val first = visibleStart.coerceIn(0, source.length)
    val last = visibleEnd.coerceIn(first, source.length)
    val context = contextChars.coerceIn(0, EDITOR_HIGHLIGHT_MAX_CHARS / 4)
    val rawStart = (first - context).coerceAtLeast(0)
    val earliest = (first - EDITOR_HIGHLIGHT_MAX_CHARS / 2).coerceAtLeast(0)
    var start = rawStart
    while (start > earliest && source[start - 1] != '\n') start--
    if (start > 0 && source[start - 1] != '\n') start = rawStart
    val limit = (start.toLong() + EDITOR_HIGHLIGHT_MAX_CHARS).coerceAtMost(source.length.toLong()).toInt()
    var end = (last.toLong() + context).coerceAtMost(limit.toLong()).toInt()
    // Preserve whole normal lines, but never tokenize an entire minified bundle.
    while (end < limit && source[end] != '\n') end++
    return EditorHighlightWindow(start, end)
}

internal object ManualEditorBringIntoViewSpec : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float) = 0f
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
internal fun ManualScrollEditor(
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit
) {
    CompositionLocalProvider(LocalBringIntoViewSpec provides ManualEditorBringIntoViewSpec) {
        Column(modifier = modifier.manualEditorScrolling(), content = content)
    }
}

// Consume editor focus/caret reveal requests so outer page containers do not scroll either.
// User drag, fling, scrollbar and accessibility scroll actions still reach the scroll containers.
internal fun Modifier.manualEditorScrolling(): Modifier = this.then(ManualEditorScrollElement)

private data object ManualEditorScrollElement : ModifierNodeElement<ManualEditorScrollNode>() {
    override fun create() = ManualEditorScrollNode()
    override fun update(node: ManualEditorScrollNode) = Unit
    override fun InspectorInfo.inspectableProperties() {
        name = "manualEditorScrolling"
    }
}

private class ManualEditorScrollNode : Modifier.Node(), BringIntoViewModifierNode {
    override suspend fun bringIntoView(
            childCoordinates: LayoutCoordinates,
            boundsProvider: () -> Rect?
    ) = Unit
}
