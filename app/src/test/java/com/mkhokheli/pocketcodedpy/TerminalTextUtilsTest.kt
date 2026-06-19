package com.mkhokheli.pocketcodedpy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalTextUtilsTest {
    @Test
    fun redactsCredentialsFromRemoteUrls() {
        val command = "git clone https://student:secret-token@gitlab.example.com/team/project.git"

        val redacted = redactTerminalCommandForDisplay(command)

        assertEquals("git clone https://gitlab.example.com/team/project.git", redacted)
        assertFalse(redacted.contains("secret-token"))
    }

    @Test
    fun redactsCurlAuthorizationAndUserOptions() {
        val command = "curl -u user:password -H \"Authorization: Bearer abc123\" https://example.com"

        val redacted = redactTerminalCommandForDisplay(command)

        assertTrue(redacted.contains("-u ***"))
        assertTrue(redacted.contains("Authorization: Bearer ***"))
        assertFalse(redacted.contains("password"))
        assertFalse(redacted.contains("abc123"))
    }

    @Test
    fun trimsTerminalHistoryAtALineBoundary() {
        val text = buildString {
            repeat(25_000) { append("terminal line $it\n") }
        }

        val trimmed = trimTerminalBuffer(text)

        assertTrue(trimmed.startsWith("[Earlier terminal output trimmed]\n"))
        assertTrue(trimmed.length < text.length)
        assertTrue(trimmed.endsWith("terminal line 24999\n"))
    }
}
