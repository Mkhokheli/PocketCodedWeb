package com.mkhokheli.pocketcodedweb

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WebQuickFixTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun fix(name: String, source: String, title: String) =
        inspectWebSource(name, source).mapNotNull { it.quickFix }.first { it.title == title }

    @Test fun correctsJavaScriptTyposWithoutReplacingOtherOccurrences() {
        val source = "const text = 'docuemnt';\nconst title = docuemnt.title;"
        val fixes = inspectWebSource("main.js", source).mapNotNull { it.quickFix }
        assertEquals(1, fixes.size)
        assertEquals("const text = 'docuemnt';\nconst title = document.title;", applyWebQuickFix(source, fixes.single()).getOrThrow())
    }

    @Test fun commentsStringsAndRegexesDoNotGetTokenFixes() {
        listOf("// docuemnt", "/* docuemnt */", "const x = `docuemnt`;", "const x = /docuemnt/;").forEach { source ->
            assertTrue(source, inspectWebSource("main.js", source).none { it.quickFix != null })
        }
    }

    @Test fun staleFixCannotChangeTextEvenWhenTheTokenStillMatches() {
        val source = "const a = docuemnt.title;"
        val suggestion = fix("main.js", source, "Replace with document")
        assertTrue(applyWebQuickFix(source.replace("a =", "b ="), suggestion).isFailure)
        assertTrue(applyWebQuickFix(source + "\n", suggestion).isFailure)
        assertEquals("const a = document.title;", applyWebQuickFix(source, suggestion).getOrThrow())
    }

    @Test fun htmlDoctypeViewportAltAndAttributeFixesUseExactOffsets() {
        val source = "<html><head></head><body><img src=photo.png></body></html>"
        assertTrue(applyWebQuickFix(source, fix("index.html", source, "Add HTML doctype")).getOrThrow().startsWith("<!DOCTYPE html>\n"))
        assertTrue(applyWebQuickFix(source, fix("index.html", source, "Add mobile viewport")).getOrThrow()
            .contains("<head>\n    <meta name=\"viewport\""))
        assertTrue(applyWebQuickFix(source, fix("index.html", source, "Add alt attribute")).getOrThrow().contains("photo.png alt=\"\">"))
        assertTrue(applyWebQuickFix(source, fix("index.html", source, "Quote attribute value")).getOrThrow().contains("src=\"photo.png\""))
    }

    @Test fun existingSpacedAltAttributeIsNeverDuplicated() {
        val source = "<!DOCTYPE html><img src=\"photo.png\" alt = 'Photo'>"
        assertTrue(inspectWebSource("index.html", source).none { it.quickFix?.title == "Add alt attribute" })
    }

    @Test fun cssSemicolonFixPreservesTrailingWhitespaceAndNextLine() {
        val source = ".card {\n    color: red   \n}\n"
        val result = applyWebQuickFix(source, fix("style.css", source, "Add semicolon")).getOrThrow()
        assertEquals(".card {\n    color: red;   \n}\n", result)
    }

    @Test fun debuggerRemovalLeavesSurroundingStatementsAlone() {
        val source = "const a = 1; debugger; console.log(a);"
        assertEquals("const a = 1;  console.log(a);",
            applyWebQuickFix(source, fix("main.js", source, "Remove debugger statement")).getOrThrow())
    }

    @Test fun closingElementFixClosesNestedHtmlInStackOrder() {
        val source = "<!DOCTYPE html><html><body><div>hello"
        assertEquals("$source</div></body></html>", applyWebQuickFix(source,
            fix("index.html", source, "Close unclosed elements")).getOrThrow())
    }

    @Test fun delimiterFixUsesTheEndOfExpressionBeforeATrailingSemicolon() {
        val source = "const value = (42;"
        assertEquals("const value = (42);", applyWebQuickFix(source,
            fix("main.js", source, "Close open delimiters")).getOrThrow())
        val block = "function greet() {\n    console.log('hi');"
        assertEquals("$block}", applyWebQuickFix(block, fix("main.js", block, "Close open delimiters")).getOrThrow())
    }

    @Test fun stringAndCommentClosersDoNotInventMissingContent() {
        val quoted = "const value = \"hello"
        assertEquals("$quoted\"", applyWebQuickFix(quoted, fix("main.js", quoted, "Close string literal")).getOrThrow())
        val comment = "/* incomplete"
        assertEquals("$comment*/", applyWebQuickFix(comment, fix("main.js", comment, "Close block comment")).getOrThrow())
    }

    @Test fun projectScanPrioritisesActiveDraftAndExcludesMetadataDependenciesAndInvalidText() = runBlocking {
        val root = temporary.newFolder()
        val active = File(root, "main.js").apply { writeText("console.log('saved');") }
        File(root, "index.html").writeText("<img src=\"x\">")
        listOf(".ssh", ".git", ".pocketcodedweb", "node_modules", "dist").forEach {
            File(root, it).mkdirs(); File(root, "$it/secret.js").writeText("docuemnt.title;")
        }
        File(root, "binary.js").writeBytes(byteArrayOf(0, 1, 2))
        File(root, "invalid.js").writeBytes(byteArrayOf(0xc3.toByte(), 0x28))
        val result = scanWebProjectDiagnostics(root, active, "docuemnt.title;")
        assertFalse(result.truncated)
        assertTrue(result.entries.any { it.relativePath == "main.js" && it.diagnostic.quickFix?.title == "Replace with document" })
        assertTrue(result.entries.all { it.relativePath in setOf("main.js", "index.html") })
        assertEquals("console.log('saved');", active.readText())
    }

    @Test fun projectScanLimitsFilesSnapshotsAndDisplayedDiagnostics() = runBlocking {
        val root = temporary.newFolder()
        val active = File(root, "main.js").apply { writeText("docuemnt.title;") }
        File(root, "other.js").writeText("docuemnt.title;")
        assertTrue(scanWebProjectDiagnostics(root, active, active.readText(), maxFiles = 1).truncated)
        assertTrue(scanWebProjectDiagnostics(root, active, active.readText(), maxBytes = 1).truncated)
        val bounded = scanWebProjectDiagnostics(root, active, "docuemnt.title; cosnole.log(1);", maxDiagnostics = 1)
        assertTrue(bounded.truncated)
        assertEquals(1, bounded.entries.size)
    }

    @Test fun explainPromptIncludesLocationAndDiagnostic() {
        val diagnostic = inspectWebSource("main.js", "docuemnt.title;").first()
        val prompt = webDiagnosticPrompt("src/main.js", diagnostic)
        assertTrue(prompt.contains("File: src/main.js"))
        assertTrue(prompt.contains(diagnostic.message))
        assertTrue(prompt.contains("line 1"))
    }
}
