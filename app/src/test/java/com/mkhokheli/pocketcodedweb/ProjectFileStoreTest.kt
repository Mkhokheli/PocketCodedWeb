package com.mkhokheli.pocketcodedweb

import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProjectFileStoreTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun previousVersionCanBeRestoredAfterSave() {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("before") }
        val store = ProjectFileStore(root)
        store.save(file.path, "after").getOrThrow()
        val revision = store.history(file.path).single()
        assertEquals("before", store.readRevision(file.path, revision.id).getOrThrow())
        assertEquals("before", store.restore(file.path, revision.id).getOrThrow())
        assertEquals("before", file.readText())
        assertTrue(store.history(file.path).any { store.readRevision(file.path, it.id).getOrThrow() == "after" })
    }

    @Test fun cancelledOlderEditCannotOverwriteNewerSave() {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("original") }
        val store = ProjectFileStore(root)
        val older = store.prepareEdit(file.path, "older")
        val newer = store.prepareEdit(file.path, "newer")
        store.save(newer).getOrThrow()
        store.save(older).getOrThrow()
        assertEquals("newer", file.readText())
    }

    @Test fun refusesPathsOutsideProject() {
        val root = temporary.newFolder()
        val outside = temporary.newFile()
        assertTrue(ProjectFileStore(root).save(outside.path, "replacement").isFailure)
    }

    @Test fun recoveryCanBePreviewedRestoredAndRemovedWithoutLosingDiskVersion() {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("disk version") }
        val store = ProjectFileStore(root)
        store.stageDraft(store.prepareEdit(file.path, "unsaved version")).getOrThrow()
        assertEquals("unsaved version", store.readDraft(file.path).getOrThrow())
        assertEquals("disk version", file.readText())
        assertEquals(file.path, store.recoveryDrafts().single().filePath)
        assertEquals("unsaved version", store.recoverDraft(file.path).getOrThrow())
        assertEquals("unsaved version", file.readText())
        assertTrue(store.recoveryDrafts().isEmpty())
        assertEquals("disk version", store.readRevision(file.path, store.history(file.path).single().id).getOrThrow())
    }

    @Test fun unresolvedRecoverySurvivesNewAutosaveAndDiscardKeepsLiveFile() {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("disk version") }
        val store = ProjectFileStore(root)
        store.stageDraft(store.prepareEdit(file.path, "recovered version")).getOrThrow()
        store.recoveryDrafts()
        assertTrue(store.save(file.path, "new typing").isFailure)
        assertEquals("recovered version", store.readDraft(file.path).getOrThrow())
        assertEquals("disk version", file.readText())
        store.discardDraft(file.path).getOrThrow()
        assertTrue(store.readDraft(file.path).isFailure)
        assertEquals("disk version", file.readText())
        store.save(file.path, "new typing").getOrThrow()
        assertEquals("new typing", file.readText())
    }

    @Test fun damagedRecoveryDoesNotReplaceLiveFile() {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("live") }
        val store = ProjectFileStore(root)
        store.stageDraft(store.prepareEdit(file.path, "recovery")).getOrThrow()
        root.walkTopDown().first { it.extension == "draft" }.appendText("damaged")
        assertTrue(store.readDraft(file.path).isFailure)
        assertTrue(store.recoverDraft(file.path).isFailure)
        assertEquals("live", file.readText())
    }
}
