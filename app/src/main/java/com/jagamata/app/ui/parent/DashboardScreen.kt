package com.jagamata.app.ui.parent

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.data.entity.DailyStats
import com.jagamata.app.detection.RiskLevel
import com.jagamata.app.ui.components.BottomNavBar
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.components.Sparkle
import com.jagamata.app.ui.components.ZoneChip
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Cream
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.PrimaryDark
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt
import com.jagamata.app.ui.viewModel.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 09 Dashboard Orang Tua — rework visual dari desain FigCraft 2669.
 * Data tetap dari [DashboardViewModel] (stats, siklus live, grafik harian).
 * Signature publik dipertahankan: onOpenSettings / onOpenReport.
 * [onTab] opsional untuk bottom nav (default no-op bila belum di-wire).
 */
@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit,
    onOpenReport: () -> Unit,
    onTab: (Int) -> Unit = {},
    onOpenChildProfile: () -> Unit = {},
    onOpenDailyStats: () -> Unit = {},
) {
    val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember { com.jagamata.app.data.SettingsRepository(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val minDistanceFloat by settings.closeThresholdCm.collectAsState(initial = 30f)
    var stagedDistance by remember(minDistanceFloat) { mutableIntStateOf(minDistanceFloat.toInt()) }
    var isSavedFeedback by remember { mutableStateOf(false) }
    var showDistanceDialog by remember { mutableStateOf(false) }

    if (showDistanceDialog) {
        var tempCm by remember { mutableFloatStateOf(minDistanceFloat) }
        AlertDialog(
            onDismissRequest = { showDistanceDialog = false },
            title = {
                Text(
                    text = "Atur Jarak Minimum Layar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Batas jarak aman mata ke layar gadget. Jika lebih dekat dari batas ini, alarm & overlay akan muncul.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        IconButton(
                            onClick = { tempCm = (tempCm - 5f).coerceIn(20f, 50f) },
                            modifier = Modifier.size(44.dp).background(SurfaceAlt, CircleShape),
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Kurang 5 cm", tint = Ink)
                        }

                        Text(
                            text = "${tempCm.toInt()} cm",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )

                        IconButton(
                            onClick = { tempCm = (tempCm + 5f).coerceIn(20f, 50f) },
                            modifier = Modifier.size(44.dp).background(SurfaceAlt, CircleShape),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Tambah 5 cm", tint = Ink)
                        }
                    }

                    Slider(
                        value = tempCm,
                        onValueChange = { tempCm = it },
                        valueRange = 20f..50f,
                        steps = 5,
                        colors =
                            SliderDefaults.colors(
                                activeTrackColor = Primary,
                                inactiveTrackColor = SurfaceAlt,
                                thumbColor = PrimaryDark,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "Rekomendasi dokter spesialis mata: 30–40 cm untuk mencegah miopia (rabun jauh).",
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            settings.setCloseThresholdCm(tempCm)
                            stagedDistance = tempCm.toInt()
                            Toast.makeText(context, "Jarak minimum berhasil diubah ke ${tempCm.toInt()} cm!", Toast.LENGTH_SHORT).show()
                        }
                        showDistanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDistanceDialog = false }) {
                    Text("Batal", color = Ink)
                }
            },
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header: judul + sparkle, maskot Kiko free-standing di kanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = stringResource(R.string.s09_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Sparkle(
                            modifier =
                                Modifier
                                    .padding(top = 2.dp)
                                    .size(14.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.s09_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.72f),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                KikoImage(
                    expression = KikoExpression.SENANG,
                    modifier =
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenChildProfile),
                )
            }

            WeeklyReportCard(
                state = state,
                thresholdCm = minDistanceFloat,
                onClick = onOpenDailyStats,
            )
            CycleCard(state = state)
            PausedSessionsCard(pausedCount = state.pausedSessionsToday)
            TipsCard()
            ShareButton(onClick = onOpenReport)
            MinDistanceCard(
                valueCm = stagedDistance,
                isSaved = isSavedFeedback,
                onDecrease = {
                    val next = (stagedDistance - 5).coerceIn(20, 50)
                    stagedDistance = next
                    scope.launch {
                        settings.setCloseThresholdCm(next.toFloat())
                        isSavedFeedback = true
                        delay(1500L)
                        isSavedFeedback = false
                    }
                },
                onIncrease = {
                    val next = (stagedDistance + 5).coerceIn(20, 50)
                    stagedDistance = next
                    scope.launch {
                        settings.setCloseThresholdCm(next.toFloat())
                        isSavedFeedback = true
                        delay(1500L)
                        isSavedFeedback = false
                    }
                },
                onSaveDistance = {
                    scope.launch {
                        settings.setCloseThresholdCm(stagedDistance.toFloat())
                        isSavedFeedback = true
                        delay(2000L)
                        isSavedFeedback = false
                    }
                },
                onEditClick = { showDistanceDialog = true },
                onOpenSettings = onOpenSettings,
            )
        }

        BottomNavBar(
            items = defaultBottomNavItems(),
            selectedIndex = 3,
            onSelect = onTab,
        )
    }
}

/** Placeholder grafik desain 2669 (cm) — hanya dipakai saat benar-benar belum ada data riil. */
private val DummyDailyCm = listOf(48f, 52f, 50f, 54f, 51f, 55f, 53f)

/**
 * Nilai grafik 7 hari dari data riil: rata-rata jarak per hari dari snapshot bila ada,
 * lalu estimasi dari hitungan pelanggaran terlalu dekat pada rollup harian.
 */
private fun buildWeeklyChartValues(
    state: DashboardViewModel.UiState,
    thresholdCm: Float,
): List<Float> {
    val statsByDay = state.dailyStats.associateBy { it.dayEpoch }
    val epochs = (statsByDay.keys + state.dailyAvgDistanceCm.keys).sorted().takeLast(7)
    return epochs.mapNotNull { epoch ->
        state.dailyAvgDistanceCm[epoch]
            ?: statsByDay[epoch]?.let { estimateDayDistanceCm(it, thresholdCm) }
    }
}

/** Rollup harian hanya menyimpan hitungan pelanggaran; jarak diperkirakan dari ambang + margin - penalti pelanggaran. */
private fun estimateDayDistanceCm(
    day: DailyStats,
    thresholdCm: Float,
): Float = (thresholdCm + 5f - day.closeViolations * 2.5f).coerceIn(20f, 60f)

@Composable
private fun WeeklyReportCard(
    state: DashboardViewModel.UiState,
    thresholdCm: Float,
    onClick: (() -> Unit)? = null,
) {
    val avgCm =
        state.lastCycle?.avgDistanceCm?.let { String.format(Locale.US, "%.0f cm", it) }
            ?: "-- cm"
    val statusLabel =
        if (state.lastCycle == null) {
            "Belum Ada Data"
        } else {
            when (state.riskLevel) {
                RiskLevel.OK -> stringResource(R.string.s09_report_status_good)
                RiskLevel.WARNING -> stringResource(R.string.status_warning)
                RiskLevel.BLOCKED -> stringResource(R.string.status_blocked)
            }
        }
    val statusColor =
        if (state.lastCycle == null) {
            SurfaceAlt
        } else {
            when (state.riskLevel) {
                RiskLevel.OK -> Mint
                RiskLevel.WARNING -> Sunny
                RiskLevel.BLOCKED -> com.jagamata.app.ui.theme.Coral
            }
        }

    val cardModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    JagaCard(
        modifier = cardModifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.s09_report_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.s09_report_avg_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                )
            }
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(statusColor)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = avgCm,
                style = MaterialTheme.typography.displayMedium,
                color = Ink,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.s09_report_delta),
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val realValues: List<Float> =
            remember(state.dailyStats, state.dailyAvgDistanceCm, thresholdCm) {
                buildWeeklyChartValues(state, thresholdCm)
            }
        val chartValues: List<Float> = if (realValues.isEmpty()) DummyDailyCm else realValues
        val showEmptyState = realValues.size < 2

        // Grafik garis 7 hari + label ambang
        Box {
            WeeklyLineChart(
                values = chartValues,
                thresholdCm = thresholdCm,
                emptyState = showEmptyState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(90.dp),
            )
            Text(
                text = stringResource(R.string.s09_report_threshold),
                style = MaterialTheme.typography.labelSmall,
                color = Ink.copy(alpha = 0.72f),
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 28.dp, end = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("S", "S", "R", "K", "J", "S", "M").forEach { d ->
                Text(
                    text = d,
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Grafik garis mingguan: kurva halus biru #4A86E8 + titik ring + garis ambang putus-putus.
 * Sumbu nilai [46,56] cm; 50 cm ≈ posisi garis ambang desain.
 */
@Composable
private fun WeeklyLineChart(
    values: List<Float>,
    thresholdCm: Float,
    modifier: Modifier = Modifier,
    emptyState: Boolean = values.size < 2,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val padTop = 8f
            val padBottom = 8f
            val usableH = h - padTop - padBottom
            val vMin = 20f
            val vMax = 60f

            fun yOf(v: Float): Float = padTop + usableH * ((vMax - v.coerceIn(vMin, vMax)) / (vMax - vMin))

            // Garis putus-putus ambang
            val thresholdY = yOf(thresholdCm)
            val dash = 10f
            var x = 0f
            while (x < w) {
                drawLine(
                    color = Mint,
                    start = Offset(x, thresholdY),
                    end = Offset(minOf(x + dash, w), thresholdY),
                    strokeWidth = 2f,
                )
                x += dash * 2
            }

            // Kurva data halus jika ada data riil
            val n = values.size
            if (n > 1) {
                val stepX = w / (n - 1)
                val path = Path()
                path.moveTo(0f, yOf(values[0]))
                for (i in 1 until n) {
                    val x0 = (i - 1) * stepX
                    val x1 = i * stepX
                    val y0 = yOf(values[i - 1])
                    val y1 = yOf(values[i])
                    path.cubicTo(x0 + stepX * 0.5f, y0, x1 - stepX * 0.5f, y1, x1, y1)
                }
                drawPath(path, color = Primary, style = Stroke(width = 3f))

                // Titik ring; titik terakhir di-highlight (fill + ring putih)
                values.forEachIndexed { i, v ->
                    val px = i * stepX
                    val py = yOf(v)
                    if (i == values.lastIndex) {
                        drawCircle(color = Primary, radius = 7f, center = Offset(px, py))
                        drawCircle(color = Surface, radius = 5.5f, center = Offset(px, py), style = Stroke(width = 3f))
                    } else {
                        drawCircle(color = Surface, radius = 5f, center = Offset(px, py))
                        drawCircle(color = Primary, radius = 5f, center = Offset(px, py), style = Stroke(width = 3f))
                    }
                }
            }
        }
        if (emptyState) {
            Text(
                text = "Belum ada riwayat aktivitas pekan ini",
                style = MaterialTheme.typography.bodySmall,
                color = Ink.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun CycleCard(state: DashboardViewModel.UiState) {
    JagaCard(
        containerColor = Surface,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.s09_cycle_active),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
        Spacer(modifier = Modifier.height(8.dp))
        MonitoringStatusLine(state = state)
        Spacer(modifier = Modifier.height(8.dp))
        val dist =
            state.lastCycle?.avgDistanceCm?.let { String.format(Locale.US, "%.0f cm", it) }
                ?: "-- cm"
        val lux =
            state.lastCycle?.avgLux?.let { String.format(Locale.US, "%.0f lux", it) }
                ?: "-- lux"
        val pitch =
            state.lastCycle?.maxPitchDeg?.let { String.format(Locale.US, "%.0f°", it) }
                ?: "--°"
        val risk =
            when (state.riskLevel) {
                RiskLevel.OK -> stringResource(R.string.zone_safe)
                RiskLevel.WARNING -> stringResource(R.string.zone_warn)
                RiskLevel.BLOCKED -> stringResource(R.string.zone_danger)
            }
        val timeLabel =
            state.lastCycle?.startedAtMs?.let {
                val sdf = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(java.util.Date(it))
            } ?: "baru"
        val line =
            if (state.lastCycle != null) {
                "Siklus $timeLabel · 📏 $dist · 🌑 $lux · 🙇 $pitch ($risk)"
            } else {
                "Belum ada rekaman siklus · Tekan Pantau Live"
            }
        Text(
            text = line,
            style = MaterialTheme.typography.labelLarge,
            color = Primary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.s09_cycle_meta),
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
        )
    }
}

/**
 * T5: baris status pemantauan sesungguhnya — "Pemantauan aktif" / "Dijeda sejak HH:mm" /
 * "Berhenti", dari [DetectionForegroundService.isRunning] + waktu jeda. Menggantikan
 * klaim statis "Siklus 1 menit — aktif" (audit V-S2).
 */
@Composable
private fun MonitoringStatusLine(state: DashboardViewModel.UiState) {
    val (text, color) =
        when {
            state.isMonitoring -> stringResource(R.string.monitoring_status_active) to Mint
            state.pausedSinceEpochMs > 0L ->
                stringResource(
                    R.string.monitoring_status_paused,
                    hhmm(state.pausedSinceEpochMs),
                ) to Sunny
            else -> stringResource(R.string.monitoring_status_stopped) to Coral
        }
    ZoneChip(text = text, containerColor = color)
}

/** Format HH:mm lokal untuk stempel waktu status. */
private fun hhmm(epochMs: Long): String = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date(epochMs))

/** Transparansi jeda sesi hari ini (DEV-38) — selalu tampil, netral/positif bila nol. */
@Composable
private fun PausedSessionsCard(pausedCount: Int) {
    val hasPause = pausedCount > 0
    JagaCard(
        containerColor = if (hasPause) Cream else Surface,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (hasPause) Sunny else Mint),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⏸",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
            }
            Text(
                text =
                    if (hasPause) {
                        stringResource(R.string.dashboard_paused_count, pausedCount)
                    } else {
                        stringResource(R.string.dashboard_paused_none)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
            )
        }
    }
}

@Composable
private fun TipsCard() {
    JagaCard(
        containerColor = Cream,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Sunny),
                contentAlignment = Alignment.Center,
            ) {
                BulbIcon(modifier = Modifier.size(13.dp, 19.dp))
            }
            Column {
                Text(
                    text = stringResource(R.string.s09_tips_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.s09_tips_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            }
        }
    }
}

/** Bohlam line-art desain (stroke #12263A 2px) — path dari SVG 2669. */
@Composable
private fun BulbIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sx = size.width / 12f
        val sy = size.height / 18f
        val path =
            Path().apply {
                // filamen
                moveTo(3f * sx, 15f * sy)
                lineTo(9f * sx, 15f * sy)
                moveTo(4f * sx, 18f * sy)
                lineTo(8f * sx, 18f * sy)
                // bohlam
                moveTo(6f * sx, 0f)
                cubicTo(2.7f * sx, 0f, 0f, 2.7f * sy, 0f, 6f * sy)
                cubicTo(0f, 8.2f * sy, 1.2f * sx, 10f * sy, 3f * sx, 11f * sy)
                lineTo(3f * sx, 12.5f * sy)
                lineTo(9f * sx, 12.5f * sy)
                lineTo(9f * sx, 11f * sy)
                cubicTo(10.8f * sx, 10f * sy, 12f * sx, 8.2f * sy, 12f * sx, 6f * sy)
                cubicTo(12f * sx, 2.7f * sy, 9.3f * sx, 0f, 6f * sx, 0f)
                close()
            }
        drawPath(path, color = Ink, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun ShareButton(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Mint)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.s09_share),
            style = MaterialTheme.typography.labelLarge,
            color = Ink,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MinDistanceCard(
    valueCm: Int,
    isSaved: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onSaveDistance: () -> Unit,
    onEditClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenSettings: () -> Unit,
) {
    JagaCard(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.s09_min_distance_label),
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                    Text(
                        text = "Batas ambang alarm: jika anak lebih dekat dari ini, alarm & overlay akan muncul",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.65f),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundStepButton(onClick = onDecrease) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "−",
                            tint = Ink,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable(onClick = onEditClick)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = String.format(Locale.US, "%d cm", valueCm),
                            style = MaterialTheme.typography.displayMedium,
                            color = Ink,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Ubah Ambang Jarak",
                            tint = Ink.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    RoundStepButton(onClick = onIncrease) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "+",
                            tint = Ink,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSaveDistance,
                shape = RoundedCornerShape(16.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isSaved) Mint else Primary,
                        contentColor = if (isSaved) Ink else Color.White,
                    ),
                modifier = Modifier.fillMaxWidth().height(46.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaved) "✓ Ambang Tersimpan ($valueCm cm)" else "Simpan Ambang Jarak",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun RoundStepButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceAlt)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
