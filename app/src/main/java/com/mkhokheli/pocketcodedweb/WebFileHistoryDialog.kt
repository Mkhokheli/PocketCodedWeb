package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Preview first; restoration enters the editor's undo history through [onRestore]. */
@Composable
internal fun WebFileHistoryDialog(
    filePath: String,
    actions: WebDevelopmentActions,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit,
) {
    val colors = ideToolColors()
    var revisions by remember(filePath) { mutableStateOf<List<FileRevision>>(emptyList()) }
    var selected by remember(filePath) { mutableStateOf<FileRevision?>(null) }
    var preview by remember(filePath) { mutableStateOf<String?>(null) }
    var loading by remember(filePath) { mutableStateOf(true) }
    var error by remember(filePath) { mutableStateOf<String?>(null) }

    LaunchedEffect(actions.fileStore, filePath) {
        try {
            // Use the owner's flush so pending jobs and editor state stay in sync.
            actions.prepareSearch()
            revisions = withContext(Dispatchers.IO) { actions.fileStore.history(filePath) }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { error = failure.message ?: "Could not load local history." }
        finally { loading = false }
    }
    LaunchedEffect(selected) {
        preview = null
        val revision = selected ?: return@LaunchedEffect
        error = null
        withContext(Dispatchers.IO) { actions.fileStore.readRevision(filePath, revision.id) }
            .onSuccess { preview = it }
            .onFailure { error = it.message ?: "Could not read this version." }
    }

    IdeToolDialog(
        onDismissRequest = onDismiss,
        title = { Text("Local history · ${File(filePath).name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Restoring keeps the current saved file in history. History is stored on this device.",
                    color = colors.mutedText, style = MaterialTheme.typography.bodySmall)
                if (loading) CircularProgressIndicator(color = colors.accent)
                else if (revisions.isEmpty() && error == null) Text("No earlier versions yet. Previous contents are kept whenever this file is saved.", color = colors.mutedText)
                LazyColumn(Modifier.heightIn(max = 180.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(revisions, key = { it.id }) { revision ->
                        val isSelected = revision.id == selected?.id
                        Row(Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.selected else colors.panel)
                            .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(8.dp))
                            .clickable { if (!isSelected) { preview = null; selected = revision } }
                            .padding(4.dp)) {
                            RadioButton(selected = isSelected, onClick = { if (!isSelected) { preview = null; selected = revision } },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accent, unselectedColor = colors.mutedText))
                            Column(Modifier.padding(top = 5.dp)) {
                                Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Date(revision.savedAt)), style = MaterialTheme.typography.bodyMedium)
                                Text("${revision.sizeBytes} bytes", style = MaterialTheme.typography.bodySmall, color = colors.mutedText)
                            }
                        }
                    }
                }
                if (selected != null) HorizontalDivider(color = colors.border)
                if (selected != null && preview == null && error == null) Text("Loading preview…", color = colors.mutedText)
                preview?.let { content ->
                    SelectionContainer {
                        Text(content.take(12_000).ifEmpty { "(Empty file)" },
                            modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(8.dp)).background(colors.codeBackground)
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                .verticalScroll(rememberScrollState()).padding(12.dp),
                            color = colors.text, fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (content.length > 12_000) Text("Preview shortened. Restoring uses the complete file.",
                        color = colors.mutedText, style = MaterialTheme.typography.bodySmall)
                }
                error?.let { Text(it, color = colors.error) }
            }
        },
        confirmButton = {
            IdeToolPrimaryButton(enabled = preview != null && !loading && error == null, onClick = {
                preview?.let(onRestore)
                onDismiss()
            }) { Text("Restore version") }
        },
        dismissButton = { IdeToolTextButton(onClick = onDismiss) { Text("Close") } },
    )
}
