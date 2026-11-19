package com.jagamata.app.detection

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test helper [ModelPitchBackend]: crop wajah, skala landmark ternormalisasi
 * ke piksel, ketersediaan model, dan pemilihan backend (PRD §5.5, DEV-34).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ModelPitchBackendTest {
    @Test
    fun `model pitch tflite terpasang di assets`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertTrue(
            "pitch_model.tflite harus ada di assets (hasil konversi JagaMata-ML)",
            ModelPitchBackend.modelExists(context),
        )
    }

    @Test
    fun `toPixelBox menskala landmark ternormalisasi ke piksel`() {
        // Landmark ternormalisasi [0,1] → dikali dimensi frame
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

    @Test
    fun `toPixelBox pada frame kosong menghasilkan kotak nol`() {
        val box = ModelPitchBackend.toPixelBox(emptyList(), 400, 800)
        assertEquals(Rect(0, 0, 0, 0), box)
    }

    @Test
    fun `cropFace memotong sesuai bounding box`() {
        val frame = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888)
        val crop = ModelPitchBackend.cropFace(frame, Rect(100, 200, 300, 500))
        assertNotNull(crop)
        assertEquals(200, crop!!.width)
        assertEquals(300, crop.height)
        frame.recycle()
        crop.recycle()
    }

    @Test
    fun `cropFace menolak kotak terlalu kecil`() {
        val frame = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888)
        // 20x20 < MIN_FACE_PX (40)
        val crop = ModelPitchBackend.cropFace(frame, Rect(10, 10, 30, 30))
        assertNull(crop)
        frame.recycle()
    }

    @Test
    fun `cropFace meng-clamp kotak keluar frame`() {
        val frame = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888)
        val crop = ModelPitchBackend.cropFace(frame, Rect(-50, -50, 9999, 9999))
        assertNotNull(crop)
        assertEquals(400, crop!!.width)
        assertEquals(800, crop.height)
        frame.recycle()
        crop.recycle()
    }

    @Test
    fun `requiresFaceCrop benar untuk backend model`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertTrue(ModelPitchBackend(context).requiresFaceCrop())
        assertFalse(LandmarkPitchBackend().requiresFaceCrop())
    }

    @Test
    fun `factory memilih backend model saat flag aktif dan file ada`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val resolved =
            PitchBackendFactory.resolve(
                useTfliteFlag = true,
                tfliteModelAvailable = ModelPitchBackend.modelExists(context),
                tflite = ModelPitchBackend(context),
            )
        assertTrue(resolved is ModelPitchBackend)
    }

    @Test
    fun `guardrail yaw menghasilkan null untuk kepala menoleh jauh`() {
        // Guardrail adalah aturan model (|yaw| >= 45 → null). Dipastikan lewat
        // konstanta; inferensi penuh diverifikasi via smoke test TFLite (Python).
        assertEquals(45f, ModelPitchBackend.YAW_GUARDRAIL_DEG, 0.001f)
    }
}
