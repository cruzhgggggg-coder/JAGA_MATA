package com.jagamata.app.detection

/**
 * Agregasi sampel per siklus 60 detik (PRD US-09).
 * Menghitung min/avg/max jarak, avg lux, max pitch, jumlah sample,
 * jumlah tanpa wajah, dan daftar violations. [clear] dipanggil setelah evaluate.
 */
class CycleAggregator {
    private val samples = mutableListOf<DetectionResult>()

    fun add(result: DetectionResult) {
        samples.add(result)
    }

    fun sampleCount(): Int = samples.size

    fun evaluate(
        startedAtMs: Long = samples.firstOrNull()?.timestampMs ?: 0L,
        cycleDurationMs: Long = 0L,
    ): CycleSummary {
        val summary = summarize(startedAtMs, cycleDurationMs)
        clear()
        return summary
    }

    fun clear() {
        samples.clear()
    }

    private fun summarize(
        startedAtMs: Long,
        cycleDurationMs: Long,
    ): CycleSummary {
        val distances = samples.mapNotNull { it.distanceCm }
        val luxValues = samples.mapNotNull { it.lux }
        val pitches = samples.mapNotNull { it.pitchDeg }

        val violations =
            buildList {
                if (distances.any { it < CLOSE_CM }) add(ViolationType.TOO_CLOSE)
                if (luxValues.any { it < DARK_LUX }) add(ViolationType.TOO_DARK)
                if (pitches.any { it > POSTURE_DEG }) add(ViolationType.BAD_POSTURE)
            }

        return CycleSummary(
            startedAtMs = startedAtMs,
            sampleCount = samples.size,
            noFaceCount = samples.count { it.distanceCm == null },
            minDistanceCm = distances.minOrNull(),
            avgDistanceCm = distances.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            maxDistanceCm = distances.maxOrNull(),
            avgLux = luxValues.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            maxPitchDeg = pitches.maxOrNull(),
            violations = violations,
            cycleDurationMs = cycleDurationMs,
        )
    }

    companion object {
        const val CLOSE_CM = 30f
        const val DARK_LUX = 150f
        const val POSTURE_DEG = 30f
    }
}

/** Hasil satu siklus — 1 baris `check_snapshots` (PRD §2.11). */
data class CycleSummary(
    val startedAtMs: Long,
    val sampleCount: Int,
    val noFaceCount: Int,
    val minDistanceCm: Float?,
    val avgDistanceCm: Float?,
    val maxDistanceCm: Float?,
    val avgLux: Float?,
    val maxPitchDeg: Float?,
    val violations: List<ViolationType>,
    val cycleDurationMs: Long = 0L,
) {
    val hasViolation: Boolean get() = violations.isNotEmpty()

    fun violationsCsv(): String = violations.joinToString("|") { it.name }
}
