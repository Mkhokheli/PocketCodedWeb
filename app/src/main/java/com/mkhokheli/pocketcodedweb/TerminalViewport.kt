package com.mkhokheli.pocketcodedweb

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

internal data class TerminalScrollTargets(val horizontal: Int, val vertical: Int)

/** The field's measured origin includes the actual prompt width and baseline alignment. */
internal fun terminalCursorScrollTargets(
    horizontal: Int,
    vertical: Int,
    viewportWidth: Int,
    viewportHeight: Int,
    horizontalMax: Int,
    verticalMax: Int,
    inputRowOrigin: Offset,
    inputFieldOrigin: Offset,
    caret: Rect,
    margin: Float,
): TerminalScrollTargets {
    val origin = inputRowOrigin + inputFieldOrigin
    return TerminalScrollTargets(
        horizontal = editorCursorScrollTarget(horizontal, viewportWidth, horizontalMax,
            origin.x + caret.left, origin.x + caret.right.coerceAtLeast(caret.left + 2f), margin),
        vertical = editorCursorScrollTarget(vertical, viewportHeight, verticalMax,
            origin.y + caret.top, origin.y + caret.bottom, margin),
    )
}
