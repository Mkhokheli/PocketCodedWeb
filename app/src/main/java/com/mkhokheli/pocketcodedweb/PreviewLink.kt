package com.mkhokheli.pocketcodedweb

import java.net.URI

internal fun normalizePreviewLink(value: String): String? {
    val text = value.trim()
    val uri = runCatching { URI(text) }.getOrNull() ?: return null
    if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) return null
    if (uri.rawUserInfo != null || uri.port < -1 || uri.port == 0 || uri.port > 65535) return null
    return uri.toASCIIString()
}
