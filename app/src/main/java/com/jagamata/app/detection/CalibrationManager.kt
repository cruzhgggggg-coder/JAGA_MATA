package com.jagamata.app.detection

import kotlin.math.abs

/**
 * Kalibrasi jarak (PRD §2.7–8, US-02).
 *
 * f_px = h_px_kalib × 30 / 15
 * Target 30 cm dengan tinggi wajah acuan 15 cm. Wajib 1× saat onboarding.
 */
class CalibrationManager(
    private val targetDistanceCm: Float = TARGET_DISTANCE_CM,
    private val referenceFaceHeightCm: Float = REFERENCE_FACE_HEIGHT_CM,
    private val stabilityMs: Long = STABILITY_MS,
    private val stabilityTolerancePx: Float = DEFAULT_TOLERANCE_PX
) {
    private val recentSamples = ArrayDeque<Pair<Long, Float>>()

    /**
     * Tambahkan sampel tinggi wajah (px) pada [timestampMs].
     * @return f_px jika seri sudah stabil ≥ [stabilityMs], selain itu null.
     */
    fun addSample(faceHeightPx: Float, timestampMs: Long): Float? {
        if (faceHeightPx <= 0f) return null
        recentSamples.addLast(timestampMs to faceHeightPx)
        while (recentSamples.isNotEmpty() && timestampMs - recentSamples.first().first > stabilityMs) {
            recentSamples.removeFirst()
        }
        if (!isStable()) return null
        return focalPxFromCalibration(faceHeightPx)
    }

    fun isStable(): Boolean {
        if (recentSamples.size < 2) return false
        val (start, _) = recentSamples.first()
        val (end, _) = recentSamples.last()
        if (end - start < stabilityMs) return false
        return DistanceEstimator.isStableSeries(
            recentSamples.map { it.second },
            tolerance = stabilityTolerancePx
        )
    }

    fun reset() {
        recentSamples.clear()
    }

    companion object {
        const val TARGET_DISTANCE_CM = 30f
        const val REFERENCE_FACE_HEIGHT_CM = 15f
        const val STABILITY_MS = 1500L
        const val DEFAULT_TOLERANCE_PX = 8f

        /** Rumus PRD: f_px = h_px_kalib × 30 / 15. */
        fun focalPxFromCalibration(calibrationFaceHeightPx: Float): Float =
            calibrationFaceHeightPx * targetRatio()

        fun targetRatio(): Float = 30f / 15f

        /** Balik rumus — dipakai test: h_px = (H × f_px) / d. */
        fun faceHeightPxFor(distanceCm: Float, focalPx: Float, faceHeightCm: Float = 15f): Float =
            (faceHeightCm * focalPx) / distanceCm

        fun isApproxEqual(a: Float, b: Float, epsilon: Float = 0.01f): Boolean = abs(a - b) <= epsilon
    }
}
