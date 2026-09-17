package com.safefleet.ai.mobile.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrowsinessEngineTest {
    @Test
    fun `without calibrated profile engine remains uncalibrated`() {
        val result = DrowsinessEngine().observe(metrics(timestamp = 0, ear = 0.1, mar = 0.9))

        assertEquals(DriverState.UNCALIBRATED, result.state)
        assertNull(result.perclosPercent)
    }

    @Test
    fun `counts closure in blink duration range as blink`() {
        val blinkOnlyProfile = testProfile().copy(
            perclosCautionPercent = 70.0,
            perclosDrowsyPercent = 90.0,
        )
        val engine = DrowsinessEngine().apply { updateProfile(blinkOnlyProfile) }

        engine.observe(metrics(timestamp = 0, ear = 0.30))
        engine.observe(metrics(timestamp = 100, ear = 0.20))
        val result = engine.observe(metrics(timestamp = 300, ear = 0.30))

        assertEquals(1.0, result.blinkRatePerMinute!!, 0.001)
        assertEquals(DriverState.NORMAL, result.state)
    }

    @Test
    fun `prolonged eye closure moves temporal state to drowsy`() {
        val engine = DrowsinessEngine().apply { updateProfile(testProfile()) }

        engine.observe(metrics(timestamp = 0, ear = 0.30))
        engine.observe(metrics(timestamp = 100, ear = 0.20))
        val result = engine.observe(metrics(timestamp = 1_200, ear = 0.20))

        assertEquals(DriverState.DROWSY, result.state)
        assertTrue("EYE_CLOSURE_PROLONGED" in result.activeRules)
        assertEquals(1_100, result.eyeClosureDurationMs)
    }

    @Test
    fun `sustained mouth opening is counted as yawn and caution`() {
        val engine = DrowsinessEngine().apply { updateProfile(testProfile()) }

        engine.observe(metrics(timestamp = 0, mar = 0.20))
        engine.observe(metrics(timestamp = 100, mar = 0.70))
        val result = engine.observe(metrics(timestamp = 700, mar = 0.70))

        assertEquals(DriverState.CAUTION, result.state)
        assertTrue(result.yawning)
        assertEquals(1, result.yawnCountInWindow)
        assertTrue("YAWN_ACTIVE" in result.activeRules)
    }

    private fun metrics(timestamp: Long, ear: Double = 0.30, mar: Double = 0.20) = FaceMetrics(
        timestampMs = timestamp,
        eyeAspectRatio = ear,
        mouthAspectRatio = mar,
        headPose = HeadPose(0.0, 0.0, 0.0),
        inferenceLatencyMs = 10,
        modelVersion = "test-model",
    )

    private fun testProfile() = ThresholdProfile(
        id = "test-only",
        version = 1,
        displayName = "Unit test profile",
        eyeAspectRatioClosedThreshold = 0.25,
        mouthAspectRatioYawnThreshold = 0.60,
        perclosWindowMs = 10_000,
        perclosCautionPercent = 50.0,
        perclosDrowsyPercent = 80.0,
        blinkMinClosureMs = 100,
        blinkMaxClosureMs = 500,
        prolongedEyeClosureMs = 1_000,
        yawnMinDurationMs = 500,
        yawnWindowMs = 10_000,
        yawnCountCaution = 2,
    )
}
