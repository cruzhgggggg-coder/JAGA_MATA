package com.jagamata.app.service

import android.util.Log
import com.jagamata.app.data.entity.ViolationLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mengelola sesi jeda pemantauan: mencatat pelanggaran, notifikasi orang tua,
 * dan auto-resume setelah [autoResumeDelayMs]. Scope harus process-lifetime
 * agar timer auto-resume tetap hidup saat navigasi.
 */
class PauseSessionManager(
    private val scope: CoroutineScope,
    private val recordPause: (ViolationLog) -> Unit,
    private val notifyParent: () -> Unit,
    private val stopMonitoring: () -> Unit,
    private val startMonitoring: () -> Unit,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val autoResumeDelayMs: Long = AUTO_RESUME_DELAY_MS,
) {
    private var autoResumeJob: Job? = null

    /** Jeda pemantauan: catat pelanggaran, notifikasi orang tua, stop service, jadwalkan auto-resume. */
    fun pause() {
        recordPause(
            ViolationLog(
                timestampMs = clock(),
                type = TYPE_PAUSED_BY_CHILD,
                distanceCm = null,
                lux = null,
                pitchDeg = null,
            ),
        )
        notifyParent()
        stopMonitoring()
        autoResumeJob?.cancel()
        autoResumeJob =
            scope.launch {
                delay(autoResumeDelayMs)
                Log.d(TAG, "Auto-resume triggered after ${autoResumeDelayMs}ms pause window")
                startMonitoring()
            }
    }

    /** Lanjutkan pemantauan segera dan batalkan auto-resume yang tertunda. */
    fun resume() {
        autoResumeJob?.cancel()
        autoResumeJob = null
        startMonitoring()
    }

    companion object {
        private const val TAG = "PauseSession"
        const val TYPE_PAUSED_BY_CHILD = "PAUSED_BY_CHILD"
        const val AUTO_RESUME_DELAY_MS = 5 * 60_000L
    }
}
