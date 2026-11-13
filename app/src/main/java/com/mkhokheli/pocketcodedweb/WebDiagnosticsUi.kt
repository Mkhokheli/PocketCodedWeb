package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun severityRank(severity: String) = when (severity.lowercase()) { "error" -> 0; "warning" -> 1; else -> 2 }
private fun severityColor(severity: String, palette: WebPalette) = when (severityRank(severity)) {
    0 -> palette.error; 1 -> palette.warning; else -> palette.info
}

@Composable
internal fun WebInspectionStrip(
    diagnostics: List<CodeDiagnostic>, palette: WebPalette, modifier: Modifier = Modifier,
    onOpenProject: () -> Unit, onNavigate: (CodeDiagnostic) -> Unit,
    onQuickFix: (CodeDiagnostic) -> Unit, onExplain: ((CodeDiagnostic) -> Unit)? = null,
) {
    var maximumSeverity by rememberSaveable { mutableIntStateOf(2) }
    val visible = remember(diagnostics, maximumSeverity) { diagnostics.filter { severityRank(it.severity) <= maximumSeverity }
        .sortedWith(compareBy<CodeDiagnostic> { severityRank(it.severity) }.thenBy { it.line }.thenBy { it.start }) }
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(palette.elevatedPanel)
        .border(1.dp, palette.border.copy(alpha = 0.78f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 7.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Inspections", color = palette.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onOpenProject))
            TextButton(onClick = { maximumSeverity = (maximumSeverity + 2) % 3 },
                colors = readableTextButtonColors(), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(24.dp)) {
                Text(when (maximumSeverity) { 0 -> "Errors"; 1 -> "Warnings"; else -> "All" }, fontSize = 11.sp)
            }
        }
        Column(Modifier.heightIn(max = 66.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            visible.forEach { diagnostic ->
                BoxWithConstraints(Modifier.fillMaxWidth().heightIn(min = 32.dp)
                    .combinedClickable(onClick = { onNavigate(diagnostic) }, onDoubleClick = onOpenProject)) {
                    val fixLabel = if (maxWidth < 300.dp) "Fix" else "Quick fix"
                    Row(Modifier.fillMaxWidth().heightIn(min = 30.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(severityColor(diagnostic.severity, palette)))
                        Spacer(Modifier.width(9.dp))
                        Text("L${diagnostic.line}: ${diagnostic.message}", color = palette.text,
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 14.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        diagnostic.quickFix?.let { fix ->
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.height(28.dp).clip(RoundedCornerShape(8.dp))
                                .border(1.dp, palette.accent, RoundedCornerShape(8.dp))
                                .clickable(role = Role.Button) { onQuickFix(diagnostic) }
                                .semantics { contentDescription = "Quick fix: ${fix.title}" }
                                .padding(horizontal = 9.dp), contentAlignment = Alignment.Center) {
                                Text(fixLabel, color = palette.text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            if (visible.isEmpty()) Text("No diagnostics at this severity", color = palette.mutedText, fontSize = 11.sp)
        }
    }
}

@Composable
internal fun WebProjectDiagnosticsDialog(
    activeFile: WebFile, activeSource: String, palette: WebPalette, actions: WebDevelopmentActions?,
    onDismiss: () -> Unit, onRestoreActive: (String) -> Unit,
) {
    var entries by remember(activeFile.filePath) { mutableStateOf<List<WebFileDiagnostic>>(emptyList()) }
    var scanning by remember { mutableStateOf(true) }
    var truncated by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(activeFile.filePath, activeSource) {
        scanning = true
        try {
            actions?.prepareSearch?.invoke()
            val result = scanWebProjectDiagnostics(actions?.fileStore?.projectDirectory, File(activeFile.filePath), activeSource)
            entries = result.entries; truncated = result.truncated
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { error = failure.message ?: "Could not inspect the project." }
        finally { scanning = false }
    }
    fun apply(entry: WebFileDiagnostic) {
        val fix = entry.diagnostic.quickFix ?: return
        if (entry.file.path == File(activeFile.filePath).canonicalPath) {
            applyWebQuickFix(activeSource, fix).onSuccess { onRestoreActive(it); onDismiss() }.onFailure { error = it.message }
            return
        }
        val owner = actions ?: return
        busy = true
        scope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    val root = owner.fileStore.projectDirectory.canonicalPath + File.separator
                    require(entry.file.canonicalPath.startsWith(root)) { "File is outside this project." }
                    require(entry.file.length() <= 1_000_000) { "File is too large to fix." }
                    val source = entry.file.readText()
                    val updated = applyWebQuickFix(source, fix).getOrThrow()
                    owner.fileStore.save(owner.fileStore.prepareEdit(entry.file.path, updated, expectedContent = source)).getOrThrow()
                    updated
                }
                owner.fileEdited(entry.file, content)
                onDismiss()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { error = failure.message ?: "Could not apply this fix." }
            finally { busy = false }
        }
    }
    Dialog(onDismissRequest = { if (!busy) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxWidth(0.94f).widthIn(max = 560.dp).heightIn(max = 700.dp)
            .clip(RoundedCornerShape(16.dp)).background(palette.panel)
            .border(1.dp, palette.strongBorder, RoundedCornerShape(16.dp))) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Project errors and warnings", color = palette.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${entries.count { severityRank(it.diagnostic.severity) == 0 }} errors · ${entries.count { severityRank(it.diagnostic.severity) == 1 }} warnings",
                        color = palette.subtleText, fontSize = 12.sp)
                }
                IconButton(onClick = onDismiss, enabled = !busy) { Icon(Icons.Rounded.Close, "Close project diagnostics", tint = palette.text) }
            }
            HorizontalDivider(color = palette.divider)
            if (scanning || busy) Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = palette.accent) }
            else if (entries.isEmpty()) Text("No errors or warnings found", modifier = Modifier.padding(24.dp), color = palette.text)
            else LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 520.dp).padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { "${it.relativePath}:${it.diagnostic.start}:${it.diagnostic.message}" }) { entry ->
                    Column(Modifier.testTag("diagnostic:${entry.relativePath}:${entry.diagnostic.start}")
                        .fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(palette.elevatedPanel)
                        .border(1.dp, palette.border, RoundedCornerShape(10.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${entry.relativePath}:${entry.diagnostic.line}:${entry.diagnostic.column}", color = palette.accent,
                            fontSize = 12.sp, modifier = Modifier.clickable(enabled = actions != null) {
                                onDismiss(); actions?.source?.invoke(entry.file.path, entry.diagnostic.line)
                            })
                        Text(entry.diagnostic.message, color = palette.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        if (entry.preview.isNotEmpty()) Text(entry.preview, color = palette.mutedText, fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().background(palette.editorBackground).padding(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            entry.diagnostic.quickFix?.let { fix ->
                                Button(onClick = { apply(entry) }, modifier = Modifier.weight(1f).heightIn(min = 42.dp),
                                    shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    colors = readableButtonColors()) {
                                    Text("Quick fix: ${fix.title}", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            OutlinedButton(onClick = {
                                onDismiss(); actions?.explainDiagnostic?.invoke(entry.file.path, entry.diagnostic)
                            }, enabled = actions?.canExplainDiagnostic == true,
                                modifier = Modifier.weight(1f).heightIn(min = 42.dp), shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, palette.accent), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = readableOutlinedButtonColors()) { Text("Explain with AI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }
            error?.let { Text(it, color = palette.error, modifier = Modifier.padding(12.dp)) }
            if (truncated) Text("Inspection limit reached. Open a file to prioritise it.", color = palette.mutedText, fontSize = 11.sp, modifier = Modifier.padding(12.dp))
            HorizontalDivider(color = palette.divider)
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, enabled = !busy, colors = readableTextButtonColors()) { Text("Done") }
            }
        }
    }
}
