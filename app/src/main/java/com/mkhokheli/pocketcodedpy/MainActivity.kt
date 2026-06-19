package com.mkhokheli.pocketcodedpy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.mkhokheli.pocketcodedpy.ui.theme.PocketCodedPyTheme
import androidx.core.content.edit
import androidx.core.net.toUri
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URI
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.URLConnection
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import java.security.KeyPairGenerator
import java.security.KeyPair
import java.security.SecureRandom
import java.util.concurrent.LinkedBlockingQueue
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileBasedConfig
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder
import org.eclipse.jgit.util.FS
import org.json.JSONArray
import org.json.JSONObject

private const val MAX_TERMINAL_BUFFER_CHARS = 200_000
private const val TERMINAL_TRUNCATION_NOTICE = "[Earlier terminal output trimmed]"
private const val TERMINAL_PROCESS_TIMEOUT_MS = 300_000L
private const val COMPLETION_IDLE_TIMEOUT_MS = 3_500L
private const val COMPLETION_INTERACTION_TIMEOUT_MS = 7_000L
private const val JGIT_VERSION = "7.7.0.202606012155-r"

internal fun trimTerminalBuffer(text: String): String {
    if (text.length <= MAX_TERMINAL_BUFFER_CHARS) return text

    val tailStart = text.length - MAX_TERMINAL_BUFFER_CHARS
    val nextLineStart = text.indexOf('\n', tailStart)
        .takeIf { it >= 0 }
        ?.plus(1)
        ?: tailStart
    return "$TERMINAL_TRUNCATION_NOTICE\n${text.substring(nextLineStart)}"
}

internal fun redactTerminalCommandForDisplay(command: String): String {
    return command
        .replace(Regex("(?i)(https?://)[^/@\\s]+@"), "$1")
        .replace(Regex("(?i)(authorization\\s*:\\s*bearer\\s+)[^\\s\\\"]+"), "$1***")
        .replace(Regex("(?i)(authorization\\s*:\\s*basic\\s+)[^\\s\\\"]+"), "$1***")
        .replace(Regex("(?i)(^|\\s)(-u|--user)(=|\\s+)(\\\"[^\\\"]*\\\"|'[^']*'|\\S+)")) { match ->
            val prefix = match.groupValues[1]
            val option = match.groupValues[2]
            val separator = match.groupValues[3]
            "$prefix$option$separator***"
        }
}

internal fun redactRemoteUrlForDisplay(remoteUrl: String): String {
    return remoteUrl.replace(Regex("(?i)(https?://)[^/@\\s]+@"), "$1")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activityContext = this@MainActivity
            var ideTheme by rememberSaveable { mutableStateOf(loadIdeTheme(activityContext)) }

            LaunchedEffect(ideTheme) {
                saveIdeTheme(activityContext, ideTheme)
            }

            PocketCodedPyTheme(
                darkTheme = ideTheme.isDark,
                dynamicColor = false
            ) {
                PocketCodedPyEntry(
                    darkMode = ideTheme.isDark,
                    currentThemeName = ideTheme.name,
                    nextThemeName = ideTheme.next().displayName,
                    onToggleTheme = { ideTheme = ideTheme.next() },
                ) { projectDirectory, onBackToProjects ->
                    PocketIdeApp(
                        ideTheme = ideTheme,
                        onToggleTheme = { ideTheme = ideTheme.next() },
                        lifecycle = this@MainActivity.lifecycle,
                        projectDirectory = projectDirectory,
                        onBackToProjects = onBackToProjects,
                    )
                }
            }
        }
    }
}

@Stable
private data class PythonFile(
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

@Stable
private data class RunResult(
    val success: Boolean,
    val output: String,
    val error: String,
    val stopped: Boolean = false,
)

class TerminalRunController {
    @Volatile
    private var stopped = false

    @Volatile
    private var process: Process? = null

    fun attachProcess(process: Process) {
        this.process = process
        if (stopped) {
            process.destroy()
        }
    }

    fun requestStop() {
        stopped = true
        process?.destroy()
    }

    fun isStopped(): Boolean = stopped
}

class PythonInputBridge(
    private val onInputRequested: (String, PythonInputBridge) -> Unit,
) {
    private val inputLines = LinkedBlockingQueue<String>()

    @Volatile
    var echoInline: Boolean = false
        private set

    @Suppress("unused")
    fun requestInput(output: String?, error: String?): String {
        val pendingOutput = output.orEmpty() + error.orEmpty()
        echoInline = pendingOutput.isNotBlank()
        onInputRequested(pendingOutput, this)
        return runCatching { inputLines.take() }.getOrDefault("")
    }

    fun submitInput(input: String) {
        inputLines.offer(input)
    }

    fun close() {
        inputLines.offer("")
    }
}

private data class ImportResult(
    val success: Boolean,
    val message: String,
    val selectedFilePath: String? = null,
    val workingDirectoryPath: String? = null,
)

private data class StorageOperationResult(
    val success: Boolean,
    val message: String,
)

private data class CompletionItem(
    val label: String,
    val insertText: String = label,
    val detail: String,
    val cursorOffset: Int = insertText.length,
)

private data class CompletionContext(
    val prefix: String,
    val replaceStart: Int,
    val replaceEnd: Int,
    val isDotAccess: Boolean,
)

private data class SyntaxSpan(
    val start: Int,
    val end: Int,
    val style: SpanStyle,
)

private data class CodeDiagnostic(
    val start: Int,
    val end: Int,
    val severity: String,
    val message: String,
    val source: String,
    val line: Int,
)

private data class ShellCommandResult(
    val output: String = "",
    val error: String = "",
    val workingDirectoryPath: String? = null,
    val clearTerminal: Boolean = false,
    val workspaceChanged: Boolean = false,
    val stopped: Boolean = false,
)

private data class VirtualEnvironmentActivation(
    val environmentPath: String? = null,
    val output: String = "",
    val error: String = "",
)

private class PocketWheel(
    val name: String,
    val version: String,
    val filename: String,
    val bytes: ByteArray,
    val requirements: List<String>,
)

private data class AndroidWheelCandidate(
    val filename: String,
    val url: String,
    val version: String,
    val apiLevel: Int,
    val abi: String,
)

private data class IdeSession(
    val activeFileId: Long = -1L,
    val activeFilePath: String? = null,
    val workingDirectoryPath: String? = null,
    val terminalDirectoryPath: String? = null,
    val expandedFolderPaths: List<String> = emptyList(),
    val explorerVisible: Boolean = false,
    val fileSearch: String = "",
    val terminalInput: String = "",
    val terminalText: String = "",
    val fullScreenMode: FullScreenMode? = null,
)

@Stable
private data class IdePalette(
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

private enum class FullScreenMode {
    Editor,
    Terminal,
}

private enum class IdeTheme(
    val displayName: String,
    val isDark: Boolean,
) {
    DarkBlue("Dark Blue", true),
    LightBlue("Light Blue", false),
    DarkPurple("Dark Purple", true),
    LightPurple("Light Purple", false),
    DarkGreen("Dark Green", true),
    LightGreen("Light Green", false),
    DarkTeal("Dark Teal", true),
    LightTeal("Light Teal", false),
    DarkOrange("Dark Orange", true),
    LightOrange("Light Orange", false),
    DarkGray("Dark Gray", true),
    LightGray("Light Gray", false);

    fun next(): IdeTheme = entries[(ordinal + 1) % entries.size]
}

private const val IDE_PREFS_NAME = "pocket_ide"
private const val IMPORT_LINKS_FILE = ".pocketcodedpy-imports.json"
private const val PREF_ACTIVE_FILE_ID = "active_file_id"
private const val PREF_ACTIVE_FILE_PATH = "active_file_path"
private const val PREF_DARK_MODE = "dark_mode"
private const val PREF_IDE_THEME = "ide_theme"
private const val PREF_WORKING_DIRECTORY_PATH = "working_directory_path"
private const val PREF_TERMINAL_DIRECTORY_PATH = "terminal_directory_path"
private const val PREF_EXPANDED_FOLDER_PATHS = "expanded_folder_paths"
private const val PREF_EXPLORER_VISIBLE = "explorer_visible"
private const val PREF_FILE_SEARCH = "file_search"
private const val PREF_TERMINAL_INPUT = "terminal_input"
private const val PREF_TERMINAL_TEXT = "terminal_text"
private const val PREF_FULL_SCREEN_MODE = "full_screen_mode"
private const val MAX_EDITABLE_PROJECT_FILE_BYTES = 1_000_000L
private val WORKSPACE_METADATA_NAMES = setOf(
    ".git",
    ".ssh",
    ".mypy_cache",
    ".pytest_cache",
    ".ruff_cache",
    "__pycache__",
)
private val PROJECT_EXPORT_IGNORED_NAMES = setOf(
    ".ssh",
    ".mypy_cache",
    ".pytest_cache",
    ".ruff_cache",
    "__pycache__",
    IMPORT_LINKS_FILE,
)

private data class SafDocument(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
)

private sealed class ExplorerEntry {
    data class Directory(
        val target: FolderTarget,
        val depth: Int,
        val expanded: Boolean,
    ) : ExplorerEntry()

    data class PythonSourceFile(
        val file: PythonFile,
        val depth: Int,
    ) : ExplorerEntry()
}

@Composable
private fun PocketIdeApp(
    ideTheme: IdeTheme,
    onToggleTheme: () -> Unit,
    lifecycle: Lifecycle? = null,
    projectDirectory: File,
    onBackToProjects: () -> Unit,
) {
    val context = LocalContext.current
    val darkMode = ideTheme.isDark
    val nextThemeName = ideTheme.next().displayName
    val palette = remember(ideTheme) { idePalette(ideTheme) }
    val coroutineScope = rememberCoroutineScope()
    val rootDirectory = remember(projectDirectory.absolutePath) {
        projectDirectory.apply { mkdirs() }
    }
    val initialFiles = remember { loadFiles(rootDirectory) }
    val savedSession = remember { loadIdeSession(context, rootDirectory) }
    val initialActiveFileId = remember(initialFiles, savedSession) {
        savedSession.activeFilePath
            ?.let { savedPath -> initialFiles.firstOrNull { file -> samePath(file.filePath, savedPath) } }
            ?.id
            ?: savedSession.activeFileId
                .takeIf { savedFileId -> initialFiles.any { file -> file.id == savedFileId } }
            ?: initialFiles.first().id
    }
    val files = remember {
        mutableStateListOf<PythonFile>().apply {
            addAll(initialFiles)
        }
    }
    var activeFileId by rememberSaveable {
        mutableLongStateOf(initialActiveFileId)
    }
    var explorerVisible by rememberSaveable { mutableStateOf(savedSession.explorerVisible) }
    var fileSearch by rememberSaveable { mutableStateOf(savedSession.fileSearch) }
    var terminalInput by remember { mutableStateOf("") }
    var terminalText by remember { mutableStateOf("") }
    var activeVirtualEnvironmentPath by remember { mutableStateOf<String?>(null) }
    var isCodeRunning by remember { mutableStateOf(false) }
    var isTerminalRunning by remember { mutableStateOf(false) }
    var activeRunController by remember { mutableStateOf<TerminalRunController?>(null) }
    var fullScreenMode by rememberSaveable { mutableStateOf(savedSession.fullScreenMode) }
    var terminalRevealRequest by remember { mutableLongStateOf(0L) }
    var importOptionsOpen by remember { mutableStateOf(false) }
    var storageMessage by remember { mutableStateOf<StorageOperationResult?>(null) }
    var newFileDialogOpen by remember { mutableStateOf(false) }
    var newFolderDialogOpen by remember { mutableStateOf(false) }
    var directoryVersion by remember { mutableLongStateOf(0L) }
    val pendingEditorSaveJobs = remember { mutableMapOf<Long, Job>() }
    val dirtyFileIds = remember { mutableSetOf<Long>() }
    var awaitingPythonInputBridge by remember { mutableStateOf<PythonInputBridge?>(null) }
    val expandedFolderPaths = remember {
        mutableStateListOf<String>().apply {
            addAll(normalizedWorkspaceDirectoryPaths(rootDirectory, savedSession.expandedFolderPaths))
        }
    }
    var renameTarget by remember { mutableStateOf<PythonFile?>(null) }
    var renameFolderTarget by remember { mutableStateOf<FolderTarget?>(null) }
    var deleteTarget by remember { mutableStateOf<PythonFile?>(null) }
    var deleteFolderTarget by remember { mutableStateOf<FolderTarget?>(null) }
    val isAnyRunActive = isCodeRunning || isTerminalRunning

    val activeFile = files.firstOrNull { it.id == activeFileId } ?: files.first()
    var workingDirectoryPath by rememberSaveable {
        mutableStateOf(
            validDirectoryOrRoot(
                rootDirectory = rootDirectory,
                candidatePath = savedSession.workingDirectoryPath ?: activeFile.parentDirectoryPath(rootDirectory)
            ).absolutePath
        )
    }
    var terminalDirectoryPath by rememberSaveable {
        mutableStateOf(
            validDirectoryOrRoot(
                rootDirectory = rootDirectory,
                candidatePath = savedSession.terminalDirectoryPath ?: activeFile.parentDirectoryPath(rootDirectory)
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
    val activeVirtualEnvironment = activeVirtualEnvironmentPath
        ?.let(::File)
        ?.takeIf(::isPocketVirtualEnvironment)
    val terminalPromptText = terminalPrompt(
        rootDirectory = rootDirectory,
        directory = terminalDirectory,
        virtualEnvironment = activeVirtualEnvironment
    )

    fun selectFile(fileId: Long) {
        val file = files.firstOrNull { it.id == fileId } ?: return
        activeFileId = file.id
        workingDirectoryPath = file.parentDirectoryPath(rootDirectory)
    }

    fun selectFolder(folder: FolderTarget) {
        workingDirectoryPath = folder.path
    }

    fun applyLoadedFiles(
        loadedFiles: List<PythonFile>,
        selectPath: String? = null,
        workingPath: String? = null,
    ) {
        val mergedFiles = loadedFiles.map { loadedFile ->
            val currentFile = files.firstOrNull { it.id == loadedFile.id }
            if (currentFile != null && currentFile.id in dirtyFileIds) {
                loadedFile.copy(
                    content = currentFile.content,
                    modifiedAt = currentFile.modifiedAt,
                )
            } else {
                loadedFile
            }
        }
        files.clear()
        files.addAll(mergedFiles)
        val selectedFile = selectPath?.let { path ->
            mergedFiles.firstOrNull { samePath(it.filePath, path) }
        } ?: mergedFiles.firstOrNull { it.id == activeFileId }
        ?: mergedFiles.first()
        activeFileId = selectedFile.id
        workingDirectoryPath = workingPath ?: selectedFile.parentDirectoryPath(rootDirectory)
    }

    fun refreshFiles(selectPath: String? = null, workingPath: String? = null) {
        applyLoadedFiles(loadFiles(rootDirectory), selectPath, workingPath)
    }

    suspend fun refreshFilesInBackground(selectPath: String? = null, workingPath: String? = null) {
        val loadedFiles = withContext(Dispatchers.IO) { loadFiles(rootDirectory) }
        applyLoadedFiles(loadedFiles, selectPath, workingPath)
    }

    fun handleImportResult(result: ImportResult) {
        storageMessage = StorageOperationResult(result.success, result.message)
        if (result.success) {
            directoryVersion += 1L
            result.workingDirectoryPath?.let { expandedFolderPaths.addUniquePath(it) }
            refreshFiles(
                selectPath = result.selectedFilePath,
                workingPath = result.workingDirectoryPath.takeIf { result.selectedFilePath == null }
            )
        }
    }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            coroutineScope.launch {
                val importResult = withContext(Dispatchers.IO) {
                    importFileFromUri(context, uri, workingDirectory, rootDirectory)
                }
                handleImportResult(importResult)
            }
        }
    }

    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            coroutineScope.launch {
                val importResult = withContext(Dispatchers.IO) {
                    importFolderFromUri(context, uri, workingDirectory, rootDirectory)
                }
                handleImportResult(importResult)
            }
        }
    }

    val saveFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            val sourceDirectory = workingDirectory
            coroutineScope.launch {
                val exportResult = withContext(Dispatchers.IO) {
                    exportFolderToUri(context, uri, sourceDirectory)
                }
                storageMessage = exportResult
            }
        }
    }

    fun updateActiveContent(newCode: String) {
        val fileToSync = activeFile
        dirtyFileIds += fileToSync.id
        files.replaceFile(activeFile.id) {
            copy(
                content = newCode,
                modifiedAt = System.currentTimeMillis()
            )
        }
        pendingEditorSaveJobs.remove(fileToSync.id)?.cancel()
        pendingEditorSaveJobs[fileToSync.id] = coroutineScope.launch {
            delay(120)
            withContext(Dispatchers.IO) {
                saveFileContent(fileToSync.filePath, newCode)
                syncExternalFileContent(context, fileToSync, newCode)
            }
            if (files.firstOrNull { it.id == fileToSync.id }?.content == newCode) {
                dirtyFileIds.remove(fileToSync.id)
            }
        }
    }

    fun flushWorkspaceFiles() {
        pendingEditorSaveJobs.values.forEach(Job::cancel)
        pendingEditorSaveJobs.clear()
        val dirtyFiles = files.filter { it.id in dirtyFileIds }
        dirtyFileIds.clear()
        saveWorkspaceFiles(dirtyFiles)
    }

    fun createFile(rawName: String): String? {
        val cleanName = normalizePythonFileName(rawName)
            ?: return "Use a simple Python file name."
        val target = File(workingDirectory, cleanName)
        if (!isInDirectory(workingDirectory, target)) {
            return "Use a file name in the current folder."
        }
        if (target.exists()) {
            return "$cleanName already exists."
        }

        return runCatching {
            target.writeText("")
            val file = target.toPythonFile(rootDirectory)
            files.add(0, file)
            directoryVersion += 1L
            selectFile(file.id)
            null
        }.getOrElse { error ->
            error.message ?: "Could not create $cleanName."
        }
    }

    fun createFolder(rawName: String): String? {
        val cleanName = normalizeFolderName(rawName)
            ?: return "Use a simple folder name."
        val target = File(workingDirectory, cleanName)
        if (!isInDirectory(workingDirectory, target)) {
            return "Use a folder name in the current folder."
        }
        if (target.exists()) {
            return "$cleanName already exists."
        }

        return runCatching {
            if (!target.mkdirs()) {
                return "Could not create $cleanName."
            }
            expandedFolderPaths.addUniquePath(workingDirectory.absolutePath)
            expandedFolderPaths.addUniquePath(target.absolutePath)
            workingDirectoryPath = target.absolutePath
            directoryVersion += 1L
            null
        }.getOrElse { error ->
            error.message ?: "Could not create $cleanName."
        }
    }

    fun deleteFile(fileId: Long) {
        val index = files.indexOfFirst { it.id == fileId }
        if (index >= 0) {
            val file = files[index]
            pendingEditorSaveJobs.remove(file.id)?.cancel()
            dirtyFileIds.remove(file.id)
            runCatching {
                File(file.filePath).delete()
                removeImportLink(rootDirectory, File(file.filePath))
            }
            files.removeAt(index)
            if (files.isEmpty()) {
                val defaultFile = createDefaultFile(rootDirectory)
                files.add(defaultFile)
                selectFile(defaultFile.id)
            } else if (activeFileId == fileId) {
                selectFile((files.getOrNull(index) ?: files.last()).id)
            }
            directoryVersion += 1L
        }
    }

    fun renameFile(file: PythonFile, newName: String): String? {
        val cleanName = normalizePythonFileName(newName)
            ?: return "Use a simple Python file name."
        if (cleanName == file.name) return null

        val source = File(file.filePath)
        val parentDirectory = source.parentFile ?: workingDirectory
        val target = File(parentDirectory, cleanName)
        if (!isInDirectory(parentDirectory, target)) {
            return "Use a file name in the current folder."
        }
        if (target.exists()) {
            return "$cleanName already exists."
        }

        return runCatching {
            pendingEditorSaveJobs.remove(file.id)?.cancel()
            if (dirtyFileIds.remove(file.id)) {
                saveFileContent(source.absolutePath, file.content)
            }
            if (!source.renameTo(target)) {
                return "Could not rename ${file.name}."
            }
            moveImportLink(rootDirectory, source, target)
            val renamedFile = target.toPythonFile(rootDirectory)
            files.replaceFile(file.id) { renamedFile }
            if (activeFileId == file.id) {
                selectFile(renamedFile.id)
            }
            directoryVersion += 1L
            null
        }.getOrElse { error ->
            error.message ?: "Could not rename ${file.name}."
        }
    }

    fun renameFolder(folder: FolderTarget, newName: String): String? {
        val cleanName = normalizeFolderName(newName)
            ?: return "Use a simple folder name."
        if (cleanName == folder.name) return null

        val source = File(folder.path)
        val parentDirectory = source.parentFile ?: rootDirectory
        val target = File(parentDirectory, cleanName)
        if (!isInDirectory(parentDirectory, target)) {
            return "Use a folder name in the current folder."
        }
        if (target.exists()) {
            return "$cleanName already exists."
        }

        val activeRelativePath = File(activeFile.filePath).takeIf { isSameOrInside(source, it) }
            ?.relativeTo(source)
            ?.path

        return runCatching {
            flushWorkspaceFiles()
            if (!source.renameTo(target)) {
                return "Could not rename ${folder.name}."
            }
            moveImportLinksUnder(rootDirectory, source, target)
            expandedFolderPaths.remove(folder.path)
            expandedFolderPaths.addUniquePath(target.absolutePath)
            workingDirectoryPath = if (samePath(workingDirectoryPath, folder.path)) {
                target.absolutePath
            } else {
                workingDirectoryPath
            }
            directoryVersion += 1L
            refreshFiles(activeRelativePath?.let { File(target, it).absolutePath })
            null
        }.getOrElse { error ->
            error.message ?: "Could not rename ${folder.name}."
        }
    }

    fun deleteFolder(folder: FolderTarget) {
        val directory = File(folder.path)
        if (samePath(directory.absolutePath, rootDirectory.absolutePath)) return
        val activeFallback = files.firstOrNull { !isSameOrInside(directory, File(it.filePath)) }
        files.filter { isSameOrInside(directory, File(it.filePath)) }.forEach { file ->
            pendingEditorSaveJobs.remove(file.id)?.cancel()
            dirtyFileIds.remove(file.id)
        }
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    directory.deleteRecursively()
                    removeImportLinksUnder(rootDirectory, directory)
                }
            }
            expandedFolderPaths.removeAll { path -> isSameOrInside(directory, File(path)) }
            directoryVersion += 1L
            refreshFilesInBackground(activeFallback?.filePath)
        }
    }

    fun createPythonInputBridge(): PythonInputBridge {
        lateinit var bridge: PythonInputBridge
        bridge = PythonInputBridge { pendingOutput, requestedBridge ->
            coroutineScope.launch {
                if (pendingOutput.isNotEmpty()) {
                    terminalText = appendTerminalStreamOutput(terminalText, pendingOutput)
                }
                awaitingPythonInputBridge = requestedBridge
            }
        }
        return bridge
    }

    fun beginCodeRun(): TerminalRunController {
        val controller = TerminalRunController()
        activeRunController = controller
        isCodeRunning = true
        isTerminalRunning = false
        return controller
    }

    fun beginTerminalRun(): TerminalRunController {
        val controller = TerminalRunController()
        activeRunController = controller
        isCodeRunning = false
        isTerminalRunning = true
        return controller
    }

    fun endCurrentRun(controller: TerminalRunController) {
        if (activeRunController === controller) {
            activeRunController = null
            isCodeRunning = false
            isTerminalRunning = false
        }
    }

    fun stopCurrentRun() {
        activeRunController?.requestStop()
        awaitingPythonInputBridge?.close()
        awaitingPythonInputBridge = null
    }

    fun appendRunResult(result: RunResult, failureText: String): String {
        return buildString {
            append(result.output.trimEnd())
            if (result.output.isNotBlank() && result.error.isNotBlank()) {
                append("\n")
            }
            append(result.error.trimEnd())
            if (!result.success && result.error.isBlank() && !result.stopped) {
                append(failureText)
            }
            if (isNotBlank()) append("\n")
            append(if (result.stopped) "Stopped" else "Finished")
        }
    }

    fun appendShellResult(result: ShellCommandResult): String {
        return buildString {
            append((result.output + result.error).trimEnd())
            if (result.stopped && isBlank()) {
                append("Stopped")
            }
        }
    }

    fun appendTerminalRunResult(result: RunResult, failureText: String): String {
        return buildString {
            append(result.output.trimEnd())
            if (result.output.isNotBlank() && result.error.isNotBlank()) {
                append("\n")
            }
            append(result.error.trimEnd())
            if (!result.success && result.error.isBlank() && !result.stopped) {
                if (isNotBlank()) append("\n")
                append(failureText)
            }
            if (result.stopped && isBlank()) {
                append("Stopped")
            }
        }
    }

    fun runActiveFile() {
        if (isAnyRunActive) return
        when (fullScreenMode) {
            FullScreenMode.Editor -> fullScreenMode = FullScreenMode.Terminal
            null -> terminalRevealRequest += 1L
            FullScreenMode.Terminal -> Unit
        }
        flushWorkspaceFiles()
        val runController = beginCodeRun()
        val stdinText = terminalInput
        val inputBridge = createPythonInputBridge()
        terminalInput = ""
        terminalText = stdinText.trimEnd()
        coroutineScope.launch {
            val result = runPythonCode(
                context = context,
                source = activeFile.content,
                stdin = stdinText,
                filePath = activeFile.filePath,
                workingDirectoryPath = activeFile.parentDirectoryPath(rootDirectory),
                rootDirectoryPath = rootDirectory.absolutePath,
                virtualEnvironmentPath = activeVirtualEnvironment?.absolutePath,
                inputBridge = inputBridge,
                runController = runController
            )
            inputBridge.close()
            if (awaitingPythonInputBridge === inputBridge) {
                awaitingPythonInputBridge = null
            }
            terminalText = appendTerminalOutput(
                terminalText,
                appendRunResult(result, "Process failed")
            )
            endCurrentRun(runController)
        }
    }

    fun submitTerminalCommand(commandOverride: String? = null) {
        awaitingPythonInputBridge?.let { inputBridge ->
            val inputLine = commandOverride ?: terminalInput
            terminalInput = ""
            terminalText = appendTerminalInputEcho(
                current = terminalText,
                input = inputLine,
                inline = inputBridge.echoInline
            )
            awaitingPythonInputBridge = null
            inputBridge.submitInput(inputLine)
            return
        }

        val command = (commandOverride ?: terminalInput).trim()
        if (command.isBlank() || isAnyRunActive) return

        terminalInput = ""
        terminalText = appendTerminalOutput(
            terminalText,
            "$terminalPromptText ${redactTerminalCommandForDisplay(command)}"
        )
        flushWorkspaceFiles()

        val args = splitCommandLine(command)
        val executable = args.firstOrNull()?.lowercase().orEmpty()
        virtualEnvironmentSessionCommand(
            args = args,
            rootDirectory = rootDirectory,
            workingDirectory = terminalDirectory,
            activeEnvironment = activeVirtualEnvironment,
        )?.let { activation ->
            if (activation.error.isNotBlank()) {
                terminalText = appendTerminalOutput(terminalText, activation.error)
            } else {
                activeVirtualEnvironmentPath = activation.environmentPath
                terminalText = appendTerminalOutput(terminalText, activation.output)
            }
            return
        }

        val invokedPipEnvironment = virtualEnvironmentForPipExecutable(
            executable = args.firstOrNull().orEmpty(),
            rootDirectory = rootDirectory,
            workingDirectory = terminalDirectory,
        )
        if (executable == "pip" || executable == "pip3" || invokedPipEnvironment != null) {
            val pipEnvironment = invokedPipEnvironment ?: activeVirtualEnvironment
            if (pipEnvironment == null) {
                terminalText = appendTerminalOutput(
                    terminalText,
                    "pip: no virtual environment is active; run python -m venv .venv and activate it first"
                )
                return
            }
            val runController = beginTerminalRun()
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    executePocketPip(
                        arguments = args.drop(1),
                        environment = pipEnvironment,
                        rootDirectory = rootDirectory,
                        workingDirectory = terminalDirectory,
                        runController = runController,
                    )
                }
                terminalText = appendTerminalOutput(terminalText, appendShellResult(result))
                if (result.workspaceChanged) directoryVersion += 1L
                endCurrentRun(runController)
            }
            return
        }

        val invokedVirtualEnvironment = virtualEnvironmentForPythonExecutable(
            executable = args.firstOrNull().orEmpty(),
            rootDirectory = rootDirectory,
            workingDirectory = terminalDirectory,
        )
        if (executable == "python" || executable == "python3" || executable == "py" || invokedVirtualEnvironment != null) {
            val pythonArgs = args.drop(1)
            val executionVirtualEnvironment = invokedVirtualEnvironment ?: activeVirtualEnvironment
            val scriptArg = pythonArgs.firstOrNull()
            if (scriptArg == "--version" || scriptArg == "-V") {
                val environmentSuffix = executionVirtualEnvironment?.let { " (${it.name})" }.orEmpty()
                terminalText = appendTerminalOutput(terminalText, "Python powered by Chaquopy$environmentSuffix")
                return
            }
            if (scriptArg == "-m") {
                val moduleName = pythonArgs.getOrNull(1)
                if (moduleName == "pip") {
                    if (executionVirtualEnvironment == null) {
                        terminalText = appendTerminalOutput(
                            terminalText,
                            "pip: no virtual environment is active; run python -m venv .venv and activate it first"
                        )
                        return
                    }
                    val runController = beginTerminalRun()
                    coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            executePocketPip(
                                arguments = pythonArgs.drop(2),
                                environment = executionVirtualEnvironment,
                                rootDirectory = rootDirectory,
                                workingDirectory = terminalDirectory,
                                runController = runController,
                            )
                        }
                        terminalText = appendTerminalOutput(terminalText, appendShellResult(result))
                        if (result.workspaceChanged) directoryVersion += 1L
                        endCurrentRun(runController)
                    }
                    return
                }
                if (moduleName == "venv") {
                    val result = createPocketVirtualEnvironment(
                        arguments = pythonArgs.drop(2),
                        rootDirectory = rootDirectory,
                        workingDirectory = terminalDirectory,
                    )
                    terminalText = appendTerminalOutput(terminalText, appendShellResult(result))
                    if (result.workspaceChanged) {
                        directoryVersion += 1L
                        refreshFiles(workingPath = workingDirectoryPath)
                    }
                    return
                }
                if (moduleName != "unittest") {
                    terminalText = appendTerminalOutput(
                        terminalText,
                        "python: supported modules are pip, venv and unittest"
                    )
                    return
                }

                val runController = beginTerminalRun()
                flushWorkspaceFiles()
                coroutineScope.launch {
                    val result = runPythonUnitTests(
                        context = context,
                        arguments = pythonArgs.drop(2),
                        workingDirectoryPath = terminalDirectory.absolutePath,
                        rootDirectoryPath = rootDirectory.absolutePath,
                        virtualEnvironmentPath = executionVirtualEnvironment?.absolutePath,
                        runController = runController
                    )
                    terminalText = appendTerminalOutput(
                        terminalText,
                        appendTerminalRunResult(result, "Tests failed")
                    )
                    endCurrentRun(runController)
                }
                return
            }

            val fileToRun = if (scriptArg.isNullOrBlank()) {
                File(activeFile.filePath)
            } else {
                val targetFile = resolveTerminalPath(rootDirectory, terminalDirectory, scriptArg)
                when {
                    targetFile == null -> {
                        terminalText = appendTerminalOutput(terminalText, "python: $scriptArg: outside sandbox")
                        return
                    }
                    !targetFile.isFile -> {
                        terminalText = appendTerminalOutput(terminalText, "python: $scriptArg: no such file")
                        return
                    }
                    else -> targetFile
                }
            }

            if (!fileToRun.extension.equals("py", ignoreCase = true)) {
                terminalText = appendTerminalOutput(terminalText, "python: ${fileToRun.name}: not a Python file")
                return
            }

            val runController = beginTerminalRun()
            flushWorkspaceFiles()
            val inputBridge = createPythonInputBridge()
            coroutineScope.launch {
                val source = withContext(Dispatchers.IO) {
                    runCatching { fileToRun.readText() }
                }
                    .getOrElse { error ->
                        inputBridge.close()
                        terminalText = appendTerminalOutput(terminalText, error.message ?: "python: could not read ${fileToRun.name}")
                        if (awaitingPythonInputBridge === inputBridge) {
                            awaitingPythonInputBridge = null
                        }
                        endCurrentRun(runController)
                        return@launch
                    }
                val result = runPythonCode(
                    context = context,
                    source = source,
                    stdin = "",
                    filePath = fileToRun.absolutePath,
                    workingDirectoryPath = fileToRun.parentFile?.absolutePath ?: terminalDirectory.absolutePath,
                    rootDirectoryPath = rootDirectory.absolutePath,
                    virtualEnvironmentPath = executionVirtualEnvironment?.absolutePath,
                    inputBridge = inputBridge,
                    runController = runController
                )
                inputBridge.close()
                if (awaitingPythonInputBridge === inputBridge) {
                    awaitingPythonInputBridge = null
                }
                terminalText = appendTerminalOutput(
                    terminalText,
                    appendTerminalRunResult(result, "Process failed")
                )
                endCurrentRun(runController)
            }
            return
        }

        val runController = beginTerminalRun()
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                executeShellCommand(
                    command = command,
                    rootDirectory = rootDirectory,
                    workingDirectory = terminalDirectory,
                    activeEnvironment = activeVirtualEnvironment,
                    runController = runController
                )
            }
            if (result.clearTerminal) {
                terminalText = ""
            } else {
                terminalText = appendTerminalOutput(terminalText, appendShellResult(result))
            }
            result.workingDirectoryPath?.let { terminalDirectoryPath = it }
            if (result.workspaceChanged) {
                directoryVersion += 1L
                refreshFilesInBackground(workingPath = workingDirectoryPath)
            }
            endCurrentRun(runController)
        }
    }

    fun saveCurrentSession(
        flushFiles: Boolean = false,
    ) {
        if (flushFiles) {
            flushWorkspaceFiles()
        }
        val selectedFile = files.firstOrNull { it.id == activeFileId } ?: files.firstOrNull()
        saveIdeSession(
            context = context,
            session = IdeSession(
                activeFileId = selectedFile?.id ?: activeFileId,
                activeFilePath = selectedFile?.filePath,
                workingDirectoryPath = workingDirectory.absolutePath,
                terminalDirectoryPath = terminalDirectory.absolutePath,
                expandedFolderPaths = normalizedWorkspaceDirectoryPaths(rootDirectory, expandedFolderPaths),
                explorerVisible = explorerVisible,
                fileSearch = fileSearch,
                terminalInput = "",
                terminalText = "",
                fullScreenMode = fullScreenMode
            )
        )
    }

    val expandedFoldersKey = expandedFolderPaths.joinToString("\u001F")
    LaunchedEffect(
        activeFileId,
        workingDirectoryPath,
        terminalDirectoryPath,
        expandedFoldersKey,
        explorerVisible,
        fileSearch,
        fullScreenMode
    ) {
        saveCurrentSession()
    }

    val latestSaveSession by rememberUpdatedState(
        newValue = { saveCurrentSession(flushFiles = true) }
    )
    DisposableEffect(lifecycle) {
        if (lifecycle == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    latestSaveSession()
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                activeRunController?.requestStop()
                awaitingPythonInputBridge?.close()
                latestSaveSession()
                lifecycle.removeObserver(observer)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(palette.backgroundTop, palette.backgroundBottom)
                )
            )
            .systemBarsPadding()
    ) {
        when (fullScreenMode) {
            FullScreenMode.Editor -> EditorFullScreen(
                files = tabFiles,
                activeFile = activeFile,
                palette = palette,
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                isRunning = isCodeRunning,
                rootDirectory = rootDirectory,
                workingDirectory = workingDirectory,
                activeVirtualEnvironment = activeVirtualEnvironment,
                onToggleTheme = onToggleTheme,
                onBack = { fullScreenMode = null },
                onSelectFile = ::selectFile,
                onAddFile = { newFileDialogOpen = true },
                onDeleteFile = { fileId ->
                    deleteTarget = files.firstOrNull { it.id == fileId }
                },
                onCodeChange = ::updateActiveContent,
                onRunCode = ::runActiveFile,
            )

            FullScreenMode.Terminal -> TerminalFullScreen(
                palette = palette,
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                terminalText = terminalText,
                terminalInput = terminalInput,
                promptText = terminalPromptText,
                isRunning = isAnyRunActive,
                keepInputVisibleWhileRunning = isTerminalRunning,
                isAwaitingInput = awaitingPythonInputBridge != null,
                onTerminalInputChange = { terminalInput = it },
                onTerminalCommandSubmit = { submitTerminalCommand() },
                onStopRun = ::stopCurrentRun,
                onToggleTheme = onToggleTheme,
                onBack = { fullScreenMode = null },
            )

            null -> IdeHomeScreen(
                files = files,
                tabFiles = tabFiles,
                activeFile = activeFile,
                activeFileId = activeFileId,
                projectName = rootDirectory.name,
                rootDirectory = rootDirectory,
                workingDirectory = workingDirectory,
                activeVirtualEnvironment = activeVirtualEnvironment,
                expandedFolderPaths = expandedFolderPaths,
                directoryVersion = directoryVersion,
                palette = palette,
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                explorerVisible = explorerVisible,
                fileSearch = fileSearch,
                terminalText = terminalText,
                terminalInput = terminalInput,
                promptText = terminalPromptText,
                isCodeRunning = isCodeRunning,
                isTerminalRunning = isAnyRunActive,
                keepTerminalInputVisibleWhileRunning = isTerminalRunning,
                isAwaitingInput = awaitingPythonInputBridge != null,
                terminalRevealRequest = terminalRevealRequest,
                onToggleTheme = onToggleTheme,
                onImportClick = { importOptionsOpen = true },
                onProjectsClick = onBackToProjects,
                onToggleExplorer = { explorerVisible = !explorerVisible },
                onFileSearchChange = { fileSearch = it },
                onSelectFile = ::selectFile,
                onSelectFolder = ::selectFolder,
                onToggleFolder = { folder ->
                    if (expandedFolderPaths.contains(folder.path)) {
                        expandedFolderPaths.remove(folder.path)
                    } else {
                        expandedFolderPaths.addUniquePath(folder.path)
                    }
                    selectFolder(folder)
                },
                onAddFile = { newFileDialogOpen = true },
                onAddFolder = { newFolderDialogOpen = true },
                onCreateFileInFolder = { folder ->
                    selectFolder(folder)
                    newFileDialogOpen = true
                },
                onCreateFolderInFolder = { folder ->
                    selectFolder(folder)
                    newFolderDialogOpen = true
                },
                onDeleteFile = { fileId ->
                    deleteTarget = files.firstOrNull { it.id == fileId }
                },
                onRenameFile = { renameTarget = it },
                onRenameFolder = { renameFolderTarget = it },
                onDeleteFolder = { deleteFolderTarget = it },
                onCodeChange = ::updateActiveContent,
                onRunCode = ::runActiveFile,
                onTerminalInputChange = { terminalInput = it },
                onTerminalCommandSubmit = { submitTerminalCommand() },
                onStopRun = ::stopCurrentRun,
                onFullScreen = { fullScreenMode = it },
            )
        }

        if (importOptionsOpen) {
            ImportOptionsDialog(
                palette = palette,
                currentDirectoryName = relativeDirectoryLabel(rootDirectory, workingDirectory),
                onDismiss = { importOptionsOpen = false },
                onOpenFile = {
                    importOptionsOpen = false
                    openFileLauncher.launch(openDocumentIntent())
                },
                onOpenFolder = {
                    importOptionsOpen = false
                    openFolderLauncher.launch(openDocumentTreeIntent())
                },
                onSaveFolder = {
                    flushWorkspaceFiles()
                    importOptionsOpen = false
                    saveFolderLauncher.launch(openDocumentTreeIntent())
                }
            )
        }

        storageMessage?.let { result ->
            StorageMessageDialog(
                result = result,
                onDismiss = { storageMessage = null }
            )
        }

        if (newFileDialogOpen) {
            NewFileDialog(
                palette = palette,
                initialName = nextUntitledFileName(tabFiles),
                currentDirectoryName = relativeDirectoryLabel(rootDirectory, workingDirectory),
                onDismiss = { newFileDialogOpen = false },
                onCreate = { name ->
                    val error = createFile(name)
                    if (error == null) {
                        newFileDialogOpen = false
                    }
                    error
                }
            )
        }

        if (newFolderDialogOpen) {
            NewFolderDialog(
                palette = palette,
                currentDirectoryName = relativeDirectoryLabel(rootDirectory, workingDirectory),
                onDismiss = { newFolderDialogOpen = false },
                onCreate = { name ->
                    val error = createFolder(name)
                    if (error == null) {
                        newFolderDialogOpen = false
                    }
                    error
                }
            )
        }

        renameTarget?.let { file ->
            RenameFileDialog(
                file = file,
                palette = palette,
                onDismiss = { renameTarget = null },
                onRename = { name ->
                    val error = renameFile(file, name)
                    if (error == null) {
                        renameTarget = null
                    }
                    error
                }
            )
        }

        renameFolderTarget?.let { folder ->
            RenameFolderDialog(
                folder = folder,
                palette = palette,
                onDismiss = { renameFolderTarget = null },
                onRename = { name ->
                    val error = renameFolder(folder, name)
                    if (error == null) {
                        renameFolderTarget = null
                    }
                    error
                }
            )
        }

        deleteTarget?.let { file ->
            ConfirmDeleteFileDialog(
                file = file,
                palette = palette,
                onDismiss = { deleteTarget = null },
                onDelete = {
                    deleteFile(file.id)
                    deleteTarget = null
                }
            )
        }

        deleteFolderTarget?.let { folder ->
            ConfirmDeleteFolderDialog(
                folder = folder,
                palette = palette,
                onDismiss = { deleteFolderTarget = null },
                onDelete = {
                    deleteFolder(folder)
                    deleteFolderTarget = null
                }
            )
        }
    }
}

@Composable
private fun IdeHomeScreen(
    files: List<PythonFile>,
    tabFiles: List<PythonFile>,
    activeFile: PythonFile,
    activeFileId: Long,
    projectName: String,
    rootDirectory: File,
    workingDirectory: File,
    activeVirtualEnvironment: File?,
    expandedFolderPaths: List<String>,
    directoryVersion: Long,
    palette: IdePalette,
    darkMode: Boolean,
    nextThemeName: String,
    explorerVisible: Boolean,
    fileSearch: String,
    terminalText: String,
    terminalInput: String,
    promptText: String,
    isCodeRunning: Boolean,
    isTerminalRunning: Boolean,
    keepTerminalInputVisibleWhileRunning: Boolean,
    isAwaitingInput: Boolean,
    terminalRevealRequest: Long,
    onToggleTheme: () -> Unit,
    onImportClick: () -> Unit,
    onProjectsClick: () -> Unit,
    onToggleExplorer: () -> Unit,
    onFileSearchChange: (String) -> Unit,
    onSelectFile: (Long) -> Unit,
    onSelectFolder: (FolderTarget) -> Unit,
    onToggleFolder: (FolderTarget) -> Unit,
    onAddFile: () -> Unit,
    onAddFolder: () -> Unit,
    onCreateFileInFolder: (FolderTarget) -> Unit,
    onCreateFolderInFolder: (FolderTarget) -> Unit,
    onDeleteFile: (Long) -> Unit,
    onRenameFile: (PythonFile) -> Unit,
    onRenameFolder: (FolderTarget) -> Unit,
    onDeleteFolder: (FolderTarget) -> Unit,
    onCodeChange: (String) -> Unit,
    onRunCode: () -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onTerminalCommandSubmit: () -> Unit,
    onStopRun: () -> Unit,
    onFullScreen: (FullScreenMode) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 14.dp)
    ) {
        val pageScroll = rememberScrollState()
        LaunchedEffect(terminalRevealRequest) {
            if (terminalRevealRequest > 0L) {
                delay(80)
                pageScroll.animateScrollTo(pageScroll.maxValue)
            }
        }
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
                .verticalScroll(pageScroll)
        ) {
            Spacer(Modifier.height(8.dp))
            IdeHeader(
                projectName = projectName,
                palette = palette,
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                onToggleTheme = onToggleTheme,
                onImportClick = onImportClick,
                onProjectsClick = onProjectsClick,
            )
            Spacer(Modifier.height(10.dp))
            FileTabsBar(
                files = tabFiles,
                activeFileId = activeFileId,
                palette = palette,
                onSelectFile = onSelectFile,
                onAddFile = onAddFile,
                onDeleteFile = onDeleteFile,
                onToggleExplorer = onToggleExplorer,
            )
            Spacer(Modifier.height(10.dp))

            if (explorerVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExplorerPanel(
                        files = files,
                        rootDirectory = rootDirectory,
                        workingDirectory = workingDirectory,
                        expandedFolderPaths = expandedFolderPaths,
                        directoryVersion = directoryVersion,
                        activeFileId = activeFileId,
                        query = fileSearch,
                        palette = palette,
                        height = editorHeight,
                        modifier = Modifier.weight(explorerFraction),
                        onSearchChange = onFileSearchChange,
                        onSelectFile = onSelectFile,
                        onSelectFolder = onSelectFolder,
                        onToggleFolder = onToggleFolder,
                        onAddFile = onAddFile,
                        onAddFolder = onAddFolder,
                        onCreateFileInFolder = onCreateFileInFolder,
                        onCreateFolderInFolder = onCreateFolderInFolder,
                        onDeleteFile = onDeleteFile,
                        onRenameFile = onRenameFile,
                        onRenameFolder = onRenameFolder,
                        onDeleteFolder = onDeleteFolder,
                    )
                    ResizeHandle(
                        palette = palette,
                        height = editorHeight,
                        onToggle = {
                            explorerFraction = if (explorerFraction > 0.50f) 0.35f else 0.62f
                        },
                        onDrag = { dragAmount ->
                            explorerFraction = (explorerFraction + (dragAmount / layoutWidthPx))
                                .coerceIn(0.28f, 0.72f)
                        }
                    )

                    CodeEditorPanel(
                        file = activeFile,
                        palette = palette,
                        modifier = Modifier.weight(1f - explorerFraction),
                        height = editorHeight,
                        compact = true,
                        rootDirectory = rootDirectory,
                        workingDirectory = workingDirectory,
                        activeVirtualEnvironment = activeVirtualEnvironment,
                        onCodeChange = onCodeChange,
                        onFullScreen = { onFullScreen(FullScreenMode.Editor) },
                    )
                }
            } else {
                CodeEditorPanel(
                    file = activeFile,
                    palette = palette,
                    modifier = Modifier.fillMaxWidth(),
                    height = editorHeight,
                    compact = false,
                    rootDirectory = rootDirectory,
                    workingDirectory = workingDirectory,
                    activeVirtualEnvironment = activeVirtualEnvironment,
                    onCodeChange = onCodeChange,
                    onFullScreen = { onFullScreen(FullScreenMode.Editor) },
                )
            }

            Spacer(Modifier.height(12.dp))
            RunButton(
                palette = palette,
                isRunning = isCodeRunning,
                onRunCode = onRunCode,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            TerminalPanel(
                palette = palette,
                terminalText = terminalText,
                terminalInput = terminalInput,
                promptText = promptText,
                height = terminalHeight,
                isRunning = isTerminalRunning,
                keepInputVisibleWhileRunning = keepTerminalInputVisibleWhileRunning,
                isAwaitingInput = isAwaitingInput,
                onTerminalInputChange = onTerminalInputChange,
                onTerminalCommandSubmit = onTerminalCommandSubmit,
                onStopRun = onStopRun,
                onFullScreen = { onFullScreen(FullScreenMode.Terminal) },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EditorFullScreen(
    files: List<PythonFile>,
    activeFile: PythonFile,
    palette: IdePalette,
    darkMode: Boolean,
    nextThemeName: String,
    isRunning: Boolean,
    rootDirectory: File,
    workingDirectory: File,
    activeVirtualEnvironment: File?,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onSelectFile: (Long) -> Unit,
    onAddFile: () -> Unit,
    onDeleteFile: (Long) -> Unit,
    onCodeChange: (String) -> Unit,
    onRunCode: () -> Unit,
) {
    val density = LocalDensity.current
    val keyboardVisible = WindowInsets.ime.getBottom(density) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 14.dp)
            .padding(bottom = 10.dp)
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
        CodeEditorPanel(
            file = activeFile,
            palette = palette,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            height = Dp.Unspecified,
            compact = false,
            rootDirectory = rootDirectory,
            workingDirectory = workingDirectory,
            activeVirtualEnvironment = activeVirtualEnvironment,
            showKeyboardAccessory = true,
            darkMode = darkMode,
            nextThemeName = nextThemeName,
            onToggleTheme = onToggleTheme,
            onCodeChange = onCodeChange,
            onFullScreen = onBack,
        )
        if (!keyboardVisible) {
            Spacer(Modifier.height(12.dp))
            RunButton(
                palette = palette,
                isRunning = isRunning,
                onRunCode = onRunCode,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TerminalFullScreen(
    palette: IdePalette,
    darkMode: Boolean,
    nextThemeName: String,
    terminalText: String,
    terminalInput: String,
    promptText: String,
    isRunning: Boolean,
    keepInputVisibleWhileRunning: Boolean,
    isAwaitingInput: Boolean,
    onTerminalInputChange: (String) -> Unit,
    onTerminalCommandSubmit: () -> Unit,
    onStopRun: () -> Unit,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 8.dp)
            .padding(bottom = 6.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        TerminalPanel(
            palette = palette,
            terminalText = terminalText,
            terminalInput = terminalInput,
            promptText = promptText,
            height = Dp.Unspecified,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            showHeader = true,
            darkMode = darkMode,
            nextThemeName = nextThemeName,
            onToggleTheme = onToggleTheme,
            isRunning = isRunning,
            keepInputVisibleWhileRunning = keepInputVisibleWhileRunning,
            isAwaitingInput = isAwaitingInput,
            onTerminalInputChange = onTerminalInputChange,
            onTerminalCommandSubmit = onTerminalCommandSubmit,
            onStopRun = onStopRun,
            onFullScreen = onBack,
        )
    }
}

@Composable
private fun IdeHeader(
    projectName: String,
    palette: IdePalette,
    darkMode: Boolean,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    onImportClick: () -> Unit,
    onProjectsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onProjectsClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(palette.elevatedPanel)
                .border(BorderStroke(1.dp, palette.border), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = "Back to projects",
                tint = palette.text,
                modifier = Modifier.size(21.dp)
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
        IconButton(
            onClick = onImportClick,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Code,
                contentDescription = "Import or save files and folders",
                tint = palette.text,
                modifier = Modifier.size(21.dp),
            )
        }
        ThemeCycleButton(
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
internal fun ThemeCycleButton(
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
private fun FileTabsBar(
    files: List<PythonFile>,
    activeFileId: Long,
    palette: IdePalette,
    onSelectFile: (Long) -> Unit,
    onAddFile: () -> Unit,
    onDeleteFile: (Long) -> Unit,
    onToggleExplorer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(palette.elevatedPanel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(15.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            files.forEach { file ->
                FileTab(
                    file = file,
                    selected = file.id == activeFileId,
                    palette = palette,
                    onSelect = { onSelectFile(file.id) },
                    onClose = { onDeleteFile(file.id) }
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        AddFileButton(
            palette = palette,
            onAddFile = onAddFile
        )
        Spacer(Modifier.width(4.dp))
        IconButton(
            onClick = onToggleExplorer,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Explorer",
                tint = palette.text,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun FileTabsOnlyBar(
    files: List<PythonFile>,
    activeFileId: Long,
    palette: IdePalette,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        files.forEach { file ->
            FileTab(
                file = file,
                selected = file.id == activeFileId,
                palette = palette,
                onSelect = { onSelectFile(file.id) },
                onClose = { onDeleteFile(file.id) }
            )
        }
        AddFileButton(
            palette = palette,
            onAddFile = onAddFile
        )
    }
}

@Composable
private fun FileTab(
    file: PythonFile,
    selected: Boolean,
    palette: IdePalette,
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
                RoundedCornerShape(11.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Description,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = file.name,
            color = palette.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Delete file",
                tint = palette.mutedText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddFileButton(
    palette: IdePalette,
    onAddFile: () -> Unit,
) {
    IconButton(
        onClick = onAddFile,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(palette.tab)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(11.dp))
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "New file",
            tint = palette.text,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun ResizeHandle(
    palette: IdePalette,
    height: Dp,
    onToggle: () -> Unit,
    onDrag: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(16.dp)
            .height(height)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    onDrag(dragAmount)
                }
            }
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "||",
            color = palette.mutedText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExplorerPanel(
    files: List<PythonFile>,
    rootDirectory: File,
    workingDirectory: File,
    expandedFolderPaths: List<String>,
    directoryVersion: Long,
    activeFileId: Long,
    query: String,
    palette: IdePalette,
    height: Dp,
    modifier: Modifier = Modifier,
    onSearchChange: (String) -> Unit,
    onSelectFile: (Long) -> Unit,
    onSelectFolder: (FolderTarget) -> Unit,
    onToggleFolder: (FolderTarget) -> Unit,
    onAddFile: () -> Unit,
    onAddFolder: () -> Unit,
    onCreateFileInFolder: (FolderTarget) -> Unit,
    onCreateFolderInFolder: (FolderTarget) -> Unit,
    onDeleteFile: (Long) -> Unit,
    onRenameFile: (PythonFile) -> Unit,
    onRenameFolder: (FolderTarget) -> Unit,
    onDeleteFolder: (FolderTarget) -> Unit,
) {
    var addMenuOpen by remember { mutableStateOf(false) }

    Panel(
        palette = palette,
        modifier = modifier.height(height)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "EXPLORER",
                color = palette.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = relativeDirectoryLabel(rootDirectory, workingDirectory),
                color = palette.subtleText,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(
                    onClick = { addMenuOpen = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New item",
                        tint = palette.text
                    )
                }
                DropdownMenu(
                    expanded = addMenuOpen,
                    onDismissRequest = { addMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Create file") },
                        leadingIcon = { Icon(Icons.Rounded.Description, null) },
                        onClick = {
                            addMenuOpen = false
                            onAddFile()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Create folder") },
                        leadingIcon = { Icon(Icons.Rounded.Folder, null) },
                        onClick = {
                            addMenuOpen = false
                            onAddFolder()
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SearchBox(
            value = query,
            palette = palette,
            onValueChange = onSearchChange,
        )
        Spacer(Modifier.height(10.dp))

        val entries = remember(
            directoryVersion,
            expandedFolderPaths.joinToString("|"),
            query,
        ) {
            buildExplorerEntries(
                rootDirectory = rootDirectory,
                files = files,
                expandedFolderPaths = expandedFolderPaths.toSet(),
                query = query
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
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
                        onCreateFile = { onCreateFileInFolder(entry.target) },
                        onCreateFolder = { onCreateFolderInFolder(entry.target) },
                        onRename = { onRenameFolder(entry.target) },
                        onDelete = { onDeleteFolder(entry.target) },
                    )

                    is ExplorerEntry.PythonSourceFile -> ExplorerFileRow(
                        file = entry.file,
                        depth = entry.depth,
                        selected = entry.file.id == activeFileId,
                        palette = palette,
                        onSelect = { onSelectFile(entry.file.id) },
                        onDelete = { onDeleteFile(entry.file.id) },
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
    palette: IdePalette,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = palette.text,
            fontSize = 13.sp
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = palette.subtleText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(7.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = "Search files...",
                            color = palette.subtleText,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun ExplorerFolderRow(
    folder: FolderTarget,
    depth: Int,
    expanded: Boolean,
    selected: Boolean,
    palette: IdePalette,
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onSelect()
                onToggle()
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = if (expanded) "Collapse folder" else "Expand folder",
                tint = palette.mutedText,
                modifier = Modifier.size(17.dp)
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Rounded.FolderOpen else Icons.Rounded.Folder,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = folder.name,
            color = palette.text,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "Folder options",
                    tint = palette.text,
                    modifier = Modifier.size(17.dp)
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Create file") },
                    leadingIcon = { Icon(Icons.Rounded.Description, null) },
                    onClick = {
                        menuOpen = false
                        onCreateFile()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Create folder") },
                    leadingIcon = { Icon(Icons.Rounded.Folder, null) },
                    onClick = {
                        menuOpen = false
                        onCreateFolder()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                    onClick = {
                        menuOpen = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun ExplorerFileRow(
    file: PythonFile,
    depth: Int,
    selected: Boolean,
    palette: IdePalette,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Description,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = file.name,
            color = palette.text,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "File options",
                    tint = palette.text,
                    modifier = Modifier.size(17.dp)
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                    onClick = {
                        menuOpen = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun CodeEditorPanel(
    file: PythonFile,
    palette: IdePalette,
    height: Dp,
    compact: Boolean,
    rootDirectory: File,
    workingDirectory: File,
    activeVirtualEnvironment: File?,
    modifier: Modifier = Modifier,
    showKeyboardAccessory: Boolean = false,
    darkMode: Boolean? = null,
    nextThemeName: String = "",
    onToggleTheme: (() -> Unit)? = null,
    onCodeChange: (String) -> Unit,
    onFullScreen: () -> Unit,
) {
    val panelModifier = if (height == Dp.Unspecified) {
        modifier
    } else {
        modifier.height(height)
    }

    Panel(
        palette = palette,
        modifier = panelModifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Description,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = if (compact) "Code\nEditor -\nPython" else "Code Editor - Python",
                color = palette.text,
                fontSize = if (compact) 16.sp else 18.sp,
                lineHeight = 21.sp,
                maxLines = if (compact) 3 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (darkMode != null && onToggleTheme != null) {
                ThemeCycleButton(
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
            IconButton(
                onClick = onFullScreen,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = "Fullscreen editor",
                    tint = palette.text,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalRule(palette)
        Spacer(Modifier.height(7.dp))
        CodeEditorBody(
            code = file.content,
            filePath = file.filePath,
            palette = palette,
            rootDirectory = rootDirectory,
            workingDirectory = workingDirectory,
            activeVirtualEnvironment = activeVirtualEnvironment,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            showKeyboardAccessory = showKeyboardAccessory,
            onCodeChange = onCodeChange,
        )
    }
}

@Composable
private fun CodeEditorBody(
    code: String,
    filePath: String,
    palette: IdePalette,
    rootDirectory: File,
    workingDirectory: File,
    activeVirtualEnvironment: File?,
    modifier: Modifier = Modifier,
    showKeyboardAccessory: Boolean = false,
    onCodeChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val editorVerticalScroll = rememberScrollState()
    val editorHorizontalScroll = rememberScrollState()
    var editorValue by remember(filePath) {
        mutableStateOf(TextFieldValue(code, TextRange(code.length)))
    }
    var completionsVisible by remember { mutableStateOf(false) }
    var suppressCompletionsForText by remember { mutableStateOf<String?>(null) }
    var completionInteractionVersion by remember { mutableLongStateOf(0L) }
    var diagnostics by remember { mutableStateOf<List<CodeDiagnostic>>(emptyList()) }
    val lineCount = remember(editorValue.text) {
        (editorValue.text.count { it == '\n' } + 1).coerceAtLeast(1)
    }
    val lineNumbers = remember(lineCount) {
        (1..lineCount).joinToString("\n")
    }
    val gutterWidth = remember(lineCount) {
        maxOf(20.dp, (lineCount.toString().length * 8 + 10).dp)
    }
    val completionContext = remember(editorValue.text, editorValue.selection) {
        completionContextAt(editorValue.text, editorValue.selection.start)
    }
    val completions = remember(editorValue.text, completionContext, activeVirtualEnvironment) {
        completionContext?.let {
            val lineOffsets = sourceLineOffsets(editorValue.text)
            val cursor = editorValue.selection.start
            val line = lineOffsets.indexOfLast { it <= cursor }.coerceAtLeast(0) + 1
            val column = cursor - lineOffsets.getOrElse(line - 1) { 0 }

            pythonCompletionsForContext(
                context = context,
                source = editorValue.text,
                line = line,
                column = column,
                filePath = filePath,
                rootDirectory = rootDirectory,
                workingDirectory = workingDirectory,
                virtualEnvironment = activeVirtualEnvironment
            )
        }.orEmpty()
    }
    LaunchedEffect(
        editorValue.text,
        editorValue.selection,
        completions.size,
        completionsVisible,
        completionInteractionVersion,
    ) {
        if (completions.isEmpty() || completionContext == null) {
            completionsVisible = false
        } else if (completionsVisible) {
            delay(
                if (completionInteractionVersion == 0L) {
                    COMPLETION_IDLE_TIMEOUT_MS
                } else {
                    COMPLETION_INTERACTION_TIMEOUT_MS
                }
            )
            completionsVisible = false
        }
    }
    LaunchedEffect(editorValue.text, filePath) {
        val snapshot = editorValue.text
        delay(350)
        diagnostics = runPythonInspection(
            context = context,
            source = snapshot,
            filePath = filePath
        )
    }
    val codeStyle = TextStyle(
        color = palette.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val maxLineCharacters = remember(editorValue.text) {
        editorValue.text
            .lineSequence()
            .maxOfOrNull { it.length }
            ?.coerceAtLeast(48)
            ?: 48
    }
    val editorContentWidth = maxOf(640.dp, (maxLineCharacters * 9).dp)
    val density = LocalDensity.current
    val showQuickKeys = showKeyboardAccessory && WindowInsets.ime.getBottom(density) > 0
    val signatureHelp = remember(editorValue.text, editorValue.selection) {
        pythonSignatureHelpAt(editorValue.text, editorValue.selection.start)
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
                    .fillMaxHeight()
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
                        .padding(end = 3.dp)
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(palette.strongBorder)
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(editorHorizontalScroll)
            ) {
                Box(
                    modifier = Modifier
                        .width(editorContentWidth)
                        .fillMaxHeight()
                        .heightIn(min = 240.dp)
                        .verticalScroll(editorVerticalScroll)
                ) {
                    if (editorValue.text.isBlank()) {
                        Text(
                            text = "Write your code here",
                            color = palette.subtleText,
                            style = codeStyle
                        )
                    }
                    BasicTextField(
                        value = editorValue,
                        onValueChange = { requestedValue ->
                            val previousValue = editorValue
                            val textChanged = requestedValue.text != previousValue.text
                            val updatedValue = applySmartEditorChange(previousValue, requestedValue)
                            editorValue = updatedValue
                            if (textChanged) {
                                suppressCompletionsForText = null
                                completionInteractionVersion = 0L
                                completionsVisible = true
                            } else if (requestedValue.selection != previousValue.selection) {
                                completionsVisible = false
                            }
                            if (updatedValue.text != code) {
                                onCodeChange(updatedValue.text)
                            }
                        },
                        textStyle = codeStyle,
                        cursorBrush = SolidColor(palette.accent),
                        visualTransformation = PythonSyntaxTransformation(palette, diagnostics),
                        modifier = Modifier.fillMaxSize()
                    )
                    signatureHelp?.let { signature ->
                        SignatureHelpChip(
                            signature = signature,
                            palette = palette,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 6.dp, bottom = 6.dp)
                        )
                    }
                    if (
                        completionsVisible &&
                        completions.isNotEmpty() &&
                        completionContext != null &&
                        suppressCompletionsForText != editorValue.text
                    ) {
                        CompletionPopup(
                            completions = completions,
                            palette = palette,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 28.dp, start = 2.dp),
                            onSelect = { item ->
                                val inserted = insertCompletion(editorValue, completionContext, item)
                                editorValue = inserted
                                suppressCompletionsForText = inserted.text
                                completionInteractionVersion = 0L
                                completionsVisible = false
                                onCodeChange(inserted.text)
                            },
                            onInteraction = { completionInteractionVersion += 1L },
                        )
                    }
                }
            }
        }
        if (showQuickKeys) {
            Spacer(Modifier.height(6.dp))
            EditorQuickKeys(
                palette = palette,
                editorValue = editorValue,
                onEditorValueChange = { updatedValue ->
                    editorValue = updatedValue
                    suppressCompletionsForText = updatedValue.text
                    completionsVisible = false
                    onCodeChange(updatedValue.text)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (diagnostics.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            InspectionStrip(
                diagnostics = diagnostics,
                palette = palette,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SignatureHelpChip(
    signature: String,
    palette: IdePalette,
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
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

@Composable
private fun InspectionStrip(
    diagnostics: List<CodeDiagnostic>,
    palette: IdePalette,
    modifier: Modifier = Modifier,
) {
    val sortedDiagnostics = remember(diagnostics) {
        diagnostics.sortedWith(
            compareBy<CodeDiagnostic> { diagnosticSeverityRank(it.severity) }
                .thenBy { it.line }
                .thenBy { it.start }
        )
    }
    val inspectionScroll = rememberScrollState()
    val highestSeverity = sortedDiagnostics.firstOrNull()?.severity ?: "hint"
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.elevatedPanel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier
                .heightIn(max = 39.dp)
                .verticalScroll(inspectionScroll)
        ) {
            sortedDiagnostics.forEach { diagnostic ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(diagnosticColor(diagnostic.severity, palette))
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "L${diagnostic.line}: ${diagnostic.message}",
                        color = if (diagnostic.severity == highestSeverity) palette.text else palette.mutedText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionPopup(
    completions: List<CompletionItem>,
    palette: IdePalette,
    modifier: Modifier = Modifier,
    onSelect: (CompletionItem) -> Unit,
    onInteraction: () -> Unit,
) {
    val completionKey = completions.joinToString("|") { it.completionKey() }
    var detailItemKey by remember(completionKey) { mutableStateOf<String?>(null) }
    val detailItem = completions.firstOrNull { it.completionKey() == detailItemKey }
    val popupScroll = rememberScrollState()

    Column(
        modifier = modifier
            .width(270.dp)
            .heightIn(max = 320.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.elevatedPanel)
            .border(BorderStroke(1.dp, palette.strongBorder), RoundedCornerShape(12.dp))
            .pointerInput(onInteraction) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed != it.previousPressed }) {
                            onInteraction()
                        }
                    }
                }
            }
            .verticalScroll(popupScroll)
            .padding(vertical = 4.dp)
    ) {
        completions.take(8).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item) }
                    .padding(start = 10.dp, end = 2.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    color = palette.text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.detail,
                    color = palette.subtleText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier.width(72.dp)
                )
                IconButton(
                    onClick = {
                        detailItemKey = if (detailItemKey == item.completionKey()) {
                            null
                        } else {
                            item.completionKey()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = "Show completion details",
                        tint = palette.subtleText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        detailItem?.let { item ->
            CompletionDetailCard(
                item = item,
                palette = palette,
                modifier = Modifier
                    .padding(horizontal = 7.dp, vertical = 5.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CompletionDetailCard(
    item: CompletionItem,
    palette: IdePalette,
    modifier: Modifier = Modifier,
) {
    val syntax = completionSyntax(item)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.panel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(10.dp))
            .heightIn(max = 250.dp)
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = item.label,
            color = palette.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "Kind: ${item.detail}",
            color = palette.subtleText,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = completionExplanation(item),
            color = palette.mutedText,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        syntax?.let { value ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Syntax:",
                color = palette.subtleText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = palette.text,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
        completionExample(item)?.let { example ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Example:",
                color = palette.subtleText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = example,
                color = palette.text,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun EditorQuickKeys(
    palette: IdePalette,
    editorValue: TextFieldValue,
    modifier: Modifier = Modifier,
    onEditorValueChange: (TextFieldValue) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        QuickKey("Tab", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, "    "))
        }
        QuickKey("()", palette) {
            onEditorValueChange(wrapOrInsertPair(editorValue, "(", ")"))
        }
        QuickKey("\"\"", palette) {
            onEditorValueChange(wrapOrInsertPair(editorValue, "\"", "\""))
        }
        QuickKey("''", palette) {
            onEditorValueChange(wrapOrInsertPair(editorValue, "'", "'"))
        }
        QuickKey(":", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, ":"))
        }
        QuickKey("#", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, "# "))
        }
        QuickKey("+", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, "+"))
        }
        QuickKey("-", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, "-"))
        }
        QuickKey("=", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, "="))
        }
        QuickKey("*", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, "*"))
        }
        QuickKey("/", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, "/"))
        }
        QuickKey("\\", palette) {
            onEditorValueChange(insertCodeAtSelection(editorValue, "\\"))
        }
    }
}

@Composable
private fun QuickKey(
    label: String,
    palette: IdePalette,
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
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = palette.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun RunButton(
    palette: IdePalette,
    isRunning: Boolean,
    onRunCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onRunCode,
        enabled = !isRunning,
        border = BorderStroke(0.dp, Color.Transparent),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.run,
            contentColor = palette.runText,
            disabledContainerColor = palette.run.copy(alpha = 0.55f),
            disabledContentColor = palette.runText.copy(alpha = 0.75f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(54.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = if (isRunning) "Running" else "Run Code",
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TerminalPanel(
    palette: IdePalette,
    terminalText: String,
    terminalInput: String,
    promptText: String,
    height: Dp,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    darkMode: Boolean? = null,
    nextThemeName: String = "",
    onToggleTheme: (() -> Unit)? = null,
    isRunning: Boolean,
    keepInputVisibleWhileRunning: Boolean,
    isAwaitingInput: Boolean,
    onTerminalInputChange: (String) -> Unit,
    onTerminalCommandSubmit: () -> Unit,
    onStopRun: () -> Unit,
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

    val panelModifier = if (height == Dp.Unspecified) {
        modifier
    } else {
        modifier.height(height)
    }.bringIntoViewRequester(bringIntoViewRequester)

    Panel(
        palette = palette,
        modifier = panelModifier
    ) {
        if (showHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terminal Output",
                    color = palette.text,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                if (isRunning) {
                    IconButton(
                        onClick = onStopRun,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "Stop terminal process",
                            tint = palette.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                if (darkMode != null && onToggleTheme != null) {
                    ThemeCycleButton(
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
                IconButton(
                    onClick = onFullScreen,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = "Fullscreen terminal",
                        tint = palette.text,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
            HorizontalRule(palette)
            Spacer(Modifier.height(7.dp))
        } else {
            HorizontalRule(palette)
            Spacer(Modifier.height(10.dp))
        }

        TerminalBody(
            terminalText = terminalText,
            terminalInput = terminalInput,
            promptText = promptText,
            palette = palette,
            isRunning = isRunning,
            keepInputVisibleWhileRunning = keepInputVisibleWhileRunning,
            isAwaitingInput = isAwaitingInput,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onTerminalFocusChange = { focused ->
                terminalFocused = focused
                if (focused) {
                    bringTerminalIntoView()
                }
            },
            onTerminalInputChange = onTerminalInputChange,
            onTerminalCommandSubmit = onTerminalCommandSubmit,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TerminalBody(
    terminalText: String,
    terminalInput: String,
    promptText: String,
    palette: IdePalette,
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
    val activeInputLineStart = if (isAwaitingInput) terminalText.lastIndexOf('\n') else -1
    val rawDisplayedTerminalText = if (isAwaitingInput) {
        when {
            terminalText.isBlank() -> ""
            activeInputLineStart >= 0 -> terminalText.substring(0, activeInputLineStart)
            else -> ""
        }
    } else {
        terminalText
    }
    val activeInputPrefix = if (isAwaitingInput) {
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
    val displayedTerminalText = terminalDisplayText(
        text = rawDisplayedTerminalText,
        promptText = promptText,
        keepLastPrompt = isAwaitingInput
    )
    val terminalTextStyle = TextStyle(
        color = palette.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
    val terminalInputTextStyle = terminalTextStyle.copy(lineHeight = 18.sp)
    val terminalInputPromptStyle = terminalInputTextStyle.copy(
        color = palette.terminalPrompt,
        fontWeight = FontWeight.SemiBold
    )
    val longestTerminalLineLength = remember(
        displayedTerminalText,
        activeInputPrefix,
        terminalInput,
        showInputRow,
    ) {
        val outputLength = displayedTerminalText
            .lineSequence()
            .maxOfOrNull(String::length)
            ?: 0
        val inputLength = if (showInputRow) activeInputPrefix.length + terminalInput.length + 1 else 0
        maxOf(outputLength, inputLength, 1).coerceAtMost(20_000)
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

    LaunchedEffect(
        displayedTerminalText,
        showInputRow,
        outputVerticalScroll.maxValue
    ) {
        if (showInputRow) {
            delay(24)
            outputVerticalScroll.scrollTo(outputVerticalScroll.maxValue)
        }
    }

    LaunchedEffect(terminalInput, activeInputPrefix, showInputRow) {
        if (showInputRow && terminalInput.isNotEmpty()) {
            delay(24)
            inputBringIntoViewRequester.bringIntoView()
        }
    }

    BoxWithConstraints(modifier = modifier) {
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
                    .verticalScroll(outputVerticalScroll)
            ) {
                if (showInputRow) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(showInputRow) {
                                detectTapGestures(onTap = { focusTerminalInput() })
                            }
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    if (displayedTerminalText.isNotBlank()) {
                        SelectionContainer {
                            Text(
                                text = terminalOutputAnnotatedString(
                                    text = displayedTerminalText,
                                    promptText = promptText,
                                    palette = palette
                                ),
                                style = terminalTextStyle,
                                softWrap = false,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                    if (showInputRow) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(inputBringIntoViewRequester)
                        ) {
                            Text(
                                text = activeInputPrefix,
                                style = if (isAwaitingInput) terminalInputTextStyle else terminalInputPromptStyle,
                                modifier = Modifier.alignByBaseline()
                            )
                            BasicTextField(
                                value = terminalInput,
                                onValueChange = onTerminalInputChange,
                                textStyle = terminalInputTextStyle,
                                cursorBrush = SolidColor(palette.accent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = { onTerminalCommandSubmit() },
                                    onDone = { onTerminalCommandSubmit() },
                                    onGo = { onTerminalCommandSubmit() }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 24.dp)
                                    .alignByBaseline()
                                    .focusRequester(inputFocusRequester)
                                    .onFocusChanged { focusState ->
                                        onTerminalFocusChange(focusState.isFocused)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Panel(
    palette: IdePalette,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.panel)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(14.dp))
            .padding(10.dp),
        content = content
    )
}

@Composable
private fun HorizontalRule(palette: IdePalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(palette.divider)
    )
}

@Composable
private fun ImportOptionsDialog(
    palette: IdePalette,
    currentDirectoryName: String,
    onDismiss: () -> Unit,
    onOpenFile: () -> Unit,
    onOpenFolder: () -> Unit,
    onSaveFolder: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Files and folders") },
        text = {
            Column {
                Text(
                    text = "Destination: $currentDirectoryName",
                    color = palette.mutedText,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onOpenFile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Open file")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenFolder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Open folder")
                }
                Spacer(Modifier.height(14.dp))
                HorizontalRule(palette)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Save from sandbox",
                    color = palette.mutedText,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSaveFolder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SaveAlt,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Save current folder")
                }
            }
        }
    )
}

@Composable
private fun StorageMessageDialog(
    result: StorageOperationResult,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = { Text(if (result.success) "Storage complete" else "Storage error") },
        text = { Text(result.message) }
    )
}

@Composable
private fun NewFileDialog(
    palette: IdePalette,
    initialName: String,
    currentDirectoryName: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> String?,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    error = onCreate(name)
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("New Python file") },
        text = {
            Column {
                Text(
                    text = "Folder: $currentDirectoryName",
                    color = palette.mutedText,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = palette.text,
                        fontFamily = FontFamily.Monospace
                    ),
                    isError = error != null
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = palette.error,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun NewFolderDialog(
    palette: IdePalette,
    currentDirectoryName: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> String?,
) {
    var name by remember { mutableStateOf("folder") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    error = onCreate(name)
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Create folder") },
        text = {
            Column {
                Text(
                    text = "Folder: $currentDirectoryName",
                    color = palette.mutedText,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = palette.text,
                        fontFamily = FontFamily.Monospace
                    ),
                    isError = error != null
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = palette.error,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun RenameFileDialog(
    file: PythonFile,
    palette: IdePalette,
    onDismiss: () -> Unit,
    onRename: (String) -> String?,
) {
    var name by remember(file.id) { mutableStateOf(file.name) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    error = onRename(name)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Rename file") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = palette.text,
                        fontFamily = FontFamily.Monospace
                    ),
                    isError = error != null
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = palette.error,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun RenameFolderDialog(
    folder: FolderTarget,
    palette: IdePalette,
    onDismiss: () -> Unit,
    onRename: (String) -> String?,
) {
    var name by remember(folder.path) { mutableStateOf(folder.name) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    error = onRename(name)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Rename folder") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = palette.text,
                        fontFamily = FontFamily.Monospace
                    ),
                    isError = error != null
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = palette.error,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun ConfirmDeleteFileDialog(
    file: PythonFile,
    palette: IdePalette,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(
                    text = "Delete",
                    color = palette.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Delete ${file.name}?") },
        text = {
            Text(
                text = "Are you sure you want to delete this file from the current folder?",
                color = palette.mutedText
            )
        }
    )
}

@Composable
private fun ConfirmDeleteFolderDialog(
    folder: FolderTarget,
    palette: IdePalette,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(
                    text = "Delete",
                    color = palette.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Delete ${folder.name}?") },
        text = {
            Text(
                text = "Are you sure you want to delete this folder and everything inside it?",
                color = palette.mutedText
            )
        }
    )
}

private class PythonSyntaxTransformation(
    private val palette: IdePalette,
    private val diagnostics: List<CodeDiagnostic> = emptyList(),
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = pythonSyntaxAnnotatedString(text.text, palette, diagnostics),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

private fun pythonSyntaxAnnotatedString(
    source: String,
    palette: IdePalette,
    diagnostics: List<CodeDiagnostic> = emptyList(),
): AnnotatedString {
    if (source.isEmpty()) return AnnotatedString("")

    val dark = palette.text == Color.White
    val keywordStyle = SpanStyle(
        color = if (dark) Color(0xFFFF7AB6) else Color(0xFFB4236E),
        fontWeight = FontWeight.SemiBold
    )
    val builtinStyle = SpanStyle(color = if (dark) Color(0xFF82AAFF) else Color(0xFF2558C7))
    val stringStyle = SpanStyle(color = if (dark) Color(0xFFC3E88D) else Color(0xFF168048))
    val numberStyle = SpanStyle(color = if (dark) Color(0xFFFFCB6B) else Color(0xFF9A5B00))
    val commentStyle = SpanStyle(
        color = palette.subtleText,
        fontStyle = FontStyle.Italic
    )
    val decoratorStyle = SpanStyle(
        color = if (dark) Color(0xFF89DDFF) else Color(0xFF067A93),
        fontWeight = FontWeight.SemiBold
    )
    val constantStyle = SpanStyle(
        color = if (dark) Color(0xFFFFA3A3) else Color(0xFFC24141),
        fontWeight = FontWeight.Medium
    )

    val spans = mutableListOf<SyntaxSpan>()
    var index = 0
    while (index < source.length) {
        val char = source[index]
        when {
            char == '#' -> {
                val end = source.indexOf('\n', index).takeIf { it >= 0 } ?: source.length
                spans.add(SyntaxSpan(index, end, commentStyle))
                index = end
            }

            char == '"' || char == '\'' -> {
                val end = findPythonStringEnd(source, index)
                spans.add(SyntaxSpan(index, end, stringStyle))
                index = end
            }

            char == '@' && source.getOrNull(index + 1)?.let(::isIdentifierStart) == true -> {
                var end = index + 2
                while (end < source.length && isIdentifierPart(source[end])) end += 1
                spans.add(SyntaxSpan(index, end, decoratorStyle))
                index = end
            }

            char.isDigit() -> {
                var end = index + 1
                while (end < source.length && (source[end].isLetterOrDigit() || source[end] in "._")) end += 1
                spans.add(SyntaxSpan(index, end, numberStyle))
                index = end
            }

            isIdentifierStart(char) -> {
                var end = index + 1
                while (end < source.length && isIdentifierPart(source[end])) end += 1
                val word = source.substring(index, end)
                val style = when {
                    word in pythonKeywords -> keywordStyle
                    word in pythonConstants -> constantStyle
                    word in pythonBuiltins -> builtinStyle
                    else -> null
                }
                if (style != null) spans.add(SyntaxSpan(index, end, style))
                index = end
            }

            else -> index += 1
        }
    }

    return buildAnnotatedString {
        var cursor = 0
        spans
            .filter { it.start < it.end && it.start >= 0 && it.end <= source.length }
            .sortedBy { it.start }
            .forEach { span ->
                if (cursor < span.start) append(source.substring(cursor, span.start))
                withStyle(span.style) {
                    append(source.substring(span.start, span.end))
                }
                cursor = span.end
            }
        if (cursor < source.length) append(source.substring(cursor))
        diagnostics.forEach { diagnostic ->
            val start = diagnostic.start.coerceIn(0, source.length)
            val end = diagnostic.end.coerceIn(start, source.length)
            if (end > start) {
                addStyle(diagnosticSpanStyle(palette, diagnostic.severity), start, end)
            }
        }
    }
}

private fun diagnosticSpanStyle(
    palette: IdePalette,
    severity: String,
): SpanStyle {
    val color = diagnosticColor(severity, palette)
    return SpanStyle(
        background = color.copy(alpha = if (palette.text == Color.White) 0.18f else 0.12f),
        textDecoration = TextDecoration.Underline
    )
}

private fun diagnosticColor(severity: String, palette: IdePalette): Color {
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

private fun findPythonStringEnd(source: String, start: Int): Int {
    val quote = source[start]
    val tripleQuoted = source.startsWith("$quote$quote$quote", start)
    var index = if (tripleQuoted) start + 3 else start + 1
    while (index < source.length) {
        if (source[index] == '\\') {
            index += 2
            continue
        }
        if (tripleQuoted && source.startsWith("$quote$quote$quote", index)) return index + 3
        if (!tripleQuoted && source[index] == quote) return index + 1
        if (!tripleQuoted && source[index] == '\n') return index
        index += 1
    }
    return source.length
}

private fun applySmartEditorChange(
    current: TextFieldValue,
    requested: TextFieldValue,
): TextFieldValue {
    val inserted = singleInsertedText(current.text, requested.text) ?: return requested
    val insertedText = inserted.second
    if (insertedText.length != 1) return requested

    val char = insertedText.single()
    if (current.selection.collapsed && char in listOf(')', ']', '}', '"', '\'')) {
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
        '\n' -> if (current.selection.collapsed) {
            insertPythonIndentedNewline(current)
        } else {
            requested
        }
        else -> requested
    }
}

private fun insertPythonIndentedNewline(value: TextFieldValue): TextFieldValue {
    val cursor = value.selection.start
    val currentLine = value.text
        .substring(0, cursor)
        .substringAfterLast('\n')
    val existingIndent = currentLine.takeWhile { char -> char == ' ' || char == '\t' }
    val blockIndent = if (currentLine.trimEnd().endsWith(':')) "    " else ""
    return insertCodeAtSelection(value, "\n$existingIndent$blockIndent")
}

private fun singleInsertedText(
    oldText: String,
    newText: String,
): Pair<Int, String>? {
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
        selection = TextRange((start + cursorOffset).coerceIn(0, newText.length))
    )
}

private fun wrapOrInsertPair(
    value: TextFieldValue,
    open: String,
    close: String,
): TextFieldValue {
    val start = minOf(value.selection.start, value.selection.end)
    val end = maxOf(value.selection.start, value.selection.end)
    val selected = value.text.substring(start, end)
    val replacement = open + selected + close
    val newText = value.text.replaceRange(start, end, replacement)
    val cursor = if (selected.isEmpty()) start + open.length else start + replacement.length
    return TextFieldValue(newText, TextRange(cursor.coerceIn(0, newText.length)))
}

private fun completionContextAt(
    source: String,
    rawCursor: Int,
): CompletionContext? {
    val cursor = rawCursor.coerceIn(0, source.length)
    if (!isPythonCompletionPosition(source, cursor)) return null
    var start = cursor
    while (start > 0 && isIdentifierPart(source[start - 1])) start -= 1
    val prefix = source.substring(start, cursor)
    val isDotAccess = start > 0 && source[start - 1] == '.'
    if (!isDotAccess && prefix.length < 1) return null
    return CompletionContext(
        prefix = prefix,
        replaceStart = start,
        replaceEnd = cursor,
        isDotAccess = isDotAccess
    )
}

private enum class PythonLexicalState {
    Code,
    Comment,
    SingleQuotedString,
    DoubleQuotedString,
    TripleSingleQuotedString,
    TripleDoubleQuotedString,
}

internal fun isPythonCompletionPosition(source: String, rawCursor: Int): Boolean {
    val cursor = rawCursor.coerceIn(0, source.length)
    var state = PythonLexicalState.Code
    var index = 0

    while (index < cursor) {
        val char = source[index]
        when (state) {
            PythonLexicalState.Code -> when {
                char == '#' -> {
                    state = PythonLexicalState.Comment
                    index += 1
                }
                char == '\'' && source.startsWith("'''", index) -> {
                    state = PythonLexicalState.TripleSingleQuotedString
                    index += 3
                }
                char == '"' && source.startsWith("\"\"\"", index) -> {
                    state = PythonLexicalState.TripleDoubleQuotedString
                    index += 3
                }
                char == '\'' -> {
                    state = PythonLexicalState.SingleQuotedString
                    index += 1
                }
                char == '"' -> {
                    state = PythonLexicalState.DoubleQuotedString
                    index += 1
                }
                else -> index += 1
            }

            PythonLexicalState.Comment -> {
                if (char == '\n') state = PythonLexicalState.Code
                index += 1
            }

            PythonLexicalState.SingleQuotedString -> when {
                char == '\\' -> index = (index + 2).coerceAtMost(cursor)
                char == '\'' -> {
                    state = PythonLexicalState.Code
                    index += 1
                }
                char == '\n' -> {
                    state = PythonLexicalState.Code
                    index += 1
                }
                else -> index += 1
            }

            PythonLexicalState.DoubleQuotedString -> when {
                char == '\\' -> index = (index + 2).coerceAtMost(cursor)
                char == '"' -> {
                    state = PythonLexicalState.Code
                    index += 1
                }
                char == '\n' -> {
                    state = PythonLexicalState.Code
                    index += 1
                }
                else -> index += 1
            }

            PythonLexicalState.TripleSingleQuotedString -> when {
                char == '\\' -> index = (index + 2).coerceAtMost(cursor)
                source.startsWith("'''", index) -> {
                    state = PythonLexicalState.Code
                    index += 3
                }
                else -> index += 1
            }

            PythonLexicalState.TripleDoubleQuotedString -> when {
                char == '\\' -> index = (index + 2).coerceAtMost(cursor)
                source.startsWith("\"\"\"", index) -> {
                    state = PythonLexicalState.Code
                    index += 3
                }
                else -> index += 1
            }
        }
    }

    return state == PythonLexicalState.Code
}

private fun pythonCompletionsForContext(
    context: Context,
    source: String,
    line: Int,
    column: Int,
    filePath: String,
    rootDirectory: File,
    workingDirectory: File,
    virtualEnvironment: File?,
): List<CompletionItem> {
    return try {
        val json = pocketPython(context)
            .getModule("pocket_runner")
            .callAttr(
                "get_completions",
                source,
                line,
                column,
                filePath,
                workingDirectory.absolutePath,
                rootDirectory.absolutePath,
                virtualEnvironment?.absolutePath
            )
            .toString()
        
        val completionsJson = JSONObject(json).optJSONArray("completions") ?: JSONArray()
        List(completionsJson.length()) { index ->
            val item = completionsJson.optJSONObject(index) ?: JSONObject()
            CompletionItem(
                label = item.optString("label"),
                insertText = item.optString("insertText"),
                detail = item.optString("detail"),
                cursorOffset = item.optInt("cursorOffset", 0)
            )
        }
    } catch (e: Exception) {
        // Fallback to basic static analysis if python fails
        userDefinedPythonCompletions(source) + pythonCompletionItems
    }
}

private fun insertCompletion(
    value: TextFieldValue,
    context: CompletionContext,
    item: CompletionItem,
): TextFieldValue {
    val newText = value.text.replaceRange(context.replaceStart, context.replaceEnd, item.insertText)
    val cursor = (context.replaceStart + item.cursorOffset).coerceIn(0, newText.length)
    return TextFieldValue(newText, TextRange(cursor))
}

private fun pythonSignatureHelpAt(
    source: String,
    rawCursor: Int,
): String? {
    val cursor = rawCursor.coerceIn(0, source.length)
    val openParen = activeCallOpenParen(source, cursor) ?: return null
    var nameEnd = openParen
    while (nameEnd > 0 && source[nameEnd - 1].isWhitespace()) nameEnd -= 1
    var nameStart = nameEnd
    while (nameStart > 0 && isIdentifierPart(source[nameStart - 1])) nameStart -= 1
    if (nameStart == nameEnd) return null
    val name = source.substring(nameStart, nameEnd)
    val userSignature = Regex("""def\s+${Regex.escape(name)}\s*\(([^)]*)\)""")
        .find(source)
        ?.groupValues
        ?.getOrNull(1)
        ?.let { args -> "$name($args)" }
    if (userSignature != null) return userSignature

    return completionSyntaxes["$name|builtin"]
        ?: completionSyntaxes["$name|function"]
        ?: completionSyntaxes["$name|str"]
        ?: completionSyntaxes["$name|list"]
        ?: completionSyntaxes["$name|dict"]
        ?: completionSyntaxes["$name|number"]
}

private fun activeCallOpenParen(
    source: String,
    cursor: Int,
): Int? {
    val stack = mutableListOf<Int>()
    var index = 0
    while (index < cursor) {
        val char = source[index]
        when {
            char == '#' -> {
                index = source.indexOf('\n', index).takeIf { it >= 0 } ?: cursor
            }
            char == '"' || char == '\'' -> {
                index = findPythonStringEnd(source, index).coerceAtMost(cursor)
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
    }
    return stack.lastOrNull()
}

private fun CompletionItem.completionKey(): String {
    return "$label|$insertText|$detail"
}

private fun completionExplanation(item: CompletionItem): String {
    val key = completionLookupKey(item)
    completionDefinitions[key]?.let { return it }

    return when (item.detail) {
        "keyword" -> "A Python keyword is part of the language grammar. Use it to build control flow, functions, classes, imports, and error handling. Keywords cannot be used as variable names."
        "builtin" -> "A built-in function is always available in Python. It usually creates values, converts data, inspects collections, or prints output without needing an import."
        "function" -> "A callable function. Type parentheses after the name, place arguments inside them, and use the returned value if the function gives one back."
        "snippet" -> "A reusable code template. Choosing it inserts a common Python block with indentation already started so you can fill in the names, conditions, and body."
        "variable" -> "A variable found in this file. Use it when you want to read the value you stored earlier, pass it into a function, or assign an updated value."
        "class" -> "A class found in this file. Classes define object blueprints with shared data and behavior. Call the class name to create a new object."
        "module" -> "A module imported in this file. Modules group reusable Python code. Use dot access to call functions or read values from the module."
        "list" -> "A list method. Lists store ordered items and can grow, shrink, sort, and copy their contents. Most list methods change the existing list."
        "dict" -> "A dictionary method. Dictionaries store key-value pairs, which are useful for records, lookup tables, settings, and structured data."
        "str" -> "A string method. Strings are text values; these methods help clean, search, split, combine, or transform text while returning a new string."
        "number" -> "A numeric method. These methods expose lower-level information about numbers or convert them into exact or binary representations."
        "dunder" -> "A double-underscore name used by Python itself. These names carry special meaning, so they are useful in specific patterns like checking whether a file was run directly."
        "constant" -> "A common constant-like value used by Python code. Use it exactly as written when comparing against runtime values."
        else -> "A Python completion available in the current editor context."
    }
}

private fun completionExample(item: CompletionItem): String? {
    return completionExamples[completionLookupKey(item)]
        ?: completionExamples[item.label]
        ?: when (item.detail) {
            "function" -> item.insertText
            "variable" -> "${item.label} = ${item.label}"
            "class" -> "obj = ${item.label}()"
            "module" -> "${item.label}.some_function()"
            else -> null
        }
}

private fun completionSyntax(item: CompletionItem): String? {
    return completionSyntaxes[completionLookupKey(item)]
        ?: when (item.detail) {
            "function", "builtin" -> item.insertText
            "variable" -> item.label
            "class" -> "${item.label}(...)"
            "module" -> "${item.label}.name"
            else -> null
        }
}

private fun completionLookupKey(item: CompletionItem): String {
    val baseLabel = item.label.removeSuffix("()")
    return "$baseLabel|${item.detail}"
}

private fun userDefinedPythonCompletions(source: String): List<CompletionItem> {
    val items = mutableListOf<CompletionItem>()
    Regex("""(?m)^\s*def\s+([A-Za-z_]\w*)""").findAll(source).forEach { match ->
        val name = match.groupValues[1]
        items.add(CompletionItem("$name()", "$name()", "function", name.length + 1))
    }
    Regex("""(?m)^\s*class\s+([A-Za-z_]\w*)""").findAll(source).forEach { match ->
        val name = match.groupValues[1]
        items.add(CompletionItem(name, name, "class"))
    }
    Regex("""(?m)^\s*([A-Za-z_]\w*)\s*=""").findAll(source).forEach { match ->
        val name = match.groupValues[1]
        if (name !in pythonKeywords) {
            items.add(CompletionItem(name, name, "variable"))
        }
    }
    Regex("""(?m)^\s*(?:import|from)\s+([A-Za-z_][\w.]*)""").findAll(source).forEach { match ->
        val moduleName = match.groupValues[1].substringAfterLast('.')
        items.add(CompletionItem(moduleName, moduleName, "module"))
    }
    return items.distinctBy { it.label }
}

private fun inferDotTargetType(
    source: String,
    dotIndex: Int,
): String? {
    val beforeDot = source.substring(0, dotIndex.coerceIn(0, source.length)).trimEnd()
    if (beforeDot.endsWith("\"") || beforeDot.endsWith("'")) return "str"
    if (beforeDot.endsWith("]")) return "list"
    if (beforeDot.endsWith("}")) return "dict"

    val variable = Regex("""([A-Za-z_]\w*)$""").find(beforeDot)?.groupValues?.getOrNull(1) ?: return null
    val assignment = Regex("""(?m)^\s*${Regex.escape(variable)}\s*=\s*(.+)$""")
        .findAll(source)
        .lastOrNull()
        ?.groupValues
        ?.getOrNull(1)
        ?.trimStart()
        ?: return null

    return when {
        assignment.startsWith("\"") || assignment.startsWith("'") -> "str"
        assignment.startsWith("[") -> "list"
        assignment.startsWith("{") -> "dict"
        assignment.startsWith("(") -> "tuple"
        assignment.firstOrNull()?.isDigit() == true -> "number"
        else -> null
    }
}

private fun pythonDotCompletionsForType(type: String?): List<CompletionItem> {
    return when (type) {
        "str" -> pythonStringDotCompletions
        "list", "tuple" -> pythonListDotCompletions
        "dict" -> pythonDictDotCompletions
        "number" -> pythonNumberDotCompletions
        else -> pythonStringDotCompletions + pythonListDotCompletions + pythonDictDotCompletions
    }
}

private fun terminalDisplayText(
    text: String,
    promptText: String,
    keepLastPrompt: Boolean,
): String {
    if (text.isBlank()) return text
    val lines = text.split('\n')
    val lastPromptIndex = if (keepLastPrompt) {
        lines.indexOfLast { line -> line.isShellPromptEcho(promptText) }
    } else {
        -1
    }
    return lines.mapIndexed { index, line ->
        if (line.isShellPromptEcho(promptText) && index != lastPromptIndex) {
            line.removeShellPromptEcho(promptText)
        } else {
            line
        }
    }.joinToString("\n")
}

private fun terminalOutputAnnotatedString(
    text: String,
    promptText: String,
    palette: IdePalette,
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    val promptStyle = SpanStyle(
        color = palette.terminalPrompt,
        fontWeight = FontWeight.SemiBold
    )
    val ansiPattern = Regex("(?:\u001B\\[|\u009B)([0-9;]*)m")
    var ansiForeground: Color? = null
    var ansiBold = false

    return buildAnnotatedString {
        fun applyAnsiCodes(rawCodes: String) {
            val codes = rawCodes
                .takeIf { it.isNotBlank() }
                ?.split(';')
                ?.map { it.toIntOrNull() ?: 0 }
                ?: listOf(0)
            var index = 0
            while (index < codes.size) {
                when (val code = codes[index]) {
                    0 -> {
                        ansiForeground = null
                        ansiBold = false
                    }
                    1 -> ansiBold = true
                    22 -> ansiBold = false
                    in 30..37 -> ansiForeground = terminalAnsiColor(code, palette)
                    39 -> ansiForeground = null
                    in 90..97 -> ansiForeground = terminalAnsiColor(code, palette)
                    38 -> when (codes.getOrNull(index + 1)) {
                        5 -> {
                            ansiForeground = ansi256Color(codes.getOrNull(index + 2) ?: 7, palette)
                            index += 2
                        }
                        2 -> {
                            val red = (codes.getOrNull(index + 2) ?: 255).coerceIn(0, 255)
                            val green = (codes.getOrNull(index + 3) ?: 255).coerceIn(0, 255)
                            val blue = (codes.getOrNull(index + 4) ?: 255).coerceIn(0, 255)
                            ansiForeground = ensureReadableColor(Color(red, green, blue), palette.panel)
                            index += 4
                        }
                    }
                    48 -> {
                        index += if (codes.getOrNull(index + 1) == 2) 4 else if (codes.getOrNull(index + 1) == 5) 2 else 0
                    }
                }
                index += 1
            }
        }

        fun appendAnsiText(rawText: String, fallbackColor: Color? = null) {
            var cursor = 0
            ansiPattern.findAll(rawText).forEach { match ->
                val segment = rawText.substring(cursor, match.range.first)
                val color = ansiForeground ?: fallbackColor
                if (color != null || ansiBold) {
                    withStyle(
                        SpanStyle(
                            color = color ?: Color.Unspecified,
                            fontWeight = if (ansiBold) FontWeight.Bold else null
                        )
                    ) {
                        append(segment)
                    }
                } else {
                    append(segment)
                }
                applyAnsiCodes(match.groupValues[1])
                cursor = match.range.last + 1
            }
            val remaining = rawText.substring(cursor)
            val color = ansiForeground ?: fallbackColor
            if (color != null || ansiBold) {
                withStyle(
                    SpanStyle(
                        color = color ?: Color.Unspecified,
                        fontWeight = if (ansiBold) FontWeight.Bold else null
                    )
                ) {
                    append(remaining)
                }
            } else {
                append(remaining)
            }
        }

        text.split('\n').forEachIndexed { index, line ->
            if (index > 0) append('\n')
            when {
                line == promptText -> withStyle(promptStyle) {
                    append(line)
                }
                line.startsWith("$promptText ") -> {
                    ansiForeground = null
                    ansiBold = false
                    withStyle(promptStyle) {
                        append(promptText)
                    }
                    appendAnsiText(line.removePrefix(promptText))
                }
                else -> {
                    val plainLine = ansiPattern.replace(line, "")
                    appendAnsiText(line, terminalSemanticColor(plainLine, palette))
                }
            }
        }
    }
}

private fun terminalSemanticColor(line: String, palette: IdePalette): Color? {
    val normalized = line.trim().lowercase()
    if (normalized.isBlank()) return null
    return when {
        normalized == "ok" ||
            normalized == "finished" ||
            normalized.contains("build successful") ||
            normalized.contains("successfully installed") ||
            Regex("\\b\\d+ passed\\b").containsMatchIn(normalized) ||
            Regex("\\b0 failed\\b").containsMatchIn(normalized) -> palette.success
        normalized.startsWith("traceback") ||
            normalized.startsWith("failed (") ||
            normalized.contains("build failed") ||
            normalized.contains("permission denied") ||
            normalized.contains("no such file") ||
            normalized.contains("command not found") ||
            Regex("\\b(error|failed|failure|exception|fatal)\\b").containsMatchIn(normalized) -> palette.error
        normalized == "stopped" ||
            Regex("\\b(warning|warn|deprecated|skipped)\\b").containsMatchIn(normalized) -> palette.warning
        Regex("^ran \\d+ tests?").containsMatchIn(normalized) -> palette.info
        else -> palette.text
    }
}

private fun terminalAnsiColor(code: Int, palette: IdePalette): Color {
    val darkBackground = palette.panel.luminance() < 0.5f
    val colors = if (darkBackground) {
        listOf(
            Color(0xFF9AA4B2), palette.error, palette.success, palette.warning,
            Color(0xFF7CB7FF), Color(0xFFE49AFF), palette.info, Color(0xFFE8EDF6),
            Color(0xFFB5BECA), Color(0xFFFF9A9A), Color(0xFF7BEDA7), Color(0xFFFFD580),
            Color(0xFF9AC8FF), Color(0xFFF0B1FF), Color(0xFFA0ECFF), Color.White
        )
    } else {
        listOf(
            Color(0xFF20242A), palette.error, palette.success, palette.warning,
            Color(0xFF2458A6), Color(0xFF7A3D8C), palette.info, Color(0xFF4B5563),
            Color(0xFF475569), Color(0xFF9F1D16), Color(0xFF08653A), Color(0xFF765000),
            Color(0xFF174A8B), Color(0xFF692D79), Color(0xFF075E73), Color(0xFF1F2937)
        )
    }
    val index = if (code >= 90) code - 90 + 8 else code - 30
    return ensureReadableColor(colors[index.coerceIn(0, colors.lastIndex)], palette.panel)
}

private fun ansi256Color(code: Int, palette: IdePalette): Color {
    val value = code.coerceIn(0, 255)
    if (value < 16) {
        val ansiCode = if (value < 8) 30 + value else 90 + value - 8
        return terminalAnsiColor(ansiCode, palette)
    }
    if (value >= 232) {
        val gray = 8 + (value - 232) * 10
        return ensureReadableColor(Color(gray, gray, gray), palette.panel)
    }
    val cube = value - 16
    val red = cube / 36
    val green = (cube % 36) / 6
    val blue = cube % 6
    fun channel(component: Int) = if (component == 0) 0 else 55 + component * 40
    return ensureReadableColor(Color(channel(red), channel(green), channel(blue)), palette.panel)
}

private fun ensureReadableColor(foreground: Color, background: Color): Color {
    var adjusted = foreground.copy(alpha = 1f)
    val target = if (background.luminance() > 0.5f) Color.Black else Color.White
    repeat(10) {
        val lighter = maxOf(adjusted.luminance(), background.luminance()) + 0.05f
        val darker = minOf(adjusted.luminance(), background.luminance()) + 0.05f
        if (lighter / darker >= 4.5f) return adjusted
        adjusted = Color(
            red = adjusted.red * 0.82f + target.red * 0.18f,
            green = adjusted.green * 0.82f + target.green * 0.18f,
            blue = adjusted.blue * 0.82f + target.blue * 0.18f,
            alpha = 1f
        )
    }
    return adjusted
}

private fun String.isShellPromptEcho(promptText: String): Boolean {
    return this == promptText || startsWith("$promptText ")
}

private fun String.removeShellPromptEcho(promptText: String): String {
    return when {
        this == promptText -> ""
        startsWith("$promptText ") -> removePrefix("$promptText ")
        else -> this
    }
}

private fun appendTerminalOutput(
    current: String,
    addition: String,
): String {
    val trimmedAddition = addition.trimEnd()
    if (trimmedAddition.isBlank()) return current
    return trimTerminalBuffer(when {
        current.isBlank() -> trimmedAddition
        current.endsWith("\n") -> current + trimmedAddition
        else -> "$current\n$trimmedAddition"
    })
}

private fun appendTerminalStreamOutput(
    current: String,
    addition: String,
): String {
    if (addition.isBlank()) return current
    return trimTerminalBuffer(when {
        current.isBlank() -> addition.trimStart('\n')
        current.endsWith("\n") || addition.startsWith("\n") -> current + addition
        else -> "$current\n$addition"
    })
}

private fun appendTerminalInputEcho(
    current: String,
    input: String,
    inline: Boolean,
): String {
    return trimTerminalBuffer(when {
        current.isBlank() -> input
        inline && !current.endsWith("\n") -> current + input
        current.endsWith("\n") -> current + input
        else -> "$current\n$input"
    })
}

private fun virtualEnvironmentSessionCommand(
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
    activeEnvironment: File?,
): VirtualEnvironmentActivation? {
    val executable = args.firstOrNull()?.lowercase().orEmpty()
    if (executable == "deactivate") {
        return if (activeEnvironment == null) {
            VirtualEnvironmentActivation(error = "deactivate: no virtual environment is active")
        } else {
            VirtualEnvironmentActivation(
                environmentPath = null,
                output = "Deactivated ${activeEnvironment.name}"
            )
        }
    }

    val rawActivationPath = when {
        executable == "activate" -> args.getOrNull(1) ?: ".venv"
        executable == "source" || executable == "." -> {
            val sourcePath = args.getOrNull(1) ?: return VirtualEnvironmentActivation(
                error = "$executable: filename argument required"
            )
            if (!sourcePath.replace('\\', '/').endsWith("/bin/activate")) return null
            sourcePath
        }
        executable.replace('\\', '/').endsWith("/bin/activate") -> args.first()
        else -> return null
    }

    val resolved = resolveTerminalPath(rootDirectory, workingDirectory, rawActivationPath)
        ?: return VirtualEnvironmentActivation(error = "activate: $rawActivationPath: outside sandbox")
    val environment = when {
        resolved.name == "activate" && resolved.parentFile?.name == "bin" -> resolved.parentFile?.parentFile
        else -> resolved
    }
    if (environment == null || !isPocketVirtualEnvironment(environment)) {
        return VirtualEnvironmentActivation(
            error = "activate: $rawActivationPath: not a PocketCodedPy virtual environment"
        )
    }
    return VirtualEnvironmentActivation(
        environmentPath = environment.absolutePath,
        output = "Activated ${environment.name}"
    )
}

private fun createPocketVirtualEnvironment(
    arguments: List<String>,
    rootDirectory: File,
    workingDirectory: File,
): ShellCommandResult {
    var clearExisting = false
    var environmentArgument: String? = null
    var index = 0
    while (index < arguments.size) {
        val argument = arguments[index]
        when {
            argument == "--clear" -> clearExisting = true
            argument == "--without-pip" ||
                argument == "--system-site-packages" ||
                argument == "--upgrade-deps" -> Unit
            argument == "--prompt" -> index += 1
            argument.startsWith("-") -> return ShellCommandResult(
                error = "venv: unsupported option $argument"
            )
            environmentArgument == null -> environmentArgument = argument
            else -> return ShellCommandResult(error = "venv: expected one environment directory")
        }
        index += 1
    }

    val rawPath = environmentArgument
        ?: return ShellCommandResult(error = "venv: missing environment directory")
    val environment = resolveTerminalPath(rootDirectory, workingDirectory, rawPath)
        ?: return ShellCommandResult(error = "venv: $rawPath: outside sandbox")

    if (environment.exists() && clearExisting) {
        if (!environment.deleteRecursively()) {
            return ShellCommandResult(error = "venv: could not clear $rawPath")
        }
    } else if (
        environment.exists() &&
        environment.listFiles().orEmpty().isNotEmpty() &&
        !isPocketVirtualEnvironment(environment)
    ) {
        return ShellCommandResult(error = "venv: $rawPath: directory is not empty")
    }

    return runCatching {
        val binDirectory = File(environment, "bin").apply { mkdirs() }
        val sitePackages = File(environment, "lib/python/site-packages").apply { mkdirs() }
        if (!binDirectory.isDirectory || !sitePackages.isDirectory) {
            error("could not create environment directories")
        }
        File(environment, "pyvenv.cfg").writeText(
            """
            implementation = PocketCodedPy Chaquopy
            include-system-site-packages = false
            version = 3
            """.trimIndent() + "\n"
        )
        File(binDirectory, "activate").writeText(
            """
            # PocketCodedPy virtual environment
            # Activate with: source ${environment.name}/bin/activate
            export VIRTUAL_ENV="${environment.absolutePath}"
            """.trimIndent() + "\n"
        )
        val pythonShim = """
            #!/system/bin/sh
            # PocketCodedPy intercepts this environment-local Python command.
        """.trimIndent() + "\n"
        File(binDirectory, "python").writeText(pythonShim)
        File(binDirectory, "python3").writeText(pythonShim)
        val pipShim = """
            #!/system/bin/sh
            # PocketCodedPy intercepts this environment-local pip command.
        """.trimIndent() + "\n"
        File(binDirectory, "pip").writeText(pipShim)
        File(binDirectory, "pip3").writeText(pipShim)
        ShellCommandResult(
            output = buildString {
                append("Created virtual environment at ")
                append(relativeDirectoryLabel(rootDirectory, environment))
                append("\nActivate it with: source ")
                append(relativeDirectoryLabel(rootDirectory, File(environment, "bin/activate")))
            },
            workspaceChanged = true
        )
    }.getOrElse { error ->
        ShellCommandResult(error = "venv: ${error.message ?: "could not create environment"}")
    }
}

private fun isPocketVirtualEnvironment(directory: File): Boolean {
    return directory.isDirectory && File(directory, "pyvenv.cfg").isFile
}

private fun virtualEnvironmentForPythonExecutable(
    executable: String,
    rootDirectory: File,
    workingDirectory: File,
): File? {
    val normalized = executable.replace('\\', '/')
    if (!normalized.endsWith("/bin/python") && !normalized.endsWith("/bin/python3")) return null
    val pythonFile = resolveTerminalPath(rootDirectory, workingDirectory, executable) ?: return null
    return pythonFile.parentFile?.parentFile?.takeIf(::isPocketVirtualEnvironment)
}

private fun virtualEnvironmentForPipExecutable(
    executable: String,
    rootDirectory: File,
    workingDirectory: File,
): File? {
    val normalized = executable.replace('\\', '/')
    if (!normalized.endsWith("/bin/pip") && !normalized.endsWith("/bin/pip3")) return null
    val pipFile = resolveTerminalPath(rootDirectory, workingDirectory, executable) ?: return null
    return pipFile.parentFile?.parentFile?.takeIf(::isPocketVirtualEnvironment)
}

private fun executePocketPip(
    arguments: List<String>,
    environment: File,
    rootDirectory: File,
    workingDirectory: File,
    runController: TerminalRunController? = null,
): ShellCommandResult {
    val command = arguments.firstOrNull()?.lowercase() ?: "help"
    val sitePackages = File(environment, "lib/python/site-packages").apply { mkdirs() }
    if (!sitePackages.isDirectory) return ShellCommandResult(error = "pip: invalid virtual environment")

    return try {
        when (command) {
            "-v", "--version" -> ShellCommandResult(
                output = "pip 24.0 (PocketCodedPy Android wheel installer) from ${sitePackages.absolutePath}"
            )
            "help", "-h", "--help" -> ShellCommandResult(
                output = "pip install <package>[==version] | pip list | pip freeze | pip show <package> | pip uninstall [-y] <package>"
            )
            "list", "freeze" -> {
                val packages = installedPocketPackages(sitePackages)
                val output = if (command == "freeze") {
                    packages.joinToString("\n") { (name, version) -> "$name==$version" }
                } else {
                    buildString {
                        append("Package  Version\n")
                        append("-------- -------")
                        packages.forEach { (name, version) -> append("\n$name  $version") }
                    }
                }
                ShellCommandResult(output = output)
            }
            "show" -> {
                val requested = arguments.getOrNull(1)
                    ?: return ShellCommandResult(error = "pip show: missing package name")
                val metadata = pocketPackageMetadata(sitePackages, requested)
                    ?: return ShellCommandResult(error = "WARNING: Package(s) not found: $requested")
                ShellCommandResult(output = metadata.lines().filter { line ->
                    line.startsWith("Name:") || line.startsWith("Version:") ||
                        line.startsWith("Summary:") || line.startsWith("Home-page:") ||
                        line.startsWith("Requires:")
                }.joinToString("\n") + "\nLocation: ${sitePackages.absolutePath}")
            }
            "uninstall" -> {
                val names = arguments.drop(1).filterNot { it == "-y" || it == "--yes" }
                if (names.isEmpty()) return ShellCommandResult(error = "pip uninstall: missing package name")
                val removed = names.map { name -> uninstallPocketPackage(sitePackages, name) }
                ShellCommandResult(output = removed.joinToString("\n"), workspaceChanged = true)
            }
            "install" -> {
                var upgrade = false
                var installDependencies = true
                val requirements = mutableListOf<String>()
                arguments.drop(1).forEach { argument ->
                    when (argument) {
                        "-U", "--upgrade" -> upgrade = true
                        "--no-deps" -> installDependencies = false
                        else -> if (argument.startsWith("-")) {
                            return ShellCommandResult(error = "pip install: unsupported option $argument")
                        } else requirements += argument
                    }
                }
                if (requirements.isEmpty()) return ShellCommandResult(error = "pip install: missing package")
                val installed = installedPocketPackages(sitePackages)
                    .associate { (name, version) -> normalizePackageName(name) to version }
                    .toMutableMap()
                val visiting = mutableSetOf<String>()
                val messages = mutableListOf<String>()

                lateinit var installRequirement: (String) -> Unit
                installRequirement = install@{ rawRequirement ->
                    if (runController?.isStopped() == true) throw InterruptedException("Stopped")
                    val requirement = applicablePocketRequirement(rawRequirement) ?: return@install
                    val localWheel = resolveTerminalPath(rootDirectory, workingDirectory, requirement)
                        ?.takeIf { it.isFile && it.extension.equals("whl", ignoreCase = true) }
                    val packageName = localWheel?.nameWithoutExtension?.substringBefore('-')
                        ?: pocketRequirementName(requirement)
                        ?: error("invalid requirement $requirement")
                    val normalizedName = normalizePackageName(packageName)
                    if (normalizedName.isBlank() || normalizedName in visiting) return@install
                    if (!upgrade && installed[normalizedName]?.let { pocketRequirementAllowsVersion(requirement, it) } == true) {
                        return@install
                    }
                    visiting += normalizedName
                    val wheel = loadPocketWheel(
                        requirement = requirement,
                        rootDirectory = rootDirectory,
                        workingDirectory = workingDirectory,
                        runController = runController,
                    )
                    if (installDependencies) wheel.requirements.forEach(installRequirement)
                    installPocketWheel(wheel, sitePackages)
                    installed[normalizedName] = wheel.version
                    visiting -= normalizedName
                    messages += "Successfully installed ${wheel.name}-${wheel.version}"
                }
                requirements.forEach(installRequirement)
                ShellCommandResult(
                    output = messages.distinct().joinToString("\n").ifBlank { "Requirement already satisfied" },
                    workspaceChanged = true
                )
            }
            else -> ShellCommandResult(error = "pip: unknown command $command")
        }
    } catch (_: InterruptedException) {
        ShellCommandResult(stopped = true)
    } catch (error: Throwable) {
        ShellCommandResult(error = "pip: ${error.message ?: "operation failed"}")
    }
}

private fun loadPocketWheel(
    requirement: String,
    rootDirectory: File,
    workingDirectory: File,
    runController: TerminalRunController?,
): PocketWheel {
    val localWheel = resolveTerminalPath(rootDirectory, workingDirectory, requirement)
        ?.takeIf { it.isFile && it.extension.equals("whl", ignoreCase = true) }
    val bytes: ByteArray
    val filename: String
    val fallbackName: String
    val fallbackVersion: String
    if (localWheel != null) {
        bytes = localWheel.readBytes()
        filename = localWheel.name
        val wheelParts = filename.removeSuffix(".whl").split('-')
        fallbackName = wheelParts.getOrElse(0) { localWheel.nameWithoutExtension }
        fallbackVersion = wheelParts.getOrElse(1) { "unknown" }
    } else {
        val packageName = pocketRequirementName(requirement)
            ?: error("invalid requirement $requirement")
        val requestedVersion = pocketExactRequirementVersion(requirement)
        val androidWheel = findChaquopyAndroidWheel(packageName, requirement, runController)
        if (androidWheel != null) {
            filename = androidWheel.filename
            bytes = pocketHttpGet(androidWheel.url, runController)
            fallbackName = packageName
            fallbackVersion = androidWheel.version
        } else {
            val packagePath = URLEncoder.encode(packageName, "UTF-8").replace("+", "%20")
            val versionPath = requestedVersion?.let { "/${URLEncoder.encode(it, "UTF-8")}" }.orEmpty()
            val metadataBytes = pocketHttpGet("https://pypi.org/pypi/$packagePath$versionPath/json", runController)
            val metadata = JSONObject(metadataBytes.toString(Charsets.UTF_8))
            val info = metadata.optJSONObject("info") ?: JSONObject()
            fallbackName = info.optString("name", packageName)
            fallbackVersion = info.optString("version", requestedVersion.orEmpty())
            val files = metadata.optJSONArray("urls") ?: JSONArray()
            val candidates = (0 until files.length()).mapNotNull(files::optJSONObject)
            val wheelFile = candidates
                .filter { it.optString("packagetype") == "bdist_wheel" }
                .filter { candidate ->
                    val candidateName = candidate.optString("filename")
                    candidateName.endsWith(".whl") &&
                        (candidateName.contains("-py3-none-any.whl") || candidateName.contains("-py2.py3-none-any.whl"))
                }
                .maxByOrNull { candidate ->
                    if (candidate.optString("filename").contains("-py3-none-any.whl")) 2 else 1
                }
                ?: error(
                    "$packageName has no compatible Android wheel. " +
                        "Native packages must be published for Python 3.10 and this device ABI."
                )
            filename = wheelFile.optString("filename")
            bytes = pocketHttpGet(wheelFile.getString("url"), runController)
        }
    }
    val metadataText = wheelMetadata(bytes)
    val name = metadataValue(metadataText, "Name") ?: fallbackName
    val version = metadataValue(metadataText, "Version") ?: fallbackVersion
    val requirements = metadataText.lineSequence()
        .filter { it.startsWith("Requires-Dist:") }
        .map { it.substringAfter(':').trim() }
        .toList()
    return PocketWheel(name, version, filename, bytes, requirements)
}

private fun findChaquopyAndroidWheel(
    packageName: String,
    requirement: String,
    runController: TerminalRunController?,
): AndroidWheelCandidate? {
    val packagePath = URLEncoder.encode(normalizePackageName(packageName), "UTF-8").replace("+", "%20")
    val indexUrl = "https://chaquo.com/pypi-13.1/$packagePath/"
    val html = runCatching {
        pocketHttpGet(indexUrl, runController).toString(Charsets.UTF_8)
    }.getOrNull() ?: return null
    val supportedAbis = Build.SUPPORTED_ABIS
        .map { it.replace('-', '_') }
        .filter { it == "arm64_v8a" || it == "x86_64" }
    if (supportedAbis.isEmpty()) return null

    val links = Regex("href=\"([^\"]+\\.whl)\"", RegexOption.IGNORE_CASE)
        .findAll(html)
        .map { it.groupValues[1] }
        .toList()
    val candidates = links.mapNotNull { href ->
        val filename = Uri.decode(href.substringAfterLast('/'))
        val parts = filename.removeSuffix(".whl").split('-')
        if (parts.size < 5) return@mapNotNull null
        val pythonTag = parts[parts.size - 3]
        val abiTag = parts[parts.size - 2]
        val platformTag = parts.last()
        if (pythonTag !in setOf("cp310", "py3", "py2.py3")) return@mapNotNull null
        if (abiTag !in setOf("cp310", "abi3", "none")) return@mapNotNull null
        val platform = Regex("android_(\\d+)_(.+)").matchEntire(platformTag) ?: return@mapNotNull null
        val apiLevel = platform.groupValues[1].toIntOrNull() ?: return@mapNotNull null
        val wheelAbi = platform.groupValues[2]
        if (wheelAbi !in supportedAbis || apiLevel > Build.VERSION.SDK_INT) return@mapNotNull null
        val version = parts.getOrNull(1) ?: return@mapNotNull null
        if (!pocketRequirementAllowsVersion(requirement, version)) return@mapNotNull null
        AndroidWheelCandidate(
            filename = filename,
            url = URL(URL(indexUrl), href).toString(),
            version = version,
            apiLevel = apiLevel,
            abi = wheelAbi,
        )
    }
    return candidates.maxWithOrNull { left, right ->
        val abiComparison = supportedAbis.indexOf(right.abi)
            .compareTo(supportedAbis.indexOf(left.abi))
        if (abiComparison != 0) abiComparison else {
            val versionComparison = comparePocketVersions(left.version, right.version)
            if (versionComparison != 0) versionComparison else left.apiLevel.compareTo(right.apiLevel)
        }
    }
}

private fun pocketRequirementAllowsVersion(requirement: String, version: String): Boolean {
    val constraints = Regex("(==|>=|<=|~=|>|<)\\s*([^,;\\s]+)")
        .findAll(requirement)
        .map { it.groupValues[1] to it.groupValues[2] }
        .toList()
    return constraints.all { (operator, expected) ->
        val comparison = comparePocketVersions(version, expected)
        when (operator) {
            "==" -> comparison == 0
            ">=" -> comparison >= 0
            "<=" -> comparison <= 0
            ">" -> comparison > 0
            "<" -> comparison < 0
            "~=" -> comparison >= 0 && version.substringBeforeLast('.', version) == expected.substringBeforeLast('.', expected)
            else -> true
        }
    }
}

private fun comparePocketVersions(left: String, right: String): Int {
    val tokenPattern = Regex("\\d+|[A-Za-z]+")
    val leftTokens = tokenPattern.findAll(left).map { it.value }.toList()
    val rightTokens = tokenPattern.findAll(right).map { it.value }.toList()
    val size = maxOf(leftTokens.size, rightTokens.size)
    for (index in 0 until size) {
        val leftToken = leftTokens.getOrNull(index) ?: "0"
        val rightToken = rightTokens.getOrNull(index) ?: "0"
        val leftNumber = leftToken.toLongOrNull()
        val rightNumber = rightToken.toLongOrNull()
        val comparison = when {
            leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
            leftNumber != null -> 1
            rightNumber != null -> -1
            else -> leftToken.lowercase().compareTo(rightToken.lowercase())
        }
        if (comparison != 0) return comparison
    }
    return 0
}

private fun pocketHttpGet(urlText: String, runController: TerminalRunController?): ByteArray {
    val connection = (URL(urlText).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        instanceFollowRedirects = true
        connectTimeout = 20_000
        readTimeout = 300_000
        useCaches = false
        setRequestProperty("Accept", "application/json, application/octet-stream, text/html")
        setRequestProperty("User-Agent", "PocketCodedPy-pip/1.0")
    }
    try {
        if (runController?.isStopped() == true) throw InterruptedException("Stopped")
        val responseCode = connection.responseCode
        val stream = if (responseCode >= 400) connection.errorStream else connection.inputStream
        val bytes = stream?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                if (runController?.isStopped() == true) throw InterruptedException("Stopped")
                val read = input.read(buffer)
                if (read < 0) break
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        } ?: ByteArray(0)
        if (responseCode >= 400) {
            val detail = bytes.toString(Charsets.UTF_8).take(200).trim()
            throw IOException("HTTP $responseCode${if (detail.isBlank()) "" else ": $detail"}")
        }
        return bytes
    } finally {
        connection.disconnect()
    }
}

private fun wheelMetadata(bytes: ByteArray): String {
    ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
        while (true) {
            val entry = zip.nextEntry ?: break
            if (!entry.isDirectory && entry.name.endsWith(".dist-info/METADATA")) {
                return zip.readBytes().toString(Charsets.UTF_8)
            }
        }
    }
    error("wheel metadata is missing")
}

private fun installPocketWheel(wheel: PocketWheel, sitePackages: File) {
    val root = sitePackages.canonicalFile
    ZipInputStream(ByteArrayInputStream(wheel.bytes)).use { zip ->
        while (true) {
            val entry = zip.nextEntry ?: break
            var relativePath = entry.name.replace('\\', '/')
            if (".data/purelib/" in relativePath) relativePath = relativePath.substringAfter(".data/purelib/")
            if (".data/platlib/" in relativePath) relativePath = relativePath.substringAfter(".data/platlib/")
            if (
                ".data/" in relativePath &&
                ".data/purelib/" !in entry.name &&
                ".data/platlib/" !in entry.name
            ) continue
            val target = File(root, relativePath).canonicalFile
            if (!isSameOrInside(root, target)) error("unsafe wheel entry ${entry.name}")
            if (entry.isDirectory) {
                target.mkdirs()
            } else {
                target.parentFile?.mkdirs()
                target.outputStream().use { output -> zip.copyTo(output) }
            }
        }
    }
}

private fun installedPocketPackages(sitePackages: File): List<Pair<String, String>> {
    return sitePackages.listFiles()
        .orEmpty()
        .filter { it.isDirectory && it.name.endsWith(".dist-info") }
        .mapNotNull { directory ->
            val metadata = File(directory, "METADATA").takeIf(File::isFile)?.readText() ?: return@mapNotNull null
            val name = metadataValue(metadata, "Name") ?: return@mapNotNull null
            name to (metadataValue(metadata, "Version") ?: "unknown")
        }
        .sortedBy { it.first.lowercase() }
}

private fun pocketPackageMetadata(sitePackages: File, requestedName: String): String? {
    val normalized = normalizePackageName(requestedName)
    return sitePackages.listFiles()
        .orEmpty()
        .filter { it.isDirectory && it.name.endsWith(".dist-info") }
        .mapNotNull { File(it, "METADATA").takeIf(File::isFile) }
        .firstOrNull { metadata ->
            normalizePackageName(metadataValue(metadata.readText(), "Name").orEmpty()) == normalized
        }
        ?.readText()
}

private fun uninstallPocketPackage(sitePackages: File, requestedName: String): String {
    val normalized = normalizePackageName(requestedName)
    val metadataDirectory = sitePackages.listFiles()
        .orEmpty()
        .filter { it.isDirectory && it.name.endsWith(".dist-info") }
        .firstOrNull { directory ->
            val metadata = File(directory, "METADATA").takeIf(File::isFile)?.readText().orEmpty()
            normalizePackageName(metadataValue(metadata, "Name").orEmpty()) == normalized
        } ?: return "WARNING: Skipping $requestedName as it is not installed"
    val root = sitePackages.canonicalFile
    File(metadataDirectory, "RECORD").takeIf(File::isFile)?.readLines()?.forEach { recordLine ->
        val relativePath = recordLine.substringBefore(',')
        val target = File(root, relativePath).canonicalFile
        if (isSameOrInside(root, target) && target.isFile) target.delete()
    }
    metadataDirectory.deleteRecursively()
    return "Successfully uninstalled $requestedName"
}

private fun metadataValue(metadata: String, key: String): String? {
    return metadata.lineSequence()
        .firstOrNull { it.startsWith("$key:", ignoreCase = true) }
        ?.substringAfter(':')
        ?.trim()
        ?.takeIf(String::isNotBlank)
}

private fun normalizePackageName(name: String): String {
    return name.lowercase().replace(Regex("[-_.]+"), "-")
}

private fun pocketRequirementName(requirement: String): String? {
    return Regex("^[A-Za-z0-9][A-Za-z0-9._-]*")
        .find(requirement.trim())
        ?.value
}

private fun pocketExactRequirementVersion(requirement: String): String? {
    return Regex("==\\s*([^,;\\s]+)")
        .find(requirement)
        ?.groupValues
        ?.getOrNull(1)
}

private fun applicablePocketRequirement(rawRequirement: String): String? {
    val requirement = rawRequirement.substringBefore(';').trim()
    val marker = rawRequirement.substringAfter(';', "").trim().lowercase()
    if (marker.contains("extra ==") || marker.contains("extra !=")) return null
    if (marker.contains("sys_platform") && !marker.contains("linux") && !marker.contains("android")) return null
    if (marker.contains("platform_system") && !marker.contains("linux")) return null
    return requirement.takeIf(String::isNotBlank)
}

private fun executeShellCommand(
    command: String,
    rootDirectory: File,
    workingDirectory: File,
    activeEnvironment: File? = null,
    runController: TerminalRunController? = null,
): ShellCommandResult {
    val args = splitCommandLine(command)
    val executable = args.firstOrNull()?.lowercase().orEmpty()
    val commandArgs = args.drop(1)

    if (hasShellOperators(command)) {
        return executeSystemShellCommand(command, rootDirectory, workingDirectory, runController)
    }

    return when (executable) {
        "clear", "cls" -> ShellCommandResult(clearTerminal = true)
        "help" -> ShellCommandResult(
            output = """
                PocketCodedPy shell commands
                pwd, ls, tree, cd <dir>, cat <file>, touch <file>, mkdir <dir>, rm [-r] <path>
                python <file.py>, python -m unittest [discover|test_module], echo <text>, clear, help
                python -m venv .venv | virtualenv .venv
                source .venv/bin/activate | deactivate | .venv/bin/python <file.py>
                pip install <package> | pip list | pip freeze | pip show | pip uninstall
                bash -c "command" | bash script.sh [args]
                curl [options] <url> (GET, POST, headers, data, redirects, and output files)
                git --version, git init|clone|status|add|commit|log|branch|checkout|remote|pull|push
                ssh-keygen -t ed25519 -C "you@example.com"
                ssh-keygen -t rsa -b 4096 -C "you@example.com"
                
                GitHub/GitLab
                SSH remotes use sandbox keys from ~/.ssh. HTTPS works for public repositories; prefer SSH for private repositories so secrets stay out of terminal history.

                Desktop-style shell passthrough
                Most other commands run through /system/bin/sh from the current sandbox folder.
                Pipes, redirects, env vars, grep/find/sed/awk, and installed system binaries are supported when Android provides them.
            """.trimIndent()
        )
        "pwd" -> ShellCommandResult(output = relativeDirectoryLabel(rootDirectory, workingDirectory))
        "tree" -> ShellCommandResult(output = treeShellDirectory(rootDirectory, workingDirectory))
        "cd" -> {
            val target = commandArgs.firstOrNull() ?: "~"
            val directory = resolveTerminalPath(rootDirectory, workingDirectory, target)
            when {
                directory == null -> ShellCommandResult(error = "cd: $target: Permission denied")
                !directory.exists() -> ShellCommandResult(error = "cd: $target: No such file or directory")
                !directory.isDirectory -> ShellCommandResult(error = "cd: $target: Not a directory")
                else -> ShellCommandResult(
                    output = relativeDirectoryLabel(rootDirectory, directory),
                    workingDirectoryPath = directory.absolutePath
                )
            }
        }
        "bash" -> executeBuiltInBash(commandArgs, rootDirectory, workingDirectory, runController)
        "curl" -> executeBuiltInCurl(commandArgs, rootDirectory, workingDirectory, runController)
        "virtualenv" -> createPocketVirtualEnvironment(commandArgs, rootDirectory, workingDirectory)
        "git" -> executeBuiltInGitCommand(commandArgs, rootDirectory, workingDirectory, activeEnvironment)
        "ssh-keygen" -> generateSshKey(commandArgs, rootDirectory, workingDirectory)
        "python", "python3", "py" -> ShellCommandResult(output = "Use python <file.py>, python -m unittest, or the Run Code button.")
        "" -> ShellCommandResult()
        else -> executeSystemShellCommand(command, rootDirectory, workingDirectory, runController)
    }
}

private fun hasShellOperators(command: String): Boolean {
    var quote: Char? = null
    var escaping = false
    command.forEach { char ->
        when {
            escaping -> escaping = false
            char == '\\' -> escaping = true
            quote != null && char == quote -> quote = null
            quote != null -> Unit
            char == '"' || char == '\'' -> quote = char
            char in setOf('|', '&', ';', '<', '>', '(', ')', '$', '`') -> return true
        }
    }
    return false
}

private fun executeBuiltInCurl(
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
    runController: TerminalRunController? = null,
): ShellCommandResult {
    if (args.isEmpty() || args.firstOrNull() in setOf("-h", "--help")) {
        return ShellCommandResult(output = builtInCurlHelp())
    }
    if (args.firstOrNull() in setOf("-V", "--version")) {
        return ShellCommandResult(output = "curl 8.0.0 (PocketCodedPy built-in HTTP client)")
    }

    var method: String? = null
    var urlText: String? = null
    var includeHeaders = false
    var headOnly = false
    var followRedirects = false
    var failOnHttpError = false
    var outputPath: String? = null
    var useRemoteName = false
    var connectTimeoutMs = 15_000
    var readTimeoutMs = 300_000
    var useGetWithData = false
    val headers = linkedMapOf<String, String>()
    val dataParts = mutableListOf<String>()
    val formParts = mutableListOf<String>()

    fun requiredValue(index: Int): String? = args.getOrNull(index + 1)

    var index = 0
    while (index < args.size) {
        val arg = args[index]
        when {
            arg == "-X" || arg == "--request" -> {
                method = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                index += 2
            }
            arg.startsWith("--request=") -> {
                method = arg.substringAfter('=')
                index += 1
            }
            arg == "-H" || arg == "--header" -> {
                val header = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                val separator = header.indexOf(':')
                if (separator <= 0) return ShellCommandResult(error = "curl: (2) malformed header: $header")
                headers[header.substring(0, separator).trim()] = header.substring(separator + 1).trim()
                index += 2
            }
            arg.startsWith("--header=") -> {
                val header = arg.substringAfter('=')
                val separator = header.indexOf(':')
                if (separator <= 0) return ShellCommandResult(error = "curl: (2) malformed header: $header")
                headers[header.substring(0, separator).trim()] = header.substring(separator + 1).trim()
                index += 1
            }
            arg in setOf("-d", "--data", "--data-raw", "--data-binary") -> {
                val data = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                val resolvedData = if (data.startsWith("@") && arg != "--data-raw") {
                    val file = resolveTerminalPath(rootDirectory, workingDirectory, data.removePrefix("@"))
                        ?: return ShellCommandResult(error = "curl: option $arg: error encountered when reading a file")
                    runCatching { file.readText() }.getOrElse {
                        return ShellCommandResult(error = "curl: option $arg: error encountered when reading a file")
                    }
                } else {
                    data
                }
                dataParts += resolvedData
                index += 2
            }
            arg == "--data-urlencode" -> {
                val data = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                dataParts += URLEncoder.encode(data, "UTF-8")
                index += 2
            }
            arg == "--json" -> {
                val data = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                dataParts += data
                headers.putIfAbsent("Content-Type", "application/json")
                headers.putIfAbsent("Accept", "application/json")
                index += 2
            }
            arg == "-F" || arg == "--form" -> {
                formParts += requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                index += 2
            }
            arg == "-u" || arg == "--user" -> {
                val credentials = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                val encoded = Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                headers["Authorization"] = "Basic $encoded"
                index += 2
            }
            arg == "-A" || arg == "--user-agent" -> {
                headers["User-Agent"] = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                index += 2
            }
            arg == "-e" || arg == "--referer" -> {
                headers["Referer"] = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                index += 2
            }
            arg == "-b" || arg == "--cookie" -> {
                headers["Cookie"] = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                index += 2
            }
            arg == "-o" || arg == "--output" -> {
                outputPath = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                index += 2
            }
            arg == "-O" || arg == "--remote-name" -> {
                useRemoteName = true
                index += 1
            }
            arg == "--connect-timeout" -> {
                val seconds = requiredValue(index)?.toDoubleOrNull()
                    ?: return ShellCommandResult(error = "curl: option $arg: expected a proper numerical parameter")
                connectTimeoutMs = (seconds * 1000).toInt().coerceAtLeast(1)
                index += 2
            }
            arg == "-m" || arg == "--max-time" -> {
                val seconds = requiredValue(index)?.toDoubleOrNull()
                    ?: return ShellCommandResult(error = "curl: option $arg: expected a proper numerical parameter")
                readTimeoutMs = (seconds * 1000).toInt().coerceAtLeast(1)
                index += 2
            }
            arg == "--url" -> {
                urlText = requiredValue(index)
                    ?: return ShellCommandResult(error = "curl: option $arg: requires parameter")
                index += 2
            }
            arg in setOf("-i", "--include") -> {
                includeHeaders = true
                index += 1
            }
            arg in setOf("-I", "--head") -> {
                headOnly = true
                method = "HEAD"
                includeHeaders = true
                index += 1
            }
            arg in setOf("-L", "--location") -> {
                followRedirects = true
                index += 1
            }
            arg in setOf("-f", "--fail", "--fail-with-body") -> {
                failOnHttpError = true
                index += 1
            }
            arg == "-G" || arg == "--get" -> {
                useGetWithData = true
                index += 1
            }
            arg in setOf("-s", "--silent", "-S", "--show-error", "--compressed") -> index += 1
            arg.startsWith("-") -> return ShellCommandResult(
                error = "curl: option $arg: is unknown\ncurl: try 'curl --help' for more information"
            )
            urlText == null -> {
                urlText = arg
                index += 1
            }
            else -> return ShellCommandResult(error = "curl: (3) URL rejected: Bad hostname")
        }
    }

    var requestUrl = urlText ?: return ShellCommandResult(
        error = "curl: no URL specified!\ncurl: try 'curl --help' for more information"
    )
    if (useGetWithData && dataParts.isNotEmpty()) {
        requestUrl += (if ('?' in requestUrl) "&" else "?") + dataParts.joinToString("&")
        dataParts.clear()
    }

    val url = runCatching { URL(requestUrl) }.getOrElse {
        return ShellCommandResult(error = "curl: (3) URL rejected: ${it.message ?: "Malformed input"}")
    }
    if (url.protocol !in setOf("http", "https")) {
        return ShellCommandResult(error = "curl: (1) Protocol '${url.protocol}' not supported")
    }

    val multipart = if (formParts.isNotEmpty()) {
        buildCurlMultipartBody(formParts, rootDirectory, workingDirectory)
            ?: return ShellCommandResult(error = "curl: (26) Failed to open/read local data from file/application")
    } else {
        null
    }
    val body = multipart?.second ?: dataParts.takeIf { it.isNotEmpty() }
        ?.joinToString("&")
        ?.toByteArray(Charsets.UTF_8)
    multipart?.first?.let { headers.putIfAbsent("Content-Type", it) }
    val requestMethod = method?.uppercase() ?: if (body != null) "POST" else "GET"

    return try {
        val connection = (url.openConnection() as HttpURLConnection).apply {
            this.requestMethod = requestMethod
            instanceFollowRedirects = followRedirects
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            useCaches = false
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
            if (body != null && requestMethod !in setOf("GET", "HEAD")) {
                doOutput = true
                setFixedLengthStreamingMode(body.size)
            }
        }
        if (runController?.isStopped() == true) {
            connection.disconnect()
            return ShellCommandResult(stopped = true)
        }
        body?.takeIf { requestMethod !in setOf("GET", "HEAD") }?.let { bytes ->
            connection.outputStream.use { it.write(bytes) }
        }
        val responseCode = connection.responseCode
        val responseHeaders = curlResponseHeaders(connection)
        val responseContentType = connection.contentType
        val responseBytes = if (headOnly) {
            ByteArray(0)
        } else {
            val stream = if (responseCode >= 400) connection.errorStream else connection.inputStream
            stream?.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    if (runController?.isStopped() == true) break
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                }
                output.toByteArray()
            } ?: ByteArray(0)
        }
        connection.disconnect()
        if (runController?.isStopped() == true) return ShellCommandResult(stopped = true)

        val targetName = when {
            outputPath != null -> outputPath
            useRemoteName -> url.path.substringAfterLast('/').ifBlank { "curl_response" }
            else -> null
        }
        if (targetName != null) {
            val target = resolveTerminalPath(rootDirectory, workingDirectory, targetName)
                ?: return ShellCommandResult(error = "curl: (23) Failure writing output to destination")
            runCatching {
                target.parentFile?.mkdirs()
                target.writeBytes(responseBytes)
            }.getOrElse {
                return ShellCommandResult(error = "curl: (23) Failure writing output to destination")
            }
        }

        if (failOnHttpError && responseCode >= 400) {
            return ShellCommandResult(error = "curl: (22) The requested URL returned error: $responseCode")
        }
        val charsetName = responseContentType
            ?.substringAfter("charset=", "")
            ?.substringBefore(';')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "UTF-8"
        val responseText = runCatching { responseBytes.toString(charset(charsetName)) }
            .getOrElse { responseBytes.toString(Charsets.UTF_8) }
        ShellCommandResult(
            output = buildString {
                if (includeHeaders) append(responseHeaders)
                if (targetName == null && !headOnly) append(responseText)
            }.trimEnd(),
            workspaceChanged = targetName != null
        )
    } catch (_: UnknownHostException) {
        ShellCommandResult(error = "curl: (6) Could not resolve host: ${url.host}")
    } catch (_: SocketTimeoutException) {
        ShellCommandResult(error = "curl: (28) Operation timed out")
    } catch (_: ConnectException) {
        ShellCommandResult(error = "curl: (7) Failed to connect to ${url.host}")
    } catch (error: SSLException) {
        ShellCommandResult(error = "curl: (60) SSL certificate problem: ${error.message ?: "certificate verification failed"}")
    } catch (error: Exception) {
        ShellCommandResult(error = "curl: (7) ${error.message ?: "Failed to connect"}")
    }
}

private fun buildCurlMultipartBody(
    parts: List<String>,
    rootDirectory: File,
    workingDirectory: File,
): Pair<String, ByteArray>? {
    val boundary = "----PocketCodedPy${System.currentTimeMillis()}"
    val output = ByteArrayOutputStream()
    parts.forEach { part ->
        val separator = part.indexOf('=')
        if (separator <= 0) return null
        val name = part.substring(0, separator)
        val value = part.substring(separator + 1)
        output.write("--$boundary\r\n".toByteArray())
        if (value.startsWith("@")) {
            val file = resolveTerminalPath(rootDirectory, workingDirectory, value.removePrefix("@"))
                ?.takeIf { it.isFile }
                ?: return null
            output.write(
                "Content-Disposition: form-data; name=\"$name\"; filename=\"${file.name}\"\r\n".toByteArray()
            )
            output.write("Content-Type: application/octet-stream\r\n\r\n".toByteArray())
            file.inputStream().use { it.copyTo(output) }
            output.write("\r\n".toByteArray())
        } else {
            output.write("Content-Disposition: form-data; name=\"$name\"\r\n\r\n".toByteArray())
            output.write(value.toByteArray(Charsets.UTF_8))
            output.write("\r\n".toByteArray())
        }
    }
    output.write("--$boundary--\r\n".toByteArray())
    return "multipart/form-data; boundary=$boundary" to output.toByteArray()
}

private fun curlResponseHeaders(connection: HttpURLConnection): String {
    return buildString {
        var index = 0
        while (true) {
            val key = connection.getHeaderFieldKey(index)
            val value = connection.getHeaderField(index)
            if (key == null && value == null) break
            if (key == null) append(value.orEmpty()) else append(key).append(": ").append(value.orEmpty())
            append("\r\n")
            index += 1
        }
        append("\r\n")
    }
}

private fun builtInCurlHelp(): String {
    return """
        Usage: curl [options] <url>
        -X, --request <method>       HTTP method
        -H, --header <header>       Add request header
        -d, --data <data>           Send request body
            --json <json>           Send JSON body
        -F, --form <name=value>     Send multipart form data; use name=@file
        -u, --user <user:password>  Basic authentication
        -i, --include               Include response headers
        -I, --head                  Fetch headers only
        -L, --location              Follow redirects
        -o, --output <file>         Save response body
        -O, --remote-name           Save using the remote filename
        -f, --fail                  Fail on HTTP 400 or greater
        -m, --max-time <seconds>    Request timeout

        Examples:
        curl https://api.example.com/items
        curl -X POST -H "Content-Type: application/json" -d '{"name":"Ada"}' https://api.example.com/items
        curl --json '{"name":"Ada"}' https://api.example.com/items
        curl -H "Authorization: Bearer TOKEN" https://api.example.com/me
    """.trimIndent()
}

private fun executeBuiltInBash(
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
    runController: TerminalRunController? = null,
): ShellCommandResult {
    val realBash = findTerminalExecutable(rootDirectory, workingDirectory)
    val shell = realBash ?: File("/system/bin/sh")
    if (!shell.isFile) {
        return ShellCommandResult(error = "bash: shell runtime is not available on this device")
    }

    if (args.firstOrNull() in setOf("--version", "-version")) {
        return if (realBash != null) {
            executeTerminalProcess(
                command = listOf(realBash.absolutePath, "--version"),
                rootDirectory = rootDirectory,
                workingDirectory = workingDirectory,
                runController = runController,
                shell = realBash,
                workspaceMayChange = false,
            )
        } else {
            ShellCommandResult(
                output = "PocketCodedPy bash compatibility shell (powered by ${shell.absolutePath})"
            )
        }
    }
    if (args.firstOrNull() in setOf("--help", "-h")) {
        return ShellCommandResult(output = builtInBashHelp(realBash != null))
    }
    if (args.isEmpty()) {
        return ShellCommandResult(
            output = "PocketCodedPy terminal is already in shell mode. Use bash -c \"command\" or bash script.sh."
        )
    }

    val command = if (args.first() == "-c") {
        val source = args.getOrNull(1)
            ?: return ShellCommandResult(error = "bash: -c: option requires an argument")
        buildList {
            add(shell.absolutePath)
            add("-c")
            add(source)
            addAll(args.drop(2))
        }
    } else {
        val optionArgs = mutableListOf<String>()
        var scriptIndex = 0
        while (scriptIndex < args.size) {
            val arg = args[scriptIndex]
            if (arg == "--") {
                scriptIndex += 1
                break
            }
            if (!arg.startsWith("-") || arg == "-") break
            if (arg.any { option -> option !in "-euxnvo" }) {
                return ShellCommandResult(error = "bash: $arg: invalid option")
            }
            optionArgs += arg
            scriptIndex += 1
        }
        val rawScript = args.getOrNull(scriptIndex)
            ?: return ShellCommandResult(error = "bash: no script specified")
        val script = resolveTerminalPath(rootDirectory, workingDirectory, rawScript)
            ?: return ShellCommandResult(error = "bash: $rawScript: Permission denied")
        when {
            !script.exists() -> return ShellCommandResult(error = "bash: $rawScript: No such file or directory")
            !script.isFile -> return ShellCommandResult(error = "bash: $rawScript: Is a directory")
        }
        buildList {
            add(shell.absolutePath)
            addAll(optionArgs)
            add(script.absolutePath)
            addAll(args.drop(scriptIndex + 1))
        }
    }

    return executeTerminalProcess(
        command = command,
        rootDirectory = rootDirectory,
        workingDirectory = workingDirectory,
        runController = runController,
        shell = shell
    )
}

private fun builtInBashHelp(realBashAvailable: Boolean): String {
    val runtime = if (realBashAvailable) {
        "A real Android-compatible Bash binary is available."
    } else {
        "Bash-compatible mode uses /system/bin/sh. GNU Bash-only features require a Bash binary in ~/bin or the app bin directory."
    }
    return """
        Usage: bash -c "command" [name [args]]
               bash [options] script.sh [args]

        Supported compatibility flags: -e, -u, -x, -n, -v
        $runtime
    """.trimIndent()
}

private fun findTerminalExecutable(
    rootDirectory: File,
    workingDirectory: File,
): File? {
    val name = "bash"
    val candidates = listOf(
        File(terminalRuntimeDirectory(rootDirectory), "bin/$name"),
        File(rootDirectory, "bin/$name"),
        File(rootDirectory, ".local/bin/$name"),
        File(workingDirectory, name),
        File("/system/bin/$name"),
        File("/system/xbin/$name"),
        File("/system_ext/bin/$name"),
        File("/product/bin/$name")
    )
    return candidates.firstOrNull { it.isFile && it.canExecute() }
}

private fun executeSystemShellCommand(
    command: String,
    rootDirectory: File,
    workingDirectory: File,
    runController: TerminalRunController? = null,
): ShellCommandResult {
    val shell = File("/system/bin/sh")
    if (!shell.isFile) {
        return ShellCommandResult(error = "shell: /system/bin/sh is not available on this device")
    }

    return executeTerminalProcess(
        command = listOf(shell.absolutePath, "-c", command),
        rootDirectory = rootDirectory,
        workingDirectory = workingDirectory,
        runController = runController,
        shell = shell,
        workspaceMayChange = shellCommandMayChangeWorkspace(command),
    )
}

private fun shellCommandMayChangeWorkspace(command: String): Boolean {
    if ('>' in command) return true
    val arguments = splitCommandLine(command)
    val executable = arguments.firstOrNull()?.substringAfterLast('/')?.lowercase().orEmpty()
    if (executable == "sed" && arguments.drop(1).any { it == "-i" || it.startsWith("-i") }) return true
    if (executable == "find" && arguments.any { it in setOf("-delete", "-exec", "-execdir") }) return true
    if (Regex("(?i)(^|[|;&]\\s*)tee\\b").containsMatchIn(command)) return true
    return executable !in setOf(
        "",
        "awk",
        "cat",
        "date",
        "df",
        "du",
        "echo",
        "env",
        "find",
        "grep",
        "head",
        "id",
        "ls",
        "printf",
        "ps",
        "pwd",
        "sed",
        "sort",
        "tail",
        "uname",
        "wc",
        "which",
        "whoami",
    )
}

private fun executeTerminalProcess(
    command: List<String>,
    rootDirectory: File,
    workingDirectory: File,
    runController: TerminalRunController? = null,
    shell: File = File("/system/bin/sh"),
    workspaceMayChange: Boolean = true,
): ShellCommandResult {

    return runCatching {
        val outputLock = Any()
        val outputBuffer = StringBuilder()
        val process = ProcessBuilder(command)
            .directory(workingDirectory)
            .redirectErrorStream(true)
            .apply {
                val environment = environment()
                val existingPath = environment["PATH"].orEmpty()
                val runtimeDirectory = terminalRuntimeDirectory(rootDirectory)
                val appBinDirectory = File(runtimeDirectory, "bin").apply {
                    mkdirs()
                }
                val terminalPath = listOf(
                    appBinDirectory.absolutePath,
                    File(rootDirectory, "bin").absolutePath,
                    File(rootDirectory, ".local/bin").absolutePath,
                    "/system/bin",
                    "/system/xbin",
                    "/system_ext/bin",
                    "/vendor/bin",
                    "/product/bin",
                    "/odm/bin",
                    "/apex/com.android.runtime/bin",
                    "/apex/com.android.art/bin",
                    workingDirectory.absolutePath,
                    rootDirectory.absolutePath,
                ).joinToString(":")
                environment["HOME"] = rootDirectory.absolutePath
                environment["PWD"] = workingDirectory.absolutePath
                environment["TMPDIR"] = File(runtimeDirectory, "tmp").apply {
                    mkdirs()
                }.absolutePath
                environment["TERM"] = "xterm-256color"
                environment["SHELL"] = shell.absolutePath
                environment["LANG"] = "C.UTF-8"
                environment["LC_ALL"] = "C.UTF-8"
                environment["PATH"] = listOf(terminalPath, existingPath)
                    .filter { it.isNotBlank() }
                    .joinToString(":")
            }
            .start()
        runController?.attachProcess(process)

        val outputThread = Thread {
            runCatching {
                process.inputStream.use { input ->
                    val buffer = ByteArray(4096)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        val chunk = String(buffer, 0, read, Charsets.UTF_8)
                        synchronized(outputLock) {
                            outputBuffer.append(chunk)
                        }
                    }
                }
            }
        }.apply {
            isDaemon = true
            start()
        }

        val finished = waitForProcessCompat(process, runController)
        outputThread.join(2_000)
        if (outputThread.isAlive) {
            runCatching { process.inputStream.close() }
            outputThread.join(250)
        }
        val output = synchronized(outputLock) { outputBuffer.toString() }
        if (runController?.isStopped() == true) {
            return@runCatching ShellCommandResult(
                output = output,
                workspaceChanged = workspaceMayChange,
                stopped = true
            )
        }
        if (!finished) {
            process.destroy()
            return@runCatching ShellCommandResult(
                error = "shell: command timed out after 300 seconds",
                workspaceChanged = workspaceMayChange
            )
        }
        ShellCommandResult(
            output = output,
            workspaceChanged = workspaceMayChange
        )
    }.getOrElse { error ->
        ShellCommandResult(
            error = error.message ?: "shell: command failed",
            workspaceChanged = workspaceMayChange
        )
    }
}

private fun terminalRuntimeDirectory(rootDirectory: File): File {
    val projectContainer = rootDirectory.parentFile
    val filesDirectory = if (projectContainer?.name == "projects") {
        projectContainer.parentFile
    } else {
        projectContainer
    } ?: rootDirectory
    return File(filesDirectory, ".runtime").apply { mkdirs() }
}

private fun waitForProcessCompat(
    process: Process,
    runController: TerminalRunController?,
): Boolean {
    val deadline = System.currentTimeMillis() + TERMINAL_PROCESS_TIMEOUT_MS
    while (System.currentTimeMillis() < deadline) {
        if (runController?.isStopped() == true) {
            process.destroy()
            return true
        }
        runCatching {
            process.exitValue()
            return true
        }
        Thread.sleep(50)
    }
    process.destroy()
    return false
}

private fun executeBuiltInGitCommand(
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
    activeEnvironment: File? = null,
): ShellCommandResult {
    return runCatching {
        configureJGitSandboxHome(rootDirectory)
        ensureSandboxSshFiles(rootDirectory)
        // If we are in a virtual environment, we might want to set some environment variables for JGit
        // but JGit is internal, so we mostly just need to ensure the sandbox is correct.
        when (val subcommand = args.firstOrNull()) {
            null, "-h", "--help", "help" -> ShellCommandResult(output = builtInGitHelp())
            "--version", "-v", "version" -> ShellCommandResult(
                output = "git version 2.45.0 (PocketCodedPy built-in JGit $JGIT_VERSION)"
            )
            "init" -> gitInit(args.drop(1), rootDirectory, workingDirectory)
            "clone" -> gitClone(args.drop(1), rootDirectory, workingDirectory)
            "status" -> withGitRepository(workingDirectory) { gitStatus(it) }
            "add" -> withGitRepository(workingDirectory) { gitAdd(it, args.drop(1), rootDirectory, workingDirectory) }
            "commit" -> withGitRepository(workingDirectory) { gitCommit(it, args.drop(1), rootDirectory) }
            "log" -> withGitRepository(workingDirectory) { gitLog(it, args.drop(1)) }
            "branch" -> withGitRepository(workingDirectory) { gitBranch(it, args.drop(1)) }
            "checkout", "switch" -> withGitRepository(workingDirectory) { gitCheckout(it, args.drop(1)) }
            "remote" -> withGitRepository(workingDirectory) { gitRemote(it, args.drop(1)) }
            "config" -> gitConfig(args.drop(1), rootDirectory, workingDirectory)
            "pull" -> withGitRepository(workingDirectory) { gitPull(it, args.drop(1)) }
            "push" -> withGitRepository(workingDirectory) { gitPush(it, args.drop(1)) }
            else -> ShellCommandResult(
                error = "git: '$subcommand' is not a git command. See 'git --help'."
            )
        }
    }.getOrElse { error ->
        ShellCommandResult(error = "git: ${gitErrorMessage(error)}")
    }
}

private fun builtInGitHelp(): String {
    return """
        Built-in Git commands
        git init [dir]
        git clone [-b branch] <ssh-or-https-url> [dir]
        git status
        git add <path>|. [-A]
        git commit -m "message"
        git log [--oneline] [-n count]
        git branch [-a] | git branch <name> | git branch -d <name>
        git checkout <branch> | git checkout -b <branch>
        git remote [-v] | git remote add <name> <url> | git remote set-url <name> <url>
        git config [--global] user.name "Name"
        git config [--global] user.email "you@example.com"
        git pull [remote] [branch]
        git push [remote] [branch]

        SSH keys live in ~/.ssh inside this app sandbox.
        Use ssh-keygen -t ed25519 -C "you@example.com"
        Or  ssh-keygen -t rsa -b 4096 -C "you@example.com"
    """.trimIndent()
}

private fun gitInit(
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
): ShellCommandResult {
    if (args.any { it == "--bare" }) {
        return ShellCommandResult(error = "git init: bare repositories are not supported in the built-in terminal")
    }
    val targetArg = args.firstOrNull { !it.startsWith("-") } ?: "."
    val directory = resolveTerminalPath(rootDirectory, workingDirectory, targetArg)
        ?: return ShellCommandResult(error = "git init: outside sandbox")
    if (directory.exists() && !directory.isDirectory) {
        return ShellCommandResult(error = "git init: ${targetArg}: not a directory")
    }
    directory.mkdirs()
    val git = Git.init().setDirectory(directory).call()
    git.close()
    return ShellCommandResult(
        output = "Initialized empty Git repository in ${relativeDirectoryLabel(rootDirectory, directory)}/.git",
        workspaceChanged = true
    )
}

private fun gitClone(
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
): ShellCommandResult {
    var branch: String? = null
    var depth: Int? = null
    var singleBranch = false
    val positionals = mutableListOf<String>()
    var index = 0
    while (index < args.size) {
        val arg = args[index]
        when {
            arg == "-b" || arg == "--branch" -> {
                branch = args.getOrNull(index + 1)
                    ?: return ShellCommandResult(error = "git clone: missing branch after $arg")
                index += 2
            }
            arg.startsWith("--branch=") -> {
                branch = arg.substringAfter("=")
                index += 1
            }
            arg == "--depth" -> {
                depth = args.getOrNull(index + 1)?.toIntOrNull()
                    ?: return ShellCommandResult(error = "git clone: missing depth after --depth")
                index += 2
            }
            arg.startsWith("--depth=") -> {
                depth = arg.substringAfter("=").toIntOrNull()
                    ?: return ShellCommandResult(error = "git clone: invalid depth ${arg.substringAfter("=")}")
                index += 1
            }
            arg == "--single-branch" -> {
                singleBranch = true
                index += 1
            }
            arg.startsWith("-") -> {
                return ShellCommandResult(error = "git clone: option $arg is not supported by the built-in clone")
            }
            else -> {
                positionals += arg
                index += 1
            }
        }
    }

    val rawRemoteUrl = positionals.getOrNull(0)
        ?: return ShellCommandResult(error = "git clone: missing repository URL")
    if (rawRemoteUrl.startsWith("git://", ignoreCase = true)) {
        return ShellCommandResult(
            error = "git clone: git:// remotes are not supported by GitHub/GitLab anymore. Use https://... or git@host:owner/repo.git"
        )
    }
    val remoteUrl = normalizeGitRemoteUrl(rawRemoteUrl)
    val displayRemoteUrl = redactRemoteUrlForDisplay(remoteUrl)
    if (isSshRemoteUrl(remoteUrl) && sandboxSshIdentities(rootDirectory).isEmpty()) {
        return ShellCommandResult(
            error = """
                git clone: no SSH key found in ~/.ssh.
                Run: ssh-keygen -t ed25519 -C "you@example.com"
                Then add the printed .pub key to GitHub/GitLab and try cloning again.
            """.trimIndent()
        )
    }

    val directoryName = positionals.getOrNull(1) ?: repositoryNameFromUrl(remoteUrl)
    val targetDirectory = resolveTerminalPath(rootDirectory, workingDirectory, directoryName)
        ?: return ShellCommandResult(error = "git clone: outside sandbox")
    if (targetDirectory.exists() && targetDirectory.listFiles().orEmpty().isNotEmpty()) {
        return ShellCommandResult(error = "git clone: ${directoryName}: directory is not empty")
    }

    val clone = Git.cloneRepository()
        .setURI(remoteUrl)
        .setDirectory(targetDirectory)
        .setBare(false)
        .setNoCheckout(false)
        .setTimeout(120)
    depth?.let { clone.setDepth(it) }
    if (singleBranch) {
        clone.setCloneAllBranches(false)
    }
    branch?.takeIf { it.isNotBlank() }?.let { clone.setBranch(it) }
    credentialsProviderFor(remoteUrl)?.let { clone.setCredentialsProvider(it) }
    val git = runCatching { clone.call() }.getOrElse { error ->
        return ShellCommandResult(error = gitCloneError(rawRemoteUrl, remoteUrl, error))
    }
    val checkoutNote = runCatching {
        ensureCloneWorkingTree(git, branch, targetDirectory)
    }.getOrElse { error ->
        git.close()
        return ShellCommandResult(error = gitCloneError(rawRemoteUrl, remoteUrl, error))
    }
    git.close()
    val normalizedNote = if (remoteUrl != rawRemoteUrl) {
        "\nUsed repository URL: $displayRemoteUrl"
    } else {
        ""
    }
    return ShellCommandResult(
        output = buildString {
            append("Cloned $displayRemoteUrl into ${relativeDirectoryLabel(rootDirectory, targetDirectory)}")
            checkoutNote?.takeIf { it.isNotBlank() }?.let { note ->
                append('\n').append(note)
            }
            append(normalizedNote)
        },
        workspaceChanged = true
    )
}

private fun ensureCloneWorkingTree(
    git: Git,
    requestedBranch: String?,
    targetDirectory: File,
): String? {
    if (hasWorkspaceFilesOutsideMetadata(targetDirectory)) return null

    runCatching {
        git.reset()
            .setMode(ResetCommand.ResetType.HARD)
            .setRef(Constants.HEAD)
            .call()
    }
    if (hasWorkspaceFilesOutsideMetadata(targetDirectory)) return "Checked out working tree files."

    val checkedOutBranch = checkoutBestRemoteBranch(git, requestedBranch)
    if (checkedOutBranch != null) {
        runCatching {
            git.reset()
                .setMode(ResetCommand.ResetType.HARD)
                .setRef(Constants.HEAD)
                .call()
        }
        if (hasWorkspaceFilesOutsideMetadata(targetDirectory)) {
            return "Checked out $checkedOutBranch."
        }
    }

    val repository = git.repository
    val remoteBranches = remoteBranchNames(repository)
    val hasHead = runCatching { repository.resolve(Constants.HEAD) != null }.getOrDefault(false)
    val headHasFiles = hasHead && repositoryHeadHasFiles(repository)
    return when {
        !hasHead && remoteBranches.isEmpty() ->
            "Remote repository has no commits yet, so only Git metadata was created."
        !headHasFiles ->
            "The checked-out branch has no files yet. Add files to the repo or clone a branch that contains files."
        remoteBranches.isNotEmpty() ->
            "No working files were checked out. Available remote branches: ${remoteBranches.joinToString(", ")}"
        else ->
            "No working files were checked out."
    }
}

private fun checkoutBestRemoteBranch(
    git: Git,
    requestedBranch: String?,
): String? {
    val repository = git.repository
    val remoteRefs = repository.refDatabase
        .getRefsByPrefix(Constants.R_REMOTES + "origin/")
        .filterNot { it.name.endsWith("/HEAD") }
    if (remoteRefs.isEmpty()) return null

    val preferredRefs = listOfNotNull(
        requestedBranch?.takeIf { it.isNotBlank() }?.let { remoteRefNameForBranch(it) },
        Constants.R_REMOTES + "origin/main",
        Constants.R_REMOTES + "origin/master",
    ) + remoteRefs.map { it.name }

    val refName = preferredRefs.firstOrNull { preferred ->
        remoteRefs.any { it.name == preferred }
    } ?: return null
    val localBranch = Repository.shortenRefName(refName).removePrefix("origin/")
    val localExists = repository.exactRef(Constants.R_HEADS + localBranch) != null
    val checkout = git.checkout().setName(localBranch)
    if (!localExists) {
        checkout
            .setCreateBranch(true)
            .setStartPoint(refName)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
    }
    checkout.call()
    return localBranch
}

private fun remoteRefNameForBranch(branch: String): String {
    val cleanBranch = branch
        .removePrefix(Constants.R_HEADS)
        .removePrefix(Constants.R_REMOTES)
    return if (cleanBranch.startsWith("origin/")) {
        Constants.R_REMOTES + cleanBranch
    } else {
        Constants.R_REMOTES + "origin/$cleanBranch"
    }
}

private fun remoteBranchNames(repository: Repository): List<String> {
    return repository.refDatabase
        .getRefsByPrefix(Constants.R_REMOTES + "origin/")
        .filterNot { it.name.endsWith("/HEAD") }
        .map { Repository.shortenRefName(it.name).removePrefix("origin/") }
        .distinct()
        .sorted()
}

private fun repositoryHeadHasFiles(repository: Repository): Boolean {
    val head = runCatching { repository.resolve(Constants.HEAD) }.getOrNull() ?: return false
    RevWalk(repository).use { revWalk ->
        val commit = revWalk.parseCommit(head)
        TreeWalk(repository).use { treeWalk ->
            treeWalk.addTree(commit.tree)
            treeWalk.isRecursive = true
            return treeWalk.next()
        }
    }
}

private fun gitStatus(git: Git): ShellCommandResult {
    val repository = git.repository
    val status = git.status().call()
    val lines = mutableListOf<String>()
    lines += "On branch ${currentBranchLabel(repository)}"
    if (status.isClean) {
        lines += "working tree clean"
    } else {
        appendGitStatusSection(lines, "Changes to be committed", status.added + status.changed + status.removed)
        appendGitStatusSection(lines, "Changes not staged for commit", status.modified + status.missing)
        appendGitStatusSection(lines, "Untracked files", status.untracked)
        appendGitStatusSection(lines, "Conflicts", status.conflicting)
    }
    return ShellCommandResult(output = lines.joinToString("\n"))
}

private fun appendGitStatusSection(
    lines: MutableList<String>,
    title: String,
    paths: Set<String>,
) {
    if (paths.isEmpty()) return
    lines += ""
    lines += title
    paths.sorted().forEach { path -> lines += "  $path" }
}

private fun gitAdd(
    git: Git,
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
): ShellCommandResult {
    if (args.isEmpty()) return ShellCommandResult(error = "git add: missing path")
    val stageAll = args.any { it == "." || it == "-A" || it == "--all" }
    val rawPaths = args.filter { !it.startsWith("-") }.ifEmpty { listOf(".") }
    val patterns = rawPaths.map { rawPath ->
        gitPathPattern(git.repository, rootDirectory, workingDirectory, rawPath)
            ?: return ShellCommandResult(error = "git add: ${rawPath}: outside repository")
    }.distinct()

    val add = git.add()
    patterns.forEach { add.addFilepattern(it) }
    add.call()
    if (stageAll) {
        git.add().setUpdate(true).addFilepattern(".").call()
    }
    return ShellCommandResult(output = "staged ${patterns.joinToString(", ")}", workspaceChanged = true)
}

private fun gitCommit(
    git: Git,
    args: List<String>,
    rootDirectory: File,
): ShellCommandResult {
    val message = gitCommitMessage(args)
        ?: return ShellCommandResult(error = "git commit: use -m \"message\"")
    val (name, email) = gitIdentity(git.repository, rootDirectory)
    val commit = git.commit()
        .setMessage(message)
        .setAuthor(name, email)
        .setCommitter(name, email)
        .call()
    return ShellCommandResult(
        output = "[${commit.name.take(7)}] ${commit.shortMessage}",
        workspaceChanged = true
    )
}

private fun gitLog(
    git: Git,
    args: List<String>,
): ShellCommandResult {
    var maxCount = 10
    val oneline = args.any { it == "--oneline" }
    var index = 0
    while (index < args.size) {
        val arg = args[index]
        when {
            arg == "-n" -> {
                maxCount = args.getOrNull(index + 1)?.toIntOrNull() ?: maxCount
                index += 2
            }
            arg.startsWith("-n") && arg.length > 2 -> {
                maxCount = arg.drop(2).toIntOrNull() ?: maxCount
                index += 1
            }
            else -> index += 1
        }
    }

    val commits = git.log().setMaxCount(maxCount).call().toList()
    if (commits.isEmpty()) return ShellCommandResult(output = "(no commits)")
    val output = commits.joinToString(if (oneline) "\n" else "\n\n") { commit ->
        if (oneline) {
            "${commit.name.take(7)} ${commit.shortMessage}"
        } else {
            "commit ${commit.name}\nAuthor: ${commit.authorIdent.name} <${commit.authorIdent.emailAddress}>\n\n    ${commit.fullMessage.trim().replace("\n", "\n    ")}"
        }
    }
    return ShellCommandResult(output = output)
}

private fun gitBranch(
    git: Git,
    args: List<String>,
): ShellCommandResult {
    val repository = git.repository
    if (args.isEmpty() || args.first() == "-a") {
        val command = git.branchList()
        if (args.firstOrNull() == "-a") {
            command.setListMode(ListBranchCommand.ListMode.ALL)
        }
        val current = repository.fullBranch
        val output = command.call().joinToString("\n") { ref ->
            val shortName = Repository.shortenRefName(ref.name)
            val marker = if (ref.name == current || shortName == currentBranchLabel(repository)) "*" else " "
            "$marker $shortName"
        }
        return ShellCommandResult(output = output.ifBlank { "(no branches)" })
    }

    return when (args.first()) {
        "-d", "-D" -> {
            val branchName = args.getOrNull(1)
                ?: return ShellCommandResult(error = "git branch ${args.first()}: missing branch")
            val deleted = git.branchDelete()
                .setBranchNames(branchName)
                .setForce(args.first() == "-D")
                .call()
            ShellCommandResult(output = "Deleted ${deleted.joinToString(", ") { Repository.shortenRefName(it) }}", workspaceChanged = true)
        }
        else -> {
            val branchName = args.first()
            git.branchCreate().setName(branchName).call()
            ShellCommandResult(output = "Created branch $branchName", workspaceChanged = true)
        }
    }
}

private fun gitCheckout(
    git: Git,
    args: List<String>,
): ShellCommandResult {
    if (args.isEmpty()) return ShellCommandResult(error = "git checkout: missing branch")
    val createBranch = args.first() == "-b"
    val branchName = if (createBranch) args.getOrNull(1) else args.first()
    if (branchName.isNullOrBlank()) return ShellCommandResult(error = "git checkout: missing branch")
    git.checkout()
        .setCreateBranch(createBranch)
        .setName(branchName)
        .call()
    return ShellCommandResult(
        output = if (createBranch) "Created and switched to branch $branchName" else "Switched to branch $branchName",
        workspaceChanged = true
    )
}

private fun gitRemote(
    git: Git,
    args: List<String>,
): ShellCommandResult {
    val repository = git.repository
    val config = repository.config
    if (args.isEmpty() || args.first() == "-v") {
        val verbose = args.firstOrNull() == "-v"
        val output = config.getSubsections("remote").sorted().flatMap { name ->
            val urls = config.getStringList("remote", name, "url").toList()
            if (verbose) {
                urls.flatMap { url -> listOf("$name\t$url (fetch)", "$name\t$url (push)") }
            } else {
                listOf(name)
            }
        }.joinToString("\n")
        return ShellCommandResult(output = output.ifBlank { "(no remotes)" })
    }

    return when (args.first()) {
        "add" -> {
            val name = args.getOrNull(1) ?: return ShellCommandResult(error = "git remote add: missing name")
            val url = args.getOrNull(2) ?: return ShellCommandResult(error = "git remote add: missing URL")
            git.remoteAdd().setName(name).setUri(URIish(url)).call()
            ShellCommandResult(output = "Added remote $name", workspaceChanged = true)
        }
        "set-url" -> {
            val name = args.getOrNull(1) ?: return ShellCommandResult(error = "git remote set-url: missing name")
            val url = args.getOrNull(2) ?: return ShellCommandResult(error = "git remote set-url: missing URL")
            config.setString("remote", name, "url", url)
            config.save()
            ShellCommandResult(output = "Updated remote $name", workspaceChanged = true)
        }
        "remove", "rm" -> {
            val name = args.getOrNull(1) ?: return ShellCommandResult(error = "git remote ${args.first()}: missing name")
            git.remoteRemove().setRemoteName(name).call()
            ShellCommandResult(output = "Removed remote $name", workspaceChanged = true)
        }
        else -> ShellCommandResult(error = "git remote: unsupported action ${args.first()}")
    }
}

private fun gitConfig(
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
): ShellCommandResult {
    val global = args.any { it == "--global" }
    val listValues = args.any { it == "--list" || it == "-l" }
    val filteredArgs = args.filterNot { it == "--global" || it == "--local" || it == "--list" || it == "-l" }

    if (listValues) {
        return if (global) {
            val config = loadSandboxGitConfig(rootDirectory)
            ShellCommandResult(output = gitConfigList(config))
        } else {
            withGitRepository(workingDirectory) { git -> ShellCommandResult(output = gitConfigList(git.repository.config)) }
        }
    }

    val key = filteredArgs.firstOrNull() ?: return ShellCommandResult(error = "git config: missing key")
    val (section, name) = splitGitConfigKey(key)
        ?: return ShellCommandResult(error = "git config: expected keys like user.name or user.email")
    val value = filteredArgs.drop(1).joinToString(" ").takeIf { it.isNotBlank() }

    if (global) {
        val config = loadSandboxGitConfig(rootDirectory)
        if (value == null) {
            return ShellCommandResult(output = config.getString(section, null, name).orEmpty())
        }
        config.setString(section, null, name, value)
        config.save()
        return ShellCommandResult(output = "Set $key", workspaceChanged = true)
    }

    return withGitRepository(workingDirectory) { git ->
        val config = git.repository.config
        if (value == null) {
            ShellCommandResult(output = config.getString(section, null, name).orEmpty())
        } else {
            config.setString(section, null, name, value)
            config.save()
            ShellCommandResult(output = "Set $key", workspaceChanged = true)
        }
    }
}

private fun gitPull(
    git: Git,
    args: List<String>,
): ShellCommandResult {
    val positionals = args.filter { !it.startsWith("-") }
    val remote = positionals.getOrNull(0)
    val branch = positionals.getOrNull(1)
    val command = git.pull()
        .setTimeout(120)
    remote?.let { command.setRemote(it) }
    branch?.let { command.setRemoteBranchName(it) }
    credentialsProviderFor(remoteUrlFor(git.repository, remote))?.let { command.setCredentialsProvider(it) }
    val result = command.call()
    val lines = mutableListOf<String>()
    lines += if (result.isSuccessful) "Pull completed successfully." else "Pull completed with issues."
    result.mergeResult?.mergeStatus?.let { lines += "Merge: $it" }
    result.rebaseResult?.status?.let { lines += "Rebase: $it" }
    result.fetchResult?.messages?.takeIf { it.isNotBlank() }?.let { lines += it.trim() }
    return ShellCommandResult(output = lines.joinToString("\n"), workspaceChanged = true)
}

private fun gitPush(
    git: Git,
    args: List<String>,
): ShellCommandResult {
    val positionals = args.filter { !it.startsWith("-") }
    val remote = positionals.getOrNull(0) ?: "origin"
    val branch = positionals.getOrNull(1) ?: currentBranchLabel(git.repository)
    val command = git.push()
        .setRemote(remote)
        .setTimeout(120)
    if (branch.isNotBlank() && branch != "HEAD") {
        command.setRefSpecs(RefSpec("refs/heads/$branch:refs/heads/$branch"))
    }
    credentialsProviderFor(remoteUrlFor(git.repository, remote))?.let { command.setCredentialsProvider(it) }
    val results = command.call()
    return ShellCommandResult(output = formatPushResults(results), workspaceChanged = true)
}

private fun formatPushResults(results: Iterable<PushResult>): String {
    val lines = mutableListOf<String>()
    results.forEach { result ->
        result.messages?.takeIf { it.isNotBlank() }?.let { lines += it.trim() }
        result.remoteUpdates.forEach { update ->
            val message = update.message?.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty()
            lines += "${update.remoteName}: ${update.status}$message"
        }
    }
    return lines.joinToString("\n").ifBlank { "Push completed." }
}

private fun withGitRepository(
    workingDirectory: File,
    block: (Git) -> ShellCommandResult,
): ShellCommandResult {
    val git = openGitRepository(workingDirectory)
        ?: return ShellCommandResult(
            error = "fatal: not a git repository (or any of the parent directories): .git"
        )
    return try {
        block(git)
    } finally {
        git.close()
    }
}

private fun openGitRepository(workingDirectory: File): Git? {
    val builder = FileRepositoryBuilder()
        .readEnvironment()
        .findGitDir(workingDirectory)
    if (builder.gitDir == null) return null
    return Git(builder.build())
}

private fun currentBranchLabel(repository: Repository): String {
    return runCatching {
        val branch = repository.branch
        branch.takeIf { it.isNotBlank() } ?: Constants.HEAD
    }.getOrDefault(Constants.HEAD)
}

private fun gitPathPattern(
    repository: Repository,
    rootDirectory: File,
    workingDirectory: File,
    rawPath: String,
): String? {
    if (rawPath == ".") return "."
    val workTree = repository.workTree?.canonicalFile ?: return null
    val file = resolveTerminalPath(rootDirectory, workingDirectory, rawPath)?.canonicalFile ?: return null
    if (!isSameOrInside(workTree, file)) return null
    if (samePath(workTree.absolutePath, file.absolutePath)) return "."
    return file.absolutePath
        .removePrefix(workTree.absolutePath + File.separator)
        .replace(File.separatorChar, '/')
        .ifBlank { "." }
}

private fun gitCommitMessage(args: List<String>): String? {
    val messages = mutableListOf<String>()
    var index = 0
    while (index < args.size) {
        val arg = args[index]
        when {
            arg == "-m" || arg == "--message" -> {
                args.getOrNull(index + 1)?.let { messages += it }
                index += 2
            }
            arg.startsWith("--message=") -> {
                messages += arg.substringAfter("=")
                index += 1
            }
            else -> index += 1
        }
    }
    return messages.joinToString("\n\n").takeIf { it.isNotBlank() }
}

private fun gitIdentity(
    repository: Repository,
    rootDirectory: File,
): Pair<String, String> {
    val globalConfig = loadSandboxGitConfig(rootDirectory)
    val name = repository.config.getString("user", null, "name")
        ?: globalConfig.getString("user", null, "name")
        ?: "PocketCodedPy"
    val email = repository.config.getString("user", null, "email")
        ?: globalConfig.getString("user", null, "email")
        ?: "pocketcodedpy@local"
    return name to email
}

private fun splitGitConfigKey(key: String): Pair<String, String>? {
    val split = key.lastIndexOf('.')
    if (split <= 0 || split == key.lastIndex) return null
    return key.substring(0, split) to key.substring(split + 1)
}

private fun configureJGitSandboxHome(rootDirectory: File): File {
    val home = rootDirectory.canonicalFile.apply { mkdirs() }
    System.setProperty("user.home", home.absolutePath)
    FS.DETECTED.setUserHome(home)
    return home
}

private fun loadSandboxGitConfig(rootDirectory: File): FileBasedConfig {
    val home = configureJGitSandboxHome(rootDirectory)
    val file = File(home, ".gitconfig")
    val config = FileBasedConfig(file, FS.DETECTED)
    if (file.isFile) {
        runCatching { config.load() }
    }
    return config
}

private fun gitConfigList(config: org.eclipse.jgit.lib.Config): String {
    val lines = mutableListOf<String>()
    config.sections.sorted().forEach { section ->
        config.getNames(section).sorted().forEach { name ->
            lines += "$section.$name=${config.getString(section, null, name).orEmpty()}"
        }
        config.getSubsections(section).sorted().forEach { subsection ->
            config.getNames(section, subsection).sorted().forEach { name ->
                lines += "$section.$subsection.$name=${config.getString(section, subsection, name).orEmpty()}"
            }
        }
    }
    return lines.joinToString("\n").ifBlank { "(empty)" }
}

private fun remoteUrlFor(
    repository: Repository,
    remoteName: String?,
): String? {
    val branchRemote = runCatching {
        repository.config.getString("branch", currentBranchLabel(repository), "remote")
    }.getOrNull()
    val name = remoteName ?: branchRemote ?: "origin"
    return repository.config.getString("remote", name, "url")
}

private fun credentialsProviderFor(remoteUrl: String?): CredentialsProvider? {
    if (remoteUrl == null || !remoteUrl.startsWith("http", ignoreCase = true)) return null
    val userInfo = runCatching { java.net.URI(remoteUrl).rawUserInfo }.getOrNull() ?: return null
    val parts = userInfo.split(":", limit = 2)
    if (parts.isEmpty()) return null
    val username = URLDecoder.decode(parts[0], "UTF-8")
    val password = URLDecoder.decode(parts.getOrElse(1) { "" }, "UTF-8")
    return UsernamePasswordCredentialsProvider(username, password)
}

private fun normalizeGitRemoteUrl(remoteUrl: String): String {
    val trimmed = remoteUrl.trim().trimEnd('/')
    if (isSshRemoteUrl(trimmed)) return trimmed
    val uri = runCatching { URI(trimmed) }.getOrNull() ?: return trimmed
    val scheme = uri.scheme?.takeIf { it.equals("http", ignoreCase = true) || it.equals("https", ignoreCase = true) }
        ?: return trimmed
    val host = uri.host?.lowercase() ?: return trimmed
    val normalizedHost = host.removePrefix("www.")
    val isGitHub = normalizedHost == "github.com"
    val isKnownGitHost = isGitHub || normalizedHost == "gitlab.com"
    if (!isKnownGitHost) return trimmed

    val segments = uri.path
        .trim('/')
        .split('/')
        .filter { it.isNotBlank() }
    val repoSegments = when {
        isGitHub -> segments.take(2)
        else -> segments.takeWhile { it != "-" && it != "tree" && it != "blob" }
    }
    if (repoSegments.size < 2) return trimmed
    val repoPath = repoSegments
        .joinToString("/")
        .removeSuffix(".git")

    val authority = buildString {
        uri.rawUserInfo?.takeIf { it.isNotBlank() }?.let {
            append(it).append('@')
        }
        append(normalizedHost)
        if (uri.port != -1) {
            append(':').append(uri.port)
        }
    }
    return "$scheme://$authority/$repoPath.git"
}

private fun isSshRemoteUrl(remoteUrl: String): Boolean {
    val trimmed = remoteUrl.trim()
    return trimmed.startsWith("ssh://", ignoreCase = true) ||
        Regex("""^[A-Za-z0-9._%+-]+@[^:]+:.+""").matches(trimmed)
}

private fun sandboxSshIdentities(rootDirectory: File): List<File> {
    val sshDirectory = File(rootDirectory, ".ssh")
    return listOf("id_ed25519", "id_rsa", "id_ecdsa", "id_dsa")
        .map { File(sshDirectory, it) }
        .filter { it.isFile && it.length() > 0L }
}

private fun gitCloneError(
    rawRemoteUrl: String,
    normalizedRemoteUrl: String,
    error: Throwable,
): String {
    val messages = gitErrorMessages(error)
    val original = messages.joinToString("\n").ifBlank { error::class.java.simpleName }
    val lowerMessage = original.lowercase()
    val tips = mutableListOf<String>()

    if (rawRemoteUrl != normalizedRemoteUrl) {
        tips += "I normalized the pasted URL to: ${redactRemoteUrlForDisplay(normalizedRemoteUrl)}"
    }
    when {
        "remote hung up unexpectedly" in lowerMessage ||
            "unexpected disconnect" in lowerMessage ||
            "connection reset" in lowerMessage -> {
            tips += "The remote closed the connection before Git data arrived."
            if (isSshRemoteUrl(normalizedRemoteUrl)) {
                tips += "For SSH, make sure the public key from ~/.ssh/id_ed25519.pub or ~/.ssh/id_rsa.pub is added to your GitHub/GitLab account."
            } else {
                tips += "For private repositories, prefer an SSH remote so credentials are not stored in terminal history."
            }
            tips += "If the repo is large or the network is weak, try: git clone --depth 1 ${redactRemoteUrlForDisplay(normalizedRemoteUrl)}"
        }
        "auth" in lowerMessage || "not authorized" in lowerMessage || "401" in lowerMessage || "403" in lowerMessage -> {
            tips += if (isSshRemoteUrl(normalizedRemoteUrl)) {
                "SSH authentication failed. Add your .pub key to GitHub/GitLab, then retry."
            } else {
                "HTTPS authentication failed. GitHub/GitLab passwords do not work here; use a personal access token."
            }
        }
        "not found" in lowerMessage || "404" in lowerMessage -> {
            tips += "The repository was not found. Check the owner/repo name, or use credentials if it is private."
        }
        "unknownhost" in lowerMessage || "unable to resolve host" in lowerMessage -> {
            tips += "DNS failed. Check the device's internet connection and the remote host name."
        }
    }

    return buildString {
        append("git clone failed for ").append(redactRemoteUrlForDisplay(normalizedRemoteUrl)).append('\n')
        if (tips.isNotEmpty()) {
            tips.forEach { append("- ").append(it).append('\n') }
        }
        append("Original error: ").append(redactTerminalCommandForDisplay(original))
    }.trimEnd()
}

private fun repositoryNameFromUrl(remoteUrl: String): String {
    val trimmed = remoteUrl.trim().trimEnd('/')
    val candidate = trimmed
        .substringAfterLast('/')
        .substringAfterLast(':')
        .removeSuffix(".git")
        .replace(Regex("""[^A-Za-z0-9._-]"""), "-")
    return candidate.ifBlank { "repository" }
}

private fun ensureSandboxSshFiles(rootDirectory: File) {
    val homeDirectory = configureJGitSandboxHome(rootDirectory)
    val sshDirectory = File(homeDirectory, ".ssh").apply { mkdirs() }
    val knownHosts = File(sshDirectory, "known_hosts")
    if (!knownHosts.exists()) {
        knownHosts.writeText("")
    }
    val config = File(sshDirectory, "config")
    ensureSandboxSshConfig(config)
    setPrivateFilePermissions(sshDirectory)
    setPrivateFilePermissions(knownHosts)
    setPrivateFilePermissions(config)
    SshSessionFactory.setInstance(
        SshdSessionFactoryBuilder()
            .setHomeDirectory(homeDirectory)
            .setSshDirectory(sshDirectory)
            .setConnectorFactory(null)
            .build(null)
    )
}

private fun ensureSandboxSshConfig(config: File) {
    val defaultBlock = """
        Host *
            StrictHostKeyChecking accept-new
            UserKnownHostsFile ~/.ssh/known_hosts
            IdentityFile ~/.ssh/id_ed25519
            IdentityFile ~/.ssh/id_rsa
    """.trimIndent()
    if (!config.exists()) {
        config.writeText(defaultBlock + "\n")
        return
    }
    val current = config.readText()
    if (!Regex("""(?m)^\s*Host\s+\*($|\s)""").containsMatchIn(current)) {
        val separator = if (current.endsWith("\n")) "" else "\n"
        config.writeText(current + separator + "\n" + defaultBlock + "\n")
    }
}

private fun generateSshKey(
    args: List<String>,
    rootDirectory: File,
    workingDirectory: File,
): ShellCommandResult {
    if (args.firstOrNull() in setOf("-h", "--help", "help")) {
        return ShellCommandResult(output = sshKeygenHelp())
    }
    var type = "rsa"
    var bits = 4096
    var comment = "pocketcodedpy"
    var targetPath = "~/.ssh/id_rsa"
    var targetPathProvided = false
    var commentProvided = false
    var index = 0
    while (index < args.size) {
        when (val arg = args[index]) {
            "-t" -> {
                type = args.getOrNull(index + 1)?.lowercase()
                    ?: return ShellCommandResult(error = "ssh-keygen: missing key type after -t")
                index += 2
            }
            "-b" -> {
                bits = args.getOrNull(index + 1)?.toIntOrNull()
                    ?: return ShellCommandResult(error = "ssh-keygen: missing bit count after -b")
                index += 2
            }
            "-C" -> {
                comment = args.getOrNull(index + 1)
                    ?: return ShellCommandResult(error = "ssh-keygen: missing comment after -C")
                commentProvided = true
                index += 2
            }
            "-f" -> {
                targetPath = args.getOrNull(index + 1)
                    ?: return ShellCommandResult(error = "ssh-keygen: missing path after -f")
                targetPathProvided = true
                index += 2
            }
            "-N" -> {
                val passphrase = args.getOrNull(index + 1)
                    ?: return ShellCommandResult(error = "ssh-keygen: missing passphrase after -N")
                if (passphrase.isNotEmpty()) {
                    return ShellCommandResult(error = "ssh-keygen: passphrase-protected keys are not supported yet; use -N \"\"")
                }
                index += 2
            }
            "-q" -> {
                index += 1
            }
            else -> {
                if (arg.startsWith("-")) {
                    return ShellCommandResult(error = "ssh-keygen: unsupported option $arg")
                }
                if (!commentProvided && comment == "pocketcodedpy") {
                    comment = arg
                }
                index += 1
            }
        }
    }

    if (type == "ssh-ed25519") {
        type = "ed25519"
    }
    if (type !in setOf("rsa", "ed25519")) {
        return ShellCommandResult(error = "ssh-keygen: supported key types are ed25519 and rsa")
    }
    if (type == "rsa" && bits < 2048) {
        return ShellCommandResult(error = "ssh-keygen: RSA keys must be at least 2048 bits")
    }
    if (type == "ed25519" && !targetPathProvided) {
        targetPath = "~/.ssh/id_ed25519"
    }

    ensureSandboxSshFiles(rootDirectory)
    val privateKeyFile = resolveTerminalPath(rootDirectory, workingDirectory, targetPath)
        ?: return ShellCommandResult(error = "ssh-keygen: outside sandbox")
    val publicKeyFile = File(privateKeyFile.absolutePath + ".pub")
    if (privateKeyFile.exists() || publicKeyFile.exists()) {
        return ShellCommandResult(error = "ssh-keygen: ${targetPath}: key already exists")
    }

    privateKeyFile.parentFile?.mkdirs()
    val keyPair = generateSshKeyPair(type, bits)
        ?: return ShellCommandResult(error = "ssh-keygen: ed25519 is not available on this build; use -t rsa -b 4096")
    writeOpenSshKeyPair(keyPair, comment, privateKeyFile, publicKeyFile)
    setPrivateFilePermissions(privateKeyFile)
    setPrivateFilePermissions(publicKeyFile)

    val keyLabel = if (type == "rsa") "RSA-$bits" else "ED25519"
    return ShellCommandResult(
        output = """
            Generated $keyLabel SSH key.
            Private key: ${relativeDirectoryLabel(rootDirectory, privateKeyFile.parentFile ?: rootDirectory)}/${privateKeyFile.name}
            Public key: ${relativeDirectoryLabel(rootDirectory, publicKeyFile.parentFile ?: rootDirectory)}/${publicKeyFile.name}

            Add this public key to GitHub or GitLab:
            ${publicKeyFile.readText().trim()}
        """.trimIndent(),
        workspaceChanged = true
    )
}

private fun sshKeygenHelp(): String {
    return """
        Built-in ssh-keygen
        ssh-keygen -t ed25519 -C "you@example.com"
        ssh-keygen -t rsa -b 4096 -C "you@example.com"
        ssh-keygen -t ed25519 -f ~/.ssh/id_ed25519 -C "you@example.com"

        Keys are stored inside this app sandbox under ~/.ssh.
    """.trimIndent()
}

private fun generateSshKeyPair(
    type: String,
    bits: Int,
): KeyPair? {
    return when (type) {
        "ed25519" -> runCatching {
            net.i2p.crypto.eddsa.KeyPairGenerator().apply {
                initialize(256, SecureRandom())
            }.generateKeyPair()
        }.getOrNull()
        else -> {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(bits)
            generator.generateKeyPair()
        }
    }
}

private fun writeOpenSshKeyPair(
    keyPair: KeyPair,
    comment: String,
    privateKeyFile: File,
    publicKeyFile: File,
) {
    privateKeyFile.outputStream().use { output ->
        OpenSSHKeyPairResourceWriter.INSTANCE.writePrivateKey(keyPair, comment, null, output)
    }
    publicKeyFile.outputStream().use { output ->
        OpenSSHKeyPairResourceWriter.INSTANCE.writePublicKey(keyPair, comment, output)
        output.write('\n'.code)
    }
}

private fun setPrivateFilePermissions(file: File) {
    runCatching {
        file.setReadable(false, false)
        file.setWritable(false, false)
        file.setExecutable(false, false)
        file.setReadable(true, true)
        file.setWritable(true, true)
        if (file.isDirectory) {
            file.setExecutable(true, true)
        }
    }
}

private fun gitErrorMessage(error: Throwable): String {
    return gitErrorMessages(error).firstOrNull()
        ?: error::class.java.simpleName
}

private fun gitErrorMessages(error: Throwable): List<String> {
    val messages = mutableListOf<String>()
    var current: Throwable? = error
    while (current != null && messages.size < 8) {
        current.message?.takeIf { it.isNotBlank() }?.let { message ->
            if (messages.none { it == message }) {
                messages += message
            }
        }
        current = current.cause
    }
    return messages.ifEmpty { listOf(error::class.java.simpleName) }
}

private fun treeShellDirectory(
    rootDirectory: File,
    workingDirectory: File,
): String {
    val builder = StringBuilder()
    builder.append(relativeDirectoryLabel(rootDirectory, workingDirectory)).append('\n')

    fun appendDirectory(directory: File, depth: Int) {
        if (depth > 4) return
        directory
            .listFiles()
            .orEmpty()
            .filter { !it.name.startsWith(".") }
            .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
            .forEach { child ->
                builder.append("  ".repeat(depth)).append(if (child.isDirectory) "├─ " else "└─ ")
                builder.append(child.name).append(if (child.isDirectory) "/" else "").append('\n')
                if (child.isDirectory) appendDirectory(child, depth + 1)
            }
    }

    appendDirectory(workingDirectory, 1)
    return builder.toString().trimEnd()
}

private fun resolveTerminalPath(
    rootDirectory: File,
    workingDirectory: File,
    rawPath: String,
): File? {
    val candidate = when {
        rawPath == "~" -> rootDirectory
        rawPath.startsWith("~/") -> File(rootDirectory, rawPath.removePrefix("~/"))
        rawPath.startsWith("/") -> File(rootDirectory, rawPath.trimStart('/'))
        else -> File(workingDirectory, rawPath)
    }
    val canonical = runCatching { candidate.canonicalFile }.getOrNull() ?: return null
    return canonical.takeIf { isSameOrInside(rootDirectory, it) }
}

private fun splitCommandLine(command: String): List<String> {
    val args = mutableListOf<String>()
    val current = StringBuilder()
    var quote: Char? = null
    var escaping = false
    command.forEach { char ->
        when {
            escaping -> {
                current.append(char)
                escaping = false
            }
            char == '\\' -> escaping = true
            quote != null && char == quote -> quote = null
            quote != null -> current.append(char)
            char == '"' || char == '\'' -> quote = char
            char.isWhitespace() -> {
                if (current.isNotEmpty()) {
                    args.add(current.toString())
                    current.clear()
                }
            }
            else -> current.append(char)
        }
    }
    if (current.isNotEmpty()) args.add(current.toString())
    return args
}

private fun isIdentifierStart(char: Char): Boolean {
    return char == '_' || char.isLetter()
}

private fun isIdentifierPart(char: Char): Boolean {
    return char == '_' || char.isLetterOrDigit()
}

private val pythonKeywords = setOf(
    "False", "None", "True", "and", "as", "assert", "async", "await", "break",
    "class", "continue", "def", "del", "elif", "else", "except", "finally",
    "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal",
    "not", "or", "pass", "raise", "return", "try", "while", "with", "yield"
)

private val pythonConstants = setOf("True", "False", "None", "self", "cls")

private val pythonBuiltins = setOf(
    "abs", "all", "any", "bin", "bool", "bytes", "callable", "chr", "dict",
    "dir", "enumerate", "eval", "filter", "float", "format", "help", "hex",
    "id", "input", "int", "isinstance", "issubclass", "iter", "len", "list",
    "map", "max", "min", "next", "object", "open", "ord", "pow", "print",
    "range", "repr", "reversed", "round", "set", "slice", "sorted", "str",
    "sum", "super", "tuple", "type", "zip"
)

private val pythonCompletionItems =
    pythonKeywords.map { CompletionItem(it, detail = "keyword") } +
        pythonBuiltins.map { CompletionItem("$it()", "$it()", "builtin", it.length + 1) } +
        listOf(
            CompletionItem("print()", "print()", "function", "print(".length),
            CompletionItem("input()", "input()", "function", "input(".length),
            CompletionItem("def", "def function_name():\n    ", "snippet"),
            CompletionItem("class", "class ClassName:\n    def __init__(self):\n        ", "snippet"),
            CompletionItem("for", "for item in items:\n    ", "snippet"),
            CompletionItem("if", "if condition:\n    ", "snippet"),
            CompletionItem("elif", "elif condition:\n    ", "snippet"),
            CompletionItem("while", "while condition:\n    ", "snippet"),
            CompletionItem("try", "try:\n    \nexcept Exception as error:\n    print(error)", "snippet", "try:\n    ".length),
            CompletionItem("__name__", "__name__", "dunder"),
            CompletionItem("__main__", "\"__main__\"", "constant"),
        )

private val pythonListDotCompletions = listOf(
    CompletionItem("append()", "append()", "list", "append(".length),
    CompletionItem("extend()", "extend()", "list", "extend(".length),
    CompletionItem("insert()", "insert()", "list", "insert(".length),
    CompletionItem("remove()", "remove()", "list", "remove(".length),
    CompletionItem("pop()", "pop()", "list", "pop(".length),
    CompletionItem("sort()", "sort()", "list", "sort(".length),
    CompletionItem("reverse()", "reverse()", "list", "reverse(".length),
    CompletionItem("copy()", "copy()", "list", "copy(".length),
)

private val pythonDictDotCompletions = listOf(
    CompletionItem("keys()", "keys()", "dict", "keys(".length),
    CompletionItem("values()", "values()", "dict", "values(".length),
    CompletionItem("items()", "items()", "dict", "items(".length),
    CompletionItem("get()", "get()", "dict", "get(".length),
    CompletionItem("update()", "update()", "dict", "update(".length),
    CompletionItem("setdefault()", "setdefault()", "dict", "setdefault(".length),
    CompletionItem("pop()", "pop()", "dict", "pop(".length),
)

private val pythonStringDotCompletions = listOf(
    CompletionItem("split()", "split()", "str", "split(".length),
    CompletionItem("join()", "join()", "str", "join(".length),
    CompletionItem("strip()", "strip()", "str", "strip(".length),
    CompletionItem("lower()", "lower()", "str", "lower(".length),
    CompletionItem("upper()", "upper()", "str", "upper(".length),
    CompletionItem("title()", "title()", "str", "title(".length),
    CompletionItem("replace()", "replace()", "str", "replace(".length),
    CompletionItem("startswith()", "startswith()", "str", "startswith(".length),
    CompletionItem("endswith()", "endswith()", "str", "endswith(".length),
    CompletionItem("find()", "find()", "str", "find(".length),
)

private val pythonNumberDotCompletions = listOf(
    CompletionItem("bit_length()", "bit_length()", "number", "bit_length(".length),
    CompletionItem("to_bytes()", "to_bytes()", "number", "to_bytes(".length),
    CompletionItem("as_integer_ratio()", "as_integer_ratio()", "number", "as_integer_ratio(".length),
)

private val completionDefinitions = mapOf(
    "abs|builtin" to "Returns the distance from zero for a number. Use it when only the size of a value matters, such as a difference, score change, or movement amount, and the sign should not matter.",
    "all|builtin" to "Returns True only when every item in an iterable is true. It is useful for checking a list of rules before allowing an action to continue.",
    "any|builtin" to "Returns True when at least one item in an iterable is true. Use it when one matching result is enough, such as finding whether a player has any lives left.",
    "bin|builtin" to "Converts an integer into a binary text string that starts with 0b. This is helpful when you want to inspect flags, bit masks, or low-level number data.",
    "bool|builtin" to "Converts a value into True or False using Python's truth rules. Empty strings, empty lists, zero, and None become False; most other values become True.",
    "bytes|builtin" to "Creates an immutable bytes object. Use bytes when working with binary data instead of normal text, such as encoded files or network payloads.",
    "callable|builtin" to "Checks whether a value can be called like a function. This is useful before running callbacks or objects that might or might not be functions.",
    "chr|builtin" to "Returns the character for a Unicode number. For example, 65 becomes A. It is the reverse of ord().",
    "dict|builtin" to "Creates a dictionary, which stores key-value pairs. Dictionaries are good for records, settings, counters, and fast lookups by name or id.",
    "dir|builtin" to "Lists the attributes and methods available on an object. It is often used while exploring unfamiliar values in small experiments.",
    "enumerate|builtin" to "Loops over an iterable while also giving each item's index. Use it when you need both the position and the value without managing a counter yourself.",
    "eval|builtin" to "Runs a string as a Python expression and returns the result. It is powerful but risky with user text, because the string can execute code.",
    "filter|builtin" to "Keeps only the items that pass a test function. It returns an iterable, so wrap it with list() when you want to see all filtered results immediately.",
    "float|builtin" to "Converts a value into a decimal number. Use it for measurements, prices, percentages, or division results that need fractional values.",
    "format|builtin" to "Formats a value as text using a format specifier. It is useful for rounding numbers, padding output, or controlling how values are displayed.",
    "help|builtin" to "Shows Python help for an object, function, type, or module. It is useful when you want to inspect built-in documentation while learning.",
    "hex|builtin" to "Converts an integer into hexadecimal text that starts with 0x. Hex is often used for colors, bytes, and low-level numeric values.",
    "id|builtin" to "Returns the unique identity number for an object during this program run. It is mostly useful for debugging object references.",
    "input|builtin" to "Pauses the program, shows an optional prompt, and waits for the user to type text. It always returns a string, so convert the result with int() or float() when you need a number.",
    "input|function" to "Pauses the program, shows an optional prompt, and waits for the user to type text. It always returns a string, so convert the result with int() or float() when you need a number.",
    "int|builtin" to "Converts a value into an integer. It can parse strings like \"42\" and also truncates decimal floats toward zero.",
    "isinstance|builtin" to "Checks whether a value belongs to a type or tuple of types. Use it before handling values differently based on whether they are strings, numbers, lists, and so on.",
    "issubclass|builtin" to "Checks whether one class inherits from another class. It is useful when working with class hierarchies and reusable base behavior.",
    "iter|builtin" to "Returns an iterator for an iterable value. Iterators remember their position and produce the next item each time next() is called.",
    "len|builtin" to "Counts how many items are inside a string, list, dictionary, tuple, set, or other collection. For dictionaries, len() counts keys.",
    "list|builtin" to "Creates a list from another iterable. Lists keep items in order and can be changed with methods like append(), pop(), sort(), and reverse().",
    "map|builtin" to "Applies a function to every item in an iterable. It returns an iterable, so wrap it with list() when you need the converted results right away.",
    "max|builtin" to "Returns the largest item from values or from an iterable. It can compare numbers, strings, or custom values using a key function.",
    "min|builtin" to "Returns the smallest item from values or from an iterable. It is useful for scores, limits, dates, and any data where the lowest value matters.",
    "next|builtin" to "Gets the next item from an iterator. You can provide a default value to avoid an error when the iterator is empty.",
    "object|builtin" to "Creates the most basic Python object. You rarely call object() directly, but all normal Python classes ultimately inherit from object.",
    "open|builtin" to "Opens a file for reading, writing, or appending. Use it with a with block so Python closes the file automatically when the block finishes.",
    "ord|builtin" to "Returns the Unicode number for a one-character string. It is the reverse of chr().",
    "pow|builtin" to "Raises a number to a power. With a third argument, it performs modular exponentiation efficiently.",
    "print|builtin" to "Writes values to the terminal so the user or developer can see them. It converts each value to text, separates multiple values with spaces by default, and ends with a new line.",
    "print|function" to "Writes values to the terminal so the user or developer can see them. It converts each value to text, separates multiple values with spaces by default, and ends with a new line.",
    "range|builtin" to "Creates a sequence of integers without storing the whole sequence in memory. It is most commonly used in for loops when you need to repeat code a known number of times.",
    "repr|builtin" to "Returns a developer-focused text representation of a value. It often includes quotes and escape characters so you can see the value more precisely than with str().",
    "reversed|builtin" to "Returns an iterator that reads an iterable in reverse order. It does not change the original list or string.",
    "round|builtin" to "Rounds a number to the nearest integer or to a chosen number of decimal places. It is useful for scores, money-like displays, and measurements.",
    "set|builtin" to "Creates a set, which stores unique values with no guaranteed order. Sets are useful for removing duplicates and checking membership quickly.",
    "slice|builtin" to "Creates a reusable slice object for selecting part of a sequence. Most code uses square bracket slicing directly, but slice() is useful when the same slice is reused.",
    "sorted|builtin" to "Returns a new sorted list from any iterable. Unlike list.sort(), sorted() leaves the original collection unchanged.",
    "str|builtin" to "Converts a value into readable text. Use it when building messages, joining values into strings, or showing non-text values to the user.",
    "sum|builtin" to "Adds numeric values from an iterable and returns the total. It is best for numbers; use join() for combining strings.",
    "super|builtin" to "Returns a helper for calling methods from a parent class. It is most often used inside class methods such as __init__.",
    "tuple|builtin" to "Creates a tuple, which is an ordered collection that cannot be changed after creation. Tuples are useful for fixed groups of values.",
    "type|builtin" to "Returns the class/type of a value, or creates a new type in advanced metaprogramming. In everyday code, it is mostly used for inspection and debugging.",
    "zip|builtin" to "Combines multiple iterables item by item. Each loop gives one item from each iterable, which is useful for pairing names with scores or keys with values.",
    "and|keyword" to "Combines conditions and only passes when both sides are true. Python stops early when the left side is false, so the right side may not run.",
    "as|keyword" to "Gives an imported module, context manager value, or caught exception a local name. This keeps code shorter or makes the value available inside a block.",
    "assert|keyword" to "Checks that a condition is true while debugging or testing. If the condition is false, Python raises an AssertionError.",
    "async|keyword" to "Defines asynchronous functions or context blocks. Async code can pause while waiting for other work without blocking the whole program.",
    "await|keyword" to "Pauses an async function until an awaitable result is ready. It can only be used inside async functions.",
    "break|keyword" to "Exits the nearest loop immediately. Use it when you have found what you need or want to stop looping early.",
    "class|keyword" to "Defines a class, which is a blueprint for objects that share data and behavior. Classes group related values and functions into one reusable type.",
    "class|snippet" to "Inserts a starter class block with an __init__ method. Fill in the class name and instance fields to model a real object in your program.",
    "continue|keyword" to "Skips the rest of the current loop body and moves to the next iteration. Use it when one item should be ignored but the loop should keep running.",
    "def|keyword" to "Defines a reusable function. Put parameters in parentheses, indent the body, and optionally use return to send a value back to the caller.",
    "def|snippet" to "Inserts a starter function block. Replace the name and parameters, then write the indented code that should run when the function is called.",
    "del|keyword" to "Deletes a name, list item, dictionary entry, or object attribute. After deletion, that target is no longer available in the same place.",
    "elif|keyword" to "Adds another condition after an if block. Python checks elif only when the previous if or elif condition was false.",
    "elif|snippet" to "Inserts an elif branch. Use it after if when you have another specific condition to test before falling back to else.",
    "else|keyword" to "Runs when the previous if or elif condition did not run. It is the fallback branch for decisions.",
    "except|keyword" to "Catches an error raised inside a try block. Use specific exception types when possible so unrelated errors still surface.",
    "finally|keyword" to "Runs cleanup code after try and except, whether an error happened or not. It is useful for closing resources or resetting state.",
    "for|keyword" to "Repeats code once for each item in an iterable such as a list, string, dictionary, range, or file. The loop variable receives the current item each time.",
    "for|snippet" to "Inserts a starter for loop. Replace the item and collection names, then write the indented code that should run for each item.",
    "from|keyword" to "Imports selected names from a module. This lets you use a function, class, or constant directly without writing the module name every time.",
    "global|keyword" to "Tells Python that a name inside a function refers to a module-level variable. Use it sparingly because it can make state harder to follow.",
    "if|keyword" to "Runs an indented block only when a condition is true. Combine it with elif and else when your program needs multiple branches.",
    "if|snippet" to "Inserts a starter if block. Replace the condition with an expression that evaluates to True or False.",
    "import|keyword" to "Loads a module so you can use code defined somewhere else. After importing, use dot access to reach functions, classes, and constants from the module.",
    "in|keyword" to "Checks membership or receives items during a for loop. With collections, it answers whether a value appears inside.",
    "is|keyword" to "Checks whether two names refer to the exact same object. Use == for normal value equality; use is mainly for None checks and identity checks.",
    "lambda|keyword" to "Creates a small anonymous function expression. It is useful for short callbacks or key functions, but def is clearer for larger logic.",
    "nonlocal|keyword" to "Lets an inner function update a variable from the nearest enclosing function scope. It does not refer to global variables.",
    "not|keyword" to "Flips a condition from true to false or false to true. It makes negative checks readable, such as not ready or value not in items.",
    "or|keyword" to "Combines conditions and passes when either side is true. Python stops early when the left side is true.",
    "pass|keyword" to "Does nothing. Use it as a placeholder where Python requires an indented block but you have not written the real code yet.",
    "raise|keyword" to "Creates an error on purpose. Use it when invalid data or unsupported behavior should stop the current operation.",
    "return|keyword" to "Stops a function and sends a value back to the caller. Without a value, the function returns None.",
    "try|keyword" to "Starts a protected block for code that might raise an error. Pair it with except to handle expected problems without crashing the program.",
    "try|snippet" to "Inserts a try/except block. Put risky code in try and the recovery or message in except.",
    "while|keyword" to "Repeats code while a condition stays true. Make sure something inside the loop eventually changes the condition so it can stop.",
    "while|snippet" to "Inserts a starter while loop. Replace the condition and update loop state inside the body so the loop does not run forever.",
    "with|keyword" to "Runs a block with a context manager that handles setup and cleanup. It is commonly used for files so they close automatically.",
    "yield|keyword" to "Produces one value from a generator function and pauses until the next value is requested. It is useful for lazy sequences.",
    "False|keyword" to "The Boolean false value. Use it for conditions, flags, and results that mean no, off, failed, or not active.",
    "None|keyword" to "The special value meaning no value. Use None as a default, missing result, or empty placeholder when zero or an empty string would mean something else.",
    "True|keyword" to "The Boolean true value. Use it for conditions, flags, and results that mean yes, on, succeeded, or active.",
    "append|list" to "Adds one item to the end of the same list. It changes the list in place and returns None, so call it on its own line.",
    "extend|list" to "Adds every item from another iterable to the end of the same list. Use extend() when you want to merge lists item by item instead of adding a nested list.",
    "insert|list" to "Places one item into the list at a specific index. Existing items at that index and after it move to the right.",
    "remove|list" to "Deletes the first item whose value matches the argument. It raises ValueError if the value is not in the list.",
    "pop|list" to "Removes and returns an item from the list. Without an index it removes the last item, which makes it useful for stack-like behavior.",
    "sort|list" to "Sorts the same list in place and returns None. Use sorted() instead when you need a new sorted list and want to keep the original order.",
    "reverse|list" to "Reverses the same list in place. It changes the existing list and returns None.",
    "copy|list" to "Returns a shallow copy of the list. The new list has the same items, but changing the list length or order does not affect the original.",
    "keys|dict" to "Returns a dynamic view of all keys in the dictionary. The view updates as the dictionary changes and is commonly used in loops or membership checks.",
    "values|dict" to "Returns a dynamic view of all values in the dictionary. Use it when the keys do not matter and you only need the stored data.",
    "items|dict" to "Returns a dynamic view of key-value pairs. It is the cleanest way to loop through both keys and values together.",
    "get|dict" to "Reads a value by key without crashing when the key is missing. Provide a fallback as the second argument when missing keys are expected.",
    "update|dict" to "Adds or replaces entries using another dictionary or keyword arguments. It changes the same dictionary in place.",
    "setdefault|dict" to "Returns the value for a key, or stores and returns a default if the key is missing. It is useful for grouping or counting data.",
    "pop|dict" to "Removes a key from the dictionary and returns its value. You can pass a fallback to avoid KeyError when the key is missing.",
    "split|str" to "Breaks text into a list of smaller strings. Without an argument it splits on whitespace; with a separator it splits wherever that exact separator appears.",
    "join|str" to "Combines an iterable of strings into one string, using the string before .join() as the separator. It is the standard way to build comma-separated or space-separated text.",
    "strip|str" to "Returns a copy of the text with whitespace removed from both ends. It is helpful for cleaning input before validation.",
    "lower|str" to "Returns a lowercase copy of the text. Use it for case-insensitive comparisons or normalized usernames.",
    "upper|str" to "Returns an uppercase copy of the text. Use it for display, codes, or normalized comparisons.",
    "title|str" to "Returns a copy where each word starts with an uppercase letter. It is useful for display names and headings, but review output for names with apostrophes or special casing.",
    "replace|str" to "Returns a copy of the text with every matching part replaced by another string. The original string is unchanged.",
    "startswith|str" to "Returns True when the text begins with the given prefix. It is useful for filtering filenames, commands, and user input.",
    "endswith|str" to "Returns True when the text ends with the given suffix. It is common for checking file extensions or command endings.",
    "find|str" to "Returns the index where a substring first appears. It returns -1 when the substring is missing, so check for -1 before using the index.",
    "bit_length|number" to "Returns how many bits are needed to represent an integer without its sign. It is useful for binary storage, masks, and size checks.",
    "to_bytes|number" to "Converts an integer into a bytes object using a chosen byte length and byte order. Use it when writing binary data or working with protocols.",
    "as_integer_ratio|number" to "Returns the exact numerator and denominator for a number. For floats, it reveals the exact stored fraction behind the decimal value.",
    "__name__|dunder" to "The current module name. It is \"__main__\" when this file is being run directly, and another name when it is imported.",
    "__main__|constant" to "The module name Python uses for the file that was started directly. Compare __name__ to \"__main__\" before running script-only code."
)

private val completionSyntaxes = mapOf(
    "abs|builtin" to "abs(number)",
    "all|builtin" to "all(iterable)",
    "any|builtin" to "any(iterable)",
    "bin|builtin" to "bin(integer)",
    "bool|builtin" to "bool(value)",
    "bytes|builtin" to "bytes(source)\nbytes(source, encoding)",
    "callable|builtin" to "callable(value)",
    "chr|builtin" to "chr(code_point)",
    "dict|builtin" to "dict()\ndict(key=value)\ndict(iterable)",
    "dir|builtin" to "dir(object)",
    "enumerate|builtin" to "enumerate(iterable, start=0)",
    "eval|builtin" to "eval(expression)",
    "filter|builtin" to "filter(function, iterable)",
    "float|builtin" to "float(value)",
    "format|builtin" to "format(value, spec)",
    "help|builtin" to "help(object)",
    "hex|builtin" to "hex(integer)",
    "id|builtin" to "id(object)",
    "input|builtin" to "input(prompt=\"\")",
    "input|function" to "input(prompt=\"\")",
    "int|builtin" to "int(value)\nint(text, base)",
    "isinstance|builtin" to "isinstance(value, type_or_tuple)",
    "issubclass|builtin" to "issubclass(class, classinfo)",
    "iter|builtin" to "iter(iterable)",
    "len|builtin" to "len(collection)",
    "list|builtin" to "list(iterable)",
    "map|builtin" to "map(function, iterable)",
    "max|builtin" to "max(iterable)\nmax(a, b, c)",
    "min|builtin" to "min(iterable)\nmin(a, b, c)",
    "next|builtin" to "next(iterator)\nnext(iterator, default)",
    "object|builtin" to "object()",
    "open|builtin" to "open(path, mode=\"r\")",
    "ord|builtin" to "ord(character)",
    "pow|builtin" to "pow(base, exponent)\npow(base, exponent, mod)",
    "print|builtin" to "print(*values, sep=\" \", end=\"\\n\")",
    "print|function" to "print(*values, sep=\" \", end=\"\\n\")",
    "range|builtin" to "range(stop)\nrange(start, stop, step)",
    "repr|builtin" to "repr(value)",
    "reversed|builtin" to "reversed(sequence)",
    "round|builtin" to "round(number, ndigits=None)",
    "set|builtin" to "set(iterable)",
    "slice|builtin" to "slice(start, stop, step)",
    "sorted|builtin" to "sorted(iterable, key=None, reverse=False)",
    "str|builtin" to "str(value)",
    "sum|builtin" to "sum(iterable, start=0)",
    "super|builtin" to "super()",
    "tuple|builtin" to "tuple(iterable)",
    "type|builtin" to "type(value)",
    "zip|builtin" to "zip(iterable1, iterable2, ...)",
    "and|keyword" to "condition_a and condition_b",
    "as|keyword" to "import module as name\nexcept Error as error\nwith manager as value:",
    "assert|keyword" to "assert condition\nassert condition, message",
    "async|keyword" to "async def name():\n    ...",
    "await|keyword" to "result = await awaitable",
    "break|keyword" to "break",
    "class|keyword" to "class ClassName:\n    ...",
    "class|snippet" to "class ClassName:\n    def __init__(self):\n        ...",
    "continue|keyword" to "continue",
    "def|keyword" to "def function_name(parameters):\n    ...",
    "def|snippet" to "def function_name(parameters):\n    ...",
    "del|keyword" to "del name\ndel items[index]\ndel mapping[key]",
    "elif|keyword" to "elif condition:\n    ...",
    "elif|snippet" to "elif condition:\n    ...",
    "else|keyword" to "else:\n    ...",
    "except|keyword" to "except ErrorType as error:\n    ...",
    "finally|keyword" to "finally:\n    ...",
    "for|keyword" to "for item in iterable:\n    ...",
    "for|snippet" to "for item in items:\n    ...",
    "from|keyword" to "from module import name",
    "global|keyword" to "global name",
    "if|keyword" to "if condition:\n    ...",
    "if|snippet" to "if condition:\n    ...",
    "import|keyword" to "import module\nimport module as alias",
    "in|keyword" to "value in collection\nfor item in collection:",
    "is|keyword" to "value is None",
    "lambda|keyword" to "lambda argument: expression",
    "nonlocal|keyword" to "nonlocal name",
    "not|keyword" to "not condition",
    "or|keyword" to "condition_a or condition_b",
    "pass|keyword" to "pass",
    "raise|keyword" to "raise ErrorType(message)",
    "return|keyword" to "return value",
    "try|keyword" to "try:\n    ...\nexcept ErrorType:\n    ...",
    "try|snippet" to "try:\n    ...\nexcept Exception as error:\n    ...",
    "while|keyword" to "while condition:\n    ...",
    "while|snippet" to "while condition:\n    ...",
    "with|keyword" to "with context_manager as value:\n    ...",
    "yield|keyword" to "yield value",
    "append|list" to "list.append(item)",
    "extend|list" to "list.extend(iterable)",
    "insert|list" to "list.insert(index, item)",
    "remove|list" to "list.remove(value)",
    "pop|list" to "list.pop()\nlist.pop(index)",
    "sort|list" to "list.sort(key=None, reverse=False)",
    "reverse|list" to "list.reverse()",
    "copy|list" to "list.copy()",
    "keys|dict" to "dict.keys()",
    "values|dict" to "dict.values()",
    "items|dict" to "dict.items()",
    "get|dict" to "dict.get(key, default=None)",
    "update|dict" to "dict.update(other)\ndict.update(key=value)",
    "setdefault|dict" to "dict.setdefault(key, default)",
    "pop|dict" to "dict.pop(key)\ndict.pop(key, default)",
    "split|str" to "text.split()\ntext.split(separator, maxsplit=-1)",
    "join|str" to "separator.join(strings)",
    "strip|str" to "text.strip(chars=None)",
    "lower|str" to "text.lower()",
    "upper|str" to "text.upper()",
    "title|str" to "text.title()",
    "replace|str" to "text.replace(old, new, count=-1)",
    "startswith|str" to "text.startswith(prefix)",
    "endswith|str" to "text.endswith(suffix)",
    "find|str" to "text.find(substring)",
    "bit_length|number" to "integer.bit_length()",
    "to_bytes|number" to "integer.to_bytes(length, byteorder)",
    "as_integer_ratio|number" to "number.as_integer_ratio()",
    "__name__|dunder" to "__name__",
    "__main__|constant" to "\"__main__\""
)

private val completionExamples = mapOf(
    "abs|builtin" to "distance = abs(player_x - target_x)\nif distance < 5:\n    print(\"Close enough\")",
    "all|builtin" to "checks = [has_key, door_open, energy > 0]\nif all(checks):\n    enter_room()",
    "any|builtin" to "if any(score >= 100 for score in scores):\n    print(\"A player won\")",
    "bin|builtin" to "flags = bin(10)\nprint(flags)  # 0b1010",
    "bool|builtin" to "name = input(\"Name: \")\nif bool(name.strip()):\n    print(\"Hello\", name)",
    "bytes|builtin" to "payload = bytes([80, 89])\nprint(payload)  # b'PY'",
    "callable|builtin" to "if callable(on_finish):\n    on_finish()",
    "chr|builtin" to "letter = chr(65)\nprint(letter)  # A",
    "dict|builtin" to "player = dict(name=\"Ada\", score=10)\nprint(player[\"name\"])",
    "dir|builtin" to "methods = dir(\"hello\")\nprint(\"upper\" in methods)",
    "enumerate|builtin" to "for index, item in enumerate(items, start=1):\n    print(index, item)",
    "eval|builtin" to "answer = eval(\"2 + 3\")\nprint(answer)  # 5",
    "filter|builtin" to "passed = list(filter(lambda score: score >= 50, scores))\nprint(passed)",
    "float|builtin" to "price = float(\"19.99\")\ntotal = price * 2",
    "format|builtin" to "message = format(3.14159, \".2f\")\nprint(message)  # 3.14",
    "help|builtin" to "help(str.lower)",
    "hex|builtin" to "color = hex(255)\nprint(color)  # 0xff",
    "id|builtin" to "same_object = id(first) == id(second)\nprint(same_object)",
    "input|builtin" to "name = input(\"Name: \")\nprint(\"Welcome\", name)",
    "input|function" to "age = int(input(\"Age: \"))\nprint(age + 1)",
    "int|builtin" to "age = int(input(\"Age: \"))\nprint(age + 1)",
    "isinstance|builtin" to "if isinstance(value, str):\n    value = value.strip()",
    "issubclass|builtin" to "if issubclass(BossEnemy, Enemy):\n    print(\"Boss is an enemy type\")",
    "iter|builtin" to "iterator = iter([\"a\", \"b\"])\nprint(next(iterator))",
    "len|builtin" to "password = input(\"Password: \")\nif len(password) < 8:\n    print(\"Too short\")",
    "list|builtin" to "numbers = list(range(1, 4))\nprint(numbers)  # [1, 2, 3]",
    "map|builtin" to "numbers = list(map(int, [\"1\", \"2\", \"3\"]))\nprint(numbers)",
    "max|builtin" to "high_score = max(scores)\nprint(\"Best:\", high_score)",
    "min|builtin" to "lowest_health = min(health_values)\nprint(lowest_health)",
    "next|builtin" to "names = iter([\"Ada\", \"Grace\"])\nprint(next(names, \"No name\"))",
    "object|builtin" to "marker = object()\nprint(type(marker))",
    "open|builtin" to "with open(\"scores.txt\", \"r\") as file:\n    text = file.read()",
    "ord|builtin" to "code = ord(\"A\")\nprint(code)  # 65",
    "pow|builtin" to "damage = pow(2, 3)\nprint(damage)  # 8",
    "print|builtin" to "name = \"Ada\"\nscore = 42\nprint(\"Player\", name, \"score\", score)",
    "print|function" to "print(\"Welcome back, coder!\")",
    "range|builtin" to "for level in range(1, 6):\n    print(\"Level\", level)",
    "repr|builtin" to "text = \"Hi\\nthere\"\nprint(repr(text))",
    "reversed|builtin" to "for item in reversed(items):\n    print(item)",
    "round|builtin" to "average = round(total / count, 2)\nprint(average)",
    "set|builtin" to "unique_names = set(names)\nprint(len(unique_names))",
    "slice|builtin" to "middle = slice(1, 4)\nprint(items[middle])",
    "sorted|builtin" to "ordered_scores = sorted(scores, reverse=True)\nprint(ordered_scores)",
    "str|builtin" to "message = \"Score: \" + str(score)\nprint(message)",
    "sum|builtin" to "total = sum(scores)\nprint(\"Total:\", total)",
    "super|builtin" to "class Player(Entity):\n    def __init__(self, name):\n        super().__init__()\n        self.name = name",
    "tuple|builtin" to "position = tuple([10, 20])\nprint(position)",
    "type|builtin" to "print(type(score))\nif type(score) is int:\n    print(\"Whole number\")",
    "zip|builtin" to "for name, score in zip(names, scores):\n    print(name, score)",
    "and|keyword" to "if has_key and door_is_locked:\n    unlock_door()",
    "as|keyword" to "import random as rng\nprint(rng.randint(1, 6))",
    "assert|keyword" to "assert score >= 0, \"Score cannot be negative\"",
    "async|keyword" to "async def load_profile():\n    data = await fetch_profile()\n    return data",
    "await|keyword" to "data = await fetch_profile()\nprint(data)",
    "break|keyword" to "for item in items:\n    if item == target:\n        break",
    "class|keyword" to "class Player:\n    def __init__(self, name):\n        self.name = name",
    "class|snippet" to "class Player:\n    def __init__(self, name):\n        self.name = name\n\nplayer = Player(\"Ada\")",
    "continue|keyword" to "for score in scores:\n    if score < 0:\n        continue\n    print(score)",
    "def|keyword" to "def calculate_total(prices):\n    return sum(prices)\n\nprint(calculate_total([4, 6]))",
    "def|snippet" to "def calculate_total(prices):\n    return sum(prices)\n\nprint(calculate_total([4, 6]))",
    "del|keyword" to "player = {\"name\": \"Ada\", \"score\": 10}\ndel player[\"score\"]",
    "elif|keyword" to "if score >= 80:\n    print(\"Great\")\nelif score >= 50:\n    print(\"Keep going\")",
    "elif|snippet" to "elif score >= 50:\n    print(\"Keep going\")",
    "else|keyword" to "if lives > 0:\n    play_round()\nelse:\n    print(\"Game over\")",
    "except|keyword" to "try:\n    value = int(text)\nexcept ValueError as error:\n    print(\"Not a number\")",
    "finally|keyword" to "try:\n    run_level()\nfinally:\n    print(\"Round finished\")",
    "for|keyword" to "for item in items:\n    print(item)",
    "for|snippet" to "for item in items:\n    print(item)",
    "from|keyword" to "from math import sqrt\nprint(sqrt(81))",
    "global|keyword" to "count = 0\n\ndef add_one():\n    global count\n    count += 1",
    "if|keyword" to "if score >= 80:\n    print(\"Great job!\")",
    "if|snippet" to "if score >= 80:\n    print(\"Great job!\")",
    "import|keyword" to "import random\nroll = random.randint(1, 6)\nprint(roll)",
    "in|keyword" to "if \"admin\" in roles:\n    print(\"Access granted\")",
    "is|keyword" to "if selected_item is None:\n    print(\"Nothing selected\")",
    "lambda|keyword" to "scores.sort(key=lambda player: player[\"score\"])",
    "nonlocal|keyword" to "def counter():\n    count = 0\n    def add_one():\n        nonlocal count\n        count += 1",
    "not|keyword" to "if not is_ready:\n    print(\"Please wait\")",
    "or|keyword" to "if is_admin or owns_file:\n    allow_edit()",
    "pass|keyword" to "def unfinished_feature():\n    pass",
    "raise|keyword" to "if age < 0:\n    raise ValueError(\"Age cannot be negative\")",
    "return|keyword" to "def double(number):\n    return number * 2",
    "try|keyword" to "try:\n    value = int(text)\nexcept ValueError:\n    print(\"Not a number\")",
    "try|snippet" to "try:\n    value = int(text)\nexcept ValueError:\n    print(\"Not a number\")",
    "while|keyword" to "while lives > 0:\n    play_round()\n    lives -= 1",
    "while|snippet" to "while lives > 0:\n    play_round()\n    lives -= 1",
    "with|keyword" to "with open(\"scores.txt\", \"w\") as file:\n    file.write(\"100\")",
    "yield|keyword" to "def countdown(start):\n    while start > 0:\n        yield start\n        start -= 1",
    "False|keyword" to "is_game_over = False\nif not is_game_over:\n    play_round()",
    "None|keyword" to "winner = None\nif winner is None:\n    print(\"No winner yet\")",
    "True|keyword" to "is_running = True\nwhile is_running:\n    is_running = False",
    "append|list" to "tasks = []\ntasks.append(\"Finish lesson\")\nprint(tasks)",
    "extend|list" to "tasks = [\"Plan\"]\ntasks.extend([\"Build\", \"Test\"])\nprint(tasks)",
    "insert|list" to "scores = [80, 90]\nscores.insert(0, 100)\nprint(scores)",
    "remove|list" to "tasks = [\"Start\", \"Done\", \"Review\"]\ntasks.remove(\"Done\")",
    "pop|list" to "tasks = [\"Start\", \"Build\", \"Test\"]\nlast_task = tasks.pop()\nprint(last_task)",
    "sort|list" to "scores = [30, 10, 20]\nscores.sort()\nprint(scores)",
    "reverse|list" to "items = [1, 2, 3]\nitems.reverse()\nprint(items)",
    "copy|list" to "original = [\"a\", \"b\"]\nbackup = original.copy()\nbackup.append(\"c\")",
    "keys|dict" to "player = {\"name\": \"Ada\", \"score\": 10}\nfor key in player.keys():\n    print(key)",
    "values|dict" to "scores = {\"Ada\": 10, \"Bo\": 7}\nfor score in scores.values():\n    print(score)",
    "items|dict" to "scores = {\"Ada\": 10, \"Bo\": 7}\nfor name, score in scores.items():\n    print(name, score)",
    "get|dict" to "player = {\"name\": \"Ada\"}\nscore = player.get(\"score\", 0)\nprint(score)",
    "update|dict" to "player = {\"name\": \"Ada\", \"score\": 10}\nplayer.update({\"score\": 20, \"level\": 2})",
    "setdefault|dict" to "groups = {}\ngroups.setdefault(\"team\", []).append(\"Ada\")\nprint(groups)",
    "pop|dict" to "player = {\"name\": \"Ada\", \"score\": 10}\nscore = player.pop(\"score\", 0)\nprint(score)",
    "split|str" to "message = \"red green blue\"\ncolors = message.split()\nprint(colors)",
    "join|str" to "words = [\"Python\", \"is\", \"fun\"]\nsentence = \" \".join(words)\nprint(sentence)",
    "strip|str" to "email = \"  ADA@example.com  \"\nclean_email = email.strip().lower()",
    "lower|str" to "answer = input(\"Continue? \").lower()\nif answer == \"yes\":\n    print(\"Continuing\")",
    "upper|str" to "code = \"py\".upper()\nprint(code)  # PY",
    "title|str" to "name = \"ada lovelace\".title()\nprint(name)",
    "replace|str" to "filename = \"my project.py\"\nsafe_name = filename.replace(\" \", \"_\")",
    "startswith|str" to "filename = \"test_player.py\"\nif filename.startswith(\"test_\"):\n    print(\"Test file\")",
    "endswith|str" to "filename = \"main.py\"\nif filename.endswith(\".py\"):\n    print(\"Python file\")",
    "find|str" to "message = \"Error: missing name\"\nindex = message.find(\"missing\")\nif index != -1:\n    print(index)",
    "bit_length|number" to "value = 255\nprint(value.bit_length())  # 8",
    "to_bytes|number" to "value = 1024\npayload = value.to_bytes(2, \"big\")\nprint(payload)",
    "as_integer_ratio|number" to "ratio = (0.75).as_integer_ratio()\nprint(ratio)  # (3, 4)",
    "__name__|dunder" to "def main():\n    print(\"Running directly\")\n\nif __name__ == \"__main__\":\n    main()",
    "__main__|constant" to "if __name__ == \"__main__\":\n    main()"
)

private val pythonRuntimeLock = Any()

private fun pocketPython(context: Context): Python {
    synchronized(pythonRuntimeLock) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context.applicationContext))
        }
        return Python.getInstance()
    }
}

private suspend fun runPythonCode(
    context: Context,
    source: String,
    stdin: String,
    filePath: String,
    workingDirectoryPath: String,
    rootDirectoryPath: String,
    virtualEnvironmentPath: String? = null,
    inputBridge: PythonInputBridge? = null,
    runController: TerminalRunController? = null,
): RunResult = withContext(Dispatchers.Default) {
    try {
        val json = pocketPython(context)
            .getModule("pocket_runner")
            .callAttr(
                "run_code",
                source,
                stdin,
                filePath,
                workingDirectoryPath,
                rootDirectoryPath,
                virtualEnvironmentPath,
                inputBridge,
                runController
            )
            .toString()
        val result = JSONObject(json)
        RunResult(
            success = result.optBoolean("success"),
            output = result.optString("output"),
            error = result.optString("error"),
            stopped = result.optBoolean("stopped")
        )
    } catch (error: Throwable) {
        RunResult(
            success = false,
            output = "",
            error = error.stackTraceToString()
        )
    }
}

private suspend fun runPythonInspection(
    context: Context,
    source: String,
    filePath: String,
): List<CodeDiagnostic> = withContext(Dispatchers.Default) {
    try {
        val json = pocketPython(context)
            .getModule("pocket_runner")
            .callAttr("inspect_code", source, filePath)
            .toString()
        val diagnosticsJson = JSONObject(json).optJSONArray("diagnostics") ?: JSONArray()
        val lineOffsets = sourceLineOffsets(source)
        List(diagnosticsJson.length()) { index ->
            val item = diagnosticsJson.optJSONObject(index) ?: JSONObject()
            val line = item.optInt("line", 1)
            val column = item.optInt("column", 0)
            val endLine = item.optInt("endLine", line)
            val endColumn = item.optInt("endColumn", column + 1)
            val start = sourceOffsetForLineColumn(lineOffsets, line, column, source.length)
            val rawEnd = sourceOffsetForLineColumn(lineOffsets, endLine, endColumn, source.length)
            CodeDiagnostic(
                start = start,
                end = rawEnd.coerceAtLeast((start + 1).coerceAtMost(source.length)),
                severity = item.optString("severity", "hint"),
                message = item.optString("message", "Inspection"),
                source = item.optString("source", "inspection"),
                line = line
            )
        }
    } catch (_: Throwable) {
        emptyList()
    }
}

private fun sourceLineOffsets(source: String): List<Int> {
    val offsets = mutableListOf(0)
    source.forEachIndexed { index, char ->
        if (char == '\n' && index + 1 <= source.length) {
            offsets += index + 1
        }
    }
    return offsets
}

private fun sourceOffsetForLineColumn(
    lineOffsets: List<Int>,
    line: Int,
    column: Int,
    sourceLength: Int,
): Int {
    val lineIndex = (line - 1).coerceIn(0, (lineOffsets.size - 1).coerceAtLeast(0))
    return (lineOffsets.getOrElse(lineIndex) { 0 } + column.coerceAtLeast(0)).coerceIn(0, sourceLength)
}

private suspend fun runPythonUnitTests(
    context: Context,
    arguments: List<String>,
    workingDirectoryPath: String,
    rootDirectoryPath: String,
    virtualEnvironmentPath: String? = null,
    runController: TerminalRunController? = null,
): RunResult = withContext(Dispatchers.Default) {
    try {
        val json = pocketPython(context)
            .getModule("pocket_runner")
            .callAttr(
                "run_unittest",
                JSONArray(arguments).toString(),
                workingDirectoryPath,
                rootDirectoryPath,
                virtualEnvironmentPath,
                runController
            )
            .toString()
        val result = JSONObject(json)
        RunResult(
            success = result.optBoolean("success"),
            output = result.optString("output"),
            error = result.optString("error"),
            stopped = result.optBoolean("stopped")
        )
    } catch (error: Throwable) {
        RunResult(
            success = false,
            output = "",
            error = error.stackTraceToString()
        )
    }
}

private fun idePalette(theme: IdeTheme): IdePalette {
    return when (theme) {
        IdeTheme.DarkBlue -> darkIdePalette(
            backgroundTop = Color(0xFF071524),
            backgroundBottom = Color(0xFF0C2944),
            panel = Color(0xFF0D2944),
            elevatedPanel = Color(0xFF091E33),
            selectedTab = Color(0xFF173B5E),
            border = Color(0xFF315D82),
            accent = Color(0xFF65A8FF),
            run = Color(0xFF0E6B3E),
        )
        IdeTheme.DarkPurple -> darkIdePalette(
            backgroundTop = Color(0xFF100A1D),
            backgroundBottom = Color(0xFF2A1746),
            panel = Color(0xFF24163B),
            elevatedPanel = Color(0xFF1A102B),
            selectedTab = Color(0xFF3B245D),
            border = Color(0xFF70449A),
            accent = Color(0xFFCA83FF),
            run = Color(0xFF6534D5),
        )
        IdeTheme.DarkGreen -> darkIdePalette(
            backgroundTop = Color(0xFF031611),
            backgroundBottom = Color(0xFF06412F),
            panel = Color(0xFF073727),
            elevatedPanel = Color(0xFF05271D),
            selectedTab = Color(0xFF0E5139),
            border = Color(0xFF24845D),
            accent = Color(0xFF4DE09B),
            run = Color(0xFF07843F),
        )
        IdeTheme.DarkTeal -> darkIdePalette(
            backgroundTop = Color(0xFF031A20),
            backgroundBottom = Color(0xFF064B56),
            panel = Color(0xFF073D47),
            elevatedPanel = Color(0xFF052A32),
            selectedTab = Color(0xFF0B5966),
            border = Color(0xFF218696),
            accent = Color(0xFF31D4DF),
            run = Color(0xFF129DA5),
        )
        IdeTheme.DarkOrange -> darkIdePalette(
            backgroundTop = Color(0xFF1B0E03),
            backgroundBottom = Color(0xFF462706),
            panel = Color(0xFF352214),
            elevatedPanel = Color(0xFF27170B),
            selectedTab = Color(0xFF543116),
            border = Color(0xFF9B5717),
            accent = Color(0xFFFF9A35),
            run = Color(0xFFD75B00),
        )
        IdeTheme.DarkGray -> darkIdePalette(
            backgroundTop = Color(0xFF101417),
            backgroundBottom = Color(0xFF2A3034),
            panel = Color(0xFF20272C),
            elevatedPanel = Color(0xFF181E22),
            selectedTab = Color(0xFF343D43),
            border = Color(0xFF56636C),
            accent = Color(0xFFBBC5CC),
            run = Color(0xFF48535B),
        )
        IdeTheme.LightBlue -> lightIdePalette(
            backgroundTop = Color(0xFFF6F9FF),
            backgroundBottom = Color(0xFFDDE8FF),
            elevatedPanel = Color(0xFFF1F5FF),
            selectedTab = Color(0xFFE5EDFF),
            border = Color(0xFF9BB6F4),
            accent = Color(0xFF426BDE),
            run = Color(0xFF486CF1),
        )
        IdeTheme.LightPurple -> lightIdePalette(
            backgroundTop = Color(0xFFFCF8FF),
            backgroundBottom = Color(0xFFF0E2FF),
            elevatedPanel = Color(0xFFF8F1FF),
            selectedTab = Color(0xFFF0E1FF),
            border = Color(0xFFC79AEF),
            accent = Color(0xFF8A45D8),
            run = Color(0xFF9349E8),
        )
        IdeTheme.LightGreen -> lightIdePalette(
            backgroundTop = Color(0xFFF7FCF9),
            backgroundBottom = Color(0xFFDDF4E7),
            elevatedPanel = Color(0xFFF0F9F4),
            selectedTab = Color(0xFFE2F5E9),
            border = Color(0xFF8ACCA5),
            accent = Color(0xFF278F58),
            run = Color(0xFF32A967),
        )
        IdeTheme.LightTeal -> lightIdePalette(
            backgroundTop = Color(0xFFF5FCFD),
            backgroundBottom = Color(0xFFD8F1F4),
            elevatedPanel = Color(0xFFEDF9FA),
            selectedTab = Color(0xFFDCF3F5),
            border = Color(0xFF7CC8D0),
            accent = Color(0xFF168899),
            run = Color(0xFF1A9DA8),
        )
        IdeTheme.LightOrange -> lightIdePalette(
            backgroundTop = Color(0xFFFFFAF6),
            backgroundBottom = Color(0xFFFFE8D7),
            elevatedPanel = Color(0xFFFFF4EC),
            selectedTab = Color(0xFFFFE9DA),
            border = Color(0xFFFFB57D),
            accent = Color(0xFFE66516),
            run = Color(0xFFFF6D25),
        )
        IdeTheme.LightGray -> lightIdePalette(
            backgroundTop = Color(0xFFFAFBFC),
            backgroundBottom = Color(0xFFE8EBEE),
            elevatedPanel = Color(0xFFF4F5F6),
            selectedTab = Color(0xFFECEFF1),
            border = Color(0xFFB7C0C7),
            accent = Color(0xFF59656E),
            run = Color(0xFF737D85),
        )
    }
}

private fun darkIdePalette(
    backgroundTop: Color,
    backgroundBottom: Color,
    panel: Color,
    elevatedPanel: Color,
    selectedTab: Color,
    border: Color,
    accent: Color,
    run: Color,
): IdePalette = IdePalette(
    backgroundTop = backgroundTop,
    backgroundBottom = backgroundBottom,
    panel = panel,
    elevatedPanel = elevatedPanel,
    tab = elevatedPanel,
    selectedTab = selectedTab,
    text = Color.White,
    mutedText = Color(0xFFD8DEEB),
    subtleText = Color(0xFFA9B5CA),
    border = border,
    strongBorder = accent.copy(alpha = 0.78f),
    accent = accent,
    run = run,
    runText = Color.White,
    terminalPrompt = Color(0xFF59E391),
    divider = border.copy(alpha = 0.86f),
    error = Color(0xFFFF7B7B),
    warning = Color(0xFFFFCC66),
    success = Color(0xFF59E391),
    info = Color(0xFF7DE3FF),
)

private fun lightIdePalette(
    backgroundTop: Color,
    backgroundBottom: Color,
    elevatedPanel: Color,
    selectedTab: Color,
    border: Color,
    accent: Color,
    run: Color,
): IdePalette = IdePalette(
    backgroundTop = backgroundTop,
    backgroundBottom = backgroundBottom,
    panel = Color.White,
    elevatedPanel = elevatedPanel,
    tab = Color.White,
    selectedTab = selectedTab,
    text = Color(0xFF151A24),
    mutedText = Color(0xFF475569),
    subtleText = Color(0xFF667085),
    border = border,
    strongBorder = accent.copy(alpha = 0.82f),
    accent = accent,
    run = run,
    runText = Color.White,
    terminalPrompt = Color(0xFF087A43),
    divider = Color(0xFF64748B),
    error = Color(0xFFB42318),
    warning = Color(0xFF8A5300),
    success = Color(0xFF087A43),
    info = Color(0xFF075E73),
)

private fun SnapshotStateList<PythonFile>.replaceFile(
    fileId: Long,
    transform: PythonFile.() -> PythonFile,
) {
    val index = indexOfFirst { it.id == fileId }
    if (index >= 0) {
        this[index] = this[index].transform()
    }
}

private fun nextUntitledFileName(files: List<PythonFile>): String {
    var index = 1
    while (true) {
        val candidate = "file$index.py"
        if (files.none { it.name == candidate }) return candidate
        index += 1
    }
}

private fun loadFiles(directory: File): List<PythonFile> {
    val importLinks = loadImportLinks(directory)
    val files = directory
        .walkTopDown()
        .onEnter { file -> file == directory || !isProjectIgnoredDirectory(file) }
        .mapNotNull { file -> file.toEditableProjectFile(directory, importLinks) }
        .sortedByDescending { it.modifiedAt }
        .toList()

    return files.ifEmpty { listOf(createDefaultFile(directory)) }
}

private fun loadIdeSession(
    context: Context,
    rootDirectory: File,
): IdeSession {
    val preferences = idePreferences(context)
    return IdeSession(
        activeFileId = preferences.getLong(PREF_ACTIVE_FILE_ID, -1L),
        activeFilePath = preferences.getString(PREF_ACTIVE_FILE_PATH, null)?.takeIf { it.isNotBlank() },
        workingDirectoryPath = preferences.getString(PREF_WORKING_DIRECTORY_PATH, null)?.takeIf { it.isNotBlank() },
        terminalDirectoryPath = preferences.getString(PREF_TERMINAL_DIRECTORY_PATH, null)?.takeIf { it.isNotBlank() },
        expandedFolderPaths = normalizedWorkspaceDirectoryPaths(
            rootDirectory = rootDirectory,
            paths = stringListFromJson(preferences.getString(PREF_EXPANDED_FOLDER_PATHS, null))
        ),
        explorerVisible = preferences.getBoolean(PREF_EXPLORER_VISIBLE, false),
        fileSearch = preferences.getString(PREF_FILE_SEARCH, "").orEmpty(),
        terminalInput = preferences.getString(PREF_TERMINAL_INPUT, "").orEmpty(),
        terminalText = preferences.getString(PREF_TERMINAL_TEXT, "").orEmpty(),
        fullScreenMode = preferences.getString(PREF_FULL_SCREEN_MODE, null)
            ?.let { savedMode -> runCatching { FullScreenMode.valueOf(savedMode) }.getOrNull() }
    )
}

private fun saveIdeSession(
    context: Context,
    session: IdeSession,
) {
    idePreferences(context).edit {
        putLong(PREF_ACTIVE_FILE_ID, session.activeFileId)
        putBoolean(PREF_EXPLORER_VISIBLE, session.explorerVisible)
        putString(PREF_FILE_SEARCH, session.fileSearch)
        putString(PREF_TERMINAL_INPUT, session.terminalInput)
        putString(PREF_TERMINAL_TEXT, session.terminalText)
        putString(PREF_EXPANDED_FOLDER_PATHS, jsonStringList(session.expandedFolderPaths))

        if (session.activeFilePath.isNullOrBlank()) remove(PREF_ACTIVE_FILE_PATH)
        else putString(PREF_ACTIVE_FILE_PATH, session.activeFilePath)

        if (session.workingDirectoryPath.isNullOrBlank()) remove(PREF_WORKING_DIRECTORY_PATH)
        else putString(PREF_WORKING_DIRECTORY_PATH, session.workingDirectoryPath)

        if (session.terminalDirectoryPath.isNullOrBlank()) remove(PREF_TERMINAL_DIRECTORY_PATH)
        else putString(PREF_TERMINAL_DIRECTORY_PATH, session.terminalDirectoryPath)

        if (session.fullScreenMode == null) remove(PREF_FULL_SCREEN_MODE)
        else putString(PREF_FULL_SCREEN_MODE, session.fullScreenMode.name)
    }
}

private fun loadIdeTheme(context: Context): IdeTheme {
    val preferences = idePreferences(context)
    val savedTheme = preferences.getString(PREF_IDE_THEME, null)
        ?.let { savedName -> IdeTheme.entries.firstOrNull { it.name == savedName } }

    return savedTheme ?: if (preferences.getBoolean(PREF_DARK_MODE, true)) {
        IdeTheme.DarkBlue
    } else {
        IdeTheme.LightBlue
    }
}

private fun saveIdeTheme(
    context: Context,
    theme: IdeTheme,
) {
    idePreferences(context).edit {
        putString(PREF_IDE_THEME, theme.name)
        putBoolean(PREF_DARK_MODE, theme.isDark)
    }
}

private fun idePreferences(context: Context) = context.getSharedPreferences(IDE_PREFS_NAME, Context.MODE_PRIVATE)

internal fun clearLocalCodingData(context: Context) {
    listOf(
        File(context.filesDir, "projects"),
        File(context.filesDir, "workspace"),
        File(context.filesDir, ".runtime"),
    ).forEach { directory ->
        runCatching { directory.deleteRecursively() }
    }
    File(context.filesDir, "projects").mkdirs()
    idePreferences(context).edit().clear().commit()
    context.contentResolver.persistedUriPermissions.forEach { permission ->
        val flags = (if (permission.isReadPermission) Intent.FLAG_GRANT_READ_URI_PERMISSION else 0) or
            (if (permission.isWritePermission) Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)
        if (flags != 0) {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(permission.uri, flags)
            }
        }
    }
}

private fun workspaceDirectory(context: Context): File {
    return File(context.filesDir, "workspace").apply {
        if (!exists()) {
            mkdirs()
        }
    }
}

private fun createDefaultFile(directory: File): PythonFile {
    val file = File(directory, "main.py")
    if (!file.exists()) {
        file.writeText("")
    }
    return file.toPythonFile(directory)
}

private fun File.toPythonFile(
    rootDirectory: File,
    importLinks: Map<String, String> = loadImportLinks(rootDirectory),
): PythonFile {
    return PythonFile(
        id = fileIdFor(this),
        name = name,
        content = runCatching { readText() }.getOrDefault(""),
        filePath = absolutePath,
        modifiedAt = lastModified().takeIf { it > 0L } ?: System.currentTimeMillis(),
        externalUri = importLinks[relativeWorkspacePath(rootDirectory, this)]
    )
}

private fun File.toEditableProjectFile(
    rootDirectory: File,
    importLinks: Map<String, String>,
): PythonFile? {
    if (!isEditableProjectFile(rootDirectory, this)) return null
    val text = runCatching { readText() }.getOrNull() ?: return null
    return PythonFile(
        id = fileIdFor(this),
        name = name,
        content = text,
        filePath = absolutePath,
        modifiedAt = lastModified().takeIf { it > 0L } ?: System.currentTimeMillis(),
        externalUri = importLinks[relativeWorkspacePath(rootDirectory, this)]
    )
}

private fun isEditableProjectFile(rootDirectory: File, file: File): Boolean {
    if (!file.isFile) return false
    if (isWorkspaceMetadataPath(file)) return false
    if (file.name == IMPORT_LINKS_FILE) return false
    if (isInsideWorkspaceMetadataDirectory(rootDirectory, file)) return false
    if (file.length() > MAX_EDITABLE_PROJECT_FILE_BYTES) return false
    return fileLooksLikeText(file)
}

private fun fileLooksLikeText(file: File): Boolean {
    return runCatching {
        file.inputStream().use { input ->
            val buffer = ByteArray(1024)
            val read = input.read(buffer)
            if (read <= 0) return@use true
            buffer.take(read).none { byte -> byte.toInt() == 0 }
        }
    }.getOrDefault(false)
}

private fun fileIdFor(file: File): Long {
    return file.absolutePath.fold(1125899906842597L) { hash, char ->
        hash * 31 + char.code
    }
}

private fun saveFileContent(filePath: String, content: String) {
    runCatching {
        File(filePath).writeText(content)
    }
}

private fun saveWorkspaceFiles(files: List<PythonFile>) {
    files.forEach { file ->
        saveFileContent(file.filePath, file.content)
    }
}

private fun stringListFromJson(rawJson: String?): List<String> {
    if (rawJson.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(rawJson)
        List(array.length()) { index -> array.optString(index) }
            .filter { it.isNotBlank() }
    }.getOrDefault(emptyList())
}

private fun jsonStringList(values: List<String>): String {
    val array = JSONArray()
    values.forEach { value ->
        if (value.isNotBlank()) {
            array.put(value)
        }
    }
    return array.toString()
}

private fun openDocumentIntent(): Intent {
    return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
    }
}

private fun openDocumentTreeIntent(): Intent {
    return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
    }
}

private fun syncExternalFileContent(
    context: Context,
    file: PythonFile,
    content: String,
): Boolean {
    val externalUri = file.externalUri ?: return false
    return runCatching {
        val outputStream = context.contentResolver.openOutputStream(externalUri.toUri(), "wt")
            ?: error("Could not open the original file for writing.")
        outputStream.use { output ->
            output.write(content.toByteArray(Charsets.UTF_8))
        }
    }.isSuccess
}

private fun importFileFromUri(
    context: Context,
    uri: Uri,
    destinationDirectory: File,
    rootDirectory: File,
): ImportResult {
    persistDocumentPermission(context, uri)
    val rawName = displayNameForUri(context, uri, "imported_file.py")
    val targetFile = uniqueChildFile(
        destinationDirectory = destinationDirectory,
        requestedName = sanitizeImportedName(rawName, "imported_file.py")
    )

    return runCatching {
        copyUriToFile(context, uri, targetFile)
        setImportLink(rootDirectory, targetFile, uri)
        ImportResult(
            success = true,
            message = "Imported ${targetFile.name} into ${destinationDirectory.name}. Edits will sync back to the original file when Android grants write access.",
            selectedFilePath = targetFile.takeIf { it.extension.equals("py", ignoreCase = true) }?.absolutePath,
            workingDirectoryPath = destinationDirectory.absolutePath
        )
    }.getOrElse { error ->
        ImportResult(
            success = false,
            message = error.message ?: "Could not import $rawName."
        )
    }
}

private fun importFolderFromUri(
    context: Context,
    uri: Uri,
    destinationDirectory: File,
    rootDirectory: File,
): ImportResult {
    persistDocumentPermission(context, uri)
    val rootDocumentUri = rootDocumentUriForTree(uri)
        ?: return ImportResult(false, "Could not open the selected folder.")
    val sourceFolderName = displayNameForUri(context, rootDocumentUri, "imported_folder")
    val folderName = sanitizeImportedName(sourceFolderName, "imported_folder")
    val targetFolder = uniqueChildDirectory(destinationDirectory, folderName)
    var firstPythonFile: File? = null

    fun copyDocumentTree(source: Uri, targetDirectory: File) {
        targetDirectory.mkdirs()
        listChildDocuments(context, uri, source).forEach { child ->
            val childName = sanitizeImportedName(
                rawName = child.name,
                fallback = if (child.isDirectory) "folder" else "file"
            )

            if (child.isDirectory) {
                val childTarget = uniqueChildDirectory(targetDirectory, childName)
                copyDocumentTree(child.uri, childTarget)
            } else {
                val childTarget = uniqueChildFile(targetDirectory, childName)
                copyUriToFile(context, child.uri, childTarget)
                setImportLink(rootDirectory, childTarget, child.uri)
                if (firstPythonFile == null && childTarget.extension.equals("py", ignoreCase = true)) {
                    firstPythonFile = childTarget
                }
            }
        }
    }

    return runCatching {
        copyDocumentTree(rootDocumentUri, targetFolder)
        ImportResult(
            success = true,
            message = "Imported folder ${targetFolder.name} into ${destinationDirectory.name}. Existing files will sync back to their originals when Android grants write access.",
            selectedFilePath = firstPythonFile?.absolutePath,
            workingDirectoryPath = targetFolder.absolutePath
        )
    }.getOrElse { error ->
        runCatching { targetFolder.deleteRecursively() }
        ImportResult(
            success = false,
            message = error.message ?: "Could not import $sourceFolderName."
        )
    }
}

private fun exportFolderToUri(
    context: Context,
    treeUri: Uri,
    sourceDirectory: File,
): StorageOperationResult {
    if (!sourceDirectory.isDirectory) {
        return StorageOperationResult(false, "The current folder is no longer available.")
    }

    persistDocumentWritePermission(context, treeUri)
    val destinationRoot = rootDocumentUriForTree(treeUri)
        ?: return StorageOperationResult(false, "Could not open the selected destination.")
    val destinationName = displayNameForUri(context, destinationRoot, "selected location")
    val requestedName = sanitizeImportedName(sourceDirectory.name, "PocketCodedPy project")
    val exportName = uniqueSafChildName(context, treeUri, destinationRoot, requestedName)
    var exportedRoot: Uri? = null
    var fileCount = 0
    var folderCount = 1
    var skippedCount = 0
    val canonicalSource = runCatching { sourceDirectory.canonicalFile }.getOrDefault(sourceDirectory.absoluteFile)

    fun copyDirectory(source: File, destination: Uri) {
        source.listFiles()
            ?.sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() }))
            .orEmpty()
            .forEach { child ->
                val canonicalChild = runCatching { child.canonicalFile }.getOrDefault(child.absoluteFile)
                if (!isSameOrInside(canonicalSource, canonicalChild) || shouldSkipProjectExport(child)) {
                    skippedCount += 1
                    return@forEach
                }

                if (child.isDirectory) {
                    val childDirectory = DocumentsContract.createDocument(
                        context.contentResolver,
                        destination,
                        DocumentsContract.Document.MIME_TYPE_DIR,
                        child.name,
                    ) ?: error("Could not create folder ${child.name}.")
                    folderCount += 1
                    copyDirectory(child, childDirectory)
                } else if (child.isFile) {
                    val childDocument = DocumentsContract.createDocument(
                        context.contentResolver,
                        destination,
                        URLConnection.guessContentTypeFromName(child.name) ?: "application/octet-stream",
                        child.name,
                    ) ?: error("Could not create file ${child.name}.")
                    val output = context.contentResolver.openOutputStream(childDocument, "w")
                        ?: error("Could not write ${child.name}.")
                    child.inputStream().use { input ->
                        output.use { stream -> input.copyTo(stream) }
                    }
                    fileCount += 1
                }
            }
    }

    return runCatching {
        exportedRoot = DocumentsContract.createDocument(
            context.contentResolver,
            destinationRoot,
            DocumentsContract.Document.MIME_TYPE_DIR,
            exportName,
        ) ?: error("Could not create the project folder in $destinationName.")
        copyDirectory(sourceDirectory, exportedRoot!!)

        val skippedSummary = if (skippedCount > 0) {
            " $skippedCount private, cache, or environment item(s) were left out."
        } else {
            ""
        }
        StorageOperationResult(
            success = true,
            message = "Saved $exportName to $destinationName with $fileCount file(s) in $folderCount folder(s).$skippedSummary",
        )
    }.getOrElse { error ->
        exportedRoot?.let { partialExport ->
            runCatching { DocumentsContract.deleteDocument(context.contentResolver, partialExport) }
        }
        StorageOperationResult(
            success = false,
            message = error.message ?: "Could not save ${sourceDirectory.name}.",
        )
    }
}

private fun uniqueSafChildName(
    context: Context,
    treeUri: Uri,
    parentDocumentUri: Uri,
    requestedName: String,
): String {
    val existingNames = listChildDocuments(context, treeUri, parentDocumentUri)
        .mapTo(mutableSetOf()) { it.name.lowercase() }
    if (requestedName.lowercase() !in existingNames) return requestedName

    var index = 2
    while ("$requestedName ($index)".lowercase() in existingNames) {
        index += 1
    }
    return "$requestedName ($index)"
}

private fun shouldSkipProjectExport(file: File): Boolean {
    if (file.name in PROJECT_EXPORT_IGNORED_NAMES) return true
    if (file.isFile && file.extension.equals("pyc", ignoreCase = true)) return true
    return file.isDirectory && File(file, "pyvenv.cfg").isFile
}

private fun rootDocumentUriForTree(treeUri: Uri): Uri? {
    return runCatching {
        DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
    }.getOrNull()
}

private fun displayNameForUri(
    context: Context,
    uri: Uri,
    fallback: String,
): String {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    return runCatching {
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    }.getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: fallback
}

private fun listChildDocuments(
    context: Context,
    treeUri: Uri,
    parentDocumentUri: Uri,
): List<SafDocument> {
    val parentDocumentId = runCatching {
        DocumentsContract.getDocumentId(parentDocumentUri)
    }.getOrNull() ?: return emptyList()
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
    val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE
    )
    val children = mutableListOf<SafDocument>()

    runCatching {
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            if (idColumn < 0 || nameColumn < 0 || mimeColumn < 0) {
                return@use
            }

            while (cursor.moveToNext()) {
                val documentId = cursor.getString(idColumn)
                val name = cursor.getString(nameColumn).orEmpty()
                val mimeType = cursor.getString(mimeColumn).orEmpty()
                children.add(
                    SafDocument(
                        uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId),
                        name = name.ifBlank { if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) "folder" else "file" },
                        isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
                    )
                )
            }
        }
    }

    return children
}

private fun persistDocumentPermission(context: Context, uri: Uri) {
    val readWriteFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    runCatching {
        context.contentResolver.takePersistableUriPermission(uri, readWriteFlags)
    }.recoverCatching {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

private fun persistDocumentWritePermission(context: Context, uri: Uri) {
    val readWriteFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    runCatching {
        context.contentResolver.takePersistableUriPermission(uri, readWriteFlags)
    }.recoverCatching {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
}

private fun importLinksFile(rootDirectory: File): File {
    return File(rootDirectory, IMPORT_LINKS_FILE)
}

private fun loadImportLinks(rootDirectory: File): Map<String, String> {
    val file = importLinksFile(rootDirectory)
    if (!file.isFile) return emptyMap()

    return runCatching {
        val json = JSONObject(file.readText())
        json.keys().asSequence().mapNotNull { key ->
            json.optString(key).takeIf { it.isNotBlank() }?.let { uri ->
                key to uri
            }
        }.toMap()
    }.getOrDefault(emptyMap())
}

private fun saveImportLinks(
    rootDirectory: File,
    links: Map<String, String>,
) {
    val file = importLinksFile(rootDirectory)
    runCatching {
        if (links.isEmpty()) {
            file.delete()
            return@runCatching
        }

        val json = JSONObject()
        links.toSortedMap().forEach { (localPath, uri) ->
            json.put(localPath, uri)
        }
        file.writeText(json.toString())
    }
}

private fun setImportLink(
    rootDirectory: File,
    localFile: File,
    externalUri: Uri,
) {
    if (!isSameOrInside(rootDirectory, localFile)) return
    val links = loadImportLinks(rootDirectory).toMutableMap()
    links[relativeWorkspacePath(rootDirectory, localFile)] = externalUri.toString()
    saveImportLinks(rootDirectory, links)
}

private fun removeImportLink(
    rootDirectory: File,
    localFile: File,
) {
    val links = loadImportLinks(rootDirectory).toMutableMap()
    links.remove(relativeWorkspacePath(rootDirectory, localFile))
    saveImportLinks(rootDirectory, links)
}

private fun moveImportLink(
    rootDirectory: File,
    oldFile: File,
    newFile: File,
) {
    val oldPath = relativeWorkspacePath(rootDirectory, oldFile)
    val links = loadImportLinks(rootDirectory).toMutableMap()
    val externalUri = links.remove(oldPath) ?: return
    links[relativeWorkspacePath(rootDirectory, newFile)] = externalUri
    saveImportLinks(rootDirectory, links)
}

private fun removeImportLinksUnder(
    rootDirectory: File,
    directory: File,
) {
    val prefix = relativeWorkspacePath(rootDirectory, directory).trimEnd('/')
    val links = loadImportLinks(rootDirectory).filterKeys { path ->
        path != prefix && !path.startsWith("$prefix/")
    }
    saveImportLinks(rootDirectory, links)
}

private fun moveImportLinksUnder(
    rootDirectory: File,
    oldDirectory: File,
    newDirectory: File,
) {
    val oldPrefix = relativeWorkspacePath(rootDirectory, oldDirectory).trimEnd('/')
    val newPrefix = relativeWorkspacePath(rootDirectory, newDirectory).trimEnd('/')
    val links = loadImportLinks(rootDirectory).toMutableMap()
    val movedLinks = links
        .filterKeys { path -> path == oldPrefix || path.startsWith("$oldPrefix/") }
        .toList()

    movedLinks.forEach { (oldPath, uri) ->
        links.remove(oldPath)
        val suffix = oldPath.removePrefix(oldPrefix).trimStart('/')
        val newPath = if (suffix.isBlank()) newPrefix else "$newPrefix/$suffix"
        links[newPath] = uri
    }
    saveImportLinks(rootDirectory, links)
}

private fun relativeWorkspacePath(
    rootDirectory: File,
    file: File,
): String {
    return runCatching {
        file.absoluteFile.relativeTo(rootDirectory.absoluteFile).path.replace(File.separatorChar, '/')
    }.getOrDefault(file.name)
}

private fun copyUriToFile(
    context: Context,
    uri: Uri,
    targetFile: File,
) {
    targetFile.parentFile?.mkdirs()
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: error("Could not open the selected item.")
    inputStream.use { input ->
        targetFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

private fun uniqueChildFile(
    destinationDirectory: File,
    requestedName: String,
): File {
    var candidate = File(destinationDirectory, requestedName)
    if (!candidate.exists()) return candidate

    val extension = candidate.extension
    val baseName = if (extension.isBlank()) {
        requestedName
    } else {
        requestedName.removeSuffix(".$extension")
    }
    var index = 2
    while (candidate.exists()) {
        val nextName = if (extension.isBlank()) {
            "$baseName ($index)"
        } else {
            "$baseName ($index).$extension"
        }
        candidate = File(destinationDirectory, nextName)
        index += 1
    }
    return candidate
}

private fun uniqueChildDirectory(
    destinationDirectory: File,
    requestedName: String,
): File {
    var candidate = File(destinationDirectory, requestedName)
    var index = 2
    while (candidate.exists()) {
        candidate = File(destinationDirectory, "$requestedName ($index)")
        index += 1
    }
    return candidate
}

private fun sanitizeImportedName(rawName: String, fallback: String): String {
    val cleaned = rawName
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .trim()
        .trim('.')
    return cleaned.ifBlank { fallback }
}

private fun normalizePythonFileName(rawName: String): String? {
    val trimmed = rawName.trim()
    if (trimmed.isBlank()) return null
    if (trimmed.contains("/") || trimmed.contains("\\")) return null

    val fileName = if (trimmed.endsWith(".py", ignoreCase = true)) {
        trimmed
    } else {
        "$trimmed.py"
    }

    val simplePythonName = Regex("[A-Za-z0-9][A-Za-z0-9_. -]*\\.py", RegexOption.IGNORE_CASE)
    return fileName.takeIf { simplePythonName.matches(it) }
}

private fun normalizeFolderName(rawName: String): String? {
    val trimmed = rawName.trim()
    if (trimmed.isBlank()) return null
    if (trimmed.contains("/") || trimmed.contains("\\")) return null

    val simpleFolderName = Regex("[A-Za-z0-9][A-Za-z0-9_. -]*")
    return trimmed.takeIf { simpleFolderName.matches(it) }
}

private fun isInDirectory(directory: File, file: File): Boolean {
    return runCatching {
        file.canonicalFile.parentFile?.canonicalPath == directory.canonicalPath
    }.getOrDefault(false)
}

private fun isWorkspaceMetadataPath(file: File): Boolean {
    return file.name in WORKSPACE_METADATA_NAMES
}

private fun isProjectIgnoredDirectory(file: File): Boolean {
    return file.isDirectory && (
        isWorkspaceMetadataPath(file) ||
            File(file, "pyvenv.cfg").isFile
        )
}

private fun isInsideWorkspaceMetadataDirectory(rootDirectory: File, file: File): Boolean {
    val root = runCatching { rootDirectory.canonicalFile }.getOrDefault(rootDirectory.absoluteFile)
    var current = runCatching {
        (if (file.isDirectory) file else file.parentFile ?: file).canonicalFile
    }.getOrDefault(file.absoluteFile)

    while (isSameOrInside(root, current)) {
        if (isProjectIgnoredDirectory(current)) return true
        if (samePath(root.absolutePath, current.absolutePath)) break
        current = current.parentFile ?: break
    }
    return false
}

private fun hasWorkspaceFilesOutsideMetadata(directory: File): Boolean {
    if (!directory.isDirectory) return false
    return directory
        .walkTopDown()
        .onEnter { file -> file == directory || !isProjectIgnoredDirectory(file) }
        .any { file -> file.isFile && file.name != IMPORT_LINKS_FILE && !isWorkspaceMetadataPath(file) }
}

private fun buildExplorerEntries(
    rootDirectory: File,
    files: List<PythonFile>,
    expandedFolderPaths: Set<String>,
    query: String,
): List<ExplorerEntry> {
    val normalizedQuery = query.trim()
    val entries = mutableListOf<ExplorerEntry>()
    val directoryMatchCache = mutableMapOf<String, Boolean>()

    fun directoryMatches(directory: File): Boolean {
        if (normalizedQuery.isBlank()) return true
        return directoryMatchCache.getOrPut(directory.absolutePath) {
            directory.name.contains(normalizedQuery, ignoreCase = true) ||
                files.any { file ->
                    val localFile = File(file.filePath)
                    isSameOrInside(directory, localFile) &&
                        file.name.contains(normalizedQuery, ignoreCase = true)
                } ||
                directory
                    .listFiles { child -> child.isDirectory && !isProjectIgnoredDirectory(child) }
                    .orEmpty()
                    .any { child -> directoryMatches(child) }
        }
    }

    fun appendDirectory(directory: File, depth: Int) {
        val childDirectories = directory
            .listFiles { child -> child.isDirectory && !isProjectIgnoredDirectory(child) }
            .orEmpty()
            .sortedBy { it.name.lowercase() }

        childDirectories.forEach { childDirectory ->
            if (directoryMatches(childDirectory)) {
                val target = childDirectory.toFolderTarget(rootDirectory)
                val expanded = expandedFolderPaths.any { samePath(it, childDirectory.absolutePath) } ||
                    normalizedQuery.isNotBlank()
                entries.add(
                    ExplorerEntry.Directory(
                        target = target,
                        depth = depth,
                        expanded = expanded
                    )
                )
                if (expanded) {
                    appendDirectory(childDirectory, depth + 1)
                }
            }
        }

        files
            .filter { it.isDirectChildOf(directory) }
            .filter { normalizedQuery.isBlank() || it.name.contains(normalizedQuery, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
            .forEach { file ->
                entries.add(
                    ExplorerEntry.PythonSourceFile(
                        file = file,
                        depth = depth
                    )
                )
            }
    }

    appendDirectory(rootDirectory, depth = 0)
    return entries
}

private fun File.toFolderTarget(rootDirectory: File): FolderTarget {
    val relative = runCatching { relativeTo(rootDirectory).path.replace(File.separatorChar, '/') }
        .getOrDefault(name)
    return FolderTarget(
        name = name,
        path = absolutePath,
        relativePath = relative
    )
}

private fun PythonFile.isDirectChildOf(directory: File): Boolean {
    return runCatching {
        File(filePath).canonicalFile.parentFile?.canonicalPath == directory.canonicalPath
    }.getOrDefault(false)
}

private fun PythonFile.parentDirectoryPath(rootDirectory: File): String {
    return File(filePath).parentFile?.absolutePath ?: rootDirectory.absolutePath
}

private fun validDirectoryOrRoot(rootDirectory: File, candidatePath: String): File {
    val candidate = File(candidatePath)
    return if (candidate.isDirectory && isSameOrInside(rootDirectory, candidate)) {
        candidate
    } else {
        rootDirectory
    }
}

private fun normalizedWorkspaceDirectoryPaths(
    rootDirectory: File,
    paths: List<String>,
): List<String> {
    val normalizedPaths = mutableListOf<String>()
    paths.forEach { path ->
        val directory = File(path)
        if (directory.isDirectory && isSameOrInside(rootDirectory, directory)) {
            normalizedPaths.addUniquePath(directory.absolutePath)
        }
    }
    return normalizedPaths
}

private fun MutableList<String>.addUniquePath(path: String) {
    if (none { samePath(it, path) }) {
        add(path)
    }
}

private fun samePath(first: String, second: String): Boolean {
    return runCatching {
        File(first).canonicalPath == File(second).canonicalPath
    }.getOrDefault(first == second)
}

private fun isSameOrInside(parent: File, child: File): Boolean {
    return runCatching {
        val parentPath = parent.canonicalPath
        val childPath = child.canonicalPath
        childPath == parentPath || childPath.startsWith(parentPath + File.separator)
    }.getOrDefault(false)
}

private fun relativeDirectoryLabel(rootDirectory: File, directory: File): String {
    val relative = runCatching {
        val root = rootDirectory.canonicalFile
        val current = directory.canonicalFile
        if (!isSameOrInside(root, current)) {
            return@runCatching ""
        }
        current.relativeTo(root).path.replace(File.separatorChar, '/')
    }.getOrDefault("")
    return if (relative.isBlank() || relative == ".") {
        "~"
    } else {
        "~/$relative"
    }
}

private fun terminalPrompt(
    rootDirectory: File,
    directory: File,
    virtualEnvironment: File? = null,
): String {
    val environmentPrefix = virtualEnvironment?.let { "(${it.name}) " }.orEmpty()
    return "$environmentPrefix${relativeDirectoryLabel(rootDirectory, directory)}$"
}

@Preview(showBackground = true, widthDp = 390, heightDp = 840)
@Composable
private fun PocketIdePreviewDark() {
    PocketCodedPyTheme(darkTheme = true, dynamicColor = false) {
        PocketIdeApp(
            ideTheme = IdeTheme.DarkBlue,
            onToggleTheme = {},
            projectDirectory = File(LocalContext.current.filesDir, "preview-project"),
            onBackToProjects = {},
        )
    }
}
