package com.jagamata.app.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalibrationManagerTest {
    @Test
    fun `f_px = h_px kali 30 dibagi 15`() {
        // h_px_kalib = 120 -> f_px = 120 * 30 / 15 = 240
        assertEquals(240f, CalibrationManager.focalPxFromCalibration(120f), 0.01f)
        assertEquals(200f, CalibrationManager.focalPxFromCalibration(100f), 0.01f)
        assertEquals(2f, CalibrationManager.targetRatio(), 0.0001f)
    }

    @Test
    fun `balik rumus konsisten - faceHeightPx for distance`() {
        val focal = CalibrationManager.focalPxFromCalibration(120f)
        // Pada 30 cm, tinggi wajah harus kembali 120 px
        val hPx = CalibrationManager.faceHeightPxFor(distanceCm = 30f, focalPx = focal)
        assertEquals(120f, hPx, 0.01f)
    }

    @Test
    fun `kalibrasi stabil setelah 1500 ms sampel konsisten`() {
        val manager = CalibrationManager()
        assertNull(manager.addSample(120f, 0L))
        assertNull(manager.addSample(121f, 500L))
        assertNull(manager.addSample(119f, 1000L))
        val fPx = manager.addSample(120f, 1500L)
        assertEquals(240f, fPx!!, 0.01f)
        assertTrue(manager.isStable())
    }

    @Test
    fun `kalibrasi tidak stabil bila sampel bervariasi`() {
        val manager = CalibrationManager(stabilityTolerancePx = 8f)
        manager.addSample(100f, 0L)
        manager.addSample(140f, 700L)
        manager.addSample(105f, 1400L)
        manager.addSample(160f, 1500L)
        assertFalse(manager.isStable())
    }

    @Test
    fun `reset mengosongkan sampel`() {
        val manager = CalibrationManager()
        manager.addSample(120f, 0L)
        manager.addSample(120f, 1500L)
        assertTrue(manager.isStable())
        manager.reset()
        assertFalse(manager.isStable())
    }
}
