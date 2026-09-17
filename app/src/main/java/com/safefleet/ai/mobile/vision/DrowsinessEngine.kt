package com.safefleet.ai.mobile.vision

import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min

class DrowsinessEngine {
    private var profile: ThresholdProfile? = null
    private var lastTimestampMs: Long? = null
    private var lastEyesClosed = false
    private var eyeClosureStartedAtMs: Long? = null
    private var yawnStartedAtMs: Long? = null
    private var currentYawnCounted = false

    private val eyeSegments = ArrayDeque<EyeSegment>()
    private val blinkTimes = ArrayDeque<Long>()
    private val yawnTimes = ArrayDeque<Long>()

    @Synchronized
    fun updateProfile(next: ThresholdProfile?) {
        val validated = next?.let(ThresholdProfileValidator::validate)
        if (profile?.id == validated?.id && profile?.version == validated?.version) return
        profile = validated
        resetTemporalState()
    }

    @Synchronized
    fun observe(metrics: FaceMetrics): TemporalDrowsinessResult {
        val activeProfile = profile ?: return TemporalDrowsinessResult(
            timestampMs = metrics.timestampMs,
            state = DriverState.UNCALIBRATED,
            perclosPercent = null,
            blinkRatePerMinute = null,
            eyeClosureDurationMs = 0,
            yawning = false,
            yawnDurationMs = 0,
            yawnCountInWindow = 0,
            activeRules = emptyList(),
        ).also {
            lastTimestampMs = metrics.timestampMs
        }

        if (lastTimestampMs != null && metrics.timestampMs < lastTimestampMs!!) {
            resetTemporalState()
        }

        val timestamp = metrics.timestampMs
        val eyesClosed = metrics.eyeAspectRatio < activeProfile.eyeAspectRatioClosedThreshold
        val yawnSignal = metrics.mouthAspectRatio >= activeProfile.mouthAspectRatioYawnThreshold

        appendObservedEyeInterval(timestamp)
        updateEyeClosure(timestamp, eyesClosed, activeProfile)
        updateYawn(timestamp, yawnSignal, activeProfile)

        lastTimestampMs = timestamp
        lastEyesClosed = eyesClosed

        pruneTemporalWindows(timestamp, activeProfile)

        val eyeClosureDuration = if (eyesClosed) {
            timestamp - (eyeClosureStartedAtMs ?: timestamp)
        } else {
            0L
        }.coerceAtLeast(0)
        val yawnDuration = if (yawnSignal) {
            timestamp - (yawnStartedAtMs ?: timestamp)
        } else {
            0L
        }.coerceAtLeast(0)
        val perclos = calculatePerclos(timestamp, activeProfile.perclosWindowMs)
        val blinkRate = blinkTimes.size.toDouble()

        val rules = mutableListOf<String>()
        var state = DriverState.NORMAL

        if (eyeClosureDuration >= activeProfile.prolongedEyeClosureMs) {
            rules += "EYE_CLOSURE_PROLONGED"
            state = DriverState.DROWSY
        }
        if (perclos != null && perclos >= activeProfile.perclosDrowsyPercent) {
            rules += "PERCLOS_DROWSY"
            state = DriverState.DROWSY
        } else if (perclos != null && perclos >= activeProfile.perclosCautionPercent) {
            rules += "PERCLOS_CAUTION"
            if (state != DriverState.DROWSY) state = DriverState.CAUTION
        }
        if (yawnDuration >= activeProfile.yawnMinDurationMs) {
            rules += "YAWN_ACTIVE"
            if (state == DriverState.NORMAL) state = DriverState.CAUTION
        }
        if (yawnTimes.size >= activeProfile.yawnCountCaution) {
            rules += "REPEATED_YAWN"
            if (state == DriverState.NORMAL) state = DriverState.CAUTION
        }

        return TemporalDrowsinessResult(
            timestampMs = timestamp,
            state = state,
            perclosPercent = perclos,
            blinkRatePerMinute = blinkRate,
            eyeClosureDurationMs = eyeClosureDuration,
            yawning = yawnDuration >= activeProfile.yawnMinDurationMs,
            yawnDurationMs = yawnDuration,
            yawnCountInWindow = yawnTimes.size,
            activeRules = rules,
        )
    }

    @Synchronized
    fun onFaceMissing(timestampMs: Long): TemporalDrowsinessResult {
        profile?.let { activeProfile ->
            appendObservedEyeInterval(timestampMs)
            pruneTemporalWindows(timestampMs, activeProfile)
        }
        lastTimestampMs = null
        eyeClosureStartedAtMs = null
        yawnStartedAtMs = null
        currentYawnCounted = false
        lastEyesClosed = false

        return TemporalDrowsinessResult(
            timestampMs = timestampMs,
            state = DriverState.FACE_NOT_VISIBLE,
            perclosPercent = profile?.let { calculatePerclos(timestampMs, it.perclosWindowMs) },
            blinkRatePerMinute = profile?.let { blinkTimes.size.toDouble() },
            eyeClosureDurationMs = 0,
            yawning = false,
            yawnDurationMs = 0,
            yawnCountInWindow = yawnTimes.size,
            activeRules = listOf("FACE_NOT_VISIBLE"),
        )
    }

    private fun appendObservedEyeInterval(timestampMs: Long) {
        val previousTimestamp = lastTimestampMs ?: return
        if (timestampMs <= previousTimestamp) return
        eyeSegments.addLast(
            EyeSegment(
                startMs = previousTimestamp,
                endMs = timestampMs,
                closed = lastEyesClosed,
            ),
        )
    }

    private fun updateEyeClosure(
        timestampMs: Long,
        eyesClosed: Boolean,
        profile: ThresholdProfile,
    ) {
        if (eyesClosed && !lastEyesClosed) {
            eyeClosureStartedAtMs = timestampMs
        } else if (!eyesClosed && lastEyesClosed) {
            val startedAt = eyeClosureStartedAtMs
            if (startedAt != null) {
                val duration = (timestampMs - startedAt).coerceAtLeast(0)
                if (duration in profile.blinkMinClosureMs..profile.blinkMaxClosureMs) {
                    blinkTimes.addLast(timestampMs)
                }
            }
            eyeClosureStartedAtMs = null
        }
    }

    private fun updateYawn(
        timestampMs: Long,
        yawnSignal: Boolean,
        profile: ThresholdProfile,
    ) {
        if (yawnSignal) {
            if (yawnStartedAtMs == null) yawnStartedAtMs = timestampMs
            val duration = timestampMs - (yawnStartedAtMs ?: timestampMs)
            if (duration >= profile.yawnMinDurationMs && !currentYawnCounted) {
                yawnTimes.addLast(timestampMs)
                currentYawnCounted = true
            }
        } else {
            yawnStartedAtMs = null
            currentYawnCounted = false
        }
    }

    private fun pruneTemporalWindows(timestampMs: Long, profile: ThresholdProfile) {
        val perclosStart = timestampMs - profile.perclosWindowMs
        while (eyeSegments.isNotEmpty() && eyeSegments.first().endMs <= perclosStart) {
            eyeSegments.removeFirst()
        }
        val blinkStart = timestampMs - ONE_MINUTE_MS
        while (blinkTimes.isNotEmpty() && blinkTimes.first() < blinkStart) {
            blinkTimes.removeFirst()
        }
        val yawnStart = timestampMs - profile.yawnWindowMs
        while (yawnTimes.isNotEmpty() && yawnTimes.first() < yawnStart) {
            yawnTimes.removeFirst()
        }
    }

    private fun calculatePerclos(timestampMs: Long, windowMs: Long): Double? {
        if (eyeSegments.isEmpty()) return null
        val windowStart = timestampMs - windowMs
        var observedMs = 0L
        var closedMs = 0L
        eyeSegments.forEach { segment ->
            val start = max(segment.startMs, windowStart)
            val end = min(segment.endMs, timestampMs)
            if (end > start) {
                val duration = end - start
                observedMs += duration
                if (segment.closed) closedMs += duration
            }
        }
        if (observedMs <= 0) return null
        return closedMs.toDouble() * 100.0 / observedMs.toDouble()
    }

    private fun resetTemporalState() {
        lastTimestampMs = null
        lastEyesClosed = false
        eyeClosureStartedAtMs = null
        yawnStartedAtMs = null
        currentYawnCounted = false
        eyeSegments.clear()
        blinkTimes.clear()
        yawnTimes.clear()
    }

    private data class EyeSegment(
        val startMs: Long,
        val endMs: Long,
        val closed: Boolean,
    )

    private companion object {
        const val ONE_MINUTE_MS = 60_000L
    }
}
