package com.jagamata.app.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jagamata.app.data.AppDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Rollup harian `daily_stats` + prune data >30 hari (PRD §2.9, DEV-17).
 * Dijadwalkan tiap hari sekitar 00:05 waktu lokal.
 */
class DailyRollupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val now = System.currentTimeMillis()

        val cal =
            Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        val dayStart = cal.timeInMillis
        val dayEpoch = dayStart / DAY_MS
        val dayEnd = dayStart + DAY_MS

        val aggregate = database.dailyDao().aggregateCycles(dayStart, dayEnd)
        val darkMinutes =
            database.snapshotDao().getSince(dayStart)
                .count { (it.avgLux ?: Float.MAX_VALUE) < DARK_LUX }

        database.dailyDao().upsert(
            com.jagamata.app.data.entity.DailyStats(
                dayEpoch = dayEpoch,
                closeViolations = aggregate.closeViolations,
                darkMinutes = darkMinutes,
                postureViolations = aggregate.postureViolations,
                totalCycles = aggregate.totalCycles,
            ),
        )

        val cutoff = now - RETENTION_DAYS * DAY_MS
        database.snapshotDao().deleteOlderThan(cutoff)
        database.violationDao().deleteOlderThan(cutoff)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "jagamata_daily_rollup"
        private const val DAY_MS = 86_400_000L
        private const val RETENTION_DAYS = 30L
        private const val DARK_LUX = 150f

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
