package com.jagamata.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Perilaku PIN orang tua (T1). Fallback DEFAULT_PIN ("1234") sudah DIHAPUS:
 * hash kosong menolak semua PIN. Murni tanpa Android/DataStore — keputusan
 * verifikasi diekstrak ke [SettingsRepository.pinMatches].
 */
class SettingsRepositoryPinTest {
    @Test
    fun hashPinDeterministikUntukPinDanSaltSama() {
        val a = SettingsRepository.hashPin("8765", "salt-1")
        val b = SettingsRepository.hashPin("8765", "salt-1")
        assertEquals(a, b)
    }

    @Test
    fun hashPinBerbedaUntukPinAtauSaltBerbeda() {
        val base = SettingsRepository.hashPin("8765", "salt-1")
        assertNotEquals(base, SettingsRepository.hashPin("8766", "salt-1"))
        assertNotEquals(base, SettingsRepository.hashPin("8765", "salt-2"))
    }

    @Test
    fun hashKosongMenolakSemuaPinTermasuk1234() {
        // Regresi T1: dulu `pin == DEFAULT_PIN` diterima selamanya bila hash kosong.
        assertFalse(SettingsRepository.pinMatches("1234", null, ""))
        assertFalse(SettingsRepository.pinMatches("1234", "bebas", ""))
        assertFalse(SettingsRepository.pinMatches("0000", null, ""))
        assertFalse(SettingsRepository.pinMatches("", null, ""))
    }

    @Test
    fun saltHilangMenolakSemuaPin() {
        val hash = SettingsRepository.hashPin("4321", "salt-x")
        assertFalse(SettingsRepository.pinMatches("4321", null, hash))
    }

    @Test
    fun pinBenarDiterimaDan1234TidakIstimewa() {
        val salt = "salt-y"
        val hash = SettingsRepository.hashPin("8765", salt)
        assertTrue(SettingsRepository.pinMatches("8765", salt, hash))
        // "1234" kini PIN biasa: hanya sah bila memang PIN yang dibuat.
        assertFalse(SettingsRepository.pinMatches("1234", salt, hash))
        val hash1234 = SettingsRepository.hashPin("1234", salt)
        assertTrue(SettingsRepository.pinMatches("1234", salt, hash1234))
    }

    @Test
    fun pinSalahDitolak() {
        val salt = "salt-z"
        val hash = SettingsRepository.hashPin("1357", salt)
        assertFalse(SettingsRepository.pinMatches("1358", salt, hash))
    }
}
