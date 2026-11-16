package com.jagamata.app.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class HeadPitchAnalyzerTest {
    /**
     * Fixture sintetis: vektor nose->chin dengan panjang L dan sudut pitch.
     * pitch = atan2(dz, dy) — positif = menunduk (US-07).
     */
    private fun fixtureLandmarks(
        pitchDeg: Float,
        length: Float = 0.12f,
    ): List<LandmarkPoint> {
        val rad = Math.toRadians(pitchDeg.toDouble())
        val dy = (cos(rad) * length).toFloat()
        val dz = (sin(rad) * length).toFloat()
        val points = MutableList(478) { LandmarkPoint(0.5f, 0.5f, 0f) }
        points[HeadPitchAnalyzer.NOSE_INDEX] = LandmarkPoint(0.5f, 0.50f, -0.02f)
        points[HeadPitchAnalyzer.CHIN_INDEX] = LandmarkPoint(0.5f, 0.50f + dy, -0.02f + dz)
        return points
    }

    @Test
    fun `fixture tegak 10 derajat`() {
        val pitch = HeadPitchAnalyzer.pitchDegFromLandmarks(fixtureLandmarks(10f))
        assertEquals(10f, pitch!!, 0.5f)
    }

    @Test
    fun `fixture tunduk 40 derajat`() {
        val pitch = HeadPitchAnalyzer.pitchDegFromLandmarks(fixtureLandmarks(40f))
        assertEquals(40f, pitch!!, 0.5f)
    }

    @Test
    fun `landmark kurang dari 153 menghasilkan null`() {
        assertNull(HeadPitchAnalyzer.pitchDegFromLandmarks(List(10) { LandmarkPoint(0f, 0f, 0f) }))
    }

    @Test
    fun `nose dan chin identik menghasilkan nol`() {
        val p = LandmarkPoint(0.5f, 0.5f, 0f)
        assertEquals(0f, HeadPitchAnalyzer.pitchDeg(p, p), 0.001f)
    }
}
