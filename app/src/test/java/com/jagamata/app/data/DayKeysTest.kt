package com.jagamata.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * M8: kunci hari lokal — 00:00–07:00 WIB tidak boleh jatuh ke hari sebelumnya
 * (bug `ms / 86_400_000` UTC). Semua kasus memakai zona eksplisit agar
 * deterministik lintas mesin.
 */
class DayKeysTest {
    private val wib: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")

    // month: 0-based Calendar
    private fun epochOf(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        zone: TimeZone,
    ): Long =
        Calendar
            .getInstance(zone)
            .apply {
                clear()
                set(year, month, day, hour, minute, 0)
            }.timeInMillis

    @Test
    fun `pagi WIB tetap di hari yang sama - bukan hari sebelumnya`() {
        // 27 Nov 2026 00:30 WIB (di UTC masih 26 Nov 17:30).
        val ts = epochOf(2026, Calendar.NOVEMBER, 27, 0, 30, wib)
        val key = localDayEpoch(ts, wib)
        // Kunci = epoch-day tanggal 27 Nov 2026 (via tengah malam UTC tanggal sama).
        val expected = localDayEpoch(epochOf(2026, Calendar.NOVEMBER, 27, 12, 0, wib), wib)
        assertEquals(expected, key)
        // Bukti bug lama: kalender UTC untuk timestamp yang sama masih 26 Nov.
        val utcDay =
            Calendar
                .getInstance(TimeZone.getTimeZone("UTC"))
                .apply { timeInMillis = ts }
                .get(Calendar.DAY_OF_MONTH)
        assertEquals(26, utcDay)
        assertEquals(
            27,
            Calendar.getInstance(wib).apply { timeInMillis = ts }.get(Calendar.DAY_OF_MONTH),
        )
    }

    @Test
    fun `satu hari lokal menghasilkan satu kunci untuk semua jam`() {
        val keys =
            (0..23).map { hour ->
                localDayEpoch(epochOf(2026, Calendar.NOVEMBER, 27, hour, 0, wib), wib)
            }
        assertEquals(1, keys.toSet().size)
    }

    @Test
    fun `kunci berbeda untuk tanggal berbeda dan berurutan`() {
        val d26 = localDayEpoch(epochOf(2026, Calendar.NOVEMBER, 26, 12, 0, wib), wib)
        val d27 = localDayEpoch(epochOf(2026, Calendar.NOVEMBER, 27, 12, 0, wib), wib)
        assertNotEquals(d26, d27)
        assertEquals(d26 + 1, d27)
    }

    @Test
    fun `localDayStartMs jatuh tepat tengah malam lokal`() {
        val key = localDayEpoch(epochOf(2026, Calendar.NOVEMBER, 27, 15, 45, wib), wib)
        val start = localDayStartMs(key, wib)
        val cal = Calendar.getInstance(wib).apply { timeInMillis = start }
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH))
        assertEquals(27, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `localDayEndMs adalah awal hari berikutnya`() {
        val key = localDayEpoch(epochOf(2026, Calendar.NOVEMBER, 27, 12, 0, wib), wib)
        assertEquals(localDayStartMs(key + 1, wib), localDayEndMs(key, wib))
    }

    @Test
    fun `localDaysEndingAt menghasilkan n hari inklusif berurutan`() {
        assertEquals(listOf(95L, 96L, 97L), localDaysEndingAt(97L, 3))
        assertEquals(listOf(97L), localDaysEndingAt(97L, 1))
    }

    @Test
    fun `dayInitial memakai inisial Indonesia Senin sampai Minggu`() {
        // epoch-day 0 = 1970-01-01 Kamis.
        assertEquals("K", dayInitial(0))
        assertEquals("J", dayInitial(1))
        assertEquals("S", dayInitial(2)) // Sabtu
        assertEquals("M", dayInitial(3)) // Minggu
        assertEquals("S", dayInitial(4)) // Senin
        assertEquals("S", dayInitial(5)) // Selasa
        assertEquals("R", dayInitial(6)) // Rabu
        // 7 hari berturut-turut selalu menghasilkan 7 inisial (urutan S S R K J S M bila Senin-mulai).
        val week = (0L..6L).map { dayInitial(4 + it) } // mulai Senin
        assertEquals(listOf("S", "S", "R", "K", "J", "S", "M"), week)
    }

    @Test
    fun `label tanggal memakai nama hari Indonesia`() {
        val ts = epochOf(2026, Calendar.NOVEMBER, 27, 10, 0, wib)
        val label = localDayLabel(ts, wib)
        assertTrue("label harus memuat tanggal 27: $label", label.contains("27"))
        assertTrue("label harus memuat nama bulan: $label", label.contains("November"))
        assertTrue("label harus memuat nama hari Indonesia: $label", label.contains("Jumat"))
    }

    @Test
    fun `zona berbeda untuk timestamp dekat tengah malam`() {
        // 26 Nov 2026 23:30 UTC = 27 Nov 06:30 WIB → kunci WIB harus tanggal 27.
        val utc = TimeZone.getTimeZone("UTC")
        val ts = epochOf(2026, Calendar.NOVEMBER, 26, 23, 30, utc)
        assertEquals(
            localDayEpoch(epochOf(2026, Calendar.NOVEMBER, 27, 12, 0, wib), wib),
            localDayEpoch(ts, wib),
        )
        assertEquals(
            localDayEpoch(epochOf(2026, Calendar.NOVEMBER, 26, 12, 0, utc), utc),
            localDayEpoch(ts, utc),
        )
    }
}
