package com.mkhokheli.pocketcodedweb

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class NativeEditorViewportDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val palette = WebPalette(
        backgroundTop = Color.Black, backgroundBottom = Color.Black, panel = Color.Black,
        elevatedPanel = Color.DarkGray, tab = Color.DarkGray, selectedTab = Color.Gray,
        text = Color.White, mutedText = Color.LightGray, subtleText = Color.Gray,
        border = Color.Gray, strongBorder = Color.Gray, accent = Color.Cyan,
        run = Color.Green, runText = Color.Black, terminalPrompt = Color.Green,
        divider = Color.Gray, error = Color.Red, warning = Color.Yellow, success = Color.Green, info = Color.Cyan,
    )

    @Test fun nativeTypingBatchesSnapshotsAndAStaleBindCannotReplaceUnpublishedText() {
        val original = "x".repeat(200_000)
        val session = EditorSession(original)
        val publications = AtomicInteger()
        val latest = AtomicReference<TextFieldValue>()
        lateinit var editor: LargeDocumentEditText
        compose.setContent {
            MaterialTheme {
                AndroidView(
                    modifier = Modifier.width(280.dp).height(220.dp),
                    factory = { context -> LargeDocumentEditText(context).also { editor = it } },
                    update = { view ->
                        view.bindSession(session)
                        view.bind(session.value, palette, { AnnotatedString(it) }) {
                            session.value = it; latest.set(it); publications.incrementAndGet()
                        }
                    },
                    onRelease = { it.dispose() },
                )
            }
        }
        compose.runOnIdle {
            publications.set(0)
            repeat(32) { editor.editableText.append('a') }
            editor.setSelection(editor.length())
            assertEquals("Typing must not copy and publish the entire document per character", 0, publications.get())
            editor.bind(TextFieldValue(original, TextRange(original.length)), palette, { AnnotatedString(it) }) {
                session.value = it; latest.set(it); publications.incrementAndGet()
            }
            assertEquals(original + "a".repeat(32), editor.text.toString())
            assertEquals(original.length + 32, editor.selectionEnd)
        }
        compose.waitUntil(5_000) { publications.get() > 0 }
        compose.runOnIdle {
            assertEquals(1, publications.get())
            assertEquals(original + "a".repeat(32), latest.get().text)
            assertEquals(original.length + 32, latest.get().selection.end)
        }
    }

    @Test fun ownerFlushAndDisposalPublishPendingTypingWithoutWaitingForTheDebounce() {
        val registry = EditorSessionRegistry()
        val session = registry.open("main.js", "original")
        val publications = AtomicInteger()
        var mounted by mutableStateOf(true)
        lateinit var editor: LargeDocumentEditText
        compose.setContent {
            MaterialTheme {
                if (mounted) AndroidView(
                    modifier = Modifier.width(280.dp).height(220.dp),
                    factory = { context -> LargeDocumentEditText(context).also { editor = it } },
                    update = { view ->
                        view.bindSession(session)
                        view.bind(session.value, palette, { AnnotatedString(it) }) {
                            session.value = it; publications.incrementAndGet()
                        }
                    },
                    onRelease = { it.dispose() },
                )
            }
        }
        compose.runOnIdle {
            publications.set(0)
            editor.editableText.append(" first")
            registry.flushPendingEdits()
            assertEquals("original first", session.value.text)
            assertEquals(1, publications.get())
            editor.editableText.append(" second")
            editor.dispose()
            assertEquals("original first second", session.value.text)
            assertEquals(2, publications.get())
            registry.flushPendingEdits()
            assertEquals("Released views must not stay registered", 2, publications.get())
            mounted = false
        }
        compose.waitForIdle()
        assertEquals(2, publications.get())
    }

    @Test fun bindingFocusingAndRemountingPreserveTheSavedManualViewport() {
        val source = (1..1_000).joinToString("\n") { "const line$it = $it;" }
        val savedScroll = 400
        val session = EditorSession(source).apply {
            followCursor = false
            verticalScroll = savedScroll
        }
        var mounted by mutableStateOf(true)
        lateinit var editor: LargeDocumentEditText
        compose.setContent {
            MaterialTheme {
                if (mounted) AndroidView(
                    modifier = Modifier.width(280.dp).height(220.dp),
                    factory = { context -> LargeDocumentEditText(context).also { editor = it } },
                    update = { view ->
                        view.bindSession(session)
                        view.bind(session.value, palette, { AnnotatedString(it) }, { session.value = it })
                        // Focus before posted restoration/reveal tasks run, as it can be
                        // restored automatically when a tab or fullscreen editor remounts.
                        view.requestFocus()
                    },
                    onRelease = { it.dispose() },
                )
            }
        }
        compose.waitForIdle()
        fun awaitEditorFocus(window: Boolean) {
            try {
                compose.waitUntil(5_000) { if (window) editor.hasWindowFocus() else editor.hasFocus() }
            } catch (timeout: ComposeTimeoutException) {
                var diagnostic = ""
                compose.runOnIdle {
                    val activity = compose.activity
                    val decor = activity.window.decorView
                    val root = editor.rootView
                    fun identity(value: Any) = value.javaClass.name + "@" +
                        Integer.toHexString(System.identityHashCode(value))
                    diagnostic = "waitingFor=${if (window) "window focus" else "editor focus"}; " +
                        "editorAttached=${editor.isAttachedToWindow}; editorRootIsActivityDecor=${root === decor}; " +
                        "editorRoot=${identity(root)}; activityDecor=${identity(decor)}; " +
                        "activity=${identity(activity)}; lifecycle=${activity.lifecycle.currentState}; " +
                        "activityWindowFocus=${activity.hasWindowFocus()}; decorWindowFocus=${decor.hasWindowFocus()}; " +
                        "activityFinishing=${activity.isFinishing}; activityDestroyed=${activity.isDestroyed}; " +
                        "editorShown=${editor.isShown}; editorVisibility=${editor.visibility}; " +
                        "editorWindowVisibility=${editor.windowVisibility}; editorSize=${editor.width}x${editor.height}; " +
                        "editorFocused=${editor.hasFocus()}; editorFocusable=${editor.isFocusable}; " +
                        "editorFocusableInTouchMode=${editor.isFocusableInTouchMode}; " +
                        "editorWindowTokenPresent=${editor.windowToken != null}; rootWindowFocus=${root.hasWindowFocus()}"
                }
                android.util.Log.e("NativeEditorViewportTest", diagnostic, timeout)
                throw AssertionError("Native editor focus fixture did not become active: $diagnostic", timeout)
            }
        }
        fun assertSavedViewport() {
            // MIUI may foreground the test activity after its view has been
            // bound. Request focus again once the actual window can accept it.
            awaitEditorFocus(window = true)
            compose.runOnIdle { editor.requestFocus() }
            awaitEditorFocus(window = false)
            compose.runOnIdle {
                assertTrue("The native editor has a measured text layout", editor.layout != null && editor.height > 0)
                assertTrue("The saved viewport is deliberately far from the EOF caret",
                    editor.layout.getLineTop(editor.layout.lineCount - 1) > savedScroll + editor.height)
                assertTrue(editor.hasFocus())
                assertEquals("The selection is restored independently of the viewport", source.length, editor.selectionEnd)
                assertFalse("Initial binding must preserve manual scroll mode", session.followCursor)
                assertEquals("Binding and focus must preserve the saved position", savedScroll, editor.scrollY)
                assertEquals(savedScroll, session.verticalScroll)
            }
        }
        assertSavedViewport()
        val firstEditor = editor
        compose.runOnIdle { mounted = false }
        compose.waitForIdle()
        compose.runOnIdle { mounted = true }
        compose.waitForIdle()
        compose.runOnIdle { assertNotSame("The fixture recreates the native view", firstEditor, editor) }
        assertSavedViewport()
    }
}
