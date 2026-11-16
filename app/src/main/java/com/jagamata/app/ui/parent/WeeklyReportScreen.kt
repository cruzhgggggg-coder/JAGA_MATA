package com.jagamata.app.ui.parent

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.jagamata.app.R
import com.jagamata.app.report.ReportGenerator
import java.io.File
import java.io.FileOutputStream

/**
 * Laporan mingguan (PRD US-12, DEV-23): ringkasan 7 hari + PNG 1080x1920
 * + share sheet (ACTION_SEND / WhatsApp).
 */
@Composable
fun WeeklyReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val summary =
        remember {
            ReportGenerator.summarize(
                perDay =
                    listOf(
                        com.jagamata.app.data.entity.DailyStats(0, 0, 0, 0, 0),
                    ),
                periodLabel = "7 hari terakhir",
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.report_title),
            style = MaterialTheme.typography.displayMedium,
        )
        Text(
            text = stringResource(R.string.report_period, summary.periodLabel),
            style = MaterialTheme.typography.bodyMedium,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.report_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = ReportGenerator.summaryText(summary),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Button(
            onClick = { shareReport(context, summary) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.report_share))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.close))
        }
    }
}

/**
 * Render PNG lalu bagikan lewat ACTION_SEND (WhatsApp dan lainnya).
 * File ditulis ke cacheDir/reports/ (bukan folder gambar pengguna) —
 * mematuhi aturan privasi K9 (tanpa foto kamera).
 */
private fun shareReport(
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
