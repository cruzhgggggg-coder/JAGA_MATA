package com.jagamata.app.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jagamata.app.data.AppDatabase
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.data.entity.DailyStats
import com.jagamata.app.data.localDayEndMs
import com.jagamata.app.data.localDayEpoch
import com.jagamata.app.data.localDayStartMs
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Rollup harian `daily_stats` + prune data >30 hari (PRD §2.9, DEV-17).
 * Dijadwalkan tiap hari sekitar 00:05 waktu lokal.
 *
 * Perbaikan audit:
 * - M8: kunci `dayEpoch` = hari lokal (`localDayEpoch`), bukan `ms / 86.400.000` UTC.
 * - M9: hari tanpa satu pun siklus TIDAK ditulis (baris 0-siklus membuat
 *   PDF/streak palsu). Hari yang sudah punya baris tidak dihapus.
 * - M11: backfill — hari yang terlewat (worker mati / device off) di-rollup
 *   ulang dari baris terakhir sampai kemarin, maksimal [MAX_BACKFILL_DAYS].
 * - T9: ambang gelap memakai ambang lux aktif dari [SettingsRepository].
 */
class DailyRollupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val now = System.currentTimeMillis()
        val zone = TimeZone.getDefault()

        val luxThreshold =
            runCatching { SettingsRepository(applicationContext).luxThreshold.first() }
                .getOrDefault(SettingsRepository.DEFAULT_LUX)

        // M11: hari yang belum pernah di-rollup (atau terlewat) ikut diproses.
        val lastRolled = database.dailyDao().getAll().maxOfOrNull { it.dayEpoch }
        val range = backfillDayRange(lastRolled, now, zone)

        if (range != null) {
            val windowStart = localDayStartMs(range.first, zone)
            val snapshots = database.snapshotDao().getSince(windowStart)
            for (dayEpoch in range) {
                // M8: jendela = 00:00 lokal s/d 00:00 lokal hari berikutnya.
                val dayStart = localDayStartMs(dayEpoch, zone)
                val dayEnd = localDayEndMs(dayEpoch, zone)

                val aggregate = database.dailyDao().aggregateCycles(dayStart, dayEnd)
                // M9: 0 siklus → jangan tulis baris kosong (menghindari
                // "7 hari Aman" tanpa pemantauan & inflasi streak).
                if (aggregate.totalCycles == 0) continue

                val darkMinutes =
                    snapshots.count {
                        it.timestampMs >= dayStart &&
                            it.timestampMs < dayEnd &&
                            (it.avgLux ?: Float.MAX_VALUE) < luxThreshold
                    }

                database.dailyDao().upsert(
                    DailyStats(
                        dayEpoch = dayEpoch,
                        closeViolations = aggregate.closeViolations,
                        darkMinutes = darkMinutes,
                        postureViolations = aggregate.postureViolations,
                        totalCycles = aggregate.totalCycles,
                    ),
                )
            }
        }

        val cutoff = now - RETENTION_DAYS * DAY_MS
        database.snapshotDao().deleteOlderThan(cutoff)
        database.violationDao().deleteOlderThan(cutoff)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "jagamata_daily_rollup"
        private const val DAY_MS = 86_400_000L
        private const val RETENTION_DAYS = 30L

        /** Backfill default saat belum ada baris sama sekali: 7 hari terakhir. */
        const val DEFAULT_BACKFILL_DAYS = 7

        /** Batas backfill mengikuti retensi data 30 hari. */
        const val MAX_BACKFILL_DAYS = 30

        /**
         * Rentang hari (epoch-day lokal) yang harus di-rollup, tertutup
         * `start..kemarin`; null bila tidak ada hari yang perlu diproses.
         *
         * - Belum ada baris → [DEFAULT_BACKFILL_DAYS] hari terakhir s/d kemarin.
         * - Sudah ada baris → dari baris terakhir + 1 s/d kemarin, dibatasi
         *   mundur [MAX_BACKFILL_DAYS] hari.
         * - `nowMs` selalu dihitung terhadap zona lokal [zone] (M8).
         */
        internal fun backfillDayRange(
            lastRolledDayEpoch: Long?,
            nowMs: Long,
            zone: TimeZone,
        ): LongRange? {
            val yesterday = localDayEpoch(nowMs, zone) - 1
            val earliest = yesterday - (MAX_BACKFILL_DAYS - 1)
            val start =
                if (lastRolledDayEpoch == null) {
                    maxOf(yesterday - DEFAULT_BACKFILL_DAYS + 1, earliest)
                } else {
                    maxOf(lastRolledDayEpoch + 1, earliest)
                }
            if (start > yesterday) return null
            return start..yesterday
        }

        fun schedule(context: Context) {
            val request =
                PeriodicWorkRequestBuilder<DailyRollupWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(initialDelayMs(), TimeUnit.MILLISECONDS)
                    .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        /** Delay sampai jam 00:05 berikutnya. */
        fun initialDelayMs(nowMs: Long = System.currentTimeMillis()): Long {
            val cal =
                Calendar.getInstance().apply {
                    timeInMillis = nowMs
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 5)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= nowMs) add(Calendar.DAY_OF_YEAR, 1)
                }
            return cal.timeInMillis - nowMs
        }
    }
}
