package com.mkhokheli.pocketcodedweb

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.os.Build
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

internal object NodeSessionRuntime {
    private data class CachedStatus(val modified: Long, val size: Long, val value: JSONObject)
    @Volatile private var cachedStatus: CachedStatus? = null
    private lateinit var context: Context
    fun initialize(value: Context) { context = value.applicationContext }
    private val directory get() = File(context.noBackupFilesDir, "node-session")
    private fun sessionProcesses() =
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .runningAppProcesses.orEmpty().filter { it.processName == "${context.packageName}:nodeSession" && it.uid == android.os.Process.myUid() }
    fun isRunning(): Boolean = ::context.isInitialized && sessionProcesses().isNotEmpty()
    private fun running(): Boolean = isRunning()

    fun previewUrl(): String? = if (running()) readStatus().optJSONArray("urls")?.optString(0)?.takeIf { it.isNotBlank() } else null
    fun isRunningFor(root: File): Boolean = running() && runCatching {
        JSONObject(File(directory, "config.json").readText()).optString("root") == root.canonicalPath
    }.getOrDefault(false)

    fun awaitPreviewUrl(timeoutMs: Long = 30_000): String? {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            val status = readStatus()
            status.optJSONArray("urls")?.optString(0)?.takeIf { it.isNotBlank() && running() }?.let { return it }
            if (status.optString("state") in setOf("failed", "exited")) return null
            Thread.sleep(200)
        }
        return previewUrl()
    }
    private fun readStatus(): JSONObject = runCatching {
        val file = File(directory, "status.json")
        val modified = file.lastModified()
        val size = file.length()
        val cached = cachedStatus
        if (modified != 0L && cached?.modified == modified && cached.size == size) cached.value
        else JSONObject(file.readText()).also { cachedStatus = CachedStatus(modified, size, it) }
    }.getOrElse { JSONObject().put("state", "starting") }

    @Synchronized
    fun start(root: File, working: File, file: File, args: List<String>, env: Map<String, String> = emptyMap()): WebShellResult = runCatching {
        check(!running()) { "A Node session is already running. Use node stop before starting another; node logs shows its output." }
        fun inside(parent: File, child: File): Boolean = child.canonicalPath == parent.canonicalPath || child.canonicalPath.startsWith(parent.canonicalPath + File.separator)
        require(inside(context.filesDir, root) && inside(root, working) && inside(root, file)) { "Node entry point is outside the project." }
        require(file.isFile) { "Node entry point does not exist: ${file.name}" }
        directory.mkdirs()
        File(directory, "status.json").delete()
        cachedStatus = null
        File(directory, "session.log").writeText("")
        context.assets.open("node-runtime/session.cjs").use { input -> File(directory, "session.cjs").outputStream().use { input.copyTo(it) } }
        File(directory, "config.json").writeText(JSONObject()
            .put("root", root.canonicalPath)
            .put("maxLogChars", DeviceEfficiency.budget.nodeLogChars)
            .put("logFlushMs", DeviceEfficiency.budget.logFlushMs)
            .put("cwd", working.canonicalPath).put("file", file.canonicalPath)
            .put("args", JSONArray(args)).put("env", JSONObject(env))
            .put("statusPath", File(directory, "status.json").absolutePath)
            .put("logPath", File(directory, "session.log").absolutePath).toString())
        val intent = Intent(context, NodeSessionService::class.java)
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
        val deadline = SystemClock.elapsedRealtime() + 5_000
        while (SystemClock.elapsedRealtime() < deadline) {
            val status = readStatus()
            if ((status.optJSONArray("urls")?.length() ?: 0) > 0 || status.optString("state") in setOf("exited", "failed")) break
            Thread.sleep(100)
        }
        if (!running() && readStatus().optString("state") == "starting") {
            WebShellResult(error = "The Node session did not start. Retry while the app is visible.")
        } else status()
    }.getOrElse { WebShellResult(error = it.message ?: "Could not start Node session") }

    fun status(): WebShellResult {
        val status = readStatus()
        val state = if (running()) status.optString("state") else status.optString("state").takeIf { it in setOf("failed", "exited") } ?: "stopped"
        val log = runCatching { File(directory, "session.log").readText() }.getOrDefault("")
        val urls = status.optJSONArray("urls")
        val addresses = (0 until (urls?.length() ?: 0)).map { urls!!.optString(it) }
            .filter { it.isNotBlank() && !log.contains(it) && state == "running" }
            .joinToString("\n") { "[preview] $it" }
        return WebShellResult(
            output = listOf("Node session: $state", addresses, log, "Use node logs, node stop, or preview to open the server.")
                .filter { it.isNotBlank() }.joinToString("\n"),
            error = if (state == "failed") "Node session failed. See the output above." else "",
        )
    }

    fun stop(force: Boolean = false): WebShellResult {
        context.stopService(Intent(context, NodeSessionService::class.java))
        val deadline = SystemClock.elapsedRealtime() + if (force) 0 else 800
        while (running() && SystemClock.elapsedRealtime() < deadline) Thread.sleep(25)
        // Kill only this app's dedicated session, never the editor or bounded Node host.
        sessionProcesses().forEach { android.os.Process.killProcess(it.pid) }
        val killDeadline = SystemClock.elapsedRealtime() + 1_000
        while (running() && SystemClock.elapsedRealtime() < killDeadline) Thread.sleep(25)
        return if (running()) WebShellResult(error = "Node session is still stopping; try node stop again.")
        else WebShellResult(output = "Node session stopped. Its servers and timers are closed.")
    }
}
