package com.mkhokheli.pocketcodedweb

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PyRefinementsDeviceTest {
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

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun project() = File(context.filesDir, "py-refinements-${UUID.randomUUID()}").apply { mkdirs() }
    private fun actions(root: File, flush: suspend () -> Unit = {}) = WebDevelopmentActions(
        fileStore = ProjectFileStore(root), development = WebDevelopmentController(context, root),
        source = { _, _ -> }, startDevelopment = { _, _, _ -> }, recover = { _, done -> done(null) },
        discardRecovery = { _, done -> done(null) }, clearOutput = {}, history = emptyList(), recall = {},
        scripts = { emptyList() }, command = {}, preview = {}, prepareSearch = flush,
    )

    @Test fun historyPreviewsBeforeRestoringAndFlushesPendingEdits() {
        val root = project()
        val file = File(root, "main.js").apply { writeText("version one") }
        val store = ProjectFileStore(root)
        store.save(file.path, "current disk").getOrThrow()
        val restored = AtomicReference<String>()
        val actions = actions(root) { store.save(file.path, "pending edits").getOrThrow() }
        try {
            compose.setContent { MaterialTheme {
                WebFileHistoryDialog(file.path, actions, onDismiss = {}, onRestore = { restored.set(it) })
            } }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("11 bytes").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Restore version").assertIsNotEnabled()
            compose.onNodeWithText("11 bytes").performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithText("version one").fetchSemanticsNodes().isNotEmpty() }
            assertEquals("pending edits", file.readText())
            assertNull(restored.get())
            compose.onNodeWithText("Restore version").performClick()
            assertEquals("version one", restored.get())
            // The parent editor owns the undoable restore; reading a preview never writes it.
            assertEquals("pending edits", file.readText())
        } finally { root.deleteRecursively() }
    }

    @Test fun emptyHistoryVersionCanBePreviewedAndRestored() {
        val root = project()
        val file = File(root, "empty.js").apply { writeText("") }
        val actions = actions(root)
        actions.fileStore.save(file.path, "current").getOrThrow()
        val restored = AtomicReference<String>()
        try {
            compose.setContent { MaterialTheme {
                WebFileHistoryDialog(file.path, actions, onDismiss = {}, onRestore = { restored.set(it) })
            } }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("0 bytes").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("0 bytes").performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithText("(Empty file)").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Restore version").assertIsEnabled().performClick()
            assertEquals("", restored.get())
        } finally { root.deleteRecursively() }
    }

    @Test fun runConfigurationCanBeEditedRenamedAndReloaded() {
        val root = project()
        val file = File(root, "main.js").apply { writeText("console.log(1)") }
        val configurationStore = WebRunConfigurationStore(context, root.path)
        configurationStore.save(listOf(WebRunConfiguration("Old name", "npm test")))
        val actions = actions(root)
        try {
            compose.setContent { MaterialTheme {
                CompositionLocalProvider(LocalWebDevelopmentActions provides actions) {
                    WebDevelopmentMenu(WebFile(1, file.name, file.readText(), file.path, file.lastModified()),
                        file.readText(), webIdePalette(WebTheme.VSCodeDefaultDark), onRestore = {})
                }
            } }
            compose.onNodeWithContentDescription("Editor tools").performClick()
            compose.onNodeWithText("Run configurations").performClick()
            compose.onNodeWithText("Edit").performClick()
            compose.onNode(hasSetTextAction() and hasText("Old name")).performTextReplacement("Watch tests")
            compose.onNodeWithText("Update configuration").performScrollTo().performClick()
            compose.onNodeWithText("Watch tests").assertExists()
            assertEquals(listOf(WebRunConfiguration("Watch tests", "npm test")), configurationStore.load())
        } finally {
            context.getSharedPreferences("web_run_configurations", 0).edit()
                .remove(root.path).remove(root.canonicalPath).commit()
            root.deleteRecursively()
        }
    }
}
