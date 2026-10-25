package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class EditorFeaturesDeviceTest {
    @get:Rule val compose = createComposeRule()
    private val palette = WebPalette(
        backgroundTop = Color.Black, backgroundBottom = Color.Black, panel = Color.Black,
        elevatedPanel = Color.DarkGray, tab = Color.DarkGray, selectedTab = Color.Gray,
        text = Color.White, mutedText = Color.LightGray, subtleText = Color.Gray,
        border = Color.Gray, strongBorder = Color.Gray, accent = Color.Cyan,
        run = Color.Green, runText = Color.Black, terminalPrompt = Color.Green,
        divider = Color.Gray, error = Color.Red, warning = Color.Yellow, success = Color.Green, info = Color.Cyan,
    )

    @OptIn(ExperimentalTestApi::class)
    private fun editorHistoryShortcut(redo: Boolean = false) {
        compose.onNode(hasSetTextAction()).performClick().performKeyInput {
            keyDown(Key.CtrlLeft)
            val shortcut = if (redo) Key.Y else Key.Z
            keyDown(shortcut)
            keyUp(shortcut)
            keyUp(Key.CtrlLeft)
        }
    }

    @Test fun shortcutUndoAndRedoSitNextToTabAndReflectHistoryAvailability() {
        val history = EditorHistory()
        var value by mutableStateOf(TextFieldValue("hello", TextRange(5)))
        compose.setContent {
            MaterialTheme {
                EditorQuickKeys(palette, value, "main.js",
                    canUndo = history.canUndo, canRedo = history.canRedo,
                    onUndo = { history.undo(value)?.let { value = it } },
                    onRedo = { history.redo(value)?.let { value = it } },
                    onEditorValueChange = { updated -> history.record(value, updated); value = updated })
            }
        }
        compose.onNodeWithContentDescription("Undo").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Redo").assertIsNotEnabled()
        val tab = compose.onNodeWithText("Tab").fetchSemanticsNode().boundsInRoot
        val undo = compose.onNodeWithContentDescription("Undo").fetchSemanticsNode().boundsInRoot
        val redo = compose.onNodeWithContentDescription("Redo").fetchSemanticsNode().boundsInRoot
        assertEquals("Undo matches the shortcut row height", tab.height, undo.height, 1f)
        assertEquals("Redo matches the shortcut row height", tab.height, redo.height, 1f)
        assertTrue(tab.right <= undo.left)
        assertTrue(undo.right <= redo.left)
        compose.onNodeWithText("Tab").performClick()
        compose.onNodeWithContentDescription("Undo").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals("hello", value.text) }
        compose.onNodeWithContentDescription("Redo").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals("hello    ", value.text) }
    }

    @Test fun editingRevealsTheCaretButResizingAfterManualScrollKeepsTheViewport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var content by mutableStateOf("const initial = 1;")
        var editorHeight by mutableStateOf(340.dp)
        compose.setContent {
            MaterialTheme {
                SmartCodeEditor(WebFile(99, "scroll.js", content, File(context.cacheDir, "scroll.js").path, 0),
                    palette, Modifier.fillMaxWidth().height(editorHeight), onCodeChange = { content = it })
            }
        }
        val input = compose.onNode(hasSetTextAction())
        input.performClick()
        input.performTextReplacement((1..100).joinToString("\n") { "const line$it = $it;" })
        compose.waitForIdle()
        val viewport = compose.onNodeWithTag("editor-vertical-scroll")
        fun scrollPosition() = viewport.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
        val followedPosition = scrollPosition()
        assertTrue("Editing must reveal the final line", followedPosition > 0f)
        viewport.performTouchInput { swipeDown() }
        compose.waitForIdle()
        val manualPosition = scrollPosition()
        assertTrue("Manual scrolling must move away from the caret", manualPosition < followedPosition)
        compose.runOnIdle { editorHeight = 240.dp }
        compose.waitForIdle()
        assertEquals(manualPosition, scrollPosition(), 1f)
    }

    @Test fun undoRedoRemainIndependentAcrossFilesAndEditorRemounts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(context.cacheDir, "editor-device-${System.nanoTime()}").apply { mkdirs() }
        val sessions = EditorSessionRegistry()
        val contents = mutableStateMapOf(1L to "const value = 1;", 2L to "const value = 10;")
        var active by mutableStateOf(1L)
        var mounted by mutableStateOf(true)
        try {
            compose.setContent {
                MaterialTheme {
                    Column(Modifier.fillMaxSize()) {
                        Row {
                            TextButton(onClick = { active = 1L }) { Text("First file") }
                            TextButton(onClick = { active = 2L }) { Text("Second file") }
                        }
                        if (mounted) CompositionLocalProvider(LocalEditorSessions provides sessions) {
                            SmartCodeEditor(WebFile(active, "file$active.js", contents.getValue(active), File(root, "file$active.js").path, 0),
                                palette, Modifier.weight(1f), onCodeChange = { contents[active] = it })
                        }
                    }
                }
            }
            compose.onNode(hasSetTextAction()).performTextReplacement("const value = 2;")
            compose.onNodeWithText("Second file").performClick()
            compose.onNode(hasSetTextAction()).performTextReplacement("const value = 20;")
            compose.onNodeWithText("First file").performClick()
            compose.onNode(hasSetTextAction()).assertTextEquals("const value = 2;")
            editorHistoryShortcut()
            compose.onNode(hasSetTextAction()).assertTextEquals("const value = 1;")
            editorHistoryShortcut(redo = true)
            compose.onNode(hasSetTextAction()).assertTextEquals("const value = 2;")
            compose.runOnIdle { mounted = false }
            compose.waitForIdle()
            compose.runOnIdle { mounted = true }
            compose.onNode(hasSetTextAction()).assertTextEquals("const value = 2;")
            editorHistoryShortcut()
            compose.onNode(hasSetTextAction()).assertTextEquals("const value = 1;")
            compose.onNodeWithText("Second file").performClick()
            editorHistoryShortcut()
            compose.onNode(hasSetTextAction()).assertTextEquals("const value = 10;")
        } finally { root.deleteRecursively() }
    }

    @Test fun savedRunConfigurationsExecuteAndCanBeDeleted() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(context.cacheDir, "run-ui-${System.nanoTime()}").apply { mkdirs() }
        val store = ProjectFileStore(root)
        val controller = WebDevelopmentController(context, root)
        val command = AtomicReference<String>()
        val actions = WebDevelopmentActions(store, controller, { _, _ -> }, { _, _, _ -> },
            { _, done -> done(null) }, { _, done -> done(null) }, {}, emptyList(), {}, { emptyList() }, { command.set(it) }, {})
        val file = WebFile(1L, "main.js", "", File(root, "main.js").path, 0)
        try {
            compose.setContent { MaterialTheme { CompositionLocalProvider(LocalWebDevelopmentActions provides actions) { WebDevelopmentMenu(file, "", palette) {} } } }
            compose.onNodeWithContentDescription("Editor tools").performClick()
            compose.onNodeWithText("Undo").assertDoesNotExist()
            compose.onNodeWithText("Redo").assertDoesNotExist()
            compose.onNodeWithText("Run configurations").performClick()
            compose.onNodeWithText("Name").performTextInput("Smoke tests")
            compose.onNodeWithText("Command, e.g. npm test").performTextInput("npm test")
            compose.onNodeWithText("Save configuration").performClick()
            compose.onNodeWithText("Run Smoke tests").performClick()
            compose.runOnIdle { assertEquals("npm test", command.get()) }
            compose.onNodeWithContentDescription("Editor tools").performClick()
            compose.onNodeWithText("Run configurations").performClick()
            compose.onNodeWithText("Delete").performClick()
            compose.onNodeWithText("Run Smoke tests").assertDoesNotExist()
            assertTrue(WebRunConfigurationStore(context, root.path).load().isEmpty())
        } finally { root.deleteRecursively() }
    }

    @Test fun terminalToolsMatchEditorToolsAndKeepScrollableHistorySeparateFromOutputActions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(context.cacheDir, "terminal-menu-${System.nanoTime()}").apply { mkdirs() }
        val commands = listOf(
            "curl --user demo:sample https://example.invalid",
            "node first.js", "npm install", "npm test", "git status", "node latest.js",
        )
        val recalled = AtomicReference<String>()
        val clipboard = object : ClipboardManager {
            private var storedText: AnnotatedString? = null
            override fun setText(annotatedString: AnnotatedString) { storedText = annotatedString }
            override fun getText(): AnnotatedString? = storedText
        }
        var clears = 0
        var refreshes = 0
        val output = "Build complete\nReady for another command"
        val actions = WebDevelopmentActions(
            ProjectFileStore(root), WebDevelopmentController(context, root),
            { _, _ -> }, { _, _, _ -> }, { _, done -> done(null) }, { _, done -> done(null) },
            { clears++ }, commands, { recalled.set(it) }, { emptyList() }, {}, {},
            refreshHistory = { refreshes++ },
        )
        val file = WebFile(1L, "main.js", "", File(root, "main.js").path, 0)
        try {
            compose.setContent {
                MaterialTheme {
                    CompositionLocalProvider(
                        LocalWebDevelopmentActions provides actions,
                        LocalClipboardManager provides clipboard,
                    ) {
                        Row {
                            WebDevelopmentMenu(file, "", palette) {}
                            WebTerminalMenu(output, palette)
                        }
                    }
                }
            }
            val editorButton = compose.onNodeWithContentDescription("Editor tools").fetchSemanticsNode().boundsInRoot
            val terminalButton = compose.onNodeWithContentDescription("Terminal tools").fetchSemanticsNode().boundsInRoot
            assertEquals("Tool buttons have the same width", editorButton.width, terminalButton.width, 1f)
            assertEquals("Tool buttons have the same height", editorButton.height, terminalButton.height, 1f)

            compose.onNodeWithContentDescription("Terminal tools").performClick()
            compose.runOnIdle { assertEquals(1, refreshes) }
            compose.onNodeWithText("Copy output").assertIsDisplayed()
            compose.onNodeWithText("Clear output").assertIsDisplayed()
            compose.onNodeWithText("Recent commands").assertIsDisplayed()
            val history = compose.onNodeWithTag("terminal-command-history").assert(hasScrollAction())
            val newest = compose.onNodeWithText(commands.last()).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val previous = compose.onNodeWithText(commands[commands.lastIndex - 1]).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            assertTrue("Newest commands appear first", newest.top < previous.top)
            assertEquals("History viewport fits two command rows", newest.height + previous.height,
                history.fetchSemanticsNode().boundsInRoot.height, 1f)
            val oldestLabel = redactTerminalCommandForDisplay(commands.first())
            compose.onNodeWithText(commands.first()).assertDoesNotExist()
            compose.onNodeWithText(oldestLabel).assertIsNotDisplayed().performScrollTo().performClick()
            compose.runOnIdle { assertEquals("Recall retains the full command", commands.first(), recalled.get()) }
            compose.onNodeWithText("Recent commands").assertDoesNotExist()

            compose.onNodeWithContentDescription("Terminal tools").performClick()
            compose.onNodeWithText(commands.last()).assertIsDisplayed()
            compose.onNodeWithText("Copy output").performClick()
            compose.runOnIdle { assertEquals(output, clipboard.getText()?.text) }
            compose.onNodeWithContentDescription("Terminal tools").performClick()
            compose.onNodeWithText("Clear output").performClick()
            compose.runOnIdle { assertEquals(1, clears) }
            compose.onNodeWithText("Recent commands").assertDoesNotExist()
        } finally { root.deleteRecursively() }
    }
}
