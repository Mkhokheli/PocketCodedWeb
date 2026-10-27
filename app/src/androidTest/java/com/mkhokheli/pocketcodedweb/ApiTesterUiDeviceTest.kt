package com.mkhokheli.pocketcodedweb

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.mkhokheli.pocketcodedweb.ui.theme.PocketCodedWebTheme
import java.io.File
import java.util.UUID
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ApiTesterUiDeviceTest {
    @get:Rule(order = 0) val foreground = object : org.junit.rules.ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val pkg = instrumentation.targetContext.packageName
            android.os.ParcelFileDescriptor.AutoCloseInputStream(
                instrumentation.uiAutomation.executeShellCommand("am start -W -n $pkg/androidx.activity.ComponentActivity")
            ).use { it.readBytes() }
        }
    }
    @get:Rule(order = 1) val compose = createComposeRule()
    private lateinit var project: File
    private val server = ApiTestServer()
    private val visible = mutableStateOf(true)
    private val theme = mutableStateOf(WebTheme.VSCodeDefaultDark)
    private var originalSession: Map<String, *> = emptyMap<String, Any>()
    private val keys = setOf("active_file_id", "active_file_path", "working_directory_path", "terminal_directory_path",
        "expanded_folder_paths", "explorer_visible", "file_search", "full_screen_mode")

    @Before fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("web_ide", 0)
        originalSession = prefs.all.filterKeys { it in keys }
        prefs.edit().apply { keys.forEach { remove(it) }; putBoolean("explorer_visible", false) }.commit()
        NativeNodeRuntime.initialize(context)
        project = File(context.filesDir, "api-tester-ui-${UUID.randomUUID()}").apply { mkdirs() }
        File(project, "main.js").writeText("console.log('editor stays intact');\n")
        compose.setContent {
            PocketCodedWebTheme(darkTheme = theme.value.isDark, themeName = theme.value.name, dynamicColor = false) {
                if (visible.value) WebIdeApp(theme.value, { theme.value = theme.value.next() }, projectDirectory = project, onBackToProjects = {})
            }
        }
        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Editor tools").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Editor tools").performClick()
        compose.onNodeWithText("API tester").performClick()
        compose.onNodeWithTag("api-tester").assertExists()
    }

    @After fun cleanup() {
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        server.close()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("web_ide", 0).edit().apply {
            keys.forEach { remove(it) }
            originalSession.forEach { (key, value) -> when (value) {
                is String -> putString(key, value); is Boolean -> putBoolean(key, value); is Long -> putLong(key, value)
            } }
        }.commit()
        if (::project.isInitialized) {
            check(project.canonicalFile.parentFile == context.filesDir.canonicalFile)
            project.deleteRecursively()
        }
    }
    private fun url(path: String) = compose.onNodeWithTag("api-url").performScrollTo().performTextReplacement(server.url + path)
    private fun send() = compose.onNodeWithText("Send request").performScrollTo().performClick()
    private fun waitStatus(status: Int) {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("api-status").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("api-status").performScrollTo().assertTextContains(status.toString(), substring = true)
    }
    private fun expand() = compose.onNodeWithContentDescription("Fullscreen API tester").performClick()
    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.getExternalFilesDir(null), "api-tester-tests").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun inlineGetFullscreenRestoreAndEditorPreservation() {
        url("/echo?phone=true")
        send()
        waitStatus(200)
        compose.onNodeWithText("Headers", substring = false).performScrollTo().performClick()
        compose.onNodeWithTag("api-response").performScrollTo().assertTextContains("x-api-test: phone", substring = true, ignoreCase = true)
        expand()
        compose.onNodeWithTag("api-status").assertTextContains("200", substring = true)
        screenshot("get-response-fullscreen")
        compose.onNodeWithContentDescription("Minimize API tester").performClick()
        compose.onNodeWithContentDescription("Close API tester").performClick()
        compose.onNodeWithContentDescription("Editor tools").assertExists()
        assertEquals("console.log('editor stays intact');\n", File(project, "main.js").readText())
        compose.onNodeWithContentDescription("Editor tools").performClick()
        compose.onNodeWithText("API tester").performClick()
        compose.onNodeWithTag("api-url").assertTextContains(server.url + "/echo?phone=true")
        compose.onNodeWithTag("api-status").performScrollTo().assertTextContains("200", substring = true)
    }

    @Test fun postBodyHeadersPrettyCopyAndErrorResponse() {
        expand()
        url("/echo")
        compose.onNodeWithText("GET", substring = false).performClick()
        compose.onNodeWithText("POST", substring = false).performClick()
        compose.onNodeWithTag("api-headers").performScrollTo().performTextReplacement("X-Test-Token: ui-token\nContent-Type: application/json")
        compose.onNodeWithTag("api-body").performScrollTo().performTextReplacement("{\"from\":\"phone\"}")
        send(); waitStatus(200)
        compose.onNodeWithTag("api-response").performScrollTo().assertTextContains("ui-token", substring = true)
        compose.onNodeWithText("Copy response").performScrollTo().assertIsEnabled().performClick()
        val clipboard = InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertTrue(clipboard.primaryClip?.getItemAt(0)?.text.toString().contains("ui-token"))
        }
        compose.onNodeWithText("Pretty JSON").performScrollTo().performClick()
        compose.onNodeWithTag("api-response").performScrollTo().assertTextContains("POST", substring = true)
        screenshot("post-response")
        url("/error"); send(); waitStatus(422)
        compose.onNodeWithTag("api-response").performScrollTo().assertTextContains("true", substring = true)
    }

    @Test fun invalidInputTimeoutCancelAndRetry() {
        expand()
        compose.onNodeWithTag("api-url").performTextReplacement("not a url")
        send()
        compose.onNodeWithTag("api-error").performScrollTo().assertTextContains("complete http", substring = true)
        url("/echo")
        compose.onNodeWithTag("api-headers").performScrollTo().performTextReplacement("invalid header")
        send()
        compose.onNodeWithTag("api-error").performScrollTo().assertTextContains("Name: value", substring = true)
        compose.onNodeWithTag("api-headers").performScrollTo().performTextReplacement("")
        compose.onNodeWithTag("api-timeout").performScrollTo().performTextReplacement("1")
        url("/slow"); send()
        compose.waitUntil(8_000) { compose.onAllNodesWithText("Request timed out.", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("api-timeout").performScrollTo().performTextReplacement("15")
        send()
        compose.onNodeWithText("Cancel request").performScrollTo().performClick()
        compose.onNodeWithTag("api-error").performScrollTo().assertTextContains("canceled", substring = true)
        url("/echo"); send(); waitStatus(200)
    }

    @Test fun resizingDuringRequestKeepsWorkAlive() {
        url("/slow")
        send()
        expand()
        compose.onNodeWithText("Cancel request").assertExists()
        waitStatus(200)
        compose.onNodeWithTag("api-response").performScrollTo().assertTextContains("/slow", substring = true)
    }

    @Test fun closingDuringRequestCancelsAndReopensWithoutLosingDraft() {
        expand()
        url("/slow")
        send()
        compose.onNodeWithContentDescription("Close API tester").performClick()
        compose.onNodeWithContentDescription("Editor tools").performClick()
        compose.onNodeWithText("API tester").performClick()
        compose.onNodeWithTag("api-url").assertTextContains(server.url + "/slow")
        compose.onNodeWithText("Send request").performScrollTo().assertIsEnabled()
        url("/echo"); send(); waitStatus(200)
        // The canceled slow response must not replace the successful retry.
        Thread.sleep(3_200)
        compose.onNodeWithTag("api-response").performScrollTo().assertTextContains("/echo", substring = true)
    }

    @Test fun largeResponsePreviewEmptyBodyAndBackNavigation() {
        expand()
        url("/large"); send(); waitStatus(200)
        compose.onNodeWithText("Response limited to the first 1 MB.").performScrollTo().assertExists()
        compose.onNodeWithText("Preview shows the first", substring = true).performScrollTo().assertExists()
        compose.onNodeWithTag("api-response").assertTextEquals("x".repeat(32_000))
        url("/empty"); send(); waitStatus(204)
        compose.onNodeWithTag("api-response").performScrollTo().assertTextEquals("(empty body)")
        androidx.test.espresso.Espresso.pressBack()
        compose.onNodeWithTag("api-tester").assertDoesNotExist()
        compose.onNodeWithText("Run Code").assertExists()
    }

    @Test fun usersNodeApiCanBeTestedInsideTheEditorCard() {
        expand()
        val source = File(project, "api-server.cjs").apply { writeText("""
            const http = require('node:http');
            http.createServer((req, res) => {
                res.setHeader('Content-Type', 'application/json');
                res.end(JSON.stringify({message: 'API written in PocketCoded Web', path: req.url}));
            }).listen(0, '127.0.0.1');
        """.trimIndent()) }
        try {
            val started = NodeSessionRuntime.start(project, project, source, emptyList())
            assertTrue(started.error, started.error.isBlank())
            val endpoint = NodeSessionRuntime.awaitPreviewUrl()
            assertNotNull(endpoint)
            compose.onNodeWithTag("api-url").performScrollTo().performTextReplacement(endpoint!!.trimEnd('/') + "/api/health")
            send(); waitStatus(200)
            compose.onNodeWithTag("api-response").performScrollTo().assertTextContains("API written in PocketCoded Web", substring = true)
            screenshot("node-api-response")
        } finally { NodeSessionRuntime.stop() }
    }

    @Test fun everyThemeAndCompactFullscreenKeepRequestDraft() {
        expand()
        url("/echo")
        for (next in WebTheme.entries) {
            compose.runOnIdle { theme.value = next }
            compose.onNodeWithTag("api-url").assertTextContains(server.url + "/echo")
            compose.onNodeWithText("Send request").assertIsEnabled()
            if (next in listOf(WebTheme.VSCodeDefaultDark, WebTheme.VSCodeDefaultLight, WebTheme.VioletHazeDark)) screenshot("theme-${next.name}")
        }
        repeat(4) {
            compose.onNodeWithContentDescription("Minimize API tester").performClick()
            compose.onNodeWithTag("api-url").assertTextContains(server.url + "/echo")
            expand()
        }
        screenshot("request-form")
    }
}
