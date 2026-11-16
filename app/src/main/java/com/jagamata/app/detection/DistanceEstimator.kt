package com.jagamata.app.detection

import kotlin.math.abs

/**
 * Estimasi jarak wajah ke layar (PRD §2.7, US-03).
 *
 * Rumus: d = (H_wajah × f_px) / h_piksel
 * - H_wajah default 15 cm
 * - f_px hasil kalibrasi (lihat [CalibrationManager])
 * - tanpa wajah -> null, bukan 0
 *
 * Nilai halus memakai EMA (alpha 0.4) supaya angka tidak berkedip.
 */
class DistanceEstimator(
    private val faceHeightCm: Float = DEFAULT_FACE_HEIGHT_CM,
    private val emaAlpha: Float = DEFAULT_EMA_ALPHA
) {
    private var emaCm: Float? = null

    /** Hitung mentah tanpa smoothing. */
    fun computeRawCm(faceHeightPx: Float?, focalPx: Float): Float? {
        if (faceHeightPx == null || faceHeightPx <= 0f || focalPx <= 0f) return null
        return (faceHeightCm * focalPx) / faceHeightPx
    }

    /** Hitung + haluskan (EMA). Reset otomatis kalau wajah hilang terlalu lama? Tidak — nilai terakhir dipertahankan. */
    fun update(faceHeightPx: Float?, focalPx: Float): Float? {
        val raw = computeRawCm(faceHeightPx, focalPx) ?: return null
        val previous = emaCm
        val smoothed = if (previous == null) {
            raw
        } else {
            emaAlpha * raw + (1f - emaAlpha) * previous
        }
        emaCm = smoothed
        return smoothed
    }

    /** Nilai terakhir yang sudah dihaluskan (untuk tampilan debug). */
    fun lastSmoothedCm(): Float? = emaCm

    fun reset() {
        emaCm = null
    }

    companion object {
        const val DEFAULT_FACE_HEIGHT_CM = 15f
        const val DEFAULT_EMA_ALPHA = 0.4f

        /**
         * Tinggi wajah dalam piksel dari bounding box deteksi.
         * ML Kit / MediaPipe memberi kotak; kita pakai tinggi kotak.
         */
        fun faceHeightPxFromBox(boxHeightPx: Float): Float? =
            if (boxHeightPx > 0f) boxHeightPx else null

        /** Selisih relatif — dipakai test stabilitas kalibrasi. */
        fun isStableSeries(values: List<Float>, tolerance: Float): Boolean {
            if (values.size < 2) return false
            val avg = values.average().toFloat()
            return values.all { abs(it - avg) <= tolerance }
        }
    }
}
