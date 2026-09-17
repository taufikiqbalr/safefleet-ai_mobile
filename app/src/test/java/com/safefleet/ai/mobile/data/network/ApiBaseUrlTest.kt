package com.safefleet.ai.mobile.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ApiBaseUrlTest {
    @Test
    fun `normalizer adds trailing slash`() {
        assertEquals(
            "https://example.test/api/v1/",
            normalizeApiBaseUrl("https://example.test/api/v1"),
        )
    }

    @Test
    fun `normalizer preserves existing trailing slash`() {
        assertEquals(
            "http://10.0.2.2:6100/api/v1/",
            normalizeApiBaseUrl("http://10.0.2.2:6100/api/v1/"),
        )
    }

    @Test
    fun `normalizer rejects unsupported schemes`() {
        assertThrows(IllegalArgumentException::class.java) {
            normalizeApiBaseUrl("ftp://example.test")
        }
    }
}
