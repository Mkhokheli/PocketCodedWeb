package com.mkhokheli.pocketcodedweb

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.platform.app.InstrumentationRegistry
import com.mkhokheli.pocketcodedweb.ui.theme.PocketCodedWebTheme
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PyEditorDetailsDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val palette = webIdePalette(WebTheme.VSCodeDefaultDark)
    private fun root() = File(context.filesDir, "py-editor-details-${UUID.randomUUID()}").apply { mkdirs() }
    private fun actions(root: File, explain: (String, CodeDiagnostic) -> Unit = { _, _ -> },
        edited: (File, String) -> Unit = { _, _ -> }) = WebDevelopmentActions(
        ProjectFileStore(root), WebDevelopmentController(context, root), { _, _ -> }, { _, _, _ -> },
        { _, done -> done(null) }, { _, done -> done(null) }, {}, emptyList(), {}, { emptyList() }, {}, {},
        explainDiagnostic = explain, canExplainDiagnostic = true, fileEdited = edited,
    )
    private fun screenshot(name: String, bitmap: Bitmap = compose.onAllNodes(isRoot()).onLast().captureToImage().asAndroidBitmap()) {
        val output = File(context.getExternalFilesDir(null), "py-ui-stress").apply { mkdirs() }
        File(output, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test fun inlineQuickFixIsUndoableAndSeverityFilteringMatchesPython() {
        val root = root()
        val file = File(root, "main.js")
        val original = "const title = docuemnt.title;"
        var code by mutableStateOf(original)
        file.writeText(original)
        try {
            compose.setContent { PocketCodedWebTheme(true, WebTheme.VSCodeDefaultDark.name, false) {
                SmartCodeEditor(WebFile(1, file.name, code, file.path, 0), palette,
                    Modifier.fillMaxSize(), showKeyboardAccessory = true, onCodeChange = { code = it })
            } }
            compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Quick fix: Replace with document").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithContentDescription("Quick fix: Replace with document").performClick()
            compose.runOnIdle { assertEquals("const title = document.title;", code) }
            compose.onNode(hasSetTextAction()).performClick().performKeyInput {
                keyDown(Key.CtrlLeft); keyDown(Key.Z); keyUp(Key.Z); keyUp(Key.CtrlLeft)
            }
            compose.runOnIdle { assertEquals(original, code) }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("All").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("All").performClick()
            compose.onNodeWithText("Warnings").performClick()
            compose.onNodeWithText("Errors").assertExists()
            compose.onNodeWithText("No diagnostics at this severity").assertExists()
        } finally { root.deleteRecursively() }
    }

    @Test fun projectDiagnosticExplainButtonPassesTheCorrectFileAndMessageWithoutSendingARequest() {
        val root = root()
        val file = File(root, "main.js").apply { writeText("docuemnt.title;") }
        val explained = AtomicReference<Pair<String, CodeDiagnostic>>()
        var dismissed by mutableStateOf(false)
        val actions = actions(root, explain = { path, diagnostic -> explained.set(path to diagnostic) })
        try {
            compose.setContent { MaterialTheme {
                if (!dismissed) WebProjectDiagnosticsDialog(WebFile(1, file.name, file.readText(), file.path, 0),
                    file.readText(), palette, actions, onDismiss = { dismissed = true }, onRestoreActive = {})
            } }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Explain with AI").fetchSemanticsNodes().isNotEmpty() }
            screenshot("diagnostic-quick-fix-and-ai")
            compose.onNodeWithText("Explain with AI").performClick()
            assertEquals(file.canonicalPath, File(explained.get().first).canonicalPath)
            assertEquals("Did you mean 'document'?", explained.get().second.message)
            assertEquals("docuemnt.title;", file.readText())
        } finally { root.deleteRecursively() }
    }

    @Test fun projectQuickFixPreservesAnInactiveFilesPreviousContentsInHistory() {
        val root = root()
        val active = File(root, "main.js").apply { writeText("console.log('ok');") }
        val html = File(root, "index.html").apply { writeText("<img src=\"photo.png\">") }
        val original = html.readText()
        val edited = AtomicReference<String>()
        val actions = actions(root, edited = { _, content -> edited.set(content) })
        var dismissed by mutableStateOf(false)
        try {
            val scan = runBlocking { scanWebProjectDiagnostics(actions.fileStore.projectDirectory, active, active.readText()) }
            assertTrue("Inactive file must be scanned: $scan; root=${actions.fileStore.projectDirectory}; " +
                "children=${actions.fileStore.projectDirectory.listFiles()?.map { "${it.path} -> ${it.canonicalPath}" }}",
                scan.entries.any { it.relativePath == "index.html" })
            compose.setContent { MaterialTheme {
                if (!dismissed) WebProjectDiagnosticsDialog(WebFile(1, active.name, active.readText(), active.path, 0),
                    active.readText(), palette, actions, onDismiss = { dismissed = true }, onRestoreActive = {})
            } }
            try {
                compose.waitUntil(10_000) { compose.onAllNodes(hasScrollToIndexAction()).fetchSemanticsNodes().isNotEmpty() }
            } catch (failure: Throwable) {
                android.util.Log.e("PocketCodedPyDetails", compose.onAllNodes(isRoot()).onLast().printToString())
                throw failure
            }
            compose.onAllNodes(hasScrollToIndexAction()).onLast().performScrollToNode(hasText("Quick fix: Add HTML doctype"))
            compose.onNodeWithText("Quick fix: Add HTML doctype").performClick()
            compose.waitUntil(10_000) { edited.get() != null }
            assertEquals("<!DOCTYPE html>\n$original", html.readText())
            val previous = actions.fileStore.history(html.path).first()
            assertEquals(original, actions.fileStore.readRevision(html.path, previous.id).getOrThrow())
        } finally { root.deleteRecursively() }
    }

    @Test fun regularEditorDrawsFaintRailsAcrossBlankLines() {
        val source = "function greet() {\n    const ok = true;\n\n        return ok;\n}\n"
        var code by mutableStateOf(source)
        compose.setContent { PocketCodedWebTheme(true, WebTheme.VSCodeDefaultDark.name, false) {
            SmartCodeEditor(WebFile(2, "guides.js", code, "/guides.js", 0), palette, Modifier.fillMaxSize(), onCodeChange = { code = it })
        } }
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNode(hasSetTextAction()).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.last()
        val bounds = compose.onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val y = (bounds.top + (layout.getLineTop(2) + layout.getLineBottom(2)) / 2).roundToInt()
        val x = (bounds.left + layout.getBoundingBox(0).width * 2).roundToInt()
        val background = bitmap.getPixel(x + 10, y)
        assertTrue("A continuous guide must cross the blank line", (-3..3).any { offset -> bitmap.getPixel(x + offset, y) != background })
        assertEquals(source, code)
        screenshot("editor-indent-guides", bitmap)
    }

    @Test fun nativeEditorDrawsTheSameRailsWithoutCopyingTheDocument() {
        val source = "function greet() {\n    const ok = true;\n\n        return ok;\n}\n"
        val session = EditorSession(source)
        lateinit var editor: LargeDocumentEditText
        compose.setContent { MaterialTheme {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { LargeDocumentEditText(it).also { editor = it } }, update = {
                it.bindSession(session)
                it.bind(TextFieldValue(source), palette, { text -> AnnotatedString(text) }) {}
            }, onRelease = { it.dispose() })
        } }
        lateinit var bitmap: Bitmap
        compose.runOnIdle {
            bitmap = Bitmap.createBitmap(editor.width, editor.height, Bitmap.Config.ARGB_8888)
            editor.draw(Canvas(bitmap))
            val x = (editor.totalPaddingLeft + editor.paint.measureText("    ") / 2).roundToInt()
            val y = editor.totalPaddingTop + (editor.layout.getLineTop(2) + editor.layout.getLineBottom(2)) / 2
            val background = bitmap.getPixel(x + 10, y)
            assertTrue("The native rail must cross the blank line", (-2..2).any { bitmap.getPixel(x + it, y) != background })
            assertEquals(source, editor.text.toString())
        }
        screenshot("native-indent-guides", bitmap)
    }

    @Test fun explorerShowsNestedRailsAndEmptyAndPackageBadges() {
        val root = root()
        val source = File(root, "src/components/card.js").apply { parentFile?.mkdirs(); writeText("export const card = 1;") }
        File(root, "vacant").mkdirs()
        val packageFile = File(root, "packages/widget/package.json").apply { parentFile?.mkdirs(); writeText("{\"name\":\"widget\"}") }
        try {
            val files = loadWebFiles(root)
            val expanded = listOf(root, File(root, "src"), source.parentFile!!, File(root, "packages"), packageFile.parentFile!!)
                .map { it.canonicalPath }
            compose.setContent { PocketCodedWebTheme(true, WebTheme.VSCodeDefaultDark.name, false) {
                ExplorerPanel(files, -1, root, root, "", expanded, palette, Modifier.fillMaxSize(), 0,
                    {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
            } }
            val path = source.canonicalPath
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("explorer-file:$path").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("empty").assertExists()
            compose.onNodeWithText("pkg").assertExists()
            val row = compose.onNodeWithTag("explorer-file:$path").fetchSemanticsNode().boundsInRoot
            val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            val pixelsPerDp = context.resources.displayMetrics.density
            val x = (row.left + 11f * pixelsPerDp).roundToInt()
            val y = row.center.y.roundToInt()
            val background = bitmap.getPixel(x + 10, y)
            assertTrue("A faint rail must connect nested explorer rows", (-3..3).any { bitmap.getPixel(x + it, y) != background })
            screenshot("explorer-indent-guides-and-badges", bitmap)
        } finally { root.deleteRecursively() }
    }
}
