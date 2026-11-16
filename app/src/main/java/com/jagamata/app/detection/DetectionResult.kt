package com.jagamata.app.detection

/**
 * Satu sampel hasil deteksi untuk satu frame.
 * `null` = fitur tidak terdeteksi / tidak tersedia — BUKAN 0 (US-03).
 */
data class DetectionResult(
    val timestampMs: Long,
    val faceHeightPx: Float? = null,
    val faceWidthPx: Float? = null,
    val pitchDeg: Float? = null,
    val lux: Float? = null,
    val distanceCm: Float? = null,
    val backend: DetectorBackend = DetectorBackend.NONE,
)

enum class DetectorBackend {
    NONE,
    MEDIAPIPE,
    MLKIT,
}

/** Level risiko — prioritas BLOCKED > WARNING > OK (PRD §2.6). */
enum class RiskLevel {
    OK,
    WARNING,
    BLOCKED,
}

/** Jenis pelanggaran per siklus (PRD US-09). */
enum class ViolationType {
    TOO_CLOSE,
    TOO_DARK,
    BAD_POSTURE,
}
