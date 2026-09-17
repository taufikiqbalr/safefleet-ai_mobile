package com.safefleet.ai.mobile.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FaceMetricsCalculatorTest {
    @Test
    fun `calculates EAR and MAR from landmark geometry`() {
        val landmarks = MutableList(478) { FaceLandmark(0.5, 0.5, 0.0) }

        setEye(landmarks, intArrayOf(33, 160, 158, 133, 153, 144), xOffset = 10.0)
        setEye(landmarks, intArrayOf(362, 385, 387, 263, 373, 380), xOffset = 50.0)
        setMouth(landmarks)
        setHeadPosePoints(landmarks)

        val metrics = FaceMetricsCalculator.calculate(
            landmarks = landmarks,
            imageWidth = 100,
            imageHeight = 100,
            timestampMs = 1_000,
            inferenceLatencyMs = 12,
        )

        assertNotNull(metrics)
        assertEquals(0.2, metrics!!.eyeAspectRatio, 0.0001)
        assertEquals(0.6, metrics.mouthAspectRatio, 0.0001)
        assertEquals(12, metrics.inferenceLatencyMs)
        assertEquals(FaceMetricsCalculator.MODEL_VERSION, metrics.modelVersion)
    }

    private fun setEye(landmarks: MutableList<FaceLandmark>, indices: IntArray, xOffset: Double) {
        val points = listOf(
            xOffset to 40.0,
            (xOffset + 2.0) to 41.0,
            (xOffset + 8.0) to 41.0,
            (xOffset + 10.0) to 40.0,
            (xOffset + 8.0) to 39.0,
            (xOffset + 2.0) to 39.0,
        )
        indices.forEachIndexed { index, landmarkIndex ->
            val point = points[index]
            landmarks[landmarkIndex] = FaceLandmark(point.first / 100.0, point.second / 100.0, 0.0)
        }
    }

    private fun setMouth(landmarks: MutableList<FaceLandmark>) {
        val points = mapOf(
            61 to (30.0 to 70.0),
            81 to (32.0 to 68.0),
            13 to (35.0 to 68.0),
            311 to (38.0 to 68.0),
            291 to (40.0 to 70.0),
            402 to (38.0 to 72.0),
            14 to (35.0 to 72.0),
            178 to (32.0 to 72.0),
        )
        points.forEach { (index, point) ->
            landmarks[index] = FaceLandmark(point.first / 100.0, point.second / 100.0, 0.0)
        }
    }

    private fun setHeadPosePoints(landmarks: MutableList<FaceLandmark>) {
        landmarks[10] = FaceLandmark(0.5, 0.2, -0.05)
        landmarks[152] = FaceLandmark(0.5, 0.8, 0.02)
        landmarks[234] = FaceLandmark(0.2, 0.5, 0.0)
        landmarks[454] = FaceLandmark(0.8, 0.5, 0.0)
    }
}
