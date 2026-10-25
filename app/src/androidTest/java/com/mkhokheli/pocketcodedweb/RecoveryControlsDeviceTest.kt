package com.mkhokheli.pocketcodedweb

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecoveryControlsDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun previewRestoreAndConfirmedDiscardUseRecoveryCopies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(context.filesDir, "recovery-controls-device-test").apply { mkdirs() }
        val restoredFile = File(root, "restored.js").apply { writeText("original disk contents") }
        val discardedFile = File(root, "discarded.js").apply { writeText("keep this disk file") }
        val store = ProjectFileStore(root)
        store.stageDraft(store.prepareEdit(restoredFile.path, "unsaved recovered text")).getOrThrow()
        store.stageDraft(store.prepareEdit(discardedFile.path, "discard this copy")).getOrThrow()
        val restoredIndex = store.recoveryDrafts().indexOfFirst { File(it.filePath).canonicalPath == restoredFile.canonicalPath }
        val controller = WebDevelopmentController(context, root)
        val actions = WebDevelopmentActions(fileStore = store, development = controller,
            source = { _, _ -> }, startDevelopment = { _, _, _ -> },
            recover = { path, done -> done(store.recoverDraft(path).exceptionOrNull()?.message) },
            discardRecovery = { path, done -> done(store.discardDraft(path).exceptionOrNull()?.message) },
            clearOutput = {}, history = emptyList(), recall = {}, scripts = { emptyList() }, command = {}, preview = {})
        try {
            compose.setContent {
                MaterialTheme {
                    CompositionLocalProvider(LocalWebDevelopmentActions provides actions) { WebDevelopmentPanels(controller) }
                }
            }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Recovery drafts").fetchSemanticsNodes().isNotEmpty() }
            // Find controls within the row belonging to the selected file.
            compose.onNodeWithText("restored.js (22 bytes)").assertExists()
            compose.onAllNodesWithText("Preview")[restoredIndex].performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithText("discard this copy").fetchSemanticsNodes().isNotEmpty() || compose.onAllNodesWithText("unsaved recovered text").fetchSemanticsNodes().isNotEmpty() }
            compose.onAllNodesWithText("Close").onLast().performClick()
            compose.onAllNodesWithText("Restore")[restoredIndex].performClick()
            compose.waitUntil(5_000) { store.recoveryDrafts().size == 1 }
            // The other draft remains available for a separate discard decision.
            compose.onNodeWithText("Discard").performClick()
            compose.onNodeWithText("Discard recovery copy?").assertExists()
            compose.onAllNodesWithText("Discard").onLast().performClick()
            compose.waitUntil(5_000) { store.recoveryDrafts().isEmpty() }
            assertEquals("unsaved recovered text", restoredFile.readText())
            assertEquals("keep this disk file", discardedFile.readText())
        } finally { root.deleteRecursively() }
    }
}
