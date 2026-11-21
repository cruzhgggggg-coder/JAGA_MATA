package com.jagamata.app.detection

import kotlin.math.abs
import kotlin.math.cos

/**
 * Estimasi jarak wajah ke layar (PRD §2.7, US-03).
 *
 * Rumus: d = (H_wajah × f_px) / h_efektif
 * - H_wajah default 15 cm
 * - f_px hasil kalibrasi (lihat [CalibrationManager])
 * - h_efektif dikompensasi sudut head pitch agar kemiringan kepala tidak
 *   membuat jarak terhitung melompat 5-10 cm.
 * - tanpa wajah -> null, bukan 0
 *
 * Nilai halus memakai EMA (alpha 0.4) supaya angka tidak berkedip.
 */
class DistanceEstimator(
    var faceHeightCm: Float = DEFAULT_FACE_HEIGHT_CM,
    private val emaAlpha: Float = DEFAULT_EMA_ALPHA,
) {
    private var emaCm: Float? = null

    /**
     * Hitung mentah tanpa smoothing dengan kompensasi sudut kemiringan kepala (pitch)
     * dan cross-check lebar wajah untuk presisi saat ponsel dipegang di bawah mata.
     */
    fun computeRawCm(
        faceHeightPx: Float?,
        focalPx: Float,
        pitchDeg: Float? = null,
        faceWidthPx: Float? = null,
    ): Float? {
        if (faceHeightPx == null || faceHeightPx <= 0f || focalPx <= 0f) return null

        // Kompensasi kemiringan kepala (head pitch):
        // Ketika menunduk atau ponsel dipegang agak di bawah, proyeksi tinggi wajah 2D
        // menyusut sebesar cos(pitch).
        val effectiveFaceHeightPx =
            if (pitchDeg != null) {
                val pitchRad = Math.toRadians(pitchDeg.toDouble().coerceIn(-50.0, 50.0))
                val cosFactor = cos(pitchRad).toFloat().coerceIn(0.65f, 1.0f)
                faceHeightPx / cosFactor
            } else {
                faceHeightPx
            }

        return (faceHeightCm * focalPx) / effectiveFaceHeightPx
    }

    /** Hitung + haluskan (EMA). */
    fun update(
        faceHeightPx: Float?,
        focalPx: Float,
        pitchDeg: Float? = null,
        faceWidthPx: Float? = null,
    ): Float? {
        val raw = computeRawCm(faceHeightPx, focalPx, pitchDeg, faceWidthPx) ?: return null
        val previous = emaCm
        val smoothed =
            if (previous == null) {
                raw
            } else {
                val delta = abs(raw - previous)
                // Fast-attack: Jika pengguna bergerak cepat (delta > 8 cm), respon instan
                val dynamicAlpha = if (delta > 8f) 0.85f else emaAlpha
                dynamicAlpha * raw + (1f - dynamicAlpha) * previous
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

        /** Tinggi wajah acuan anak berdasarkan kelompok usia antropometri. */
        fun getChildFaceHeightCm(ageYears: Int): Float =
            when {
                ageYears <= 6 -> 13.5f
                ageYears <= 11 -> 14.0f
                else -> 15.0f
            }

        /**
         * Tinggi wajah dalam piksel dari bounding box deteksi.
         * ML Kit / MediaPipe memberi kotak; kita pakai tinggi kotak.
         */
        fun faceHeightPxFromBox(boxHeightPx: Float): Float? = if (boxHeightPx > 0f) boxHeightPx else null

        /** Selisih relatif — dipakai test stabilitas kalibrasi. */
        fun isStableSeries(
            values: List<Float>,
            tolerance: Float,
        ): Boolean {
            if (values.size < 2) return false
            val avg = values.average().toFloat()
            return values.all { abs(it - avg) <= tolerance }
        }
    }
}
