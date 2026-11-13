package com.mkhokheli.pocketcodedweb

import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal data class WebFileDiagnostic(val file: File, val relativePath: String, val preview: String, val diagnostic: CodeDiagnostic)
internal data class WebDiagnosticScan(val entries: List<WebFileDiagnostic>, val truncated: Boolean)

internal fun webDiagnosticPrompt(path: String, diagnostic: CodeDiagnostic): String =
    "Explain this coding diagnostic and suggest a safe fix.\nFile: $path\n" +
        "Diagnostic: ${diagnostic.severity} on line ${diagnostic.line}, column ${diagnostic.column}: ${diagnostic.message}"

internal suspend fun scanWebProjectDiagnostics(
    rootDirectory: File?, activeFile: File, activeSource: String,
    maxFiles: Int = 1_000, maxBytes: Long = 8_000_000, maxDiagnostics: Int = 500,
): WebDiagnosticScan = withContext(Dispatchers.IO) {
    require(maxFiles > 0 && maxBytes > 0 && maxDiagnostics > 0)
    val root = rootDirectory?.canonicalFile
    val active = activeFile.canonicalFile
    val excluded = setOf(".git", ".ssh", ".pocketcodedweb", "node_modules", "dist", "build", "coverage", ".next", ".nuxt")
    val extensions = setOf("html", "htm", "css", "scss", "js", "mjs", "cjs", "jsx", "ts", "tsx", "json", "py", "pyw", "php", "phtml", "sql")
    fun contained(file: File): Boolean = root == null ||
        (file.canonicalFile == file.absoluteFile && file.path.startsWith(root.path + File.separator))
    val candidates = sequence {
        if (contained(active)) yield(active)
        if (root != null) yieldAll(root.walkTopDown().onEnter { folder ->
            folder == root || (folder.name !in excluded && contained(folder))
        }.filter { it.isFile && it != active && it.extension.lowercase() in extensions && contained(it) })
    }
    val results = mutableListOf<WebFileDiagnostic>()
    var bytes = 0L
    var files = 0
    for (file in candidates) {
        currentCoroutineContext().ensureActive()
        if (files >= maxFiles) return@withContext WebDiagnosticScan(results, true)
        if (file.length() > 1_000_000) continue
        val source = if (file == active) activeSource else runCatching {
            val data = file.inputStream().use { it.readBytesBounded(1_000_000) }
            if (data.any { it == 0.toByte() }) return@runCatching null
            Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(data)).toString()
        }.getOrNull() ?: continue
        val size = source.toByteArray(Charsets.UTF_8).size
        if (size > maxBytes - bytes) return@withContext WebDiagnosticScan(results, true)
        bytes += size
        files++
        for (diagnostic in inspectWebSource(file.name, source)) {
            if (results.size >= maxDiagnostics) return@withContext WebDiagnosticScan(results, true)
            val offset = diagnostic.start.coerceIn(0, source.length)
            val lineStart = source.lastIndexOf('\n', offset - 1) + 1
            val lineEnd = source.indexOf('\n', offset).takeIf { it >= 0 } ?: source.length
            results += WebFileDiagnostic(file, root?.let { file.relativeTo(it).invariantSeparatorsPath } ?: file.name,
                source.substring(lineStart, minOf(lineEnd, lineStart + 240)).trim(), diagnostic)
        }
    }
    WebDiagnosticScan(results.sortedWith(compareBy<WebFileDiagnostic> {
        when (it.diagnostic.severity.lowercase()) { "error" -> 0; "warning" -> 1; else -> 2 }
    }.thenBy { it.relativePath }.thenBy { it.diagnostic.line }), false)
}

private fun java.io.InputStream.readBytesBounded(limit: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val read = read(buffer)
        if (read < 0) return output.toByteArray()
        require(output.size() + read <= limit) { "File is too large to inspect." }
        output.write(buffer, 0, read)
    }
}
