package com.jagamata.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Perluasan [ParentGate] untuk T4 (guard terpusat, butuh [ParentGate.requiresGate])
 * dan T6 (kalibrasi-ulang digerbangkan, kalibrasi pertama tidak).
 */
class ParentGateExtensionTest {
    private val gate = ParentGate()

    // ——— requiresGate (dipakai guard terpusat AppNav untuk back stack yang di-restore) ———

    @Test
    fun `requiresGate benar untuk semua rute mode ortu terlepas status unlock`() {
        gate.unlocked = true
        for (route in ParentGate.GATED_ROUTES) {
            assertTrue(gate.requiresGate(route))
        }
    }

    @Test
    fun `requiresGate false untuk rute non-sensitif`() {
        gate.hasSavedFocal = true
        val nonGated =
            listOf(
                Routes.BERANDA,
                Routes.PANTAU_LIVE,
                Routes.HADIAH,
                Routes.PROFIL_ANAK,
                Routes.STATISTIK,
                Routes.IZIN,
                Routes.KUNCI,
                Routes.ONBOARDING,
                Routes.SPLASH,
            )
        for (route in nonGated) {
            assertFalse(route, gate.requiresGate(route))
        }
    }

    // ——— T6: kalibrasi pertama (setup) tanpa gerbang ———

    @Test
    fun `kalibrasi pertama saat terkunci tetap lolos tanpa PIN`() {
        gate.hasSavedFocal = false

        assertEquals(Routes.KALIBRASI, gate.tryNavigate(Routes.KALIBRASI))
        assertNull(gate.pending)
    }

    @Test
    fun `kalibrasi ulang saat terkunci dialihkan ke KUNCI dengan pending`() {
        gate.hasSavedFocal = true

        val target = gate.tryNavigate(Routes.KALIBRASI)

        assertEquals(Routes.KUNCI, target)
        assertEquals(Routes.KALIBRASI, gate.pending)
        assertFalse(gate.unlocked)
    }

    @Test
    fun `kalibrasi ulang setelah unlock langsung menuju tujuan`() {
        gate.hasSavedFocal = true
        gate.onUnlocked()

        assertEquals(Routes.KALIBRASI, gate.tryNavigate(Routes.KALIBRASI))
        assertNull(gate.pending)
    }

    @Test
    fun `menyimpan focal membuat kalibrasi berikutnya menjadi gerbang`() {
        assertFalse(gate.requiresGate(Routes.KALIBRASI))

        gate.hasSavedFocal = true

        assertTrue(gate.requiresGate(Routes.KALIBRASI))
        assertEquals(Routes.KUNCI, gate.tryNavigate(Routes.KALIBRASI))
    }

    // ——— T4: unlock melewati pending route yang dibuat guard ———

    @Test
    fun `unlock setelah guard mengembalikan tujuan yang diganti KUNCI`() {
        gate.hasSavedFocal = true
        gate.tryNavigate(Routes.KALIBRASI)

        val resumed = gate.onUnlocked()

        assertEquals(Routes.KALIBRASI, resumed)
        assertTrue(gate.unlocked)
    }
}
