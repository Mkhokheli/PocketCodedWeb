package com.mkhokheli.pocketcodedweb

import org.json.JSONObject

internal data class CompletionDocumentation(
    val description: String,
    val syntax: String,
    val example: String = "",
    val source: String = "",
    val standard: String = "",
)

/** Immutable offline catalog; receiver names prevent Array/String method collisions. */
internal class CompletionDocumentationCatalog(json: String) {
    companion object {
        private var cached: CompletionDocumentationCatalog? = null

        @Synchronized
        fun load(context: android.content.Context): CompletionDocumentationCatalog {
            return cached ?: context.assets.open("completion-docs/reference.json")
                .bufferedReader().use { CompletionDocumentationCatalog(it.readText()) }
                .also { cached = it }
        }
    }
    private val entries = JSONObject(json)

    fun resolve(item: CompletionItem): CompletionDocumentation? {
        specialCompletionDocumentation(item)?.let { return it }
        val name = item.label.trim().removePrefix("<").removePrefix("/")
            .removeSuffix(">").substringBefore('(').substringBefore('=')
        val kind = item.detail.lowercase()
        val key = when {
            kind == "attribute" -> {
                val scoped = "attribute:${item.documentationOwner}:$name"
                if (entries.has(scoped)) scoped else "attribute:$name"
            }
            kind == "css value" && item.documentationOwner.isNotBlank() -> {
                val scoped = "css-value:${item.documentationOwner}:${item.label}"
                if (entries.has(scoped)) scoped else "css:${item.documentationOwner}"
            }
            kind.contains("css") || kind == "at-rule" || kind.contains("pseudo") || name.startsWith(':') -> {
                if (entries.has("css:$name")) "css:$name" else "css:$name()"
            }
            item.insertText.startsWith("<") || kind == "closing tag" -> "html:${name.substringBefore(':')}"
            else -> {
                val owners = if (item.documentationOwner.isNotBlank()) {
                    listOf(item.documentationOwner) + when (item.documentationOwner) {
                        "Document" -> listOf("ParentNode", "Node", "EventTarget")
                        "Element" -> listOf("ParentNode", "Node", "EventTarget", "HTMLElement")
                        "Response" -> listOf("Body")
                        "Window" -> listOf("WindowOrWorkerGlobalScope")
                        else -> emptyList()
                    }
                } else when (kind) {
                    "array" -> listOf("Array")
                    "string" -> listOf("String")
                    "number" -> listOf("Number")
                    "date" -> listOf("Date")
                    "object" -> listOf("ObjectConstructor")
                    "json" -> listOf("JSON")
                    "promise" -> listOf("Promise", "PromiseConstructor")
                    "storage" -> listOf("Storage")
                    "response" -> listOf("Response", "Body")
                    "headers" -> listOf("Headers")
                    "url" -> listOf("URL")
                    "search params", "url params", "searchparams" -> listOf("URLSearchParams")
                    "location" -> listOf("Location")
                    "history" -> listOf("History")
                    "navigator" -> listOf("Navigator")
                    "clipboard" -> listOf("Clipboard")
                    "event" -> listOf("Event", "MouseEvent", "KeyboardEvent")
                    "style" -> listOf("CSSStyleDeclaration")
                    "dom", "nodelist" -> listOf("Document", "ParentNode", "Element", "HTMLElement", "EventTarget", "Node", "NodeList", "DOMTokenList")
                    "function", "window" -> listOf("Window", "WindowOrWorkerGlobalScope", "Console", "Math", "JSON")
                    else -> emptyList()
                }
                owners.map { "js:$it:${name.substringAfterLast('.')}" }
                    .firstOrNull { entries.has(it) } ?: return null
            }
        }
        val entry = entries.optJSONObject(key) ?: return null
        val standard = when {
            key.startsWith("html:") -> "https://html.spec.whatwg.org/multipage/"
            key.startsWith("attribute:") -> "https://html.spec.whatwg.org/multipage/dom.html#attributes"
            key.startsWith("js:Array:") -> "https://tc39.es/ecma262/multipage/indexed-collections.html#sec-array.prototype.${name.lowercase()}"
            key.startsWith("js:String:") -> "https://tc39.es/ecma262/multipage/text-processing.html#sec-string.prototype.${name.lowercase()}"
            else -> ""
        }
        return CompletionDocumentation(
            description = entry.getString("description"),
            syntax = entry.optString("syntax").ifBlank { item.insertText },
            example = documentationExample(item),
            source = entry.optString("source"),
            standard = standard,
        )
    }
}

internal fun specialCompletionDocumentation(item: CompletionItem): CompletionDocumentation? {
    if (item.detail == "field") {
        val field = item.label.trim('"')
        val description = when (field) {
            "name" -> "In package.json, names the npm package. Published package names must be lowercase and may include an @scope prefix. In other JSON files, the meaning is defined by the application."
            "version" -> "In package.json, identifies the package release using semantic versioning, such as 1.0.0. In other JSON files, the meaning is defined by the application."
            "scripts" -> "In package.json, maps script names to commands. Run a named command with npm run <name>; npm start runs the start script."
            "dependencies" -> "In package.json, maps runtime package names to version ranges or other package specifiers. npm install uses these entries to install dependencies."
            else -> return null
        }
        val value = when (field) {
            "name" -> "\"my-web-app\""
            "version" -> "\"1.0.0\""
            "scripts" -> "{ \"start\": \"node server.js\" }"
            else -> "{ \"example-package\": \"^1.0.0\" }"
        }
        return CompletionDocumentation(description, "\"$field\": $value", "{\n  \"$field\": $value\n}",
            "https://docs.npmjs.com/cli/v11/configuring-npm/package-json/#$field")
    }
    if (item.label.equals("<!DOCTYPE html>", ignoreCase = true)) return CompletionDocumentation(
        "Declares an HTML document and makes browsers use standards mode instead of quirks mode. Place it before the document’s html element; it is a declaration, not an HTML element.",
        "<!DOCTYPE html>",
        "<!DOCTYPE html>\n<html lang=\"en\">\n<head><title>Hello</title></head>\n<body><p>Hello, world!</p></body>\n</html>",
        "https://html.spec.whatwg.org/multipage/syntax.html#the-doctype",
    )
    if (item.detail.contains("css") && item.label.startsWith("var(")) {
        val variable = item.label.substringAfter('(').substringBefore(')').substringBefore(',').trim()
        return CompletionDocumentation(
            "Substitutes the value of a CSS custom property into another property’s value. Custom property names begin with -- and are case-sensitive. An optional fallback is used when the referenced property is missing or has an invalid custom-property value.",
            "var(<custom-property-name>, <fallback>?)",
            ":root { $variable: #182126; }\n.card {\n  background-color: var($variable, #fff);\n}",
            "https://www.w3.org/TR/css-variables-1/#using-variables",
        )
    }
    return null
}

private fun documentationExample(item: CompletionItem): String {
    val name = item.label.substringBefore('(')
    return when ("${item.detail}:$name") {
        "array:map" -> "const doubled = [1, 2, 3].map(n => n * 2);\n// [2, 4, 6]"
        "array:filter" -> "const large = [1, 3, 5].filter(n => n > 2);\n// [3, 5]"
        "array:reduce" -> "const sum = [1, 2, 3].reduce((total, n) => total + n, 0);\n// 6"
        "array:forEach" -> "['Ada', 'Lin'].forEach(name => {\n  console.log(name);\n});"
        "array:find" -> "const found = [1, 3, 5].find(n => n > 2);\n// 3"
        "string:trim" -> "const name = '  Ada  '.trim();\n// 'Ada'"
        "string:includes" -> "'Hello, Ada'.includes('Ada');\n// true"
        "number:toFixed" -> "const price = (12.5).toFixed(2);\n// '12.50'"
        "dom:querySelector" -> "const card = document.querySelector('.card');\nif (card) card.textContent = 'Hello';"
        "dom:addEventListener" -> "document.addEventListener('click', event => {\n  console.log(event.target);\n});"
        "css property:display" -> ".cards {\n  display: flex;\n  gap: 1rem;\n}"
        else -> ""
    }
}
