package com.mkhokheli.pocketcodedweb

import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AiParityTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun breakpointsFollowInsertedAndRemovedLines() {
        assertEquals(setOf(3), relocateWebBreakpoints("first\nsecond", "new\nfirst\nsecond", setOf(2)))
        assertEquals(setOf(2), relocateWebBreakpoints("new\nfirst\nsecond", "first\nsecond", setOf(3)))
        assertEquals(emptySet<Int>(), relocateWebBreakpoints("first", "", setOf(1)))
    }

    @Test fun aiPathsAreValidatedBeforeApplyingTheBatch() {
        val root = temporary.newFolder()
        val existing = File(root, "index.html").apply { writeText("original") }
        fun write(path: String) = AiProjectFileChange(AiProjectFileOperation.Write, path, "after")
        listOf("../outside.txt", ".git/config", ".ssh/id_rsa", ".pocketcodedweb/history/data", "node_modules/library.js").forEach { path ->
            assertTrue(path, runCatching { resolveAiEditTargets(root, listOf(write("index.html"), write(path))) }.isFailure)
            assertEquals("original", existing.readText())
        }
        assertTrue(runCatching { resolveAiEditTargets(root, listOf(write("index.html"), write("./index.html"))) }.isFailure)
        File(root, "image.png").writeBytes(byteArrayOf(0, 1, 2))
        assertTrue(runCatching { resolveAiEditTargets(root, listOf(write("image.png"))) }.isFailure)
        assertEquals(listOf(File(root, "new.js").canonicalFile), resolveAiEditTargets(root, listOf(write("new.js"))))
    }

    @Test fun undoRestoresWritesDeletesAndNewFiles() {
        val root = temporary.newFolder()
        val edited = File(root, "edited.js").apply { writeText("before") }
        val deleted = File(root, "deleted.js").apply { writeText("deleted before") }
        val created = File(root, "created.js")
        val before = AiFileUndo.capture(listOf(edited, deleted, created))
        edited.writeText("after"); deleted.delete(); created.writeText("new")
        val undo = AiFileUndo.completed(before)
        undo.undo(ProjectFileStore(root))
        assertEquals("before", edited.readText())
        assertEquals("deleted before", deleted.readText())
        assertFalse(created.exists())
    }

    @Test fun undoChecksEveryFileBeforeRestoringAny() {
        val root = temporary.newFolder()
        val first = File(root, "first.js").apply { writeText("before") }
        val second = File(root, "second.js").apply { writeText("before") }
        val before = AiFileUndo.capture(listOf(first, second))
        first.writeText("AI"); second.writeText("AI")
        val undo = AiFileUndo.completed(before)
        second.writeText("manual edit")
        assertTrue(runCatching { undo.undo() }.isFailure)
        assertEquals("AI", first.readText())
        assertEquals("manual edit", second.readText())
    }

    @Test fun endpointValidationAllowsLocalServersAndRejectsUnsafeOrIncompleteUrls() {
        listOf("http://127.0.0.1:11434/v1/chat/completions", "http://192.168.1.10:11434/v1/chat/completions", "https://example.com/v1/chat/completions").forEach {
            assertEquals(it, validatedAiEndpoint(it))
        }
        listOf("", "example.com", "file:///tmp/key", "http://example.com/api", "https://key:secret@example.com/api", "https://example.com/api#fragment").forEach {
            assertTrue(it, runCatching { validatedAiEndpoint(it) }.isFailure)
        }
        assertTrue(runCatching { validatedAiEndpoint("https://example.com/chat/completions", azure = true) }.isFailure)
        assertEquals("https://example.com/chat/completions?api-version=2024-01-01", validatedAiEndpoint("https://example.com/chat/completions?api-version=2024-01-01", azure = true))
    }

    @Test fun databaseLoadingDoesNotReadOrRewriteBinaryContents() {
        val root = temporary.newFolder()
        val file = File(root, "data.sqlite").apply { writeBytes(byteArrayOf(0, 1, 2, -1)) }
        val config = File(root, ".env").apply { writeText("PORT=3000") }
        val loaded = loadWebFiles(root)
        assertEquals("", loaded.single { it.name == file.name }.content)
        assertEquals("PORT=3000", loaded.single { it.name == config.name }.content)
        assertArrayEquals(byteArrayOf(0, 1, 2, -1), file.readBytes())
    }
}
