package com.mkhokheli.pocketcodedweb

import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExternalBrowserPerformanceTest {
    class Ticks {
        val count = AtomicInteger()
        @JavascriptInterface fun tick() { count.incrementAndGet() }
    }

    @Test fun embeddedPreviewTimersPauseInBackgroundAndResumeOnReturnOrDisposal() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val ticks = Ticks()
        lateinit var view: WebView
        lateinit var controller: PreviewWebViewLifecycle
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
        }
        fun waitForTicks(after: Int) {
            val deadline = SystemClock.elapsedRealtime() + 10_000
            while (ticks.count.get() <= after && SystemClock.elapsedRealtime() < deadline) Thread.sleep(25)
            assertTrue("Embedded JavaScript did not resume", ticks.count.get() > after)
        }
        instrumentation.runOnMainSync {
            owner.registry.currentState = Lifecycle.State.STARTED
            view = WebView(instrumentation.targetContext)
            view.settings.javaScriptEnabled = true
            view.addJavascriptInterface(ticks, "benchmark")
            controller = PreviewWebViewLifecycle(view, owner.lifecycle)
            view.loadData("<script>setInterval(()=>benchmark.tick(),50)</script>", "text/html", "UTF-8")
        }
        try {
            waitForTicks(3)
            instrumentation.runOnMainSync { owner.registry.currentState = Lifecycle.State.CREATED }
            Thread.sleep(200) // Allow any already-dispatched bridge callback to finish.
            val paused = ticks.count.get()
            Thread.sleep(400)
            assertEquals("Background preview kept executing JavaScript", paused, ticks.count.get())
            instrumentation.runOnMainSync { owner.registry.currentState = Lifecycle.State.STARTED }
            waitForTicks(paused)
            instrumentation.runOnMainSync { owner.registry.currentState = Lifecycle.State.CREATED }
            Thread.sleep(200)
            val beforeClose = ticks.count.get()
            instrumentation.runOnMainSync { controller.close() }
            waitForTicks(beforeClose)
        } finally {
            instrumentation.runOnMainSync { controller.close(); view.destroy() }
        }
    }

    @Test fun chromeAnimationsWhileNodeDevelopmentServerRuns() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val root = File(context.filesDir, "external-browser-benchmark").apply { mkdirs() }
        val script = File(root, "server.cjs")
        instrumentation.context.assets.open("external-browser-benchmark.cjs").use { input -> script.outputStream().use(input::copyTo) }
        File(root, "package.json").writeText("""{"scripts":{"dev":"node server.cjs"}}""")
        val result = File(root, "browser-result.json").apply { delete() }
        instrumentation.uiAutomation.executeShellCommand("am start -W -n ${context.packageName}/${MainActivity::class.java.name}").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        NativeNodeRuntime.initialize(context)
        try {
            NodeSessionRuntime.stop()
            val launch = executeWebShellCommand("npm run dev", root, root)
            assertTrue(launch.error, launch.error.isBlank())
            val url = NodeSessionRuntime.awaitPreviewUrl()
            assertNotNull(url)
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.android.chrome").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            val deadline = SystemClock.elapsedRealtime() + 90_000
            while (!result.isFile && SystemClock.elapsedRealtime() < deadline) Thread.sleep(250)
            assertTrue("Chrome did not report results; keep the phone unlocked on the benchmark tab", result.isFile)
            val json = JSONObject(result.readText())
            assertEquals(6000, json.getInt("moduleCount"))
            val results = json.getJSONArray("results")
            assertEquals(2, results.length())
            repeat(results.length()) {
                val phase = results.getJSONObject(it)
                assertFalse("Benchmark tab was hidden", phase.getBoolean("hidden"))
                assertTrue(phase.getInt("frames") > 100)
            }
            Log.i("PocketCodedChrome", json.toString())
        } finally { NodeSessionRuntime.stop() }
    }
}
