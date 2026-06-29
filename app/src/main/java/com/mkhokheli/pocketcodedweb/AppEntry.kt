package com.mkhokheli.pocketcodedweb

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal data class LocalProject(
    val name: String,
    val directory: File,
    val modifiedAt: Long,
    val canDelete: Boolean,
)

private data class EntryPalette(
    val background: Color,
    val panel: Color,
    val elevatedPanel: Color,
    val text: Color,
    val mutedText: Color,
    val border: Color,
    val accent: Color,
    val accentAlt: Color,
    val error: Color,
)

@Composable
internal fun PocketCodedWebEntry(
    darkMode: Boolean,
    currentThemeName: String,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    ideContent: @Composable (projectDirectory: File, onBackToProjects: () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val palette = remember(currentThemeName, darkMode) { entryPalette(currentThemeName, darkMode) }
    var selectedProjectPath by rememberSaveable { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedProjectPath == null) {
            ProjectHomeScreen(
                darkMode = darkMode,
                currentThemeName = currentThemeName,
                nextThemeName = nextThemeName,
                onToggleTheme = onToggleTheme,
                onOpenProject = { project -> selectedProjectPath = project.directory.absolutePath },
            )
        } else {
            val projectDirectory = File(selectedProjectPath.orEmpty()).apply { mkdirs() }
            key(projectDirectory.absolutePath) {
                ideContent(projectDirectory) { selectedProjectPath = null }
            }
        }
    }
}

@Composable
private fun ProjectHomeScreen(
    darkMode: Boolean,
    currentThemeName: String,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    onOpenProject: (LocalProject) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val palette = remember(currentThemeName, darkMode) { entryPalette(currentThemeName, darkMode) }
    var projects by remember { mutableStateOf<List<LocalProject>>(emptyList()) }
    var refreshVersion by remember { mutableLongStateOf(0L) }
    var search by rememberSaveable { mutableStateOf("") }
    var createDialogOpen by remember { mutableStateOf(false) }
    var deleteProject by remember { mutableStateOf<LocalProject?>(null) }
    var renameProject by remember { mutableStateOf<LocalProject?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshVersion) {
        projects = withContext(Dispatchers.IO) { discoverLocalProjects(context) }
    }

    val visibleProjects = remember(projects, search) {
        val query = search.trim()
        if (query.isBlank()) projects else projects.filter { it.name.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BrandTitle(palette, fontSize = 27)
                Text("Select a project to get started", color = palette.mutedText, fontSize = 14.sp)
            }
            ThemeCycleButton(
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                buttonColor = palette.panel,
                borderColor = palette.border,
                contentColor = palette.text,
                onClick = onToggleTheme,
            )
        }
        Spacer(Modifier.height(24.dp))

        Surface(
            color = palette.panel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { createDialogOpen = true },
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("New Project", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Create a new Web project", color = palette.mutedText, fontSize = 13.sp)
                }
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Create project",
                    tint = palette.accent,
                )
            }
        }
        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Existing Projects",
                color = palette.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.mutedText)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search projects") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        message?.let {
            Text(it, color = palette.error, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(12.dp))

        if (visibleProjects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.Folder, contentDescription = null, tint = palette.mutedText, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (search.isBlank()) "No projects yet" else "No matching projects",
                    color = palette.text,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (search.isBlank()) "Create your first project to start coding." else "Try another search.",
                    color = palette.mutedText,
                    fontSize = 13.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visibleProjects, key = { it.directory.absolutePath }) { project ->
                    ProjectRow(
                        project = project,
                        palette = palette,
                        onOpen = { onOpenProject(project) },
                        onRename = { renameProject = project },
                        onDelete = { deleteProject = project },
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }

    if (createDialogOpen) {
        CreateProjectDialog(
            palette = palette,
            onDismiss = { createDialogOpen = false },
            onCreate = { name ->
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) { createLocalProject(context, name) }
                    result.onSuccess { project ->
                        createDialogOpen = false
                        refreshVersion += 1L
                        onOpenProject(project)
                    }.onFailure { error ->
                        message = error.message ?: "Could not create the project."
                    }
                }
            },
        )
    }

    renameProject?.let { project ->
        RenameProjectDialog(
            project = project,
            palette = palette,
            onDismiss = { renameProject = null },
            onRename = { name ->
                renameLocalProject(context, project, name)
                    .fold(
                        onSuccess = {
                            renameProject = null
                            message = null
                            refreshVersion += 1L
                            null
                        },
                        onFailure = { error -> error.message ?: "Could not rename the project." },
                    )
            },
        )
    }

    deleteProject?.let { project ->
        AlertDialog(
            onDismissRequest = { deleteProject = null },
            containerColor = palette.elevatedPanel,
            title = { Text("Delete ${project.name}?", color = palette.text) },
            text = { Text("This permanently removes the project and all of its files.", color = palette.mutedText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) { project.directory.deleteRecursively() }
                            deleteProject = null
                            refreshVersion += 1L
                        }
                    }
                ) {
                    Text("Delete", color = palette.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteProject = null }) { Text("Cancel", color = palette.text) }
            },
        )
    }
}

@Composable
private fun ProjectRow(
    project: LocalProject,
    palette: EntryPalette,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        color = palette.panel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.accentAlt.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Code, contentDescription = null, tint = palette.accentAlt)
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    color = palette.text,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (project.canDelete) {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Project options", tint = palette.mutedText)
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        containerColor = palette.elevatedPanel,
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open", color = palette.text) },
                            onClick = { menuOpen = false; onOpen() },
                            leadingIcon = { Icon(Icons.Rounded.Folder, null, tint = palette.accent) },
                        )
                        DropdownMenuItem(
                            text = { Text("Rename", color = palette.text) },
                            onClick = { menuOpen = false; onRename() },
                            leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = palette.accent) },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = palette.error) },
                            onClick = { menuOpen = false; onDelete() },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = palette.error) },
                        )
                    }
                }
            } else {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Open project", tint = palette.accent)
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun RenameProjectDialog(
    project: LocalProject,
    palette: EntryPalette,
    onDismiss: () -> Unit,
    onRename: (String) -> String?,
) {
    var name by rememberSaveable(project.directory.absolutePath) { mutableStateOf(project.name) }
    var error by remember(project.directory.absolutePath) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.elevatedPanel,
        title = { Text("Rename Project", color = palette.text) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Project name") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val validation = validateProjectName(name)
                            error = validation ?: onRename(name)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(it, color = palette.error, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = validateProjectName(name)
                    error = validation ?: onRename(name)
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = palette.text) }
        },
    )
}

@Composable
private fun CreateProjectDialog(
    palette: EntryPalette,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.elevatedPanel,
        title = { Text("New Web Project", color = palette.text) },
        text = {
            Column {
                Text("Choose a name for the project folder.", color = palette.mutedText, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Project name") },
                    leadingIcon = { Icon(Icons.Rounded.Folder, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val validation = validateProjectName(name)
                            if (validation == null) onCreate(name) else error = validation
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let { Text(it, color = palette.error, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = validateProjectName(name)
                    if (validation == null) onCreate(name) else error = validation
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = palette.text) }
        },
    )
}

@Composable
private fun BrandTitle(palette: EntryPalette, fontSize: Int = 31) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Pocket", color = palette.text, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
        Text("CodedWeb", color = palette.accent, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
    }
}

private fun entryPalette(themeName: String, darkMode: Boolean): EntryPalette = when (themeName) {
    "CyberpunkElectricDark", "Cyberpunk", "MidnightNeon" -> darkEntryPalette(
        background = Color(0xFF070617),
        panel = Color(0xFF0A0C24),
        elevatedPanel = Color(0xFF11122D),
        border = Color(0xFFFF2D75).copy(alpha = 0.38f),
        accent = Color(0xFFFF2D75),
    )
    "CyberpunkElectricLight" -> lightEntryPalette(
        background = Color(0xFFFFFBF8),
        elevatedPanel = Color(0xFFFFE7F0),
        border = Color(0xFFFFC4DC),
        accent = Color(0xFFFF2D75),
    )
    "ForestNightDark", "ForestDark", "SoftMint" -> darkEntryPalette(
        background = Color(0xFF031611),
        panel = Color(0xFF042018),
        elevatedPanel = Color(0xFF062D21),
        border = Color(0xFF22C55E).copy(alpha = 0.38f),
        accent = Color(0xFF22C55E),
    )
    "ForestNightLight" -> lightEntryPalette(
        background = Color(0xFFF7FFF9),
        elevatedPanel = Color(0xFFECFDF5),
        border = Color(0xFFD1FAE5),
        accent = Color(0xFF22C55E),
    )
    "SunsetDuskDark" -> darkEntryPalette(
        background = Color(0xFF1E0F0B),
        panel = Color(0xFF180F18),
        elevatedPanel = Color(0xFF2A1510),
        border = Color(0xFFFB923C).copy(alpha = 0.38f),
        accent = Color(0xFFFB923C),
    )
    "SunsetDuskLight", "RosePine", "Solarized" -> lightEntryPalette(
        background = Color(0xFFFFFDF9),
        elevatedPanel = Color(0xFFFFF0D0),
        border = Color(0xFFFED7AA),
        accent = Color(0xFFFB923C),
    )
    "ArcticAuroraDark", "OceanicNext", "SereneBlue" -> darkEntryPalette(
        background = Color(0xFF031A24),
        panel = Color(0xFF052536),
        elevatedPanel = Color(0xFF073047),
        border = Color(0xFF06B6D4).copy(alpha = 0.38f),
        accent = Color(0xFF06B6D4),
    )
    "ArcticAuroraLight" -> lightEntryPalette(
        background = Color(0xFFF7FEFF),
        elevatedPanel = Color(0xFFF0FDFF),
        border = Color(0xFFBAE6FD),
        accent = Color(0xFF06B6D4),
    )
    "VioletHazeDark", "Amethyst", "Cloudy" -> darkEntryPalette(
        background = Color(0xFF1A123A),
        panel = Color(0xFF201540),
        elevatedPanel = Color(0xFF281A4F),
        border = Color(0xFFA78BFA).copy(alpha = 0.38f),
        accent = Color(0xFFA78BFA),
    )
    "VioletHazeLight" -> lightEntryPalette(
        background = Color(0xFFFAF5FF),
        elevatedPanel = Color(0xFFEDE9FE),
        border = Color(0xFFDDD6FE),
        accent = Color(0xFFA78BFA),
    )
    "MonochromeProDark", "Nordic" -> darkEntryPalette(
        background = Color(0xFF111827),
        panel = Color(0xFF111827),
        elevatedPanel = Color(0xFF1F2937),
        border = Color(0xFF64748B).copy(alpha = 0.42f),
        accent = Color(0xFF64748B),
    )
    "MonochromeProLight", "Paper" -> lightEntryPalette(
        background = Color(0xFFFFFFFF),
        elevatedPanel = Color(0xFFF3F4F6),
        border = Color(0xFFE5E7EB),
        accent = Color(0xFF64748B),
    )
    "VSCodeDefaultDark" -> darkEntryPalette(
        background = Color(0xFF1E1E1E),
        panel = Color(0xFF252526),
        elevatedPanel = Color(0xFF2D2D30),
        border = Color(0xFF3C3C3C),
        accent = Color(0xFF007ACC),
    )
    "VSCodeDefaultLight" -> lightEntryPalette(
        background = Color(0xFFFFFFFF),
        elevatedPanel = Color(0xFFF3F3F3),
        border = Color(0xFFE5E5E5),
        accent = Color(0xFF007ACC),
    )
    else -> if (darkMode) {
        darkEntryPalette(
            background = Color(0xFF070617),
            panel = Color(0xFF0A0C24),
            elevatedPanel = Color(0xFF11122D),
            border = Color(0xFFFF2D75).copy(alpha = 0.38f),
            accent = Color(0xFFFF2D75),
        )
    } else {
        lightEntryPalette(
            background = Color(0xFFFFFBF8),
            elevatedPanel = Color(0xFFFFE7F0),
            border = Color(0xFFFFC4DC),
            accent = Color(0xFFFF2D75),
        )
    }
}

private fun darkEntryPalette(
    background: Color,
    panel: Color,
    elevatedPanel: Color,
    border: Color,
    accent: Color,
): EntryPalette = EntryPalette(
    background = background,
    panel = panel,
    elevatedPanel = elevatedPanel,
    text = Color.White,
    mutedText = Color(0xFFD8DEEB),
    border = border,
    accent = accent,
    accentAlt = accent,
    error = Color(0xFFFF7B7B),
)

private fun lightEntryPalette(
    background: Color,
    elevatedPanel: Color,
    border: Color,
    accent: Color,
): EntryPalette = EntryPalette(
    background = background,
    panel = Color.White,
    elevatedPanel = elevatedPanel,
    text = Color(0xFF151A24),
    mutedText = Color(0xFF475569),
    border = border,
    accent = accent,
    accentAlt = accent,
    error = Color(0xFFB42318),
)

@Composable
private fun ThemeCycleButton(
    darkMode: Boolean,
    nextThemeName: String,
    buttonColor: Color,
    borderColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(40.dp),
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = buttonColor,
        border = BorderStroke(1.dp, borderColor),
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (darkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = "Cycle theme to $nextThemeName",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun discoverLocalProjects(context: Context): List<LocalProject> {
    val projects = mutableListOf<LocalProject>()
    val legacyWorkspace = File(context.filesDir, "workspace")
    if (legacyWorkspace.isDirectory && legacyWorkspace.listFiles().orEmpty().any { !it.name.startsWith('.') }) {
        projects += LocalProject(
            name = "My Workspace",
            directory = legacyWorkspace,
            modifiedAt = projectModifiedAt(legacyWorkspace),
            canDelete = false,
        )
    }

    val projectsDirectory = File(context.filesDir, "projects").apply { mkdirs() }
    projectsDirectory.listFiles()
        .orEmpty()
        .filter { directory ->
            directory.isDirectory && isVisibleProjectDirectoryName(directory.name)
        }
        .forEach { directory ->
            projects += LocalProject(
                name = directory.name,
                directory = directory,
                modifiedAt = projectModifiedAt(directory),
                canDelete = true,
            )
        }
    return projects.sortedByDescending(LocalProject::modifiedAt)
}

internal fun renameLocalProject(
    context: Context,
    project: LocalProject,
    rawName: String,
): Result<LocalProject> = runCatching {
    check(project.canDelete) { "This project cannot be renamed." }
    validateProjectName(rawName)?.let { error(it) }

    val name = rawName.trim()
    val projectsDirectory = File(context.filesDir, "projects").apply { mkdirs() }.canonicalFile
    val source = project.directory.canonicalFile
    check(source.parentFile == projectsDirectory) { "This project is outside the projects folder." }
    check(source.isDirectory) { "The project folder no longer exists." }

    val target = File(projectsDirectory, name).canonicalFile
    check(target.parentFile == projectsDirectory) { "Invalid project name." }
    if (source == target) return@runCatching project
    check(!target.exists()) { "A project named $name already exists." }
    check(source.renameTo(target)) { "Could not rename the project folder." }

    LocalProject(
        name = name,
        directory = target,
        modifiedAt = projectModifiedAt(target),
        canDelete = true,
    )
}

internal fun createLocalProject(context: Context, rawName: String): Result<LocalProject> = runCatching {
    validateProjectName(rawName)?.let { error(it) }
    val name = rawName.trim()
    val projectsDirectory = File(context.filesDir, "projects").apply { mkdirs() }
    val projectDirectory = File(projectsDirectory, name)
    check(projectDirectory.canonicalFile.parentFile == projectsDirectory.canonicalFile) { "Invalid project name." }
    check(!projectDirectory.exists()) { "A project named $name already exists." }
    check(projectDirectory.mkdirs()) { "Could not create the project folder." }
    File(projectDirectory, "index.html").writeText(
        """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$name</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <main class="app">
        <h1>$name</h1>
        <p>Edit HTML, CSS, and JavaScript offline in PocketCoded Web.</p>
        <button id="actionButton" type="button">Run JavaScript</button>
        <p id="output"></p>
    </main>
    <script src="script.js"></script>
</body>
</html>
""".trimIndent()
    )
    File(projectDirectory, "style.css").writeText(
        """:root {
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
""".trimIndent()
    )
    File(projectDirectory, "script.js").writeText(
        """const button = document.querySelector('#actionButton');
const output = document.querySelector('#output');

button?.addEventListener('click', () => {
    output.textContent = 'JavaScript is running in the PocketCoded Web preview.';
});
""".trimIndent()
    )
    LocalProject(
        name = name,
        directory = projectDirectory,
        modifiedAt = projectModifiedAt(projectDirectory),
        canDelete = true,
    )
}

internal fun validateProjectName(rawName: String): String? {
    val name = rawName.trim()
    if (name.isBlank()) return "Enter a project name."
    if (name.length > 60) return "Use a project name under 60 characters."
    if (name.any { it in "\\/:*?\"<>|" }) return "The project name contains an unsupported character."
    if (name.startsWith('.')) return "Project names cannot start with a period."
    if (!isVisibleProjectDirectoryName(name)) return "That name is reserved for app runtime files."
    return null
}

internal fun isVisibleProjectDirectoryName(name: String): Boolean {
    if (name.startsWith('.')) return false
    return name.lowercase() !in setOf("bin", "tmp")
}

private fun projectModifiedAt(directory: File): Long {
    return directory.walkTopDown()
        .maxOfOrNull(File::lastModified)
        ?.takeIf { it > 0L }
        ?: directory.lastModified()
}
