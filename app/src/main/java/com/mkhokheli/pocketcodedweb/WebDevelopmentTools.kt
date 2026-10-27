package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal data class WebDevelopmentActions(
    val fileStore: ProjectFileStore,
    val development: WebDevelopmentController,
    val source: (String, Int) -> Unit,
    val startDevelopment: (String, String, List<String>) -> Unit,
    val recover: (String, (String?) -> Unit) -> Unit,
    val discardRecovery: (String, (String?) -> Unit) -> Unit,
    val clearOutput: () -> Unit,
    val history: List<String>,
    val recall: (String) -> Unit,
    val scripts: () -> List<String>,
    val command: (String) -> Unit,
    val preview: () -> Unit,
    val refreshHistory: () -> Unit = {},
    val prepareSearch: suspend () -> Unit = {},
    val filesChanged: (List<File>) -> Unit = {},
)
internal val LocalApiTester = staticCompositionLocalOf<ApiTesterState?> { null }

internal val LocalWebDevelopmentActions = staticCompositionLocalOf<WebDevelopmentActions?> { null }

@Composable
private fun WebToolsMenuButton(description: String, palette: WebPalette, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(Icons.Rounded.MoreVert, contentDescription = description, tint = palette.text,
            modifier = Modifier.size(22.dp))
    }
}

@Composable
internal fun WebTerminalMenu(output: String, palette: WebPalette) {
    val actions = LocalWebDevelopmentActions.current
    val clipboard = LocalClipboardManager.current
    var expanded by remember { mutableStateOf(false) }
    val historyScroll = rememberScrollState()
    val rowHeight = maxOf(48.dp, (20f * LocalDensity.current.fontScale + 16f).dp)
    val itemColors = MenuDefaults.itemColors(textColor = palette.text, disabledTextColor = palette.subtleText)
    Box {
        WebToolsMenuButton("Terminal tools", palette) {
            actions?.refreshHistory?.invoke()
            expanded = true
        }
        LaunchedEffect(expanded) { if (expanded) historyScroll.scrollTo(0) }
        DropdownMenu(expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 220.dp, max = 320.dp),
            containerColor = palette.elevatedPanel, shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, palette.border)) {
            DropdownMenuItem(text = { Text("Copy output") }, colors = itemColors, onClick = {
                clipboard.setText(AnnotatedString(output))
                expanded = false
            })
            DropdownMenuItem(text = { Text("Clear output") }, colors = itemColors,
                enabled = actions != null, onClick = {
                    actions?.clearOutput?.invoke()
                    expanded = false
                })
            HorizontalDivider(color = palette.border)
            Text("Recent commands", color = palette.mutedText,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            Column(Modifier.fillMaxWidth().height(rowHeight * 2)
                .testTag("terminal-command-history").verticalScroll(historyScroll)) {
                val history = actions?.history.orEmpty()
                if (history.isEmpty()) {
                    Text("No commands today", color = palette.subtleText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp))
                } else history.asReversed().forEach { command ->
                    DropdownMenuItem(
                        text = { Text(redactTerminalCommandForDisplay(command),
                            fontFamily = FontFamily.Monospace, maxLines = 1,
                            overflow = TextOverflow.Ellipsis) },
                        colors = itemColors, modifier = Modifier.height(rowHeight),
                        onClick = { actions?.recall?.invoke(command); expanded = false })
                }
            }
        }
    }
}

@Composable
internal fun WebDevelopmentMenu(
    file: WebFile,
    source: String,
    palette: WebPalette,
    onRestore: (String) -> Unit,
) {
    val actions = LocalWebDevelopmentActions.current
    val apiTester = LocalApiTester.current
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val configurationStore = remember(actions?.fileStore?.projectDirectory) {
        WebRunConfigurationStore(context, actions?.fileStore?.projectDirectory?.path.orEmpty())
    }
    var configurations by remember(configurationStore) { mutableStateOf(configurationStore.load()) }
    var configurationName by remember { mutableStateOf("") }
    var configurationCommand by remember { mutableStateOf("") }
    var revisions by remember(file.filePath) { mutableStateOf<List<FileRevision>>(emptyList()) }
    var error by remember(file.filePath) { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<String?>(null) }
    var problems by remember { mutableStateOf<List<CodeDiagnostic>>(emptyList()) }
    LaunchedEffect(dialog, file.filePath) {
        if (dialog == "Local history") {
            busy = true
            val result = withContext(Dispatchers.IO) { runCatching {
                val store = actions!!.fileStore
                store.save(file.filePath, source).getOrThrow()
                store.history(file.filePath)
            } }
            result.onSuccess { revisions = it }.onFailure { error = it.message }
            busy = false
        }
    }
    LaunchedEffect(dialog, if (dialog == "Problems") source else null) {
        if (dialog == "Problems") problems = withContext(Dispatchers.Default) {
            webDevelopmentDiagnostics(File(file.filePath), file.name, source)
        }
    }
    val itemColors = MenuDefaults.itemColors(textColor = palette.text, disabledTextColor = palette.subtleText)
    Box {
        WebToolsMenuButton("Editor tools", palette) { expanded = true }
        DropdownMenu(expanded, onDismissRequest = { expanded = false },
            containerColor = palette.elevatedPanel, shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, palette.border)) {
            DropdownMenuItem(text = { Text("API tester") }, colors = itemColors, enabled = apiTester != null,
                onClick = { expanded = false; apiTester?.open = true })
            DropdownMenuItem(text = { Text("Debug current file") }, colors = itemColors, enabled = actions != null && !actions.development.starting && file.name.substringAfterLast('.').lowercase() in setOf("js", "cjs", "mjs"),
                onClick = { expanded = false; actions?.startDevelopment?.invoke("debug", file.filePath, emptyList()) })
            DropdownMenuItem(text = { Text("Profile current file") }, colors = itemColors,
                enabled = actions != null && !actions.development.starting && file.name.substringAfterLast('.').lowercase() in setOf("js", "cjs", "mjs"),
                onClick = { expanded = false; actions?.startDevelopment?.invoke("profile", file.filePath, emptyList()) })
            DropdownMenuItem(text = { Text("Profiler") }, colors = itemColors, enabled = actions != null,
                onClick = { expanded = false; actions?.development?.panel = "Profiler" })
            DropdownMenuItem(text = { Text("Debugger") }, colors = itemColors, enabled = actions != null,
                onClick = { expanded = false; actions?.development?.panel = "Debugger" })
            DropdownMenuItem(text = { Text("Visual tests") }, colors = itemColors, enabled = actions != null,
                onClick = { expanded = false; actions?.development?.panel = "Tests" })
            DropdownMenuItem(text = { Text("Recovery drafts") }, colors = itemColors, enabled = actions != null,
                onClick = { expanded = false; actions?.development?.panel = "Recovery" })
            DropdownMenuItem(text = { Text("Run configurations") }, colors = itemColors, enabled = actions != null,
                onClick = { expanded = false; dialog = "Run configurations" })
            DropdownMenuItem(text = { Text("Local history") }, colors = itemColors, enabled = actions != null,
                onClick = { expanded = false; error = null; dialog = "Local history" })
            DropdownMenuItem(text = { Text("Inspect problems") }, colors = itemColors, onClick = { expanded = false; dialog = "Problems" })
            DropdownMenuItem(text = { Text("Tests and project scripts") }, colors = itemColors, enabled = actions != null,
                onClick = { expanded = false; dialog = "Tests and project scripts" })
            DropdownMenuItem(text = { Text("Browser debugging") }, colors = itemColors, enabled = actions != null,
                onClick = { expanded = false; dialog = "Browser debugging" })
        }
    }
    dialog?.let { title ->
        AlertDialog(onDismissRequest = { dialog = null }, title = { Text(title) }, text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                when (title) {
                    "Run configurations" -> {
                        Text("Save terminal commands for this project.")
                        configurations.forEach { configuration ->
                            Row {
                                TextButton(modifier = Modifier.weight(1f), onClick = {
                                    dialog = null
                                    actions?.command(configuration.command)
                                }, colors = readableTextButtonColors()) { Text("Run ${configuration.name}") }
                                TextButton(onClick = {
                                    configurations = configurations.filterNot { it.name == configuration.name }
                                    configurationStore.save(configurations)
                                }, colors = readableTextButtonColors()) { Text("Delete") }
                            }
                        }
                        OutlinedTextField(value = configurationName, onValueChange = { configurationName = it },
                            label = { Text("Name") }, singleLine = true, colors = readableFieldColors())
                        OutlinedTextField(value = configurationCommand, onValueChange = { configurationCommand = it },
                            label = { Text("Command, e.g. npm test") }, singleLine = true, colors = readableFieldColors())
                        TextButton(enabled = configurationName.isNotBlank() && configurationCommand.isNotBlank() && configurations.size < 40,
                            onClick = {
                                val configuration = WebRunConfiguration(configurationName.trim(), configurationCommand.trim())
                                configurations = configurations.filterNot { it.name == configuration.name } + configuration
                                configurationStore.save(configurations)
                                configurationName = ""
                                configurationCommand = ""
                            }, colors = readableTextButtonColors()) { Text("Save configuration") }
                    }
                    "Local history" -> {
                        if (busy) Text("Loading saved versions...")
                        error?.let { Text(it) }
                        if (!busy && revisions.isEmpty()) Text("No earlier saved versions yet.")
                        revisions.forEach { revision ->
                            TextButton(enabled = !busy, onClick = {
                                busy = true
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        actions!!.fileStore.readRevision(file.filePath, revision.id)
                                    }
                                    result.onSuccess { onRestore(it); dialog = null }
                                        .onFailure { error = it.message }
                                    busy = false
                                }
                            }, colors = readableTextButtonColors()) { Text("Restore " + java.text.DateFormat.getDateTimeInstance().format(java.util.Date(revision.savedAt)) + " (${revision.sizeBytes} bytes)") }
                        }
                    }
                    "Problems" -> {
                        if (problems.isEmpty()) Text("No problems reported by the language service.")
                        problems.forEach { Text("${it.severity} · ${it.line}:${it.column} — ${it.message}", modifier = Modifier.padding(vertical = 6.dp)) }
                    }
                    "Browser debugging" -> Text("Preview console messages and JavaScript errors appear in the terminal. For breakpoints, stepping, variables, and network inspection, connect this Android device to desktop Chrome and open chrome://inspect. Choose the in-app preview after enabling debugging below.")
                    else -> {
                        Text("Run the project's package.json scripts. Pending editor changes are saved before execution; results appear in the terminal.")
                        val scripts = actions?.scripts().orEmpty()
                        if (scripts.isEmpty()) Text("No package.json scripts found. Add a test script to your project to enable its test runner.")
                        scripts.sorted().forEach { script ->
                            TextButton(onClick = {
                                dialog = null
                                actions?.command("npm run " + "'" + script.replace("'", "'\\''") + "'")
                            }, colors = readableTextButtonColors()) { Text(if (script == "test") "Run tests" else "Run $script") }
                        }
                    }
                }
            }
        }, confirmButton = {
            if (title == "Browser debugging") TextButton(onClick = { dialog = null; actions?.preview() }, colors = readableTextButtonColors()) { Text("Enable and preview") }
            else TextButton(onClick = { dialog = null }, colors = readableTextButtonColors()) { Text("Close") }
        })
    }
}
