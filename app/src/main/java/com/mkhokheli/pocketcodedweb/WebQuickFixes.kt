package com.mkhokheli.pocketcodedweb

import java.security.MessageDigest

internal data class WebQuickFix(
    val title: String, val start: Int, val end: Int, val replacement: String,
    val sourceLength: Int, val sourceDigest: String,
)

private fun diagnosticSourceDigest(source: String): String =
    MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it.toInt() and 255) }

/** Refuse stale diagnostics, including edits outside the highlighted range. */
internal fun applyWebQuickFix(source: String, fix: WebQuickFix): Result<String> = runCatching {
    require(source.length == fix.sourceLength && diagnosticSourceDigest(source) == fix.sourceDigest) {
        "The file changed. Wait for inspections to refresh before applying this fix."
    }
    require(fix.start in 0..source.length && fix.end in fix.start..source.length) { "Invalid fix range." }
    source.replaceRange(fix.start, fix.end, fix.replacement)
}

internal fun withWebQuickFixes(name: String, source: String, diagnostics: List<CodeDiagnostic>): List<CodeDiagnostic> {
    var digest: String? = null
    val missingTags = diagnostics.mapNotNull {
        Regex("Element <([A-Za-z][A-Za-z0-9-]*)> is missing a closing tag\\.").matchEntire(it.message)?.groupValues?.get(1)
    }
    val missingDelimiters = diagnostics.mapNotNull {
        Regex("Missing closing '(.)'\\.").matchEntire(it.message)?.groupValues?.get(1)
    }.filter { it.length == 1 && it[0] in ")}]" }
    fun fix(title: String, start: Int, end: Int = start, replacement: String): WebQuickFix {
        val fingerprint = digest ?: diagnosticSourceDigest(source).also { digest = it }
        return WebQuickFix(title, start, end, replacement, source.length, fingerprint)
    }
    return diagnostics.map { diagnostic ->
        val start = diagnostic.start.coerceIn(0, source.length)
        val end = diagnostic.end.coerceIn(start, source.length)
        val token = source.substring(start, end)
        val suggestion = when {
            diagnostic.message == "Add <!DOCTYPE html> at the top for standards mode." ->
                fix("Add HTML doctype", 0, replacement = "<!DOCTYPE html>\n")
            diagnostic.message == "Add a viewport meta tag for mobile layouts." -> {
                val tagEnd = source.indexOf('>', start)
                if (tagEnd < 0) null else fix("Add mobile viewport", tagEnd + 1,
                    replacement = "\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            }
            diagnostic.message == "Images should have an 'alt' attribute for accessibility." -> {
                if (!token.endsWith('>') || Regex("\\balt\\s*=", RegexOption.IGNORE_CASE).containsMatchIn(token)) null
                else fix("Add alt attribute", end - if (token.endsWith("/>")) 2 else 1, replacement = " alt=\"\"")
            }
            diagnostic.message == "Quote attribute values for predictable HTML parsing." -> {
                val match = Regex("(\\s*)([A-Za-z_:][-A-Za-z0-9_:.]*)\\s*=\\s*([^\\s\"'<>`=]+)").matchEntire(token)
                match?.let { fix("Quote attribute value", start, end,
                    "${it.groupValues[1]}${it.groupValues[2]}=\"${it.groupValues[3]}\"") }
            }
            diagnostic.message == "CSS declarations usually end with a semicolon." -> {
                val trimmed = token.trimEnd()
                if (Regex("[ \\t]*(?:--[\\w-]+|[A-Za-z][A-Za-z-]*)\\s*:[^;{}]+").matches(trimmed))
                    fix("Add semicolon", start + trimmed.length, replacement = ";") else null
            }
            diagnostic.message == "CSS declarations need a colon between the property and value." -> {
                Regex("([ \\t]*)([A-Za-z-]+)[ \\t]+([^:;{}]+)(;?)").matchEntire(token)?.let {
                    fix("Add colon", start, end, "${it.groupValues[1]}${it.groupValues[2]}: ${it.groupValues[3]}${it.groupValues[4]}")
                }
            }
            diagnostic.message.startsWith("Element <") && diagnostic.message.endsWith("is missing a closing tag.") && missingTags.isNotEmpty() ->
                fix("Close unclosed elements", source.length, replacement = missingTags.joinToString("") { "</$it>" })
            diagnostic.message.startsWith("Missing closing '") && missingDelimiters.isNotEmpty() &&
                diagnostics.none { it.message.startsWith("Unexpected '") || it.message.startsWith("String literal is missing") || it.message.startsWith("Block comment is missing") } -> {
                val trimmedEnd = source.indexOfLast { !it.isWhitespace() } + 1
                val semicolon = source.getOrNull(trimmedEnd - 1) == ';'
                when {
                    !semicolon || missingDelimiters.all { it == "}" } ->
                        fix("Close open delimiters", source.length, replacement = missingDelimiters.joinToString(""))
                    missingDelimiters.none { it == "}" } ->
                        fix("Close open delimiters", trimmedEnd - 1, replacement = missingDelimiters.joinToString(""))
                    else -> null
                }
            }
            diagnostic.message == "Block comment is missing */." ->
                fix("Close block comment", source.length, replacement = "*/")
            diagnostic.message.startsWith("String literal is missing a closing ") -> {
                val quote = Regex("String literal is missing a closing (.) quote\\.")
                    .matchEntire(diagnostic.message)?.groupValues?.get(1)?.singleOrNull()
                if (quote != null && quote in "\"'`" && source.getOrNull(start) == quote && source.getOrNull(end - 1) != '\\')
                    fix("Close string literal", end, replacement = quote.toString()) else null
            }
            diagnostic.message.startsWith("Did you mean '") &&
                name.substringAfterLast('.').lowercase() in setOf("js", "mjs", "cjs", "jsx", "ts", "tsx") &&
                isDiagnosticCodeOffset(source, start) -> {
                val correction = Regex("Did you mean '([A-Za-z_$][\\w$]*)'\\?").matchEntire(diagnostic.message)?.groupValues?.get(1)
                if (correction != null && token.matches(Regex("[A-Za-z_$][\\w$]*")))
                    fix("Replace with $correction", start, end, correction) else null
            }
            diagnostic.message == "Remove 'debugger' statements before production." && token == "debugger" &&
                isDiagnosticCodeOffset(source, start) ->
                fix("Remove debugger statement", start, end + if (source.getOrNull(end) == ';') 1 else 0, "")
            else -> null
        }
        diagnostic.copy(quickFix = suggestion)
    }
}

/** Keep token fixes out of comments and quoted/template text. Ambiguous regexes get no fix. */
private fun isDiagnosticCodeOffset(source: String, target: Int): Boolean {
    var index = 0
    var quote: Char? = null
    var lineComment = false
    var blockComment = false
    while (index < target) {
        val current = source[index]
        val next = source.getOrNull(index + 1)
        when {
            lineComment -> { if (current == '\n') lineComment = false; index++ }
            blockComment -> if (current == '*' && next == '/') { blockComment = false; index += 2 } else index++
            quote != null -> when {
                current == '\\' -> index += 2
                current == quote -> { quote = null; index++ }
                else -> index++
            }
            current == '/' && next == '/' -> { lineComment = true; index += 2 }
            current == '/' && next == '*' -> { blockComment = true; index += 2 }
            current in "\"'`" -> { quote = current; index++ }
            // A regex literal and division cannot be distinguished safely here.
            current == '/' -> return false
            else -> index++
        }
    }
    return quote == null && !lineComment && !blockComment
}
