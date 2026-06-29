package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebEditorIntelligenceTest {
    @Test
    fun htmlInspectionFindsUnmatchedTags() {
        val diagnostics = inspectWebSource("index.html", "<main><section></main>")

        assertTrue(diagnostics.any { it.severity == "error" && it.message.contains("section") })
    }

    @Test
    fun cssInspectionFindsInvalidColorsAndUnknownProperties() {
        val diagnostics = inspectWebSource(
            "style.css",
            ".card {\n    colour: #12;\n    display: flex\n}",
        )

        assertTrue(diagnostics.any { it.message.contains("Hex colors") })
        assertTrue(diagnostics.any { it.message.contains("colour") })
        assertTrue(diagnostics.any { it.message.contains("semicolon") })
    }

    @Test
    fun javascriptInspectionFindsDuplicateDeclarationsAndMissingDelimiters() {
        val diagnostics = inspectWebSource(
            "script.js",
            "const total = 1;\nconst total = 2;\nif (total > 1) {\n    console.log(total);",
        )

        assertTrue(diagnostics.any { it.message.contains("already declared") })
        assertTrue(diagnostics.any { it.message.contains("Missing closing '}'") })
    }

    @Test
    fun javascriptInspectionFindsIncompleteDeclarations() {
        val diagnostics = inspectWebSource(
            "script.js",
            "const button\nlet count =\ndocument.querySelector('')",
        )

        assertTrue(diagnostics.any { it.message.contains("initializer") })
        assertTrue(diagnostics.any { it.message.contains("missing a value") })
        assertTrue(diagnostics.any { it.message.contains("non-empty selector") })
    }

    @Test
    fun javascriptInspectionFindsCommonTypingErrors() {
        val diagnostics = inspectWebSource(
            "script.js",
            "docuemnt.querySelector('#app')\nif (ready = true) {\nfunction start()\nconst total = 1",
        )

        assertTrue(diagnostics.any { it.message.contains("document") })
        assertTrue(diagnostics.any { it.message.contains("'==='") })
        assertTrue(diagnostics.any { it.message.contains("body block") })
        assertTrue(diagnostics.any { it.message.contains("semicolon") })
    }

    @Test
    fun javascriptInspectionFindsGameCodedStyleRealtimeIssues() {
        val diagnostics = inspectWebSource(
            "script.js",
            "else {\nconst = 1\nif {\nfunction () {}\nbutton.",
        )

        assertTrue(diagnostics.any { it.message.contains("preceding block") })
        assertTrue(diagnostics.any { it.message.contains("variable name") })
        assertTrue(diagnostics.any { it.message.contains("condition") })
        assertTrue(diagnostics.any { it.message.contains("Function declarations need a name") })
        assertTrue(diagnostics.any { it.message.contains("member name") })
    }

    @Test
    fun diagnosticSeverityByLineKeepsHighestSeverity() {
        val diagnostics = listOf(
            CodeDiagnostic(1, 1, 0, 1, "hint", "hint"),
            CodeDiagnostic(1, 1, 0, 1, "error", "error"),
            CodeDiagnostic(2, 1, 2, 3, "warning", "warning"),
        )

        val severities = diagnosticSeverityByLine(diagnostics)

        assertEquals("error", severities[1])
        assertEquals("warning", severities[2])
    }

    @Test
    fun cssInspectionFindsMissingDeclarationColon() {
        val diagnostics = inspectWebSource(
            "style.css",
            ".card {\n    color red;\n}",
        )

        assertTrue(diagnostics.any { it.message.contains("colon") })
    }

    @Test
    fun htmlAttributeCompletionsAppearInsideTags() {
        val source = "<button cl"
        val context = completionContextAt(source, source.length, "index.html")

        assertNotNull(context)
        val completions = webCompletionsForContext("index.html", source, context!!)
        assertTrue(completions.any { it.label == "class" })
    }

    @Test
    fun htmlAttributeCompletionsAreTagAware() {
        val loadingSource = "<img lo"
        val loadingContext = completionContextAt(loadingSource, loadingSource.length, "index.html")

        assertNotNull(loadingContext)
        val loadingCompletions = webCompletionsForContext("index.html", loadingSource, loadingContext!!)
        assertTrue(loadingCompletions.any { it.label == "loading" })

        val altSource = "<img a"
        val altContext = completionContextAt(altSource, altSource.length, "index.html")

        assertNotNull(altContext)
        val altCompletions = webCompletionsForContext("index.html", altSource, altContext!!)
        assertTrue(altCompletions.any { it.label == "alt" })
    }

    @Test
    fun htmlAttributeValueCompletionsUseCurrentAttributeAndFileSymbols() {
        val button = "<button type=\""
        val buttonContext = completionContextAt(button, button.length, "index.html")

        assertNotNull(buttonContext)
        val buttonCompletions = webCompletionsForContext("index.html", button, buttonContext!!)
        assertTrue(buttonCompletions.any { it.label == "submit" })

        val classes = "<div class=\"card primary\"></div>\n<section class=\""
        val classContext = completionContextAt(classes, classes.length, "index.html")

        assertNotNull(classContext)
        val classCompletions = webCompletionsForContext("index.html", classes, classContext!!)
        assertTrue(classCompletions.any { it.label == "card" })
        assertTrue(classCompletions.any { it.label == "primary" })
    }

    @Test
    fun htmlAttributeValueCompletionsCoverLinksSourcesAndInlineStyles() {
        val script = "<script src=\""
        val scriptContext = completionContextAt(script, script.length, "index.html")

        assertNotNull(scriptContext)
        val scriptCompletions = webCompletionsForContext("index.html", script, scriptContext!!)
        assertTrue(scriptCompletions.any { it.label == "script.js" })

        val styleProperty = "<div style=\"dis"
        val stylePropertyContext = completionContextAt(styleProperty, styleProperty.length, "index.html")

        assertNotNull(stylePropertyContext)
        val stylePropertyCompletions = webCompletionsForContext("index.html", styleProperty, stylePropertyContext!!)
        assertTrue(stylePropertyCompletions.any { it.label == "display" })

        val styleValue = "<div style=\"display: f"
        val styleValueContext = completionContextAt(styleValue, styleValue.length, "index.html")

        assertNotNull(styleValueContext)
        val styleValueCompletions = webCompletionsForContext("index.html", styleValue, styleValueContext!!)
        assertTrue(styleValueCompletions.any { it.label == "flex" })
    }

    @Test
    fun javascriptDotCompletionsUseLocalTypeHints() {
        val source = "const items = [];\nitems."
        val context = completionContextAt(source, source.length, "script.js")

        assertNotNull(context)
        val completions = webCompletionsForContext("script.js", source, context!!)
        assertTrue(completions.any { it.label == "map()" })
    }

    @Test
    fun javascriptRootCompletionsWorkInJsFiles() {
        val source = "con"
        val context = completionContextAt(source, source.length, "script.js")

        assertNotNull(context)
        val completions = webCompletionsForContext("script.js", source, context!!)
        assertTrue(completions.any { it.label == "const" })
        assertTrue(completions.any { it.label == "console.log" })
    }

    @Test
    fun javascriptDomDotCompletionsWorkInJsFiles() {
        val source = "document."
        val context = completionContextAt(source, source.length, "script.js")

        assertNotNull(context)
        val completions = webCompletionsForContext("script.js", source, context!!)
        assertTrue(completions.any { it.label == "querySelector()" })
        assertTrue(completions.any { it.label == "createElement()" })
    }

    @Test
    fun javascriptStyleDotCompletionsWorkInJsFiles() {
        val source = "const card = document.querySelector('.card');\ncard.style."
        val context = completionContextAt(source, source.length, "script.js")

        assertNotNull(context)
        val completions = webCompletionsForContext("script.js", source, context!!)
        assertTrue(completions.any { it.label == "backgroundColor" })
    }

    @Test
    fun javascriptCompletionsInferPromiseDateAndNodeListMembers() {
        val promiseSource = "const request = fetch('/api');\nrequest."
        val promiseContext = completionContextAt(promiseSource, promiseSource.length, "script.js")

        assertNotNull(promiseContext)
        val promiseCompletions = webCompletionsForContext("script.js", promiseSource, promiseContext!!)
        assertTrue(promiseCompletions.any { it.label == "then()" })

        val dateSource = "const created = new Date();\ncreated."
        val dateContext = completionContextAt(dateSource, dateSource.length, "script.js")

        assertNotNull(dateContext)
        val dateCompletions = webCompletionsForContext("script.js", dateSource, dateContext!!)
        assertTrue(dateCompletions.any { it.label == "toISOString()" })

        val nodeListSource = "document.querySelectorAll('.item')."
        val nodeListContext = completionContextAt(nodeListSource, nodeListSource.length, "script.js")

        assertNotNull(nodeListContext)
        val nodeListCompletions = webCompletionsForContext("script.js", nodeListSource, nodeListContext!!)
        assertTrue(nodeListCompletions.any { it.label == "forEach()" })
    }

    @Test
    fun javascriptStaticPromiseCompletionsWork() {
        val source = "Promise."
        val context = completionContextAt(source, source.length, "script.js")

        assertNotNull(context)
        val completions = webCompletionsForContext("script.js", source, context!!)
        assertTrue(completions.any { it.label == "all()" })
        assertTrue(completions.any { it.label == "resolve()" })
    }

    @Test
    fun javascriptCompletionsInferEventsDatasetsHeadersAndObjectMembers() {
        val eventSource = "button.addEventListener('click', event => {\n    event."
        val eventContext = completionContextAt(eventSource, eventSource.length, "script.js")

        assertNotNull(eventContext)
        val eventCompletions = webCompletionsForContext("script.js", eventSource, eventContext!!)
        assertTrue(eventCompletions.any { it.label == "preventDefault()" })

        val datasetSource = "const button = document.querySelector('button');\nbutton.dataset."
        val datasetContext = completionContextAt(datasetSource, datasetSource.length, "script.js")

        assertNotNull(datasetContext)
        val datasetCompletions = webCompletionsForContext("script.js", datasetSource, datasetContext!!)
        assertTrue(datasetCompletions.any { it.label == "state" })

        val headersSource = "const response = await fetch('/api');\nresponse.headers."
        val headersContext = completionContextAt(headersSource, headersSource.length, "script.js")

        assertNotNull(headersContext)
        val headersCompletions = webCompletionsForContext("script.js", headersSource, headersContext!!)
        assertTrue(headersCompletions.any { it.label == "get()" })

        val objectSource = "const config = { apiUrl: '/api', retries: 3, refresh() {} };\nconfig."
        val objectContext = completionContextAt(objectSource, objectSource.length, "script.js")

        assertNotNull(objectContext)
        val objectCompletions = webCompletionsForContext("script.js", objectSource, objectContext!!)
        assertTrue(objectCompletions.any { it.label == "apiUrl" })
        assertTrue(objectCompletions.any { it.label == "refresh()" })
    }

    @Test
    fun webCompletionsWorkInsideHtmlScriptBlocks() {
        val source = "<script>\ndocument.\n</script>"
        val cursor = source.indexOf("document.") + "document.".length
        val context = completionContextAt(source, cursor, "index.html")

        assertNotNull(context)
        val completions = webCompletionsForContext("index.html", source, context!!)
        assertTrue(completions.any { it.label == "querySelector()" })
    }

    @Test
    fun webCompletionsWorkInsideHtmlStyleBlocks() {
        val source = "<style>\n.card {\n    display: \n}\n</style>"
        val cursor = source.indexOf("display: ") + "display: ".length
        val context = completionContextAt(source, cursor, "index.html")

        assertNotNull(context)
        val completions = webCompletionsForContext("index.html", source, context!!)
        assertTrue(completions.any { it.label == "flex" })
    }

    @Test
    fun htmlInspectionMapsInlineScriptDiagnostics() {
        val diagnostics = inspectWebSource(
            "index.html",
            "<!DOCTYPE html>\n<html><body><script>\nconst total = ;\n</script></body></html>",
        )

        assertTrue(diagnostics.any { it.line == 3 && it.message.contains("missing a value") })
    }

    @Test
    fun javascriptPrefixedDotCompletionsWorkInMjsFiles() {
        val source = "const items = [];\nitems.ma"
        val context = completionContextAt(source, source.length, "module.mjs")

        assertNotNull(context)
        val completions = webCompletionsForContext("module.mjs", source, context!!)
        assertTrue(completions.any { it.label == "map()" })
    }

    @Test
    fun javascriptCompletionsWorkInCjsFiles() {
        val source = "document.querySelector('#app').classList."
        val context = completionContextAt(source, source.length, "legacy.cjs")

        assertNotNull(context)
        val completions = webCompletionsForContext("legacy.cjs", source, context!!)
        assertTrue(completions.any { it.label == "toggle()" })
    }

    @Test
    fun javascriptCompletionsWorkInJsxFiles() {
        val source = "document."
        val context = completionContextAt(source, source.length, "component.jsx")

        assertNotNull(context)
        val completions = webCompletionsForContext("component.jsx", source, context!!)
        assertTrue(completions.any { it.label == "querySelector()" })
    }

    @Test
    fun cssValueCompletionsWorkInCssFiles() {
        val source = ".app {\n    display: "
        val context = completionContextAt(source, source.length, "style.css")

        assertNotNull(context)
        val completions = webCompletionsForContext("style.css", source, context!!)
        assertTrue(completions.any { it.label == "flex" })
        assertTrue(completions.any { it.label == "grid" })
    }

    @Test
    fun cssAtRulePseudoAndVariableCompletionsUseSyntaxContext() {
        val media = "@m"
        val mediaContext = completionContextAt(media, media.length, "style.css")

        assertNotNull(mediaContext)
        val mediaCompletions = webCompletionsForContext("style.css", media, mediaContext!!)
        assertTrue(mediaCompletions.any { it.label == "@media" })

        val pseudo = ".card:ho"
        val pseudoContext = completionContextAt(pseudo, pseudo.length, "style.css")

        assertNotNull(pseudoContext)
        val pseudoCompletions = webCompletionsForContext("style.css", pseudo, pseudoContext!!)
        assertTrue(pseudoCompletions.any { it.label == ":hover" })

        val variable = ":root {\n    --brand: #2255ff;\n}\n.card {\n    color: var(--"
        val variableContext = completionContextAt(variable, variable.length, "style.css")

        assertNotNull(variableContext)
        val variableCompletions = webCompletionsForContext("style.css", variable, variableContext!!)
        assertTrue(variableCompletions.any { it.label == "--brand" })
    }

    @Test
    fun cssCompletionsUseHtmlSelectorsAndRicherPropertyValues() {
        val htmlStyle = "<main id=\"app\" class=\"shell primary\"><style>\n."
        val styleCursor = htmlStyle.length
        val selectorContext = completionContextAt(htmlStyle, styleCursor, "index.html")

        assertNotNull(selectorContext)
        val selectorCompletions = webCompletionsForContext("index.html", htmlStyle, selectorContext!!)
        assertTrue(selectorCompletions.any { it.label == ".shell" })
        assertTrue(selectorCompletions.any { it.label == ".primary" })

        val gridValue = ".grid {\n    grid-template-columns: rep"
        val gridContext = completionContextAt(gridValue, gridValue.length, "style.css")

        assertNotNull(gridContext)
        val gridCompletions = webCompletionsForContext("style.css", gridValue, gridContext!!)
        assertTrue(gridCompletions.any { it.label.startsWith("repeat(") })
    }

    @Test
    fun completionDetailsProvideDefinitionSyntaxAndExamplesForAllLanguages() {
        val items = listOf(
            CompletionItem("/main>", "/main>", "closing tag"),
            CompletionItem("button", "<button type=\"button\"></button>", "element", 23),
            CompletionItem("display", "display: flex;", "css property"),
            CompletionItem("map()", "map(item => item)", "array", 4),
            CompletionItem("toFixed()", "toFixed(2)", "number", 8),
            CompletionItem("\"scripts\"", "\"scripts\": {\n    \"start\": \"\"\n}", "field", 29),
        )

        items.forEach { item ->
            assertTrue(completionExplanation(item).isNotBlank())
            assertTrue(completionSyntax(item).isNotBlank())
            assertTrue(completionExample(item).isNotBlank())
        }
    }

    @Test
    fun completionDetailsAreAvailableForEditorLanguageSuggestions() {
        val scenarios = listOf(
            "index.html" to "<main><",
            "index.html" to "<main></",
            "style.css" to ".card {\n    color: ",
            "script.js" to "const total = 2;\ntotal.",
            "package.json" to "\"scr",
        )

        scenarios.forEach { (fileName, source) ->
            val context = completionContextAt(source, source.length, fileName)

            assertNotNull("$fileName context should exist for $source", context)
            val completions = webCompletionsForContext(fileName, source, context!!)
            assertTrue("$fileName completions should not be empty for $source", completions.isNotEmpty())
            completions.forEach { item ->
                assertTrue("$fileName ${item.label} definition", completionExplanation(item).isNotBlank())
                assertTrue("$fileName ${item.label} syntax", completionSyntax(item).isNotBlank())
                assertTrue("$fileName ${item.label} example", completionExample(item).isNotBlank())
            }
        }
    }

    @Test
    fun estimatedLongestLineLengthStopsAtCap() {
        val text = "short\n${"x".repeat(10_000)}"

        assertEquals(200, estimatedLongestLineLength(text, cap = 200))
    }
}
