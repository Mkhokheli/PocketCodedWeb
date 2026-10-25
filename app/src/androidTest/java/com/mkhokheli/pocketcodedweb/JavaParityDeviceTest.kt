package com.mkhokheli.pocketcodedweb

import android.database.sqlite.SQLiteDatabase
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class JavaParityDeviceTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun awaitUi(condition: () -> Boolean) {
        try { compose.waitUntil(10_000, condition) }
        catch (error: Throwable) {
            throw AssertionError(compose.onRoot(useUnmergedTree = true).printToString(maxDepth = 20), error)
        }
    }

    private fun project(): File {
        NativeNodeRuntime.initialize(context)
        return File(context.filesDir, "java-parity-${System.nanoTime()}").apply {
            mkdirs()
            File(this, "index.html").writeText("<h1>Original heading</h1>")
            File(this, "script.js").writeText("const heading = 'Original';")
        }
    }

    private fun show(root: File) {
        compose.setContent { MaterialTheme { WebIdeApp(WebTheme.VSCodeDefaultDark, {}, root, {}) } }
        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Fullscreen explorer").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    }

    @Test fun explorerSearchReplacesWithHistoryAndNavigatesToResult() {
        val root = project()
        try {
            show(root)
            compose.onNodeWithContentDescription("Fullscreen explorer").performScrollTo().performClick()
            compose.onNodeWithText("Search & Replace").performClick()
            compose.onAllNodes(hasSetTextAction())[0].performTextInput("Original")
            compose.onAllNodes(hasSetTextAction())[1].performTextInput("Updated")
            Espresso.closeSoftKeyboard()
            awaitUi { compose.onAllNodesWithText("2 matches found").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Replace all").performClick()
            compose.onAllNodesWithText("Replace all").onLast().performClick()
            compose.waitUntil(10_000) { File(root, "index.html").readText().contains("Updated") && File(root, "script.js").readText().contains("Updated") }
            assertTrue(ProjectFileStore(root).history(File(root, "index.html").path).isNotEmpty())
            compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("Updated")
            Espresso.closeSoftKeyboard()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("index.html:1").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("index.html:1").performClick()
            compose.onNodeWithContentDescription("Minimize editor").assertExists()
            compose.onNode(hasSetTextAction()).assertTextContains("Updated", substring = true)
        } finally { NodeSessionRuntime.stop(); root.deleteRecursively() }
    }

    @Test fun sqliteViewerOpensTablesAndExecutesDataChanges() {
        val root = project()
        val database = File(root, "sample.sqlite")
        SQLiteDatabase.openOrCreateDatabase(database, null).use {
            it.execSQL("CREATE TABLE notes (title TEXT)")
            it.execSQL("INSERT INTO notes VALUES ('Phone database row')")
        }
        assertEquals(listOf(listOf("Phone database row")), querySqlite(database, "SELECT * FROM notes;").rows)
        try {
            show(root)
            compose.onNodeWithContentDescription("Fullscreen explorer").performScrollTo().performClick()
            compose.onNodeWithText("sample.sqlite").performClick()
            awaitUi { compose.onAllNodesWithText("Phone database row").fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(hasSetTextAction()).performTextReplacement("UPDATE notes SET title = 'Updated row'")
            Espresso.closeSoftKeyboard()
            compose.onNodeWithText("Execute").performClick()
            compose.waitUntil(10_000) {
                SQLiteDatabase.openDatabase(database.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                    db.rawQuery("SELECT title FROM notes", null).use { it.moveToFirst() && it.getString(0) == "Updated row" }
                }
            }
            compose.onNodeWithText("notes").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Updated row").fetchSemanticsNodes().isNotEmpty() }
        } finally { NodeSessionRuntime.stop(); root.deleteRecursively() }
    }

    @Test fun profilerRunsOnEmbeddedNodeAndExportsCpuSamples() {
        val root = project()
        try {
            val file = File(root, "profile.cjs").apply {
                writeText("function phoneHotFunction() { const end = Date.now() + 250; let n = 0; while (Date.now() < end) n += Math.sqrt(n + 1); return n; } console.log(phoneHotFunction());")
            }
            show(root)
            compose.onNodeWithContentDescription("Fullscreen explorer").performScrollTo().performClick()
            compose.onNodeWithText("profile.cjs").performClick()
            compose.onNodeWithContentDescription("Editor tools").performClick()
            compose.onNodeWithText("Profile current file").performScrollTo().performClick()
            compose.waitUntil(20_000) { NodeSessionRuntime.developmentState(root, "profile").optString("state") == "complete" }
            val report = NodeSessionRuntime.developmentState(root, "profile")
            assertTrue(report.toString(), report.optInt("samples") > 0)
            assertTrue(File(report.getString("reportPath")).isFile)
            assertTrue(report.getJSONArray("functions").toString().contains("phoneHotFunction"))
            compose.onNodeWithText("JavaScript profiler").assertExists()
        } finally { NodeSessionRuntime.stop(); root.deleteRecursively() }
    }

    @Test fun aiSettingsRequestApprovalUndoCopyAndClearWorkTogether() {
        val root = project()
        val store = AiConnectionStore(context)
        val oldKey = store.key("Custom / Ollama")
        val oldEndpoint = store.endpoint("local-model")
        val oldModel = store.model("local-model")
        val server = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        val response = JSONObject().put("choices", JSONArray().put(JSONObject().put("message", JSONObject().put("content",
            "<file path=\"index.html\"><h1>AI updated heading</h1></file>\nUpdated the heading.")))).toString().toByteArray()
        val worker = thread(isDaemon = true) {
            runCatching {
                server.accept().use { socket ->
                    socket.soTimeout = 20_000
                    val input = socket.getInputStream()
                    val header = StringBuilder()
                    while (!header.endsWith("\r\n\r\n")) {
                        val byte = input.read(); check(byte >= 0); header.append(byte.toChar())
                    }
                    val length = Regex("(?im)^Content-Length: (\\d+)").find(header)?.groupValues?.get(1)?.toInt() ?: 0
                    repeat(length) { check(input.read() >= 0) }
                    socket.getOutputStream().apply {
                        write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${response.size}\r\nConnection: close\r\n\r\n".toByteArray())
                        write(response); flush()
                    }
                }
            }
        }
        try {
            store.save("Custom / Ollama", "local-model", null, null, null)
            show(root)
            compose.onNodeWithContentDescription("Open AI chat").performScrollTo().performClick()
            compose.onNodeWithContentDescription("AI settings").performClick()
            compose.onNodeWithText("Custom / Ollama").performScrollTo().performClick()
            compose.onNodeWithText("Configure AI connection").performClick()
            compose.onNodeWithText("API endpoint URL").performTextReplacement("http://127.0.0.1:${server.localPort}/v1/chat/completions")
            compose.onNodeWithText("Model / deployment name").performTextReplacement("phone-test-model")
            Espresso.closeSoftKeyboard()
            compose.onNodeWithText("Save settings").performClick()
            compose.onNode(hasSetTextAction()).performTextInput("Update the heading")
            compose.onNode(hasSetTextAction()).performImeAction()
            Espresso.closeSoftKeyboard()
            compose.waitUntil(20_000) { compose.onAllNodesWithText("Apply").fetchSemanticsNodes().isNotEmpty() }
            assertEquals("<h1>Original heading</h1>", File(root, "index.html").readText())
            compose.onNodeWithText("Apply").performClick()
            compose.waitUntil(10_000) { File(root, "index.html").readText() == "<h1>AI updated heading</h1>" }
            compose.onNodeWithText("Undo AI changes").performClick()
            compose.waitUntil(10_000) { File(root, "index.html").readText() == "<h1>Original heading</h1>" }
            compose.onAllNodesWithContentDescription("Copy response").onLast().performClick()
            compose.onNodeWithContentDescription("AI settings").performClick()
            compose.onNodeWithText("Clear chat").performClick()
            compose.onAllNodesWithText("Clear chat").onLast().performClick()
            compose.onAllNodesWithContentDescription("Copy response").assertCountEquals(0)
            compose.onNodeWithText("Explain this project").assertExists()
        } finally {
            server.close(); worker.join(1_000)
            store.save("Custom / Ollama", "local-model", oldKey, oldEndpoint, oldModel)
            NodeSessionRuntime.stop(); root.deleteRecursively()
        }
    }
}
