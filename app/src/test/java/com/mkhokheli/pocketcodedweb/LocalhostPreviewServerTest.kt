package com.mkhokheli.pocketcodedweb

import java.io.IOException
import java.net.SocketException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalhostPreviewServerTest {
    @Test
    fun brokenPipeIsAnExpectedClientDisconnect() {
        assertTrue(isPreviewClientDisconnect(SocketException("Broken pipe")))
    }

    @Test
    fun wrappedConnectionResetIsAnExpectedClientDisconnect() {
        assertTrue(
            isPreviewClientDisconnect(
                IOException("response failed", SocketException("Connection reset by peer")),
            ),
        )
    }

    @Test
    fun unrelatedIoFailureIsNotMisclassified() {
        assertFalse(isPreviewClientDisconnect(IOException("File could not be read")))
    }
}
