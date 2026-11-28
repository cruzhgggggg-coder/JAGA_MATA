package com.jagamata.app.detection

/**
 * Agregasi sampel per siklus 60 detik (PRD US-09).
 * Menghitung min/avg/max jarak, avg lux, max pitch, jumlah sample,
 * jumlah tanpa wajah, dan daftar violations. [clear] dipanggil setelah evaluate.
 *
 * Ambang/zona diambil dari satu sumber [Thresholds] (T9 + M25) — bukan
 * konstanta internal. Sebuah siklus dihitung sebagai pelanggaran hanya bila
 * ≥ [VIOLATION_MIN_PERCENT]% sampel valid melanggar (bukan 1 sampel saja).
 *
 * Thread-safety (M17): `samples` di-`add` dari thread MediaPipe dan
 * di-`evaluate`/`clear` dari thread ticker - semua akses dikunci `@Synchronized`
 * pada instance ini agar tidak terjadi ConcurrentModificationException.
 * [thresholds] ditulis dari collector Flow di main thread dan dibaca di dalam
 * sinkronisasi yang sama.
 */
class CycleAggregator(
    initialThresholds: Thresholds = Thresholds.DEFAULT,
) {
    private val samples = mutableListOf<DetectionResult>()

    @set:Synchronized
    var thresholds: Thresholds = initialThresholds

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
        val t = thresholds
        val snapshot = samples.toList()
        val distances = snapshot.mapNotNull { it.distanceCm }
        val luxValues = snapshot.mapNotNull { it.lux }
        val pitches = snapshot.mapNotNull { it.pitchDeg }

        // Pelanggaran = proporsi sampel valid (T9): ≥30% harus melanggar.
        val closeViolating = distances.count { it < t.closeEnterCm }
        // M7: gelap hanya dihitung pada sampel yang memuat wajah + lux terukur —
        // ponsel telungkup / kamera tertutup di malam hari bukan pelanggaran.
        val darkValid = snapshot.count { it.hasFace() && it.lux != null }
        val darkViolating = snapshot.count { it.hasFace() && (it.lux ?: Float.MAX_VALUE) < t.lux }
        val postureViolating = pitches.count { it > t.pitchDeg }

        val violations =
            buildList {
                if (isViolation(distances.size, closeViolating)) add(ViolationType.TOO_CLOSE)
                if (isViolation(darkValid, darkViolating)) add(ViolationType.TOO_DARK)
                if (isViolation(pitches.size, postureViolating)) add(ViolationType.BAD_POSTURE)
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
        /**
         * Ambang proporsi pelanggaran per siklus (T9): 1 sampel nakal di antara
         * 10 sampel baik bukan pelanggaran siklus.
         */
        const val VIOLATION_MIN_PERCENT = 30

        /** True bila ≥[VIOLATION_MIN_PERCENT]% dari [validCount] sampel melanggar. */
        fun isViolation(
            validCount: Int,
            violatingCount: Int,
        ): Boolean = validCount > 0 && violatingCount * 100 >= validCount * VIOLATION_MIN_PERCENT
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
