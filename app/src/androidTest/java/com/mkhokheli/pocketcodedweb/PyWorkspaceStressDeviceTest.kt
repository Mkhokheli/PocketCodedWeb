package com.mkhokheli.pocketcodedweb

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.mkhokheli.pocketcodedweb.ui.theme.PocketCodedWebTheme
import java.io.File
import java.util.UUID
import kotlin.system.measureTimeMillis
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Real workspace boundaries must flush the native Editable before its 120 ms timer fires. */
class PyWorkspaceStressDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var project: File
    private lateinit var file: File
    private lateinit var original: String
    private var preferences: Map<String, *> = emptyMap<String, Any>()
    private val visible = mutableStateOf(true)
    private val keys = setOf("active_file_id", "active_file_path", "working_directory_path",
        "terminal_directory_path", "expanded_folder_paths", "explorer_visible", "file_search", "full_screen_mode")

    @Before fun setup() {
        NativeNodeRuntime.initialize(context)
        project = File(context.filesDir, "py-workspace-stress-${UUID.randomUUID()}").apply { mkdirs() }
        original = buildString { repeat(7_000) { append("// native editor stress line $it\n") } }
        file = File(project, "main.js").apply { writeText(original) }
        File(project, "other.css").writeText("body { color: red; }")
        val prefs = context.getSharedPreferences("web_ide", 0)
        preferences = prefs.all.filterKeys { it in keys }
        prefs.edit().apply {
            keys.forEach { remove(it) }
            putString("active_file_path", file.path)
            putBoolean("explorer_visible", false)
        }.commit()
        compose.setContent {
            PocketCodedWebTheme(darkTheme = true, themeName = WebTheme.VSCodeDefaultDark.name, dynamicColor = false) {
                if (visible.value) WebIdeApp(WebTheme.VSCodeDefaultDark, {}, project, {})
            }
        }
        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Fullscreen editor").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Fullscreen editor").performScrollTo().performClick()
    }

    @After fun cleanup() {
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        NodeSessionRuntime.stop()
        context.getSharedPreferences("web_ide", 0).edit().apply {
            keys.forEach { remove(it) }
            preferences.forEach { (key, value) -> when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Long -> putLong(key, value)
            } }
        }.commit()
        if (::project.isInitialized) {
            check(project.canonicalFile.parentFile == context.filesDir.canonicalFile)
            project.deleteRecursively()
        }
    }

    private fun nativeEditor(): LargeDocumentEditText {
        fun find(view: View): LargeDocumentEditText? {
            if (view is LargeDocumentEditText) return view
            if (view is ViewGroup) repeat(view.childCount) { find(view.getChildAt(it))?.let { return it } }
            return null
        }
        return requireNotNull(find(compose.activity.window.decorView)) { "Large document must use the native editor" }
    }

    @Test fun rapidBurstsSurviveRepeatedImmediateFullscreenSwitchesAndSaveNow() {
        var expected = original
        val elapsed = measureTimeMillis {
            repeat(8) { iteration ->
                val suffix = "// burst-$iteration-" + "x".repeat(64) + "\n"
                expected += suffix
                val minimize = compose.onNodeWithContentDescription("Minimize editor").fetchSemanticsNode()
                    .config[SemanticsActions.OnClick].action!!
                compose.runOnIdle {
                    val editor = nativeEditor()
                    suffix.forEach { editor.editableText.append(it) }
                    // Invoke in the same main-thread turn, before the native debounce can run.
                    assertTrue(minimize())
                }
                compose.onNodeWithContentDescription("Fullscreen editor").performScrollTo().performClick()
                compose.runOnIdle { assertEquals(expected, nativeEditor().text.toString()) }
            }
        }
        compose.onNodeWithContentDescription("Editor tools").performClick()
        compose.onNode(hasText("Save now", substring = true)).performClick()
        compose.waitUntil(10_000) { file.readText() == expected }
        assertEquals(expected, file.readText())
        val destination = File(context.getExternalFilesDir(null), "py-ui-stress").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(destination, "large-editor-fullscreen.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Log.i("PocketCodedPyStress", "burst-switch rounds=8 chars=${expected.length} elapsed-ms=$elapsed exact-save=true")
    }

    @Test fun runImmediatelyAfterTypingExecutesTheLatestNativeSnapshot() {
        val marker = "PY_NATIVE_IMMEDIATE_RUN_OK"
        val suffix = "\nconsole.log('$marker');\n"
        val run = compose.onNodeWithText("Run Code").fetchSemanticsNode().config[SemanticsActions.OnClick].action!!
        compose.runOnIdle {
            nativeEditor().editableText.append(suffix)
            assertTrue(run())
        }
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText(marker, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitUntil(10_000) { file.readText() == original + suffix }
        assertEquals(original + suffix, file.readText())
        Log.i("PocketCodedPyStress", "immediate-run chars=${original.length + suffix.length} latest-snapshot=true")
    }

    @Test fun compactExplorerResizesWithoutLosingTextAndTabsRevealTheActiveFile() {
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        repeat(64) { index -> File(project, "module-$index.js").writeText("export const value$index = $index;") }
        context.getSharedPreferences("web_ide", 0).edit()
            .putBoolean("explorer_visible", true).remove("full_screen_mode")
            .putString("active_file_path", file.path).commit()
        compose.runOnIdle { visible.value = true }
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("explorer-resize-handle").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("file-tab:main.js").assertIsDisplayed()
        var originalWidth = 0
        compose.runOnIdle { originalWidth = nativeEditor().width }
        compose.onNodeWithTag("explorer-resize-handle").performTouchInput {
            down(center)
            moveBy(Offset(90f, 0f))
            up()
        }
        compose.runOnIdle {
            assertTrue("Dragging right gives the explorer more width", nativeEditor().width < originalWidth)
            assertEquals(original, nativeEditor().text.toString())
        }
        val destination = File(context.getExternalFilesDir(null), "py-ui-stress").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(destination, "resized-workspace.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        // Restore the narrow tree so its overflow menu uses the Py compact layout.
        compose.onNodeWithTag("explorer-resize-handle").performTouchInput {
            down(center); moveBy(Offset(-150f, 0f)); up()
        }
        compose.onNodeWithContentDescription("Explorer actions").performClick()
        compose.onNodeWithText("Fullscreen explorer").performClick()
        compose.onNodeWithText("Search & Replace").assertExists()
        Log.i("PocketCodedPyStress", "workspace files=66 active-tab-revealed=true resize-preserves-text=true compact-menu=true")
    }
}
