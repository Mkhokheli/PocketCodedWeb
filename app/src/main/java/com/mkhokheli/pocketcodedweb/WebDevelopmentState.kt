package com.mkhokheli.pocketcodedweb

import android.content.Context
import androidx.compose.runtime.*
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

internal data class WebSourceJump(val file: String, val line: Int, val version: Long = System.nanoTime())
internal data class WebTestResult(val id: String, val name: String, val status: String, val file: String, val line: Int, val details: String)

internal fun parseWebTestResults(json: JSONObject): List<WebTestResult> {
    val results = json.optJSONArray("results") ?: return emptyList()
    return (0 until minOf(results.length(), 1000)).mapNotNull { index ->
        val result = results.optJSONObject(index) ?: return@mapNotNull null
        WebTestResult(result.optString("id"), result.optString("name"), result.optString("status"), result.optString("file"), result.optInt("line", 1).coerceAtLeast(1), result.optString("details"))
    }
}

internal fun discoverWebTestFiles(root: File): List<File> {
    val base = root.canonicalFile
    val excluded = setOf("node_modules", ".git", ".pocketcodedweb", "dist", "build", "coverage", ".next")
    return base.walkTopDown().onEnter { folder ->
        folder == base || (folder.name !in excluded && folder.canonicalFile == folder.absoluteFile)
    }.filter { file ->
        file.isFile && file.extension.lowercase() in setOf("js", "cjs", "mjs") &&
            (Regex(".*[.](test|spec)[.](js|cjs|mjs)$", RegexOption.IGNORE_CASE).matches(file.name) ||
                file.relativeTo(base).parentFile?.path?.split(File.separatorChar)?.any { it in setOf("test", "tests", "__tests__") } == true)
    }.take(2000).toList().sortedBy { it.path }
}

internal class WebDevelopmentController(context: Context, val root: File) {
    private val preferences = context.getSharedPreferences("web_debug_breakpoints", Context.MODE_PRIVATE)
    private val key = root.canonicalPath
    var panel by mutableStateOf<String?>(null)
    var sourceJump by mutableStateOf<WebSourceJump?>(null)
    var debugState by mutableStateOf(JSONObject().put("state", "idle"))
    var testState by mutableStateOf(JSONObject().put("state", "idle"))
    var profileState by mutableStateOf(JSONObject().put("state", "idle"))
    var error by mutableStateOf<String?>(null)
    var starting by mutableStateOf(false)
    var breakpoints by mutableStateOf(loadBreakpoints())
        private set

    private fun loadBreakpoints(): Map<String, Set<Int>> = runCatching {
        val json = JSONObject(preferences.getString(key, "{}").orEmpty())
        json.keys().asSequence().associateWith { path ->
            val lines = json.getJSONArray(path)
            (0 until lines.length()).map { lines.getInt(it) }.filter { it > 0 }.toSet()
        }.filterKeys { File(it).canonicalPath.startsWith(key + File.separator) }
    }.getOrDefault(emptyMap())

    fun toggleBreakpoint(path: String, line: Int) {
        require(line > 0 && File(path).canonicalPath.startsWith(key + File.separator))
        val existing = breakpoints[path].orEmpty()
        val updated = if (line in existing) existing - line else existing + line
        breakpoints = (breakpoints + (path to updated)).filterValues { it.isNotEmpty() }
        val json = JSONObject()
        breakpoints.forEach { (file, lines) -> json.put(file, JSONArray(lines.sorted())) }
        preferences.edit().putString(key, json.toString()).apply()
    }
    fun relocateBreakpoints(path: String, before: String, after: String) {
        val previous = breakpoints[path].orEmpty()
        val relocated = relocateWebBreakpoints(before, after, previous)
        if (previous == relocated) return
        breakpoints = (breakpoints + (path to relocated)).filterValues { it.isNotEmpty() }
        val json = JSONObject()
        breakpoints.forEach { (file, lines) -> json.put(file, JSONArray(lines.sorted())) }
        preferences.edit().putString(key, json.toString()).apply()
    }
    fun breakpointList(): List<Pair<String, Int>> = breakpoints.flatMap { (path, lines) -> lines.sorted().map { path to it } }
}

/** Keep markers attached to existing lines when a multiline edit inserts/removes earlier lines. */
internal fun relocateWebBreakpoints(before: String, after: String, points: Set<Int>): Set<Int> {
    if (points.isEmpty() || before == after) return points
    if (after.isBlank()) return emptySet()
    var start = 0
    while (start < minOf(before.length, after.length) && before[start] == after[start]) start++
    var oldEnd = before.length
    var newEnd = after.length
    while (oldEnd > start && newEnd > start && before[oldEnd - 1] == after[newEnd - 1]) { oldEnd--; newEnd-- }
    val removed = before.substring(start, oldEnd).count { it == '\n' }
    val inserted = after.substring(start, newEnd).count { it == '\n' }
    if (removed == 0 && inserted == 0) return points
    val first = before.take(start).count { it == '\n' } + 1
    val last = first + removed
    val lineStartInsertion = oldEnd == start && (start == 0 || before[start - 1] == '\n')
    val max = after.count { it == '\n' } + 1
    return points.map { line ->
        when {
            line < first -> line
            lineStartInsertion || line > last -> line + inserted - removed
            else -> first
        }.coerceIn(1, max)
    }.toSet()
}

