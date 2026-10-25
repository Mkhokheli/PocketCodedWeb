package com.mkhokheli.pocketcodedweb.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.AnnotatedString

internal const val MIN_TEXT_CONTRAST = 4.5f

internal fun textContrast(foreground: Color, background: Color): Float {
    val surface = background.compositeOver(Color.White)
    val ink = foreground.compositeOver(surface)
    return (maxOf(ink.luminance(), surface.luminance()) + 0.05f) /
        (minOf(ink.luminance(), surface.luminance()) + 0.05f)
}

internal fun readableContentColor(background: Color): Color =
    if (textContrast(Color.Black, background) >= textContrast(Color.White, background)) Color.Black else Color.White

/** Preserve a theme's hue when possible, adjusting only enough to make small text readable. */
internal fun readableTextColor(foreground: Color, backgrounds: List<Color>, minimum: Float = MIN_TEXT_CONTRAST): Color {
    if (!foreground.isSpecified || backgrounds.isEmpty()) return foreground
    fun worst(color: Color) = backgrounds.minOf { textContrast(color, it) }
    if (worst(foreground) >= minimum) return foreground
    val opaque = foreground.copy(alpha = 1f)
    if (worst(opaque) >= minimum) return opaque
    val target = if (worst(Color.Black) >= worst(Color.White)) Color.Black else Color.White
    if (worst(target) < minimum) return target
    var low = 0f
    var high = 1f
    repeat(24) {
        val middle = (low + high) / 2f
        if (worst(lerp(opaque, target, middle)) >= minimum + 0.05f) high = middle else low = middle
    }
    return lerp(opaque, target, high)
}

internal fun readableTextColor(foreground: Color, background: Color, minimum: Float = MIN_TEXT_CONTRAST): Color =
    readableTextColor(foreground, listOf(background), minimum)

/** Copy spans without dropping links, paragraph styles, or other annotations. */
internal fun AnnotatedString.withReadableText(background: Color, selection: Color = Color.Transparent): AnnotatedString {
    if (isEmpty() || spanStyles.isEmpty()) return this
    val builder = AnnotatedString.Builder(this)
    data class Event(val offset: Int, val index: Int, val start: Boolean)
    val events = spanStyles.flatMapIndexed { index, range ->
        listOf(Event(range.start, index, true), Event(range.end, index, false))
    }.sortedBy { it.offset }
    val foregrounds = java.util.TreeMap<Int, Color>()
    val backgrounds = java.util.TreeMap<Int, Color>()
    var cursor = 0
    while (cursor < events.size) {
        val position = events[cursor].offset
        while (cursor < events.size && events[cursor].offset == position) {
            val event = events[cursor++]
            val style = spanStyles[event.index].item
            if (event.start) {
                if (style.color.isSpecified) foregrounds[event.index] = style.color
                if (style.background.isSpecified) backgrounds[event.index] = style.background
            } else {
                foregrounds.remove(event.index)
                backgrounds.remove(event.index)
            }
        }
        val end = events.getOrNull(cursor)?.offset ?: length
        if (end > position) {
            val ink = foregrounds.lastEntry()?.value ?: readableContentColor(background)
            val surface = backgrounds.lastEntry()?.value?.compositeOver(background) ?: background
            val adjusted = readableTextColor(ink, listOf(surface, selection.compositeOver(surface)))
            if (adjusted != ink) builder.addStyle(androidx.compose.ui.text.SpanStyle(color = adjusted), position, end)
        }
    }
    return builder.toAnnotatedString()
}
