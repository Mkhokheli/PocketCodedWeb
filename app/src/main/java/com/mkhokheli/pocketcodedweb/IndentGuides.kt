package com.mkhokheli.pocketcodedweb

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal data class IndentGuideSegment(val level: Int, val firstLine: Int, val endLine: Int)

/** Null marks a blank line so rails can continue through gaps in an indented block. */
internal fun indentationDepth(text: CharSequence, start: Int, end: Int): Int? {
    var columns = 0
    for (index in start.coerceIn(0, text.length) until end.coerceIn(0, text.length)) {
        when (text[index]) {
            ' ' -> columns++
            '\t' -> columns += 4
            '\r', '\n' -> Unit
            else -> return (columns / 4).coerceAtMost(64)
        }
    }
    return null
}

/** Linear blank-line resolution, then bounded rail construction for visible lines only. */
internal fun indentationGuideSegments(depths: List<Int?>): List<IndentGuideSegment> {
    if (depths.isEmpty()) return emptyList()
    val effective = IntArray(depths.size)
    var following = 0
    for (index in depths.indices.reversed()) {
        depths[index]?.let { following = it.coerceIn(0, 64) }
        effective[index] = following
    }
    var previous = 0
    for (index in depths.indices) {
        val actual = depths[index]?.coerceIn(0, 64)
        effective[index] = actual ?: maxOf(previous, effective[index])
        if (actual != null) previous = actual
    }
    val result = mutableListOf<IndentGuideSegment>()
    for (level in 1..(effective.maxOrNull() ?: 0)) {
        var start = -1
        for (index in 0..effective.size) {
            if (index < effective.size && effective[index] >= level) {
                if (start < 0) start = index
            } else if (start >= 0) {
                result += IndentGuideSegment(level, start, index)
                start = -1
            }
        }
    }
    return result
}

internal fun Modifier.explorerIndentGuides(depth: Int, color: Color): Modifier = drawBehind {
    for (level in 0 until depth.coerceIn(0, 64)) {
        val x = 11.dp.toPx() + level * 14.dp.toPx()
        drawLine(color, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
    }
}
