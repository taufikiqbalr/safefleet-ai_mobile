package com.safefleet.ai.mobile.vision

data class FaceLandmark(
    val x: Double,
    val y: Double,
    val z: Double,
)

data class HeadPose(
    val pitchDeg: Double,
    val yawDeg: Double,
    val rollDeg: Double,
)

data class FaceMetrics(
    val timestampMs: Long,
    val eyeAspectRatio: Double,
    val mouthAspectRatio: Double,
    val headPose: HeadPose,
    val inferenceLatencyMs: Long,
    val modelVersion: String,
)

enum class DriverState {
    UNCALIBRATED,
    NORMAL,
    CAUTION,
    DROWSY,
    FACE_NOT_VISIBLE,
}

data class TemporalDrowsinessResult(
    val timestampMs: Long,
    val state: DriverState,
    val perclosPercent: Double?,
    val blinkRatePerMinute: Double?,
    val eyeClosureDurationMs: Long,
    val yawning: Boolean,
    val yawnDurationMs: Long,
    val yawnCountInWindow: Int,
    val activeRules: List<String>,
)

data class ThresholdProfile(
    val id: String,
    val version: Int,
    val displayName: String,
    val eyeAspectRatioClosedThreshold: Double,
    val mouthAspectRatioYawnThreshold: Double,
    val perclosWindowMs: Long,
    val perclosCautionPercent: Double,
    val perclosDrowsyPercent: Double,
    val blinkMinClosureMs: Long,
    val blinkMaxClosureMs: Long,
    val prolongedEyeClosureMs: Long,
    val yawnMinDurationMs: Long,
    val yawnWindowMs: Long,
    val yawnCountCaution: Int,
)

object ThresholdProfileValidator {
    fun validate(profile: ThresholdProfile): ThresholdProfile {
        require(profile.id.isNotBlank()) { "Threshold profile ID is required" }
        require(profile.displayName.isNotBlank()) { "Threshold profile name is required" }
        require(profile.version > 0) { "Threshold profile version must be positive" }
        require(profile.eyeAspectRatioClosedThreshold > 0.0) { "EAR threshold must be positive" }
        require(profile.mouthAspectRatioYawnThreshold > 0.0) { "MAR threshold must be positive" }
        require(profile.perclosWindowMs > 0) { "PERCLOS window must be positive" }
        require(profile.perclosCautionPercent in 0.0..100.0) { "PERCLOS caution must be 0..100" }
        require(profile.perclosDrowsyPercent in 0.0..100.0) { "PERCLOS drowsy must be 0..100" }
        require(profile.perclosCautionPercent <= profile.perclosDrowsyPercent) {
            "PERCLOS caution cannot exceed drowsy threshold"
        }
        require(profile.blinkMinClosureMs > 0) { "Minimum blink closure must be positive" }
        require(profile.blinkMaxClosureMs >= profile.blinkMinClosureMs) {
            "Maximum blink closure cannot be lower than minimum blink closure"
        }
        require(profile.prolongedEyeClosureMs > profile.blinkMaxClosureMs) {
            "Prolonged eye closure must exceed maximum blink closure"
        }
        require(profile.yawnMinDurationMs > 0) { "Minimum yawn duration must be positive" }
        require(profile.yawnWindowMs >= profile.yawnMinDurationMs) {
            "Yawn observation window must include minimum yawn duration"
        }
        require(profile.yawnCountCaution > 0) { "Repeated-yawn count must be positive" }
        return profile
    }
}
