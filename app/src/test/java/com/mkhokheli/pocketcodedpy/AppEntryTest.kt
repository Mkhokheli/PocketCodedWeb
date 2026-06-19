package com.mkhokheli.pocketcodedpy

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

    @Test
    fun explainsDisabledFirebaseProviders() {
        assertEquals(
            "Email/password sign-in is disabled for this Firebase project. Enable Email/Password in Firebase Authentication > Sign-in method.",
            authenticationConfigurationMessage("email", "ERROR_OPERATION_NOT_ALLOWED")
        )
        assertEquals(
            "Google sign-in is not fully configured. Enable Google in Firebase Authentication, add this app's SHA-1 in Firebase Project settings, then download a fresh google-services.json.",
            authenticationConfigurationMessage("google", "ERROR_OPERATION_NOT_ALLOWED")
        )
    }

    @Test
    fun explainsFirebaseNetworkFailures() {
        assertEquals(
            "Firebase could not be reached. Check your internet connection and try again.",
            authenticationConfigurationMessage("email", "ERROR_NETWORK_REQUEST_FAILED")
        )
        assertNull(authenticationConfigurationMessage("email", "ERROR_UNKNOWN"))
    }
}
