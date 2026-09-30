package com.mkhokheli.pocketcodedweb

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeGitDeviceTest {
    private lateinit var context: Context
    private lateinit var root: File
    private lateinit var git: File

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        root = File(context.cacheDir, "native-git-${System.nanoTime()}").apply { mkdirs() }
        git = NativeGitRuntime.install(context).getOrThrow()
        assertTrue(git.canExecute())
    }

    @After fun tearDown() { root.deleteRecursively() }

    @Test fun versionCommitDiffAndUnicodePathsWork() {
        assertTrue(runGit("--version").output.contains(NativeGitRuntime.VERSION))
        initRepository()
        val unusual = File(root, "space unicode-✓ # file.txt").apply { writeText("one\n") }
        assertSuccess(runGit("add", "--", unusual.name))
        assertSuccess(runGit("commit", "-m", "initial device commit"))
        unusual.appendText("two\n")
        assertTrue(runGit("diff", "--", unusual.name).output.contains("+two"))
    }

    @Test fun mergeRebaseStashTagsAndWorktreesWork() {
        initRepository()
        File(root, "base.txt").writeText("base\n")
        assertSuccess(runGit("add", ".")); assertSuccess(runGit("commit", "-m", "base"))
        assertSuccess(runGit("switch", "-c", "feature"))
        File(root, "feature.txt").writeText("feature\n")
        assertSuccess(runGit("add", ".")); assertSuccess(runGit("commit", "-m", "feature"))
        assertSuccess(runGit("switch", "master")); assertSuccess(runGit("merge", "--no-edit", "feature"))
        assertSuccess(runGit("tag", "v1.0"))
        File(root, "base.txt").appendText("dirty\n")
        assertSuccess(runGit("stash", "push", "-m", "device test"))
        assertFalse(File(root, "base.txt").readText().contains("dirty"))
        val worktree = File(root.parentFile, "${root.name}-worktree")
        try { assertSuccess(runGit("worktree", "add", "--detach", worktree.absolutePath, "HEAD")) }
        finally { worktree.deleteRecursively() }
    }

    @Test fun thousandFileIndexAndRepeatedStatusStayBounded() {
        initRepository()
        val data = File(root, "tree").apply { mkdirs() }
        repeat(1_000) { File(data, "file-$it.txt").writeText("$it:${"x".repeat(64)}\n") }
        val addMs = measureTimeMillis { assertSuccess(runGit("add", ".", timeoutSeconds = 30)) }
        assertSuccess(runGit("commit", "-m", "one thousand files", timeoutSeconds = 30))
        val statusMs = measureTimeMillis { repeat(20) { assertEquals("", runGit("status", "--porcelain=v1").output.trim()) } }
        assertTrue("git add took ${addMs}ms", addMs < 15_000)
        assertTrue("20 status runs took ${statusMs}ms", statusMs < 15_000)
    }

    @Test fun invalidRepositoryAndRevisionFailWithoutHanging() {
        assertTrue(runGit("status").exitCode != 0)
        initRepository()
        assertTrue(runGit("show", "revision-that-does-not-exist").exitCode != 0)
    }

    private fun initRepository() {
        assertSuccess(runGit("init", "--initial-branch=master"))
        assertSuccess(runGit("config", "user.name", "PocketCoded Web Device Test"))
        assertSuccess(runGit("config", "user.email", "device-test@example.invalid"))
        assertSuccess(runGit("config", "core.quotePath", "false"))
    }

    private fun runGit(vararg args: String, timeoutSeconds: Long = 10): Result {
        val prefix = NativeGitRuntime.prefix()
        val process = ProcessBuilder(listOf(git.absolutePath) + args).directory(root).redirectErrorStream(true).apply {
            environment().putAll(mapOf(
                "GIT_EXEC_PATH" to File(prefix, "libexec/git-core").absolutePath,
                "GIT_TEMPLATE_DIR" to File(prefix, "share/git-core/templates").absolutePath,
                "GIT_CONFIG_NOSYSTEM" to "1", "GIT_ATTR_NOSYSTEM" to "1",
                "GIT_PAGER" to "cat", "PAGER" to "cat", "LC_ALL" to "C.UTF-8",
                "PATH" to File(prefix, "bin").absolutePath + File.pathSeparator + System.getenv("PATH").orEmpty(),
            ))
        }.start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        assertTrue("Git timed out: ${args.toList()}", process.waitFor(timeoutSeconds, TimeUnit.SECONDS))
        return Result(process.exitValue(), output)
    }

    private fun assertSuccess(result: Result) = assertEquals(result.output, 0, result.exitCode)
    private data class Result(val exitCode: Int, val output: String)
}
