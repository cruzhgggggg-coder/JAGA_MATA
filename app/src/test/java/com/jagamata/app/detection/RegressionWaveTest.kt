package com.jagamata.app.detection

import android.content.Context
import android.graphics.Bitmap
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

/**
 * Regresi audit menyeluruh: T11 (kalibrasi multi-fps), T10 (round-trip kalibrasi
 * ke runtime), M17 (thread-safety CycleAggregator), M6 (fallback ModelPitchBackend).
 * JVM murni tanpa emulator.
 *
 * Catatan cakupan:
 * - T7 / M3 (toleransi frame hilang pada state machine) ada di
 *   [DetectionStateMachineTest] skenario 15-17.
 * - Seam pemilihan backend (M6: `PitchBackendFactory.resolve` saat flag aktif
 *   tapi file model tidak ada) sudah tercakup di [PitchBackendFactoryTest]
 *   ("flag aktif tapi file tidak ada - tetap LandmarkPitchBackend").
 */
class RegressionWaveTest {
    // ------------------------------------------------------------------ T11

    private data class CalibOutcome(
        val focalPx: Float,
        val elapsedMs: Long,
    )

    /**
     * Stream kamera deterministik: tinggi wajah konstan [baseHeightPx] dengan
     * jitter ±[heightJitterPx], interval frame [fpsIntervalMs] dengan jitter
     * waktu ±[timestampJitterMs]. Mengembalikan f_px dan waktu simulasi saat
     * kalibrasi selesai, atau null bila tidak selesai dalam [maxSimulatedMs].
     */
    private fun runCalibrationStream(
        fpsIntervalMs: Long,
        timestampJitterMs: Int = 3,
        baseHeightPx: Float = 120f,
        heightJitterPx: Float = 2f,
        maxSimulatedMs: Long = 120_000L,
        seed: Int = 42,
    ): CalibOutcome? {
        val manager = CalibrationManager()
        val random = Random(seed)
        val maxFrames = (maxSimulatedMs / fpsIntervalMs + 10).toInt()
        var t = 0L
        repeat(maxFrames) {
            val height = baseHeightPx + random.nextFloat() * 2f * heightJitterPx - heightJitterPx
            val focal = manager.addSample(height, t)
            if (focal != null) return CalibOutcome(focal, t)
            t += fpsIntervalMs + random.nextInt(-timestampJitterMs, timestampJitterMs + 1)
        }
        return null
    }

    private fun assertCalibrationCompletes(
        label: String,
        fpsIntervalMs: Long,
    ) {
        val outcome = runCalibrationStream(fpsIntervalMs)
        assertNotNull("kalibrasi pada $label tidak selesai dalam 120 dtk simulasi", outcome)
        val result = outcome!!
        assertTrue(
            "kalibrasi $label selesai terlalu cepat (${result.elapsedMs} ms, " +
                "stability window ${CalibrationManager.STABILITY_MS} ms)",
            result.elapsedMs >= CalibrationManager.STABILITY_MS,
        )
        assertTrue(
            "kalibrasi $label terlalu lama (${result.elapsedMs} ms, harap ~1,5-2 dtk)",
            result.elapsedMs <= 3_000L,
        )
        // Tinggi acuan 120 px -> f_px = 120 x 30/15 = 240 (jitter +-2 px).
        assertEquals("f_px $label meleset", 240.0, result.focalPx.toDouble(), 5.0)
    }

    @Test
    fun `kalibrasi lolos pada 15 fps dengan jitter waktu dan tinggi wajah`() {
        // 15 fps = 66 ms/frame (ruangan redup) — kasus yang dulu 0-7% selesai.
        assertCalibrationCompletes("15 fps", fpsIntervalMs = 66L)
    }

    @Test
    fun `kalibrasi lolos pada 24 fps dengan jitter - regresi T11`() {
        // 24 fps = 42 ms/frame — kasus yang dulu TIDAK PERNAH selesai (T11).
        assertCalibrationCompletes("24 fps", fpsIntervalMs = 42L)
    }

    @Test
    fun `kalibrasi lolos pada 30 fps dengan jitter waktu dan tinggi wajah`() {
        // 30 fps = 33 ms/frame dengan jitter timestamp kecil.
        assertCalibrationCompletes("30 fps", fpsIntervalMs = 33L)
    }

    // ------------------------------------------------------------------ T10

    @Test
    fun `round-trip T10 - kalibrasi dan runtime tetap 30 cm pada pitch 0 10 20 25`() {
        val hRaw = 300f
        for (pitch in listOf(0f, 10f, 20f, 25f)) {
            // Kalibrasi memakai tinggi efektif yang sama dengan runtime (T10).
            val effectiveHeight = DistanceEstimator.effectiveFaceHeightPx(hRaw, pitch)
            val focal =
                CalibrationManager.focalPxFromCalibration(
                    effectiveHeight,
                    CalibrationManager.REFERENCE_FACE_HEIGHT_CM,
                )
            val distance =
                DistanceEstimator().update(
                    faceHeightPx = hRaw,
                    focalPx = focal,
                    pitchDeg = pitch,
                )
            assertNotNull("pitch $pitch - runtime tidak menghasilkan jarak", distance)
            assertEquals("pitch $pitch - jarak runtime harus 30 cm", 30.0, distance!!.toDouble(), 1.0)
        }
    }

    // ------------------------------------------------------------------ M17

    @Test
    fun `M17 - add paralel dan evaluate tidak melempar dan jumlah sampel konsisten`() {
        val agg = CycleAggregator()
        val errors = CopyOnWriteArrayList<Throwable>()
        val evaluatedCounts = CopyOnWriteArrayList<Int>()
        val totalAdds = 2_000

        val adder =
            Thread {
                try {
                    repeat(totalAdds) { i ->
                        agg.add(
                            DetectionResult(
                                timestampMs = i.toLong(),
                                distanceCm = 20f + (i % 5) * 10f,
                                lux = 300f,
                                pitchDeg = 5f,
                            ),
                        )
                    }
                } catch (t: Throwable) {
                    errors.add(t)
                }
            }
        val evaluator =
            Thread {
                try {
                    repeat(200) {
                        val summary = agg.evaluate()
                        evaluatedCounts.add(summary.sampleCount)
                        val min = summary.minDistanceCm
                        val avg = summary.avgDistanceCm
                        val max = summary.maxDistanceCm
                        if (min != null && avg != null && max != null) {
                            val ordered = min <= avg && avg <= max
                            if (!ordered) {
                                errors.add(AssertionError("min/avg/max tidak konsisten: $min / $avg / $max"))
                            }
                        }
                    }
                } catch (t: Throwable) {
                    errors.add(t)
                }
            }

        adder.start()
        evaluator.start()
        adder.join(1_000)
        evaluator.join(1_000)
        assertFalse("thread adder masih hidup", adder.isAlive)
        assertFalse("thread evaluator masih hidup", evaluator.isAlive)
        assertTrue("exception dari thread: $errors", errors.isEmpty())
        // Konservasi jumlah: tiap add masuk tepat satu snapshot evaluate atau tersisa.
        assertEquals(
            "total add harus sama dengan total evaluasi + sisa",
            totalAdds.toLong(),
            (evaluatedCounts.sum() + agg.sampleCount()).toLong(),
        )
    }

    // ------------------------------------------------------------------ M6

    private val sentinelBackend =
        object : PitchBackend {
            override fun pitchDeg(
                landmarks: List<LandmarkPoint>,
                faceCrop: Bitmap?,
            ): Float = 11.5f
        }

    @Test
    fun `M6 - tanpa face crop ModelPitchBackend memakai fallback`() {
        // Tanpa crop, model tidak bisa jalan — harus jatuh ke fallback (M6),
        // bukan null permanen. Context tidak pernah disentuh di jalur ini.
        val backend = ModelPitchBackend(context = mockk(), fallback = sentinelBackend)
        val result = backend.pitchDeg(listOf(LandmarkPoint(0.5f, 0.5f, 0f)), faceCrop = null)
        assertEquals(11.5f, result!!, 0.001f)
    }

    @Test
    fun `M6 - gagal memuat model ModelPitchBackend memakai fallback`() {
        // Model gagal dimuat (asset tidak ada) -> fallback LandmarkPitchBackend
        // yang diinjeksi, bukan null permanen (M6).
        val context = mockk<Context>()
        every { context.assets } throws IllegalStateException("model tidak tersedia")
        val backend = ModelPitchBackend(context = context, fallback = sentinelBackend)
        val crop = mockk<Bitmap>()
        val result = backend.pitchDeg(listOf(LandmarkPoint(0.5f, 0.5f, 0f)), faceCrop = crop)
        assertEquals(11.5f, result!!, 0.001f)
    }
}
