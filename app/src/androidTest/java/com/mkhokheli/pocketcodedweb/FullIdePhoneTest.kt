package com.mkhokheli.pocketcodedweb

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Exercises the real IDE root and callbacks in the signed test host, without using an online account. */
class FullIdePhoneTest {
    @get:Rule val compose = createComposeRule()
    private fun fixture(): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NativeNodeRuntime.initialize(context)
        return File(context.filesDir, "full-ide-phone-${System.nanoTime()}").apply {
            mkdirs()
            File(this, "index.html").writeText("<!doctype html><h1>Original phone document</h1>")
            File(this, "package.json").writeText("""{"name":"phone-test","scripts":{"test":"node math.test.cjs"}}""")
            File(this, "math.test.cjs").writeText("require('node:test')('phone case', () => require('node:assert/strict').equal(1, 1));")
        }
    }
    private fun show(root: File) {
        compose.setContent { MaterialTheme { WebIdeApp(WebTheme.VSCodeDefaultDark, {}, root, {}) } }
        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Fullscreen editor").fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun realEditorAutosavesUndoRedoAndRestoresHistory() {
        val root = fixture()
        try {
            show(root)
            compose.onNodeWithContentDescription("Fullscreen editor").performScrollTo().performClick()
            val edited = "<!doctype html><h1>Edited on the connected phone</h1>"
            compose.onNode(hasSetTextAction()).performTextReplacement(edited)
            compose.waitUntil(10_000) { File(root, "index.html").readText() == edited }
            compose.onNodeWithContentDescription("Undo").performClick()
            compose.waitUntil(10_000) { File(root, "index.html").readText().contains("Original phone document") }
            compose.onNodeWithContentDescription("Redo").performClick()
            compose.waitUntil(10_000) { File(root, "index.html").readText() == edited }
            compose.onNodeWithContentDescription("Tools").performClick()
            compose.onNodeWithText("Local history").performClick()
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("Restore ", substring = true)).fetchSemanticsNodes().isNotEmpty() }
            compose.onAllNodes(hasText("Restore ", substring = true)).onFirst().performClick()
            compose.waitUntil(10_000) { File(root, "index.html").readText().contains("Original phone document") }
        } finally { NodeSessionRuntime.stop(); root.deleteRecursively() }
    }
    @Test fun realTerminalRunsClearsCopiesAndRecallsCommands() {
        val root = fixture()
        val command = "node -e \"console.log('PHONE_TERMINAL_OK')\""
        try {
            show(root)
            compose.onNodeWithContentDescription("Fullscreen terminal").performScrollTo().performClick()
            compose.onNode(hasSetTextAction()).performTextInput(command)
            compose.onNode(hasSetTextAction()).performImeAction()
            compose.waitUntil(15_000) { compose.onAllNodesWithText("\nPHONE_TERMINAL_OK", substring = true).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("\u22ee").performClick()
            compose.onNodeWithText("Copy output").performClick()
            compose.runOnIdle {
                val clipboard = InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(android.content.ClipboardManager::class.java)
                assertTrue(clipboard.primaryClip?.getItemAt(0)?.text.toString().contains("PHONE_TERMINAL_OK"))
            }
            compose.onNodeWithText("\u22ee").performClick()
            compose.onNodeWithText("Clear output").performClick()
            compose.onAllNodesWithText("PHONE_TERMINAL_OK", substring = true).assertCountEquals(0)
            compose.onNodeWithText("\u22ee").performClick()
            compose.onNodeWithText(command).performClick()
            compose.onNode(hasSetTextAction()).assertTextEquals(command)
            compose.onNode(hasSetTextAction()).performImeAction()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("\nPHONE_TERMINAL_OK", substring = true).fetchSemanticsNodes().isNotEmpty() }
        } finally { NodeSessionRuntime.stop(); root.deleteRecursively() }
    }
}
