package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation

class TerminalTextUtilsTest {
    @Test fun localhostUrlsHaveClickableAnnotationsWithoutChangingOutput() {
        val source = "[preview] http://127.0.0.1:3000/\nOpen (http://localhost:5173/api?q=1).\nhttp://[::1]:8080/"
        val result = linkifyTerminalOutput(AnnotatedString(source))
        assertEquals(source, result.text)
        assertEquals(listOf("http://127.0.0.1:3000/", "http://localhost:5173/api?q=1", "http://[::1]:8080/"),
            result.getLinkAnnotations(0, result.length).map { (it.item as LinkAnnotation.Url).url })
    }

    @Test fun invalidAndNonWebAddressesAreNotLinked() {
        val result = linkifyTerminalOutput(AnnotatedString("javascript:alert(1) file:///data http:// http://localhost:99999"))
        assertTrue(result.getLinkAnnotations(0, result.length).isEmpty())
    }

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
