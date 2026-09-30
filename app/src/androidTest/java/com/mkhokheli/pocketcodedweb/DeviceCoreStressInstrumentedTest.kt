package com.mkhokheli.pocketcodedweb

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.Locale
import kotlin.system.measureTimeMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceCoreStressInstrumentedTest {
    private lateinit var root: File

    @Before fun setup() {
        val projects = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "projects").apply { mkdirs() }
        createDeviceProjectFixtures(projects)
        root = File(projects, "StressLab")
        assertTrue("StressLab device fixture is missing", root.isDirectory)
    }

    @Test fun representativeProjectsLoadAndDiagnoseReliably() {
        val projects = root.parentFile!!
        val expectations = mapOf(
            "BeginnerPortfolio" to false,
            "JuniorDashboard" to false,
            "EnterpriseConsole" to false,
            "BrokenLab" to true,
        )
        expectations.forEach { (name, shouldHaveErrors) ->
            val project = File(projects, name)
            val elapsed = measureTimeMillis {
                val files = loadWebFiles(project)
                assertTrue("$name should contain editable files", files.isNotEmpty())
                val errors = files.flatMap { file ->
                    inspectWebSource(file.name, file.content).filter { it.severity.lowercase(Locale.US) == "error" }
                }
                if (shouldHaveErrors) assertTrue("$name should expose broken-code diagnostics", errors.isNotEmpty())
                else assertTrue("$name should not contain syntax errors: $errors", errors.isEmpty())
            }
            Log.i("PocketCodedStress", "fixture=$name load-and-diagnose-ms=$elapsed")
            assertTrue("$name load/diagnostics is too slow: $elapsed", elapsed < 5_000)
        }
    }

    @Test fun semanticIndexColdAndWarmPerformance() {
        ProjectSemanticIndexer.invalidate(root)
        lateinit var snapshot: SemanticIndexSnapshot
        val cold = measureTimeMillis { snapshot = ProjectSemanticIndexer.build(root) }
        val warm = (1..20).map { measureTimeMillis { ProjectSemanticIndexer.build(root) } }
        Log.i("PocketCodedStress", "semantic-index cold-ms=$cold warm-ms=$warm files=${snapshot.indexedFiles}")
        assertTrue(snapshot.indexedFiles <= 4_000)
        assertTrue("Warm semantic index is too slow: $warm", warm.average() < 20.0)
    }

    @Test fun projectLoadPerformanceAndHeap() {
        val runtime = Runtime.getRuntime()
        val before = runtime.totalMemory() - runtime.freeMemory()
        lateinit var files: List<WebFile>
        val elapsed = measureTimeMillis { files = loadWebFiles(root) }
        val after = runtime.totalMemory() - runtime.freeMemory()
        Log.i("PocketCodedStress", "load-files=${files.size} elapsed-ms=$elapsed heap-delta=${after - before}")
        // The deliberately >1 MB TypeScript file is excluded by the editor safety limit.
        assertEquals(2_507, files.size)
        assertTrue("Project load is too slow: $elapsed", elapsed < 30_000)
        assertTrue("Project load retained too much heap", after - before < 96L * 1024L * 1024L)
    }

    @Test fun nativeNodeInfiniteLoopTimeoutAndRecovery() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NativeNodeRuntime.initialize(context)
        val timed = NativeNodeRuntime.evaluate(root, root, "while (true) {}", 750)
        Log.i("PocketCodedStress", "native-loop exit=${timed.exitCode} timedOut=${timed.timedOut} stdout=${timed.stdout.take(80)} stderr=${timed.stderr.take(160)}")
        assertTrue(timed.stderr, timed.timedOut)
        assertEquals(124, timed.exitCode)
        val recovered = NativeNodeRuntime.evaluate(root, root, "console.log(6 * 7)", 5_000)
        Log.i("PocketCodedStress", "native-timeout=${timed.exitCode} recovery=${recovered.stdout.trim()}")
        assertTrue(recovered.stderr, recovered.succeeded)
        assertTrue(recovered.stdout.contains("42"))
    }


    private fun createDeviceProjectFixtures(projects: File) {
        createSmallFixture(
            File(projects, "BeginnerPortfolio"),
            mapOf(
                "index.html" to """<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"><link rel=\"stylesheet\" href=\"style.css\"></head><body><main><h1>My Portfolio</h1><button id=\"hello\">Say hello</button></main><script src=\"app.js\"></script></body></html>""",
                "style.css" to "body { font-family: sans-serif; margin: 0; padding: 2rem; } main { max-width: 48rem; margin: auto; }",
                "app.js" to "document.querySelector('#hello').addEventListener('click', () => console.log('Hello'));",
            ),
        )
        createSmallFixture(
            File(projects, "JuniorDashboard"),
            mapOf(
                "index.html" to """<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"><link rel=\"stylesheet\" href=\"src/style.css\"></head><body><div id=\"app\"></div><script type=\"module\" src=\"src/app.js\"></script></body></html>""",
                "src/style.css" to ":root { color-scheme: dark; } body { margin: 0; font-family: sans-serif; } .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr)); gap: 1rem; padding: 1rem; }",
                "src/app.js" to "const metrics = [12, 27, 41]; document.querySelector('#app').innerHTML = `<main class=\"grid\">${'$'}{metrics.map(value => `<article>Metric: ${'$'}{value}</article>`).join('')}</main>`;",
                "package.json" to """{"name":"junior-dashboard","private":true,"scripts":{"dev":"preview","build":"preview"}}""",
            ),
        )
        createSmallFixture(
            File(projects, "EnterpriseConsole"),
            mapOf(
                "index.html" to """<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"></head><body><div id=\"root\"></div><script type=\"module\" src=\"src/main.tsx\"></script></body></html>""",
                "src/main.tsx" to "type Tenant = { id: string; name: string }; const tenants: Tenant[] = [{ id: 'acme', name: 'Acme' }]; document.querySelector('#root')!.textContent = tenants.map(tenant => tenant.name).join(', ');",
                "src/api/client.ts" to "export async function request<T>(url: string): Promise<T> { const response = await fetch(url); if (!response.ok) throw new Error(`HTTP ${'$'}{response.status}`); return response.json() as Promise<T>; }",
                "src/state/store.ts" to "export type State = { tenantId: string | null }; export const state: State = { tenantId: null };",
                "db/schema.sql" to "CREATE TABLE tenants (id TEXT PRIMARY KEY, name TEXT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);",
                "server/index.js" to "export function health() { return { ok: true }; }",
                "server/report.py" to "def summarize(values):\n    return sum(values) / len(values) if values else 0\n",
                "server/legacy.php" to "<?php function health(): array { return ['ok' => true]; } ?>",
                "package.json" to """{"name":"enterprise-console","private":true,"scripts":{"dev":"preview","test":"lint","build":"preview"},"dependencies":{"react":"latest","next":"latest"}}""",
            ),
        )
        createSmallFixture(
            File(projects, "BrokenLab"),
            mapOf(
                "index.html" to "<!DOCTYPE html><html><head></head><body><main><div></main></body></html>",
                "broken.js" to "function broken(value] { return { value; }",
                "broken.css" to ".card { color: red; color: blue; transform: translateX(10px; }",
                "broken.py" to "def broken(value]:\n    return value\n",
                "broken.sql" to "SELECT (name] FROM users;",
            ),
        )

        val stress = File(projects, "StressLab")
        val marker = File(stress, ".fixture-v2")
        if (!marker.isFile) {
            stress.deleteRecursively()
            val modules = File(stress, "src/modules").apply { mkdirs() }
            repeat(2_500) { index ->
                File(modules, "module$index.ts").writeText("export const value$index: number = $index;\n")
            }
            File(stress, "index.html").writeText("<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"></head><body><script type=\"module\" src=\"src/main.ts\"></script></body></html>")
            File(stress, "src/main.ts").writeText("import { value1 } from './modules/module1'; console.log(value1);\n")
            File(stress, "src/style.css").writeText("body { margin: 0; font-family: sans-serif; }\n")
            File(stress, "package.json").writeText("{\"name\":\"stress-lab\",\"private\":true}")
            File(stress, "README.md").writeText("# Stress Lab\n")
            File(stress, "query.sql").writeText("SELECT 1;\n")
            File(stress, "worker.py").writeText("print(sum(range(1000)))\n")
            File(stress, "oversized.ts").writeText("const payload = '${"x".repeat(1_100_000)}';\n")
            marker.writeText("2")
        }
    }

    private fun createSmallFixture(directory: File, files: Map<String, String>) {
        directory.mkdirs()
        files.forEach { (path, content) ->
            File(directory, path).apply { parentFile?.mkdirs(); writeText(content) }
        }
    }
}
