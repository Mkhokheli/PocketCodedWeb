package com.mkhokheli.pocketcodedweb

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Real terminal -> Node service -> idle/delayed logs -> visible Compose output. */
class LiveNodeTerminalDeviceTest {
    @get:Rule(order = 0) val foreground = object : org.junit.rules.ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val pkg = instrumentation.targetContext.packageName
            android.os.ParcelFileDescriptor.AutoCloseInputStream(
                instrumentation.uiAutomation.executeShellCommand("am start -W -n $pkg/androidx.activity.ComponentActivity")
            ).use { it.readBytes() }
        }
    }
    @get:Rule(order = 1) val compose = createComposeRule()

    @Test fun nodeAndNpmApisStreamIdleAndRequestLogsWithoutNodeLogsCommand() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NativeNodeRuntime.initialize(context)
        assertFalse("A user Node session is already running", NodeSessionRuntime.isRunning())
        val root = File(context.filesDir, "live-terminal-test-${System.nanoTime()}").apply { mkdirs() }
        val visible = mutableStateOf(true)
        val prefs = context.getSharedPreferences("web_ide", 0)
        val keys = setOf("active_file_id", "active_file_path", "working_directory_path", "terminal_directory_path",
            "expanded_folder_paths", "explorer_visible", "file_search", "full_screen_mode")
        val original = prefs.all.filterKeys { it in keys }
        try {
            prefs.edit().apply { keys.forEach { remove(it) }; putBoolean("explorer_visible", false) }.commit()
            File(root, "package.json").writeText("""{"scripts":{"dev":"node server.cjs"}}""")
            File(root, "server.cjs").writeText("""
                const http = require('http');
                const items = [{id: 1, name: 'Item One'}];
                const run = process.argv[2];
                console.log('BOOT_ONCE_' + run);
                http.createServer((req, res) => {
                    console.log('REQUEST_LOG_' + run, req.method, req.url);
                    let body = '';
                    req.on('data', chunk => body += chunk);
                    req.on('end', () => {
                        res.setHeader('Content-Type', 'application/json');
                        if (req.url === '/api/items' && req.method === 'POST') {
                            const data = JSON.parse(body || '{}');
                            if (!data.name) { res.statusCode = 400; return res.end(JSON.stringify({error: 'Missing name'})); }
                            const item = {id: items.length + 1, name: data.name};
                            items.push(item); res.statusCode = 201; return res.end(JSON.stringify(item));
                        }
                        res.end(JSON.stringify({items}));
                    });
                }).listen(0, '127.0.0.1');
                setTimeout(() => {
                    console.error('DELAYED_ERROR_MARKER_' + run);
                    process.stdout.write('IDLE_' + run + '_PART_');
                    setTimeout(() => process.stdout.write('COMPLETE\n'), 700);
                }, 1500);
            """.trimIndent())
            compose.setContent { MaterialTheme {
                if (visible.value) WebIdeApp(WebTheme.VSCodeDefaultDark, {}, root, {})
            } }
            compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Fullscreen terminal").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithContentDescription("Fullscreen terminal").performScrollTo().performClick()
            fun command(text: String) {
                compose.onNode(hasSetTextAction()).performTextReplacement(text)
                compose.onNode(hasSetTextAction()).performImeAction()
            }
            fun waitText(text: String) {
                try {
                    compose.waitUntil(15_000) { compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
                } catch (error: Throwable) {
                    throw AssertionError("Missing $text; session=${NodeSessionRuntime.status()}; UI=" + compose.onRoot().printToString(), error)
                }
            }
            repeat(2) { iteration ->
                val run = if (iteration == 0) "direct" else "npm"
                command(if (iteration == 0) "node server.cjs direct" else "npm run dev -- npm")
                // No HTTP request or node logs command may release these messages.
                waitText("IDLE_${run}_PART_COMPLETE")
                waitText("DELAYED_ERROR_MARKER_$run")
                val url = NodeSessionRuntime.awaitPreviewUrl() ?: error("No API URL")
                repeat(20) {
                    val connection = URL(url + "api/items").openConnection() as HttpURLConnection
                    connection.connectTimeout = 3000; connection.readTimeout = 3000
                    try {
                        assertEquals(200, connection.responseCode)
                        assertTrue(connection.inputStream.bufferedReader().use { it.readText() }.contains("Item One"))
                    } finally { connection.disconnect() }
                }
                waitText("REQUEST_LOG_$run GET /api/items")
                compose.onNodeWithContentDescription("Minimize terminal").performClick()
                compose.onNodeWithContentDescription("Fullscreen terminal").performScrollTo().performClick()
                waitText("IDLE_${run}_PART_COMPLETE")
                val text = compose.onAllNodes(hasText("BOOT_ONCE_$run", substring = true)).fetchSemanticsNodes()
                    .flatMap { it.config.getOrElse(androidx.compose.ui.semantics.SemanticsProperties.Text) { emptyList() } }
                    .joinToString("\n") { it.text }
                assertEquals(1, Regex("BOOT_ONCE_$run").findAll(text).count())
                command("node stop")
                compose.waitUntil(15_000) { !NodeSessionRuntime.isRunning() }
                waitText("Node session stopped")
                assertFalse(NodeSessionRuntime.isRunning())
            }
        } finally {
            if (NodeSessionRuntime.isRunningFor(root)) NodeSessionRuntime.stop()
            compose.runOnIdle { visible.value = false }
            compose.waitForIdle()
            prefs.edit().apply {
                keys.forEach { remove(it) }
                original.forEach { (key, value) -> when (value) {
                    is String -> putString(key, value); is Boolean -> putBoolean(key, value); is Long -> putLong(key, value)
                } }
            }.commit()
            check(root.canonicalFile.parentFile == context.filesDir.canonicalFile)
            root.deleteRecursively()
        }
    }
}
