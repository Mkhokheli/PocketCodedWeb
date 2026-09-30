package com.mkhokheli.pocketcodedweb

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NodeSessionDeviceTest {
    @Test fun npmBackendPreviewStopAndRestart() {
        ActivityScenario.launch(MainActivity::class.java).use {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            NativeNodeRuntime.initialize(context)
            val root = File(context.filesDir, "node-session-device-test").apply { mkdirs() }
            try {
                NodeSessionRuntime.stop()
                File(root, "package.json").writeText("""{"scripts":{"dev":"PCW_TEST=yes node server.cjs"}}""")
                File(root, "server.cjs").writeText("""
                    if (process.env.PCW_TEST !== 'yes' || process.argv[2] !== 'two words') throw new Error('script arguments lost');
                    require('http').createServer((req, res) => setTimeout(() => {
                        console.log('served', req.url);
                        res.end('backend-on-android');
                    }, 10)).listen(0, '127.0.0.1');
                """.trimIndent())
                repeat(2) {
                    val started = executeWebShellCommand("npm run dev -- \"two words\"", root, root)
                    assertTrue(started.output + started.error, started.error.isBlank())
                    val url = NodeSessionRuntime.awaitPreviewUrl()
                    assertNotNull(started.output, url)
                    val connection = URL(url!! + "api").openConnection() as HttpURLConnection
                    connection.connectTimeout = 3_000
                    connection.readTimeout = 3_000
                    try {
                        assertEquals("backend-on-android", connection.inputStream.bufferedReader().use { it.readText() })
                    } finally { connection.disconnect() }
                    val logDeadline = android.os.SystemClock.elapsedRealtime() + 3_000
                    while (!NodeSessionRuntime.status().output.contains("served /api") && android.os.SystemClock.elapsedRealtime() < logDeadline) Thread.sleep(100)
                    assertTrue(NodeSessionRuntime.status().output.contains("served /api"))
                    assertEquals(url, executeWebShellCommand("preview $url", root, root).previewLink)
                    assertTrue(NodeSessionRuntime.stop().error.isBlank())
                    assertNull(NodeSessionRuntime.previewUrl())
                }
                File(root, "esm.mjs").writeText("""
                    import http from 'node:http';
                    await new Promise(resolve => setTimeout(resolve, 20));
                    http.createServer((req, res) => res.end('esm')).listen(0, '127.0.0.1');
                """.trimIndent())
                File(root, "package.json").writeText("""{"scripts":{"dev":"node esm.mjs"}}""")
                val preview = prepareProjectServerPreview(root, root)
                assertNotNull(preview?.output.orEmpty() + preview?.error.orEmpty(), preview?.previewLink)
                assertEquals(preview?.previewLink, prepareProjectServerPreview(root, root)?.previewLink)
                NodeSessionRuntime.stop()
                File(root, "package.json").writeText("""{"scripts":{"dev":"preview"}}""")
                assertNull(prepareProjectServerPreview(root, root))
                File(root, "loop.cjs").writeText("while (true) {}")
                assertTrue(NodeSessionRuntime.start(root, root, File(root, "loop.cjs"), emptyList()).error.isBlank())
                assertTrue(NodeSessionRuntime.stop(force = true).error.isBlank())
            } finally {
                NodeSessionRuntime.stop()
                root.deleteRecursively()
            }
        }
    }
}
