package com.jagamata.app.report

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.jagamata.app.data.entity.DailyStats
import java.io.File
import java.io.FileOutputStream

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

    /**
     * Render dokumen PDF format standar A4 (595 x 842 pt) untuk konsultasi dokter mata / arsip ortu.
     * Menggunakan native Android android.graphics.pdf.PdfDocument tanpa pustaka pihak ketiga.
     */
    fun renderPdf(
        context: Context,
        summary: ReportSummary,
        childName: String = "Rafa",
        childAge: Int = 8,
    ): File {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val pdfFile = File(dir, "Laporan_JagaMata_Mingguan.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        canvas.drawColor(Color.WHITE)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4A86E8")
            style = Paint.Style.FILL
        }
        // Header banner
        canvas.drawRect(0f, 0f, 595f, 90f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
        }
        canvas.drawText("JagaMata — Laporan Kesehatan Mata & Skrining Layar", 30f, 45f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E1EFFF")
            textSize = 11f
        }
        canvas.drawText("Protokol Pemantauan Jarak On-Device (Rule 20-20-20 & Distance Compliance)", 30f, 68f, subtitlePaint)

        // Info Pasien / Anak Card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F2F7FF")
            style = Paint.Style.FILL
        }
        val cardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D6E6FF")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(30f, 110f, 565f, 185f, 8f, 8f, cardPaint)
        canvas.drawRoundRect(30f, 110f, 565f, 185f, 8f, 8f, cardBorder)

        val textBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#12263A")
            textSize = 12f
            isFakeBoldText = true
        }
        val textBody = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#12263A")
            textSize = 11f
        }

        canvas.drawText("Nama Anak:", 45f, 135f, textBold)
        canvas.drawText("$childName ($childAge Tahun)", 130f, 135f, textBody)

        canvas.drawText("Periode:", 45f, 155f, textBold)
        canvas.drawText(summary.periodLabel.ifEmpty { "7 Hari Terakhir" }, 130f, 155f, textBody)

        canvas.drawText("Metode Evaluasi:", 320f, 135f, textBold)
        canvas.drawText("Kamera Depan 5 FPS (On-Device)", 430f, 135f, textBody)

        canvas.drawText("Ambang Aman:", 320f, 155f, textBold)
        canvas.drawText("Jarak >= 30 cm | Cahaya >= 150 lux", 430f, 155f, textBody)

        // Ringkasan Metrik
        var y = 220f
        canvas.drawText("RINGKASAN KEPATUHAN & STATUS KLINIS", 30f, y, textBold)
        y += 15f
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#4A86E8")
            strokeWidth = 2f
        }
        canvas.drawLine(30f, y, 565f, y, dividerPaint)
        y += 25f

        // 4 Kotak Metrik (KPI)
        val kpiWidth = 125f
        val kpis = listOf(
            Triple("Total Siklus", "${summary.totalCycles}", "#4A86E8"),
            Triple("Pelanggaran Jarak", "${summary.totalCloseViolations}x", if (summary.totalCloseViolations > 10) "#FF7B6B" else "#50C878"),
            Triple("Menit Ruang Gelap", "${summary.totalDarkMinutes} mnt", if (summary.totalDarkMinutes > 30) "#FFD166" else "#50C878"),
            Triple("Kepatuhan Postur", "${summary.totalPostureViolations}x", "#4A86E8")
        )
        for (i in kpis.indices) {
            val kpiLeft = 30f + i * (kpiWidth + 8f)
            val (kpiTitle, kpiVal, kpiColor) = kpis[i]
            val bg = Paint().apply { color = Color.parseColor("#F8FAFC"); style = Paint.Style.FILL }
            val border = Paint().apply { color = Color.parseColor(kpiColor); style = Paint.Style.STROKE; strokeWidth = 1f }
            canvas.drawRoundRect(kpiLeft, y, kpiLeft + kpiWidth, y + 55f, 6f, 6f, bg)
            canvas.drawRoundRect(kpiLeft, y, kpiLeft + kpiWidth, y + 55f, 6f, 6f, border)

            val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(kpiColor)
                textSize = 16f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val lblPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(kpiVal, kpiLeft + kpiWidth / 2f, y + 26f, valPaint)
            canvas.drawText(kpiTitle, kpiLeft + kpiWidth / 2f, y + 44f, lblPaint)
        }

        y += 85f
        canvas.drawText("RINCIAN PEMANTAUAN HARIAN", 30f, y, textBold)
        y += 15f
        canvas.drawLine(30f, y, 565f, y, dividerPaint)
        y += 20f

        // Header Tabel
        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        canvas.drawRect(30f, y, 565f, y + 22f, thPaint)
        val thText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            isFakeBoldText = true
        }
        canvas.drawText("Hari / Tanggal", 40f, y + 15f, thText)
        canvas.drawText("Total Sesi", 170f, y + 15f, thText)
        canvas.drawText("Jarak < 30 cm", 270f, y + 15f, thText)
        canvas.drawText("Ruang Gelap", 380f, y + 15f, thText)
        canvas.drawText("Status Risiko", 480f, y + 15f, thText)

        y += 25f
        val days = summary.perDay.take(7)
        val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
        for (idx in 0 until 7) {
            val d = days.getOrNull(idx)
            val dName = dayNames.getOrElse(idx) { "Hari ${idx + 1}" }
            val rowBg = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
            canvas.drawRect(30f, y, 565f, y + 20f, Paint().apply { color = rowBg })

            val rowText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 10f
            }
            canvas.drawText(dName, 40f, y + 14f, rowText)
            canvas.drawText("${d?.totalCycles ?: 0} siklus", 170f, y + 14f, rowText)
            canvas.drawText("${d?.closeViolations ?: 0} kali", 270f, y + 14f, rowText)
            canvas.drawText("${d?.darkMinutes ?: 0} menit", 380f, y + 14f, rowText)

            val status = if ((d?.closeViolations ?: 0) > 5) "Perhatian" else "Aman"
            val statusColor = if (status == "Aman") "#50C878" else "#FF7B6B"
            val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(statusColor)
                textSize = 10f
                isFakeBoldText = true
            }
            canvas.drawText(status, 480f, y + 14f, statusPaint)
            y += 20f
        }

        // Rekomendasi Medis & Validasi Model Miopia
        y += 30f
        canvas.drawText("CATATAN DOKTER & REKOMENDASI PREVENTIF MIOPIA", 30f, y, textBold)
        y += 15f
        canvas.drawLine(30f, y, 565f, y, dividerPaint)
        y += 20f

        val recoBox = Paint().apply {
            color = Color.parseColor("#FFFBEB")
            style = Paint.Style.FILL
        }
        val recoBorder = Paint().apply {
            color = Color.parseColor("#FDE68A")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(30f, y, 565f, y + 115f, 6f, 6f, recoBox)
        canvas.drawRoundRect(30f, y, 565f, y + 115f, 6f, 6f, recoBorder)

        val recoText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#92400E")
            textSize = 9.5f
        }
        var ry = y + 18f
        val recoLines = listOf(
            "1. Aturan 20-20-20: Setiap 20 menit menatap layar, pandang objek berjarak 6 meter selama minimal 20 detik.",
            "2. Validasi Klinis Miopia (SPORTHR, p < 0.001): Tingkatkan aktivitas luar ruangan minimal 1.5 - 2 jam per hari.",
            "   Penelitian membuktikan sinar matahari alami merangsang pelepasan dopamin retina penghambat pemanjangan aksial bola mata.",
            "3. Pertahankan pencahayaan ruangan di atas 150 lux dan hindari penggunaan gawai di ruangan gelap total sebelum tidur.",
            "4. Jika anak sering mengucek mata atau memicingkan mata saat melihat jauh, segera konsultasikan ke Dokter Spesialis Mata (Sp.M)."
        )
        for (line in recoLines) {
            canvas.drawText(line, 42f, ry, recoText)
            ry += 18f
        }

        // Tanda Tangan
        y += 135f
        canvas.drawText("Orang Tua / Wali Murid,", 60f, y, textBody)
        canvas.drawText("Dokter Spesialis Mata / Pemeriksa,", 360f, y, textBody)
        y += 45f
        canvas.drawLine(60f, y, 200f, y, Paint().apply { color = Color.parseColor("#94A3B8"); strokeWidth = 1f })
        canvas.drawLine(360f, y, 520f, y, Paint().apply { color = Color.parseColor("#94A3B8"); strokeWidth = 1f })
        y += 14f
        canvas.drawText("(..........................................)", 60f, y, Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 9f })
        canvas.drawText("(..........................................)", 360f, y, Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 9f })

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Dokumen ini dihasilkan secara otomatis oleh Aplikasi JagaMata — On-Device Eye Protection Platform", 595f / 2f, 825f, footerPaint)

        document.finishPage(page)
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return pdfFile
    }
}
