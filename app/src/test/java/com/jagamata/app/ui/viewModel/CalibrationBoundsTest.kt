package com.jagamata.app.ui.viewModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit test batas wajar focal kalibrasi (T6c) — [CalibrationBounds] murni Kotlin. */
class CalibrationBoundsTest {
    // ——— rentang absolut 350–900 px ———

    @Test
    fun `focal dalam rentang 350-900 diterima`() {
        assertTrue(CalibrationBounds.isPlausible(350f, null))
        assertTrue(CalibrationBounds.isPlausible(560f, null))
        assertTrue(CalibrationBounds.isPlausible(900f, null))
    }

    @Test
    fun `focal di bawah 350 ditolak OUT_OF_RANGE`() {
        assertEquals(CalibrationBounds.Reject.OUT_OF_RANGE, CalibrationBounds.rejectReason(349f, null))
        assertEquals(CalibrationBounds.Reject.OUT_OF_RANGE, CalibrationBounds.rejectReason(150f, null))
    }

    @Test
    fun `focal di atas 900 ditolak OUT_OF_RANGE`() {
        assertEquals(CalibrationBounds.Reject.OUT_OF_RANGE, CalibrationBounds.rejectReason(901f, null))
        assertEquals(CalibrationBounds.Reject.OUT_OF_RANGE, CalibrationBounds.rejectReason(1200f, null))
    }

    // ——— deviasi > 30% dari focal sebelumnya ———

    @Test
    fun `tanpa focal sebelumnya hanya rentang yang dicek`() {
        assertTrue(CalibrationBounds.isPlausible(360f, null))
        assertNull(CalibrationBounds.rejectReason(890f, null))
    }

    @Test
    fun `focal sebelumnya nol dianggap belum ada`() {
        assertTrue(CalibrationBounds.isPlausible(360f, 0f))
    }

    @Test
    fun `deviasi lebih dari 30 persen ditolak`() {
        assertEquals(CalibrationBounds.Reject.DEVIATION, CalibrationBounds.rejectReason(700f, 500f))
        assertEquals(CalibrationBounds.Reject.DEVIATION, CalibrationBounds.rejectReason(500f, 800f))
    }

    @Test
    fun `deviasi tepat 30 persen diterima`() {
        assertTrue(CalibrationBounds.isPlausible(650f, 500f))
        assertTrue(CalibrationBounds.isPlausible(350f, 500f))
    }

    @Test
    fun `deviasi di bawah 30 persen diterima`() {
        assertTrue(CalibrationBounds.isPlausible(600f, 500f))
        assertTrue(CalibrationBounds.isPlausible(400f, 500f))
    }

    @Test
    fun `OUT_OF_RANGE didahulukan daripada DEVIATION`() {
        assertEquals(CalibrationBounds.Reject.OUT_OF_RANGE, CalibrationBounds.rejectReason(1000f, 500f))
    }

    @Test
    fun `focal wajar yang cocok dengan sebelumnya diterima`() {
        assertNull(CalibrationBounds.rejectReason(560f, 560f))
    }
}
