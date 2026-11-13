package com.mkhokheli.pocketcodedweb

import android.content.Context
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

internal data class WebRunConfiguration(val name: String, val command: String)

/** Keep edits in place and reject accidental name collisions instead of losing a command. */
internal fun updateWebRunConfigurations(
    configurations: List<WebRunConfiguration>,
    originalName: String?,
    configuration: WebRunConfiguration,
): List<WebRunConfiguration> {
    require(configuration.name.isNotBlank() && configuration.command.isNotBlank()) { "Enter a name and command." }
    require(configuration.name.length <= 120 && configuration.command.length <= 16_000) { "Name or command is too long." }
    require('\u0000' !in configuration.name && '\u0000' !in configuration.command) { "Remove null characters." }
    require(configurations.none { it.name == configuration.name && it.name != originalName }) { "A configuration with this name already exists." }
    if (originalName == null) {
        require(configurations.size < 40) { "A project can have up to 40 run configurations." }
        return configurations + configuration
    }
    require(configurations.any { it.name == originalName }) { "This configuration no longer exists." }
    return configurations.map { if (it.name == originalName) configuration else it }
}

internal class WebRunConfigurationStore(context: Context, projectPath: String) {
    private val preferences = context.getSharedPreferences("web_run_configurations", Context.MODE_PRIVATE)
    private val key = File(projectPath).canonicalPath
    private val legacyKey = projectPath
    fun load(): List<WebRunConfiguration> = runCatching {
        val entries = JSONArray(preferences.getString(key, null) ?: preferences.getString(legacyKey, "[]"))
        (0 until minOf(entries.length(), 40)).mapNotNull {
            val entry = entries.optJSONObject(it) ?: return@mapNotNull null
            val configuration = WebRunConfiguration(entry.optString("name"), entry.optString("command"))
            configuration.takeIf { value -> runCatching { updateWebRunConfigurations(emptyList(), null, value) }.isSuccess }
        }.distinctBy { it.name }
    }.getOrDefault(emptyList())
    fun save(configurations: List<WebRunConfiguration>) {
        val entries = JSONArray()
        configurations.take(40).forEach { entries.put(JSONObject().put("name", it.name).put("command", it.command)) }
        preferences.edit().putString(key, entries.toString()).apply()
    }
}
