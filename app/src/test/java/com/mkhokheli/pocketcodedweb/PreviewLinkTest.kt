package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreviewLinkTest {
    @Test fun acceptsServerAddresses() {
        assertEquals("http://localhost:3000/api?q=1", normalizePreviewLink(" http://localhost:3000/api?q=1 "))
        assertEquals("http://192.168.1.4:5173/", normalizePreviewLink("http://192.168.1.4:5173/"))
        assertEquals("http://[::1]:3000", normalizePreviewLink("http://[::1]:3000"))
    }
    @Test fun rejectsInvalidAddressesAndNonWebSchemes() {
        listOf("", "localhost:3000", "javascript:alert(1)", "file:///etc/passwd", "https://", "https://a:99999", "http://a:0", "https://user:password@example.com").forEach {
            assertNull(it, normalizePreviewLink(it))
        }
    }
}
