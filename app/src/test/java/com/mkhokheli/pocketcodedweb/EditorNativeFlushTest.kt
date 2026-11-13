package com.mkhokheli.pocketcodedweb

import org.junit.Assert.*
import org.junit.Test

class EditorNativeFlushTest {
    @Test fun workspaceFlushPublishesEveryAttachedSessionBeforeSnapshot() {
        val registry = EditorSessionRegistry()
        var first = "old first"
        var second = "old second"
        registry.open("first.js", first).attachNativeEditor(Any()) { first = "latest first" }
        registry.open("second.js", second).attachNativeEditor(Any()) { second = "latest second" }
        registry.flushPendingEdits()
        assertEquals(listOf("latest first", "latest second"), listOf(first, second))
    }

    @Test fun invalidationDropsTheNativePublisherInsteadOfRecreatingADeletedFile() {
        val registry = EditorSessionRegistry()
        val session = registry.open("folder/main.js", "original")
        var publications = 0
        session.attachNativeEditor(Any()) { publications++ }
        registry.invalidateUnder("folder")
        session.flushPendingEdits()
        registry.flushPendingEdits()
        assertEquals(0, publications)
        assertFalse(session.valid)
    }

    @Test fun replacingAViewFlushesItsDraftAndOldReleaseCannotDetachTheNewView() {
        val session = EditorSession("original")
        val oldOwner = Any()
        val newOwner = Any()
        var oldPublications = 0
        var newPublications = 0
        session.attachNativeEditor(oldOwner) { oldPublications++ }
        session.attachNativeEditor(newOwner) { newPublications++ }
        assertEquals(1, oldPublications)
        session.detachNativeEditor(oldOwner)
        session.flushPendingEdits()
        assertEquals(1, newPublications)
        session.detachNativeEditor(newOwner)
        session.flushPendingEdits()
        assertEquals(1, newPublications)
    }

    @Test fun sessionEvictionFlushesTheNativeDraftBeforeDroppingItsPublisher() {
        val registry = EditorSessionRegistry(maxSessions = 1)
        val session = registry.open("first.js", "old")
        var publications = 0
        session.attachNativeEditor(Any()) { publications++ }
        registry.open("second.js", "second")
        assertEquals(1, publications)
        registry.flushPendingEdits()
        assertEquals(1, publications)
    }
}
