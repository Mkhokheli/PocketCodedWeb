package com.mkhokheli.pocketcodedweb

import com.mkhokheli.pocketcodedweb.ui.theme.spaceThemeColors
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ContentCopy
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.compose.ui.input.key.*
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material.icons.rounded.Css
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Html
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.mkhokheli.pocketcodedweb.ui.theme.PocketCodedWebTheme
import com.mkhokheli.pocketcodedweb.ui.theme.readableTextColor
import com.mkhokheli.pocketcodedweb.ui.theme.withReadableText
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.LinkedHashMap
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.GZIPInputStream
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.testTag

private const val IDE_PREFS_NAME = "web_ide"

internal fun minimalScrollTarget(
        current: Int,
        viewportSize: Int,
        contentMax: Int,
        cursorStart: Float,
        cursorEnd: Float,
        leadingInset: Float,
        trailingInset: Float,
): Int {
    if (viewportSize <= 0) return current.coerceIn(0, contentMax)
    val safeStart = current + leadingInset
    val safeEnd = current + viewportSize - trailingInset
    return when {
                cursorStart < safeStart -> cursorStart - leadingInset
                cursorEnd > safeEnd -> cursorEnd + trailingInset - viewportSize
                else -> current.toFloat()
            }
            .toInt()
            .coerceIn(0, contentMax)
}

private const val PREF_ACTIVE_FILE_ID = "active_file_id"
private const val PREF_ACTIVE_FILE_PATH = "active_file_path"
private const val PREF_WORKING_DIRECTORY_PATH = "working_directory_path"
private const val PREF_TERMINAL_DIRECTORY_PATH = "terminal_directory_path"
private const val PREF_EXPANDED_FOLDER_PATHS = "expanded_folder_paths"
private const val PREF_EXPLORER_VISIBLE = "explorer_visible"
private const val PREF_FILE_SEARCH = "file_search"
private const val PREF_TERMINAL_INPUT = "terminal_input"
private const val PREF_TERMINAL_TEXT = "terminal_text"
private const val PREF_TERMINAL_TEXT_SAVED_AT = "terminal_text_saved_at"
private const val PREF_FULL_SCREEN_MODE = "full_screen_mode"
private const val PREF_IDE_THEME = "ide_theme"
private const val PREF_USER_AI_API_KEY = "user_ai_api_key"
private const val MAX_EDITABLE_PROJECT_FILE_BYTES = 1_000_000L
private const val AI_CONTEXT_FILE_LIMIT = 24
private const val AI_CONTEXT_CHAR_LIMIT = 60_000
private const val AI_TERMINAL_CONTEXT_LIMIT = 12_000
private const val AI_MAX_OUTPUT_TOKENS = 16_384
private const val PREVIEW_HANDOFF_TERMINAL_SNAPSHOT_CHARS = 60_000
private const val TERMINAL_ACTIVITY_RETENTION_MS = 60L * 60L * 1000L
private const val COMPLETION_IDLE_TIMEOUT_MS = 3_500L
private const val EDITOR_SAVE_DEBOUNCE_MS = 1_000L
private const val DIAGNOSTIC_IDLE_DELAY_MS = 220L
private const val EDITOR_METRICS_IDLE_DELAY_MS = 60L
private const val LSP_COMPLETION_DEBOUNCE_MS = 45L
private const val LSP_CACHE_ENTRY_LIMIT = 32
private const val LIVE_DIAGNOSTIC_CHAR_LIMIT = 180_000
private const val LIVE_SYNTAX_HIGHLIGHT_CHAR_LIMIT = 120_000
private const val RESPONSIVE_DIAGNOSTIC_CHAR_LIMIT = 60_000
private const val RESPONSIVE_HIGHLIGHT_CHAR_LIMIT = 50_000
private const val COMPLETION_SCAN_CHAR_LIMIT = 30_000
private const val COMMENT_STATE_SCAN_CHAR_LIMIT = 20_000
private const val EXTERNAL_LSP_LARGE_FILE_LIMIT = 80_000
private const val LIVE_COLOR_RETARGET_RADIUS = 2_000
private const val LARGE_FILE_HIGHLIGHT_IDLE_MS = 700L
private const val LIVE_PREVIEW_CACHE_VERSION = 5
private const val LIVE_PREVIEW_MAX_BROWSER_ASSET_BYTES = 30_000_000L
private const val NPM_REGISTRY_URL = "https://registry.npmjs.org"
private const val NPM_MAX_PACKAGE_DOWNLOAD_BYTES = 30_000_000L
private const val NPM_MAX_INSTALL_PACKAGES = 160
private const val NPM_MAX_DEPENDENCY_DEPTH = 7

private val htmlFileExtensions = setOf("html", "htm", "svg")
private val cssFileExtensions = setOf("css")
private val javascriptFileExtensions = setOf("js", "mjs", "cjs", "jsx", "ts", "tsx")
private val generalCodeFileExtensions =
        setOf(
                "kt",
                "kts",
                "java",
                "py",
                "pyw",
                "rb",
                "php",
                "c",
                "h",
                "cc",
                "cpp",
                "cxx",
                "hpp",
                "cs",
                "go",
                "rs",
                "swift",
                "dart",
                "scala",
                "sh",
                "bash",
                "zsh",
                "ps1",
                "sql",
                "xml",
                "yaml",
                "yml",
                "toml",
                "ini",
                "conf",
                "properties",
                "gradle",
                "groovy",
                "lua",
                "r",
                "vue",
                "svelte",
        )
private val editableWebExtensions =
        htmlFileExtensions +
                cssFileExtensions +
                javascriptFileExtensions +
                generalCodeFileExtensions +
                setOf("json", "md", "txt", "db", "db3", "s3db", "sl3", "sqlite", "sqlite3", "database")
private val defaultWebFileNames = listOf("index.html", "style.css", "script.js")
private val workspaceMetadataNames = setOf(".git", ".ssh", ".pocketcodedweb")
private val aiEditableFilePathRegex =
        Regex(
                """(?i)(?:^|[`'"\s(:-])([A-Za-z0-9_. -]+(?:/[A-Za-z0-9_. -]+)*\.(?:html|htm|css|js|mjs|cjs|jsx|ts|tsx|json|md|txt|svg))(?=$|[`'")\s:,-])"""
        )
private val jsImportSpecifierRegex =
        Regex(
                """(?m)\b(?:import|export)\s+(?:[^'"]*?\s+from\s*)?["']([^"']+)["']|import\s*\(\s*["']([^"']+)["']\s*\)"""
        )
private val commonLibraryPackageNames =
        listOf(
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
private val HTML_ATTR_NAME_CORE_REGEX =
        Regex("\\b(id|class|href|src|alt|type|name|value|style|onclick|role|aria-[\\w-]+)(?=\\s*=)")
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
private val CSS_KEYWORD_REGEX =
        Regex(
                "(?i)\\b(true|false|inherit|initial|unset|none|auto|block|inline|flex|grid|absolute|relative|fixed|sticky|center|space-between|space-around|column|row|sans-serif|serif|monospace)\\b"
        )

private val JS_COMMENT_REGEX = Regex("//.*$|/\\*[\\s\\S]*?\\*/", RegexOption.MULTILINE)
private val JS_STRING_REGEX = Regex("`[\\s\\S]*?`|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'")
private val JS_BOOLEAN_NULL_REGEX = Regex("\\b(true|false|null|undefined|NaN|Infinity)\\b")
private val JS_NUMBER_REGEX = Regex("\\b\\d+(?:\\.\\d+)?\\b")
private val JS_MEMBER_ACCESS_REGEX = Regex("\\.([A-Za-z_$][\\w$]*)")
private val JS_FUNC_CALL_REGEX =
        Regex("\\b([A-Za-z_$][A-Za-z0-9_$]*)\\s*(?=\\()|\\.([A-Za-z_$][A-Za-z0-9_$]*)\\s*(?=\\()")
private val JS_MODULE_SYNTAX_REGEX = Regex("""(?m)^\s*(import|export)\b""")

private val JSON_KEY_REGEX = Regex("\"[^\"]*\"(?=\\s*:)")
private val JSON_STRING_VALUE_REGEX = Regex(":\\s*(\"[^\"]*\")")
private val JSON_LITERAL_VALUE_REGEX = Regex(":\\s*(\\d+|true|false|null)")

private val aiModelOptions =
        listOf(
                AiModelOption(
                        "gemini-3.5-flash",
                        "Gemini 3.5 Flash",
                        "Fast coding help for everyday web work.",
                        AiProvider.Gemini
                ),
                AiModelOption(
                        "gemini-3.1-flash-lite",
                        "Gemini 3.1 Flash-Lite",
                        "Quick answers and small edits.",
                        AiProvider.Gemini
                ),
                AiModelOption(
                        "gemini-2.5-pro",
                        "Gemini 2.5 Pro",
                        "Deeper reasoning for larger changes.",
                        AiProvider.Gemini
                ),
                AiModelOption(
                        "gemini-2.5-flash",
                        "Gemini 2.5 Flash",
                        "Stable coding support.",
                        AiProvider.Gemini
                ),
                AiModelOption(
                        "gpt-4.1-mini",
                        "OpenAI — GPT-4.1 mini",
                        "OpenAI chat-completions API.",
                        AiProvider.OpenAiCompatible,
                        "https://api.openai.com/v1/chat/completions"
                ),
                AiModelOption(
                        "claude-sonnet-4-5",
                        "Anthropic — Claude Sonnet",
                        "Anthropic Messages API.",
                        AiProvider.Anthropic,
                        "https://api.anthropic.com/v1/messages"
                ),
                AiModelOption(
                        "openai/gpt-4.1-mini",
                        "OpenRouter",
                        "Use an OpenRouter API key and routed model.",
                        AiProvider.OpenAiCompatible,
                        "https://openrouter.ai/api/v1/chat/completions"
                ),
                AiModelOption(
                        "llama-3.3-70b-versatile",
                        "Groq — Llama 3.3",
                        "Fast OpenAI-compatible inference.",
                        AiProvider.OpenAiCompatible,
                        "https://api.groq.com/openai/v1/chat/completions"
                ),
                AiModelOption(
                        "mistral-small-latest",
                        "Mistral Small",
                        "Mistral OpenAI-compatible chat API.",
                        AiProvider.OpenAiCompatible,
                        "https://api.mistral.ai/v1/chat/completions"
                ),
                AiModelOption("meta-llama/Llama-3.3-70B-Instruct-Turbo", "Together — Llama 3.3", "Together OpenAI-compatible inference.", AiProvider.OpenAiCompatible, "https://api.together.xyz/v1/chat/completions"),
                AiModelOption("deployment", "Azure OpenAI", "Your Azure deployment chat-completions endpoint.", AiProvider.AzureOpenAi),
                AiModelOption("local-model", "Custom / Ollama", "A local or remote OpenAI-compatible endpoint.", AiProvider.OpenAiCompatible),
        )

@Stable
internal data class WebFile(
        val id: Long,
        val name: String,
        val content: String,
        val filePath: String,
        val modifiedAt: Long,
        val externalUri: String? = null,
        val sizeBytes: Long = content.length.toLong(),
        val contentHash: Int = content.hashCode(),
)

@Stable
internal data class FolderTarget(
        val name: String,
        val path: String,
        val relativePath: String,
)

private sealed class ExplorerEntry {
    data class Directory(val target: FolderTarget, val depth: Int, val expanded: Boolean) :
            ExplorerEntry()
    data class SourceFile(val file: WebFile, val depth: Int) : ExplorerEntry()
}

private enum class WebFullScreenMode {
    Explorer,
    Editor,
    Terminal,
    Ai,
}

private enum class WebPreviewDestination {
    InApp,
    Browser,
}

private data class InAppLivePreviewState(
        val title: String,
        val url: String,
)

internal enum class WebTheme(
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
    VSCodeDefaultLight("VS Code Default Light", false),
    CyanOrbitDark("Cyan Orbit Dark", true),
    CyanOrbitLight("Cyan Orbit Light", false),
    BlueHorizonDark("Blue Horizon Dark", true),
    BlueHorizonLight("Blue Horizon Light", false),
    GreenCometDark("Green Comet Dark", true),
    GreenCometLight("Green Comet Light", false);

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
        val editorBackground: Color = panel,
        val terminalBackground: Color = panel,
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

internal data class WebShellResult(
        val output: String = "",
        val error: String = "",
        val workingDirectoryPath: String? = null,
        val clearTerminal: Boolean = false,
        val workspaceChanged: Boolean = false,
        val openPreview: Boolean = false,
        val previewLink: String? = null,
        val runJs: String? = null,
        val openFilePath: String? = null,
)

internal data class CompletionItem(
        val label: String,
        val insertText: String = label,
        val detail: String = "",
        val cursorOffset: Int = insertText.length,
        val documentation: String = "",
        val documentationOwner: String = "",
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

private fun plainEditorVisualResult(source: String): EditorVisualResult {
    return EditorVisualResult(source, AnnotatedString(source))
}

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
    Python,
    Php,
    Sql,
    Text,
}

private data class EmbeddedWebBlock(
        val language: WebEditorLanguage,
        val start: Int,
        val end: Int,
)

private enum class AiProvider {
    Gemini,
    Anthropic,
    OpenAiCompatible,
    AzureOpenAi
}

private data class AiModelOption(
        val id: String,
        val label: String,
        val description: String,
        val provider: AiProvider,
        val endpoint: String = "",
) {
    val defaultEndpoint: String get() = endpoint.ifBlank {
        if (provider == AiProvider.Gemini) "https://generativelanguage.googleapis.com/v1beta" else ""
    }
    val connectionKey: String get() = if (provider == AiProvider.Gemini) "gemini" else label
}

private data class AiChatMessage(
        val role: AiChatRole,
        val text: String,
        val modifiedFiles: List<String> = emptyList(),
)

private enum class AiChatRole {
    User,
    Assistant,
}

internal sealed class AiSuggestedAction {
    data class RunTerminalCommand(val command: String) : AiSuggestedAction()
    data class ReplaceActiveFile(val code: String) : AiSuggestedAction()
    data class ApplyProjectFileChanges(val changes: List<AiProjectFileChange>) :
            AiSuggestedAction()
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

private data class NpmPackageSpec(
        val name: String,
        val requestedVersion: String = "latest",
)

private data class NpmResolvedPackage(
        val name: String,
        val version: String,
        val tarballUrl: String,
        val integrity: String,
        val dependencies: Map<String, String>,
)

private data class NpmInstallOptions(
        val packages: List<NpmPackageSpec>,
        val saveDev: Boolean = false,
        val noSave: Boolean = false,
)

private data class PackageExecutableRequest(
        val command: String,
        val packageSpec: NpmPackageSpec?,
        val args: List<String>,
        val noInstall: Boolean = false,
)

private data class Semver(
        val major: Int,
        val minor: Int,
        val patch: Int,
) : Comparable<Semver> {
    override fun compareTo(other: Semver): Int {
        return compareValuesBy(this, other, Semver::major, Semver::minor, Semver::patch)
    }
}

private class NpmInstallState(
        val rootDirectory: File,
        val metadataCache: MutableMap<String, JSONObject> = mutableMapOf(),
        val installedByName: MutableMap<String, String> = mutableMapOf(),
        val lockPackages: JSONObject = JSONObject(),
        val messages: MutableList<String> = mutableListOf(),
) {
    var installCount: Int = 0
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NativeNodeRuntime.initialize(applicationContext)
        // Filesystem setup is independent of the first frame. Until it finishes the
        // terminal safely retains the existing JGit implementation as a fallback.
        lifecycleScope.launch(Dispatchers.IO) { NativeGitRuntime.install(applicationContext) }
        enableEdgeToEdge()
        setContent {
            val activityContext = this@MainActivity
            var ideTheme by rememberSaveable { mutableStateOf(loadWebIdeTheme(activityContext)) }
            var appServicesReady by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    runCatching { configureFirebaseAppCheck(activityContext.applicationContext) }
                }
                appServicesReady = true
            }

            PocketCodedWebTheme(
                    darkTheme = ideTheme.isDark,
                    themeName = ideTheme.name,
                    dynamicColor = false,
            ) {
                if (appServicesReady) {
                    PocketCodedWebEntry(
                            darkMode = ideTheme.isDark,
                            currentThemeName = ideTheme.name,
                            nextThemeName = ideTheme.next().displayName,
                            onToggleTheme = {
                                ideTheme =
                                        ideTheme.next().also {
                                            saveWebIdeTheme(activityContext, it)
                                        }
                            },
                    ) { projectDirectory, onBackToProjects ->
                        WebIdeApp(
                                ideTheme = ideTheme,
                                onToggleTheme = {
                                    ideTheme =
                                            ideTheme.next().also {
                                                saveWebIdeTheme(activityContext, it)
                                            }
                                },
                                projectDirectory = projectDirectory,
                                onBackToProjects = onBackToProjects,
                        )
                    }
                } else {
                    Surface(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun WebIdeApp(
        ideTheme: WebTheme,
        onToggleTheme: () -> Unit,
        projectDirectory: File,
        onBackToProjects: () -> Unit,
) {
    val context = LocalContext.current
    val palette = remember(ideTheme) { webIdePalette(ideTheme) }
    val coroutineScope = rememberCoroutineScope()
    val rootDirectory =
            remember(projectDirectory.absolutePath) { projectDirectory.apply { mkdirs() } }
    val editorSessions = remember(rootDirectory.absolutePath) { EditorSessionRegistry() }
    val apiTester = remember(rootDirectory.absolutePath) { ApiTesterState(coroutineScope) }
    DisposableEffect(apiTester) { onDispose { apiTester.cancel() } }
    val fileStore = remember(rootDirectory.absolutePath) { ProjectFileStore(rootDirectory) }
    val development = remember(rootDirectory.absolutePath) { WebDevelopmentController(context, rootDirectory) }
    val commandHistory = remember(rootDirectory.absolutePath) { DailyTerminalCommandHistory() }
    val historyLifecycle = (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
    LaunchedEffect(commandHistory, historyLifecycle) {
        suspend fun refreshDailyHistory() {
            while (true) {
                commandHistory.refresh()
                val midnight = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                delay((midnight.timeInMillis - System.currentTimeMillis()).coerceAtLeast(1_000L))
            }
        }
        if (historyLifecycle != null) {
            historyLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { refreshDailyHistory() }
        } else refreshDailyHistory()
    }
    val savedSession =
            remember(rootDirectory.absolutePath) { loadWebIdeSession(context, rootDirectory) }
    val initialFiles =
            remember(rootDirectory.absolutePath) {
                loadInitialWebFiles(rootDirectory, savedSession.activeFilePath)
            }
    val files =
            remember(rootDirectory.absolutePath) {
                mutableStateListOf<WebFile>().apply { addAll(initialFiles) }
            }
    var activeFileId by
            rememberSaveable(rootDirectory.absolutePath) {
                mutableLongStateOf(
                        savedSession.activeFilePath?.let { savedPath ->
                            initialFiles.firstOrNull { samePath(it.filePath, savedPath) }?.id
                        }
                                ?: savedSession.activeFileId.takeIf { id ->
                                    initialFiles.any { it.id == id }
                                }
                                        ?: initialFiles.firstOrNull()?.id ?: -1L
                )
            }
    var explorerVisible by
            rememberSaveable(rootDirectory.absolutePath) {
                mutableStateOf(savedSession.explorerVisible)
            }
    var fileSearch by
            rememberSaveable(rootDirectory.absolutePath) { mutableStateOf(savedSession.fileSearch) }
    var terminalInput by
            rememberSaveable(rootDirectory.absolutePath) {
                mutableStateOf(savedSession.terminalInput)
            }
    var terminalText by
            rememberSaveable(rootDirectory.absolutePath) {
                mutableStateOf(savedSession.terminalText)
            }
    val nodeLogLifecycle = (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
    LaunchedEffect(rootDirectory.absolutePath, nodeLogLifecycle) {
        var previousLiveText: String? = null
        suspend fun followNodeOutput() {
            while (true) {
                val (output, running) = withContext(Dispatchers.IO) {
                    NodeSessionRuntime.drainOutput(rootDirectory) to NodeSessionRuntime.isRunningFor(rootDirectory)
                }
                if (output.isNotEmpty()) {
                    terminalText = trimTerminalBuffer(terminalText +
                        (if (terminalText != previousLiveText && terminalText.isNotEmpty() && !terminalText.endsWith("\n")) "\n" else "") + output)
                    previousLiveText = terminalText
                }
                delay(if (running) DeviceEfficiency.budget.logFlushMs.toLong().coerceAtLeast(250L)
                      else DeviceEfficiency.budget.idlePollMs)
            }
        }
        // Keep state updates on Android's UI dispatcher, including in UI tests.
        withContext(Dispatchers.Main.immediate) {
            if (nodeLogLifecycle != null) {
                nodeLogLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { followNodeOutput() }
            } else followNodeOutput()
        }
    }
    var fullScreenMode by
            rememberSaveable(rootDirectory.absolutePath) {
                mutableStateOf(savedSession.fullScreenMode)
            }
    var directoryVersion by remember { mutableLongStateOf(0L) }
    var fileActionsDialogOpen by remember { mutableStateOf(false) }
    var newFileDialogOpen by remember { mutableStateOf(false) }
    var newFolderDialogOpen by remember { mutableStateOf(false) }
    var renameFileTarget by remember { mutableStateOf<WebFile?>(null) }
    var renameFolderTarget by remember { mutableStateOf<FolderTarget?>(null) }
    var deleteFileTarget by remember { mutableStateOf<WebFile?>(null) }
    var deleteFolderTarget by remember { mutableStateOf<FolderTarget?>(null) }
    val expandedFolderPaths =
            remember(rootDirectory.absolutePath) {
                mutableStateListOf<String>().apply {
                    addAll(
                            normalizedWorkspaceDirectoryPaths(
                                    rootDirectory,
                                    savedSession.expandedFolderPaths
                            )
                    )
                }
            }
    val dirtyFileIds = remember { mutableSetOf<Long>() }
    val pendingEditorTextByFileId =
            remember(rootDirectory.absolutePath) { mutableMapOf<Long, String>() }
    val pendingFileEdits = remember(rootDirectory.absolutePath) { mutableMapOf<Long, PendingFileEdit>() }
    val pendingEditorSaveJobs = remember { mutableMapOf<Long, Job>() }
    val fileRefreshGeneration = remember(rootDirectory.absolutePath) { AtomicLong(0L) }
    var lastPersistedSession by
            remember(rootDirectory.absolutePath) { mutableStateOf(savedSession) }
    var lastPersistedTerminalText by
            remember(rootDirectory.absolutePath) {
                mutableStateOf(savedSession.terminalText.isNotBlank())
            }

    val activeFile =
            (files.firstOrNull { it.id == activeFileId } ?: files.firstOrNull())
                    ?.withPendingEditorText(pendingEditorTextByFileId)

    LaunchedEffect(files.size, activeFileId) {
        when {
            files.isEmpty() && activeFileId != -1L -> activeFileId = -1L
            files.isNotEmpty() && files.none { it.id == activeFileId } ->
                    activeFileId = files.first().id
        }
    }

    BackHandler(enabled = true) {
        if (fullScreenMode != null) {
            fullScreenMode = null
        } else {
            onBackToProjects()
        }
    }

    var workingDirectoryPath by
            rememberSaveable(rootDirectory.absolutePath) {
                mutableStateOf(
                        validDirectoryOrRoot(
                                        rootDirectory = rootDirectory,
                                        candidatePath = savedSession.workingDirectoryPath
                                                        ?: activeFile?.parentDirectoryPath(
                                                                rootDirectory
                                                        )
                                                                ?: rootDirectory.absolutePath,
                                )
                                .absolutePath
                )
            }
    var terminalDirectoryPath by
            rememberSaveable(rootDirectory.absolutePath) {
                mutableStateOf(
                        validDirectoryOrRoot(
                                        rootDirectory = rootDirectory,
                                        candidatePath = savedSession.terminalDirectoryPath
                                                        ?: activeFile?.parentDirectoryPath(
                                                                rootDirectory
                                                        )
                                                                ?: rootDirectory.absolutePath,
                                )
                                .absolutePath
                )
            }
    val workingDirectory =
            remember(workingDirectoryPath, directoryVersion) {
                validDirectoryOrRoot(rootDirectory, workingDirectoryPath)
            }
    val terminalDirectory =
            remember(terminalDirectoryPath, directoryVersion) {
                validDirectoryOrRoot(rootDirectory, terminalDirectoryPath)
            }
    val tabFiles by
            remember(workingDirectoryPath, directoryVersion) {
                derivedStateOf {
                    files.asSequence()
                            .filter { it.isDirectChildOf(workingDirectory) }
                            .sortedByDescending { it.modifiedAt }
                            .toList()
                }
            }
    val terminalPromptText = terminalPrompt(rootDirectory, terminalDirectory)

    var selectedAiModelId by
            rememberSaveable(rootDirectory.absolutePath) {
                mutableStateOf(aiModelOptions.first().id)
            }
    val selectedAiModel =
            aiModelOptions.firstOrNull { it.id == selectedAiModelId } ?: aiModelOptions.first()
    val aiConnections = remember { AiConnectionStore(context) }
    remember(aiConnections) { aiConnections.migrateLegacy(aiModelOptions.first().connectionKey, savedSession.userAiApiKey) }
    var userAiApiKey by remember(selectedAiModel.connectionKey) {
        mutableStateOf(aiConnections.key(selectedAiModel.connectionKey).orEmpty())
    }
    var aiEndpointOverride by remember(selectedAiModel.id) { mutableStateOf(aiConnections.endpoint(selectedAiModel.id)) }
    var aiModelOverride by remember(selectedAiModel.id) { mutableStateOf(aiConnections.model(selectedAiModel.id)) }
    var aiUndoChange by remember(rootDirectory.absolutePath) { mutableStateOf<AiFileUndo?>(null) }
    var aiActionTargetPath by remember(rootDirectory.absolutePath) { mutableStateOf<String?>(null) }
    val aiMessages =
            remember(rootDirectory.absolutePath) {
                mutableStateListOf(
                        AiChatMessage(
                                role = AiChatRole.Assistant,
                                text =
                                        "Hi, I can read this Web project, explain code, suggest edits, and prepare terminal commands. Add a Gemini API key when you want me to make a request.",
                        )
                )
            }
    var aiInput by rememberSaveable(rootDirectory.absolutePath) { mutableStateOf("") }
    var aiBusy by remember { mutableStateOf(false) }
    var aiApplying by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var aiPendingAction by remember { mutableStateOf<AiSuggestedAction?>(null) }
    var aiVerificationMessage by remember { mutableStateOf<String?>(null) }
    var terminalJsToRun by remember { mutableStateOf<String?>(null) }
    var terminalRevealRequest by remember { mutableLongStateOf(0L) }
    var browserPreviewReturnPending by
            remember(rootDirectory.absolutePath) { mutableStateOf(false) }
    var previewDestinationDialogOpen by
            remember(rootDirectory.absolutePath) { mutableStateOf(false) }
    var previewLaunchBusy by remember(rootDirectory.absolutePath) { mutableStateOf(false) }
    var inAppLivePreview by
            remember(rootDirectory.absolutePath) { mutableStateOf<InAppLivePreviewState?>(null) }
    var terminalBackgroundedAt by
            rememberSaveable(rootDirectory.absolutePath) { mutableLongStateOf(0L) }

    fun currentWebSession(terminalSnapshot: String = terminalText): WebSession {
        return WebSession(
                activeFileId = activeFile?.id ?: -1L,
                activeFilePath = activeFile?.filePath,
                workingDirectoryPath = workingDirectoryPath,
                terminalDirectoryPath = terminalDirectoryPath,
                expandedFolderPaths = expandedFolderPaths.toList(),
                explorerVisible = explorerVisible,
                fileSearch = fileSearch,
                terminalInput = terminalInput,
                terminalText = terminalSnapshot,
                fullScreenMode = fullScreenMode,
                userAiApiKey = null,
        )
    }

    fun saveCurrentWebIdeSession(
            persistTerminalText: Boolean = false,
            terminalSnapshot: String = terminalText,
    ) {
        val session = currentWebSession(terminalSnapshot)
        if (session == lastPersistedSession && persistTerminalText == lastPersistedTerminalText)
                return
        saveWebIdeSession(
                context = context,
                rootDirectory = rootDirectory,
                session = session,
                persistTerminalText = persistTerminalText,
        )
        lastPersistedSession = session
        lastPersistedTerminalText = persistTerminalText
    }

    fun releaseBrowserPreviewHandoffMemory() {
        WebLanguageServices.releasePreviewHandoffMemory()
        ProjectFileCache.clearAll()
    }

    fun prepareBrowserPreviewHandoff() {
        val compactTerminalText = compactTerminalSnapshotForPreviewHandoff(terminalText)
        terminalBackgroundedAt = System.currentTimeMillis()
        if (compactTerminalText != terminalText) {
            terminalText = compactTerminalText
        }
        releaseBrowserPreviewHandoffMemory()
        saveCurrentWebIdeSession(
                persistTerminalText = true,
                terminalSnapshot = compactTerminalText,
        )
    }

    fun persistTerminalForBackground() {
        val compactTerminalText = compactTerminalSnapshotForPreviewHandoff(terminalText)
        terminalBackgroundedAt = System.currentTimeMillis()
        if (compactTerminalText != terminalText) {
            terminalText = compactTerminalText
        }
        saveCurrentWebIdeSession(
                persistTerminalText = compactTerminalText.isNotBlank(),
                terminalSnapshot = compactTerminalText,
        )
    }

    DisposableEffect(context, rootDirectory.absolutePath) {
        val lifecycle = (context as? ComponentActivity)?.lifecycle
        if (lifecycle == null) {
            onDispose {}
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        val draftsToStage = pendingFileEdits.values.toList()
                        (context as? ComponentActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
                            draftsToStage.forEach { fileStore.stageDraft(it) }
                        }
                        persistTerminalForBackground()
                        if (browserPreviewReturnPending) {
                            releaseBrowserPreviewHandoffMemory()
                        }
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        val terminalExpired =
                                terminalBackgroundedAt > 0L &&
                                        System.currentTimeMillis() - terminalBackgroundedAt >=
                                                TERMINAL_ACTIVITY_RETENTION_MS
                        terminalBackgroundedAt = 0L
                        if (terminalExpired) {
                            terminalText = ""
                            browserPreviewReturnPending = false
                            saveCurrentWebIdeSession()
                        } else if (browserPreviewReturnPending) {
                            browserPreviewReturnPending = false
                            if (fullScreenMode != WebFullScreenMode.Terminal) {
                                fullScreenMode = null
                            }
                            terminalRevealRequest += 1L
                            terminalText =
                                    appendTerminalOutput(
                                            terminalText,
                                            "[localhost] Returned from browser preview. Terminal activity is current.",
                                    )
                        }
                    }
                    else -> Unit
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    if (terminalJsToRun != null) {
        val jsCode = terminalJsToRun!!
        AndroidView(
                factory = {
                    WebView(it).apply {
                        settings.javaScriptEnabled = true
                        webChromeClient =
                                object : WebChromeClient() {
                                    override fun onConsoleMessage(
                                            consoleMessage: ConsoleMessage?
                                    ): Boolean {
                                        consoleMessage ?: return false
                                        coroutineScope.launch {
                                            terminalText =
                                                    appendTerminalOutput(
                                                            terminalText,
                                                            formatPreviewRuntimeMessage(
                                                                    level =
                                                                            consoleMessage
                                                                                    .messageLevel()
                                                                                    .name
                                                                                    .lowercase(),
                                                                    message =
                                                                            consoleMessage
                                                                                    .message()
                                                                                    .orEmpty(),
                                                                    source =
                                                                            consoleMessage
                                                                                    .sourceId()
                                                                                    .orEmpty()
                                                                                    .ifBlank {
                                                                                        "terminal.js"
                                                                                    },
                                                                    line =
                                                                            consoleMessage
                                                                                    .lineNumber(),
                                                                    column = 0,
                                                            ),
                                                    )
                                        }
                                        return true
                                    }
                                }
                        addJavascriptInterface(
                                object {
                                    @JavascriptInterface
                                    fun log(msg: String) {
                                        coroutineScope.launch {
                                            terminalText = appendTerminalOutput(terminalText, msg)
                                        }
                                    }
                                    @JavascriptInterface
                                    fun error(msg: String) {
                                        coroutineScope.launch {
                                            terminalText =
                                                    appendTerminalOutput(
                                                            terminalText,
                                                            formatPreviewRuntimeMessage(
                                                                    "error",
                                                                    msg,
                                                                    "terminal.js",
                                                                    0,
                                                                    0
                                                            ),
                                                    )
                                        }
                                    }
                                },
                                "terminal"
                        )
                    }
                },
                update = { webView ->
                    val wrappedJs =
                            """
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
        val loadedFileIds = loadedFiles.mapTo(mutableSetOf()) { it.id }
        val missingFileIds =
                (pendingEditorTextByFileId.keys + dirtyFileIds + pendingEditorSaveJobs.keys)
                        .filter { it !in loadedFileIds }
        missingFileIds.forEach { fileId ->
            pendingEditorSaveJobs.remove(fileId)?.cancel()
            pendingEditorTextByFileId.remove(fileId)
            dirtyFileIds.remove(fileId)
        }
        val currentFilesById = files.associateBy(WebFile::id)
        val mergedFiles =
                loadedFiles.map { loadedFile ->
                    val currentFile = currentFilesById[loadedFile.id]
                    val pendingText = pendingEditorTextByFileId[loadedFile.id]
                    if (pendingText != null) {
                        loadedFile.withContent(
                                content = pendingText,
                                modifiedAt = currentFile?.modifiedAt ?: loadedFile.modifiedAt,
                        )
                    } else if (currentFile != null && currentFile.id in dirtyFileIds) {
                        loadedFile.withContent(
                                content = currentFile.content,
                                modifiedAt = currentFile.modifiedAt,
                        )
                    } else {
                        loadedFile
                    }
                }
        files.clear()
        files.addAll(mergedFiles)
        val selectedFile =
                selectPath?.let { path -> mergedFiles.firstOrNull { samePath(it.filePath, path) } }
                        ?: mergedFiles.firstOrNull { it.id == activeFileId }
                                ?: mergedFiles.firstOrNull()
        activeFileId = selectedFile?.id ?: -1L
        workingDirectoryPath =
                validDirectoryOrRoot(
                                rootDirectory = rootDirectory,
                                candidatePath = workingPath
                                                ?: selectedFile?.parentDirectoryPath(rootDirectory)
                                                        ?: rootDirectory.absolutePath,
                        )
                        .absolutePath
        directoryVersion += 1L
    }

    fun refreshFiles(selectPath: String? = null, workingPath: String? = null) {
        ProjectSemanticIndexer.invalidate(rootDirectory)
        val generation = fileRefreshGeneration.incrementAndGet()
        coroutineScope.launch {
            val loadedFiles = withContext(Dispatchers.IO) { loadWebFiles(rootDirectory) }
            if (fileRefreshGeneration.get() == generation) {
                applyLoadedFiles(loadedFiles, selectPath, workingPath)
            }
        }
    }

    val openFileLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
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

    val openFolderLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
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

    val exportFolderLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
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
    }

    fun discardEditorState(fileId: Long) {
        files.firstOrNull { it.id == fileId }?.let { editorSessions.invalidate(it.filePath) }
        pendingEditorSaveJobs.remove(fileId)?.cancel()
        pendingFileEdits.remove(fileId)
        files.firstOrNull { it.id == fileId }?.let { fileStore.invalidatePendingEdit(it.filePath) }
        pendingEditorTextByFileId.remove(fileId)
        dirtyFileIds.remove(fileId)
    }

    fun discardEditorStateUnder(directory: File) {
        files.filter { file -> isSameOrInside(directory, File(file.filePath)) }.forEach { file ->
            discardEditorState(file.id)
        }
    }

    fun discardEditorStateForTerminalDelete(command: String) {
        val args = splitShellCommandLine(command)
        val executable = args.firstOrNull()?.lowercase().orEmpty()
        if (executable !in setOf("rm", "del")) return
        val target =
                resolveTerminalPath(rootDirectory, terminalDirectory, args.getOrNull(1).orEmpty())
                        ?: return
        if (samePath(target.absolutePath, rootDirectory.absolutePath)) return
        if (target.isDirectory) {
            discardEditorStateUnder(target)
        } else {
            files.firstOrNull { file -> samePath(file.filePath, target.absolutePath) }?.let { file
                ->
                discardEditorState(file.id)
            }
        }
    }

    LaunchedEffect(rootDirectory.absolutePath) {
        refreshFiles(
                selectPath = savedSession.activeFilePath,
                workingPath = savedSession.workingDirectoryPath,
        )
    }

    fun updateActiveContent(newCode: String) {
        val fileToSync = activeFile ?: return
        development.relocateBreakpoints(fileToSync.filePath, pendingEditorTextByFileId[fileToSync.id] ?: fileToSync.content, newCode)
        val pendingEdit = fileStore.prepareEdit(fileToSync.filePath, newCode)
        pendingFileEdits[fileToSync.id] = pendingEdit
        pendingEditorTextByFileId[fileToSync.id] = newCode
        dirtyFileIds += fileToSync.id
        pendingEditorSaveJobs.remove(fileToSync.id)?.cancel()
        pendingEditorSaveJobs[fileToSync.id] =
                coroutineScope.launch {
                    delay(150)
                    withContext(Dispatchers.IO) { fileStore.stageDraft(pendingEdit) }
                    delay(EDITOR_SAVE_DEBOUNCE_MS - 150)
                    if (files.none { it.id == fileToSync.id }) {
                        pendingEditorTextByFileId.remove(fileToSync.id)
                        pendingFileEdits.remove(fileToSync.id)
                        dirtyFileIds.remove(fileToSync.id)
                        pendingEditorSaveJobs.remove(fileToSync.id)
                        return@launch
                    }
                    if (pendingEditorTextByFileId[fileToSync.id] != newCode) {
                        return@launch
                    }
                    val modifiedAt = System.currentTimeMillis()
                    val saved = withContext(Dispatchers.IO) {
                        fileStore.save(pendingEdit).onSuccess {
                            ProjectFileCache.invalidate(File(fileToSync.filePath))
                            ProjectSemanticIndexer.invalidate()
                        }
                    }
                    if (saved.isFailure) {
                        terminalText = appendTerminalOutput(terminalText, "Save failed: ${saved.exceptionOrNull()?.message}")
                        return@launch
                    }
                    // A newer edit may have arrived while the file write was in progress.
                    // Never publish this older snapshot back into Compose/editor state.
                    if (pendingEditorTextByFileId[fileToSync.id] != newCode) {
                        return@launch
                    }
                    files.replaceFile(fileToSync.id) {
                        withContent(content = newCode, modifiedAt = modifiedAt)
                    }
                    if (pendingEditorTextByFileId[fileToSync.id] == newCode) {
                        pendingEditorTextByFileId.remove(fileToSync.id)
                        pendingFileEdits.remove(fileToSync.id)
                        dirtyFileIds.remove(fileToSync.id)
                        pendingEditorSaveJobs.remove(fileToSync.id)
                    }
                }
    }

    fun createFile(rawName: String): String? {
        val cleanName = normalizeWebFileName(rawName) ?: return "Use a simple Web file name."
        val target = File(workingDirectory, cleanName).canonicalFile
        if (!isSameOrInside(rootDirectory, target)) return "File must stay inside this project."
        if (target.exists()) return "A file named $cleanName already exists here."
        target.parentFile?.mkdirs()
        if (isDatabaseFileName(cleanName)) SQLiteDatabase.openOrCreateDatabase(target, null).close()
        else target.writeText(defaultContentForFile(cleanName, rootDirectory.name))
        refreshFiles(
                selectPath = target.absolutePath,
                workingPath = target.parentFile?.absolutePath
        )
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
        refreshFiles(
                selectPath = target.absolutePath,
                workingPath = target.parentFile?.absolutePath
        )
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
        expandedFolderPaths.removeAll {
            samePath(it, source.absolutePath) || it.startsWith(source.absolutePath + File.separator)
        }
        expandedFolderPaths.addUniquePath(target.absolutePath)
        refreshFiles(workingPath = target.absolutePath)
        return null
    }

    fun deleteFile(file: WebFile) {
        discardEditorState(file.id)
        File(file.filePath).takeIf { isSameOrInside(rootDirectory, it) }?.delete()
        refreshFiles(workingPath = workingDirectory.absolutePath)
    }

    fun deleteFolder(folder: FolderTarget) {
        val directory = File(folder.path)
        if (samePath(directory.absolutePath, rootDirectory.absolutePath)) return
        if (isSameOrInside(rootDirectory, directory)) {
            discardEditorStateUnder(directory)
            directory.deleteRecursively()
            expandedFolderPaths.removeAll {
                samePath(it, directory.absolutePath) ||
                        it.startsWith(directory.absolutePath + File.separator)
            }
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
                withContent(content = pendingText, modifiedAt = modifiedAt)
            }
            dirtyFileIds.remove(fileId)
        }
        pendingEditorTextByFileId.clear()
    }

    fun launchLivePreview(
            previewActiveFile: WebFile,
            previewFiles: List<WebFile>,
            destination: WebPreviewDestination,
    ) {
        coroutineScope.launch {
            val result =
                    withContext(Dispatchers.IO) {
                        runCatching {
                            prepareLivePreviewEntry(rootDirectory, previewFiles, previewActiveFile)
                        }
                    }
            result
                    .onSuccess { entryFile ->
                        val onPreviewLog: (String) -> Unit = { message ->
                            coroutineScope.launch {
                                terminalText = appendTerminalOutput(terminalText, message)
                            }
                        }
                        when (destination) {
                            WebPreviewDestination.Browser -> {
                                inAppLivePreview = null
                                val launchResult =
                                        openLivePreviewInDefaultBrowser(
                                                context = context,
                                                entryFile = entryFile,
                                                onLog = onPreviewLog,
                                                beforeOpen = { url ->
                                                    browserPreviewReturnPending = true
                                                    terminalText =
                                                            appendTerminalOutput(
                                                                    terminalText,
                                                                    "Serving ${previewActiveFile.name} at $url\nOpening in your device browser. Press Back from the browser to return here and review terminal activity.",
                                                            )
                                                    prepareBrowserPreviewHandoff()
                                                },
                                        )
                                if (launchResult.error != null) {
                                    browserPreviewReturnPending = false
                                    terminalText =
                                            appendTerminalOutput(
                                                    terminalText,
                                                    "Serving ${previewActiveFile.name} at ${launchResult.url}\nCould not open the device browser: ${launchResult.error}",
                                            )
                                }
                            }
                            WebPreviewDestination.InApp -> {
                                val previewResult =
                                        startLivePreviewServer(
                                                entryFile = entryFile,
                                                onLog = onPreviewLog,
                                        )
                                if (previewResult.error != null) {
                                    terminalText =
                                            appendTerminalOutput(
                                                    terminalText,
                                                    "Could not open in-app Live Preview: ${previewResult.error}",
                                            )
                                } else {
                                    browserPreviewReturnPending = false
                                    inAppLivePreview =
                                            InAppLivePreviewState(
                                                    title = previewActiveFile.name,
                                                    url = previewResult.url,
                                            )
                                    terminalText =
                                            appendTerminalOutput(
                                                    terminalText,
                                                    "Serving ${previewActiveFile.name} at ${previewResult.url}\nOpened in in-app Live Preview.",
                                            )
                                }
                            }
                        }
                    }
                    .onFailure { error ->
                        terminalText =
                                appendTerminalOutput(
                                        terminalText,
                                        "Could not prepare live preview: ${error.message ?: "unknown error"}",
                                )
                    }
        }
    }

    fun requestWebPreview() {
        previewDestinationDialogOpen = true
    }

    fun runWebPreview(destination: WebPreviewDestination) {
        if (previewLaunchBusy) return
        val previewActiveFile = activeFile
        val pendingFiles =
                files.filter { it.id in pendingEditorTextByFileId }.map {
                    it.withPendingEditorText(pendingEditorTextByFileId)
                }
        val previewFiles = files.withPendingEditorTexts(pendingEditorTextByFileId)
        commitPendingEditorStateForPreview()
        previewLaunchBusy = true
        terminalText =
                appendTerminalOutput(
                        terminalText,
                        "$terminalPromptText preview\nStarting localhost preview. Waiting for the server address...",
                )
        coroutineScope.launch {
            try {
                val server =
                        withContext(Dispatchers.IO) {
                            pendingFiles.forEach {
                                saveFileContent(it.filePath, it.content, it.modifiedAt)
                            }
                            prepareProjectServerPreview(
                                    rootDirectory,
                                    workingDirectory,
                                    browserPreview = destination == WebPreviewDestination.Browser,
                            )
                        }
                previewDestinationDialogOpen = false
                if (server == null) {
                    if (previewActiveFile == null) {
                        terminalText =
                                appendTerminalOutput(
                                        terminalText,
                                        "Open a frontend file or add a dev/start script to package.json."
                                )
                    } else {
                        launchLivePreview(previewActiveFile, previewFiles, destination)
                    }
                } else {
                    terminalText = appendTerminalOutput(terminalText, appendShellResult(server))
                    server.previewLink?.let { url ->
                        if (destination == WebPreviewDestination.InApp) {
                            inAppLivePreview =
                                    InAppLivePreviewState(title = "Server preview", url = url)
                        } else {
                            inAppLivePreview = null
                            browserPreviewReturnPending = true
                            terminalText =
                                    appendTerminalOutput(
                                            terminalText,
                                            "Opening in your device browser: $url"
                                    )
                            prepareBrowserPreviewHandoff()
                            openUrlInDefaultBrowser(context, url)?.let { error ->
                                browserPreviewReturnPending = false
                                terminalText =
                                        appendTerminalOutput(
                                                terminalText,
                                                "Could not open browser: $error\nPreview URL: $url"
                                        )
                            }
                        }
                    }
                }
            } catch (error: kotlinx.coroutines.CancellationException) {
                throw error
            } catch (error: Exception) {
                terminalText =
                        appendTerminalOutput(
                                terminalText,
                                "Could not start preview: ${error.message}"
                        )
                previewDestinationDialogOpen = false
            } finally {
                previewLaunchBusy = false
            }
        }
    }

    fun runActiveScriptFile(scriptFile: WebFile) {
        val runnableFile = scriptFile.withPendingEditorText(pendingEditorTextByFileId)
        commitPendingEditorStateForPreview()
        val committedFile = files.firstOrNull { it.id == runnableFile.id } ?: runnableFile
        coroutineScope.launch(Dispatchers.IO) {
            saveFileContent(committedFile.filePath, committedFile.content, committedFile.modifiedAt)
        }
        terminalJsToRun = terminalRunnableJavaScript(committedFile, rootDirectory)
        when (fullScreenMode) {
            WebFullScreenMode.Editor, WebFullScreenMode.Ai, WebFullScreenMode.Explorer ->
                    fullScreenMode = WebFullScreenMode.Terminal
            WebFullScreenMode.Terminal -> Unit
            null -> terminalRevealRequest += 1L
        }
    }

    fun runActiveFile() {
        val fileToRun = activeFile
        if (fileToRun != null && isDatabaseFileName(fileToRun.name)) {
            fullScreenMode = WebFullScreenMode.Editor
            return
        }
        if (fileToRun == null) {
            terminalText =
                    appendTerminalOutput(
                            terminalText,
                            "$terminalPromptText run\nNo file is open. Create or import a Web file before running code.",
                    )
            return
        }
        if (fileToRun.name.endsWith(".sql", ignoreCase = true)) {
            val sqlFile = fileToRun.withPendingEditorText(pendingEditorTextByFileId)
            commitPendingEditorStateForPreview()
            terminalText =
                    appendTerminalOutput(terminalText, "$terminalPromptText sql ${sqlFile.name}")
            coroutineScope.launch {
                val output =
                        withContext(Dispatchers.IO) { runSqlScript(rootDirectory, sqlFile.content) }
                terminalText = appendTerminalOutput(terminalText, output)
                when (fullScreenMode) {
                    WebFullScreenMode.Editor, WebFullScreenMode.Ai, WebFullScreenMode.Explorer ->
                            fullScreenMode = WebFullScreenMode.Terminal
                    else -> terminalRevealRequest += 1L
                }
                refreshFiles(workingPath = workingDirectory.absolutePath)
            }
        } else if (isTerminalRunnableScript(fileToRun.name)) {
            runActiveScriptFile(fileToRun)
        } else {
            requestWebPreview()
        }
    }

    fun submitTerminalCommand(commandOverride: String? = null) {
        val command = commandOverride ?: terminalInput.trim()
        commandHistory.record(command)
        if (command.isBlank()) return
        terminalInput = ""
        terminalText =
                appendTerminalOutput(
                        terminalText,
                        "$terminalPromptText ${redactTerminalCommandForDisplay(command)}"
                )
        discardEditorStateForTerminalDelete(command)
        coroutineScope.launch {
            val result =
                    withContext(Dispatchers.IO) {
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
            if (result.workspaceChanged || result.openFilePath != null) {
                val safeTerminalPath =
                        validDirectoryOrRoot(rootDirectory, terminalDirectoryPath).absolutePath
                terminalDirectoryPath = safeTerminalPath
                val openPath = result.openFilePath
                refreshFiles(
                        selectPath = openPath,
                        workingPath = openPath?.let { File(it).parent } ?: safeTerminalPath,
                )
                if (openPath != null) fullScreenMode = WebFullScreenMode.Editor
            }
            if (result.openPreview) {
                previewDestinationDialogOpen = true
            }
            result.previewLink?.let { url ->
                inAppLivePreview = InAppLivePreviewState(title = "Server preview", url = url)
            }
        }
    }

    suspend fun flushPendingEditorEdits() {
        val edits = pendingFileEdits.toMap()
        val jobs = edits.keys.mapNotNull { pendingEditorSaveJobs.remove(it) }
        jobs.forEach { it.cancel() }
        jobs.forEach { it.join() }
        for ((id, edit) in edits) {
            withContext(Dispatchers.IO) { fileStore.save(edit).getOrThrow() }
            if (pendingFileEdits[id] == edit) {
                pendingFileEdits.remove(id)
                pendingEditorTextByFileId.remove(id)
                dirtyFileIds.remove(id)
                files.replaceFile(id) { withContent(content = edit.content, modifiedAt = File(edit.filePath).lastModified()) }
            }
        }
    }


    fun submitAiMessage() {
        val promptText = aiInput.trim()
        if (promptText.isBlank() || aiBusy) return
        val apiKey = userAiApiKey.trim()
        val requestModel = selectedAiModel.copy(id = aiModelOverride ?: selectedAiModel.id,
            endpoint = aiEndpointOverride ?: selectedAiModel.defaultEndpoint)
        if (apiKey.isBlank() && selectedAiModel.label != "Custom / Ollama") {
            aiError = "Add this provider's API key in AI settings."; return
        }
        val endpointCheck = runCatching { validatedAiEndpoint(requestModel.endpoint, requestModel.provider == AiProvider.AzureOpenAi) }
        if (endpointCheck.isFailure) { aiError = endpointCheck.exceptionOrNull()?.message; return }
        val contextFiles = files.withPendingEditorTexts(pendingEditorTextByFileId)
        val contextActive = activeFile?.withPendingEditorText(pendingEditorTextByFileId)
        val terminalSnapshot = terminalText
        val conversation = aiMessages.takeLast(12).joinToString("\n\n") { "${it.role}: ${it.text.take(4000)}" }
        aiActionTargetPath = contextActive?.filePath
        aiPendingAction = null
        aiBusy = true
        aiInput = ""
        aiError = null
        aiVerificationMessage = null
        aiMessages += AiChatMessage(AiChatRole.User, promptText)
        coroutineScope.launch {
            aiBusy = true
            val result =
                    withContext(Dispatchers.IO) {
                        runCatching {
                            val fullPrompt =
                                    buildAiCopilotPrompt(
                                            rootDirectory = rootDirectory,
                                            files =
                                                    contextFiles,
                                            activeFile =
                                                    contextActive,
                                            userPrompt = "Recent conversation:\n$conversation\n\nCurrent request:\n$promptText",
                                            terminalText = terminalSnapshot,
                                    )
                            requestPersonalAiCompletion(requestModel, fullPrompt, apiKey)
                        }
                    }
            result
                    .onSuccess { response ->
                        aiMessages += AiChatMessage(AiChatRole.Assistant, response)
                        aiPendingAction =
                                suggestedActionFromAiText(
                                        text = response,
                                        activeFile =
                                                contextActive,
                                )
                    }
                    .onFailure { error -> aiError = friendlyAiError(error.message.orEmpty()) }
            aiBusy = false
        }
    }

    fun applyAiAction(action: AiSuggestedAction) {
        if (aiBusy) return
        aiVerificationMessage = null
        if (action is AiSuggestedAction.RunTerminalCommand) {
            fullScreenMode = WebFullScreenMode.Terminal
            submitTerminalCommand(action.command)
            aiVerificationMessage = "Started suggested terminal command."
            aiPendingAction = null
            return
        }
        val changes = when (action) {
            is AiSuggestedAction.ApplyProjectFileChanges -> action.changes
            is AiSuggestedAction.ReplaceActiveFile -> {
                val path = aiActionTargetPath ?: activeFile?.filePath
                if (path == null) { aiError = "Open a file before applying this edit."; return }
                listOf(AiProjectFileChange(AiProjectFileOperation.Write, relativeWorkspacePath(rootDirectory, File(path)), action.code))
            }
            else -> return
        }
        aiBusy = true
        aiApplying = true
        coroutineScope.launch {
            try {
                flushPendingEditorEdits()
                val result = withContext(Dispatchers.IO) {
                    val targets = resolveAiEditTargets(rootDirectory, changes)
                    val before = AiFileUndo.capture(targets)
                    try {
                        val message = applyAiProjectFileChanges(rootDirectory, changes, fileStore)
                        Triple(message, AiFileUndo.completed(before), targets)
                    } catch (error: Exception) {
                        runCatching { AiFileUndo.completed(before).undo() }.onFailure(error::addSuppressed)
                        throw error
                    }
                }
                aiUndoChange = result.second
                result.third.forEach { file ->
                    files.firstOrNull { samePath(it.filePath, file.path) }?.let { discardEditorState(it.id) }
                    editorSessions.invalidate(file.path)
                    ProjectFileCache.invalidate(file)
                }
                refreshFiles(workingPath = workingDirectoryPath)
                aiVerificationMessage = result.first
                aiMessages += AiChatMessage(AiChatRole.Assistant, result.first, result.third.filter { it.exists() }.map { it.path })
                aiPendingAction = null
            } catch (error: Exception) { aiError = error.message ?: "Could not apply AI changes." }
            finally { aiBusy = false; aiApplying = false }
        }
    }

    fun undoAiChanges() {
        val change = aiUndoChange ?: return
        if (aiBusy) return
        aiBusy = true
        aiApplying = true
        coroutineScope.launch {
            try {
                flushPendingEditorEdits()
                withContext(Dispatchers.IO) { change.undo(fileStore) }
                change.paths.forEach { path ->
                    files.firstOrNull { samePath(it.filePath, path) }?.let { discardEditorState(it.id) }
                    editorSessions.invalidate(path)
                    ProjectFileCache.invalidate(File(path))
                }
                refreshFiles(workingPath = workingDirectoryPath)
                aiUndoChange = null
                aiVerificationMessage = "Undid the last AI file changes."
            } catch (error: Exception) { aiError = error.message ?: "Could not undo AI changes." }
            finally { aiBusy = false; aiApplying = false }
        }
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
            browserPreviewReturnPending,
    ) {
        delay(2000L) // Debounce session saving to reduce IO overhead
        saveCurrentWebIdeSession(persistTerminalText = browserPreviewReturnPending)
    }

    PocketCodedWebTheme(darkTheme = ideTheme.isDark, themeName = ideTheme.name, dynamicColor = false) {
    androidx.compose.runtime.CompositionLocalProvider(LocalEditorSessions provides editorSessions,
        LocalApiTester provides apiTester,
        LocalIdeToolColors provides IdeToolColors(
            surface = palette.elevatedPanel, panel = palette.panel, codeBackground = palette.editorBackground,
            selected = palette.selectedTab, text = palette.text, mutedText = palette.mutedText,
            border = palette.border, accent = palette.accent, onAccent = palette.text,
            error = palette.error, success = palette.success, warning = palette.warning,
            run = palette.run, onRun = palette.runText),
        LocalWebDevelopmentActions provides WebDevelopmentActions(
            fileStore = fileStore,
            development = development,
            source = { path, line ->
                val target = runCatching { File(path).canonicalFile }.getOrNull()
                if (target == null || !isSameOrInside(rootDirectory, target) || !target.isFile || target == rootDirectory) {
                    development.error = "Source is outside this project or unavailable."
                } else {
                    development.sourceJump = WebSourceJump(target.path, line.coerceAtLeast(1))
                    files.firstOrNull { samePath(it.filePath, target.path) }?.let { selectFile(it.id) }
                        ?: refreshFiles(selectPath = target.path, workingPath = target.parent)
                    development.panel = null
                    fullScreenMode = WebFullScreenMode.Editor
                }
            },
            startDevelopment = { mode, path, testPaths ->
                development.panel = when (mode) { "debug" -> "Debugger"; "profile" -> "Profiler"; else -> "Tests" }
                if (!development.starting) {
                    development.starting = true
                    development.error = null
                    val snapshot = pendingFileEdits.values.toList()
                    coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) { runCatching {
                            check(!NodeSessionRuntime.isRunning()) { "Stop the current Node session before starting another run." }
                            snapshot.forEach { fileStore.save(it).getOrThrow() }
                            val target = File(path).canonicalFile
                            require(target.extension.lowercase() in setOf("js", "cjs", "mjs")) { "Select a JavaScript .js, .cjs or .mjs file." }
                            NodeSessionRuntime.start(rootDirectory, rootDirectory, target, emptyList(), mode = mode,
                                breakpoints = development.breakpointList(), testFiles = testPaths.map(::File))
                        } }
                        result.onSuccess { shell ->
                            development.error = shell.error.takeIf { it.isNotBlank() }
                            terminalText = appendTerminalOutput(terminalText, appendShellResult(shell))
                        }.onFailure { development.error = it.message }
                        development.starting = false
                    }
                }
            },
            recover = { path, done ->
                files.firstOrNull { samePath(it.filePath, path) }?.let { file ->
                    pendingEditorSaveJobs.remove(file.id)?.cancel()
                }
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) { fileStore.recoverDraft(path) }
                    result.onSuccess {
                        files.firstOrNull { samePath(it.filePath, path) }?.let { file ->
                            pendingFileEdits.remove(file.id)
                            pendingEditorTextByFileId.remove(file.id)
                            dirtyFileIds.remove(file.id)
                        }
                        editorSessions.invalidate(path)
                        ProjectFileCache.invalidate(File(path))
                        refreshFiles(selectPath = path, workingPath = File(path).parent)
                    }
                    done(result.exceptionOrNull()?.message)
                }
            },
            discardRecovery = { path, done ->
                val file = files.firstOrNull { samePath(it.filePath, path) }
                val pending = file?.let { pendingEditorTextByFileId[it.id] }
                file?.let { pendingEditorSaveJobs.remove(it.id)?.cancel() }
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) { runCatching {
                        fileStore.discardDraft(path).getOrThrow()
                        if (pending != null) fileStore.save(path, pending).getOrThrow()
                    } }
                    if (result.isSuccess && file != null && pending != null && pendingEditorTextByFileId[file.id] == pending) {
                        pendingEditorTextByFileId.remove(file.id)
                        pendingFileEdits.remove(file.id)
                        dirtyFileIds.remove(file.id)
                        files.replaceFile(file.id) { withContent(content = pending, modifiedAt = System.currentTimeMillis()) }
                        ProjectFileCache.invalidate(File(path))
                    }
                    done(result.exceptionOrNull()?.message)
                }
            },
            clearOutput = { terminalText = "" },
            history = commandHistory.entries,
            recall = { terminalInput = it },
            scripts = { runCatching {
                val scripts = JSONObject(File(rootDirectory, "package.json").readText()).optJSONObject("scripts")
                scripts?.keys()?.asSequence()?.toList().orEmpty()
            }.getOrDefault(emptyList()) },
            command = { command ->
                val snapshot = pendingFileEdits.values.toList()
                fullScreenMode = WebFullScreenMode.Terminal
                coroutineScope.launch {
                    val saved = withContext(Dispatchers.IO) {
                        runCatching { snapshot.forEach { fileStore.save(it).getOrThrow() } }
                    }
                    saved.onSuccess { submitTerminalCommand(command) }
                        .onFailure { terminalText = appendTerminalOutput(terminalText, "Save failed: ${it.message}") }
                }
            },
            preview = {
                android.webkit.WebView.setWebContentsDebuggingEnabled(true)
                runActiveFile()
            },
            prepareSearch = { withContext(kotlinx.coroutines.NonCancellable) { flushPendingEditorEdits() } },
            filesChanged = { changed ->
                changed.forEach { file ->
                    ProjectFileCache.invalidate(file)
                    editorSessions.invalidate(file.path)
                }
                if (changed.isNotEmpty()) refreshFiles(workingPath = workingDirectoryPath)
            },
            refreshHistory = { commandHistory.refresh() },
        )) {
    WebDevelopmentPanels(development)
    if (aiApplying) AlertDialog(
        onDismissRequest = {}, title = { Text("Updating project files") },
        text = { Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp)); Text("Saving AI file changes...")
        } }, confirmButton = {},
    )
    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    Brush.verticalGradient(
                                            listOf(palette.backgroundTop, palette.backgroundBottom)
                                    )
                            )
                            .systemBarsPadding(),
    ) {
        when (fullScreenMode) {
            WebFullScreenMode.Explorer -> Column(Modifier.fillMaxSize().imePadding().padding(10.dp)) {
                ExplorerPanel(
                    files = files, activeFileId = activeFileId,
                    rootDirectory = rootDirectory, workingDirectory = workingDirectory,
                    search = fileSearch, expandedFolderPaths = expandedFolderPaths,
                    palette = palette, modifier = Modifier.fillMaxSize(), directoryVersion = directoryVersion,
                    expanded = true, onFullScreen = { fullScreenMode = null },
                    onSearchChange = { fileSearch = it },
                    onToggleFolder = { folder ->
                        if (expandedFolderPaths.any { samePath(it, folder.path) })
                            expandedFolderPaths.removeAll { samePath(it, folder.path) }
                        else expandedFolderPaths.addUniquePath(folder.path)
                    },
                    onSelectFolder = ::selectFolder, onSelectFile = { selectFile(it); fullScreenMode = WebFullScreenMode.Editor },
                    onNewFile = { newFileDialogOpen = true }, onNewFolder = { newFolderDialogOpen = true },
                    onRenameFile = { renameFileTarget = it }, onRenameFolder = { renameFolderTarget = it },
                    onDeleteFile = { deleteFileTarget = it }, onDeleteFolder = { deleteFolderTarget = it },
                )
            }
            WebFullScreenMode.Editor -> {
                val currentFile = activeFile
                if (currentFile == null) {
                    EmptyEditorFullScreen(
                            files = tabFiles,
                            palette = palette,
                            onSelectFile = ::selectFile,
                            onAddFile = { newFileDialogOpen = true },
                            onDeleteFile = { fileId ->
                                files.firstOrNull { it.id == fileId }?.let { deleteFileTarget = it }
                            },
                            onBack = { fullScreenMode = null },
                    )
                } else {
                    WebEditorFullScreen(
                            files = tabFiles,
                            activeFile = currentFile,
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
                            onRunPreview = ::runActiveFile,
                            onBack = { fullScreenMode = null },
                    )
                }
            }
            WebFullScreenMode.Terminal ->
                    WebTerminalFullScreen(
                            palette = palette,
                            darkMode = ideTheme.isDark,
                            nextThemeName = ideTheme.next().displayName,
                            onToggleTheme = onToggleTheme,
                            promptText = terminalPromptText,
                            text = terminalText,
                            input = terminalInput,
                            onInputChange = { terminalInput = it },
                            onSend = { submitTerminalCommand() },
                            onBack = { fullScreenMode = null },
                    )
            WebFullScreenMode.Ai ->
                    AiChatFullScreen(
                            palette = palette,
                            modelOptions = aiModelOptions,
                            selectedModel = selectedAiModel,
                            onSelectedModel = { if (!aiBusy) selectedAiModelId = it.id },
                            messages = aiMessages,
                            input = aiInput,
                            onInputChange = { aiInput = it },
                            userAiApiKey = userAiApiKey,
                            endpointOverride = aiEndpointOverride, modelOverride = aiModelOverride,
                            onSaveSettings = { key, endpoint, model ->
                                aiConnections.save(selectedAiModel.connectionKey, selectedAiModel.id, key, endpoint, model)
                                userAiApiKey = key.orEmpty(); aiEndpointOverride = endpoint; aiModelOverride = model
                            },
                            onClearChat = { aiMessages.clear(); aiPendingAction = null; aiError = null; aiVerificationMessage = null },
                            canUndo = aiUndoChange != null, onUndo = ::undoAiChanges,
                            busy = aiBusy,
                            error = aiError,
                            pendingAction = aiPendingAction,
                            verificationMessage = aiVerificationMessage,
                            onSend = ::submitAiMessage,
                            onApplyAction = ::applyAiAction,
                            onDismissAction = { aiPendingAction = null },
                            onBack = { fullScreenMode = null },
                    )
            null ->
                    WebIdeHomeScreen(
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
                            terminalRevealRequest = terminalRevealRequest,
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
                            onRunPreview = ::runActiveFile,
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

        inAppLivePreview?.let { preview ->
            InAppLivePreviewScreen(
                    preview = preview,
                    palette = palette,
                    onClose = { inAppLivePreview = null },
                    onOpenExternal = { url ->
                        inAppLivePreview = null
                        browserPreviewReturnPending = true
                        terminalText =
                                appendTerminalOutput(
                                        terminalText,
                                        "Opening in your device browser: $url",
                                )
                        prepareBrowserPreviewHandoff()
                        val launchError = openUrlInDefaultBrowser(context, url)
                        if (launchError != null) {
                            browserPreviewReturnPending = false
                            terminalText =
                                    appendTerminalOutput(
                                            terminalText,
                                            "Could not open the device browser: $launchError",
                                    )
                        }
                    },
                    onLog = { message ->
                        terminalText = appendTerminalOutput(terminalText, message)
                    },
            )
        }
    }

    if (previewDestinationDialogOpen) {
        PreviewDestinationDialog(
                palette = palette,
                serverUrl = NodeSessionRuntime.previewUrl(),
                busy = previewLaunchBusy,
                onDismiss = { if (!previewLaunchBusy) previewDestinationDialogOpen = false },
                onOpenInApp = { runWebPreview(WebPreviewDestination.InApp) },
                onOpenBrowser = { runWebPreview(WebPreviewDestination.Browser) },
                onOpenLink = { url ->
                    previewDestinationDialogOpen = false
                    inAppLivePreview = InAppLivePreviewState(title = "Server preview", url = url)
                },
        )
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
                onConfirm = { name ->
                    createFile(name)?.also {
                        return@NameInputDialog it
                    }
                    newFileDialogOpen = false
                    null
                },
        )
    }
    if (newFolderDialogOpen) {
        NameInputDialog(
                title = "New folder",
                label = "Folder name",
                initialValue = "components",
                palette = palette,
                onDismiss = { newFolderDialogOpen = false },
                onConfirm = { name ->
                    createFolder(name)?.also {
                        return@NameInputDialog it
                    }
                    newFolderDialogOpen = false
                    null
                },
        )
    }
    renameFileTarget?.let { file ->
        NameInputDialog(
                title = "Rename file",
                label = "File name",
                initialValue = file.name,
                palette = palette,
                onDismiss = { renameFileTarget = null },
                onConfirm = { name ->
                    renameFile(file, name)?.also {
                        return@NameInputDialog it
                    }
                    renameFileTarget = null
                    null
                },
        )
    }
    renameFolderTarget?.let { folder ->
        NameInputDialog(
                title = "Rename folder",
                label = "Folder name",
                initialValue = folder.name,
                palette = palette,
                onDismiss = { renameFolderTarget = null },
                onConfirm = { name ->
                    renameFolder(folder, name)?.also {
                        return@NameInputDialog it
                    }
                    renameFolderTarget = null
                    null
                },
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
}
}

private fun configureFirebaseAppCheck(context: Context) {
    FirebaseApp.initializeApp(context)
    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(appCheckProviderFactory())
}

@Composable
private fun PreviewDestinationDialog(
        palette: WebPalette,
        serverUrl: String?,
        busy: Boolean,
        onDismiss: () -> Unit,
        onOpenInApp: () -> Unit,
        onOpenBrowser: () -> Unit,
        onOpenLink: (String) -> Unit,
) {
    var link by remember { mutableStateOf(serverUrl.orEmpty()) }
    val normalizedLink = normalizePreviewLink(link)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
                color = palette.panel,
                contentColor = palette.text,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.strongBorder.copy(alpha = 0.65f)),
                tonalElevation = 0.dp,
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(18.dp),
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                            modifier =
                                    Modifier.size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(palette.accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Visibility,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(23.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = "Open preview",
                                color = palette.text,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                                text =
                                        if (busy) "Starting server, please wait..."
                                        else "Choose a preview target",
                                color = palette.mutedText,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Button(
                        onClick = onOpenInApp,
                        enabled = !busy,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = palette.run,
                                        contentColor = palette.runText,
                                ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                            text = "In-app Live Preview",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                        onClick = onOpenBrowser,
                        enabled = !busy,
                        border = BorderStroke(1.dp, palette.border),
                        colors =
                                ButtonDefaults.outlinedButtonColors(
                                        containerColor = palette.tab,
                                        contentColor = palette.text,
                                ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = palette.text,
                            modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                            text = if (busy) "Starting localhost..." else "Browser localhost",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("Preview URL") },
                        placeholder = { Text("http://localhost:3000") },
                        singleLine = true,
                        isError = link.isNotBlank() && normalizedLink == null,
                        modifier = Modifier.fillMaxWidth(),
                    colors = readableFieldColors(),
                )
                Text(
                        text =
                                "Open a running frontend or backend server. For a computer, use its network address; localhost refers to this phone. Opening a link does not start the server.",
                        color = palette.mutedText,
                        fontSize = 12.sp,
                )
                OutlinedButton(
                        onClick = { normalizedLink?.let(onOpenLink) },
                        enabled = normalizedLink != null && !busy,
                        modifier = Modifier.fillMaxWidth(),
                    colors = readableOutlinedButtonColors(),
                ) { Text("Open link") }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun InAppLivePreviewScreen(
        preview: InAppLivePreviewState,
        palette: WebPalette,
        onClose: () -> Unit,
        onOpenExternal: (String) -> Unit,
        onLog: (String) -> Unit,
) {
    var webView by remember(preview.url) { mutableStateOf<WebView?>(null) }
    val previewLifecycle = (LocalContext.current as? androidx.lifecycle.LifecycleOwner)?.lifecycle
    var webViewLifecycle by remember(preview.url) { mutableStateOf<PreviewWebViewLifecycle?>(null) }
    var currentUrl by remember(preview.url) { mutableStateOf(preview.url) }
    var currentTitle by remember(preview.url) { mutableStateOf(preview.title) }
    var progress by remember(preview.url) { mutableStateOf(0) }
    var canGoBack by remember(preview.url) { mutableStateOf(false) }
    var canGoForward by remember(preview.url) { mutableStateOf(false) }
    var lastReportedProgress by remember(preview.url) { mutableIntStateOf(-1) }

    fun updateNavigationState(view: WebView) {
        val nextCanGoBack = view.canGoBack()
        val nextCanGoForward = view.canGoForward()
        val nextUrl = view.url.orEmpty().ifBlank { currentUrl }
        val nextTitle = view.title.orEmpty().ifBlank { currentTitle.ifBlank { preview.title } }
        if (canGoBack != nextCanGoBack) canGoBack = nextCanGoBack
        if (canGoForward != nextCanGoForward) canGoForward = nextCanGoForward
        if (currentUrl != nextUrl) currentUrl = nextUrl
        if (currentTitle != nextTitle) currentTitle = nextTitle
    }

    fun reportProgress(value: Int) {
        val next = value.coerceIn(0, 100)
        if (next == 100 || next == 0 || kotlin.math.abs(next - lastReportedProgress) >= 5) {
            lastReportedProgress = next
            progress = next
        }
    }

    BackHandler(enabled = true) {
        val view = webView
        if (view?.canGoBack() == true) {
            view.goBack()
            updateNavigationState(view)
        } else {
            onClose()
        }
    }

    DisposableEffect(preview.url) {
        onDispose {
            webViewLifecycle?.close()
            webViewLifecycle = null
            webView?.stopLoading()
            webView?.destroy()
            webView = null
        }
    }

    Surface(
            color = palette.backgroundBottom,
            modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(palette.panel)
                                    .border(BorderStroke(1.dp, palette.border))
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(
                            onClick = {
                                webView?.goBack()
                                webView?.let(::updateNavigationState)
                            },
                            enabled = canGoBack,
                            modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = if (canGoBack) palette.text else palette.subtleText,
                        )
                    }
                    IconButton(
                            onClick = {
                                webView?.goForward()
                                webView?.let(::updateNavigationState)
                            },
                            enabled = canGoForward,
                            modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Forward",
                                tint = if (canGoForward) palette.text else palette.subtleText,
                        )
                    }
                    IconButton(
                            onClick = { webView?.reload() },
                            modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Reload",
                                tint = palette.text,
                        )
                    }
                    Surface(
                            color = palette.elevatedPanel,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, palette.border),
                            modifier = Modifier.weight(1f).height(38.dp),
                    ) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                    text = currentUrl,
                                    color = palette.mutedText,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(
                            onClick = { onOpenExternal(currentUrl.ifBlank { preview.url }) },
                            modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = "Open in browser",
                                tint = palette.text,
                        )
                    }
                    IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close preview",
                                tint = palette.text,
                        )
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                        text = currentTitle.ifBlank { preview.title },
                        color = palette.text,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp),
                )
                Spacer(Modifier.height(6.dp))
                Box(
                        modifier = Modifier.fillMaxWidth().height(2.dp).background(palette.border),
                ) {
                    if (progress in 1..99) {
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth(progress / 100f)
                                                .fillMaxHeight()
                                                .background(palette.accent),
                        )
                    }
                }
            }

            key(preview.url) {
                AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webView = this
                                previewLifecycle?.let { webViewLifecycle = PreviewWebViewLifecycle(this, it) }
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                settings.allowContentAccess = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                webViewClient =
                                        object : WebViewClient() {
                                            override fun onPageFinished(
                                                    view: WebView?,
                                                    url: String?
                                            ) {
                                                view ?: return
                                                currentUrl = url.orEmpty().ifBlank { preview.url }
                                                reportProgress(100)
                                                updateNavigationState(view)
                                            }

                                            override fun onReceivedError(
                                                    view: WebView?,
                                                    request: WebResourceRequest?,
                                                    error: WebResourceError?,
                                            ) {
                                                if (request?.isForMainFrame == true) {
                                                    onLog(
                                                            formatPreviewRuntimeMessage(
                                                                    level = "error",
                                                                    message =
                                                                            error?.description
                                                                                    ?.toString()
                                                                                    ?: "Page load failed.",
                                                                    source =
                                                                            request.url
                                                                                    ?.toString()
                                                                                    .orEmpty()
                                                                                    .ifBlank {
                                                                                        "in-app preview"
                                                                                    },
                                                                    line = 0,
                                                                    column = 0,
                                                            ),
                                                    )
                                                }
                                            }
                                        }
                                webChromeClient =
                                        object : WebChromeClient() {
                                            override fun onProgressChanged(
                                                    view: WebView?,
                                                    newProgress: Int
                                            ) {
                                                reportProgress(newProgress)
                                                view?.let(::updateNavigationState)
                                            }

                                            override fun onReceivedTitle(
                                                    view: WebView?,
                                                    title: String?
                                            ) {
                                                if (!title.isNullOrBlank()) {
                                                    currentTitle = title
                                                }
                                                view?.let(::updateNavigationState)
                                            }
                                        }
                                loadUrl(preview.url)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WebIdeHomeScreen(
        files: List<WebFile>,
        tabFiles: List<WebFile>,
        activeFile: WebFile?,
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
        terminalRevealRequest: Long,
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
            modifier = Modifier.fillMaxSize().imePadding().padding(horizontal = 14.dp),
    ) {
        val pageScroll = rememberScrollState()
        LaunchedEffect(terminalRevealRequest) {
            if (terminalRevealRequest > 0L) {
                delay(80)
                pageScroll.animateScrollTo(pageScroll.maxValue)
            }
        }
        val showExplorer = explorerVisible && LocalApiTester.current?.open != true
        val editorHeight =
                when {
                    maxHeight < 740.dp -> 310.dp
                    showExplorer -> 390.dp
                    else -> 430.dp
                }
        val terminalHeight = if (maxHeight < 740.dp) 190.dp else 220.dp
        var explorerFraction by rememberSaveable { mutableFloatStateOf(0.50f) }
        val layoutWidthPx = with(LocalDensity.current) { maxWidth.toPx() }.coerceAtLeast(1f)

        Column(
                modifier = Modifier.fillMaxSize().verticalScroll(pageScroll),
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
                    activeFileId = activeFile?.id ?: -1L,
                    palette = palette,
                    onSelectFile = onSelectFile,
                    onAddFile = onNewFile,
                    onDeleteFile = { fileId ->
                        files.firstOrNull { it.id == fileId }?.let(onDeleteFile)
                    },
                    onToggleExplorer = onToggleExplorer,
            )
            Spacer(Modifier.height(10.dp))

            if (showExplorer) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExplorerPanel(
                            files = files,
                            activeFileId = activeFile?.id ?: -1L,
                            rootDirectory = rootDirectory,
                            workingDirectory = workingDirectory,
                            search = fileSearch,
                            expandedFolderPaths = expandedFolderPaths,
                            palette = palette,
                            modifier = Modifier.weight(explorerFraction).height(editorHeight),
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
                            onFullScreen = { onFullScreen(WebFullScreenMode.Explorer) },
                    )
                    ResizeHandle(
                            palette = palette,
                            height = editorHeight,
                            onToggle = {
                                explorerFraction = if (explorerFraction > 0.50f) 0.35f else 0.62f
                            },
                            onDrag = { dragAmount ->
                                explorerFraction =
                                        (explorerFraction + dragAmount / layoutWidthPx).coerceIn(
                                                0.28f,
                                                0.72f
                                        )
                            },
                    )
                    if (activeFile == null) {
                        EmptyEditorPanel(
                                palette = palette,
                                modifier = Modifier.weight(1f - explorerFraction),
                                height = editorHeight,
                                onNewFile = onNewFile,
                        )
                    } else {
                        WebCodeEditorPanel(
                                file = activeFile,
                                palette = palette,
                                modifier = Modifier.weight(1f - explorerFraction),
                                height = editorHeight,
                                compact = true,
                                darkMode = darkMode, nextThemeName = nextThemeName, onToggleTheme = onToggleTheme,
                                onCodeChange = onCodeChange,
                                onRunPreview = onRunPreview,
                                onFullScreen = { onFullScreen(WebFullScreenMode.Editor) },
                        )
                    }
                }
            } else {
                if (activeFile == null) {
                    EmptyEditorPanel(
                            palette = palette,
                            modifier = Modifier.fillMaxWidth(),
                            height = editorHeight,
                            onNewFile = onNewFile,
                    )
                } else {
                    WebCodeEditorPanel(
                            file = activeFile,
                            palette = palette,
                            modifier = Modifier.fillMaxWidth(),
                            height = editorHeight,
                            compact = false,
                            darkMode = darkMode, nextThemeName = nextThemeName, onToggleTheme = onToggleTheme,
                            onCodeChange = onCodeChange,
                            onRunPreview = onRunPreview,
                            onFullScreen = { onFullScreen(WebFullScreenMode.Editor) },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            RunPreviewButton(
                    palette = palette,
                    label = runButtonLabelFor(activeFile),
                    preview = activeFile?.let { isLivePreviewFile(it.name) } == true,
                    enabled = activeFile != null,
                    onClick = onRunPreview,
            )
            Spacer(Modifier.height(12.dp))
            WebTerminalPanel(
                    palette = palette,
                    promptText = terminalPromptText,
                    text = terminalText,
                    input = terminalInput,
                    height = terminalHeight,
                    darkMode = darkMode, nextThemeName = nextThemeName, onToggleTheme = onToggleTheme,
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
        Box(
                modifier =
                        Modifier.size(40.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(palette.elevatedPanel)
                                .border(
                                        BorderStroke(1.dp, palette.border),
                                        RoundedCornerShape(11.dp)
                                )
                                .clickable(onClick = onBackToProjects),
                contentAlignment = Alignment.Center,
        ) {
            Icon(
                    painter = painterResource(R.drawable.mit_folder),
                    contentDescription = "Back to projects",
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = "IDE",
                    color = palette.text,
                    fontSize = 22.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
            )
            Text(
                    text = projectName,
                    color = palette.subtleText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(6.dp))
        HeaderActionButton(
                Icons.Rounded.FolderOpen,
                "Import or save files and folders",
                palette.text,
                palette,
                onFileActions
        )
        Spacer(Modifier.width(6.dp))
        HeaderActionButton(
                Icons.Rounded.AutoAwesome,
                "Open AI chat",
                palette.accent,
                palette,
                onOpenAi
        )
        Spacer(Modifier.width(6.dp))
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
            modifier =
                    modifier.clip(RoundedCornerShape(11.dp))
                            .background(buttonColor)
                            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(11.dp))
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
            modifier =
                    Modifier.width(16.dp)
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
internal fun ExplorerPanel(
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
        expanded: Boolean = false,
        onFullScreen: (() -> Unit)? = null,
) {
    var addMenuOpen by remember { mutableStateOf(false) }
    var searchAndReplace by rememberSaveable { mutableStateOf(false) }

    Panel(
            palette = palette,
            modifier = modifier,
    ) panelContent@{
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
                        containerColor = palette.elevatedPanel,
                ) {
                    DropdownMenuItem(colors = readableMenuItemColors(),
                            text = { Text("Create file", color = palette.text) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Description, null, tint = palette.accent)
                            },
                            onClick = {
                                addMenuOpen = false
                                onNewFile()
                            },
                    )
                    DropdownMenuItem(colors = readableMenuItemColors(),
                            text = { Text("Create folder", color = palette.text) },
                            leadingIcon = {
                                Icon(
                                        painterResource(R.drawable.mit_folder),
                                        null,
                                        tint = palette.accent
                                )
                            },
                            onClick = {
                                addMenuOpen = false
                                onNewFolder()
                            },
                    )
                }
            }
            onFullScreen?.let { toggle ->
                IconButton(onClick = toggle, modifier = Modifier.size(34.dp)) {
                    Icon(if (expanded) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                        contentDescription = if (expanded) "Minimize explorer" else "Fullscreen explorer",
                        tint = palette.text, modifier = Modifier.size(22.dp))
                }
            }
        }
        if (expanded) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { searchAndReplace = false }, modifier = Modifier.weight(1f), colors = readableTextButtonColors()) {
                    Text("Files", color = if (!searchAndReplace) palette.accent else palette.mutedText)
                }
                TextButton(onClick = { searchAndReplace = true }, modifier = Modifier.weight(1f), colors = readableTextButtonColors()) {
                    Text("Search & Replace", color = if (searchAndReplace) palette.accent else palette.mutedText)
                }
            }
            if (searchAndReplace) {
                WebProjectSearchReplace(rootDirectory, workingDirectory,
                    files.firstOrNull { it.id == activeFileId }?.let { File(it.filePath) },
                    palette, Modifier.weight(1f))
                return@panelContent
            }
        }
        Spacer(Modifier.height(10.dp))
        SearchBox(
                value = search,
                palette = palette,
                onValueChange = onSearchChange,
        )
        Spacer(Modifier.height(10.dp))

        // Only tree metadata invalidates the scan; typing in a file does not.
        val treeFiles = files.map { WebFile(it.id, it.name, "", it.filePath, 0L) }
        val expandedPaths = expandedFolderPaths.toList()
        val entries by
                produceState<List<ExplorerEntry>>(
                        initialValue = emptyList(),
                        rootDirectory.absolutePath,
                        treeFiles,
                        expandedPaths,
                        search,
                        directoryVersion,
                ) {
                    if (search.isNotBlank()) delay(150)
                    value =
                            withContext(Dispatchers.IO) {
                                buildExplorerEntries(
                                        rootDirectory,
                                        treeFiles,
                                        expandedPaths,
                                        search
                                )
                            }
                }
        val currentFiles = remember(files.toList()) { files.associateBy { it.id } }
        val measurer = androidx.compose.ui.text.rememberTextMeasurer(cacheSize = 512)
        val labelStyle = androidx.compose.material3.LocalTextStyle.current.merge(TextStyle(fontSize = 13.sp))
        val density = LocalDensity.current
        val treeWidth = remember(entries, measurer, labelStyle, density) {
            entries.maxOfOrNull { entry ->
                val (name, fixed) = when (entry) {
                    is ExplorerEntry.Directory -> entry.target.name to (87 + entry.depth * 14).dp
                    is ExplorerEntry.SourceFile -> entry.file.name to (84 + entry.depth * 14).dp
                }
                fixed + with(density) { measurer.measure(name, labelStyle, softWrap = false, maxLines = 1).size.width.toDp() }
            } ?: 0.dp
        }
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val contentWidth = maxOf(maxWidth, treeWidth)
            Box(Modifier.fillMaxSize().horizontalScroll(rememberScrollState())) {
                LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.width(contentWidth).fillMaxHeight(),
                ) {
                    items(
                            items = entries,
                            key = { entry ->
                                when (entry) {
                                    is ExplorerEntry.Directory -> "dir:${entry.target.path}"
                                    is ExplorerEntry.SourceFile -> "file:${entry.file.id}"
                                }
                            }
                    ) { entry ->
                        when (entry) {
                            is ExplorerEntry.Directory ->
                                    ExplorerFolderRow(
                                            folder = entry.target,
                                            depth = entry.depth,
                                            expanded = entry.expanded,
                                            selected =
                                                    samePath(
                                                            entry.target.path,
                                                            workingDirectory.absolutePath
                                                    ),
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
                            is ExplorerEntry.SourceFile ->
                                    ExplorerFileRow(
                                            file = currentFiles[entry.file.id] ?: entry.file,
                                            depth = entry.depth,
                                            selected = entry.file.id == activeFileId,
                                            palette = palette,
                                            onSelect = { onSelectFile(entry.file.id) },
                                            onDelete = {
                                                currentFiles[entry.file.id]?.let(onDeleteFile)
                                            },
                                            onRename = {
                                                currentFiles[entry.file.id]?.let(onRenameFile)
                                            },
                                    )
                        }
                    }
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
            textStyle =
                    TextStyle(
                            color = palette.text,
                            fontSize = 13.sp,
                    ),
            cursorBrush = SolidColor(palette.accent),
            decorationBox = { innerTextField ->
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(palette.panel)
                                        .border(
                                                BorderStroke(1.dp, palette.border),
                                                RoundedCornerShape(11.dp)
                                        )
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
            modifier =
                    Modifier.fillMaxWidth()
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
                    imageVector =
                            if (expanded) Icons.Rounded.KeyboardArrowDown
                            else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = if (expanded) "Collapse folder" else "Expand folder",
                    tint = palette.mutedText,
                    modifier = Modifier.size(17.dp),
            )
        }
        Icon(
                painter =
                        painterResource(
                                if (expanded) R.drawable.mit_folder_open else R.drawable.mit_folder
                        ),
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
                    containerColor = palette.elevatedPanel,
            ) {
                DropdownMenuItem(colors = readableMenuItemColors(),
                        text = { Text("Create file", color = palette.text) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Description, null, tint = palette.accent)
                        },
                        onClick = {
                            menuOpen = false
                            onCreateFile()
                        },
                )
                DropdownMenuItem(colors = readableMenuItemColors(),
                        text = { Text("Create folder", color = palette.text) },
                        leadingIcon = {
                            Icon(
                                    painterResource(R.drawable.mit_folder),
                                    null,
                                    tint = palette.accent
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onCreateFolder()
                        },
                )
                DropdownMenuItem(colors = readableMenuItemColors(),
                        text = { Text("Rename", color = palette.text) },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = palette.text) },
                        onClick = {
                            menuOpen = false
                            onRename()
                        },
                )
                DropdownMenuItem(colors = readableMenuItemColors(),
                        text = { Text("Delete", color = palette.error) },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = palette.error) },
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
            modifier =
                    Modifier.fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selected) palette.selectedTab else Color.Transparent)
                            .clickable(onClick = onSelect)
                            .padding(start = (20 + depth * 14).dp, end = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        WebFileIcon(file.name, palette, Modifier.size(17.dp))
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
                    containerColor = palette.elevatedPanel,
            ) {
                DropdownMenuItem(colors = readableMenuItemColors(),
                        text = { Text("Rename", color = palette.text) },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = palette.text) },
                        onClick = {
                            menuOpen = false
                            onRename()
                        },
                )
                DropdownMenuItem(colors = readableMenuItemColors(),
                        text = { Text("Delete", color = palette.error) },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = palette.error) },
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
    val apiTester = LocalApiTester.current
    if (apiTester?.open == true) {
        Panel(palette, modifier.then(if (height == Dp.Unspecified) Modifier else Modifier.height(height)),
            backgroundColor = palette.editorBackground) {
            ApiTesterContent(apiTester, Modifier.fillMaxSize(), expanded = height == Dp.Unspecified, onExpand = onFullScreen)
        }
        return
    }
    val panelModifier =
            if (height == Dp.Unspecified) {
                modifier
            } else {
                modifier.height(height)
            }

    if (isDatabaseFileName(file.name)) {
        Panel(palette, panelModifier, backgroundColor = palette.editorBackground) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Database", color = palette.text, modifier = Modifier.weight(1f))
                IconButton(onClick = onFullScreen) {
                    Icon(if (showKeyboardAccessory) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                        if (showKeyboardAccessory) "Minimize editor" else "Fullscreen editor", tint = palette.text)
                }
            }
            SQLiteDataPanel(File(file.filePath), palette, Modifier.weight(1f))
        }
        return
    }
    Panel(
            palette = palette,
            modifier = panelModifier,
            backgroundColor = palette.editorBackground,
    ) {
        SmartCodeEditor(
                file = file,
                palette = palette,
                modifier = Modifier.fillMaxWidth().weight(1f),
                showKeyboardAccessory = showKeyboardAccessory,
                header = { tools ->
                    @Composable fun editorActions() {
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
                        tools()
                        IconButton(onClick = onFullScreen, modifier = Modifier.size(34.dp)) {
                            Icon(
                                    imageVector = if (showKeyboardAccessory) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                    contentDescription = if (showKeyboardAccessory) "Minimize editor" else "Fullscreen editor",
                                    tint = palette.text,
                                    modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        WebFileIcon(file.name, palette, Modifier.size(18.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(
                                text = codeEditorTitle(file.name, false),
                                color = palette.text,
                                fontSize = if (compact) 16.sp else 18.sp,
                                lineHeight = 21.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                        )
                        if (!compact) editorActions()
                    }
                    if (compact) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) { editorActions() }
                    Spacer(Modifier.height(8.dp))
                    HorizontalRule(palette)
                    Spacer(Modifier.height(7.dp))
                },
                onCodeChange = onCodeChange,
        )
    }
}

@Composable
private fun HeaderActionButton(
        icon: ImageVector,
        contentDescription: String,
        tint: Color,
        palette: WebPalette,
        onClick: () -> Unit,
) {
    Box(
            modifier =
                    Modifier.size(40.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(palette.elevatedPanel)
                            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(11.dp))
                            .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(20.dp)) }
}

@Composable
private fun EmptyEditorPanel(
        palette: WebPalette,
        modifier: Modifier = Modifier,
        height: Dp = Dp.Unspecified,
        onNewFile: () -> Unit,
) {
    val panelModifier =
            if (height == Dp.Unspecified) {
                modifier
            } else {
                modifier.height(height)
            }

    Panel(
            palette = palette,
            modifier = panelModifier,
    ) {
        Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(34.dp),
                )
                Text(
                        text = "No file open",
                        color = palette.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                )
                Text(
                        text = "Create or import a Web file to start editing.",
                        color = palette.mutedText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                )
                Button(
                        onClick = onNewFile,
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent, disabledContainerColor = palette.tab, disabledContentColor = palette.mutedText),
                        shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("New File")
                }
            }
        }
    }
}

@Composable
internal fun SmartCodeEditor(
        file: WebFile,
        palette: WebPalette,
        modifier: Modifier = Modifier,
        showKeyboardAccessory: Boolean = false,
        header: @Composable (tools: @Composable () -> Unit) -> Unit = { tools -> tools() },
        onCodeChange: (String) -> Unit,
) {
    val usePlatformEditor = remember(file.id) { file.content.length > 100_000 }
    val sessions = LocalEditorSessions.current
    val sessionGeneration = sessions?.generation
    val session = remember(file.filePath, sessions, sessionGeneration) {
        sessions?.open(file.filePath, file.content) ?: EditorSession(file.content)
    }
    var editorValue by remember(session) { mutableStateOf(session.value) }
    var editorFocused by remember(session) { mutableStateOf(false) }
    var followCursor by remember(session) { mutableStateOf(session.followCursor) }
    var caretRevealVersion by remember(session) { mutableIntStateOf(0) }
    fun revealEditedCursor() {
        followCursor = true
        session.followCursor = true
        caretRevealVersion++
    }
    val history = session.history
    val developmentActions = LocalWebDevelopmentActions.current
    val development = developmentActions?.development
    val developmentScope = rememberCoroutineScope()
    val fileBreakpoints = development?.breakpoints?.get(file.filePath).orEmpty()
    fun toggleBreakpoint(line: Int) {
        development?.toggleBreakpoint(file.filePath, line)
        developmentScope.launch(Dispatchers.IO) {
            if (development != null && NodeSessionRuntime.isRunningFor(development.root)) {
                NodeSessionRuntime.debugCommand(development.root, "breakpoints", breakpoints = development.breakpointList())
            }
        }
    }
    DisposableEffect(session) {
        onDispose { session.value = editorValue }
    }
    var completionsVisible by remember(file.id) { mutableStateOf(false) }
    var suppressCompletionsForText by remember(file.id) { mutableStateOf<String?>(null) }
    var diagnostics by remember(file.id) { mutableStateOf<List<CodeDiagnostic>>(emptyList()) }
    var completionResult by remember(file.id) { mutableStateOf(EditorCompletionResult()) }
    var textMetrics by
            remember(file.id) { mutableStateOf(editorTextMetrics(file.content, cap = 1_600)) }
    LaunchedEffect(file.id, file.content) {
        if (file.content != editorValue.text) {
            val cursor = min(editorValue.selection.start, file.content.length).coerceAtLeast(0)
            val updated = TextFieldValue(file.content, selection = TextRange(cursor))
            history.record(editorValue, updated)
            editorValue = updated
            session.value = updated
            completionResult = EditorCompletionResult()
            completionsVisible = false
        }
    }
    LaunchedEffect(file.filePath, file.name, editorValue.text, editorValue.selection) {
        val snapshot = editorValue.text
        val cursor = editorValue.selection.start
        val fileName = file.name
        val fileOnDisk = File(file.filePath)
        delay(LSP_COMPLETION_DEBOUNCE_MS)
        val result =
                withContext(Dispatchers.Default) {
                    WebLanguageServices.completion(fileOnDisk, fileName, snapshot, cursor)
                }
        if (snapshot == editorValue.text && cursor == editorValue.selection.start) {
            completionResult = result
            completionsVisible =
                    result.context != null &&
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
    LaunchedEffect(file.filePath, file.name, editorValue.text) {
        val snapshot = editorValue.text
        delay(
                when {
                    snapshot.length > LIVE_COLOR_RETARGET_RADIUS -> LARGE_FILE_HIGHLIGHT_IDLE_MS
                    snapshot.length > RESPONSIVE_HIGHLIGHT_CHAR_LIMIT ->
                            DIAGNOSTIC_IDLE_DELAY_MS * 2
                    else -> DIAGNOSTIC_IDLE_DELAY_MS
                }
        )
        val fileOnDisk = File(file.filePath)
        val inspected =
                withContext(Dispatchers.Default) {
                    if (snapshot.length <= LIVE_DIAGNOSTIC_CHAR_LIMIT &&
                                    snapshot.length <= RESPONSIVE_DIAGNOSTIC_CHAR_LIMIT
                    ) {
                        WebLanguageServices.diagnostics(fileOnDisk, file.name, snapshot)
                    } else {
                        emptyList()
                    }
                }
        if (snapshot == editorValue.text) {
            diagnostics = inspected
        }
    }
    LaunchedEffect(editorValue.text) {
        val snapshot = editorValue.text
        delay(EDITOR_METRICS_IDLE_DELAY_MS)
        val metrics = withContext(Dispatchers.Default) { editorTextMetrics(snapshot, cap = 1_600) }
        if (snapshot == editorValue.text) {
            textMetrics = metrics
        }
    }
    val editorVerticalScroll = key(file.id) { rememberScrollState(session.verticalScroll) }
    val editorHorizontalScroll = key(file.id) { rememberScrollState(session.horizontalScroll) }
    DisposableEffect(session, editorVerticalScroll, editorHorizontalScroll) {
        onDispose {
            if (!usePlatformEditor) {
                session.verticalScroll = editorVerticalScroll.value
                session.horizontalScroll = editorHorizontalScroll.value
            }
        }
    }
    val density = LocalDensity.current
    var editorLayout by remember(file.id) { mutableStateOf<TextLayoutResult?>(null) }
    val onGutterTap by rememberUpdatedState<(Offset) -> Unit>({ tap ->
        editorLayout?.takeIf { it.layoutInput.text.text == editorValue.text }?.let { layout ->
            val sourceY = tap.y + editorVerticalScroll.value
            if (layout.lineCount > 0 && sourceY >= 0f && sourceY < layout.getLineBottom(layout.lineCount - 1)) {
                toggleBreakpoint(layout.getLineForVerticalPosition(sourceY) + 1)
            }
        }
    })
    var visibleCode by remember(file.id) { mutableStateOf(EditorHighlightWindow(0, 0)) }
    fun highlightCurrentSection() {
        if (editorVerticalScroll.isScrollInProgress || editorHorizontalScroll.isScrollInProgress)
                return
        val layout = editorLayout ?: return
        if (layout.lineCount == 0) return
        val first = layout.getLineForVerticalPosition(editorVerticalScroll.value.toFloat())
        val last =
                layout.getLineForVerticalPosition(
                        (editorVerticalScroll.value + editorVerticalScroll.viewportSize).toFloat(),
                )
        visibleCode = EditorHighlightWindow(layout.getLineStart(first), layout.getLineEnd(last))
    }
    val highlightTouchedSection by rememberUpdatedState(newValue = { highlightCurrentSection() })
    LaunchedEffect(file.id) {
        // Observe only the scroll transition. Reading the pixel offset here would wake
        // the collector on every frame and make large-file scrolling feel sluggish.
        snapshotFlow { editorVerticalScroll.isScrollInProgress || editorHorizontalScroll.isScrollInProgress }.collectLatest { isScrolling ->
            if (!isScrolling) {
                delay(EDITOR_SCROLL_HIGHLIGHT_IDLE_MS)
                highlightTouchedSection()
            }
        }
    }

    val sourceJump = development?.sourceJump
    LaunchedEffect(sourceJump, file.filePath) {
        if (sourceJump != null && samePath(sourceJump.file, file.filePath) && session.lastSourceJumpVersion != sourceJump.version) {
            var offset = 0
            repeat((sourceJump.line - 1).coerceAtMost(editorValue.text.length)) {
                val next = editorValue.text.indexOf('\n', offset)
                offset = if (next < 0) editorValue.text.length else next + 1
            }
            editorValue = editorValue.copy(selection = TextRange(offset), composition = null)
            session.value = editorValue
            revealEditedCursor()
            session.lastSourceJumpVersion = sourceJump.version
            if (!usePlatformEditor) editorVerticalScroll.scrollTo(with(density) { ((sourceJump.line - 1) * 20).sp.toPx().toInt() })
        }
    }
    val lineCount = textMetrics.lineCount
    val lineNumbers =
            remember(lineCount, diagnostics, palette, usePlatformEditor, fileBreakpoints) {
                if (usePlatformEditor) AnnotatedString("") else {
                    val base = diagnosticLineNumberText(lineCount, diagnostics, palette)
                    AnnotatedString(maskBreakpointLineNumbers(base.text, fileBreakpoints), base.spanStyles)
                }
            }
    val maxLineCharacters = textMetrics.longestLineLength.coerceAtLeast(48)
    val codeStyle =
            TextStyle(
                    color = palette.text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
            )
    val editorTextMeasurer = rememberTextMeasurer()
    val gutterDigits = lineCount.toString().length
    val measuredGutterWidth = remember(editorTextMeasurer, gutterDigits, codeStyle) {
        editorTextMeasurer.measure("0".repeat(gutterDigits), codeStyle).size.width
    }
    val measuredCharacterWidth = remember(editorTextMeasurer, codeStyle) {
        editorTextMeasurer.measure("M", codeStyle).size.width
    }
    val gutterWidth = (with(density) { measuredGutterWidth.toDp() } + 12.dp).coerceAtLeast(22.dp)
    val editorContentWidth = maxOf(640.dp, with(density) { (maxLineCharacters * measuredCharacterWidth).toDp() } + 48.dp)
    val showQuickKeys = showKeyboardAccessory && WindowInsets.ime.getBottom(density) > 0
    LaunchedEffect(
            editorLayout, editorValue.selection, editorValue.text, editorFocused, followCursor,
            caretRevealVersion, editorVerticalScroll.viewportSize, editorHorizontalScroll.viewportSize,
            editorVerticalScroll.isScrollInProgress, editorHorizontalScroll.isScrollInProgress,
            completionSurfaceVisible, showQuickKeys,
    ) {
        if (usePlatformEditor || !editorFocused || !followCursor ||
                editorVerticalScroll.isScrollInProgress || editorHorizontalScroll.isScrollInProgress) return@LaunchedEffect
        val layout = editorLayout ?: return@LaunchedEffect
        if (layout.layoutInput.text.text != editorValue.text) return@LaunchedEffect
        val cursor = layout.getCursorRect(editorValue.selection.end.coerceIn(0, editorValue.text.length))
        val margin = with(density) { 12.dp.toPx() }
        val vertical = editorCursorScrollTarget(editorVerticalScroll.value,
                editorVerticalScroll.viewportSize, editorVerticalScroll.maxValue,
                cursor.top, cursor.bottom, margin)
        val horizontal = editorCursorScrollTarget(editorHorizontalScroll.value,
                editorHorizontalScroll.viewportSize, editorHorizontalScroll.maxValue,
                cursor.left, cursor.right.coerceAtLeast(cursor.left + 2f), margin)
        // The viewport has already excluded the completion strip and keyboard. Reveal only
        // the hidden caret, without animating or moving the surrounding page on each key.
        if (vertical != editorVerticalScroll.value) editorVerticalScroll.scrollTo(vertical)
        if (horizontal != editorHorizontalScroll.value) editorHorizontalScroll.scrollTo(horizontal)
    }
    val editorBottomComfort = 16.dp
    val editorVisualTransformation =
            remember(
                    file.name,
                    palette,
                    diagnostics,
                    visibleCode,
            ) {
                WebSyntaxTransformation(
                        fileName = file.name,
                        palette = palette,
                        diagnostics = diagnostics,
                        visibleStart = visibleCode.start,
                        visibleEnd = visibleCode.end,
                )
            }
    fun selectCompletion(item: CompletionItem) {
        val context = completionResult.context ?: return
        if (completionResult.source != editorValue.text ||
                        completionResult.cursor != editorValue.selection.start
        )
                return
        val inserted = applyCompletion(editorValue, context, item)
        history.record(editorValue, inserted)
        editorValue = inserted
        session.value = inserted
        revealEditedCursor()
        completionResult = EditorCompletionResult()
        suppressCompletionsForText = inserted.text
        completionsVisible = false
        onCodeChange(inserted.text)
    }

    fun updateEditorValue(requestedValue: TextFieldValue) {
        val previousValue = editorValue
        val textChanged = requestedValue.text !== previousValue.text
        val updatedValue = applySmartWebEditorChange(previousValue, requestedValue, file.name)
        history.record(previousValue, updatedValue)
        editorValue = updatedValue
        session.value = updatedValue
        if (textChanged || updatedValue.selection != previousValue.selection) revealEditedCursor()
        if (textChanged || requestedValue.selection != previousValue.selection) highlightCurrentSection()
        if (textChanged) {
            suppressCompletionsForText = null
            completionResult = EditorCompletionResult()
            completionsVisible = false
        } else if (requestedValue.selection != previousValue.selection) {
            completionsVisible = false
        }
        if (updatedValue.text !== previousValue.text) onCodeChange(updatedValue.text)
    }
    fun restoreEdit(redo: Boolean) {
        val restored = (if (redo) history.redo(editorValue) else history.undo(editorValue)) ?: return
        editorValue = restored
        session.value = restored
        revealEditedCursor()
        completionResult = EditorCompletionResult()
        completionsVisible = false
        suppressCompletionsForText = restored.text
        onCodeChange(restored.text)
    }
    ManualScrollEditor(modifier = modifier.onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && (event.isCtrlPressed || event.isMetaPressed)) {
            when (event.key) {
                Key.Z -> { restoreEdit(event.isShiftPressed); true }
                Key.Y -> { restoreEdit(true); true }
                else -> false
            }
        } else false
    }) {
        header {
            WebDevelopmentMenu(file, editorValue.text, palette) { restored ->
                val updated = TextFieldValue(restored, TextRange(minOf(editorValue.selection.end, restored.length)))
                history.record(editorValue, updated)
                editorValue = updated
                session.value = updated
                revealEditedCursor()
                completionResult = EditorCompletionResult()
                completionsVisible = false
                onCodeChange(restored)
            }
        }

        if (usePlatformEditor) {
            key(file.id) { LargeFileCodeEditor(
                value = editorValue,
                session = session,
                palette = palette,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                highlight = { highlightWebSource(file.name, it, palette) },
                breakpoints = fileBreakpoints,
                onBreakpoint = ::toggleBreakpoint,
                sourceJump = sourceJump?.takeIf { samePath(it.file, file.filePath) },
                onValueChange = ::updateEditorValue,
            ) }
        } else
        Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            Box(
                    modifier = Modifier.width(gutterWidth).fillMaxHeight().clipToBounds()
                        .pointerInput(file.filePath) {
                            detectTapGestures { tap -> onGutterTap(tap) }
                        },
            ) {
                Text(
                        text = lineNumbers,
                        color = palette.subtleText,
                        style = codeStyle,
                        textAlign = TextAlign.Center,
                        modifier =
                                Modifier.fillMaxWidth()
                                        .wrapContentHeight(unbounded = true, align = Alignment.Top)
                                        .graphicsLayer {
                                            translationY = -editorVerticalScroll.value.toFloat()
                                        },
                )
                Canvas(Modifier.fillMaxSize()) {
                    val layout = editorLayout?.takeIf { it.layoutInput.text.text == editorValue.text }
                            ?: return@Canvas
                    for (line in fileBreakpoints) {
                        val index = line - 1
                        if (index !in 0 until layout.lineCount) continue
                        val centerY = (layout.getLineTop(index) + layout.getLineBottom(index)) / 2f - editorVerticalScroll.value
                        if (centerY in 0f..size.height) drawCircle(palette.error,
                                radius = 3.dp.toPx(), center = Offset(size.width / 2f, centerY))
                    }
                }
            }
            Box(
                    modifier =
                            Modifier.width(1.dp).fillMaxHeight().background(palette.strongBorder),
            )
            Spacer(Modifier.width(6.dp))
            Box(
                    modifier =
                            Modifier.weight(1f)
                                    .fillMaxHeight()
                                    .horizontalScroll(editorHorizontalScroll),
            ) {
                Box(
                        modifier =
                                Modifier.width(editorContentWidth)
                                        .fillMaxHeight()
                                        .heightIn(min = 240.dp)
                                        .verticalScroll(editorVerticalScroll)
                                        .testTag("editor-vertical-scroll"),
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
                            onValueChange = ::updateEditorValue,
                            onTextLayout = { result ->
                                // Colour-only relayouts must not cause a recomposition loop.
                                val previous = editorLayout
                                if (previous == null ||
                                                previous.layoutInput.text.text !==
                                                        result.layoutInput.text.text ||
                                                previous.size != result.size ||
                                                previous.lineCount != result.lineCount ||
                                                previous.layoutInput.style !=
                                                        result.layoutInput.style
                                )
                                        editorLayout = result
                            },
                            textStyle = codeStyle,
                            cursorBrush = SolidColor(palette.accent),
                            visualTransformation = editorVisualTransformation,
                            modifier =
                                    Modifier.fillMaxSize()
                                            .onFocusChanged {
                                                editorFocused = it.isFocused
                                            }
                                            .pointerInput(file.id, session) {
                                                // Observe taps without consuming editor selection
                                                // or scrolling gestures.
                                                awaitEachGesture {
                                                    val down =
                                                            awaitFirstDown(
                                                                    requireUnconsumed = false,
                                                                    pass = PointerEventPass.Initial
                                                            )
                                                    var dragged = false
                                                    do {
                                                        val event =
                                                                awaitPointerEvent(
                                                                        PointerEventPass.Final
                                                                )
                                                        if (event.changes.size != 1 ||
                                                                        event.changes.any {
                                                                            (it.position -
                                                                                            down.position)
                                                                                    .getDistance() >
                                                                                    viewConfiguration
                                                                                            .touchSlop
                                                                        }
                                                        )
                                                                dragged = true
                                                        val pressed =
                                                                event.changes.any { it.pressed }
                                                        if (dragged) {
                                                            followCursor = false
                                                            session.followCursor = false
                                                        } else if (!pressed) {
                                                            revealEditedCursor()
                                                            highlightTouchedSection()
                                                        }
                                                    } while (pressed)
                                                }
                                            }
                                            .padding(bottom = editorBottomComfort),
                    )

                    val currentSignatureHelp =
                            completionResult.signatureHelp?.takeIf {
                                completionResult.source == editorValue.text &&
                                        completionResult.cursor == editorValue.selection.start
                            }
                    currentSignatureHelp?.let { signature ->
                        SignatureHelpChip(
                                signature = signature,
                                palette = palette,
                                modifier =
                                        Modifier.align(Alignment.BottomStart)
                                                .padding(start = 6.dp, bottom = 6.dp),
                        )
                    }
                }
            }
        }
        // Keep one compact completion strip mounted so suggestions never resize the
        // editor. Items flow horizontally and can be scrolled when they overflow.
        Spacer(Modifier.height(6.dp))
        CompletionSuggestionBar(
                completions = if (completionSurfaceVisible) completionResult.items else emptyList(),
                palette = palette,
                modifier = Modifier.fillMaxWidth(),
                onSelect = { item -> selectCompletion(item) },
        )
        if (showQuickKeys) {
            Spacer(Modifier.height(6.dp))
            EditorQuickKeys(
                    palette = palette,
                    editorValue = editorValue,
                    fileName = file.name,
                    canUndo = history.canUndo,
                    canRedo = history.canRedo,
                    onUndo = { restoreEdit(false) },
                    onRedo = { restoreEdit(true) },
                    onEditorValueChange = { updated ->
                        history.record(editorValue, updated)
                        editorValue = updated
                        session.value = updated
                        revealEditedCursor()
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
            modifier =
                    modifier.clip(RoundedCornerShape(8.dp))
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
    var detailsItem by remember { mutableStateOf<CompletionItem?>(null) }
    detailsItem?.let { item -> CompletionDetailsDialog(item, palette) { detailsItem = null } }
    Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                    modifier.height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.elevatedPanel)
                            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(10.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp),
    ) {
        completions.take(16).forEach { item ->
            CompletionSuggestionChip(
                    item = item,
                    palette = palette,
                    onSelect = { onSelect(item) },
                    onDetails = { detailsItem = item },
            )
        }
    }
}

@Composable
private fun CompletionSuggestionChip(
        item: CompletionItem,
        palette: WebPalette,
        onSelect: () -> Unit,
        onDetails: () -> Unit,
) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                    Modifier.height(34.dp)
                            .width(190.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.panel)
                            .border(
                                    BorderStroke(1.dp, palette.strongBorder),
                                    RoundedCornerShape(8.dp)
                            )
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
        IconButton(onClick = onDetails, modifier = Modifier.size(34.dp)) {
            Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "Details for ${item.label}",
                    tint = palette.subtleText,
                    modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CompletionDetailsDialog(
        item: CompletionItem,
        palette: WebPalette,
        onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var documentation by remember(item) { mutableStateOf(specialCompletionDocumentation(item)) }
    var loading by remember(item) { mutableStateOf(true) }
    LaunchedEffect(item) {
        documentation =
                withContext(Dispatchers.IO) {
                    CompletionDocumentationCatalog.load(context).resolve(item)
                }
        loading = false
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
                color = palette.elevatedPanel,
                contentColor = palette.text,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, palette.strongBorder),
                modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End).size(32.dp)
                ) {
                    Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close suggestion details",
                            tint = palette.text
                    )
                }
                Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier =
                                Modifier.fillMaxWidth()
                                        .weight(1f, fill = false)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(palette.panel)
                                        .border(
                                                BorderStroke(1.dp, palette.border),
                                                RoundedCornerShape(12.dp)
                                        )
                                        .verticalScroll(rememberScrollState())
                                        .padding(10.dp),
                ) {
                    androidx.compose.material3.ProvideTextStyle(
                            androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                            ),
                    ) {
                        Text(item.label, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                                "Kind: ${item.detail.ifBlank { "suggestion" }}",
                                color = palette.subtleText
                        )
                        Text(
                                item.documentation.ifBlank {
                                    documentation?.description
                                            ?: if (loading) "Loading reference…"
                                            else
                                                    "No bundled reference is available for this suggestion. The insertion text is shown below."
                                }
                        )
                        Text("Syntax:", color = palette.subtleText, fontWeight = FontWeight.Bold)
                        Text(documentation?.syntax ?: completionSyntax(item))
                        val example = documentation?.example.orEmpty()
                        Text(
                                if (example.isNotBlank()) "Example:" else "Insertion:",
                                color = palette.subtleText,
                                fontWeight = FontWeight.Bold
                        )
                        Text(example.ifBlank { item.insertText })
                        documentation?.let { doc ->
                            if (doc.source.isNotBlank()) {
                                val sourceName =
                                        when {
                                            "developer.mozilla.org" in doc.source -> "MDN reference"
                                            "typescriptlang.org" in doc.source ->
                                                    "TypeScript reference"
                                            else -> "Official reference"
                                        }
                                Text(
                                        sourceName,
                                        color = palette.accent,
                                        modifier =
                                                Modifier.clickable {
                                                    uriHandler.openUri(doc.source)
                                                }
                                )
                            }
                            if (doc.standard.isNotBlank() && doc.standard != doc.source) {
                                Text(
                                        "Official standard",
                                        color = palette.accent,
                                        modifier =
                                                Modifier.clickable {
                                                    uriHandler.openUri(doc.standard)
                                                }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectionStrip(
        diagnostics: List<CodeDiagnostic>,
        palette: WebPalette,
        modifier: Modifier = Modifier,
) {
    val sortedDiagnostics =
            remember(diagnostics) {
                diagnostics.sortedWith(
                        compareBy<CodeDiagnostic> { diagnosticSeverityRank(it.severity) }
                                .thenBy { it.line }
                                .thenBy { it.column }
                )
            }
    val highestSeverity = sortedDiagnostics.firstOrNull()?.severity ?: "hint"

    Column(
            modifier =
                    modifier.clip(RoundedCornerShape(8.dp))
                            .background(palette.elevatedPanel)
                            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.heightIn(max = 42.dp).verticalScroll(rememberScrollState()),
        ) {
            sortedDiagnostics.forEach { diagnostic ->
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(18.dp),
                ) {
                    Box(
                            modifier =
                                    Modifier.size(7.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    diagnosticColor(diagnostic.severity, palette)
                                            ),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                            text = "L${diagnostic.line}: ${diagnostic.message}",
                            color =
                                    if (diagnostic.severity == highestSeverity) palette.text
                                    else palette.mutedText,
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
internal fun EditorQuickKeys(
        palette: WebPalette,
        editorValue: TextFieldValue,
        fileName: String,
        canUndo: Boolean = false,
        canRedo: Boolean = false,
        onUndo: () -> Unit = {},
        onRedo: () -> Unit = {},
        modifier: Modifier = Modifier,
        onEditorValueChange: (TextFieldValue) -> Unit,
) {
    val language = remember(fileName) { webLanguageForFile(fileName) }

    Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        QuickKey("Tab", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "    ")) }
        QuickKeyIcon(Icons.AutoMirrored.Rounded.Undo, "Undo", palette, canUndo, onUndo)
        QuickKeyIcon(Icons.AutoMirrored.Rounded.Redo, "Redo", palette, canRedo, onRedo)

        when (language) {
            WebEditorLanguage.Html -> {
                QuickKey("<>", palette) {
                    onEditorValueChange(wrapOrInsertPair(editorValue, "<", ">"))
                }
                QuickKey("</>", palette) { onEditorValueChange(insertClosingHtmlTag(editorValue)) }
            }
            WebEditorLanguage.Css -> {
                QuickKey(":", palette) {
                    onEditorValueChange(insertCodeAtSelection(editorValue, ":"))
                }
                QuickKey(";", palette) {
                    onEditorValueChange(insertCodeAtSelection(editorValue, ";"))
                }
                QuickKey("#", palette) {
                    onEditorValueChange(insertCodeAtSelection(editorValue, "#"))
                }
                QuickKey("()", palette) {
                    onEditorValueChange(wrapOrInsertPair(editorValue, "(", ")"))
                }
            }
            WebEditorLanguage.JavaScript -> {
                QuickKey("()", palette) {
                    onEditorValueChange(wrapOrInsertPair(editorValue, "(", ")"))
                }
                QuickKey("{}", palette) {
                    onEditorValueChange(wrapOrInsertPair(editorValue, "{", "}"))
                }
                QuickKey("[]", palette) {
                    onEditorValueChange(wrapOrInsertPair(editorValue, "[", "]"))
                }
                QuickKey("=>", palette) {
                    onEditorValueChange(insertCodeAtSelection(editorValue, "=> "))
                }
                QuickKey(";", palette) {
                    onEditorValueChange(insertCodeAtSelection(editorValue, ";"))
                }
            }
            else -> {}
        }

        if (language != WebEditorLanguage.Html && language != WebEditorLanguage.JavaScript) {
            QuickKey("{}", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "{", "}")) }
        }

        QuickKey("\"\"", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "\"", "\"")) }
        QuickKey("''", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "'", "'")) }
        QuickKey("=", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "=")) }
        QuickKey(".", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, ".")) }
        if (language !in setOf(WebEditorLanguage.Css, WebEditorLanguage.JavaScript)) {
            QuickKey("()", palette) { onEditorValueChange(wrapOrInsertPair(editorValue, "(", ")")) }
            QuickKey(";", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, ";")) }
        }
        if (language != WebEditorLanguage.Css) {
            QuickKey(":", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, ":")) }
            QuickKey("#", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "#")) }
        }
        listOf("_", "+", "-", "*", "\\").forEach { symbol ->
            QuickKey(symbol, palette) { onEditorValueChange(insertCodeAtSelection(editorValue, symbol)) }
        }
        QuickKey("/", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "/")) }

        if (language == WebEditorLanguage.JavaScript) {
            QuickKey("!", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "!")) }
            QuickKey("&", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "&")) }
            QuickKey("|", palette) { onEditorValueChange(insertCodeAtSelection(editorValue, "|")) }
        }
    }
}

@Composable
private fun QuickKeyIcon(
        icon: ImageVector,
        label: String,
        palette: WebPalette,
        enabled: Boolean,
        onClick: () -> Unit,
) {
    Box(contentAlignment = Alignment.Center,
            modifier = Modifier.size(width = 40.dp, height = 32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(palette.selectedTab)
                    .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(9.dp))
                    .clickable(enabled = enabled, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)) {
        Icon(icon, contentDescription = label,
                tint = if (enabled) palette.text else palette.mutedText,
                modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun QuickKey(
        label: String,
        palette: WebPalette,
        onClick: () -> Unit,
) {
    Box(
            modifier =
                    Modifier.height(32.dp)
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
private fun EmptyEditorFullScreen(
        files: List<WebFile>,
        palette: WebPalette,
        onSelectFile: (Long) -> Unit,
        onAddFile: () -> Unit,
        onDeleteFile: (Long) -> Unit,
        onBack: () -> Unit,
) {
    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .imePadding()
                            .padding(horizontal = 14.dp)
                            .padding(bottom = 10.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        FileTabsOnlyBar(
                files = files,
                activeFileId = -1L,
                palette = palette,
                onSelectFile = onSelectFile,
                onAddFile = onAddFile,
                onDeleteFile = onDeleteFile,
        )
        Spacer(Modifier.height(10.dp))
        EmptyEditorPanel(
                palette = palette,
                modifier = Modifier.weight(1f),
                onNewFile = onAddFile,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
            colors = readableOutlinedButtonColors(),
        ) { Text("Back") }
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
            modifier =
                    Modifier.fillMaxSize()
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
        if (!keyboardVisible && !isDatabaseFileName(activeFile.name)) {
            Spacer(Modifier.height(12.dp))
            RunPreviewButton(
                    palette = palette,
                    label = runButtonLabelFor(activeFile),
                    preview = isLivePreviewFile(activeFile.name),
                    onClick = onRunPreview,
            )
        }
    }
}

@Composable
private fun RunPreviewButton(
        palette: WebPalette,
        label: String = "Run Code",
        preview: Boolean = false,
        enabled: Boolean = true,
        onClick: () -> Unit,
) {
    if (LocalApiTester.current?.open == true) return
    Button(
            onClick = onClick,
            enabled = enabled,
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = palette.run,
                            contentColor = palette.runText,
                            disabledContainerColor = palette.tab,
                            disabledContentColor = palette.mutedText,
                    ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp),
    ) {
        Icon(
                imageVector = if (preview) Icons.Rounded.Visibility else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(25.dp),
        )
        Spacer(Modifier.width(9.dp))
        Text(
                text = label,
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
        showHeader: Boolean = true,
        expanded: Boolean = false,
        darkMode: Boolean? = null,
        nextThemeName: String = "",
        onToggleTheme: (() -> Unit)? = null,
        isRunning: Boolean = false,
        keepInputVisibleWhileRunning: Boolean = true,
        isAwaitingInput: Boolean = false,
        onInputChange: (String) -> Unit,
        onSend: () -> Unit,
        onFullScreen: () -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var sessionRunning by remember { mutableStateOf(false) }
    var stoppingSession by remember { mutableStateOf(false) }
    var stopError by remember { mutableStateOf("") }
    val terminalContext = LocalContext.current
    val terminalLifecycle = (terminalContext as? androidx.lifecycle.LifecycleOwner)?.lifecycle
    LaunchedEffect(terminalLifecycle) {
        suspend fun observeSession() {
            while (true) {
                sessionRunning = withContext(Dispatchers.IO) { NodeSessionRuntime.isRunning() }
                delay(
                        if (sessionRunning) DeviceEfficiency.budget.nodeStatusPollMs
                        else DeviceEfficiency.budget.idlePollMs
                )
            }
        }
        if (terminalLifecycle != null) {
            terminalLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { observeSession() }
        } else observeSession()
    }
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

    val panelModifier =
            if (height == Dp.Unspecified) {
                modifier.fillMaxWidth()
            } else {
                modifier.fillMaxWidth().height(height)
            }

    Panel(
            palette = palette,
            modifier = panelModifier.bringIntoViewRequester(bringIntoViewRequester),
            backgroundColor = palette.terminalBackground,
    ) {
        if (showHeader) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                        text = if (sessionRunning) "Terminal · Running" else "Terminal Output",
                        color = palette.text,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f),
                )
                WebTerminalMenu(output = text, palette = palette)
                if (sessionRunning) {
                    TextButton(
                            onClick = {
                                val force = stoppingSession
                                stoppingSession = true
                                coroutineScope.launch {
                                    try {
                                        val result =
                                                withContext(Dispatchers.IO) {
                                                    NodeSessionRuntime.stop(force)
                                                }
                                        stopError = result.error
                                        sessionRunning =
                                                withContext(Dispatchers.IO) {
                                                    NodeSessionRuntime.isRunning()
                                                }
                                    } finally {
                                        stoppingSession = false
                                    }
                                }
                            },
                            colors =
                                    ButtonDefaults.textButtonColors(
                                            contentColor = palette.error
                                    )
                    ) {
                        Text(
                                if (stoppingSession) "■ Kill" else "■ Stop",
                                fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (darkMode != null && onToggleTheme != null) {
                    IdeThemeCycleButton(
                            darkMode = darkMode,
                            nextThemeName = nextThemeName,
                            buttonColor = palette.elevatedPanel,
                            borderColor = palette.border,
                            contentColor = palette.text,
                            onClick = onToggleTheme,
                            modifier = Modifier.size(30.dp),
                            iconSize = 18.dp,
                    )
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = onFullScreen, modifier = Modifier.size(34.dp)) {
                    Icon(
                            imageVector =
                                    if (expanded) Icons.Rounded.FullscreenExit
                                    else Icons.Rounded.Fullscreen,
                            contentDescription =
                                    if (expanded) "Minimize terminal" else "Fullscreen terminal",
                            tint = palette.text,
                            modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
            HorizontalRule(palette)
            Spacer(Modifier.height(7.dp))
        }
        TerminalBody(
                terminalText = text,
                terminalInput = input,
                palette = palette,
                promptText = promptText,
                isRunning = isRunning || sessionRunning,
                keepInputVisibleWhileRunning = keepInputVisibleWhileRunning,
                isAwaitingInput = isAwaitingInput,
                onTerminalFocusChange = { focused ->
                    terminalFocused = focused
                    if (focused) bringTerminalIntoView()
                },
                onTerminalInputChange = onInputChange,
                onTerminalCommandSubmit = onSend,
                modifier = Modifier.fillMaxWidth().weight(1f),
        )
        if (stopError.isNotBlank()) Text(stopError, color = palette.error, fontSize = 12.sp)
    }
}

@Composable
internal fun TerminalBody(
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
    val inputFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    var inputValue by remember {
        mutableStateOf(TextFieldValue(terminalInput, TextRange(terminalInput.length)))
    }
    var inputFocused by remember { mutableStateOf(false) }
    var inputLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var inputRowOrigin by remember { mutableStateOf<Offset?>(null) }
    var inputFieldOrigin by remember { mutableStateOf<Offset?>(null) }
    var inputRevealVersion by remember { mutableIntStateOf(0) }
    val inputCursorRect = remember(inputLayout, inputValue.text, inputValue.selection) {
        inputLayout?.takeIf { it.layoutInput.text.text == inputValue.text }?.let { layout ->
            layout.getCursorRect(inputValue.selection.end.coerceIn(0, inputValue.text.length))
        }
    }
    val imeBottom = WindowInsets.ime.getBottom(density)
    val showInputRow = !isRunning || isAwaitingInput || keepInputVisibleWhileRunning
    val activeInputLineStart =
            remember(terminalText, isAwaitingInput) {
                if (isAwaitingInput) terminalText.lastIndexOf('\n') else -1
            }
    val rawDisplayedTerminalText =
            remember(terminalText, isAwaitingInput, activeInputLineStart) {
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
    val activeInputPrefix =
            remember(
                    terminalText,
                    promptText,
                    isAwaitingInput,
                    isRunning,
                    keepInputVisibleWhileRunning,
                    activeInputLineStart
            ) {
                if (isAwaitingInput) {
                    when {
                        terminalText.isBlank() -> ""
                        activeInputLineStart >= 0 ->
                                terminalText.substring(activeInputLineStart + 1)
                        else -> terminalText
                    }
                } else if (isRunning && !keepInputVisibleWhileRunning) {
                    ""
                } else {
                    "$promptText "
                }
            }
    val displayedTerminalText =
            remember(rawDisplayedTerminalText, promptText, isAwaitingInput) {
                val budget = DeviceEfficiency.budget
                terminalDisplayText(
                        terminalRenderTail(
                                rawDisplayedTerminalText,
                                budget.terminalRenderChars,
                                budget.terminalRenderLines
                        ),
                        promptText,
                        keepLastPrompt = isAwaitingInput,
                )
            }
    val parsedTerminalText =
            remember(displayedTerminalText, promptText, palette) {
                linkifyTerminalOutput(
                        parseAnsiTerminalOutput(displayedTerminalText, promptText, palette),
                        background = palette.terminalBackground,
                )
            }
    val terminalTextStyle =
            TextStyle(
                    color = palette.text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
            )
    val terminalInputPromptStyle =
            terminalTextStyle.copy(
                    color = palette.terminalPrompt,
                    fontWeight = FontWeight.SemiBold,
            )
    val longestOutputLineLength =
            remember(displayedTerminalText) {
                estimatedLongestLineLength(displayedTerminalText, cap = 4_000)
            }
    val longestTerminalLineLength =
            remember(longestOutputLineLength, activeInputPrefix, terminalInput, showInputRow) {
                val inputLength =
                        if (showInputRow) activeInputPrefix.length +
                                estimatedLongestLineLength(terminalInput, cap = 4_000) + 1 else 0
                maxOf(longestOutputLineLength, inputLength, 1).coerceAtMost(4_000)
            }
    val terminalTextMeasurer = rememberTextMeasurer()
    val terminalCharacterWidth = remember(terminalTextMeasurer, terminalTextStyle) {
        terminalTextMeasurer.measure("M", terminalTextStyle).size.width
    }

    fun focusTerminalInput() {
        if (!showInputRow) return
        inputFocusRequester.requestFocus()
        keyboardController?.show()
        inputRevealVersion++
        onTerminalFocusChange(true)
    }

    LaunchedEffect(isAwaitingInput, showInputRow) {
        if (isAwaitingInput && showInputRow) {
            delay(60)
            focusTerminalInput()
        }
    }

    LaunchedEffect(terminalInput) {
        if (terminalInput != inputValue.text) {
            inputValue = TextFieldValue(terminalInput, TextRange(terminalInput.length))
        }
    }

    LaunchedEffect(
            inputCursorRect, inputRowOrigin, inputFieldOrigin, inputRevealVersion,
            inputFocused, isAwaitingInput, showInputRow, imeBottom,
            outputVerticalScroll.viewportSize, outputHorizontalScroll.viewportSize,
            outputVerticalScroll.maxValue, outputHorizontalScroll.maxValue,
            outputVerticalScroll.isScrollInProgress, outputHorizontalScroll.isScrollInProgress,
    ) {
        if (!showInputRow || (!inputFocused && !isAwaitingInput) ||
                outputVerticalScroll.isScrollInProgress || outputHorizontalScroll.isScrollInProgress)
                return@LaunchedEffect
        val cursor = inputCursorRect ?: return@LaunchedEffect
        val rowOrigin = inputRowOrigin ?: return@LaunchedEffect
        val fieldOrigin = inputFieldOrigin ?: return@LaunchedEffect
        val target = terminalCursorScrollTargets(
                horizontal = outputHorizontalScroll.value,
                vertical = outputVerticalScroll.value,
                viewportWidth = outputHorizontalScroll.viewportSize,
                viewportHeight = outputVerticalScroll.viewportSize,
                horizontalMax = outputHorizontalScroll.maxValue,
                verticalMax = outputVerticalScroll.maxValue,
                inputRowOrigin = rowOrigin,
                inputFieldOrigin = fieldOrigin,
                caret = cursor,
                margin = with(density) { 12.dp.toPx() },
        )
        // Measure the prompt and input row instead of assuming the caret is at the end
        // of the output. Earlier lines in multiline input must remain reachable too.
        if (target.vertical != outputVerticalScroll.value) outputVerticalScroll.scrollTo(target.vertical)
        if (target.horizontal != outputHorizontalScroll.value) {
            outputHorizontalScroll.scrollTo(target.horizontal)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val estimatedContentWidth = with(density) {
            (longestTerminalLineLength * terminalCharacterWidth).toDp()
        } + 20.dp
        val terminalContentWidth = maxOf(maxWidth, estimatedContentWidth)
        Box(modifier = Modifier.fillMaxSize().horizontalScroll(outputHorizontalScroll)
                .testTag("terminal-horizontal-scroll")) {
            Box(
                    modifier =
                            Modifier.width(terminalContentWidth)
                                    .fillMaxHeight()
                                    .verticalScroll(outputVerticalScroll)
                                    .testTag("terminal-vertical-scroll"),
            ) {
                if (showInputRow) {
                    Box(
                            modifier =
                                    Modifier.matchParentSize().pointerInput(showInputRow) {
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
                                modifier = Modifier.fillMaxWidth().onGloballyPositioned {
                                    inputRowOrigin = it.positionInParent()
                                },
                        ) {
                            Text(
                                    text = activeInputPrefix,
                                    style =
                                            if (isAwaitingInput) terminalTextStyle
                                            else terminalInputPromptStyle,
                                    modifier = Modifier.alignByBaseline(),
                            )
                            BasicTextField(
                                    value = inputValue,
                                    onValueChange = { requested ->
                                        val changed = requested.text != inputValue.text ||
                                                requested.selection != inputValue.selection
                                        inputValue = requested
                                        if (changed) inputRevealVersion++
                                        onTerminalInputChange(requested.text)
                                    },
                                    textStyle = terminalTextStyle,
                                    cursorBrush = SolidColor(palette.accent),
                                    onTextLayout = { result ->
                                        val previous = inputLayout
                                        if (previous == null || previous.layoutInput.text.text != result.layoutInput.text.text ||
                                                previous.size != result.size || previous.layoutInput.style != result.layoutInput.style)
                                                inputLayout = result
                                    },
                                    singleLine = false,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions =
                                            KeyboardActions(
                                                    onSend = { onTerminalCommandSubmit() },
                                                    onDone = { onTerminalCommandSubmit() },
                                                    onGo = { onTerminalCommandSubmit() },
                                            ),
                                    modifier =
                                            Modifier.weight(1f)
                                                    .heightIn(min = 24.dp)
                                                    .alignByBaseline()
                                                    .testTag("terminal-input")
                                                    .onGloballyPositioned { inputFieldOrigin = it.positionInParent() }
                                                    .focusRequester(inputFocusRequester)
                                                    .onFocusChanged { focusState ->
                                                        inputFocused = focusState.isFocused
                                                        onTerminalFocusChange(focusState.isFocused)
                                                    },
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
        darkMode: Boolean,
        nextThemeName: String,
        onToggleTheme: () -> Unit,
        promptText: String,
        text: String,
        input: String,
        onInputChange: (String) -> Unit,
        onSend: () -> Unit,
        onBack: () -> Unit,
) {
    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .imePadding()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 6.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        WebTerminalPanel(
                palette = palette,
                promptText = promptText,
                text = text,
                input = input,
                height = Dp.Unspecified,
                modifier = Modifier.fillMaxWidth().weight(1f),
                expanded = true,
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                onToggleTheme = onToggleTheme,
                isRunning = false,
                keepInputVisibleWhileRunning = true,
                isAwaitingInput = false,
                onInputChange = onInputChange,
                onSend = onSend,
                onFullScreen = onBack,
        )
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
        endpointOverride: String?,
        modelOverride: String?,
        onSaveSettings: (String?, String?, String?) -> Unit,
        onClearChat: () -> Unit,
        canUndo: Boolean,
        onUndo: () -> Unit,
        busy: Boolean,
        error: String?,
        pendingAction: AiSuggestedAction?,
        verificationMessage: String?,
        onSend: () -> Unit,
        onApplyAction: (AiSuggestedAction) -> Unit,
        onDismissAction: () -> Unit,
        onBack: () -> Unit,
) {
    val aiEnabled = !busy
    val chatScroll = rememberLazyListState()
    val chatScope = rememberCoroutineScope()
    var clearConfirmation by remember { mutableStateOf(false) }
    var wasNearLatest by remember { mutableStateOf(true) }
    LaunchedEffect(chatScroll) {
        snapshotFlow { !chatScroll.canScrollForward ||
            (chatScroll.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= chatScroll.layoutInfo.totalItemsCount - 2
        }.collect { wasNearLatest = it }
    }
    LaunchedEffect(messages.size, busy) {
        if (wasNearLatest && (messages.isNotEmpty() || busy))
            chatScroll.animateScrollToItem((messages.size + if (busy) 1 else 0).minus(1).coerceAtLeast(0))
    }
    if (clearConfirmation) AlertDialog(
        onDismissRequest = { clearConfirmation = false }, title = { Text("Clear chat?") },
        text = { Text("Remove this conversation and pending suggestions? Project files will stay as they are.") },
        confirmButton = { TextButton(onClick = { clearConfirmation = false; onClearChat() }, colors = readableTextButtonColors()) { Text("Clear chat") } },
        dismissButton = { TextButton(onClick = { clearConfirmation = false }, colors = readableTextButtonColors()) { Text("Cancel") } },
    )

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .imePadding()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Panel(
                palette = palette,
                modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            var apiKeyEditRequest by remember { mutableLongStateOf(0L) }

            AiChatHeader(
                    palette = palette,
                    selectedModel = selectedModel,
                    modelOptions = modelOptions,
                    hasApiKey = userAiApiKey.isNotBlank(),
                    onChangeApiKey = { apiKeyEditRequest += 1L },
                    onSelectModel = { if (!busy) onSelectedModel(it) },
                    onClearChat = { clearConfirmation = true },
                    canClear = messages.isNotEmpty() && !busy,
                    onBack = onBack,
            )
            Spacer(Modifier.height(8.dp))
            AiPersonalKeySection(
                    palette = palette,
                    selectedModel = selectedModel,
                    currentKey = userAiApiKey,
                    endpointOverride = endpointOverride, modelOverride = modelOverride,
                    onSave = onSaveSettings,
                    editRequest = apiKeyEditRequest,
            )
            if (userAiApiKey.isBlank() && (selectedModel.label != "Custom / Ollama" || endpointOverride.isNullOrBlank())) {
                TextButton(onClick = { apiKeyEditRequest++ }, colors = readableTextButtonColors()) { Text("Configure AI connection", color = palette.accent) }
            }
            Spacer(Modifier.height(8.dp))
            if (canUndo) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Last AI file changes", color = palette.mutedText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onUndo, enabled = !busy, colors = readableTextButtonColors()) { Text("Undo AI changes") }
            }
            LazyColumn(
                    state = chatScroll,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages) { message -> AiMessageBubble(message = message, palette = palette) }
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
                            Text(
                                    "Thinking with ${selectedModel.label}...",
                                    color = palette.mutedText,
                                    fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            if (chatScroll.canScrollForward) TextButton(onClick = {
                chatScope.launch { chatScroll.animateScrollToItem((messages.size + if (busy) 1 else 0).minus(1).coerceAtLeast(0)) }
            }, modifier = Modifier.align(Alignment.End), colors = readableTextButtonColors()) { Text("Latest messages") }
            verificationMessage?.let { Text(it, color = palette.success, fontSize = 12.sp) }
            pendingAction?.takeIf { !busy }?.let { action ->
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
            if (messages.size <= 1 && input.isBlank() && !busy) Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf("Explain this project", "Find bugs", "Suggest tests").forEach { prompt ->
                    OutlinedButton(onClick = { onInputChange(prompt) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp), colors = readableOutlinedButtonColors()) {
                        Text(prompt, fontSize = 11.sp)
                    }
                }
            }
            Text("Project context is included when you send a message.", color = palette.subtleText, fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 6.dp))
            AiComposer(
                    palette = palette,
                    input = input,
                    enabled = aiEnabled,
                    canSend = userAiApiKey.isNotBlank() || (selectedModel.label == "Custom / Ollama" && !endpointOverride.isNullOrBlank()),
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
    hasApiKey: Boolean,
    onChangeApiKey: () -> Unit,
    onSelectModel: (AiModelOption) -> Unit,
    onClearChat: () -> Unit,
    canClear: Boolean,
    onBack: () -> Unit,
) {
    var settingsOpen by remember { mutableStateOf(false) }
    val menuScroll = rememberScrollState()
    LaunchedEffect(settingsOpen) { if (settingsOpen) menuScroll.scrollTo(0) }
    val geminiOptions = remember(modelOptions) { modelOptions.filter { it.provider == AiProvider.Gemini } }
    val providers = remember(modelOptions) { listOfNotNull(geminiOptions.firstOrNull()) + modelOptions.filter { it.provider != AiProvider.Gemini } }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.AutoAwesome, null, tint = palette.accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text("AI Chat", color = palette.text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(selectedModel.label, color = palette.mutedText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box {
            IconButton(onClick = { settingsOpen = true }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.Settings, "AI settings", tint = palette.accent)
            }
            DropdownMenu(settingsOpen, { settingsOpen = false }, containerColor = palette.elevatedPanel, scrollState = menuScroll,
                modifier = Modifier.heightIn(max = 440.dp)) {
                DropdownMenuItem(colors = readableMenuItemColors(), text = { Text("API settings", color = palette.text) },
                    leadingIcon = { Icon(Icons.Rounded.VpnKey, null, tint = palette.accent) },
                    onClick = { settingsOpen = false; onChangeApiKey() })
                DropdownMenuItem(colors = readableMenuItemColors(), text = { Text("Clear chat", color = palette.text) }, enabled = canClear,
                    leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = palette.mutedText) },
                    onClick = { settingsOpen = false; onClearChat() })
                Text("Provider", color = palette.mutedText, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                providers.forEach { option ->
                    DropdownMenuItem(colors = readableMenuItemColors(), text = { Text(if (option.provider == AiProvider.Gemini) "Google Gemini" else option.label,
                        color = if (option.provider == selectedModel.provider && (option.provider == AiProvider.Gemini || option.id == selectedModel.id)) palette.accent else palette.text) },
                        onClick = { onSelectModel(option); settingsOpen = false })
                }
                if (selectedModel.provider == AiProvider.Gemini) {
                    Text("Gemini model", color = palette.mutedText, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    geminiOptions.forEach { option ->
                        DropdownMenuItem(colors = readableMenuItemColors(), text = { Text(option.label, color = if (option.id == selectedModel.id) palette.accent else palette.text) },
                            onClick = { onSelectModel(option); settingsOpen = false })
                    }
                }
            }
        }
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.Close, "Close AI chat", tint = palette.text)
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
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.elevatedPanel)
                            .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                            .padding(10.dp),
    ) {
        Text(
                text =
                        "Using ${selectedModel.label}. Project files, active editor text, and recent terminal output can be sent to Gemini for this request.",
                color = palette.mutedText,
                fontSize = 12.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
                text =
                        if (hasApiKey) {
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
    val clipboard = LocalClipboardManager.current
    val actions = LocalWebDevelopmentActions.current
    val visibleText = remember(message.text, isUser) { if (isUser) message.text else aiVisibleResponseText(message.text) }
    Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isUser) {
                Icon(
                        Icons.Rounded.AutoAwesome,
                        null,
                        tint = palette.accent,
                        modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                    if (isUser) "You" else "AI",
                    color = palette.subtleText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
            )
            IconButton(onClick = { clipboard.setText(AnnotatedString(visibleText)) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.ContentCopy, if (isUser) "Copy message" else "Copy response", tint = palette.mutedText, modifier = Modifier.size(16.dp))
            }
        }
        Box(
                modifier =
                        Modifier.fillMaxWidth(if (isUser) 0.85f else 1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                        if (isUser) palette.selectedTab else palette.elevatedPanel
                                )
                                .border(
                                        1.dp,
                                        palette.border.copy(alpha = 0.6f),
                                        RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
        ) {
            Column {
                Text(
                        if (isUser) "You" else "Response",
                        color = palette.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp),
                )
                SelectionContainer {
                    Text(
                            text =
                                    if (isUser) AnnotatedString(message.text)
                                    else
                                            renderAiMarkdown(
                                                    aiVisibleResponseText(message.text),
                                                    palette
                                            ),
                            color = palette.text,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                    )
                }
                if (message.modifiedFiles.isNotEmpty()) {
                    Text("Files changed", color = palette.mutedText, fontSize = 11.sp)
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        message.modifiedFiles.forEach { path ->
                            TextButton(onClick = { actions?.source?.invoke(path, 1) }, colors = readableTextButtonColors()) {
                                WebFileIcon(File(path).name, palette, Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(actions?.fileStore?.projectDirectory?.let { File(path).relativeToOrSelf(it).invariantSeparatorsPath } ?: File(path).name)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun renderAiMarkdown(text: String, palette: WebPalette): AnnotatedString =
        buildAnnotatedString {
            var inCodeBlock = false
            text.lines().forEachIndexed { index, line ->
                if (line.trim().startsWith("```")) {
                    inCodeBlock = !inCodeBlock
                } else if (inCodeBlock) {
                    withStyle(
                            SpanStyle(fontFamily = FontFamily.Monospace, color = palette.accent)
                    ) { append(line) }
                } else {
                    val header = Regex("^(#{1,6})\\s*(.*)").find(line)
                    if (header != null) {
                        val size =
                                when (header.groupValues[1].length) {
                                    1 -> 18.sp
                                    2 -> 16.sp
                                    3 -> 15.sp
                                    else -> 13.sp
                                }
                        withStyle(
                                SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = size,
                                        color = palette.accent
                                )
                        ) { append(header.groupValues[2]) }
                    } else {
                        val tokens = Regex("\\*\\*(.*?)\\*\\*|`(.*?)`").findAll(line).toList()
                        var cursor = 0
                        tokens.forEach { match ->
                            append(line.substring(cursor, match.range.first))
                            val bold = match.groupValues[1].isNotEmpty()
                            val content = if (bold) match.groupValues[1] else match.groupValues[2]
                            withStyle(
                                    if (bold) SpanStyle(fontWeight = FontWeight.Bold)
                                    else
                                            SpanStyle(
                                                    fontFamily = FontFamily.Monospace,
                                                    background = palette.panel.copy(alpha = 0.5f),
                                                    color = palette.accent
                                            )
                            ) { append(content) }
                            cursor = match.range.last + 1
                        }
                        append(line.substring(cursor))
                    }
                }
                if (index < text.lines().lastIndex) append('\n')
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
            modifier =
                    Modifier.fillMaxWidth()
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
    val title =
            when (action) {
                is AiSuggestedAction.RunTerminalCommand -> "Suggested Command"
                is AiSuggestedAction.ReplaceActiveFile -> "Prepared Edit"
                is AiSuggestedAction.ApplyProjectFileChanges -> "Proposed File Changes"
            }
    val subtitle =
            when (action) {
                is AiSuggestedAction.RunTerminalCommand -> "Ready to run in the project terminal."
                is AiSuggestedAction.ReplaceActiveFile ->
                        "${action.code.lines().size} lines for the active file."
                is AiSuggestedAction.ApplyProjectFileChanges -> {
                    val writes =
                            action.changes.count { it.operation == AiProjectFileOperation.Write }
                    val deletes =
                            action.changes.count { it.operation == AiProjectFileOperation.Delete }
                    listOfNotNull(
                                    writes.takeIf { it > 0 }?.let {
                                        "$it write${if (it == 1) "" else "s"}"
                                    },
                                    deletes.takeIf { it > 0 }?.let {
                                        "$it delete${if (it == 1) "" else "s"}"
                                    },
                            )
                            .joinToString(", ")
                            .ifBlank {
                                "${action.changes.size} file change${if (action.changes.size == 1) "" else "s"}"
                            }
                }
            }

    Column(
            modifier =
                    Modifier.fillMaxWidth()
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 24.dp),
    ) {
        val operation =
                when (change.operation) {
                    AiProjectFileOperation.Write -> "write"
                    AiProjectFileOperation.Delete -> "delete"
                }
        Text(
                text = operation,
                color =
                        if (change.operation == AiProjectFileOperation.Delete) palette.error
                        else palette.success,
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
    selectedModel: AiModelOption,
    currentKey: String?,
    endpointOverride: String?,
    modelOverride: String?,
    onSave: (String?, String?, String?) -> Unit,
    editRequest: Long,
) {
    var editing by remember(selectedModel) { mutableStateOf(false) }
    var key by remember(selectedModel, currentKey) { mutableStateOf(currentKey.orEmpty()) }
    var endpoint by remember(selectedModel, endpointOverride) { mutableStateOf(endpointOverride ?: selectedModel.defaultEndpoint) }
    var model by remember(selectedModel, modelOverride) { mutableStateOf(modelOverride ?: selectedModel.id) }
    var settingsError by remember(selectedModel) { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current
    val portal = remember(selectedModel) { aiKeyPortal(selectedModel) }
    val custom = selectedModel.label == "Custom / Ollama"
    val configured = if (custom) !endpointOverride.isNullOrBlank() else !currentKey.isNullOrBlank()
    LaunchedEffect(editRequest) { if (editRequest > 0) editing = true }
    if (editing) AlertDialog(
        onDismissRequest = { editing = false },
        title = { Text("AI settings") },
        containerColor = palette.panel, titleContentColor = palette.text, textContentColor = palette.text,
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(selectedModel.label, color = palette.accent, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(endpoint, { endpoint = it; settingsError = null },
                    label = { Text("API endpoint URL") }, modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text(if (custom) "Ollama: http://your-computer-IP:11434/v1/chat/completions. Use an address reachable from this phone."
                        else if (selectedModel.provider == AiProvider.AzureOpenAi) "Full deployment URL with /chat/completions and api-version."
                        else "Provider API URL; Gemini uses its API base URL.") }, colors = readableFieldColors())
                OutlinedTextField(model, { model = it; settingsError = null }, label = { Text("Model / deployment name") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = readableFieldColors())
                OutlinedTextField(key, { key = it }, label = { Text(if (custom) "API key (optional)" else "Provider API key") },
                    visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = readableFieldColors())
                Text("Settings are saved on this device for this provider. Requests go directly to the configured server.",
                    color = palette.mutedText, fontSize = 11.sp)
                Text(portal.label, color = palette.accent, fontSize = 12.sp,
                    modifier = Modifier.clickable { uriHandler.openUri(portal.url) })
                settingsError?.let { Text(it, color = palette.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val validation = runCatching {
                    validatedAiEndpoint(endpoint, selectedModel.provider == AiProvider.AzureOpenAi)
                    require(model.isNotBlank()) { "Enter a model or deployment name." }
                    require(custom || key.isNotBlank()) { "Enter this provider's API key." }
                }
                if (validation.isFailure) settingsError = validation.exceptionOrNull()?.message
                else { onSave(key.trim().ifBlank { null }, endpoint.trim(), model.trim()); editing = false }
            }, colors = readableTextButtonColors()) { Text("Save settings") }
        },
        dismissButton = { TextButton(onClick = {
            editing = false; key = currentKey.orEmpty(); endpoint = endpointOverride ?: selectedModel.defaultEndpoint
            model = modelOverride ?: selectedModel.id; settingsError = null
        }, colors = readableTextButtonColors()) { Text("Cancel") } },
    )
}

private data class AiKeyPortal(val label: String, val url: String)

private fun aiKeyPortal(model: AiModelOption): AiKeyPortal =
        when {
            model.label == "Custom / Ollama" -> AiKeyPortal("Ollama documentation", "https://ollama.com/")
            model.provider == AiProvider.AzureOpenAi -> AiKeyPortal("Azure Portal", "https://portal.azure.com/")
            model.provider == AiProvider.Gemini ->
                    AiKeyPortal(
                            "Get a free API key at Google AI Studio",
                            "https://aistudio.google.com/app/apikey"
                    )
            model.provider == AiProvider.Anthropic ->
                    AiKeyPortal(
                            "Get an API key at Anthropic Console",
                            "https://console.anthropic.com/settings/keys"
                    )
            model.endpoint.contains("openrouter.ai") ->
                    AiKeyPortal(
                            "Get a free API key at OpenRouter",
                            "https://openrouter.ai/settings/keys"
                    )
            model.endpoint.contains("groq.com") ->
                    AiKeyPortal("Get a free API key at GroqCloud", "https://console.groq.com/keys")
            model.endpoint.contains("together.xyz") -> AiKeyPortal("Together API keys", "https://api.together.xyz/settings/api-keys")
            model.endpoint.contains("mistral.ai") ->
                    AiKeyPortal(
                            "Get a free API key at Mistral Console",
                            "https://console.mistral.ai/api-keys/"
                    )
            else ->
                    AiKeyPortal(
                            "Get an API key at OpenAI Platform",
                            "https://platform.openai.com/api-keys"
                    )
        }

@Composable
private fun AiActionApproval(
        palette: WebPalette,
        action: AiSuggestedAction,
        onApply: () -> Unit,
        onDismiss: () -> Unit,
) {
    val title =
            when (action) {
                is AiSuggestedAction.RunTerminalCommand -> "Run suggested terminal command?"
                is AiSuggestedAction.ReplaceActiveFile -> "Replace active file with suggested code?"
                is AiSuggestedAction.ApplyProjectFileChanges ->
                        "Apply suggested project file changes?"
            }
    val body =
            when (action) {
                is AiSuggestedAction.RunTerminalCommand -> action.command
                is AiSuggestedAction.ReplaceActiveFile ->
                        action.code.lineSequence().take(12).joinToString("\n")
                is AiSuggestedAction.ApplyProjectFileChanges ->
                        action.changes.joinToString("\n") { change ->
                            val label =
                                    when (change.operation) {
                                        AiProjectFileOperation.Write -> "write"
                                        AiProjectFileOperation.Delete -> "delete"
                                    }
                            "$label ${change.path}"
                        }
            }
    Column(
            modifier =
                    Modifier.fillMaxWidth()
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
            OutlinedButton(onClick = onDismiss, colors = readableOutlinedButtonColors()) { Text("Dismiss") }
            OutlinedButton(onClick = onApply, colors = readableOutlinedButtonColors()) {
                Text(
                        text =
                                when (action) {
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
        canSend: Boolean = true,
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
                textStyle =
                        TextStyle(
                                color = if (enabled) palette.text else palette.mutedText,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                        ),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (enabled && canSend && input.isNotBlank()) onSend() }),
                decorationBox = { inner ->
                    Box(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .heightIn(min = 44.dp, max = 120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(palette.elevatedPanel)
                                            .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        if (input.isBlank()) {
                            Text(
                                    text =
                                            if (enabled) "Ask about this project..."
                                            else "AI request in progress…",
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
                enabled = enabled && canSend && input.isNotBlank(),
                modifier =
                        Modifier.size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                        if (enabled && canSend && input.isNotBlank()) palette.accent
                                        else palette.tab
                                ),
        ) {
            Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send AI message",
                    tint = if (enabled && canSend && input.isNotBlank()) palette.onAccent else palette.mutedText,
            )
        }
    }
}

private data class BrowserPreviewLaunchResult(
        val url: String,
        val error: String? = null,
)

private fun startLivePreviewServer(
        entryFile: File,
        onLog: (String) -> Unit,
): BrowserPreviewLaunchResult {
    val previewDirectory =
            entryFile.parentFile
                    ?: return BrowserPreviewLaunchResult(
                            url = "",
                            error = "Preview directory is missing."
                    )
    val url =
            runCatching {
                LocalhostPreviewServer.serve(
                        directory = previewDirectory,
                        entryName = entryFile.name,
                        onLog = onLog,
                )
            }
                    .getOrElse { error ->
                        return BrowserPreviewLaunchResult(
                                url = "",
                                error = error.message ?: error::class.java.simpleName,
                        )
                    }

    return BrowserPreviewLaunchResult(url = url)
}

private fun openLivePreviewInDefaultBrowser(
        context: Context,
        entryFile: File,
        onLog: (String) -> Unit,
        beforeOpen: (String) -> Unit = {},
): BrowserPreviewLaunchResult {
    val previewResult = startLivePreviewServer(entryFile, onLog)
    if (previewResult.error != null) return previewResult

    beforeOpen(previewResult.url)
    val launchError = openUrlInDefaultBrowser(context, previewResult.url)
    return BrowserPreviewLaunchResult(url = previewResult.url, error = launchError)
}

private fun openUrlInDefaultBrowser(context: Context, url: String): String? {
    val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

    return runCatching { context.startActivity(intent) }
            .fold(
                    onSuccess = { null },
                    onFailure = { error -> error.message ?: error::class.java.simpleName },
            )
}

private object LocalhostPreviewServer {
    private val lock = Any()
    private var serverSocket: ServerSocket? = null
    private var serverThread: Thread? = null
    private var rootDirectory: File? = null
    private var entryFileName: String = "index.html"
    private var logSink: ((String) -> Unit)? = null
    private var port: Int = -1

    fun serve(directory: File, entryName: String, onLog: (String) -> Unit): String {
        val root = directory.canonicalFile
        synchronized(lock) {
            logSink = onLog
            val socket = serverSocket
            if (socket != null &&
                            !socket.isClosed &&
                            rootDirectory?.let { samePath(it.absolutePath, root.absolutePath) } ==
                                    true
            ) {
                entryFileName = entryName
                return previewUrl(port, entryName)
            }

            stopLocked()
            val loopback = InetAddress.getByName("127.0.0.1")
            val newSocket = ServerSocket(0, 50, loopback)
            serverSocket = newSocket
            rootDirectory = root
            entryFileName = entryName
            port = newSocket.localPort
            serverThread =
                    Thread { serveLoop(newSocket) }.apply {
                        name = "PocketCodedWebLocalhostPreview"
                        isDaemon = true
                        start()
                    }
            return previewUrl(port, entryName)
        }
    }

    private fun previewUrl(port: Int, entryName: String): String {
        val safeEntry = entryName.ifBlank { "index.html" }
        return "http://127.0.0.1:$port/${urlPathEncode(safeEntry)}"
    }

    private fun serveLoop(socket: ServerSocket) {
        while (!socket.isClosed) {
            val client = runCatching { socket.accept() }.getOrNull() ?: break
            Thread {
                client.use { connectedClient ->
                    try {
                        handleClient(connectedClient)
                    } catch (error: IOException) {
                        // Browsers routinely cancel favicon, reload, media-range, and stale
                        // navigation requests. A write to that closed socket is not a server
                        // failure and must not escape this request thread as an app crash.
                        if (!isPreviewClientDisconnect(error)) {
                            log(
                                    "[localhost] request ended: ${error.message ?: error.javaClass.simpleName}"
                            )
                        }
                    }
                }
            }
                    .apply {
                        name = "PocketCodedWebPreviewRequest"
                        isDaemon = true
                        start()
                    }
        }
    }

    private fun handleClient(client: Socket) {
        client.soTimeout = 15_000
        val input = client.getInputStream().bufferedReader(Charsets.ISO_8859_1)
        val output = BufferedOutputStream(client.getOutputStream())
        val requestLine = input.readLine().orEmpty()
        val parts = requestLine.split(' ')
        if (parts.size < 2) {
            writeTextResponse(output, 400, "Bad Request", "Bad request.")
            logRequest("?", "/", 400, "Bad Request")
            return
        }

        val method = parts[0].uppercase(Locale.US)
        val rawPath = parts[1]
        val headers = mutableMapOf<String, String>()
        while (true) {
            val line = input.readLine() ?: break
            if (line.isEmpty()) break
            val splitAt = line.indexOf(':')
            if (splitAt > 0) {
                headers[line.substring(0, splitAt).trim().lowercase(Locale.US)] =
                        line.substring(splitAt + 1).trim()
            }
        }

        if (method == "POST" && requestPathOnly(rawPath) == "/__pcw_console") {
            readRequestBody(input, headers)?.let(::decodePreviewConsolePayload)?.let(::log)
            writeHeaders(output, 204, "No Content", "text/plain; charset=utf-8", 0)
            return
        }

        if (method == "OPTIONS") {
            writeHeaders(output, 204, "No Content", "text/plain; charset=utf-8", 0)
            logRequest(method, rawPath, 204, "No Content")
            return
        }
        if (method != "GET" && method != "HEAD") {
            writeTextResponse(output, 405, "Method Not Allowed", "Method not allowed.")
            logRequest(method, rawPath, 405, "Method Not Allowed")
            return
        }

        val target = resolveRequestFile(rawPath)
        if (target == null || !target.isFile) {
            writeTextResponse(output, 404, "Not Found", "Not found.")
            logRequest(method, rawPath, 404, "Not Found")
            return
        }

        val range = parseByteRange(headers["range"], target.length())
        if (range == null && headers.containsKey("range")) {
            writeRangeNotSatisfiable(output, target.length())
            logRequest(method, rawPath, 416, "Range Not Satisfiable")
            return
        }

        val mimeType = previewMimeType(target)
        if (range != null) {
            writeFileRangeResponse(
                    output,
                    target,
                    mimeType,
                    range.first,
                    range.last,
                    method == "HEAD"
            )
            logRequest(method, rawPath, 206, "Partial Content")
        } else {
            writeFileResponse(output, target, mimeType, method == "HEAD")
            logRequest(method, rawPath, 200, "OK")
        }
    }

    private fun requestPathOnly(rawPath: String): String {
        return rawPath.substringBefore('?').substringBefore('#')
    }

    private fun readRequestBody(
            input: java.io.BufferedReader,
            headers: Map<String, String>,
    ): String? {
        val length = headers["content-length"]?.toIntOrNull()?.coerceIn(0, 64_000) ?: return null
        if (length == 0) return ""
        val buffer = CharArray(length)
        var offset = 0
        while (offset < length) {
            val read = input.read(buffer, offset, length - offset)
            if (read < 0) break
            offset += read
        }
        return String(buffer, 0, offset)
    }

    private fun decodePreviewConsolePayload(body: String): String? {
        val values =
                body.split('&')
                        .mapNotNull { pair ->
                            val splitAt = pair.indexOf('=')
                            if (splitAt < 0) return@mapNotNull null
                            val key = formDecode(pair.substring(0, splitAt))
                            val value = formDecode(pair.substring(splitAt + 1))
                            key to value
                        }
                        .toMap()
        val message = values["message"].orEmpty()
        if (message.isBlank()) return null
        return formatPreviewRuntimeMessage(
                level = values["level"].orEmpty().ifBlank { "log" },
                message = message,
                source = values["source"].orEmpty().ifBlank { "browser" },
                line = values["line"]?.toIntOrNull() ?: 0,
                column = values["column"]?.toIntOrNull() ?: 0,
        )
    }

    private fun formDecode(value: String): String {
        return runCatching { URLDecoder.decode(value, "UTF-8") }.getOrDefault(value)
    }

    private fun logRequest(method: String, rawPath: String, status: Int, message: String) {
        if (requestPathOnly(rawPath) == "/__pcw_console") return
        val path = rawPath.substringBefore('#').ifBlank { "/" }
        log("[localhost] $method $path -> $status $message")
    }

    private fun log(message: String) {
        synchronized(lock) { logSink }?.invoke(message)
    }

    private fun resolveRequestFile(rawPath: String): File? {
        val root = synchronized(lock) { rootDirectory } ?: return null
        val requestPath =
                rawPath.substringBefore('?').substringBefore('#').trimStart('/').ifBlank {
                    entryFileName.ifBlank { "index.html" }
                }
        val decodedPath =
                runCatching { URLDecoder.decode(requestPath.replace("+", "%2B"), "UTF-8") }
                        .getOrDefault(requestPath)
        val normalizedPath = decodedPath.replace('\\', '/').trimStart('/')
        if (normalizedPath.split('/').any { it == ".." }) return null
        val candidate = File(root, normalizedPath).canonicalFile
        if (!isSameOrInside(root, candidate)) return null
        return if (candidate.isDirectory) {
            File(candidate, "index.html").canonicalFile.takeIf { isSameOrInside(root, it) }
        } else {
            candidate
        }
    }

    private fun parseByteRange(rangeHeader: String?, fileLength: Long): LongRange? {
        if (rangeHeader.isNullOrBlank() || !rangeHeader.startsWith("bytes=")) return null
        val rangeText = rangeHeader.removePrefix("bytes=").substringBefore(',').trim()
        val separator = rangeText.indexOf('-')
        if (separator < 0) return null
        val startText = rangeText.substring(0, separator).trim()
        val endText = rangeText.substring(separator + 1).trim()
        val start =
                if (startText.isBlank()) {
                    val suffixLength = endText.toLongOrNull() ?: return null
                    (fileLength - suffixLength).coerceAtLeast(0L)
                } else {
                    startText.toLongOrNull() ?: return null
                }
        val end =
                if (endText.isBlank()) {
                            fileLength - 1
                        } else {
                            endText.toLongOrNull() ?: return null
                        }
                        .coerceAtMost(fileLength - 1)
        if (fileLength <= 0L || start !in 0L until fileLength || end < start) return null
        return start..end
    }

    private fun writeFileResponse(
            output: BufferedOutputStream,
            file: File,
            mimeType: String,
            headOnly: Boolean
    ) {
        writeHeaders(output, 200, "OK", mimeType, file.length())
        if (!headOnly) {
            FileInputStream(file).use { input -> input.copyTo(output) }
        }
        output.flush()
    }

    private fun writeFileRangeResponse(
            output: BufferedOutputStream,
            file: File,
            mimeType: String,
            start: Long,
            end: Long,
            headOnly: Boolean,
    ) {
        val contentLength = end - start + 1
        val headers = buildString {
            append("HTTP/1.1 206 Partial Content\r\n")
            append("Content-Type: ").append(mimeType).append("\r\n")
            append("Content-Length: ").append(contentLength).append("\r\n")
            append("Content-Range: bytes ")
                    .append(start)
                    .append('-')
                    .append(end)
                    .append('/')
                    .append(file.length())
                    .append("\r\n")
            append(commonPreviewHeaders())
            append("\r\n")
        }
        output.write(headers.toByteArray(Charsets.UTF_8))
        if (!headOnly) {
            FileInputStream(file).use { input ->
                skipFully(input, start)
                copyLimited(input, output, contentLength)
            }
        }
        output.flush()
    }

    private fun writeRangeNotSatisfiable(output: BufferedOutputStream, fileLength: Long) {
        val response = buildString {
            append("HTTP/1.1 416 Range Not Satisfiable\r\n")
            append("Content-Range: bytes */").append(fileLength).append("\r\n")
            append("Content-Length: 0\r\n")
            append(commonPreviewHeaders())
            append("\r\n")
        }
        output.write(response.toByteArray(Charsets.UTF_8))
        output.flush()
    }

    private fun writeTextResponse(
            output: BufferedOutputStream,
            code: Int,
            message: String,
            body: String
    ) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        writeHeaders(output, code, message, "text/plain; charset=utf-8", bytes.size.toLong())
        output.write(bytes)
        output.flush()
    }

    private fun writeHeaders(
            output: BufferedOutputStream,
            code: Int,
            message: String,
            contentType: String,
            contentLength: Long,
    ) {
        val headers = buildString {
            append("HTTP/1.1 ").append(code).append(' ').append(message).append("\r\n")
            append("Content-Type: ").append(contentType).append("\r\n")
            append("Content-Length: ").append(contentLength).append("\r\n")
            append(commonPreviewHeaders())
            append("\r\n")
        }
        output.write(headers.toByteArray(Charsets.UTF_8))
        output.flush()
    }

    private fun commonPreviewHeaders(): String {
        return buildString {
            append("Access-Control-Allow-Origin: *\r\n")
            append("Access-Control-Allow-Methods: GET, HEAD, OPTIONS\r\n")
            append("Access-Control-Allow-Headers: Content-Type, Range\r\n")
            append("Accept-Ranges: bytes\r\n")
            append("Cache-Control: no-store, no-cache, must-revalidate\r\n")
            append("Pragma: no-cache\r\n")
            append("Connection: close\r\n")
        }
    }

    private fun stopLocked() {
        runCatching { serverSocket?.close() }
        serverSocket = null
        serverThread = null
        rootDirectory = null
        logSink = null
        port = -1
    }
}

internal fun isPreviewClientDisconnect(error: Throwable): Boolean {
    var current: Throwable? = error
    while (current != null) {
        val message = current.message.orEmpty().lowercase(Locale.US)
        if (current is SocketException &&
                        (message.contains("broken pipe") ||
                                message.contains("connection reset") ||
                                message.contains("socket closed") ||
                                message.contains("connection abort"))
        ) {
            return true
        }
        current = current.cause
    }
    return false
}

private fun urlPathEncode(path: String): String {
    return path.split('/').joinToString("/") { segment ->
        URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
    }
}

private fun previewMimeType(file: File): String {
    return when (file.extension.lowercase(Locale.US)) {
        "html", "htm" -> "text/html; charset=utf-8"
        "css" -> "text/css; charset=utf-8"
        "js", "mjs", "cjs", "jsx", "ts", "tsx" -> "text/javascript; charset=utf-8"
        "json", "map" -> "application/json; charset=utf-8"
        "txt", "md" -> "text/plain; charset=utf-8"
        "svg" -> "image/svg+xml"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "ico" -> "image/x-icon"
        "wasm" -> "application/wasm"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "mp4" -> "video/mp4"
        "webm" -> "video/webm"
        "woff" -> "font/woff"
        "woff2" -> "font/woff2"
        "ttf" -> "font/ttf"
        "otf" -> "font/otf"
        else -> "application/octet-stream"
    }
}

private fun copyLimited(input: InputStream, output: BufferedOutputStream, byteCount: Long) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var remaining = byteCount
    while (remaining > 0L) {
        val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
        if (read == -1) return
        output.write(buffer, 0, read)
        remaining -= read
    }
}

private fun prepareLivePreviewEntry(
        rootDirectory: File,
        files: List<WebFile>,
        activeFile: WebFile,
): File {
    val root = rootDirectory.canonicalFile
    val previewDir = File(root, ".pocketcodedweb/preview").apply { mkdirs() }
    val entryFile = File(previewDir, "index.html")
    val signatureFile = File(previewDir, "signature.txt")
    val previewSignature = livePreviewSignature(root, files, activeFile)

    if (entryFile.isFile && runCatching { signatureFile.readText() }.getOrNull() == previewSignature
    ) {
        return entryFile
    }

    // 1. Save changed project files to disk so assets like images are available.
    syncPreviewProjectFilesToDisk(root, files)
    syncPreviewBrowsableAssets(root, previewDir)
    val filesByCanonicalPath = buildFilesByCanonicalPath(files)

    // 2. Identify the base HTML
    val activeExtension = fileExtension(activeFile.name)
    val baseHtml =
            when {
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
    val bundledHtml =
            if (baseHtml.contains("<html", ignoreCase = true) || looksLikeHtmlSource(baseHtml)) {
                inlinePreviewProjectAssets(
                        root,
                        filesByCanonicalPath,
                        File(activeFile.filePath),
                        baseHtml
                )
            } else {
                bundleProjectAssets(baseHtml, files, activeFile)
            }

    // 4. Inject preview browser compatibility, terminal logging, and responsive baseline.
    val finalHtml =
            injectLivePreviewLogBridge(
                    injectLivePreviewCompatibility(injectResponsiveBaseline(bundledHtml))
            )

    // 5. Write the final bundled HTML to a temp preview file and return it.
    if (!entryFile.isFile || runCatching { entryFile.readText() }.getOrNull() != finalHtml) {
        entryFile.writeText(finalHtml)
    }
    signatureFile.writeText(previewSignature)
    return entryFile
}

private fun livePreviewSignature(
        rootDirectory: File,
        files: List<WebFile>,
        activeFile: WebFile,
): String = buildString {
    append("v").append(LIVE_PREVIEW_CACHE_VERSION)
    append("|active|")
    append(relativeWorkspacePath(rootDirectory, File(activeFile.filePath)))
    append('|').append(activeFile.modifiedAt)
    append('|').append(activeFile.sizeBytes)
    append('|').append(activeFile.contentHash)

    files.sortedBy { it.filePath }.forEach { file ->
        append('\n')
        append(relativeWorkspacePath(rootDirectory, File(file.filePath)))
        append('|').append(file.modifiedAt)
        append('|').append(file.sizeBytes)
        append('|').append(file.contentHash)
    }

    rootDirectory
            .walkTopDown()
            .onEnter { file -> file == rootDirectory || !isProjectIgnoredDirectory(file) }
            .filter { it.isFile && it.length() <= LIVE_PREVIEW_MAX_BROWSER_ASSET_BYTES }
            .sortedBy { relativeWorkspacePath(rootDirectory, it) }
            .forEach { file ->
                append('\n')
                append("asset|").append(relativeWorkspacePath(rootDirectory, file))
                append('|').append(file.lastModified())
                append('|').append(file.length())
            }
}

private fun syncPreviewProjectFilesToDisk(rootDirectory: File, files: List<WebFile>) {
    files.filterNot { isDatabaseFileName(it.name) }.forEach { file ->
        val target = runCatching { File(file.filePath).canonicalFile }.getOrNull() ?: return@forEach
        if (!isSameOrInside(rootDirectory, target)) return@forEach
        if (target.isFile && target.lastModified() == file.modifiedAt) return@forEach

        target.parentFile?.mkdirs()
        target.writeText(file.content)
        target.setLastModified(file.modifiedAt)
        ProjectFileCache.invalidate(target)
    }
}

private fun syncPreviewBrowsableAssets(rootDirectory: File, previewDirectory: File) {
    val root = rootDirectory.canonicalFile
    val preview = previewDirectory.canonicalFile

    root.walkTopDown()
            .onEnter { file ->
                file == root || (!isProjectIgnoredDirectory(file) && !isSameOrInside(preview, file))
            }
            .filter { file ->
                file.isFile &&
                        !isSameOrInside(preview, file) &&
                        file.length() <= LIVE_PREVIEW_MAX_BROWSER_ASSET_BYTES
            }
            .forEach { source ->
                val relativePath =
                        runCatching { source.canonicalFile.relativeTo(root).path }.getOrNull()
                                ?: return@forEach
                val target = File(preview, relativePath).canonicalFile
                if (!isSameOrInside(preview, target)) return@forEach
                if (target.isFile &&
                                target.length() == source.length() &&
                                target.lastModified() == source.lastModified()
                ) {
                    return@forEach
                }
                target.parentFile?.mkdirs()
                runCatching {
                    source.copyTo(target, overwrite = true)
                    target.setLastModified(source.lastModified())
                }
            }
}

private fun buildFilesByCanonicalPath(files: List<WebFile>): Map<String, WebFile> {
    val result = mutableMapOf<String, WebFile>()
    files.forEach { file ->
        val path = runCatching { File(file.filePath).canonicalFile.absolutePath }.getOrNull()
        if (path != null) result[path] = file
    }
    return result
}

private fun bundleProjectAssets(html: String, files: List<WebFile>, activeFile: WebFile): String {
    val cssFiles = files.filter { fileExtension(it.name) in cssFileExtensions }
    val jsFiles = files.filter { fileExtension(it.name) in javascriptFileExtensions }
    val cssBlock = cssFiles.joinToString("\n\n") { "/* ${it.name} */\n${it.content}" }.trim()

    val jsBlock =
            jsFiles
                    .joinToString("\n\n") { "// ${it.name}\n${it.content}" }
                    .replace("</script", "<\\/script", ignoreCase = true)
                    .trim()

    val needsTranspilation =
            jsFiles.any {
                fileExtension(it.name) in setOf("ts", "tsx", "jsx") ||
                        JS_MODULE_SYNTAX_REGEX.containsMatchIn(it.content)
            }

    var result = html
    if (cssBlock.isNotBlank()) {
        result =
                injectIntoHeadEnd(
                        result,
                        "\n<style id=\"pcw-bundled-css\" data-pcw-source=\"project-css\">\n$cssBlock\n</style>\n"
                )
    }
    if (jsBlock.isNotBlank()) {
        val scriptType = if (needsTranspilation) "text/babel" else "text/javascript"
        val babelPresets = if (needsTranspilation) " data-presets=\"react,typescript,env\"" else ""

        if (needsTranspilation && !result.contains("babel.min.js")) {
            val babelScript =
                    "\n<script src=\"https://unpkg.com/@babel/standalone@7.23.11/babel.min.js\"></script>\n"
            result = injectIntoHeadStart(result, babelScript)
        }

        result =
                injectIntoBodyEnd(
                        result,
                        "\n<script id=\"pcw-bundled-js\" type=\"$scriptType\"$babelPresets data-pcw-source=\"project-js\">\n$jsBlock\n</script>\n"
                )
    }
    return result
}

private fun inlinePreviewProjectAssets(
        rootDirectory: File,
        filesByCanonicalPath: Map<String, WebFile>,
        sourceOwner: File,
        html: String,
): String {
    val withStyles =
            Regex("""(?is)<link\b[^>]*>""").replace(html) { match ->
                val tag = match.value
                val rel = htmlAttributeValue(tag, "rel").orEmpty()
                val href = htmlAttributeValue(tag, "href") ?: return@replace tag
                if (!rel.contains("stylesheet", ignoreCase = true)) return@replace tag
                val file =
                        resolvePreviewFile(rootDirectory, sourceOwner, href)?.let { target ->
                            filesByCanonicalPath[target.canonicalFile.absolutePath]
                        }
                                ?: return@replace tag
                if (fileExtension(file.name) !in cssFileExtensions) return@replace tag
                """<style id="pcw-inlined-${file.name}" data-pcw-source="${escapeHtmlAttribute(href)}">
${file.content}
</style>"""
            }
    return Regex("""(?is)<script\b([^>]*)>(.*?)</script>""").replace(withStyles) { match ->
        val attributes = match.groupValues[1]
        val src = htmlAttributeValue(attributes, "src") ?: return@replace match.value
        val file =
                resolvePreviewFile(rootDirectory, sourceOwner, src)?.let { target ->
                    filesByCanonicalPath[target.canonicalFile.absolutePath]
                }
                        ?: return@replace match.value
        if (fileExtension(file.name) !in javascriptFileExtensions) return@replace match.value
        val cleanedAttributes = removeHtmlAttribute(attributes, "src")
        val needsTranspilation =
                fileExtension(file.name) in setOf("ts", "tsx", "jsx") ||
                        JS_MODULE_SYNTAX_REGEX.containsMatchIn(file.content)
        val type =
                if (needsTranspilation) " type=\"text/babel\" data-presets=\"react,typescript,env\""
                else cleanedAttributes
        val escapedContent = file.content.replace("</script", "<\\/script", ignoreCase = true)
        """<script$type data-pcw-source="${escapeHtmlAttribute(src)}">
$escapedContent
//# sourceURL=${relativeWorkspacePath(rootDirectory, File(file.filePath))}
</script>"""
    }
}

private fun htmlAttributeValue(tagText: String, attributeName: String): String? {
    val escapedName = Regex.escape(attributeName)
    return Regex("""(?is)\b$escapedName\s*=\s*(["'])(.*?)\1""")
            .find(tagText)
            ?.groupValues
            ?.getOrNull(2)
            ?: Regex("""(?is)\b$escapedName\s*=\s*([^\s"'=<>`]+)""")
                    .find(tagText)
                    ?.groupValues
                    ?.getOrNull(1)
}

private fun removeHtmlAttribute(tagText: String, attributeName: String): String {
    val escapedName = Regex.escape(attributeName)
    return Regex("""(?is)\s*\b$escapedName\s*=\s*(?:"[^"]*"|'[^']*'|[^\s"'=<>`]+)""")
            .replace(tagText, "")
}

private fun injectIntoHeadStart(html: String, content: String): String {
    val headOpen = Regex("""(?is)<head\b[^>]*>""").find(html)
    return if (headOpen != null) {
        html.replaceRange(headOpen.range.last + 1, headOpen.range.last + 1, content)
    } else if (html.contains("<html", ignoreCase = true)) {
        html.replace(
                Regex("<html([^>]*)>", RegexOption.IGNORE_CASE),
                "<html$1><head>$content</head>"
        )
    } else {
        "<!DOCTYPE html><html><head>$content</head><body>$html</body></html>"
    }
}

private fun injectLivePreviewLogBridge(html: String): String {
    val bridge =
            """
        <script id="pcw-live-preview-log-bridge">
            (function () {
                if (window.__pcwLivePreviewLogBridgeInstalled) return;
                window.__pcwLivePreviewLogBridgeInstalled = true;

                var endpoint = "/__pcw_console";
                var originalConsole = {
                    log: console.log,
                    info: console.info,
                    warn: console.warn,
                    error: console.error
                };

                function encode(value) {
                    return encodeURIComponent(value == null ? "" : String(value));
                }

                function stringify(value) {
                    if (value instanceof Error) return value.stack || value.message || String(value);
                    if (typeof value === "string") return value;
                    if (value == null) return String(value);
                    try {
                        return JSON.stringify(value);
                    } catch (error) {
                        return String(value);
                    }
                }

                function stackLocation() {
                    var stack = "";
                    try {
                        throw new Error();
                    } catch (error) {
                        stack = error && error.stack || "";
                    }
                    var lines = stack.split("\n").slice(2);
                    for (var index = 0; index < lines.length; index++) {
                        var line = lines[index];
                        if (line.indexOf("pcw-live-preview-log-bridge") >= 0) continue;
                        var match = line.match(/(?:\()?(https?:\/\/[^)\s]+|[^)\s]+):(\d+):(\d+)\)?/);
                        if (match) {
                            return {
                                source: match[1].split("/").pop() || match[1],
                                line: match[2],
                                column: match[3]
                            };
                        }
                    }
                    return { source: "browser", line: "0", column: "0" };
                }

                function post(level, message, source, line, column) {
                    var body = "level=" + encode(level) +
                        "&message=" + encode(message) +
                        "&source=" + encode(source || "browser") +
                        "&line=" + encode(line || 0) +
                        "&column=" + encode(column || 0);
                    try {
                        if (navigator.sendBeacon) {
                            var blob = new Blob([body], { type: "application/x-www-form-urlencoded" });
                            if (navigator.sendBeacon(endpoint, blob)) return;
                        }
                    } catch (error) {}
                    try {
                        fetch(endpoint, {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: body,
                            keepalive: true
                        }).catch(function () {});
                    } catch (error) {}
                }

                function forward(level, args) {
                    var location = stackLocation();
                    post(
                        level,
                        Array.prototype.slice.call(args).map(stringify).join(" "),
                        location.source,
                        location.line,
                        location.column
                    );
                }

                ["log", "info", "warn", "error"].forEach(function (level) {
                    console[level] = function () {
                        try {
                            originalConsole[level].apply(console, arguments);
                        } catch (error) {}
                        forward(level, arguments);
                    };
                });

                window.addEventListener("error", function (event) {
                    var message = event.message || (event.error && (event.error.stack || event.error.message)) || "Script error";
                    post("error", message, event.filename || "browser", event.lineno || 0, event.colno || 0);
                });

                window.addEventListener("unhandledrejection", function (event) {
                    var reason = event.reason;
                    post("error", "Unhandled promise rejection: " + stringify(reason), "browser", 0, 0);
                });

                var lastVisibilityState = document.visibilityState || "visible";
                document.addEventListener("visibilitychange", function () {
                    var state = document.visibilityState || "visible";
                    if (state === lastVisibilityState) return;
                    lastVisibilityState = state;
                    post("info", "Preview tab " + state, "browser", 0, 0);
                });

                window.addEventListener("pagehide", function () {
                    post("info", "Preview page closed or backgrounded", "browser", 0, 0);
                });
            })();
        </script>
    """.trimIndent()

    return injectIntoHeadStart(html, bridge)
}

private fun injectLivePreviewCompatibility(html: String): String {
    val compatibility =
            """
        <script id="pcw-live-preview-compatibility">
            (function () {
                if (window.__pcwLivePreviewCompatibilityInstalled) return;
                window.__pcwLivePreviewCompatibilityInstalled = true;

                var fullscreenElement = null;
                var savedNoFitValue;
                var savedNoFitWasSet = false;
                var pointerLockElement = null;

                function createEvent(name) {
                    try {
                        return new Event(name, { bubbles: true, cancelable: false });
                    } catch (error) {
                        var event = document.createEvent("Event");
                        event.initEvent(name, true, false);
                        return event;
                    }
                }

                function dispatch(target, name) {
                    try {
                        (target || document).dispatchEvent(createEvent(name));
                    } catch (error) {}
                }

                function setGetter(owner, name, getter) {
                    try {
                        Object.defineProperty(owner, name, {
                            configurable: true,
                            enumerable: true,
                            get: getter
                        });
                    } catch (error) {}
                }

                function setMethod(owner, name, method) {
                    try {
                        Object.defineProperty(owner, name, {
                            configurable: true,
                            writable: true,
                            value: method
                        });
                    } catch (error) {
                        try { owner[name] = method; } catch (ignored) {}
                    }
                }

                function addCompatibilityStyle() {
                    if (document.getElementById("pcw-live-preview-compatibility-style")) return;
                    var style = document.createElement("style");
                    style.id = "pcw-live-preview-compatibility-style";
                    style.textContent = [
                        "html.pcw-preview-fullscreen-active,body.pcw-preview-fullscreen-active{width:100%!important;height:100%!important;margin:0!important;padding:0!important;overflow:hidden!important;}",
                        "[data-pcw-preview-fullscreen='true']{box-sizing:border-box!important;overscroll-behavior:none!important;}"
                    ].join("\n");
                    (document.head || document.documentElement).appendChild(style);
                }

                function rememberStyle(element) {
                    if (!element || element.dataset.pcwPreviewFullscreenStyleSet === "true") return;
                    element.dataset.pcwPreviewFullscreenStyle = element.getAttribute("style") || "";
                    element.dataset.pcwPreviewFullscreenStyleSet = "true";
                }

                function restoreStyle(element) {
                    if (!element || element.dataset.pcwPreviewFullscreenStyleSet !== "true") return;
                    var original = element.dataset.pcwPreviewFullscreenStyle || "";
                    if (original) {
                        element.setAttribute("style", original);
                    } else {
                        element.removeAttribute("style");
                    }
                    delete element.dataset.pcwPreviewFullscreenStyle;
                    delete element.dataset.pcwPreviewFullscreenStyleSet;
                    delete element.dataset.pcwPreviewFullscreen;
                }

                function applyFullscreenStyle(element) {
                    rememberStyle(element);
                    addCompatibilityStyle();
                    document.documentElement.classList.add("pcw-preview-fullscreen-active");
                    if (document.body) document.body.classList.add("pcw-preview-fullscreen-active");
                    element.dataset.pcwPreviewFullscreen = "true";
                    element.style.position = "fixed";
                    element.style.left = "0";
                    element.style.top = "0";
                    element.style.width = "100vw";
                    element.style.height = "100vh";
                    element.style.maxWidth = "none";
                    element.style.maxHeight = "none";
                    element.style.margin = "0";
                    element.style.padding = "0";
                    element.style.transform = "none";
                    element.style.transformOrigin = "center center";
                    element.style.overflow = "hidden";
                    element.style.zIndex = "2147483647";
                    element.style.background = element.style.background || (document.body && getComputedStyle(document.body).backgroundColor) || "#000";
                }

                function refreshPreviewLayout() {
                    setTimeout(function () {
                        dispatch(window, "resize");
                        if (typeof window.__pcwLivePreviewApplyFit === "function") {
                            window.__pcwLivePreviewApplyFit();
                        }
                    }, 0);
                }

                function exitPreviewFullscreen() {
                    if (!fullscreenElement) return Promise.resolve();
                    var previous = fullscreenElement;
                    fullscreenElement = null;
                    restoreStyle(previous);
                    document.documentElement.classList.remove("pcw-preview-fullscreen-active");
                    if (document.body) document.body.classList.remove("pcw-preview-fullscreen-active");
                    if (savedNoFitWasSet) {
                        if (typeof savedNoFitValue === "undefined") {
                            delete document.documentElement.dataset.pcwNoFit;
                        } else {
                            document.documentElement.dataset.pcwNoFit = savedNoFitValue;
                        }
                    }
                    savedNoFitWasSet = false;
                    savedNoFitValue = undefined;
                    dispatch(document, "fullscreenchange");
                    dispatch(document, "webkitfullscreenchange");
                    dispatch(previous, "fullscreenchange");
                    refreshPreviewLayout();
                    return Promise.resolve();
                }

                function requestPreviewFullscreen(element) {
                    element = element && element.nodeType === 1 ? element : document.documentElement;
                    if (fullscreenElement && fullscreenElement !== element) {
                        exitPreviewFullscreen();
                    }
                    fullscreenElement = element;
                    if (!savedNoFitWasSet) {
                        savedNoFitValue = document.documentElement.dataset.pcwNoFit;
                        savedNoFitWasSet = true;
                    }
                    document.documentElement.dataset.pcwNoFit = "true";
                    applyFullscreenStyle(element);
                    dispatch(document, "fullscreenchange");
                    dispatch(document, "webkitfullscreenchange");
                    dispatch(element, "fullscreenchange");
                    refreshPreviewLayout();
                    return Promise.resolve();
                }

                setGetter(document, "fullscreenEnabled", function () { return true; });
                setGetter(document, "webkitFullscreenEnabled", function () { return true; });
                setGetter(document, "mozFullScreenEnabled", function () { return true; });
                setGetter(document, "msFullscreenEnabled", function () { return true; });
                setGetter(document, "fullscreenElement", function () { return fullscreenElement; });
                setGetter(document, "webkitFullscreenElement", function () { return fullscreenElement; });
                setGetter(document, "mozFullScreenElement", function () { return fullscreenElement; });
                setGetter(document, "msFullscreenElement", function () { return fullscreenElement; });

                setMethod(document, "exitFullscreen", exitPreviewFullscreen);
                setMethod(document, "webkitExitFullscreen", exitPreviewFullscreen);
                setMethod(document, "mozCancelFullScreen", exitPreviewFullscreen);
                setMethod(document, "msExitFullscreen", exitPreviewFullscreen);

                function installElementPreviewMethods(owner) {
                    if (!owner) return;
                    setMethod(owner, "requestFullscreen", function () { return requestPreviewFullscreen(this); });
                    setMethod(owner, "webkitRequestFullscreen", function () { return requestPreviewFullscreen(this); });
                    setMethod(owner, "webkitRequestFullScreen", function () { return requestPreviewFullscreen(this); });
                    setMethod(owner, "mozRequestFullScreen", function () { return requestPreviewFullscreen(this); });
                    setMethod(owner, "msRequestFullscreen", function () { return requestPreviewFullscreen(this); });
                    setMethod(owner, "requestPointerLock", function () {
                        pointerLockElement = this;
                        dispatch(document, "pointerlockchange");
                    });
                    setMethod(owner, "webkitRequestPointerLock", function () {
                        pointerLockElement = this;
                        dispatch(document, "pointerlockchange");
                    });
                }

                if (window.Element && Element.prototype) installElementPreviewMethods(Element.prototype);
                if (window.HTMLElement && HTMLElement.prototype) installElementPreviewMethods(HTMLElement.prototype);
                if (window.SVGElement && SVGElement.prototype) installElementPreviewMethods(SVGElement.prototype);

                function installPreviewMethodsOnExistingElements(root) {
                    try {
                        if (root && root.nodeType === 1) installElementPreviewMethods(root);
                        Array.prototype.forEach.call(
                            (root || document).querySelectorAll("*"),
                            installElementPreviewMethods
                        );
                    } catch (error) {}
                }

                if (document.readyState === "loading") {
                    document.addEventListener("DOMContentLoaded", function () {
                        installPreviewMethodsOnExistingElements(document);
                    });
                } else {
                    installPreviewMethodsOnExistingElements(document);
                }

                try {
                    new MutationObserver(function (records) {
                        records.forEach(function (record) {
                            Array.prototype.forEach.call(record.addedNodes || [], function (node) {
                                installPreviewMethodsOnExistingElements(node);
                            });
                        });
                    }).observe(document.documentElement, { childList: true, subtree: true });
                } catch (error) {}

                setGetter(document, "pointerLockElement", function () { return pointerLockElement; });
                setGetter(document, "webkitPointerLockElement", function () { return pointerLockElement; });
                setMethod(document, "exitPointerLock", function () {
                    pointerLockElement = null;
                    dispatch(document, "pointerlockchange");
                });
                setMethod(document, "webkitExitPointerLock", function () {
                    pointerLockElement = null;
                    dispatch(document, "pointerlockchange");
                });

                try {
                    var orientation = screen.orientation || screen;
                    if (orientation) {
                        setMethod(orientation, "lock", function () { return Promise.resolve(); });
                        setMethod(orientation, "unlock", function () {});
                    }
                } catch (error) {}

                if (!navigator.wakeLock) {
                    try {
                        Object.defineProperty(navigator, "wakeLock", {
                            configurable: true,
                            value: {
                                request: function () {
                                    return Promise.resolve({
                                        released: false,
                                        release: function () {
                                            this.released = true;
                                            return Promise.resolve();
                                        },
                                        addEventListener: function () {},
                                        removeEventListener: function () {}
                                    });
                                }
                            }
                        });
                    } catch (error) {}
                }
            })();
        </script>
    """.trimIndent()

    return injectIntoHeadStart(html, compatibility)
}

private fun injectResponsiveBaseline(html: String): String {
    val baseline =
            """
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

private fun isTerminalRunnableScript(fileName: String): Boolean {
    return fileExtension(fileName) in javascriptFileExtensions
}

private fun isLivePreviewFile(fileName: String): Boolean {
    return fileExtension(fileName) in htmlFileExtensions ||
            fileExtension(fileName) in cssFileExtensions
}

private fun runButtonLabelFor(file: WebFile?): String {
    return when {
        file != null && isDatabaseFileName(file.name) -> "Open database"
        file != null && isLivePreviewFile(file.name) -> "Preview"
        else -> "Run Code"
    }
}

private fun terminalRunnableJavaScript(file: WebFile, rootDirectory: File): String {
    val sourceLabel = relativeWorkspacePath(rootDirectory, File(file.filePath))
    return buildString {
        append(file.content)
        append("\n\n//# sourceURL=")
        append(sourceLabel.replace("\n", " "))
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
            modifier =
                    Modifier.fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.elevatedPanel)
                            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
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
        Spacer(Modifier.width(6.dp))
        AddFileButton(palette = palette, onAddFile = onAddFile)
        Spacer(Modifier.width(6.dp))
        Box(
                modifier =
                        Modifier.size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.tab)
                                .border(
                                        BorderStroke(1.dp, palette.border),
                                        RoundedCornerShape(10.dp)
                                )
                                .clickable(onClick = onToggleExplorer),
                contentAlignment = Alignment.Center,
        ) {
            Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Explorer",
                    tint = palette.text,
                    modifier = Modifier.size(22.dp),
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
            modifier =
                    Modifier.height(38.dp)
                            .width(126.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) palette.selectedTab else palette.tab)
                            .border(
                                    BorderStroke(
                                            1.dp,
                                            if (selected) palette.accent else palette.border
                                    ),
                                    RoundedCornerShape(10.dp),
                            )
                            .clickable(onClick = onSelect)
                            .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        WebFileIcon(file.name, palette, Modifier.size(17.dp))
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
    Box(
            modifier =
                    Modifier.size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.tab)
                            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(10.dp))
                            .clickable(onClick = onAddFile),
            contentAlignment = Alignment.Center,
    ) {
        Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "New file",
                tint = palette.text,
                modifier = Modifier.size(23.dp),
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
            modifier =
                    Modifier.fillMaxWidth()
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
    Surface(
            color = palette.elevatedPanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, palette.border)
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                    Icons.Rounded.Search,
                    null,
                    tint = palette.subtleText,
                    modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = palette.text, fontSize = 13.sp),
                    cursorBrush = SolidColor(palette.accent),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (value.isBlank())
                                Text(placeholder, color = palette.subtleText, fontSize = 13.sp)
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
                            onValueChange = {
                                value = it
                                error = null
                            },
                            label = { Text(label) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions =
                                    KeyboardActions(onDone = { error = onConfirm(value) }),
                            modifier = Modifier.fillMaxWidth(),
                        colors = readableFieldColors(),
                    )
                    error?.let {
                        Text(
                                it,
                                color = palette.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { error = onConfirm(value) },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent, disabledContainerColor = palette.tab, disabledContentColor = palette.mutedText),
                        shape = RoundedCornerShape(8.dp),
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, colors = readableTextButtonColors()) { Text("Cancel", color = palette.text) }
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
                TextButton(onClick = onConfirm, colors = readableTextButtonColors()) { Text("Delete", color = palette.error) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, colors = readableTextButtonColors()) { Text("Cancel", color = palette.text) }
            },
    )
}

@Composable
private fun Panel(
        palette: WebPalette,
        modifier: Modifier = Modifier,
        backgroundColor: Color = palette.panel,
        content: @Composable ColumnScope.() -> Unit,
) {
    Column(
            modifier =
                    modifier.clip(RoundedCornerShape(14.dp))
                            .background(backgroundColor)
                            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(14.dp))
                            .padding(10.dp),
            content = content,
    )
}

@Composable
private fun HorizontalRule(palette: WebPalette) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(palette.divider))
}

private object OfflineWebLspServer {
    private val completionCache =
            SmallLruCache<String, EditorCompletionResult>(LSP_CACHE_ENTRY_LIMIT)
    private val diagnosticsCache =
            SmallLruCache<String, List<CodeDiagnostic>>(LSP_CACHE_ENTRY_LIMIT)
    private val highlightCache = SmallLruCache<String, EditorVisualResult>(LSP_CACHE_ENTRY_LIMIT)
    private val formatCache = SmallLruCache<String, String>(LSP_CACHE_ENTRY_LIMIT)

    fun status(): String {
        return buildString {
            append("Offline LSP: active")
            append('\n').append("Languages: ").append(offlineLspSupportedLanguageSummary())
            append('\n')
                    .append(
                            "Capabilities: completion, diagnostics, formatting, syntax highlighting, signature help"
                    )
            append('\n').append("Network: not required")
        }
    }

    fun completion(
            file: File,
            fileName: String,
            source: String,
            rawCursor: Int
    ): EditorCompletionResult {
        val cursor = rawCursor.coerceIn(0, source.length)
        val key = "offline-completion|$fileName|${source.length}|${source.hashCode()}|$cursor"
        return completionCache.getOrPutValue(key) {
            val context =
                    completionContextAt(source, cursor, fileName)
                            ?: plainCompletionContextAt(source, cursor)
            EditorCompletionResult(
                    context = context,
                    items = webCompletionsForContext(fileName, source, context),
                    source = source,
                    cursor = cursor,
                    signatureHelp =
                            if (shouldComputeSignatureHelp(source, cursor)) {
                                webSignatureHelpAt(fileName, source, cursor)
                            } else {
                                null
                            },
            )
        }
    }

    fun diagnostics(file: File, fileName: String, source: String): List<CodeDiagnostic> {
        val key = "offline-diagnostics|$fileName|${source.length}|${source.hashCode()}"
        return diagnosticsCache.getOrPutValue(key) { inspectWebSource(fileName, source) }
    }

    fun highlight(
            fileName: String,
            source: String,
            palette: WebPalette,
            diagnostics: List<CodeDiagnostic>,
    ): EditorVisualResult {
        val key =
                "offline-highlight|$fileName|${source.length}|${source.hashCode()}|${palette.hashCode()}|${diagnostics.hashCode()}"
        return highlightCache.getOrPutValue(key) {
            EditorVisualResult(
                    source = source,
                    text =
                            addDiagnosticStyles(
                                    source = source,
                                    highlighted = highlightWebSource(fileName, source, palette),
                                    palette = palette,
                                    diagnostics = diagnostics,
                            ),
            )
        }
    }

    fun format(file: File, fileName: String, source: String): String {
        val key = "offline-format|$fileName|${source.length}|${source.hashCode()}"
        return formatCache.getOrPutValue(key) { formatWebSource(fileName, source) }
    }

    fun clearCaches() {
        completionCache.clearValues()
        diagnosticsCache.clearValues()
        highlightCache.clearValues()
        formatCache.clearValues()
    }
}

private object WebLanguageServices {
    fun completion(
            file: File,
            fileName: String,
            source: String,
            rawCursor: Int
    ): EditorCompletionResult {
        val cursor = rawCursor.coerceIn(0, source.length)
        val offlineResult = OfflineWebLspServer.completion(file, fileName, source, cursor)
        if (offlineResult.items.isNotEmpty()) {
            return offlineResult
        }
        if (source.length > EXTERNAL_LSP_LARGE_FILE_LIMIT) {
            return offlineResult
        }
        val externalItems = ExternalLspManager.completions(file, fileName, source, cursor)
        if (!externalItems.isNullOrEmpty()) {
            return EditorCompletionResult(
                    context = completionContextAt(source, cursor, fileName)
                                    ?: plainCompletionContextAt(source, cursor),
                    items = externalItems,
                    source = source,
                    cursor = cursor,
                    signatureHelp =
                            if (shouldComputeSignatureHelp(source, cursor)) {
                                webSignatureHelpAt(fileName, source, cursor)
                            } else {
                                null
                            },
            )
        }
        return offlineResult
    }

    fun diagnostics(file: File, fileName: String, source: String): List<CodeDiagnostic> {
        return ExternalLspManager.diagnostics(file, fileName, source)
                ?: OfflineWebLspServer.diagnostics(file, fileName, source)
    }

    fun highlight(
            fileName: String,
            source: String,
            palette: WebPalette,
            diagnostics: List<CodeDiagnostic>,
    ): EditorVisualResult {
        return OfflineWebLspServer.highlight(fileName, source, palette, diagnostics)
    }

    fun format(file: File, fileName: String, source: String): String {
        return ExternalLspManager.format(file, fileName, source)
                ?: OfflineWebLspServer.format(file, fileName, source)
    }

    fun status(): String {
        return buildString {
            append(OfflineWebLspServer.status())
            append("\n\n")
            append(ExternalLspManager.status())
        }
    }

    fun releasePreviewHandoffMemory() {
        OfflineWebLspServer.clearCaches()
    }
}

private fun offlineLspSupportedLanguageSummary(): String {
    return listOf(
                    "HTML/HTM/SVG",
                    "CSS",
                    "JavaScript/JSX/MJS/CJS",
                    "TypeScript/TSX",
                    "JSON",
                    "SQL/SQLite",
                    "Markdown/Text as plaintext",
            )
            .joinToString(", ")
}

private class SmallLruCache<K, V>(
        private val maxEntries: Int,
) : LinkedHashMap<K, V>(maxEntries, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxEntries
    }

    fun getOrPutValue(key: K, producer: () -> V): V {
        synchronized(this) {
            get(key)?.let {
                return it
            }
        }
        val value = producer()
        synchronized(this) { put(key, value) }
        return value
    }

    fun clearValues() {
        synchronized(this) { clear() }
    }
}

private object ProjectFileCache {
    private const val MAX_CACHED_FILES = 64
    private const val MAX_CACHED_CONTENT_BYTES = 12L * 1024L * 1024L
    private data class CachedFile(
            val modifiedAt: Long,
            val sizeBytes: Long,
            val file: WebFile,
    )

    private val lock = Any()
    private val filesByPath = LinkedHashMap<String, CachedFile>(64, 0.75f, true)
    // Reuse documents already held by the UI without retaining whole large projects.
    private val liveFiles = LinkedHashMap<String, java.lang.ref.WeakReference<WebFile>>(128, 0.75f, true)

    fun webFileFor(rootDirectory: File, file: File): WebFile? {
        val canonicalFile = file.canonicalFile
        val rootPath = rootDirectory.canonicalPath
        if (canonicalFile.path != rootPath && !canonicalFile.path.startsWith(rootPath + File.separator)) return null

        val key = canonicalFile.absolutePath
        val modifiedAt = canonicalFile.lastModified()
        val sizeBytes = canonicalFile.length()
        val cached =
                synchronized(lock) {
                    (filesByPath[key]?.file ?: liveFiles[key]?.get())
                            ?.takeIf { it.modifiedAt == modifiedAt && it.sizeBytes == sizeBytes }
                }
        if (cached != null) return cached

        val content = if (isDatabaseFileName(canonicalFile.name)) "" else canonicalFile.readText()
        val webFile =
                WebFile(
                        id = key.hashCode().toLong(),
                        name = canonicalFile.name,
                        content = content,
                        filePath = canonicalFile.absolutePath,
                        modifiedAt = modifiedAt,
                        sizeBytes = sizeBytes,
                        contentHash = content.hashCode(),
                )
        synchronized(lock) {
            filesByPath[key] = CachedFile(modifiedAt, sizeBytes, webFile)
            liveFiles[key] = java.lang.ref.WeakReference(webFile)
            if (liveFiles.size > 8_192) {
                val iterator = liveFiles.entries.iterator()
                iterator.next()
                iterator.remove()
            }
            trimToBudget()
        }
        return webFile
    }

    fun invalidate(file: File) {
        val key = runCatching { file.canonicalFile.absolutePath }.getOrNull() ?: return
        synchronized(lock) { filesByPath.remove(key); liveFiles.remove(key) }
    }

    fun prune(rootDirectory: File, livePaths: Set<String>) {
        val rootPath = rootDirectory.canonicalFile.absolutePath
        val rootPrefix = rootPath + File.separator
        synchronized(lock) {
            liveFiles.entries.removeAll { (path, reference) ->
                reference.get() == null || ((path == rootPath || path.startsWith(rootPrefix)) && path !in livePaths)
            }
            filesByPath.keys
                    .filter { path ->
                        (path == rootPath || path.startsWith(rootPrefix)) && path !in livePaths
                    }
                    .forEach { path -> filesByPath.remove(path) }
        }
    }

    fun clearAll() {
        synchronized(lock) { filesByPath.clear(); liveFiles.clear() }
    }

    private fun trimToBudget() {
        var bytes = filesByPath.values.sumOf(CachedFile::sizeBytes)
        val iterator = filesByPath.entries.iterator()
        while ((filesByPath.size > MAX_CACHED_FILES || bytes > MAX_CACHED_CONTENT_BYTES) &&
                iterator.hasNext()) {
            bytes -= iterator.next().value.sizeBytes
            iterator.remove()
        }
    }
}

private fun plainCompletionContextAt(source: String, rawCursor: Int): CompletionContext {
    val cursor = rawCursor.coerceIn(0, source.length)
    var start = cursor
    while (start > 0) {
        val char = source[start - 1]
        if (!char.isLetterOrDigit() && char != '_' && char != '$' && char != '-') break
        start -= 1
    }
    return CompletionContext(
            prefix = source.substring(start, cursor),
            replaceStart = start,
            replaceEnd = cursor,
    )
}

private class WebSyntaxTransformation(
        private val fileName: String,
        private val palette: WebPalette,
        private val diagnostics: List<CodeDiagnostic>,
        private val visibleStart: Int,
        private val visibleEnd: Int,
) : VisualTransformation {
    private var cachedSource: String? = null
    private var cachedResult: AnnotatedString? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val currentSource = text.text
        if (currentSource === cachedSource) {
            return TransformedText(
                    cachedResult ?: text,
                    OffsetMapping.Identity,
            )
        }
        val window =
                editorHighlightWindow(
                        currentSource,
                        visibleStart,
                        visibleEnd,
                        LIVE_COLOR_RETARGET_RADIUS
                )
        val windowStart = window.start
        val windowEnd = window.end
        val localSource = currentSource.substring(windowStart, windowEnd)
        val localHighlight = highlightWebSource(fileName, localSource, palette)
        val spans = buildList {
            localHighlight.spanStyles.forEach { range ->
                add(
                        AnnotatedString.Range(
                                item = range.item,
                                start = range.start + windowStart,
                                end = range.end + windowStart,
                        )
                )
            }
            diagnostics.forEach { diagnostic ->
                val start = diagnostic.start.coerceIn(windowStart, windowEnd)
                val end = diagnostic.end.coerceIn(start, windowEnd)
                if (end > start) {
                    add(
                            AnnotatedString.Range(
                                    diagnosticSpanStyle(palette, diagnostic.severity),
                                    start,
                                    end
                            )
                    )
                }
            }
        }
        val result = AnnotatedString(currentSource, spanStyles = spans)
        cachedSource = currentSource
        cachedResult = result
        return TransformedText(
                result,
                OffsetMapping.Identity,
        )
    }
}

private fun retargetEditorVisualText(
        source: String,
        highlighted: AnnotatedString,
        currentSource: String,
        cursorHint: Int? = null,
        styleRadius: Int = Int.MAX_VALUE,
): AnnotatedString {
    if (currentSource == source) return highlighted
    if (source.isEmpty() || highlighted.spanStyles.isEmpty()) return AnnotatedString(currentSource)

    val lengthDelta = currentSource.length - source.length
    val hintedPrefix =
            cursorHint?.let { cursor ->
                when (lengthDelta) {
                    1 -> (cursor - 1).coerceIn(0, source.length)
                    -1 -> cursor.coerceIn(0, currentSource.length)
                    else -> null
                }
            }
    val commonSuffix =
            if (hintedPrefix != null) {
                minOf(source.length, currentSource.length) - hintedPrefix
            } else if (lengthDelta == 1 || lengthDelta == -1) {
                // Most editor updates are one-character inserts/deletes. Finding the edit
                // from the end is fast for the common case of typing near the cursor at the
                // end of a large document and avoids rescanning the whole leading content.
                commonSuffixLength(source, currentSource, commonPrefix = 0)
            } else {
                0
            }
    val inferredPrefix = minOf(source.length, currentSource.length) - commonSuffix
    val commonPrefix =
            hintedPrefix
                    ?: if ((lengthDelta == 1 || lengthDelta == -1) && inferredPrefix >= 0) {
                        inferredPrefix
                    } else {
                        commonPrefixLength(source, currentSource)
                    }
    val verifiedSuffix =
            if (lengthDelta == 1 || lengthDelta == -1) {
                commonSuffix
            } else {
                commonSuffixLength(source, currentSource, commonPrefix)
            }
    val oldEditEnd = source.length - verifiedSuffix
    val newEditEnd = currentSource.length - verifiedSuffix
    val delta = currentSource.length - source.length

    return buildAnnotatedString {
        append(currentSource)
        val styleWindowStart = cursorHint?.minus(styleRadius)?.coerceAtLeast(0) ?: 0
        val styleWindowEnd =
                cursorHint?.plus(styleRadius)?.coerceAtMost(currentSource.length)
                        ?: currentSource.length
        highlighted
                .spanStyles
                .asSequence()
                .filter { range ->
                    styleRadius == Int.MAX_VALUE ||
                            (range.end >= styleWindowStart && range.start <= styleWindowEnd)
                }
                .forEach { range ->
                    addRetargetedStyle(
                            style = range.item,
                            oldStart = range.start,
                            oldEnd = range.end,
                            commonPrefix = commonPrefix,
                            oldEditEnd = oldEditEnd,
                            newEditEnd = newEditEnd,
                            delta = delta,
                            currentLength = currentSource.length,
                    )
                }
    }
}

private fun commonPrefixLength(left: String, right: String): Int {
    val limit = minOf(left.length, right.length)
    var index = 0
    while (index < limit && left[index] == right[index]) {
        index += 1
    }
    return index
}

private fun commonSuffixLength(left: String, right: String, commonPrefix: Int): Int {
    val limit = minOf(left.length, right.length) - commonPrefix
    var count = 0
    while (count < limit && left[left.lastIndex - count] == right[right.lastIndex - count]) {
        count += 1
    }
    return count
}

private fun AnnotatedString.Builder.addRetargetedStyle(
        style: SpanStyle,
        oldStart: Int,
        oldEnd: Int,
        commonPrefix: Int,
        oldEditEnd: Int,
        newEditEnd: Int,
        delta: Int,
        currentLength: Int,
) {
    if (oldStart < commonPrefix) {
        addBoundedStyle(style, oldStart, minOf(oldEnd, commonPrefix), currentLength)
    }

    val editIsInsertion = oldEditEnd == commonPrefix && newEditEnd > commonPrefix
    val styleCoversInsertionPoint =
            editIsInsertion && oldStart < commonPrefix && oldEnd > commonPrefix
    val styleOverlapsReplacedText = oldStart < oldEditEnd && oldEnd > commonPrefix
    if ((styleCoversInsertionPoint || styleOverlapsReplacedText) && newEditEnd > commonPrefix) {
        addBoundedStyle(style, commonPrefix, newEditEnd, currentLength)
    }

    if (oldEnd > oldEditEnd) {
        val shiftedStart = maxOf(oldStart, oldEditEnd) + delta
        val shiftedEnd = oldEnd + delta
        addBoundedStyle(style, shiftedStart, shiftedEnd, currentLength)
    }
}

private fun AnnotatedString.Builder.addBoundedStyle(
        style: SpanStyle,
        start: Int,
        end: Int,
        length: Int,
) {
    val boundedStart = start.coerceIn(0, length)
    val boundedEnd = end.coerceIn(boundedStart, length)
    if (boundedEnd > boundedStart) {
        addStyle(style, boundedStart, boundedEnd)
    }
}

internal fun highlightWebSource(
        fileName: String,
        source: String,
        palette: WebPalette
): AnnotatedString {
    return when (fileExtension(fileName)) {
        in htmlFileExtensions -> highlightHtml(source, palette)
        in cssFileExtensions -> highlightCss(source, palette)
        in javascriptFileExtensions -> highlightJs(source, palette)
        "json" -> highlightJson(source, palette)
        "py", "pyw" -> highlightPython(source, palette)
        "php", "phtml" -> highlightPhp(source, palette)
        "sql" -> highlightSql(source, palette)
        else -> AnnotatedString(source)
    }.withReadableText(palette.editorBackground, selection = palette.accent.copy(alpha = 0.18f))
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
}.withReadableText(palette.editorBackground, selection = palette.accent.copy(alpha = 0.18f))

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
                ) { append(line.toString()) }
            }
        }
    }
}

internal fun diagnosticSeverityByLine(diagnostics: List<CodeDiagnostic>): Map<Int, String> {
    return diagnostics.groupBy { it.line }.mapValues { (_, lineDiagnostics) ->
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

private val javascriptKeywords =
        setOf(
                "break",
                "case",
                "catch",
                "class",
                "const",
                "continue",
                "debugger",
                "default",
                "delete",
                "do",
                "else",
                "export",
                "extends",
                "finally",
                "for",
                "function",
                "if",
                "import",
                "in",
                "instanceof",
                "let",
                "new",
                "return",
                "super",
                "switch",
                "this",
                "throw",
                "try",
                "typeof",
                "var",
                "void",
                "while",
                "with",
                "yield",
                "async",
                "await"
        )

private val javascriptBuiltins =
        setOf(
                "console",
                "window",
                "document",
                "localStorage",
                "sessionStorage",
                "fetch",
                "Promise",
                "Array",
                "Object",
                "Math",
                "JSON",
                "Date",
                "Set",
                "Map",
                "Error",
                "HTMLElement",
                "Element",
                "Node"
        )

private fun applyRegexColor(
        builder: AnnotatedString.Builder,
        source: String,
        regex: Regex,
        color: Color,
) {
    regex.findAll(source).forEach { match ->
        builder.addStyle(SpanStyle(color = color), match.range.first, match.range.last + 1)
    }
}

private fun applyRegexGroupColor(
        builder: AnnotatedString.Builder,
        source: String,
        regex: Regex,
        groupIndex: Int,
        color: Color,
) {
    regex.findAll(source).forEach { match ->
        val group = match.groups[groupIndex] ?: return@forEach
        builder.addStyle(SpanStyle(color = color), group.range.first, group.range.last + 1)
    }
}

private fun highlightHtml(source: String, palette: WebPalette): AnnotatedString =
        buildAnnotatedString {
            append(source)
            if (source.isEmpty()) return@buildAnnotatedString
            val dark = palette.text == Color.White

            // Comments
            applyRegexColor(this, source, HTML_COMMENT_REGEX, palette.subtleText)

            // Doctype
            applyRegexColor(
                    this,
                    source,
                    HTML_DOCTYPE_REGEX,
                    if (dark) Color(0xFFFF7AB6) else Color(0xFFB4236E)
            )

            // Tag Brackets
            applyRegexColor(this, source, HTML_TAG_BRACKETS_REGEX, palette.text.copy(alpha = 0.7f))

            // Tag Names
            applyRegexGroupColor(
                    this,
                    source,
                    HTML_TAG_NAME_REGEX,
                    1,
                    if (dark) Color(0xFF569CD6) else Color(0xFF264F78)
            )

            // Attribute Names
            applyRegexGroupColor(
                    this,
                    source,
                    HTML_ATTR_NAME_CORE_REGEX,
                    1,
                    if (dark) Color(0xFF9CDCFE) else Color(0xFF001080)
            )
            applyRegexGroupColor(
                    this,
                    source,
                    HTML_ATTR_NAME_GENERIC_REGEX,
                    1,
                    if (dark) Color(0xFF9CDCFE) else Color(0xFF001080)
            )

            // Attribute Values (Strings)
            applyRegexColor(
                    this,
                    source,
                    HTML_ATTR_VALUE_REGEX,
                    if (dark) Color(0xFFCE9178) else Color(0xFFA31515)
            )

            // Entities
            applyRegexColor(
                    this,
                    source,
                    HTML_ENTITY_REGEX,
                    if (dark) Color(0xFFB5CEA8) else Color(0xFF098658)
            )
        }

private fun highlightCss(source: String, palette: WebPalette): AnnotatedString =
        buildAnnotatedString {
            append(source)
            if (source.isEmpty()) return@buildAnnotatedString
            val dark = palette.text == Color.White

            // Comments
            applyRegexColor(this, source, CSS_COMMENT_REGEX, palette.subtleText)

            // At-rules
            applyRegexColor(
                    this,
                    source,
                    CSS_AT_RULE_REGEX,
                    if (dark) Color(0xFFC586C0) else Color(0xFFAF57A0)
            )

            // Selectors
            applyRegexColor(
                    this,
                    source,
                    CSS_SELECTOR_ID_CLASS_REGEX,
                    if (dark) Color(0xFFD7BA7D) else Color(0xFF795E26)
            )
            applyRegexGroupColor(
                    this,
                    source,
                    CSS_SELECTOR_BLOCK_REGEX,
                    1,
                    if (dark) Color(0xFFD7BA7D) else Color(0xFF795E26)
            )

            // Properties
            applyRegexGroupColor(
                    this,
                    source,
                    CSS_PROPERTY_REGEX,
                    1,
                    if (dark) Color(0xFF9CDCFE) else Color(0xFF001080)
            )

            // Values - Hex Colors
            applyRegexColor(
                    this,
                    source,
                    CSS_COLOR_HEX_REGEX,
                    if (dark) Color(0xFFB5CEA8) else Color(0xFF098658)
            )

            // Values - Numbers and Units
            applyRegexColor(
                    this,
                    source,
                    CSS_NUMBER_UNIT_REGEX,
                    if (dark) Color(0xFFB5CEA8) else Color(0xFF098658)
            )

            // Values - Strings
            applyRegexColor(
                    this,
                    source,
                    CSS_STRING_REGEX,
                    if (dark) Color(0xFFCE9178) else Color(0xFFA31515)
            )

            // Values - Keywords
            applyRegexColor(
                    this,
                    source,
                    CSS_KEYWORD_REGEX,
                    if (dark) Color(0xFF569CD6) else Color(0xFF0451A5)
            )
        }

private fun highlightJs(source: String, palette: WebPalette): AnnotatedString =
        buildAnnotatedString {
            append(source)
            if (source.isEmpty()) return@buildAnnotatedString
            val dark = palette.text == Color.White

            // Comments
            applyRegexColor(this, source, JS_COMMENT_REGEX, palette.subtleText)

            // Strings
            applyRegexColor(
                    this,
                    source,
                    JS_STRING_REGEX,
                    if (dark) Color(0xFFCE9178) else Color(0xFFA31515)
            )

            // Keywords (including TS)
            val tsKeywords =
                    setOf(
                            "type",
                            "interface",
                            "enum",
                            "as",
                            "readonly",
                            "private",
                            "protected",
                            "public",
                            "namespace",
                            "abstract",
                            "declare",
                            "module"
                    )
            val allKeywords = javascriptKeywords + tsKeywords
            val jsKeywordsPattern = "\\b(${allKeywords.joinToString("|")})\\b"
            applyRegexColor(
                    this,
                    source,
                    Regex(jsKeywordsPattern),
                    if (dark) Color(0xFFC586C0) else Color(0xFFAF57A0)
            )

            // Booleans/Null
            applyRegexColor(
                    this,
                    source,
                    JS_BOOLEAN_NULL_REGEX,
                    if (dark) Color(0xFF569CD6) else Color(0xFF0451A5)
            )

            // Builtins/Globals
            val tsBuiltins =
                    setOf(
                            "any",
                            "number",
                            "string",
                            "boolean",
                            "void",
                            "never",
                            "unknown",
                            "object",
                            "Record",
                            "Partial",
                            "Pick",
                            "Omit"
                    )
            val allBuiltins = javascriptBuiltins + tsBuiltins
            val jsBuiltinsPattern = "\\b(${allBuiltins.joinToString("|")})\\b"
            applyRegexColor(
                    this,
                    source,
                    Regex(jsBuiltinsPattern),
                    if (dark) Color(0xFF4EC9B0) else Color(0xFF267F99)
            )

            // Numbers
            applyRegexColor(
                    this,
                    source,
                    JS_NUMBER_REGEX,
                    if (dark) Color(0xFFB5CEA8) else Color(0xFF098658)
            )

            // Member access (Properties)
            applyRegexGroupColor(
                    this,
                    source,
                    JS_MEMBER_ACCESS_REGEX,
                    1,
                    if (dark) Color(0xFF9CDCFE) else Color(0xFF001080)
            )

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

private fun highlightJson(source: String, palette: WebPalette): AnnotatedString =
        buildAnnotatedString {
            append(source)
            if (source.isEmpty()) return@buildAnnotatedString
            val dark = palette.text == Color.White

            // Keys
            applyRegexGroupColor(
                    this,
                    source,
                    JSON_KEY_REGEX,
                    0,
                    if (dark) Color(0xFF9CDCFE) else Color(0xFF001080)
            )

            // Values - Strings
            applyRegexGroupColor(
                    this,
                    source,
                    JSON_STRING_VALUE_REGEX,
                    1,
                    if (dark) Color(0xFFCE9178) else Color(0xFFA31515)
            )

            // Values - Numbers/Booleans/Null
            applyRegexGroupColor(
                    this,
                    source,
                    JSON_LITERAL_VALUE_REGEX,
                    1,
                    if (dark) Color(0xFFB5CEA8) else Color(0xFF098658)
            )
        }

private fun webLanguageForFile(fileName: String): WebEditorLanguage {
    return when (fileExtension(fileName)) {
        in htmlFileExtensions -> WebEditorLanguage.Html
        in cssFileExtensions -> WebEditorLanguage.Css
        in javascriptFileExtensions -> WebEditorLanguage.JavaScript
        "json" -> WebEditorLanguage.Json
        "py", "pyw" -> WebEditorLanguage.Python
        "php", "phtml" -> WebEditorLanguage.Php
        "sql" -> WebEditorLanguage.Sql
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
    val openBlocks =
            listOfNotNull(
                    scriptOpen?.takeIf { it.range.last > scriptClose }?.let {
                        WebEditorLanguage.JavaScript to it.range.last
                    },
                    styleOpen?.takeIf { it.range.last > styleClose }?.let {
                        WebEditorLanguage.Css to it.range.last
                    },
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
            cssLineBeforeCursor(beforeCursor).substringAfterLast(':').none {
                it == ';' || it == '{' || it == '}'
            }
}

internal fun completionContextAt(
        text: String,
        rawCursor: Int,
        fileName: String
): CompletionContext? {
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
    val trigger =
            when {
                before.endsWith("</") || completingClosingHtmlTag -> "closingTag"
                attributeValueName != null -> "attributeValue:$attributeValueName"
                insideHtmlTag && before.substring(tagStart).contains(Regex("\\s")) -> "attribute"
                before.endsWith("<") || completingOpeningHtmlTag -> "tag"
                language == WebEditorLanguage.Css &&
                        isInsideCssRuleBlock(before) &&
                        isCssVariableValueCompletion(before) -> "cssVariable"
                language == WebEditorLanguage.Css && isCssAtRuleCompletion(before) -> "cssAtRule"
                language == WebEditorLanguage.Css && isCssDeclarationValueCompletion(before) ->
                        "cssValue"
                else -> ""
            }

    val validPart: (Char) -> Boolean =
            when {
                trigger == "attribute" -> { char -> char.isLetterOrDigit() || char in "-:@" }
                trigger.startsWith("attributeValue:") -> { char ->
                            char.isLetterOrDigit() || char in "-_:/#.%@"
                        }
                trigger == "cssVariable" -> { char -> char.isLetterOrDigit() || char in "-_" }
                trigger == "cssValue" -> { char -> char.isLetterOrDigit() || char in "-#.%()," }
                language == WebEditorLanguage.JavaScript -> { char ->
                            char.isLetterOrDigit() || char == '_' || char == '$'
                        }
                language == WebEditorLanguage.Css -> { char ->
                            char.isLetterOrDigit() || char in "_-#@$"
                        }
                else -> { char -> char.isLetterOrDigit() || char in "_-:#@$" }
            }

    var start = cursor
    while (start > 0 && validPart(text[start - 1])) start -= 1
    val isDotAccess = start > 0 && text[start - 1] == '.'
    val prefix = text.substring(start, cursor)
    if (prefix.isBlank() && trigger.isBlank() && !isDotAccess) return null
    val replaceStart =
            when {
                completingClosingHtmlTag || before.endsWith("</") -> tagStart + 1
                completingOpeningHtmlTag || before.endsWith("<") -> tagStart
                else -> start
            }
    val replaceEnd =
            if (trigger == "tag" || trigger == "closingTag") {
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
    val htmlTag =
            activeHtmlTagName(source.substring(0, context.replaceStart.coerceIn(0, source.length)))
    val base =
            when {
                context.trigger == "closingTag" ->
                        openHtmlTagsBefore(source, context.replaceStart).asReversed().map {
                            CompletionItem("/$it>", "/$it>", "closing tag")
                        }
                context.trigger == "attribute" -> htmlAttributeCompletionsForTag(htmlTag)
                context.trigger.startsWith("attributeValue:") ->
                        htmlAttributeValueCompletions(
                                attribute = context.trigger.substringAfter(':'),
                                tag = htmlTag,
                                source = source,
                                cursor = context.replaceStart,
                        )
                context.trigger == "cssAtRule" -> cssAtRuleCompletions
                context.trigger == "cssVariable" -> cssCustomPropertyValueCompletions(source)
                context.trigger == "cssValue" ->
                        cssValueCompletionsFor(source, context.replaceStart)
                context.isDotAccess && language == WebEditorLanguage.JavaScript ->
                        jsDotCompletionsForSource(source, context.replaceStart - 1)
                context.trigger == "tag" && language == WebEditorLanguage.Html ->
                        htmlCompletionItems
                language == WebEditorLanguage.Html ->
                        userDefinedHtmlCompletions(
                                completionScanWindow(source, context.replaceStart)
                        ) + htmlCompletionItems
                language == WebEditorLanguage.Css ->
                        cssContextualCompletions(source, context.replaceStart)
                language == WebEditorLanguage.JavaScript ->
                        userDefinedJsCompletions(
                                completionScanWindow(source, context.replaceStart)
                        ) + jsCompletionItems + jsLibraryCompletionItems
                language == WebEditorLanguage.Json -> jsonCompletionItems
                else ->
                        generalCodeCompletions(
                                fileName,
                                completionScanWindow(source, context.replaceStart)
                        )
            }
    if (context.trigger == "tag" && prefix.isBlank()) return base.take(12)
    return base
            .distinctBy { it.completionKey() }
            .filter { item -> completionMatchesPrefix(item, prefix) }
            .take(16)
}

private val pythonKeywords =
        setOf(
                "and",
                "as",
                "assert",
                "async",
                "await",
                "break",
                "class",
                "continue",
                "def",
                "del",
                "elif",
                "else",
                "except",
                "False",
                "finally",
                "for",
                "from",
                "global",
                "if",
                "import",
                "in",
                "is",
                "lambda",
                "None",
                "nonlocal",
                "not",
                "or",
                "pass",
                "raise",
                "return",
                "True",
                "try",
                "while",
                "with",
                "yield",
        )

private val phpKeywords =
        setOf(
                "abstract",
                "array",
                "as",
                "break",
                "callable",
                "case",
                "catch",
                "class",
                "clone",
                "const",
                "continue",
                "declare",
                "default",
                "do",
                "echo",
                "else",
                "elseif",
                "empty",
                "enum",
                "extends",
                "false",
                "final",
                "finally",
                "fn",
                "for",
                "foreach",
                "function",
                "global",
                "if",
                "implements",
                "include",
                "instanceof",
                "interface",
                "isset",
                "match",
                "namespace",
                "new",
                "null",
                "print",
                "private",
                "protected",
                "public",
                "readonly",
                "require",
                "return",
                "static",
                "switch",
                "throw",
                "trait",
                "true",
                "try",
                "unset",
                "use",
                "while",
                "yield",
        )

private fun highlightPython(source: String, palette: WebPalette): AnnotatedString =
        highlightKeywordLanguage(source, palette, pythonKeywords, "#", null)

private fun highlightPhp(source: String, palette: WebPalette): AnnotatedString =
        highlightKeywordLanguage(source, palette, phpKeywords, "//", '$')

private fun highlightKeywordLanguage(
        source: String,
        palette: WebPalette,
        keywords: Set<String>,
        lineCommentPrefix: String,
        variablePrefix: Char?,
): AnnotatedString = buildAnnotatedString {
    append(source)
    if (source.isEmpty()) return@buildAnnotatedString
    val dark = palette.text == Color.White
    applyRegexColor(
            this,
            source,
            Regex("(?m)${Regex.escape(lineCommentPrefix)}.*$|/\\*[\\s\\S]*?\\*/"),
            palette.subtleText
    )
    applyRegexColor(
            this,
            source,
            Regex(
                    "(?s)\\\"\\\"\\\".*?\\\"\\\"\\\"|'''.*?'''|\\\"(?:\\\\.|[^\\\"\\\\])*\\\"|'(?:\\\\.|[^'\\\\])*'"
            ),
            if (dark) Color(0xFFCE9178) else Color(0xFFA31515)
    )
    applyRegexColor(
            this,
            source,
            Regex("\\b\\d+(?:\\.\\d+)?\\b"),
            if (dark) Color(0xFFB5CEA8) else Color(0xFF098658)
    )
    Regex("\\b[A-Za-z_][A-Za-z0-9_]*\\b").findAll(source).forEach { match ->
        if (keywords.any { it.equals(match.value, ignoreCase = true) }) {
            addStyle(
                    SpanStyle(
                            color = if (dark) Color(0xFFC586C0) else Color(0xFF6F42C1),
                            fontWeight = FontWeight.SemiBold
                    ),
                    match.range.first,
                    match.range.last + 1
            )
        }
    }
    variablePrefix?.let { prefix ->
        applyRegexColor(
                this,
                source,
                Regex("${Regex.escape(prefix.toString())}[A-Za-z_][A-Za-z0-9_]*"),
                if (dark) Color(0xFF9CDCFE) else Color(0xFF001080)
        )
    }
}

internal val sqlKeywords =
        setOf(
                "ABORT",
                "ACTION",
                "ADD",
                "ALTER",
                "ANALYZE",
                "AS",
                "ASC",
                "ATTACH",
                "AUTOINCREMENT",
                "BEGIN",
                "BETWEEN",
                "BY",
                "CASCADE",
                "CASE",
                "CAST",
                "CHECK",
                "COLLATE",
                "COLUMN",
                "COMMIT",
                "CONFLICT",
                "CONSTRAINT",
                "CREATE",
                "CROSS",
                "CURRENT_DATE",
                "CURRENT_TIME",
                "CURRENT_TIMESTAMP",
                "DATABASE",
                "DEFAULT",
                "DEFERRABLE",
                "DEFERRED",
                "DELETE",
                "DESC",
                "DETACH",
                "DISTINCT",
                "DO",
                "DROP",
                "EACH",
                "ELSE",
                "END",
                "ESCAPE",
                "EXCEPT",
                "EXCLUDE",
                "EXISTS",
                "EXPLAIN",
                "FAIL",
                "FILTER",
                "FIRST",
                "FOLLOWING",
                "FOR",
                "FOREIGN",
                "FROM",
                "FULL",
                "GENERATED",
                "GLOB",
                "GROUP",
                "GROUPS",
                "HAVING",
                "IF",
                "IGNORE",
                "IMMEDIATE",
                "IN",
                "INDEX",
                "INDEXED",
                "INITIALLY",
                "INNER",
                "INSERT",
                "INSTEAD",
                "INTERSECT",
                "INTO",
                "IS",
                "ISNULL",
                "JOIN",
                "KEY",
                "LAST",
                "LEFT",
                "LIKE",
                "LIMIT",
                "MATCH",
                "MATERIALIZED",
                "NATURAL",
                "NO",
                "NOT",
                "NOTHING",
                "NOTNULL",
                "NULL",
                "NULLS",
                "OF",
                "OFFSET",
                "ON",
                "OR",
                "ORDER",
                "OTHERS",
                "OUTER",
                "OVER",
                "PARTITION",
                "PLAN",
                "PRAGMA",
                "PRECEDING",
                "PRIMARY",
                "QUERY",
                "RAISE",
                "RANGE",
                "RECURSIVE",
                "REFERENCES",
                "REGEXP",
                "REINDEX",
                "RELEASE",
                "RENAME",
                "REPLACE",
                "RESTRICT",
                "RETURNING",
                "RIGHT",
                "ROLLBACK",
                "ROW",
                "ROWS",
                "SAVEPOINT",
                "SELECT",
                "SET",
                "TABLE",
                "TEMP",
                "TEMPORARY",
                "THEN",
                "TIES",
                "TO",
                "TRANSACTION",
                "TRIGGER",
                "UNBOUNDED",
                "UNION",
                "UNIQUE",
                "UPDATE",
                "USING",
                "VACUUM",
                "VALUES",
                "VIEW",
                "VIRTUAL",
                "WHEN",
                "WHERE",
                "WINDOW",
                "WITH",
                "WITHOUT",
        )

private fun highlightSql(source: String, palette: WebPalette): AnnotatedString =
        buildAnnotatedString {
            append(source)
            if (source.isEmpty()) return@buildAnnotatedString
            val dark = palette.text == Color.White
            val keywordStyle =
                    SpanStyle(
                            color = if (dark) Color(0xFFC586C0) else Color(0xFF6F42C1),
                            fontWeight = FontWeight.SemiBold,
                    )
            val stringStyle = SpanStyle(color = if (dark) Color(0xFFCE9178) else Color(0xFF087A43))
            val numberStyle = SpanStyle(color = if (dark) Color(0xFF4FC1FF) else Color(0xFF075E73))
            val commentStyle = SpanStyle(color = palette.subtleText, fontStyle = FontStyle.Italic)

            Regex("--[^\\n]*|/\\*[\\s\\S]*?\\*/").findAll(source).forEach {
                addStyle(commentStyle, it.range.first, it.range.last + 1)
            }
            Regex("'(?:''|[^'])*'").findAll(source).forEach {
                addStyle(stringStyle, it.range.first, it.range.last + 1)
            }
            Regex("\\b\\d+(?:\\.\\d+)?\\b").findAll(source).forEach {
                addStyle(numberStyle, it.range.first, it.range.last + 1)
            }
            Regex("\\b[A-Za-z_][A-Za-z0-9_]*\\b").findAll(source).forEach {
                if (it.value.uppercase(Locale.US) in sqlKeywords) {
                    addStyle(keywordStyle, it.range.first, it.range.last + 1)
                }
            }
        }

private fun completionScanWindow(source: String, rawCursor: Int): String {
    if (source.length <= COMPLETION_SCAN_CHAR_LIMIT) return source
    val cursor = rawCursor.coerceIn(0, source.length)
    val halfWindow = COMPLETION_SCAN_CHAR_LIMIT / 2
    val start = (cursor - halfWindow).coerceAtLeast(0)
    val end = (start + COMPLETION_SCAN_CHAR_LIMIT).coerceAtMost(source.length)
    val adjustedStart = (end - COMPLETION_SCAN_CHAR_LIMIT).coerceAtLeast(0)
    return source.substring(adjustedStart, end)
}

private fun completionMatchesPrefix(item: CompletionItem, prefix: String): Boolean {
    if (prefix.isBlank()) return true
    val normalizedPrefix = prefix.trimCompletionPrefixSymbols()
    return listOf(item.label, item.insertText).map { it.lowercase() }.any { value ->
        value.startsWith(prefix) || value.trimCompletionPrefixSymbols().startsWith(normalizedPrefix)
    }
}

private fun String.trimCompletionPrefixSymbols(): String {
    return trimStart('<', '/', '.', '#', ':', '"', '\'')
}

private fun isCompletingOpeningHtmlTag(activeTagText: String): Boolean {
    if (!activeTagText.startsWith("<")) return false
    if (activeTagText.startsWith("</") ||
                    activeTagText.startsWith("<!") ||
                    activeTagText.startsWith("<?")
    )
            return false
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

private fun applyCompletion(
        value: TextFieldValue,
        context: CompletionContext,
        item: CompletionItem
): TextFieldValue {
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

internal fun completionExplanation(item: CompletionItem): String {
    specialCompletionDocumentation(item)?.let {
        return it.description
    }
    if (item.documentation.isNotBlank()) return item.documentation
    return when {
        item.detail == "closing tag" -> "Closes the matching HTML element."
        item.detail.contains("element", ignoreCase = true) -> "HTML element suggestion."
        item.detail.contains("attribute", ignoreCase = true) -> "HTML attribute suggestion."
        item.detail.contains("css", ignoreCase = true) ->
                "CSS suggestion for styling the current rule."
        item.detail.contains("array", ignoreCase = true) -> "JavaScript array helper."
        item.detail.contains("dom", ignoreCase = true) -> "Browser DOM API suggestion."
        item.detail.contains("function", ignoreCase = true) -> "JavaScript function suggestion."
        item.detail.contains("field", ignoreCase = true) -> "JSON field suggestion."
        item.detail.isNotBlank() -> "${item.detail.replaceFirstChar { it.uppercase() }} suggestion."
        else -> "Code completion suggestion."
    }
}

internal fun completionSyntax(item: CompletionItem): String {
    specialCompletionDocumentation(item)?.let {
        return it.syntax
    }
    val lookupLabel = completionLookupLabel(item.label)
    val directKey = "$lookupLabel|${item.detail}"
    val labelKey = "${item.label}|${item.detail}"
    return webCompletionSyntaxes[directKey]
            ?: webCompletionSyntaxes[labelKey] ?: item.insertText.takeIf { it.isNotBlank() }
                    ?: item.label
}

internal fun completionExample(item: CompletionItem): String {
    specialCompletionDocumentation(item)?.let {
        return it.example
    }
    return when {
        item.detail == "closing tag" -> "</${completionLookupLabel(item.label)}>"
        item.detail.contains("css", ignoreCase = true) -> completionSyntax(item)
        item.detail.contains("field", ignoreCase = true) -> "{\n  ${item.insertText}\n}"
        item.insertText.isNotBlank() -> item.insertText
        else -> item.label
    }
}

private fun completionLookupLabel(label: String): String {
    return label.trim()
            .removePrefix("<")
            .removePrefix("/")
            .removeSuffix(">")
            .removeSuffix("()")
            .removeSurrounding("\"")
            .substringBefore("(")
}

private val htmlCompletionItems =
        listOf(
                CompletionItem("<!DOCTYPE html>", "<!DOCTYPE html>\n", "document"),
                CompletionItem(
                        "html",
                        "<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Document</title>\n</head>\n<body>\n    \n</body>\n</html>",
                        "page",
                        156
                ),
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
                CompletionItem(
                        "picture",
                        "<picture>\n    <source srcset=\"\" media=\"\">\n    <img src=\"\" alt=\"\">\n</picture>",
                        "media",
                        25
                ),
                CompletionItem("video", "<video controls src=\"\"></video>", "media", 21),
                CompletionItem("canvas", "<canvas></canvas>", "media", 8),
                CompletionItem("form", "<form>\n    \n</form>", "element", 12),
                CompletionItem("input", "<input type=\"text\" name=\"\">", "form", 25),
                CompletionItem("label", "<label for=\"\"></label>", "form", 12),
                CompletionItem(
                        "select",
                        "<select name=\"\">\n    <option value=\"\"></option>\n</select>",
                        "form",
                        15
                ),
                CompletionItem("option", "<option value=\"\"></option>", "form", 15),
                CompletionItem("textarea", "<textarea name=\"\"></textarea>", "form", 16),
                CompletionItem("ul", "<ul>\n    <li></li>\n</ul>", "list", 14),
                CompletionItem("ol", "<ol>\n    <li></li>\n</ol>", "list", 14),
                CompletionItem("li", "<li></li>", "list", 4),
                CompletionItem(
                        "details",
                        "<details>\n    <summary></summary>\n    \n</details>",
                        "element",
                        25
                ),
                CompletionItem("dialog", "<dialog>\n    \n</dialog>", "element", 13),
                CompletionItem("template", "<template>\n    \n</template>", "element", 15),
                CompletionItem("style", "<style>\n    \n</style>", "style", 12),
                CompletionItem(
                        "link:css",
                        "<link rel=\"stylesheet\" href=\"style.css\">",
                        "stylesheet"
                ),
                CompletionItem(
                        "meta:viewport",
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">",
                        "metadata"
                ),
                CompletionItem("script:src", "<script src=\"script.js\"></script>", "script"),
                CompletionItem(
                        "script:module",
                        "<script type=\"module\" src=\"script.js\"></script>",
                        "script"
                ),
                CompletionItem(
                        "script:importmap",
                        "<script type=\"importmap\">\n{\n    \"imports\": {\n        \"three\": \"./libs/three.module.js\"\n    }\n}\n</script>",
                        "libraries",
                        70
                ),
                CompletionItem(
                        "lib:tailwind",
                        "<link rel=\"stylesheet\" href=\"vendor/tailwind.css\">",
                        "libraries"
                ),
                CompletionItem(
                        "lib:bootstrap",
                        "<link href=\"vendor/bootstrap.min.css\" rel=\"stylesheet\">\n<script src=\"vendor/bootstrap.bundle.min.js\"></script>",
                        "libraries"
                ),
                CompletionItem(
                        "lib:react-root",
                        "<div id=\"root\"></div>\n<script type=\"module\">\n    import React from \"react\";\n    import { createRoot } from \"react-dom/client\";\n</script>",
                        "libraries",
                        108
                ),
                CompletionItem(
                        "lib:vue-app",
                        "<div id=\"app\">{{ message }}</div>\n<script type=\"module\">\n    import { createApp } from \"vue\";\n</script>",
                        "libraries",
                        92
                ),
        )

private val htmlGlobalAttributeCompletions =
        listOf(
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

private val htmlTagAttributeCompletions =
        mapOf(
                "a" to
                        listOf(
                                CompletionItem("href", "href=\"\"", "attribute", 6),
                                CompletionItem("target", "target=\"_blank\"", "attribute"),
                                CompletionItem("rel", "rel=\"noopener\"", "attribute"),
                                CompletionItem("download", "download", "attribute"),
                        ),
                "img" to
                        listOf(
                                CompletionItem("src", "src=\"\"", "attribute", 5),
                                CompletionItem("alt", "alt=\"\"", "attribute", 5),
                                CompletionItem("loading", "loading=\"lazy\"", "attribute"),
                                CompletionItem("decoding", "decoding=\"async\"", "attribute"),
                                CompletionItem("width", "width=\"\"", "attribute", 7),
                                CompletionItem("height", "height=\"\"", "attribute", 8),
                        ),
                "source" to
                        listOf(
                                CompletionItem("srcset", "srcset=\"\"", "attribute", 8),
                                CompletionItem("media", "media=\"\"", "attribute", 7),
                                CompletionItem("type", "type=\"\"", "attribute", 6),
                        ),
                "script" to
                        listOf(
                                CompletionItem("src", "src=\"\"", "attribute", 5),
                                CompletionItem("type", "type=\"module\"", "attribute"),
                                CompletionItem("defer", "defer", "attribute"),
                                CompletionItem("async", "async", "attribute"),
                        ),
                "link" to
                        listOf(
                                CompletionItem("rel", "rel=\"stylesheet\"", "attribute"),
                                CompletionItem("href", "href=\"\"", "attribute", 6),
                                CompletionItem("as", "as=\"style\"", "attribute"),
                                CompletionItem("media", "media=\"\"", "attribute", 7),
                        ),
                "meta" to
                        listOf(
                                CompletionItem("name", "name=\"viewport\"", "attribute"),
                                CompletionItem("content", "content=\"\"", "attribute", 9),
                                CompletionItem("charset", "charset=\"UTF-8\"", "attribute"),
                        ),
                "button" to
                        listOf(
                                CompletionItem("type", "type=\"button\"", "attribute"),
                                CompletionItem("disabled", "disabled", "attribute"),
                                CompletionItem(
                                        "aria-pressed",
                                        "aria-pressed=\"false\"",
                                        "attribute",
                                        14
                                ),
                        ),
                "input" to
                        listOf(
                                CompletionItem("type", "type=\"text\"", "attribute"),
                                CompletionItem("name", "name=\"\"", "attribute", 6),
                                CompletionItem("value", "value=\"\"", "attribute", 7),
                                CompletionItem("placeholder", "placeholder=\"\"", "attribute", 13),
                                CompletionItem("required", "required", "attribute"),
                                CompletionItem("checked", "checked", "attribute"),
                                CompletionItem(
                                        "autocomplete",
                                        "autocomplete=\"\"",
                                        "attribute",
                                        14
                                ),
                        ),
                "label" to listOf(CompletionItem("for", "for=\"\"", "attribute", 5)),
                "form" to
                        listOf(
                                CompletionItem("action", "action=\"\"", "attribute", 8),
                                CompletionItem("method", "method=\"post\"", "attribute"),
                                CompletionItem("autocomplete", "autocomplete=\"on\"", "attribute"),
                        ),
                "select" to
                        listOf(
                                CompletionItem("name", "name=\"\"", "attribute", 6),
                                CompletionItem("multiple", "multiple", "attribute"),
                                CompletionItem("required", "required", "attribute"),
                        ),
                "option" to
                        listOf(
                                CompletionItem("value", "value=\"\"", "attribute", 7),
                                CompletionItem("selected", "selected", "attribute"),
                        ),
                "textarea" to
                        listOf(
                                CompletionItem("name", "name=\"\"", "attribute", 6),
                                CompletionItem("placeholder", "placeholder=\"\"", "attribute", 13),
                                CompletionItem("rows", "rows=\"4\"", "attribute"),
                        ),
                "video" to
                        listOf(
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
            .map { it.copy(documentationOwner = tag.orEmpty()) }
}

private fun htmlAttributeValueCompletions(
        attribute: String,
        tag: String?,
        source: String,
        cursor: Int,
): List<CompletionItem> {
    val values =
            when (attribute) {
                "class" -> htmlClassValueCompletions(source)
                "id", "for" -> htmlIdValueCompletions(source)
                "style" -> inlineStyleAttributeCompletions(source, cursor)
                "type" ->
                        when (tag) {
                            "button" -> listOf("button", "submit", "reset")
                            "input" ->
                                    listOf(
                                            "text",
                                            "email",
                                            "password",
                                            "number",
                                            "search",
                                            "tel",
                                            "url",
                                            "checkbox",
                                            "radio",
                                            "file",
                                            "date",
                                            "color",
                                            "range"
                                    )
                            "script" -> listOf("module", "text/javascript")
                            else -> listOf("button", "text", "module", "submit")
                        }.map { CompletionItem(it, it, "attribute value") }
                "rel" ->
                        listOf(
                                        "stylesheet",
                                        "preload",
                                        "modulepreload",
                                        "icon",
                                        "noopener",
                                        "noreferrer"
                                )
                                .map { CompletionItem(it, it, "attribute value") }
                "target" ->
                        listOf("_self", "_blank", "_parent", "_top").map {
                            CompletionItem(it, it, "attribute value")
                        }
                "loading" ->
                        listOf("lazy", "eager").map { CompletionItem(it, it, "attribute value") }
                "decoding" ->
                        listOf("async", "sync", "auto").map {
                            CompletionItem(it, it, "attribute value")
                        }
                "method" ->
                        listOf("get", "post", "dialog").map {
                            CompletionItem(it, it, "attribute value")
                        }
                "href" ->
                        when (tag) {
                            "link" -> listOf("style.css", "./style.css", "https://")
                            "a" -> listOf("#", "#main", "mailto:", "tel:", "https://")
                            else -> listOf("#", "./", "https://")
                        }.map { CompletionItem(it, it, "attribute value") }
                "src" ->
                        when (tag) {
                            "script" -> listOf("script.js", "./script.js", "main.js")
                            "img", "source", "video" ->
                                    listOf(
                                            "assets/",
                                            "images/",
                                            "image.png",
                                            "photo.jpg",
                                            "video.mp4"
                                    )
                            else -> listOf("./", "assets/")
                        }.map { CompletionItem(it, it, "attribute value") }
                "name" ->
                        when (tag) {
                            "meta" -> listOf("viewport", "description", "theme-color", "robots")
                            else -> listOf("name", "email", "password", "search", "message")
                        }.map { CompletionItem(it, it, "attribute value") }
                "media" ->
                        listOf(
                                        "(max-width: 700px)",
                                        "(min-width: 768px)",
                                        "(prefers-color-scheme: dark)"
                                )
                                .map { CompletionItem(it, it, "attribute value") }
                "charset" -> listOf("UTF-8").map { CompletionItem(it, it, "attribute value") }
                "autocomplete" ->
                        listOf(
                                        "on",
                                        "off",
                                        "name",
                                        "email",
                                        "username",
                                        "current-password",
                                        "new-password"
                                )
                                .map { CompletionItem(it, it, "attribute value") }
                "aria-hidden", "aria-expanded", "aria-pressed", "draggable", "contenteditable" ->
                        listOf("true", "false").map { CompletionItem(it, it, "attribute value") }
                "role" ->
                        listOf(
                                        "button",
                                        "navigation",
                                        "main",
                                        "banner",
                                        "contentinfo",
                                        "dialog",
                                        "list",
                                        "listitem",
                                        "status",
                                        "alert"
                                )
                                .map { CompletionItem(it, it, "attribute value") }
                "as" ->
                        listOf("style", "script", "image", "font", "fetch").map {
                            CompletionItem(it, it, "attribute value")
                        }
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

private val cssCompletionItems =
        listOf(
                CompletionItem("--custom-property", "--custom-property: ;", "css property", 19),
                CompletionItem("display", "display: flex;", "css property"),
                CompletionItem("position", "position: relative;", "css property"),
                CompletionItem("inset", "inset: 0;", "css property"),
                CompletionItem("grid", "display: grid;", "css property"),
                CompletionItem(
                        "grid-template-columns",
                        "grid-template-columns: repeat(3, minmax(0, 1fr));",
                        "css property"
                ),
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
                CompletionItem(
                        "box-shadow",
                        "box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);",
                        "css property"
                ),
                CompletionItem(
                        "font-family",
                        "font-family: system-ui, sans-serif;",
                        "css property"
                ),
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

private val cssAtRuleCompletions =
        listOf(
                CompletionItem("@media", "@media (max-width: 700px) {\n    \n}", "at-rule", 31),
                CompletionItem("@supports", "@supports (display: grid) {\n    \n}", "at-rule", 31),
                CompletionItem(
                        "@container",
                        "@container (min-width: 480px) {\n    \n}",
                        "at-rule",
                        35
                ),
                CompletionItem(
                        "@keyframes",
                        "@keyframes fade-in {\n    from { opacity: 0; }\n    to { opacity: 1; }\n}",
                        "at-rule",
                        11
                ),
                CompletionItem(
                        "@font-face",
                        "@font-face {\n    font-family: '';\n    src: url('');\n}",
                        "at-rule",
                        33
                ),
                CompletionItem("@import", "@import url('');", "at-rule", 13),
                CompletionItem("@layer", "@layer components {\n    \n}", "at-rule", 24),
        )

private val cssSelectorCompletions =
        listOf(
                CompletionItem("body", "body {\n    \n}", "selector", 11),
                CompletionItem(":root", ":root {\n    \n}", "selector", 12),
                CompletionItem("*", "* {\n    box-sizing: border-box;\n}", "selector"),
                CompletionItem("::before", "::before", "selector"),
                CompletionItem("::after", "::after", "selector"),
        )

private val cssPseudoClassCompletions =
        listOf(
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

private val cssValueCompletions =
        listOf(
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
                CompletionItem(
                        "linear-gradient()",
                        "linear-gradient(135deg, #111827, #426bde)",
                        "css value",
                        16
                ),
                CompletionItem("calc()", "calc(100% - 1rem)", "css value", 5),
                CompletionItem("clamp()", "clamp(1rem, 2vw, 2rem)", "css value", 6),
                CompletionItem("var()", "var(--color)", "css value", 6),
        )

private val cssPropertyValueOptions =
        mapOf(
                "display" to listOf("block", "inline-block", "flex", "grid", "none", "contents"),
                "position" to listOf("relative", "absolute", "fixed", "sticky", "static"),
                "flex-direction" to listOf("row", "column", "row-reverse", "column-reverse"),
                "flex-wrap" to listOf("nowrap", "wrap", "wrap-reverse"),
                "align-items" to listOf("stretch", "center", "flex-start", "flex-end", "baseline"),
                "justify-content" to
                        listOf(
                                "center",
                                "flex-start",
                                "flex-end",
                                "space-between",
                                "space-around",
                                "space-evenly"
                        ),
                "place-items" to listOf("center", "stretch", "start", "end"),
                "overflow" to listOf("visible", "hidden", "clip", "scroll", "auto"),
                "object-fit" to listOf("cover", "contain", "fill", "scale-down", "none"),
                "background" to
                        listOf(
                                "#ffffff",
                                "transparent",
                                "linear-gradient(135deg, #111827, #426bde)",
                                "var(--background)"
                        ),
                "background-color" to listOf("#ffffff", "transparent", "var(--background)"),
                "color" to listOf("#111827", "#ffffff", "currentColor", "var(--color)"),
                "text-align" to listOf("left", "center", "right", "start", "end"),
                "font-family" to listOf("system-ui, sans-serif", "Inter, sans-serif", "monospace"),
                "font-weight" to listOf("400", "500", "600", "700", "bold"),
                "border-radius" to listOf("0", "4px", "8px", "999px", "50%"),
                "grid-template-columns" to
                        listOf(
                                "repeat(2, minmax(0, 1fr))",
                                "repeat(3, minmax(0, 1fr))",
                                "1fr auto",
                                "minmax(0, 1fr)"
                        ),
                "transition" to
                        listOf("all 180ms ease", "color 180ms ease", "transform 180ms ease"),
                "cursor" to listOf("pointer", "default", "grab", "not-allowed", "text"),
                "border-style" to listOf("solid", "dashed", "dotted", "none"),
                "transition-timing-function" to
                        listOf("ease", "ease-in", "ease-out", "ease-in-out", "linear"),
        )

private val jsCompletionItems =
        listOf(
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
                CompletionItem(
                        "class",
                        "class Name {\n    constructor() {\n        \n    }\n}",
                        "snippet",
                        6
                ),
                CompletionItem("if", "if (condition) {\n    \n}", "snippet", 4),
                CompletionItem("else", "else {\n    \n}", "snippet", 7),
                CompletionItem("for", "for (const item of items) {\n    \n}", "snippet", 11),
                CompletionItem("while", "while (condition) {\n    \n}", "snippet", 7),
                CompletionItem(
                        "switch",
                        "switch (value) {\n    case value:\n        break;\n    default:\n        break;\n}",
                        "snippet",
                        8
                ),
                CompletionItem("forEach", "items.forEach(item => {\n    \n});", "snippet", 25),
                CompletionItem("map", "items.map(item => item)", "array", 10),
                CompletionItem("filter", "items.filter(item => item)", "array", 13),
                CompletionItem("reduce", "items.reduce((total, item) => total, 0)", "array", 15),
                CompletionItem(
                        "DOMContentLoaded",
                        "document.addEventListener('DOMContentLoaded', () => {\n    \n});",
                        "dom",
                        49
                ),
                CompletionItem(
                        "try",
                        "try {\n    \n} catch (error) {\n    console.error(error);\n}",
                        "snippet",
                        10
                ),
                CompletionItem(
                        "addEventListener",
                        "addEventListener('click', () => {\n    \n})",
                        "dom",
                        18
                ),
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
                CompletionItem(
                        "fetch",
                        "fetch('').then(response => response.json())",
                        "function",
                        7
                ),
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
                CompletionItem(
                        "setInterval",
                        "setInterval(() => {\n    \n}, 1000)",
                        "function",
                        20
                ),
                CompletionItem(
                        "requestAnimationFrame",
                        "requestAnimationFrame(() => {\n    \n})",
                        "function",
                        30
                ),
                CompletionItem("parseInt", "parseInt(value, 10)", "function", 9),
                CompletionItem(
                        "useState",
                        "const [state, setState] = useState(initialValue);",
                        "react hook",
                        13
                ),
                CompletionItem("useEffect", "useEffect(() => {\n    \n}, []);", "react hook", 16),
                CompletionItem("useRef", "const ref = useRef(null);", "react hook", 12),
                CompletionItem(
                        "useMemo",
                        "const memo = useMemo(() => value, []);",
                        "react hook",
                        13
                ),
                CompletionItem("interface", "interface Name {\n    \n}", "typescript", 10),
                CompletionItem("type", "type Name = ", "typescript", 5),
                CompletionItem("enum", "enum Name {\n    \n}", "typescript", 5),
        )

private val jsLibraryCompletionItems =
        listOf(
                CompletionItem("React", "React", "library"),
                CompletionItem("ReactDOM", "ReactDOM", "library"),
                CompletionItem(
                        "createRoot",
                        "createRoot(document.getElementById('root'))",
                        "react",
                        11
                ),
                CompletionItem("Vue", "Vue", "library"),
                CompletionItem(
                        "createApp",
                        "createApp({\n    data: () => ({})\n}).mount('#app')",
                        "vue",
                        10
                ),
                CompletionItem("THREE", "THREE", "library"),
                CompletionItem("gsap", "gsap", "library"),
                CompletionItem("anime", "anime", "library"),
                CompletionItem("axios", "axios", "library"),
                CompletionItem("_", "_", "lodash"),
                CompletionItem("d3", "d3", "library"),
                CompletionItem("Chart", "Chart", "library"),
                CompletionItem("dayjs", "dayjs", "library"),
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
                CompletionItem(
                        "import React",
                        "import React from \"react\";\nimport { createRoot } from \"react-dom/client\";",
                        "snippet"
                ),
                CompletionItem("import Vue", "import { createApp } from \"vue\";", "snippet"),
                CompletionItem("import THREE", "import * as THREE from \"three\";", "snippet"),
                CompletionItem("import gsap", "import gsap from \"gsap\";", "snippet"),
                CompletionItem("import axios", "import axios from \"axios\";", "snippet"),
        )

private val jsDocumentCompletions =
        listOf(
                        CompletionItem("querySelector()", "querySelector('')", "dom", 15),
                        CompletionItem("querySelectorAll()", "querySelectorAll('')", "dom", 18),
                        CompletionItem("getElementById()", "getElementById('')", "dom", 16),
                        CompletionItem(
                                "getElementsByClassName()",
                                "getElementsByClassName('')",
                                "dom",
                                24
                        ),
                        CompletionItem("createElement()", "createElement('div')", "dom", 16),
                        CompletionItem(
                                "addEventListener()",
                                "addEventListener('DOMContentLoaded', () => {\n    \n})",
                                "dom",
                                40
                        ),
                        CompletionItem("body", "body", "dom"),
                        CompletionItem("head", "head", "dom"),
                        CompletionItem("documentElement", "documentElement", "dom"),
                )
                .map { it.copy(documentationOwner = "Document") }

private val jsWindowCompletions =
        listOf(
                        CompletionItem(
                                "addEventListener()",
                                "addEventListener('resize', () => {\n    \n})",
                                "dom",
                                31
                        ),
                        CompletionItem("location", "location", "dom"),
                        CompletionItem("innerWidth", "innerWidth", "dom"),
                        CompletionItem("innerHeight", "innerHeight", "dom"),
                        CompletionItem(
                                "requestAnimationFrame()",
                                "requestAnimationFrame(() => {\n    \n})",
                                "function",
                                30
                        ),
                        CompletionItem("matchMedia()", "matchMedia('')", "function", 12),
                )
                .map { it.copy(documentationOwner = "Window") }

private val jsStorageCompletions =
        listOf(
                        CompletionItem("getItem()", "getItem('')", "dom", 9),
                        CompletionItem("setItem()", "setItem('', '')", "dom", 9),
                        CompletionItem("removeItem()", "removeItem('')", "dom", 12),
                        CompletionItem("clear()", "clear()", "dom", 6),
                )
                .map { it.copy(documentationOwner = "Storage") }

private val jsJsonCompletions =
        listOf(
                        CompletionItem("parse()", "parse('')", "function", 7),
                        CompletionItem("stringify()", "stringify(value, null, 2)", "function", 10),
                )
                .map { it.copy(documentationOwner = "JSON") }

private val jsObjectCompletions =
        listOf(
                        CompletionItem("keys()", "keys(object)", "function", 5),
                        CompletionItem("values()", "values(object)", "function", 7),
                        CompletionItem("entries()", "entries(object)", "function", 8),
                        CompletionItem("assign()", "assign(target, source)", "function", 7),
                        CompletionItem("hasOwn()", "hasOwn(object, key)", "function", 7),
                )
                .map { it.copy(documentationOwner = "ObjectConstructor") }

private val jsArrayCompletions =
        listOf(
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
                .map { it.copy(documentationOwner = "Array") }

private val jsStringCompletions =
        listOf(
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
                .map { it.copy(documentationOwner = "String") }

private val jsNumberCompletions =
        listOf(
                        CompletionItem("toFixed()", "toFixed(2)", "number", 8),
                        CompletionItem("toString()", "toString()", "number", 9),
                        CompletionItem("toLocaleString()", "toLocaleString()", "number", 15),
                )
                .map { it.copy(documentationOwner = "Number") }

private val jsDateCompletions =
        listOf(
                        CompletionItem("toISOString()", "toISOString()", "date", 12),
                        CompletionItem("toLocaleDateString()", "toLocaleDateString()", "date", 19),
                        CompletionItem("getFullYear()", "getFullYear()", "date", 12),
                        CompletionItem("getMonth()", "getMonth()", "date", 9),
                        CompletionItem("getDate()", "getDate()", "date", 8),
                        CompletionItem("getTime()", "getTime()", "date", 8),
                )
                .map { it.copy(documentationOwner = "Date") }

private val jsPromiseCompletions =
        listOf(
                        CompletionItem("then()", "then(value => value)", "promise", 5),
                        CompletionItem(
                                "catch()",
                                "catch(error => {\n    console.error(error);\n})",
                                "promise",
                                6
                        ),
                        CompletionItem("finally()", "finally(() => {\n    \n})", "promise", 16),
                )
                .map { it.copy(documentationOwner = "Promise") }

private val jsPromiseStaticCompletions =
        listOf(
                        CompletionItem("resolve()", "resolve(value)", "promise", 8),
                        CompletionItem("reject()", "reject(error)", "promise", 7),
                        CompletionItem("all()", "all([])", "promise", 4),
                        CompletionItem("race()", "race([])", "promise", 5),
                        CompletionItem("allSettled()", "allSettled([])", "promise", 11),
                )
                .map { it.copy(documentationOwner = "PromiseConstructor") }

private val jsNodeListCompletions =
        listOf(
                        CompletionItem("forEach()", "forEach(element => {\n    \n})", "dom", 20),
                        CompletionItem("item()", "item(0)", "dom", 5),
                        CompletionItem("entries()", "entries()", "dom", 8),
                        CompletionItem("length", "length", "dom"),
                )
                .map { it.copy(documentationOwner = "NodeList") }

private val jsResponseCompletions =
        listOf(
                        CompletionItem("json()", "json()", "promise", 5),
                        CompletionItem("text()", "text()", "promise", 5),
                        CompletionItem("blob()", "blob()", "promise", 5),
                        CompletionItem("ok", "ok", "dom"),
                        CompletionItem("status", "status", "dom"),
                        CompletionItem("headers", "headers", "dom"),
                )
                .map { it.copy(documentationOwner = "Response") }

private val jsHeadersCompletions =
        listOf(
                        CompletionItem("get()", "get('content-type')", "dom", 5),
                        CompletionItem("has()", "has('content-type')", "dom", 5),
                        CompletionItem("set()", "set('', '')", "dom", 5),
                        CompletionItem("append()", "append('', '')", "dom", 8),
                        CompletionItem("entries()", "entries()", "dom", 8),
                )
                .map { it.copy(documentationOwner = "Headers") }

private val jsUrlCompletions =
        listOf(
                        CompletionItem("href", "href", "dom"),
                        CompletionItem("origin", "origin", "dom"),
                        CompletionItem("pathname", "pathname", "dom"),
                        CompletionItem("search", "search", "dom"),
                        CompletionItem("searchParams", "searchParams", "dom"),
                        CompletionItem("toString()", "toString()", "function", 9),
                )
                .map { it.copy(documentationOwner = "URL") }

private val jsSearchParamsCompletions =
        listOf(
                        CompletionItem("get()", "get('')", "dom", 5),
                        CompletionItem("set()", "set('', '')", "dom", 5),
                        CompletionItem("append()", "append('', '')", "dom", 8),
                        CompletionItem("delete()", "delete('')", "dom", 8),
                        CompletionItem("has()", "has('')", "dom", 5),
                        CompletionItem("toString()", "toString()", "function", 9),
                )
                .map { it.copy(documentationOwner = "URLSearchParams") }

private val jsLocationCompletions =
        listOf(
                        CompletionItem("href", "href", "dom"),
                        CompletionItem("pathname", "pathname", "dom"),
                        CompletionItem("search", "search", "dom"),
                        CompletionItem("hash", "hash", "dom"),
                        CompletionItem("assign()", "assign('')", "dom", 8),
                        CompletionItem("replace()", "replace('')", "dom", 9),
                        CompletionItem("reload()", "reload()", "dom", 7),
                )
                .map { it.copy(documentationOwner = "Location") }

private val jsHistoryCompletions =
        listOf(
                        CompletionItem("pushState()", "pushState({}, '', '')", "dom", 10),
                        CompletionItem("replaceState()", "replaceState({}, '', '')", "dom", 13),
                        CompletionItem("back()", "back()", "dom", 5),
                        CompletionItem("forward()", "forward()", "dom", 8),
                )
                .map { it.copy(documentationOwner = "History") }

private val jsNavigatorCompletions =
        listOf(
                        CompletionItem("clipboard", "clipboard", "dom"),
                        CompletionItem("language", "language", "dom"),
                        CompletionItem("onLine", "onLine", "dom"),
                        CompletionItem("userAgent", "userAgent", "dom"),
                        CompletionItem("share()", "share({ title: '', url: '' })", "dom", 6),
                )
                .map { it.copy(documentationOwner = "Navigator") }

private val jsClipboardCompletions =
        listOf(
                        CompletionItem("writeText()", "writeText('')", "promise", 11),
                        CompletionItem("readText()", "readText()", "promise", 9),
                )
                .map { it.copy(documentationOwner = "Clipboard") }

private val jsEventCompletions =
        listOf(
                        CompletionItem("target", "target", "dom"),
                        CompletionItem("currentTarget", "currentTarget", "dom"),
                        CompletionItem("preventDefault()", "preventDefault()", "dom", 15),
                        CompletionItem("stopPropagation()", "stopPropagation()", "dom", 16),
                        CompletionItem("key", "key", "dom"),
                        CompletionItem("clientX", "clientX", "dom"),
                        CompletionItem("clientY", "clientY", "dom"),
                )
                .map { it.copy(documentationOwner = "Event") }

private val jsDatasetCompletions =
        listOf(
                        CompletionItem("id", "id", "dom"),
                        CompletionItem("state", "state", "dom"),
                        CompletionItem("value", "value", "dom"),
                        CompletionItem("index", "index", "dom"),
                )
                .map { it.copy(documentationOwner = "DOMStringMap") }

private val jsStyleCompletions =
        listOf(
                        CompletionItem("display", "display = ''", "style", 11),
                        CompletionItem("color", "color = ''", "style", 9),
                        CompletionItem("background", "background = ''", "style", 14),
                        CompletionItem("backgroundColor", "backgroundColor = ''", "style", 19),
                        CompletionItem("width", "width = ''", "style", 9),
                        CompletionItem("height", "height = ''", "style", 10),
                        CompletionItem("transform", "transform = ''", "style", 13),
                        CompletionItem("opacity", "opacity = ''", "style", 11),
                )
                .map { it.copy(documentationOwner = "CSSStyleDeclaration") }

private val jsElementCompletions =
        listOf(
                        CompletionItem(
                                "addEventListener()",
                                "addEventListener('click', () => {\n    \n})",
                                "dom",
                                18
                        ),
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
                .map { it.copy(documentationOwner = "Element") }

private val jsReactCompletions =
        listOf(
                CompletionItem("createElement()", "createElement('div', null)", "react", 14),
                CompletionItem("useState()", "useState(initialValue)", "react", 9),
                CompletionItem("useEffect()", "useEffect(() => {\n    \n}, [])", "react", 17),
                CompletionItem("useMemo()", "useMemo(() => value, [])", "react", 8),
                CompletionItem("useRef()", "useRef(null)", "react", 7),
                CompletionItem("Fragment", "Fragment", "react"),
        )

private val jsReactDomCompletions =
        listOf(
                CompletionItem(
                        "createRoot()",
                        "createRoot(document.getElementById('root'))",
                        "react",
                        11
                ),
                CompletionItem("render()", "render()", "react", 7),
        )

private val jsVueCompletions =
        listOf(
                CompletionItem("createApp()", "createApp({}).mount('#app')", "vue", 10),
                CompletionItem("ref()", "ref()", "vue", 4),
                CompletionItem("reactive()", "reactive({})", "vue", 9),
                CompletionItem("computed()", "computed(() => value)", "vue", 9),
                CompletionItem("watch()", "watch(source, callback)", "vue", 6),
        )

private val jsThreeCompletions =
        listOf(
                CompletionItem("Scene()", "Scene()", "three", 6),
                CompletionItem(
                        "PerspectiveCamera()",
                        "PerspectiveCamera(75, innerWidth / innerHeight, 0.1, 1000)",
                        "three",
                        18
                ),
                CompletionItem("WebGLRenderer()", "WebGLRenderer({ canvas })", "three", 14),
                CompletionItem("Mesh()", "Mesh(geometry, material)", "three", 5),
                CompletionItem("BoxGeometry()", "BoxGeometry(1, 1, 1)", "three", 12),
                CompletionItem(
                        "MeshBasicMaterial()",
                        "MeshBasicMaterial({ color: 0xffffff })",
                        "three",
                        18
                ),
        )

private val jsAnimationCompletions =
        listOf(
                CompletionItem("to()", "to(target, { duration: 0.5 })", "animation", 3),
                CompletionItem("from()", "from(target, { duration: 0.5 })", "animation", 5),
                CompletionItem("fromTo()", "fromTo(target, fromVars, toVars)", "animation", 7),
                CompletionItem("timeline()", "timeline()", "animation", 10),
        )

private val jsHttpCompletions =
        listOf(
                CompletionItem("get()", "get('')", "http", 5),
                CompletionItem("post()", "post('', {})", "http", 6),
                CompletionItem("put()", "put('', {})", "http", 5),
                CompletionItem("delete()", "delete('')", "http", 8),
        )

private val jsChartCompletions =
        listOf(
                CompletionItem("Chart()", "Chart(ctx, { type: 'bar', data: {} })", "chart", 6),
                CompletionItem("register()", "register()", "chart", 9),
        )

private val jsD3Completions =
        listOf(
                CompletionItem("select()", "select('')", "d3", 8),
                CompletionItem("selectAll()", "selectAll('')", "d3", 11),
                CompletionItem("scaleLinear()", "scaleLinear()", "d3", 12),
                CompletionItem("axisBottom()", "axisBottom(scale)", "d3", 11),
                CompletionItem("json()", "json('')", "d3", 6),
        )

private val jsJqueryCompletions =
        listOf(
                CompletionItem("on()", "on('click', () => {})", "jquery", 4),
                CompletionItem("addClass()", "addClass('')", "jquery", 10),
                CompletionItem("removeClass()", "removeClass('')", "jquery", 13),
                CompletionItem("css()", "css('', '')", "jquery", 5),
                CompletionItem("html()", "html('')", "jquery", 6),
                CompletionItem("ajax()", "ajax({ url: '' })", "jquery", 6),
        )

private val jsLodashCompletions =
        listOf(
                CompletionItem("map()", "map(collection, iteratee)", "lodash", 4),
                CompletionItem("filter()", "filter(collection, predicate)", "lodash", 7),
                CompletionItem("debounce()", "debounce(func, 250)", "lodash", 9),
                CompletionItem("throttle()", "throttle(func, 250)", "lodash", 9),
                CompletionItem("cloneDeep()", "cloneDeep(value)", "lodash", 10),
        )

private val jsSupabaseCompletions =
        listOf(
                CompletionItem("createClient()", "createClient(url, anonKey)", "supabase", 13),
                CompletionItem("from()", "from('table')", "supabase", 5),
                CompletionItem("auth", "auth", "supabase"),
        )

private val jsonCompletionItems =
        listOf(
                CompletionItem("\"name\"", "\"name\": \"\"", "field", 9),
                CompletionItem("\"version\"", "\"version\": \"1.0.0\"", "field"),
                CompletionItem(
                        "\"scripts\"",
                        "\"scripts\": {\n    \"start\": \"\"\n}",
                        "field",
                        29
                ),
                CompletionItem("\"dependencies\"", "\"dependencies\": {\n    \n}", "field", 23),
        )

private fun cssValueCompletionsFor(source: String, cursor: Int): List<CompletionItem> {
    val line = source.substring(0, cursor.coerceIn(0, source.length)).substringAfterLast('\n')
    val property = line.substringBefore(':').trim().lowercase()
    val variableValues =
            cssCustomPropertyValueCompletions(source).map {
                CompletionItem(
                        "var(${it.label})",
                        "var(${it.label})",
                        "css value",
                        it.label.length + 5
                )
            }
    val propertyOptions =
            cssPropertyValueOptions[property]?.map { CompletionItem(it, it, "css value") }
    val colorValues =
            cssValueCompletions.filter {
                it.label.startsWith("#") ||
                        it.label.startsWith("rgba") ||
                        it.label.startsWith("var")
            }
    val base =
            when {
                property in
                        setOf(
                                "color",
                                "background",
                                "background-color",
                                "border-color",
                                "outline-color",
                                "box-shadow"
                        ) -> colorValues
                propertyOptions != null -> propertyOptions
                else -> cssValueCompletions
            }
    return (base + variableValues).distinctBy { it.label }.map {
        it.copy(documentationOwner = property)
    }
}

private fun cssContextualCompletions(source: String, cursor: Int): List<CompletionItem> {
    val before = source.substring(0, cursor.coerceIn(0, source.length))
    val line = cssLineBeforeCursor(before)
    val lastOpen = before.lastIndexOf('{')
    val lastClose = before.lastIndexOf('}')
    val insideRuleBlock = lastOpen > lastClose

    return when {
        isCssAtRuleCompletion(before) -> cssAtRuleCompletions
        !insideRuleBlock ->
                userDefinedCssCompletions(source) +
                        cssAtRuleCompletions +
                        cssSelectorCompletions +
                        cssPseudoClassCompletions
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
    val classes =
            Regex("""class\s*=\s*["']([^"']+)["']""")
                    .findAll(source)
                    .flatMap { it.groupValues[1].split(Regex("\\s+")) }
                    .filter { it.isNotBlank() }
                    .map { CompletionItem(".$it", "class=\"$it\"", "attribute", it.length + 8) }
                    .toList()
    val ids =
            Regex("""id\s*=\s*["']([^"']+)["']""")
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
    val variables =
            cssCustomPropertyValueCompletions(source).map {
                CompletionItem(
                        "var(${it.label})",
                        "var(${it.label})",
                        "css value",
                        it.label.length + 5
                )
            }
    val selectors =
            Regex("""(?m)([.#][A-Za-z0-9_-]+)\s*\{""")
                    .findAll(source)
                    .map { CompletionItem(it.groupValues[1], it.groupValues[1], "selector") }
                    .toList()
    val htmlClasses =
            Regex("""class\s*=\s*["']([^"']+)["']""")
                    .findAll(source)
                    .flatMap { it.groupValues[1].split(Regex("\\s+")) }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { CompletionItem(".$it", ".$it", "selector") }
                    .toList()
    val htmlIds =
            Regex("""id\s*=\s*["']([^"']+)["']""")
                    .findAll(source)
                    .map { it.groupValues[1].trim() }
                    .filter { it.isNotBlank() }
                    .map { CompletionItem("#$it", "#$it", "selector") }
                    .toList()
    return (variables + selectors + htmlClasses + htmlIds).distinctBy { it.label }
}

private fun userDefinedJsCompletions(source: String): List<CompletionItem> {
    val functions =
            Regex("""\bfunction\s+([A-Za-z_$][\w$]*)\s*\(([^)]*)\)""")
                    .findAll(source)
                    .map { match ->
                        val name = match.groupValues[1]
                        CompletionItem("$name()", "$name()", "function", name.length + 1)
                    }
                    .toList()
    val arrowFunctions =
            Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)\s*=\s*(?:async\s*)?\([^)]*\)\s*=>""")
                    .findAll(source)
                    .map { match ->
                        val name = match.groupValues[1]
                        CompletionItem("$name()", "$name()", "function", name.length + 1)
                    }
                    .toList()
    val classes =
            Regex("""\bclass\s+([A-Za-z_$][\w$]*)""")
                    .findAll(source)
                    .map { CompletionItem(it.groupValues[1], it.groupValues[1], "class") }
                    .toList()
    val variables =
            Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)""")
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
        "supabase" -> jsSupabaseCompletions
        "classList" ->
                listOf(
                        CompletionItem("add()", "add('')", "dom", 5),
                        CompletionItem("remove()", "remove('')", "dom", 8),
                        CompletionItem("toggle()", "toggle('')", "dom", 8),
                        CompletionItem("contains()", "contains('')", "dom", 10),
                )
        "console" ->
                listOf(
                        CompletionItem("log()", "log()", "function", 4),
                        CompletionItem("error()", "error()", "function", 6),
                        CompletionItem("warn()", "warn()", "function", 5),
                        CompletionItem("table()", "table()", "function", 6),
                )
        "math" ->
                listOf(
                        CompletionItem("random()", "random()", "function", 7),
                        CompletionItem("round()", "round()", "function", 6),
                        CompletionItem("floor()", "floor()", "function", 6),
                        CompletionItem("ceil()", "ceil()", "function", 5),
                        CompletionItem("max()", "max()", "function", 4),
                        CompletionItem("min()", "min()", "function", 4),
                )
        else ->
                jsDocumentCompletions +
                        jsElementCompletions +
                        jsArrayCompletions +
                        jsStringCompletions
    }
}

private fun jsDotCompletionsForSource(source: String, dotIndex: Int): List<CompletionItem> {
    val targetName = jsDotTargetName(source, dotIndex)
    val customMembers = targetName?.let { userDefinedJsMemberCompletions(source, it) }.orEmpty()
    return (customMembers + jsDotCompletionsForType(inferJsDotTargetType(source, dotIndex)))
            .distinctBy { it.completionKey() }
}

private fun jsDotTargetName(source: String, dotIndex: Int): String? {
    val beforeDot = source.substring(0, dotIndex.coerceIn(0, source.length)).trimEnd()
    return Regex("""([A-Za-z_$][\w$]*)$""").find(beforeDot)?.groupValues?.getOrNull(1)
}

private fun userDefinedJsMemberCompletions(source: String, variable: String): List<CompletionItem> {
    val objectBody =
            Regex("""(?s)\b(?:const|let|var)\s+${Regex.escape(variable)}\s*=\s*\{(.*?)\}""")
                    .findAll(source)
                    .lastOrNull()
                    ?.groupValues
                    ?.getOrNull(1)
                    ?: return emptyList()
    val properties =
            Regex("""(?m)\b([A-Za-z_$][\w$]*)\s*:""").findAll(objectBody).map { it.groupValues[1] }
    val methods =
            Regex("""(?m)\b([A-Za-z_$][\w$]*)\s*\([^)]*\)\s*\{""").findAll(objectBody).map {
                "${it.groupValues[1]}()"
            }
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
    if (beforeDot.endsWith("supabase")) return "supabase"
    if (beforeDot.endsWith("headers")) return "headers"
    if (beforeDot.endsWith("searchParams")) return "searchParams"
    if (beforeDot.endsWith("target") || beforeDot.endsWith("currentTarget")) return "element"
    if (beforeDot.endsWith("\"") || beforeDot.endsWith("'") || beforeDot.endsWith("`"))
            return "string"
    if (beforeDot.endsWith("]")) return "array"
    if (Regex("""(?:querySelectorAll|getElementsByClassName|getElementsByTagName)\([^)]*\)$""")
                    .containsMatchIn(beforeDot)
    )
            return "nodeList"
    if (Regex("""Array\.from\([^)]*\)$""").containsMatchIn(beforeDot)) return "array"
    if (Regex("""(?:querySelector|getElementById|createElement)\([^)]*\)$""")
                    .containsMatchIn(beforeDot)
    )
            return "element"
    if (Regex("""(?:new\s+Date|Date)\([^)]*\)$""").containsMatchIn(beforeDot)) return "date"
    if (Regex("""new\s+URL\([^)]*\)$""").containsMatchIn(beforeDot)) return "url"
    if (Regex("""fetch\([^)]*\)$""").containsMatchIn(beforeDot)) return "promise"
    val variable =
            Regex("""([A-Za-z_$][\w$]*)$""").find(beforeDot)?.groupValues?.getOrNull(1)
                    ?: return null
    if (variable.equals("response", ignoreCase = true)) return "response"
    if (variable.equals("event", ignoreCase = true)) return "event"
    if (variable.equals("e", ignoreCase = true)) return "event"
    val assignment =
            Regex("""(?m)\b(?:const|let|var)\s+${Regex.escape(variable)}\s*=\s*(.+)$""")
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
        assignment.contains("querySelectorAll(") ||
                assignment.contains("getElementsByClassName(") ||
                assignment.contains("getElementsByTagName(") -> "nodeList"
        assignment.startsWith("\"") || assignment.startsWith("'") || assignment.startsWith("`") ->
                "string"
        assignment.contains("querySelector(") ||
                assignment.contains("getElementById(") ||
                assignment.contains("createElement(") -> "element"
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
    while (nameStart > 0 &&
            (isIdentifierPart(source[nameStart - 1]) || source[nameStart - 1] == '.')) nameStart -=
            1
    if (nameStart == nameEnd) return null
    val name = source.substring(nameStart, nameEnd).substringAfterLast('.')
    return webCompletionSyntaxes["$name|function"]
            ?: webCompletionSyntaxes["$name|dom"] ?: webCompletionSyntaxes["$name|array"]
                    ?: webCompletionSyntaxes["$name|string"]
                    ?: when (name) {
                "rgba" -> "rgba(red, green, blue, alpha)"
                "repeat" -> "repeat(count, track-size)"
                "var" -> "var(--custom-property, fallback)"
                "useState" -> "useState(initialState)"
                "useEffect" -> "useEffect(effect, deps?)"
                "useMemo" -> "useMemo(factory, deps?)"
                "useRef" -> "useRef(initialValue)"
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
            // Keep template flag referenced so Kotlin does not optimize away intent in this
            // lightweight scanner.
        }
    }
    return stack.lastOrNull()
}

private fun applySmartWebEditorChange(
        current: TextFieldValue,
        requested: TextFieldValue,
        fileName: String,
): TextFieldValue {
    val insertedText =
            directlyInsertedCharacter(current, requested)
                    ?: singleInsertedText(current.text, requested.text)?.second ?: return requested
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
        '<' ->
                if (fileExtension(fileName) in htmlFileExtensions)
                        wrapOrInsertPair(current, "<", ">")
                else requested
        '\n' ->
                if (current.selection.collapsed) insertWebIndentedNewline(current, fileName)
                else requested
        else -> requested
    }
}

private fun directlyInsertedCharacter(
        current: TextFieldValue,
        requested: TextFieldValue,
): String? {
    if (!current.selection.collapsed || !requested.selection.collapsed) return null
    if (requested.text.length != current.text.length + 1) return null
    val insertionPoint = current.selection.start
    if (requested.selection.start != insertionPoint + 1) return null
    return requested.text.substring(insertionPoint, insertionPoint + 1)
}

private fun insertWebIndentedNewline(value: TextFieldValue, fileName: String): TextFieldValue {
    val cursor = value.selection.start
    val currentLine = value.text.substring(0, cursor).substringAfterLast('\n')
    val existingIndent = currentLine.takeWhile { it == ' ' || it == '\t' }
    val trimmed = currentLine.trimEnd()
    val blockIndent =
            when {
                trimmed.endsWith("{") || trimmed.endsWith("(") || trimmed.endsWith("[") -> "    "
                fileExtension(fileName) in htmlFileExtensions && opensHtmlBlock(trimmed) -> "    "
                else -> ""
            }
    return insertCodeAtSelection(value, "\n$existingIndent$blockIndent")
}

private fun insertClosingHtmlTag(value: TextFieldValue): TextFieldValue {
    val tag =
            openHtmlTagsBefore(value.text, value.selection.start).lastOrNull()
                    ?: return insertCodeAtSelection(value, "</")
    return insertCodeAtSelection(value, "</$tag>")
}

private fun singleInsertedText(oldText: String, newText: String): Pair<Int, String>? {
    if (newText.length <= oldText.length) return null
    var prefix = 0
    val shortest = minOf(oldText.length, newText.length)
    while (prefix < shortest && oldText[prefix] == newText[prefix]) prefix += 1
    var suffix = 0
    while (suffix < oldText.length - prefix &&
            suffix < newText.length - prefix &&
            oldText[oldText.lastIndex - suffix] == newText[newText.lastIndex - suffix]) {
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
        "py", "pyw", "php", "phtml", "sql" ->
                delimiterDiagnostics(
                        source,
                        allowLineComments = fileExtension(fileName) in setOf("php", "phtml"),
                )
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
        val tagName =
                Regex("""^<\s*/?\s*([A-Za-z][A-Za-z0-9-]*)""")
                        .find(token)
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.lowercase()
                        ?: return@forEach

        // Check for duplicate IDs
        Regex("""\sid\s*=\s*["']([^"']+)["']""").find(token)?.let { idMatch ->
            val id = idMatch.groupValues[1]
            if (id in ids) {
                diagnostics +=
                        diagnosticAt(
                                source,
                                match.range.first + idMatch.range.first,
                                match.range.first + idMatch.range.last + 1,
                                "warning",
                                "Duplicate ID '$id' found in document."
                        )
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
                    diagnostics +=
                            diagnosticAt(
                                    source,
                                    match.range.first,
                                    match.range.last + 1,
                                    "error",
                                    "Closing tag </$tagName> has no matching opening tag."
                            )
                } else {
                    for (index in stack.lastIndex downTo last + 1) {
                        val unclosed = stack.removeAt(index)
                        diagnostics +=
                                diagnosticAt(
                                        source,
                                        unclosed.second,
                                        unclosed.second + unclosed.first.length + 2,
                                        "error",
                                        "Element <${unclosed.first}> is not closed before </$tagName>."
                                )
                    }
                    stack.removeAt(last)
                }
            }
            !selfClosing -> stack += tagName to match.range.first
        }

        // Image alt check
        if (tagName == "img" && !token.contains("alt=", ignoreCase = true)) {
            diagnostics +=
                    diagnosticAt(
                            source,
                            match.range.first,
                            match.range.last + 1,
                            "warning",
                            "Images should have an 'alt' attribute for accessibility."
                    )
        }

        Regex("""\s([A-Za-z_:][-A-Za-z0-9_:.]*)\s*=\s*([^"'\s>][^\s>]*)""")
                .findAll(maskHtmlQuotedAttributeValues(token))
                .forEach { attr ->
                    val start = match.range.first + attr.range.first
                    diagnostics +=
                            diagnosticAt(
                                    source,
                                    start,
                                    start + attr.value.length,
                                    "warning",
                                    "Quote attribute values for predictable HTML parsing."
                            )
                }
    }
    diagnostics += inspectEmbeddedWebBlocks(source)
    stack.asReversed().forEach { (tag, start) ->
        diagnostics +=
                diagnosticAt(
                        source,
                        start,
                        (start + tag.length + 2).coerceAtMost(source.length),
                        "error",
                        "Element <$tag> is missing a closing tag."
                )
    }
    if (source.isNotBlank() && !source.contains("<!DOCTYPE html>", ignoreCase = true)) {
        diagnostics +=
                diagnosticAt(
                        source,
                        0,
                        source.lineEndAt(0),
                        "hint",
                        "Add <!DOCTYPE html> at the top for standards mode."
                )
    }
    if (source.contains("<head", ignoreCase = true) &&
                    !source.contains("name=\"viewport\"", ignoreCase = true)
    ) {
        val headStart = source.indexOf("<head", ignoreCase = true).coerceAtLeast(0)
        diagnostics +=
                diagnosticAt(
                        source,
                        headStart,
                        source.lineEndAt(headStart),
                        "warning",
                        "Add a viewport meta tag for mobile layouts."
                )
    }
    return diagnostics
}

private fun embeddedWebBlocks(source: String): List<EmbeddedWebBlock> {
    return Regex("""(?is)<(script|style)\b[^>]*>([\s\S]*?)</\1\s*>""")
            .findAll(source)
            .mapNotNull { match ->
                val content = match.groups[2] ?: return@mapNotNull null
                val language =
                        when (match.groupValues[1].lowercase()) {
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
            if ((char == '"' || char == '\'') &&
                            index > 0 &&
                            token.substring(0, index).trimEnd().endsWith("=")
            ) {
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
        val diagnostics =
                when (block.language) {
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
            val propMatch =
                    Regex("""^\s*(--[A-Za-z0-9_-]+|[A-Za-z-][A-Za-z0-9-]*)\s*:""").find(line)
            if (propMatch != null) {
                val prop = propMatch.groupValues[1].lowercase()
                val propertyStart =
                        blockStart +
                                lineOffset +
                                (propMatch.groups[1]?.range?.first ?: propMatch.range.first)
                if (prop in props) {
                    diagnostics +=
                            diagnosticAt(
                                    source,
                                    propertyStart,
                                    propertyStart + prop.length,
                                    "warning",
                                    "Duplicate property '$prop' in CSS rule."
                            )
                } else {
                    props += prop
                }
                if (!prop.startsWith("--") && !isVendorPrefixed(prop) && prop !in knownCssProperties
                ) {
                    diagnostics +=
                            diagnosticAt(
                                    source,
                                    propertyStart,
                                    propertyStart + prop.length,
                                    "warning",
                                    "Unknown or unsupported CSS property '$prop'."
                            )
                }
            }
            lineOffset += line.length + 1
        }
    }

    Regex("""#[0-9a-fA-F]+""").findAll(source).forEach { match ->
        val length = match.value.length
        if (length !in setOf(4, 5, 7, 9)) {
            diagnostics +=
                    diagnosticAt(
                            source,
                            match.range.first,
                            match.range.last + 1,
                            "error",
                            "Hex colors must be #RGB, #RGBA, #RRGGBB, or #RRGGBBAA."
                    )
        }
    }

    // Improved semicolon detection
    source.lines().fold(0) { offset, line ->
        val code = line.substringBefore("/*").substringBefore("//")
        val trimmed = code.trim()
        val lineStart = offset + line.indexOf(code).coerceAtLeast(0)

        val declarationWithoutColon = Regex("""^[A-Za-z-]+\s+[^:;{}]+;?$""").matches(trimmed)
        if (declarationWithoutColon &&
                        !trimmed.startsWith("@") &&
                        !trimmed.startsWith("from ") &&
                        !trimmed.startsWith("to ")
        ) {
            diagnostics +=
                    diagnosticAt(
                            source,
                            lineStart,
                            offset + line.length,
                            "error",
                            "CSS declarations need a colon between the property and value."
                    )
        }

        if (trimmed.contains(":") &&
                        !trimmed.endsWith(";") &&
                        !trimmed.endsWith("{") &&
                        !trimmed.endsWith("}") &&
                        !trimmed.startsWith("@")
        ) {
            diagnostics +=
                    diagnosticAt(
                            source,
                            lineStart,
                            offset + line.length,
                            "warning",
                            "CSS declarations usually end with a semicolon."
                    )
        }
        offset + line.length + 1
    }
    return diagnostics
}

private fun inspectJavaScriptSource(source: String): List<CodeDiagnostic> {
    val diagnostics = mutableListOf<CodeDiagnostic>()
    diagnostics += delimiterDiagnostics(source, allowLineComments = true)
    jsCommonIdentifierTypos.forEach { (typo, correction) ->
        Regex("""\b${Regex.escape(typo)}\b""").findAll(source).forEach { match ->
            diagnostics +=
                    diagnosticAt(
                            source,
                            match.range.first,
                            match.range.last + 1,
                            "warning",
                            "Did you mean '$correction'?"
                    )
        }
    }

    // Check for debugger and eval
    Regex("""\bdebugger\b""").findAll(source).forEach { match ->
        diagnostics +=
                diagnosticAt(
                        source,
                        match.range.first,
                        match.range.last + 1,
                        "warning",
                        "Remove 'debugger' statements before production."
                )
    }
    Regex("""\beval\s*\(""").findAll(source).forEach { match ->
        diagnostics +=
                diagnosticAt(
                        source,
                        match.range.first,
                        match.range.last + 4,
                        "error",
                        "Avoid using 'eval()'; it is a security risk."
                )
    }

    val declared = mutableMapOf<String, Int>()
    Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)""").findAll(source).forEach { match ->
        val name = match.groupValues[1]
        val previous = declared[name]
        if (previous != null) {
            diagnostics +=
                    diagnosticAt(
                            source,
                            match.range.first,
                            match.range.last + 1,
                            "error",
                            "Variable '$name' is already declared on line ${lineNumberAt(source, previous)}."
                    )
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
                diagnostics +=
                        diagnosticAt(
                                source,
                                match.range.first,
                                match.range.last + 1,
                                "warning",
                                "Selector calls need a non-empty selector."
                        )
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
                diagnostics +=
                        diagnosticAt(
                                source,
                                lineStart,
                                offset + line.length,
                                "error",
                                "${trimmed.substringBefore(" ")} needs a preceding block."
                        )
            }
        }
        invalidDeclaration.find(trimmed)?.let { match ->
            val start = lineStart + match.range.first
            diagnostics +=
                    diagnosticAt(
                            source,
                            start,
                            offset + line.length,
                            "error",
                            "Declarations need a variable name before '='."
                    )
        }
        keywordWithoutCondition.find(trimmed)?.let { match ->
            val keyword = match.groupValues[1]
            val start = lineStart + match.range.first
            diagnostics +=
                    diagnosticAt(
                            source,
                            start,
                            (start + keyword.length).coerceAtMost(offset + line.length),
                            "error",
                            "$keyword needs a condition."
                    )
        }
        Regex("""^(const|let|var)\s+([A-Za-z_$][\w$]*)\s*(?:=\s*)?(;)?$""").find(trimmed)?.let {
                match ->
            val keyword = match.groupValues[1]
            val hasEquals = trimmed.contains("=")
            val hasOnlyTerminator = match.groupValues[3].isNotBlank()
            if (keyword == "const" && !hasEquals) {
                diagnostics +=
                        diagnosticAt(
                                source,
                                lineStart,
                                offset + line.length,
                                "error",
                                "const declarations need an initializer."
                        )
            } else if (hasEquals && (hasOnlyTerminator || trimmed.endsWith("="))) {
                diagnostics +=
                        diagnosticAt(
                                source,
                                lineStart,
                                offset + line.length,
                                "error",
                                "Assignment is missing a value."
                        )
            }
        }
        Regex("""^function\s+[A-Za-z_$][\w$]*\s*\([^)]*\)\s*$""").find(trimmed)?.let {
            diagnostics +=
                    diagnosticAt(
                            source,
                            lineStart,
                            offset + line.length,
                            "error",
                            "Function declarations need a body block."
                    )
        }
        Regex("""^function\s*\(""").find(trimmed)?.let {
            diagnostics +=
                    diagnosticAt(
                            source,
                            lineStart,
                            offset + line.length,
                            "error",
                            "Function declarations need a name, or assign the function to a variable."
                    )
        }
        Regex("""\b(if|while|switch|catch)\s*\(\s*\)""").find(trimmed)?.let { match ->
            val start = lineStart + match.range.first
            diagnostics +=
                    diagnosticAt(
                            source,
                            start,
                            start + match.value.length,
                            "error",
                            "${match.groupValues[1]} needs a condition."
                    )
        }
        Regex("""\b(if|while)\s*\([^)]*[^=!<>]=[^=][^)]*\)""").find(trimmed)?.let { match ->
            val start = lineStart + match.range.first
            diagnostics +=
                    diagnosticAt(
                            source,
                            start,
                            start + match.value.length,
                            "warning",
                            "Use '===' for comparison; '=' assigns a value."
                    )
        }
        if (looksLikeJavaScriptStatement(trimmed) && !hasJavaScriptStatementTerminator(trimmed)) {
            diagnostics +=
                    diagnosticAt(
                            source,
                            lineStart,
                            offset + line.length,
                            "hint",
                            "JavaScript statements usually end with a semicolon."
                    )
        }
        val trailingDot = line.lastIndexOf('.')
        if (trimmed.endsWith(".") && line.getOrNull(trailingDot - 1)?.isDigit() != true) {
            diagnostics +=
                    diagnosticAt(
                            source,
                            offset + trailingDot,
                            offset + line.length,
                            "error",
                            "Property access needs a member name after '.'."
                    )
        }
        if (trimmed.matches(Regex("""^(if|for|while|switch|catch)\s*$"""))) {
            diagnostics +=
                    diagnosticAt(
                            source,
                            lineStart,
                            offset + line.length,
                            "error",
                            "$trimmed needs a condition in parentheses."
                    )
        }
        offset + line.length + 1
    }
    return diagnostics
}

private fun inspectJsonSource(source: String): List<CodeDiagnostic> {
    return runCatching {
        JSONObject(source)
        emptyList<CodeDiagnostic>()
    }
            .getOrElse { error ->
                listOf(
                        diagnosticAt(
                                source,
                                0,
                                source.lineEndAt(0),
                                "error",
                                error.message ?: "Invalid JSON."
                        )
                )
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
                        diagnostics +=
                                diagnosticAt(
                                        source,
                                        quoteStart,
                                        index,
                                        "error",
                                        "String literal is missing a closing ${quote ?: '"'} quote."
                                )
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
                val expectedOpen =
                        when (char) {
                            ')' -> '('
                            '}' -> '{'
                            else -> '['
                        }
                if (stack.lastOrNull()?.first == expectedOpen) {
                    stack.removeAt(stack.lastIndex)
                } else {
                    diagnostics +=
                            diagnosticAt(source, index, index + 1, "error", "Unexpected '$char'.")
                }
                index += 1
            }
            else -> index += 1
        }
    }
    quote?.let {
        diagnostics +=
                diagnosticAt(
                        source,
                        quoteStart,
                        source.length,
                        "error",
                        "String literal is missing a closing $it quote."
                )
    }
    if (blockComment) {
        diagnostics +=
                diagnosticAt(
                        source,
                        blockCommentStart,
                        source.length,
                        "error",
                        "Block comment is missing */."
                )
    }
    stack.asReversed().forEach { (open, start) ->
        val close =
                when (open) {
                    '(' -> ')'
                    '{' -> '}'
                    else -> ']'
                }
        diagnostics += diagnosticAt(source, start, start + 1, "error", "Missing closing '$close'.")
    }
    return diagnostics
}

private fun diagnosticAt(
        source: String,
        start: Int,
        end: Int,
        severity: String,
        message: String
): CodeDiagnostic {
    val safeStart = start.coerceIn(0, source.length)
    val safeEnd = end.coerceIn(safeStart, source.length)
    val line = lineNumberAt(source, safeStart)
    val lineStart =
            source.lastIndexOf('\n', (safeStart - 1).coerceAtLeast(0)).let {
                if (it < 0) 0 else it + 1
            }
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
        val tag =
                Regex("""^<\s*/?\s*([A-Za-z][A-Za-z0-9-]*)""")
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
    // Avoid re-parsing an entire large document after every keystroke. This bounded
    // look-behind still captures the local string/comment state in ordinary code.
    var index = (cursor - COMMENT_STATE_SCAN_CHAR_LIMIT).coerceAtLeast(0)
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
            char == '"' ||
                    char == '\'' ||
                    (language == WebEditorLanguage.JavaScript && char == '`') -> {
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

private val htmlVoidTags =
        setOf(
                "area",
                "base",
                "br",
                "col",
                "embed",
                "hr",
                "img",
                "input",
                "link",
                "meta",
                "param",
                "source",
                "track",
                "wbr",
        )

private val knownCssProperties =
        setOf(
                "align-content",
                "align-items",
                "align-self",
                "animation",
                "animation-delay",
                "animation-duration",
                "animation-fill-mode",
                "animation-name",
                "animation-timing-function",
                "appearance",
                "aspect-ratio",
                "backdrop-filter",
                "background",
                "background-attachment",
                "background-blend-mode",
                "background-clip",
                "background-color",
                "background-image",
                "background-position",
                "background-repeat",
                "background-size",
                "border",
                "border-bottom",
                "border-bottom-color",
                "border-bottom-left-radius",
                "border-bottom-right-radius",
                "border-bottom-style",
                "border-bottom-width",
                "border-color",
                "border-image",
                "border-left",
                "border-left-color",
                "border-left-style",
                "border-left-width",
                "border-radius",
                "border-right",
                "border-right-color",
                "border-right-style",
                "border-right-width",
                "border-style",
                "border-top",
                "border-top-color",
                "border-top-left-radius",
                "border-top-right-radius",
                "border-top-style",
                "border-top-width",
                "border-width",
                "bottom",
                "box-shadow",
                "box-sizing",
                "clip-path",
                "color",
                "column-gap",
                "container",
                "container-name",
                "container-type",
                "content",
                "cursor",
                "display",
                "filter",
                "flex",
                "flex-basis",
                "flex-direction",
                "flex-grow",
                "flex-shrink",
                "flex-wrap",
                "font",
                "font-family",
                "font-feature-settings",
                "font-size",
                "font-style",
                "font-variant",
                "font-weight",
                "gap",
                "grid",
                "grid-area",
                "grid-column",
                "grid-row",
                "grid-template-columns",
                "grid-template-rows",
                "height",
                "inset",
                "justify-content",
                "left",
                "letter-spacing",
                "line-height",
                "list-style",
                "list-style-image",
                "list-style-position",
                "list-style-type",
                "margin",
                "margin-bottom",
                "margin-left",
                "margin-right",
                "margin-top",
                "max-height",
                "max-width",
                "min-height",
                "min-width",
                "mix-blend-mode",
                "object-fit",
                "object-position",
                "opacity",
                "order",
                "outline",
                "outline-color",
                "outline-offset",
                "outline-style",
                "outline-width",
                "overflow",
                "overflow-x",
                "overflow-y",
                "padding",
                "padding-bottom",
                "padding-left",
                "padding-right",
                "padding-top",
                "place-items",
                "pointer-events",
                "position",
                "right",
                "row-gap",
                "scroll-behavior",
                "scroll-snap-align",
                "scroll-snap-type",
                "text-align",
                "text-fill-color",
                "text-decoration",
                "text-overflow",
                "text-shadow",
                "text-transform",
                "top",
                "touch-action",
                "transform",
                "transform-origin",
                "transform-style",
                "transition",
                "transition-delay",
                "transition-duration",
                "transition-property",
                "transition-timing-function",
                "translate",
                "user-select",
                "visibility",
                "white-space",
                "width",
                "z-index",
        )

private fun isVendorPrefixed(prop: String): Boolean {
    return prop.startsWith("-webkit-") ||
            prop.startsWith("-moz-") ||
            prop.startsWith("-ms-") ||
            prop.startsWith("-o-")
}

private val jsCommonIdentifierTypos =
        mapOf(
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
    if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*"))
            return false
    if (trimmed.startsWith("if ") || trimmed.startsWith("if(")) return false
    if (trimmed.startsWith("for ") || trimmed.startsWith("for(")) return false
    if (trimmed.startsWith("while ") || trimmed.startsWith("while(")) return false
    if (trimmed.startsWith("switch ") || trimmed.startsWith("switch(")) return false
    if (trimmed.startsWith("function ")) return false
    if (trimmed.startsWith("else") || trimmed.startsWith("catch") || trimmed.startsWith("finally"))
            return false
    return Regex("""^(const|let|var|return|throw|await|break|continue)\b""")
            .containsMatchIn(trimmed) ||
            Regex(
                            """^[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*|\[[^\]]+])*\s*(?:=|\+=|-=|\*=|/=|\+\+|--|\()"""
                    )
                    .containsMatchIn(trimmed)
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

private val webCompletionSyntaxes =
        mapOf(
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

internal fun webIdePalette(theme: WebTheme): WebPalette =
        when (theme) {
            WebTheme.CyanOrbitDark, WebTheme.CyanOrbitLight,
            WebTheme.BlueHorizonDark, WebTheme.BlueHorizonLight,
            WebTheme.GreenCometDark, WebTheme.GreenCometLight -> {
                val colors = requireNotNull(spaceThemeColors(theme.name))
                if (colors.isDark) darkWebPalette(
                    bgTop = colors.background, bgBottom = colors.gradientEnd, panel = colors.panel,
                    accent = colors.accent, run = colors.run, secondary = colors.secondary,
                    terminalPrompt = colors.secondary, border = colors.border,
                ).copy(elevatedPanel = colors.elevated).withReadableContrast()
                else lightWebPalette(
                    bgTop = colors.background, bgBottom = colors.gradientEnd, panel = colors.panel,
                    elevatedPanel = colors.elevated, accent = colors.accent, run = colors.run,
                    secondary = colors.secondary, terminalPrompt = colors.secondary, border = colors.border,
                )
            }
            WebTheme.CyberpunkElectricDark ->
                    darkWebPalette(
                            bgTop = Color(0xFF070617),
                            bgBottom = Color(0xFF1A1033),
                            panel = Color(0xFF0A0C24),
                            accent = Color(0xFFFF2D75),
                            secondary = Color(0xFF00E5FF),
                            warningColor = Color(0xFFFF3EF5),
                    )
            WebTheme.CyberpunkElectricLight ->
                    lightWebPalette(
                            bgTop = Color(0xFFFFFBF8),
                            bgBottom = Color(0xFFFFE7F0),
                            panel = Color.White,
                            elevatedPanel = Color(0xFFFFF7FB),
                            border = Color(0xFFFFC4DC),
                            accent = Color(0xFFFF2D75),
                            secondary = Color(0xFF00BBFF),
                    )
            WebTheme.ForestNightDark ->
                    darkWebPalette(
                            bgTop = Color(0xFF031611),
                            bgBottom = Color(0xFF0E1812),
                            panel = Color(0xFF042018),
                            accent = Color(0xFF22C55E),
                            secondary = Color(0xFFA3E635),
                            warningColor = Color(0xFFA3E635),
                    )
            WebTheme.ForestNightLight ->
                    lightWebPalette(
                            bgTop = Color(0xFFF7FFF9),
                            bgBottom = Color(0xFFECFDF5),
                            panel = Color(0xFFFFFFFF),
                            elevatedPanel = Color(0xFFF2FFF7),
                            border = Color(0xFFD1FAE5),
                            accent = Color(0xFF22C55E),
                            secondary = Color(0xFF16A34A),
                    )
            WebTheme.SunsetDuskDark ->
                    darkWebPalette(
                            bgTop = Color(0xFF1E0F0B),
                            bgBottom = Color(0xFF27120E),
                            panel = Color(0xFF180F18),
                            accent = Color(0xFFFB923C),
                            secondary = Color(0xFFFDBF24),
                            warningColor = Color(0xFFFDBF24),
                    )
            WebTheme.SunsetDuskLight ->
                    lightWebPalette(
                            bgTop = Color(0xFFFFFDF9),
                            bgBottom = Color(0xFFFFF0D0),
                            panel = Color.White,
                            elevatedPanel = Color(0xFFFFF7ED),
                            border = Color(0xFFFED7AA),
                            accent = Color(0xFFFB923C),
                            secondary = Color(0xFFF97316),
                    )
            WebTheme.ArcticAuroraDark ->
                    darkWebPalette(
                            bgTop = Color(0xFF031A24),
                            bgBottom = Color(0xFF0A1A1D),
                            panel = Color(0xFF052536),
                            accent = Color(0xFF06B6D4),
                            secondary = Color(0xFF60A5FA),
                            warningColor = Color(0xFF22D3EE),
                    )
            WebTheme.ArcticAuroraLight ->
                    lightWebPalette(
                            bgTop = Color(0xFFF7FEFF),
                            bgBottom = Color(0xFFF0FDFF),
                            panel = Color.White,
                            elevatedPanel = Color(0xFFEFFFFD),
                            border = Color(0xFFBAE6FD),
                            accent = Color(0xFF06B6D4),
                            secondary = Color(0xFF60A5FA),
                    )
            WebTheme.VioletHazeDark ->
                    darkWebPalette(
                            bgTop = Color(0xFF1A123A),
                            bgBottom = Color(0xFF120C2A),
                            panel = Color(0xFF201540),
                            accent = Color(0xFFA78BFA),
                            run = Color(0xFF7C3AED),
                            secondary = Color(0xFFF472B6),
                            warningColor = Color(0xFFC4B5FD),
                    )
            WebTheme.VioletHazeLight ->
                    lightWebPalette(
                            bgTop = Color(0xFFFAF5FF),
                            bgBottom = Color(0xFFEDE9FE),
                            panel = Color.White,
                            elevatedPanel = Color(0xFFF8F5FF),
                            border = Color(0xFFDDD6FE),
                            accent = Color(0xFFA78BFA),
                            secondary = Color(0xFFF472B6),
                    )
            WebTheme.MonochromeProDark ->
                    darkWebPalette(
                            bgTop = Color(0xFF111827),
                            bgBottom = Color(0xFF0B1118),
                            panel = Color(0xFF111827),
                            accent = Color(0xFF64748B),
                            run = Color(0xFF334155),
                            secondary = Color(0xFFA1A1AA),
                            warningColor = Color(0xFFE5E7EB),
                    )
            WebTheme.MonochromeProLight ->
                    lightWebPalette(
                            bgTop = Color(0xFFFFFFFF),
                            bgBottom = Color(0xFFF3F4F6),
                            panel = Color.White,
                            elevatedPanel = Color(0xFFF8FAFC),
                            border = Color(0xFFE5E7EB),
                            accent = Color(0xFF64748B),
                            run = Color(0xFF64748B),
                            secondary = Color(0xFF374151),
                    )
            WebTheme.VSCodeDefaultDark ->
                    darkWebPalette(
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
            WebTheme.VSCodeDefaultLight ->
                    lightWebPalette(
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

internal fun darkWebPalette(
        bgTop: Color,
        bgBottom: Color,
        panel: Color,
        accent: Color,
        run: Color = accent,
        secondary: Color = accent,
        warningColor: Color = Color(0xFFFFC46B),
        terminalPrompt: Color = accent,
        border: Color = accent.copy(alpha = 0.38f),
) =
        WebPalette(
                backgroundTop = bgTop,
                backgroundBottom = bgBottom,
                panel = panel,
                editorBackground = panel,
                terminalBackground = panel,
                // Surface colors must remain opaque: dialogs and menus otherwise reveal editor text
                // below.
                elevatedPanel = panel,
                tab = Color.White.copy(alpha = 0.06f).compositeOver(panel),
                selectedTab = accent.copy(alpha = 0.24f).compositeOver(panel),
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
        ).withReadableContrast()

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
) =
        WebPalette(
                backgroundTop = bgTop,
                backgroundBottom = bgBottom,
                panel = panel,
                editorBackground = panel,
                terminalBackground = panel,
                elevatedPanel = elevatedPanel,
                tab = accent.copy(alpha = 0.07f).compositeOver(panel),
                selectedTab = accent.copy(alpha = 0.13f).compositeOver(panel),
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
        ).withReadableContrast()

internal fun executeWebShellCommand(
        command: String,
        root: File,
        working: File,
        browserPreview: Boolean = false,
): WebShellResult {
    val args = splitShellCommandLine(command)
    val executable = args.firstOrNull()?.lowercase().orEmpty()
    return when (executable) {
        "" -> WebShellResult()
        "clear", "cls" -> WebShellResult(clearTerminal = true)
        "help" ->
                WebShellResult(
                        output =
                                """
                PocketCoded Web terminal

                Files: ls, tree, pwd, cd <dir>, cat <file>, touch <file>, mkdir <dir>, rm <path>, mv <from> <to>, cp <from> <to>
                Git: native Git ${NativeGitRuntime.VERSION} porcelain and plumbing; use git --help for commands
                SFTP: sftp help (secure uploads, downloads, directory transfers, and remote file management)
                Web: preview [http://host:port], run, serve, lint, validate, format <file>
                Node: node --version, node status, node logs, node stop, node <file> [args...], node -e <code>
                Next.js: npx create-next-app <name>, next dev, next build, next start
                Packages: npm/pnpm/yarn install <pkg>, npx <tool>, pnpm dlx <tool>, yarn dlx <tool>
                LSP: offline by default; lsp status, lsp stdio <server> [args...], lsp tcp <host> <port>, lsp stop
                Semantics: index, symbols [query], refs <symbol>, type <symbol>, modules, framework, refactor rename <old> <new> [--apply]
                Libraries: libs, libraries, lib <name>
                Versions: web --version, html --version, css --version, js --version, lsp --version, npm/pnpm/yarn/npx --version, git --version
                Utilities: echo <text>, clear, cls, help
            """.trimIndent()
                )
        "libs", "libraries" -> WebShellResult(output = webLibraryTerminalHelp())
        "lib", "library", "cdn" -> WebShellResult(output = webLibrarySnippet(args.getOrNull(1)))
        "version" ->
                WebShellResult(
                        output = "PocketCoded Web 1.0\nHTML, CSS and JavaScript preview engine"
                )
        "web", "html", "css", "js", "javascript" -> {
            if (args.any { it == "--version" || it == "-v" }) {
                val label =
                        when (executable) {
                            "html" -> "HTML5"
                            "css" -> "CSS3"
                            "js", "javascript" -> "JavaScript ES2023"
                            else -> "PocketCoded Web"
                        }
                WebShellResult(output = "$label (PocketCoded Web built-in preview engine)")
            } else {
                WebShellResult(
                        error =
                                "$executable: use $executable --version, run, preview, lint, or format <file>"
                )
            }
        }
        "git" -> {
            executeGitCommand(args, root, working)
        }
        "sftp" -> executeBuiltInSftpCommand(args.drop(1), root, working)
        "curl" -> executeCurlCommand(args, root, working)
        "vim", "vi", "nano" -> openTerminalEditor(args, root, working, executable)
        "sudo", "apt", "apt-get" ->
                WebShellResult(
                        error =
                                "$executable is an Android/Linux system command and is not available inside this project sandbox. Use help for built-in Web commands, or use Termux for a full Linux shell."
                )
        "index", "symbols", "refs", "type", "modules", "framework", "refactor" ->
                executeSemanticCommand(args, root)
        "pwd" -> WebShellResult(output = terminalDirectoryLabel(root, working))
        "ls", "dir" ->
                WebShellResult(
                        output =
                                listDirectory(
                                        working,
                                        showHidden =
                                                args.any { it.contains("a") && it.startsWith("-") }
                                )
                )
        "tree" -> WebShellResult(output = treeDirectory(root, working))
        "cd" -> {
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(1) ?: root.absolutePath)
                            ?: return WebShellResult(error = "cd: outside project")
            if (!target.isDirectory)
                    WebShellResult(error = "cd: ${args.getOrNull(1).orEmpty()}: no such directory")
            else WebShellResult(workingDirectoryPath = target.absolutePath)
        }
        "cat", "type" -> {
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                            ?: return WebShellResult(error = "cat: outside project")
            if (!target.isFile) WebShellResult(error = "cat: ${target.name}: no such file")
            else WebShellResult(output = target.readText().take(80_000))
        }
        "echo" -> WebShellResult(output = args.drop(1).joinToString(" "))
        "touch" -> {
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                            ?: return WebShellResult(error = "touch: outside project")
            target.parentFile?.mkdirs()
            if (!target.exists()) target.writeText("")
            target.setLastModified(System.currentTimeMillis())
            WebShellResult(
                    output = "Created ${relativeWorkspacePath(root, target)}",
                    workspaceChanged = true
            )
        }
        "mkdir" -> {
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                            ?: return WebShellResult(error = "mkdir: outside project")
            if (target.mkdirs() || target.isDirectory)
                    WebShellResult(
                            output = "Created ${relativeWorkspacePath(root, target)}/",
                            workspaceChanged = true
                    )
            else WebShellResult(error = "mkdir: could not create ${target.name}")
        }
        "rm", "del" -> {
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                            ?: return WebShellResult(error = "rm: outside project")
            if (samePath(target.absolutePath, root.absolutePath))
                    return WebShellResult(error = "rm: refusing to remove project root")
            val removed = if (target.isDirectory) target.deleteRecursively() else target.delete()
            if (removed)
                    WebShellResult(
                            output = "Removed ${relativeWorkspacePath(root, target)}",
                            workspaceChanged = true
                    )
            else WebShellResult(error = "rm: could not remove ${target.name}")
        }
        "mv", "move", "ren", "rename" -> {
            val source =
                    resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                            ?: return WebShellResult(error = "mv: source outside project")
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(2).orEmpty())
                            ?: return WebShellResult(error = "mv: destination outside project")
            if (!source.exists()) return WebShellResult(error = "mv: ${source.name}: no such file")
            target.parentFile?.mkdirs()
            if (source.renameTo(target))
                    WebShellResult(
                            output =
                                    "Moved ${source.name} -> ${relativeWorkspacePath(root, target)}",
                            workspaceChanged = true
                    )
            else WebShellResult(error = "mv: could not move ${source.name}")
        }
        "cp", "copy" -> {
            val source =
                    resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                            ?: return WebShellResult(error = "cp: source outside project")
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(2).orEmpty())
                            ?: return WebShellResult(error = "cp: destination outside project")
            if (!source.isFile) return WebShellResult(error = "cp: ${source.name}: no such file")
            target.parentFile?.mkdirs()
            source.copyTo(target, overwrite = true)
            WebShellResult(
                    output = "Copied ${source.name} -> ${relativeWorkspacePath(root, target)}",
                    workspaceChanged = true
            )
        }
        "preview", "run", "open", "serve" -> {
            if (args.size > 1) {
                val url = normalizePreviewLink(args.drop(1).joinToString(" "))
                if (url == null)
                        WebShellResult(
                                error =
                                        "Use preview <http://host:port or https://host> to open a running server."
                        )
                else WebShellResult(output = "Opening $url", previewLink = url)
            } else
                    WebShellResult(
                            output = "Choose where to open the live preview.",
                            openPreview = true
                    )
        }
        "lint", "check", "validate" -> WebShellResult(output = auditWebProject(root))
        "format" -> {
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                            ?: return WebShellResult(error = "format: outside project")
            if (!target.isFile || !isEditableWebFile(target))
                    return WebShellResult(error = "format: expected an editable Web file")
            val formatted = WebLanguageServices.format(target, target.name, target.readText())
            target.writeText(formatted)
            WebShellResult(
                    output = "Formatted ${relativeWorkspacePath(root, target)}",
                    workspaceChanged = true
            )
        }
        "node", "js" -> {
            if (args.getOrNull(1) == "stop") return NodeSessionRuntime.stop()
            if (args.getOrNull(1) == "logs") return NodeSessionRuntime.status()
            if (args.any { it == "--version" || it == "-v" }) {
                return WebShellResult(output = NativeNodeRuntime.status())
            }
            if (args.getOrNull(1)?.lowercase() == "status") {
                return NodeSessionRuntime.status()
            }
            if (args.getOrNull(1) == "-e") {
                val code = args.drop(2).joinToString(" ")
                return if (code.isBlank()) {
                    WebShellResult(error = "$executable -e: expected JavaScript code")
                } else {
                    nativeNodeResult { NativeNodeRuntime.evaluate(root, working, code) }
                }
            }
            val target =
                    resolveTerminalPath(root, working, args.getOrNull(1).orEmpty())
                            ?: return WebShellResult(error = "$executable: outside project")
            if (!target.isFile || fileExtension(target.name) !in javascriptFileExtensions) {
                return WebShellResult(
                        error =
                                "$executable: ${args.getOrNull(1).orEmpty()}: expected a JavaScript file"
                )
            }
            NodeSessionRuntime.start(root, working, target, args.drop(2))
        }
        "npm" -> executeNpmCommand(args, root, working, browserPreview)
        "next" -> runKnownPackageExecutable("next", args.drop(1), root, working, "next")
                        ?: WebShellResult(error = "next: command unavailable")
        "npx" -> executeNpxCommand(args, root, working)
        "pnpm" -> executePnpmCommand(args, root, working)
        "yarn" -> executeYarnCommand(args, root, working)
        "lsp" -> executeLspCommand(args, root)
        else ->
                WebShellResult(
                        error =
                                "Command not found: $executable. Run help to see available commands."
                )
    }
}

private inline fun nativeNodeResult(block: () -> NativeNodeResult): WebShellResult {
    return try {
        val result = block()
        val output = buildString {
            append(result.stdout.trimEnd())
            if (result.stderr.isNotBlank()) {
                if (isNotEmpty()) append('\n')
                append(result.stderr.trimEnd())
            }
        }
        if (result.succeeded) WebShellResult(output = output)
        else
                WebShellResult(
                        error =
                                output.ifBlank {
                                    "Native Node exited with code ${result.exitCode}."
                                }
                )
    } catch (error: Throwable) {
        WebShellResult(
                error = "Native Node failed safely: ${error.message ?: error.javaClass.simpleName}"
        )
    }
}

private fun executeSemanticCommand(args: List<String>, root: File): WebShellResult {
    return try {
        val snapshot = ProjectSemanticIndexer.build(root)
        val types by lazy(LazyThreadSafetyMode.NONE) { TypeScriptProjectAnalyzer.analyze(snapshot) }
        when (args.firstOrNull()?.lowercase()) {
            "index" ->
                    WebShellResult(
                            output =
                                    buildString {
                                        append(
                                                "Project index: ${snapshot.indexedFiles} files, ${snapshot.indexedBytes} bytes"
                                        )
                                        if (snapshot.skippedFiles > 0)
                                                append(
                                                        ", ${snapshot.skippedFiles} skipped by safety limits"
                                                )
                                        append(
                                                "\nFrameworks: ${snapshot.frameworks.joinToString()}"
                                        )
                                        append(
                                                "\nSymbols: ${snapshot.occurrences.count { (_, values) -> values.any(SemanticOccurrence::declaration) }}"
                                        )
                                    },
                    )
            "symbols" -> {
                val query = args.getOrNull(1)
                val declarations =
                        snapshot.occurrences
                                .values
                                .flatten()
                                .filter(SemanticOccurrence::declaration)
                                .filter {
                                    query.isNullOrBlank() ||
                                            it.name.contains(query, ignoreCase = true)
                                }
                                .sortedWith(
                                        compareBy<SemanticOccurrence> { it.name.lowercase() }
                                                .thenBy { it.file.path }
                                                .thenBy { it.offset }
                                )
                                .take(250)
                WebShellResult(
                        output =
                                declarations
                                        .joinToString("\n") {
                                            "${it.name}\t${relativeWorkspacePath(root, it.file)}:${it.line}:${it.column}\t${it.kind}"
                                        }
                                        .ifBlank { "No matching project declarations." }
                )
            }
            "refs" -> {
                val name =
                        args.getOrNull(1)
                                ?: return WebShellResult(error = "refs: expected a symbol name")
                val occurrences = snapshot.occurrences[name].orEmpty()
                WebShellResult(
                        output =
                                occurrences
                                        .joinToString("\n") {
                                            val role =
                                                    if (it.declaration) "declaration"
                                                    else "reference"
                                            "$role\t${relativeWorkspacePath(root, it.file)}:${it.line}:${it.column}"
                                        }
                                        .ifBlank { "No project symbol named '$name'." }
                )
            }
            "type" -> {
                val name =
                        args.getOrNull(1)
                                ?: return WebShellResult(error = "type: expected a symbol name")
                val facts = types.types[name].orEmpty()
                WebShellResult(
                        output =
                                facts
                                        .joinToString("\n") {
                                            "${it.name}: ${it.type} (${it.confidence})\t${relativeWorkspacePath(root, it.file)}:${it.line}"
                                        }
                                        .ifBlank {
                                            "No inferred or declared TypeScript type for '$name'. Connect tsserver through LSP for compiler-level inference."
                                        }
                )
            }
            "modules" ->
                    WebShellResult(
                            output =
                                    types.modules
                                            .joinToString("\n") { dependency ->
                                                val target =
                                                        dependency.resolvedFile?.let {
                                                            relativeWorkspacePath(root, it)
                                                        }
                                                                ?: "unresolved/external"
                                                "${relativeWorkspacePath(root, dependency.sourceFile)}\t${dependency.specifier}\t$target"
                                            }
                                            .ifBlank { "No ES/CommonJS module dependencies found." }
                    )
            "framework" ->
                    WebShellResult(
                            output =
                                    buildString {
                                                types.frameworkNotes.forEach {
                                                    append(it).append('\n')
                                                }
                                                if (types.scripts.isNotEmpty()) {
                                                    append("Scripts:\n")
                                                    types.scripts.forEach { (name, command) ->
                                                        append("  ")
                                                                .append(name)
                                                                .append(": ")
                                                                .append(command)
                                                                .append('\n')
                                                    }
                                                }
                                            }
                                            .trimEnd()
                    )
            "refactor" -> {
                if (args.getOrNull(1)?.lowercase() != "rename") {
                    return WebShellResult(error = "Usage: refactor rename <old> <new> [--apply]")
                }
                val oldName =
                        args.getOrNull(2)
                                ?: return WebShellResult(
                                        error = "refactor rename: missing old name"
                                )
                val newName =
                        args.getOrNull(3)
                                ?: return WebShellResult(
                                        error = "refactor rename: missing new name"
                                )
                val plan = ProjectSemanticIndexer.rename(snapshot, oldName, newName)
                if (!plan.valid) return WebShellResult(error = plan.error ?: "Rename is not safe.")
                if (args.drop(4).any { it == "--apply" }) {
                    val count =
                            ProjectSemanticIndexer.apply(plan).getOrElse { error ->
                                return WebShellResult(
                                        error =
                                                "Rename failed and was rolled back: ${error.message ?: "unknown error"}"
                                )
                            }
                    WebShellResult(
                            output = "Renamed $oldName to $newName at $count project locations.",
                            workspaceChanged = true
                    )
                } else {
                    WebShellResult(
                            output =
                                    buildString {
                                                append(
                                                        "Safe rename preview: ${plan.edits.size} edits\n"
                                                )
                                                plan.edits.take(250).forEach { edit ->
                                                    append(relativeWorkspacePath(root, edit.file))
                                                            .append(':')
                                                            .append(
                                                                    snapshot.occurrences[oldName]
                                                                            .orEmpty()
                                                                            .firstOrNull {
                                                                                it.file ==
                                                                                        edit.file &&
                                                                                        it.offset ==
                                                                                                edit.offset
                                                                            }
                                                                            ?.line
                                                                            ?: 0
                                                            )
                                                            .append("  ")
                                                            .append(oldName)
                                                            .append(" -> ")
                                                            .append(newName)
                                                            .append('\n')
                                                }
                                                append(
                                                        "Run 'refactor rename $oldName $newName --apply' to write these changes."
                                                )
                                            }
                                            .trimEnd()
                    )
                }
            }
            else -> WebShellResult(error = "Unknown semantic command.")
        }
    } catch (error: Throwable) {
        WebShellResult(
                error =
                        "Project analysis failed safely: ${error.message ?: error.javaClass.simpleName}"
        )
    }
}

private fun generalCodeCompletions(fileName: String, source: String): List<CompletionItem> {
    val extension = fileExtension(fileName)
    val keywords =
            when (extension) {
                "kt", "kts" ->
                        listOf(
                                "class",
                                "data class",
                                "object",
                                "interface",
                                "fun",
                                "val",
                                "var",
                                "when",
                                "if",
                                "else",
                                "for",
                                "while",
                                "return",
                                "import",
                                "package",
                                "private",
                                "public",
                                "internal",
                                "suspend",
                                "override",
                                "null",
                                "true",
                                "false"
                        )
                "java" ->
                        listOf(
                                "class",
                                "interface",
                                "enum",
                                "record",
                                "public",
                                "private",
                                "protected",
                                "static",
                                "final",
                                "void",
                                "new",
                                "return",
                                "if",
                                "else",
                                "for",
                                "while",
                                "try",
                                "catch",
                                "import",
                                "package",
                                "null",
                                "true",
                                "false"
                        )
                "py", "pyw" ->
                        listOf(
                                "def",
                                "class",
                                "import",
                                "from",
                                "as",
                                "if",
                                "elif",
                                "else",
                                "for",
                                "while",
                                "return",
                                "yield",
                                "async",
                                "await",
                                "try",
                                "except",
                                "with",
                                "lambda",
                                "None",
                                "True",
                                "False",
                                "print()"
                        )
                "c", "h", "cc", "cpp", "cxx", "hpp" ->
                        listOf(
                                "class",
                                "struct",
                                "enum",
                                "namespace",
                                "template",
                                "public",
                                "private",
                                "protected",
                                "const",
                                "static",
                                "void",
                                "int",
                                "auto",
                                "new",
                                "delete",
                                "return",
                                "if",
                                "else",
                                "for",
                                "while",
                                "nullptr",
                                "true",
                                "false",
                                "#include"
                        )
                "cs" ->
                        listOf(
                                "class",
                                "struct",
                                "interface",
                                "enum",
                                "namespace",
                                "using",
                                "public",
                                "private",
                                "protected",
                                "internal",
                                "static",
                                "readonly",
                                "async",
                                "await",
                                "var",
                                "new",
                                "return",
                                "if",
                                "else",
                                "foreach",
                                "while",
                                "null",
                                "true",
                                "false"
                        )
                "go" ->
                        listOf(
                                "package",
                                "import",
                                "func",
                                "type",
                                "struct",
                                "interface",
                                "var",
                                "const",
                                "go",
                                "defer",
                                "select",
                                "chan",
                                "map",
                                "range",
                                "return",
                                "if",
                                "else",
                                "for",
                                "nil",
                                "true",
                                "false"
                        )
                "rs" ->
                        listOf(
                                "fn",
                                "struct",
                                "enum",
                                "trait",
                                "impl",
                                "mod",
                                "use",
                                "pub",
                                "let",
                                "mut",
                                "const",
                                "async",
                                "await",
                                "match",
                                "if",
                                "else",
                                "for",
                                "while",
                                "loop",
                                "return",
                                "self",
                                "Some",
                                "None",
                                "Ok",
                                "Err"
                        )
                "php", "phtml" ->
                        listOf(
                                "class",
                                "interface",
                                "trait",
                                "function",
                                "public",
                                "private",
                                "protected",
                                "static",
                                "namespace",
                                "use",
                                "new",
                                "return",
                                "if",
                                "else",
                                "foreach",
                                "while",
                                "null",
                                "true",
                                "false",
                                "echo"
                        )
                "rb" ->
                        listOf(
                                "class",
                                "module",
                                "def",
                                "end",
                                "require",
                                "include",
                                "attr_reader",
                                "if",
                                "elsif",
                                "else",
                                "unless",
                                "each",
                                "do",
                                "yield",
                                "return",
                                "nil",
                                "true",
                                "false",
                                "puts"
                        )
                "swift" ->
                        listOf(
                                "class",
                                "struct",
                                "enum",
                                "protocol",
                                "extension",
                                "func",
                                "let",
                                "var",
                                "guard",
                                "if",
                                "else",
                                "switch",
                                "for",
                                "while",
                                "return",
                                "import",
                                "async",
                                "await",
                                "nil",
                                "true",
                                "false"
                        )
                "dart" ->
                        listOf(
                                "class",
                                "enum",
                                "extension",
                                "mixin",
                                "void",
                                "final",
                                "const",
                                "var",
                                "late",
                                "async",
                                "await",
                                "Future",
                                "import",
                                "return",
                                "if",
                                "else",
                                "for",
                                "while",
                                "null",
                                "true",
                                "false"
                        )
                "sh", "bash", "zsh" ->
                        listOf(
                                "if",
                                "then",
                                "elif",
                                "else",
                                "fi",
                                "for",
                                "while",
                                "do",
                                "done",
                                "case",
                                "esac",
                                "function",
                                "local",
                                "export",
                                "source",
                                "echo",
                                "printf"
                        )
                "ps1" ->
                        listOf(
                                "function",
                                "param",
                                "if",
                                "elseif",
                                "else",
                                "foreach",
                                "while",
                                "switch",
                                "try",
                                "catch",
                                "finally",
                                "return",
                                "Import-Module",
                                "Write-Output",
                                "Get-ChildItem"
                        )
                "sql" -> sqlKeywords.sorted().map { "$it " }
                "yaml", "yml" ->
                        listOf(
                                "name:",
                                "version:",
                                "services:",
                                "image:",
                                "build:",
                                "environment:",
                                "dependencies:",
                                "scripts:"
                        )
                "xml" -> listOf("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "<!--  -->")
                "md" -> listOf("# ", "## ", "### ", "- ", "1. ", "[text](url)", "```")
                else -> emptyList()
            }.map {
                CompletionItem(
                        it.trimEnd(),
                        it,
                        if (extension == "sql") "SQLite keyword" else "keyword"
                )
            }

    val identifiers =
            Regex("""[A-Za-z_][A-Za-z0-9_]{2,}""")
                    .findAll(
                            if (source.length > COMPLETION_SCAN_CHAR_LIMIT)
                                    source.takeLast(COMPLETION_SCAN_CHAR_LIMIT)
                            else source
                    )
                    .map { it.value }
                    .filterNot { value ->
                        keywords.any { it.label.equals(value, ignoreCase = true) }
                    }
                    .distinct()
                    .take(80)
                    .map { CompletionItem(it, it, "symbol") }
                    .toList()
    return keywords + identifiers
}

private fun executeLspCommand(args: List<String>, root: File): WebShellResult {
    val subcommand = args.getOrNull(1)?.lowercase().orEmpty()
    return when (subcommand) {
        "", "help", "-h", "--help" -> WebShellResult(output = lspTerminalHelp())
        "version", "--version", "-v" ->
                WebShellResult(
                        output =
                                "Offline Web LSP + Language Server Protocol 3.x external client (PocketCoded Web)"
                )
        "status" -> WebShellResult(output = WebLanguageServices.status())
        "stop", "shutdown", "exit" -> WebShellResult(output = ExternalLspManager.stop())
        "stdio", "start" -> {
            val command = args.drop(2)
            if (command.isEmpty()) {
                WebShellResult(error = "lsp $subcommand: expected a language-server command.")
            } else {
                WebShellResult(output = ExternalLspManager.startStdio(command, root))
            }
        }
        "tcp", "connect" -> {
            val host = args.getOrNull(2).orEmpty()
            val port = args.getOrNull(3)?.toIntOrNull()
            when {
                host.isBlank() -> WebShellResult(error = "lsp $subcommand: expected a host.")
                port == null -> WebShellResult(error = "lsp $subcommand: expected a numeric port.")
                else -> WebShellResult(output = ExternalLspManager.connectTcp(host, port, root))
            }
        }
        else -> WebShellResult(error = "lsp: unsupported command '$subcommand'. Run lsp help.")
    }
}

private fun lspTerminalHelp(): String {
    return """
        Offline LSP

        lsp status
        lsp stop
        lsp stdio <server-command> [args...]
        lsp tcp <host> <port>

        Examples:
        lsp stdio typescript-language-server --stdio
        lsp stdio vscode-html-language-server --stdio
        lsp tcp 192.168.1.20 2087

        The editor always has an offline LSP for HTML, CSS, JavaScript, TypeScript, JSON, Markdown, and plain text. If an external server is connected, it can override completions, diagnostics, and formatting while it is active. Stdio requires the server executable to be available to this Android app process; TCP works with servers running in Termux or on another reachable machine.
    """.trimIndent()
}

private fun executeNpmCommand(
        args: List<String>,
        root: File,
        working: File,
        browserPreview: Boolean = false,
): WebShellResult {
    if (args.any { it == "--version" || it == "-v" }) {
        return WebShellResult(output = "npm 10.8.0-compatible (PocketCoded Web package manager)")
    }
    val subcommand = args.getOrNull(1)?.lowercase().orEmpty()
    return when (subcommand) {
        "", "help", "-h", "--help" -> WebShellResult(output = npmTerminalHelp())
        "init" -> createOrUpdatePackageJson(root, force = args.any { it == "-y" || it == "--yes" })
        "install", "i", "add" -> installNpmPackages(root, parseNpmInstallOptions(args.drop(2)))
        "ci" -> cleanInstallNpmPackages(root)
        "update" -> installNpmPackages(root, parseNpmInstallOptions(emptyList()))
        "uninstall", "remove", "rm", "un" -> uninstallNpmPackages(root, args.drop(2))
        "list", "ls", "la", "ll" -> listNpmPackages(root)
        "run", "run-script" -> runNpmScript(root, working, args.drop(2), browserPreview)
        "start", "test", "stop", "restart" -> runNpmScript(root, working, listOf(subcommand), browserPreview)
        "exec", "x" ->
                executePackageExecutableCommand(
                        args.drop(2),
                        root,
                        working,
                        installMissing = false,
                        runner = "npm exec"
                )
        "create" -> executeCreatePackageCommand(args.drop(2), root, working, runner = "npm create")
        "scripts" -> WebShellResult(output = npmScriptsText(root))
        "view", "info", "show" -> viewNpmPackage(args.drop(2))
        "audit" ->
                WebShellResult(
                        output =
                                "PocketCoded Web does not bundle npm audit. Review package sources before installing and keep dependencies minimal."
                )
        "cache" ->
                WebShellResult(
                        output = "Package metadata is cached only for the current install command."
                )
        else -> WebShellResult(error = "npm: unsupported command '$subcommand'. Run npm help.")
    }
}

private fun executeNpxCommand(args: List<String>, root: File, working: File): WebShellResult {
    if (args.any { it == "--version" || it == "-v" }) {
        return WebShellResult(output = "npx 10.8.0-compatible (PocketCoded Web package runner)")
    }
    val subcommand = args.getOrNull(1)?.lowercase().orEmpty()
    return when (subcommand) {
        "", "help", "-h", "--help" -> WebShellResult(output = packageRunnerHelp("npx"))
        else ->
                executePackageExecutableCommand(
                        args.drop(1),
                        root,
                        working,
                        installMissing = true,
                        runner = "npx"
                )
    }
}

private fun executePnpmCommand(args: List<String>, root: File, working: File): WebShellResult {
    if (args.any { it == "--version" || it == "-v" }) {
        return WebShellResult(output = "pnpm 9.15.0-compatible (PocketCoded Web package manager)")
    }
    val subcommand = args.getOrNull(1)?.lowercase().orEmpty()
    return when (subcommand) {
        "", "help", "-h", "--help" -> WebShellResult(output = packageManagerHelp("pnpm"))
        "install", "i", "add" ->
                packageManagerResult(
                        root,
                        "pnpm",
                        installNpmPackages(root, parseNpmInstallOptions(args.drop(2)))
                )
        "ci" -> packageManagerResult(root, "pnpm", cleanInstallNpmPackages(root))
        "update", "up", "upgrade" ->
                packageManagerResult(
                        root,
                        "pnpm",
                        installNpmPackages(root, parseNpmInstallOptions(args.drop(2)))
                )
        "uninstall", "remove", "rm", "un" ->
                packageManagerResult(root, "pnpm", uninstallNpmPackages(root, args.drop(2)))
        "list", "ls", "la", "ll" -> listNpmPackages(root)
        "run", "run-script" -> runNpmScript(root, working, args.drop(2))
        "exec" ->
                executePackageExecutableCommand(
                        args.drop(2),
                        root,
                        working,
                        installMissing = false,
                        runner = "pnpm exec"
                )
        "dlx" ->
                executePackageExecutableCommand(
                        args.drop(2),
                        root,
                        working,
                        installMissing = true,
                        runner = "pnpm dlx"
                )
        "create" -> executeCreatePackageCommand(args.drop(2), root, working, runner = "pnpm create")
        "dev", "start", "test", "build", "lint", "preview", "serve" ->
                runNpmScript(root, working, listOf(subcommand))
        else -> {
            when {
                hasPackageScript(root, subcommand) ->
                        runNpmScript(root, working, listOf(subcommand))
                else ->
                        WebShellResult(
                                error = "pnpm: unsupported command '$subcommand'. Run pnpm help."
                        )
            }
        }
    }
}

private fun executeYarnCommand(args: List<String>, root: File, working: File): WebShellResult {
    if (args.any { it == "--version" || it == "-v" }) {
        return WebShellResult(output = "yarn 4.5.0-compatible (PocketCoded Web package manager)")
    }
    val subcommand = args.getOrNull(1)?.lowercase().orEmpty()
    return when (subcommand) {
        "", "install" ->
                packageManagerResult(
                        root,
                        "yarn",
                        installNpmPackages(root, parseNpmInstallOptions(args.drop(2)))
                )
        "help", "-h", "--help" -> WebShellResult(output = packageManagerHelp("yarn"))
        "add" ->
                packageManagerResult(
                        root,
                        "yarn",
                        installNpmPackages(root, parseNpmInstallOptions(args.drop(2)))
                )
        "upgrade", "up", "update" ->
                packageManagerResult(
                        root,
                        "yarn",
                        installNpmPackages(root, parseNpmInstallOptions(args.drop(2)))
                )
        "remove", "uninstall" ->
                packageManagerResult(root, "yarn", uninstallNpmPackages(root, args.drop(2)))
        "list", "why" -> listNpmPackages(root)
        "run", "run-script" -> runNpmScript(root, working, args.drop(2))
        "exec" ->
                executePackageExecutableCommand(
                        args.drop(2),
                        root,
                        working,
                        installMissing = false,
                        runner = "yarn exec"
                )
        "dlx" ->
                executePackageExecutableCommand(
                        args.drop(2),
                        root,
                        working,
                        installMissing = true,
                        runner = "yarn dlx"
                )
        "create" -> executeCreatePackageCommand(args.drop(2), root, working, runner = "yarn create")
        "dev", "start", "test", "build", "lint", "preview", "serve" ->
                runNpmScript(root, working, listOf(subcommand))
        else -> {
            when {
                hasPackageScript(root, subcommand) ->
                        runNpmScript(root, working, listOf(subcommand))
                else ->
                        executePackageExecutableCommand(
                                args.drop(1),
                                root,
                                working,
                                installMissing = false,
                                runner = "yarn"
                        )
            }
        }
    }
}

private fun npmTerminalHelp(): String {
    return """
        PocketCoded Web npm support

        Project:
        - npm init -y
        - npm install
        - npm install react three @scope/package@1.2.3
        - npm install -D typescript
        - npm uninstall react
        - npm list
        - pnpm add three
        - yarn add lodash-es

        Scripts:
        - npm run
        - npm run start
        - npm start
        - npm test
        - npm run lint
        - pnpm dev
        - yarn build

        Package runners:
        - npx vite
        - npx prettier --write index.html
        - pnpm dlx eslint .
        - yarn dlx create-vite demo

        Notes:
        - npm, pnpm, yarn, and npx are built-in compatibility commands backed by PocketCoded Web's package manager.
        - Packages are downloaded from the npm registry into local node_modules.
        - Browser/ESM packages can be imported in preview with bare imports such as import * as THREE from "three".
        - Node scripts and installed JavaScript CLIs run in a managed session. Use node logs and node stop.
        - Run preview and open the detected server link to view backend responses.
        - Desktop native binaries and install-time postinstall scripts are not executed on Android.
        - npm ci is unavailable; existing lockfiles and installed packages are preserved when it is requested.
    """.trimIndent()
}

private fun packageManagerHelp(manager: String): String {
    val addCommand = if (manager == "yarn") "yarn add three" else "$manager add three"
    return """
        PocketCoded Web $manager support

        Project:
        - $manager install
        - $addCommand
        - $manager remove three
        - $manager list

        Scripts and tools:
        - $manager dev
        - $manager run build
        - $manager exec vite
        - $manager dlx prettier --write index.html
        - $manager create vite demo

        Notes:
        - $manager is implemented as a built-in compatibility layer over PocketCoded Web's local node_modules.
        - Browser/ESM packages work best in the live preview.
    """.trimIndent()
}

private fun packageRunnerHelp(runner: String): String {
    return """
        PocketCoded Web $runner support

        Examples:
        - $runner vite
        - $runner prettier --write index.html
        - $runner eslint .
        - $runner create-vite demo

        Notes:
        - Missing packages are downloaded from the npm registry before running.
        - Installed JavaScript CLIs run with Node; node logs shows output and node stop ends the session.
        - Native Node-only binaries are detected and reported when Android cannot execute them directly.
    """.trimIndent()
}

private fun packageManagerResult(
        root: File,
        manager: String,
        result: WebShellResult
): WebShellResult {
    if (!result.workspaceChanged) return result
    val lockMessage =
            runCatching { syncPackageManagerLockfile(root, manager) }.getOrNull().orEmpty()
    return result.copy(
            output =
                    listOf(result.output, lockMessage)
                            .filter { it.isNotBlank() }
                            .joinToString("\n"),
    )
}

private fun executePackageExecutableCommand(
        rawArgs: List<String>,
        root: File,
        working: File,
        installMissing: Boolean,
        runner: String,
): WebShellResult {
    val request =
            parsePackageExecutableRequest(rawArgs)
                    ?: return WebShellResult(
                            error = "$runner: expected a package or executable name"
                    )
    val packageSpec = request.packageSpec
    val installResult =
            if (installMissing &&
                            !request.noInstall &&
                            packageSpec != null &&
                            (!isBuiltInPackageExecutable(request.command) ||
                                    request.command in
                                            setOf(
                                                    "vite",
                                                    "webpack",
                                                    "webpack-cli",
                                                    "parcel",
                                                    "astro",
                                                    "next",
                                                    "nuxt",
                                                    "serve",
                                                    "http-server",
                                                    "live-server",
                                                    "browser-sync",
                                                    "snowpack",
                                            )) &&
                            !isNpmPackageInstalled(root, packageSpec.name)
            ) {
                installNpmPackages(
                        root,
                        NpmInstallOptions(packages = listOf(packageSpec), noSave = true)
                )
            } else {
                WebShellResult()
            }
    if (installResult.error.isNotBlank()) {
        return installResult.copy(error = "$runner: ${installResult.error}")
    }

    val runResult =
            runPackageExecutable(
                    command = request.command,
                    args = request.args,
                    root = root,
                    working = working,
                    runner = runner,
                    packageName = packageSpec?.name,
            )
    return runResult.copy(
            output =
                    listOf(
                                    "$runner ${listOf(request.command).plus(request.args).joinToString(" ")}",
                                    installResult.output,
                                    runResult.output,
                            )
                            .filter { it.isNotBlank() }
                            .joinToString("\n"),
            workspaceChanged = runResult.workspaceChanged || installResult.workspaceChanged,
    )
}

private fun executeCreatePackageCommand(
        rawArgs: List<String>,
        root: File,
        working: File,
        runner: String,
): WebShellResult {
    val createTarget =
            rawArgs.firstOrNull { !it.startsWith("-") }
                    ?: return WebShellResult(
                            error =
                                    "$runner: expected a template name, for example '$runner vite my-app'"
                    )
    val rest = rawArgs.dropWhile { it != createTarget }.drop(1)
    val packageToken = createPackageToken(createTarget)
    return executePackageExecutableCommand(
            rawArgs = listOf(packageToken) + rest,
            root = root,
            working = working,
            installMissing = true,
            runner = runner,
    )
}

private fun parsePackageExecutableRequest(rawArgs: List<String>): PackageExecutableRequest? {
    val packageSpecs = mutableListOf<NpmPackageSpec>()
    var noInstall = false
    var commandToken: String? = null
    var commandArgs = emptyList<String>()
    var index = 0
    while (index < rawArgs.size) {
        val arg = rawArgs[index]
        when {
            arg == "-y" || arg == "--yes" -> Unit
            arg == "--no-install" -> noInstall = true
            arg == "-p" || arg == "--package" -> {
                index += 1
                rawArgs.getOrNull(index)?.let(::parseNpmPackageSpec)?.let(packageSpecs::add)
            }
            arg.startsWith("--package=") -> {
                parseNpmPackageSpec(arg.substringAfter('='))?.let(packageSpecs::add)
            }
            arg == "--registry" || arg == "--cache" || arg == "--tag" || arg == "--userconfig" ->
                    index += 1
            arg.startsWith("--registry=") ||
                    arg.startsWith("--cache=") ||
                    arg.startsWith("--tag=") -> Unit
            arg.startsWith("-") -> Unit
            else -> {
                commandToken = arg
                commandArgs = rawArgs.drop(index + 1)
                break
            }
        }
        index += 1
    }

    val explicitPackage = packageSpecs.firstOrNull()
    val inferredPackage = commandToken?.let(::parseNpmPackageSpec)
    val packageSpec = explicitPackage ?: inferredPackage
    val command =
            when {
                commandToken != null -> inferredPackage?.name?.let(::packageBinaryName)
                                ?: commandToken.orEmpty()
                explicitPackage != null -> packageBinaryName(explicitPackage.name)
                else -> return null
            }
    return PackageExecutableRequest(
            command = normalizePackageExecutableName(command),
            packageSpec = packageSpec,
            args = commandArgs,
            noInstall = noInstall,
    )
}

private fun createPackageToken(rawTemplate: String): String {
    val parsed = parseNpmPackageSpec(rawTemplate)
    val name = parsed?.name ?: rawTemplate.trim().lowercase()
    val version = parsed?.requestedVersion?.takeIf { it.isNotBlank() && it != "latest" }
    val createName =
            when (name) {
                "vite" -> "create-vite"
                "react", "react-app" -> "create-react-app"
                "next", "next-app" -> "create-next-app"
                "astro" -> "create-astro"
                else ->
                        if (name.startsWith("create-") || name.contains("/create-")) name
                        else "create-$name"
            }
    return if (version == null) createName else "$createName@$version"
}

private fun runPackageExecutable(
        command: String,
        args: List<String>,
        root: File,
        working: File,
        runner: String,
        packageName: String?,
): WebShellResult {
    val localBinary = findLocalPackageBinary(root, command, packageName)
    return if (localBinary != null) {
        NodeSessionRuntime.start(root, working, localBinary, args)
    } else {
        runKnownPackageExecutable(command, args, root, working, runner)?.let {
            return it
        }
        WebShellResult(
                error =
                        "$runner: '$command' is not installed or not supported by the built-in mobile runner."
        )
    }
}

private fun isBuiltInPackageExecutable(command: String): Boolean {
    return normalizePackageExecutableName(command) in
            setOf(
                    "vite",
                    "webpack",
                    "webpack-cli",
                    "parcel",
                    "astro",
                    "next",
                    "nuxt",
                    "serve",
                    "http-server",
                    "live-server",
                    "browser-sync",
                    "snowpack",
                    "eslint",
                    "stylelint",
                    "htmlhint",
                    "jshint",
                    "biome",
                    "oxlint",
                    "prettier",
                    "tsc",
                    "typescript",
                    "vue-tsc",
                    "rollup",
                    "esbuild",
                    "babel",
                    "babel-cli",
                    "swc",
                    "tsup",
                    "create-vite",
                    "@vitejs/create-vite",
                    "create-react-app",
                    "create-next-app",
                    "create-astro",
                    "npm",
                    "pnpm",
                    "yarn",
            )
}

private fun runKnownPackageExecutable(
        command: String,
        args: List<String>,
        root: File,
        working: File,
        runner: String,
): WebShellResult? {
    val normalized = normalizePackageExecutableName(command)
    return when (normalized) {
        "vite",
        "webpack",
        "webpack-cli",
        "parcel",
        "astro",
        "next",
        "nuxt",
        "serve",
        "http-server",
        "live-server",
        "browser-sync",
        "snowpack" -> {
            val binary = findLocalPackageBinary(root, normalized, null)
            if (binary != null) NodeSessionRuntime.start(root, working, binary, args)
            else
                    WebShellResult(
                            error =
                                    "$normalized is not installed. Run npm install first. Use preview for the built-in frontend preview."
                    )
        }
        "eslint", "stylelint", "htmlhint", "jshint", "biome", "oxlint" -> {
            WebShellResult(output = auditWebProject(root))
        }
        "prettier" -> runPrettierCompatibleCommand(args, root, working)
        "tsc", "typescript", "vue-tsc" -> {
            if (args.any { it == "--version" || it == "-v" }) {
                WebShellResult(output = "TypeScript-compatible checks (PocketCoded Web built-in)")
            } else {
                WebShellResult(
                        output =
                                listOf(
                                                "TypeScript no-emit check handled by PocketCoded Web diagnostics.",
                                                auditWebProject(root),
                                        )
                                        .joinToString("\n")
                )
            }
        }
        "rollup", "esbuild", "babel", "babel-cli", "swc", "tsup" -> {
            WebShellResult(
                    output =
                            "$normalized build commands are mapped to the live preview pipeline on Android.",
                    openPreview = true,
            )
        }
        "create-vite",
        "@vitejs/create-vite",
        "create-react-app",
        "create-next-app",
        "create-astro" -> {
            scaffoldWebStarter(normalized, args, root, working, runner)
        }
        "npm" -> executeNpmCommand(listOf("npm") + args, root, working)
        "pnpm" -> executePnpmCommand(listOf("pnpm") + args, root, working)
        "yarn" -> executeYarnCommand(listOf("yarn") + args, root, working)
        else -> null
    }
}

private fun runPrettierCompatibleCommand(
        args: List<String>,
        root: File,
        working: File
): WebShellResult {
    if (args.any { it == "--version" || it == "-v" }) {
        return WebShellResult(output = "Prettier-compatible formatter (PocketCoded Web built-in)")
    }
    val write = args.any { it == "--write" || it == "-w" }
    val targets = editableTargetsFromToolArgs(args, root, working)
    if (!write) {
        return WebShellResult(
                output =
                        "Prettier check completed. Add --write to format ${if (targets.size == 1) targets.first().name else "${targets.size} files"}."
        )
    }
    if (targets.isEmpty()) return WebShellResult(output = "No editable Web files matched.")
    targets.forEach { target ->
        target.writeText(WebLanguageServices.format(target, target.name, target.readText()))
    }
    return WebShellResult(
            output = "Formatted ${targets.size} file${if (targets.size == 1) "" else "s"}.",
            workspaceChanged = true,
    )
}

private fun editableTargetsFromToolArgs(args: List<String>, root: File, working: File): List<File> {
    val pathTokens =
            args.filterNot { it.startsWith("-") }.filterNot { it == "." || it == "./" }.filterNot {
                it.contains("=")
            }
    val resolved =
            pathTokens.mapNotNull { token -> resolveTerminalPath(root, working, token) }.flatMap {
                    target ->
                when {
                    target.isFile && isEditableWebFile(target) -> listOf(target)
                    target.isDirectory -> editableFilesUnder(target)
                    else -> emptyList()
                }
            }
    return (if (resolved.isNotEmpty()) resolved else editableFilesUnder(root))
            .distinctBy { it.absolutePath }
            .take(200)
}

private fun editableFilesUnder(directory: File): List<File> {
    return directory
            .walkTopDown()
            .onEnter { child -> !isProjectIgnoredDirectory(child) }
            .filter { it.isFile && isEditableWebFile(it) }
            .toList()
}

private fun scaffoldWebStarter(
        command: String,
        args: List<String>,
        root: File,
        working: File,
        runner: String,
): WebShellResult {
    val targetName = args.firstOrNull { !it.startsWith("-") } ?: "."
    val targetDirectory =
            resolveTerminalPath(root, working, targetName)
                    ?: return WebShellResult(
                            error = "$runner: target directory must stay inside this project"
                    )
    if (targetDirectory.exists() && targetDirectory.listFiles().orEmpty().isNotEmpty()) {
        return WebShellResult(
                error = "$runner: ${relativeWorkspacePath(root, targetDirectory)} is not empty"
        )
    }
    targetDirectory.mkdirs()

    val projectName =
            if (targetName == "." || targetName == "./") root.name else targetDirectory.name
    if (command == "create-next-app") {
        return scaffoldNextStarter(targetDirectory, projectName, root)
    }
    val useReact =
            command == "create-react-app" || args.any { it.contains("react", ignoreCase = true) }
    File(targetDirectory, "package.json").writeText(scaffoldPackageJson(projectName, useReact))
    File(targetDirectory, "index.html").writeText(scaffoldIndexHtml(projectName, useReact))
    File(targetDirectory, "src").mkdirs()
    File(targetDirectory, "src/style.css").writeText(scaffoldCss())
    File(targetDirectory, "src/main.${if (useReact) "jsx" else "js"}")
            .writeText(scaffoldMainJs(useReact))

    return WebShellResult(
            output =
                    buildString {
                        append("Created ")
                                .append(relativeWorkspacePath(root, targetDirectory))
                                .append(" with a PocketCoded Web-compatible starter.")
                        if (useReact)
                                append(
                                        "\nRun npm install, pnpm install, or yarn install before previewing the React template."
                                )
                    },
            workspaceChanged = true,
    )
}

private fun scaffoldPackageJson(projectName: String, useReact: Boolean): String {
    val scripts =
            JSONObject()
                    .put("dev", "preview")
                    .put("start", "preview")
                    .put("build", "preview")
                    .put("lint", "lint")
    val json =
            JSONObject()
                    .put("name", sanitizeNpmPackageName(projectName))
                    .put("version", "1.0.0")
                    .put("private", true)
                    .put("type", "module")
                    .put("scripts", scripts)
    if (useReact) {
        json.put("dependencies", JSONObject().put("react", "^19.0.0").put("react-dom", "^19.0.0"))
    }
    return json.toString(2) + "\n"
}

private fun scaffoldIndexHtml(projectName: String, useReact: Boolean): String {
    val entryFile = if (useReact) "./src/main.jsx" else "./src/main.js"
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${escapeHtml(projectName)}</title>
            <link rel="stylesheet" href="./src/style.css">
        </head>
        <body>
            <main id="app"></main>
            <script type="module" src="$entryFile"></script>
        </body>
        </html>
    """.trimIndent() +
            "\n"
}

private fun scaffoldCss(): String {
    return """
        :root {
            color-scheme: light dark;
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
        }

        body {
            margin: 0;
            min-height: 100vh;
            display: grid;
            place-items: center;
            background: #f7f7fb;
            color: #151821;
        }

        #app {
            width: min(920px, calc(100vw - 32px));
        }
    """.trimIndent() +
            "\n"
}

private fun scaffoldMainJs(useReact: Boolean): String {
    return if (useReact) {
        """
            import React from "react";
            import { createRoot } from "react-dom/client";

            function App() {
                return <h1>PocketCoded Web React starter</h1>;
            }

            createRoot(document.getElementById("app")).render(<App />);
        """.trimIndent() +
                "\n"
    } else {
        """
            const app = document.getElementById("app");

            app.innerHTML = `
                <h1>PocketCoded Web starter</h1>
                <p>Edit src/main.js and press Run Code.</p>
            `;
        """.trimIndent() +
                "\n"
    }
}

private fun hasPackageScript(root: File, scriptName: String): Boolean {
    val packageJson = runCatching { JSONObject(File(root, "package.json").readText()) }.getOrNull()
    return packageJson?.optJSONObject("scripts")?.has(scriptName) == true
}

private fun isNpmPackageInstalled(root: File, packageName: String): Boolean {
    return readInstalledPackageVersion(npmPackageDirectory(root, packageName)) != null
}

private fun findLocalPackageBinary(root: File, command: String, packageName: String?): File? {
    val normalizedCommand = normalizePackageExecutableName(command)
    val packageDirectories =
            if (packageName != null) {
                listOf(npmPackageDirectory(root, packageName))
            } else {
                installedNpmPackageDirectories(root)
            }
    return packageDirectories
            .asSequence()
            .flatMap { packageDirectory -> packageBinCommands(packageDirectory).asSequence() }
            .firstOrNull { (binName, _) ->
                normalizePackageExecutableName(binName) == normalizedCommand
            }
            ?.value
}

private fun packageBinCommands(packageDirectory: File): Map<String, File> {
    val packageJson = File(packageDirectory, "package.json")
    if (!packageJson.isFile) return emptyMap()
    val json = runCatching { JSONObject(packageJson.readText()) }.getOrNull() ?: return emptyMap()
    return when (val bin = json.opt("bin")) {
        is String ->
                mapOf(
                        packageBinaryName(
                                json.optString("name").ifBlank { packageDirectory.name }
                        ) to File(packageDirectory, bin)
                )
        is JSONObject ->
                buildMap {
                    bin.keys().forEach { key ->
                        bin.optString(key).takeIf { it.isNotBlank() }?.let { relativePath ->
                            put(key, File(packageDirectory, relativePath))
                        }
                    }
                }
        else -> emptyMap()
    }
}

private fun installedNpmPackageDirectories(root: File): List<File> {
    val nodeModules = File(root, "node_modules")
    if (!nodeModules.isDirectory) return emptyList()
    return nodeModules.listFiles().orEmpty().flatMap { entry ->
        if (entry.name.startsWith("@") && entry.isDirectory) {
            entry.listFiles().orEmpty().filter { it.isDirectory }
        } else {
            listOf(entry).filter { it.isDirectory }
        }
    }
}

private fun packageBinaryName(packageName: String): String {
    return packageName.substringAfterLast('/').removePrefix("@")
}

private fun normalizePackageExecutableName(command: String): String {
    return command.trim()
            .removeSuffix(".cmd")
            .removeSuffix(".bin")
            .removeSuffix(".js")
            .substringAfterLast('/')
            .lowercase()
}

private fun syncPackageManagerLockfile(root: File, manager: String): String {
    return when (manager) {
        "pnpm" -> {
            writePnpmLockfile(root)
            "Wrote pnpm-lock.yaml"
        }
        "yarn" -> {
            writeYarnLockfile(root)
            "Wrote yarn.lock"
        }
        else -> ""
    }
}

private fun writePnpmLockfile(root: File) {
    val packageJson =
            runCatching { JSONObject(File(root, "package.json").readText()) }.getOrNull()
                    ?: JSONObject()
    val installed = installedNpmPackages(root)
    File(root, "pnpm-lock.yaml")
            .writeText(
                    buildString {
                        append("lockfileVersion: '9.0'\n\n")
                        append("importers:\n")
                        append("  .:\n")
                        appendPnpmDependencySection("dependencies", packageJson, installed)
                        appendPnpmDependencySection("devDependencies", packageJson, installed)
                        append("\npackages:\n")
                        installed.toSortedMap().forEach { (name, version) ->
                            append("  /").append(name).append('@').append(version).append(":\n")
                            append("    resolution: {integrity: pocketcoded-web}\n")
                        }
                    }
            )
}

private fun StringBuilder.appendPnpmDependencySection(
        sectionName: String,
        packageJson: JSONObject,
        installed: Map<String, String>,
) {
    val dependencies = jsonObjectToStringMap(packageJson.optJSONObject(sectionName))
    if (dependencies.isEmpty()) return
    append("    ").append(sectionName).append(":\n")
    dependencies.toSortedMap().forEach { (name, range) ->
        append("      ").append(name).append(":\n")
        append("        specifier: ").append(range).append('\n')
        append("        version: ").append(installed[name] ?: range).append('\n')
    }
}

private fun writeYarnLockfile(root: File) {
    val packageJson =
            runCatching { JSONObject(File(root, "package.json").readText()) }.getOrNull()
                    ?: JSONObject()
    val installed = installedNpmPackages(root)
    val dependencies =
            listOf("dependencies", "devDependencies", "optionalDependencies")
                    .flatMap { section ->
                        jsonObjectToStringMap(packageJson.optJSONObject(section)).entries
                    }
                    .distinctBy { it.key }
                    .sortedBy { it.key }
    File(root, "yarn.lock")
            .writeText(
                    buildString {
                        append(
                                "# This file is generated by PocketCoded Web's built-in Yarn-compatible manager.\n\n"
                        )
                        dependencies.forEach { (name, range) ->
                            val version = installed[name] ?: range
                            append('"')
                                    .append(name)
                                    .append('@')
                                    .append(range)
                                    .append('"')
                                    .append(":\n")
                            append("  version \"").append(version).append("\"\n")
                            append("  resolved \"https://registry.npmjs.org/")
                                    .append(name)
                                    .append("/-/")
                                    .append(name.substringAfterLast('/'))
                                    .append('-')
                                    .append(version)
                                    .append(".tgz\"\n\n")
                        }
                    }
            )
}

private fun parseNpmInstallOptions(rawArgs: List<String>): NpmInstallOptions {
    val packages = mutableListOf<NpmPackageSpec>()
    var saveDev = false
    var noSave = false
    var index = 0
    while (index < rawArgs.size) {
        val arg = rawArgs[index]
        when {
            arg == "-D" || arg == "--save-dev" || arg == "--dev" -> saveDev = true
            arg == "-S" || arg == "--save" -> saveDev = false
            arg == "--no-save" -> noSave = true
            arg == "--registry" || arg == "--tag" || arg == "--cache" -> index += 1
            arg.startsWith("--registry=") ||
                    arg.startsWith("--tag=") ||
                    arg.startsWith("--cache=") -> Unit
            arg.startsWith("-") -> Unit
            else -> parseNpmPackageSpec(arg)?.let(packages::add)
        }
        index += 1
    }
    return NpmInstallOptions(packages = packages, saveDev = saveDev, noSave = noSave)
}

private fun parseNpmPackageSpec(raw: String): NpmPackageSpec? {
    val clean = raw.trim().removeSurrounding("\"").removeSurrounding("'")
    if (clean.isBlank() || clean.startsWith("http://") || clean.startsWith("https://")) return null
    val atIndex =
            if (clean.startsWith("@")) clean.indexOf('@', startIndex = 1)
            else clean.lastIndexOf('@')
    val name = if (atIndex > 0) clean.substring(0, atIndex) else clean
    val version = if (atIndex > 0) clean.substring(atIndex + 1).ifBlank { "latest" } else "latest"
    return NpmPackageSpec(
                    name = name.trim().lowercase(),
                    requestedVersion = version.trim().ifBlank { "latest" },
            )
            .takeIf { isValidNpmPackageName(it.name) }
}

private fun createOrUpdatePackageJson(root: File, force: Boolean): WebShellResult {
    val packageFile = File(root, "package.json")
    if (packageFile.exists() && !force) {
        return WebShellResult(
                output = "package.json already exists. Use npm init -y to refresh defaults."
        )
    }
    val packageJson =
            if (packageFile.exists()) {
                runCatching { JSONObject(packageFile.readText()) }.getOrDefault(JSONObject())
            } else {
                JSONObject()
            }
    applyDefaultPackageJsonFields(packageJson, root.name)
    packageFile.writeText(packageJson.toString(2) + "\n")
    return WebShellResult(output = "Wrote package.json", workspaceChanged = true)
}

private fun installNpmPackages(root: File, options: NpmInstallOptions): WebShellResult {
    return runCatching {
        val packageFile = File(root, "package.json")
        val packageJson = loadOrCreatePackageJson(root)
        val requestedPackages =
                if (options.packages.isNotEmpty()) {
                    options.packages
                } else {
                    packageJsonDependencySpecs(packageJson)
                }
        if (requestedPackages.isEmpty()) {
            return WebShellResult(
                    output = "No dependencies to install. Try npm install <package>.",
                    workspaceChanged = false
            )
        }

        val state = NpmInstallState(rootDirectory = root)
        requestedPackages.forEach { spec ->
            installNpmPackage(spec = spec, state = state, depth = 0)
        }

        if (!options.noSave && options.packages.isNotEmpty()) {
            val dependencySectionName = if (options.saveDev) "devDependencies" else "dependencies"
            val dependencySection =
                    packageJson.optJSONObject(dependencySectionName)
                            ?: JSONObject().also { packageJson.put(dependencySectionName, it) }
            requestedPackages.forEach { spec ->
                state.installedByName[spec.name]?.let { version ->
                    dependencySection.put(spec.name, "^$version")
                }
            }
        }

        packageFile.writeText(packageJson.toString(2) + "\n")
        writeNpmPackageLock(root, packageJson, state.lockPackages)

        val summary = buildString {
            append("Installed ").append(state.installedByName.size).append(" package")
            append(if (state.installedByName.size == 1) "." else "s.")
            if (state.messages.isNotEmpty()) {
                append("\n").append(state.messages.joinToString("\n"))
            }
        }
        WebShellResult(output = summary, workspaceChanged = true)
    }
            .getOrElse { error ->
                WebShellResult(error = "npm install failed: ${error.message ?: "unknown error"}")
            }
}

private fun cleanInstallNpmPackages(root: File): WebShellResult {
    if (!File(root, "package.json").isFile) {
        return WebShellResult(error = "npm ci: package.json is missing")
    }
    // Never replace a locked install with a fresh dependency resolution.
    return WebShellResult(
            error =
                    "npm ci: reproducible lockfile installs are not supported by the mobile package manager. " +
                            "No files were changed. Use npm install only if you intend to resolve dependencies again.",
    )
}

private fun installNpmPackage(
        spec: NpmPackageSpec,
        state: NpmInstallState,
        depth: Int,
): NpmResolvedPackage? {
    if (depth > NPM_MAX_DEPENDENCY_DEPTH) {
        state.messages += "warning: skipped deep dependency ${spec.name}@${spec.requestedVersion}"
        return null
    }
    if (state.installCount >= NPM_MAX_INSTALL_PACKAGES) {
        state.messages +=
                "warning: package limit reached; skipped ${spec.name}@${spec.requestedVersion}"
        return null
    }

    val metadata = npmPackageMetadata(spec.name, state)
    val version =
            resolveNpmVersion(metadata, spec.requestedVersion)
                    ?: error("Could not resolve ${spec.name}@${spec.requestedVersion}")
    val existingVersion = state.installedByName[spec.name]
    if (existingVersion != null) {
        if (existingVersion != version) {
            state.messages +=
                    "warning: ${spec.name} already installed as $existingVersion; nested $version is not supported."
        }
        return null
    }

    val versionJson =
            metadata.optJSONObject("versions")?.optJSONObject(version)
                    ?: error("Registry metadata for ${spec.name}@$version is incomplete.")
    val dist =
            versionJson.optJSONObject("dist")
                    ?: error("${spec.name}@$version has no downloadable tarball.")
    val tarballUrl =
            dist.optString("tarball").ifBlank { error("${spec.name}@$version has no tarball URL.") }
    val dependencies = jsonObjectToStringMap(versionJson.optJSONObject("dependencies"))
    val resolved =
            NpmResolvedPackage(
                    name = spec.name,
                    version = version,
                    tarballUrl = tarballUrl,
                    integrity = dist.optString("integrity").ifBlank { dist.optString("shasum") },
                    dependencies = dependencies,
            )

    state.installCount += 1
    state.installedByName[spec.name] = version
    extractNpmPackageTarball(
            tarballUrl = tarballUrl,
            targetDirectory = npmPackageDirectory(state.rootDirectory, spec.name),
            projectRoot = state.rootDirectory,
    )
    addNpmLockPackage(state.lockPackages, resolved)
    state.messages += "+ ${spec.name}@$version"

    dependencies.forEach { (dependencyName, range) ->
        if (isValidNpmPackageName(dependencyName)) {
            installNpmPackage(NpmPackageSpec(dependencyName, range), state, depth + 1)
        }
    }
    return resolved
}

private fun uninstallNpmPackages(root: File, rawArgs: List<String>): WebShellResult {
    val specs = rawArgs.filterNot { it.startsWith("-") }.mapNotNull(::parseNpmPackageSpec)
    if (specs.isEmpty()) return WebShellResult(error = "npm uninstall: expected a package name")

    val packageJson = loadOrCreatePackageJson(root)
    val removed = mutableListOf<String>()
    specs.forEach { spec ->
        val target = npmPackageDirectory(root, spec.name)
        if (safeDeleteNpmPackageDirectory(root, target)) removed += spec.name
        packageJson.optJSONObject("dependencies")?.remove(spec.name)
        packageJson.optJSONObject("devDependencies")?.remove(spec.name)
        packageJson.optJSONObject("optionalDependencies")?.remove(spec.name)
    }
    File(root, "package.json").writeText(packageJson.toString(2) + "\n")

    val lockFile = File(root, "package-lock.json")
    if (lockFile.isFile) {
        runCatching {
            val lock = JSONObject(lockFile.readText())
            val packages = lock.optJSONObject("packages") ?: JSONObject()
            specs.forEach { packages.remove("node_modules/${it.name}") }
            lock.put("packages", packages)
            lockFile.writeText(lock.toString(2) + "\n")
        }
    }

    return WebShellResult(
            output =
                    if (removed.isEmpty()) "No installed packages matched."
                    else "Removed ${removed.joinToString(", ")}",
            workspaceChanged = true,
    )
}

private fun listNpmPackages(root: File): WebShellResult {
    val packageJson = runCatching { JSONObject(File(root, "package.json").readText()) }.getOrNull()
    val requested = packageJson?.let(::packageJsonDependencySpecs).orEmpty()
    val installed = installedNpmPackages(root)
    if (requested.isEmpty() && installed.isEmpty()) {
        return WebShellResult(output = "No npm packages installed.")
    }

    val lines = mutableListOf("${sanitizeNpmPackageName(root.name)}")
    requested.forEach { spec ->
        val version = installed[spec.name]
        lines +=
                if (version == null) {
                    "`-- ${spec.name}@${spec.requestedVersion} missing"
                } else {
                    "`-- ${spec.name}@$version"
                }
    }
    installed
            .filterKeys { installedName -> requested.none { it.name == installedName } }
            .toSortedMap()
            .forEach { (name, version) -> lines += "`-- $name@$version extraneous" }
    return WebShellResult(output = lines.joinToString("\n"))
}

private fun runNpmScript(
        root: File,
        working: File,
        rawArgs: List<String>,
        browserPreview: Boolean = false,
): WebShellResult {
    val packageRoot =
            generateSequence(working.canonicalFile) { current ->
                current.parentFile?.takeIf { isSameOrInside(root.canonicalFile, it) }
            }
                    .firstOrNull { File(it, "package.json").isFile }
                    ?: root
    val scriptName =
            rawArgs.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: return WebShellResult(output = npmScriptsText(packageRoot))
    val packageJson =
            runCatching { JSONObject(File(packageRoot, "package.json").readText()) }.getOrNull()
                    ?: return WebShellResult(error = "npm run: package.json is missing")
    val scripts =
            packageJson.optJSONObject("scripts")
                    ?: return WebShellResult(error = "npm run: no scripts defined")
    val script =
            scripts.optString(scriptName).ifBlank {
                return WebShellResult(error = "npm run: missing script '$scriptName'")
            }
    return runSupportedNpmScript(
            scriptName,
            script,
            root,
            packageRoot,
            rawArgs.drop(1).let { if (it.firstOrNull() == "--") it.drop(1) else it },
            browserPreview,
    )
}

private fun runSupportedNpmScript(
        scriptName: String,
        script: String,
        root: File,
        working: File,
        extraArgs: List<String> = emptyList(),
        browserPreview: Boolean = false,
): WebShellResult {
    val command = script.trim()
    val lower = command.lowercase()
    val prefix = "> $scriptName\n> $command"
    val delegated =
            when {
                lower == "preview" ||
                        lower == "run" ||
                        lower == "serve" ||
                        lower.startsWith("preview ") ||
                        lower.startsWith("run ") ||
                        lower.startsWith("serve ") -> {
                    WebShellResult(
                            output = "Choose where to open the live preview.",
                            openPreview = true
                    )
                }
                lower == "lint" || lower == "check" || lower == "validate" -> {
                    WebShellResult(output = auditWebProject(root))
                }
                lower.startsWith("format ") -> executeWebShellCommand(command, root, working)
                else -> runNodePackageScript(command, extraArgs, root, working, scriptName, browserPreview)
            }
    return delegated.copy(
            output = listOf(prefix, delegated.output).filter { it.isNotBlank() }.joinToString("\n"),
    )
}

private fun runNodePackageScript(
        command: String,
        extraArgs: List<String>,
        root: File,
        working: File,
        scriptName: String,
        browserPreview: Boolean = false,
): WebShellResult {
    val tokens = splitShellCommandLine(command).toMutableList()
    val env = mutableMapOf("npm_lifecycle_event" to scriptName, "npm_lifecycle_script" to command)
    while (tokens.firstOrNull()?.matches(Regex("[A-Za-z_][A-Za-z0-9_]*=.*")) == true) {
        val assignment = tokens.removeAt(0)
        env[assignment.substringBefore('=')] = assignment.substringAfter('=')
    }
    val executable = tokens.firstOrNull() ?: return WebShellResult(error = "Empty npm script")
    val args = tokens.drop(1) + extraArgs
    val file: File?
    val fileArgs: List<String>
    if (executable in setOf("node", "js")) {
        file = args.firstOrNull()?.let { resolveTerminalPath(root, working, it) }
        fileArgs = args.drop(1)
    } else {
        file =
                findLocalPackageBinary(working, executable, null)
                        ?: findLocalPackageBinary(root, executable, null)
        fileArgs = args
    }
    if (file == null || !file.isFile)
            return WebShellResult(
                    error =
                            "Cannot find the Node entry point for '$executable'. Run npm install and check package.json."
            )
    if (tokens.any { it in setOf("&&", "||", ";", "|", "&", ">", ">>") }) {
        return WebShellResult(
                error =
                        "This script uses shell operators. Put startup logic in a JavaScript entry point and run it with node."
        )
    }
    return NodeSessionRuntime.start(root, working, file, fileArgs, env, browserPreview = browserPreview)
}

private fun npmScriptsText(root: File): String {
    val packageJson =
            runCatching { JSONObject(File(root, "package.json").readText()) }.getOrNull()
                    ?: return "No package.json. Run npm init -y."
    val scripts = packageJson.optJSONObject("scripts") ?: return "No scripts defined."
    if (scripts.length() == 0) return "No scripts defined."
    return buildString {
                append("Scripts\n")
                scripts.keys().asSequence().sorted().forEach { name ->
                    append("- ")
                            .append(name)
                            .append(": ")
                            .append(scripts.optString(name))
                            .append('\n')
                }
            }
            .trimEnd()
}

private fun viewNpmPackage(rawArgs: List<String>): WebShellResult {
    val spec =
            rawArgs.firstOrNull()?.let(::parseNpmPackageSpec)
                    ?: return WebShellResult(error = "npm view: expected a package name")
    return runCatching {
        val metadata = npmPackageMetadata(spec.name, NpmInstallState(rootDirectory = File(".")))
        val latest = metadata.optJSONObject("dist-tags")?.optString("latest").orEmpty()
        val version = resolveNpmVersion(metadata, spec.requestedVersion) ?: latest
        val versionJson = metadata.optJSONObject("versions")?.optJSONObject(version)
        val dependencies = jsonObjectToStringMap(versionJson?.optJSONObject("dependencies"))
        WebShellResult(
                output =
                        buildString {
                            append(spec.name)
                                    .append('@')
                                    .append(version.ifBlank { latest })
                                    .append('\n')
                            metadata.optString("description").takeIf { it.isNotBlank() }?.let {
                                append(it).append('\n')
                            }
                            if (latest.isNotBlank()) append("latest: ").append(latest).append('\n')
                            append("dependencies: ").append(dependencies.size)
                        }
        )
    }
            .getOrElse { error ->
                WebShellResult(error = "npm view failed: ${error.message ?: "unknown error"}")
            }
}

private fun loadOrCreatePackageJson(root: File): JSONObject {
    val packageFile = File(root, "package.json")
    val packageJson =
            if (packageFile.isFile) {
                runCatching { JSONObject(packageFile.readText()) }.getOrDefault(JSONObject())
            } else {
                JSONObject()
            }
    applyDefaultPackageJsonFields(packageJson, root.name)
    if (!packageFile.exists()) packageFile.writeText(packageJson.toString(2) + "\n")
    return packageJson
}

private fun applyDefaultPackageJsonFields(packageJson: JSONObject, projectName: String) {
    if (!packageJson.has("name")) packageJson.put("name", sanitizeNpmPackageName(projectName))
    if (!packageJson.has("version")) packageJson.put("version", "1.0.0")
    if (!packageJson.has("private")) packageJson.put("private", true)
    if (!packageJson.has("type")) packageJson.put("type", "module")
    if (!packageJson.has("scripts")) {
        packageJson.put(
                "scripts",
                JSONObject()
                        .put("start", "preview")
                        .put("dev", "preview")
                        .put("build", "preview")
                        .put("lint", "lint")
                        .put("test", "lint"),
        )
    }
}

private fun packageJsonDependencySpecs(packageJson: JSONObject): List<NpmPackageSpec> {
    return listOf("dependencies", "devDependencies", "optionalDependencies")
            .flatMap { sectionName ->
                jsonObjectToStringMap(packageJson.optJSONObject(sectionName)).mapNotNull {
                        (name, version) ->
                    if (isValidNpmPackageName(name))
                            NpmPackageSpec(name, version.ifBlank { "latest" })
                    else null
                }
            }
            .distinctBy { it.name }
}

private fun npmPackageMetadata(name: String, state: NpmInstallState): JSONObject {
    state.metadataCache[name]?.let {
        return it
    }
    val encodedName = URLEncoder.encode(name, Charsets.UTF_8.name()).replace("+", "%20")
    val metadata = JSONObject(httpGetText("$NPM_REGISTRY_URL/$encodedName"))
    state.metadataCache[name] = metadata
    return metadata
}

private fun resolveNpmVersion(metadata: JSONObject, requestedRange: String): String? {
    val versions = metadata.optJSONObject("versions") ?: return null
    val tags = metadata.optJSONObject("dist-tags")
    val cleanRange =
            requestedRange.trim().removePrefix("npm:").substringBefore("||").trim().ifBlank {
                "latest"
            }
    tags?.optString(cleanRange)?.takeIf { it.isNotBlank() }?.let {
        return it
    }
    if (cleanRange == "latest" || cleanRange == "*" || cleanRange == "x") {
        return tags?.optString("latest")?.takeIf { it.isNotBlank() } ?: newestNpmVersion(versions)
    }
    versions.optJSONObject(cleanRange)?.let {
        return cleanRange
    }

    val range =
            cleanRange
                    .replace(Regex("""[<>=]"""), " ")
                    .trim()
                    .split(Regex("""\s+"""))
                    .firstOrNull()
                    .orEmpty()
    val base =
            parseSemver(range.trimStart('^', '~'))
                    ?: return tags?.optString("latest").takeIf { !it.isNullOrBlank() }
    val matcher: (Semver) -> Boolean =
            when {
                cleanRange.startsWith("^") -> { version ->
                            version >= base &&
                                    if (base.major == 0) {
                                        version.major == 0 && version.minor == base.minor
                                    } else {
                                        version.major == base.major
                                    }
                        }
                cleanRange.startsWith("~") -> { version ->
                            version >= base &&
                                    version.major == base.major &&
                                    version.minor == base.minor
                        }
                cleanRange.startsWith(">=") -> { version -> version >= base }
                else -> { version -> version >= base && version.major == base.major }
            }
    return versions.keys()
            .asSequence()
            .filterNot { '-' in it && '-' !in cleanRange }
            .mapNotNull { version -> parseSemver(version)?.let { it to version } }
            .filter { (semver, _) -> matcher(semver) }
            .maxByOrNull { it.first }
            ?.second
}

private fun newestNpmVersion(versions: JSONObject): String? {
    return versions.keys()
            .asSequence()
            .filterNot { '-' in it }
            .mapNotNull { version -> parseSemver(version)?.let { it to version } }
            .maxByOrNull { it.first }
            ?.second
}

private fun parseSemver(version: String): Semver? {
    val match = Regex("""^(\d+)\.(\d+)\.(\d+)""").find(version.trim()) ?: return null
    return Semver(
            major = match.groupValues[1].toIntOrNull() ?: return null,
            minor = match.groupValues[2].toIntOrNull() ?: return null,
            patch = match.groupValues[3].toIntOrNull() ?: return null,
    )
}

private fun httpGetText(url: String): String {
    val connection = openNpmHttpConnection(url)
    val status = connection.responseCode
    val stream = if (status in 200..299) connection.inputStream else connection.errorStream
    val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    if (status !in 200..299) error("HTTP $status ${connection.responseMessage.orEmpty()} $response")
    return response
}

private fun openNpmHttpConnection(url: String): HttpURLConnection {
    return (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 20_000
        readTimeout = 90_000
        setRequestProperty("Accept", "application/json")
        setRequestProperty("User-Agent", "PocketCodedWeb/npm-compatible")
    }
}

private fun extractNpmPackageTarball(
        tarballUrl: String,
        targetDirectory: File,
        projectRoot: File,
) {
    val nodeModules = File(projectRoot, "node_modules").canonicalFile.apply { mkdirs() }
    val target = targetDirectory.canonicalFile
    if (!isSameOrInside(nodeModules, target)) error("Refusing to install outside node_modules.")

    val connection = openNpmHttpConnection(tarballUrl)
    val length = connection.contentLengthLong
    if (length > NPM_MAX_PACKAGE_DOWNLOAD_BYTES) {
        error("Package is too large for mobile install (${length / 1_000_000} MB).")
    }
    if (connection.responseCode !in 200..299) {
        error("Download failed with HTTP ${connection.responseCode}.")
    }

    target.deleteRecursively()
    target.mkdirs()
    GZIPInputStream(BufferedInputStream(connection.inputStream)).use { input ->
        extractTarStream(input, target)
    }
}

private fun extractTarStream(input: InputStream, targetDirectory: File): Int {
    val header = ByteArray(512)
    var extractedFiles = 0
    var extractedBytes = 0L
    while (readTarHeader(input, header)) {
        val rawName = tarString(header, 0, 100)
        val prefix = tarString(header, 345, 155)
        val fullName =
                listOf(prefix, rawName)
                        .filter { it.isNotBlank() }
                        .joinToString("/")
                        .replace('\\', '/')
        val relativeName = fullName.removePrefix("package/").trimStart('/')
        val size = tarOctal(header, 124, 12)
        val typeFlag = header[156].toInt().toChar()
        val padding = (512 - (size % 512)) % 512

        if (relativeName.isBlank()) {
            skipFully(input, size + padding)
            continue
        }
        extractedBytes += size
        if (extractedBytes > NPM_MAX_PACKAGE_DOWNLOAD_BYTES) {
            error("Extracted package is too large for mobile install.")
        }

        val target = File(targetDirectory, relativeName).canonicalFile
        if (!isSameOrInside(targetDirectory, target)) {
            skipFully(input, size + padding)
            continue
        }

        when (typeFlag) {
            '0', '\u0000' -> {
                target.parentFile?.mkdirs()
                FileOutputStream(target).use { output -> copyExactly(input, output, size) }
                skipFully(input, padding)
                extractedFiles += 1
            }
            '5' -> {
                target.mkdirs()
                skipFully(input, size + padding)
            }
            else -> skipFully(input, size + padding)
        }
    }
    return extractedFiles
}

private fun readTarHeader(input: InputStream, header: ByteArray): Boolean {
    var offset = 0
    while (offset < header.size) {
        val read = input.read(header, offset, header.size - offset)
        if (read < 0) return false
        offset += read
    }
    return header.any { it.toInt() != 0 }
}

private fun tarString(header: ByteArray, offset: Int, length: Int): String {
    val end =
            (offset until offset + length).firstOrNull { header[it].toInt() == 0 }
                    ?: (offset + length)
    return header.copyOfRange(offset, end).toString(Charsets.UTF_8).trim()
}

private fun tarOctal(header: ByteArray, offset: Int, length: Int): Long {
    return tarString(header, offset, length).trim().takeWhile { it in '0'..'7' }.fold(0L) {
            total,
            char ->
        total * 8 + (char - '0')
    }
}

private fun copyExactly(input: InputStream, output: FileOutputStream, byteCount: Long) {
    val buffer = ByteArray(16_384)
    var remaining = byteCount
    while (remaining > 0) {
        val read = input.read(buffer, 0, min(buffer.size.toLong(), remaining).toInt())
        if (read < 0) error("Unexpected end of tarball.")
        output.write(buffer, 0, read)
        remaining -= read
    }
}

private fun skipFully(input: InputStream, byteCount: Long) {
    var remaining = byteCount
    val buffer = ByteArray(8_192)
    while (remaining > 0) {
        val skipped = input.skip(remaining)
        if (skipped > 0) {
            remaining -= skipped
        } else {
            val read = input.read(buffer, 0, min(buffer.size.toLong(), remaining).toInt())
            if (read < 0) return
            remaining -= read
        }
    }
}

private fun addNpmLockPackage(lockPackages: JSONObject, resolved: NpmResolvedPackage) {
    val entry = JSONObject().put("version", resolved.version).put("resolved", resolved.tarballUrl)
    if (resolved.integrity.isNotBlank()) entry.put("integrity", resolved.integrity)
    if (resolved.dependencies.isNotEmpty()) {
        entry.put("dependencies", stringMapToJson(resolved.dependencies))
    }
    lockPackages.put("node_modules/${resolved.name}", entry)
}

private fun writeNpmPackageLock(root: File, packageJson: JSONObject, lockPackages: JSONObject) {
    val rootEntry =
            JSONObject()
                    .put(
                            "name",
                            packageJson.optString("name").ifBlank {
                                sanitizeNpmPackageName(root.name)
                            }
                    )
                    .put("version", packageJson.optString("version").ifBlank { "1.0.0" })
    packageJson.optJSONObject("dependencies")?.takeIf { it.length() > 0 }?.let {
        rootEntry.put("dependencies", JSONObject(it.toString()))
    }
    packageJson.optJSONObject("devDependencies")?.takeIf { it.length() > 0 }?.let {
        rootEntry.put("devDependencies", JSONObject(it.toString()))
    }
    lockPackages.put("", rootEntry)
    val lockJson =
            JSONObject()
                    .put("name", rootEntry.optString("name"))
                    .put("version", rootEntry.optString("version"))
                    .put("lockfileVersion", 3)
                    .put("requires", true)
                    .put("packages", lockPackages)
    File(root, "package-lock.json").writeText(lockJson.toString(2) + "\n")
}

private fun npmPackageDirectory(root: File, packageName: String): File {
    return File(root, "node_modules/${packageName.replace('/', File.separatorChar)}")
}

private fun safeDeleteNpmPackageDirectory(root: File, target: File): Boolean {
    val nodeModules = File(root, "node_modules").canonicalFile
    val canonicalTarget = runCatching { target.canonicalFile }.getOrNull() ?: return false
    if (!isSameOrInside(nodeModules, canonicalTarget)) return false
    return canonicalTarget.exists() && canonicalTarget.deleteRecursively()
}

private fun installedNpmPackages(root: File): Map<String, String> {
    val nodeModules = File(root, "node_modules")
    if (!nodeModules.isDirectory) return emptyMap()
    val installed = mutableMapOf<String, String>()
    nodeModules.listFiles().orEmpty().forEach { entry ->
        if (entry.name.startsWith("@") && entry.isDirectory) {
            entry.listFiles().orEmpty().forEach { scopedPackage ->
                readInstalledPackageVersion(scopedPackage)?.let { version ->
                    installed["${entry.name}/${scopedPackage.name}"] = version
                }
            }
        } else {
            readInstalledPackageVersion(entry)?.let { version -> installed[entry.name] = version }
        }
    }
    return installed
}

private fun readInstalledPackageVersion(directory: File): String? {
    val packageJson = File(directory, "package.json")
    if (!packageJson.isFile) return null
    return runCatching { JSONObject(packageJson.readText()).optString("version") }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
}

private fun jsonObjectToStringMap(json: JSONObject?): Map<String, String> {
    json ?: return emptyMap()
    return buildMap {
        json.keys().forEach { key ->
            val value = json.optString(key).takeIf { it.isNotBlank() } ?: return@forEach
            put(key, value)
        }
    }
}

private fun stringMapToJson(values: Map<String, String>): JSONObject {
    val json = JSONObject()
    values.toSortedMap().forEach { (key, value) -> json.put(key, value) }
    return json
}

private fun isValidNpmPackageName(name: String): Boolean {
    return Regex("""^(?:@[a-z0-9][a-z0-9_.-]*/)?[a-z0-9][a-z0-9_.-]*$""").matches(name)
}

private fun sanitizeNpmPackageName(raw: String): String {
    return raw.trim()
            .lowercase()
            .replace(Regex("""[^a-z0-9_.-]+"""), "-")
            .trim('-', '.', '_')
            .ifBlank { "pocketcoded-web-project" }
            .take(80)
}

private fun appendShellResult(result: WebShellResult): String {
    return buildString {
                if (result.output.isNotBlank()) append(result.output.trimEnd()).append('\n')
                if (result.error.isNotBlank())
                        append("Error: ").append(result.error.trimEnd()).append('\n')
            }
            .trimEnd()
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
            "react" to
                    """
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
            "vue" to
                    """
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
            "three" to
                    """
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
            "bootstrap" to
                    """
            <link href="vendor/bootstrap.min.css" rel="stylesheet">
            <script src="vendor/bootstrap.bundle.min.js"></script>
        """.trimIndent(),
            "tailwind" to
                    """
            <link rel="stylesheet" href="vendor/tailwind.css">
        """.trimIndent(),
            "gsap" to
                    """
            <script type="importmap">
            { "imports": { "gsap": "./libs/gsap.js" } }
            </script>
            <script type="module">
                import gsap from "gsap";
                gsap.to(".box", { x: 120, duration: 0.8 });
            </script>
        """.trimIndent(),
            "axios" to
                    """
            <script type="importmap">
            { "imports": { "axios": "./libs/axios.js" } }
            </script>
            <script type="module">
                import axios from "axios";
                const response = await axios.get("./data.json");
                console.log(response.data);
            </script>
        """.trimIndent(),
            "chart" to
                    """
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
            "jquery" to
                    """
            <script src="libs/jquery.min.js"></script>
        """.trimIndent(),
            "p5" to
                    """
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
            "phaser" to
                    """
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

private fun scaffoldNextStarter(
        targetDirectory: File,
        projectName: String,
        root: File
): WebShellResult {
    val safeJsProjectName = projectName.replace("\\", "\\\\").replace("\"", "\\\"")
    val packageJson =
            JSONObject()
                    .put("name", sanitizeNpmPackageName(projectName))
                    .put("version", "0.1.0")
                    .put("private", true)
                    .put(
                            "scripts",
                            JSONObject()
                                    .put("dev", "next dev")
                                    .put("build", "next build")
                                    .put("start", "next start")
                                    .put("lint", "next lint")
                    )
                    .put(
                            "dependencies",
                            JSONObject()
                                    .put("next", "^15.0.0")
                                    .put("react", "^19.0.0")
                                    .put("react-dom", "^19.0.0")
                    )
    File(targetDirectory, "package.json").writeText(packageJson.toString(2) + "\n")
    File(targetDirectory, "next.config.mjs")
            .writeText(
                    """
            /** @type {import('next').NextConfig} */
            const nextConfig = { output: "export", images: { unoptimized: true } };
            export default nextConfig;
        """.trimIndent() +
                            "\n"
            )
    val appDirectory = File(targetDirectory, "app").apply { mkdirs() }
    File(appDirectory, "layout.jsx")
            .writeText(
                    """
            import "./globals.css";
            export const metadata = { title: "$safeJsProjectName" };
            export default function RootLayout({ children }) {
              return <html lang="en"><body>{children}</body></html>;
            }
        """.trimIndent() +
                            "\n"
            )
    File(appDirectory, "page.jsx")
            .writeText(
                    """
            export default function Home() {
              return <main><h1>${escapeHtml(projectName)}</h1><p>Edit app/page.jsx to get started.</p></main>;
            }
        """.trimIndent() +
                            "\n"
            )
    File(appDirectory, "globals.css").writeText(scaffoldCss())
    File(targetDirectory, "index.html")
            .writeText(
                    """
            <!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${escapeHtml(projectName)}</title><link rel="stylesheet" href="./app/globals.css"></head>
            <body><main id="app"><h1>${escapeHtml(projectName)}</h1><p>Next.js project ready. Edit app/page.jsx, then run <code>npm run dev</code>.</p></main></body></html>
        """.trimIndent() +
                            "\n"
            )
    File(targetDirectory, ".gitignore").writeText("node_modules\n.next\nout\n.env*.local\n")
    return WebShellResult(
            output =
                    "Created ${relativeWorkspacePath(root, targetDirectory)} as a Next.js App Router project. Run npm install, then npm run dev.",
            workspaceChanged = true,
    )
}

private fun openTerminalEditor(
        args: List<String>,
        root: File,
        working: File,
        command: String,
): WebShellResult {
    val rawPath =
            args.drop(1).firstOrNull { !it.startsWith("+") && !it.startsWith("-") }
                    ?: return WebShellResult(error = "$command: expected a file path")
    val target =
            resolveTerminalPath(root, working, rawPath)
                    ?: return WebShellResult(error = "$command: outside project")
    if (!target.exists()) {
        if (!isEditableWebFile(target))
                return WebShellResult(error = "$command: unsupported file type")
        target.parentFile?.mkdirs()
        target.writeText("")
    }
    if (!target.isFile || !isEditableWebFile(target)) {
        return WebShellResult(error = "$command: ${target.name}: expected an editable Web file")
    }
    return WebShellResult(
            output =
                    "Opening ${relativeWorkspacePath(root, target)} in the PocketCoded Web editor.",
            workspaceChanged = true,
            openFilePath = target.absolutePath,
    )
}

private fun executeCurlCommand(args: List<String>, root: File, working: File): WebShellResult {
    if (args.any { it == "--version" || it == "-V" }) {
        return WebShellResult(output = "curl-compatible HTTP client (PocketCoded Web)")
    }
    var method = "GET"
    var outputPath: String? = null
    var headersOnly = false
    val headers = linkedMapOf<String, String>()
    var urlText: String? = null
    var index = 1
    while (index < args.size) {
        when (val argument = args[index]) {
            "-L", "--location", "-s", "--silent", "-S", "--show-error" -> Unit
            "-I", "--head" -> {
                headersOnly = true
                method = "HEAD"
            }
            "-X", "--request" ->
                    method =
                            args.getOrNull(++index)?.uppercase()
                                    ?: return WebShellResult(error = "curl: -X requires a method")
            "-o", "--output" ->
                    outputPath =
                            args.getOrNull(++index)
                                    ?: return WebShellResult(
                                            error = "curl: -o requires a project file"
                                    )
            "-H", "--header" -> {
                val header =
                        args.getOrNull(++index)
                                ?: return WebShellResult(error = "curl: -H requires a header")
                val separator = header.indexOf(':')
                if (separator <= 0) return WebShellResult(error = "curl: invalid header '$header'")
                headers[header.substring(0, separator).trim()] =
                        header.substring(separator + 1).trim()
            }
            else ->
                    if (argument.startsWith("-")) {
                        return WebShellResult(error = "curl: unsupported option '$argument'")
                    } else urlText = argument
        }
        index++
    }
    val requestUrl =
            urlText ?: return WebShellResult(error = "curl: expected an http:// or https:// URL")
    if (!requestUrl.startsWith("https://", true) && !requestUrl.startsWith("http://", true)) {
        return WebShellResult(error = "curl: only HTTP and HTTPS URLs are supported")
    }
    return runCatching {
        val connection = URL(requestUrl).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.setRequestProperty("User-Agent", "PocketCodedWeb/3.1")
        headers.forEach(connection::setRequestProperty)
        val status = connection.responseCode
        val headerText =
                buildString {
                            append("HTTP ")
                                    .append(status)
                                    .append(' ')
                                    .append(connection.responseMessage.orEmpty())
                                    .append('\n')
                            connection.headerFields.filterKeys { it != null }.forEach {
                                    (name, values) ->
                                append(name)
                                        .append(": ")
                                        .append(values.joinToString(", "))
                                        .append('\n')
                            }
                        }
                        .trimEnd()
        val body =
                if (headersOnly) ByteArray(0)
                else {
                    val stream =
                            if (status >= 400) connection.errorStream else connection.inputStream
                    stream?.use { it.readBytes() } ?: ByteArray(0)
                }
        connection.disconnect()
        if (outputPath != null) {
            val target =
                    resolveTerminalPath(root, working, outputPath!!)
                            ?: return@runCatching WebShellResult(
                                    error = "curl: output path is outside project"
                            )
            target.parentFile?.mkdirs()
            target.writeBytes(body)
            WebShellResult(
                    output =
                            "Downloaded ${body.size} bytes to ${relativeWorkspacePath(root, target)} (HTTP $status)",
                    workspaceChanged = true,
            )
        } else {
            val rendered = if (headersOnly) headerText else body.toString(Charsets.UTF_8)
            WebShellResult(
                    output = rendered.take(100_000),
                    error = if (status >= 400) "curl: HTTP $status" else ""
            )
        }
    }
            .getOrElse { WebShellResult(error = "curl: ${it.message ?: "request failed"}") }
}

private fun executeGitCommand(args: List<String>, root: File, working: File): WebShellResult {
    val subcommand = args.getOrNull(1)?.lowercase().orEmpty()
    if (subcommand in setOf("clone", "fetch", "pull", "push")) {
        return executeGitRemoteCommand(args.drop(1), root, working)
    }
    val nativeGit = NativeGitRuntime.executable()
    if (nativeGit.isFile) return executeNativeGit(args.drop(1), root, working, nativeGit)
    return executeJGitCommand(args, root)
}

private fun executeNativeGit(
        args: List<String>,
        root: File,
        working: File,
        git: File
): WebShellResult {
    val prefix = NativeGitRuntime.prefix()
    val mayChangeWorkspace = nativeGitMayChangeWorkspace(args)
    return runCatching {
        val process =
                ProcessBuilder(listOf(git.absolutePath) + args)
                        .directory(working)
                        .redirectErrorStream(true)
                        .apply {
                            environment()
                                    .putAll(
                                            mapOf(
                                                    "GIT_EXEC_PATH" to
                                                            File(prefix, "libexec/git-core")
                                                                    .absolutePath,
                                                    "GIT_TEMPLATE_DIR" to
                                                            File(prefix, "share/git-core/templates")
                                                                    .absolutePath,
                                                    "GIT_CONFIG_GLOBAL" to
                                                            File(root, ".gitconfig").absolutePath,
                                                    "GIT_CONFIG_NOSYSTEM" to "1",
                                                    "GIT_ATTR_NOSYSTEM" to "1",
                                                    "GIT_PAGER" to "cat",
                                                    "PAGER" to "cat",
                                                    "LC_ALL" to "C.UTF-8",
                                                    "PATH" to
                                                            listOf(
                                                                            File(prefix, "bin")
                                                                                    .absolutePath,
                                                                            System.getenv("PATH")
                                                                                    .orEmpty()
                                                                    )
                                                                    .filter(String::isNotBlank)
                                                                    .joinToString(
                                                                            File.pathSeparator
                                                                    ),
                                            )
                                    )
                        }
                        .start()
        val output =
                process.inputStream
                        .bufferedReader()
                        .use { reader ->
                            val captured = StringBuilder()
                            val buffer = CharArray(8 * 1024)
                            while (true) {
                                val count = reader.read(buffer)
                                if (count < 0) break
                                val remaining = 200_000 - captured.length
                                if (remaining > 0)
                                        captured.append(buffer, 0, minOf(count, remaining))
                            }
                            captured.toString()
                        }
                        .trimEnd()
        val exitCode = process.waitFor()
        if (exitCode == 0) WebShellResult(output = output, workspaceChanged = mayChangeWorkspace)
        else
                WebShellResult(
                        error = output.ifBlank { "git exited with code $exitCode" },
                        workspaceChanged = mayChangeWorkspace
                )
    }
            .getOrElse { error ->
                WebShellResult(
                        error = "git: ${error.message ?: "native command failed"}",
                        workspaceChanged = mayChangeWorkspace
                )
            }
}

internal fun nativeGitMayChangeWorkspace(args: List<String>): Boolean {
    val command = args.firstOrNull()?.lowercase().orEmpty()
    return command !in
            setOf(
                    "",
                    "--help",
                    "--version",
                    "annotate",
                    "blame",
                    "branch",
                    "cat-file",
                    "check-attr",
                    "check-ignore",
                    "check-ref-format",
                    "count-objects",
                    "describe",
                    "diff",
                    "diff-files",
                    "diff-index",
                    "diff-tree",
                    "for-each-ref",
                    "fsck",
                    "grep",
                    "help",
                    "log",
                    "ls-files",
                    "ls-remote",
                    "ls-tree",
                    "merge-base",
                    "name-rev",
                    "rev-list",
                    "rev-parse",
                    "shortlog",
                    "show",
                    "show-branch",
                    "show-ref",
                    "status",
                    "tag",
                    "var",
                    "verify-commit",
                    "verify-pack",
                    "verify-tag",
                    "version",
                    "whatchanged",
            )
}

private fun executeJGitCommand(args: List<String>, root: File): WebShellResult {
    val subcommand = args.getOrNull(1)?.lowercase().orEmpty()
    if (subcommand in setOf("--version", "version")) {
        return WebShellResult(output = "git version 2.45.0 (PocketCoded Web JGit fallback)")
    }
    if (subcommand == "init") {
        return runCatching {
            Git.init().setDirectory(root).call().close()
            WebShellResult(
                    output = "Initialized Git repository in ${root.absolutePath}",
                    workspaceChanged = true
            )
        }
                .getOrElse { WebShellResult(error = "git init: ${it.message ?: "failed"}") }
    }
    val git =
            runCatching { Git.open(root) }.getOrElse {
                return WebShellResult(error = "git: not a repository. Run git init first.")
            }
    return git.use { repository ->
        runCatching {
            when (subcommand) {
                "status" -> {
                    val status = repository.status().call()
                    val changes =
                            buildList {
                                        status.added.forEach { add("added: $it") }
                                        status.changed.forEach { add("modified: $it") }
                                        status.modified.forEach { add("modified: $it") }
                                        status.missing.forEach { add("deleted: $it") }
                                        status.removed.forEach { add("deleted: $it") }
                                        status.untracked.forEach { add("untracked: $it") }
                                        status.conflicting.forEach { add("conflict: $it") }
                                    }
                                    .distinct()
                    WebShellResult(
                            output = changes.joinToString("\n").ifBlank { "Working tree clean" }
                    )
                }
                "add" -> {
                    val path =
                            args.getOrNull(2)
                                    ?: return@use WebShellResult(
                                            error = "git add: expected a path (use . for all files)"
                                    )
                    repository.add().addFilepattern(path.replace('\\', '/')).call()
                    WebShellResult(output = "Staged $path", workspaceChanged = true)
                }
                "commit" -> {
                    val messageIndex = args.indexOfFirst { it == "-m" || it == "--message" }
                    val message = args.getOrNull(messageIndex + 1).orEmpty()
                    if (messageIndex < 0 || message.isBlank())
                            return@use WebShellResult(
                                    error = "git commit: use git commit -m \"message\""
                            )
                    val commit =
                            repository
                                    .commit()
                                    .setMessage(message)
                                    .setAuthor("PocketCoded Web", "user@pocketcoded.local")
                                    .setCommitter("PocketCoded Web", "user@pocketcoded.local")
                                    .call()
                    WebShellResult(
                            output = "[${commit.name.take(7)}] ${commit.shortMessage}",
                            workspaceChanged = true
                    )
                }
                "log" -> {
                    val commits = repository.log().setMaxCount(20).call().toList()
                    WebShellResult(
                            output =
                                    commits
                                            .joinToString("\n") {
                                                "${it.name.take(7)} ${it.shortMessage}"
                                            }
                                            .ifBlank { "No commits yet" }
                    )
                }
                "", "help", "--help", "-h" ->
                        WebShellResult(
                                output =
                                        "git init | git status | git add <path> | git commit -m <message> | git log"
                        )
                else ->
                        WebShellResult(
                                error =
                                        "git: unsupported command '$subcommand'. Supported: init, status, add, commit, log."
                        )
            }
        }
                .getOrElse { WebShellResult(error = "git $subcommand: ${it.message ?: "failed"}") }
    }
}

private fun compactTerminalSnapshotForPreviewHandoff(text: String): String {
    val trimmed = trimTerminalBuffer(text)
    if (trimmed.length <= PREVIEW_HANDOFF_TERMINAL_SNAPSHOT_CHARS) return trimmed
    val tailStart = trimmed.length - PREVIEW_HANDOFF_TERMINAL_SNAPSHOT_CHARS
    val nextLineStart = trimmed.indexOf('\n', tailStart).takeIf { it >= 0 }?.plus(1) ?: tailStart
    return "[Earlier terminal output trimmed for browser preview]\n\n${trimmed.substring(nextLineStart)}"
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

    val removalEnd =
            if (lastPromptEnd < text.length && text[lastPromptEnd] == '\n') lastPromptEnd + 1
            else lastPromptEnd
    return buildString(text.length - (removalEnd - lastPromptStart)) {
        append(text, 0, lastPromptStart)
        append(text, removalEnd, text.length)
    }
}

internal fun estimatedLongestLineLength(text: String, cap: Int): Int {
    if (text.isEmpty()) return 0
    val scanText = if (text.length > 5000) text.takeLast(5000) else text
    var longest = 0
    var current = 0
    scanText.forEach { char ->
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
    val children =
            directory
                    .listFiles()
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
        directory
                .listFiles()
                .orEmpty()
                .filter { !it.name.startsWith(".") && !isProjectIgnoredDirectory(it) }
                .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
                .take(80)
                .forEach { child ->
                    builder.append("  ".repeat(depth))
                            .append(if (child.isDirectory) "${child.name}/" else child.name)
                            .append('\n')
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
    return diagnostics.takeIf { it.isNotBlank() }?.prependIndent("[preview] ").orEmpty()
}

private fun auditWebFiles(root: File, files: List<WebFile>, includeOkMessage: Boolean): String {
    val messages = mutableListOf<String>()
    val hasIndex =
            files.any {
                samePath(File(it.filePath).absolutePath, File(root, "index.html").absolutePath)
            }
    if (!hasIndex)
            messages +=
                    "warning: index.html is missing. Preview will use the active file or a generated shell."
    files.forEach { file ->
        val text = file.content
        val relativeFile = relativeWorkspacePath(root, File(file.filePath))
        WebLanguageServices.diagnostics(File(file.filePath), file.name, text).forEach { diagnostic
            ->
            messages +=
                    "${diagnostic.severity}: $relativeFile:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
        }
        when (fileExtension(file.name)) {
            "html", "htm" -> {
                if (!text.contains("<meta name=\"viewport\"", ignoreCase = true)) {
                    messages +=
                            "warning: $relativeFile: add a viewport meta tag for mobile layouts."
                }
                Regex("""(?:href|src)=["']([^"']+)["']""").findAll(text).forEach { match ->
                    val ref = match.groupValues[1]
                    if (isLocalPreviewReference(ref)) {
                        val target = resolvePreviewFile(root, File(file.filePath), ref)
                        if (target == null) {
                            messages += "error: $relativeFile: missing linked file $ref"
                        }
                    }
                }
            }
            "css", "js", "mjs", "cjs", "jsx", "ts", "tsx" -> {
                val balance = braceBalance(text)
                if (balance != 0) messages += "warning: $relativeFile: brace balance is $balance"
                if (fileExtension(file.name) == "css") {
                    Regex("""url\(\s*(['"]?)(.*?)\1\s*\)""").findAll(text).forEach { match ->
                        val ref = match.groupValues[2].trim()
                        if (isLocalPreviewReference(ref) &&
                                        resolvePreviewFile(root, File(file.filePath), ref) == null
                        ) {
                            messages += "error: $relativeFile: missing CSS asset $ref"
                        }
                    }
                }
                if (fileExtension(file.name) in javascriptFileExtensions) {
                    jsImportSpecifierRegex.findAll(text).forEach { match ->
                        val specifier =
                                match.groupValues
                                        .getOrNull(1)
                                        ?.ifBlank { match.groupValues.getOrNull(2).orEmpty() }
                                        ?.trim()
                                        .orEmpty()
                        when {
                            isBareImportSpecifier(specifier) &&
                                    localEsmUrlForSpecifier(root, specifier) == null -> {
                                messages +=
                                        "warning: $relativeFile: package '$specifier' is not available offline. Add it under node_modules/, libs/, vendor/, or assets/."
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
        "json" ->
                runCatching { JSONObject(normalized).toString(2) + "\n" }
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
    val packageName =
            if (parts.first().startsWith("@") && parts.size >= 2) {
                "${parts[0]}/${parts[1]}"
            } else {
                parts[0]
            }
    val packagePartCount = packageName.count { it == '/' } + 1
    val subPath = parts.drop(packagePartCount).joinToString("/")
    val packageDirectory =
            File(rootDirectory, "node_modules/${packageName.replace('/', File.separatorChar)}")
    if (!packageDirectory.isDirectory || !isSameOrInside(rootDirectory, packageDirectory))
            return null

    if (subPath.isNotBlank()) {
        javaScriptModuleCandidates(File(packageDirectory, subPath))
                .firstOrNull { it.isFile }
                ?.let { candidate ->
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
                        is JSONObject ->
                                value.optString("module").ifBlank { value.optString("default") }
                        else -> null
                    }
                }
                .flatMap { entry -> javaScriptModuleCandidates(File(packageDirectory, entry)) }
                .firstOrNull { it.isFile }
                ?.let { candidate ->
                    return previewLocalUrl(rootDirectory, candidate)
                }
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
            ?.let { candidate ->
                return previewLocalUrl(rootDirectory, candidate)
            }

    return null
}

private fun localVendorLibraryEntry(rootDirectory: File, specifier: String): String? {
    val cleanSpecifier = specifier.trim().removePrefix("/")
    val libraryName =
            cleanSpecifier
                    .substringAfterLast('/')
                    .removePrefix("@")
                    .replace(Regex("""[^A-Za-z0-9_.-]"""), "-")
                    .ifBlank {
                        return null
                    }
    val directoryNames = listOf("libs", "lib", "vendor", "assets")
    val fileNames =
            listOf(
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
                ?.let { candidate ->
                    return previewLocalUrl(rootDirectory, candidate)
                }
    }
    return null
}

private fun javaScriptModuleCandidates(candidate: File): List<File> {
    val hasExtension = candidate.extension.isNotBlank()
    val baseCandidates =
            if (hasExtension) {
                listOf(candidate)
            } else {
                listOf(
                        candidate,
                        File("${candidate.path}.mjs"),
                        File("${candidate.path}.js"),
                        File("${candidate.path}.min.js"),
                )
            }
    return baseCandidates +
            listOf(
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
    return previewReferenceCandidates(rootDirectory, sourceOwner, reference).firstOrNull { candidate
        ->
        isSameOrInside(rootDirectory, candidate) && candidate.isFile
    }
}

private fun previewReferenceCandidates(
        rootDirectory: File,
        sourceOwner: File,
        reference: String,
): List<File> {
    if (!isLocalPreviewReference(reference)) return emptyList()
    val cleanReference =
            reference
                    .substringBefore('#')
                    .substringBefore('?')
                    .trim()
                    .replace('\\', '/')
                    .removePrefix("./")
                    .trimStart('/')
    if (cleanReference.isBlank()) return emptyList()
    val ownerDirectory =
            if (sourceOwner.isDirectory) sourceOwner else sourceOwner.parentFile ?: rootDirectory
    return listOf(
                    File(ownerDirectory, cleanReference),
                    File(rootDirectory, cleanReference),
            )
            .mapNotNull { candidate -> runCatching { candidate.canonicalFile }.getOrNull() }
}

private fun rootIndexWebFile(rootDirectory: File, files: List<WebFile>): WebFile? {
    return files.firstOrNull {
        it.name.lowercase() in setOf("index.html", "index.htm") &&
                samePath(it.parentDirectoryPath(rootDirectory), rootDirectory.absolutePath)
    }
}

private fun nearestIndexWebFile(
        rootDirectory: File,
        files: List<WebFile>,
        fromFile: File
): WebFile? {
    val root = runCatching { rootDirectory.canonicalFile }.getOrDefault(rootDirectory)
    val startDirectory =
            runCatching {
                        (if (fromFile.isDirectory) fromFile
                                else fromFile.parentFile ?: rootDirectory)
                                .canonicalFile
                    }
                    .getOrDefault(rootDirectory)
    var directory: File? = startDirectory
    while (directory != null && isSameOrInside(root, directory)) {
        val currentDirectory = directory
        files
                .firstOrNull { file ->
                    file.name.lowercase() in setOf("index.html", "index.htm") &&
                            samePath(
                                    file.parentDirectoryPath(rootDirectory),
                                    currentDirectory.absolutePath
                            )
                }
                ?.let {
                    return it
                }
        if (samePath(currentDirectory.absolutePath, root.absolutePath)) break
        directory = currentDirectory.parentFile
    }
    return rootIndexWebFile(rootDirectory, files)
}

private fun isLocalPreviewReference(reference: String): Boolean {
    val value = reference.trim()
    if (value.isBlank() || value.startsWith("#")) return false
    val lower = value.lowercase()
    return !(lower.startsWith("http://") ||
            lower.startsWith("https://") ||
            lower.startsWith("data:") ||
            lower.startsWith("blob:") ||
            lower.startsWith("mailto:") ||
            lower.startsWith("tel:") ||
            lower.startsWith("//"))
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
    val normalizedLevel =
            when (level.lowercase()) {
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
    val clean = source.substringBefore('?').substringBefore('#').trim()
    if (clean.isBlank() || clean == "about:blank") return ""
    return clean.substringAfterLast('/').ifBlank { clean }
}

private fun buildAiCopilotPrompt(
        rootDirectory: File,
        files: List<WebFile>,
        activeFile: WebFile?,
        userPrompt: String,
        terminalText: String,
): String {
    val activeFileId = activeFile?.id
    val contextFiles =
            files.filterNot { isDatabaseFileName(it.name) || it.name == ".env" || it.name.startsWith(".env.") ||
                it.name in setOf(".npmrc", ".pypirc") || it.name.endsWith(".pem", true) || it.name.endsWith(".key", true) }
                    .sortedBy { if (activeFileId != null && it.id == activeFileId) 0 else 1 }
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
    val projectTree =
            files
                    .sortedBy { relativeWorkspacePath(rootDirectory, File(it.filePath)) }
                    .joinToString("\n") {
                        "- ${relativeWorkspacePath(rootDirectory, File(it.filePath))}"
                    }
                    .ifBlank { "(no editable files)" }
    val activeFileLabel =
            activeFile?.let { relativeWorkspacePath(rootDirectory, File(it.filePath)) } ?: "(none)"
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

        Active file: $activeFileLabel

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

private fun callGeminiDirectly(modelId: String, prompt: String, apiKey: String, endpoint: String): String {
    val url =
            URL(
                    "${validatedAiEndpoint(endpoint).trimEnd('/')}/models/${URLEncoder.encode(modelId, "UTF-8")}:generateContent"
            )
    val payload =
            JSONObject()
                    .put(
                            "contents",
                            JSONArray()
                                    .put(
                                            JSONObject()
                                                    .put("role", "user")
                                                    .put(
                                                            "parts",
                                                            JSONArray()
                                                                    .put(
                                                                            JSONObject()
                                                                                    .put(
                                                                                            "text",
                                                                                            prompt
                                                                                    )
                                                                    )
                                                    )
                                    )
                    )
                    .put(
                            "generationConfig",
                            JSONObject()
                                    .put("temperature", 0.25)
                                    .put("maxOutputTokens", AI_MAX_OUTPUT_TOKENS)
                    )
                    .toString()
    val connection =
            (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20_000
                readTimeout = 90_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-goog-api-key", apiKey)
                instanceFollowRedirects = false
            }
    connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
    val status = connection.responseCode
    val responseText =
            (if (status in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()
    if (status !in 200..299) {
        val message =
                runCatching {
                    JSONObject(responseText).optJSONObject("error")?.optString("message")
                }
                        .getOrNull()
                        .orEmpty()
                        .ifBlank { responseText }
        error(message.ifBlank { "Gemini request failed with HTTP $status." })
    }
    val json = JSONObject(responseText)
    val candidates = json.optJSONArray("candidates") ?: JSONArray()
    val text =
            buildString {
                        for (i in 0 until candidates.length()) {
                            val parts =
                                    candidates
                                            .optJSONObject(i)
                                            ?.optJSONObject("content")
                                            ?.optJSONArray("parts")
                                            ?: continue
                            for (partIndex in 0 until parts.length()) {
                                append(parts.optJSONObject(partIndex)?.optString("text").orEmpty())
                            }
                        }
                    }
                    .trim()
    return text.ifBlank { "I could not generate a response." }
}

private fun requestPersonalAiCompletion(
        model: AiModelOption,
        prompt: String,
        apiKey: String
): String =
        when (model.provider) {
            AiProvider.Gemini -> callGeminiDirectly(model.id, prompt, apiKey, model.defaultEndpoint)
            AiProvider.Anthropic ->
                    postAiJson(
                                    endpoint = model.endpoint,
                                    body =
                                            JSONObject()
                                                    .put("model", model.id)
                                                    .put("max_tokens", AI_MAX_OUTPUT_TOKENS)
                                                    .put(
                                                            "messages",
                                                            JSONArray()
                                                                    .put(
                                                                            JSONObject()
                                                                                    .put(
                                                                                            "role",
                                                                                            "user"
                                                                                    )
                                                                                    .put(
                                                                                            "content",
                                                                                            prompt
                                                                                    )
                                                                    )
                                                    ),
                                    headers =
                                            mapOf(
                                                    "x-api-key" to apiKey,
                                                    "anthropic-version" to "2023-06-01"
                                            ),
                            )
                            .optJSONArray("content")
                            ?.optJSONObject(0)
                            ?.optString("text")
                            .orEmpty()
                            .ifBlank { "I could not generate a response." }
            AiProvider.OpenAiCompatible, AiProvider.AzureOpenAi ->
                    postAiJson(
                                    endpoint = model.endpoint,
                                    body =
                                            JSONObject()
                                                    .put("model", model.id)
                                                    .put("temperature", 0.25)
                                                    .put("max_tokens", AI_MAX_OUTPUT_TOKENS)
                                                    .put(
                                                            "messages",
                                                            JSONArray()
                                                                    .put(
                                                                            JSONObject()
                                                                                    .put(
                                                                                            "role",
                                                                                            "user"
                                                                                    )
                                                                                    .put(
                                                                                            "content",
                                                                                            prompt
                                                                                    )
                                                                    )
                                                    ),
                                    headers = if (model.provider == AiProvider.AzureOpenAi) mapOf("api-key" to apiKey)
                                        else if (apiKey.isBlank()) emptyMap() else mapOf("Authorization" to "Bearer $apiKey"),
                            )
                            .optJSONArray("choices")
                            ?.optJSONObject(0)
                            ?.optJSONObject("message")
                            ?.optString("content")
                            .orEmpty()
                            .ifBlank { "I could not generate a response." }
        }

private fun postAiJson(
        endpoint: String,
        body: JSONObject,
        headers: Map<String, String>
): JSONObject {
    validatedAiEndpoint(endpoint)
    val connection =
            (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                instanceFollowRedirects = false
                connectTimeout = 20_000
                readTimeout = 90_000
                doOutput = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                headers.forEach(::setRequestProperty)
            }
    return try {
        connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val status = connection.responseCode
        val responseText =
                (if (status in 200..299) connection.inputStream else connection.errorStream)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        .orEmpty()
        val json = runCatching { JSONObject(responseText) }.getOrDefault(JSONObject())
        if (status !in 200..299) {
            val message =
                    json.optJSONObject("error")?.optString("message") ?: json.optString("message")
            error(message.ifBlank { "AI request failed with HTTP $status." })
        }
        json
    } finally {
        connection.disconnect()
    }
}

private fun friendlyAiError(message: String): String {
    val lower = message.lowercase()
    return when {
        lower.contains("api key") -> "The AI provider API key was rejected. Check its settings and try again."
        lower.contains("quota") || lower.contains("rate") ->
                "The AI provider's quota is exhausted for this key. Try again later or use another key."
        lower.contains("model") && lower.contains("not found") ->
                "That AI model is not available for this key."
        lower.contains("network") ||
                lower.contains("timeout") ||
                lower.contains("failed to connect") ->
                "AI request could not connect. Check the device internet connection."
        else -> message.ifBlank { "AI request failed. Try again in a moment." }
    }
}

private fun suggestedActionFromAiText(
        text: String,
        activeFile: WebFile? = null
): AiSuggestedAction? {
    jsonObjectTexts(text).forEach { rawJson ->
        val json = runCatching { JSONObject(rawJson) }.getOrNull() ?: return@forEach
        suggestedActionFromJson(json)?.let {
            return it
        }
    }
    Regex("(?is)<terminal>(.*?)</terminal>")
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let {
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
            changes.takeIf { it.isNotEmpty() }?.let {
                AiSuggestedAction.ApplyProjectFileChanges(it)
            }
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
        val operation =
                when (item.optString("operation").ifBlank { item.optString("action") }.lowercase()
                ) {
                    "delete", "remove" -> AiProjectFileOperation.Delete
                    else -> AiProjectFileOperation.Write
                }
        changes +=
                AiProjectFileChange(
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
                val path =
                        editablePathFromAiText(block.language)
                                ?: editablePathFromAiText(block.prefix) ?: return@mapNotNull null
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
    val looksLikeReplacement =
            listOf(
                            "replace active file",
                            "updated active file",
                            "full file",
                            "replace the file",
                            "here is the updated",
                    )
                    .any { lower.contains(it) }
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
    return aiEditableFilePathRegex.find(text.replace('\\', '/'))?.groupValues?.getOrNull(1)?.trim()
}

private fun aiVisibleResponseText(text: String): String {
    var cleaned =
            Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE).replace(text) { match
                ->
                val body = match.groupValues.getOrNull(1).orEmpty().trim()
                if (body.startsWith("{") && isAiActionJsonText(body)) "" else match.value
            }
    cleaned = removeUnclosedAiActionFence(cleaned)
    cleaned =
            Regex("""(?is)<file\s+[^>]*path\s*=\s*(["'])(.*?)\1[^>]*>(.*?)</file>""")
                    .replace(cleaned, "")
    cleaned = Regex("""(?is)<file\s+[^>]*path\s*=\s*(["'])(.*?)\1[^>]*>.*$""").replace(cleaned, "")
    cleaned =
            Regex("""(?is)```([A-Za-z0-9_+#./-]*)\s*\n(.*?)```""").replace(cleaned) { match ->
                val prefixStart = (match.range.first - 240).coerceAtLeast(0)
                val prefix = cleaned.substring(prefixStart, match.range.first)
                val hasFilePath =
                        editablePathFromAiText(match.groupValues.getOrNull(1).orEmpty()) != null ||
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
    if (possiblePartialJson >= 0 && looksLikeAiActionPayload(cleaned.substring(possiblePartialJson))
    ) {
        cleaned = cleaned.substring(0, possiblePartialJson)
    }
    return cleaned.lines()
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
            )
            .any { lower.contains(it) }
}

private fun isAiActionJsonText(text: String): Boolean {
    return runCatching { suggestedActionFromJson(JSONObject(text)) != null }.getOrDefault(false)
}

private fun jsonObjectTexts(text: String): List<String> {
    val blocks =
            Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
                    .findAll(text)
                    .map { it.groupValues[1].trim() }
                    .filter { it.startsWith("{") }
                    .toList()
    if (blocks.isNotEmpty()) return blocks
    val first = text.indexOf('{')
    val last = text.lastIndexOf('}')
    return if (first >= 0 && last > first) listOf(text.substring(first, last + 1)) else emptyList()
}

internal fun resolveAiEditTargets(root: File, changes: List<AiProjectFileChange>): List<File> {
    require(changes.isNotEmpty() && changes.size <= 100) { "AI edits must contain 1 to 100 files." }
    val targets = changes.map { change ->
        require(!File(change.path).isAbsolute && !change.path.contains(':')) { "AI paths must be relative to the project." }
        val target = File(root, change.path).canonicalFile
        require(target != root.canonicalFile && isSameOrInside(root, target)) { "AI path leaves the project: ${change.path}" }
        require(target.relativeTo(root.canonicalFile).invariantSeparatorsPath.split('/').none {
            it in setOf(".git", ".ssh", ".pocketcodedweb", "node_modules")
        }) { "AI cannot edit project metadata or dependencies." }
        require(!target.isDirectory && !isDatabaseFileName(target.name) && (!target.exists() || isEditableWebFile(target))) {
            "AI can edit text files only: ${change.path}"
        }
        target
    }
    require(targets.distinct().size == targets.size) { "AI edits contain duplicate paths." }
    require(changes.sumOf { it.content.toByteArray(Charsets.UTF_8).size.toLong() } <= 32L * 1024 * 1024) { "AI edit exceeds the 32 MiB limit." }
    return targets
}

private fun applyAiProjectFileChanges(
        rootDirectory: File,
        changes: List<AiProjectFileChange>,
        store: ProjectFileStore,
): String {
    resolveAiEditTargets(rootDirectory, changes)
    var writes = 0
    var deletes = 0
    changes.forEach { change ->
        val target = File(rootDirectory, change.path).canonicalFile
        if (!isSameOrInside(rootDirectory, target))
                error("AI change tried to leave the project: ${change.path}")
        if (target.isDirectory) error("AI Chat can only edit files, not folders: ${change.path}")
        when (change.operation) {
            AiProjectFileOperation.Write -> {
                target.parentFile?.mkdirs()
                store.save(target.path, change.content).getOrThrow()
                writes += 1
            }
            AiProjectFileOperation.Delete -> {
                if (target.exists()) {
                    check(target.delete()) { "Could not delete ${change.path}." }
                    deletes += 1
                }
            }
        }
    }
    return buildString {
        append("Applied AI changes.")
        if (writes > 0)
                append(" Wrote ")
                        .append(writes)
                        .append(" file")
                        .append(if (writes == 1) "." else "s.")
        if (deletes > 0)
                append(" Deleted ")
                        .append(deletes)
                        .append(" file")
                        .append(if (deletes == 1) "." else "s.")
    }
}

internal fun runSqlScript(rootDirectory: File, source: String, selectedDatabase: File? = null): String {
    val databaseFile = selectedDatabase ?:
            rootDirectory.walkTopDown().firstOrNull { file ->
                file.isFile && isDatabaseFileName(file.name)
            }
                    ?: File(rootDirectory, "project.sqlite")
    val statements = splitSqlStatements(source)
    if (statements.isEmpty()) return "No SQL statement to run."
    val output = StringBuilder("SQLite: ${databaseFile.name}\n")
    val database =
            SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
            )
    try {
        statements.forEachIndexed { index, statement ->
            val sql = statement.trim()
            if (Regex("(?is)^(SELECT|WITH|PRAGMA|EXPLAIN)\\b").containsMatchIn(sql)) {
                database.rawQuery(sql, null).use { cursor ->
                    val columns = cursor.columnNames.toList()
                    output.append(columns.joinToString(" | ")).append('\n')
                    output.append(
                                    columns.joinToString("-+-") {
                                        "-".repeat(it.length.coerceAtLeast(3))
                                    }
                            )
                            .append('\n')
                    var shown = 0
                    while (cursor.moveToNext() && shown < 500) {
                        output.append(
                                        columns.indices.joinToString(" | ") { column ->
                                            when (cursor.getType(column)) {
                                                android.database.Cursor.FIELD_TYPE_NULL -> "NULL"
                                                android.database.Cursor.FIELD_TYPE_BLOB ->
                                                        "<blob ${cursor.getBlob(column).size} bytes>"
                                                else -> cursor.getString(column).orEmpty()
                                            }
                                        }
                                )
                                .append('\n')
                        shown++
                    }
                    if (cursor.count > shown) output.append("… ${cursor.count - shown} more rows\n")
                    output.append("$shown row(s) shown\n")
                }
            } else {
                database.execSQL(sql)
                val changed =
                        database.rawQuery("SELECT changes()", null).use { cursor ->
                            if (cursor.moveToFirst()) cursor.getLong(0) else 0L
                        }
                output.append("Statement ${index + 1}: OK ($changed row(s) changed)\n")
            }
        }
    } catch (error: Throwable) {
        output.append("SQL error: ${error.message ?: error.javaClass.simpleName}\n")
    } finally {
        database.close()
    }
    return output.toString().trimEnd()
}

internal fun isDatabaseFileName(name: String): Boolean =
        name.substringAfterLast('.', "").lowercase(Locale.US) in
                setOf(
                        "db",
                        "db3",
                        "s3db",
                        "sl3",
                        "sqlite",
                        "sqlite3",
                        "database",
                        "duckdb",
                )

internal fun splitSqlStatements(source: String): List<String> {
    val statements = mutableListOf<String>()
    var start = 0
    var quote: Char? = null
    var index = 0
    while (index < source.length) {
        val char = source[index]
        if (quote != null) {
            if (char == quote && source.getOrNull(index + 1) == quote) index++
            else if (char == quote) quote = null
        } else if (char == '\'' || char == '"') {
            quote = char
        } else if (char == ';') {
            source.substring(start, index).trim().takeIf(String::isNotBlank)?.let(statements::add)
            start = index + 1
        }
        index++
    }
    source.substring(start).trim().takeIf(String::isNotBlank)?.let(statements::add)
    return statements
}

internal fun loadWebFiles(directory: File): List<WebFile> {
    val root = directory.canonicalFile.apply { mkdirs() }
    val loadedFiles = mutableListOf<WebFile>()
    val livePaths = mutableSetOf<String>()

    root.walkTopDown()
            .onEnter { file -> file == root || !isProjectIgnoredDirectory(file) }
            .filter {
                it.isFile && isEditableWebFile(it) && it.length() <= MAX_EDITABLE_PROJECT_FILE_BYTES
            }
            .forEach { file ->
                file.toWebFileOrNull(root)?.let { webFile ->
                    loadedFiles += webFile
                    livePaths += webFile.filePath
                }
            }

    ProjectFileCache.prune(root, livePaths)
    return loadedFiles
}

internal fun loadInitialWebFiles(directory: File, preferredPath: String?): List<WebFile> {
    val root = directory.canonicalFile.apply { mkdirs() }
    val preferred =
            preferredPath?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { file ->
                file.isFile &&
                        isSameOrInside(root, file) &&
                        isEditableWebFile(file) &&
                        file.length() <= MAX_EDITABLE_PROJECT_FILE_BYTES
            }
    val entry =
            preferred
                    ?: listOf("index.html", "index.htm", "main.html")
                            .asSequence()
                            .map { name -> File(root, name) }
                            .firstOrNull { file ->
                                file.isFile && file.length() <= MAX_EDITABLE_PROJECT_FILE_BYTES
                            }
                            ?: root.listFiles()
                            .orEmpty()
                            .asSequence()
                            .filter { file ->
                                file.isFile &&
                                        isEditableWebFile(file) &&
                                        file.length() <= MAX_EDITABLE_PROJECT_FILE_BYTES
                            }
                            .minByOrNull { it.name.lowercase() }
    return entry?.toWebFileOrNull(root)?.let(::listOf).orEmpty()
}

internal fun ensureDefaultWebProject(directory: File) {
    directory.mkdirs()
    if (directory.walkTopDown().any { it.isFile && isEditableWebFile(it) }) return
    File(directory, "index.html").writeText(defaultIndexHtml(directory.name))
    File(directory, "style.css").writeText(defaultCss())
    File(directory, "script.js").writeText(defaultJs())
}

private fun File.toWebFile(rootDirectory: File): WebFile {
    return ProjectFileCache.webFileFor(rootDirectory, this) ?: error("File is outside the project.")
}

private fun File.toWebFileOrNull(rootDirectory: File): WebFile? {
    // webFileFor performs the canonical containment check once.
    return runCatching { ProjectFileCache.webFileFor(rootDirectory, this) }.getOrNull()
}

private fun saveFileContent(path: String, content: String, modifiedAt: Long? = null) {
    if (isDatabaseFileName(File(path).name)) return
    runCatching {
        val file = File(path)
        file.writeText(content)
        modifiedAt?.let { file.setLastModified(it) }
        ProjectFileCache.invalidate(file)
        ProjectSemanticIndexer.invalidate()
    }
}

private fun defaultContentForFile(name: String, projectName: String): String =
        when (fileExtension(name)) {
            in htmlFileExtensions -> defaultIndexHtml(projectName)
            in cssFileExtensions -> defaultCss()
            in javascriptFileExtensions -> defaultJs()
            "json" ->
                    if (name.equals("package.json", ignoreCase = true)) {
                        defaultPackageJson(projectName)
                    } else {
                        "{\n  \"name\": \"${sanitizeNpmPackageName(projectName)}\"\n}\n"
                    }
            "svg" ->
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\">\n    <rect width=\"100\" height=\"100\" fill=\"#426bde\"/>\n</svg>\n"
            else -> ""
        }

private fun defaultIndexHtml(projectName: String): String =
        """
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
""".trimIndent() +
                "\n"

private fun defaultCss(): String =
        """
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
""".trimIndent() +
                "\n"

private fun defaultJs(): String =
        """
    const button = document.querySelector('#actionButton');
    const output = document.querySelector('#output');

    button?.addEventListener('click', () => {
        output.textContent = 'JavaScript is running in the PocketCoded Web preview.';
    });
""".trimIndent() +
                "\n"

private fun defaultPackageJson(projectName: String): String {
    val json = JSONObject()
    applyDefaultPackageJsonFields(json, projectName)
    return json.toString(2) + "\n"
}

private fun buildExplorerEntries(
        rootDirectory: File,
        files: List<WebFile>,
        expandedFolderPaths: List<String>,
        search: String,
): List<ExplorerEntry> {
    val normalizedSearch = search.trim()
    val searchActive = normalizedSearch.isNotBlank()
    val entries = mutableListOf<ExplorerEntry>()
    val root = runCatching { rootDirectory.canonicalFile }.getOrDefault(rootDirectory)
    val rootPath = root.path
    val expanded =
            expandedFolderPaths.mapTo(mutableSetOf()) {
                runCatching { File(it).canonicalPath }.getOrDefault(it)
            }

    data class ExplorerFileInfo(
            val file: WebFile,
            val parentPath: String,
            val relativePath: String,
    )

    fun isPathSameOrInside(parentPath: String, childPath: String): Boolean {
        return childPath == parentPath || childPath.startsWith(parentPath + File.separator)
    }

    fun addAncestorDirectories(path: String, matches: MutableSet<String>) {
        var current: File? = File(path)
        while (current != null) {
            val currentPath = current.path
            if (!isPathSameOrInside(rootPath, currentPath)) break
            matches += currentPath
            if (currentPath == rootPath) break
            current = current.parentFile
        }
    }

    val fileInfos =
            files.mapNotNull { file ->
                val localFile =
                        runCatching { File(file.filePath).canonicalFile }.getOrNull()
                                ?: return@mapNotNull null
                val parentPath = localFile.parentFile?.path ?: return@mapNotNull null
                if (!isPathSameOrInside(rootPath, localFile.path)) return@mapNotNull null
                ExplorerFileInfo(
                        file = file,
                        parentPath = parentPath,
                        relativePath = relativeWorkspacePath(rootDirectory, localFile),
                )
            }
    val filesByParent = fileInfos.groupBy { it.parentPath }
    val matchingDirectoryPaths = mutableSetOf<String>()
    val matchingFileIds =
            if (searchActive) {
                fileInfos
                        .filter { info ->
                            info.file.name.contains(normalizedSearch, ignoreCase = true) ||
                                    info.relativePath.contains(normalizedSearch, ignoreCase = true)
                        }
                        .onEach { info ->
                            addAncestorDirectories(info.parentPath, matchingDirectoryPaths)
                        }
                        .mapTo(mutableSetOf()) { it.file.id }
            } else {
                emptySet<Long>()
            }

    fun directoryMatches(directory: File, directoryPath: String): Boolean {
        if (!searchActive) return true
        return directoryPath in matchingDirectoryPaths ||
                directory.name.contains(normalizedSearch, ignoreCase = true) ||
                relativeWorkspacePath(rootDirectory, directory)
                        .contains(normalizedSearch, ignoreCase = true)
    }

    fun appendDirectory(directory: File, depth: Int) {
        val canonicalDirectory = runCatching { directory.canonicalFile }.getOrDefault(directory)
        val directoryPath = canonicalDirectory.path

        canonicalDirectory
                .listFiles { file -> file.isDirectory && !isProjectIgnoredDirectory(file) }
                .orEmpty()
                .mapNotNull { childDirectory ->
                    runCatching { childDirectory.canonicalFile }.getOrNull()
                }
                .filter { childDirectory -> directoryMatches(childDirectory, childDirectory.path) }
                .sortedBy { it.name.lowercase() }
                .forEach { childDirectory ->
                    val target = childDirectory.toFolderTarget(rootDirectory)
                    val isExpanded = searchActive || childDirectory.path in expanded
                    entries += ExplorerEntry.Directory(target, depth, isExpanded)
                    if (isExpanded) appendDirectory(childDirectory, depth + 1)
                }
        filesByParent[directoryPath]
                .orEmpty()
                .asSequence()
                .filter { info -> !searchActive || info.file.id in matchingFileIds }
                .sortedBy { it.file.name.lowercase() }
                .forEach { entries += ExplorerEntry.SourceFile(it.file, depth) }
    }

    appendDirectory(root, 0)
    return entries
}

@Composable
private fun WebFileIcon(name: String, palette: WebPalette, modifier: Modifier = Modifier) {
    val extension = fileExtension(name)
    if (extension in cssFileExtensions) {
        Icon(
                imageVector = Icons.Rounded.Css,
                contentDescription = null,
                tint = fileColor(name, palette),
                modifier = modifier,
        )
        return
    }
    val drawable =
            when (extension) {
                in htmlFileExtensions -> R.drawable.mit_html
                "js", "jsx", "mjs", "cjs" -> R.drawable.mit_javascript
                "ts", "tsx" -> R.drawable.mit_typescript
                "json" -> R.drawable.mit_json
                "md", "markdown" -> R.drawable.mit_markdown
                "xml", "svg" -> R.drawable.mit_xml
                "yaml", "yml" -> R.drawable.mit_yaml
                "java" -> R.drawable.mit_java
                "kt", "kts" -> R.drawable.mit_kotlin
                "py", "pyw" -> R.drawable.mit_python
                "pdf" -> R.drawable.mit_pdf
                "db", "sqlite", "sqlite3" -> R.drawable.mit_database
                "png", "jpg", "jpeg", "gif", "webp", "ico", "bmp" -> R.drawable.mit_image
                "zip", "jar", "tar", "gz" -> R.drawable.mit_archive
                else -> R.drawable.mit_document
            }
    Icon(
            painter = painterResource(drawable),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = modifier,
    )
}

private fun fileColor(name: String, palette: WebPalette): Color =
        readableTextColor(when (fileExtension(name)) {
            in htmlFileExtensions -> Color(0xFFE86F3A)
            in cssFileExtensions -> Color(0xFF3A8DFF)
            "ts", "tsx" -> Color(0xFF3178C6)
            "jsx" -> Color(0xFF61DAFB)
            in javascriptFileExtensions -> Color(0xFFF7D84A)
            "json" -> palette.warning
            "svg" -> Color(0xFFFF9A35)
            else -> palette.accent
        }, listOf(palette.panel, palette.selectedTab))

private fun fileLanguageLabel(name: String): String =
        when (fileExtension(name)) {
            in htmlFileExtensions -> "Code Editor - HTML"
            in cssFileExtensions -> "Code Editor - CSS"
            in javascriptFileExtensions -> "Code Editor - JavaScript"
            "json" -> "Code Editor - JSON"
            "svg" -> "Code Editor - SVG"
            else -> "Code Editor - Text"
        }

private fun codeEditorTitle(name: String, compact: Boolean): String {
    val language =
            when (fileExtension(name)) {
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
    val trimmed = rawName.trim()
    if (trimmed.isBlank() || trimmed in setOf(".", "..") || trimmed.any { it in "\\/:*?\"<>|" || it.isISOControl() }) return null
    if (trimmed in workspaceMetadataNames || trimmed.endsWith('.')) return null
    val withExtension = if (!trimmed.contains('.') && trimmed !in setOf("Dockerfile", "Makefile", "LICENSE", "README")) "$trimmed.html" else trimmed
    return withExtension.takeIf { Regex("[A-Za-z0-9_. -]+").matches(it) }
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
    if (file.extension.lowercase() in editableWebExtensions) return true
    if (file.extension.lowercase() in setOf("png", "jpg", "jpeg", "gif", "webp", "ico", "pdf", "zip", "jar", "class", "so", "wasm", "woff", "woff2", "ttf", "mp3", "mp4", "duckdb")) return false
    if (!file.isFile || file.length() > MAX_EDITABLE_PROJECT_FILE_BYTES) return false
    return runCatching {
        val prefix = ByteArray(8192)
        val size = file.inputStream().use { it.read(prefix) }.coerceAtLeast(0)
        (0 until size).none { prefix[it] == 0.toByte() || (prefix[it] in 1..8) || (prefix[it] in 14..31) }
    }.getOrDefault(false)
}

private fun looksLikeHtmlSource(source: String): Boolean =
        source.contains("</") ||
                source.contains("<html", ignoreCase = true) ||
                source.contains("<!doctype", ignoreCase = true) ||
                Regex("<[A-Za-z][\\w:-]*(\\s|>|/)").containsMatchIn(source)

private fun isProjectIgnoredDirectory(file: File): Boolean {
    return file.name in workspaceMetadataNames ||
            file.name.equals("node_modules", ignoreCase = true)
}

private fun resolveTerminalPath(
        rootDirectory: File,
        workingDirectory: File,
        rawPath: String
): File? {
    if (rawPath.isBlank()) return workingDirectory
    val candidate =
            if (rawPath.startsWith("/") || rawPath.matches(Regex("^[A-Za-z]:.*"))) {
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
    return runCatching { candidate.canonicalFile }.getOrDefault(rootDirectory).takeIf {
        it.isDirectory && isSameOrInside(rootDirectory, it)
    }
            ?: rootDirectory
}

private fun terminalPrompt(rootDirectory: File, directory: File): String {
    val label = terminalDirectoryLabel(rootDirectory, directory)
    return if (label == "~") "~$" else "$label $"
}

private fun terminalDirectoryLabel(rootDirectory: File, directory: File): String {
    val relative =
            runCatching {
                        directory
                                .canonicalFile
                                .relativeTo(rootDirectory.canonicalFile)
                                .path
                                .replace(File.separatorChar, '/')
                    }
                    .getOrDefault("")
    return if (relative.isBlank() || relative == ".") "~" else "~/$relative"
}

private fun relativeDirectoryLabel(rootDirectory: File, directory: File): String {
    val relative =
            runCatching {
                        directory
                                .canonicalFile
                                .relativeTo(rootDirectory.canonicalFile)
                                .path
                                .replace(File.separatorChar, '/')
                    }
                    .getOrDefault("")
    return if (relative.isBlank() || relative == ".") "" else relative
}

private fun relativeWorkspacePath(rootDirectory: File, file: File): String {
    return runCatching {
                file.canonicalFile
                        .relativeTo(rootDirectory.canonicalFile)
                        .path
                        .replace(File.separatorChar, '/')
            }
            .getOrDefault(file.name)
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
            }
            .getOrDefault(false)
}

private fun WebFile.parentDirectoryPath(root: File): String {
    return File(filePath).parentFile?.absolutePath ?: root.absolutePath
}

private fun isSameOrInside(parent: File, child: File): Boolean {
    return runCatching {
                val parentPath = parent.canonicalFile.path
                val childPath = child.canonicalFile.path
                childPath == parentPath || childPath.startsWith(parentPath + File.separator)
            }
            .getOrDefault(false)
}

private fun samePath(first: String, second: String): Boolean {
    return runCatching { File(first).canonicalPath == File(second).canonicalPath }
            .getOrDefault(first == second)
}

private fun normalizedWorkspaceDirectoryPaths(
        rootDirectory: File,
        paths: List<String>
): List<String> {
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

private fun WebFile.withContent(content: String, modifiedAt: Long = this.modifiedAt): WebFile {
    return copy(
            content = content,
            modifiedAt = modifiedAt,
            sizeBytes = content.length.toLong(),
            contentHash = content.hashCode(),
    )
}

private fun WebFile.withPendingEditorText(pendingTexts: Map<Long, String>): WebFile {
    val pending = pendingTexts[id]
    return if (pending != null) withContent(pending) else this
}

private fun List<WebFile>.withPendingEditorTexts(pendingTexts: Map<Long, String>): List<WebFile> {
    if (pendingTexts.isEmpty()) return this
    return map { it.withPendingEditorText(pendingTexts) }
}

private fun SnapshotStateList<WebFile>.replaceFile(id: Long, transform: WebFile.() -> WebFile) {
    val index = indexOfFirst { it.id == id }
    if (index >= 0) this[index] = this[index].transform()
}

private fun fileExtension(name: String): String = name.substringAfterLast('.', "").lowercase()

private fun escapeHtml(value: String): String {
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}

private fun loadWebIdeTheme(context: Context): WebTheme {
    val raw =
            context.getSharedPreferences(IDE_PREFS_NAME, Context.MODE_PRIVATE)
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

internal fun clearLocalCodingData(context: Context) {
    listOf(
                    File(context.filesDir, "projects"),
                    File(context.filesDir, "workspace"),
                    File(context.filesDir, ".runtime"),
            )
            .forEach { directory -> runCatching { directory.deleteRecursively() } }
    File(context.filesDir, "projects").mkdirs()

    context.getSharedPreferences(IDE_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
    File(context.applicationInfo.dataDir, "shared_prefs")
            .listFiles()
            .orEmpty()
            .filter { file -> file.name.startsWith("${IDE_PREFS_NAME}_") }
            .forEach { file -> runCatching { file.delete() } }

    context.contentResolver.persistedUriPermissions.forEach { permission ->
        val flags =
                (if (permission.isReadPermission) Intent.FLAG_GRANT_READ_URI_PERMISSION else 0) or
                        (if (permission.isWritePermission) Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        else 0)
        if (flags != 0) {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(permission.uri, flags)
            }
        }
    }
}

private fun loadWebIdeSession(context: Context, rootDirectory: File): WebSession {
    val preferences =
            context.getSharedPreferences(
                    "${IDE_PREFS_NAME}_${rootDirectory.absolutePath.hashCode()}",
                    Context.MODE_PRIVATE
            )
    val savedTerminalText = preferences.getString(PREF_TERMINAL_TEXT, "").orEmpty()
    val savedTerminalAt = preferences.getLong(PREF_TERMINAL_TEXT_SAVED_AT, 0L)
    val terminalText =
            if (savedTerminalText.isNotBlank() &&
                            savedTerminalAt > 0L &&
                            System.currentTimeMillis() - savedTerminalAt <
                                    TERMINAL_ACTIVITY_RETENTION_MS
            ) {
                savedTerminalText
            } else {
                if (savedTerminalText.isNotBlank()) {
                    preferences.edit {
                        remove(PREF_TERMINAL_TEXT)
                        remove(PREF_TERMINAL_TEXT_SAVED_AT)
                    }
                }
                ""
            }
    return WebSession(
            activeFileId = preferences.getLong(PREF_ACTIVE_FILE_ID, -1L),
            activeFilePath = preferences.getString(PREF_ACTIVE_FILE_PATH, null),
            workingDirectoryPath = preferences.getString(PREF_WORKING_DIRECTORY_PATH, null),
            terminalDirectoryPath = preferences.getString(PREF_TERMINAL_DIRECTORY_PATH, null),
            expandedFolderPaths =
                    parseStringList(
                            preferences.getString(PREF_EXPANDED_FOLDER_PATHS, "[]").orEmpty()
                    ),
            explorerVisible = preferences.getBoolean(PREF_EXPLORER_VISIBLE, true),
            fileSearch = preferences.getString(PREF_FILE_SEARCH, "").orEmpty(),
            terminalInput = preferences.getString(PREF_TERMINAL_INPUT, "").orEmpty(),
            terminalText = terminalText,
            fullScreenMode =
                    preferences.getString(PREF_FULL_SCREEN_MODE, null)?.let { raw ->
                        runCatching { WebFullScreenMode.valueOf(raw) }.getOrNull()
                    },
            userAiApiKey =
                    preferences.getString(PREF_USER_AI_API_KEY, null)?.takeIf { it.isNotBlank() },
    )
}

private fun saveWebIdeSession(
        context: Context,
        rootDirectory: File,
        session: WebSession,
        persistTerminalText: Boolean = false,
) {
    context.getSharedPreferences(
                    "${IDE_PREFS_NAME}_${rootDirectory.absolutePath.hashCode()}",
                    Context.MODE_PRIVATE
            )
            .edit {
                putLong(PREF_ACTIVE_FILE_ID, session.activeFileId)
                session.activeFilePath?.let { putString(PREF_ACTIVE_FILE_PATH, it) }
                        ?: remove(PREF_ACTIVE_FILE_PATH)
                session.workingDirectoryPath?.let { putString(PREF_WORKING_DIRECTORY_PATH, it) }
                        ?: remove(PREF_WORKING_DIRECTORY_PATH)
                session.terminalDirectoryPath?.let { putString(PREF_TERMINAL_DIRECTORY_PATH, it) }
                        ?: remove(PREF_TERMINAL_DIRECTORY_PATH)
                putString(PREF_EXPANDED_FOLDER_PATHS, jsonStringList(session.expandedFolderPaths))
                putBoolean(PREF_EXPLORER_VISIBLE, session.explorerVisible)
                putString(PREF_FILE_SEARCH, session.fileSearch)
                putString(PREF_TERMINAL_INPUT, session.terminalInput)
                if (persistTerminalText && session.terminalText.isNotBlank()) {
                    putString(
                            PREF_TERMINAL_TEXT,
                            compactTerminalSnapshotForPreviewHandoff(session.terminalText)
                    )
                    putLong(PREF_TERMINAL_TEXT_SAVED_AT, System.currentTimeMillis())
                } else {
                    remove(PREF_TERMINAL_TEXT)
                    remove(PREF_TERMINAL_TEXT_SAVED_AT)
                }
                remove(PREF_FULL_SCREEN_MODE)
                session.userAiApiKey?.let { putString(PREF_USER_AI_API_KEY, it) }
                        ?: remove(PREF_USER_AI_API_KEY)
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
            }
            .getOrDefault(emptyList())
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
                TextButton(onClick = onDismiss, colors = readableTextButtonColors()) { Text("Cancel", color = palette.accent) }
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
            modifier = Modifier.fillMaxWidth().height(48.dp),
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
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                true
            }
            .getOrDefault(false)
}

private fun importFromDocumentFile(context: Context, docFile: DocumentFile, targetDir: File) {
    if (docFile.isDirectory) {
        val newDir = File(targetDir, docFile.name ?: "imported_folder").apply { mkdirs() }
        docFile.listFiles().forEach { child -> importFromDocumentFile(context, child, newDir) }
    } else {
        val targetFile = File(targetDir, docFile.name ?: "imported_file")
        context.contentResolver.openInputStream(docFile.uri)?.use { input ->
            targetFile.outputStream().use { output -> input.copyTo(output) }
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
            val destFile =
                    destFolder.createFile("application/octet-stream", file.name ?: "file")
                            ?: return@forEach
            context.contentResolver.openOutputStream(destFile.uri)?.use { output ->
                file.inputStream().use { input -> input.copyTo(output) }
            }
        }
    }
}

internal fun webDevelopmentDiagnostics(file: File, name: String, source: String): List<CodeDiagnostic> =
    WebLanguageServices.diagnostics(file, name, source)
