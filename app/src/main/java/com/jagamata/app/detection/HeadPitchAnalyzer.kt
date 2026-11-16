package com.jagamata.app.detection

import kotlin.math.abs
import kotlin.math.atan2

/**
 * Titik landmark ternormalisasi (skala MediaPipe: x/y relatif gambar, z kedalaman).
 */
data class LandmarkPoint(val x: Float, val y: Float, val z: Float)

/**
 * Analisis pitch kepala (PRD §2/US-07): geometri nose–chin dengan atan2.
 *
 * pitch = atan2(dz, dy) di mana v = chin - nose.
 * Konvensi tanda: positif = menunduk (memenuhi ambang >30° = postur buruk).
 */
object HeadPitchAnalyzer {
    const val NOSE_INDEX = 1
    const val CHIN_INDEX = 152
    const val LEFT_EYE_INDEX = 33
    const val RIGHT_EYE_INDEX = 263

    fun pitchDeg(nose: LandmarkPoint, chin: LandmarkPoint): Float {
        val dy = chin.y - nose.y
        val dz = chin.z - nose.z
        if (abs(dy) < EPSILON && abs(dz) < EPSILON) return 0f
        return Math.toDegrees(atan2(dz.toDouble(), dy.toDouble())).toFloat()
    }

    /** Ambil dari daftar landmark MediaPipe (478 titik). */
    fun pitchDegFromLandmarks(landmarks: List<LandmarkPoint>): Float? {
        if (landmarks.size <= CHIN_INDEX) return null
        val nose = landmarks[NOSE_INDEX]
        val chin = landmarks[CHIN_INDEX]
        return pitchDeg(nose, chin)
    }

    private const val EPSILON = 1e-6f
}
