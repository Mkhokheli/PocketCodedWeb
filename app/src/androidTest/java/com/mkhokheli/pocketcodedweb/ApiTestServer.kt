package com.mkhokheli.pocketcodedweb

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import org.json.JSONObject

/** Loopback fixture: no credentials or traffic leave the phone. */
internal class ApiTestServer : AutoCloseable {
    private val server = ServerSocket(0, 20, java.net.InetAddress.getByName("127.0.0.1"))
    private val workers = Executors.newCachedThreadPool()
    val url = "http://127.0.0.1:${server.localPort}"
    init {
        workers.submit {
            while (!server.isClosed) {
                val socket = runCatching { server.accept() }.getOrNull() ?: break
                workers.submit { runCatching { handle(socket) }; runCatching { socket.close() } }
            }
        }
    }
    private fun handle(socket: Socket) {
        socket.soTimeout = 5_000
        val input = socket.getInputStream()
        fun line(): String {
            val bytes = java.io.ByteArrayOutputStream()
            while (true) {
                val ch = input.read()
                if (ch == -1 || ch == 10) break
                if (ch != 13) bytes.write(ch)
            }
            return bytes.toString("UTF-8")
        }
        val request = line().split(' ')
        if (request.size < 2) return
        val method = request[0]
        val path = request[1]
        val headers = linkedMapOf<String, String>()
        while (true) {
            val header = line()
            if (header.isEmpty()) break
            headers[header.substringBefore(':').lowercase()] = header.substringAfter(':').trim()
        }
        val body = ByteArray(headers["content-length"]?.toIntOrNull() ?: 0)
        var read = 0
        while (read < body.size) { val count = input.read(body, read, body.size - read); if (count < 0) break; read += count }
        if (path.startsWith("/slow")) Thread.sleep(3_000)
        val status = when {
            path.startsWith("/error") -> "422 Unprocessable Entity"
            path.startsWith("/empty") -> "204 No Content"
            path.startsWith("/redirect") -> "302 Found"
            else -> "200 OK"
        }
        val text = when {
            path.startsWith("/large") -> "x".repeat(API_RESPONSE_LIMIT + 32_000)
            path.startsWith("/charset") -> "caf\u00e9"
            path.startsWith("/empty") -> ""
            else -> JSONObject().put("method", method).put("path", path)
                .put("body", String(body, Charsets.UTF_8)).put("token", headers["x-test-token"].orEmpty())
                .put("error", path.startsWith("/error")).toString()
        }
        val charset = if (path.startsWith("/charset")) Charsets.ISO_8859_1 else Charsets.UTF_8
        val data = text.toByteArray(charset)
        val output = socket.getOutputStream()
        output.write(("HTTP/1.1 $status\r\nContent-Type: application/json; charset=${charset.name()}\r\n" +
            "Content-Length: ${data.size}\r\nX-Api-Test: phone\r\nLocation: /echo\r\nConnection: close\r\n\r\n").toByteArray())
        if (method != "HEAD") output.write(data)
        output.flush()
    }
    override fun close() { server.close(); workers.shutdownNow() }
}
