package com.mkhokheli.pocketcodedweb

import android.content.Context

/** Provider credentials are separate; model/endpoint overrides belong to each model option. */
internal class AiConnectionStore(context: Context) {
    private val preferences = context.getSharedPreferences("ai_connections", Context.MODE_PRIVATE)
    fun key(provider: String): String? = preferences.getString("key:$provider", null)
    fun endpoint(model: String): String? = preferences.getString("endpoint:$model", null)
    fun model(model: String): String? = preferences.getString("model:$model", null)
    fun save(provider: String, option: String, key: String?, endpoint: String?, model: String?) {
        preferences.edit().putString("key:$provider", key).putString("endpoint:$option", endpoint)
            .putString("model:$option", model).apply()
    }
    fun migrateLegacy(provider: String, key: String?) {
        if (!preferences.getBoolean("legacy_migrated", false)) {
            preferences.edit().putBoolean("legacy_migrated", true).apply()
            if (!key.isNullOrBlank() && key(provider) == null) preferences.edit().putString("key:$provider", key).apply()
        }
    }
}
