package com.mkhokheli.pocketcodedweb

import android.content.Context
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal object NativeNodeBridge {
    external fun start(arguments: Array<String>): Int
}

internal data class NativeNodeResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val timedOut: Boolean,
) {
    val succeeded: Boolean get() = exitCode == 0 && !timedOut
}

/**
 * Owns the process-wide Node.js Mobile instance. Node.js Mobile cannot be restarted in the
 * same process, so startup is intentionally idempotent. Bounded evaluations use V8 contexts
 * behind an authenticated loopback endpoint; file sessions use NodeSessionRuntime.
 */
internal object NativeNodeRuntime {
    private const val TAG = "NativeNodeRuntime"
    private const val RUNTIME_ASSET_VERSION = "18.20.4-1"
    private const val START_TIMEOUT_MS = 12_000L
    private const val MAX_RESPONSE_BYTES = 1_100_000
    private val started = AtomicBoolean(false)
    private val ready = CountDownLatch(1)
    private var startupError: String? = null
    private var endpoint: String? = null
    private lateinit var token: String
    @Volatile private var applicationContext: Context? = null

    fun initialize(context: Context) {
        DeviceEfficiency.initialize(context)
        applicationContext = context.applicationContext
        NodeSessionRuntime.initialize(context)
    }

    private fun ensureStarted() {
        if (!started.compareAndSet(false, true)) return
        val appContext = checkNotNull(applicationContext) { "Native Node was not initialized" }
        Thread({ startHost(appContext) }, "pocketcoded-node-host").apply {
            isDaemon = true
            start()
        }
    }

    fun status(): String {
        ensureStarted()
        ready.await(START_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        val error = startupError
        if (error != null) return "Native Node unavailable: $error"
        if (endpoint == null) return "Native Node is starting (${Build.SUPPORTED_ABIS.joinToString()})"
        return try {
            val health = request("GET", "/health", null, 2_500)
            "Native Node ${health.optString("version", "unknown")} ready (${health.optString("abi", "unknown")}); bounded evaluations enabled"
        } catch (error: Throwable) {
            "Native Node host is not responding: ${safeMessage(error)}"
        }
    }

    fun runFile(root: File, working: File, file: File, args: List<String>, timeoutMs: Int = 15_000): NativeNodeResult =
        run(JSONObject().apply {
            put("mode", "file")
            put("root", root.canonicalPath)
            put("cwd", working.canonicalPath)
            put("file", file.canonicalPath)
            put("args", org.json.JSONArray(args))
            put("timeoutMs", timeoutMs)
        }, timeoutMs)

    fun evaluate(root: File, working: File, code: String, timeoutMs: Int = 15_000): NativeNodeResult =
        run(JSONObject().apply {
            put("mode", "eval")
            put("root", root.canonicalPath)
            put("cwd", working.canonicalPath)
            put("code", code)
            put("timeoutMs", timeoutMs)
        }, timeoutMs)

    private fun run(payload: JSONObject, timeoutMs: Int): NativeNodeResult {
        ensureStarted()
        check(ready.await(START_TIMEOUT_MS, TimeUnit.MILLISECONDS)) { "Native Node startup timed out" }
        startupError?.let { error(it) }
        val response = request("POST", "/run", payload, timeoutMs.coerceIn(100, 120_000) + 3_000)
        return NativeNodeResult(
            exitCode = response.optInt("exitCode", if (response.optBoolean("ok")) 0 else 1),
            stdout = response.optString("stdout"),
            stderr = response.optString("stderr").ifBlank { response.optString("error") },
            timedOut = response.optBoolean("timedOut"),
        )
    }

    private fun startHost(context: Context) {
        try {
            val runtimeDir = File(context.noBackupFilesDir, "node-runtime-v18.20.4").apply { mkdirs() }
            val versionMarker = File(runtimeDir, "asset-version")
            val worker = File(runtimeDir, "worker.js")
            val jobRunner = File(runtimeDir, "job-runner.js")
            val installedAssetVersion = runCatching { versionMarker.readText() }.getOrNull()
            if (installedAssetVersion != RUNTIME_ASSET_VERSION || !worker.isFile || !jobRunner.isFile) {
                copyAsset(context, "node-runtime/worker.js", worker)
                copyAsset(context, "node-runtime/job-runner.js", jobRunner)
                versionMarker.writeText(RUNTIME_ASSET_VERSION)
            }
            val readyFile = File(runtimeDir, "ready.json").apply { delete() }
            token = ByteArray(32).also(SecureRandom()::nextBytes).joinToString("") { "%02x".format(it) }
            val config = File(runtimeDir, "config.json")
            config.writeText(JSONObject().apply {
                put("token", token)
                put("readyPath", readyFile.absolutePath)
                put("workspaceBase", context.filesDir.canonicalPath)
            }.toString())
            config.setReadable(false, false)
            config.setReadable(true, true)
            System.loadLibrary("node")
            System.loadLibrary("pocketcoded-node")
            Thread({
                val exitCode = NativeNodeBridge.start(arrayOf("node", worker.absolutePath, config.absolutePath))
                if (endpoint == null) {
                    startupError = "Node host exited during startup (code $exitCode)"
                    ready.countDown()
                }
            }, "pocketcoded-node-main").start()
            val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(START_TIMEOUT_MS)
            while (!readyFile.isFile && System.nanoTime() < deadline) Thread.sleep(25)
            check(readyFile.isFile) { "Node host did not publish its loopback endpoint" }
            val port = JSONObject(readyFile.readText()).getInt("port")
            endpoint = "http://127.0.0.1:$port"
        } catch (error: Throwable) {
            startupError = safeMessage(error)
            Log.e(TAG, "Native Node startup failed", error)
        } finally {
            ready.countDown()
        }
    }

    private fun copyAsset(context: Context, name: String, destination: File): File {
        context.assets.open(name).use { input -> destination.outputStream().use(input::copyTo) }
        return destination
    }

    private fun request(method: String, path: String, body: JSONObject?, timeoutMs: Int): JSONObject {
        val base = endpoint ?: error(startupError ?: "Native Node is not ready")
        val connection = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = timeoutMs.coerceAtMost(10_000)
            readTimeout = timeoutMs
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            doOutput = body != null
        }
        try {
            body?.toString()?.toByteArray(Charsets.UTF_8)?.let { bytes -> connection.outputStream.use { it.write(bytes) } }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val bytes = stream?.use { it.readUpTo(MAX_RESPONSE_BYTES) } ?: ByteArray(0)
            check(bytes.size < MAX_RESPONSE_BYTES) { "Native Node response exceeded the safety limit" }
            val response = JSONObject(bytes.toString(Charsets.UTF_8).ifBlank { "{}" })
            check(connection.responseCode in 200..299) { response.optString("error", "Native Node request failed") }
            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun safeMessage(error: Throwable): String = error.message?.take(300) ?: error.javaClass.simpleName

    private fun InputStream.readUpTo(maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream(minOf(maxBytes, 16 * 1024))
        val buffer = ByteArray(16 * 1024)
        while (output.size() < maxBytes) {
            val count = read(buffer, 0, minOf(buffer.size, maxBytes - output.size()))
            if (count < 0) break
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }
}
