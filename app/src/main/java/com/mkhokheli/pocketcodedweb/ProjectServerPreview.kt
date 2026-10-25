package com.mkhokheli.pocketcodedweb

import java.io.File
import org.json.JSONObject

/** Null selects the built-in frontend preview; errors never silently open stale static content. */
internal fun prepareProjectServerPreview(
    root: File,
    working: File,
    browserPreview: Boolean = false,
): WebShellResult? {
    var started = WebShellResult()
    if (!NodeSessionRuntime.isRunningFor(root)) {
        val project = root.canonicalFile
        val packageRoot = generateSequence(working.canonicalFile) { current ->
            current.parentFile?.takeIf { it == project || it.path.startsWith(project.path + File.separator) }
        }.firstOrNull { File(it, "package.json").isFile } ?: return null
        val json = runCatching { JSONObject(File(packageRoot, "package.json").readText()) }
            .getOrElse { return WebShellResult(error = "Cannot read package.json: ${it.message}") }
        val scripts = json.optJSONObject("scripts") ?: return null
        val script = listOf("dev", "start").firstOrNull { scripts.optString(it).isNotBlank() } ?: return null
        started = executeWebShellCommand("npm run $script", root, packageRoot, browserPreview = browserPreview)
        if (started.error.isNotBlank()) return started
        if (started.openPreview) return null
    }
    val url = NodeSessionRuntime.awaitPreviewUrl()
    val status = NodeSessionRuntime.status()
    return if (url != null) WebShellResult(output = status.output, previewLink = url)
    else WebShellResult(
        output = listOf(started.output, status.output).filter { it.isNotBlank() }.joinToString("\n"),
        error = "The Node session has not opened an HTTP server. Check node logs, then retry Preview when it is ready.",
    )
}
