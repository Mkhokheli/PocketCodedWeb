package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class WebProjectFilesTest {
    @Test fun largeRefreshReusesLiveDocumentsAndStillDetectsChanges() {
        withTempProject { root ->
            repeat(200) { File(root, "module$it.js").writeText("const value = $it;") }
            val first = loadWebFiles(root).associateBy { it.filePath }
            val second = loadWebFiles(root)
            assertTrue(second.all { first[it.filePath] === it })
            val changed = File(root, "module0.js")
            changed.writeText("const changedValue = 12345;")
            val third = loadWebFiles(root).first { it.name == changed.name }
            assertEquals("const changedValue = 12345;", third.content)
        }
    }

    @Test
    fun loadWebFilesAllowsEmptyProjectAfterDeletion() {
        withTempProject { root ->
            ensureDefaultWebProject(root)

            val seededFiles = loadWebFiles(root)
            val seededNames = seededFiles.map { it.name }.toSet()

            assertTrue(seededNames.contains("index.html"))
            assertTrue(seededNames.contains("style.css"))
            assertTrue(seededNames.contains("script.js"))

            seededFiles.forEach { file ->
                assertTrue(File(file.filePath).delete())
            }

            val filesAfterDeletion = loadWebFiles(root)

            assertTrue(filesAfterDeletion.isEmpty())
            assertFalse(File(root, "index.html").exists())
        }
    }

    @Test
    fun loadWebFilesRefreshesCachedContentWhenFileChanges() {
        withTempProject { root ->
            val file = File(root, "index.html")
            file.writeText("<h1>One</h1>")

            val firstLoad = loadWebFiles(root).single()
            assertEquals("<h1>One</h1>", firstLoad.content)

            file.writeText("<h1>Two</h1>")
            file.setLastModified(firstLoad.modifiedAt + 2_000L)

            val secondLoad = loadWebFiles(root).single()

            assertEquals("<h1>Two</h1>", secondLoad.content)
            assertEquals("<h1>Two</h1>".hashCode(), secondLoad.contentHash)
        }
    }

    @Test
    fun loadInitialWebFilesRestoresPreferredFileWithoutLoadingWorkspace() {
        withTempProject { root ->
            val preferred = File(root, "page.js").apply { writeText("console.log('page')") }
            File(root, "index.html").writeText("<h1>Home</h1>")
            repeat(25) { index -> File(root, "extra$index.js").writeText("export default $index") }

            val initial = loadInitialWebFiles(root, preferred.absolutePath)

            assertEquals(1, initial.size)
            assertEquals(preferred.absolutePath, initial.single().filePath)
        }
    }

    @Test
    fun loadInitialWebFilesUsesIndexAsFastFallback() {
        withTempProject { root ->
            File(root, "z.js").writeText("console.log('z')")
            File(root, "index.html").writeText("<h1>Home</h1>")

            val initial = loadInitialWebFiles(root, preferredPath = null)

            assertEquals(listOf("index.html"), initial.map { it.name })
        }
    }

    private fun withTempProject(block: (File) -> Unit) {
        val root = Files.createTempDirectory("pcw-empty-project").toFile()
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
