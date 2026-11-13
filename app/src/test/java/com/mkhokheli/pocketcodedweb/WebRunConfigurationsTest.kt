package com.mkhokheli.pocketcodedweb

import org.junit.Assert.*
import org.junit.Test

class WebRunConfigurationsTest {
    @Test fun renamingPreservesOrderAndOtherCommands() {
        val first = WebRunConfiguration("Dev", "npm run dev")
        val second = WebRunConfiguration("Test", "npm test")
        val updated = WebRunConfiguration("Watch tests", "npm test -- --watch")
        assertEquals(listOf(first, updated), updateWebRunConfigurations(listOf(first, second), "Test", updated))
    }

    @Test fun duplicateNamesDoNotOverwriteSavedCommands() {
        val saved = listOf(WebRunConfiguration("Dev", "npm run dev"), WebRunConfiguration("Test", "npm test"))
        assertTrue(runCatching { updateWebRunConfigurations(saved, null, WebRunConfiguration("Dev", "other")) }.isFailure)
        assertTrue(runCatching { updateWebRunConfigurations(saved, "Test", WebRunConfiguration("Dev", "other")) }.isFailure)
        assertEquals("npm run dev", saved.first().command)
    }

    @Test fun fullStoreStillAllowsEditingButRejectsNewEntries() {
        val saved = List(40) { WebRunConfiguration("Task $it", "echo $it") }
        val updated = WebRunConfiguration("Renamed", "npm test")
        assertEquals(40, updateWebRunConfigurations(saved, "Task 0", updated).size)
        assertTrue(runCatching { updateWebRunConfigurations(saved, null, updated) }.isFailure)
    }

    @Test fun invalidOrRemovedEntriesCannotBeSaved() {
        listOf(WebRunConfiguration(" ", "npm test"), WebRunConfiguration("Name", ""),
            WebRunConfiguration("Name", "npm\u0000test"), WebRunConfiguration("x".repeat(121), "npm test"),
            WebRunConfiguration("Name", "x".repeat(16_001))).forEach {
            assertTrue(runCatching { updateWebRunConfigurations(emptyList(), null, it) }.isFailure)
        }
        assertTrue(runCatching {
            updateWebRunConfigurations(emptyList(), "Deleted", WebRunConfiguration("New", "npm test"))
        }.isFailure)
    }
}
