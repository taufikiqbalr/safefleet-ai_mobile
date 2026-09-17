package com.safefleet.ai.mobile.data.network

fun normalizeApiBaseUrl(value: String): String {
    val trimmed = value.trim()
    require(trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        "SafeFleet API base URL must use http or https"
    }
    return if (trimmed.endsWith('/')) trimmed else "$trimmed/"
}
