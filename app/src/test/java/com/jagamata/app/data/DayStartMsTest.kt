package com.jagamata.app.data

import com.jagamata.app.ui.viewModel.dayStartMs
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/** Jendela "hari ini" (tengah malam zona lokal) untuk hitungan sesi dijeda (DEV-38). */
class DayStartMsTest {
    @Test
    fun dayStartMsJatuhDiTengahMalamLokal() {
        val now =
            Calendar
                .getInstance()
                .apply {
                    set(2026, Calendar.JUNE, 15, 14, 30, 45)
                    set(Calendar.MILLISECOND, 123)
                }.timeInMillis
        val start = dayStartMs(now)
        val cal =
            Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH))
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test
    fun dayStartMsSamaUntukTimestampDalamSatuHari() {
        val startOfDay =
            Calendar
                .getInstance()
                .apply {
                    set(2026, Calendar.JUNE, 15, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
        assertEquals(startOfDay, dayStartMs(startOfDay))
        assertEquals(startOfDay, dayStartMs(startOfDay + 12 * 3_600_000L))
    }
}
