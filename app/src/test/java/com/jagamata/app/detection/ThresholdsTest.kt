package com.jagamata.app.detection

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * T9 + M25: satu sumber ambang/zona — pemetaan `exit = close+5`, `safe = close+20`
 * (default 30/35/50 = desain §5.4/§2.23) dan tidak ada definisi zona kedua.
 */
class ThresholdsTest {
    @Test
    fun `default menghasilkan angka desain 30-35-50-150-30`() {
        val t = Thresholds.DEFAULT
        assertEquals(30f, t.closeEnterCm, 0.01f)
        assertEquals(35f, t.closeExitCm, 0.01f)
        assertEquals(50f, t.safeZoneCm, 0.01f)
        assertEquals(150f, t.lux, 0.01f)
        assertEquals(30f, t.pitchDeg, 0.01f)
    }

    @Test
    fun `fromClose menurunkan exit dan safe dari ambang pengguna`() {
        val t = Thresholds.fromClose(closeEnterCm = 45f, lux = 200f, pitchDeg = 40f)
        assertEquals(45f, t.closeEnterCm, 0.01f)
        assertEquals(50f, t.closeExitCm, 0.01f)
        assertEquals(65f, t.safeZoneCm, 0.01f)
        assertEquals(200f, t.lux, 0.01f)
        assertEquals(40f, t.pitchDeg, 0.01f)
    }

    @Test
    fun `margin zona konsisten untuk semua ambang`() {
        for (close in listOf(20f, 30f, 37f, 50f)) {
            val t = Thresholds.fromClose(closeEnterCm = close)
            assertEquals(close + Thresholds.CLOSE_EXIT_MARGIN_CM, t.closeExitCm, 0.01f)
            assertEquals(close + Thresholds.SAFE_ZONE_MARGIN_CM, t.safeZoneCm, 0.01f)
            assertEquals(true, t.closeEnterCm < t.closeExitCm && t.closeExitCm < t.safeZoneCm)
        }
    }

    @Test
    fun `state machine menerima seluruh zona dari Thresholds`() {
        val machine = DetectionStateMachine()
        val t = Thresholds.fromClose(closeEnterCm = 45f, lux = 120f, pitchDeg = 35f)
        machine.updateThresholds(t)
        assertEquals(45f, machine.config.closeEnterCm, 0.01f)
        assertEquals(50f, machine.config.closeExitCm, 0.01f)
        assertEquals(65f, machine.config.safeZoneCm, 0.01f)
        assertEquals(120f, machine.config.luxThreshold, 0.01f)
        assertEquals(35f, machine.config.pitchThresholdDeg, 0.01f)
    }
}
