package com.safefleet.ai.mobile.vision

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

object FaceMetricsCalculator {
    const val MODEL_VERSION = "mediapipe-face-landmarker-float16-v1"

    private val leftEye = intArrayOf(33, 160, 158, 133, 153, 144)
    private val rightEye = intArrayOf(362, 385, 387, 263, 373, 380)
    private val mouth = intArrayOf(61, 81, 13, 311, 291, 402, 14, 178)

    fun calculate(
        landmarks: List<FaceLandmark>,
        imageWidth: Int,
        imageHeight: Int,
        timestampMs: Long,
        inferenceLatencyMs: Long,
    ): FaceMetrics? {
        if (imageWidth <= 0 || imageHeight <= 0 || landmarks.size <= 454) return null

        val leftEar = eyeAspectRatio(landmarks, leftEye, imageWidth, imageHeight) ?: return null
        val rightEar = eyeAspectRatio(landmarks, rightEye, imageWidth, imageHeight) ?: return null
        val mar = mouthAspectRatio(landmarks, imageWidth, imageHeight) ?: return null
        val headPose = approximateHeadPose(landmarks, imageWidth, imageHeight) ?: return null

        return FaceMetrics(
            timestampMs = timestampMs,
            eyeAspectRatio = (leftEar + rightEar) / 2.0,
            mouthAspectRatio = mar,
            headPose = headPose,
            inferenceLatencyMs = inferenceLatencyMs.coerceAtLeast(0),
            modelVersion = MODEL_VERSION,
        )
    }

    private fun eyeAspectRatio(
        landmarks: List<FaceLandmark>,
        indices: IntArray,
        width: Int,
        height: Int,
    ): Double? {
        val p1 = point(landmarks, indices[0], width, height)
        val p2 = point(landmarks, indices[1], width, height)
        val p3 = point(landmarks, indices[2], width, height)
        val p4 = point(landmarks, indices[3], width, height)
        val p5 = point(landmarks, indices[4], width, height)
        val p6 = point(landmarks, indices[5], width, height)
        val horizontal = distance2d(p1, p4)
        if (horizontal <= 1e-9) return null
        return (distance2d(p2, p6) + distance2d(p3, p5)) / (2.0 * horizontal)
    }

    private fun mouthAspectRatio(
        landmarks: List<FaceLandmark>,
        width: Int,
        height: Int,
    ): Double? {
        val left = point(landmarks, mouth[0], width, height)
        val upperLeft = point(landmarks, mouth[1], width, height)
        val upperCenter = point(landmarks, mouth[2], width, height)
        val upperRight = point(landmarks, mouth[3], width, height)
        val right = point(landmarks, mouth[4], width, height)
        val lowerRight = point(landmarks, mouth[5], width, height)
        val lowerCenter = point(landmarks, mouth[6], width, height)
        val lowerLeft = point(landmarks, mouth[7], width, height)
        val horizontal = distance2d(left, right)
        if (horizontal <= 1e-9) return null
        return (
            distance2d(upperLeft, lowerLeft) +
                distance2d(upperCenter, lowerCenter) +
                distance2d(upperRight, lowerRight)
            ) / (2.0 * horizontal)
    }

    private fun approximateHeadPose(
        landmarks: List<FaceLandmark>,
        width: Int,
        height: Int,
    ): HeadPose? {
        val forehead = point(landmarks, 10, width, height)
        val chin = point(landmarks, 152, width, height)
        val leftCheek = point(landmarks, 234, width, height)
        val rightCheek = point(landmarks, 454, width, height)
        val leftEyeCorner = point(landmarks, 33, width, height)
        val rightEyeCorner = point(landmarks, 263, width, height)

        val horizontal = rightCheek - leftCheek
        val vertical = chin - forehead
        val normal = horizontal.cross(vertical).normalized() ?: return null

        val yaw = atan2(normal.x, -normal.z) * 180.0 / PI
        val pitch = atan2(normal.y, sqrt(normal.x * normal.x + normal.z * normal.z)) * 180.0 / PI
        val roll = atan2(
            rightEyeCorner.y - leftEyeCorner.y,
            rightEyeCorner.x - leftEyeCorner.x,
        ) * 180.0 / PI

        return HeadPose(pitchDeg = pitch, yawDeg = yaw, rollDeg = roll)
    }

    private fun point(
        landmarks: List<FaceLandmark>,
        index: Int,
        width: Int,
        height: Int,
    ): Point3 = landmarks[index].let {
        Point3(
            x = it.x * width,
            y = it.y * height,
            z = it.z * width,
        )
    }

    private fun distance2d(a: Point3, b: Point3): Double {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    private data class Point3(val x: Double, val y: Double, val z: Double) {
        operator fun minus(other: Point3) = Point3(x - other.x, y - other.y, z - other.z)

        fun cross(other: Point3) = Point3(
            x = y * other.z - z * other.y,
            y = z * other.x - x * other.z,
            z = x * other.y - y * other.x,
        )

        fun normalized(): Point3? {
            val length = sqrt(x * x + y * y + z * z)
            if (length <= 1e-9) return null
            return Point3(x / length, y / length, z / length)
        }
    }
}
