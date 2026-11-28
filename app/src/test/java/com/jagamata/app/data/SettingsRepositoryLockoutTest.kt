package com.jagamata.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kebijakan lockout PIN (T3) — murni dengan clock terinjeksi (elapsedRealtime).
 * Semantik sisa-durasi + decrement-on-read, kebal rollback jam dinding, dan
 * fail-safe terhadap reboot (elapsedRealtime reset).
 */
class SettingsRepositoryLockoutTest {
    // ---- Jadwal eksponensial: 1, 5, 15, 60 menit setelah kegagalan ke-5, 6, 7, 8+ ----

    @Test
    fun diBawahAmbangLimaKegagalanTidakAdaLockout() {
        assertEquals(0L, SettingsRepository.lockoutDurationMs(0))
        assertEquals(0L, SettingsRepository.lockoutDurationMs(1))
        assertEquals(0L, SettingsRepository.lockoutDurationMs(2))
        assertEquals(0L, SettingsRepository.lockoutDurationMs(3))
        assertEquals(0L, SettingsRepository.lockoutDurationMs(4))
    }

    @Test
    fun jadwalLockoutEksponensialLimaEnamTujuhDelapan() {
        assertEquals(1 * 60_000L, SettingsRepository.lockoutDurationMs(5))
        assertEquals(5 * 60_000L, SettingsRepository.lockoutDurationMs(6))
        assertEquals(15 * 60_000L, SettingsRepository.lockoutDurationMs(7))
        assertEquals(60 * 60_000L, SettingsRepository.lockoutDurationMs(8))
    }

    @Test
    fun kegagalanDiAtasDelapanTetapMaksimumEnamPuluhMenit() {
        assertEquals(60 * 60_000L, SettingsRepository.lockoutDurationMs(9))
        assertEquals(60 * 60_000L, SettingsRepository.lockoutDurationMs(99))
    }

    @Test
    fun eskalasiTidakPernahMemperpendekDurasi() {
        var prev = 0L
        for (attempts in 1..12) {
            val duration = SettingsRepository.lockoutDurationMs(attempts)
            assertTrue("durasi harus monoton naik pada attempts=$attempts", duration >= prev)
            prev = duration
        }
    }

    // ---- Sisa durasi efektif (clock terinjeksi) ----

    @Test
    fun sisaDurasiBerkurangSesuaiElapsedMonotonik() {
        val stamp = 1_000_000L
        assertEquals(
            40_000L,
            SettingsRepository.effectiveLockoutRemainingMs(60_000L, stamp, stamp + 20_000L),
        )
        assertEquals(
            0L,
            SettingsRepository.effectiveLockoutRemainingMs(60_000L, stamp, stamp + 60_000L),
        )
    }

    @Test
    fun sisaDurasiTidakNegatifSetelahKadaluarsa() {
        val stamp = 5_000L
        assertEquals(
            0L,
            SettingsRepository.effectiveLockoutRemainingMs(60_000L, stamp, stamp + 3_600_000L),
        )
    }

    @Test
    fun sisaNolTetapNol() {
        assertEquals(0L, SettingsRepository.effectiveLockoutRemainingMs(0L, 100L, 200_000L))
    }

    @Test
    fun rebootFailSafeMenahanSisaPenuh() {
        // elapsedRealtime di-reset ke ~0 setelah boot → now < stamp.
        // Lockout tidak boleh dibobol: sisa tetap penuh, lalu di-stamp ulang oleh pembaca.
        assertEquals(
            60_000L,
            SettingsRepository.effectiveLockoutRemainingMs(60_000L, 50_000_000L, 1_000L),
        )
    }

    @Test
    fun decrementOnReadDuaBerturutTidakKehilanganWaktu() {
        // Simulasi pinLockoutState(): baca → sisa dikurangi → di-stamp ulang.
        var remaining = 60_000L
        var stamp = 1_000_000L

        remaining = SettingsRepository.effectiveLockoutRemainingMs(remaining, stamp, 1_020_000L)
        stamp = 1_020_000L
        assertEquals(40_000L, remaining)

        remaining = SettingsRepository.effectiveLockoutRemainingMs(remaining, stamp, 1_050_000L)
        stamp = 1_050_000L
        assertEquals(10_000L, remaining)

        remaining = SettingsRepository.effectiveLockoutRemainingMs(remaining, stamp, 1_060_000L)
        assertEquals(0L, remaining)
    }

    @Test
    fun rollbackJamDindingTidakMempengaruhiSisaDurasi() {
        // Fungsi hanya menerima elapsedRealtime (monotonik) — timestamp wall-clock
        // tidak ikut dihitung, jadi memundurkan jam dinding tidak memperpendek lockout.
        val stamp = 2_000_000L
        val remaining = SettingsRepository.effectiveLockoutRemainingMs(900_000L, stamp, stamp + 1_000L)
        assertEquals(899_000L, remaining)
    }
}
