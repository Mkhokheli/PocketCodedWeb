package com.mkhokheli.pocketcodedweb

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SftpDeviceTest {
    private lateinit var root: File
    @Before fun setUp() { root = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "web-sftp-${System.nanoTime()}"); assertTrue(root.mkdirs()) }
    @After fun tearDown() { root.deleteRecursively() }

    @Test fun androidSftpRuntimeLoadsAndCommandPreflightWorks() {
        assertTrue(executeWebShellCommand("sftp help", root, root).output.contains("Built-in SFTP"))
        root.resolve("bad-key").writeText("not a private key")
        val result = executeWebShellCommand("sftp -i bad-key -P 2222 ls device@192.0.2.1 /", root, root)
        assertTrue(result.error.startsWith("sftp:"))
    }

    @Test fun rejectsUnsafeIncompleteAndInvalidCommandsBeforeConnecting() {
        assertTrue(executeWebShellCommand("sftp -i ../private ls user@host", root, root).error.contains("outside sandbox"))
        assertTrue(executeWebShellCommand("sftp chmod user@host 755", root, root).error.contains("missing path"))
        assertTrue(executeWebShellCommand("sftp -P 99999 ls user@host", root, root).error.contains("invalid or missing port"))
    }
}
