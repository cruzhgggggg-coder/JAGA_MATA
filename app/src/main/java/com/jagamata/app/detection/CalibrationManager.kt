package com.jagamata.app.detection

import kotlin.math.abs

/**
 * Kalibrasi jarak (PRD §2.7–8, US-02).
 *
 * f_px = h_px_kalib × 30 / 15
 * Target 30 cm dengan tinggi wajah acuan 15 cm. Wajib 1× saat onboarding.
 */
class CalibrationManager(
    private val stabilityMs: Long = STABILITY_MS,
    private val stabilityTolerancePx: Float = DEFAULT_TOLERANCE_PX,
) {
    private val recentSamples = ArrayDeque<Pair<Long, Float>>()

    /**
     * Tambahkan sampel tinggi wajah (px) pada [timestampMs].
     *
     * [pitchDeg] (opsional) adalah head pitch saat sampel diambil; tinggi wajah
     * dikompensasi memakai fungsi yang sama dengan runtime ([DistanceEstimator.effectiveFaceHeightPx])
     * supaya f_px yang dihasilkan konsisten dengan rumus jarak runtime (T10).
     *
     * @return f_px jika seri sudah stabil ≥ [stabilityMs], selain itu null.
     */
    fun addSample(
        faceHeightPx: Float,
        timestampMs: Long,
        faceHeightCm: Float = REFERENCE_FACE_HEIGHT_CM,
        pitchDeg: Float? = null,
    ): Float? {
        if (faceHeightPx <= 0f) return null
        val effectiveHeightPx = DistanceEstimator.effectiveFaceHeightPx(faceHeightPx, pitchDeg)
        if (!effectiveHeightPx.isFinite() || effectiveHeightPx <= 0f) return null
        recentSamples.addLast(timestampMs to effectiveHeightPx)
        // T11: pangkas hanya bila sampel KEDUA sudah menutup window stabilityMs —
        // dengan begitu span (terakhir - pertama) selalu bisa mencapai >= stabilityMs
        // pada fps berapa pun (15/24/30), bukan hanya saat span tepat == stabilityMs.
        while (recentSamples.size >= 2 && timestampMs - recentSamples[1].first >= stabilityMs) {
            recentSamples.removeFirst()
        }
        if (!isStable()) return null
        // Gunakan rata-rata tinggi wajah selama window stabil agar minim noise kamera
        val avgHeight = recentSamples.map { it.second }.average().toFloat()
        return focalPxFromCalibration(avgHeight, faceHeightCm)
    }

    fun isStable(): Boolean {
        if (recentSamples.size < 2) return false
        val (start, _) = recentSamples.first()
        val (end, _) = recentSamples.last()
        if (end - start < stabilityMs) return false
        return DistanceEstimator.isStableSeries(
            recentSamples.map { it.second },
            tolerance = stabilityTolerancePx,
        )
    }

    fun reset() {
        recentSamples.clear()
    }

    companion object {
        const val TARGET_DISTANCE_CM = 30f
        const val REFERENCE_FACE_HEIGHT_CM = 15f
        const val STABILITY_MS = 1500L
        const val DEFAULT_TOLERANCE_PX = 18f

        /**
         * Rumus PRD: f_px = h_px_kalib × 30 / 15.
         * [calibrationFaceHeightPx] harus tinggi efektif (sudah dikompensasi pitch
         * lewat [DistanceEstimator.effectiveFaceHeightPx]) supaya sama dengan
         * tinggi yang dipakai rumus jarak runtime (T10).
         */
        fun focalPxFromCalibration(
            calibrationFaceHeightPx: Float,
            faceHeightCm: Float = REFERENCE_FACE_HEIGHT_CM,
        ): Float = calibrationFaceHeightPx * (TARGET_DISTANCE_CM / faceHeightCm)

        fun targetRatio(): Float = 30f / 15f

        /** Balik rumus — dipakai test: h_px = (H × f_px) / d. */
        fun faceHeightPxFor(
            distanceCm: Float,
            focalPx: Float,
            faceHeightCm: Float = 15f,
        ): Float = (faceHeightCm * focalPx) / distanceCm

        fun isApproxEqual(
            a: Float,
            b: Float,
            epsilon: Float = 0.01f,
        ): Boolean = abs(a - b) <= epsilon
    }
}
