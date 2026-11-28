package com.jagamata.app.detection

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Minimal 12 skenario transisi (PRD US-04): flapping, cepat-lewat, null face,
 * hysteresis 35 cm, debounce close 500 ms / pose 3 s / dark 10 s.
 */
class DetectionStateMachineTest {
    private class Harness {
        var now = 0L
        val machine = DetectionStateMachine()

        fun feed(
            distanceCm: Float? = null,
            pitchDeg: Float? = 0f,
            lux: Float? = 300f,
            advanceMs: Long = 0,
        ): RiskLevel {
            now += advanceMs
            return machine.onResult(
                DetectionResult(
                    timestampMs = now,
                    distanceCm = distanceCm,
                    pitchDeg = pitchDeg,
                    lux = lux,
                ),
            )
        }
    }

    @Test
    fun `01 kondisi aman tetap OK`() {
        val h = Harness()
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 80f))
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 80f, advanceMs = 2500))
    }

    @Test
    fun `02 dekat 500 ms langsung BLOCKED - latensi K5 di bawah 1500 ms`() {
        val h = Harness()
        h.feed(distanceCm = 25f)
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 25f, advanceMs = 200))
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = 25f, advanceMs = 350))
    }

    @Test
    fun `03 debounce close - 200 ms belum BLOCKED`() {
        val h = Harness()
        h.feed(distanceCm = 25f)
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 25f, advanceMs = 100))
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 25f, advanceMs = 100))
    }

    @Test
    fun `04 cepat lewat (100 ms) tidak memicu BLOCKED`() {
        val h = Harness()
        h.feed(distanceCm = 25f)
        h.feed(distanceCm = 25f, advanceMs = 100)
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 90f, advanceMs = 100))
    }

    @Test
    fun `05 flapping di sekitar 30 cm tidak berkedip ke BLOCKED`() {
        val h = Harness()
        repeat(6) {
            h.feed(distanceCm = 28f)
            h.feed(distanceCm = 32f, advanceMs = 150)
        }
        assertEquals(RiskLevel.WARNING, h.machine.current())
    }

    @Test
    fun `06 hysteresis - 32 cm tetap BLOCKED karena keluar butuh di atas 35 cm`() {
        val h = Harness()
        h.feed(distanceCm = 25f)
        h.feed(distanceCm = 25f, advanceMs = 600)
        assertEquals(RiskLevel.BLOCKED, h.machine.current())
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = 32f, advanceMs = 200))
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = 32f, advanceMs = 1000))
    }

    @Test
    fun `07 keluar alarm hanya setelah di atas 35 cm selama 2 dtk`() {
        val h = Harness()
        h.feed(distanceCm = 25f)
        h.feed(distanceCm = 25f, advanceMs = 600)
        assertEquals(RiskLevel.BLOCKED, h.machine.current())
        // >35 cm tapi baru 200 ms -> MASIH BLOCKED (US-04)
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = 40f, advanceMs = 200))
        // Lewat 2.000 ms di atas 35 cm -> turun; 40 cm masih zona Waspada -> WARNING
        assertEquals(RiskLevel.WARNING, h.feed(distanceCm = 40f, advanceMs = 2000))
        // Pindah ke zona Aman (60 cm) — timer aman mulai dari sini
        assertEquals(RiskLevel.WARNING, h.feed(distanceCm = 60f, advanceMs = 100))
        // Setelah 2 dtk di zona Aman -> OK
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 60f, advanceMs = 2100))
    }

    @Test
    fun `08 null face tidak dianggap 0 cm`() {
        val h = Harness()
        h.feed(distanceCm = 80f, advanceMs = 3000)
        assertEquals(RiskLevel.OK, h.feed(distanceCm = null, advanceMs = 100))
        assertEquals(RiskLevel.OK, h.feed(distanceCm = null, advanceMs = 100))
    }

    @Test
    fun `09 null face saat BLOCKED ditahan grace 10 dtk lalu OK (T7)`() {
        val h = Harness()
        h.feed(distanceCm = 25f)
        h.feed(distanceCm = 25f, advanceMs = 600)
        assertEquals(RiskLevel.BLOCKED, h.machine.current())
        // Wajah hilang 2 dtk TIDAK mematikan alarm (T7) — state ditahan selama grace.
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = null, advanceMs = 100))
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = null, advanceMs = 2000))
        // Lewat grace 10 dtk sejak wajah hilang → alarm boleh turun ke OK.
        assertEquals(RiskLevel.OK, h.feed(distanceCm = null, advanceMs = 9_000))
    }

    @Test
    fun `10 pitch di atas 30 selama 3 dtk memicu WARNING`() {
        val h = Harness()
        h.feed(distanceCm = 80f, pitchDeg = 45f)
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 80f, pitchDeg = 45f, advanceMs = 1500))
        assertEquals(RiskLevel.WARNING, h.feed(distanceCm = 80f, pitchDeg = 45f, advanceMs = 1600))
    }

    @Test
    fun `11 lux di bawah 150 selama 10 dtk memicu WARNING`() {
        val h = Harness()
        h.feed(distanceCm = 80f, lux = 100f)
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 80f, lux = 100f, advanceMs = 5000))
        assertEquals(RiskLevel.WARNING, h.feed(distanceCm = 80f, lux = 100f, advanceMs = 5100))
    }

    @Test
    fun `12 prioritas BLOCKED di atas WARNING`() {
        val h = Harness()
        // Gelap lama -> WARNING
        h.feed(distanceCm = 80f, lux = 50f)
        h.feed(distanceCm = 80f, lux = 50f, advanceMs = 11_000)
        assertEquals(RiskLevel.WARNING, h.machine.current())
        // Lalu terlalu dekat -> harus BLOCKED (prioritas)
        h.feed(distanceCm = 20f, lux = 50f)
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = 20f, lux = 50f, advanceMs = 600))
    }

    @Test
    fun `13 zona waspada 30-50 cm menampilkan WARNING`() {
        val h = Harness()
        assertEquals(RiskLevel.WARNING, h.feed(distanceCm = 40f, advanceMs = 2500))
    }

    @Test
    fun `14 zona aman 50 cm ke atas selama 2 dtk menjadi OK`() {
        val h = Harness()
        h.feed(distanceCm = 40f, advanceMs = 2500)
        assertEquals(RiskLevel.WARNING, h.machine.current())
        // Naik ke 70 cm; timer aman mulai dari sini, butuh 2 dtk
        assertEquals(RiskLevel.WARNING, h.feed(distanceCm = 70f, advanceMs = 100))
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 70f, advanceMs = 2100))
    }

    @Test
    fun `15 M3 - debounce close tidak terreset oleh 1-2 frame null`() {
        // Probe tajam M3: sebelum BLOCKED terkunci, 1-2 frame tanpa wajah tidak
        // boleh mematikan timer debounce close 500 ms (missingFrameTolerance = 2).
        val h1 = Harness()
        h1.feed(distanceCm = 25f)
        assertEquals(RiskLevel.OK, h1.feed(distanceCm = 25f, advanceMs = 300))
        assertEquals(RiskLevel.OK, h1.feed(distanceCm = null, advanceMs = 100))
        // Total 500 ms sejak kontak dekat pertama -> BLOCKED (timer dipertahankan).
        assertEquals(RiskLevel.BLOCKED, h1.feed(distanceCm = 25f, advanceMs = 100))

        // Varian 2 frame null berturut-turut (batas toleransi).
        val h2 = Harness()
        h2.feed(distanceCm = 25f)
        assertEquals(RiskLevel.OK, h2.feed(distanceCm = 25f, advanceMs = 300))
        assertEquals(RiskLevel.OK, h2.feed(distanceCm = null, advanceMs = 50))
        assertEquals(RiskLevel.OK, h2.feed(distanceCm = null, advanceMs = 50))
        assertEquals(RiskLevel.BLOCKED, h2.feed(distanceCm = 25f, advanceMs = 150))
    }

    @Test
    fun `16 M3 - BLOCKED bertahan melewati 1 frame null lalu dekat lagi`() {
        val h = Harness()
        h.feed(distanceCm = 25f)
        h.feed(distanceCm = 25f, advanceMs = 600)
        assertEquals(RiskLevel.BLOCKED, h.machine.current())
        // 1 frame tanpa wajah: state ditahan, debounce close tidak terreset.
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = null, advanceMs = 100))
        // Kembali 25 cm: langsung BLOCKED tanpa jeda debounce ulang.
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = 25f, advanceMs = 100))
    }

    @Test
    fun `17 M3 - 4 frame null berturut-turut mereset debounce close`() {
        // Perilaku aktual: toleransi frame hilang hanya 2 (missingFrameTolerance),
        // jadi frame kosong ke-3 dan seterusnya mematikan closeSinceMs — debounce
        // 500 ms harus dihitung ulang dari nol. T7 grace (skenario 09) hanya
        // menahan state yang SUDAH terkunci, bukan debounce yang belum sempat
        // tercapai.
        val h = Harness()
        h.feed(distanceCm = 25f)
        assertEquals(RiskLevel.OK, h.feed(distanceCm = null, advanceMs = 100))
        assertEquals(RiskLevel.OK, h.feed(distanceCm = null, advanceMs = 100))
        assertEquals(RiskLevel.OK, h.feed(distanceCm = null, advanceMs = 100))
        assertEquals(RiskLevel.OK, h.feed(distanceCm = null, advanceMs = 100))
        // Tanpa reset, closeSince = 0 -> 500 ms sudah lewat dan ini akan BLOCKED.
        // Dengan reset, kontak dekat dihitung ulang dari titik ini.
        assertEquals(RiskLevel.OK, h.feed(distanceCm = 25f, advanceMs = 100))
        assertEquals(RiskLevel.BLOCKED, h.feed(distanceCm = 25f, advanceMs = 500))
    }
}
