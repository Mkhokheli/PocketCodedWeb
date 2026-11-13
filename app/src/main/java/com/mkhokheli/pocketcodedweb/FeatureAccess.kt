package com.mkhokheli.pocketcodedweb

import androidx.compose.runtime.staticCompositionLocalOf
import java.io.File

internal enum class PremiumFeature(val title: String, val benefit: String) {
    Projects("Projects and files", "Create multiple files, folders and complete Web projects."),
    ImportExport("Import and export", "Move files and projects between your device and workspace."),
    Tools("Development tools", "Debug code, test APIs, run tests and inspect project history."),
    Ai("AI assistant", "Use your configured AI provider to explain and improve your code. Provider costs may apply."),
    Shell("Interactive terminal", "Use shell commands, Git, SSH, npm packages and development servers."),
    ExpandedPanels("Expanded workspace", "Expand the terminal and explorer for more room."),
}

internal data class FeatureAccess(
    val fullAccess: Boolean = false,
    val requestUpgrade: (PremiumFeature) -> Unit = {},
) {
    fun require(feature: PremiumFeature): Boolean {
        if (fullAccess) return true
        requestUpgrade(feature)
        return false
    }
}

internal val LocalFeatureAccess = staticCompositionLocalOf { FeatureAccess() }

internal fun hasFullFeatureAccess(premium: Boolean, now: Long, trialStart: Long?, locked: Boolean): Boolean =
    premium || (!locked && trialStart != null && trialStart > 0L && now >= trialStart && now < trialEndTimestamp(trialStart))

/** Kept outside the projects folder so discovery/renaming cannot overwrite free work. */
internal fun freeWorkspace(filesDir: File): File = File(filesDir, "free_scratchpad").apply {
    mkdirs()
    val html = File(this, "index.html")
    if (!html.exists()) html.writeText("""
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Scratchpad</title>
          <link rel="stylesheet" href="style.css">
          <script src="script.js" defer></script>
        </head>
        <body>
          <h1>Hello, Web!</h1>
          <p>Edit index.html, style.css and script.js, then run your preview.</p>
          <button id="scratchpad-button">Try JavaScript</button>
        </body>
        </html>
    """.trimIndent() + "\n")
    val css = File(this, "style.css")
    if (!css.exists()) css.writeText("""
        body {
          font-family: sans-serif;
          padding: 24px;
        }

        button {
          padding: 12px;
          cursor: pointer;
        }
    """.trimIndent() + "\n")
    val js = File(this, "script.js")
    if (!js.exists()) js.writeText("""
        const button = document.getElementById('scratchpad-button');
        if (button) {
          button.addEventListener('click', () => {
            button.textContent = 'It works!';
          });
        }
        console.log('Scratchpad ready');
    """.trimIndent() + "\n")

    // Upgrade older scratchpads by adding missing asset references, preserving their code.
    val source = html.readText()
    val references = buildList {
        if (!Regex("""(?i)\bhref\s*=\s*["'](?:\./)?style\.css(?:[?#][^"']*)?["']""").containsMatchIn(source)) {
            add("<link rel=\"stylesheet\" href=\"style.css\">")
        }
        if (!Regex("""(?i)\bsrc\s*=\s*["'](?:\./)?script\.js(?:[?#][^"']*)?["']""").containsMatchIn(source)) {
            add("<script src=\"script.js\" defer></script>")
        }
    }
    if (references.isNotEmpty()) {
        val links = references.joinToString("\n") + "\n"
        val headEnd = Regex("""(?i)</head\s*>""").find(source)
        val updated = if (headEnd == null) links + source
            else source.substring(0, headEnd.range.first) + links + source.substring(headEnd.range.first)
        html.writeText(updated)
    }
}

internal fun isEditableScratchpadFile(filesDir: File, root: File, file: File): Boolean {
    val scratchpad = File(filesDir, "free_scratchpad").canonicalFile
    val target = file.canonicalFile
    return root.canonicalFile == scratchpad && target.parentFile == scratchpad &&
        target.name in setOf("index.html", "style.css", "script.js")
}

internal fun canEditWorkspace(fullAccess: Boolean, root: File): Boolean =
    fullAccess || (root.name == "free_scratchpad" && root.parentFile?.name != "projects")

internal fun canOpenProject(fullAccess: Boolean, scratchpadAvailable: Boolean, root: File, filesDir: File): Boolean {
    val isScratchpad = root.canonicalFile == File(filesDir, "free_scratchpad").canonicalFile
    return if (isScratchpad) scratchpadAvailable else fullAccess
}
