package com.jagamata.app.report

import com.jagamata.app.data.entity.DailyStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportGeneratorTest {
    private fun sevenDays(): List<DailyStats> =
        (0 until 7).map { i ->
            DailyStats(
                dayEpoch = 20_000L + i,
                // 1..7 -> total 28
                closeViolations = i + 1,
                // 7 x 2 -> 14
                darkMinutes = 2,
                // 4 hari genap
                postureViolations = if (i % 2 == 0) 1 else 0,
                totalCycles = 60,
            )
        }

    @Test
    fun `ringkasan 7 hari cocok dengan fixture`() {
        val summary = ReportGenerator.summarize(sevenDays(), periodLabel = "1-7 Juli")
        assertEquals(28, summary.totalCloseViolations)
        assertEquals(14, summary.totalDarkMinutes)
        assertEquals(4, summary.totalPostureViolations)
        assertEquals(7 * 60, summary.totalCycles)
        assertEquals(7, summary.perDay.size)
    }

    @Test
    fun `teks ringkasan memuat angka yang benar`() {
        val summary = ReportGenerator.summarize(sevenDays(), periodLabel = "1-7 Juli")
        val text = ReportGenerator.summaryText(summary)
        assertTrue(text.contains("Pelanggaran jarak: 28"))
        assertTrue(text.contains("Menit gelap: 14"))
        assertTrue(text.contains("Pelanggaran postur: 4"))
        assertTrue(text.contains("Periode: 1-7 Juli"))
    }

    @Test
    fun `data kosong dilaporkan kosong`() {
        val summary = ReportGenerator.summarize(emptyList())
        assertTrue(summary.isEmpty)
        assertEquals(0, summary.totalCloseViolations)
    }

    @Test
    fun `periode tujuh hari tetap tujuh baris meski tidak urut`() {
        val shuffled = sevenDays().shuffled()
        val summary = ReportGenerator.summarize(shuffled)
        assertEquals(7, summary.perDay.size)
        assertTrue(summary.perDay.zipWithNext().all { (a, b) -> a.dayEpoch < b.dayEpoch })
    }
}
