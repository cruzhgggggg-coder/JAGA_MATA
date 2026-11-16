package com.jagamata.app.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleAggregatorTest {
    private fun sample(
        ts: Long = 0,
        distance: Float? = 50f,
        lux: Float? = 300f,
        pitch: Float? = 5f,
    ) = DetectionResult(
        timestampMs = ts,
        faceHeightPx = distance?.let { 2000f / it },
        pitchDeg = pitch,
        lux = lux,
        distanceCm = distance,
    )

    @Test
    fun `min avg max jarak dihitung benar`() {
        val agg = CycleAggregator()
        agg.add(sample(distance = 20f))
        agg.add(sample(distance = 40f))
        agg.add(sample(distance = 60f))
        val summary = agg.evaluate()
        assertEquals(20f, summary.minDistanceCm!!, 0.01f)
        assertEquals(60f, summary.maxDistanceCm!!, 0.01f)
        assertEquals(40f, summary.avgDistanceCm!!, 0.01f)
        assertEquals(3, summary.sampleCount)
    }

    @Test
    fun `tanpa wajah dihitung terpisah`() {
        val agg = CycleAggregator()
        agg.add(sample(distance = 50f))
        agg.add(sample(distance = null))
        agg.add(sample(distance = null))
        val summary = agg.evaluate()
        assertEquals(3, summary.sampleCount)
        assertEquals(2, summary.noFaceCount)
    }

    @Test
    fun `violations terdeteksi dari sampel`() {
        val agg = CycleAggregator()
        agg.add(sample(distance = 25f, lux = 300f, pitch = 5f))
        agg.add(sample(distance = 60f, lux = 100f, pitch = 45f))
        val summary = agg.evaluate()
        assertEquals(
            listOf(ViolationType.TOO_CLOSE, ViolationType.TOO_DARK, ViolationType.BAD_POSTURE),
            summary.violations.sortedBy { it.ordinal },
        )
        assertTrue(summary.hasViolation)
    }

    @Test
    fun `tidak ada violations saat semua aman`() {
        val agg = CycleAggregator()
        agg.add(sample(distance = 60f, lux = 300f, pitch = 5f))
        val summary = agg.evaluate()
        assertFalse(summary.hasViolation)
        assertEquals("", summary.violationsCsv())
    }

    @Test
    fun `evaluate membersihkan sampel berikutnya`() {
        val agg = CycleAggregator()
        agg.add(sample(distance = 25f))
        val first = agg.evaluate()
        assertEquals(1, first.sampleCount)
        val second = agg.evaluate()
        assertEquals(0, second.sampleCount)
        assertNull(second.minDistanceCm)
    }

    @Test
    fun `cycleDurationMs dan startedAtMs tercatat`() {
        val agg = CycleAggregator()
        agg.add(sample(ts = 1000))
        val summary = agg.evaluate(startedAtMs = 1000, cycleDurationMs = 60_000)
        assertEquals(1000, summary.startedAtMs)
        assertEquals(60_000, summary.cycleDurationMs)
    }
}
