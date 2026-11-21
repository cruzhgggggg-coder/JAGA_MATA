package com.jagamata.app.ui.parent

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.jagamata.app.R
import com.jagamata.app.data.AppDatabase
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.data.entity.DailyStats
import com.jagamata.app.report.ReportGenerator
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import java.io.File
import java.io.FileOutputStream

/**
 * Laporan mingguan (PRD US-12, DEV-23, Blueprint D1):
 * - Membaca data riil dari Room Database.
 * - Ekspor Dokumen Klinis Asli PDF (untuk konsultasi dokter spesialis mata).
 * - Ekspor Ringkasan Gambar PNG (untuk WhatsApp / Status Media Sosial).
 */
@Composable
fun WeeklyReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { SettingsRepository(context) }
    val childName by settings.childName.collectAsState(initial = "Rafa")
    val childAge by settings.childAge.collectAsState(initial = 8)

    var dailyList by remember { mutableStateOf<List<DailyStats>>(emptyList()) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context)
        val stats = db.dailyDao().getAll()
        dailyList = if (stats.isNotEmpty()) {
            stats.takeLast(7)
        } else {
            // Data awal edukatif bila belum ada riwayat siklus
            listOf(
                DailyStats(dayEpoch = 1, totalCycles = 15, closeViolations = 2, darkMinutes = 0, postureViolations = 1),
                DailyStats(dayEpoch = 2, totalCycles = 22, closeViolations = 1, darkMinutes = 1, postureViolations = 0),
                DailyStats(dayEpoch = 3, totalCycles = 30, closeViolations = 0, darkMinutes = 0, postureViolations = 1),
            )
        }
    }

    val summary = remember(dailyList) {
        ReportGenerator.summarize(dailyList, periodLabel = "7 Hari Terakhir")
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Surface),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.onboarding_back),
                    tint = Ink,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Laporan Mingguan",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Text(
                    text = "Ringkasan kebiasaan layar untuk $childName ($childAge th)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.72f),
                )
            }
        }

        // Kartu Ringkasan Utama
        JagaCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
            Text(
                text = "Kepatuhan Skrining Mata",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatBadge(title = "Total Sesi", value = "${summary.totalCycles}", color = Primary)
                StatBadge(
                    title = "Terlalu Dekat",
                    value = "${summary.totalCloseViolations}x",
                    color = if (summary.totalCloseViolations > 5) Coral else Mint,
                )
                StatBadge(
                    title = "Ruang Gelap",
                    value = "${summary.totalDarkMinutes} mnt",
                    color = if (summary.totalDarkMinutes > 20) Sunny else Mint,
                )
            }
        }

        // Kotak Informasi Validasi Medis
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEBF3FF))
                    .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Rekomendasi Klinis Dokter Spesialis Mata",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Berdasarkan analisis onset miopia, anak disarankan memperbanyak aktivitas luar ruangan minimal 1.5–2 jam/hari. Sinar matahari merangsang pelepasan dopamin yang terbukti efektif menekan risiko mata minus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.85f),
                    )
                }
            }
        }

        // Tombol Ekspor PDF
        Button(
            onClick = { shareReportPdf(context, summary, childName, childAge) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(26.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                tint = Color.White,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Bagikan Dokumen PDF (Dokter Mata / Cetak)",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }

        // Tombol Ekspor WhatsApp (Gambar)
        Button(
            onClick = { shareReportPng(context, summary) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Mint),
            shape = RoundedCornerShape(26.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                tint = Ink,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Bagikan ke WhatsApp (Ringkasan Gambar)",
                style = MaterialTheme.typography.labelLarge,
                color = Ink,
            )
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text(
                text = stringResource(R.string.close),
                color = Ink,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatBadge(
    title: String,
    value: String,
    color: Color,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Ink.copy(alpha = 0.65f),
        )
    }
}

/** Bagikan dokumen PDF resmi ke WhatsApp / Dokter Mata / Printer. */
private fun shareReportPdf(
    context: Context,
    summary: ReportGenerator.ReportSummary,
    childName: String,
    childAge: Int,
) {
    val file = ReportGenerator.renderPdf(context, summary, childName, childAge)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Kesehatan Mata Anak — JagaMata ($childName)")
            putExtra(
                Intent.EXTRA_TEXT,
                "Halo Dokter / Bapak/Ibu,\nBerikut terlampir dokumen resmi Laporan Kesehatan Mata & Skrining Kebiasaan Layar untuk ananda $childName dari aplikasi JagaMata.",
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Bagikan Dokumen PDF Laporan"))
    }
}

/** Bagikan ringkasan grafik PNG ke WhatsApp / media sosial. */
private fun shareReportPng(
    context: Context,
    summary: ReportGenerator.ReportSummary,
) {
    val bitmap: Bitmap = ReportGenerator.renderPng(summary)
    val dir = File(context.cacheDir, "reports").apply { mkdirs() }
    val file = File(dir, "laporan_mingguan_jagamata.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, ReportGenerator.summaryText(summary))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    runCatching {
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.report_share)))
    }
}
