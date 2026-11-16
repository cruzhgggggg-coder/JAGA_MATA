package com.jagamata.app.detection

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Size
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Konversi pipeline kamera (PRD DEV-14): bounding box landmark, rotasi
 * bitmap, ukuran analisis 480p.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageConvertersTest {
    @Test
    fun `bounding box landmark dihitung dari min max`() {
        val points =
            listOf(
                LandmarkPoint(0.10f, 0.20f, 0f),
                LandmarkPoint(0.50f, 0.40f, 0f),
                LandmarkPoint(0.30f, 0.80f, 0f),
            )
        val box: Rect = FaceDetectorManager.boundingBoxOf(points)
        assertEquals(0, box.left)
        assertEquals(0, box.top)
        assertEquals(0, box.right) // 0.50 -> int 0
        assertEquals(0, box.bottom) // 0.80 -> int 0
    }

    @Test
    fun `bounding box landmark dengan skala piksel`() {
        val points =
            listOf(
                LandmarkPoint(10f, 20f, 0f),
                LandmarkPoint(50f, 40f, 0f),
                LandmarkPoint(30f, 80f, 0f),
            )
        val box: Rect = FaceDetectorManager.boundingBoxOf(points)
        assertEquals(10, box.left)
        assertEquals(20, box.top)
        assertEquals(50, box.right)
        assertEquals(80, box.bottom)
    }

    @Test
    fun `bounding box kosong tidak crash`() {
        val box = FaceDetectorManager.boundingBoxOf(emptyList())
        assertEquals(Rect(0, 0, 0, 0), box)
    }

    @Test
    fun `rotasi 90 derajat menukar dimensi`() {
        val bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val rotated = ImageConverters.rotateBitmap(bitmap, 90)
        assertEquals(100, rotated.width)
        assertEquals(200, rotated.height)
    }

    @Test
    fun `rotasi 0 derajat mempertahankan dimensi`() {
        val bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val rotated = ImageConverters.rotateBitmap(bitmap, 0)
        assertEquals(200, rotated.width)
        assertEquals(100, rotated.height)
    }

    @Test
    fun `ukuran analisis memakai 480p`() {
        val size: Size = ImageConverters.targetAnalysisSize()
        assertEquals(480, size.width)
        assertEquals(480, size.height)
    }

    @Test
    fun `faceHeightPx dari kotak nol menghasilkan null`() {
        assertNull(DistanceEstimator.faceHeightPxFromBox(0f))
        assertNotNull(DistanceEstimator.faceHeightPxFromBox(120f))
        assertEquals(120f, DistanceEstimator.faceHeightPxFromBox(120f)!!, 0.001f)
    }

    @Test
    fun `cek keberadaan model mediapipe tidak crash`() {
        // assets/face_landmarker.task BELUM dipasang (lihat MODEL_README.txt),
        // sehingga FaceDetectorManager jatuh ke ML Kit (fallback F1).
        val exists = FaceDetectorManager.modelExists(ApplicationProvider.getApplicationContext())
        org.junit.Assert.assertFalse(
            "Model belum dipasang - kode harus siap degraded mode (F1)",
            exists,
        )
    }
}
