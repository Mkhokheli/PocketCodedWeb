package com.mkhokheli.pocketcodedweb

import java.net.URI

internal fun validatedAiEndpoint(raw: String, azure: Boolean = false): String {
    require(raw.isNotBlank()) { "Open AI API Settings and enter the complete API endpoint URL." }
    val uri = runCatching { URI(raw.trim()) }.getOrNull()
    require(uri != null && uri.scheme in setOf("https", "http") && !uri.host.isNullOrBlank()) {
        "Enter a complete AI endpoint URL starting with https:// or http://."
    }
    require(uri.rawUserInfo == null && uri.fragment == null) { "Remove credentials and fragments from the AI endpoint URL." }
    if (uri.scheme == "http") {
        val host = uri.host.lowercase().removeSurrounding("[", "]")
        val octets = host.split('.').mapNotNull { it.toIntOrNull() }
        val privateIpv4 = octets.size == 4 && octets.all { it in 0..255 } &&
            (octets[0] == 10 || octets[0] == 127 ||
                (octets[0] == 192 && octets[1] == 168) ||
                (octets[0] == 172 && octets[1] in 16..31))
        require(host == "localhost" || host == "::1" || privateIpv4) {
            "Use HTTPS for remote AI providers. HTTP is supported only for localhost or a private IPv4 address."
        }
    }
    if (azure) {
        require(uri.scheme == "https" && uri.path.endsWith("/chat/completions") &&
            uri.rawQuery.orEmpty().split('&').any { it.startsWith("api-version=") && it.substringAfter('=').isNotBlank() }) {
            "Enter the complete Azure HTTPS deployment chat-completions URL, including api-version."
        }
    }
    return uri.toString()
}
