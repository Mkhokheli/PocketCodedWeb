package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private enum class WebSearchScope(val label: String) {
    PROJECT("Project"), FOLDER("Selected folder"), FILE("Active file"),
}

@Composable
internal fun WebProjectSearchReplace(
    rootDirectory: File,
    selectedDirectory: File = rootDirectory,
    activeFile: File? = null,
    palette: WebPalette,
    modifier: Modifier = Modifier,
) {
    val actions = LocalWebDevelopmentActions.current ?: return
    var truncated by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    var replacing by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var fileFilter by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var wholeWord by remember { mutableStateOf(false) }
    var regex by remember { mutableStateOf(false) }
    var searchScope by remember { mutableStateOf(WebSearchScope.PROJECT) }
    var scopeMenuOpen by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf<List<WebSearchMatch>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var confirmReplace by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val options = remember(query, replacement, fileFilter, matchCase, wholeWord, regex) {
        WebSearchOptions(query, replacement, matchCase, wholeWord, regex, fileFilter)
    }
    val searchRoot = when (searchScope) {
        WebSearchScope.PROJECT -> rootDirectory
        WebSearchScope.FOLDER -> selectedDirectory
        WebSearchScope.FILE -> activeFile
    }
    val scopeDescription = when (searchScope) {
        WebSearchScope.PROJECT -> rootDirectory.name
        WebSearchScope.FOLDER -> selectedDirectory.name
        WebSearchScope.FILE -> activeFile?.name ?: "No active file"
    }

    LaunchedEffect(query, fileFilter, matchCase, wholeWord, regex, searchScope, searchRoot, refresh) {
        matches = emptyList()
        searching = query.isNotEmpty()
        status = null
        truncated = false
        if (query.isNotEmpty() && searchRoot != null) {
            try {
                delay(250)
                actions.prepareSearch()
                val found = findWebMatches(rootDirectory, searchRoot, options)
                matches = found.matches
                truncated = found.truncated
                status = if (found.matches.isEmpty()) "No matches found" else
                    "${found.matches.size} matches${if (found.truncated) " (limit reached; narrow your search)" else " found"}"
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) { status = error.message ?: "Search failed" }
            finally { searching = false }
        } else searching = false
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                TextButton(onClick = { scopeMenuOpen = true }, colors = readableTextButtonColors()) {
                    Text("Scope: $scopeDescription", color = palette.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(
                    expanded = scopeMenuOpen,
                    onDismissRequest = { scopeMenuOpen = false },
                ) {
                    WebSearchScope.values().forEach { option ->
                        DropdownMenuItem(colors = readableMenuItemColors(),
                            text = { Text(option.label) },
                            enabled = option != WebSearchScope.FILE || activeFile != null,
                            onClick = {
                                searchScope = option
                                scopeMenuOpen = false
                            },
                        )
                    }
                }
            }
            Text(
                text = "${matches.size} matches",
                color = palette.mutedText,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
            )
        }
        ProjectSearchField(query, "Search in selected scope…", palette) { query = it }
        ProjectSearchField(replacement, "Replace with…", palette) { replacement = it }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SearchToggle("Aa", matchCase, palette) { matchCase = !matchCase }
            SearchToggle("Word", wholeWord, palette) { wholeWord = !wholeWord }
            SearchToggle(".*", regex, palette) { regex = !regex }
            OutlinedTextField(
                value = fileFilter,
                onValueChange = { fileFilter = it },
                placeholder = { Text("*.js, *.html", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).heightIn(min = 36.dp),
                colors = readableFieldColors(),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { refresh++ }, enabled = !searching && !replacing && query.isNotEmpty(), colors = readableTextButtonColors()) { Text("Refresh", color = palette.accent) }
            if (searching) {
                CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp, color = palette.accent)
                Spacer(Modifier.width(6.dp))
            }
            Text(status ?: "Search the selected scope", color = palette.subtleText, fontSize = 11.sp, modifier = Modifier.weight(1f))
            if (matches.isNotEmpty() && !searching && !replacing) {
                Button(
                    onClick = { confirmReplace = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    colors = readableButtonColors(),
                ) { Text("Replace all", fontSize = 11.sp) }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            items(matches) { match ->
                Column(
                    Modifier.fillMaxWidth().clipSearchRow(palette).clickable { actions.source(match.file.path, match.line) }.padding(8.dp)
                ) {
                    Text("${match.relativePath}:${match.line}", color = palette.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(match.preview, color = palette.text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    if (replacing) {
        AlertDialog(onDismissRequest = {}, title = { Text("Replacing matches") },
            text = { CircularProgressIndicator() }, confirmButton = {})
    }
    if (confirmReplace) {
        val selectedMatches = matches.toList()
        val selectedOptions = options
        AlertDialog(
            onDismissRequest = { confirmReplace = false },
            title = { Text("Replace matches?") },
            text = { Text("Replace ${selectedMatches.size} displayed matches in $scopeDescription? Previous file contents will be kept in Local history.${if (truncated) " Only displayed matches will be replaced." else ""}") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReplace = false
                    replacing = true
                    coroutineScope.launch {
                        try {
                            actions.prepareSearch()
                            val result = replaceWebMatches(selectedMatches, selectedOptions, actions.fileStore)
                            actions.filesChanged(result.files)
                            matches = emptyList()
                            status = "Replaced ${result.count} matches" +
                                result.errors.takeIf { it.isNotEmpty() }?.joinToString("\n", prefix = "\n").orEmpty()
                        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                        catch (error: Exception) { status = error.message ?: "Replacement failed" }
                        finally { replacing = false }
                    }
                }, colors = readableTextButtonColors()) { Text("Replace all") }
            },
            dismissButton = { TextButton(onClick = { confirmReplace = false }, colors = readableTextButtonColors()) { Text("Cancel") } },
        )
    }

}

@Composable
private fun ProjectSearchField(value: String, placeholder: String, palette: WebPalette, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 12.sp, color = palette.subtleText) },
        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = palette.subtleText) },
        trailingIcon = if (value.isNotEmpty()) ({ IconButton(onClick = { onValueChange("") }) { Icon(Icons.Rounded.Clear, "Clear") } }) else null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
        colors = readableFieldColors(),
    )
}

@Composable
private fun SearchToggle(label: String, selected: Boolean, palette: WebPalette, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) palette.text else palette.mutedText,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clipSearchRow(palette, selected).clickable(onClick = onClick).padding(horizontal = 9.dp, vertical = 8.dp),
    )
}

private fun Modifier.clipSearchRow(palette: WebPalette, selected: Boolean = false): Modifier =
    clip(RoundedCornerShape(8.dp))
        .background(if (selected) palette.selectedTab else palette.tab)
        .border(1.dp, if (selected) palette.accent else palette.border, RoundedCornerShape(8.dp))
