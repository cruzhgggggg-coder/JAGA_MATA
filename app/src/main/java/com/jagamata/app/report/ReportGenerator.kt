package com.jagamata.app.report

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.jagamata.app.data.entity.DailyStats

/**
 * Laporan mingguan (PRD US-12, DEV-23): merangkum 7 hari -> PNG 1080x1920
 * untuk dibagikan lewat share sheet (ACTION_SEND / WhatsApp).
 *
 * [summarize] murni (diuji unit test); [renderPng] bergantung Android.
 */
object ReportGenerator {
    data class ReportSummary(
        val periodLabel: String,
        val totalCloseViolations: Int,
        val totalDarkMinutes: Int,
        val totalPostureViolations: Int,
        val totalCycles: Int,
        val perDay: List<DailyStats>,
    ) {
        val isEmpty: Boolean get() = totalCycles == 0 && perDay.isEmpty()
    }

    /** Merangkum data harian (urut naik) menjadi ringkasan laporan. */
    fun summarize(
        perDay: List<DailyStats>,
        periodLabel: String = "",
    ): ReportSummary {
        val sorted = perDay.sortedBy { it.dayEpoch }
        return ReportSummary(
            periodLabel = periodLabel,
            totalCloseViolations = sorted.sumOf { it.closeViolations },
            totalDarkMinutes = sorted.sumOf { it.darkMinutes },
            totalPostureViolations = sorted.sumOf { it.postureViolations },
            totalCycles = sorted.sumOf { it.totalCycles },
            perDay = sorted,
        )
    }

    /** Teks ringkasan (dipakai body share + unit test). */
    fun summaryText(summary: ReportSummary): String =
        buildString {
            appendLine("Laporan Mingguan JagaMata")
            if (summary.periodLabel.isNotEmpty()) appendLine("Periode: ${summary.periodLabel}")
            appendLine("Pelanggaran jarak: ${summary.totalCloseViolations} kali")
            appendLine("Menit gelap: ${summary.totalDarkMinutes} menit")
            appendLine("Pelanggaran postur: ${summary.totalPostureViolations} kali")
            appendLine("Total siklus pemantauan: ${summary.totalCycles}")
        }

    const val PNG_WIDTH = 1080
    const val PNG_HEIGHT = 1920

    /** Render PNG 1080x1920: ringkasan + grafik batang sederhana. */
    fun renderPng(summary: ReportSummary): Bitmap {
        val bitmap = Bitmap.createBitmap(PNG_WIDTH, PNG_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#F2F7FF"))

        val inkPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#12263A")
                textSize = 56f
                isFakeBoldText = true
            }
        val bodyPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#12263A")
                textSize = 42f
            }

        var y = 160f
        canvas.drawText("Laporan Mingguan JagaMata", 60f, y, inkPaint)
        y += 120f
        for (line in summaryText(summary).lines().drop(1)) {
            if (line.isBlank()) continue
            canvas.drawText(line, 60f, y, bodyPaint)
            y += 80f
        }

        y += 120f
        canvas.drawText("Grafik 7 hari", 60f, y, inkPaint)
        y += 120f

        val maxClose = (summary.perDay.maxOfOrNull { it.closeViolations } ?: 1).coerceAtLeast(1)
        val maxPosture = (summary.perDay.maxOfOrNull { it.postureViolations } ?: 1).coerceAtLeast(1)
        val barWidth = (PNG_WIDTH - 160f) / 7f
        summary.perDay.take(7).forEachIndexed { index, day ->
            val left = 80f + index * barWidth
            val closeHeight = 600f * day.closeViolations / maxClose
            val postureHeight = 600f * day.postureViolations / maxPosture

            val closePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF7B6B") }
            val posturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFD166") }

            canvas.drawRect(left, y + 600f - closeHeight, left + barWidth * 0.4f, y + 600f, closePaint)
            canvas.drawRect(
                left + barWidth * 0.45f,
                y + 600f - postureHeight,
                left + barWidth * 0.85f,
                y + 600f,
                posturePaint,
            )
        }
        return bitmap
    }
}
