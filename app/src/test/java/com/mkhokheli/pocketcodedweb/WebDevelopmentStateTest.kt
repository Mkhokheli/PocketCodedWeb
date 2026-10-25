package com.mkhokheli.pocketcodedweb

import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WebDevelopmentStateTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun discoversProjectTestsAndExcludesGeneratedDependencies() {
        val root = temporary.newFolder()
        fun file(path: String) { File(root, path).apply { parentFile!!.mkdirs(); writeText("") } }
        file("math.test.js")
        file("tests/integration.mjs")
        file("__tests__/nested/async.cjs")
        file("normal.js")
        file("node_modules/package/other.test.js")
        file(".pocketcodedweb/copy.test.js")
        file("dist/generated.test.js")
        file("test.ts")
        assertEquals(setOf("math.test.js", "integration.mjs", "async.cjs"), discoverWebTestFiles(root).map { it.name }.toSet())
    }
}
