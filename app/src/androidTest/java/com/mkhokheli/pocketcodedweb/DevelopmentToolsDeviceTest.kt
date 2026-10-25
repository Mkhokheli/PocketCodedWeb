package com.mkhokheli.pocketcodedweb

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DevelopmentToolsDeviceTest {
    private fun waitFor(root: File, mode: String, predicate: (JSONObject) -> Boolean): JSONObject {
        val deadline = android.os.SystemClock.elapsedRealtime() + 15_000
        var snapshot = JSONObject()
        while (android.os.SystemClock.elapsedRealtime() < deadline) {
            snapshot = NodeSessionRuntime.developmentState(root, mode)
            if (predicate(snapshot)) return snapshot
            Thread.sleep(100)
        }
        fail("Missing $mode state: $snapshot; ${NodeSessionRuntime.status()}")
        return snapshot
    }

    @Test fun debuggerAndVisualTestsOnEmbeddedNode() {
        ActivityScenario.launch(MainActivity::class.java).use {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            NativeNodeRuntime.initialize(context)
            val root = File(context.filesDir, "development-tools-device-test").apply { mkdirs() }
            try {
                NodeSessionRuntime.stop()
                val file = File(root, "entry.cjs").apply { writeText("let answer = 40;\nanswer += 2;\nconsole.log(answer);\n") }
                val started = NodeSessionRuntime.start(root, root, file, emptyList(), mode = "debug")
                assertTrue(started.toString(), started.error.isBlank())
                val pause = waitFor(root, "debug") { it.optString("state") == "paused" && it.optJSONArray("stack") != null }
                assertEquals(file.canonicalPath, File(pause.getJSONArray("stack").getJSONObject(0).getString("file")).canonicalPath)
                NodeSessionRuntime.debugCommand(root, "over").getOrThrow()
                val next = waitFor(root, "debug") { it.optString("state") == "paused" && it.optJSONArray("stack")?.optJSONObject(0)?.optInt("line") == 2 }
                assertTrue(next.toString(), next.getJSONArray("variables").toString().contains("answer"))
                NodeSessionRuntime.debugCommand(root, "breakpoints", breakpoints = listOf(file.path to 3)).getOrThrow()
                NodeSessionRuntime.debugCommand(root, "continue").getOrThrow()
                val atBreakpoint = waitFor(root, "debug") { it.optString("state") == "paused" && it.optJSONArray("stack")?.optJSONObject(0)?.optInt("line") == 3 }
                assertTrue(atBreakpoint.toString(), atBreakpoint.getJSONArray("variables").toString().contains("42"))
                NodeSessionRuntime.debugCommand(root, "inspect", frame = 0).getOrThrow()
                NodeSessionRuntime.debugCommand(root, "continue").getOrThrow()
                waitFor(root, "debug") { it.optString("state") == "exited" }
                NodeSessionRuntime.stop()
                file.writeText("const test = require('node:test'); const assert = require('node:assert/strict');\ntest('passes', () => assert.equal(1,1));\ntest('fails', () => assert.equal(1,2));\ntest.skip('skipped', () => {});\n")
                val tests = NodeSessionRuntime.start(root, root, file, emptyList(), mode = "tests", testFiles = listOf(file))
                assertTrue(tests.toString(), tests.error.isBlank() || tests.error == "Node session failed. See the output above.")
                val results = parseWebTestResults(waitFor(root, "tests") { it.optString("state") == "failed" && it.optJSONArray("results")?.length() == 3 })
                assertEquals(setOf("passed", "failed", "skipped"), results.map { it.status }.toSet())
                assertTrue(results.any { it.name == "fails" && it.line == 3 })
            } finally {
                NodeSessionRuntime.stop()
                root.deleteRecursively()
            }
        }
    }

    @Test fun esmDebuggerCanPauseStopAndRestart() {
        ActivityScenario.launch(MainActivity::class.java).use {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            NativeNodeRuntime.initialize(context)
            val root = File(context.filesDir, "esm-debug-device-test").apply { mkdirs() }
            try {
                NodeSessionRuntime.stop()
                val file = File(root, "entry.mjs").apply { writeText("let total = 1;\ntotal += 1;\nsetInterval(() => { total += 1; }, 25);\n") }
                repeat(2) {
                    assertTrue(NodeSessionRuntime.start(root, root, file, emptyList(), mode = "debug").error.isBlank())
                    waitFor(root, "debug") { it.optString("state") == "paused" && it.optJSONArray("stack") != null }
                    NodeSessionRuntime.debugCommand(root, "continue").getOrThrow()
                    waitFor(root, "debug") { it.optString("state") == "running" }
                    NodeSessionRuntime.debugCommand(root, "pause").getOrThrow()
                    waitFor(root, "debug") { it.optString("state") == "paused" }
                    assertTrue(NodeSessionRuntime.stop(force = true).error.isBlank())
                    assertFalse(NodeSessionRuntime.isRunningFor(root))
                }
            } finally { NodeSessionRuntime.stop(); root.deleteRecursively() }
        }
    }
}
