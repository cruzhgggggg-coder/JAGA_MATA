package com.jagamata.app.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceEstimatorTest {
    private val estimator = DistanceEstimator()

    /** Rumus PRD §2.7: d = (H × f_px) / h_px dengan H = 15 cm. */
    @Test
    fun `jarak dekat sesuai rumus`() {
        // f_px = 200 (h_px_kalib 100 × 30/15), h_px = 100 -> d = 15*200/100 = 30 cm
        val d = estimator.computeRawCm(faceHeightPx = 100f, focalPx = 200f)
        assertEquals(30f, d!!, 0.01f)
    }

    @Test
    fun `jarak sedang sesuai rumus`() {
        // h_px = 60 -> d = 15*200/60 = 50 cm
        val d = estimator.computeRawCm(faceHeightPx = 60f, focalPx = 200f)
        assertEquals(50f, d!!, 0.01f)
    }

    @Test
    fun `jarak jauh sesuai rumus`() {
        // h_px = 40 -> d = 15*200/40 = 75 cm
        val d = estimator.computeRawCm(faceHeightPx = 40f, focalPx = 200f)
        assertEquals(75f, d!!, 0.01f)
    }

    @Test
    fun `tanpa wajah menghasilkan null bukan nol`() {
        assertNull(estimator.computeRawCm(faceHeightPx = null, focalPx = 200f))
        assertNull(estimator.update(faceHeightPx = null, focalPx = 200f))
        assertNull(estimator.update(faceHeightPx = 0f, focalPx = 200f))
    }

    @Test
    fun `EMA menghaluskan perubahan nilai`() {
        val first = estimator.update(faceHeightPx = 100f, focalPx = 200f)!!
        assertEquals(30f, first, 0.01f)
        // h_px 80 -> raw 37,5; EMA = 0,4*37,5 + 0,6*30 = 33
        val second = estimator.update(faceHeightPx = 80f, focalPx = 200f)!!
        assertEquals(33f, second, 0.01f)
    }

    @Test
    fun `MAE rumus di bawah 6 cm terhadap ground truth sintetis`() {
        val focal = 200f
        // h_px = (15 x 200) / d  ->  d terharap dari rumus harus kembali
        val cases =
            listOf(
                // 15*200/150 = 20 cm
                20f to 150f,
                // 15*200/120 = 25 cm
                25f to 120f,
                // 15*200/100 = 30 cm
                30f to 100f,
                // 15*200/60  = 50 cm
                50f to 60f,
            )
        val errors =
            cases.map { (expectedCm, faceHeightPx) ->
                val predicted = estimator.computeRawCm(faceHeightPx, focal)!!
                Math.abs(predicted - expectedCm)
            }
        val mae = errors.average()
        assertTrue("MAE = $mae harus <= 6", mae <= 6.0)
    }
}
