package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.repeatOnLifecycle
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun WebDevelopmentPanels(controller: WebDevelopmentController) {
    val actions = LocalWebDevelopmentActions.current ?: return
    val scope = rememberCoroutineScope()
    val lifecycle = (LocalContext.current as androidx.lifecycle.LifecycleOwner).lifecycle
    var drafts by remember { mutableStateOf<List<RecoveredDraft>>(emptyList()) }
    var testFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var preview by remember { mutableStateOf<Pair<RecoveredDraft, String>?>(null) }
    var discard by remember { mutableStateOf<RecoveredDraft?>(null) }
    var busy by remember { mutableStateOf(false) }
    var expandedTest by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(controller) {
        drafts = withContext(Dispatchers.IO) { actions.fileStore.recoveryDrafts() }
        if (drafts.isNotEmpty()) controller.panel = "Recovery"
    }
    LaunchedEffect(controller.panel) {
        if (controller.panel == "Recovery") drafts = withContext(Dispatchers.IO) { actions.fileStore.recoveryDrafts() }
        if (controller.panel == "Tests") testFiles = withContext(Dispatchers.IO) { discoverWebTestFiles(controller.root) }
    }
    LaunchedEffect(controller, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                if (controller.panel in setOf("Debugger", "Tests", "Profiler")) {
                    val snapshots = withContext(Dispatchers.IO) {
                        Triple(NodeSessionRuntime.developmentState(controller.root, "debug"), NodeSessionRuntime.developmentState(controller.root, "tests"), NodeSessionRuntime.developmentState(controller.root, "profile"))
                    }
                    controller.debugState = snapshots.first
                    controller.testState = snapshots.second
                    controller.profileState = snapshots.third
                }
                delay(if (controller.panel == null) 1000 else 250)
            }
        }
    }
    fun command(name: String, frame: Int = 0) {
        scope.launch {
            val result = withContext(Dispatchers.IO) { NodeSessionRuntime.debugCommand(controller.root, name, frame, controller.breakpointList()) }
            controller.error = result.exceptionOrNull()?.message
        }
    }
    fun stop() {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                if (NodeSessionRuntime.isRunningFor(controller.root)) NodeSessionRuntime.stop() else WebShellResult()
            }
            controller.error = result.error.takeIf { it.isNotBlank() }
        }
    }
    fun recover(draft: RecoveredDraft) {
        busy = true
        actions.recover(draft.filePath) { error ->
            controller.error = error
            scope.launch {
                drafts = withContext(Dispatchers.IO) { actions.fileStore.recoveryDrafts() }
                preview = null; busy = false
            }
        }
    }
    controller.panel?.let { panel ->
        val debugState = controller.debugState.optString("state", "idle")
        val testState = controller.testState.optString("state", "idle")
        val running = when (panel) { "Debugger" -> debugState in setOf("running", "paused", "starting"); "Tests" -> testState in setOf("running", "starting"); else -> false }
        AlertDialog(onDismissRequest = { if (!busy) controller.panel = null },
            title = { Text(if (panel == "Debugger") "Debugger - $debugState" else if (panel == "Tests") "Tests - $testState" else if (panel == "Profiler") "JavaScript profiler" else "Recovery drafts") },
            text = {
                Column(Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                    controller.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    when (panel) {
                        "Debugger" -> {
                            controller.debugState.optString("error").takeIf { it.isNotBlank() }?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            if (debugState == "paused") {
                                val stack = controller.debugState.optJSONArray("stack")
                                val selected = controller.debugState.optInt("selectedFrame")
                                val frame = stack?.optJSONObject(selected)
                                Text("${File(frame?.optString("file").orEmpty()).name}:${frame?.optInt("line", 1)}")
                                Row {
                                    TextButton(onClick = { command("into") }, colors = readableTextButtonColors()) { Text("Into") }
                                    TextButton(onClick = { command("over") }, colors = readableTextButtonColors()) { Text("Over") }
                                    TextButton(onClick = { command("out") }, colors = readableTextButtonColors()) { Text("Out") }
                                }
                                LazyColumn(Modifier.weight(1f, false)) {
                                    item { Text("Variables", style = MaterialTheme.typography.titleSmall) }
                                    val variables = controller.debugState.optJSONArray("variables")
                                    items(variables?.length() ?: 0) { index ->
                                        val variable = variables!!.getJSONObject(index)
                                        SelectionContainer { Text("${variable.optString("name")} = ${variable.optString("value")}\n${variable.optString("scope")} / ${variable.optString("type")}", fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 4.dp)) }
                                    }
                                    item { Text("Call stack", style = MaterialTheme.typography.titleSmall) }
                                    items(stack?.length() ?: 0) { index ->
                                        val item = stack!!.getJSONObject(index)
                                        Row {
                                            TextButton(modifier = Modifier.weight(1f), onClick = { command("inspect", index) }, colors = readableTextButtonColors()) {
                                                Text("${if (index == selected) "> " else ""}${item.optString("function")}\n${File(item.optString("file")).name}:${item.optInt("line")}")
                                            }
                                            TextButton(onClick = { actions.source(item.optString("file"), item.optInt("line", 1)) }, colors = readableTextButtonColors()) { Text("Source") }
                                        }
                                    }
                                }
                            } else {
                                Text(if (debugState == "running") "Running until the next breakpoint. Pause to inspect execution." else "Open a JavaScript file and choose Debug current file. Tap line numbers to toggle breakpoints. The debugger pauses at entry and uncaught exceptions.")
                            }
                            Text("Breakpoints", style = MaterialTheme.typography.titleSmall)
                            LazyColumn(Modifier.heightIn(max = 100.dp)) {
                                items(controller.breakpointList()) { (file, line) ->
                                    Row {
                                        TextButton(modifier = Modifier.weight(1f), onClick = { actions.source(file, line) }, colors = readableTextButtonColors()) { Text("${File(file).name}:$line") }
                                        TextButton(onClick = { controller.toggleBreakpoint(file, line); if (running) command("breakpoints") }, colors = readableTextButtonColors()) { Text("Remove") }
                                    }
                                }
                            }
                        }
                        "Tests" -> {
                            val results = parseWebTestResults(controller.testState)
                            val passed = results.count { it.status == "passed" }
                            val failed = results.count { it.status == "failed" }
                            val skipped = results.count { it.status == "skipped" }
                            Text("$passed passed / $failed failed / $skipped skipped")
                            Text("Runs JavaScript node:test files in this project.")
                            Row {
                                TextButton(enabled = !running && !controller.starting && testFiles.isNotEmpty(), onClick = { actions.startDevelopment("tests", testFiles.first().path, testFiles.map { it.path }) }, colors = readableTextButtonColors()) { Text("Run all") }
                                val failedFiles = results.filter { it.status == "failed" }.map { it.file }.filter { it.isNotBlank() }.distinct()
                                TextButton(enabled = !running && !controller.starting && failedFiles.isNotEmpty(), onClick = { actions.startDevelopment("tests", failedFiles.first(), failedFiles) }, colors = readableTextButtonColors()) { Text("Rerun failed") }
                            }
                            if (controller.testState.optBoolean("truncated")) Text("Showing the first 1,000 test results.")
                            LazyColumn(Modifier.weight(1f, false)) {
                                if (results.isEmpty()) {
                                    item { Text(if (testFiles.isEmpty()) "No *.test.js, *.spec.js, .cjs/.mjs equivalents or files under test/tests/__tests__ found." else "${testFiles.size} test files found.") }
                                    items(testFiles) { file ->
                                        TextButton(enabled = !running && !controller.starting, onClick = { actions.startDevelopment("tests", file.path, listOf(file.path)) }, colors = readableTextButtonColors()) { Text("Run ${file.relativeTo(controller.root).path}") }
                                    }
                                }
                                items(results, key = { it.id }) { result ->
                                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        TextButton(onClick = { expandedTest = if (expandedTest == result.id) null else result.id }, colors = readableTextButtonColors()) {
                                            Text("${result.status.uppercase()}: ${result.name}", color = if (result.status == "failed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                                        }
                                        if (expandedTest == result.id) {
                                            if (result.details.isNotBlank()) SelectionContainer { Text(result.details, fontFamily = FontFamily.Monospace) }
                                            if (result.file.isNotBlank()) Row {
                                                TextButton(onClick = { actions.source(result.file, result.line) }, colors = readableTextButtonColors()) { Text("Show source") }
                                                TextButton(enabled = !running && !controller.starting, onClick = { actions.startDevelopment("tests", result.file, listOf(result.file)) }, colors = readableTextButtonColors()) { Text("Rerun file") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "Profiler" -> {
                            val report = controller.profileState
                            val state = report.optString("state", "idle")
                            Text(when (state) {
                                "running", "starting" -> "Recording CPU samples. Finite scripts finish automatically; servers record for five seconds."
                                "complete" -> "${report.optInt("samples")} samples / ${report.optLong("durationMs")} ms / ${report.optLong("heapUsed") / 1024} KiB heap at completion"
                                else -> report.optString("error").ifBlank { "Choose Profile current file from a JavaScript editor's tools menu." }
                            })
                            report.optString("reportPath").takeIf { it.isNotBlank() }?.let { path ->
                                Text("Saved ${File(path).relativeToOrSelf(controller.root).path}. Open the .cpuprofile in a compatible CPU profile viewer.")
                            }
                            val functions = report.optJSONArray("functions")
                            LazyColumn(Modifier.weight(1f, false)) {
                                items(functions?.length() ?: 0) { index ->
                                    val function = functions!!.getJSONObject(index)
                                    val file = function.optString("file")
                                    Column(Modifier.padding(vertical = 4.dp)) {
                                        Text("${function.optString("name")} - ${function.optDouble("selfMs").toInt()} ms self", fontFamily = FontFamily.Monospace)
                                        if (file.isNotBlank()) TextButton(onClick = { actions.source(file, function.optInt("line", 1)) },
                                            enabled = runCatching { File(file).canonicalPath.startsWith(controller.root.canonicalPath + File.separator) }.getOrDefault(false), colors = readableTextButtonColors()) {
                                            Text("${File(file).name}:${function.optInt("line", 1)}")
                                        }
                                    }
                                }
                            }
                        }
                        "Recovery" -> {
                            Text("Preview unsaved copies before restoring or discarding them. Restoring keeps the current disk version in local history. Resolve a draft before saving new edits to its file.")
                            if (drafts.isEmpty()) Text("No recovery drafts found.")
                            LazyColumn(Modifier.weight(1f, false)) {
                                items(drafts, key = { it.filePath }) { draft ->
                                    Column(Modifier.padding(vertical = 6.dp)) {
                                        Text("${draft.relativePath} (${draft.sizeBytes} bytes)")
                                        Row {
                                            TextButton(enabled = !busy, onClick = {
                                                busy = true
                                                scope.launch {
                                                    val result = withContext(Dispatchers.IO) { actions.fileStore.readDraft(draft.filePath) }
                                                    result.onSuccess { preview = draft to it }.onFailure { controller.error = it.message }
                                                    busy = false
                                                }
                                            }, colors = readableTextButtonColors()) { Text("Preview") }
                                            TextButton(enabled = !busy, onClick = { recover(draft) }, colors = readableTextButtonColors()) { Text("Restore") }
                                            TextButton(enabled = !busy, onClick = { discard = draft }, colors = readableTextButtonColors()) { Text("Discard") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (panel == "Debugger" && debugState == "paused") TextButton(onClick = { command("continue") }, colors = readableTextButtonColors()) { Text("Continue") }
                else if (panel == "Debugger" && debugState == "running") TextButton(onClick = { command("pause") }, colors = readableTextButtonColors()) { Text("Pause") }
                else TextButton(enabled = !busy, onClick = { controller.panel = null }, colors = readableTextButtonColors()) { Text("Close") }
            },
            dismissButton = {
                Row {
                    if (running) TextButton(onClick = { stop() }, colors = readableTextButtonColors()) { Text("Stop") }
                    TextButton(enabled = !busy, onClick = { controller.panel = null }, colors = readableTextButtonColors()) { Text(if (panel == "Recovery") "Later" else "Hide") }
                }
            })
    }
    preview?.let { (draft, text) ->
        AlertDialog(onDismissRequest = { if (!busy) preview = null }, title = { Text(draft.relativePath) },
            text = { SelectionContainer { Text(text.take(100_000), fontFamily = FontFamily.Monospace, modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) } },
            confirmButton = { TextButton(enabled = !busy, onClick = { recover(draft) }, colors = readableTextButtonColors()) { Text("Restore") } },
            dismissButton = { TextButton(enabled = !busy, onClick = { preview = null }, colors = readableTextButtonColors()) { Text("Close") } })
    }
    discard?.let { draft ->
        AlertDialog(onDismissRequest = { discard = null }, title = { Text("Discard recovery copy?") },
            text = { Text("Delete the unsaved recovery copy of ${draft.relativePath}? The current project file is kept.") },
            confirmButton = { TextButton(enabled = !busy, onClick = {
                busy = true
                actions.discardRecovery(draft.filePath) { error ->
                    controller.error = error
                    scope.launch {
                        drafts = withContext(Dispatchers.IO) { actions.fileStore.recoveryDrafts() }
                        discard = null; busy = false
                    }
                }
            }, colors = readableTextButtonColors()) { Text("Discard") } }, dismissButton = { TextButton(onClick = { discard = null }, colors = readableTextButtonColors()) { Text("Cancel") } })
    }
}
