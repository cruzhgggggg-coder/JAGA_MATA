package com.jagamata.app.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regresi audit M18: "menit kosong dihitung siklus" — siklus tanpa satu pun
 * wajah (termasuk siklus 0 sampel saat layar mati) tidak boleh menghasilkan
 * baris snapshot. Aturan murni diuji tanpa Android runtime.
 */
class EmptyCycleSkipTest {
    @Test
    fun `siklus tanpa sampel dilewati`() {
        assertTrue(DetectionForegroundService.shouldSkipCycle(sampleCount = 0, noFaceCount = 0))
    }

    @Test
    fun `siklus dengan semua sampel tanpa wajah dilewati`() {
        assertTrue(DetectionForegroundService.shouldSkipCycle(sampleCount = 5, noFaceCount = 5))
    }

    @Test
    fun `siklus dengan sebagian wajah tetap disimpan`() {
        assertFalse(DetectionForegroundService.shouldSkipCycle(sampleCount = 5, noFaceCount = 3))
    }

    @Test
    fun `siklus dengan semua wajah terdeteksi tetap disimpan`() {
        assertFalse(DetectionForegroundService.shouldSkipCycle(sampleCount = 5, noFaceCount = 0))
    }
}
