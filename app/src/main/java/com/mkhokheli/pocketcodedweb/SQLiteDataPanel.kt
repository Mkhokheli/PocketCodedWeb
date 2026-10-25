package com.mkhokheli.pocketcodedweb

import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope

internal data class SqlGridResult(
    val columns: List<String>,
    val rows: List<List<String?>>, 
    val message: String,
)

internal fun querySqlite(databaseFile: File, source: String): SqlGridResult = runCatching {
    databaseFile.parentFile?.mkdirs()
    val statements = splitSqlStatements(source)
    if (statements.size != 1 || !statements.single().trimStart().matches(Regex("(?is)^(select|pragma|with)\\b.*"))) {
        return@runCatching SqlGridResult(emptyList(), emptyList(), runSqlScript(databaseFile.parentFile!!, source, databaseFile))
    }
    SQLiteDatabase.openDatabase(databaseFile.absolutePath, null,
        SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY).use { database ->
        database.rawQuery(statements.single(), null).use { cursor ->
            val columns = cursor.columnNames.toList()
            val rows = buildList {
                while (cursor.moveToNext() && size < 500) add((0 until cursor.columnCount).map { index ->
                    if (cursor.isNull(index)) null else if (cursor.getType(index) == android.database.Cursor.FIELD_TYPE_BLOB) "<blob ${cursor.getBlob(index).size} bytes>" else cursor.getString(index)
                })
            }
            SqlGridResult(columns, rows, "${rows.size} row(s) shown")
        }
    }
}.getOrElse { SqlGridResult(emptyList(), emptyList(), "SQL error: ${it.message ?: it.javaClass.simpleName}") }

internal fun sqliteTableNames(databaseFile: File): List<String> {
    if (!databaseFile.exists()) return emptyList()
    return SQLiteDatabase.openDatabase(databaseFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { database ->
        database.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'android_metadata' ORDER BY name",
            null,
        ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getString(0)) } }
    }
}

@Composable
internal fun SQLiteDataPanel(databaseFile: File, palette: WebPalette, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var query by remember(databaseFile.absolutePath) { mutableStateOf("") }
    var tables by remember(databaseFile.absolutePath) { mutableStateOf(emptyList<String>()) }
    var result by remember(databaseFile.absolutePath) { mutableStateOf<SqlGridResult?>(null) }
    var running by remember { mutableStateOf(false) }

    fun execute(sql: String) {
        if (sql.isBlank() || running) return
        running = true
        scope.launch {
            result = withContext(Dispatchers.IO) { querySqlite(databaseFile, sql) }
            tables = withContext(Dispatchers.IO) { runCatching { sqliteTableNames(databaseFile) }.getOrDefault(emptyList()) }
            running = false
        }
    }
    LaunchedEffect(databaseFile.absolutePath) {
        tables = withContext(Dispatchers.IO) { runCatching { sqliteTableNames(databaseFile) }.getOrDefault(emptyList()) }
        tables.firstOrNull()?.let { table ->
            query = "SELECT * FROM \"${table.replace("\"", "\"\"")}\";"
            execute(query)
        }
    }
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("SQLite Data Editor — ${databaseFile.name}", color = palette.text, fontSize = 17.sp)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            tables.forEach { table -> TextButton(onClick = {
                query = "SELECT * FROM \"${table.replace("\"", "\"\"")}\";"; execute(query)
            }, colors = readableTextButtonColors()) { Text(table) } }
        }
        OutlinedTextField(query, { query = it }, label = { Text("SQL query or data modification") },
            modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 5,
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp), colors = readableFieldColors())
        Button(onClick = { execute(query) }, enabled = !running, colors = readableButtonColors()) {
            Icon(Icons.Rounded.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text(if (running) "Running…" else "Execute")
        }
        result?.let { grid ->
            Text(grid.message, color = palette.mutedText, fontSize = 12.sp)
            Column(Modifier.fillMaxWidth().weight(1f).horizontalScroll(rememberScrollState()).verticalScroll(rememberScrollState())) {
                if (grid.columns.isNotEmpty()) DatabaseRow(grid.columns, palette, true)
                grid.rows.forEach { row -> DatabaseRow(row.map { it ?: "NULL" }, palette, false) }
            }
        }
    }
}

@Composable
private fun DatabaseRow(values: List<String>, palette: WebPalette, header: Boolean) {
    Row(Modifier.background(if (header) palette.elevatedPanel else Color.Transparent)) {
        values.forEach { value ->
            Text(value, color = if (header) palette.text else palette.mutedText, fontFamily = FontFamily.Monospace,
                fontSize = 12.sp, maxLines = 3, modifier = Modifier.width(150.dp).border(BorderStroke(0.5.dp, palette.border)).padding(6.dp))
        }
    }
}
