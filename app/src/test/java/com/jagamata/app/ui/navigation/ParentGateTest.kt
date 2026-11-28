package com.jagamata.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test gerbang rute orang tua (DEV-37). [ParentGate] murni Kotlin — tanpa Compose/Android.
 */
class ParentGateTest {
    private val gate = ParentGate()

    // ——— (a) terkunci → rute sensitif dialihkan ke KUNCI + pending tersimpan ———

    @Test
    fun `rute sensitif saat terkunci dialihkan ke KUNCI dan pending tersimpan`() {
        val target = gate.tryNavigate(Routes.SETTINGS)

        assertEquals(Routes.KUNCI, target)
        assertEquals(Routes.SETTINGS, gate.pending)
        assertFalse(gate.unlocked)
    }

    @Test
    fun `semua rute gerbang saat terkunci menuju KUNCI`() {
        for (route in ParentGate.GATED_ROUTES) {
            val fresh = ParentGate()
            assertEquals(Routes.KUNCI, fresh.tryNavigate(route))
            assertEquals(route, fresh.pending)
        }
    }

    // ——— (b) unlock dengan pending → kembali ke tujuan asli ———

    @Test
    fun `unlock dengan pending mengembalikan tujuan asli`() {
        gate.tryNavigate(Routes.DELETE_DATA)

        val resumed = gate.onUnlocked()

        assertEquals(Routes.DELETE_DATA, resumed)
        assertTrue(gate.unlocked)
        assertNull(gate.pending)
    }

    @Test
    fun `unlock tanpa pending mengembalikan null`() {
        assertNull(gate.onUnlocked())
        assertTrue(gate.unlocked)
    }

    @Test
    fun `rute sensitif setelah unlock langsung menuju tujuan`() {
        gate.onUnlocked()

        assertEquals(Routes.DASHBOARD, gate.tryNavigate(Routes.DASHBOARD))
        assertNull(gate.pending)
    }

    // ——— (c) auto-relock saat foreground setelah jeda latar-belakang ———

    @Test
    fun `foreground setelah jeda lebih dari 5 menit mengunci ulang`() {
        val t0 = 1_000_000L
        gate.onUnlocked()
        gate.onAppBackground(t0)

        gate.onAppForeground(nowMs = t0 + ParentGate.DEFAULT_TIMEOUT_MS + 1)

        assertFalse(gate.unlocked)
    }

    @Test
    fun `foreground setelah jeda kurang dari 5 menit tetap terbuka`() {
        val t0 = 1_000_000L
        gate.onUnlocked()
        gate.onAppBackground(t0)

        gate.onAppForeground(nowMs = t0 + ParentGate.DEFAULT_TIMEOUT_MS - 1)

        assertTrue(gate.unlocked)
    }

    @Test
    fun `tanpa riwayat latar-belakang tidak pernah relock`() {
        gate.onUnlocked()

        gate.onAppForeground(nowMs = ParentGate.DEFAULT_TIMEOUT_MS * 100)

        assertTrue(gate.unlocked)
    }

    // ——— (d) rute non-sensitif tidak diganggu gerbang ———

    @Test
    fun `rute non-sensitif saat terkunci tetap lolos apa adanya`() {
        val nonGated =
            listOf(
                Routes.BERANDA,
                Routes.PANTAU_LIVE,
                Routes.HADIAH,
                Routes.PROFIL_ANAK,
                Routes.REPORT,
                Routes.ISTIRAHAT,
                Routes.STATISTIK,
                Routes.IZIN,
                Routes.KALIBRASI,
                Routes.KUNCI,
                Routes.OVERLAY_DITOLAK,
                Routes.ONBOARDING,
                Routes.PERINGATAN,
            )

        for (route in nonGated) {
            assertEquals(route, gate.tryNavigate(route))
        }
        assertNull(gate.pending)
    }
}
