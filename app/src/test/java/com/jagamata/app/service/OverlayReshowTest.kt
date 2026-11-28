package com.jagamata.app.service

import com.jagamata.app.detection.RiskLevel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regresi K1 (audit menyeluruh 28 Nov 2026): overlay BLOCKED harus tetap tampil
 * selama kondisi berlangsung, bukan sekali 5 detik lalu hilang selamanya.
 */
class OverlayReshowTest {
    private class Sim {
        companion object {
            const val FRAME_MS = 200L
            const val AUTO_DISMISS_MS = 5_000L
        }

        var lastSendAtMs = Long.MIN_VALUE / 2
        var quietUntilMs = 0L
        var showing = false
        var hideAtMs = Long.MIN_VALUE / 2
        var sends = 0
        var visibleMs = 0L

        fun tick(
            now: Long,
            level: RiskLevel,
            prev: RiskLevel,
        ) {
            if (showing && now >= hideAtMs) showing = false
            val send =
                OverlayService.shouldResendIntent(prev, level, showing, now, lastSendAtMs, quietUntilMs)
            if (send) {
                sends++
                lastSendAtMs = now
                showing = true
                hideAtMs = now + AUTO_DISMISS_MS
            }
            if (showing) visibleMs += FRAME_MS
        }
    }

    @Test
    fun `BLOCKED 60 detik overlay terlihat hampir sepanjang waktu`() {
        val sim = Sim()
        var prev = RiskLevel.OK
        var now = 0L
        while (now < 60_000L) {
            sim.tick(now, RiskLevel.BLOCKED, prev)
            prev = RiskLevel.BLOCKED
            now += Sim.FRAME_MS
        }
        val visibilityPct = sim.visibleMs * 100 / 60_000L
        assertTrue("visibilitas $visibilityPct% harus >= 90%", visibilityPct >= 90)
        assertTrue("overlay harus tampil ulang berkali-kali (sends=${sim.sends})", sim.sends >= 5)
    }

    @Test
    fun `level sama saat overlay masih tampil tidak mengirim ulang`() {
        assertFalse(
            OverlayService.shouldResendIntent(
                prev = RiskLevel.BLOCKED,
                level = RiskLevel.BLOCKED,
                overlayShowing = true,
                nowMs = 60_000L,
                lastSendAtMs = 0L,
                quietUntilMs = 0L,
            ),
        )
    }

    @Test
    fun `perubahan level selalu dikirim`() {
        assertTrue(
            OverlayService.shouldResendIntent(
                prev = RiskLevel.WARNING,
                level = RiskLevel.BLOCKED,
                overlayShowing = true,
                nowMs = 10L,
                lastSendAtMs = 5L,
                quietUntilMs = 0L,
            ),
        )
    }

    @Test
    fun `OK dengan level sama tidak dikirim ulang`() {
        assertFalse(
            OverlayService.shouldResendIntent(
                prev = RiskLevel.OK,
                level = RiskLevel.OK,
                overlayShowing = false,
                nowMs = 999_999L,
                lastSendAtMs = 0L,
                quietUntilMs = 0L,
            ),
        )
    }

    @Test
    fun `dismiss manual memberi jeda singkat sebelum tampil lagi`() {
        // quiet 3 detik: pada detik pertama setelah dismiss tidak boleh kirim ulang.
        val quietUntil = 3_000L
        assertFalse(
            OverlayService.shouldResendIntent(
                prev = RiskLevel.BLOCKED,
                level = RiskLevel.BLOCKED,
                overlayShowing = false,
                nowMs = 1_000L,
                lastSendAtMs = 0L,
                quietUntilMs = quietUntil,
            ),
        )
        assertTrue(
            OverlayService.shouldResendIntent(
                prev = RiskLevel.BLOCKED,
                level = RiskLevel.BLOCKED,
                overlayShowing = false,
                nowMs = 4_000L,
                lastSendAtMs = 0L,
                quietUntilMs = quietUntil,
            ),
        )
    }
}
