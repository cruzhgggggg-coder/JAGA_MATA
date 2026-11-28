package com.jagamata.app.service

import com.jagamata.app.data.entity.ViolationLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PauseSessionManagerTest {
    private val recordedLogs = mutableListOf<ViolationLog>()
    private var notifyCount = 0
    private var stopCount = 0
    private var startCount = 0

    private fun manager(
        scope: kotlinx.coroutines.CoroutineScope,
        autoResumeDelayMs: Long = PauseSessionManager.AUTO_RESUME_DELAY_MS,
    ) = PauseSessionManager(
        scope = scope,
        recordPause = { recordedLogs.add(it) },
        notifyParent = { notifyCount++ },
        stopMonitoring = { stopCount++ },
        startMonitoring = { startCount++ },
        clock = { FIXED_CLOCK_MS },
        autoResumeDelayMs = autoResumeDelayMs,
    )

    @Test
    fun `pause mencatat ViolationLog dengan type PAUSED_BY_CHILD`() =
        runTest {
            val mgr = manager(this)
            mgr.pause()

            assertEquals(1, recordedLogs.size)
            val log = recordedLogs.first()
            assertEquals(PauseSessionManager.TYPE_PAUSED_BY_CHILD, log.type)
            assertEquals(FIXED_CLOCK_MS, log.timestampMs)
            assertNull(log.distanceCm)
            assertNull(log.lux)
            assertNull(log.pitchDeg)
        }

    @Test
    fun `pause memanggil notifyParent dan stopMonitoring`() =
        runTest {
            val mgr = manager(this)
            mgr.pause()

            assertEquals(1, notifyCount)
            assertEquals(1, stopCount)
        }

    @Test
    fun `auto-resume setelah 5 menit memanggil startMonitoring`() =
        runTest {
            val mgr = manager(this)
            mgr.pause()
            assertEquals(0, startCount)

            advanceTimeBy(PauseSessionManager.AUTO_RESUME_DELAY_MS)
            runCurrent()

            assertEquals(1, startCount)
        }

    @Test
    fun `resume membatalkan auto-resume yang tertunda`() =
        runTest {
            val mgr = manager(this)
            mgr.pause()
            mgr.resume()
            assertEquals(1, startCount)

            advanceTimeBy(PauseSessionManager.AUTO_RESUME_DELAY_MS)
            runCurrent()

            // startMonitoring hanya dipanggil sekali oleh resume(), bukan oleh auto-resume
            assertEquals(1, startCount)
        }

    companion object {
        private const val FIXED_CLOCK_MS = 1_700_000_000_000L
    }
}
