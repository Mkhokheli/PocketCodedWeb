package com.mkhokheli.pocketcodedweb

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.Charset
import java.util.concurrent.CancellationException

internal const val API_RESPONSE_LIMIT = 1024 * 1024
internal val API_METHODS = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")

internal data class ApiRequest(
    val method: String, val url: String, val headers: String = "",
    val body: String = "", val timeoutSeconds: Int = 15,
) {
    fun validate(): List<Pair<String, String>> {
        require(method in API_METHODS) { "Choose a supported HTTP method." }
        val uri = runCatching { URI(url.trim()) }.getOrNull()
        require(uri != null && uri.scheme?.lowercase() in listOf("http", "https") && !uri.host.isNullOrBlank()) {
            "Enter a complete http:// or https:// URL."
        }
        require(uri.rawUserInfo == null) { "Use an Authorization header instead of credentials in the URL." }
        require(uri.port in -1..65535) { "The URL port must be between 0 and 65535." }
        require(timeoutSeconds in 1..120) { "Timeout must be between 1 and 120 seconds." }
        require(body.toByteArray(Charsets.UTF_8).size <= API_RESPONSE_LIMIT) { "Request body exceeds 1 MB." }
        return headers.lineSequence().filter { it.isNotBlank() }.mapIndexed { index, line ->
            val colon = line.indexOf(':')
            require(colon > 0) { "Header ${index + 1} must use Name: value." }
            val name = line.substring(0, colon).trim()
            val value = line.substring(colon + 1).trim()
            require(name.matches(Regex("[!#$%&'*+.^_`|~0-9A-Za-z-]+")) && value.none { it == '\r' || it == '\n' || it == '\u0000' }) {
                "Header ${index + 1} contains invalid characters."
            }
            require(name.lowercase() !in listOf("content-length", "transfer-encoding")) {
                "$name is set automatically."
            }
            name to value
        }.toList()
    }
}

internal data class ApiResponse(
    val status: Int, val reason: String, val headers: String, val body: String,
    val elapsedMillis: Long, val bytes: Int, val truncated: Boolean,
)

/** Each call has its own connection. Closing a card cancels work without touching editor files. */
internal class ApiRequestCall {
    @Volatile private var connection: HttpURLConnection? = null
    @Volatile private var canceled = false
    fun cancel() { canceled = true; connection?.disconnect() }

    fun execute(request: ApiRequest): ApiResponse {
        val headers = request.validate()
        val started = System.nanoTime()
        val conn = URI(request.url.trim()).toURL().openConnection() as HttpURLConnection
        connection = conn
        try {
            if (canceled) throw CancellationException()
            conn.requestMethod = request.method
            conn.connectTimeout = request.timeoutSeconds * 1000
            conn.readTimeout = request.timeoutSeconds * 1000
            // Show redirects and their Location header instead of forwarding credentials to another host.
            conn.instanceFollowRedirects = false
            conn.useCaches = false
            headers.forEach { (name, value) -> conn.addRequestProperty(name, value) }
            if (request.method !in listOf("GET", "HEAD") && request.body.isNotEmpty()) {
                conn.doOutput = true
                if (headers.none { it.first.equals("Content-Type", true) })
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                val data = request.body.toByteArray(Charsets.UTF_8)
                conn.setFixedLengthStreamingMode(data.size)
                conn.outputStream.use { it.write(data) }
            }
            val status = conn.responseCode
            val responseHeaders = conn.headerFields.entries.filter { it.key != null }
                .sortedBy { it.key.lowercase() }.flatMap { (key, values) -> values.map { "$key: $it" } }.joinToString("\n")
            val output = ByteArrayOutputStream()
            val stream = if (status >= 400) conn.errorStream else conn.inputStream
            stream?.use { input ->
                val buffer = ByteArray(8192)
                while (output.size() <= API_RESPONSE_LIMIT) {
                    if (canceled) throw CancellationException()
                    val count = input.read(buffer, 0, minOf(buffer.size, API_RESPONSE_LIMIT + 1 - output.size()))
                    if (count < 0) break
                    output.write(buffer, 0, count)
                }
            }
            if (canceled) throw CancellationException()
            val bytes = output.toByteArray()
            val truncated = bytes.size > API_RESPONSE_LIMIT
            val charsetName = Regex("charset\\s*=\\s*[\"']?([^;\\s\"']+)", RegexOption.IGNORE_CASE)
                .find(conn.contentType.orEmpty())?.groupValues?.get(1)
            val charset = runCatching { Charset.forName(charsetName ?: "UTF-8") }.getOrDefault(Charsets.UTF_8)
            return ApiResponse(status, conn.responseMessage.orEmpty(), responseHeaders,
                String(bytes, 0, minOf(bytes.size, API_RESPONSE_LIMIT), charset),
                (System.nanoTime() - started) / 1_000_000, minOf(bytes.size, API_RESPONSE_LIMIT), truncated)
        } finally {
            conn.disconnect()
            connection = null
        }
    }
}
