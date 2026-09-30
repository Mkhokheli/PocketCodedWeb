package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ProjectSemanticIndexerTest {
    @Test fun skipsLongCommentsAndEscapedStringsWithoutLosingOffsets() {
        val root = Files.createTempDirectory("semantic-comments").toFile()
        try {
            val source = "/*" + "ignored ".repeat(10_000) + "*/\n" +
                "const actual = \"escaped \\\" ignored\"; // ignored\nactual;\n"
            root.resolve("main.js").writeText(source)
            val snapshot = ProjectSemanticIndexer.build(root)
            assertTrue(snapshot.occurrences["ignored"].isNullOrEmpty())
            assertEquals(1, snapshot.declarations("actual").size)
            assertEquals(1, snapshot.references("actual").size)
            assertEquals(source.lastIndexOf("actual"), snapshot.references("actual").single().offset)
            assertEquals(3, snapshot.references("actual").single().line)
        } finally { root.deleteRecursively() }
    }

    @Test
    fun indexesFrameworksDeclarationsAndReferencesWithoutDependencies() {
        val root = Files.createTempDirectory("semantic-index").toFile()
        try {
            root.resolve("package.json").writeText("""{"dependencies":{"react":"latest","next":"latest"}}""")
            root.resolve("page.tsx").writeText("""
                import React from "react";
                const greeting = "hello";
                export function Page() { return <p>{greeting}</p>; }
            """.trimIndent())
            root.resolve("node_modules/noise.js").apply { parentFile.mkdirs(); writeText("const greeting = 1") }

            val index = ProjectSemanticIndexer.build(root)

            assertTrue(WebFramework.React in index.frameworks)
            assertTrue(WebFramework.NextJs in index.frameworks)
            assertEquals(2, index.indexedFiles)
            assertEquals(1, index.declarations("greeting").size)
            assertEquals(1, index.references("greeting").size)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun projectRenameIsValidatedAndAppliedAcrossFiles() {
        val root = Files.createTempDirectory("semantic-rename").toFile()
        try {
            val first = root.resolve("first.ts").apply { writeText("export const total = 2;\n") }
            val second = root.resolve("second.ts").apply { writeText("import { total } from './first';\nconsole.log(total);\n") }
            val index = ProjectSemanticIndexer.build(root)
            val plan = ProjectSemanticIndexer.rename(index, "total", "count")

            assertTrue(plan.valid)
            assertEquals(3, plan.edits.size)
            assertEquals(3, ProjectSemanticIndexer.apply(plan).getOrThrow())
            assertTrue("count" in first.readText())
            assertFalse("total" in second.readText())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun projectRenameRejectsDeclarationCollisions() {
        val root = Files.createTempDirectory("semantic-collision").toFile()
        try {
            root.resolve("index.ts").writeText("const first = 1;\nconst second = first;\n")
            val plan = ProjectSemanticIndexer.rename(ProjectSemanticIndexer.build(root), "first", "second")
            assertFalse(plan.valid)
            assertTrue(plan.error.orEmpty().contains("already declared"))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun infersDeclaredAndLiteralTypesAndResolvesModules() {
        val root = Files.createTempDirectory("typescript-analysis").toFile()
        try {
            root.resolve("model.ts").writeText("export const answer: number = 42;\n")
            root.resolve("app.ts").writeText(
                "import { answer } from './model';\nconst title = 'PocketCoded';\nconst nodes = document.querySelectorAll('.item');\n",
            )
            val analysis = TypeScriptProjectAnalyzer.analyze(ProjectSemanticIndexer.build(root))
            assertEquals("number", analysis.types.getValue("answer").first().type)
            assertEquals("string", analysis.types.getValue("title").first().type)
            assertEquals("NodeListOf<Element>", analysis.types.getValue("nodes").first().type)
            assertTrue(analysis.modules.any { it.specifier == "./model" && it.resolvedFile?.name == "model.ts" })
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun reportsFrameworkScriptsAndUnresolvedImports() {
        val root = Files.createTempDirectory("framework-analysis").toFile()
        try {
            root.resolve("package.json").writeText("""{"scripts":{"test":"vitest","build":"next build"},"dependencies":{"next":"latest","react":"latest"}}""")
            root.resolve("page.tsx").writeText("import missing from './missing';\nexport const Page = () => <main />;\n")
            val analysis = TypeScriptProjectAnalyzer.analyze(ProjectSemanticIndexer.build(root))
            assertEquals("vitest", analysis.scripts["test"])
            assertTrue(analysis.frameworkNotes.any { it.startsWith("Next.js:") })
            assertTrue(analysis.frameworkNotes.any { it.startsWith("Warning:") })
        } finally {
            root.deleteRecursively()
        }
    }
}
