package com.jagamata.app.detection

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regresi audit menyeluruh:
 * - M2: banyak wajah di frame -> pilih wajah terdekat (bounding box terbesar).
 * - D17: [ModelPitchBackend.toPixelBox] tidak memangkas kotak ke tepi frame,
 *   sehingga tinggi wajah tetap utuh untuk estimasi jarak.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FaceSelectionTest {
    private fun face(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
    ): List<LandmarkPoint> =
        listOf(
            LandmarkPoint(x0, y0, 0f),
            LandmarkPoint(x1, y1, 0f),
        )

    // ------------------------------------------------------------------ M2

    @Test
    fun `pickClosestFace memilih wajah dengan bbox terbesar`() {
        val kecil = face(0.1f, 0.1f, 0.2f, 0.2f) // luas 0.01
        val besar = face(0.3f, 0.3f, 0.8f, 0.9f) // luas 0.30
        assertEquals(besar, FaceDetectorManager.pickClosestFace(listOf(kecil, besar)))
    }

    @Test
    fun `pickClosestFace tidak tergantung urutan kandidat`() {
        val kecil = face(0.1f, 0.1f, 0.2f, 0.2f)
        val besar = face(0.3f, 0.3f, 0.8f, 0.9f)
        assertEquals(besar, FaceDetectorManager.pickClosestFace(listOf(besar, kecil)))
    }

    @Test
    fun `pickClosestFace mengembalikan null untuk daftar kosong`() {
        assertNull(FaceDetectorManager.pickClosestFace(emptyList()))
    }

    @Test
    fun `pickClosestFace dengan satu kandidat mengembalikan kandidat itu`() {
        val satu = face(0.2f, 0.2f, 0.5f, 0.6f)
        assertEquals(satu, FaceDetectorManager.pickClosestFace(listOf(satu)))
    }

    @Test
    fun `pickLargestBox memilih kotak dengan luas terbesar`() {
        val boxes =
            listOf(
                Rect(0, 0, 10, 10),
                Rect(0, 0, 50, 40),
                Rect(5, 5, 30, 30),
            )
        assertEquals(Rect(0, 0, 50, 40), FaceDetectorManager.pickLargestBox(boxes))
    }

    @Test
    fun `pickLargestBox mengembalikan null untuk daftar kosong`() {
        assertNull(FaceDetectorManager.pickLargestBox(emptyList()))
    }

    // ------------------------------------------------------------------ D17

    @Test
    fun `toPixelBox tidak memangkas kotak yang sebagian keluar frame`() {
        // Wajah terpotong tepi kiri-atas: landmark di luar rentang [0,1].
        val points =
            listOf(
                LandmarkPoint(-0.10f, -0.05f, 0f),
                LandmarkPoint(0.40f, 0.35f, 0f),
            )
        val box = ModelPitchBackend.toPixelBox(points, frameWidth = 400, frameHeight = 800)
        assertEquals(-40, box.left)
        assertEquals(-40, box.top)
        assertEquals(160, box.right)
        assertEquals(280, box.bottom)
        // Tinggi wajah utuh (0.40 * 800 = 320) — bukan terpangkas ke tepi frame.
        assertEquals(320, box.height())
        assertEquals(200, box.width())
    }

    @Test
    fun `toPixelBox tetap sama untuk wajah sepenuhnya di dalam frame`() {
        val points =
            listOf(
                LandmarkPoint(0.25f, 0.40f, 0f),
                LandmarkPoint(0.75f, 0.80f, 0f),
            )
        val box = ModelPitchBackend.toPixelBox(points, frameWidth = 400, frameHeight = 800)
        assertEquals(100, box.left)
        assertEquals(320, box.top)
        assertEquals(300, box.right)
        assertEquals(640, box.bottom)
    }
}
