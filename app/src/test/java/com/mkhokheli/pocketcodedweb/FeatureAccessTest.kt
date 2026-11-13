package com.mkhokheli.pocketcodedweb

import java.io.File
import java.nio.file.Files
import org.junit.Assert.*
import org.junit.Test

class FeatureAccessTest {
    @Test fun scratchpadIsOnlyAvailableAfterTrialWithoutPremiumAndOtherProjectsAreLocked() {
        val filesDir = File("test-files")
        val scratchpad = File(filesDir, "free_scratchpad")
        val project = File(filesDir, "projects/My project")
        assertFalse(canOpenProject(true, false, scratchpad, filesDir))
        assertFalse(canOpenProject(false, false, scratchpad, filesDir))
        assertTrue(canOpenProject(false, true, scratchpad, filesDir))
        assertFalse(canOpenProject(false, true, project, filesDir))
        assertTrue(canOpenProject(true, false, project, filesDir))
        assertFalse(canOpenProject(false, true, File(filesDir, "projects/free_scratchpad"), filesDir))
    }

    @Test fun trialEndsAtDeadlineAndPurchaseOverridesExpiredTrial() {
        val start = 1000L
        val end = trialEndTimestamp(start)
        assertTrue(hasFullFeatureAccess(false, end - 1, start, false))
        assertFalse(hasFullFeatureAccess(false, end, start, false))
        assertFalse(hasFullFeatureAccess(false, start, start, true))
        assertFalse(hasFullFeatureAccess(false, start, null, false))
        assertTrue(hasFullFeatureAccess(true, end, start, true))
    }

    @Test fun freeActionsRequestSpecificUpgradeAndPremiumActionsProceed() {
        val requests = mutableListOf<PremiumFeature>()
        val free = FeatureAccess(false) { requests += it }
        PremiumFeature.entries.forEach { assertFalse(free.require(it)) }
        assertEquals(PremiumFeature.entries.toList(), requests)
        val premium = FeatureAccess(true) { fail("Premium must not show an upgrade") }
        PremiumFeature.entries.forEach { assertTrue(premium.require(it)) }
    }

    @Test fun scratchpadSurvivesReopeningAndProjectsRemainReadOnly() {
        val directory = Files.createTempDirectory("free-workspace").toFile()
        try {
            val root = freeWorkspace(directory)
            File(root, "index.html").writeText("<h1>saved</h1>")
            val upgraded = File(freeWorkspace(directory), "index.html").readText()
            assertTrue(upgraded.contains("<h1>saved</h1>"))
            assertEquals(upgraded, File(freeWorkspace(directory), "index.html").readText())
            assertTrue(canEditWorkspace(false, root))
            assertFalse(canEditWorkspace(false, File(directory, "projects/free_scratchpad")))
            assertFalse(canEditWorkspace(false, File(directory, "projects/My project")))
            assertTrue(canEditWorkspace(true, File(directory, "projects/My project")))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun scratchpadCreatesLinkedFilesAndPreservesCustomizedAssets() {
        val directory = Files.createTempDirectory("scratchpad-assets").toFile()
        try {
            val root = freeWorkspace(directory)
            val html = File(root, "index.html").readText()
            assertTrue(html.contains("href=\"style.css\""))
            assertTrue(html.contains("src=\"script.js\""))
            File(root, "style.css").writeText("body { color: purple; }")
            File(root, "script.js").writeText("console.log('my code');")
            freeWorkspace(directory)
            assertEquals(html, File(root, "index.html").readText())
            assertEquals("body { color: purple; }", File(root, "style.css").readText())
            assertEquals("console.log('my code');", File(root, "script.js").readText())
            for (name in listOf("index.html", "style.css", "script.js")) {
                assertTrue(isEditableScratchpadFile(directory, root, File(root, name)))
            }
            assertFalse(isEditableScratchpadFile(directory, root, File(root, "extra.js")))
            assertFalse(isEditableScratchpadFile(directory, root, File(root, "nested/script.js")))
            assertFalse(isEditableScratchpadFile(directory, root, File(root, "../script.js")))
            assertFalse(isEditableScratchpadFile(directory, File(directory, "projects/Other"), File(root, "script.js")))
        } finally {
            check(directory.canonicalFile.parentFile == File(System.getProperty("java.io.tmpdir")).canonicalFile)
            directory.deleteRecursively()
        }
    }

    @Test fun olderScratchpadGetsMissingReferencesInsideHeadOnlyOnce() {
        val directory = Files.createTempDirectory("scratchpad-migration").toFile()
        try {
            val root = File(directory, "free_scratchpad").apply { mkdirs() }
            val original = "<html><head><title>My work</title></head><body>Keep this</body></html>"
            File(root, "index.html").writeText(original)
            freeWorkspace(directory)
            val migrated = File(root, "index.html").readText()
            assertTrue(migrated.contains("<title>My work</title>"))
            assertTrue(migrated.contains("<body>Keep this</body>"))
            assertTrue(migrated.indexOf("href=\"style.css\"") < migrated.indexOf("</head>"))
            assertTrue(migrated.indexOf("src=\"script.js\"") < migrated.indexOf("</head>"))
            freeWorkspace(directory)
            assertEquals(migrated, File(root, "index.html").readText())
        } finally {
            check(directory.canonicalFile.parentFile == File(System.getProperty("java.io.tmpdir")).canonicalFile)
            directory.deleteRecursively()
        }
    }

    @Test fun planOrderAndUnavailableAnnualPriceAreExplicit() {
        assertEquals(listOf(PremiumPlan.Monthly, PremiumPlan.Yearly, PremiumPlan.Lifetime), PremiumPlan.entries)
        val state = BillingUiState(monthlyAvailable = true, monthlyPrice = "R29.99")
        assertFalse(state.yearlyAvailable)
        assertEquals("", state.yearlyPrice)
    }
}
