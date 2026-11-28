package com.jagamata.app.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Logika murni [shouldDeliverAlert] (T8): alert hanya boleh dikirim bila kanal
 * notifikasi benar-benar aktif dan jendela throttle sudah terbuka. Regresi T8 —
 * dulu notify() dibuang diam-diam di Android 13+ tanpa POST_NOTIFICATIONS tetapi
 * throttledAlert tetap mengembalikan true dan mengonsumsi jendela throttle.
 */
class NotificationHelperThrottleTest {
    @Test
    fun notifikasiNonaktifTidakPernahMengirim() {
        assertFalse(
            shouldDeliverAlert(
                notificationsEnabled = false,
                nowMs = 10_000L,
                lastAtMs = 0L,
                windowMs = 60_000L,
            ),
        )
    }

    @Test
    fun notifikasiNonaktifJugaTidakMengirimSaatJendelaBaruDibuka() {
        assertFalse(
            shouldDeliverAlert(
                notificationsEnabled = false,
                nowMs = 1_000_000L,
                lastAtMs = 0L,
                windowMs = 60_000L,
            ),
        )
    }

    @Test
    fun notifikasiAktifDanJendelaTerbukaMengirim() {
        assertTrue(
            shouldDeliverAlert(
                notificationsEnabled = true,
                nowMs = 120_000L,
                lastAtMs = 0L,
                windowMs = 60_000L,
            ),
        )
    }

    @Test
    fun notifikasiAktifTapiMasihDalamThrottleTidakMengirim() {
        assertFalse(
            shouldDeliverAlert(
                notificationsEnabled = true,
                nowMs = 40_000L,
                lastAtMs = 30_000L,
                windowMs = 60_000L,
            ),
        )
    }

    @Test
    fun tepatPadaBatasJendelaMengirim() {
        assertTrue(
            shouldDeliverAlert(
                notificationsEnabled = true,
                nowMs = 70_000L,
                lastAtMs = 10_000L,
                windowMs = 60_000L,
            ),
        )
    }

    @Test
    fun pengirimanPertamaKaliSelaluTerbukaJikaNotifikasiAktif() {
        assertTrue(
            shouldDeliverAlert(
                notificationsEnabled = true,
                nowMs = 1_000L,
                lastAtMs = 0L,
                windowMs = 60_000L,
            ),
        )
    }
}
