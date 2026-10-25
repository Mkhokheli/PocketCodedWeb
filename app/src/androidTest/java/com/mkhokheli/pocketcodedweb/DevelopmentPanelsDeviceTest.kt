package com.mkhokheli.pocketcodedweb

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DevelopmentPanelsDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun debuggerPanelStepsInspectsAndNavigates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NativeNodeRuntime.initialize(context)
        val root = File(context.filesDir, "debug-panel-test").apply { mkdirs() }
        val file = File(root, "entry.cjs").apply { writeText("let answer = 40;\nanswer += 2;\nconsole.log(answer);\n") }
        val controller = WebDevelopmentController(context, root)
        val source = AtomicReference<Pair<String, Int>>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val actions = actions(root, controller, source, scope)
        try {
            compose.setContent { MaterialTheme { CompositionLocalProvider(LocalWebDevelopmentActions provides actions) { WebDevelopmentPanels(controller) } } }
            NodeSessionRuntime.stop()
            val started = NodeSessionRuntime.start(root, root, file, emptyList(), mode = "debug")
            assertTrue(started.toString(), started.error.isBlank())
            compose.runOnIdle { controller.panel = "Debugger" }
            compose.waitUntil(15_000) { compose.onAllNodesWithText("Debugger - paused").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Over").performClick()
            compose.waitUntil(10_000) { controller.debugState.optJSONArray("stack")?.optJSONObject(0)?.optInt("line") == 2 }
            compose.onAllNodes(hasScrollToIndexAction()).onFirst().performScrollToNode(hasText("answer = 40", substring = true))
            compose.onNodeWithText("answer = 40", substring = true).assertExists()
            compose.onAllNodes(hasScrollToIndexAction()).onFirst().performScrollToNode(hasText("Source"))
            compose.onAllNodesWithText("Source").onFirst().performClick()
            compose.runOnIdle {
                assertEquals(file.canonicalPath, File(source.get().first).canonicalPath)
                assertEquals(2, source.get().second)
                controller.panel = "Debugger"
            }
            compose.onNodeWithText("Continue").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Debugger - exited").fetchSemanticsNodes().isNotEmpty() }
        } finally { scope.cancel(); NodeSessionRuntime.stop(); root.deleteRecursively() }
    }

    @Test fun visualTestPanelShowsFailuresNavigatesAndRerunsFixedFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NativeNodeRuntime.initialize(context)
        val root = File(context.filesDir, "test-panel-test").apply { mkdirs() }
        val file = File(root, "math.test.cjs").apply { writeText(testSource(false)) }
        val controller = WebDevelopmentController(context, root)
        val source = AtomicReference<Pair<String, Int>>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val actions = actions(root, controller, source, scope)
        try {
            compose.setContent { MaterialTheme { CompositionLocalProvider(LocalWebDevelopmentActions provides actions) { WebDevelopmentPanels(controller) } } }
            NodeSessionRuntime.stop()
            NodeSessionRuntime.start(root, root, file, emptyList(), mode = "tests", testFiles = listOf(file))
            compose.runOnIdle { controller.panel = "Tests" }
            compose.waitUntil(15_000) { compose.onAllNodesWithText("1 passed / 1 failed / 1 skipped").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("FAILED: fails").performClick()
            compose.onNodeWithText("Show source").performScrollTo().performClick()
            compose.runOnIdle {
                assertEquals(file.canonicalPath, File(source.get().first).canonicalPath)
                assertEquals(4, source.get().second)
                controller.panel = "Tests"
            }
            file.writeText(testSource(true))
            compose.onNodeWithText("Rerun failed").performClick()
            compose.waitUntil(15_000) { compose.onAllNodesWithText("2 passed / 0 failed / 1 skipped").fetchSemanticsNodes().isNotEmpty() }
        } finally { scope.cancel(); NodeSessionRuntime.stop(); root.deleteRecursively() }
    }

    private fun testSource(fixed: Boolean) = "const test = require('node:test');\nconst assert = require('node:assert/strict');\ntest('passes', () => assert.equal(1,1));\ntest('fails', () => assert.equal(1,${if (fixed) 1 else 2}));\ntest.skip('skipped', () => {});\n"

    private fun actions(root: File, controller: WebDevelopmentController, source: AtomicReference<Pair<String, Int>>, scope: CoroutineScope) =
        WebDevelopmentActions(ProjectFileStore(root), controller,
            { path, line -> source.set(path to line); controller.panel = null },
            { mode, path, tests ->
                controller.starting = true
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        NodeSessionRuntime.stop()
                        NodeSessionRuntime.start(root, root, File(path), emptyList(), mode = mode, testFiles = tests.map(::File))
                    }
                    controller.error = result.error.takeIf { it.isNotBlank() }
                    controller.starting = false
                }
            }, { _, done -> done(null) }, { _, done -> done(null) }, {}, emptyList(), {}, { emptyList() }, {}, {})
}
