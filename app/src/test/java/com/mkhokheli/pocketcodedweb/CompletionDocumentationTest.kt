package com.mkhokheli.pocketcodedweb

import org.junit.Assert.*
import org.junit.Test

class CompletionDocumentationTest {
    @Test fun doctypeExplainsStandardsModeAndHasCompleteExample() {
        val doc = specialCompletionDocumentation(CompletionItem("<!DOCTYPE html>", detail = "document"))!!
        assertTrue(doc.description.contains("standards mode"))
        assertTrue(doc.example.contains("<body>"))
        assertEquals("https://html.spec.whatwg.org/multipage/syntax.html#the-doctype", doc.source)
    }

    @Test fun customPropertyReferencePreservesNameAndExplainsFallback() {
        val doc = specialCompletionDocumentation(CompletionItem("var(--bg-dark)", detail = "css value"))!!
        assertTrue(doc.description.contains("fallback"))
        assertTrue(doc.example.contains("--bg-dark: #182126"))
        assertTrue(doc.example.contains("var(--bg-dark, #fff)"))
        assertTrue(doc.source.startsWith("https://www.w3.org/"))
        assertNull(specialCompletionDocumentation(CompletionItem("var(--bg-dark)", detail = "symbol")))
    }

    @Test fun jsonDocumentationIdentifiesPackageJsonContext() {
        val doc = specialCompletionDocumentation(CompletionItem("\"scripts\"", detail = "field"))!!
        assertTrue(doc.description.contains("package.json"))
        assertTrue(doc.description.contains("npm run"))
        assertTrue(doc.example.contains("node server.js"))
    }
}
