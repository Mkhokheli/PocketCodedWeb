package com.mkhokheli.pocketcodedweb

import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal data class WebSearchOptions(
    val query: String = "",
    val replacement: String = "",
    val matchCase: Boolean = false,
    val wholeWord: Boolean = false,
    val regex: Boolean = false,
    val fileFilter: String = "",
)

internal data class WebSearchMatch(
    val file: File,
    val relativePath: String,
    val line: Int,
    val preview: String,
    val start: Int,
    val end: Int,
    val original: String,
)

internal data class WebSearchResults(val matches: List<WebSearchMatch>, val truncated: Boolean)
internal data class WebReplaceResult(val count: Int, val files: List<File>, val errors: List<String>)

internal fun webSearchRegex(options: WebSearchOptions): Regex {
    require(options.query.isNotEmpty()) { "Enter a search term." }
    val body = if (options.regex) options.query else Regex.escape(options.query)
    val expression = if (options.wholeWord) "(?<![\\p{L}\\p{N}_])(?:$body)(?![\\p{L}\\p{N}_])" else body
    return Regex(expression, if (options.matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE))
}

private val searchExcludedDirectories = setOf(
    ".git", ".ssh", ".pocketcodedweb", ".gradle", ".idea", ".cxx", "node_modules",
    "build", "dist", "out", "coverage", ".next", ".nuxt",
)
private val searchBinaryExtensions = setOf(
    "db", "db3", "sqlite", "sqlite3", "database", "duckdb", "jar", "class", "zip", "gz",
    "apk", "aab", "png", "jpg", "jpeg", "gif", "webp", "ico", "pdf", "so", "dll",
    "woff", "woff2", "ttf", "mp3", "mp4", "wasm",
)

private fun containedSearchFile(root: File, file: File): Boolean = runCatching {
    val canonical = file.canonicalFile
    // Do not traverse symlinks, even ones pointing back into the project.
    canonical == file.absoluteFile && canonical.path.startsWith(root.path + File.separator) &&
        file.relativeTo(root).invariantSeparatorsPath.split('/').dropLast(1)
            .none { it in searchExcludedDirectories }
}.getOrDefault(false)

private fun searchFiles(project: File, scope: File, filter: String): Sequence<File> {
    require(scope == project || containedSearchFile(project, scope)) { "Search scope must be inside this project." }
    val filters = filter.split(',').map(String::trim).filter(String::isNotEmpty).map { glob ->
        Regex(if ('*' in glob) "^" + glob.split('*').joinToString(".*") { Regex.escape(it) } + "$" else Regex.escape(glob), RegexOption.IGNORE_CASE)
    }
    val candidates = if (scope.isFile) sequenceOf(scope) else scope.walkTopDown().onEnter { folder ->
        folder == scope || (folder.name !in searchExcludedDirectories && containedSearchFile(project, folder))
    }
    return candidates.filter { file ->
        file.isFile && file.length() <= 2_000_000 && containedSearchFile(project, file) &&
            file.extension.lowercase() !in searchBinaryExtensions &&
            (filters.isEmpty() || filters.any { it.containsMatchIn(file.name) })
    }
}

private fun readSearchText(file: File): String? = runCatching {
    val bytes = file.inputStream().use { input ->
        val buffer = java.io.ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        var total = 0
        while (true) {
            val read = input.read(chunk)
            if (read < 0) break
            total += read
            if (total > 2_000_000) return null
            buffer.write(chunk, 0, read)
        }
        buffer.toByteArray()
    }
    if (bytes.size > 2_000_000 || bytes.any { it == 0.toByte() }) return null
    Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
}.getOrNull()

internal suspend fun findWebMatches(projectRoot: File, searchRoot: File, options: WebSearchOptions,
    limit: Int = 2_000,
    maxSnapshotChars: Int = 8_000_000,
): WebSearchResults = withContext(Dispatchers.IO) {
    if (options.query.isEmpty()) return@withContext WebSearchResults(emptyList(), false)
    require(limit > 0)
    require(maxSnapshotChars > 0)
    val pattern = webSearchRegex(options)
    val project = projectRoot.canonicalFile
    val result = mutableListOf<WebSearchMatch>()
    var snapshotChars = 0
    // Android may expose the same app directory through /data/data and /data/user/0.
    // Normalize the selected scope before comparing it with the canonical project.
    for (file in searchFiles(project, searchRoot.canonicalFile, options.fileFilter)) {
        currentCoroutineContext().ensureActive()
        val text = readSearchText(file) ?: continue
        var line = 1
        var scanned = 0
        var retained = false
        for (match in pattern.findAll(text)) {
            currentCoroutineContext().ensureActive()
            if (result.size >= limit) return@withContext WebSearchResults(result, true)
            if (!retained) {
                if (text.length > maxSnapshotChars - snapshotChars) return@withContext WebSearchResults(result, true)
                snapshotChars += text.length
                retained = true
            }
            while (scanned < match.range.first) { if (text[scanned++] == '\n') line++ }
            val lineStart = text.lastIndexOf('\n', (match.range.first - 1).coerceAtLeast(-1)) + 1
            val lineEnd = text.indexOf('\n', match.range.first).takeIf { it >= 0 } ?: text.length
            result += WebSearchMatch(file, file.relativeTo(project).invariantSeparatorsPath, line,
                text.substring(lineStart, lineEnd).trim().take(300), match.range.first,
                match.range.last + 1, text)
        }
    }
    WebSearchResults(result, false)
}

/** Apply exactly the displayed snapshot, preserving prior versions and rejecting stale results. */
internal suspend fun replaceWebMatches(matches: List<WebSearchMatch>, options: WebSearchOptions,
    store: ProjectFileStore,
): WebReplaceResult = withContext(Dispatchers.IO) {
    val pattern = webSearchRegex(options)
    var count = 0
    val changed = mutableListOf<File>()
    val errors = mutableListOf<String>()
    for ((file, selected) in matches.groupBy { it.file }) {
        currentCoroutineContext().ensureActive()
        runCatching {
            require(containedSearchFile(store.projectDirectory, file)) { "File is outside the project." }
            val original = selected.first().original
            check(readSearchText(file) == original) { "File changed; search again before replacing." }
            val positions = selected.associateBy { it.start }
            var replacements = 0
            val updated = pattern.replace(original) { match ->
                val expected = positions[match.range.first]
                if (expected == null || expected.end != match.range.last + 1) match.value else {
                    replacements++
                    if (options.regex) expandSearchReplacement(options.replacement, match) else options.replacement
                }
            }
            if (updated != original) {
                store.save(store.prepareEdit(file.path, updated, expectedContent = original)).getOrThrow()
                changed += file
            }
            count += replacements
        }.onFailure { errors += "${file.name}: ${it.message}" }
    }
    WebReplaceResult(count, changed, errors)
}

// Regex replacement supports $0, $1... and ${name}; ordinary replacements stay literal.
private fun expandSearchReplacement(replacement: String, match: MatchResult): String = buildString {
    var index = 0
    while (index < replacement.length) {
        val char = replacement[index++]
        when {
            char == '\\' -> { require(index < replacement.length) { "Trailing replacement escape." }; append(replacement[index++]) }
            char != '$' -> append(char)
            replacement.getOrNull(index) == '{' -> {
                val end = replacement.indexOf('}', index + 1)
                require(end > index + 1) { "Invalid named replacement group." }
                append(match.groups[replacement.substring(index + 1, end)]?.value.orEmpty()); index = end + 1
            }
            else -> {
                require(replacement.getOrNull(index)?.isDigit() == true) { "Use a group number after $. Escape literal $ as \\$." }
                var group = replacement[index++].digitToInt()
                require(group < match.groups.size) { "Unknown replacement group $$group." }
                while (replacement.getOrNull(index)?.isDigit() == true) {
                    val next = group * 10 + replacement[index].digitToInt()
                    if (next >= match.groups.size) break
                    group = next; index++
                }
                append(match.groups[group]?.value.orEmpty())
            }
        }
    }
}
