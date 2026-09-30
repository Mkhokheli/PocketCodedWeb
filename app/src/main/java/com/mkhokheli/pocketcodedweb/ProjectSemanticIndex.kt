package com.mkhokheli.pocketcodedweb

import java.io.File
import java.util.Locale

internal enum class WebFramework { React, Vue, Angular, NextJs, Svelte, Vanilla }

internal data class SemanticOccurrence(
    val name: String,
    val file: File,
    val offset: Int,
    val line: Int,
    val column: Int,
    val declaration: Boolean,
    val kind: String,
)

internal data class SemanticIndexSnapshot(
    val root: File,
    val occurrences: Map<String, List<SemanticOccurrence>>,
    val frameworks: Set<WebFramework>,
    val indexedFiles: Int,
    val indexedBytes: Long,
    val skippedFiles: Int,
) {
    fun declarations(name: String) = occurrences[name].orEmpty().filter(SemanticOccurrence::declaration)
    fun references(name: String) = occurrences[name].orEmpty().filterNot(SemanticOccurrence::declaration)
}

internal data class SemanticRenameEdit(
    val file: File,
    val offset: Int,
    val oldText: String,
    val newText: String,
)

internal data class SemanticRenamePlan(
    val oldName: String,
    val newName: String,
    val edits: List<SemanticRenameEdit>,
    val error: String? = null,
) {
    val valid: Boolean get() = error == null && edits.isNotEmpty()
}

/** A bounded, allocation-conscious index for mobile project-wide navigation and refactors. */
internal object ProjectSemanticIndexer {
    private const val MAX_FILES = 4_000
    private const val MAX_FILE_BYTES = 1_000_000L
    private const val MAX_TOTAL_BYTES = 24_000_000L
    private val supportedExtensions = setOf(
        "js", "jsx", "mjs", "cjs", "ts", "tsx", "vue", "svelte", "html", "htm", "css",
        "json", "md", "java", "kt", "kts", "py", "rb", "php", "go", "rs", "swift", "dart",
    )
    private val ignoredDirectories = setOf(
        ".git", ".gradle", ".idea", ".next", ".nuxt", ".angular", "node_modules", "build", "dist", "out",
    )
    private val identifier = Regex("[A-Za-z_$][A-Za-z0-9_$]*")
    private val declarationPrefix = Regex(
        "(?:^|[;{}\\n]\\s*)(?:export\\s+)?(?:default\\s+)?(?:async\\s+)?" +
            "(?:const|let|var|function|class|interface|type|enum|namespace|import)\\s*$",
    )
    private data class CachedSnapshot(val rootPath: String, val snapshot: SemanticIndexSnapshot)
    @Volatile private var cachedSnapshot: CachedSnapshot? = null

    fun build(rootDirectory: File): SemanticIndexSnapshot {
        val root = rootDirectory.canonicalFile
        cachedSnapshot?.takeIf { it.rootPath == root.path }?.let { return it.snapshot }
        val byName = LinkedHashMap<String, MutableList<SemanticOccurrence>>()
        var files = 0
        var bytes = 0L
        var skipped = 0
        val frameworkEvidence = LinkedHashSet<WebFramework>()

        val candidates = root.walkTopDown()
            .onEnter { directory -> directory == root || directory.name !in ignoredDirectories }
            .filter(File::isFile)
            .filter { it.extension.lowercase(Locale.US) in supportedExtensions || it.name == "package.json" }

        for (file in candidates) {
            if (files >= MAX_FILES || bytes >= MAX_TOTAL_BYTES) { skipped++; continue }
            val length = file.length()
            if (length < 0L || length > MAX_FILE_BYTES || bytes + length > MAX_TOTAL_BYTES) { skipped++; continue }
            val source = runCatching { file.readText() }.getOrElse { skipped++; continue }
            files++
            bytes += length
            detectFrameworks(file, source, frameworkEvidence)
            indexSource(file, source, byName)
        }
        if (frameworkEvidence.isEmpty()) frameworkEvidence += WebFramework.Vanilla
        return SemanticIndexSnapshot(root, byName, frameworkEvidence, files, bytes, skipped).also { snapshot ->
            cachedSnapshot = CachedSnapshot(root.path, snapshot)
        }
    }

    fun invalidate(rootDirectory: File? = null) {
        val rootPath = rootDirectory?.let { runCatching { it.canonicalPath }.getOrNull() }
        if (rootPath == null || cachedSnapshot?.rootPath == rootPath) cachedSnapshot = null
    }

    fun rename(snapshot: SemanticIndexSnapshot, oldName: String, newName: String): SemanticRenamePlan {
        if (!identifier.matches(oldName)) return SemanticRenamePlan(oldName, newName, emptyList(), "Invalid source identifier '$oldName'.")
        if (!identifier.matches(newName)) return SemanticRenamePlan(oldName, newName, emptyList(), "Invalid replacement identifier '$newName'.")
        if (oldName == newName) return SemanticRenamePlan(oldName, newName, emptyList(), "The names are identical.")
        val occurrences = snapshot.occurrences[oldName].orEmpty()
        if (occurrences.isEmpty()) return SemanticRenamePlan(oldName, newName, emptyList(), "No project symbol named '$oldName' was found.")
        val conflictingDeclarations = snapshot.declarations(newName)
        if (conflictingDeclarations.isNotEmpty()) {
            val conflict = conflictingDeclarations.first()
            return SemanticRenamePlan(oldName, newName, emptyList(), "'$newName' is already declared in ${conflict.file.name}:${conflict.line}.")
        }
        return SemanticRenamePlan(
            oldName,
            newName,
            occurrences.map { SemanticRenameEdit(it.file, it.offset, oldName, newName) },
        )
    }

    fun apply(plan: SemanticRenamePlan): Result<Int> = runCatching {
        check(plan.valid) { plan.error ?: "The rename plan is empty." }
        val editsByFile = plan.edits.groupBy(SemanticRenameEdit::file)
        val originals = LinkedHashMap<File, String>()
        val replacements = LinkedHashMap<File, String>()
        editsByFile.forEach { (file, edits) ->
            val original = file.readText()
            originals[file] = original
            var updated = original
            edits.sortedByDescending(SemanticRenameEdit::offset).forEach { edit ->
                require(edit.offset >= 0 && edit.offset + edit.oldText.length <= updated.length) { "Stale index for ${file.name}." }
                require(updated.regionMatches(edit.offset, edit.oldText, 0, edit.oldText.length)) { "Source changed in ${file.name}; rebuild the index." }
                updated = updated.replaceRange(edit.offset, edit.offset + edit.oldText.length, edit.newText)
            }
            replacements[file] = updated
        }
        val written = mutableListOf<File>()
        try {
            replacements.forEach { (file, text) -> file.writeText(text); written += file }
        } catch (error: Throwable) {
            written.forEach { file -> runCatching { file.writeText(originals.getValue(file)) } }
            throw error
        }
        plan.edits.size
    }

    private fun indexSource(file: File, source: String, target: MutableMap<String, MutableList<SemanticOccurrence>>) {
        val lineStarts = ArrayList<Int>().apply {
            add(0)
            source.forEachIndexed { index, char -> if (char == '\n') add(index + 1) }
        }
        // Scan once, skipping comments and strings before allocating identifier matches.
        // Large generated files often contain far more documentation than executable code.
        var offset = 0
        while (offset < source.length) {
            val char = source[offset]
            val next = source.getOrNull(offset + 1)
            if (char == '/' && next == '/') {
                offset = source.indexOf('\n', offset + 2).let { if (it < 0) source.length else it }
                continue
            }
            if (char == '/' && next == '*') {
                offset = source.indexOf("*/", offset + 2).let { if (it < 0) source.length else it + 2 }
                continue
            }
            if (char == '\'' || char == '"' || char == '`') {
                val quote = char
                offset++
                while (offset < source.length) {
                    val current = source[offset++]
                    if (current == '\\') { if (offset < source.length) offset++ }
                    else if (current == quote) break
                }
                continue
            }
            if (!(char in 'A'..'Z' || char in 'a'..'z' || char == '_' || char == '$')) {
                offset++
                continue
            }
            val start = offset++
            while (offset < source.length) {
                val current = source[offset]
                if (!(current in 'A'..'Z' || current in 'a'..'z' || current in '0'..'9' || current == '_' || current == '$')) break
                offset++
            }
            val name = source.substring(start, offset)
            val lineIndex = lineStarts.binarySearch(start).let { if (it >= 0) it else -it - 2 }.coerceAtLeast(0)
            val prefixStart = maxOf(lineStarts[lineIndex], start - 120)
            val prefix = source.substring(prefixStart, start)
            val declaration = declarationPrefix.find(prefix)
            val kind = declaration?.value?.trim()?.substringAfterLast(' ') ?: "reference"
            target.getOrPut(name) { ArrayList() } += SemanticOccurrence(
                name, file, start, lineIndex + 1, start - lineStarts[lineIndex] + 1, declaration != null, kind,
            )
        }
    }

    private fun detectFrameworks(file: File, source: String, result: MutableSet<WebFramework>) {
        val lower = source.lowercase(Locale.US)
        when {
            file.name == "next.config.js" || file.name == "next.config.mjs" || "\"next\"" in lower -> result += WebFramework.NextJs
            file.extension.equals("vue", true) || "\"vue\"" in lower -> result += WebFramework.Vue
            "@angular/core" in lower || "angular.json" == file.name -> result += WebFramework.Angular
            file.extension.equals("svelte", true) || "\"svelte\"" in lower -> result += WebFramework.Svelte
        }
        if (file.extension.lowercase(Locale.US) in setOf("jsx", "tsx") || "\"react\"" in lower || "from 'react'" in lower) result += WebFramework.React
    }
}

internal data class TypeScriptTypeFact(
    val name: String,
    val type: String,
    val confidence: String,
    val file: File,
    val line: Int,
)

internal data class ModuleDependency(
    val sourceFile: File,
    val specifier: String,
    val resolvedFile: File?,
)

internal data class TypeScriptProjectAnalysis(
    val types: Map<String, List<TypeScriptTypeFact>>,
    val modules: List<ModuleDependency>,
    val scripts: Map<String, String>,
    val frameworkNotes: List<String>,
)

/** Lightweight inference used immediately; a connected tsserver/LSP remains authoritative. */
internal object TypeScriptProjectAnalyzer {
    private val sourceExtensions = setOf("js", "jsx", "mjs", "cjs", "ts", "tsx")
    private val typedDeclaration = Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)\s*:\s*([^=;\n]+)""")
    private val inferredDeclaration = Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)\s*=\s*([^;\n]+)""")
    private val functionDeclaration = Regex("""\b(?:export\s+)?(?:async\s+)?function\s+([A-Za-z_$][\w$]*)\s*\([^)]*\)\s*(?::\s*([^\s{]+))?""")
    private val classLikeDeclaration = Regex("""\b(?:export\s+)?(?:class|interface|type|enum)\s+([A-Za-z_$][\w$]*)""")
    private val importPattern = Regex("""\b(?:import|export)\s+(?:[^'\"]*?\s+from\s*)?['\"]([^'\"]+)['\"]|\brequire\(\s*['\"]([^'\"]+)['\"]\s*\)""")

    fun analyze(snapshot: SemanticIndexSnapshot): TypeScriptProjectAnalysis {
        val facts = LinkedHashMap<String, MutableList<TypeScriptTypeFact>>()
        val modules = ArrayList<ModuleDependency>()
        val sourceFiles = snapshot.occurrences.values.asSequence().flatten().map { it.file }.distinct()
            .filter { it.extension.lowercase(Locale.US) in sourceExtensions }.take(4_000).toList()
        sourceFiles.forEach { file ->
            val source = runCatching { file.readText() }.getOrNull() ?: return@forEach
            typedDeclaration.findAll(source).forEach { match ->
                addFact(facts, match.groupValues[1], match.groupValues[2].trim(), "declared", file, source, match.range.first)
            }
            inferredDeclaration.findAll(source).forEach { match ->
                val name = match.groupValues[1]
                if (facts[name].orEmpty().any { it.file == file }) return@forEach
                addFact(facts, name, inferExpressionType(match.groupValues[2]), "inferred", file, source, match.range.first)
            }
            functionDeclaration.findAll(source).forEach { match ->
                val returnType = match.groupValues[2].ifBlank {
                    if (source.substring(match.range.last + 1).trimStart().startsWith("{")) "function" else "unknown"
                }
                addFact(facts, match.groupValues[1], returnType, if (match.groupValues[2].isBlank()) "structural" else "declared", file, source, match.range.first)
            }
            classLikeDeclaration.findAll(source).forEach { match ->
                addFact(facts, match.groupValues[1], match.value.trim().substringBefore(' '), "declared", file, source, match.range.first)
            }
            importPattern.findAll(source).forEach { match ->
                val specifier = match.groupValues[1].ifBlank { match.groupValues[2] }
                modules += ModuleDependency(file, specifier, resolveModule(snapshot.root, file, specifier))
            }
        }
        val packageJson = File(snapshot.root, "package.json")
        val scripts = runCatching { parsePackageScripts(packageJson.readText()) }.getOrDefault(emptyMap())
        return TypeScriptProjectAnalysis(
            facts,
            modules,
            scripts,
            frameworkNotes(snapshot, scripts, modules),
        )
    }

    private fun addFact(
        target: MutableMap<String, MutableList<TypeScriptTypeFact>>,
        name: String,
        type: String,
        confidence: String,
        file: File,
        source: String,
        offset: Int,
    ) {
        val line = source.take(offset).count { it == '\n' } + 1
        target.getOrPut(name) { ArrayList() } += TypeScriptTypeFact(name, type, confidence, file, line)
    }

    private fun inferExpressionType(raw: String): String {
        val expression = raw.trim()
        return when {
            expression.startsWith("\"") || expression.startsWith("'") || expression.startsWith("`") -> "string"
            expression.matches(Regex("[-+]?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?")) -> "number"
            expression == "true" || expression == "false" -> "boolean"
            expression == "null" -> "null"
            expression == "undefined" -> "undefined"
            expression.startsWith("[") -> "array"
            expression.startsWith("{") -> "object"
            expression.startsWith("async ") || expression.contains("=>") || expression.startsWith("function") -> "function"
            expression.startsWith("new ") -> expression.removePrefix("new ").substringBefore('(').trim().ifBlank { "object" }
            expression.startsWith("Promise.") -> "Promise"
            expression.startsWith("document.querySelectorAll") -> "NodeListOf<Element>"
            expression.startsWith("document.querySelector") -> "Element | null"
            else -> "unknown"
        }
    }

    private fun resolveModule(root: File, sourceFile: File, specifier: String): File? {
        if (!specifier.startsWith(".")) return File(root, "node_modules/${specifier.substringBefore('/')}").takeIf(File::exists)
        val base = File(sourceFile.parentFile, specifier)
        val candidates = buildList {
            add(base)
            sourceExtensions.forEach { add(File(base.path + ".$it")) }
            sourceExtensions.forEach { add(File(base, "index.$it")) }
        }
        val canonicalRoot = root.canonicalFile
        val rootPrefix = canonicalRoot.path + File.separator
        return candidates.firstOrNull { candidate ->
            runCatching {
                if (!candidate.isFile) return@runCatching false
                val path = candidate.canonicalFile.path
                path == canonicalRoot.path || path.startsWith(rootPrefix)
            }.getOrDefault(false)
        }?.canonicalFile
    }

    private fun frameworkNotes(
        snapshot: SemanticIndexSnapshot,
        scripts: Map<String, String>,
        modules: List<ModuleDependency>,
    ): List<String> = buildList {
        snapshot.frameworks.forEach { framework ->
            add(when (framework) {
                WebFramework.React -> "React: JSX/TSX and React module intelligence enabled."
                WebFramework.Vue -> "Vue: .vue project detected; connect Vue Language Tools for template-level types."
                WebFramework.Angular -> "Angular: Angular project detected; connect Angular Language Service for templates."
                WebFramework.NextJs -> "Next.js: App/Pages Router module patterns and npm scripts detected."
                WebFramework.Svelte -> "Svelte: .svelte project detected; connect the Svelte language server for templates."
                WebFramework.Vanilla -> "Vanilla Web: HTML, CSS, JavaScript and TypeScript intelligence enabled."
            })
        }
        if (scripts.isEmpty()) add("No package scripts detected.")
        val unresolvedRelative = modules.count { it.specifier.startsWith(".") && it.resolvedFile == null }
        if (unresolvedRelative > 0) add("Warning: $unresolvedRelative relative module import(s) could not be resolved.")
    }

    private fun parsePackageScripts(source: String): Map<String, String> {
        val body = Regex("""[\"]scripts[\"]\s*:\s*\{([^}]*)}""").find(source)?.groupValues?.get(1)
            ?: return emptyMap()
        return Regex("""[\"]([^\"]+)[\"]\s*:\s*[\"]((?:\\.|[^\"])*)[\"]""")
            .findAll(body)
            .associate { match -> match.groupValues[1] to match.groupValues[2].replace("\\\"", "\"").replace("\\\\", "\\") }
    }
}
