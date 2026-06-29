package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppEntryTest {
    @Test
    fun acceptsNormalProjectNames() {
        assertNull(validateProjectName("Calculator App"))
        assertNull(validateProjectName("python-api_2"))
    }

    @Test
    fun rejectsUnsafeProjectNames() {
        assertEquals("Enter a project name.", validateProjectName("  "))
        assertEquals(
            "The project name contains an unsupported character.",
            validateProjectName("../../outside")
        )
        assertEquals("Project names cannot start with a period.", validateProjectName(".hidden"))
        assertEquals("That name is reserved for app runtime files.", validateProjectName("bin"))
        assertEquals("That name is reserved for app runtime files.", validateProjectName("TMP"))
    }

    @Test
    fun hidesRuntimeFoldersFromProjectPicker() {
        assertEquals(false, isVisibleProjectDirectoryName("bin"))
        assertEquals(false, isVisibleProjectDirectoryName("tmp"))
        assertEquals(false, isVisibleProjectDirectoryName(".runtime"))
        assertEquals(true, isVisibleProjectDirectoryName("Password_validation"))
    }
}
