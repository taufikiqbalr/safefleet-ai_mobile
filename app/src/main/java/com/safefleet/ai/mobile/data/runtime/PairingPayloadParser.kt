package com.safefleet.ai.mobile.data.runtime

import com.google.gson.JsonParser
import com.safefleet.ai.mobile.security.DeviceCredential

object PairingPayloadParser {
    private val uuidRegex = Regex(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
    )

    fun parse(rawPayload: String): DeviceCredential {
        val payload = rawPayload.trim()
        require(payload.isNotEmpty()) { "Pairing payload is empty" }

        val json = runCatching { JsonParser.parseString(payload).asJsonObject }
            .getOrElse { throw IllegalArgumentException("Pairing payload must be valid JSON", it) }
        val deviceId = json.get("deviceId")?.asString?.trim().orEmpty()
        val deviceKey = json.get("deviceKey")?.asString?.trim().orEmpty()

        require(uuidRegex.matches(deviceId)) { "Pairing payload contains an invalid deviceId" }
        require(deviceKey.length >= 16) { "Pairing payload contains an invalid deviceKey" }

        return DeviceCredential(deviceId = deviceId, deviceKey = deviceKey)
    }
}
