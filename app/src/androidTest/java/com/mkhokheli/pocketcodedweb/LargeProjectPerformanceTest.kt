package com.mkhokheli.pocketcodedweb

import android.util.Log
import android.os.Handler
import android.os.HandlerThread
import android.view.FrameMetrics
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Deterministic offline workload: 6,000 modules (~12 MB), a real Node build and HTTP server. */
@RunWith(AndroidJUnit4::class)
class LargeProjectPerformanceTest {
    @Test fun largeProjectLoadBuildAndServe() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(context.filesDir, "large-performance-fixture").apply { mkdirs() }
        val modules = File(root, "src").apply { mkdirs() }
        val padding = "/*" + "module documentation ".repeat(95) + "*/\n"
        repeat(6_000) { index ->
            val file = File(modules, "module$index.js")
            if (!file.exists()) file.writeText(padding + "globalThis.values.push($index);\n")
        }
        File(root, "package.json").writeText("""{"scripts":{"build":"node build.cjs","dev":"node server.cjs"}}""")
        File(root, "build.cjs").writeText("""
            const fs = require('fs'), path = require('path');
            const start = Date.now();
            fs.mkdirSync('dist', { recursive: true });
            const fd = fs.openSync('dist/app.js', 'w');
            fs.writeSync(fd, 'globalThis.values=[];\n');
            for (const name of fs.readdirSync('src')) fs.writeSync(fd, fs.readFileSync(path.join('src', name)));
            fs.writeSync(fd, 'document.body.textContent="Modules: "+values.length;');
            fs.closeSync(fd);
            fs.writeFileSync('dist/index.html', '<!doctype html><script src="/app.js" defer></script><body></body>');
            console.log('BUILD_MS=' + (Date.now() - start));
        """.trimIndent())
        File(root, "server.cjs").writeText("""
            const fs = require('fs');
            require('http').createServer((req, res) => {
                if(req.url === '/api') { res.end(JSON.stringify({modules:6000})); return; }
                const file = req.url === '/app.js' ? 'dist/app.js' : 'dist/index.html';
                res.setHeader('Content-Type', req.url === '/app.js' ? 'text/javascript' : 'text/html');
                fs.createReadStream(file).pipe(res);
            }).listen(0, '127.0.0.1');
        """.trimIndent())
        var files = emptyList<WebFile>()
        repeat(3) { run ->
            val elapsed = measureTimeMillis { files = loadWebFiles(root) }
            assertEquals(6_003, files.size)
            Log.i("PocketCodedPerf", "load run=$run ms=$elapsed chars=${files.sumOf { it.content.length.toLong() }}")
        }
        ProjectSemanticIndexer.invalidate(root)
        val indexMs = measureTimeMillis { assertEquals(4_000, ProjectSemanticIndexer.build(root).indexedFiles) }
        Log.i("PocketCodedPerf", "index ms=$indexMs")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand("am start -W -n ${context.packageName}/${MainActivity::class.java.name}").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        run {
            NativeNodeRuntime.initialize(context)
            try {
                NodeSessionRuntime.stop()
                val build = NativeNodeRuntime.runFile(root, root, File(root, "build.cjs"), emptyList(), 120_000)
                assertTrue(build.stderr, build.succeeded)
                Log.i("PocketCodedPerf", build.stdout.trim())
                assertTrue(File(root, "dist/app.js").length() > 10_000_000)
                var address: String? = null
                val startMs = measureTimeMillis {
                    val started = executeWebShellCommand("npm run dev", root, root)
                    assertTrue(started.error, started.error.isBlank())
                    address = NodeSessionRuntime.awaitPreviewUrl()
                }
                assertNotNull(address)
                val samples = (1..30).map {
                    measureTimeMillis {
                        val connection = URL(address!! + "api").openConnection() as HttpURLConnection
                        connection.connectTimeout = 5_000
                        connection.readTimeout = 5_000
                        try { assertEquals("{\"modules\":6000}", connection.inputStream.bufferedReader().use { it.readText() }) }
                        finally { connection.disconnect() }
                    }
                }.sorted()
                val transfer = measureTimeMillis {
                    val connection = URL(address!! + "app.js").openConnection() as HttpURLConnection
                    connection.readTimeout = 30_000
                    try {
                        val bytes = connection.inputStream.use { input ->
                            val buffer = ByteArray(32_768)
                            var total = 0L
                            while (true) { val count = input.read(buffer); if (count < 0) break; total += count }
                            total
                        }
                        assertEquals(File(root, "dist/app.js").length(), bytes)
                    } finally { connection.disconnect() }
                }
                Log.i("PocketCodedPerf", "server startup-ms=$startMs api-p50-ms=${samples[15]} api-p95-ms=${samples[28]} bundle-transfer-ms=$transfer")
                exerciseBrowserAndEditor(address!!, root, files)
            } finally { NodeSessionRuntime.stop() }
        }
    }

    private fun exerciseBrowserAndEditor(address: String, root: File, files: List<WebFile>) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var activity: MainActivity
        instrumentation.runOnMainSync {
            activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<MainActivity>().first()
        }
        val loaded = CountDownLatch(1)
        var result = ""
        lateinit var browser: WebView
        val browserMs = measureTimeMillis {
            instrumentation.runOnMainSync {
                browser = WebView(activity).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            view.evaluateJavascript("String(globalThis.values && values.length)") {
                                result = it
                                loaded.countDown()
                            }
                        }
                    }
                }
                activity.setContentView(browser)
                browser.loadUrl(address)
            }
            assertTrue("Browser did not finish loading", loaded.await(45, TimeUnit.SECONDS))
            assertEquals("\"6000\"", result)
        }
        Log.i("PocketCodedPerf", "webview load-and-execute-ms=$browserMs modules=$result")
        val source = buildString { repeat(12_000) { append("const item$it = $it; // editor workload\n") } }
        val file = WebFile(123L, "large.js", source, File(activity.filesDir, "large.js").path, 0)
        val samples = Collections.synchronizedList(mutableListOf<Long>())
        val thread = HandlerThread("benchmark-frames").apply { start() }
        val listener = Window.OnFrameMetricsAvailableListener { _, frame, _ ->
            samples.add(frame.getMetric(FrameMetrics.TOTAL_DURATION))
        }
        val traceDirectory = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val trace = traceDirectory?.takeIf { InstrumentationRegistry.getArguments().getString("profile") == "true" }
            ?.let { File(it, "large-editor.trace").apply { parentFile?.mkdirs() } }
        if (trace != null) android.os.Debug.startMethodTracingSampling(trace.absolutePath, 32 * 1024 * 1024, 10_000)
        try {
            instrumentation.runOnMainSync {
                browser.destroy()
                activity.window.addOnFrameMetricsAvailableListener(listener, Handler(thread.looper))
                activity.setContent {
                    SmartCodeEditor(file, darkWebPalette(Color.Black, Color.Black, Color.DarkGray, Color.Cyan), Modifier.fillMaxSize()) {}
                }
            }
            Thread.sleep(3_000)
            lateinit var nativeEditor: LargeDocumentEditText
            var originalLayout: android.text.Layout? = null
            fun findEditor(view: android.view.View): LargeDocumentEditText? {
                if (view is LargeDocumentEditText) return view
                if (view is android.view.ViewGroup) for (index in 0 until view.childCount) {
                    findEditor(view.getChildAt(index))?.let { return it }
                }
                return null
            }
            instrumentation.runOnMainSync {
                nativeEditor = checkNotNull(findEditor(activity.window.decorView))
                assertEquals("Opening must not scroll to the cursor", 0, nativeEditor.scrollY)
                assertEquals(source, nativeEditor.text.toString())
                originalLayout = nativeEditor.layout
            }
            val initialFrames = synchronized(samples) { samples.toList() }
            Log.i("PocketCodedPerf", "editor initial-max-frame-ms=${(initialFrames.maxOrNull() ?: 0) / 1e6}")
            samples.clear()
            val bounds = instrumentation.targetContext.resources.displayMetrics
            repeat(8) {
                var x = 0
                var bottom = 0
                var top = 0
                instrumentation.runOnMainSync {
                    val position = IntArray(2)
                    nativeEditor.getLocationOnScreen(position)
                    x = position[0] + nativeEditor.width / 2
                    bottom = position[1] + nativeEditor.height * 3 / 4
                    top = position[1] + nativeEditor.height / 3
                }
                instrumentation.uiAutomation.executeShellCommand("input swipe $x $bottom $x $top 350").use {
                    android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
                }
            }
            Thread.sleep(2_500)
            var position = 0
            instrumentation.runOnMainSync {
                position = nativeEditor.scrollY
                assertTrue("Manual scrolling must move the large document", position > 0)
                nativeEditor.setSelection(0)
            }
            Thread.sleep(300)
            instrumentation.runOnMainSync {
                assertEquals("Selection must not move the viewport", position, nativeEditor.scrollY)
                assertSame("Scrolling must not rebuild the entire text layout", originalLayout, nativeEditor.layout)
                assertEquals("Swiping must not edit source", source, nativeEditor.text.toString())
                nativeEditor.text!!.insert(0, "// edit\n")
                assertTrue(nativeEditor.text.toString().endsWith(source))
            }
            val sorted = synchronized(samples) { samples.sorted() }
            assertTrue("No editor frames recorded", sorted.isNotEmpty())
            Log.i("PocketCodedPerf", "editor lines=12000 chars=${source.length} frames=${sorted.size} p50-ms=${sorted[sorted.size / 2] / 1e6} p95-ms=${sorted[(sorted.size * .95).toInt().coerceAtMost(sorted.lastIndex)] / 1e6} over-32ms=${sorted.count { it > 32_000_000 }}")
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                var tapX = 0
                var tapY = 0
                instrumentation.runOnMainSync {
                    val location = IntArray(2)
                    nativeEditor.getLocationOnScreen(location)
                    tapX = location[0] + nativeEditor.width / 2
                    tapY = location[1] + nativeEditor.height / 3
                }
                instrumentation.uiAutomation.executeShellCommand("input tap $tapX $tapY").use {
                    android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
                }
                var keyboardVisible = false
                val deadline = android.os.SystemClock.elapsedRealtime() + 3_000
                while (!keyboardVisible && android.os.SystemClock.elapsedRealtime() < deadline) {
                    instrumentation.runOnMainSync {
                        keyboardVisible = nativeEditor.rootWindowInsets?.isVisible(android.view.WindowInsets.Type.ime()) == true
                    }
                    if (!keyboardVisible) Thread.sleep(100)
                }
                assertTrue("Tapping code must still open the keyboard", keyboardVisible)
                instrumentation.runOnMainSync {
                    (activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                        .hideSoftInputFromWindow(nativeEditor.windowToken, 0)
                    nativeEditor.clearFocus()
                }
                Thread.sleep(300)
            }
            samples.clear()
            instrumentation.runOnMainSync {
                activity.setContent {
                    ExplorerPanel(files, -1, root, root, "", listOf(root.canonicalPath, File(root, "src").canonicalPath),
                        darkWebPalette(Color.Black, Color.Black, Color.DarkGray, Color.Cyan), Modifier.fillMaxSize(), 0,
                        {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
                }
            }
            Thread.sleep(4_000)
            repeat(6) {
                instrumentation.uiAutomation.executeShellCommand("input swipe ${bounds.widthPixels / 2} ${bounds.heightPixels * 3 / 4} ${bounds.widthPixels / 2} ${bounds.heightPixels / 3} 350").use {
                    android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
                }
            }
            val explorerFrames = synchronized(samples) { samples.sorted() }
            assertTrue(explorerFrames.isNotEmpty())
            Log.i("PocketCodedPerf", "explorer files=6003 frames=${explorerFrames.size} p95-ms=${explorerFrames[(explorerFrames.size * .95).toInt().coerceAtMost(explorerFrames.lastIndex)] / 1e6} max-ms=${explorerFrames.last() / 1e6}")
        } finally {
            if (trace != null) android.os.Debug.stopMethodTracing()
            instrumentation.runOnMainSync { activity.window.removeOnFrameMetricsAvailableListener(listener) }
            thread.quitSafely()
        }
    }
}
