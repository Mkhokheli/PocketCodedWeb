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
    private val logCursor = TerminalLogCursor()
    private var reportedState = ""
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
    fun start(root: File, working: File, file: File, args: List<String>, env: Map<String, String> = emptyMap(), mode: String = "run", breakpoints: List<Pair<String, Int>> = emptyList(), testFiles: List<File> = emptyList(), browserPreview: Boolean = false): WebShellResult = runCatching {
        check(!running()) { "A Node session is already running. Use node stop before starting another; node logs shows its output." }
        fun inside(parent: File, child: File): Boolean = child.canonicalPath == parent.canonicalPath || child.canonicalPath.startsWith(parent.canonicalPath + File.separator)
        require(inside(context.filesDir, root) && inside(root, working) && inside(root, file)) { "Node entry point is outside the project." }
        require(mode in setOf("run", "debug", "tests", "profile")) { "Unknown development mode" }
        require(testFiles.all { inside(root, it) && it.isFile }) { "Test file is outside the project or missing" }
        require(breakpoints.all { inside(root, File(it.first)) && it.second > 0 }) { "Invalid breakpoint" }
        require(mode != "tests" || testFiles.isNotEmpty()) { "Select at least one test file" }
        require(file.isFile) { "Node entry point does not exist: ${file.name}" }
        directory.mkdirs()
        logCursor.reset()
        reportedState = "starting"
        listOf("debug-state.json", "debug-command.json", "test-results.json", "profile-state.json").forEach { File(directory, it).delete() }
        File(directory, "status.json").delete()
        cachedStatus = null
        File(directory, "session.log").writeText("")
        listOf("session.cjs", "debug-worker.cjs", "test-results.cjs", "profile-session.cjs").forEach { name ->
            context.assets.open("node-runtime/$name").use { input -> File(directory, name).outputStream().use { input.copyTo(it) } }
        }
        File(directory, "config.json").writeText(JSONObject()
            .put("root", root.canonicalPath).put("mode", mode)
            .put("debugStatePath", File(directory, "debug-state.json").absolutePath)
            .put("debugCommandPath", File(directory, "debug-command.json").absolutePath)
            .put("testResultsPath", File(directory, "test-results.json").absolutePath)
            .put("profileStatePath", File(directory, "profile-state.json").absolutePath)
            .put("testFiles", JSONArray(testFiles.map { it.canonicalPath }))
            .put("breakpoints", JSONArray().apply { breakpoints.forEach { (path, line) -> put(JSONObject().put("file", File(path).canonicalPath).put("line", line)) } })
            .put("maxLogChars", DeviceEfficiency.budget.nodeLogChars)
            .put("logFlushMs", DeviceEfficiency.budget.logFlushMs)
            .put("browserPreview", browserPreview)
            .put("cwd", working.canonicalPath).put("file", file.canonicalPath)
            .put("args", JSONArray(args)).put("env", JSONObject(env))
            .put("statusPath", File(directory, "status.json").absolutePath)
            .put("logPath", File(directory, "session.log").absolutePath).toString())
        val intent = Intent(context, NodeSessionService::class.java)
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
        val deadline = SystemClock.elapsedRealtime() + 5_000
        while (SystemClock.elapsedRealtime() < deadline) {
            val status = readStatus()
            if (mode == "debug" && File(directory, "debug-state.json").isFile) break
            if ((status.optJSONArray("urls")?.length() ?: 0) > 0 || status.optString("state") in setOf("exited", "failed")) break
            Thread.sleep(100)
        }
        if (!running() && readStatus().optString("state") == "starting") {
            WebShellResult(error = "The Node session did not start. Retry while the app is visible.")
        } else status()
    }.getOrElse { WebShellResult(error = it.message ?: "Could not start Node session") }

    fun developmentState(root: File, mode: String): JSONObject {
        if (!::context.isInitialized) return JSONObject().put("state", "idle")
        val configuration = runCatching { JSONObject(File(directory, "config.json").readText()) }.getOrNull()
        if (configuration?.optString("root") != root.canonicalPath || configuration.optString("mode") != mode) return JSONObject().put("state", "idle")
        val filename = when (mode) { "debug" -> "debug-state.json"; "profile" -> "profile-state.json"; else -> "test-results.json" }
        val result = runCatching { JSONObject(File(directory, filename).readText()) }.getOrElse { JSONObject().put("state", "starting") }
        val status = readStatus()
        if (!running() || status.optString("state") in setOf("failed", "exited")) {
            if (mode == "debug" || result.optString("state") in setOf("starting", "running")) {
                result.put("state", status.optString("state").takeIf { it in setOf("failed", "exited") } ?: "stopped")
            }
        }
        return result
    }

    @Synchronized
    fun debugCommand(root: File, command: String, frame: Int = 0, breakpoints: List<Pair<String, Int>> = emptyList()): Result<Unit> = runCatching {
        require(command in setOf("continue", "into", "over", "out", "pause", "inspect", "breakpoints"))
        val configuration = JSONObject(File(directory, "config.json").readText())
        check(running() && configuration.optString("root") == root.canonicalPath && configuration.optString("mode") == "debug") { "No debugger is running for this project" }
        val previous = runCatching { JSONObject(File(directory, "debug-command.json").readText()).optLong("sequence") }.getOrDefault(0L)
        val deadline = SystemClock.elapsedRealtime() + 2_000
        while (previous > 0 && runCatching { JSONObject(File(directory, "debug-state.json").readText()).optLong("commandSequence") }.getOrDefault(0L) < previous) {
            check(running() && SystemClock.elapsedRealtime() < deadline) { "The debugger has not acknowledged the last command. Try again or stop the session." }
            Thread.sleep(25)
        }
        val data = JSONObject().put("sequence", System.nanoTime()).put("command", command).put("frame", frame)
            .put("breakpoints", JSONArray().apply { breakpoints.forEach { (path, line) ->
                require(File(path).canonicalPath.startsWith(root.canonicalPath + File.separator) && line > 0)
                put(JSONObject().put("file", File(path).canonicalPath).put("line", line))
            } })
        durableAtomicWrite(File(directory, "debug-command.json"), data.toString().toByteArray(Charsets.UTF_8))
    }

    /** Called by the visible project's terminal, including once after a session exits. */
    @Synchronized
    fun drainOutput(root: File): String {
        if (!::context.isInitialized) return ""
        val config = runCatching { JSONObject(File(directory, "config.json").readText()) }.getOrNull()
        if (config?.optString("root") != root.canonicalPath) return ""
        val log = runCatching { File(directory, "session.log").readText() }.getOrNull() ?: return ""
        val addition = logCursor.consume(log)
        val status = readStatus().optString("state")
        val state = if (running()) status else status.takeIf { it in setOf("failed", "exited") } ?: "stopped"
        val ended = state in setOf("failed", "exited", "stopped") && state != reportedState
        reportedState = state
        return addition + if (ended) "\nNode session: $state\n" else ""
    }

    @Synchronized
    fun status(): WebShellResult {
        val status = readStatus()
        val state = if (running()) status.optString("state") else status.optString("state").takeIf { it in setOf("failed", "exited") } ?: "stopped"
        val log = runCatching { File(directory, "session.log").readText() }.getOrDefault("")
        // The command result displays this snapshot; live polling starts after it.
        logCursor.consume(log)
        reportedState = state
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
