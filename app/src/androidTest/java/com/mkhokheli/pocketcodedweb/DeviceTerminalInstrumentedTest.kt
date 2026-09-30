package com.mkhokheli.pocketcodedweb

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlin.system.measureTimeMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceTerminalInstrumentedTest {
    private lateinit var root: File

    @Before fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NativeNodeRuntime.initialize(context)
        NativeGitRuntime.install(context).getOrThrow()
        root = File(context.filesDir, "projects/TerminalQualification")
        root.deleteRecursively()
        root.mkdirs()
        File(root, "index.html").writeText("<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"></head><body><h1>Terminal test</h1></body></html>")
        File(root, "app.js").writeText("console.log('file-run-ok', process.argv.slice(2).join('|'));\n")
        File(root, "package.json").writeText("""{"name":"terminal-qualification","private":true,"scripts":{"lint":"lint","dev":"preview"}}""")
    }

    private fun run(command: String, working: File = root): WebShellResult {
        lateinit var result: WebShellResult
        val elapsed = measureTimeMillis { result = executeWebShellCommand(command, root, working) }
        Log.i("PocketCodedTerminal", "command=${command.take(80)} elapsed-ms=$elapsed output=${result.output.take(100)} error=${result.error.take(100)}")
        assertTrue("Terminal command exceeded 20 seconds: $command ($elapsed ms)", elapsed < 20_000)
        return result
    }

    @Test fun navigationQuotingAndFileLifecycle() {
        assertEquals("~", run("pwd").output)
        assertTrue(run("mkdir \"folder with spaces\"").error.isBlank())
        assertTrue(File(root, "folder with spaces").isDirectory)
        assertTrue(run("touch \"folder with spaces/source file.txt\"").workspaceChanged)
        File(root, "folder with spaces/source file.txt").writeText("safe content")
        assertEquals("safe content", run("cat \"folder with spaces/source file.txt\"").output)
        assertTrue(run("cp \"folder with spaces/source file.txt\" copied.txt").error.isBlank())
        assertTrue(run("mv copied.txt moved.txt").error.isBlank())
        assertTrue(File(root, "moved.txt").isFile)
        val changedDirectory = run("cd \"folder with spaces\"")
        assertNotNull(changedDirectory.workingDirectoryPath)
        assertTrue(run("ls", File(changedDirectory.workingDirectoryPath!!)).output.contains("source file.txt"))
        assertTrue(run("rm moved.txt").error.isBlank())
        assertFalse(File(root, "moved.txt").exists())
    }

    @Test fun sandboxRejectsTraversalAndDestructiveRootRemoval() {
        listOf("cat ../../outside.txt", "touch ../outside.txt", "mkdir ../outside", "cd ..").forEach { command ->
            assertTrue("$command should be rejected", run(command).error.contains("outside project"))
        }
        assertTrue(run("rm .").error.contains("refusing to remove project root"))
        assertTrue(root.isDirectory)
        assertTrue(run("sudo apt install nodejs").error.contains("not available"))
    }

    @Test fun nodeExecutionFailureAndRecovery() {
        val inline = run("node -e \"console.log(6 * 7)\"")
        assertTrue(inline.error, inline.output.contains("42"))
        val file = run("node app.js alpha \"two words\"")
        assertTrue(file.error, file.output.contains("file-run-ok alpha|two words"))
        val failure = run("node -e \"throw new Error('expected-terminal-failure')\"")
        assertTrue(failure.error.contains("expected-terminal-failure"))
        val recovered = run("node -e \"console.log('recovered')\"")
        assertTrue(recovered.error, recovered.output.contains("recovered"))
    }

    @Test fun webNpmSemanticAndMalformedCommands() {
        assertTrue(run("help").output.contains("PocketCoded Web terminal"))
        assertTrue(run("npm --version").output.contains("npm"))
        assertTrue(run("npm run").output.contains("lint"))
        assertTrue(run("npm run lint").error.isBlank())
        assertTrue(run("preview").openPreview)
        assertTrue(run("lint").output.isNotBlank())
        assertTrue(run("index").output.contains("Project index"))
        assertTrue(run("framework").output.isNotBlank())
        assertTrue(run("definitely-not-a-command").error.contains("Command not found"))
        assertTrue(run("node -e").error.contains("expected JavaScript code"))
        assertTrue(run("cat missing.txt").error.contains("no such file"))
        assertTrue(run("clear").clearTerminal)
    }

    @Test fun unsupportedCleanInstallPreservesProjectFiles() {
        val packageFile = File(root, "package.json")
        val originalPackage = packageFile.readText()
        val lock = File(root, "package-lock.json").apply { writeText("{\"lockfileVersion\":3}") }
        val installed = File(root, "node_modules/example/index.js").apply {
            parentFile!!.mkdirs()
            writeText("module.exports = 42;")
        }
        val result = run("npm ci")
        assertTrue(result.error.contains("not supported"))
        assertFalse(result.workspaceChanged)
        assertEquals(originalPackage, packageFile.readText())
        assertEquals("{\"lockfileVersion\":3}", lock.readText())
        assertEquals("module.exports = 42;", installed.readText())
    }

    @Test fun gitLifecycleAndLargeOutputAreBounded() {
        assertTrue(run("git init").error.isBlank())
        assertTrue(run("git config user.name \"PocketCoded Device Test\"").error.isBlank())
        assertTrue(run("git config user.email device-test@example.invalid").error.isBlank())
        assertTrue(run("git status").output.isNotBlank())
        assertTrue(run("git add index.html app.js package.json").error.isBlank())
        val commit = run("git commit -m \"terminal qualification\"")
        assertTrue(commit.error.ifBlank { commit.output }, commit.error.isBlank())
        assertTrue(run("git log").output.contains("terminal qualification"))

        File(root, "large.txt").writeText("x".repeat(120_000))
        val large = run("cat large.txt")
        assertEquals(80_000, large.output.length)
    }
}
