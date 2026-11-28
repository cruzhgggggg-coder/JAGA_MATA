package com.jagamata.app.detection

/**
 * Agregasi sampel per siklus 60 detik (PRD US-09).
 * Menghitung min/avg/max jarak, avg lux, max pitch, jumlah sample,
 * jumlah tanpa wajah, dan daftar violations. [clear] dipanggil setelah evaluate.
 *
 * Thread-safety (M17): `samples` di-`add` dari thread MediaPipe dan
 * di-`evaluate`/`clear` dari thread ticker - semua akses dikunci `@Synchronized`
 * pada instance ini agar tidak terjadi ConcurrentModificationException.
 */
class CycleAggregator {
    private val samples = mutableListOf<DetectionResult>()

    @Synchronized
    fun add(result: DetectionResult) {
        samples.add(result)
    }

    @Synchronized
    fun sampleCount(): Int = samples.size

    /**
     * Evaluasi siklus. [startedAtMs] = wall-clock (untuk baris DB); bila null,
     * dipakai timestamp sampel pertama. Default argumen dievaluasi di luar
     * monitor, karena itu resolusi waktu dilakukan di dalam sinkronisasi (M17).
     */
    @Synchronized
    fun evaluate(
        startedAtMs: Long? = null,
        cycleDurationMs: Long = 0L,
    ): CycleSummary {
        val started = startedAtMs ?: samples.firstOrNull()?.timestampMs ?: 0L
        val summary = summarize(started, cycleDurationMs)
        clear()
        return summary
    }

    @Synchronized
    fun clear() {
        samples.clear()
    }

    private fun summarize(
        startedAtMs: Long,
        cycleDurationMs: Long,
    ): CycleSummary {
        val snapshot = samples.toList()
        val distances = snapshot.mapNotNull { it.distanceCm }
        val luxValues = snapshot.mapNotNull { it.lux }
        val pitches = snapshot.mapNotNull { it.pitchDeg }

        val violations =
            buildList {
                if (distances.any { it < CLOSE_CM }) add(ViolationType.TOO_CLOSE)
                // M7: gelap hanya dihitung bila ada wajah terdeteksi pada sampel
                // yang sama - ponsel telungkup / kamera tertutup di malam hari
                // bukan pelanggaran.
                if (snapshot.any { it.hasFace() && (it.lux ?: Float.MAX_VALUE) < DARK_LUX }) {
                    add(ViolationType.TOO_DARK)
                }
                if (pitches.any { it > POSTURE_DEG }) add(ViolationType.BAD_POSTURE)
            }

        return CycleSummary(
            startedAtMs = startedAtMs,
            sampleCount = snapshot.size,
            noFaceCount = snapshot.count { !it.hasFace() },
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

/** True bila sampel memuat wajah terdeteksi (bukan frame kosong). */
internal fun DetectionResult.hasFace(): Boolean = distanceCm != null || faceHeightPx != null

/** Hasil satu siklus - 1 baris `check_snapshots` (PRD §2.11). */
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
