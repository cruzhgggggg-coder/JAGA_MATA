package com.jagamata.app.data

import com.jagamata.app.data.entity.CheckSnapshot
import java.util.Calendar
import java.util.TimeZone

/**
 * Satu rumus statistik dengan jendela kalender eksplisit (M10).
 *
 * Sebelumnya label yang sama memakai rumus berbeda (Dashboard: 1 siklus terakhir,
 * Beranda/Statistik: `getAll()` sepanjang masa). Setiap metrik di bawah ini
 * menyebut jendelanya secara eksplisit dalam hari kalender lokal (termasuk hari ini).
 *
 * Jendela selalu dihitung dari [localDayEpoch]/[localDayStartMs] (zona lokal),
 * bukan `ms / 86_400_000` (M8).
 */
class StatsRepository(
    private val database: AppDatabase,
    private val zone: TimeZone = TimeZone.getDefault(),
) {
    data class WeeklyCounts(
        val closeViolations: Int,
        val darkMinutes: Int,
        val postureViolations: Int,
    )

    // ------------------------------------------------------------------ jarak

    /**
     * Rata-rata jarak snapshot valid (`avgDistanceCm > 0`) pada [days] hari
     * kalender lokal terakhir — hari ini disertakan. null = belum ada data.
     */
    suspend fun avgDistanceCm(
        days: Int = 7,
        nowMs: Long = System.currentTimeMillis(),
    ): Float? {
        val distances = windowSnapshots(days, nowMs).mapNotNull { it.avgDistanceCm }.filter { it > 0f }
        return distances.takeIf { it.isNotEmpty() }?.average()?.toFloat()
    }

    /** Rata-rata jarak HARI INI (label "Jarak rata-rata" Beranda & Statistik). */
    suspend fun avgDistanceCmToday(nowMs: Long = System.currentTimeMillis()): Float? = avgDistanceCm(days = 1, nowMs = nowMs)

    /** Rata-rata jarak 7 hari kalender terakhir (label "Jarak rata-rata 7 hari"). */
    suspend fun avgDistance7DaysCm(nowMs: Long = System.currentTimeMillis()): Float? = avgDistanceCm(days = 7, nowMs = nowMs)

    /**
     * Seri rata-rata jarak per hari kalender untuk [days] hari terakhir
     * (berakhir hari ini). Hari tanpa data = null (celah grafik, T14).
     */
    suspend fun avgDistanceSeries(
        days: Int = 7,
        nowMs: Long = System.currentTimeMillis(),
    ): List<Float?> {
        val endDay = localDayEpoch(nowMs, zone)
        val rows = windowSnapshots(days, nowMs)
        val byDay =
            rows
                .filter { (it.avgDistanceCm ?: 0f) > 0f }
                .groupBy { localDayEpoch(it.timestampMs, zone) }
        return localDaysEndingAt(endDay, days).map { day ->
            byDay[day]
                ?.mapNotNull { it.avgDistanceCm }
                ?.takeIf { it.isNotEmpty() }
                ?.average()?.toFloat()
        }
    }

    // ------------------------------------------------------------- hari ini

    /** Jumlah siklus hari ini yang memuat ≥1 pelanggaran. */
    suspend fun violationsToday(nowMs: Long = System.currentTimeMillis()): Int =
        windowSnapshots(1, nowMs).count { it.violations.isNotEmpty() }

    /**
     * Waktu main hari ini (menit): jumlah `cycleDurationMs` siklus hari ini;
     * bila semua durasi 0 (baris lama), jatuh kembali ke jumlah siklus (≈1 menit/siklus).
     */
    suspend fun playTimeMinutesToday(nowMs: Long = System.currentTimeMillis()): Int = minutesOf(windowSnapshots(1, nowMs))

    /** Menit hari ini tanpa pelanggaran (kartu "Durasi aman"). */
    suspend fun safeMinutesToday(nowMs: Long = System.currentTimeMillis()): Int =
        minutesOf(windowSnapshots(1, nowMs).filter { it.violations.isEmpty() })

    /** Persentase siklus hari ini tanpa pelanggaran; null bila belum ada data. */
    suspend fun safeSharePercentToday(nowMs: Long = System.currentTimeMillis()): Int? {
        val rows = windowSnapshots(1, nowMs)
        if (rows.isEmpty()) return null
        return rows.count { it.violations.isEmpty() } * 100 / rows.size
    }

    /**
     * Rata-rata jarak per jam hari ini (0..23, zona lokal) — grafik "Jarak per jam"
     * (T13). Hanya jam yang punya sampel berjarak valid yang muncul.
     */
    suspend fun hourlyAvgDistanceToday(nowMs: Long = System.currentTimeMillis()): Map<Int, Float> =
        windowSnapshots(1, nowMs)
            .filter { (it.avgDistanceCm ?: 0f) > 0f }
            .groupBy { hourOfDay(it.timestampMs, zone) }
            .mapValues { (_, rows) -> rows.mapNotNull { it.avgDistanceCm }.average().toFloat() }

    // ---------------------------------------------------------------- streak

    /**
     * Streak hari berturut-turut dengan ≥1 siklus valid (`sampleCount > 0`)
     * (M9). Berakhir di hari ini; bila hari ini belum ada data, streak dihitung
     * dari kemarin (hari belum selesai — streak tidak putus di pagi hari).
     * Mundur maksimal 30 hari (retensi data).
     */
    suspend fun consecutiveActiveDays(nowMs: Long = System.currentTimeMillis()): Int {
        val today = localDayEpoch(nowMs, zone)
        val earliest = today - (RETENTION_DAYS - 1)
        val rows = database.snapshotDao().getSince(localDayStartMs(earliest, zone))
        val active =
            rows
                .filter { it.sampleCount > 0 }
                .map { localDayEpoch(it.timestampMs, zone) }
                .toSet()
        var day = if (today in active) today else today - 1
        var streak = 0
        while (day in active && day >= earliest) {
            streak++
            day--
        }
        return streak
    }

    // --------------------------------------------------------------- pekan ini

    /**
     * Ringkasan pekan ini dari snapshot (jendela 7 hari kalender termasuk hari
     * ini). [luxThreshold] ambang aktif dari pengaturan (T9).
     */
    suspend fun weeklyCounts(
        luxThreshold: Float,
        nowMs: Long = System.currentTimeMillis(),
    ): WeeklyCounts {
        val rows = windowSnapshots(7, nowMs)
        return WeeklyCounts(
            closeViolations = rows.count { it.violations.contains("TOO_CLOSE") },
            darkMinutes = rows.count { (it.avgLux ?: Float.MAX_VALUE) < luxThreshold },
            postureViolations = rows.count { it.violations.contains("BAD_POSTURE") },
        )
    }

    // ------------------------------------------------------------- internals

    /** Snapshot pada rentang `[awal hari (days-1) .. akhir hari ini)` waktu lokal. */
    private suspend fun windowSnapshots(
        days: Int,
        nowMs: Long,
    ): List<CheckSnapshot> {
        val endDay = localDayEpoch(nowMs, zone)
        val startMs = localDayStartMs(endDay - (days - 1), zone)
        val endMs = localDayEndMs(endDay, zone)
        return database.snapshotDao().getSince(startMs).filter { it.timestampMs < endMs }
    }

    private fun minutesOf(rows: List<CheckSnapshot>): Int {
        val sumMs = rows.sumOf { it.cycleDurationMs }
        return if (sumMs > 0) (sumMs / 60_000L).toInt() else rows.size
    }

    companion object {
        /** Retensi data = 30 hari (PRD §2.9) — batas pencarian streak. */
        const val RETENTION_DAYS = 30

        /** Jam hari lokal (0..23) untuk [epochMs] pada zona [zone]. */
        fun hourOfDay(
            epochMs: Long,
            zone: TimeZone = TimeZone.getDefault(),
        ): Int = Calendar.getInstance(zone).apply { timeInMillis = epochMs }.get(Calendar.HOUR_OF_DAY)
    }
}
