package com.mkhokheli.pocketcodedweb

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WebProjectSearchTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun scopesFiltersCaseAndLineNumbers() = runBlocking {
        val root = temporary.newFolder()
        val folder = File(root, "src").apply { mkdirs() }
        val file = File(folder, "main.js").apply { writeText("const title = 'Title';\nTITLE title titles") }
        File(root, "data.json").writeText("title")
        val options = WebSearchOptions("title", wholeWord = true, fileFilter = "*.js")
        val found = findWebMatches(root, root, options)
        assertEquals(listOf(1, 1, 2, 2), found.matches.map { it.line })
        assertTrue(found.matches.all { it.relativePath == "src/main.js" })
        assertEquals(2, findWebMatches(root, file, options.copy(matchCase = true)).matches.size)
        assertEquals(4, findWebMatches(root, folder, options).matches.size)
        assertEquals(4, findWebMatches(root, File(root, "."), options).matches.size)
    }

    @Test fun excludesMetadataDependenciesBinaryAndInvalidUtf8() = runBlocking {
        val root = temporary.newFolder()
        listOf(".git", ".ssh", ".pocketcodedweb", "node_modules", "dist", "build").forEach {
            File(root, it).mkdirs(); File(root, "$it/secret.txt").writeText("needle")
        }
        File(root, "image.png").writeText("needle")
        File(root, "binary.txt").writeBytes("needle\u0000".toByteArray())
        File(root, "invalid.txt").writeBytes(byteArrayOf(0xc3.toByte(), 0x28) + "needle".toByteArray())
        File(root, ".env").writeText("needle")
        assertEquals(listOf(".env"), findWebMatches(root, root, WebSearchOptions("needle")).matches.map { it.relativePath })
    }

    @Test fun displayedLimitIsAlsoReplacementLimitAndHistoryIsRetained() = runBlocking {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("old old old") }
        val options = WebSearchOptions("old", replacement = "new")
        val found = findWebMatches(root, root, options, limit = 2)
        assertTrue(found.truncated)
        val store = ProjectFileStore(root)
        val replaced = replaceWebMatches(found.matches, options, store)
        assertEquals(2, replaced.count)
        assertEquals("new new old", file.readText())
        assertEquals("old old old", store.readRevision(file.path, store.history(file.path).single().id).getOrThrow())
    }

    @Test fun retainedSnapshotsHaveABoundedMemoryBudget() = runBlocking {
        val root = temporary.newFolder()
        File(root, "first.js").writeText("needle1234")
        File(root, "second.js").writeText("needle1234")
        val result = findWebMatches(root, root, WebSearchOptions("needle"), maxSnapshotChars = 10)
        assertTrue(result.truncated)
        assertEquals(1, result.matches.size)
    }

    @Test fun rejectsChangedSnapshotAndOutsideScope() = runBlocking {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("old") }
        val options = WebSearchOptions("old", "new")
        val matches = findWebMatches(root, root, options).matches
        file.writeText("newer manual edit")
        val result = replaceWebMatches(matches, options, ProjectFileStore(root))
        assertEquals(0, result.count)
        assertEquals(1, result.errors.size)
        assertEquals("newer manual edit", file.readText())
        assertTrue(runCatching { findWebMatches(root, temporary.newFolder(), options) }.isFailure)
    }

    @Test fun literalDollarsAndRegexCaptureGroups() = runBlocking {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("value12 value34") }
        val options = WebSearchOptions("value(\\d+)", "item$1", regex = true)
        val store = ProjectFileStore(root)
        assertEquals(2, replaceWebMatches(findWebMatches(root, file, options).matches, options, store).count)
        assertEquals("item12 item34", file.readText())
        val literal = WebSearchOptions("item", "$$\\")
        replaceWebMatches(findWebMatches(root, file, literal).matches, literal, store)
        assertEquals("$$\\12 $$\\34", file.readText())
    }

    @Test fun invalidRegexAndInvalidReplacementLeaveFilesUntouched() = runBlocking {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("old") }
        assertTrue(runCatching { findWebMatches(root, root, WebSearchOptions("[", regex = true)) }.isFailure)
        val options = WebSearchOptions("(old)", "$9", regex = true)
        val result = replaceWebMatches(findWebMatches(root, file, options).matches, options, ProjectFileStore(root))
        assertEquals(1, result.errors.size)
        assertEquals("old", file.readText())
    }

    @Test fun multilineRegexAndZeroWidthMatchesHaveStableOffsets() = runBlocking {
        val root = temporary.newFolder()
        val file = File(root, "main.js").apply { writeText("first\nsecond\n") }
        val multiline = findWebMatches(root, root, WebSearchOptions("first\\nsecond", regex = true)).matches.single()
        assertEquals(1, multiline.line)
        assertEquals(12, multiline.end)
        val options = WebSearchOptions("(?m)^", ">", regex = true)
        replaceWebMatches(findWebMatches(root, file, options).matches, options, ProjectFileStore(root))
        assertEquals(">first\n>second\n", file.readText())
    }
}
