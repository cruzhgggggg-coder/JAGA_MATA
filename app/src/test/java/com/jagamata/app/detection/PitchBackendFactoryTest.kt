package com.jagamata.app.detection

import android.graphics.Bitmap
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PitchBackendFactoryTest {
    private val fakeTflite =
        object : PitchBackend {
            override fun pitchDeg(
                landmarks: List<LandmarkPoint>,
                faceCrop: Bitmap?,
            ): Float = 0f

            override fun requiresFaceCrop(): Boolean = true
        }

    @Test
    fun `tanpa file model - memakai LandmarkPitchBackend (F9)`() {
        val backend =
            PitchBackendFactory.resolve(
                useTfliteFlag = false,
                tfliteModelAvailable = false,
                tflite = fakeTflite,
            )
        assertTrue(backend is LandmarkPitchBackend)
    }

    @Test
    fun `flag aktif tapi file tidak ada - tetap LandmarkPitchBackend`() {
        val backend =
            PitchBackendFactory.resolve(
                useTfliteFlag = true,
                tfliteModelAvailable = false,
                tflite = fakeTflite,
            )
        assertTrue(backend is LandmarkPitchBackend)
    }

    @Test
    fun `flag aktif dan file ada - memakai TflitePitchBackend`() {
        val backend =
            PitchBackendFactory.resolve(
                useTfliteFlag = true,
                tfliteModelAvailable = true,
                tflite = fakeTflite,
            )
        assertSame(fakeTflite, backend)
    }

    @Test
    fun `implementasi tflite null - jatuh ke Landmark`() {
        val backend =
            PitchBackendFactory.resolve(
                useTfliteFlag = true,
                tfliteModelAvailable = true,
                tflite = null,
            )
        assertTrue(backend is LandmarkPitchBackend)
    }

    @Test
    fun `LandmarkPitchBackend memakai HeadPitchAnalyzer`() {
        val points = List(478) { LandmarkPoint(0.5f, 0.5f, 0f) }
        val pitch = LandmarkPitchBackend().pitchDeg(points, null)
        assertTrue(pitch != null && pitch == 0f)
    }
}
