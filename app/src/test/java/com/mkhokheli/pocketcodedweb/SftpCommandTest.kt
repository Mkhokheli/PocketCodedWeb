package com.mkhokheli.pocketcodedweb

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SftpCommandTest {
    @Test fun parsesPortsIdentityIpv4Ipv6AndRecursiveTransfers() {
        val root = Files.createTempDirectory("web-sftp-parse").toFile()
        try {
            val identity = root.resolve(".ssh/custom").apply { parentFile.mkdirs(); writeText("key") }
            val parsed = parseSftpOptions(listOf("-P", "2222", "-i", "~/.ssh/custom", "get", "-r", "dev@example.com", "/srv", "local"), root, root).getOrThrow()
            assertEquals("dev", parsed.user); assertEquals("example.com", parsed.host); assertEquals(2222, parsed.port)
            assertEquals(identity.canonicalFile, parsed.identityFile?.canonicalFile); assertTrue(parsed.recursive)
            assertEquals(listOf("/srv", "local"), parsed.commandArgs)
            val ipv6 = parseSftpOptions(listOf("stat", "root@[2001:db8::1]:2200", "/"), root, root).getOrThrow()
            assertEquals("2001:db8::1", ipv6.host); assertEquals(2200, ipv6.port)
        } finally { root.deleteRecursively() }
    }

    @Test fun rejectsMalformedEndpointsPortsOptionsAndSandboxEscapes() {
        val root = Files.createTempDirectory("web-sftp-invalid").toFile()
        try {
            val cases = listOf(
                listOf("-P", "0", "ls", "user@host") to "invalid or missing port",
                listOf("ls", "user@host:65536") to "invalid port",
                listOf("--password", "secret", "ls", "user@host") to "unsupported option",
                listOf("ls", "host-without-user") to "user@host",
                listOf("unknown", "user@host") to "unknown command",
                listOf("-i", "../escape", "ls", "user@host") to "outside sandbox",
            )
            cases.forEach { (args, expected) -> assertTrue(parseSftpOptions(args, root, root).exceptionOrNull()?.message.orEmpty().contains(expected)) }
        } finally { root.deleteRecursively() }
    }

    @Test fun helpAndPreflightFailuresNeedNoNetworkAndCreateNoDownload() {
        val root = Files.createTempDirectory("web-sftp-preflight").toFile()
        try {
            val help = executeWebShellCommand("sftp help", root, root)
            assertTrue(help.output.contains("Built-in SFTP")); assertTrue(help.output.contains("get|put -r")); assertTrue(help.output.contains("chmod"))
            assertTrue(executeWebShellCommand("sftp get user@host", root, root).error.contains("missing path"))
            assertTrue(executeWebShellCommand("sftp ls user@host", root, root).error.contains("no SSH key found"))
            assertFalse(root.resolve("download").exists())
        } finally { root.deleteRecursively() }
    }
}
