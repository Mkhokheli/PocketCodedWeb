package com.mkhokheli.pocketcodedweb

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Css
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Html
import androidx.compose.material.icons.rounded.Javascript
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.documentfile.provider.DocumentFile
import com.mkhokheli.pocketcodedweb.ui.theme.PocketCodedWebTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

private const val IDE_PREFS_NAME = "web_ide"
private const val PREF_ACTIVE_FILE_ID = "active_file_id"
private const val PREF_ACTIVE_FILE_PATH = "active_file_path"
private const val PREF_WORKING_DIRECTORY_PATH = "working_directory_path"
private const val PREF_TERMINAL_DIRECTORY_PATH = "terminal_directory_path"
private const val PREF_EXPANDED_FOLDER_PATHS = "expanded_folder_paths"
private const val PREF_EXPLORER_VISIBLE = "explorer_visible"
private const val PREF_FILE_SEARCH = "file_search"
private const val PREF_TERMINAL_INPUT = "terminal_input"
private const val PREF_TERMINAL_TEXT = "terminal_text"
private const val PREF_FULL_SCREEN_MODE = "full_screen_mode"
private const val PREF_IDE_THEME = "ide_theme"
private const val PREF_USER_AI_API_KEY = "user_ai_api_key"
private const val MAX_EDITABLE_PROJECT_FILE_BYTES = 1_000_000L
private const val AI_CONTEXT_FILE_LIMIT = 24
private const val AI_CONTEXT_CHAR_LIMIT = 60_000
private const val AI_TERMINAL_CONTEXT_LIMIT = 12_000
private const val AI_MAX_OUTPUT_TOKENS = 16_384
private const val COMPLETION_IDLE_TIMEOUT_MS = 3_500L
private const val EDITOR_SAVE_DEBOUNCE_MS = 1_000L
private const val DIAGNOSTIC_IDLE_DELAY_MS = 220L
private const val SYNTAX_HIGHLIGHT_IDLE_DELAY_MS = 80L
private const val CURSOR_REVEAL_DELAY_MS = 45L
private const val LIVE_DIAGNOSTIC_CHAR_LIMIT = 180_000
private const val LIVE_SYNTAX_HIGHLIGHT_CHAR_LIMIT = 120_000

private val htmlFileExtensions = setOf("html", "htm", "svg")
private val cssFileExtensions = setOf("css")
private val javascriptFileExtensions = setOf("js", "mjs", "cjs", "jsx")
private val editableWebExtensions = htmlFileExtensions + cssFileExtensions + javascriptFileExtensions + setOf("json", "md", "txt")
private val defaultWebFileNames = listOf("index.html", "style.css", "script.js")
private val workspaceMetadataNames = setOf(".git", ".ssh", ".pocketcodedweb")
private val aiEditableFilePathRegex =
    Regex("""(?i)(?:^|[`'"\s(:-])([A-Za-z0-9_. -]+(?:/[A-Za-z0-9_. -]+)*\.(?:html|htm|css|js|mjs|cjs|jsx|json|md|txt|svg))(?=$|[`'")\s:,-])""")
private val jsImportSpecifierRegex =
    Regex("""(?m)\b(?:import|export)\s+(?:[^'"]*?\s+from\s*)?["']([^"']+)["']|import\s*\(\s*["']([^"']+)["']\s*\)""")
private val commonLibraryPackageNames = listOf(
    "react",
    "react-dom/client",
    "vue",
    "petite-vue",
    "svelte",
    "three",
    "gsap",
    "animejs",
    "axios",
    "lodash",
    "lodash-es",
    "d3",
    "chart.js/auto",
    "dayjs",
    "date-fns",
    "firebase/app",
    "firebase/auth",
    "firebase/firestore",
    "@supabase/supabase-js",
    "zod",
    "uuid",
    "nanoid",
    "matter-js",
    "phaser",
    "p5",
    "pixi.js",
    "@babylonjs/core",
    "howler",
    "tone",
    "canvas-confetti",
    "swiper",
    "marked",
    "prismjs",
    "highlight.js",
)

private val HTML_COMMENT_REGEX = Regex("<!--[\\s\\S]*?-->")
private val HTML_DOCTYPE_REGEX = Regex("(?i)<!DOCTYPE[^>]*>")
private val HTML_TAG_BRACKETS_REGEX = Regex("</?|/?>")
private val HTML_TAG_NAME_REGEX = Regex("</?\\s*([a-zA-Z][\\w:-]*)")
private val HTML_ATTR_NAME_CORE_REGEX = Regex("\\b(id|class|href|src|alt|type|name|value|style|onclick|role|aria-[\\w-]+)(?=\\s*=)")
private val HTML_ATTR_NAME_GENERIC_REGEX = Regex("\\s([a-zA-Z_:][-a-zA-Z0-9_:.]*)(?=\\s*=)")
private val HTML_ATTR_VALUE_REGEX = Regex("\"[^\"]*\"|'[^']*'")
private val HTML_ENTITY_REGEX = Regex("&[a-zA-Z0-9#]+;")

private val CSS_COMMENT_REGEX = Regex("/\\*[\\s\\S]*?\\*/")
private val CSS_AT_RULE_REGEX = Regex("@[a-zA-Z-]+")
private val CSS_SELECTOR_ID_CLASS_REGEX = Regex("[.#][a-zA-Z0-9_-]+")
private val CSS_SELECTOR_BLOCK_REGEX = Regex("([a-zA-Z0-9_-]+)\\s*\\{")
private val CSS_PROPERTY_REGEX = Regex("\\b([a-zA-Z-]+)\\s*(?=:)")
private val CSS_COLOR_HEX_REGEX = Regex("#[0-9a-fA-F]{3,8}\\b")
private val CSS_NUMBER_UNIT_REGEX = Regex("\\b\\d+(?:\\.\\d+)?(px|rem|em|vh|vw|%|s|ms|deg)?\\b")
private val CSS_STRING_REGEX = Regex("\"[^\"]*\"|'[^']*'")
private val CSS_KEYWORD_REGEX = Regex("(?i)\\b(true|false|inherit|initial|unset|none|auto|block|inline|flex|grid|absolute|relative|fixed|sticky|center|space-between|space-around|column|row|sans-serif|serif|monospace)\\b")

private val JS_COMMENT_REGEX = Regex("//.*$|/\\*[\\s\\S]*?\\*/", RegexOption.MULTILINE)
private val JS_STRING_REGEX = Regex("`[\\s\\S]*?`|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'")
private val JS_BOOLEAN_NULL_REGEX = Regex("\\b(true|false|null|undefined|NaN|Infinity)\\b")
private val JS_NUMBER_REGEX = Regex("\\b\\d+(?:\\.\\d+)?\\b")
private val JS_MEMBER_ACCESS_REGEX = Regex("\\.([A-Za-z_$][\\w$]*)")
private val JS_FUNC_CALL_REGEX = Regex("\\b([A-Za-z_$][A-Za-z0-9_$]*)\\s*(?=\\()|\\.([A-Za-z_$][A-Za-z0-9_$]*)\\s*(?=\\()")

private val JSON_KEY_REGEX = Regex("\"[^\"]*\"(?=\\s*:)")
private val JSON_STRING_VALUE_REGEX = Regex(":\\s*(\"[^\"]*\")")
private val JSON_LITERAL_VALUE_REGEX = Regex(":\\s*(\\d+|true|false|null)")

private val aiModelOptions = listOf(
    AiModelOption("gemini-3.5-flash", "Gemini 3.5 Flash", "Fast coding help for everyday web work."),
    AiModelOption("gemini-3.1-flash-lite", "Gemini 3.1 Flash-Lite", "Quick answers and small edits."),
    AiModelOption("gemini-2.5-pro", "Gemini 2.5 Pro", "Deeper reasoning for larger changes."),
    AiModelOption("gemini-2.5-flash", "Gemini 2.5 Flash", "Stable coding support."),
)

@Stable
internal data class WebFile(
    val id: Long,
    val name: String,
    val content: String,
    val filePath: String,
    val modifiedAt: Long,
    val externalUri: String? = null,
)

@Stable
private data class FolderTarget(
    val name: String,
    val path: String,
    val relativePath: String,
)

private sealed class ExplorerEntry {
    data class Directory(val target: FolderTarget, val depth: Int, val expanded: Boolean) : ExplorerEntry()
    data class SourceFile(val file: WebFile, val depth: Int) : ExplorerEntry()
}

private enum class WebFullScreenMode {
    Editor,
    Terminal,
    Ai,
    Preview,
}

private enum class WebTheme(
    val displayName: String,
    val isDark: Boolean,
) {
    CyberpunkElectricDark("Cyberpunk Electric Dark", true),
    CyberpunkElectricLight("Cyberpunk Electric Light", false),
    ForestNightDark("Forest Night Dark", true),
    ForestNightLight("Forest Night Light", false),
    SunsetDuskDark("Sunset Dusk Dark", true),
    SunsetDuskLight("Sunset Dusk Light", false),
    ArcticAuroraDark("Arctic Aurora Dark", true),
    ArcticAuroraLight("Arctic Aurora Light", false),
    VioletHazeDark("Violet Haze Dark", true),
    VioletHazeLight("Violet Haze Light", false),
    MonochromeProDark("Monochrome Pro Dark", true),
    MonochromeProLight("Monochrome Pro Light", false),
    VSCodeDefaultDark("VS Code Default Dark", true),
    VSCodeDefaultLight("VS Code Default Light", false);

    fun next(): WebTheme = entries[(ordinal + 1) % entries.size]
}

@Stable
internal data class WebPalette(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val panel: Color,
    val elevatedPanel: Color,
    val tab: Color,
    val selectedTab: Color,
    val text: Color,
    val mutedText: Color,
    val subtleText: Color,
    val border: Color,
    val strongBorder: Color,
    val accent: Color,
    val run: Color,
    val runText: Color,
    val terminalPrompt: Color,
    val divider: Color,
    val error: Color,
    val warning: Color,
    val success: Color,
    val info: Color,
)

private data class WebSession(
    val activeFileId: Long = -1L,
    val activeFilePath: String? = null,
    val workingDirectoryPath: String? = null,
    val terminalDirectoryPath: String? = null,
    val expandedFolderPaths: List<String> = emptyList(),
    val explorerVisible: Boolean = true,
    val fileSearch: String = "",
    val terminalInput: String = "",
    val terminalText: String = "",
    val fullScreenMode: WebFullScreenMode? = null,
    val userAiApiKey: String? = null,
)

private data class WebShellResult(
    val output: String = "",
    val error: String = "",
    val workingDirectoryPath: String? = null,
    val clearTerminal: Boolean = false,
    val workspaceChanged: Boolean = false,
    val openPreview: Boolean = false,
    val runJs: String? = null,
)

internal data class CompletionItem(
    val label: String,
    val insertText: String = label,
    val detail: String = "",
    val cursorOffset: Int = insertText.length,
)

internal data class CompletionContext(
    val prefix: String,
    val replaceStart: Int,
    val replaceEnd: Int,
    val trigger: String = "",
    val isDotAccess: Boolean = false,
)

private data class EditorCompletionResult(
    val context: CompletionContext? = null,
    val items: List<CompletionItem> = emptyList(),
    val source: String = "",
    val cursor: Int = -1,
    val signatureHelp: String? = null,
)

private data class EditorVisualResult(
    val source: String = "",
    val text: AnnotatedString = AnnotatedString(""),
)

private data class EditorTextMetrics(
    val lineCount: Int = 1,
    val longestLineLength: Int = 48,
)

internal data class CodeDiagnostic(
    val line: Int,
    val column: Int,
    val start: Int,
    val end: Int,
    val severity: String,
    val message: String,
)

private enum class WebEditorLanguage {
    Html,
    Css,
    JavaScript,
    Json,
    Text,
}

private data class EmbeddedWebBlock(
    val language: WebEditorLanguage,
    val start: Int,
    val end: Int,
)

private data class AiModelOption(
    val id: String,
    val label: String,
    val description: String,
)

private data class AiChatMessage(
    val role: AiChatRole,
    val text: String,
)

private enum class AiChatRole {
    User,
    Assistant,
}

internal sealed class AiSuggestedAction {
    data class RunTerminalCommand(val command: String) : AiSuggestedAction()
    data class ReplaceActiveFile(val code: String) : AiSuggestedAction()
    data class ApplyProjectFileChanges(val changes: List<AiProjectFileChange>) : AiSuggestedAction()
}

internal enum class AiProjectFileOperation {
    Write,
    Delete,
}

internal data class AiProjectFileChange(
    val operation: AiProjectFileOperation,
    val path: String,
    val content: String = "",
)

private data class AiCodeBlock(
    val prefix: String,
    val language: String,
    val code: String,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activityContext = this@MainActivity
            var ideTheme by rememberSaveable { mutableStateOf(loadWebIdeTheme(activityContext)) }

            LaunchedEffect(ideTheme) {
                saveWebIdeTheme(activityContext, ideTheme)
            }

            PocketCodedWebTheme(
                darkTheme = ideTheme.isDark,
                dynamicColor = false,
            ) {
                PocketCodedWebEntry(
                    darkMode = ideTheme.isDark,
                    currentThemeName = ideTheme.name,
                    nextThemeName = ideTheme.next().displayName,
                    onToggleTheme = { ideTheme = ideTheme.next() },
                ) { projectDirectory, onBackToProjects ->
                    WebIdeApp(
                        ideTheme = ideTheme,
                        onToggleTheme = { ideTheme = ideTheme.next() },
                        projectDirectory = projectDirectory,
                        onBackToProjects = onBackToProjects,
                    )
                }
            }
        }
    }
}

@Composable
private fun WebIdeApp(
    ideTheme: WebTheme,
    onToggleTheme: () -> Unit,
    projectDirectory: File,
    onBackToProjects: () -> Unit,
) {
    val context = LocalContext.current
    val palette = remember(ideTheme) { webIdePalette(ideTheme) }
    val coroutineScope = rememberCoroutineScope()
    val rootDirectory = remember(projectDirectory.absolutePath) {
        projectDirectory.apply {
            mkdirs()
            ensureDefaultWebProject(this)
        }
    }
    val savedSession = remember(rootDirectory.absolutePath) { loadWebIdeSession(context, rootDirectory) }
    val initialFiles = remember(rootDirectory.absolutePath) { loadWebFiles(rootDirectory) }
    val files = remember(rootDirectory.absolutePath) {
        mutableStateListOf<WebFile>().apply { addAll(initialFiles) }
    }
    var activeFileId by rememberSaveable(rootDirectory.absolutePath) {
        mutableLongStateOf(
            savedSession.activeFilePath
                ?.let { savedPath -> initialFiles.firstOrNull { samePath(it.filePath, savedPath) }?.id }
                ?: savedSession.activeFileId.takeIf { id -> initialFiles.any { it.id == id } }
                ?: initialFiles.firstOrNull()?.id
                ?: -1L
        )
    }
    var explorerVisible by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf(savedSession.explorerVisible) }
    var fileSearch by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf(savedSession.fileSearch) }
    var terminalInput by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf(savedSession.terminalInput) }
    var terminalText by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf(savedSession.terminalText) }
    var fullScreenMode by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf(savedSession.fullScreenMode) }
    var directoryVersion by remember { mutableLongStateOf(0L) }
    var fileActionsDialogOpen by remember { mutableStateOf(false) }
    var newFileDialogOpen by remember { mutableStateOf(false) }
    var newFolderDialogOpen by remember { mutableStateOf(false) }
    var renameFileTarget by remember { mutableStateOf<WebFile?>(null) }
    var renameFolderTarget by remember { mutableStateOf<FolderTarget?>(null) }
    var deleteFileTarget by remember { mutableStateOf<WebFile?>(null) }
    var deleteFolderTarget by remember { mutableStateOf<FolderTarget?>(null) }
    val expandedFolderPaths = remember(rootDirectory.absolutePath) {
        mutableStateListOf<String>().apply {
            addAll(normalizedWorkspaceDirectoryPaths(rootDirectory, savedSession.expandedFolderPaths))
        }
    }
    val dirtyFileIds = remember { mutableSetOf<Long>() }
    val pendingEditorTextByFileId = remember(rootDirectory.absolutePath) { mutableMapOf<Long, String>() }
    val pendingEditorSaveJobs = remember { mutableMapOf<Long, Job>() }

    val activeFile = (files.firstOrNull { it.id == activeFileId }
        ?: files.firstOrNull()
        ?: createDefaultWebFile(rootDirectory).also {
            files.add(it)
            activeFileId = it.id
        }).withPendingEditorText(pendingEditorTextByFileId)

    BackHandler(enabled = true) {
        if (fullScreenMode != null) {
            fullScreenMode = null
        } else {
            onBackToProjects()
        }
    }

    var workingDirectoryPath by rememberSaveable(rootDirectory.absolutePath) {
        mutableStateOf(
            validDirectoryOrRoot(
                rootDirectory = rootDirectory,
                candidatePath = savedSession.workingDirectoryPath ?: activeFile.parentDirectoryPath(rootDirectory),
            ).absolutePath
        )
    }
    var terminalDirectoryPath by rememberSaveable(rootDirectory.absolutePath) {
        mutableStateOf(
            validDirectoryOrRoot(
                rootDirectory = rootDirectory,
                candidatePath = savedSession.terminalDirectoryPath ?: activeFile.parentDirectoryPath(rootDirectory),
            ).absolutePath
        )
    }
    val workingDirectory = remember(workingDirectoryPath, directoryVersion) {
        validDirectoryOrRoot(rootDirectory, workingDirectoryPath)
    }
    val terminalDirectory = remember(terminalDirectoryPath, directoryVersion) {
        validDirectoryOrRoot(rootDirectory, terminalDirectoryPath)
    }
    val tabFiles = files
        .filter { it.isDirectChildOf(workingDirectory) }
        .sortedByDescending { it.modifiedAt }
    val terminalPromptText = terminalPrompt(rootDirectory, terminalDirectory)

    var selectedAiModelId by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf(aiModelOptions.first().id) }
    val selectedAiModel = aiModelOptions.firstOrNull { it.id == selectedAiModelId } ?: aiModelOptions.first()
    var userAiApiKey by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf(savedSession.userAiApiKey.orEmpty()) }
    val aiMessages = remember(rootDirectory.absolutePath) {
        mutableStateListOf(
            AiChatMessage(
                role = AiChatRole.Assistant,
                text = "Hi, I can read this Web project, explain code, suggest edits, and prepare terminal commands. Add a Gemini API key when you want me to make a request.",
            )
        )
    }
    var aiInput by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf("") }
    var aiBusy by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var aiPendingAction by remember { mutableStateOf<AiSuggestedAction?>(null) }
    var aiVerificationMessage by remember { mutableStateOf<String?>(null) }
    var terminalJsToRun by remember { mutableStateOf<String?>(null) }

    if (terminalJsToRun != null) {
        val jsCode = terminalJsToRun!!
        AndroidView(
            factory = {
                WebView(it).apply {
                    settings.javaScriptEnabled = true
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            consoleMessage ?: return false
                            coroutineScope.launch {
                                terminalText = appendTerminalOutput(
                                    terminalText,
                                    formatPreviewRuntimeMessage(
                                        level = consoleMessage.messageLevel().name.lowercase(),
                                        message = consoleMessage.message().orEmpty(),
                                        source = consoleMessage.sourceId().orEmpty().ifBlank { "terminal.js" },
                                        line = consoleMessage.lineNumber(),
                                        column = 0,
                                    ),
                                )
                            }
                            return true
                        }
                    }
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun log(msg: String) {
                            coroutineScope.launch {
                                terminalText = appendTerminalOutput(terminalText, msg)
                            }
                        }
                        @JavascriptInterface
                        fun error(msg: String) {
                            coroutineScope.launch {
                                terminalText = appendTerminalOutput(
                                    terminalText,
                                    formatPreviewRuntimeMessage("error", msg, "terminal.js", 0, 0),
                                )
                            }
                        }
                    }, "terminal")
                }
            },
            update = { webView ->
                val wrappedJs = """
                    (function() {
                        console.log = function() { terminal.log(Array.from(arguments).join(' ')); };
                        console.warn = function() { terminal.log('Warning: ' + Array.from(arguments).join(' ')); };
                        console.error = function() { terminal.error(Array.from(arguments).join(' ')); };
                        window.addEventListener('unhandledrejection', function(event) {
                            terminal.error('Unhandled promise rejection: ' + (event.reason && (event.reason.stack || event.reason.message || event.reason) || 'unknown'));
                        });
                        try {
                            $jsCode
                        } catch (e) {
                            terminal.error(e.stack || e.message || String(e));
                        }
                    })();
                """.trimIndent()
                webView.evaluateJavascript(wrappedJs, null)
                terminalJsToRun = null
            },
            modifier = Modifier.size(0.dp)
        )
    }

    fun applyLoadedFiles(
        loadedFiles: List<WebFile>,
        selectPath: String? = null,
        workingPath: String? = null,
    ) {
        val mergedFiles = loadedFiles.map { loadedFile ->
            val currentFile = files.firstOrNull { it.id == loadedFile.id }
            val pendingText = pendingEditorTextByFileId[loadedFile.id]
            if (pendingText != null) {
                loadedFile.copy(content = pendingText, modifiedAt = currentFile?.modifiedAt ?: loadedFile.modifiedAt)
            } else if (currentFile != null && currentFile.id in dirtyFileIds) {
                loadedFile.copy(content = currentFile.content, modifiedAt = currentFile.modifiedAt)
            } else {
                loadedFile
            }
        }
        files.clear()
        files.addAll(mergedFiles)
        val selectedFile = selectPath?.let { path -> mergedFiles.firstOrNull { samePath(it.filePath, path) } }
            ?: mergedFiles.firstOrNull { it.id == activeFileId }
            ?: mergedFiles.firstOrNull()
            ?: createDefaultWebFile(rootDirectory).also { files.add(it) }
        activeFileId = selectedFile.id
        workingDirectoryPath = workingPath ?: selectedFile.parentDirectoryPath(rootDirectory)
        directoryVersion += 1L
    }

    fun refreshFiles(selectPath: String? = null, workingPath: String? = null) {
        applyLoadedFiles(loadWebFiles(rootDirectory), selectPath, workingPath)
    }

    val openFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val name = getFileName(context, it) ?: "imported_file"
                    val target = File(workingDirectory, name)
                    copyUriToFile(context, it, target)
                }
                refreshFiles()
            }
        }
    }

    val openFolderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val docFile = DocumentFile.fromTreeUri(context, it)
                    docFile?.let { importFromDocumentFile(context, it, workingDirectory) }
                }
                refreshFiles()
            }
        }
    }

    val exportFolderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val docFile = DocumentFile.fromTreeUri(context, it)
                    docFile?.let { exportToDocumentFile(context, workingDirectory, it) }
                }
            }
        }
    }

    fun selectFile(fileId: Long) {
        val file = files.firstOrNull { it.id == fileId } ?: return
        activeFileId = file.id
        workingDirectoryPath = file.parentDirectoryPath(rootDirectory)
    }

    fun selectFolder(folder: FolderTarget) {
        workingDirectoryPath = folder.path
        expandedFolderPaths.addUniquePath(folder.path)
    }

    fun updateActiveContent(newCode: String) {
        val fileToSync = activeFile
        pendingEditorTextByFileId[fileToSync.id] = newCode
        dirtyFileIds += fileToSync.id
        pendingEditorSaveJobs.remove(fileToSync.id)?.cancel()
        pendingEditorSaveJobs[fileToSync.id] = coroutineScope.launch {
            delay(EDITOR_SAVE_DEBOUNCE_MS)
            withContext(Dispatchers.IO) { saveFileContent(fileToSync.filePath, newCode) }
            files.replaceFile(fileToSync.id) {
                copy(content = newCode, modifiedAt = System.currentTimeMillis())
            }
            if (pendingEditorTextByFileId[fileToSync.id] == newCode) {
                pendingEditorTextByFileId.remove(fileToSync.id)
            }
            dirtyFileIds.remove(fileToSync.id)
            pendingEditorSaveJobs.remove(fileToSync.id)
        }
    }

    fun createFile(rawName: String): String? {
        val cleanName = normalizeWebFileName(rawName) ?: return "Use a simple Web file name."
        val target = File(workingDirectory, cleanName).canonicalFile
        if (!isSameOrInside(rootDirectory, target)) return "File must stay inside this project."
        if (target.exists()) return "A file named $cleanName already exists here."
        target.parentFile?.mkdirs()
        target.writeText(defaultContentForFile(cleanName, rootDirectory.name))
        refreshFiles(selectPath = target.absolutePath, workingPath = target.parentFile?.absolutePath)
        return null
    }

    fun createFolder(rawName: String): String? {
        val cleanName = normalizeFolderName(rawName) ?: return "Use a simple folder name."
        val target = File(workingDirectory, cleanName).canonicalFile
        if (!isSameOrInside(rootDirectory, target)) return "Folder must stay inside this project."
        if (target.exists()) return "A folder named $cleanName already exists here."
        if (!target.mkdirs()) return "Could not create the folder."
        expandedFolderPaths.addUniquePath(target.absolutePath)
        directoryVersion += 1L
        return null
    }

    fun renameFile(file: WebFile, newName: String): String? {
        val cleanName = normalizeWebFileName(newName) ?: return "Use a simple Web file name."
        val source = File(file.filePath).canonicalFile
        val target = File(source.parentFile ?: rootDirectory, cleanName).canonicalFile
        if (!isSameOrInside(rootDirectory, source) || !isSameOrInside(rootDirectory, target)) {
            return "File must stay inside this project."
        }
        if (target.exists() && !samePath(source.absolutePath, target.absolutePath)) {
            return "A file named $cleanName already exists here."
        }
        if (!source.renameTo(target)) return "Could not rename the file."
        refreshFiles(selectPath = target.absolutePath, workingPath = target.parentFile?.absolutePath)
        return null
    }

    fun renameFolder(folder: FolderTarget, newName: String): String? {
        val cleanName = normalizeFolderName(newName) ?: return "Use a simple folder name."
        val source = File(folder.path).canonicalFile
        val target = File(source.parentFile ?: rootDirectory, cleanName).canonicalFile
        if (source == rootDirectory.canonicalFile) return "The project root cannot be renamed here."
        if (!isSameOrInside(rootDirectory, source) || !isSameOrInside(rootDirectory, target)) {
            return "Folder must stay inside this project."
        }
        if (target.exists() && !samePath(source.absolutePath, target.absolutePath)) {
            return "A folder named $cleanName already exists here."
        }
        if (!source.renameTo(target)) return "Could not rename the folder."
        expandedFolderPaths.removeAll { samePath(it, source.absolutePath) || it.startsWith(source.absolutePath + File.separator) }
        expandedFolderPaths.addUniquePath(target.absolutePath)
        refreshFiles(workingPath = target.absolutePath)
        return null
    }

    fun deleteFile(file: WebFile) {
        File(file.filePath).takeIf { isSameOrInside(rootDirectory, it) }?.delete()
        refreshFiles(workingPath = workingDirectory.absolutePath)
    }

    fun deleteFolder(folder: FolderTarget) {
        val directory = File(folder.path)
        if (samePath(directory.absolutePath, rootDirectory.absolutePath)) return
        if (isSameOrInside(rootDirectory, directory)) {
            directory.deleteRecursively()
            expandedFolderPaths.removeAll { samePath(it, directory.absolutePath) || it.startsWith(directory.absolutePath + File.separator) }
            refreshFiles(workingPath = rootDirectory.absolutePath)
        }
    }

    fun commitPendingEditorStateForPreview() {
        if (pendingEditorTextByFileId.isEmpty()) return
        val pendingSnapshot = pendingEditorTextByFileId.toMap()
        val modifiedAt = System.currentTimeMillis()
        pendingSnapshot.forEach { (fileId, pendingText) ->
            pendingEditorSaveJobs.remove(fileId)?.cancel()
            files.replaceFile(fileId) {
                copy(content = pendingText, modifiedAt = modifiedAt)
            }
            dirtyFileIds.remove(fileId)
        }
        pendingEditorTextByFileId.clear()
    }

    fun runWebPreview() {
        val previewFiles = files.withPendingEditorTexts(pendingEditorTextByFileId)
        val diagnostics = previewDiagnosticsForTerminal(rootDirectory, previewFiles)
        commitPendingEditorStateForPreview()
        terminalText = appendTerminalOutput(
            terminalText,
            buildString {
                append("$terminalPromptText preview ${activeFile.name}\nOpening responsive Web preview.")
                if (diagnostics.isNotBlank()) {
                    append("\n").append(diagnostics)
                }
            },
        )
        fullScreenMode = WebFullScreenMode.Preview
    }

    fun submitTerminalCommand(commandOverride: String? = null) {
        val command = commandOverride ?: terminalInput.trim()
        if (command.isBlank()) return
        terminalInput = ""
        terminalText = appendTerminalOutput(terminalText, "$terminalPromptText ${redactTerminalCommandForDisplay(command)}")
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                executeWebShellCommand(command, rootDirectory, terminalDirectory)
            }
            if (result.clearTerminal) {
                terminalText = ""
            } else {
                terminalText = appendTerminalOutput(terminalText, appendShellResult(result))
            }
            result.runJs?.let { terminalJsToRun = it }
            result.workingDirectoryPath?.let {
                terminalDirectoryPath = it
                workingDirectoryPath = it
            }
            if (result.workspaceChanged) refreshFiles(workingPath = terminalDirectoryPath)
            if (result.openPreview) {
                val previewFiles = files.withPendingEditorTexts(pendingEditorTextByFileId)
                val diagnostics = previewDiagnosticsForTerminal(rootDirectory, previewFiles)
                commitPendingEditorStateForPreview()
                if (diagnostics.isNotBlank()) {
                    terminalText = appendTerminalOutput(terminalText, diagnostics)
                }
                fullScreenMode = WebFullScreenMode.Preview
            }
        }
    }

    fun submitAiMessage() {
        val promptText = aiInput.trim()
        if (promptText.isBlank() || aiBusy) return
        val apiKey = userAiApiKey.trim()
        if (apiKey.isBlank()) {
            aiError = "Add a Gemini API key to use AI Chat."
            return
        }
        aiInput = ""
        aiError = null
        aiVerificationMessage = null
        aiMessages += AiChatMessage(AiChatRole.User, promptText)
        coroutineScope.launch {
            aiBusy = true
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    callGeminiDirectly(
                        modelId = selectedAiModel.id,
                        prompt = buildAiCopilotPrompt(
                            rootDirectory = rootDirectory,
                            files = files.withPendingEditorTexts(pendingEditorTextByFileId),
                            activeFile = activeFile.withPendingEditorText(pendingEditorTextByFileId),
                            userPrompt = promptText,
                            terminalText = terminalText,
                        ),
                        apiKey = apiKey,
                    )
                }
            }
            result
                .onSuccess { response ->
                    aiMessages += AiChatMessage(AiChatRole.Assistant, response)
                    aiPendingAction = suggestedActionFromAiText(
                        text = response,
                        activeFile = activeFile.withPendingEditorText(pendingEditorTextByFileId),
                    )
                }
                .onFailure { error ->
                    aiError = friendlyGeminiError(error.message.orEmpty())
                }
            aiBusy = false
        }
    }

    fun applyAiAction(action: AiSuggestedAction) {
        aiVerificationMessage = null
        when (action) {
            is AiSuggestedAction.RunTerminalCommand -> {
                fullScreenMode = WebFullScreenMode.Terminal
                submitTerminalCommand(action.command)
                aiVerificationMessage = "Started suggested terminal command."
            }
            is AiSuggestedAction.ReplaceActiveFile -> {
                updateActiveContent(action.code)
                aiVerificationMessage = "Replaced ${activeFile.name}."
            }
            is AiSuggestedAction.ApplyProjectFileChanges -> {
                runCatching {
                    applyAiProjectFileChanges(rootDirectory, action.changes)
                }.onSuccess { message ->
                    val selectedPath = action.changes
                        .firstOrNull { it.operation == AiProjectFileOperation.Write }
                        ?.let { File(rootDirectory, it.path).absolutePath }
                        ?: activeFile.filePath
                    refreshFiles(selectPath = selectedPath, workingPath = workingDirectory.absolutePath)
                    aiVerificationMessage = message
                }.onFailure { error ->
                    aiVerificationMessage = error.message ?: "Could not apply AI changes."
                }
            }
        }
        aiPendingAction = null
    }

    LaunchedEffect(
        activeFileId,
        workingDirectoryPath,
        terminalDirectoryPath,
        explorerVisible,
        fileSearch,
        terminalInput,
        terminalText,
        fullScreenMode,
        userAiApiKey,
        expandedFolderPaths.size,
    ) {
        saveWebIdeSession(
            context = context,
            rootDirectory = rootDirectory,
            session = WebSession(
                activeFileId = activeFileId,
                activeFilePath = activeFile.filePath,
                workingDirectoryPath = workingDirectoryPath,
                terminalDirectoryPath = terminalDirectoryPath,
                expandedFolderPaths = expandedFolderPaths.toList(),
                explorerVisible = explorerVisible,
                fileSearch = fileSearch,
                terminalInput = terminalInput,
                terminalText = terminalText,
                fullScreenMode = fullScreenMode,
                userAiApiKey = userAiApiKey.takeIf { it.isNotBlank() },
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.backgroundTop, palette.backgroundBottom)))
            .systemBarsPadding(),
    ) {
        when (fullScreenMode) {
            WebFullScreenMode.Editor -> WebEditorFullScreen(
                files = tabFiles,
                activeFile = activeFile,
                palette = palette,
                darkMode = ideTheme.isDark,
                nextThemeName = ideTheme.next().displayName,
                onToggleTheme = onToggleTheme,
                onSelectFile = ::selectFile,
                onAddFile = { newFileDialogOpen = true },
                onDeleteFile = { fileId ->
                    files.firstOrNull { it.id == fileId }?.let { deleteFileTarget = it }
                },
                onCodeChange = ::updateActiveContent,
                onRunPreview = ::runWebPreview,
                onBack = { fullScreenMode = null },
            )
            WebFullScreenMode.Terminal -> WebTerminalFullScreen(
                palette = palette,
                promptText = terminalPromptText,
                text = terminalText,
                input = terminalInput,
                onInputChange = { terminalInput = it },
                onSend = { submitTerminalCommand() },
                onBack = { fullScreenMode = null },
            )
            WebFullScreenMode.Ai -> AiChatFullScreen(
                palette = palette,
                modelOptions = aiModelOptions,
                selectedModel = selectedAiModel,
                onSelectedModel = { selectedAiModelId = it.id },
                messages = aiMessages,
                input = aiInput,
                onInputChange = { aiInput = it },
                userAiApiKey = userAiApiKey,
                onUserAiApiKeyChange = { userAiApiKey = it },
                busy = aiBusy,
                error = aiError,
                pendingAction = aiPendingAction,
                verificationMessage = aiVerificationMessage,
                onSend = ::submitAiMessage,
                onApplyAction = ::applyAiAction,
                onDismissAction = { aiPendingAction = null },
                onBack = { fullScreenMode = null },
            )
            WebFullScreenMode.Preview -> WebPreviewFullScreen(
                rootDirectory = rootDirectory,
                files = files.withPendingEditorTexts(pendingEditorTextByFileId),
                activeFile = activeFile.withPendingEditorText(pendingEditorTextByFileId),
                palette = palette,
                onRuntimeMessage = { message ->
                    terminalText = appendTerminalOutput(terminalText, message)
                },
                onBack = { fullScreenMode = null },
            )
            null -> WebIdeHomeScreen(
                files = files,
                tabFiles = tabFiles,
                activeFile = activeFile,
                projectName = rootDirectory.name,
                rootDirectory = rootDirectory,
                workingDirectory = workingDirectory,
                terminalPromptText = terminalPromptText,
                palette = palette,
                darkMode = ideTheme.isDark,
                nextThemeName = ideTheme.next().displayName,
                explorerVisible = explorerVisible,
                fileSearch = fileSearch,
                expandedFolderPaths = expandedFolderPaths,
                terminalText = terminalText,
                terminalInput = terminalInput,
                onToggleTheme = onToggleTheme,
                onBackToProjects = onBackToProjects,
                onToggleExplorer = { explorerVisible = !explorerVisible },
                onFileSearchChange = { fileSearch = it },
                onToggleFolder = { folder ->
                    if (expandedFolderPaths.any { samePath(it, folder.path) }) {
                        expandedFolderPaths.removeAll { samePath(it, folder.path) }
                    } else {
                        expandedFolderPaths.addUniquePath(folder.path)
                    }
                },
                onSelectFolder = ::selectFolder,
                onSelectFile = ::selectFile,
                onCodeChange = ::updateActiveContent,
                onRunPreview = ::runWebPreview,
                onOpenAi = { fullScreenMode = WebFullScreenMode.Ai },
                onFileActions = { fileActionsDialogOpen = true },
                onNewFile = { newFileDialogOpen = true },
                onNewFolder = { newFolderDialogOpen = true },
                onRenameFile = { renameFileTarget = it },
                onRenameFolder = { renameFolderTarget = it },
                onDeleteFile = { deleteFileTarget = it },
                onDeleteFolder = { deleteFolderTarget = it },
                onTerminalInputChange = { terminalInput = it },
                onTerminalCommandSubmit = { submitTerminalCommand() },
                onFullScreen = { fullScreenMode = it },
                directoryVersion = directoryVersion,
            )
        }
    }

    if (fileActionsDialogOpen) {
        FilesAndFoldersDialog(
            palette = palette,
            onDismiss = { fileActionsDialogOpen = false },
            onOpenFile = {
                fileActionsDialogOpen = false
                openFileLauncher.launch(arrayOf("*/*"))
            },
            onOpenFolder = {
                fileActionsDialogOpen = false
                openFolderLauncher.launch(null)
            },
            onSaveCurrentFolder = {
                fileActionsDialogOpen = false
                exportFolderLauncher.launch(null)
            }
        )
    }

    if (newFileDialogOpen) {
        NameInputDialog(
            title = "New Web file",
            label = "File name",
            initialValue = nextUntitledFileName(files),
            palette = palette,
            onDismiss = { newFileDialogOpen = false },
            onConfirm = { name -> createFile(name)?.also { return@NameInputDialog it }; newFileDialogOpen = false; null },
        )
    }
    if (newFolderDialogOpen) {
        NameInputDialog(
            title = "New folder",
            label = "Folder name",
            initialValue = "components",
            palette = palette,
            onDismiss = { newFolderDialogOpen = false },
            onConfirm = { name -> createFolder(name)?.also { return@NameInputDialog it }; newFolderDialogOpen = false; null },
        )
    }
    renameFileTarget?.let { file ->
        NameInputDialog(
            title = "Rename file",
            label = "File name",
            initialValue = file.name,
            palette = palette,
            onDismiss = { renameFileTarget = null },
            onConfirm = { name -> renameFile(file, name)?.also { return@NameInputDialog it }; renameFileTarget = null; null },
        )
    }
    renameFolderTarget?.let { folder ->
        NameInputDialog(
            title = "Rename folder",
            label = "Folder name",
            initialValue = folder.name,
            palette = palette,
            onDismiss = { renameFolderTarget = null },
            onConfirm = { name -> renameFolder(folder, name)?.also { return@NameInputDialog it }; renameFolderTarget = null; null },
        )
    }
    deleteFileTarget?.let { file ->
        DeleteConfirmDialog(
            title = "Delete ${file.name}?",
            body = "This permanently removes the file from this project.",
            palette = palette,
            onDismiss = { deleteFileTarget = null },
            onConfirm = {
                deleteFile(file)
                deleteFileTarget = null
            },
        )
    }
    deleteFolderTarget?.let { folder ->
        DeleteConfirmDialog(
            title = "Delete ${folder.name}?",
            body = "This permanently removes the folder and all of its files.",
            palette = palette,
            onDismiss = { deleteFolderTarget = null },
            onConfirm = {
                deleteFolder(folder)
                deleteFolderTarget = null
            },
        )
    }
}

@Composable
private fun WebIdeHomeScreen(
    files: List<WebFile>,
    tabFiles: List<WebFile>,
    activeFile: WebFile,
    projectName: String,
    rootDirectory: File,
    workingDirectory: File,
    terminalPromptText: String,
    palette: WebPalette,
    darkMode: Boolean,
    nextThemeName: String,
    explorerVisible: Boolean,
    fileSearch: String,
    expandedFolderPaths: List<String>,
    terminalText: String,
    terminalInput: String,
    onToggleTheme: () -> Unit,
    onBackToProjects: () -> Unit,
    onToggleExplorer: () -> Unit,
    onFileSearchChange: (String) -> Unit,
    onToggleFolder: (FolderTarget) -> Unit,
    onSelectFolder: (FolderTarget) -> Unit,
    onSelectFile: (Long) -> Unit,
    onCodeChange: (String) -> Unit,
    onRunPreview: () -> Unit,
    onOpenAi: () -> Unit,
    onFileActions: () -> Unit,
    onNewFile: () -> Unit,
    onNewFolder: () -> Unit,
    onRenameFile: (WebFile) -> Unit,
    onRenameFolder: (FolderTarget) -> Unit,
    onDeleteFile: (WebFile) -> Unit,
    onDeleteFolder: (FolderTarget) -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onTerminalCommandSubmit: () -> Unit,
    onFullScreen: (WebFullScreenMode) -> Unit,
    directoryVersion: Long,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 14.dp),
    ) {
        val pageScroll = rememberScrollState()
        val editorHeight = when {
            maxHeight < 740.dp -> 310.dp
            explorerVisible -> 390.dp
            else -> 430.dp
        }
        val terminalHeight = if (maxHeight < 740.dp) 190.dp else 220.dp
        var explorerFraction by rememberSaveable { mutableFloatStateOf(0.50f) }
        val layoutWidthPx = with(LocalDensity.current) { maxWidth.toPx() }.coerceAtLeast(1f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(pageScroll),
        ) {
            Spacer(Modifier.height(8.dp))
            IdeTopBar(
                projectName = projectName,
                palette = palette,
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                onBackToProjects = onBackToProjects,
                onToggleTheme = onToggleTheme,
                onOpenAi = onOpenAi,
                onFileActions = onFileActions,
            )
            Spacer(Modifier.height(10.dp))
            FileTabsBar(
                files = tabFiles,
                activeFileId = activeFile.id,
                palette = palette,
                onSelectFile = onSelectFile,
                onAddFile = onNewFile,
                onDeleteFile = { fileId ->
                    files.firstOrNull { it.id == fileId }?.let(onDeleteFile)
                },
                onToggleExplorer = onToggleExplorer,
            )
            Spacer(Modifier.height(10.dp))

            if (explorerVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExplorerPanel(
                        files = files,
                        activeFileId = activeFile.id,
                        rootDirectory = rootDirectory,
                        workingDirectory = workingDirectory,
                        search = fileSearch,
                        expandedFolderPaths = expandedFolderPaths,
                        palette = palette,
                        modifier = Modifier
                            .weight(explorerFraction)
                            .height(editorHeight),
                        onSearchChange = onFileSearchChange,
                        onToggleFolder = onToggleFolder,
                        onSelectFolder = onSelectFolder,
                        onSelectFile = onSelectFile,
                        onNewFile = onNewFile,
                        onNewFolder = onNewFolder,
                        onRenameFile = onRenameFile,
                        onRenameFolder = onRenameFolder,
                        onDeleteFile = onDeleteFile,
                        onDeleteFolder = onDeleteFolder,
                        directoryVersion = directoryVersion,
                    )
                    ResizeHandle(
                        palette = palette,
                        height = editorHeight,
                        onToggle = {
                            explorerFraction = if (explorerFraction > 0.50f) 0.35f else 0.62f
                        },
                        onDrag = { dragAmount ->
                            explorerFraction = (explorerFraction + dragAmount / layoutWidthPx).coerceIn(0.28f, 0.72f)
                        },
                    )
                    WebCodeEditorPanel(
                        file = activeFile,
                        palette = palette,
                        modifier = Modifier.weight(1f - explorerFraction),
                        height = editorHeight,
                        compact = true,
                        onCodeChange = onCodeChange,
                        onRunPreview = onRunPreview,
                        onFullScreen = { onFullScreen(WebFullScreenMode.Editor) },
                    )
                }
            } else {
                WebCodeEditorPanel(
                    file = activeFile,
                    palette = palette,
                    modifier = Modifier.fillMaxWidth(),
                    height = editorHeight,
                    compact = false,
                    onCodeChange = onCodeChange,
                    onRunPreview = onRunPreview,
                    onFullScreen = { onFullScreen(WebFullScreenMode.Editor) },
                )
            }

            Spacer(Modifier.height(12.dp))
            RunPreviewButton(palette = palette, onClick = onRunPreview)
            Spacer(Modifier.height(12.dp))
            WebTerminalPanel(
                palette = palette,
                promptText = terminalPromptText,
                text = terminalText,
                input = terminalInput,
                height = terminalHeight,
                onInputChange = onTerminalInputChange,
                onSend = onTerminalCommandSubmit,
                onFullScreen = { onFullScreen(WebFullScreenMode.Terminal) },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun IdeTopBar(
    projectName: String,
    palette: WebPalette,
    darkMode: Boolean,
    nextThemeName: String,
    onBackToProjects: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenAi: () -> Unit,
    onFileActions: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackToProjects,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(palette.elevatedPanel)
                .border(BorderStroke(1.dp, palette.border), CircleShape),
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = "Back to projects",
                tint = palette.text,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "IDE",
                color = palette.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                text = projectName,
                color = palette.subtleText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onFileActions, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Rounded.Code,
                contentDescription = "Files and folders",
                tint = palette.text,
                modifier = Modifier.size(21.dp),
            )
        }
        IconButton(onClick = onOpenAi, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "Open AI chat",
                tint = palette.accent,
                modifier = Modifier.size(21.dp),
            )
        }
        IdeThemeCycleButton(
            darkMode = darkMode,
            nextThemeName = nextThemeName,
            buttonColor = palette.elevatedPanel,
            borderColor = palette.border,
            contentColor = palette.text,
            onClick = onToggleTheme,
        )
    }
}

@Composable
private fun IdeThemeCycleButton(
    darkMode: Boolean,
    nextThemeName: String,
    buttonColor: Color,
    borderColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(40.dp),
    iconSize: Dp = 21.dp,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(buttonColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (darkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
            contentDescription = "Switch to $nextThemeName theme",
            tint = contentColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun ResizeHandle(
    palette: WebPalette,
    height: Dp,
    onToggle: () -> Unit,
    onDrag: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(16.dp)
            .height(height)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount -> onDrag(dragAmount) }
            }
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "||",
            color = palette.mutedText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ExplorerPanel(
    files: List<WebFile>,
    activeFileId: Long,
    rootDirectory: File,
    workingDirectory: File,
    search: String,
    expandedFolderPaths: List<String>,
    palette: WebPalette,
    modifier: Modifier = Modifier,
    directoryVersion: Long,
    onSearchChange: (String) -> Unit,
    onToggleFolder: (FolderTarget) -> Unit,
    onSelectFolder: (FolderTarget) -> Unit,
    onSelectFile: (Long) -> Unit,
    onNewFile: () -> Unit,
    onNewFolder: () -> Unit,
    onRenameFile: (WebFile) -> Unit,
    onRenameFolder: (FolderTarget) -> Unit,
    onDeleteFile: (WebFile) -> Unit,
    onDeleteFolder: (FolderTarget) -> Unit,
) {
    var addMenuOpen by remember { mutableStateOf(false) }

    Panel(
        palette = palette,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "EXPLORER",
                color = palette.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = relativeDirectoryLabel(rootDirectory, workingDirectory),
                color = palette.subtleText,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(
                    onClick = { addMenuOpen = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New item",
                        tint = palette.text,
                    )
                }
                DropdownMenu(
                    expanded = addMenuOpen,
                    onDismissRequest = { addMenuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Create file") },
                        leadingIcon = { Icon(Icons.Rounded.Description, null) },
                        onClick = {
                            addMenuOpen = false
                            onNewFile()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Create folder") },
                        leadingIcon = { Icon(Icons.Rounded.Folder, null) },
                        onClick = {
                            addMenuOpen = false
                            onNewFolder()
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SearchBox(
            value = search,
            palette = palette,
            onValueChange = onSearchChange,
        )
        Spacer(Modifier.height(10.dp))

        val entries = remember(
            expandedFolderPaths.size,
            search,
            files.size,
            workingDirectory.absolutePath,
            directoryVersion,
        ) {
            buildExplorerEntries(rootDirectory, files, expandedFolderPaths.toList(), search)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            entries.forEach { entry ->
                when (entry) {
                    is ExplorerEntry.Directory -> ExplorerFolderRow(
                        folder = entry.target,
                        depth = entry.depth,
                        expanded = entry.expanded,
                        selected = samePath(entry.target.path, workingDirectory.absolutePath),
                        palette = palette,
                        onToggle = { onToggleFolder(entry.target) },
                        onSelect = { onSelectFolder(entry.target) },
                        onCreateFile = {
                            onSelectFolder(entry.target)
                            onNewFile()
                        },
                        onCreateFolder = {
                            onSelectFolder(entry.target)
                            onNewFolder()
                        },
                        onRename = { onRenameFolder(entry.target) },
                        onDelete = { onDeleteFolder(entry.target) },
                    )

                    is ExplorerEntry.SourceFile -> ExplorerFileRow(
                        file = entry.file,
                        depth = entry.depth,
                        selected = entry.file.id == activeFileId,
                        palette = palette,
                        onSelect = { onSelectFile(entry.file.id) },
                        onDelete = { onDeleteFile(entry.file) },
                        onRename = { onRenameFile(entry.file) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBox(
    value: String,
    palette: WebPalette,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = palette.text,
            fontSize = 13.sp,
        ),
        cursorBrush = SolidColor(palette.accent),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(palette.panel)
                    .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(11.dp))
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = palette.subtleText,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(7.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = "Search files...",
                            color = palette.subtleText,
                            fontSize = 13.sp,
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun ExplorerFolderRow(
    folder: FolderTarget,
    depth: Int,
    expanded: Boolean,
    selected: Boolean,
    palette: WebPalette,
    onToggle: () -> Unit,
    onSelect: () -> Unit,
    onCreateFile: () -> Unit,
    onCreateFolder: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) palette.selectedTab else Color.Transparent)
            .clickable {
                onSelect()
                onToggle()
            }
            .padding(start = (4 + depth * 14).dp, end = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                onSelect()
                onToggle()
            },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = if (expanded) "Collapse folder" else "Expand folder",
                tint = palette.mutedText,
                modifier = Modifier.size(17.dp),
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Rounded.FolderOpen else Icons.Rounded.Folder,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = folder.name,
            color = palette.text,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "Folder options",
                    tint = palette.text,
                    modifier = Modifier.size(17.dp),
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Create file") },
                    leadingIcon = { Icon(Icons.Rounded.Description, null) },
                    onClick = {
                        menuOpen = false
                        onCreateFile()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Create folder") },
                    leadingIcon = { Icon(Icons.Rounded.Folder, null) },
                    onClick = {
                        menuOpen = false
                        onCreateFolder()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                    onClick = {
                        menuOpen = false
                        onRename()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}

@Composable
private fun ExplorerFileRow(
    file: WebFile,
    depth: Int,
    selected: Boolean,
    palette: WebPalette,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) palette.selectedTab else Color.Transparent)
            .clickable(onClick = onSelect)
            .padding(start = (20 + depth * 14).dp, end = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Description,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = file.name,
            color = palette.text,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "File options",
                    tint = palette.text,
                    modifier = Modifier.size(17.dp),
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                    onClick = {
                        menuOpen = false
                        onRename()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}

@Composable
private fun WebCodeEditorPanel(
    file: WebFile,
    palette: WebPalette,
    modifier: Modifier = Modifier,
    height: Dp = Dp.Unspecified,
    compact: Boolean = false,
    onCodeChange: (String) -> Unit,
    onRunPreview: () -> Unit,
    onFullScreen: () -> Unit,
    showKeyboardAccessory: Boolean = false,
    darkMode: Boolean? = null,
    nextThemeName: String = "",
    onToggleTheme: (() -> Unit)? = null,
) {
    val panelModifier = if (height == Dp.Unspecified) {
        modifier
    } else {
        modifier.height(height)
    }

    Panel(
        palette = palette,
        modifier = panelModifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Description,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = codeEditorTitle(file.name, compact),
                color = palette.text,
                fontSize = if (compact) 16.sp else 18.sp,
                lineHeight = 21.sp,
                maxLines = if (compact) 3 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (darkMode != null && onToggleTheme != null) {
                IdeThemeCycleButton(
                    darkMode = darkMode,
                    nextThemeName = nextThemeName,
                    buttonColor = palette.elevatedPanel,
                    borderColor = palette.border,
                    contentColor = palette.text,
                    onClick = onToggleTheme,
                    modifier = Modifier.size(28.dp),
                    iconSize = 17.dp,
                )
                Spacer(Modifier.width(4.dp))
            }
            IconButton(onClick = onFullScreen, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = "Fullscreen editor",
                    tint = palette.text,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalRule(palette)
        Spacer(Modifier.height(7.dp))
        SmartCodeEditor(
            file = file,
            palette = palette,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            showKeyboardAccessory = showKeyboardAccessory,
            onCodeChange = onCodeChange,
        )
    }
}

@Composable
private fun SmartCodeEditor(
    file: WebFile,
    palette: WebPalette,
    modifier: Modifier = Modifier,
    showKeyboardAccessory: Boolean = false,
    onCodeChange: (String) -> Unit,
) {
    var editorValue by remember(file.id) {
        mutableStateOf(TextFieldValue(file.content, selection = TextRange(file.content.length)))
    }
    var completionsVisible by remember(file.id) { mutableStateOf(false) }
    var suppressCompletionsForText by remember(file.id) { mutableStateOf<String?>(null) }
    var diagnostics by remember(file.id) { mutableStateOf<List<CodeDiagnostic>>(emptyList()) }
    var completionResult by remember(file.id) { mutableStateOf(EditorCompletionResult()) }
    var editorVisualResult by remember(file.id) {
        val initial = highlightWebSource(file.name, file.content, palette)
        mutableStateOf(EditorVisualResult(file.content, initial))
    }
    var textMetrics by remember(file.id) {
        mutableStateOf(editorTextMetrics(file.content, cap = 1_600))
    }
    var pendingCursorReveal by remember(file.id) { mutableLongStateOf(0L) }
    LaunchedEffect(file.id, file.content) {
        if (file.content != editorValue.text) {
            val cursor = min(editorValue.selection.start, file.content.length).coerceAtLeast(0)
            editorValue = TextFieldValue(file.content, selection = TextRange(cursor))
            completionResult = EditorCompletionResult()
            completionsVisible = false
        }
    }
    LaunchedEffect(file.name, editorValue.text, editorValue.selection) {
        val snapshot = editorValue.text
        val cursor = editorValue.selection.start
        val fileName = file.name
        val result = withContext(Dispatchers.Default) {
            val context = completionContextAt(snapshot, cursor, fileName)
            EditorCompletionResult(
                context = context,
                items = context?.let { webCompletionsForContext(fileName, snapshot, it) }.orEmpty(),
                source = snapshot,
                cursor = cursor,
                signatureHelp = if (shouldComputeSignatureHelp(snapshot, cursor)) {
                    webSignatureHelpAt(fileName, snapshot, cursor)
                } else {
                    null
                },
            )
        }
        if (snapshot == editorValue.text && cursor == editorValue.selection.start) {
            completionResult = result
            completionsVisible = result.context != null &&
                    result.items.isNotEmpty() &&
                    suppressCompletionsForText != snapshot
        }
    }
    val completionSurfaceVisible by remember {
        derivedStateOf {
            completionsVisible &&
                    completionResult.context != null &&
                    completionResult.items.isNotEmpty() &&
                    completionResult.source == editorValue.text &&
                    completionResult.cursor == editorValue.selection.start &&
                    suppressCompletionsForText != editorValue.text
        }
    }
    LaunchedEffect(file.name, editorValue.text) {
        val snapshot = editorValue.text
        if (snapshot.length > LIVE_DIAGNOSTIC_CHAR_LIMIT) {
            diagnostics = emptyList()
            return@LaunchedEffect
        }
        delay(DIAGNOSTIC_IDLE_DELAY_MS)
        val inspected = withContext(Dispatchers.Default) {
            inspectWebSource(file.name, snapshot)
        }
        if (snapshot == editorValue.text) {
            diagnostics = inspected
        }
    }
    LaunchedEffect(file.name, editorValue.text, palette, diagnostics) {
        val snapshot = editorValue.text
        val fileName = file.name
        val snapshotDiagnostics = diagnostics
        if (snapshot.length > LIVE_SYNTAX_HIGHLIGHT_CHAR_LIMIT) {
            editorVisualResult = EditorVisualResult(snapshot, AnnotatedString(snapshot))
            return@LaunchedEffect
        }
        delay(SYNTAX_HIGHLIGHT_IDLE_DELAY_MS)
        val visualText = withContext(Dispatchers.Default) {
            addDiagnosticStyles(
                source = snapshot,
                highlighted = highlightWebSource(fileName, snapshot, palette),
                palette = palette,
                diagnostics = snapshotDiagnostics,
            )
        }
        if (snapshot == editorValue.text) {
            editorVisualResult = EditorVisualResult(snapshot, visualText)
        }
    }
    LaunchedEffect(editorValue.text) {
        val snapshot = editorValue.text
        val metrics = withContext(Dispatchers.Default) {
            editorTextMetrics(snapshot, cap = 1_600)
        }
        if (snapshot == editorValue.text) {
            textMetrics = metrics
        }
    }
    val editorVerticalScroll = rememberScrollState()
    val editorHorizontalScroll = rememberScrollState()
    val cursorBringIntoViewRequester = remember { BringIntoViewRequester() }

    var lastTextLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    LaunchedEffect(pendingCursorReveal) {
        if (pendingCursorReveal > 0L) {
            delay(CURSOR_REVEAL_DELAY_MS)
            cursorBringIntoViewRequester.bringIntoView()
        }
    }

    LaunchedEffect(completionSurfaceVisible) {
        if (completionSurfaceVisible) {
            // Trigger a scroll to ensure cursor is above the newly shown suggestion bar
            delay(CURSOR_REVEAL_DELAY_MS)
            cursorBringIntoViewRequester.bringIntoView()
        }
    }

    val lineCount = textMetrics.lineCount
    val lineNumbers = remember(lineCount, diagnostics, palette) {
        diagnosticLineNumberText(lineCount, diagnostics, palette)
    }
    val gutterWidth = remember(lineCount) {
        maxOf(20.dp, (lineCount.toString().length * 8 + 10).dp)
    }
    val maxLineCharacters = textMetrics.longestLineLength.coerceAtLeast(48)
    val editorContentWidth = maxOf(640.dp, (maxLineCharacters * 9).dp)
    val codeStyle = TextStyle(
        color = palette.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val density = LocalDensity.current
    val showQuickKeys = showKeyboardAccessory && WindowInsets.ime.getBottom(density) > 0
    val editorVisualTransformation = remember(editorVisualResult) {
        WebSyntaxTransformation(editorVisualResult.source, editorVisualResult.text)
    }
    fun selectCompletion(item: CompletionItem) {
        val context = completionResult.context ?: return
        if (completionResult.source != editorValue.text || completionResult.cursor != editorValue.selection.start) return
        val previousText = editorValue.text
        val inserted = applyCompletion(editorValue, context, item)
        editorValue = inserted
        completionResult = EditorCompletionResult()
        suppressCompletionsForText = inserted.text
        completionsVisible = false
        val insertedStart = context.replaceStart.coerceIn(0, inserted.text.length)
        val insertedEnd = inserted.selection.start.coerceIn(insertedStart, inserted.text.length)
        if (previousText != inserted.text && '\n' in inserted.text.substring(insertedStart, insertedEnd)) {
            pendingCursorReveal += 1L
        }
        onCodeChange(inserted.text)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .width(gutterWidth)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = lineNumbers,
                    color = palette.subtleText,
                    style = codeStyle,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(editorVerticalScroll)
                        .padding(end = 3.dp),
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(palette.strongBorder),
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(editorHorizontalScroll),
            ) {
                Box(
                    modifier = Modifier
                        .width(editorContentWidth)
                        .fillMaxHeight()
                        .heightIn(min = 240.dp)
                        .verticalScroll(editorVerticalScroll),
                ) {
                    if (editorValue.text.isBlank()) {
                        Text(
                            text = "Write your code here",
                            color = palette.subtleText,
                            style = codeStyle,
                        )
                    }
                    BasicTextField(
                        value = editorValue,
                        onValueChange = { requestedValue ->
                            val previousValue = editorValue
                            val textChanged = requestedValue.text != previousValue.text
                            val updatedValue = applySmartWebEditorChange(previousValue, requestedValue, file.name)

                            if (textChanged) {
                                // Fast-pass synchronous highlight to prevent flashing white
                                val fastVisual = highlightWebSource(file.name, updatedValue.text, palette)
                                editorVisualResult = EditorVisualResult(updatedValue.text, fastVisual)
                            }

                            val insertedSegment = if (textChanged && updatedValue.selection.collapsed) {
                                val start = minOf(previousValue.selection.start, previousValue.selection.end)
                                    .coerceIn(0, updatedValue.text.length)
                                val end = updatedValue.selection.start.coerceIn(start, updatedValue.text.length)
                                updatedValue.text.substring(start, end)
                            } else {
                                ""
                            }
                            editorValue = updatedValue
                            if (textChanged) {
                                suppressCompletionsForText = null
                                completionResult = EditorCompletionResult()
                                completionsVisible = false
                                if ('\n' in insertedSegment) {
                                    pendingCursorReveal += 1L
                                }
                            } else if (requestedValue.selection != previousValue.selection) {
                                completionsVisible = false
                            }
                            if (updatedValue.text != file.content) {
                                onCodeChange(updatedValue.text)
                            }
                        },
                        onTextLayout = { lastTextLayoutResult = it },
                        textStyle = codeStyle,
                        cursorBrush = SolidColor(palette.accent),
                        visualTransformation = editorVisualTransformation,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Cursor snap placeholder
                    val cursorRect = lastTextLayoutResult?.let { layout ->
                        if (layout.layoutInput.text.text == editorValue.text) {
                            val cursor = editorValue.selection.start.coerceIn(0, layout.layoutInput.text.length)
                            layout.getCursorRect(cursor)
                        } else null
                    }
                    if (cursorRect != null) {
                        val cursorTargetTop = with(density) { cursorRect.top.toDp() } - 24.dp
                        Box(
                            modifier = Modifier
                                .bringIntoViewRequester(cursorBringIntoViewRequester)
                                .width(1.dp)
                                .height(if (completionSurfaceVisible) 180.dp else 112.dp)
                                .offset(
                                    x = with(density) { cursorRect.left.toDp() },
                                    y = maxOf(0.dp, cursorTargetTop),
                                )
                        )
                    }
                    val currentSignatureHelp = completionResult.signatureHelp
                        ?.takeIf {
                            completionResult.source == editorValue.text &&
                                    completionResult.cursor == editorValue.selection.start
                        }
                    currentSignatureHelp?.let { signature ->
                        SignatureHelpChip(
                            signature = signature,
                            palette = palette,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 6.dp, bottom = 6.dp),
                        )
                    }
                }
            }
        }
        if (completionSurfaceVisible) {
            Spacer(Modifier.height(6.dp))
            CompletionSuggestionBar(
                completions = completionResult.items,
                palette = palette,
                modifier = Modifier.fillMaxWidth(),
                onSelect = { item -> selectCompletion(item) },
            )
        }
        if (showQuickKeys) {
            Spacer(Modifier.height(6.dp))
            EditorQuickKeys(
                palette = palette,
                editorValue = editorValue,
                fileName = file.name,
                onEditorValueChange = { updated ->
                    editorValue = updated
                    suppressCompletionsForText = updated.text
                    completionsVisible = false
                    onCodeChange(updated.text)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (diagnostics.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            InspectionStrip(
                diagnostics = diagnostics,
                palette = palette,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SignatureHelpChip(
    signature: String,
    palette: WebPalette,
    modifier: Modifier = Modifier,
) {
    Text(
        text = signature,
        color = palette.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.elevatedPanel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}

@Composable
private fun CompletionSuggestionBar(
    completions: List<CompletionItem>,
    palette: WebPalette,
    modifier: Modifier = Modifier,
    onSelect: (CompletionItem) -> Unit,
) {
    val rows = remember(completions) { completions.take(16).chunked(3) }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(4.dp),
        modifier = modifier
            .heightIn(min = 42.dp, max = 124.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.elevatedPanel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(10.dp)),
    ) {
        items(
            items = rows,
            key = { row -> row.joinToString("|") { it.completionKey() } },
        ) { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                rowItems.forEach { item ->
                    CompletionSuggestionChip(
                        item = item,
                        palette = palette,
                        onSelect = { onSelect(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionSuggestionChip(
    item: CompletionItem,
    palette: WebPalette,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(34.dp)
            .width(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.panel)
            .border(BorderStroke(1.dp, palette.strongBorder), RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(horizontal = 8.dp),
    ) {
        Text(
            text = item.label,
            color = palette.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (item.detail.isNotBlank()) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = item.detail,
                color = palette.subtleText,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(48.dp),
            )
        }
    }
}

@Composable
private fun InspectionStrip(
    diagnostics: List<CodeDiagnostic>,
    palette: WebPalette,
    modifier: Modifier = Modifier,
) {
    val sortedDiagnostics = remember(diagnostics) {
        diagnostics.sortedWith(
            compareBy<CodeDiagnostic> { diagnosticSeverityRank(it.severity) }
                .thenBy { it.line }
                .thenBy { it.column }
        )
    }
    val highestSeverity = sortedDiagnostics.firstOrNull()?.severity ?: "hint"

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.elevatedPanel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier
                .heightIn(max = 42.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            sortedDiagnostics.forEach { diagnostic ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(18.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(diagnosticColor(diagnostic.severity, palette)),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "L${diagnostic.line}: ${diagnostic.message}",
                        color = if (diagnostic.severity == highestSeverity) palette.text else palette.mutedText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorQuickKeys(
    palette: WebPalette,
    editorValue: TextFieldValue,
    fileName: String,
    modifier: Modifier = Modifier,
    onEditorValueChange: (TextFieldValue) -> Unit,
) {
    val language = remember(fileName) { webLanguageForFile(fileName) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        QuickKey("Tab", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "    ")) }

        when (language) {
            WebEditorLanguage.Html -> {
                QuickKey("<>", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "<", ">")) }
                QuickKey("</>", palette) { onEditorValueChange(insertClosingHtmlTag(editorValue)) }
            }
            WebEditorLanguage.Css -> {
                QuickKey(":", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, ":")) }
                QuickKey(";", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, ";")) }
                QuickKey("#", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "#")) }
                QuickKey("()", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "(", ")")) }
            }
            WebEditorLanguage.JavaScript -> {
                QuickKey("()", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "(", ")")) }
                QuickKey("{}", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "{", "}")) }
                QuickKey("[]", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "[", "]")) }
                QuickKey("=>", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "=> ")) }
                QuickKey(";", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, ";")) }
            }
            else -> {}
        }

        if (language != WebEditorLanguage.Html) {
            QuickKey("{}", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "{", "}")) }
        }

        QuickKey("\"\"", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "\"", "\"")) }
        QuickKey("''", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "'", "'")) }
        QuickKey("=", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "=")) }
        QuickKey(".", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, ".")) }
        QuickKey("/", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "/")) }

        if (language == WebEditorLanguage.JavaScript) {
            QuickKey("!", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "!")) }
            QuickKey("&", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "&")) }
            QuickKey("|", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "|")) }
        }
    }
}

@Composable
private fun QuickKey(
    label: String,
    palette: WebPalette,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(palette.selectedTab)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = palette.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun WebEditorFullScreen(
    files: List<WebFile>,
    activeFile: WebFile,
    palette: WebPalette,
    darkMode: Boolean,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    onSelectFile: (Long) -> Unit,
    onAddFile: () -> Unit,
    onDeleteFile: (Long) -> Unit,
    onCodeChange: (String) -> Unit,
    onRunPreview: () -> Unit,
    onBack: () -> Unit,
) {
    val density = LocalDensity.current
    val keyboardVisible = WindowInsets.ime.getBottom(density) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 14.dp)
            .padding(bottom = 10.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        FileTabsOnlyBar(
            files = files,
            activeFileId = activeFile.id,
            palette = palette,
            onSelectFile = onSelectFile,
            onAddFile = onAddFile,
            onDeleteFile = onDeleteFile,
        )
        Spacer(Modifier.height(10.dp))
        WebCodeEditorPanel(
            file = activeFile,
            palette = palette,
            modifier = Modifier.weight(1f),
            onCodeChange = onCodeChange,
            onRunPreview = onRunPreview,
            onFullScreen = onBack,
            showKeyboardAccessory = true,
            darkMode = darkMode,
            nextThemeName = nextThemeName,
            onToggleTheme = onToggleTheme,
        )
        if (!keyboardVisible) {
            Spacer(Modifier.height(12.dp))
            RunPreviewButton(
                palette = palette,
                onClick = onRunPreview,
            )
        }
    }
}

@Composable
private fun RunPreviewButton(palette: WebPalette, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = palette.run, contentColor = palette.runText),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(54.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(25.dp),
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = "Run Code",
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun WebTerminalPanel(
    palette: WebPalette,
    promptText: String,
    text: String,
    input: String,
    height: Dp = 220.dp,
    modifier: Modifier = Modifier,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onFullScreen: () -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    var terminalFocused by remember { mutableStateOf(false) }

    fun bringTerminalIntoView() {
        coroutineScope.launch {
            delay(120)
            bringIntoViewRequester.bringIntoView()
        }
    }

    LaunchedEffect(terminalFocused, imeBottom) {
        if (terminalFocused && imeBottom > 0) {
            delay(120)
            bringIntoViewRequester.bringIntoView()
        }
    }

    Panel(
        palette = palette,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .bringIntoViewRequester(bringIntoViewRequester),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Terminal Output",
                color = palette.text,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onFullScreen, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = "Fullscreen terminal",
                    tint = palette.text,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        HorizontalRule(palette)
        Spacer(Modifier.height(7.dp))
        TerminalBody(
            terminalText = text,
            terminalInput = input,
            palette = palette,
            promptText = promptText,
            isRunning = false,
            keepInputVisibleWhileRunning = true,
            isAwaitingInput = false,
            onTerminalFocusChange = { focused ->
                terminalFocused = focused
                if (focused) bringTerminalIntoView()
            },
            onTerminalInputChange = onInputChange,
            onTerminalCommandSubmit = onSend,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun TerminalBody(
    terminalText: String,
    terminalInput: String,
    palette: WebPalette,
    promptText: String,
    isRunning: Boolean,
    keepInputVisibleWhileRunning: Boolean,
    isAwaitingInput: Boolean,
    modifier: Modifier = Modifier,
    onTerminalFocusChange: (Boolean) -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onTerminalCommandSubmit: () -> Unit,
) {
    val outputVerticalScroll = rememberScrollState()
    val outputHorizontalScroll = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val inputFocusRequester = remember { FocusRequester() }
    val inputBringIntoViewRequester = remember { BringIntoViewRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val showInputRow = !isRunning || isAwaitingInput || keepInputVisibleWhileRunning
    val activeInputLineStart = remember(terminalText, isAwaitingInput) {
        if (isAwaitingInput) terminalText.lastIndexOf('\n') else -1
    }
    val rawDisplayedTerminalText = remember(terminalText, isAwaitingInput, activeInputLineStart) {
        if (isAwaitingInput) {
            when {
                terminalText.isBlank() -> ""
                activeInputLineStart >= 0 -> terminalText.substring(0, activeInputLineStart)
                else -> ""
            }
        } else {
            terminalText
        }
    }
    val activeInputPrefix = remember(terminalText, promptText, isAwaitingInput, isRunning, keepInputVisibleWhileRunning, activeInputLineStart) {
        if (isAwaitingInput) {
            when {
                terminalText.isBlank() -> ""
                activeInputLineStart >= 0 -> terminalText.substring(activeInputLineStart + 1)
                else -> terminalText
            }
        } else if (isRunning && !keepInputVisibleWhileRunning) {
            ""
        } else {
            "$promptText "
        }
    }
    val displayedTerminalText = remember(rawDisplayedTerminalText, promptText, isAwaitingInput) {
        terminalDisplayText(rawDisplayedTerminalText, promptText, keepLastPrompt = isAwaitingInput)
    }
    val parsedTerminalText = remember(displayedTerminalText, promptText, palette) {
        parseAnsiTerminalOutput(displayedTerminalText, promptText, palette)
    }
    val terminalTextStyle = TextStyle(
        color = palette.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )
    val terminalInputPromptStyle = terminalTextStyle.copy(
        color = palette.terminalPrompt,
        fontWeight = FontWeight.SemiBold,
    )
    val longestTerminalLineLength = remember(displayedTerminalText, activeInputPrefix, terminalInput, showInputRow) {
        val outputLength = estimatedLongestLineLength(displayedTerminalText, cap = 4_000)
        val inputLength = if (showInputRow) activeInputPrefix.length + terminalInput.length + 1 else 0
        maxOf(outputLength, inputLength, 1).coerceAtMost(4_000)
    }

    fun focusTerminalInput() {
        if (!showInputRow) return
        inputFocusRequester.requestFocus()
        keyboardController?.show()
        onTerminalFocusChange(true)
        coroutineScope.launch {
            delay(100)
            inputBringIntoViewRequester.bringIntoView()
        }
    }

    LaunchedEffect(isAwaitingInput, showInputRow) {
        if (isAwaitingInput && showInputRow) {
            delay(60)
            focusTerminalInput()
        }
    }

    LaunchedEffect(displayedTerminalText, showInputRow, outputVerticalScroll.maxValue) {
        if (showInputRow || isRunning) {
            delay(20)
            outputVerticalScroll.scrollTo(outputVerticalScroll.maxValue)
        }
    }

    LaunchedEffect(terminalInput, activeInputPrefix, showInputRow, terminalText) {
        if (showInputRow && (terminalInput.isNotEmpty() || terminalText.endsWith('\n'))) {
            delay(24)
            inputBringIntoViewRequester.bringIntoView()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val estimatedContentWidth = (longestTerminalLineLength * 8.2f).dp + 20.dp
        val terminalContentWidth = maxOf(maxWidth, estimatedContentWidth)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(outputHorizontalScroll)
        ) {
            Box(
                modifier = Modifier
                    .width(terminalContentWidth)
                    .fillMaxHeight()
                    .verticalScroll(outputVerticalScroll),
            ) {
                if (showInputRow) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(showInputRow) {
                                detectTapGestures(onTap = { focusTerminalInput() })
                            },
                    )
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (displayedTerminalText.isNotBlank()) {
                        SelectionContainer {
                            Text(
                                text = parsedTerminalText,
                                style = terminalTextStyle,
                                softWrap = false,
                                overflow = TextOverflow.Visible,
                            )
                        }
                    }
                    if (showInputRow) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding()
                                .bringIntoViewRequester(inputBringIntoViewRequester),
                        ) {
                            Text(
                                text = activeInputPrefix,
                                style = if (isAwaitingInput) terminalTextStyle else terminalInputPromptStyle,
                                modifier = Modifier.alignByBaseline(),
                            )
                            BasicTextField(
                                value = terminalInput,
                                onValueChange = onTerminalInputChange,
                                textStyle = terminalTextStyle,
                                cursorBrush = SolidColor(palette.accent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = { onTerminalCommandSubmit() },
                                    onDone = { onTerminalCommandSubmit() },
                                    onGo = { onTerminalCommandSubmit() },
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 24.dp)
                                    .alignByBaseline()
                                    .focusRequester(inputFocusRequester)
                                    .onFocusChanged { focusState -> onTerminalFocusChange(focusState.isFocused) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebTerminalFullScreen(
    palette: WebPalette,
    promptText: String,
    text: String,
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = palette.text) }
            Text("Terminal", color = palette.text, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            color = palette.panel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier.fillMaxSize(),
        ) {
            TerminalBody(
                terminalText = text,
                terminalInput = input,
                palette = palette,
                promptText = promptText,
                isRunning = false,
                keepInputVisibleWhileRunning = true,
                isAwaitingInput = false,
                onTerminalFocusChange = {},
                onTerminalInputChange = onInputChange,
                onTerminalCommandSubmit = onSend,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

@Composable
private fun AiChatFullScreen(
    palette: WebPalette,
    modelOptions: List<AiModelOption>,
    selectedModel: AiModelOption,
    onSelectedModel: (AiModelOption) -> Unit,
    messages: List<AiChatMessage>,
    input: String,
    onInputChange: (String) -> Unit,
    userAiApiKey: String,
    onUserAiApiKeyChange: (String) -> Unit,
    busy: Boolean,
    error: String?,
    pendingAction: AiSuggestedAction?,
    verificationMessage: String?,
    onSend: () -> Unit,
    onApplyAction: (AiSuggestedAction) -> Unit,
    onDismissAction: () -> Unit,
    onBack: () -> Unit,
) {
    val aiEnabled = userAiApiKey.isNotBlank() && !busy

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Panel(
            palette = palette,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            var showNotice by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(60_000)
                showNotice = false
            }

            AiChatHeader(
                palette = palette,
                selectedModel = selectedModel,
                modelOptions = modelOptions,
                onSelectModel = onSelectedModel,
                onBack = onBack,
            )
            if (showNotice) {
                Spacer(Modifier.height(8.dp))
                HorizontalRule(palette)
                Spacer(Modifier.height(8.dp))
                AiContextNotice(
                    palette = palette,
                    selectedModel = selectedModel,
                    hasApiKey = userAiApiKey.isNotBlank(),
                    verificationMessage = verificationMessage,
                )
            }
            if (userAiApiKey.isBlank()) {
                Spacer(Modifier.height(8.dp))
                AiPersonalKeySection(
                    palette = palette,
                    currentKey = userAiApiKey,
                    onKeyChange = onUserAiApiKeyChange,
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages) { message ->
                    AiMessageBubble(message = message, palette = palette)
                }
                if (busy) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = palette.accent,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Thinking with ${selectedModel.label}...", color = palette.mutedText, fontSize = 13.sp)
                        }
                    }
                }
            }
            pendingAction?.let { action ->
                Spacer(Modifier.height(8.dp))
                AiActionApproval(
                    palette = palette,
                    action = action,
                    onApply = { onApplyAction(action) },
                    onDismiss = onDismissAction,
                )
            }
            error?.let {
                Spacer(Modifier.height(6.dp))
                Text(text = it, color = palette.error, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            AiComposer(
                palette = palette,
                input = input,
                enabled = aiEnabled,
                onInputChange = onInputChange,
                onSend = onSend,
            )
        }
    }
}

@Composable
private fun AiChatHeader(
    palette: WebPalette,
    selectedModel: AiModelOption,
    modelOptions: List<AiModelOption>,
    onSelectModel: (AiModelOption) -> Unit,
    onBack: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = palette.accent, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("AI Chat", color = palette.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(selectedModel.label, color = palette.mutedText, fontSize = 12.sp)
        }
        Box {
            OutlinedButton(onClick = { menuOpen = true }) {
                Text(selectedModel.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = palette.elevatedPanel,
            ) {
                modelOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(option.label, color = palette.text)
                                Text(option.description, color = palette.mutedText, fontSize = 11.sp)
                            }
                        },
                        onClick = {
                            onSelectModel(option)
                            menuOpen = false
                        },
                    )
                }
            }
        }
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.Close, contentDescription = "Close AI chat", tint = palette.text)
        }
    }
}

@Composable
private fun AiContextNotice(
    palette: WebPalette,
    selectedModel: AiModelOption,
    hasApiKey: Boolean,
    verificationMessage: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.elevatedPanel)
            .border(1.dp, palette.border, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Text(
            text = "Using ${selectedModel.label}. Project files, active editor text, and recent terminal output can be sent to Gemini for this request.",
            color = palette.mutedText,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (hasApiKey) {
                "Personal Gemini API key active."
            } else {
                "Add a personal Gemini API key before using AI requests."
            },
            color = if (hasApiKey) palette.success else palette.warning,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        verificationMessage?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = palette.mutedText, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: AiChatMessage,
    palette: WebPalette,
) {
    val isUser = message.role == AiChatRole.User
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = if (isUser) "You" else "AI",
            color = palette.subtleText,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
        if (isUser) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.selectedTab)
                    .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                    .padding(10.dp),
            ) {
                SelectionContainer {
                    Text(
                        text = message.text,
                        color = palette.text,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
            }
        } else {
            AiAssistantResponse(
                text = message.text,
                palette = palette,
            )
        }
    }
}

@Composable
private fun AiAssistantResponse(
    text: String,
    palette: WebPalette,
) {
    val displayText = remember(text) { aiVisibleResponseText(text) }
    val action = remember(text) { suggestedActionFromAiText(text) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (displayText.isNotBlank()) {
            AiResponseSection(
                title = "Response",
                text = displayText,
                palette = palette,
            )
        }
        action?.let {
            AiActionSummarySection(
                action = it,
                palette = palette,
            )
        }
        if (displayText.isBlank() && action == null) {
            AiResponseSection(
                title = "Response",
                text = text,
                palette = palette,
            )
        }
    }
}

@Composable
private fun AiResponseSection(
    title: String,
    text: String,
    palette: WebPalette,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.elevatedPanel)
            .border(1.dp, palette.border, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Text(
            text = title,
            color = palette.subtleText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(5.dp))
        SelectionContainer {
            Text(
                text = text,
                color = palette.text,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun AiActionSummarySection(
    action: AiSuggestedAction,
    palette: WebPalette,
) {
    val title = when (action) {
        is AiSuggestedAction.RunTerminalCommand -> "Suggested Command"
        is AiSuggestedAction.ReplaceActiveFile -> "Prepared Edit"
        is AiSuggestedAction.ApplyProjectFileChanges -> "Proposed File Changes"
    }
    val subtitle = when (action) {
        is AiSuggestedAction.RunTerminalCommand -> "Ready to run in the project terminal."
        is AiSuggestedAction.ReplaceActiveFile -> "${action.code.lines().size} lines for the active file."
        is AiSuggestedAction.ApplyProjectFileChanges -> {
            val writes = action.changes.count { it.operation == AiProjectFileOperation.Write }
            val deletes = action.changes.count { it.operation == AiProjectFileOperation.Delete }
            listOfNotNull(
                writes.takeIf { it > 0 }?.let { "$it write${if (it == 1) "" else "s"}" },
                deletes.takeIf { it > 0 }?.let { "$it delete${if (it == 1) "" else "s"}" },
            ).joinToString(", ").ifBlank { "${action.changes.size} file change${if (action.changes.size == 1) "" else "s"}" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.panel)
            .border(1.dp, palette.strongBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Text(title, color = palette.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, color = palette.mutedText, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        when (action) {
            is AiSuggestedAction.RunTerminalCommand -> {
                Text(
                    text = action.command,
                    color = palette.text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
            is AiSuggestedAction.ReplaceActiveFile -> {
                Text(
                    text = action.code.lineSequence().take(8).joinToString("\n"),
                    color = palette.mutedText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            is AiSuggestedAction.ApplyProjectFileChanges -> {
                action.changes.take(8).forEach { change ->
                    AiActionFileRow(change = change, palette = palette)
                }
                val remaining = action.changes.size - 8
                if (remaining > 0) {
                    Spacer(Modifier.height(5.dp))
                    Text("+$remaining more", color = palette.subtleText, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AiActionFileRow(
    change: AiProjectFileChange,
    palette: WebPalette,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 24.dp),
    ) {
        val operation = when (change.operation) {
            AiProjectFileOperation.Write -> "write"
            AiProjectFileOperation.Delete -> "delete"
        }
        Text(
            text = operation,
            color = if (change.operation == AiProjectFileOperation.Delete) palette.error else palette.success,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(48.dp),
        )
        Text(
            text = change.path,
            color = palette.text,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AiPersonalKeySection(
    palette: WebPalette,
    currentKey: String,
    onKeyChange: (String) -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf(currentKey) }
    val uriHandler = LocalUriHandler.current

    Surface(
        color = palette.elevatedPanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.VpnKey,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Personal Gemini API Key",
                    color = palette.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (!isEditing) {
                    TextButton(
                        onClick = { isEditing = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp),
                    ) {
                        Text(if (currentKey.isBlank()) "Add Key" else "Change", fontSize = 12.sp)
                    }
                }
            }

            if (isEditing) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("API Key from AI Studio", fontSize = 12.sp) },
                    placeholder = { Text("Paste your key here", fontSize = 12.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { isEditing = false; keyInput = currentKey }) {
                        Text("Cancel", color = palette.mutedText, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            onKeyChange(keyInput.trim())
                            isEditing = false
                        },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text("Save Key", fontSize = 12.sp)
                    }
                }
            } else if (currentKey.isBlank()) {
                Text(
                    "Add your own free key to use AI Chat without signing in.",
                    color = palette.mutedText,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Text(
                    "Active: Key starting with ${currentKey.take(4)}...",
                    color = palette.accent,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (isEditing || currentKey.isBlank()) {
                ClickableText(
                    text = AnnotatedString("Get a free API key at Google AI Studio"),
                    style = TextStyle(color = palette.accent, fontSize = 11.sp, textDecoration = TextDecoration.Underline),
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = { uriHandler.openUri("https://aistudio.google.com/app/apikey") },
                )
            }
        }
    }
}

@Composable
private fun AiActionApproval(
    palette: WebPalette,
    action: AiSuggestedAction,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val title = when (action) {
        is AiSuggestedAction.RunTerminalCommand -> "Run suggested terminal command?"
        is AiSuggestedAction.ReplaceActiveFile -> "Replace active file with suggested code?"
        is AiSuggestedAction.ApplyProjectFileChanges -> "Apply suggested project file changes?"
    }
    val body = when (action) {
        is AiSuggestedAction.RunTerminalCommand -> action.command
        is AiSuggestedAction.ReplaceActiveFile -> action.code.lineSequence().take(12).joinToString("\n")
        is AiSuggestedAction.ApplyProjectFileChanges -> action.changes.joinToString("\n") { change ->
            val label = when (change.operation) {
                AiProjectFileOperation.Write -> "write"
                AiProjectFileOperation.Delete -> "delete"
            }
            "$label ${change.path}"
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.elevatedPanel)
            .border(1.dp, palette.strongBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Text(title, color = palette.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            color = palette.mutedText,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            maxLines = 12,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss")
            }
            OutlinedButton(onClick = onApply) {
                Text(
                    text = when (action) {
                        is AiSuggestedAction.RunTerminalCommand -> "Run"
                        is AiSuggestedAction.ReplaceActiveFile -> "Replace"
                        is AiSuggestedAction.ApplyProjectFileChanges -> "Apply"
                    },
                    color = palette.accent,
                )
            }
        }
    }
}

@Composable
private fun AiComposer(
    palette: WebPalette,
    input: String,
    enabled: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        BasicTextField(
            value = input,
            onValueChange = onInputChange,
            enabled = enabled,
            textStyle = TextStyle(
                color = if (enabled) palette.text else palette.mutedText,
                fontSize = 14.sp,
                lineHeight = 19.sp,
            ),
            cursorBrush = SolidColor(palette.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp, max = 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.elevatedPanel)
                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    if (input.isBlank()) {
                        Text(
                            text = if (enabled) "Ask about this project..." else "Add a Gemini API key to use AI",
                            color = palette.subtleText,
                            fontSize = 14.sp,
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = enabled && input.isNotBlank(),
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (enabled && input.isNotBlank()) palette.accent else palette.tab),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Send AI message",
                tint = if (enabled && input.isNotBlank()) Color.White else palette.mutedText,
            )
        }
    }
}

@Composable
private fun WebPreviewFullScreen(
    rootDirectory: File,
    files: List<WebFile>,
    activeFile: WebFile,
    palette: WebPalette,
    onRuntimeMessage: (String) -> Unit,
    onBack: () -> Unit,
) {
    val latestRuntimeMessage = rememberUpdatedState(onRuntimeMessage)
    val previewFilesKey = files.joinToString("|") { file -> "${file.filePath}:${file.modifiedAt}:${file.content.hashCode()}" }
    var previewEntryFile by remember(rootDirectory.absolutePath) { mutableStateOf<File?>(null) }
    var previewError by remember(rootDirectory.absolutePath) { mutableStateOf<String?>(null) }

    LaunchedEffect(rootDirectory.absolutePath, previewFilesKey, activeFile.filePath, activeFile.content) {
        val result = withContext(Dispatchers.IO) {
            runCatching { prepareLivePreviewEntry(rootDirectory, files, activeFile) }
        }
        result.onSuccess { entryFile ->
            previewEntryFile = entryFile
            previewError = null
        }.onFailure { error ->
            previewEntryFile = null
            previewError = error.message ?: "Could not prepare the live preview."
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(palette.backgroundTop).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = palette.text) }
            Text("Responsive Preview", color = palette.text, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(activeFile.name, color = palette.subtleText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        previewError?.let { message ->
            Text(
                text = message,
                color = palette.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.elevatedPanel)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
        AndroidView(
            factory = {
                WebView(it).apply {
                    configureLivePreviewWebView(
                        webView = this,
                        onRuntimeMessage = { message -> latestRuntimeMessage.value(message) },
                    )
                }
            },
            update = { webView ->
                val entryFile = previewEntryFile
                if (entryFile == null) {
                    if (webView.tag != "preparing") {
                        webView.tag = "preparing"
                        webView.loadDataWithBaseURL(
                            null,
                            livePreviewStatusHtml("Preparing preview..."),
                            "text/html",
                            "UTF-8",
                            null,
                        )
                    }
                    return@AndroidView
                }
                val previewUrl = Uri.fromFile(entryFile).toString()
                if (webView.tag != previewUrl) {
                    webView.tag = previewUrl
                    loadLivePreviewUrl(webView, previewUrl)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Suppress("DEPRECATION")
private fun configureLivePreviewWebView(
    webView: WebView,
    onRuntimeMessage: (String) -> Unit,
) {
    webView.isFocusable = false
    webView.isFocusableInTouchMode = false
    webView.isLongClickable = false
    webView.setBackgroundColor(android.graphics.Color.WHITE)
    webView.webChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            consoleMessage ?: return false
            onRuntimeMessage(
                formatPreviewRuntimeMessage(
                    level = consoleMessage.messageLevel().name.lowercase(),
                    message = consoleMessage.message().orEmpty(),
                    source = consoleMessage.sourceId().orEmpty(),
                    line = consoleMessage.lineNumber(),
                    column = 0,
                )
            )
            return true
        }
    }
    webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return false
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            val url = request?.url?.toString().orEmpty()
            val description = error?.description?.toString().orEmpty()
            if (url.isNotBlank() || description.isNotBlank()) {
                onRuntimeMessage(
                    formatPreviewRuntimeMessage(
                        level = "error",
                        message = listOf("Resource load failed", description)
                            .filter { it.isNotBlank() }
                            .joinToString(": "),
                        source = url,
                        line = 0,
                        column = 0,
                    )
                )
            }
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            val url = request?.url?.toString().orEmpty()
            if (url.endsWith("/favicon.ico")) return
            onRuntimeMessage(
                formatPreviewRuntimeMessage(
                    level = "error",
                    message = "HTTP ${errorResponse?.statusCode ?: 0} ${errorResponse?.reasonPhrase.orEmpty()}",
                    source = url,
                    line = 0,
                    column = 0,
                )
            )
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            view?.let(::injectLivePreviewFitController)
        }
    }
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        allowFileAccess = true
        allowContentAccess = true
        allowFileAccessFromFileURLs = true
        allowUniversalAccessFromFileURLs = true
        javaScriptCanOpenWindowsAutomatically = false
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        mediaPlaybackRequiresUserGesture = false
        loadsImagesAutomatically = true
        blockNetworkLoads = false
        setSupportMultipleWindows(false)
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        textZoom = 100
        cacheMode = WebSettings.LOAD_NO_CACHE
        defaultTextEncodingName = "utf-8"
        useWideViewPort = true
        loadWithOverviewMode = false
    }
}

private fun prepareLivePreviewEntry(
    rootDirectory: File,
    files: List<WebFile>,
    activeFile: WebFile,
): File {
    val root = rootDirectory.canonicalFile
    // 1. Save all project files to disk so assets like images are available
    files.forEach { file ->
        val target = File(file.filePath).canonicalFile
        if (isSameOrInside(root, target)) {
            target.parentFile?.mkdirs()
            saveFileContent(target.absolutePath, file.content)
        }
    }

    // 2. Identify the base HTML
    val activeExtension = fileExtension(activeFile.name)
    val baseHtml = when {
        activeExtension in htmlFileExtensions -> activeFile.content
        activeExtension in cssFileExtensions -> {
            val indexFile = nearestIndexWebFile(root, files, File(activeFile.filePath))
            indexFile?.content ?: generatedShellHtml(activeFile)
        }
        activeExtension in javascriptFileExtensions -> {
            val indexFile = nearestIndexWebFile(root, files, File(activeFile.filePath))
            indexFile?.content ?: generatedShellHtml(activeFile)
        }
        else -> generatedShellHtml(activeFile)
    }

    // 3. Adopt GameCoded "Inlining" logic - Bundle all project CSS and JS into this one HTML string
    val bundledHtml = bundleProjectAssets(baseHtml, files, activeFile)

    // 4. Inject responsive baseline (from IdeActivity.kt)
    val finalHtml = injectResponsiveBaseline(bundledHtml)

    // 5. Write the final bundled HTML to a temp preview file and return it
    val previewDir = File(root, ".pocketcodedweb/preview").apply { mkdirs() }
    val entryFile = File(previewDir, "index.html")
    entryFile.writeText(finalHtml)
    return entryFile
}

private fun bundleProjectAssets(html: String, files: List<WebFile>, activeFile: WebFile): String {
    val cssBlock = files
        .filter { fileExtension(it.name) in cssFileExtensions }
        .joinToString("\n\n") { "/* ${it.name} */\n${it.content}" }
        .trim()

    val jsBlock = files
        .filter { fileExtension(it.name) in javascriptFileExtensions }
        .joinToString("\n\n") { "// ${it.name}\n${it.content}" }
        .replace("</script", "<\\/script", ignoreCase = true)
        .trim()

    var result = html
    if (cssBlock.isNotBlank()) {
        result = injectIntoHeadEnd(result, "\n<style id=\"pcw-bundled-css\">\n$cssBlock\n</style>\n")
    }
    if (jsBlock.isNotBlank()) {
        result = injectIntoBodyEnd(result, "\n<script id=\"pcw-bundled-js\">\n$jsBlock\n</script>\n")
    }
    return result
}

private fun injectIntoHeadStart(html: String, content: String): String {
    val headOpen = Regex("""(?is)<head\b[^>]*>""").find(html)
    return if (headOpen != null) {
        html.replaceRange(headOpen.range.last + 1, headOpen.range.last + 1, content)
    } else if (html.contains("<html", ignoreCase = true)) {
        html.replace(Regex("<html([^>]*)>", RegexOption.IGNORE_CASE), "<html$1><head>$content</head>")
    } else {
        "<!DOCTYPE html><html><head>$content</head><body>$html</body></html>"
    }
}

private fun injectLivePreviewFitController(webView: WebView) {
    webView.evaluateJavascript(livePreviewFitControllerScript(), null)
}

private fun livePreviewFitControllerScript(): String {
    return """
        (function () {
            if (window.__pcwLivePreviewFitInstalled) {
                if (typeof window.__pcwLivePreviewApplyFit === "function") {
                    window.__pcwLivePreviewApplyFit();
                }
                return;
            }
            window.__pcwLivePreviewFitInstalled = true;

            var previousTarget = null;
            var scheduledFrame = 0;

            function addViewportMeta() {
                if (document.querySelector("meta[name='viewport']")) return;
                var meta = document.createElement("meta");
                meta.name = "viewport";
                meta.content = "width=device-width, initial-scale=1.0, viewport-fit=cover";
                (document.head || document.documentElement).appendChild(meta);
            }

            function addPreviewStyle() {
                if (document.getElementById("pcw-live-preview-fit-style")) return;
                var style = document.createElement("style");
                style.id = "pcw-live-preview-fit-style";
                style.textContent = [
                    "html{min-width:0;-webkit-text-size-adjust:100%;}",
                    "body{min-width:0;}",
                    "img,video,canvas,svg,iframe{max-width:100%;}",
                    "[data-pcw-preview-fit-target='true']{will-change:transform;}"
                ].join("\n");
                (document.head || document.documentElement).appendChild(style);
            }

            function visibleElement(element) {
                if (!element || element.nodeType !== 1) return false;
                if (element === document.body || element === document.documentElement) return false;
                if (/^(SCRIPT|STYLE|META|LINK|TITLE|TEMPLATE)$/i.test(element.tagName)) return false;
                var style = window.getComputedStyle(element);
                if (style.display === "none" || style.visibility === "hidden" || Number(style.opacity) === 0) return false;
                var rect = element.getBoundingClientRect();
                return rect.width > 2 && rect.height > 2;
            }

            function descriptor(element) {
                return ((element.id || "") + " " + (element.className || "") + " " + element.tagName).toLowerCase();
            }

            function hasGameWords(element) {
                var text = descriptor(element);
                return text.indexOf("game") >= 0 ||
                    text.indexOf("canvas") >= 0 ||
                    text.indexOf("stage") >= 0 ||
                    text.indexOf("screen") >= 0 ||
                    text.indexOf("app") >= 0 ||
                    text.indexOf("root") >= 0;
            }

            function candidateScore(element) {
                if (!element) return 0;
                var text = descriptor(element);
                var score = 0;
                if (element.hasAttribute("data-preview-fit") || element.hasAttribute("data-pcw-fit")) score += 100;
                if (text.indexOf("game") >= 0) score += 12;
                if (text.indexOf("canvas") >= 0) score += 10;
                if (text.indexOf("stage") >= 0) score += 8;
                if (text.indexOf("screen") >= 0) score += 6;
                if (text.indexOf("app") >= 0 || text.indexOf("root") >= 0) score += 4;
                if (element.querySelector && element.querySelector("canvas, svg, iframe")) score += 14;
                if (/^(CANVAS|SVG|IFRAME)$/i.test(element.tagName)) score += 18;
                var rect = element.getBoundingClientRect();
                score += Math.min(10, Math.round((rect.width * rect.height) / 60000));
                return score;
            }

            function bestVisibleCandidate(selector) {
                return Array.prototype.slice.call(document.querySelectorAll(selector))
                    .filter(visibleElement)
                    .sort(function (left, right) {
                        return candidateScore(right) - candidateScore(left);
                    })[0] || null;
            }

            function visibleBodyChildren() {
                return Array.prototype.slice.call((document.body && document.body.children) || [])
                    .filter(visibleElement);
            }

            function findFitTarget() {
                var body = document.body;
                if (!body || body.dataset.pcwNoFit === "true" || document.documentElement.dataset.pcwNoFit === "true") return null;

                var explicit = bestVisibleCandidate("[data-preview-fit], [data-pcw-fit]");
                if (explicit && explicit !== body && explicit !== document.documentElement) return explicit;

                var canvas = bestVisibleCandidate("canvas, svg, iframe");
                if (canvas) {
                    var parent = canvas.parentElement;
                    if (parent && parent !== body && parent !== document.documentElement && visibleElement(parent)) {
                        if (hasGameWords(parent) || parent.children.length <= 8) return parent;
                    }
                    return canvas;
                }

                var selector = [
                    "#game",
                    "#game-container",
                    "#gameContainer",
                    "#gameCanvas",
                    "#canvas",
                    "#app",
                    "#root",
                    ".game",
                    ".game-container",
                    ".gameContainer",
                    ".canvas-container",
                    ".stage",
                    ".screen",
                    ".app",
                    ".root"
                ].join(",");
                var named = bestVisibleCandidate(selector);
                if (named && named !== body && named !== document.documentElement && candidateScore(named) >= 8) return named;

                var children = visibleBodyChildren();
                if (children.length === 1 && candidateScore(children[0]) >= 4) return children[0];
                return null;
            }

            function rememberStyle(element) {
                if (!element || element.dataset.pcwPreviewOriginalStyleSet === "true") return;
                element.dataset.pcwPreviewOriginalStyle = element.getAttribute("style") || "";
                element.dataset.pcwPreviewOriginalStyleSet = "true";
            }

            function restoreStyle(element) {
                if (!element || element.dataset.pcwPreviewOriginalStyleSet !== "true") return;
                var original = element.dataset.pcwPreviewOriginalStyle || "";
                if (original) {
                    element.setAttribute("style", original);
                } else {
                    element.removeAttribute("style");
                }
                delete element.dataset.pcwPreviewOriginalStyle;
                delete element.dataset.pcwPreviewOriginalStyleSet;
                delete element.dataset.pcwPreviewFitTarget;
            }

            function viewportSize() {
                var visual = window.visualViewport;
                return {
                    width: Math.max(1, Math.floor((visual && visual.width) || window.innerWidth || document.documentElement.clientWidth || 1)),
                    height: Math.max(1, Math.floor((visual && visual.height) || window.innerHeight || document.documentElement.clientHeight || 1))
                };
            }

            function naturalSize(target) {
                restoreStyle(target);
                var rect = target.getBoundingClientRect();
                return {
                    width: Math.max(rect.width, target.scrollWidth || 0, target.offsetWidth || 0),
                    height: Math.max(rect.height, target.scrollHeight || 0, target.offsetHeight || 0)
                };
            }

            function shouldFit(target) {
                if (!target || target === document.body || target === document.documentElement) return false;
                if (target.hasAttribute("data-preview-fit") || target.hasAttribute("data-pcw-fit")) return true;
                if (/^(CANVAS|SVG|IFRAME)$/i.test(target.tagName)) return true;
                if (target.querySelector && target.querySelector("canvas, svg, iframe")) return true;
                if (hasGameWords(target)) return true;
                return visibleBodyChildren().length === 1 && candidateScore(target) >= 4;
            }

            function applyFit() {
                addViewportMeta();
                addPreviewStyle();

                var target = findFitTarget();
                if (!shouldFit(target)) {
                    if (previousTarget) restoreStyle(previousTarget);
                    previousTarget = null;
                    return;
                }

                if (previousTarget && previousTarget !== target) restoreStyle(previousTarget);
                previousTarget = target;

                var size = naturalSize(target);
                if (size.width <= 2 || size.height <= 2) return;

                var viewport = viewportSize();
                var padding = viewport.width >= 900 ? 24 : (viewport.width >= 600 ? 16 : 8);
                var availableWidth = Math.max(1, viewport.width - padding * 2);
                var availableHeight = Math.max(1, viewport.height - padding * 2);
                var maxScale = viewport.width >= 900 ? 1.75 : (viewport.width >= 600 ? 1.45 : 1.15);
                var scale = Math.min(maxScale, availableWidth / size.width, availableHeight / size.height);
                if (!isFinite(scale) || scale <= 0) scale = 1;
                scale = Math.max(0.1, scale);

                rememberStyle(target);
                target.dataset.pcwPreviewFitTarget = "true";
                target.style.position = "fixed";
                target.style.left = Math.max(padding, Math.round((viewport.width - size.width * scale) / 2)) + "px";
                target.style.top = Math.max(padding, Math.round((viewport.height - size.height * scale) / 2)) + "px";
                target.style.width = size.width + "px";
                target.style.height = size.height + "px";
                target.style.maxWidth = "none";
                target.style.maxHeight = "none";
                target.style.margin = "0";
                target.style.transformOrigin = "top left";
                target.style.transform = "scale(" + scale + ")";
                target.style.zIndex = "2147483000";
            }

            function scheduleFit() {
                if (scheduledFrame) cancelAnimationFrame(scheduledFrame);
                scheduledFrame = requestAnimationFrame(function () {
                    scheduledFrame = requestAnimationFrame(applyFit);
                });
            }

            window.__pcwLivePreviewApplyFit = scheduleFit;
            scheduleFit();
            setTimeout(scheduleFit, 120);
            setTimeout(scheduleFit, 450);
            setTimeout(scheduleFit, 1200);
            window.addEventListener("load", scheduleFit);
            window.addEventListener("resize", scheduleFit);
            window.addEventListener("orientationchange", scheduleFit);
            if (window.visualViewport) {
                window.visualViewport.addEventListener("resize", scheduleFit);
                window.visualViewport.addEventListener("scroll", scheduleFit);
            }
            try {
                new MutationObserver(function () {
                    scheduleFit();
                }).observe(document.documentElement, { childList: true, subtree: true });
            } catch (error) {}
        })();
    """.trimIndent()
}

private fun injectResponsiveBaseline(html: String): String {
    val baseline = """
        <style id="pcw-responsive-baseline">
            html, body {
                width: 100%;
                min-height: 100%;
            }
            body {
                margin: 0 !important;
                padding: clamp(12px, 2.5vw, 24px) !important;
                font-size: clamp(15px, 1.9vw, 18px);
                font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                background: #ffffff;
                color: #111827;
            }
            img, video, iframe, canvas, svg {
                max-width: 100% !important;
                height: auto !important;
            }
            main, section, article, header, footer, nav, form, table, figure, blockquote, .preview-card {
                max-width: 100% !important;
                width: 100%;
                margin-left: auto;
                margin-right: auto;
            }
            pre, code {
                white-space: pre-wrap;
                word-break: break-word;
            }
        </style>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    """.trimIndent()

    return injectIntoHeadStart(html, baseline)
}

private fun generatedShellHtml(activeFile: WebFile): String {
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>${escapeHtml(activeFile.name)} Preview</title>
        </head>
        <body>
            <main>
                <h1>${escapeHtml(activeFile.name)}</h1>
                <p>This is a generated shell for previewing your project assets.</p>
                <div id="app"></div>
            </main>
        </body>
        </html>
    """.trimIndent()
}

private fun injectIntoHeadEnd(html: String, content: String): String {
    val headEnd = Regex("""(?is)</head\b[^>]*>""").find(html)
    return if (headEnd != null) {
        html.replaceRange(headEnd.range.first, headEnd.range.first, content)
    } else {
        html + content
    }
}

private fun injectIntoBodyEnd(html: String, content: String): String {
    val bodyEnd = Regex("""(?is)</body\b[^>]*>""").find(html)
    return if (bodyEnd != null) {
        html.replaceRange(bodyEnd.range.first, bodyEnd.range.first, content)
    } else {
        html + content
    }
}

private fun livePreviewScriptTagFor(file: WebFile, safeUrl: String): String {
    return if (fileExtension(file.name) == "mjs") {
        """<script type="module" src="$safeUrl"></script>"""
    } else {
        """<script src="$safeUrl"></script>"""
    }
}

private fun livePreviewStatusHtml(message: String): String {
    val safeMessage = escapeHtml(message)
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Preview</title>
            <style>
                html, body {
                    height: 100%;
                    margin: 0;
                    font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                    background: #ffffff;
                    color: #111827;
                }
                body {
                    display: grid;
                    place-items: center;
                    padding: 24px;
                    box-sizing: border-box;
                }
            </style>
        </head>
        <body>$safeMessage</body>
        </html>
    """.trimIndent()
}

private fun loadLivePreviewUrl(webView: WebView, previewUrl: String) {
    webView.stopLoading()
    webView.clearHistory()
    webView.clearCache(true)
    webView.loadUrl("about:blank")
    webView.post {
        webView.loadUrl(previewUrl)
    }
}

@Composable
private fun FileTabsBar(
    files: List<WebFile>,
    activeFileId: Long,
    palette: WebPalette,
    onSelectFile: (Long) -> Unit,
    onAddFile: () -> Unit = {},
    onDeleteFile: (Long) -> Unit = {},
    onToggleExplorer: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(palette.elevatedPanel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(15.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            files.forEach { file ->
                FileTab(
                    file = file,
                    selected = file.id == activeFileId,
                    palette = palette,
                    onSelect = { onSelectFile(file.id) },
                    onClose = { onDeleteFile(file.id) },
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        AddFileButton(palette = palette, onAddFile = onAddFile)
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onToggleExplorer, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Explorer",
                tint = palette.text,
                modifier = Modifier.size(27.dp),
            )
        }
    }
}

@Composable
private fun FileTab(
    file: WebFile,
    selected: Boolean,
    palette: WebPalette,
    onSelect: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(42.dp)
            .width(126.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) palette.selectedTab else palette.tab)
            .border(
                BorderStroke(1.dp, if (selected) palette.accent else palette.border),
                RoundedCornerShape(11.dp),
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = fileIcon(file.name),
            contentDescription = null,
            tint = fileColor(file.name, palette),
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = file.name,
            color = palette.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Delete file",
                tint = palette.mutedText,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun AddFileButton(
    palette: WebPalette,
    onAddFile: () -> Unit,
) {
    IconButton(
        onClick = onAddFile,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(palette.tab)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(11.dp)),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "New file",
            tint = palette.text,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun FileTabsOnlyBar(
    files: List<WebFile>,
    activeFileId: Long,
    palette: WebPalette,
    onSelectFile: (Long) -> Unit,
    onAddFile: () -> Unit,
    onDeleteFile: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(palette.panel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(15.dp))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        files.forEach { file ->
            FileTab(
                file = file,
                selected = file.id == activeFileId,
                palette = palette,
                onSelect = { onSelectFile(file.id) },
                onClose = { onDeleteFile(file.id) },
            )
        }
        AddFileButton(
            palette = palette,
            onAddFile = onAddFile,
        )
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    palette: WebPalette,
    placeholder: String,
) {
    Surface(color = palette.elevatedPanel, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, palette.border)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Icon(Icons.Rounded.Search, null, tint = palette.subtleText, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = palette.text, fontSize = 13.sp),
                cursorBrush = SolidColor(palette.accent),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (value.isBlank()) Text(placeholder, color = palette.subtleText, fontSize = 13.sp)
                    inner()
                },
            )
        }
    }
}

@Composable
private fun NameInputDialog(
    title: String,
    label: String,
    initialValue: String,
    palette: WebPalette,
    onDismiss: () -> Unit,
    onConfirm: (String) -> String?,
) {
    var value by remember(title, initialValue) { mutableStateOf(initialValue) }
    var error by remember(title, initialValue) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.elevatedPanel,
        title = { Text(title, color = palette.text) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it; error = null },
                    label = { Text(label) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { error = onConfirm(value) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let { Text(it, color = palette.error, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
            }
        },
        confirmButton = {
            Button(
                onClick = { error = onConfirm(value) },
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = palette.text) }
        },
    )
}

@Composable
private fun DeleteConfirmDialog(
    title: String,
    body: String,
    palette: WebPalette,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.elevatedPanel,
        title = { Text(title, color = palette.text) },
        text = { Text(body, color = palette.mutedText) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = palette.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = palette.text) }
        },
    )
}

@Composable
private fun Panel(
    palette: WebPalette,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.panel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(14.dp))
            .padding(10.dp),
        content = content,
    )
}

@Composable
private fun HorizontalRule(palette: WebPalette) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(palette.divider))
}

private class WebSyntaxTransformation(
    private val source: String,
    private val transformed: AnnotatedString,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            if (text.text == source) transformed else AnnotatedString(text.text),
            OffsetMapping.Identity,
        )
    }
}

private fun highlightWebSource(fileName: String, source: String, palette: WebPalette): AnnotatedString {
    return when (fileExtension(fileName)) {
        in htmlFileExtensions -> highlightHtml(source, palette)
        in cssFileExtensions -> highlightCss(source, palette)
        in javascriptFileExtensions -> highlightJs(source, palette)
        "json" -> highlightJson(source, palette)
        else -> AnnotatedString(source)
    }
}

private fun addDiagnosticStyles(
    source: String,
    highlighted: AnnotatedString,
    palette: WebPalette,
    diagnostics: List<CodeDiagnostic>,
): AnnotatedString = buildAnnotatedString {
    append(highlighted)
    diagnostics.forEach { diagnostic ->
        val start = diagnostic.start.coerceIn(0, source.length)
        val end = diagnostic.end.coerceIn(start, source.length)
        if (end > start) {
            addStyle(diagnosticSpanStyle(palette, diagnostic.severity), start, end)
        }
    }
}

private fun diagnosticLineNumberText(
    lineCount: Int,
    diagnostics: List<CodeDiagnostic>,
    palette: WebPalette,
): AnnotatedString {
    val severityByLine = diagnosticSeverityByLine(diagnostics)
    return buildAnnotatedString {
        (1..lineCount).forEach { line ->
            if (line > 1) append("\n")
            val severity = severityByLine[line]
            if (severity == null) {
                append(line.toString())
            } else {
                withStyle(
                    SpanStyle(
                        color = diagnosticColor(severity, palette),
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append(line.toString())
                }
            }
        }
    }
}

internal fun diagnosticSeverityByLine(diagnostics: List<CodeDiagnostic>): Map<Int, String> {
    return diagnostics
        .groupBy { it.line }
        .mapValues { (_, lineDiagnostics) ->
            lineDiagnostics.minByOrNull { diagnosticSeverityRank(it.severity) }?.severity ?: "hint"
        }
}

private fun diagnosticSpanStyle(
    palette: WebPalette,
    severity: String,
): SpanStyle {
    val color = diagnosticColor(severity, palette)
    return SpanStyle(
        background = color.copy(alpha = if (palette.text == Color.White) 0.18f else 0.12f),
        textDecoration = TextDecoration.Underline,
    )
}

private fun diagnosticColor(severity: String, palette: WebPalette): Color {
    return when (severity.lowercase()) {
        "error" -> palette.error
        "warning" -> palette.warning
        else -> palette.info
    }
}

private fun diagnosticSeverityRank(severity: String): Int {
    return when (severity.lowercase()) {
        "error" -> 0
        "warning" -> 1
        else -> 2
    }
}

private val javascriptKeywords = setOf(
    "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do", "else", "export",
    "extends", "finally", "for", "function", "if", "import", "in", "instanceof", "let", "new", "return", "super",
    "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield", "async", "await"
)

private val javascriptBuiltins = setOf(
    "console", "window", "document", "localStorage", "sessionStorage", "fetch", "Promise", "Array", "Object",
    "Math", "JSON", "Date", "Set", "Map", "Error", "HTMLElement", "Element", "Node"
)

private fun applyRegexColor(
    builder: AnnotatedString.Builder,
    source: String,
    regex: Regex,
    color: Color,
    bold: Boolean = false,
    italic: Boolean = false
) {
    regex.findAll(source).forEach { match ->
        builder.addStyle(
            SpanStyle(
                color = color,
                fontWeight = if (bold) FontWeight.Bold else null,
                fontStyle = if (italic) FontStyle.Italic else null
            ),
            match.range.first,
            match.range.last + 1
        )
    }
}

private fun applyRegexGroupColor(
    builder: AnnotatedString.Builder,
    source: String,
    regex: Regex,
    groupIndex: Int,
    color: Color,
    bold: Boolean = false,
    italic: Boolean = false
) {
    regex.findAll(source).forEach { match ->
        val group = match.groups[groupIndex] ?: return@forEach
        builder.addStyle(
            SpanStyle(
                color = color,
                fontWeight = if (bold) FontWeight.Bold else null,
                fontStyle = if (italic) FontStyle.Italic else null
            ),
            group.range.first,
            group.range.last + 1
        )
    }
}

private fun highlightHtml(source: String, palette: WebPalette): AnnotatedString = buildAnnotatedString {
    append(source)
    if (source.isEmpty()) return@buildAnnotatedString
    val dark = palette.text == Color.White

    // Comments
    applyRegexColor(this, source, HTML_COMMENT_REGEX, palette.subtleText, italic = true)

    // Doctype
    applyRegexColor(this, source, HTML_DOCTYPE_REGEX, if (dark) Color(0xFFFF7AB6) else Color(0xFFB4236E), bold = true)

    // Tag Brackets
    applyRegexColor(this, source, HTML_TAG_BRACKETS_REGEX, palette.text.copy(alpha = 0.7f))

    // Tag Names
    applyRegexGroupColor(this, source, HTML_TAG_NAME_REGEX, 1, if (dark) Color(0xFF569CD6) else Color(0xFF264F78), bold = true)

    // Attribute Names
    applyRegexGroupColor(this, source, HTML_ATTR_NAME_CORE_REGEX, 1, if (dark) Color(0xFF9CDCFE) else Color(0xFF001080), bold = true)
    applyRegexGroupColor(this, source, HTML_ATTR_NAME_GENERIC_REGEX, 1, if (dark) Color(0xFF9CDCFE) else Color(0xFF001080))

    // Attribute Values (Strings)
    applyRegexColor(this, source, HTML_ATTR_VALUE_REGEX, if (dark) Color(0xFFCE9178) else Color(0xFFA31515))

    // Entities
    applyRegexColor(this, source, HTML_ENTITY_REGEX, if (dark) Color(0xFFB5CEA8) else Color(0xFF098658))
}

private fun highlightCss(source: String, palette: WebPalette): AnnotatedString = buildAnnotatedString {
    append(source)
    if (source.isEmpty()) return@buildAnnotatedString
    val dark = palette.text == Color.White

    // Comments
    applyRegexColor(this, source, CSS_COMMENT_REGEX, palette.subtleText, italic = true)

    // At-rules
    applyRegexColor(this, source, CSS_AT_RULE_REGEX, if (dark) Color(0xFFC586C0) else Color(0xFFAF57A0))

    // Selectors
    applyRegexColor(this, source, CSS_SELECTOR_ID_CLASS_REGEX, if (dark) Color(0xFFD7BA7D) else Color(0xFF795E26))
    applyRegexGroupColor(this, source, CSS_SELECTOR_BLOCK_REGEX, 1, if (dark) Color(0xFFD7BA7D) else Color(0xFF795E26))

    // Properties
    applyRegexGroupColor(this, source, CSS_PROPERTY_REGEX, 1, if (dark) Color(0xFF9CDCFE) else Color(0xFF001080))

    // Values - Hex Colors
    applyRegexColor(this, source, CSS_COLOR_HEX_REGEX, if (dark) Color(0xFFB5CEA8) else Color(0xFF098658))

    // Values - Numbers and Units
    applyRegexColor(this, source, CSS_NUMBER_UNIT_REGEX, if (dark) Color(0xFFB5CEA8) else Color(0xFF098658))

    // Values - Strings
    applyRegexColor(this, source, CSS_STRING_REGEX, if (dark) Color(0xFFCE9178) else Color(0xFFA31515))

    // Values - Keywords
    applyRegexColor(this, source, CSS_KEYWORD_REGEX, if (dark) Color(0xFF569CD6) else Color(0xFF0451A5))
}

private fun highlightJs(source: String, palette: WebPalette): AnnotatedString = buildAnnotatedString {
    append(source)
    if (source.isEmpty()) return@buildAnnotatedString
    val dark = palette.text == Color.White

    // Comments
    applyRegexColor(this, source, JS_COMMENT_REGEX, palette.subtleText, italic = true)

    // Strings
    applyRegexColor(this, source, JS_STRING_REGEX, if (dark) Color(0xFFCE9178) else Color(0xFFA31515))

    // Keywords
    val jsKeywordsPattern = "\\b(${javascriptKeywords.joinToString("|")})\\b"
    applyRegexColor(this, source, Regex(jsKeywordsPattern), if (dark) Color(0xFFC586C0) else Color(0xFFAF57A0), bold = true)

    // Booleans/Null
    applyRegexColor(this, source, JS_BOOLEAN_NULL_REGEX, if (dark) Color(0xFF569CD6) else Color(0xFF0451A5), bold = true)

    // Builtins/Globals
    val jsBuiltinsPattern = "\\b(${javascriptBuiltins.joinToString("|")})\\b"
    applyRegexColor(this, source, Regex(jsBuiltinsPattern), if (dark) Color(0xFF4EC9B0) else Color(0xFF267F99))

    // Numbers
    applyRegexColor(this, source, JS_NUMBER_REGEX, if (dark) Color(0xFFB5CEA8) else Color(0xFF098658))

    // Member access (Properties)
    applyRegexGroupColor(this, source, JS_MEMBER_ACCESS_REGEX, 1, if (dark) Color(0xFF9CDCFE) else Color(0xFF001080))

    // Function calls & Methods
    JS_FUNC_CALL_REGEX.findAll(source).forEach { match ->
        val group = match.groups[1] ?: match.groups[2] ?: return@forEach
        addStyle(
            SpanStyle(color = if (dark) Color(0xFFDCDCAA) else Color(0xFF795E26)),
            group.range.first,
            group.range.last + 1
        )
    }
}

private fun highlightJson(source: String, palette: WebPalette): AnnotatedString = buildAnnotatedString {
    append(source)
    if (source.isEmpty()) return@buildAnnotatedString
    val dark = palette.text == Color.White

    // Keys
    applyRegexGroupColor(this, source, JSON_KEY_REGEX, 0, if (dark) Color(0xFF9CDCFE) else Color(0xFF001080))

    // Values - Strings
    applyRegexGroupColor(this, source, JSON_STRING_VALUE_REGEX, 1, if (dark) Color(0xFFCE9178) else Color(0xFFA31515))

    // Values - Numbers/Booleans/Null
    applyRegexGroupColor(this, source, JSON_LITERAL_VALUE_REGEX, 1, if (dark) Color(0xFFB5CEA8) else Color(0xFF098658))
}

private fun webLanguageForFile(fileName: String): WebEditorLanguage {
    return when (fileExtension(fileName)) {
        in htmlFileExtensions -> WebEditorLanguage.Html
        in cssFileExtensions -> WebEditorLanguage.Css
        in javascriptFileExtensions -> WebEditorLanguage.JavaScript
        "json" -> WebEditorLanguage.Json
        else -> WebEditorLanguage.Text
    }
}

private fun webLanguageAt(fileName: String, source: String, rawCursor: Int): WebEditorLanguage {
    val base = webLanguageForFile(fileName)
    if (base != WebEditorLanguage.Html) return base

    val cursor = rawCursor.coerceIn(0, source.length)
    val before = source.substring(0, cursor)
    val scriptOpen = Regex("""(?is)<script\b[^>]*>""").findAll(before).lastOrNull()
    val styleOpen = Regex("""(?is)<style\b[^>]*>""").findAll(before).lastOrNull()
    val scriptClose = before.lastIndexOf("</script", ignoreCase = true)
    val styleClose = before.lastIndexOf("</style", ignoreCase = true)
    val openBlocks = listOfNotNull(
        scriptOpen?.takeIf { it.range.last > scriptClose }?.let { WebEditorLanguage.JavaScript to it.range.last },
        styleOpen?.takeIf { it.range.last > styleClose }?.let { WebEditorLanguage.Css to it.range.last },
    )

    return openBlocks.maxByOrNull { it.second }?.first ?: WebEditorLanguage.Html
}

private fun activeHtmlTagName(beforeCursor: String): String? {
    val tagStart = beforeCursor.lastIndexOf('<')
    if (tagStart < 0 || beforeCursor.lastIndexOf('>') > tagStart) return null
    val tagText = beforeCursor.substring(tagStart + 1)
    if (tagText.startsWith("/") || tagText.startsWith("!") || tagText.startsWith("?")) return null
    return Regex("""^\s*([A-Za-z][A-Za-z0-9-]*)""")
        .find(tagText)
        ?.groupValues
        ?.getOrNull(1)
        ?.lowercase()
}

private fun activeHtmlAttributeValueName(beforeCursor: String): String? {
    val tagStart = beforeCursor.lastIndexOf('<')
    if (tagStart < 0 || beforeCursor.lastIndexOf('>') > tagStart) return null
    val tagText = beforeCursor.substring(tagStart + 1)
    return Regex("""([A-Za-z_:][-A-Za-z0-9_:.]*)\s*=\s*(["'])[^"']*$""")
        .find(tagText)
        ?.groupValues
        ?.getOrNull(1)
        ?.lowercase()
}

private fun cssLineBeforeCursor(beforeCursor: String): String {
    return beforeCursor.substringAfterLast('\n')
}

private fun isCssAtRuleCompletion(beforeCursor: String): Boolean {
    val line = cssLineBeforeCursor(beforeCursor).trimStart()
    return line.startsWith("@") && !line.contains("{")
}

private fun isCssVariableValueCompletion(beforeCursor: String): Boolean {
    val line = cssLineBeforeCursor(beforeCursor)
    val colon = line.lastIndexOf(':')
    if (colon < 0) return false
    val valuePart = line.substring(colon + 1)
    return valuePart.lastIndexOf("var(") > valuePart.lastIndexOf(")")
}

private fun isInsideCssRuleBlock(beforeCursor: String): Boolean {
    return beforeCursor.lastIndexOf('{') > beforeCursor.lastIndexOf('}')
}

private fun isCssDeclarationValueCompletion(beforeCursor: String): Boolean {
    if (!isInsideCssRuleBlock(beforeCursor)) return false
    return cssLineBeforeCursor(beforeCursor).contains(":") &&
            cssLineBeforeCursor(beforeCursor).substringAfterLast(':').none { it == ';' || it == '{' || it == '}' }
}

internal fun completionContextAt(text: String, rawCursor: Int, fileName: String): CompletionContext? {
    val cursor = rawCursor.coerceIn(0, text.length)
    if (!isWebCompletionPosition(text, cursor, fileName)) return null

    val before = text.substring(0, cursor)
    val tagStart = before.lastIndexOf('<')
    val tagEnd = before.lastIndexOf('>')
    val language = webLanguageAt(fileName, text, cursor)
    val insideHtmlTag = language == WebEditorLanguage.Html && tagStart > tagEnd
    val attributeValueName = if (insideHtmlTag) activeHtmlAttributeValueName(before) else null
    val activeHtmlTagText = if (insideHtmlTag) before.substring(tagStart) else ""
    val completingClosingHtmlTag = isCompletingClosingHtmlTag(activeHtmlTagText)
    val completingOpeningHtmlTag = isCompletingOpeningHtmlTag(activeHtmlTagText)
    if (before.endsWith(".") && language == WebEditorLanguage.JavaScript) {
        return CompletionContext(
            prefix = "",
            replaceStart = cursor,
            replaceEnd = cursor,
            trigger = ".",
            isDotAccess = true,
        )
    }
    val trigger = when {
        before.endsWith("</") || completingClosingHtmlTag -> "closingTag"
        attributeValueName != null -> "attributeValue:$attributeValueName"
        insideHtmlTag && before.substring(tagStart).contains(Regex("\\s")) -> "attribute"
        before.endsWith("<") || completingOpeningHtmlTag -> "tag"
        language == WebEditorLanguage.Css && isInsideCssRuleBlock(before) && isCssVariableValueCompletion(before) -> "cssVariable"
        language == WebEditorLanguage.Css && isCssAtRuleCompletion(before) -> "cssAtRule"
        language == WebEditorLanguage.Css && isCssDeclarationValueCompletion(before) -> "cssValue"
        else -> ""
    }

    val validPart: (Char) -> Boolean = when {
        trigger == "attribute" -> { char -> char.isLetterOrDigit() || char in "-:@" }
        trigger.startsWith("attributeValue:") -> { char -> char.isLetterOrDigit() || char in "-_:/#.%@" }
        trigger == "cssVariable" -> { char -> char.isLetterOrDigit() || char in "-_" }
        trigger == "cssValue" -> { char -> char.isLetterOrDigit() || char in "-#.%()," }
        language == WebEditorLanguage.JavaScript -> { char -> char.isLetterOrDigit() || char == '_' || char == '$' }
        language == WebEditorLanguage.Css -> { char -> char.isLetterOrDigit() || char in "_-#@$" }
        else -> { char -> char.isLetterOrDigit() || char in "_-:#@$" }
    }

    var start = cursor
    while (start > 0 && validPart(text[start - 1])) start -= 1
    val isDotAccess = start > 0 && text[start - 1] == '.'
    val prefix = text.substring(start, cursor)
    if (prefix.isBlank() && trigger.isBlank() && !isDotAccess) return null
    val replaceStart = when {
        completingClosingHtmlTag || before.endsWith("</") -> tagStart + 1
        completingOpeningHtmlTag || before.endsWith("<") -> tagStart
        else -> start
    }
    val replaceEnd = if (trigger == "tag" || trigger == "closingTag") {
        htmlTagShellReplaceEnd(text, cursor)
    } else {
        cursor
    }
    return CompletionContext(
        prefix = prefix,
        replaceStart = replaceStart,
        replaceEnd = replaceEnd,
        trigger = trigger,
        isDotAccess = isDotAccess,
    )
}

internal fun webCompletionsForContext(
    fileName: String,
    source: String,
    context: CompletionContext,
): List<CompletionItem> {
    val language = webLanguageAt(fileName, source, context.replaceStart)
    val prefix = context.prefix.lowercase()
    val htmlTag = activeHtmlTagName(source.substring(0, context.replaceStart.coerceIn(0, source.length)))
    val base = when {
        context.trigger == "closingTag" -> openHtmlTagsBefore(source, context.replaceStart)
            .asReversed()
            .map { CompletionItem("/$it>", "/$it>", "closing tag") }
        context.trigger == "attribute" -> htmlAttributeCompletionsForTag(htmlTag)
        context.trigger.startsWith("attributeValue:") -> htmlAttributeValueCompletions(
            attribute = context.trigger.substringAfter(':'),
            tag = htmlTag,
            source = source,
            cursor = context.replaceStart,
        )
        context.trigger == "cssAtRule" -> cssAtRuleCompletions
        context.trigger == "cssVariable" -> cssCustomPropertyValueCompletions(source)
        context.trigger == "cssValue" -> cssValueCompletionsFor(source, context.replaceStart)
        context.isDotAccess && language == WebEditorLanguage.JavaScript -> jsDotCompletionsForSource(source, context.replaceStart - 1)
        context.trigger == "tag" && language == WebEditorLanguage.Html -> htmlCompletionItems
        language == WebEditorLanguage.Html -> userDefinedHtmlCompletions(source) + htmlCompletionItems
        language == WebEditorLanguage.Css -> cssContextualCompletions(source, context.replaceStart)
        language == WebEditorLanguage.JavaScript -> userDefinedJsCompletions(source) + jsCompletionItems + jsLibraryCompletionItems
        language == WebEditorLanguage.Json -> jsonCompletionItems
        else -> emptyList()
    }
    if (context.trigger == "tag" && prefix.isBlank()) return base.take(12)
    return base
        .distinctBy { it.completionKey() }
        .filter { item ->
            completionMatchesPrefix(item, prefix)
        }
        .take(16)
}

private fun completionMatchesPrefix(item: CompletionItem, prefix: String): Boolean {
    if (prefix.isBlank()) return true
    val normalizedPrefix = prefix.trimCompletionPrefixSymbols()
    return listOf(item.label, item.insertText)
        .map { it.lowercase() }
        .any { value ->
            value.startsWith(prefix) ||
                    value.trimCompletionPrefixSymbols().startsWith(normalizedPrefix)
        }
}

private fun String.trimCompletionPrefixSymbols(): String {
    return trimStart('<', '/', '.', '#', ':', '"', '\'')
}

private fun isCompletingOpeningHtmlTag(activeTagText: String): Boolean {
    if (!activeTagText.startsWith("<")) return false
    if (activeTagText.startsWith("</") || activeTagText.startsWith("<!") || activeTagText.startsWith("<?")) return false
    val body = activeTagText.drop(1)
    return body.none { it.isWhitespace() || it == '<' || it == '>' || it == '/' }
}

private fun isCompletingClosingHtmlTag(activeTagText: String): Boolean {
    if (!activeTagText.startsWith("</")) return false
    val body = activeTagText.drop(2)
    return body.none { it.isWhitespace() || it == '<' || it == '>' || it == '/' }
}

private fun htmlTagShellReplaceEnd(text: String, cursor: Int): Int {
    var end = cursor.coerceIn(0, text.length)
    while (end < text.length && text[end].isWhitespace()) end += 1
    return if (text.getOrNull(end) == '>') end + 1 else cursor
}

private fun applyCompletion(value: TextFieldValue, context: CompletionContext, item: CompletionItem): TextFieldValue {
    val nextText = buildString {
        append(value.text.substring(0, context.replaceStart))
        append(item.insertText)
        append(value.text.substring(context.replaceEnd))
    }
    val cursor = (context.replaceStart + item.cursorOffset).coerceIn(0, nextText.length)
    return TextFieldValue(nextText, selection = TextRange(cursor))
}

private fun CompletionItem.completionKey(): String {
    return "$label|$insertText|$detail"
}

private val htmlCompletionItems = listOf(
    CompletionItem("<!DOCTYPE html>", "<!DOCTYPE html>\n", "document"),
    CompletionItem("html", "<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Document</title>\n</head>\n<body>\n    \n</body>\n</html>", "page", 156),
    CompletionItem("head", "<head>\n    \n</head>", "element", 11),
    CompletionItem("body", "<body>\n    \n</body>", "element", 11),
    CompletionItem("main", "<main>\n    \n</main>", "element", 11),
    CompletionItem("section", "<section>\n    \n</section>", "element", 14),
    CompletionItem("article", "<article>\n    \n</article>", "element", 14),
    CompletionItem("header", "<header>\n    \n</header>", "element", 13),
    CompletionItem("footer", "<footer>\n    \n</footer>", "element", 13),
    CompletionItem("nav", "<nav>\n    \n</nav>", "element", 10),
    CompletionItem("aside", "<aside>\n    \n</aside>", "element", 12),
    CompletionItem("div", "<div class=\"\">\n    \n</div>", "element", 12),
    CompletionItem("span", "<span></span>", "element", 6),
    CompletionItem("h1", "<h1></h1>", "element", 4),
    CompletionItem("h2", "<h2></h2>", "element", 4),
    CompletionItem("h3", "<h3></h3>", "element", 4),
    CompletionItem("p", "<p></p>", "element", 3),
    CompletionItem("strong", "<strong></strong>", "element", 8),
    CompletionItem("small", "<small></small>", "element", 7),
    CompletionItem("a", "<a href=\"\"></a>", "element", 9),
    CompletionItem("button", "<button type=\"button\"></button>", "element", 23),
    CompletionItem("img", "<img src=\"\" alt=\"\">", "media", 10),
    CompletionItem("picture", "<picture>\n    <source srcset=\"\" media=\"\">\n    <img src=\"\" alt=\"\">\n</picture>", "media", 25),
    CompletionItem("video", "<video controls src=\"\"></video>", "media", 21),
    CompletionItem("canvas", "<canvas></canvas>", "media", 8),
    CompletionItem("form", "<form>\n    \n</form>", "element", 12),
    CompletionItem("input", "<input type=\"text\" name=\"\">", "form", 25),
    CompletionItem("label", "<label for=\"\"></label>", "form", 12),
    CompletionItem("select", "<select name=\"\">\n    <option value=\"\"></option>\n</select>", "form", 15),
    CompletionItem("option", "<option value=\"\"></option>", "form", 15),
    CompletionItem("textarea", "<textarea name=\"\"></textarea>", "form", 16),
    CompletionItem("ul", "<ul>\n    <li></li>\n</ul>", "list", 14),
    CompletionItem("ol", "<ol>\n    <li></li>\n</ol>", "list", 14),
    CompletionItem("li", "<li></li>", "list", 4),
    CompletionItem("details", "<details>\n    <summary></summary>\n    \n</details>", "element", 25),
    CompletionItem("dialog", "<dialog>\n    \n</dialog>", "element", 13),
    CompletionItem("template", "<template>\n    \n</template>", "element", 15),
    CompletionItem("style", "<style>\n    \n</style>", "style", 12),
    CompletionItem("link:css", "<link rel=\"stylesheet\" href=\"style.css\">", "stylesheet"),
    CompletionItem("meta:viewport", "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">", "metadata"),
    CompletionItem("script:src", "<script src=\"script.js\"></script>", "script"),
    CompletionItem("script:module", "<script type=\"module\" src=\"script.js\"></script>", "script"),
    CompletionItem("script:importmap", "<script type=\"importmap\">\n{\n    \"imports\": {\n        \"three\": \"./libs/three.module.js\"\n    }\n}\n</script>", "libraries", 70),
    CompletionItem("lib:tailwind", "<link rel=\"stylesheet\" href=\"vendor/tailwind.css\">", "libraries"),
    CompletionItem("lib:bootstrap", "<link href=\"vendor/bootstrap.min.css\" rel=\"stylesheet\">\n<script src=\"vendor/bootstrap.bundle.min.js\"></script>", "libraries"),
    CompletionItem("lib:react-root", "<div id=\"root\"></div>\n<script type=\"module\">\n    import React from \"react\";\n    import { createRoot } from \"react-dom/client\";\n</script>", "libraries", 108),
    CompletionItem("lib:vue-app", "<div id=\"app\">{{ message }}</div>\n<script type=\"module\">\n    import { createApp } from \"vue\";\n</script>", "libraries", 92),
)

private val htmlGlobalAttributeCompletions = listOf(
    CompletionItem("class", "class=\"\"", "attribute", 7),
    CompletionItem("id", "id=\"\"", "attribute", 4),
    CompletionItem("style", "style=\"\"", "attribute", 7),
    CompletionItem("title", "title=\"\"", "attribute", 7),
    CompletionItem("hidden", "hidden", "attribute"),
    CompletionItem("tabindex", "tabindex=\"0\"", "attribute"),
    CompletionItem("role", "role=\"\"", "attribute", 6),
    CompletionItem("aria-label", "aria-label=\"\"", "attribute", 12),
    CompletionItem("aria-hidden", "aria-hidden=\"false\"", "attribute", 13),
    CompletionItem("data-", "data-=\"\"", "attribute", 5),
    CompletionItem("onclick", "onclick=\"\"", "attribute", 9),
    CompletionItem("oninput", "oninput=\"\"", "attribute", 9),
    CompletionItem("onchange", "onchange=\"\"", "attribute", 10),
)

private val htmlTagAttributeCompletions = mapOf(
    "a" to listOf(
        CompletionItem("href", "href=\"\"", "attribute", 6),
        CompletionItem("target", "target=\"_blank\"", "attribute"),
        CompletionItem("rel", "rel=\"noopener\"", "attribute"),
        CompletionItem("download", "download", "attribute"),
    ),
    "img" to listOf(
        CompletionItem("src", "src=\"\"", "attribute", 5),
        CompletionItem("alt", "alt=\"\"", "attribute", 5),
        CompletionItem("loading", "loading=\"lazy\"", "attribute"),
        CompletionItem("decoding", "decoding=\"async\"", "attribute"),
        CompletionItem("width", "width=\"\"", "attribute", 7),
        CompletionItem("height", "height=\"\"", "attribute", 8),
    ),
    "source" to listOf(
        CompletionItem("srcset", "srcset=\"\"", "attribute", 8),
        CompletionItem("media", "media=\"\"", "attribute", 7),
        CompletionItem("type", "type=\"\"", "attribute", 6),
    ),
    "script" to listOf(
        CompletionItem("src", "src=\"\"", "attribute", 5),
        CompletionItem("type", "type=\"module\"", "attribute"),
        CompletionItem("defer", "defer", "attribute"),
        CompletionItem("async", "async", "attribute"),
    ),
    "link" to listOf(
        CompletionItem("rel", "rel=\"stylesheet\"", "attribute"),
        CompletionItem("href", "href=\"\"", "attribute", 6),
        CompletionItem("as", "as=\"style\"", "attribute"),
        CompletionItem("media", "media=\"\"", "attribute", 7),
    ),
    "meta" to listOf(
        CompletionItem("name", "name=\"viewport\"", "attribute"),
        CompletionItem("content", "content=\"\"", "attribute", 9),
        CompletionItem("charset", "charset=\"UTF-8\"", "attribute"),
    ),
    "button" to listOf(
        CompletionItem("type", "type=\"button\"", "attribute"),
        CompletionItem("disabled", "disabled", "attribute"),
        CompletionItem("aria-pressed", "aria-pressed=\"false\"", "attribute", 14),
    ),
    "input" to listOf(
        CompletionItem("type", "type=\"text\"", "attribute"),
        CompletionItem("name", "name=\"\"", "attribute", 6),
        CompletionItem("value", "value=\"\"", "attribute", 7),
        CompletionItem("placeholder", "placeholder=\"\"", "attribute", 13),
        CompletionItem("required", "required", "attribute"),
        CompletionItem("checked", "checked", "attribute"),
        CompletionItem("autocomplete", "autocomplete=\"\"", "attribute", 14),
    ),
    "label" to listOf(CompletionItem("for", "for=\"\"", "attribute", 5)),
    "form" to listOf(
        CompletionItem("action", "action=\"\"", "attribute", 8),
        CompletionItem("method", "method=\"post\"", "attribute"),
        CompletionItem("autocomplete", "autocomplete=\"on\"", "attribute"),
    ),
    "select" to listOf(
        CompletionItem("name", "name=\"\"", "attribute", 6),
        CompletionItem("multiple", "multiple", "attribute"),
        CompletionItem("required", "required", "attribute"),
    ),
    "option" to listOf(
        CompletionItem("value", "value=\"\"", "attribute", 7),
        CompletionItem("selected", "selected", "attribute"),
    ),
    "textarea" to listOf(
        CompletionItem("name", "name=\"\"", "attribute", 6),
        CompletionItem("placeholder", "placeholder=\"\"", "attribute", 13),
        CompletionItem("rows", "rows=\"4\"", "attribute"),
    ),
    "video" to listOf(
        CompletionItem("src", "src=\"\"", "attribute", 5),
        CompletionItem("controls", "controls", "attribute"),
        CompletionItem("autoplay", "autoplay", "attribute"),
        CompletionItem("muted", "muted", "attribute"),
        CompletionItem("playsinline", "playsinline", "attribute"),
    ),
)

private fun htmlAttributeCompletionsForTag(tag: String?): List<CompletionItem> {
    return (htmlTagAttributeCompletions[tag].orEmpty() + htmlGlobalAttributeCompletions)
        .distinctBy { it.label }
}

private fun htmlAttributeValueCompletions(
    attribute: String,
    tag: String?,
    source: String,
    cursor: Int,
): List<CompletionItem> {
    val values = when (attribute) {
        "class" -> htmlClassValueCompletions(source)
        "id", "for" -> htmlIdValueCompletions(source)
        "style" -> inlineStyleAttributeCompletions(source, cursor)
        "type" -> when (tag) {
            "button" -> listOf("button", "submit", "reset")
            "input" -> listOf("text", "email", "password", "number", "search", "tel", "url", "checkbox", "radio", "file", "date", "color", "range")
            "script" -> listOf("module", "text/javascript")
            else -> listOf("button", "text", "module", "submit")
        }.map { CompletionItem(it, it, "attribute value") }
        "rel" -> listOf("stylesheet", "preload", "modulepreload", "icon", "noopener", "noreferrer").map { CompletionItem(it, it, "attribute value") }
        "target" -> listOf("_self", "_blank", "_parent", "_top").map { CompletionItem(it, it, "attribute value") }
        "loading" -> listOf("lazy", "eager").map { CompletionItem(it, it, "attribute value") }
        "decoding" -> listOf("async", "sync", "auto").map { CompletionItem(it, it, "attribute value") }
        "method" -> listOf("get", "post", "dialog").map { CompletionItem(it, it, "attribute value") }
        "href" -> when (tag) {
            "link" -> listOf("style.css", "./style.css", "https://")
            "a" -> listOf("#", "#main", "mailto:", "tel:", "https://")
            else -> listOf("#", "./", "https://")
        }.map { CompletionItem(it, it, "attribute value") }
        "src" -> when (tag) {
            "script" -> listOf("script.js", "./script.js", "main.js")
            "img", "source", "video" -> listOf("assets/", "images/", "image.png", "photo.jpg", "video.mp4")
            else -> listOf("./", "assets/")
        }.map { CompletionItem(it, it, "attribute value") }
        "name" -> when (tag) {
            "meta" -> listOf("viewport", "description", "theme-color", "robots")
            else -> listOf("name", "email", "password", "search", "message")
        }.map { CompletionItem(it, it, "attribute value") }
        "media" -> listOf("(max-width: 700px)", "(min-width: 768px)", "(prefers-color-scheme: dark)").map { CompletionItem(it, it, "attribute value") }
        "charset" -> listOf("UTF-8").map { CompletionItem(it, it, "attribute value") }
        "autocomplete" -> listOf("on", "off", "name", "email", "username", "current-password", "new-password").map { CompletionItem(it, it, "attribute value") }
        "aria-hidden", "aria-expanded", "aria-pressed", "draggable", "contenteditable" -> listOf("true", "false").map { CompletionItem(it, it, "attribute value") }
        "role" -> listOf("button", "navigation", "main", "banner", "contentinfo", "dialog", "list", "listitem", "status", "alert").map { CompletionItem(it, it, "attribute value") }
        "as" -> listOf("style", "script", "image", "font", "fetch").map { CompletionItem(it, it, "attribute value") }
        else -> emptyList()
    }
    return values.distinctBy { it.label }
}

private fun inlineStyleAttributeCompletions(source: String, cursor: Int): List<CompletionItem> {
    val styleValue = activeHtmlAttributeValueTextBeforeCursor(source, cursor)
    val currentDeclaration = styleValue.substringAfterLast(';').trimStart()
    return if (currentDeclaration.contains(":")) {
        val cssSnippet = ".inline {\n    $currentDeclaration"
        cssValueCompletionsFor(cssSnippet, cssSnippet.length)
    } else {
        cssCompletionItems
    }
}

private fun activeHtmlAttributeValueTextBeforeCursor(source: String, rawCursor: Int): String {
    val cursor = rawCursor.coerceIn(0, source.length)
    val beforeCursor = source.substring(0, cursor)
    val tagStart = beforeCursor.lastIndexOf('<')
    if (tagStart < 0 || beforeCursor.lastIndexOf('>') > tagStart) return ""
    val tagText = beforeCursor.substring(tagStart + 1)
    return Regex("""(?i)\bstyle\s*=\s*(["'])([^"']*)$""")
        .find(tagText)
        ?.groupValues
        ?.getOrNull(2)
        .orEmpty()
}

private val cssCompletionItems = listOf(
    CompletionItem("--custom-property", "--custom-property: ;", "css property", 19),
    CompletionItem("display", "display: flex;", "css property"),
    CompletionItem("position", "position: relative;", "css property"),
    CompletionItem("inset", "inset: 0;", "css property"),
    CompletionItem("grid", "display: grid;", "css property"),
    CompletionItem("grid-template-columns", "grid-template-columns: repeat(3, minmax(0, 1fr));", "css property"),
    CompletionItem("grid-template-rows", "grid-template-rows: auto;", "css property"),
    CompletionItem("align-items", "align-items: center;", "css property"),
    CompletionItem("justify-content", "justify-content: center;", "css property"),
    CompletionItem("place-items", "place-items: center;", "css property"),
    CompletionItem("flex-direction", "flex-direction: column;", "css property"),
    CompletionItem("flex-wrap", "flex-wrap: wrap;", "css property"),
    CompletionItem("gap", "gap: 1rem;", "css property"),
    CompletionItem("padding", "padding: 1rem;", "css property"),
    CompletionItem("margin", "margin: 0;", "css property"),
    CompletionItem("width", "width: 100%;", "css property"),
    CompletionItem("min-width", "min-width: 0;", "css property"),
    CompletionItem("max-width", "max-width: 100%;", "css property"),
    CompletionItem("height", "height: 100%;", "css property"),
    CompletionItem("min-height", "min-height: 100vh;", "css property"),
    CompletionItem("aspect-ratio", "aspect-ratio: 16 / 9;", "css property"),
    CompletionItem("color", "color: #111827;", "css property"),
    CompletionItem("background", "background: #ffffff;", "css property"),
    CompletionItem("background-color", "background-color: #ffffff;", "css property"),
    CompletionItem("border", "border: 1px solid #e5e7eb;", "css property"),
    CompletionItem("border-radius", "border-radius: 8px;", "css property"),
    CompletionItem("outline", "outline: 2px solid transparent;", "css property"),
    CompletionItem("box-shadow", "box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);", "css property"),
    CompletionItem("font-family", "font-family: system-ui, sans-serif;", "css property"),
    CompletionItem("font-size", "font-size: 1rem;", "css property"),
    CompletionItem("font-weight", "font-weight: 700;", "css property"),
    CompletionItem("line-height", "line-height: 1.5;", "css property"),
    CompletionItem("text-align", "text-align: center;", "css property"),
    CompletionItem("object-fit", "object-fit: cover;", "css property"),
    CompletionItem("overflow", "overflow: hidden;", "css property"),
    CompletionItem("z-index", "z-index: 10;", "css property"),
    CompletionItem("opacity", "opacity: 1;", "css property"),
    CompletionItem("transform", "transform: translateY(0);", "css property"),
    CompletionItem("transition", "transition: all 180ms ease;", "css property"),
    CompletionItem("animation", "animation: fade-in 240ms ease both;", "css property"),
)

private val cssAtRuleCompletions = listOf(
    CompletionItem("@media", "@media (max-width: 700px) {\n    \n}", "at-rule", 31),
    CompletionItem("@supports", "@supports (display: grid) {\n    \n}", "at-rule", 31),
    CompletionItem("@container", "@container (min-width: 480px) {\n    \n}", "at-rule", 35),
    CompletionItem("@keyframes", "@keyframes fade-in {\n    from { opacity: 0; }\n    to { opacity: 1; }\n}", "at-rule", 11),
    CompletionItem("@font-face", "@font-face {\n    font-family: '';\n    src: url('');\n}", "at-rule", 33),
    CompletionItem("@import", "@import url('');", "at-rule", 13),
    CompletionItem("@layer", "@layer components {\n    \n}", "at-rule", 24),
)

private val cssSelectorCompletions = listOf(
    CompletionItem("body", "body {\n    \n}", "selector", 11),
    CompletionItem(":root", ":root {\n    \n}", "selector", 12),
    CompletionItem("*", "* {\n    box-sizing: border-box;\n}", "selector"),
    CompletionItem("::before", "::before", "selector"),
    CompletionItem("::after", "::after", "selector"),
)

private val cssPseudoClassCompletions = listOf(
    CompletionItem(":hover", ":hover", "selector"),
    CompletionItem(":focus", ":focus", "selector"),
    CompletionItem(":focus-visible", ":focus-visible", "selector"),
    CompletionItem(":active", ":active", "selector"),
    CompletionItem(":disabled", ":disabled", "selector"),
    CompletionItem(":checked", ":checked", "selector"),
    CompletionItem(":first-child", ":first-child", "selector"),
    CompletionItem(":last-child", ":last-child", "selector"),
    CompletionItem(":nth-child()", ":nth-child()", "selector", 11),
    CompletionItem(":not()", ":not()", "selector", 5),
    CompletionItem(":has()", ":has()", "selector", 5),
)

private val cssValueCompletions = listOf(
    CompletionItem("flex", "flex", "css value"),
    CompletionItem("grid", "grid", "css value"),
    CompletionItem("block", "block", "css value"),
    CompletionItem("none", "none", "css value"),
    CompletionItem("relative", "relative", "css value"),
    CompletionItem("absolute", "absolute", "css value"),
    CompletionItem("center", "center", "css value"),
    CompletionItem("space-between", "space-between", "css value"),
    CompletionItem("wrap", "wrap", "css value"),
    CompletionItem("column", "column", "css value"),
    CompletionItem("cover", "cover", "css value"),
    CompletionItem("contain", "contain", "css value"),
    CompletionItem("hidden", "hidden", "css value"),
    CompletionItem("auto", "auto", "css value"),
    CompletionItem("ease", "ease", "css value"),
    CompletionItem("1rem", "1rem", "css value"),
    CompletionItem("100%", "100%", "css value"),
    CompletionItem("100vh", "100vh", "css value"),
    CompletionItem("#111827", "#111827", "css value"),
    CompletionItem("rgba()", "rgba(0, 0, 0, 0.12)", "css value", 5),
    CompletionItem("rgb()", "rgb(255, 255, 255)", "css value", 4),
    CompletionItem("hsl()", "hsl(220 80% 55%)", "css value", 4),
    CompletionItem("linear-gradient()", "linear-gradient(135deg, #111827, #426bde)", "css value", 16),
    CompletionItem("calc()", "calc(100% - 1rem)", "css value", 5),
    CompletionItem("clamp()", "clamp(1rem, 2vw, 2rem)", "css value", 6),
    CompletionItem("var()", "var(--color)", "css value", 6),
)

private val cssPropertyValueOptions = mapOf(
    "display" to listOf("block", "inline-block", "flex", "grid", "none", "contents"),
    "position" to listOf("relative", "absolute", "fixed", "sticky", "static"),
    "flex-direction" to listOf("row", "column", "row-reverse", "column-reverse"),
    "flex-wrap" to listOf("nowrap", "wrap", "wrap-reverse"),
    "align-items" to listOf("stretch", "center", "flex-start", "flex-end", "baseline"),
    "justify-content" to listOf("center", "flex-start", "flex-end", "space-between", "space-around", "space-evenly"),
    "place-items" to listOf("center", "stretch", "start", "end"),
    "overflow" to listOf("visible", "hidden", "clip", "scroll", "auto"),
    "object-fit" to listOf("cover", "contain", "fill", "scale-down", "none"),
    "background" to listOf("#ffffff", "transparent", "linear-gradient(135deg, #111827, #426bde)", "var(--background)"),
    "background-color" to listOf("#ffffff", "transparent", "var(--background)"),
    "color" to listOf("#111827", "#ffffff", "currentColor", "var(--color)"),
    "text-align" to listOf("left", "center", "right", "start", "end"),
    "font-family" to listOf("system-ui, sans-serif", "Inter, sans-serif", "monospace"),
    "font-weight" to listOf("400", "500", "600", "700", "bold"),
    "border-radius" to listOf("0", "4px", "8px", "999px", "50%"),
    "grid-template-columns" to listOf("repeat(2, minmax(0, 1fr))", "repeat(3, minmax(0, 1fr))", "1fr auto", "minmax(0, 1fr)"),
    "transition" to listOf("all 180ms ease", "color 180ms ease", "transform 180ms ease"),
    "cursor" to listOf("pointer", "default", "grab", "not-allowed", "text"),
    "border-style" to listOf("solid", "dashed", "dotted", "none"),
    "transition-timing-function" to listOf("ease", "ease-in", "ease-out", "ease-in-out", "linear"),
)

private val jsCompletionItems = listOf(
    CompletionItem("const", "const ", "keyword"),
    CompletionItem("let", "let ", "keyword"),
    CompletionItem("var", "var ", "keyword"),
    CompletionItem("import", "import  from '';", "snippet", 7),
    CompletionItem("export", "export ", "keyword"),
    CompletionItem("return", "return ", "keyword"),
    CompletionItem("async", "async ", "keyword"),
    CompletionItem("await", "await ", "keyword"),
    CompletionItem("function", "function name() {\n    \n}", "snippet", 9),
    CompletionItem("arrow", "const name = () => {\n    \n};", "snippet", 6),
    CompletionItem("class", "class Name {\n    constructor() {\n        \n    }\n}", "snippet", 6),
    CompletionItem("if", "if (condition) {\n    \n}", "snippet", 4),
    CompletionItem("else", "else {\n    \n}", "snippet", 7),
    CompletionItem("for", "for (const item of items) {\n    \n}", "snippet", 11),
    CompletionItem("while", "while (condition) {\n    \n}", "snippet", 7),
    CompletionItem("switch", "switch (value) {\n    case value:\n        break;\n    default:\n        break;\n}", "snippet", 8),
    CompletionItem("forEach", "items.forEach(item => {\n    \n});", "snippet", 25),
    CompletionItem("map", "items.map(item => item)", "array", 10),
    CompletionItem("filter", "items.filter(item => item)", "array", 13),
    CompletionItem("reduce", "items.reduce((total, item) => total, 0)", "array", 15),
    CompletionItem("DOMContentLoaded", "document.addEventListener('DOMContentLoaded', () => {\n    \n});", "dom", 49),
    CompletionItem("try", "try {\n    \n} catch (error) {\n    console.error(error);\n}", "snippet", 10),
    CompletionItem("addEventListener", "addEventListener('click', () => {\n    \n})", "dom", 18),
    CompletionItem("querySelector", "document.querySelector('')", "dom", 24),
    CompletionItem("querySelectorAll", "document.querySelectorAll('')", "dom", 27),
    CompletionItem("getElementById", "document.getElementById('')", "dom", 24),
    CompletionItem("createElement", "document.createElement('div')", "dom", 24),
    CompletionItem("classList.add", "classList.add('')", "dom", 15),
    CompletionItem("textContent", "textContent", "dom"),
    CompletionItem("innerHTML", "innerHTML", "dom"),
    CompletionItem("console.log", "console.log()", "function", 12),
    CompletionItem("console.error", "console.error()", "function", 14),
    CompletionItem("console.warn", "console.warn()", "function", 13),
    CompletionItem("console", "console", "builtin"),
    CompletionItem("fetch", "fetch('').then(response => response.json())", "function", 7),
    CompletionItem("localStorage", "localStorage", "dom"),
    CompletionItem("document", "document", "dom"),
    CompletionItem("window", "window", "dom"),
    CompletionItem("navigator", "navigator", "dom"),
    CompletionItem("location", "location", "dom"),
    CompletionItem("history", "history", "dom"),
    CompletionItem("Math", "Math", "builtin"),
    CompletionItem("JSON", "JSON", "builtin"),
    CompletionItem("Array", "Array", "builtin"),
    CompletionItem("Object", "Object", "builtin"),
    CompletionItem("Date", "Date", "builtin"),
    CompletionItem("Promise", "Promise", "builtin"),
    CompletionItem("URL", "URL", "builtin"),
    CompletionItem("URLSearchParams", "URLSearchParams", "builtin"),
    CompletionItem("setTimeout", "setTimeout(() => {\n    \n}, 300)", "function", 19),
    CompletionItem("setInterval", "setInterval(() => {\n    \n}, 1000)", "function", 20),
    CompletionItem("requestAnimationFrame", "requestAnimationFrame(() => {\n    \n})", "function", 30),
    CompletionItem("parseInt", "parseInt(value, 10)", "function", 9),
)

private val jsLibraryCompletionItems = listOf(
    CompletionItem("React", "React", "library"),
    CompletionItem("ReactDOM", "ReactDOM", "library"),
    CompletionItem("createRoot", "createRoot(document.getElementById('root'))", "react", 11),
    CompletionItem("Vue", "Vue", "library"),
    CompletionItem("createApp", "createApp({\n    data: () => ({})\n}).mount('#app')", "vue", 10),
    CompletionItem("THREE", "THREE", "library"),
    CompletionItem("gsap", "gsap", "library"),
    CompletionItem("anime", "anime", "library"),
    CompletionItem("axios", "axios", "library"),
    CompletionItem("_", "_", "lodash"),
    CompletionItem("d3", "d3", "library"),
    CompletionItem("Chart", "Chart", "library"),
    CompletionItem("dayjs", "dayjs", "library"),
    CompletionItem("firebase", "firebase", "library"),
    CompletionItem("supabase", "supabase", "library"),
    CompletionItem("p5", "p5", "library"),
    CompletionItem("Phaser", "Phaser", "library"),
    CompletionItem("PIXI", "PIXI", "library"),
    CompletionItem("BABYLON", "BABYLON", "library"),
    CompletionItem("Howl", "Howl", "library"),
    CompletionItem("Tone", "Tone", "library"),
    CompletionItem("$", "$", "jquery"),
    CompletionItem("jQuery", "jQuery", "jquery"),
    CompletionItem("bootstrap", "bootstrap", "library"),
    CompletionItem("import React", "import React from \"react\";\nimport { createRoot } from \"react-dom/client\";", "snippet"),
    CompletionItem("import Vue", "import { createApp } from \"vue\";", "snippet"),
    CompletionItem("import THREE", "import * as THREE from \"three\";", "snippet"),
    CompletionItem("import gsap", "import gsap from \"gsap\";", "snippet"),
    CompletionItem("import axios", "import axios from \"axios\";", "snippet"),
)

private val jsDocumentCompletions = listOf(
    CompletionItem("querySelector()", "querySelector('')", "dom", 15),
    CompletionItem("querySelectorAll()", "querySelectorAll('')", "dom", 18),
    CompletionItem("getElementById()", "getElementById('')", "dom", 16),
    CompletionItem("getElementsByClassName()", "getElementsByClassName('')", "dom", 24),
    CompletionItem("createElement()", "createElement('div')", "dom", 16),
    CompletionItem("addEventListener()", "addEventListener('DOMContentLoaded', () => {\n    \n})", "dom", 40),
    CompletionItem("body", "body", "dom"),
    CompletionItem("head", "head", "dom"),
    CompletionItem("documentElement", "documentElement", "dom"),
)

private val jsWindowCompletions = listOf(
    CompletionItem("addEventListener()", "addEventListener('resize', () => {\n    \n})", "dom", 31),
    CompletionItem("location", "location", "dom"),
    CompletionItem("innerWidth", "innerWidth", "dom"),
    CompletionItem("innerHeight", "innerHeight", "dom"),
    CompletionItem("requestAnimationFrame()", "requestAnimationFrame(() => {\n    \n})", "function", 30),
    CompletionItem("matchMedia()", "matchMedia('')", "function", 12),
)

private val jsStorageCompletions = listOf(
    CompletionItem("getItem()", "getItem('')", "dom", 9),
    CompletionItem("setItem()", "setItem('', '')", "dom", 9),
    CompletionItem("removeItem()", "removeItem('')", "dom", 12),
    CompletionItem("clear()", "clear()", "dom", 6),
)

private val jsJsonCompletions = listOf(
    CompletionItem("parse()", "parse('')", "function", 7),
    CompletionItem("stringify()", "stringify(value, null, 2)", "function", 10),
)

private val jsObjectCompletions = listOf(
    CompletionItem("keys()", "keys(object)", "function", 5),
    CompletionItem("values()", "values(object)", "function", 7),
    CompletionItem("entries()", "entries(object)", "function", 8),
    CompletionItem("assign()", "assign(target, source)", "function", 7),
    CompletionItem("hasOwn()", "hasOwn(object, key)", "function", 7),
)

private val jsArrayCompletions = listOf(
    CompletionItem("map()", "map(item => item)", "array", 4),
    CompletionItem("filter()", "filter(item => item)", "array", 7),
    CompletionItem("find()", "find(item => item)", "array", 5),
    CompletionItem("reduce()", "reduce((total, item) => total, 0)", "array", 7),
    CompletionItem("some()", "some(item => item)", "array", 5),
    CompletionItem("every()", "every(item => item)", "array", 6),
    CompletionItem("forEach()", "forEach(item => {\n    \n})", "array", 17),
    CompletionItem("push()", "push()", "array", 5),
    CompletionItem("pop()", "pop()", "array", 5),
    CompletionItem("slice()", "slice()", "array", 6),
    CompletionItem("sort()", "sort()", "array", 5),
    CompletionItem("includes()", "includes()", "array", 9),
    CompletionItem("join()", "join(', ')", "array", 5),
    CompletionItem("length", "length", "array"),
)

private val jsStringCompletions = listOf(
    CompletionItem("trim()", "trim()", "string", 5),
    CompletionItem("toLowerCase()", "toLowerCase()", "string", 12),
    CompletionItem("toUpperCase()", "toUpperCase()", "string", 12),
    CompletionItem("includes()", "includes('')", "string", 10),
    CompletionItem("replace()", "replace('', '')", "string", 9),
    CompletionItem("split()", "split('')", "string", 7),
    CompletionItem("slice()", "slice()", "string", 6),
    CompletionItem("match()", "match()", "string", 6),
    CompletionItem("padStart()", "padStart(2, '0')", "string", 9),
    CompletionItem("startsWith()", "startsWith('')", "string", 12),
    CompletionItem("endsWith()", "endsWith('')", "string", 10),
    CompletionItem("length", "length", "string"),
)

private val jsNumberCompletions = listOf(
    CompletionItem("toFixed()", "toFixed(2)", "number", 8),
    CompletionItem("toString()", "toString()", "number", 9),
    CompletionItem("toLocaleString()", "toLocaleString()", "number", 15),
)

private val jsDateCompletions = listOf(
    CompletionItem("toISOString()", "toISOString()", "date", 12),
    CompletionItem("toLocaleDateString()", "toLocaleDateString()", "date", 19),
    CompletionItem("getFullYear()", "getFullYear()", "date", 12),
    CompletionItem("getMonth()", "getMonth()", "date", 9),
    CompletionItem("getDate()", "getDate()", "date", 8),
    CompletionItem("getTime()", "getTime()", "date", 8),
)

private val jsPromiseCompletions = listOf(
    CompletionItem("then()", "then(value => value)", "promise", 5),
    CompletionItem("catch()", "catch(error => {\n    console.error(error);\n})", "promise", 6),
    CompletionItem("finally()", "finally(() => {\n    \n})", "promise", 16),
)

private val jsPromiseStaticCompletions = listOf(
    CompletionItem("resolve()", "resolve(value)", "promise", 8),
    CompletionItem("reject()", "reject(error)", "promise", 7),
    CompletionItem("all()", "all([])", "promise", 4),
    CompletionItem("race()", "race([])", "promise", 5),
    CompletionItem("allSettled()", "allSettled([])", "promise", 11),
)

private val jsNodeListCompletions = listOf(
    CompletionItem("forEach()", "forEach(element => {\n    \n})", "dom", 20),
    CompletionItem("item()", "item(0)", "dom", 5),
    CompletionItem("entries()", "entries()", "dom", 8),
    CompletionItem("length", "length", "dom"),
)

private val jsResponseCompletions = listOf(
    CompletionItem("json()", "json()", "promise", 5),
    CompletionItem("text()", "text()", "promise", 5),
    CompletionItem("blob()", "blob()", "promise", 5),
    CompletionItem("ok", "ok", "dom"),
    CompletionItem("status", "status", "dom"),
    CompletionItem("headers", "headers", "dom"),
)

private val jsHeadersCompletions = listOf(
    CompletionItem("get()", "get('content-type')", "dom", 5),
    CompletionItem("has()", "has('content-type')", "dom", 5),
    CompletionItem("set()", "set('', '')", "dom", 5),
    CompletionItem("append()", "append('', '')", "dom", 8),
    CompletionItem("entries()", "entries()", "dom", 8),
)

private val jsUrlCompletions = listOf(
    CompletionItem("href", "href", "dom"),
    CompletionItem("origin", "origin", "dom"),
    CompletionItem("pathname", "pathname", "dom"),
    CompletionItem("search", "search", "dom"),
    CompletionItem("searchParams", "searchParams", "dom"),
    CompletionItem("toString()", "toString()", "function", 9),
)

private val jsSearchParamsCompletions = listOf(
    CompletionItem("get()", "get('')", "dom", 5),
    CompletionItem("set()", "set('', '')", "dom", 5),
    CompletionItem("append()", "append('', '')", "dom", 8),
    CompletionItem("delete()", "delete('')", "dom", 8),
    CompletionItem("has()", "has('')", "dom", 5),
    CompletionItem("toString()", "toString()", "function", 9),
)

private val jsLocationCompletions = listOf(
    CompletionItem("href", "href", "dom"),
    CompletionItem("pathname", "pathname", "dom"),
    CompletionItem("search", "search", "dom"),
    CompletionItem("hash", "hash", "dom"),
    CompletionItem("assign()", "assign('')", "dom", 8),
    CompletionItem("replace()", "replace('')", "dom", 9),
    CompletionItem("reload()", "reload()", "dom", 7),
)

private val jsHistoryCompletions = listOf(
    CompletionItem("pushState()", "pushState({}, '', '')", "dom", 10),
    CompletionItem("replaceState()", "replaceState({}, '', '')", "dom", 13),
    CompletionItem("back()", "back()", "dom", 5),
    CompletionItem("forward()", "forward()", "dom", 8),
)

private val jsNavigatorCompletions = listOf(
    CompletionItem("clipboard", "clipboard", "dom"),
    CompletionItem("language", "language", "dom"),
    CompletionItem("onLine", "onLine", "dom"),
    CompletionItem("userAgent", "userAgent", "dom"),
    CompletionItem("share()", "share({ title: '', url: '' })", "dom", 6),
)

private val jsClipboardCompletions = listOf(
    CompletionItem("writeText()", "writeText('')", "promise", 11),
    CompletionItem("readText()", "readText()", "promise", 9),
)

private val jsEventCompletions = listOf(
    CompletionItem("target", "target", "dom"),
    CompletionItem("currentTarget", "currentTarget", "dom"),
    CompletionItem("preventDefault()", "preventDefault()", "dom", 15),
    CompletionItem("stopPropagation()", "stopPropagation()", "dom", 16),
    CompletionItem("key", "key", "dom"),
    CompletionItem("clientX", "clientX", "dom"),
    CompletionItem("clientY", "clientY", "dom"),
)

private val jsDatasetCompletions = listOf(
    CompletionItem("id", "id", "dom"),
    CompletionItem("state", "state", "dom"),
    CompletionItem("value", "value", "dom"),
    CompletionItem("index", "index", "dom"),
)

private val jsStyleCompletions = listOf(
    CompletionItem("display", "display = ''", "style", 11),
    CompletionItem("color", "color = ''", "style", 9),
    CompletionItem("background", "background = ''", "style", 14),
    CompletionItem("backgroundColor", "backgroundColor = ''", "style", 19),
    CompletionItem("width", "width = ''", "style", 9),
    CompletionItem("height", "height = ''", "style", 10),
    CompletionItem("transform", "transform = ''", "style", 13),
    CompletionItem("opacity", "opacity = ''", "style", 11),
)

private val jsElementCompletions = listOf(
    CompletionItem("addEventListener()", "addEventListener('click', () => {\n    \n})", "dom", 18),
    CompletionItem("classList", "classList", "dom"),
    CompletionItem("dataset", "dataset", "dom"),
    CompletionItem("value", "value", "dom"),
    CompletionItem("checked", "checked", "dom"),
    CompletionItem("disabled", "disabled", "dom"),
    CompletionItem("textContent", "textContent", "dom"),
    CompletionItem("innerHTML", "innerHTML", "dom"),
    CompletionItem("innerText", "innerText", "dom"),
    CompletionItem("style", "style", "dom"),
    CompletionItem("children", "children", "dom"),
    CompletionItem("parentElement", "parentElement", "dom"),
    CompletionItem("setAttribute()", "setAttribute('', '')", "dom", 14),
    CompletionItem("getAttribute()", "getAttribute('')", "dom", 14),
    CompletionItem("append()", "append()", "dom", 7),
    CompletionItem("appendChild()", "appendChild()", "dom", 12),
    CompletionItem("remove()", "remove()", "dom", 7),
)

private val jsReactCompletions = listOf(
    CompletionItem("createElement()", "createElement('div', null)", "react", 14),
    CompletionItem("useState()", "useState(initialValue)", "react", 9),
    CompletionItem("useEffect()", "useEffect(() => {\n    \n}, [])", "react", 17),
    CompletionItem("useMemo()", "useMemo(() => value, [])", "react", 8),
    CompletionItem("useRef()", "useRef(null)", "react", 7),
    CompletionItem("Fragment", "Fragment", "react"),
)

private val jsReactDomCompletions = listOf(
    CompletionItem("createRoot()", "createRoot(document.getElementById('root'))", "react", 11),
    CompletionItem("render()", "render()", "react", 7),
)

private val jsVueCompletions = listOf(
    CompletionItem("createApp()", "createApp({}).mount('#app')", "vue", 10),
    CompletionItem("ref()", "ref()", "vue", 4),
    CompletionItem("reactive()", "reactive({})", "vue", 9),
    CompletionItem("computed()", "computed(() => value)", "vue", 9),
    CompletionItem("watch()", "watch(source, callback)", "vue", 6),
)

private val jsThreeCompletions = listOf(
    CompletionItem("Scene()", "Scene()", "three", 6),
    CompletionItem("PerspectiveCamera()", "PerspectiveCamera(75, innerWidth / innerHeight, 0.1, 1000)", "three", 18),
    CompletionItem("WebGLRenderer()", "WebGLRenderer({ canvas })", "three", 14),
    CompletionItem("Mesh()", "Mesh(geometry, material)", "three", 5),
    CompletionItem("BoxGeometry()", "BoxGeometry(1, 1, 1)", "three", 12),
    CompletionItem("MeshBasicMaterial()", "MeshBasicMaterial({ color: 0xffffff })", "three", 18),
)

private val jsAnimationCompletions = listOf(
    CompletionItem("to()", "to(target, { duration: 0.5 })", "animation", 3),
    CompletionItem("from()", "from(target, { duration: 0.5 })", "animation", 5),
    CompletionItem("fromTo()", "fromTo(target, fromVars, toVars)", "animation", 7),
    CompletionItem("timeline()", "timeline()", "animation", 10),
)

private val jsHttpCompletions = listOf(
    CompletionItem("get()", "get('')", "http", 5),
    CompletionItem("post()", "post('', {})", "http", 6),
    CompletionItem("put()", "put('', {})", "http", 5),
    CompletionItem("delete()", "delete('')", "http", 8),
)

private val jsChartCompletions = listOf(
    CompletionItem("Chart()", "Chart(ctx, { type: 'bar', data: {} })", "chart", 6),
    CompletionItem("register()", "register()", "chart", 9),
)

private val jsD3Completions = listOf(
    CompletionItem("select()", "select('')", "d3", 8),
    CompletionItem("selectAll()", "selectAll('')", "d3", 11),
    CompletionItem("scaleLinear()", "scaleLinear()", "d3", 12),
    CompletionItem("axisBottom()", "axisBottom(scale)", "d3", 11),
    CompletionItem("json()", "json('')", "d3", 6),
)

private val jsJqueryCompletions = listOf(
    CompletionItem("on()", "on('click', () => {})", "jquery", 4),
    CompletionItem("addClass()", "addClass('')", "jquery", 10),
    CompletionItem("removeClass()", "removeClass('')", "jquery", 13),
    CompletionItem("css()", "css('', '')", "jquery", 5),
    CompletionItem("html()", "html('')", "jquery", 6),
    CompletionItem("ajax()", "ajax({ url: '' })", "jquery", 6),
)

private val jsLodashCompletions = listOf(
    CompletionItem("map()", "map(collection, iteratee)", "lodash", 4),
    CompletionItem("filter()", "filter(collection, predicate)", "lodash", 7),
    CompletionItem("debounce()", "debounce(func, 250)", "lodash", 9),
    CompletionItem("throttle()", "throttle(func, 250)", "lodash", 9),
    CompletionItem("cloneDeep()", "cloneDeep(value)", "lodash", 10),
)

private val jsFirebaseCompletions = listOf(
    CompletionItem("initializeApp()", "initializeApp(firebaseConfig)", "firebase", 14),
    CompletionItem("getAuth()", "getAuth(app)", "firebase", 8),
    CompletionItem("getFirestore()", "getFirestore(app)", "firebase", 13),
)

private val jsSupabaseCompletions = listOf(
    CompletionItem("createClient()", "createClient(url, anonKey)", "supabase", 13),
    CompletionItem("from()", "from('table')", "supabase", 5),
    CompletionItem("auth", "auth", "supabase"),
)

private val jsonCompletionItems = listOf(
    CompletionItem("\"name\"", "\"name\": \"\"", "field", 9),
    CompletionItem("\"version\"", "\"version\": \"1.0.0\"", "field"),
    CompletionItem("\"scripts\"", "\"scripts\": {\n    \"start\": \"\"\n}", "field", 29),
    CompletionItem("\"dependencies\"", "\"dependencies\": {\n    \n}", "field", 23),
)

private fun cssValueCompletionsFor(source: String, cursor: Int): List<CompletionItem> {
    val line = source.substring(0, cursor.coerceIn(0, source.length)).substringAfterLast('\n')
    val property = line.substringBefore(':').trim().lowercase()
    val variableValues = cssCustomPropertyValueCompletions(source)
        .map { CompletionItem("var(${it.label})", "var(${it.label})", "css value", it.label.length + 5) }
    val propertyOptions = cssPropertyValueOptions[property]
        ?.map { CompletionItem(it, it, "css value") }
    val colorValues = cssValueCompletions.filter {
        it.label.startsWith("#") || it.label.startsWith("rgba") || it.label.startsWith("var")
    }
    val base = when {
        property in setOf("color", "background", "background-color", "border-color", "outline-color", "box-shadow") -> colorValues
        propertyOptions != null -> propertyOptions
        else -> cssValueCompletions
    }
    return (base + variableValues).distinctBy { it.label }
}

private fun cssContextualCompletions(source: String, cursor: Int): List<CompletionItem> {
    val before = source.substring(0, cursor.coerceIn(0, source.length))
    val line = cssLineBeforeCursor(before)
    val lastOpen = before.lastIndexOf('{')
    val lastClose = before.lastIndexOf('}')
    val insideRuleBlock = lastOpen > lastClose

    return when {
        isCssAtRuleCompletion(before) -> cssAtRuleCompletions
        !insideRuleBlock -> userDefinedCssCompletions(source) + cssAtRuleCompletions + cssSelectorCompletions + cssPseudoClassCompletions
        line.substringAfterLast(':').contains("var(") -> cssCustomPropertyValueCompletions(source)
        else -> userDefinedCssCompletions(source) + cssCompletionItems + cssAtRuleCompletions
    }
}

private fun cssCustomPropertyValueCompletions(source: String): List<CompletionItem> {
    return Regex("""--[A-Za-z0-9_-]+(?=\s*:)""")
        .findAll(source)
        .map { CompletionItem(it.value, it.value, "css variable") }
        .distinctBy { it.label }
        .toList()
}

private fun userDefinedHtmlCompletions(source: String): List<CompletionItem> {
    val classes = Regex("""class\s*=\s*["']([^"']+)["']""")
        .findAll(source)
        .flatMap { it.groupValues[1].split(Regex("\\s+")) }
        .filter { it.isNotBlank() }
        .map { CompletionItem(".$it", "class=\"$it\"", "attribute", it.length + 8) }
        .toList()
    val ids = Regex("""id\s*=\s*["']([^"']+)["']""")
        .findAll(source)
        .map { it.groupValues[1] }
        .filter { it.isNotBlank() }
        .map { CompletionItem("#$it", "id=\"$it\"", "attribute", it.length + 4) }
        .toList()
    return (classes + ids).distinctBy { it.label }
}

private fun htmlClassValueCompletions(source: String): List<CompletionItem> {
    return Regex("""class\s*=\s*["']([^"']+)["']""")
        .findAll(source)
        .flatMap { it.groupValues[1].split(Regex("\\s+")) }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .map { CompletionItem(it, it, "attribute value") }
        .toList()
}

private fun htmlIdValueCompletions(source: String): List<CompletionItem> {
    return Regex("""id\s*=\s*["']([^"']+)["']""")
        .findAll(source)
        .map { it.groupValues[1].trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .map { CompletionItem(it, it, "attribute value") }
        .toList()
}

private fun userDefinedCssCompletions(source: String): List<CompletionItem> {
    val variables = cssCustomPropertyValueCompletions(source)
        .map { CompletionItem("var(${it.label})", "var(${it.label})", "css value", it.label.length + 5) }
    val selectors = Regex("""(?m)([.#][A-Za-z0-9_-]+)\s*\{""")
        .findAll(source)
        .map { CompletionItem(it.groupValues[1], it.groupValues[1], "selector") }
        .toList()
    val htmlClasses = Regex("""class\s*=\s*["']([^"']+)["']""")
        .findAll(source)
        .flatMap { it.groupValues[1].split(Regex("\\s+")) }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { CompletionItem(".$it", ".$it", "selector") }
        .toList()
    val htmlIds = Regex("""id\s*=\s*["']([^"']+)["']""")
        .findAll(source)
        .map { it.groupValues[1].trim() }
        .filter { it.isNotBlank() }
        .map { CompletionItem("#$it", "#$it", "selector") }
        .toList()
    return (variables + selectors + htmlClasses + htmlIds).distinctBy { it.label }
}

private fun userDefinedJsCompletions(source: String): List<CompletionItem> {
    val functions = Regex("""\bfunction\s+([A-Za-z_$][\w$]*)\s*\(([^)]*)\)""")
        .findAll(source)
        .map { match ->
            val name = match.groupValues[1]
            CompletionItem("$name()", "$name()", "function", name.length + 1)
        }
        .toList()
    val arrowFunctions = Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)\s*=\s*(?:async\s*)?\([^)]*\)\s*=>""")
        .findAll(source)
        .map { match ->
            val name = match.groupValues[1]
            CompletionItem("$name()", "$name()", "function", name.length + 1)
        }
        .toList()
    val classes = Regex("""\bclass\s+([A-Za-z_$][\w$]*)""")
        .findAll(source)
        .map { CompletionItem(it.groupValues[1], it.groupValues[1], "class") }
        .toList()
    val variables = Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)""")
        .findAll(source)
        .map { CompletionItem(it.groupValues[1], it.groupValues[1], "variable") }
        .toList()
    return (functions + arrowFunctions + classes + variables).distinctBy { it.label }
}

private fun jsDotCompletionsForType(type: String?): List<CompletionItem> {
    return when (type) {
        "document" -> jsDocumentCompletions
        "window" -> jsWindowCompletions
        "storage" -> jsStorageCompletions
        "json" -> jsJsonCompletions
        "object" -> jsObjectCompletions
        "array" -> jsArrayCompletions
        "nodeList" -> jsNodeListCompletions
        "string" -> jsStringCompletions
        "number" -> jsNumberCompletions
        "date" -> jsDateCompletions
        "promise" -> jsPromiseCompletions
        "promiseStatic" -> jsPromiseStaticCompletions
        "response" -> jsResponseCompletions
        "headers" -> jsHeadersCompletions
        "url" -> jsUrlCompletions
        "searchParams" -> jsSearchParamsCompletions
        "location" -> jsLocationCompletions
        "history" -> jsHistoryCompletions
        "navigator" -> jsNavigatorCompletions
        "clipboard" -> jsClipboardCompletions
        "event" -> jsEventCompletions
        "dataset" -> jsDatasetCompletions
        "element" -> jsElementCompletions
        "style" -> jsStyleCompletions
        "react" -> jsReactCompletions
        "reactDom" -> jsReactDomCompletions
        "vue" -> jsVueCompletions
        "three" -> jsThreeCompletions
        "animation" -> jsAnimationCompletions
        "http" -> jsHttpCompletions
        "chart" -> jsChartCompletions
        "d3" -> jsD3Completions
        "jquery" -> jsJqueryCompletions
        "lodash" -> jsLodashCompletions
        "firebase" -> jsFirebaseCompletions
        "supabase" -> jsSupabaseCompletions
        "classList" -> listOf(
            CompletionItem("add()", "add('')", "dom", 5),
            CompletionItem("remove()", "remove('')", "dom", 8),
            CompletionItem("toggle()", "toggle('')", "dom", 8),
            CompletionItem("contains()", "contains('')", "dom", 10),
        )
        "console" -> listOf(
            CompletionItem("log()", "log()", "function", 4),
            CompletionItem("error()", "error()", "function", 6),
            CompletionItem("warn()", "warn()", "function", 5),
            CompletionItem("table()", "table()", "function", 6),
        )
        "math" -> listOf(
            CompletionItem("random()", "random()", "function", 7),
            CompletionItem("round()", "round()", "function", 6),
            CompletionItem("floor()", "floor()", "function", 6),
            CompletionItem("ceil()", "ceil()", "function", 5),
            CompletionItem("max()", "max()", "function", 4),
            CompletionItem("min()", "min()", "function", 4),
        )
        else -> jsDocumentCompletions + jsElementCompletions + jsArrayCompletions + jsStringCompletions
    }
}

private fun jsDotCompletionsForSource(source: String, dotIndex: Int): List<CompletionItem> {
    val targetName = jsDotTargetName(source, dotIndex)
    val customMembers = targetName
        ?.let { userDefinedJsMemberCompletions(source, it) }
        .orEmpty()
    return (customMembers + jsDotCompletionsForType(inferJsDotTargetType(source, dotIndex)))
        .distinctBy { it.completionKey() }
}

private fun jsDotTargetName(source: String, dotIndex: Int): String? {
    val beforeDot = source.substring(0, dotIndex.coerceIn(0, source.length)).trimEnd()
    return Regex("""([A-Za-z_$][\w$]*)$""")
        .find(beforeDot)
        ?.groupValues
        ?.getOrNull(1)
}

private fun userDefinedJsMemberCompletions(source: String, variable: String): List<CompletionItem> {
    val objectBody = Regex("""(?s)\b(?:const|let|var)\s+${Regex.escape(variable)}\s*=\s*\{(.*?)\}""")
        .findAll(source)
        .lastOrNull()
        ?.groupValues
        ?.getOrNull(1)
        ?: return emptyList()
    val properties = Regex("""(?m)\b([A-Za-z_$][\w$]*)\s*:""")
        .findAll(objectBody)
        .map { it.groupValues[1] }
    val methods = Regex("""(?m)\b([A-Za-z_$][\w$]*)\s*\([^)]*\)\s*\{""")
        .findAll(objectBody)
        .map { "${it.groupValues[1]}()" }
    return (properties + methods)
        .distinct()
        .map { name ->
            if (name.endsWith("()")) {
                CompletionItem(name, name, "function", name.length - 1)
            } else {
                CompletionItem(name, name, "variable")
            }
        }
        .toList()
}

private fun inferJsDotTargetType(source: String, dotIndex: Int): String? {
    val beforeDot = source.substring(0, dotIndex.coerceIn(0, source.length)).trimEnd()
    if (beforeDot.endsWith("document")) return "document"
    if (beforeDot.endsWith("window")) return "window"
    if (beforeDot.endsWith("navigator")) return "navigator"
    if (beforeDot.endsWith("clipboard")) return "clipboard"
    if (beforeDot.endsWith("history")) return "history"
    if (beforeDot.endsWith("location") || beforeDot.endsWith("window.location")) return "location"
    if (beforeDot.endsWith("localStorage") || beforeDot.endsWith("sessionStorage")) return "storage"
    if (beforeDot.endsWith("JSON")) return "json"
    if (beforeDot.endsWith("Object")) return "object"
    if (beforeDot.endsWith("Array")) return "array"
    if (beforeDot.endsWith("Promise")) return "promiseStatic"
    if (beforeDot.endsWith("URL")) return "url"
    if (beforeDot.endsWith("classList")) return "classList"
    if (beforeDot.endsWith("console")) return "console"
    if (beforeDot.endsWith("Math")) return "math"
    if (beforeDot.endsWith("style")) return "style"
    if (beforeDot.endsWith("dataset")) return "dataset"
    if (beforeDot.endsWith("React")) return "react"
    if (beforeDot.endsWith("ReactDOM")) return "reactDom"
    if (beforeDot.endsWith("Vue")) return "vue"
    if (beforeDot.endsWith("THREE")) return "three"
    if (beforeDot.endsWith("gsap") || beforeDot.endsWith("anime")) return "animation"
    if (beforeDot.endsWith("axios")) return "http"
    if (beforeDot.endsWith("Chart")) return "chart"
    if (beforeDot.endsWith("d3")) return "d3"
    if (beforeDot.endsWith("$") || beforeDot.endsWith("jQuery")) return "jquery"
    if (beforeDot.endsWith("_")) return "lodash"
    if (beforeDot.endsWith("firebase")) return "firebase"
    if (beforeDot.endsWith("supabase")) return "supabase"
    if (beforeDot.endsWith("headers")) return "headers"
    if (beforeDot.endsWith("searchParams")) return "searchParams"
    if (beforeDot.endsWith("target") || beforeDot.endsWith("currentTarget")) return "element"
    if (beforeDot.endsWith("\"") || beforeDot.endsWith("'") || beforeDot.endsWith("`")) return "string"
    if (beforeDot.endsWith("]")) return "array"
    if (Regex("""(?:querySelectorAll|getElementsByClassName|getElementsByTagName)\([^)]*\)$""").containsMatchIn(beforeDot)) return "nodeList"
    if (Regex("""Array\.from\([^)]*\)$""").containsMatchIn(beforeDot)) return "array"
    if (Regex("""(?:querySelector|getElementById|createElement)\([^)]*\)$""").containsMatchIn(beforeDot)) return "element"
    if (Regex("""(?:new\s+Date|Date)\([^)]*\)$""").containsMatchIn(beforeDot)) return "date"
    if (Regex("""new\s+URL\([^)]*\)$""").containsMatchIn(beforeDot)) return "url"
    if (Regex("""fetch\([^)]*\)$""").containsMatchIn(beforeDot)) return "promise"
    val variable = Regex("""([A-Za-z_$][\w$]*)$""").find(beforeDot)?.groupValues?.getOrNull(1) ?: return null
    if (variable.equals("response", ignoreCase = true)) return "response"
    if (variable.equals("event", ignoreCase = true)) return "event"
    if (variable.equals("e", ignoreCase = true)) return "event"
    val assignment = Regex("""(?m)\b(?:const|let|var)\s+${Regex.escape(variable)}\s*=\s*(.+)$""")
        .findAll(source)
        .lastOrNull()
        ?.groupValues
        ?.getOrNull(1)
        ?.trimStart()
        ?: return null
    return when {
        assignment.startsWith("[") || assignment.contains("Array.from(") -> "array"
        assignment.startsWith("{") -> "object"
        assignment.startsWith("new Date") || assignment.startsWith("Date(") -> "date"
        assignment.startsWith("new URL") -> "url"
        assignment.startsWith("new URLSearchParams") -> "searchParams"
        assignment.startsWith("fetch(") -> "promise"
        assignment.matches(Regex("""[-+]?\d+(\.\d+)?[;,\s)]*.*""")) -> "number"
        assignment.contains("querySelectorAll(") || assignment.contains("getElementsByClassName(") || assignment.contains("getElementsByTagName(") -> "nodeList"
        assignment.startsWith("\"") || assignment.startsWith("'") || assignment.startsWith("`") -> "string"
        assignment.contains("querySelector(") || assignment.contains("getElementById(") || assignment.contains("createElement(") -> "element"
        assignment.contains("addEventListener(") -> "event"
        else -> null
    }
}

private fun shouldComputeSignatureHelp(source: String, rawCursor: Int): Boolean {
    val cursor = rawCursor.coerceIn(0, source.length)
    val windowStart = maxOf(0, cursor - 320)
    val nearby = source.substring(windowStart, cursor)
    val openParen = nearby.lastIndexOf('(')
    return openParen >= 0 && openParen > nearby.lastIndexOf(')')
}

private fun webSignatureHelpAt(fileName: String, source: String, rawCursor: Int): String? {
    val cursor = rawCursor.coerceIn(0, source.length)
    val language = webLanguageAt(fileName, source, cursor)
    if (language !in setOf(WebEditorLanguage.JavaScript, WebEditorLanguage.Css)) return null
    val openParen = activeCallOpenParen(source, cursor) ?: return null
    var nameEnd = openParen
    while (nameEnd > 0 && source[nameEnd - 1].isWhitespace()) nameEnd -= 1
    var nameStart = nameEnd
    while (nameStart > 0 && (isIdentifierPart(source[nameStart - 1]) || source[nameStart - 1] == '.')) nameStart -= 1
    if (nameStart == nameEnd) return null
    val name = source.substring(nameStart, nameEnd).substringAfterLast('.')
    return webCompletionSyntaxes["$name|function"]
        ?: webCompletionSyntaxes["$name|dom"]
        ?: webCompletionSyntaxes["$name|array"]
        ?: webCompletionSyntaxes["$name|string"]
        ?: when (name) {
            "rgba" -> "rgba(red, green, blue, alpha)"
            "repeat" -> "repeat(count, track-size)"
            "var" -> "var(--custom-property, fallback)"
            else -> null
        }
}

private fun activeCallOpenParen(source: String, cursor: Int): Int? {
    val stack = mutableListOf<Int>()
    var quote: Char? = null
    var template = false
    var lineComment = false
    var blockComment = false
    var index = 0
    while (index < cursor) {
        val char = source[index]
        val next = source.getOrNull(index + 1)
        when {
            lineComment -> {
                if (char == '\n') lineComment = false
                index += 1
            }
            blockComment -> {
                if (char == '*' && next == '/') {
                    blockComment = false
                    index += 2
                } else {
                    index += 1
                }
            }
            quote != null -> {
                index += if (char == '\\') 2 else 1
                if (char == quote && source.getOrNull(index - 2) != '\\') {
                    quote = null
                    template = false
                }
            }
            char == '/' && next == '/' -> {
                lineComment = true
                index += 2
            }
            char == '/' && next == '*' -> {
                blockComment = true
                index += 2
            }
            char == '"' || char == '\'' || char == '`' -> {
                quote = char
                template = char == '`'
                index += 1
            }
            char == '(' -> {
                stack += index
                index += 1
            }
            char == ')' -> {
                if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                index += 1
            }
            else -> index += 1
        }
        if (!template) {
            // Keep template flag referenced so Kotlin does not optimize away intent in this lightweight scanner.
        }
    }
    return stack.lastOrNull()
}

private fun applySmartWebEditorChange(
    current: TextFieldValue,
    requested: TextFieldValue,
    fileName: String,
): TextFieldValue {
    val inserted = singleInsertedText(current.text, requested.text) ?: return requested
    val insertedText = inserted.second
    if (insertedText.length != 1) return requested

    val char = insertedText.single()
    if (current.selection.collapsed && char in listOf(')', ']', '}', '>', '"', '\'', '`')) {
        if (current.text.getOrNull(current.selection.start) == char) {
            return current.copy(selection = TextRange(current.selection.start + 1))
        }
    }

    return when (char) {
        '(' -> wrapOrInsertPair(current, "(", ")")
        '[' -> wrapOrInsertPair(current, "[", "]")
        '{' -> wrapOrInsertPair(current, "{", "}")
        '"' -> wrapOrInsertPair(current, "\"", "\"")
        '\'' -> wrapOrInsertPair(current, "'", "'")
        '`' -> wrapOrInsertPair(current, "`", "`")
        '<' -> if (fileExtension(fileName) in htmlFileExtensions) wrapOrInsertPair(current, "<", ">") else requested
        '\n' -> if (current.selection.collapsed) insertWebIndentedNewline(current, fileName) else requested
        else -> requested
    }
}

private fun insertWebIndentedNewline(value: TextFieldValue, fileName: String): TextFieldValue {
    val cursor = value.selection.start
    val currentLine = value.text.substring(0, cursor).substringAfterLast('\n')
    val existingIndent = currentLine.takeWhile { it == ' ' || it == '\t' }
    val trimmed = currentLine.trimEnd()
    val blockIndent = when {
        trimmed.endsWith("{") || trimmed.endsWith("(") || trimmed.endsWith("[") -> "    "
        fileExtension(fileName) in htmlFileExtensions && opensHtmlBlock(trimmed) -> "    "
        else -> ""
    }
    return insertCodeAtSelection(value, "\n$existingIndent$blockIndent")
}

private fun insertClosingHtmlTag(value: TextFieldValue): TextFieldValue {
    val tag = openHtmlTagsBefore(value.text, value.selection.start).lastOrNull() ?: return insertCodeAtSelection(value, "</")
    return insertCodeAtSelection(value, "</$tag>")
}

private fun singleInsertedText(oldText: String, newText: String): Pair<Int, String>? {
    if (newText.length <= oldText.length) return null
    var prefix = 0
    val shortest = minOf(oldText.length, newText.length)
    while (prefix < shortest && oldText[prefix] == newText[prefix]) prefix += 1
    var suffix = 0
    while (
        suffix < oldText.length - prefix &&
        suffix < newText.length - prefix &&
        oldText[oldText.lastIndex - suffix] == newText[newText.lastIndex - suffix]
    ) {
        suffix += 1
    }
    if (oldText.length - prefix - suffix != 0) return null
    return prefix to newText.substring(prefix, newText.length - suffix)
}

private fun insertCodeAtSelection(
    value: TextFieldValue,
    insertText: String,
    cursorOffset: Int = insertText.length,
): TextFieldValue {
    val start = minOf(value.selection.start, value.selection.end)
    val end = maxOf(value.selection.start, value.selection.end)
    val newText = value.text.replaceRange(start, end, insertText)
    return TextFieldValue(
        text = newText,
        selection = TextRange((start + cursorOffset).coerceIn(0, newText.length)),
    )
}

private fun wrapOrInsertPair(value: TextFieldValue, open: String, close: String): TextFieldValue {
    val start = minOf(value.selection.start, value.selection.end)
    val end = maxOf(value.selection.start, value.selection.end)
    val selected = value.text.substring(start, end)
    val replacement = open + selected + close
    val newText = value.text.replaceRange(start, end, replacement)
    val cursor = if (selected.isEmpty()) start + open.length else start + replacement.length
    return TextFieldValue(newText, TextRange(cursor.coerceIn(0, newText.length)))
}

internal fun inspectWebSource(fileName: String, source: String): List<CodeDiagnostic> {
    return when (fileExtension(fileName)) {
        in htmlFileExtensions -> inspectHtmlSource(source)
        in cssFileExtensions -> inspectCssSource(source)
        in javascriptFileExtensions -> inspectJavaScriptSource(source)
        "json" -> inspectJsonSource(source)
        else -> emptyList()
    }.take(30)
}

private fun inspectHtmlSource(source: String): List<CodeDiagnostic> {
    val diagnostics = mutableListOf<CodeDiagnostic>()
    val stack = mutableListOf<Pair<String, Int>>()
    val scanSource = maskEmbeddedHtmlContent(source)
    val ids = mutableSetOf<String>()

    Regex("""<!--[\s\S]*?-->|<[^>]+>""").findAll(scanSource).forEach { match ->
        val token = match.value
        if (token.startsWith("<!--")) return@forEach
        val tagName = Regex("""^<\s*/?\s*([A-Za-z][A-Za-z0-9-]*)""")
            .find(token)
            ?.groupValues
            ?.getOrNull(1)
            ?.lowercase()
            ?: return@forEach

        // Check for duplicate IDs
        Regex("""\sid\s*=\s*["']([^"']+)["']""").find(token)?.let { idMatch ->
            val id = idMatch.groupValues[1]
            if (id in ids) {
                diagnostics += diagnosticAt(source, match.range.first + idMatch.range.first, match.range.first + idMatch.range.last + 1, "warning", "Duplicate ID '$id' found in document.")
            } else {
                ids += id
            }
        }

        val closing = token.startsWith("</")
        val selfClosing = token.endsWith("/>") || tagName in htmlVoidTags
        when {
            closing -> {
                val last = stack.indexOfLast { it.first == tagName }
                if (last < 0) {
                    diagnostics += diagnosticAt(source, match.range.first, match.range.last + 1, "error", "Closing tag </$tagName> has no matching opening tag.")
                } else {
                    for (index in stack.lastIndex downTo last + 1) {
                        val unclosed = stack.removeAt(index)
                        diagnostics += diagnosticAt(source, unclosed.second, unclosed.second + unclosed.first.length + 2, "error", "Element <${unclosed.first}> is not closed before </$tagName>.")
                    }
                    stack.removeAt(last)
                }
            }
            !selfClosing -> stack += tagName to match.range.first
        }

        // Image alt check
        if (tagName == "img" && !token.contains("alt=", ignoreCase = true)) {
            diagnostics += diagnosticAt(source, match.range.first, match.range.last + 1, "warning", "Images should have an 'alt' attribute for accessibility.")
        }

        Regex("""\s([A-Za-z_:][-A-Za-z0-9_:.]*)\s*=\s*([^"'\s>][^\s>]*)""")
            .findAll(maskHtmlQuotedAttributeValues(token))
            .forEach { attr ->
                val start = match.range.first + attr.range.first
                diagnostics += diagnosticAt(source, start, start + attr.value.length, "warning", "Quote attribute values for predictable HTML parsing.")
            }
    }
    diagnostics += inspectEmbeddedWebBlocks(source)
    stack.asReversed().forEach { (tag, start) ->
        diagnostics += diagnosticAt(source, start, (start + tag.length + 2).coerceAtMost(source.length), "error", "Element <$tag> is missing a closing tag.")
    }
    if (source.isNotBlank() && !source.contains("<!DOCTYPE html>", ignoreCase = true)) {
        diagnostics += diagnosticAt(source, 0, source.lineEndAt(0), "hint", "Add <!DOCTYPE html> at the top for standards mode.")
    }
    if (source.contains("<head", ignoreCase = true) && !source.contains("name=\"viewport\"", ignoreCase = true)) {
        val headStart = source.indexOf("<head", ignoreCase = true).coerceAtLeast(0)
        diagnostics += diagnosticAt(source, headStart, source.lineEndAt(headStart), "warning", "Add a viewport meta tag for mobile layouts.")
    }
    return diagnostics
}

private fun embeddedWebBlocks(source: String): List<EmbeddedWebBlock> {
    return Regex("""(?is)<(script|style)\b[^>]*>([\s\S]*?)</\1\s*>""")
        .findAll(source)
        .mapNotNull { match ->
            val content = match.groups[2] ?: return@mapNotNull null
            val language = when (match.groupValues[1].lowercase()) {
                "script" -> WebEditorLanguage.JavaScript
                "style" -> WebEditorLanguage.Css
                else -> return@mapNotNull null
            }
            EmbeddedWebBlock(language, content.range.first, content.range.last + 1)
        }
        .toList()
}

private fun maskEmbeddedHtmlContent(source: String): String {
    if (source.isEmpty()) return source
    val chars = source.toCharArray()
    embeddedWebBlocks(source).forEach { block ->
        for (index in block.start until block.end.coerceAtMost(chars.size)) {
            if (chars[index] != '\n') chars[index] = ' '
        }
    }
    return String(chars)
}

private fun maskHtmlQuotedAttributeValues(token: String): String {
    if ('"' !in token && '\'' !in token) return token
    val chars = token.toCharArray()
    var quote: Char? = null
    var index = 0
    while (index < chars.size) {
        val char = chars[index]
        if (quote == null) {
            if ((char == '"' || char == '\'') && index > 0 && token.substring(0, index).trimEnd().endsWith("=")) {
                quote = char
            }
        } else if (char == quote) {
            quote = null
        } else if (char != '\n') {
            chars[index] = ' '
        }
        index += 1
    }
    return String(chars)
}

private fun inspectEmbeddedWebBlocks(source: String): List<CodeDiagnostic> {
    return embeddedWebBlocks(source).flatMap { block ->
        val content = source.substring(block.start, block.end)
        val diagnostics = when (block.language) {
            WebEditorLanguage.Css -> inspectCssSource(content)
            WebEditorLanguage.JavaScript -> inspectJavaScriptSource(content)
            else -> emptyList()
        }
        diagnostics.map { diagnostic ->
            diagnosticAt(
                source = source,
                start = block.start + diagnostic.start,
                end = block.start + diagnostic.end,
                severity = diagnostic.severity,
                message = diagnostic.message,
            )
        }
    }
}

private fun inspectCssSource(source: String): List<CodeDiagnostic> {
    val diagnostics = mutableListOf<CodeDiagnostic>()
    diagnostics += delimiterDiagnostics(source, allowLineComments = false)

    Regex("""([^{}]*)\{([^}]*)\}""").findAll(source).forEach { match ->
        val blockGroup = match.groups[2] ?: return@forEach
        val blockContent = blockGroup.value
        val blockStart = blockGroup.range.first
        val props = mutableSetOf<String>()
        var lineOffset = 0
        blockContent.lines().forEach { line ->
            val propMatch = Regex("""^\s*(--[A-Za-z0-9_-]+|[A-Za-z-][A-Za-z0-9-]*)\s*:""").find(line)
            if (propMatch != null) {
                val prop = propMatch.groupValues[1].lowercase()
                val propertyStart = blockStart + lineOffset + (propMatch.groups[1]?.range?.first ?: propMatch.range.first)
                if (prop in props) {
                    diagnostics += diagnosticAt(source, propertyStart, propertyStart + prop.length, "warning", "Duplicate property '$prop' in CSS rule.")
                } else {
                    props += prop
                }
                if (!prop.startsWith("--") && prop !in knownCssProperties) {
                    diagnostics += diagnosticAt(source, propertyStart, propertyStart + prop.length, "warning", "Unknown or unsupported CSS property '$prop'.")
                }
            }
            lineOffset += line.length + 1
        }
    }

    Regex("""#[0-9a-fA-F]+""").findAll(source).forEach { match ->
        val length = match.value.length
        if (length !in setOf(4, 5, 7, 9)) {
            diagnostics += diagnosticAt(source, match.range.first, match.range.last + 1, "error", "Hex colors must be #RGB, #RGBA, #RRGGBB, or #RRGGBBAA.")
        }
    }
    source.lines().fold(0) { offset, line ->
        val trimmed = line.trim()
        val declarationWithoutColon = Regex("""^[A-Za-z-]+\s+[^:;{}]+;?$""").matches(trimmed)
        if (
            declarationWithoutColon &&
            !trimmed.startsWith("@") &&
            !trimmed.startsWith("from ") &&
            !trimmed.startsWith("to ")
        ) {
            diagnostics += diagnosticAt(source, offset + line.indexOf(trimmed), offset + line.length, "error", "CSS declarations need a colon between the property and value.")
        }
        if (
            trimmed.contains(":") &&
            !trimmed.endsWith(";") &&
            !trimmed.endsWith("{") &&
            !trimmed.endsWith("}") &&
            !trimmed.startsWith("@") &&
            !trimmed.startsWith("/*") &&
            !trimmed.endsWith("*/")
        ) {
            diagnostics += diagnosticAt(source, offset + line.indexOf(trimmed), offset + line.length, "warning", "CSS declarations usually end with a semicolon.")
        }
        offset + line.length + 1
    }
    return diagnostics
}

private fun inspectJavaScriptSource(source: String): List<CodeDiagnostic> {
    val diagnostics = mutableListOf<CodeDiagnostic>()
    diagnostics += delimiterDiagnostics(source, allowLineComments = true)
    jsCommonIdentifierTypos.forEach { (typo, correction) ->
        Regex("""\b${Regex.escape(typo)}\b""")
            .findAll(source)
            .forEach { match ->
                diagnostics += diagnosticAt(source, match.range.first, match.range.last + 1, "warning", "Did you mean '$correction'?")
            }
    }

    // Check for debugger and eval
    Regex("""\bdebugger\b""").findAll(source).forEach { match ->
        diagnostics += diagnosticAt(source, match.range.first, match.range.last + 1, "warning", "Remove 'debugger' statements before production.")
    }
    Regex("""\beval\s*\(""").findAll(source).forEach { match ->
        diagnostics += diagnosticAt(source, match.range.first, match.range.last + 4, "error", "Avoid using 'eval()'; it is a security risk.")
    }

    val declared = mutableMapOf<String, Int>()
    Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)""").findAll(source).forEach { match ->
        val name = match.groupValues[1]
        val previous = declared[name]
        if (previous != null) {
            diagnostics += diagnosticAt(source, match.range.first, match.range.last + 1, "error", "Variable '$name' is already declared on line ${lineNumberAt(source, previous)}.")
        } else {
            declared[name] = match.range.first
        }
    }

    // Check for duplicate keys in objects
    Regex("""\{\s*([A-Za-z_$][\w$]*)\s*:""").findAll(source).forEach { _ ->
        // This is complex for regex, but we can do a per-line basic check for small objects
    }

    Regex("""\b(document\.querySelector|getElementById|querySelectorAll)\(\s*["']\s*["']\s*\)""")
        .findAll(source)
        .forEach { match ->
            diagnostics += diagnosticAt(source, match.range.first, match.range.last + 1, "warning", "Selector calls need a non-empty selector.")
        }
    val danglingKeyword = Regex("""^(else|catch|finally)\b""")
    val keywordWithoutCondition = Regex("""^(if|while|switch)\s*(?:\{|$)""")
    val invalidDeclaration = Regex("""^(const|let|var)\s*=""")
    source.lines().fold(0) { offset, line ->
        val code = line.substringBefore("//")
        val trimmed = code.trim()
        val lineStart = offset + line.indexOf(code).coerceAtLeast(0)

        if (danglingKeyword.containsMatchIn(trimmed)) {
            val before = source.substring(0, offset).trimEnd()
            if (!before.endsWith("}")) {
                diagnostics += diagnosticAt(source, lineStart, offset + line.length, "error", "${trimmed.substringBefore(" ")} needs a preceding block.")
            }
        }
        invalidDeclaration.find(trimmed)?.let { match ->
            val start = lineStart + match.range.first
            diagnostics += diagnosticAt(source, start, offset + line.length, "error", "Declarations need a variable name before '='.")
        }
        keywordWithoutCondition.find(trimmed)?.let { match ->
            val keyword = match.groupValues[1]
            val start = lineStart + match.range.first
            diagnostics += diagnosticAt(source, start, (start + keyword.length).coerceAtMost(offset + line.length), "error", "$keyword needs a condition.")
        }
        Regex("""^(const|let|var)\s+([A-Za-z_$][\w$]*)\s*(?:=\s*)?(;)?$""")
            .find(trimmed)
            ?.let { match ->
                val keyword = match.groupValues[1]
                val hasEquals = trimmed.contains("=")
                val hasOnlyTerminator = match.groupValues[3].isNotBlank()
                if (keyword == "const" && !hasEquals) {
                    diagnostics += diagnosticAt(source, lineStart, offset + line.length, "error", "const declarations need an initializer.")
                } else if (hasEquals && (hasOnlyTerminator || trimmed.endsWith("="))) {
                    diagnostics += diagnosticAt(source, lineStart, offset + line.length, "error", "Assignment is missing a value.")
                }
            }
        Regex("""^function\s+[A-Za-z_$][\w$]*\s*\([^)]*\)\s*$""")
            .find(trimmed)
            ?.let {
                diagnostics += diagnosticAt(source, lineStart, offset + line.length, "error", "Function declarations need a body block.")
            }
        Regex("""^function\s*\(""")
            .find(trimmed)
            ?.let {
                diagnostics += diagnosticAt(source, lineStart, offset + line.length, "error", "Function declarations need a name, or assign the function to a variable.")
            }
        Regex("""\b(if|while|switch|catch)\s*\(\s*\)""")
            .find(trimmed)
            ?.let { match ->
                val start = lineStart + match.range.first
                diagnostics += diagnosticAt(source, start, start + match.value.length, "error", "${match.groupValues[1]} needs a condition.")
            }
        Regex("""\b(if|while)\s*\([^)]*[^=!<>]=[^=][^)]*\)""")
            .find(trimmed)
            ?.let { match ->
                val start = lineStart + match.range.first
                diagnostics += diagnosticAt(source, start, start + match.value.length, "warning", "Use '===' for comparison; '=' assigns a value.")
            }
        if (looksLikeJavaScriptStatement(trimmed) && !hasJavaScriptStatementTerminator(trimmed)) {
            diagnostics += diagnosticAt(source, lineStart, offset + line.length, "hint", "JavaScript statements usually end with a semicolon.")
        }
        val trailingDot = line.lastIndexOf('.')
        if (trimmed.endsWith(".") && line.getOrNull(trailingDot - 1)?.isDigit() != true) {
            diagnostics += diagnosticAt(source, offset + trailingDot, offset + line.length, "error", "Property access needs a member name after '.'.")
        }
        if (trimmed.matches(Regex("""^(if|for|while|switch|catch)\s*$"""))) {
            diagnostics += diagnosticAt(source, lineStart, offset + line.length, "error", "$trimmed needs a condition in parentheses.")
        }
        offset + line.length + 1
    }
    return diagnostics
}

private fun inspectJsonSource(source: String): List<CodeDiagnostic> {
    return runCatching {
        JSONObject(source)
        emptyList<CodeDiagnostic>()
    }.getOrElse { error ->
        listOf(diagnosticAt(source, 0, source.lineEndAt(0), "error", error.message ?: "Invalid JSON."))
    }
}

private fun delimiterDiagnostics(source: String, allowLineComments: Boolean): List<CodeDiagnostic> {
    val diagnostics = mutableListOf<CodeDiagnostic>()
    val stack = mutableListOf<Pair<Char, Int>>()
    var quote: Char? = null
    var quoteStart = 0
    var lineComment = false
    var blockComment = false
    var blockCommentStart = 0
    var index = 0
    while (index < source.length) {
        val char = source[index]
        val next = source.getOrNull(index + 1)
        when {
            lineComment -> {
                if (char == '\n') lineComment = false
                index += 1
            }
            blockComment -> {
                if (char == '*' && next == '/') {
                    blockComment = false
                    index += 2
                } else {
                    index += 1
                }
            }
            quote != null -> {
                if (char == '\\') {
                    index += 2
                } else {
                    if (char == quote) quote = null
                    if (char == '\n' && quote != '`') {
                        diagnostics += diagnosticAt(source, quoteStart, index, "error", "String literal is missing a closing ${quote ?: '"'} quote.")
                        quote = null
                    }
                    index += 1
                }
            }
            allowLineComments && char == '/' && next == '/' -> {
                lineComment = true
                index += 2
            }
            char == '/' && next == '*' -> {
                blockComment = true
                blockCommentStart = index
                index += 2
            }
            char == '"' || char == '\'' || (allowLineComments && char == '`') -> {
                quote = char
                quoteStart = index
                index += 1
            }
            char in "({[" -> {
                stack += char to index
                index += 1
            }
            char in ")}]" -> {
                val expectedOpen = when (char) {
                    ')' -> '('
                    '}' -> '{'
                    else -> '['
                }
                if (stack.lastOrNull()?.first == expectedOpen) {
                    stack.removeAt(stack.lastIndex)
                } else {
                    diagnostics += diagnosticAt(source, index, index + 1, "error", "Unexpected '$char'.")
                }
                index += 1
            }
            else -> index += 1
        }
    }
    quote?.let {
        diagnostics += diagnosticAt(source, quoteStart, source.length, "error", "String literal is missing a closing $it quote.")
    }
    if (blockComment) {
        diagnostics += diagnosticAt(source, blockCommentStart, source.length, "error", "Block comment is missing */.")
    }
    stack.asReversed().forEach { (open, start) ->
        val close = when (open) {
            '(' -> ')'
            '{' -> '}'
            else -> ']'
        }
        diagnostics += diagnosticAt(source, start, start + 1, "error", "Missing closing '$close'.")
    }
    return diagnostics
}

private fun diagnosticAt(source: String, start: Int, end: Int, severity: String, message: String): CodeDiagnostic {
    val safeStart = start.coerceIn(0, source.length)
    val safeEnd = end.coerceIn(safeStart, source.length)
    val line = lineNumberAt(source, safeStart)
    val lineStart = source.lastIndexOf('\n', (safeStart - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
    return CodeDiagnostic(
        line = line,
        column = safeStart - lineStart + 1,
        start = safeStart,
        end = if (safeEnd > safeStart) safeEnd else (safeStart + 1).coerceAtMost(source.length),
        severity = severity,
        message = message,
    )
}

private fun lineNumberAt(source: String, index: Int): Int {
    return source.substring(0, index.coerceIn(0, source.length)).count { it == '\n' } + 1
}

private fun String.lineEndAt(index: Int): Int {
    val safeIndex = index.coerceIn(0, length)
    return indexOf('\n', safeIndex).takeIf { it >= 0 } ?: length
}

private fun openHtmlTagsBefore(source: String, rawCursor: Int): List<String> {
    val cursor = rawCursor.coerceIn(0, source.length)
    val stack = mutableListOf<String>()
    Regex("""<!--[\s\S]*?-->|<[^>]+>""").findAll(source.substring(0, cursor)).forEach { match ->
        val token = match.value
        if (token.startsWith("<!--")) return@forEach
        val tag = Regex("""^<\s*/?\s*([A-Za-z][A-Za-z0-9-]*)""")
            .find(token)
            ?.groupValues
            ?.getOrNull(1)
            ?.lowercase()
            ?: return@forEach
        val closing = token.startsWith("</")
        val selfClosing = token.endsWith("/>") || tag in htmlVoidTags
        if (closing) {
            val index = stack.indexOfLast { it == tag }
            if (index >= 0) stack.subList(index, stack.size).clear()
        } else if (!selfClosing) {
            stack += tag
        }
    }
    return stack
}

private fun opensHtmlBlock(line: String): Boolean {
    val match = Regex("""<([A-Za-z][A-Za-z0-9-]*)\b[^>]*>$""").find(line.trim()) ?: return false
    val tag = match.groupValues[1].lowercase()
    return !line.trim().startsWith("</") && !line.trim().endsWith("/>") && tag !in htmlVoidTags
}

private fun isWebCompletionPosition(source: String, rawCursor: Int, fileName: String): Boolean {
    val cursor = rawCursor.coerceIn(0, source.length)
    val language = webLanguageAt(fileName, source, cursor)
    if (language == WebEditorLanguage.Html) {
        val before = source.substring(0, cursor)
        val commentStart = before.lastIndexOf("<!--")
        val commentEnd = before.lastIndexOf("-->")
        return commentStart <= commentEnd
    }
    if (language == WebEditorLanguage.Json) {
        return true
    }
    var quote: Char? = null
    var lineComment = false
    var blockComment = false
    var index = 0
    while (index < cursor) {
        val char = source[index]
        val next = source.getOrNull(index + 1)
        when {
            lineComment -> {
                if (char == '\n') lineComment = false
                index += 1
            }
            blockComment -> {
                if (char == '*' && next == '/') {
                    blockComment = false
                    index += 2
                } else index += 1
            }
            quote != null -> {
                index += if (char == '\\') 2 else 1
                if (char == quote && source.getOrNull(index - 2) != '\\') quote = null
            }
            language == WebEditorLanguage.JavaScript && char == '/' && next == '/' -> {
                lineComment = true
                index += 2
            }
            char == '/' && next == '*' -> {
                blockComment = true
                index += 2
            }
            char == '"' || char == '\'' || (language == WebEditorLanguage.JavaScript && char == '`') -> {
                quote = char
                index += 1
            }
            else -> index += 1
        }
    }
    return quote == null && !lineComment && !blockComment
}

private fun isIdentifierPart(char: Char): Boolean {
    return char.isLetterOrDigit() || char == '_' || char == '$' || char == '-'
}

private val htmlVoidTags = setOf(
    "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr",
)

private val knownCssProperties = setOf(
    "align-content", "align-items", "align-self", "animation", "animation-delay", "animation-duration",
    "animation-fill-mode", "animation-name", "animation-timing-function", "appearance", "aspect-ratio",
    "backdrop-filter", "background", "background-attachment", "background-blend-mode", "background-clip",
    "background-color", "background-image", "background-position", "background-repeat", "background-size",
    "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius", "border-bottom-right-radius",
    "border-bottom-style", "border-bottom-width", "border-color", "border-image", "border-left", "border-left-color",
    "border-left-style", "border-left-width", "border-radius", "border-right", "border-right-color",
    "border-right-style", "border-right-width", "border-style", "border-top", "border-top-color",
    "border-top-left-radius", "border-top-right-radius", "border-top-style", "border-top-width", "border-width",
    "bottom", "box-shadow", "box-sizing", "clip-path", "color", "column-gap",
    "container", "container-name", "container-type", "content", "cursor", "display", "filter", "flex", "flex-basis",
    "flex-direction", "flex-grow", "flex-shrink", "flex-wrap",
    "font", "font-family", "font-feature-settings", "font-size", "font-style", "font-variant", "font-weight",
    "gap", "grid", "grid-area", "grid-column", "grid-row",
    "grid-template-columns", "grid-template-rows", "height", "inset", "justify-content", "left", "letter-spacing",
    "line-height", "margin", "margin-bottom", "margin-left", "margin-right", "margin-top", "max-height",
    "max-width", "min-height", "min-width", "mix-blend-mode", "object-fit", "object-position", "opacity",
    "order", "outline", "outline-color", "outline-offset",
    "outline-style", "outline-width", "overflow", "overflow-x", "overflow-y",
    "padding", "padding-bottom", "padding-left", "padding-right", "padding-top", "place-items", "pointer-events",
    "position", "right", "row-gap", "scroll-behavior", "scroll-snap-align", "scroll-snap-type", "text-align",
    "text-decoration", "text-overflow", "text-shadow", "text-transform", "top", "touch-action",
    "transform", "transform-origin", "transform-style", "transition", "transition-delay", "transition-duration", "transition-property",
    "transition-timing-function", "translate", "user-select", "visibility", "white-space", "width", "z-index",
)

private val jsCommonIdentifierTypos = mapOf(
    "docuemnt" to "document",
    "docment" to "document",
    "consol" to "console",
    "cosnole" to "console",
    "querrySelector" to "querySelector",
    "queryselector" to "querySelector",
    "addEventlistener" to "addEventListener",
    "localstorage" to "localStorage",
)

private fun looksLikeJavaScriptStatement(trimmed: String): Boolean {
    if (trimmed.isBlank()) return false
    if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) return false
    if (trimmed.startsWith("if ") || trimmed.startsWith("if(")) return false
    if (trimmed.startsWith("for ") || trimmed.startsWith("for(")) return false
    if (trimmed.startsWith("while ") || trimmed.startsWith("while(")) return false
    if (trimmed.startsWith("switch ") || trimmed.startsWith("switch(")) return false
    if (trimmed.startsWith("function ")) return false
    if (trimmed.startsWith("else") || trimmed.startsWith("catch") || trimmed.startsWith("finally")) return false
    return Regex("""^(const|let|var|return|throw|await|break|continue)\b""").containsMatchIn(trimmed) ||
            Regex("""^[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*|\[[^\]]+])*\s*(?:=|\+=|-=|\*=|/=|\+\+|--|\()""").containsMatchIn(trimmed)
}

private fun hasJavaScriptStatementTerminator(trimmed: String): Boolean {
    return trimmed.endsWith(";") ||
            trimmed.endsWith("{") ||
            trimmed.endsWith("}") ||
            trimmed.endsWith(",") ||
            trimmed.endsWith(":") ||
            trimmed.endsWith("=>") ||
            trimmed.endsWith(")")
}

private val webCompletionSyntaxes = mapOf(
    "main|element" to "<main>...</main>",
    "section|element" to "<section>...</section>",
    "button|element" to "<button type=\"button\">...</button>",
    "img|media" to "<img src=\"path\" alt=\"description\">",
    "class|attribute" to "class=\"name\"",
    "id|attribute" to "id=\"unique-name\"",
    "querySelector|dom" to "document.querySelector(selector)",
    "querySelectorAll|dom" to "document.querySelectorAll(selector)",
    "getElementById|dom" to "document.getElementById(id)",
    "createElement|dom" to "document.createElement(tagName)",
    "addEventListener|dom" to "element.addEventListener(type, listener)",
    "fetch|function" to "fetch(url, options?)",
    "console.log|function" to "console.log(value, ...more)",
    "setTimeout|function" to "setTimeout(callback, delay)",
    "map|array" to "array.map((item, index) => value)",
    "filter|array" to "array.filter((item, index) => condition)",
    "forEach|array" to "array.forEach((item, index) => { ... })",
    "includes|string" to "text.includes(search)",
    "trim|string" to "text.trim()",
    "replace|string" to "text.replace(search, replacement)",
    "then|promise" to "promise.then(value => nextValue)",
    "catch|promise" to "promise.catch(error => handle(error))",
    "display|css property" to "display: flex;",
    "position|css property" to "position: relative;",
    "gap|css property" to "gap: 1rem;",
    "@media|at-rule" to "@media (max-width: 700px) {\n    ...\n}",
)

private fun webIdePalette(theme: WebTheme): WebPalette = when (theme) {
    WebTheme.CyberpunkElectricDark -> darkWebPalette(
        bgTop = Color(0xFF070617),
        bgBottom = Color(0xFF1A1033),
        panel = Color(0xFF0A0C24),
        accent = Color(0xFFFF2D75),
        secondary = Color(0xFF00E5FF),
        warningColor = Color(0xFFFF3EF5),
    )
    WebTheme.CyberpunkElectricLight -> lightWebPalette(
        bgTop = Color(0xFFFFFBF8),
        bgBottom = Color(0xFFFFE7F0),
        panel = Color.White,
        elevatedPanel = Color(0xFFFFF7FB),
        border = Color(0xFFFFC4DC),
        accent = Color(0xFFFF2D75),
        secondary = Color(0xFF00BBFF),
    )
    WebTheme.ForestNightDark -> darkWebPalette(
        bgTop = Color(0xFF031611),
        bgBottom = Color(0xFF0E1812),
        panel = Color(0xFF042018),
        accent = Color(0xFF22C55E),
        secondary = Color(0xFFA3E635),
        warningColor = Color(0xFFA3E635),
    )
    WebTheme.ForestNightLight -> lightWebPalette(
        bgTop = Color(0xFFF7FFF9),
        bgBottom = Color(0xFFECFDF5),
        panel = Color(0xFFFFFFFF),
        elevatedPanel = Color(0xFFF2FFF7),
        border = Color(0xFFD1FAE5),
        accent = Color(0xFF22C55E),
        secondary = Color(0xFF16A34A),
    )
    WebTheme.SunsetDuskDark -> darkWebPalette(
        bgTop = Color(0xFF1E0F0B),
        bgBottom = Color(0xFF27120E),
        panel = Color(0xFF180F18),
        accent = Color(0xFFFB923C),
        secondary = Color(0xFFFDBF24),
        warningColor = Color(0xFFFDBF24),
    )
    WebTheme.SunsetDuskLight -> lightWebPalette(
        bgTop = Color(0xFFFFFDF9),
        bgBottom = Color(0xFFFFF0D0),
        panel = Color.White,
        elevatedPanel = Color(0xFFFFF7ED),
        border = Color(0xFFFED7AA),
        accent = Color(0xFFFB923C),
        secondary = Color(0xFFF97316),
    )
    WebTheme.ArcticAuroraDark -> darkWebPalette(
        bgTop = Color(0xFF031A24),
        bgBottom = Color(0xFF0A1A1D),
        panel = Color(0xFF052536),
        accent = Color(0xFF06B6D4),
        secondary = Color(0xFF60A5FA),
        warningColor = Color(0xFF22D3EE),
    )
    WebTheme.ArcticAuroraLight -> lightWebPalette(
        bgTop = Color(0xFFF7FEFF),
        bgBottom = Color(0xFFF0FDFF),
        panel = Color.White,
        elevatedPanel = Color(0xFFEFFFFD),
        border = Color(0xFFBAE6FD),
        accent = Color(0xFF06B6D4),
        secondary = Color(0xFF60A5FA),
    )
    WebTheme.VioletHazeDark -> darkWebPalette(
        bgTop = Color(0xFF1A123A),
        bgBottom = Color(0xFF120C2A),
        panel = Color(0xFF201540),
        accent = Color(0xFFA78BFA),
        run = Color(0xFF7C3AED),
        secondary = Color(0xFFF472B6),
        warningColor = Color(0xFFC4B5FD),
    )
    WebTheme.VioletHazeLight -> lightWebPalette(
        bgTop = Color(0xFFFAF5FF),
        bgBottom = Color(0xFFEDE9FE),
        panel = Color.White,
        elevatedPanel = Color(0xFFF8F5FF),
        border = Color(0xFFDDD6FE),
        accent = Color(0xFFA78BFA),
        secondary = Color(0xFFF472B6),
    )
    WebTheme.MonochromeProDark -> darkWebPalette(
        bgTop = Color(0xFF111827),
        bgBottom = Color(0xFF0B1118),
        panel = Color(0xFF111827),
        accent = Color(0xFF64748B),
        run = Color(0xFF334155),
        secondary = Color(0xFFA1A1AA),
        warningColor = Color(0xFFE5E7EB),
    )
    WebTheme.MonochromeProLight -> lightWebPalette(
        bgTop = Color(0xFFFFFFFF),
        bgBottom = Color(0xFFF3F4F6),
        panel = Color.White,
        elevatedPanel = Color(0xFFF8FAFC),
        border = Color(0xFFE5E7EB),
        accent = Color(0xFF64748B),
        run = Color(0xFF64748B),
        secondary = Color(0xFF374151),
    )
    WebTheme.VSCodeDefaultDark -> darkWebPalette(
        bgTop = Color(0xFF1E1E1E),
        bgBottom = Color(0xFF181818),
        panel = Color(0xFF252526),
        accent = Color(0xFF007ACC),
        run = Color(0xFF007ACC),
        secondary = Color(0xFF3794FF),
        warningColor = Color(0xFFDCDCAA),
        terminalPrompt = Color(0xFF4EC9B0),
        border = Color(0xFF3C3C3C),
    )
    WebTheme.VSCodeDefaultLight -> lightWebPalette(
        bgTop = Color(0xFFFFFFFF),
        bgBottom = Color(0xFFF3F3F3),
        panel = Color.White,
        elevatedPanel = Color(0xFFF8F8F8),
        border = Color(0xFFE5E5E5),
        accent = Color(0xFF007ACC),
        run = Color(0xFF007ACC),
        secondary = Color(0xFF0451A5),
        warningColor = Color(0xFF795E26),
        terminalPrompt = Color(0xFF008000),
    )
}

private fun darkWebPalette(
    bgTop: Color,
    bgBottom: Color,
    panel: Color,
    accent: Color,
    run: Color = accent,
    secondary: Color = accent,
    warningColor: Color = Color(0xFFFFC46B),
    terminalPrompt: Color = accent,
    border: Color = accent.copy(alpha = 0.38f),
) = WebPalette(
    backgroundTop = bgTop,
    backgroundBottom = bgBottom,
    panel = panel,
    elevatedPanel = panel.copy(alpha = 0.82f),
    tab = Color.White.copy(alpha = 0.06f),
    selectedTab = accent.copy(alpha = 0.24f),
    text = Color.White,
    mutedText = Color(0xFFE6ECF4),
    subtleText = Color(0xFFB6C0CE),
    border = border,
    strongBorder = accent.copy(alpha = 0.8f),
    accent = accent,
    run = run,
    runText = Color.White,
    terminalPrompt = terminalPrompt,
    divider = border.copy(alpha = 0.5f),
    error = Color(0xFFFF7B7B),
    warning = warningColor,
    success = secondary,
    info = secondary,
)

private fun lightWebPalette(
    bgTop: Color,
    bgBottom: Color,
    panel: Color,
    elevatedPanel: Color,
    accent: Color,
    run: Color = accent,
    secondary: Color = accent,
    warningColor: Color = Color(0xFF9A6700),
    terminalPrompt: Color = accent,
    border: Color = accent.copy(alpha = 0.24f),
) = WebPalette(
    backgroundTop = bgTop,
    backgroundBottom = bgBottom,
    panel = panel,
    elevatedPanel = elevatedPanel,
    tab = accent.copy(alpha = 0.07f),
    selectedTab = accent.copy(alpha = 0.13f),
    text = Color(0xFF111827),
    mutedText = Color(0xFF4B5563),
    subtleText = Color(0xFF6B7280),
    border = border,
    strongBorder = accent.copy(alpha = 0.8f),
    accent = accent,
    run = run,
    runText = Color.White,
    terminalPrompt = terminalPrompt,
    divider = border.copy(alpha = 0.65f),
    error = Color(0xFFB42318),
    warning = warningColor,
    success = secondary,
    info = secondary,
)

private fun executeWebShellCommand(command: String, root: File, working: File): WebShellResult {
    val args = splitShellCommandLine(command)
    val executable = args.firstOrNull()?.lowercase().orEmpty()
    return when (executable) {
        "" -> WebShellResult()
        "clear", "cls" -> WebShellResult(clearTerminal = true)
        "help" -> WebShellResult(
            output = """
                PocketCoded Web terminal

                Files: ls, tree, pwd, cd <dir>, cat <file>, touch <file>, mkdir <dir>, rm <path>, mv <from> <to>, cp <from> <to>
                Web: preview, run, serve, lint, validate, format <file>
                Libraries: libs, libraries, lib <name>
                Versions: web --version, html --version, css --version, js --version, git --version
                Utilities: echo <text>, clear, cls, help
            """.trimIndent()
        )
        "libs", "libraries" -> WebShellResult(output = webLibraryTerminalHelp())
        "lib", "library", "cdn" -> WebShellResult(output = webLibrarySnippet(args.getOrNull(1)))
        "version" -> WebShellResult(output = "PocketCoded Web 1.0\nHTML, CSS and JavaScript preview engine")
        "web", "html", "css", "js", "javascript" -> {
            if (args.any { it == "--version" || it == "-v" }) {
                val label = when (executable) {
                    "html" -> "HTML5"
                    "css" -> "CSS3"
                    "js", "javascript" -> "JavaScript ES2023"
                    else -> "PocketCoded Web"
                }
                WebShellResult(output = "$label (PocketCoded Web built-in preview engine)")
            } else {
                WebShellResult(error = "$executable: use $executable --version, run, preview, lint, or format <file>")
            }
        }
        "git" -> {
            if (args.getOrNull(1) == "--version" || args.getOrNull(1) == "version") {
                WebShellResult(output = "git version 2.45.0 (PocketCoded Web built-in JGit compatible)")
            } else {
                WebShellResult(error = "git: this Web terminal currently supports git --version only.")
            }
        }
        "pwd" -> WebShellResult(output = terminalDirectoryLabel(root, working))
        "ls", "dir" -> WebShellResult(output = listDirectory(working, showHidden = args.any { it.contains("a") && it.startsWith("-") }))
        "tree" -> WebShellResult(output = treeDirectory(root, working))
        "cd" -> {
            val target = resolveTerminalPath(root, working, args.getOrNull(1) ?: root.absolutePath)
                ?: return WebShellResult(error = "cd: outside project")
            if (!target.isDirectory) WebShellResult(error = "cd: ${args.getOrNull(1).orEmpty()}: no such directory")
            else WebShellResult(workingDirectoryPath = target.absolutePath)
        }
        "cat", "type" -> {
            val target = resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                ?: return WebShellResult(error = "cat: outside project")
            if (!target.isFile) WebShellResult(error = "cat: ${target.name}: no such file")
            else WebShellResult(output = target.readText().take(80_000))
        }
        "echo" -> WebShellResult(output = args.drop(1).joinToString(" "))
        "touch" -> {
            val target = resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                ?: return WebShellResult(error = "touch: outside project")
            target.parentFile?.mkdirs()
            if (!target.exists()) target.writeText("")
            target.setLastModified(System.currentTimeMillis())
            WebShellResult(output = "Created ${relativeWorkspacePath(root, target)}", workspaceChanged = true)
        }
        "mkdir" -> {
            val target = resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                ?: return WebShellResult(error = "mkdir: outside project")
            if (target.mkdirs() || target.isDirectory) WebShellResult(output = "Created ${relativeWorkspacePath(root, target)}/", workspaceChanged = true)
            else WebShellResult(error = "mkdir: could not create ${target.name}")
        }
        "rm", "del" -> {
            val target = resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                ?: return WebShellResult(error = "rm: outside project")
            if (samePath(target.absolutePath, root.absolutePath)) return WebShellResult(error = "rm: refusing to remove project root")
            val removed = if (target.isDirectory) target.deleteRecursively() else target.delete()
            if (removed) WebShellResult(output = "Removed ${relativeWorkspacePath(root, target)}", workspaceChanged = true)
            else WebShellResult(error = "rm: could not remove ${target.name}")
        }
        "mv", "move", "ren", "rename" -> {
            val source = resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                ?: return WebShellResult(error = "mv: source outside project")
            val target = resolveTerminalPath(root, working, args.getOrNull(2).orEmpty())
                ?: return WebShellResult(error = "mv: destination outside project")
            if (!source.exists()) return WebShellResult(error = "mv: ${source.name}: no such file")
            target.parentFile?.mkdirs()
            if (source.renameTo(target)) WebShellResult(output = "Moved ${source.name} -> ${relativeWorkspacePath(root, target)}", workspaceChanged = true)
            else WebShellResult(error = "mv: could not move ${source.name}")
        }
        "cp", "copy" -> {
            val source = resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                ?: return WebShellResult(error = "cp: source outside project")
            val target = resolveTerminalPath(root, working, args.getOrNull(2).orEmpty())
                ?: return WebShellResult(error = "cp: destination outside project")
            if (!source.isFile) return WebShellResult(error = "cp: ${source.name}: no such file")
            target.parentFile?.mkdirs()
            source.copyTo(target, overwrite = true)
            WebShellResult(output = "Copied ${source.name} -> ${relativeWorkspacePath(root, target)}", workspaceChanged = true)
        }
        "preview", "run", "open", "serve" -> WebShellResult(output = "Opening responsive Web preview.", openPreview = true)
        "lint", "check", "validate" -> WebShellResult(output = auditWebProject(root))
        "format" -> {
            val target = resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                ?: return WebShellResult(error = "format: outside project")
            if (!target.isFile || !isEditableWebFile(target)) return WebShellResult(error = "format: expected an editable Web file")
            val formatted = formatWebSource(target.name, target.readText())
            target.writeText(formatted)
            WebShellResult(output = "Formatted ${relativeWorkspacePath(root, target)}", workspaceChanged = true)
        }
        "node", "js" -> {
            val target = resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                ?: return WebShellResult(error = "$executable: outside project")
            if (!target.isFile || fileExtension(target.name) !in javascriptFileExtensions) {
                return WebShellResult(error = "$executable: ${args.getOrNull(1).orEmpty()}: expected a JavaScript file")
            }
            WebShellResult(output = "", runJs = target.readText())
        }
        "npm" -> WebShellResult(
            error = "npm is not bundled in PocketCoded Web. Use node/js <file> to run scripts."
        )
        else -> WebShellResult(error = "Command not found: $executable. Run help to see available commands.")
    }
}

private fun appendShellResult(result: WebShellResult): String {
    return buildString {
        if (result.output.isNotBlank()) append(result.output.trimEnd()).append('\n')
        if (result.error.isNotBlank()) append("Error: ").append(result.error.trimEnd()).append('\n')
    }.trimEnd()
}

private fun webLibraryTerminalHelp(): String {
    val names = webLibrarySnippets().keys.sorted().joinToString(", ")
    val packageNames = commonLibraryPackageNames.sorted().joinToString(", ")
    return """
        Offline library support

        Local libraries:
        - Put files in assets/, libs/, vendor/, or node_modules/ and reference them with normal relative paths.
        - Examples: <script src="libs/p5.min.js"></script>, <link rel="stylesheet" href="vendor/bootstrap.css">

        ES modules:
        - Bare imports in module scripts are mapped only when a local package/file exists.
        - Examples: import * as THREE from "three"; import axios from "axios";
        - Add packages under node_modules/ or local ESM files under libs/, vendor/, or assets/.
        - The live preview blocks internet requests so it keeps working offline.

        Snippets:
        - lib <name>
        - Available snippets: $names

        Recognized package names:
        $packageNames
    """.trimIndent()
}

private fun webLibrarySnippet(rawName: String?): String {
    val snippets = webLibrarySnippets()
    val name = rawName?.lowercase()?.trim().orEmpty()
    if (name.isBlank()) {
        return "Usage: lib <name>\nAvailable: ${snippets.keys.sorted().joinToString(", ")}"
    }
    return snippets[name]
        ?: "No snippet named '$name'. Available: ${snippets.keys.sorted().joinToString(", ")}"
}

private fun webLibrarySnippets(): Map<String, String> {
    return mapOf(
        "react" to """
            <div id="root"></div>
            <script type="importmap">
            { "imports": { "react": "./libs/react.js", "react-dom/client": "./libs/react-dom-client.js" } }
            </script>
            <script type="module">
                import React from "react";
                import { createRoot } from "react-dom/client";

                createRoot(document.getElementById("root")).render(
                    React.createElement("h1", null, "Hello React")
                );
            </script>
        """.trimIndent(),
        "vue" to """
            <div id="app">{{ message }}</div>
            <script type="importmap">
            { "imports": { "vue": "./libs/vue.esm-browser.js" } }
            </script>
            <script type="module">
                import { createApp } from "vue";

                createApp({
                    data: () => ({ message: "Hello Vue" })
                }).mount("#app");
            </script>
        """.trimIndent(),
        "three" to """
            <canvas id="scene"></canvas>
            <script type="importmap">
            { "imports": { "three": "./libs/three.module.js" } }
            </script>
            <script type="module">
                import * as THREE from "three";

                const renderer = new THREE.WebGLRenderer({ canvas: document.querySelector("#scene") });
                renderer.setSize(innerWidth, innerHeight);
            </script>
        """.trimIndent(),
        "bootstrap" to """
            <link href="vendor/bootstrap.min.css" rel="stylesheet">
            <script src="vendor/bootstrap.bundle.min.js"></script>
        """.trimIndent(),
        "tailwind" to """
            <link rel="stylesheet" href="vendor/tailwind.css">
        """.trimIndent(),
        "gsap" to """
            <script type="importmap">
            { "imports": { "gsap": "./libs/gsap.js" } }
            </script>
            <script type="module">
                import gsap from "gsap";
                gsap.to(".box", { x: 120, duration: 0.8 });
            </script>
        """.trimIndent(),
        "axios" to """
            <script type="importmap">
            { "imports": { "axios": "./libs/axios.js" } }
            </script>
            <script type="module">
                import axios from "axios";
                const response = await axios.get("./data.json");
                console.log(response.data);
            </script>
        """.trimIndent(),
        "chart" to """
            <canvas id="chart"></canvas>
            <script type="importmap">
            { "imports": { "chart.js/auto": "./libs/chart.js" } }
            </script>
            <script type="module">
                import Chart from "chart.js/auto";
                new Chart(document.getElementById("chart"), {
                    type: "bar",
                    data: { labels: ["A", "B"], datasets: [{ data: [1, 2] }] }
                });
            </script>
        """.trimIndent(),
        "jquery" to """
            <script src="libs/jquery.min.js"></script>
        """.trimIndent(),
        "p5" to """
            <script src="libs/p5.min.js"></script>
            <script>
                function setup() {
                    createCanvas(400, 400);
                }

                function draw() {
                    background(20);
                    circle(mouseX, mouseY, 40);
                }
            </script>
        """.trimIndent(),
        "phaser" to """
            <script src="libs/phaser.min.js"></script>
            <script>
                const game = new Phaser.Game({
                    type: Phaser.AUTO,
                    width: 800,
                    height: 600,
                    scene: { create() { this.add.text(20, 20, "Hello Phaser"); } }
                });
            </script>
        """.trimIndent(),
    )
}

private fun appendTerminalOutput(existing: String, addition: String): String {
    if (addition.isBlank()) return trimTerminalBuffer(existing)
    val separator = if (existing.isBlank()) "" else "\n"
    return trimTerminalBuffer(
        buildString(existing.length + separator.length + addition.length) {
            append(existing)
            append(separator)
            append(addition.trimEnd())
        }
    )
}

private fun terminalDisplayText(
    text: String,
    promptText: String,
    keepLastPrompt: Boolean,
): String {
    if (text.isBlank()) return text
    if (!keepLastPrompt) return text

    var lineStart = 0
    var lastPromptStart = -1
    var lastPromptEnd = -1
    var lastPromptWasEmptyPrompt = false
    while (lineStart <= text.length) {
        val newline = text.indexOf('\n', lineStart)
        val lineEnd = if (newline >= 0) newline else text.length
        val line = text.substring(lineStart, lineEnd)
        if (line == promptText || line.startsWith("$promptText ")) {
            lastPromptStart = lineStart
            lastPromptEnd = lineEnd
            lastPromptWasEmptyPrompt = line.trimEnd() == promptText
        }
        if (newline < 0) break
        lineStart = newline + 1
    }
    if (!lastPromptWasEmptyPrompt || lastPromptStart < 0) return text

    val removalEnd = if (lastPromptEnd < text.length && text[lastPromptEnd] == '\n') lastPromptEnd + 1 else lastPromptEnd
    return buildString(text.length - (removalEnd - lastPromptStart)) {
        append(text, 0, lastPromptStart)
        append(text, removalEnd, text.length)
    }
}

internal fun estimatedLongestLineLength(text: String, cap: Int): Int {
    if (text.isEmpty()) return 0
    var longest = 0
    var current = 0
    text.forEach { char ->
        if (char == '\n') {
            if (current > longest) longest = current
            current = 0
        } else {
            current += 1
            if (current >= cap) return cap
        }
    }
    return maxOf(longest, current)
}

private fun editorTextMetrics(text: String, cap: Int): EditorTextMetrics {
    if (text.isEmpty()) return EditorTextMetrics()
    var lineCount = 1
    var longest = 0
    var current = 0
    text.forEach { char ->
        if (char == '\n') {
            lineCount += 1
            if (current > longest) longest = current
            current = 0
        } else {
            current += 1
            if (current > cap) current = cap
        }
    }
    return EditorTextMetrics(
        lineCount = lineCount,
        longestLineLength = maxOf(longest, current).coerceAtLeast(48),
    )
}

private fun listDirectory(directory: File, showHidden: Boolean): String {
    val children = directory.listFiles()
        .orEmpty()
        .filter { showHidden || !it.name.startsWith(".") }
        .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
    if (children.isEmpty()) return "Empty"
    return children.joinToString("\n") { if (it.isDirectory) "${it.name}/" else it.name }
}

private fun treeDirectory(root: File, start: File): String {
    val builder = StringBuilder()
    builder.append(terminalDirectoryLabel(root, start)).append('\n')
    fun append(directory: File, depth: Int) {
        directory.listFiles()
            .orEmpty()
            .filter { !it.name.startsWith(".") && !isProjectIgnoredDirectory(it) }
            .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
            .take(80)
            .forEach { child ->
                builder.append("  ".repeat(depth)).append(if (child.isDirectory) "${child.name}/" else child.name).append('\n')
                if (child.isDirectory && depth < 4) append(child, depth + 1)
            }
    }
    append(start, 1)
    return builder.toString().trimEnd()
}

private fun auditWebProject(root: File): String {
    return auditWebFiles(root, loadWebFiles(root), includeOkMessage = true)
}

private fun previewDiagnosticsForTerminal(root: File, files: List<WebFile>): String {
    val diagnostics = auditWebFiles(root, files, includeOkMessage = false)
    return diagnostics
        .takeIf { it.isNotBlank() }
        ?.prependIndent("[preview] ")
        .orEmpty()
}

private fun auditWebFiles(root: File, files: List<WebFile>, includeOkMessage: Boolean): String {
    val messages = mutableListOf<String>()
    val hasIndex = files.any { samePath(File(it.filePath).absolutePath, File(root, "index.html").absolutePath) }
    if (!hasIndex) messages += "warning: index.html is missing. Preview will use the active file or a generated shell."
    files.forEach { file ->
        val text = file.content
        val relativeFile = relativeWorkspacePath(root, File(file.filePath))
        inspectWebSource(file.name, text).forEach { diagnostic ->
            messages += "${diagnostic.severity}: $relativeFile:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
        }
        when (fileExtension(file.name)) {
            "html", "htm" -> {
                if (!text.contains("<meta name=\"viewport\"", ignoreCase = true)) {
                    messages += "warning: $relativeFile: add a viewport meta tag for mobile layouts."
                }
                Regex("""(?:href|src)=["']([^"']+)["']""").findAll(text).forEach { match ->
                    val ref = match.groupValues[1]
                    if (isRemotePreviewReference(ref)) {
                        messages += "warning: $relativeFile: external preview dependency is blocked offline: $ref"
                    } else if (isLocalPreviewReference(ref)) {
                        val target = resolvePreviewFile(root, File(file.filePath), ref)
                        if (target == null) {
                            messages += "error: $relativeFile: missing linked file $ref"
                        }
                    }
                }
            }
            "css", "js", "mjs", "cjs", "jsx" -> {
                val balance = braceBalance(text)
                if (balance != 0) messages += "warning: $relativeFile: brace balance is $balance"
                if (fileExtension(file.name) == "css") {
                    Regex("""url\(\s*(['"]?)(.*?)\1\s*\)""").findAll(text).forEach { match ->
                        val ref = match.groupValues[2].trim()
                        if (isRemotePreviewReference(ref)) {
                            messages += "warning: $relativeFile: external CSS asset is blocked offline: $ref"
                        } else if (isLocalPreviewReference(ref) && resolvePreviewFile(root, File(file.filePath), ref) == null) {
                            messages += "error: $relativeFile: missing CSS asset $ref"
                        }
                    }
                }
                if (fileExtension(file.name) in javascriptFileExtensions) {
                    jsImportSpecifierRegex.findAll(text).forEach { match ->
                        val specifier = match.groupValues.getOrNull(1)
                            ?.ifBlank { match.groupValues.getOrNull(2).orEmpty() }
                            ?.trim()
                            .orEmpty()
                        when {
                            isRemotePreviewReference(specifier) -> {
                                messages += "warning: $relativeFile: external JavaScript import is blocked offline: $specifier"
                            }
                            isBareImportSpecifier(specifier) && localEsmUrlForSpecifier(root, specifier) == null -> {
                                messages += "warning: $relativeFile: package '$specifier' is not available offline. Add it under node_modules/, libs/, vendor/, or assets/."
                            }
                        }
                    }
                }
            }
        }
    }
    if (messages.isEmpty() && includeOkMessage) return "OK\nNo obvious Web project issues found."
    return messages.joinToString("\n")
}

private fun braceBalance(text: String): Int {
    var balance = 0
    text.forEach { char ->
        if (char == '{') balance += 1
        if (char == '}') balance -= 1
    }
    return balance
}

private fun formatWebSource(name: String, source: String): String {
    val normalized = source.lines().joinToString("\n") { it.trimEnd() }.trimEnd() + "\n"
    return when (fileExtension(name)) {
        "json" -> runCatching { JSONObject(normalized).toString(2) + "\n" }
            .recoverCatching { JSONArray(normalized).toString(2) + "\n" }
            .getOrDefault(normalized)
        else -> normalized
    }
}

private fun isBareImportSpecifier(specifier: String): Boolean {
    val value = specifier.trim()
    return value.isNotBlank() &&
            !value.startsWith("./") &&
            !value.startsWith("../") &&
            !value.startsWith("/") &&
            !value.startsWith("http://") &&
            !value.startsWith("https://") &&
            !value.startsWith("data:") &&
            !value.startsWith("blob:") &&
            !value.contains(":")
}

private fun localEsmUrlForSpecifier(rootDirectory: File, specifier: String): String? {
    return localNodeModuleEntry(rootDirectory, specifier)
        ?: localVendorLibraryEntry(rootDirectory, specifier)
}

private fun localNodeModuleEntry(rootDirectory: File, specifier: String): String? {
    val cleanSpecifier = specifier.trim().removePrefix("/")
    if (cleanSpecifier.isBlank()) return null
    val parts = cleanSpecifier.split('/').filter { it.isNotBlank() }
    if (parts.isEmpty()) return null
    val packageName = if (parts.first().startsWith("@") && parts.size >= 2) {
        "${parts[0]}/${parts[1]}"
    } else {
        parts[0]
    }
    val packagePartCount = packageName.count { it == '/' } + 1
    val subPath = parts.drop(packagePartCount).joinToString("/")
    val packageDirectory = File(rootDirectory, "node_modules/${packageName.replace('/', File.separatorChar)}")
    if (!packageDirectory.isDirectory || !isSameOrInside(rootDirectory, packageDirectory)) return null

    if (subPath.isNotBlank()) {
        javaScriptModuleCandidates(File(packageDirectory, subPath)).firstOrNull { it.isFile }?.let { candidate ->
            return previewLocalUrl(rootDirectory, candidate)
        }
    }

    val packageJson = File(packageDirectory, "package.json")
    if (packageJson.isFile) {
        val json = runCatching { JSONObject(packageJson.readText()) }.getOrNull()
        listOf("module", "browser", "jsnext:main", "main")
            .mapNotNull { field ->
                val value = json?.opt(field)
                when (value) {
                    is String -> value
                    is JSONObject -> value.optString("module").ifBlank { value.optString("default") }
                    else -> null
                }
            }
            .flatMap { entry -> javaScriptModuleCandidates(File(packageDirectory, entry)) }
            .firstOrNull { it.isFile }
            ?.let { candidate -> return previewLocalUrl(rootDirectory, candidate) }
    }

    listOf(
        "dist/$packageName.esm.js",
        "dist/$packageName.module.js",
        "dist/${packageName.substringAfterLast('/')}.esm.js",
        "dist/${packageName.substringAfterLast('/')}.module.js",
        "dist/index.esm.js",
        "dist/index.module.js",
        "index.mjs",
        "index.js",
    )
        .flatMap { entry -> javaScriptModuleCandidates(File(packageDirectory, entry)) }
        .firstOrNull { it.isFile }
        ?.let { candidate -> return previewLocalUrl(rootDirectory, candidate) }

    return null
}

private fun localVendorLibraryEntry(rootDirectory: File, specifier: String): String? {
    val cleanSpecifier = specifier.trim().removePrefix("/")
    val libraryName = cleanSpecifier
        .substringAfterLast('/')
        .removePrefix("@")
        .replace(Regex("""[^A-Za-z0-9_.-]"""), "-")
        .ifBlank { return null }
    val directoryNames = listOf("libs", "lib", "vendor", "assets")
    val fileNames = listOf(
        "$libraryName.module.js",
        "$libraryName.esm.js",
        "$libraryName.mjs",
        "$libraryName.min.js",
        "$libraryName.js",
        "index.mjs",
        "index.js",
    )
    directoryNames.forEach { directoryName ->
        val directory = File(rootDirectory, directoryName)
        if (!directory.isDirectory) return@forEach
        val directMatches = fileNames.map { File(directory, it) }
        val nestedMatches = fileNames.map { File(File(directory, libraryName), it) }
        (directMatches + nestedMatches)
            .flatMap(::javaScriptModuleCandidates)
            .firstOrNull { it.isFile && isSameOrInside(rootDirectory, it) }
            ?.let { candidate -> return previewLocalUrl(rootDirectory, candidate) }
    }
    return null
}

private fun javaScriptModuleCandidates(candidate: File): List<File> {
    val hasExtension = candidate.extension.isNotBlank()
    val baseCandidates = if (hasExtension) {
        listOf(candidate)
    } else {
        listOf(
            candidate,
            File("${candidate.path}.mjs"),
            File("${candidate.path}.js"),
            File("${candidate.path}.min.js"),
        )
    }
    return baseCandidates + listOf(
        File(candidate, "index.mjs"),
        File(candidate, "index.js"),
    )
}

private fun previewLocalUrl(rootDirectory: File, file: File): String? {
    val target = runCatching { file.canonicalFile }.getOrNull() ?: return null
    if (!target.isFile || !isSameOrInside(rootDirectory, target)) return null
    return Uri.fromFile(target).toString()
}

private fun resolvePreviewFile(
    rootDirectory: File,
    sourceOwner: File,
    reference: String,
): File? {
    return previewReferenceCandidates(rootDirectory, sourceOwner, reference)
        .firstOrNull { candidate ->
            isSameOrInside(rootDirectory, candidate) && candidate.isFile
        }
}

private fun previewReferenceCandidates(
    rootDirectory: File,
    sourceOwner: File,
    reference: String,
): List<File> {
    if (!isLocalPreviewReference(reference)) return emptyList()
    val cleanReference = reference
        .substringBefore('#')
        .substringBefore('?')
        .trim()
        .replace('\\', '/')
        .removePrefix("./")
        .trimStart('/')
    if (cleanReference.isBlank()) return emptyList()
    val ownerDirectory = if (sourceOwner.isDirectory) sourceOwner else sourceOwner.parentFile ?: rootDirectory
    return listOf(
        File(ownerDirectory, cleanReference),
        File(rootDirectory, cleanReference),
    ).mapNotNull { candidate ->
        runCatching { candidate.canonicalFile }.getOrNull()
    }
}

private fun rootIndexWebFile(rootDirectory: File, files: List<WebFile>): WebFile? {
    return files.firstOrNull {
        it.name.lowercase() in setOf("index.html", "index.htm") &&
                samePath(it.parentDirectoryPath(rootDirectory), rootDirectory.absolutePath)
    }
}

private fun nearestIndexWebFile(rootDirectory: File, files: List<WebFile>, fromFile: File): WebFile? {
    val root = runCatching { rootDirectory.canonicalFile }.getOrDefault(rootDirectory)
    val startDirectory = runCatching {
        (if (fromFile.isDirectory) fromFile else fromFile.parentFile ?: rootDirectory).canonicalFile
    }.getOrDefault(rootDirectory)
    var directory: File? = startDirectory
    while (directory != null && isSameOrInside(root, directory)) {
        val currentDirectory = directory
        files.firstOrNull { file ->
            file.name.lowercase() in setOf("index.html", "index.htm") &&
                    samePath(file.parentDirectoryPath(rootDirectory), currentDirectory.absolutePath)
        }?.let { return it }
        if (samePath(currentDirectory.absolutePath, root.absolutePath)) break
        directory = currentDirectory.parentFile
    }
    return rootIndexWebFile(rootDirectory, files)
}

private fun isLocalPreviewReference(reference: String): Boolean {
    val value = reference.trim()
    if (value.isBlank() || value.startsWith("#")) return false
    val lower = value.lowercase()
    return !(
            lower.startsWith("http://") ||
                    lower.startsWith("https://") ||
                    lower.startsWith("data:") ||
                    lower.startsWith("blob:") ||
                    lower.startsWith("mailto:") ||
                    lower.startsWith("tel:") ||
                    lower.startsWith("//")
            )
}

private fun isRemotePreviewReference(reference: String): Boolean {
    val value = reference.trim().lowercase()
    return value.startsWith("http://") || value.startsWith("https://") || value.startsWith("//")
}

private fun escapeHtmlAttribute(value: String): String {
    return escapeHtml(value).replace("\"", "&quot;")
}

private fun formatPreviewRuntimeMessage(
    level: String,
    message: String,
    source: String,
    line: Int,
    column: Int,
): String {
    val normalizedLevel = when (level.lowercase()) {
        "error" -> "error"
        "warning", "warn" -> "warning"
        "debug" -> "debug"
        "tip" -> "info"
        else -> level.lowercase().ifBlank { "log" }
    }
    val sourceLabel = previewSourceLabel(source)
    val location = buildString {
        if (sourceLabel.isNotBlank()) append(sourceLabel)
        if (line > 0) {
            if (isNotEmpty()) append(':')
            append(line)
            if (column > 0) append(':').append(column)
        }
    }
    return buildString {
        append("[preview:")
        append(normalizedLevel)
        append("] ")
        if (location.isNotBlank()) append(location).append(" - ")
        append(message.ifBlank { "Runtime message" })
    }
}

private fun previewSourceLabel(source: String): String {
    val clean = source
        .substringBefore('?')
        .substringBefore('#')
        .trim()
    if (clean.isBlank() || clean == "about:blank") return ""
    return clean
        .substringAfterLast('/')
        .ifBlank { clean }
}

private fun buildAiCopilotPrompt(
    rootDirectory: File,
    files: List<WebFile>,
    activeFile: WebFile,
    userPrompt: String,
    terminalText: String,
): String {
    val contextFiles = files
        .sortedBy { if (it.id == activeFile.id) 0 else 1 }
        .take(AI_CONTEXT_FILE_LIMIT)
    val fileContext = StringBuilder()
    var remaining = AI_CONTEXT_CHAR_LIMIT
    contextFiles.forEach { file ->
        if (remaining <= 0) return@forEach
        val relative = relativeWorkspacePath(rootDirectory, File(file.filePath))
        val content = file.content.take(remaining)
        fileContext.append("\n--- FILE: ").append(relative).append(" ---\n")
        fileContext.append(content).append('\n')
        remaining -= content.length
    }
    val terminalContext = terminalText.takeLast(AI_TERMINAL_CONTEXT_LIMIT)
    val projectTree = files
        .sortedBy { relativeWorkspacePath(rootDirectory, File(it.filePath)) }
        .joinToString("\n") { "- ${relativeWorkspacePath(rootDirectory, File(it.filePath))}" }
    return """
        You are the built-in copilot for PocketCoded Web, an offline-first Android IDE for HTML, CSS, and JavaScript.
        You can read the project files included below. You can propose real file edits; the app will apply them after the user approves.
        Do not say you cannot access or edit files when the needed file content is included in this prompt.
        Prefer concise Copilot-style responses: short summary first, then changed files or commands.
        If the user asks you to create, edit, replace, delete, or fix files, include machine-readable changes before the explanation.
        For large file edits, prefer plain file blocks over JSON so the response is not cut off or filled with escaped newlines.

        Preferred file edit format:
        <file path="script.js">
        ...complete file content...
        </file>

        Supported compact action JSON:
        {"action":"run_terminal","command":"lint"}
        {"action":"replace_active_file","content":"...full file content..."}
        {"action":"apply_files","files":[{"path":"index.html","operation":"write","content":"..."},{"path":"old.js","operation":"delete"}]}

        Active file: ${relativeWorkspacePath(rootDirectory, File(activeFile.filePath))}

        Project file tree:
        $projectTree

        Project files:
        $fileContext

        Recent terminal:
        $terminalContext

        User request:
        $userPrompt
    """.trimIndent()
}

private fun callGeminiDirectly(modelId: String, prompt: String, apiKey: String): String {
    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey")
    val payload = JSONObject()
        .put(
            "contents",
            JSONArray().put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            )
        )
        .put(
            "generationConfig",
            JSONObject()
                .put("temperature", 0.25)
                .put("maxOutputTokens", AI_MAX_OUTPUT_TOKENS)
        )
        .toString()
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 90_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
    }
    connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
    val status = connection.responseCode
    val responseText = (if (status in 200..299) connection.inputStream else connection.errorStream)
        ?.bufferedReader()
        ?.use { it.readText() }
        .orEmpty()
    if (status !in 200..299) {
        val message = runCatching {
            JSONObject(responseText).optJSONObject("error")?.optString("message")
        }.getOrNull().orEmpty().ifBlank { responseText }
        error(message.ifBlank { "Gemini request failed with HTTP $status." })
    }
    val json = JSONObject(responseText)
    val candidates = json.optJSONArray("candidates") ?: JSONArray()
    val text = buildString {
        for (i in 0 until candidates.length()) {
            val parts = candidates.optJSONObject(i)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?: continue
            for (partIndex in 0 until parts.length()) {
                append(parts.optJSONObject(partIndex)?.optString("text").orEmpty())
            }
        }
    }.trim()
    return text.ifBlank { "I could not generate a response." }
}

private fun friendlyGeminiError(message: String): String {
    val lower = message.lowercase()
    return when {
        lower.contains("api key") -> "The Gemini API key was rejected. Check the key and try again."
        lower.contains("quota") || lower.contains("rate") -> "Gemini quota is exhausted for this key. Try again later or use another key."
        lower.contains("model") && lower.contains("not found") -> "That Gemini model is not available for this key."
        lower.contains("network") || lower.contains("timeout") || lower.contains("failed to connect") -> "AI request could not connect. Check the device internet connection."
        else -> message.ifBlank { "AI request failed. Try again in a moment." }
    }
}

private fun suggestedActionFromAiText(text: String, activeFile: WebFile? = null): AiSuggestedAction? {
    jsonObjectTexts(text).forEach { rawJson ->
        val json = runCatching { JSONObject(rawJson) }.getOrNull() ?: return@forEach
        suggestedActionFromJson(json)?.let { return it }
    }
    Regex("(?is)<terminal>(.*?)</terminal>").find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }?.let {
        return AiSuggestedAction.RunTerminalCommand(it)
    }
    taggedFileChangesFromAiText(text).takeIf { it.isNotEmpty() }?.let {
        return AiSuggestedAction.ApplyProjectFileChanges(it)
    }
    markdownFileChangesFromAiText(text).takeIf { it.isNotEmpty() }?.let {
        return AiSuggestedAction.ApplyProjectFileChanges(it)
    }
    singleCodeBlockReplacement(text, activeFile)?.let {
        return it
    }
    return null
}

private fun suggestedActionFromJson(json: JSONObject): AiSuggestedAction? {
    val action = json.optString("action").ifBlank { json.optString("type") }.lowercase()
    val files = json.optJSONArray("files") ?: json.optJSONArray("changes")
    return when {
        action in setOf("run_terminal", "terminal", "command", "run_command") -> {
            val command = json.optString("command").ifBlank { json.optString("terminal") }
            command.takeIf { it.isNotBlank() }?.let { AiSuggestedAction.RunTerminalCommand(it) }
        }
        action in setOf("replace_active_file", "replace", "replace_file") -> {
            val content = json.optString("content").ifBlank { json.optString("code") }
            content.takeIf { it.isNotBlank() }?.let { AiSuggestedAction.ReplaceActiveFile(it) }
        }
        action in setOf("apply_files", "files", "edit_files", "write_files") || files != null -> {
            val changes = aiFileChangesFromJsonArray(files ?: JSONArray())
            changes.takeIf { it.isNotEmpty() }?.let { AiSuggestedAction.ApplyProjectFileChanges(it) }
        }
        else -> null
    }
}

private fun aiFileChangesFromJsonArray(files: JSONArray): List<AiProjectFileChange> {
    val changes = mutableListOf<AiProjectFileChange>()
    for (i in 0 until files.length()) {
        val item = files.optJSONObject(i) ?: continue
        val path = item.optString("path").ifBlank { item.optString("file") }.trim()
        if (path.isBlank()) continue
        val operation = when (item.optString("operation").ifBlank { item.optString("action") }.lowercase()) {
            "delete", "remove" -> AiProjectFileOperation.Delete
            else -> AiProjectFileOperation.Write
        }
        changes += AiProjectFileChange(
            operation = operation,
            path = path,
            content = item.optString("content").ifBlank { item.optString("code") },
        )
    }
    return changes
}

private fun taggedFileChangesFromAiText(text: String): List<AiProjectFileChange> {
    return Regex("""(?is)<file\s+[^>]*path\s*=\s*(["'])(.*?)\1[^>]*>(.*?)</file>""")
        .findAll(text)
        .mapNotNull { match ->
            val path = match.groupValues.getOrNull(2)?.trim().orEmpty()
            val content = match.groupValues.getOrNull(3)?.trim('\n', '\r').orEmpty()
            path.takeIf { it.isNotBlank() }?.let {
                AiProjectFileChange(AiProjectFileOperation.Write, it, content)
            }
        }
        .distinctBy { it.path }
        .toList()
}

private fun markdownFileChangesFromAiText(text: String): List<AiProjectFileChange> {
    return markdownCodeBlocksFromAiText(text)
        .mapNotNull { block ->
            val path = editablePathFromAiText(block.language)
                ?: editablePathFromAiText(block.prefix)
                ?: return@mapNotNull null
            AiProjectFileChange(
                operation = AiProjectFileOperation.Write,
                path = path,
                content = block.code.trim('\n', '\r'),
            )
        }
        .distinctBy { it.path }
}

private fun singleCodeBlockReplacement(text: String, activeFile: WebFile?): AiSuggestedAction? {
    activeFile ?: return null
    val blocks = markdownCodeBlocksFromAiText(text)
    if (blocks.size != 1) return null
    val lower = text.lowercase()
    val looksLikeReplacement = listOf(
        "replace active file",
        "updated active file",
        "full file",
        "replace the file",
        "here is the updated",
    ).any { lower.contains(it) }
    return if (looksLikeReplacement) {
        AiSuggestedAction.ReplaceActiveFile(blocks.first().code.trim('\n', '\r'))
    } else {
        null
    }
}

private fun markdownCodeBlocksFromAiText(text: String): List<AiCodeBlock> {
    return Regex("""(?is)```([A-Za-z0-9_+#./-]*)\s*\n(.*?)```""")
        .findAll(text)
        .map { match ->
            val prefixStart = (match.range.first - 240).coerceAtLeast(0)
            AiCodeBlock(
                prefix = text.substring(prefixStart, match.range.first),
                language = match.groupValues.getOrNull(1).orEmpty(),
                code = match.groupValues.getOrNull(2).orEmpty(),
            )
        }
        .toList()
}

private fun editablePathFromAiText(text: String): String? {
    return aiEditableFilePathRegex
        .find(text.replace('\\', '/'))
        ?.groupValues
        ?.getOrNull(1)
        ?.trim()
}

private fun aiVisibleResponseText(text: String): String {
    var cleaned = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        .replace(text) { match ->
            val body = match.groupValues.getOrNull(1).orEmpty().trim()
            if (body.startsWith("{") && isAiActionJsonText(body)) "" else match.value
        }
    cleaned = removeUnclosedAiActionFence(cleaned)
    cleaned = Regex("""(?is)<file\s+[^>]*path\s*=\s*(["'])(.*?)\1[^>]*>(.*?)</file>""")
        .replace(cleaned, "")
    cleaned = Regex("""(?is)<file\s+[^>]*path\s*=\s*(["'])(.*?)\1[^>]*>.*$""")
        .replace(cleaned, "")
    cleaned = Regex("""(?is)```([A-Za-z0-9_+#./-]*)\s*\n(.*?)```""")
        .replace(cleaned) { match ->
            val prefixStart = (match.range.first - 240).coerceAtLeast(0)
            val prefix = cleaned.substring(prefixStart, match.range.first)
            val hasFilePath = editablePathFromAiText(match.groupValues.getOrNull(1).orEmpty()) != null ||
                    editablePathFromAiText(prefix) != null
            if (hasFilePath) "" else match.value
        }
    val firstJson = cleaned.indexOf('{')
    val lastJson = cleaned.lastIndexOf('}')
    if (firstJson >= 0 && lastJson > firstJson) {
        val candidate = cleaned.substring(firstJson, lastJson + 1).trim()
        if (isAiActionJsonText(candidate)) {
            cleaned = cleaned.removeRange(firstJson, lastJson + 1)
        }
    }
    val possiblePartialJson = cleaned.indexOf('{')
    if (possiblePartialJson >= 0 && looksLikeAiActionPayload(cleaned.substring(possiblePartialJson))) {
        cleaned = cleaned.substring(0, possiblePartialJson)
    }
    return cleaned
        .lines()
        .joinToString("\n") { it.trimEnd() }
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

private fun removeUnclosedAiActionFence(text: String): String {
    val fenceStart = text.lastIndexOf("```")
    if (fenceStart < 0) return text
    val afterFence = text.substring(fenceStart + 3)
    if ("```" in afterFence) return text
    return if (looksLikeAiActionPayload(afterFence)) text.substring(0, fenceStart) else text
}

private fun looksLikeAiActionPayload(text: String): Boolean {
    val lower = text.lowercase()
    return listOf(
        "\"action\"",
        "\"apply_files\"",
        "\"replace_active_file\"",
        "\"files\"",
        "<file ",
    ).any { lower.contains(it) }
}

private fun isAiActionJsonText(text: String): Boolean {
    return runCatching {
        suggestedActionFromJson(JSONObject(text)) != null
    }.getOrDefault(false)
}

private fun jsonObjectTexts(text: String): List<String> {
    val blocks = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        .findAll(text)
        .map { it.groupValues[1].trim() }
        .filter { it.startsWith("{") }
        .toList()
    if (blocks.isNotEmpty()) return blocks
    val first = text.indexOf('{')
    val last = text.lastIndexOf('}')
    return if (first >= 0 && last > first) listOf(text.substring(first, last + 1)) else emptyList()
}

private fun applyAiProjectFileChanges(rootDirectory: File, changes: List<AiProjectFileChange>): String {
    var writes = 0
    var deletes = 0
    changes.forEach { change ->
        val target = File(rootDirectory, change.path).canonicalFile
        if (!isSameOrInside(rootDirectory, target)) error("AI change tried to leave the project: ${change.path}")
        if (target.isDirectory) error("AI Chat can only edit files, not folders: ${change.path}")
        when (change.operation) {
            AiProjectFileOperation.Write -> {
                target.parentFile?.mkdirs()
                target.writeText(change.content)
                writes += 1
            }
            AiProjectFileOperation.Delete -> {
                if (target.exists() && target.delete()) deletes += 1
            }
        }
    }
    return buildString {
        append("Applied AI changes.")
        if (writes > 0) append(" Wrote ").append(writes).append(" file").append(if (writes == 1) "." else "s.")
        if (deletes > 0) append(" Deleted ").append(deletes).append(" file").append(if (deletes == 1) "." else "s.")
    }
}

private fun loadWebFiles(directory: File): List<WebFile> {
    ensureDefaultWebProject(directory)
    return directory.walkTopDown()
        .onEnter { file -> file == directory || !isProjectIgnoredDirectory(file) }
        .filter { it.isFile && isEditableWebFile(it) && it.length() <= MAX_EDITABLE_PROJECT_FILE_BYTES }
        .mapNotNull { file -> file.toWebFileOrNull(directory) }
        .toList()
        .ifEmpty { listOf(createDefaultWebFile(directory)) }
}

private fun ensureDefaultWebProject(directory: File) {
    directory.mkdirs()
    if (directory.walkTopDown().any { it.isFile && isEditableWebFile(it) }) return
    File(directory, "index.html").writeText(defaultIndexHtml(directory.name))
    File(directory, "style.css").writeText(defaultCss())
    File(directory, "script.js").writeText(defaultJs())
}

private fun createDefaultWebFile(directory: File): WebFile {
    ensureDefaultWebProject(directory)
    val file = File(directory, "index.html")
    return file.toWebFile(directory)
}

private fun File.toWebFile(rootDirectory: File): WebFile {
    return WebFile(
        id = canonicalPath.hashCode().toLong(),
        name = name,
        content = readText(),
        filePath = absolutePath,
        modifiedAt = lastModified(),
    )
}

private fun File.toWebFileOrNull(rootDirectory: File): WebFile? {
    if (!isSameOrInside(rootDirectory, this)) return null
    return runCatching { toWebFile(rootDirectory) }.getOrNull()
}

private fun saveFileContent(path: String, content: String) {
    runCatching { File(path).writeText(content) }
}

private fun defaultContentForFile(name: String, projectName: String): String = when (fileExtension(name)) {
    in htmlFileExtensions -> defaultIndexHtml(projectName)
    in cssFileExtensions -> defaultCss()
    in javascriptFileExtensions -> defaultJs()
    "json" -> "{\n  \"name\": \"${projectName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')}\"\n}\n"
    "svg" -> "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\">\n    <rect width=\"100\" height=\"100\" fill=\"#426bde\"/>\n</svg>\n"
    else -> ""
}

private fun defaultIndexHtml(projectName: String): String = """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>$projectName</title>
        <link rel="stylesheet" href="style.css">
    </head>
    <body>
        <main class="app">
            <h1>$projectName</h1>
            <p>Edit HTML, CSS, and JavaScript offline in PocketCoded Web.</p>
            <button id="actionButton" type="button">Run JavaScript</button>
            <p id="output"></p>
        </main>
        <script src="script.js"></script>
    </body>
    </html>
""".trimIndent() + "\n"

private fun defaultCss(): String = """
    :root {
        color-scheme: light dark;
        font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    }

    body {
        margin: 0;
        min-height: 100vh;
        display: grid;
        place-items: center;
        background: #f6f9ff;
        color: #151a24;
    }

    .app {
        width: min(92vw, 680px);
        padding: 24px;
    }

    button {
        border: 0;
        border-radius: 8px;
        padding: 12px 16px;
        background: #426bde;
        color: white;
        font-weight: 700;
    }
""".trimIndent() + "\n"

private fun defaultJs(): String = """
    const button = document.querySelector('#actionButton');
    const output = document.querySelector('#output');

    button?.addEventListener('click', () => {
        output.textContent = 'JavaScript is running in the PocketCoded Web preview.';
    });
""".trimIndent() + "\n"

private fun buildExplorerEntries(
    rootDirectory: File,
    files: List<WebFile>,
    expandedFolderPaths: List<String>,
    search: String,
): List<ExplorerEntry> {
    val normalizedSearch = search.trim()
    val entries = mutableListOf<ExplorerEntry>()
    val expanded = expandedFolderPaths.mapTo(mutableSetOf()) { runCatching { File(it).canonicalPath }.getOrDefault(it) }

    fun fileMatches(file: WebFile): Boolean {
        if (normalizedSearch.isBlank()) return true
        val localFile = File(file.filePath)
        return file.name.contains(normalizedSearch, ignoreCase = true) ||
                relativeWorkspacePath(rootDirectory, localFile).contains(normalizedSearch, ignoreCase = true)
    }

    fun directoryMatches(directory: File): Boolean {
        if (normalizedSearch.isBlank()) return true
        return directory.name.contains(normalizedSearch, ignoreCase = true) ||
                files.any { file ->
                    val localFile = File(file.filePath)
                    isSameOrInside(directory, localFile) && fileMatches(file)
                }
    }

    fun appendDirectory(directory: File, depth: Int) {
        directory.listFiles { file -> file.isDirectory && !isProjectIgnoredDirectory(file) }
            .orEmpty()
            .filter(::directoryMatches)
            .sortedBy { it.name.lowercase() }
            .forEach { childDirectory ->
                val target = childDirectory.toFolderTarget(rootDirectory)
                val isExpanded = normalizedSearch.isNotBlank() || expanded.any { samePath(it, childDirectory.absolutePath) }
                entries += ExplorerEntry.Directory(target, depth, isExpanded)
                if (isExpanded) appendDirectory(childDirectory, depth + 1)
            }
        files
            .filter { file -> File(file.filePath).parentFile?.let { samePath(it.absolutePath, directory.absolutePath) } == true }
            .filter(::fileMatches)
            .sortedBy { it.name.lowercase() }
            .forEach { entries += ExplorerEntry.SourceFile(it, depth) }
    }

    appendDirectory(rootDirectory, 0)
    return entries
}

private fun fileIcon(name: String) = when (fileExtension(name)) {
    in htmlFileExtensions -> Icons.Rounded.Html
    in cssFileExtensions -> Icons.Rounded.Css
    in javascriptFileExtensions -> Icons.Rounded.Javascript
    else -> Icons.Rounded.Description
}

private fun fileColor(name: String, palette: WebPalette): Color = when (fileExtension(name)) {
    in htmlFileExtensions -> Color(0xFFE86F3A)
    in cssFileExtensions -> Color(0xFF3A8DFF)
    in javascriptFileExtensions -> Color(0xFFF7D84A)
    "json" -> palette.warning
    "svg" -> Color(0xFFFF9A35)
    else -> palette.accent
}

private fun fileLanguageLabel(name: String): String = when (fileExtension(name)) {
    in htmlFileExtensions -> "Code Editor - HTML"
    in cssFileExtensions -> "Code Editor - CSS"
    in javascriptFileExtensions -> "Code Editor - JavaScript"
    "json" -> "Code Editor - JSON"
    "svg" -> "Code Editor - SVG"
    else -> "Code Editor - Text"
}

private fun codeEditorTitle(name: String, compact: Boolean): String {
    val language = when (fileExtension(name)) {
        in htmlFileExtensions -> "HTML"
        in cssFileExtensions -> "CSS"
        in javascriptFileExtensions -> "JavaScript"
        "json" -> "JSON"
        "svg" -> "SVG"
        else -> "Text"
    }
    return if (compact) "Code\nEditor -\n$language" else "Code Editor - $language"
}

private fun normalizeWebFileName(rawName: String): String? {
    val trimmed = rawName.trim().replace('\\', '/').substringAfterLast('/')
    if (trimmed.isBlank() || trimmed.startsWith(".")) return null
    if (trimmed.any { it in "\\/:*?\"<>|" }) return null
    val withExtension = if (trimmed.substringAfterLast('.', "").isBlank()) "$trimmed.html" else trimmed
    val extension = fileExtension(withExtension)
    if (extension !in editableWebExtensions) return null
    val simpleName = Regex("[A-Za-z0-9][A-Za-z0-9_. -]*\\.[A-Za-z0-9]+")
    return withExtension.takeIf { simpleName.matches(it) }
}

private fun normalizeFolderName(rawName: String): String? {
    val trimmed = rawName.trim().replace('\\', '/').substringAfterLast('/')
    if (trimmed.isBlank() || trimmed.startsWith(".")) return null
    if (trimmed.any { it in "\\/:*?\"<>|" }) return null
    val simpleName = Regex("[A-Za-z0-9][A-Za-z0-9_. -]*")
    return trimmed.takeIf { simpleName.matches(it) }
}

private fun nextUntitledFileName(files: List<WebFile>): String {
    var index = 1
    while (true) {
        val candidate = "page$index.html"
        if (files.none { it.name.equals(candidate, ignoreCase = true) }) return candidate
        index += 1
    }
}

private fun isEditableWebFile(file: File): Boolean {
    return file.extension.lowercase() in editableWebExtensions
}

private fun looksLikeHtmlSource(source: String): Boolean =
    source.contains("</") ||
            source.contains("<html", ignoreCase = true) ||
            source.contains("<!doctype", ignoreCase = true) ||
            Regex("<[A-Za-z][\\w:-]*(\\s|>|/)").containsMatchIn(source)

private fun isProjectIgnoredDirectory(file: File): Boolean {
    return file.name in workspaceMetadataNames || file.name.equals("node_modules", ignoreCase = true)
}

private fun resolveTerminalPath(rootDirectory: File, workingDirectory: File, rawPath: String): File? {
    if (rawPath.isBlank()) return workingDirectory
    val candidate = if (rawPath.startsWith("/") || rawPath.matches(Regex("^[A-Za-z]:.*"))) {
        File(rawPath)
    } else if (rawPath == "~") {
        rootDirectory
    } else if (rawPath.startsWith("~/")) {
        File(rootDirectory, rawPath.removePrefix("~/"))
    } else {
        File(workingDirectory, rawPath)
    }
    val canonical = runCatching { candidate.canonicalFile }.getOrNull() ?: return null
    return canonical.takeIf { isSameOrInside(rootDirectory, it) }
}

private fun validDirectoryOrRoot(rootDirectory: File, candidatePath: String): File {
    val candidate = File(candidatePath)
    return runCatching { candidate.canonicalFile }
        .getOrDefault(rootDirectory)
        .takeIf { it.isDirectory && isSameOrInside(rootDirectory, it) }
        ?: rootDirectory
}

private fun terminalPrompt(rootDirectory: File, directory: File): String {
    val label = terminalDirectoryLabel(rootDirectory, directory)
    return if (label == "~") "$" else "$label $"
}

private fun terminalDirectoryLabel(rootDirectory: File, directory: File): String {
    val relative = runCatching {
        directory.canonicalFile.relativeTo(rootDirectory.canonicalFile).path.replace(File.separatorChar, '/')
    }.getOrDefault("")
    return if (relative.isBlank() || relative == ".") "~" else "~/$relative"
}

private fun relativeDirectoryLabel(rootDirectory: File, directory: File): String {
    val relative = runCatching {
        directory.canonicalFile.relativeTo(rootDirectory.canonicalFile).path.replace(File.separatorChar, '/')
    }.getOrDefault("")
    return if (relative.isBlank() || relative == ".") "" else relative
}

private fun relativeWorkspacePath(rootDirectory: File, file: File): String {
    return runCatching {
        file.canonicalFile.relativeTo(rootDirectory.canonicalFile).path.replace(File.separatorChar, '/')
    }.getOrDefault(file.name)
}

private fun File.toFolderTarget(rootDirectory: File): FolderTarget {
    val relative = relativeWorkspacePath(rootDirectory, this).takeUnless { it == "." }.orEmpty()
    return FolderTarget(
        name = if (relative.isBlank()) rootDirectory.name else name,
        path = absolutePath,
        relativePath = relative,
    )
}

private fun WebFile.isDirectChildOf(directory: File): Boolean {
    return runCatching {
        File(filePath).canonicalFile.parentFile?.canonicalPath == directory.canonicalPath
    }.getOrDefault(false)
}

private fun WebFile.parentDirectoryPath(root: File): String {
    return File(filePath).parentFile?.absolutePath ?: root.absolutePath
}

private fun isSameOrInside(parent: File, child: File): Boolean {
    return runCatching {
        val parentPath = parent.canonicalFile.path
        val childPath = child.canonicalFile.path
        childPath == parentPath || childPath.startsWith(parentPath + File.separator)
    }.getOrDefault(false)
}

private fun samePath(first: String, second: String): Boolean {
    return runCatching { File(first).canonicalPath == File(second).canonicalPath }.getOrDefault(first == second)
}

private fun normalizedWorkspaceDirectoryPaths(rootDirectory: File, paths: List<String>): List<String> {
    val normalized = mutableListOf<String>()
    paths.forEach { path ->
        val directory = File(path)
        if (directory.isDirectory && isSameOrInside(rootDirectory, directory)) {
            normalized.addUniquePath(directory.absolutePath)
        }
    }
    return normalized
}

private fun MutableList<String>.addUniquePath(path: String) {
    if (none { samePath(it, path) }) add(path)
}

private fun SnapshotStateList<String>.addUniquePath(path: String) {
    if (none { samePath(it, path) }) add(path)
}

private fun WebFile.withPendingEditorText(pendingTexts: Map<Long, String>): WebFile {
    val pending = pendingTexts[id]
    return if (pending != null) copy(content = pending) else this
}

private fun List<WebFile>.withPendingEditorTexts(pendingTexts: Map<Long, String>): List<WebFile> {
    return map { it.withPendingEditorText(pendingTexts) }
}

private fun SnapshotStateList<WebFile>.replaceFile(id: Long, transform: WebFile.() -> WebFile) {
    val index = indexOfFirst { it.id == id }
    if (index >= 0) this[index] = this[index].transform()
}

private fun fileExtension(name: String): String = name.substringAfterLast('.', "").lowercase()

private fun escapeHtml(value: String): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

private fun loadWebIdeTheme(context: Context): WebTheme {
    val raw = context.getSharedPreferences(IDE_PREFS_NAME, Context.MODE_PRIVATE)
        .getString(PREF_IDE_THEME, WebTheme.CyberpunkElectricDark.name)
    return parseWebTheme(raw)
}

private fun parseWebTheme(raw: String?): WebTheme {
    return runCatching { WebTheme.valueOf(raw ?: WebTheme.CyberpunkElectricDark.name) }.getOrElse {
        when (raw) {
            "Cyberpunk", "MidnightNeon" -> WebTheme.CyberpunkElectricDark
            "ForestDark", "SoftMint" -> WebTheme.ForestNightDark
            "RosePine", "Solarized" -> WebTheme.SunsetDuskLight
            "OceanicNext", "SereneBlue" -> WebTheme.ArcticAuroraDark
            "Amethyst", "Cloudy" -> WebTheme.VioletHazeDark
            "Nordic", "Paper" -> WebTheme.MonochromeProLight
            else -> WebTheme.CyberpunkElectricDark
        }
    }
}

private fun saveWebIdeTheme(context: Context, theme: WebTheme) {
    context.getSharedPreferences(IDE_PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(PREF_IDE_THEME, theme.name)
    }
}

private fun loadWebIdeSession(context: Context, rootDirectory: File): WebSession {
    val preferences = context.getSharedPreferences("${IDE_PREFS_NAME}_${rootDirectory.absolutePath.hashCode()}", Context.MODE_PRIVATE)
    return WebSession(
        activeFileId = preferences.getLong(PREF_ACTIVE_FILE_ID, -1L),
        activeFilePath = preferences.getString(PREF_ACTIVE_FILE_PATH, null),
        workingDirectoryPath = preferences.getString(PREF_WORKING_DIRECTORY_PATH, null),
        terminalDirectoryPath = preferences.getString(PREF_TERMINAL_DIRECTORY_PATH, null),
        expandedFolderPaths = parseStringList(preferences.getString(PREF_EXPANDED_FOLDER_PATHS, "[]").orEmpty()),
        explorerVisible = preferences.getBoolean(PREF_EXPLORER_VISIBLE, true),
        fileSearch = preferences.getString(PREF_FILE_SEARCH, "").orEmpty(),
        terminalInput = preferences.getString(PREF_TERMINAL_INPUT, "").orEmpty(),
        terminalText = preferences.getString(PREF_TERMINAL_TEXT, "").orEmpty(),
        fullScreenMode = preferences.getString(PREF_FULL_SCREEN_MODE, null)?.let { raw ->
            runCatching { WebFullScreenMode.valueOf(raw) }.getOrNull()
        },
        userAiApiKey = preferences.getString(PREF_USER_AI_API_KEY, null)?.takeIf { it.isNotBlank() },
    )
}

private fun saveWebIdeSession(context: Context, rootDirectory: File, session: WebSession) {
    context.getSharedPreferences("${IDE_PREFS_NAME}_${rootDirectory.absolutePath.hashCode()}", Context.MODE_PRIVATE).edit {
        putLong(PREF_ACTIVE_FILE_ID, session.activeFileId)
        session.activeFilePath?.let { putString(PREF_ACTIVE_FILE_PATH, it) } ?: remove(PREF_ACTIVE_FILE_PATH)
        session.workingDirectoryPath?.let { putString(PREF_WORKING_DIRECTORY_PATH, it) } ?: remove(PREF_WORKING_DIRECTORY_PATH)
        session.terminalDirectoryPath?.let { putString(PREF_TERMINAL_DIRECTORY_PATH, it) } ?: remove(PREF_TERMINAL_DIRECTORY_PATH)
        putString(PREF_EXPANDED_FOLDER_PATHS, jsonStringList(session.expandedFolderPaths))
        putBoolean(PREF_EXPLORER_VISIBLE, session.explorerVisible)
        putString(PREF_FILE_SEARCH, session.fileSearch)
        putString(PREF_TERMINAL_INPUT, session.terminalInput)
        remove(PREF_TERMINAL_TEXT)
        remove(PREF_FULL_SCREEN_MODE)
        session.userAiApiKey?.let { putString(PREF_USER_AI_API_KEY, it) } ?: remove(PREF_USER_AI_API_KEY)
    }
}

private fun jsonStringList(values: List<String>): String {
    val array = JSONArray()
    values.forEach { array.put(it) }
    return array.toString()
}

private fun parseStringList(raw: String): List<String> {
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }.getOrDefault(emptyList())
}

@Composable
private fun FilesAndFoldersDialog(
    palette: WebPalette,
    onDismiss: () -> Unit,
    onOpenFile: () -> Unit,
    onOpenFolder: () -> Unit,
    onSaveCurrentFolder: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.elevatedPanel,
        title = { Text("Files and folders", color = palette.text) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Destination: -", color = palette.subtleText, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))

                FileActionButton(
                    text = "Open file",
                    icon = Icons.Rounded.Description,
                    palette = palette,
                    onClick = onOpenFile
                )
                Spacer(Modifier.height(8.dp))
                FileActionButton(
                    text = "Open folder",
                    icon = Icons.Rounded.Folder,
                    palette = palette,
                    onClick = onOpenFolder
                )

                Spacer(Modifier.height(16.dp))
                HorizontalRule(palette)
                Spacer(Modifier.height(16.dp))

                Text("Save from sandbox", color = palette.subtleText, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))

                FileActionButton(
                    text = "Save current folder",
                    icon = Icons.Rounded.Folder,
                    palette = palette,
                    onClick = onSaveCurrentFolder
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = palette.accent) }
        }
    )
}

@Composable
private fun FileActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    palette: WebPalette,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.text),
        border = BorderStroke(1.dp, palette.border)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = palette.accent)
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 15.sp)
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
    }
    return name ?: uri.path?.substringAfterLast('/')
}

private fun copyUriToFile(context: Context, uri: Uri, target: File): Boolean {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        true
    }.getOrDefault(false)
}

private fun importFromDocumentFile(context: Context, docFile: DocumentFile, targetDir: File) {
    if (docFile.isDirectory) {
        val newDir = File(targetDir, docFile.name ?: "imported_folder").apply { mkdirs() }
        docFile.listFiles().forEach { child ->
            importFromDocumentFile(context, child, newDir)
        }
    } else {
        val targetFile = File(targetDir, docFile.name ?: "imported_file")
        context.contentResolver.openInputStream(docFile.uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

private fun exportToDocumentFile(context: Context, sourceDir: File, destFolder: DocumentFile) {
    sourceDir.listFiles()?.forEach { file ->
        if (isProjectIgnoredDirectory(file)) return@forEach
        if (file.isDirectory) {
            val subFolder = destFolder.createDirectory(file.name ?: "folder") ?: return@forEach
            exportToDocumentFile(context, file, subFolder)
        } else {
            val destFile = destFolder.createFile("application/octet-stream", file.name ?: "file") ?: return@forEach
            context.contentResolver.openOutputStream(destFile.uri)?.use { output ->
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
    }
}
