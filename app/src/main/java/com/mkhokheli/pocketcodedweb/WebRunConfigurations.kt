package com.mkhokheli.pocketcodedweb

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class WebRunConfiguration(val name: String, val command: String)

internal class WebRunConfigurationStore(context: Context, projectPath: String) {
    private val preferences = context.getSharedPreferences("web_run_configurations", Context.MODE_PRIVATE)
    private val key = projectPath
    fun load(): List<WebRunConfiguration> = runCatching {
        val entries = JSONArray(preferences.getString(key, "[]"))
        (0 until entries.length()).map {
            val entry = entries.getJSONObject(it)
            WebRunConfiguration(entry.getString("name"), entry.getString("command"))
        }.filter { it.name.isNotBlank() && it.command.isNotBlank() }.take(40)
    }.getOrDefault(emptyList())
    fun save(configurations: List<WebRunConfiguration>) {
        val entries = JSONArray()
        configurations.take(40).forEach { entries.put(JSONObject().put("name", it.name).put("command", it.command)) }
        preferences.edit().putString(key, entries.toString()).apply()
    }
}
