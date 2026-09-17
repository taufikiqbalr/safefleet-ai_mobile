package com.safefleet.ai.mobile.data.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PairingPayloadParserTest {
    @Test
    fun `parses backend credential rotation response`() {
        val credential = PairingPayloadParser.parse(
            """
            {
              "deviceId": "123e4567-e89b-12d3-a456-426614174000",
              "deviceUid": "installation-1",
              "deviceKey": "abcdefghijklmnopqrstuvwxyz1234567890",
              "keyHint": "34567890"
            }
            """.trimIndent(),
        )

        assertEquals("123e4567-e89b-12d3-a456-426614174000", credential.deviceId)
        assertEquals("abcdefghijklmnopqrstuvwxyz1234567890", credential.deviceKey)
    }

    @Test
    fun `rejects invalid device id`() {
        assertThrows(IllegalArgumentException::class.java) {
            PairingPayloadParser.parse(
                """{"deviceId":"not-a-uuid","deviceKey":"abcdefghijklmnopqrstuvwxyz"}""",
            )
        }
    }

    @Test
    fun `rejects missing device key`() {
        assertThrows(IllegalArgumentException::class.java) {
            PairingPayloadParser.parse(
                """{"deviceId":"123e4567-e89b-12d3-a456-426614174000"}""",
            )
        }
    }
}
