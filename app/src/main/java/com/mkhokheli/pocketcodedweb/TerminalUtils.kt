package com.mkhokheli.pocketcodedweb

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private const val MAX_TERMINAL_BUFFER_CHARS = 200_000
private const val TERMINAL_TRUNCATION_NOTICE = "[Earlier terminal output trimmed]\n"

private val terminalRemoteCredentialRegex = Regex("(?i)(https?://)[^/@\\s]+@")
private val terminalBearerAuthorizationRegex = Regex("(?i)(authorization\\s*:\\s*bearer\\s+)[^\\s\"]+")
private val terminalBasicAuthorizationRegex = Regex("(?i)(authorization\\s*:\\s*basic\\s+)[^\\s\"]+")
private val terminalUserOptionRegex = Regex("(?i)(^|\\s)(-u|--user)(=|\\s+)(\"[^\"]*\"|'[^']*'|\\S+)")
private val terminalAnsiPattern = Regex("(?:\u001B\\[|\u009B)([0-9;]*)m")
private val terminalPassedRegex = Regex("\\b\\d+ passed\\b")
private val terminalZeroFailedRegex = Regex("\\b0 failed\\b")
private val terminalErrorRegex = Regex("\\b(error|failed|failure|exception|fatal)\\b")
private val terminalWarningRegex = Regex("\\b(warning|warn|deprecated|skipped)\\b")
private val terminalRanTestsRegex = Regex("^ran \\d+ tests?")

/**
 * Redacts sensitive information from terminal commands before displaying them.
 */
fun redactTerminalCommandForDisplay(command: String): String {
    return command
        .replace(terminalRemoteCredentialRegex, "$1")
        .replace(terminalBearerAuthorizationRegex, "$1***")
        .replace(terminalBasicAuthorizationRegex, "$1***")
        .replace(terminalUserOptionRegex) { match ->
            val prefix = match.groupValues[1]
            val option = match.groupValues[2]
            val separator = match.groupValues[3]
            "$prefix$option$separator***"
        }
}

/**
 * Trims terminal output to a maximum size while preserving the most recent output
 * and ensuring we break at a line boundary.
 */
fun trimTerminalBuffer(text: String): String {
    if (text.length <= MAX_TERMINAL_BUFFER_CHARS) return text
    
    val tailStart = text.length - MAX_TERMINAL_BUFFER_CHARS
    val nextLineStart = text.indexOf('\n', tailStart)
        .takeIf { it >= 0 }
        ?.plus(1)
        ?: tailStart
    return "$TERMINAL_TRUNCATION_NOTICE\n${text.substring(nextLineStart)}"
}

/**
 * Parses ANSI escape codes and returns an AnnotatedString with styles.
 */
internal fun parseAnsiTerminalOutput(
    text: String,
    promptText: String,
    palette: WebPalette,
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    
    val promptStyle = SpanStyle(
        color = palette.terminalPrompt,
        fontWeight = FontWeight.SemiBold
    )
    var ansiForeground: Color? = null
    var ansiBold = false

    return buildAnnotatedString {
        fun applyAnsiCodes(rawCodes: String) {
            val codes = rawCodes
                .takeIf { it.isNotBlank() }
                ?.split(';')
                ?.map { it.toIntOrNull() ?: 0 }
                ?: listOf(0)
            var index = 0
            while (index < codes.size) {
                when (val code = codes[index]) {
                    0 -> {
                        ansiForeground = null
                        ansiBold = false
                    }
                    1 -> ansiBold = true
                    22 -> ansiBold = false
                    in 30..37 -> ansiForeground = terminalAnsiColor(code, palette)
                    39 -> ansiForeground = null
                    in 90..97 -> ansiForeground = terminalAnsiColor(code, palette)
                    38 -> when (codes.getOrNull(index + 1)) {
                        5 -> {
                            ansiForeground = ansi256Color(codes.getOrNull(index + 2) ?: 7, palette)
                            index += 2
                        }
                        2 -> {
                            val red = (codes.getOrNull(index + 2) ?: 255).coerceIn(0, 255)
                            val green = (codes.getOrNull(index + 3) ?: 255).coerceIn(0, 255)
                            val blue = (codes.getOrNull(index + 4) ?: 255).coerceIn(0, 255)
                            ansiForeground = ensureReadableColor(Color(red, green, blue), palette.panel)
                            index += 4
                        }
                    }
                }
                index += 1
            }
        }

        fun appendAnsiText(rawText: String, fallbackColor: Color? = null) {
            var cursor = 0
            terminalAnsiPattern.findAll(rawText).forEach { match ->
                val segment = rawText.substring(cursor, match.range.first)
                val color = ansiForeground ?: fallbackColor
                if ((color != null) || ansiBold) {
                    withStyle(
                        SpanStyle(
                            color = color ?: Color.Unspecified,
                            fontWeight = if (ansiBold) FontWeight.Bold else null
                        )
                    ) {
                        append(segment)
                    }
                } else {
                    append(segment)
                }
                applyAnsiCodes(match.groupValues[1])
                cursor = match.range.last + 1
            }
            val remaining = rawText.substring(cursor)
            val color = ansiForeground ?: fallbackColor
            if (color != null || ansiBold) {
                withStyle(
                    SpanStyle(
                        color = color ?: Color.Unspecified,
                        fontWeight = if (ansiBold) FontWeight.Bold else null
                    )
                ) {
                    append(remaining)
                }
            } else {
                append(remaining)
            }
        }

        text.split('\n').forEachIndexed { index, line ->
            if (index > 0) append('\n')
            when {
                line == promptText -> withStyle(promptStyle) {
                    append(line)
                }
                line.startsWith("$promptText ") -> {
                    ansiForeground = null
                    ansiBold = false
                    withStyle(promptStyle) {
                        append(promptText)
                    }
                    appendAnsiText(line.removePrefix(promptText))
                }
                else -> {
                    val plainLine = terminalAnsiPattern.replace(line, "")
                    appendAnsiText(line, terminalSemanticColor(plainLine, palette))
                }
            }
        }
    }
}

private fun terminalSemanticColor(line: String, palette: WebPalette): Color? {
    val normalized = line.trim().lowercase()
    if (normalized.isBlank()) return null
    return when {
        normalized == "ok" ||
            normalized == "finished" ||
            normalized.contains("build successful") ||
            normalized.contains("successfully installed") ||
            terminalPassedRegex.containsMatchIn(normalized) ||
            terminalZeroFailedRegex.containsMatchIn(normalized) -> palette.success
        normalized.startsWith("traceback") ||
            normalized.startsWith("failed (") ||
            normalized.contains("build failed") ||
            normalized.contains("permission denied") ||
            normalized.contains("no such file") ||
            normalized.contains("command not found") ||
            terminalErrorRegex.containsMatchIn(normalized) -> palette.error
        normalized == "stopped" ||
            terminalWarningRegex.containsMatchIn(normalized) -> palette.warning
        terminalRanTestsRegex.containsMatchIn(normalized) -> palette.info
        else -> null
    }
}

private fun terminalAnsiColor(code: Int, palette: WebPalette): Color {
    val darkBackground = palette.panel.luminance() < 0.5f
    val colors = if (darkBackground) {
        listOf(
            Color(0xFF9AA4B2), palette.error, palette.success, palette.warning,
            Color(0xFF7CB7FF), Color(0xFFE49AFF), palette.info, Color(0xFFE8EDF6),
            Color(0xFFB5BECA), Color(0xFFFF9A9A), Color(0xFF7BEDA7), Color(0xFFFFD580),
            Color(0xFF9AC8FF), Color(0xFFF0B1FF), Color(0xFFA0ECFF), Color.White
        )
    } else {
        listOf(
            Color(0xFF20242A), palette.error, palette.success, palette.warning,
            Color(0xFF2458A6), Color(0xFF7A3D8C), palette.info, Color(0xFF4B5563),
            Color(0xFF475569), Color(0xFF9F1D16), Color(0xFF08653A), Color(0xFF765000),
            Color(0xFF174A8B), Color(0xFF692D79), Color(0xFF075E73), Color(0xFF1F2937)
        )
    }
    val index = if (code >= 90) code - 90 + 8 else code - 30
    return ensureReadableColor(colors[index.coerceIn(0, colors.lastIndex)], palette.panel)
}

private fun ansi256Color(code: Int, palette: WebPalette): Color {
    val value = code.coerceIn(0, 255)
    if (value < 16) {
        val ansiCode = if (value < 8) 30 + value else 90 + value - 8
        return terminalAnsiColor(ansiCode, palette)
    }
    if (value >= 232) {
        val gray = 8 + (value - 232) * 10
        return ensureReadableColor(Color(gray, gray, gray), palette.panel)
    }
    val cube = value - 16
    val red = cube / 36
    val green = (cube % 36) / 6
    val blue = cube % 6
    fun channel(component: Int) = if (component == 0) 0 else 55 + component * 40
    return ensureReadableColor(Color(channel(red), channel(green), channel(blue)), palette.panel)
}

private fun ensureReadableColor(foreground: Color, background: Color): Color {
    var adjusted = foreground.copy(alpha = 1f)
    val target = if (background.luminance() > 0.5f) Color.Black else Color.White
    repeat(10) {
        val lighter = maxOf(adjusted.luminance(), background.luminance()) + 0.05f
        val darker = minOf(adjusted.luminance(), background.luminance()) + 0.05f
        if (lighter / darker >= 4.5f) return adjusted
        adjusted = Color(
            red = adjusted.red * 0.82f + target.red * 0.18f,
            green = adjusted.green * 0.82f + target.green * 0.18f,
            blue = adjusted.blue * 0.82f + target.blue * 0.18f,
            alpha = 1f
        )
    }
    return adjusted
}

fun splitShellCommandLine(command: String): List<String> {
    val args = mutableListOf<String>()
    val current = StringBuilder()
    var quote: Char? = null
    var escaping = false
    command.forEach { char ->
        when {
            escaping -> {
                current.append(char)
                escaping = false
            }
            char == '\\' -> escaping = true
            quote != null && char == quote -> quote = null
            quote != null -> current.append(char)
            char == '"' || char == '\'' -> quote = char
            char.isWhitespace() -> {
                if (current.isNotEmpty()) {
                    args.add(current.toString())
                    current.clear()
                }
            }
            else -> current.append(char)
        }
    }
    if (current.isNotEmpty()) args.add(current.toString())
    return args
}
