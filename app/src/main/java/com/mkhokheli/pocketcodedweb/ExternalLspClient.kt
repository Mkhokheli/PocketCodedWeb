package com.mkhokheli.pocketcodedweb

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

internal object ExternalLspManager {
    private const val INITIALIZE_TIMEOUT_MS = 7_000L

    private val lock = Any()
    private var client: JsonRpcLspClient? = null

    fun isActive(): Boolean = activeClient() != null

    fun status(): String {
        val current = synchronized(lock) { client }
        return current?.statusText() ?: "LSP: no external server connected."
    }

    fun stop(): String {
        val previous = synchronized(lock) {
            client.also { client = null }
        } ?: return "LSP: no external server was running."
        previous.shutdown()
        return "LSP: external server stopped."
    }

    fun startStdio(command: List<String>, rootDirectory: File): String {
        if (command.isEmpty()) return "lsp stdio: expected a server command."
        return runCatching {
            val process = ProcessBuilder(command)
                .directory(rootDirectory)
                .start()
            val nextClient = JsonRpcLspClient(
                label = "stdio ${command.joinToString(" ")}",
                rootDirectory = rootDirectory,
                input = process.inputStream,
                output = process.outputStream,
                closeConnection = { process.destroy() },
            )
            collectServerErrorLog(process.errorStream, nextClient::noteServerLog)
            initializeAndInstall(nextClient)
        }.getOrElse { error ->
            "LSP stdio failed: ${error.message ?: "unknown error"}"
        }
    }

    fun connectTcp(host: String, port: Int, rootDirectory: File): String {
        return runCatching {
            val socket = Socket(host, port).apply {
                tcpNoDelay = true
                keepAlive = true
            }
            val nextClient = JsonRpcLspClient(
                label = "tcp $host:$port",
                rootDirectory = rootDirectory,
                input = socket.getInputStream(),
                output = socket.getOutputStream(),
                closeConnection = { socket.close() },
            )
            initializeAndInstall(nextClient)
        }.getOrElse { error ->
            "LSP TCP failed: ${error.message ?: "unknown error"}"
        }
    }

    fun completions(file: File, fileName: String, source: String, cursor: Int): List<CompletionItem>? {
        val current = activeClient() ?: return null
        return current.completions(file, fileName, source, cursor)
    }

    fun diagnostics(file: File, fileName: String, source: String): List<CodeDiagnostic>? {
        val current = activeClient() ?: return null
        current.didOpenOrChange(file, fileName, source)
        return current.diagnostics(file)
    }

    fun format(file: File, fileName: String, source: String): String? {
        val current = activeClient() ?: return null
        return current.format(file, fileName, source)
    }

    private fun initializeAndInstall(nextClient: JsonRpcLspClient): String {
        val initialized = nextClient.initialize(INITIALIZE_TIMEOUT_MS)
        if (!initialized) {
            val failure = nextClient.statusText()
            nextClient.shutdown()
            return "LSP initialize failed.\n$failure"
        }
        val previous = synchronized(lock) {
            client.also { client = nextClient }
        }
        previous?.shutdown()
        return "LSP connected: ${nextClient.statusText()}"
    }

    private fun activeClient(): JsonRpcLspClient? {
        return synchronized(lock) {
            client?.takeIf { it.isActive() }
        }
    }

    private fun collectServerErrorLog(input: InputStream, onLine: (String) -> Unit) {
        thread(name = "pcw-lsp-stderr", isDaemon = true) {
            runCatching {
                input.bufferedReader().useLines { lines ->
                    lines.forEach { line -> onLine(line.take(2_000)) }
                }
            }
        }
    }
}

private class JsonRpcLspClient(
    private val label: String,
    private val rootDirectory: File,
    private val input: InputStream,
    private val output: OutputStream,
    private val closeConnection: () -> Unit,
) {
    private val nextId = AtomicLong(1L)
    private val pending = ConcurrentHashMap<String, PendingLspRequest>()
    private val documentStates = ConcurrentHashMap<String, LspDocumentState>()
    private val diagnosticsByUri = ConcurrentHashMap<String, List<CodeDiagnostic>>()
    private val writeLock = Any()

    @Volatile
    private var running = true

    @Volatile
    private var initialized = false

    @Volatile
    private var lastError = ""

    @Volatile
    private var lastServerLog = ""

    @Volatile
    private var serverCapabilities = JSONObject()

    init {
        thread(name = "pcw-lsp-reader", isDaemon = true) {
            readLoop()
        }
    }

    fun initialize(timeoutMs: Long): Boolean {
        val response = request(
            method = "initialize",
            params = initializeParams(),
            timeoutMs = timeoutMs,
            allowBeforeInitialized = true,
        ) ?: return false
        val error = response.optJSONObject("error")
        if (error != null) {
            lastError = error.optString("message").ifBlank { error.toString() }
            return false
        }
        serverCapabilities = response
            .optJSONObject("result")
            ?.optJSONObject("capabilities")
            ?: JSONObject()
        notify("initialized", JSONObject(), allowBeforeInitialized = true)
        initialized = true
        return true
    }

    fun isActive(): Boolean = running && initialized

    fun noteServerLog(line: String) {
        if (line.isNotBlank()) lastServerLog = line
    }

    fun statusText(): String {
        return buildString {
            append("LSP: ").append(if (isActive()) "active" else "inactive").append(" (").append(label).append(")")
            append('\n').append("Root: ").append(rootDirectory.absolutePath)
            serverCapabilitiesSummary().takeIf { it.isNotBlank() }?.let {
                append('\n').append("Capabilities: ").append(it)
            }
            if (lastServerLog.isNotBlank()) append('\n').append("Server log: ").append(lastServerLog)
            if (lastError.isNotBlank()) append('\n').append("Last error: ").append(lastError)
        }
    }

    fun shutdown() {
        if (initialized) {
            request("shutdown", null, timeoutMs = 1_000L)
            notify("exit", null)
        }
        running = false
        pending.values.forEach { it.completeError("LSP stopped.") }
        pending.clear()
        runCatching { closeConnection() }
    }

    fun didOpenOrChange(file: File, fileName: String, source: String) {
        if (!isActive()) return
        val uri = file.toURI().toString()
        val textHash = lspTextHash(source)
        val state = documentStates[uri]
        if (state == null) {
            documentStates[uri] = LspDocumentState(version = 1, textHash = textHash, latestText = source)
            notify(
                "textDocument/didOpen",
                JSONObject().put(
                    "textDocument",
                    JSONObject()
                        .put("uri", uri)
                        .put("languageId", lspLanguageIdForFile(fileName))
                        .put("version", 1)
                        .put("text", source),
                ),
            )
            return
        }
        if (state.textHash == textHash) return
        state.version += 1
        state.textHash = textHash
        state.latestText = source
        notify(
            "textDocument/didChange",
            JSONObject()
                .put(
                    "textDocument",
                    JSONObject()
                        .put("uri", uri)
                        .put("version", state.version),
                )
                .put("contentChanges", JSONArray().put(JSONObject().put("text", source))),
        )
    }

    fun completions(file: File, fileName: String, source: String, cursor: Int): List<CompletionItem>? {
        if (!isActive()) return null
        didOpenOrChange(file, fileName, source)
        val response = request(
            method = "textDocument/completion",
            params = JSONObject()
                .put("textDocument", JSONObject().put("uri", file.toURI().toString()))
                .put("position", lspPositionAtOffset(source, cursor))
                .put("context", JSONObject().put("triggerKind", 1)),
            timeoutMs = LSP_REQUEST_TIMEOUT_MS,
        ) ?: return null
        response.optJSONObject("error")?.let { error ->
            lastError = error.optString("message").ifBlank { error.toString() }
            return null
        }
        return parseCompletionItems(response.opt("result"))
    }

    fun diagnostics(file: File): List<CodeDiagnostic> {
        return diagnosticsByUri[file.toURI().toString()].orEmpty()
    }

    fun format(file: File, fileName: String, source: String): String? {
        if (!isActive()) return null
        didOpenOrChange(file, fileName, source)
        val response = request(
            method = "textDocument/formatting",
            params = JSONObject()
                .put("textDocument", JSONObject().put("uri", file.toURI().toString()))
                .put(
                    "options",
                    JSONObject()
                        .put("tabSize", 4)
                        .put("insertSpaces", true)
                        .put("trimTrailingWhitespace", true)
                        .put("insertFinalNewline", true),
                ),
            timeoutMs = LSP_REQUEST_TIMEOUT_MS,
        ) ?: return null
        response.optJSONObject("error")?.let { error ->
            lastError = error.optString("message").ifBlank { error.toString() }
            return null
        }
        val edits = response.optJSONArray("result") ?: return source
        return applyLspTextEdits(source, edits)
    }

    private fun request(
        method: String,
        params: Any?,
        timeoutMs: Long,
        allowBeforeInitialized: Boolean = false,
    ): JSONObject? {
        if (!running || (!initialized && !allowBeforeInitialized)) return null
        val id = nextId.getAndIncrement()
        val pendingRequest = PendingLspRequest()
        pending[id.toString()] = pendingRequest
        val message = JSONObject()
            .put("jsonrpc", "2.0")
            .put("id", id)
            .put("method", method)
        if (params != null) message.put("params", params)
        if (!send(message)) {
            pending.remove(id.toString())
            return null
        }
        return pendingRequest.await(timeoutMs).also {
            pending.remove(id.toString())
            if (it == null) lastError = "$method timed out after ${timeoutMs}ms."
        }
    }

    private fun notify(
        method: String,
        params: Any?,
        allowBeforeInitialized: Boolean = false,
    ) {
        if (!running || (!initialized && !allowBeforeInitialized)) return
        val message = JSONObject()
            .put("jsonrpc", "2.0")
            .put("method", method)
        if (params != null) message.put("params", params)
        send(message)
    }

    private fun send(message: JSONObject): Boolean {
        return runCatching {
            val body = message.toString().toByteArray(Charsets.UTF_8)
            val header = "Content-Length: ${body.size}\r\n\r\n".toByteArray(Charsets.UTF_8)
            synchronized(writeLock) {
                output.write(header)
                output.write(body)
                output.flush()
            }
            true
        }.getOrElse { error ->
            lastError = error.message ?: "LSP write failed."
            running = false
            false
        }
    }

    private fun readLoop() {
        while (running) {
            val message = runCatching { readMessage() }.getOrElse { error ->
                lastError = error.message ?: "LSP read failed."
                null
            } ?: break
            handleMessage(message)
        }
        running = false
        pending.values.forEach { it.completeError("LSP connection closed.") }
        pending.clear()
    }

    private fun readMessage(): JSONObject? {
        var contentLength = -1
        while (true) {
            val line = readHeaderLine(input) ?: return null
            if (line.isEmpty()) break
            val separator = line.indexOf(':')
            if (separator > 0 && line.substring(0, separator).equals("Content-Length", ignoreCase = true)) {
                contentLength = line.substring(separator + 1).trim().toIntOrNull() ?: -1
            }
        }
        if (contentLength <= 0) return null
        val body = readExactBytes(input, contentLength) ?: return null
        return JSONObject(body.toString(Charsets.UTF_8))
    }

    private fun handleMessage(message: JSONObject) {
        val id = message.opt("id")
        val method = message.optString("method")
        if (id != null && id != JSONObject.NULL && (message.has("result") || message.has("error"))) {
            pending.remove(id.toString())?.complete(message)
            return
        }
        when (method) {
            "textDocument/publishDiagnostics" -> handlePublishDiagnostics(message.optJSONObject("params"))
            "window/logMessage", "telemetry/event" -> {
                val params = message.optJSONObject("params")
                val text = params?.optString("message").orEmpty()
                if (text.isNotBlank()) lastServerLog = text.take(2_000)
            }
            "window/showMessage" -> {
                val text = message.optJSONObject("params")?.optString("message").orEmpty()
                if (text.isNotBlank()) lastServerLog = text.take(2_000)
            }
        }
        if (id != null && id != JSONObject.NULL && method.isNotBlank()) {
            respondToServerRequest(id, defaultResultForServerRequest(method, message.optJSONObject("params")))
        }
    }

    private fun respondToServerRequest(id: Any, result: Any) {
        val response = JSONObject()
            .put("jsonrpc", "2.0")
            .put("id", id)
            .put("result", result)
        send(response)
    }

    private fun defaultResultForServerRequest(method: String, params: JSONObject?): Any {
        return when (method) {
            "workspace/configuration" -> {
                val items = params?.optJSONArray("items")
                JSONArray().apply {
                    repeat(items?.length() ?: 0) { put(JSONObject()) }
                }
            }
            "workspace/workspaceFolders" -> JSONArray().put(
                JSONObject()
                    .put("uri", rootDirectory.toURI().toString())
                    .put("name", rootDirectory.name),
            )
            "window/showMessageRequest" -> JSONObject.NULL
            "client/registerCapability", "client/unregisterCapability" -> JSONObject.NULL
            "window/workDoneProgress/create" -> JSONObject.NULL
            else -> JSONObject.NULL
        }
    }

    private fun handlePublishDiagnostics(params: JSONObject?) {
        params ?: return
        val uri = params.optString("uri")
        if (uri.isBlank()) return
        val source = documentStates[uri]?.latestText
        val diagnostics = params.optJSONArray("diagnostics").orEmptyJsonArray()
            .mapJsonObjects { diagnostic ->
                lspDiagnosticToCodeDiagnostic(source.orEmpty(), diagnostic)
            }
        diagnosticsByUri[uri] = diagnostics
    }

    private fun initializeParams(): JSONObject {
        val rootUri = rootDirectory.toURI().toString()
        return JSONObject()
            .put("processId", JSONObject.NULL)
            .put("rootUri", rootUri)
            .put("rootPath", rootDirectory.absolutePath)
            .put(
                "workspaceFolders",
                JSONArray().put(
                    JSONObject()
                        .put("uri", rootUri)
                        .put("name", rootDirectory.name),
                ),
            )
            .put("capabilities", clientCapabilities())
            .put("initializationOptions", JSONObject())
            .put("trace", "off")
    }

    private fun clientCapabilities(): JSONObject {
        val documentationFormats = JSONArray().put("markdown").put("plaintext")
        return JSONObject()
            .put(
                "workspace",
                JSONObject()
                    .put("workspaceFolders", true)
                    .put("configuration", true)
                    .put("applyEdit", false),
            )
            .put(
                "textDocument",
                JSONObject()
                    .put(
                        "synchronization",
                        JSONObject()
                            .put("dynamicRegistration", false)
                            .put("didSave", true),
                    )
                    .put(
                        "completion",
                        JSONObject()
                            .put("dynamicRegistration", false)
                            .put(
                                "completionItem",
                                JSONObject()
                                    .put("snippetSupport", true)
                                    .put("documentationFormat", documentationFormats)
                                    .put("insertReplaceSupport", true)
                                    .put("labelDetailsSupport", true),
                            ),
                    )
                    .put(
                        "signatureHelp",
                        JSONObject()
                            .put(
                                "signatureInformation",
                                JSONObject().put("documentationFormat", documentationFormats),
                            ),
                    )
                    .put("hover", JSONObject().put("contentFormat", documentationFormats))
                    .put(
                        "publishDiagnostics",
                        JSONObject()
                            .put("relatedInformation", true)
                            .put("versionSupport", true),
                    )
                    .put("formatting", JSONObject().put("dynamicRegistration", false)),
            )
    }

    private fun serverCapabilitiesSummary(): String {
        if (serverCapabilities.length() == 0) return ""
        return listOfNotNull(
            "completion".takeIf { serverCapabilities.has("completionProvider") },
            "diagnostics".takeIf { initialized },
            "formatting".takeIf { serverCapabilities.optBoolean("documentFormattingProvider") },
            "hover".takeIf { serverCapabilities.has("hoverProvider") },
            "signatureHelp".takeIf { serverCapabilities.has("signatureHelpProvider") },
        ).joinToString(", ")
    }

    private fun lspDiagnosticToCodeDiagnostic(source: String, diagnostic: JSONObject): CodeDiagnostic {
        val range = diagnostic.optJSONObject("range")
        val start = range?.optJSONObject("start") ?: JSONObject()
        val end = range?.optJSONObject("end") ?: start
        val startOffset = offsetAtLspPosition(source, start)
        val endOffset = offsetAtLspPosition(source, end).coerceAtLeast(startOffset)
        val severity = when (diagnostic.optInt("severity", 3)) {
            1 -> "error"
            2 -> "warning"
            4 -> "hint"
            else -> "info"
        }
        return CodeDiagnostic(
            line = start.optInt("line", 0) + 1,
            column = start.optInt("character", 0) + 1,
            start = startOffset,
            end = if (endOffset > startOffset) endOffset else (startOffset + 1).coerceAtMost(source.length),
            severity = severity,
            message = diagnostic.optString("message").ifBlank { "Language server diagnostic." },
        )
    }
}

private const val LSP_REQUEST_TIMEOUT_MS = 2_500L

private class PendingLspRequest {
    private val latch = CountDownLatch(1)

    @Volatile
    private var response: JSONObject? = null

    fun complete(message: JSONObject) {
        response = message
        latch.countDown()
    }

    fun completeError(message: String) {
        response = JSONObject()
            .put("jsonrpc", "2.0")
            .put("error", JSONObject().put("message", message))
        latch.countDown()
    }

    fun await(timeoutMs: Long): JSONObject? {
        return if (latch.await(timeoutMs, TimeUnit.MILLISECONDS)) response else null
    }
}

private data class LspDocumentState(
    var version: Int,
    var textHash: Int,
    var latestText: String = "",
)

private fun parseCompletionItems(result: Any?): List<CompletionItem> {
    val items = when (result) {
        is JSONArray -> result
        is JSONObject -> result.optJSONArray("items")
        else -> null
    } ?: return emptyList()
    return items.mapJsonObjects { item ->
        val label = item.optString("label")
        val textEdit = item.optJSONObject("textEdit")
        val insertText = cleanLspSnippet(
            item.optString("insertText")
                .ifBlank { textEdit?.optString("newText").orEmpty() }
                .ifBlank { label },
        )
        val detail = item.optString("detail")
            .ifBlank { lspCompletionKindName(item.optInt("kind", 0)) }
        CompletionItem(
            label = label.ifBlank { insertText },
            insertText = insertText,
            detail = detail,
            cursorOffset = insertText.length,
            documentation = when (val documentation = item.opt("documentation")) {
                is JSONObject -> documentation.optString("value")
                is String -> documentation
                else -> ""
            },
        )
    }.filter { it.label.isNotBlank() }
}

private fun applyLspTextEdits(source: String, edits: JSONArray): String {
    val parsedEdits = edits.mapJsonObjects { edit ->
        val range = edit.optJSONObject("range") ?: JSONObject()
        val start = offsetAtLspPosition(source, range.optJSONObject("start") ?: JSONObject())
        val end = offsetAtLspPosition(source, range.optJSONObject("end") ?: JSONObject())
            .coerceAtLeast(start)
        LspTextEdit(start, end, edit.optString("newText"))
    }.sortedWith(compareByDescending<LspTextEdit> { it.start }.thenByDescending { it.end })
    return parsedEdits.fold(source) { current, edit ->
        val start = edit.start.coerceIn(0, current.length)
        val end = edit.end.coerceIn(start, current.length)
        current.replaceRange(start, end, edit.newText)
    }
}

private data class LspTextEdit(
    val start: Int,
    val end: Int,
    val newText: String,
)

private fun lspPositionAtOffset(source: String, rawOffset: Int): JSONObject {
    val offset = rawOffset.coerceIn(0, source.length)
    var line = 0
    var lineStart = 0
    var index = 0
    while (index < offset) {
        if (source[index] == '\n') {
            line += 1
            lineStart = index + 1
        }
        index += 1
    }
    return JSONObject()
        .put("line", line)
        .put("character", offset - lineStart)
}

private fun offsetAtLspPosition(source: String, position: JSONObject): Int {
    val targetLine = position.optInt("line", 0).coerceAtLeast(0)
    val targetCharacter = position.optInt("character", 0).coerceAtLeast(0)
    var line = 0
    var index = 0
    while (line < targetLine && index < source.length) {
        if (source[index] == '\n') line += 1
        index += 1
    }
    return (index + targetCharacter).coerceIn(0, source.lineEndFrom(index))
}

private fun String.lineEndFrom(start: Int): Int {
    val safeStart = start.coerceIn(0, length)
    return indexOf('\n', safeStart).takeIf { it >= 0 } ?: length
}

private fun lspLanguageIdForFile(fileName: String): String {
    return when (fileName.substringAfterLast('.', "").lowercase()) {
        "html", "htm" -> "html"
        "css" -> "css"
        "js", "mjs", "cjs" -> "javascript"
        "jsx" -> "javascriptreact"
        "ts" -> "typescript"
        "tsx" -> "typescriptreact"
        "json" -> "json"
        "md" -> "markdown"
        "kt", "kts" -> "kotlin"
        "java" -> "java"
        "py", "pyw" -> "python"
        "c", "h" -> "c"
        "cc", "cpp", "cxx", "hpp" -> "cpp"
        "cs" -> "csharp"
        "go" -> "go"
        "rs" -> "rust"
        "swift" -> "swift"
        "dart" -> "dart"
        "php", "phtml" -> "php"
        "rb" -> "ruby"
        "sh", "bash", "zsh" -> "shellscript"
        "ps1" -> "powershell"
        "sql" -> "sql"
        "xml" -> "xml"
        "yaml", "yml" -> "yaml"
        "toml" -> "toml"
        else -> "plaintext"
    }
}

private fun lspTextHash(source: String): Int {
    return 31 * source.length + source.hashCode()
}

private fun cleanLspSnippet(raw: String): String {
    return raw
        .replace(Regex("""\$\{(\d+):([^}]*)}""")) { match -> match.groupValues[2] }
        .replace(Regex("""\$\d+"""), "")
        .replace("\\$", "$")
}

private fun lspCompletionKindName(kind: Int): String {
    return when (kind) {
        1 -> "text"
        2 -> "method"
        3 -> "function"
        4 -> "constructor"
        5 -> "field"
        6 -> "variable"
        7 -> "class"
        8 -> "interface"
        9 -> "module"
        10 -> "property"
        14 -> "keyword"
        15 -> "snippet"
        21 -> "constant"
        22 -> "struct"
        25 -> "type"
        else -> ""
    }
}

private fun readHeaderLine(input: InputStream): String? {
    val bytes = mutableListOf<Byte>()
    while (true) {
        val value = input.read()
        if (value < 0) return if (bytes.isEmpty()) null else bytes.toByteArray().toString(Charsets.UTF_8)
        if (value == '\n'.code) break
        if (value != '\r'.code) bytes += value.toByte()
    }
    return bytes.toByteArray().toString(Charsets.UTF_8)
}

private fun readExactBytes(input: InputStream, length: Int): ByteArray? {
    val buffer = ByteArray(length)
    var offset = 0
    while (offset < length) {
        val read = input.read(buffer, offset, length - offset)
        if (read < 0) return null
        offset += read
    }
    return buffer
}

private fun JSONArray?.orEmptyJsonArray(): JSONArray = this ?: JSONArray()

private fun <T> JSONArray.mapJsonObjects(transform: (JSONObject) -> T): List<T> {
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.let { add(transform(it)) }
        }
    }
}
