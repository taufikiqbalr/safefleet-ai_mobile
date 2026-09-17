package com.safefleet.ai.mobile.vision

import org.junit.Assert.assertThrows
import org.junit.Test

class ThresholdProfileValidatorTest {
    @Test
    fun `rejects inconsistent PERCLOS thresholds`() {
        assertThrows(IllegalArgumentException::class.java) {
            ThresholdProfileValidator.validate(
                validProfile().copy(
                    perclosCautionPercent = 90.0,
                    perclosDrowsyPercent = 70.0,
                ),
            )
        }
    }

    @Test
    fun `rejects prolonged closure shorter than blink maximum`() {
        assertThrows(IllegalArgumentException::class.java) {
            ThresholdProfileValidator.validate(
                validProfile().copy(prolongedEyeClosureMs = 400),
            )
        }
    }

    private fun validProfile() = ThresholdProfile(
        id = "test-only",
        version = 1,
        displayName = "Unit test profile",
        eyeAspectRatioClosedThreshold = 0.25,
        mouthAspectRatioYawnThreshold = 0.60,
        perclosWindowMs = 10_000,
        perclosCautionPercent = 30.0,
        perclosDrowsyPercent = 60.0,
        blinkMinClosureMs = 100,
        blinkMaxClosureMs = 500,
        prolongedEyeClosureMs = 1_000,
        yawnMinDurationMs = 500,
        yawnWindowMs = 10_000,
        yawnCountCaution = 2,
    )
}
