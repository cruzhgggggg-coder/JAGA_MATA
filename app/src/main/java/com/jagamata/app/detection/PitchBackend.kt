package com.jagamata.app.detection

import android.graphics.Bitmap

/**
 * Kontrak backend pitch (PRD §5.5) — titik sambung ke model hasil training rekan tim.
 *
 * Default & fallback = [LandmarkPitchBackend] (geometri MediaPipe, selalu ada).
 * Opsional drop-in = [TflitePitchBackend] (aktif hanya bila file model ada).
 */
interface PitchBackend {
    fun pitchDeg(
        landmarks: List<LandmarkPoint>,
        faceCrop: Bitmap?,
    ): Float?

    /** True bila backend membutuhkan crop wajah sebagai masukan. */
    fun requiresFaceCrop(): Boolean = false
}

/**
 * Backend bawaan: geometri nose–chin atan2 dari landmark.
 * Tanpa file model tambahan (F9: platform tetap penuh tanpa model).
 */
class LandmarkPitchBackend : PitchBackend {
    override fun pitchDeg(
        landmarks: List<LandmarkPoint>,
        faceCrop: Bitmap?,
    ): Float? = HeadPitchAnalyzer.pitchDegFromLandmarks(landmarks)
}

/**
 * Memilih backend sesuai flag [useTflite] + ketersediaan file model.
 * File model tidak ada -> otomatis [LandmarkPitchBackend] (PRD §5.5 butir 2).
 */
object PitchBackendFactory {
    fun resolve(
        useTfliteFlag: Boolean,
        tfliteModelAvailable: Boolean,
        tflite: PitchBackend? = null,
        landmark: PitchBackend = LandmarkPitchBackend(),
    ): PitchBackend =
        if (useTfliteFlag && tfliteModelAvailable && tflite != null) {
            tflite
        } else {
            landmark
        }
}
