package com.jagamata.app.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.data.StatsRepository
import com.jagamata.app.data.localDayLabel
import com.jagamata.app.detection.Thresholds
import com.jagamata.app.ui.child.DesignSparkle
import com.jagamata.app.ui.child.jagaBottomNavItems
import com.jagamata.app.ui.components.BottomNavBar
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt

/**
 * Layar 06 — Statistik Harian.
 * Tab aktif Pantau (index 1) mengikuti PNG desain.
 *
 * T13: grafik per jam, tanggal, chip zona, dan kalimat insight semuanya
 * dihitung dari data hari ini (snapshot + ambang aktif) — bukan string statis.
 * Layout/token desain dipertahankan.
 */
@Composable
fun DailyStatsScreen(
    onBack: () -> Unit = {},
    onTab: (Int) -> Unit = {},
    initialTab: Int = 1,
) {
    val context = LocalContext.current
    val settings = remember { SettingsRepository(context) }
    val closeCm by settings.closeThresholdCm.collectAsState(initial = 30f)

    var avgDistanceCm by remember { mutableIntStateOf(0) }
    var safeDurationMinutes by remember { mutableIntStateOf(0) }
    var safeSharePercent by remember { mutableStateOf<Int?>(null) }
    var hourlyAvg by remember { mutableStateOf<Map<Int, Float>>(emptyMap()) }

    val todayLabel = remember { localDayLabel(System.currentTimeMillis()) }

    // M10 + T13: semua angka dari jendela "hari ini" via StatsRepository.
    LaunchedEffect(Unit) {
        val db = com.jagamata.app.data.AppDatabase.getInstance(context)
        val stats = StatsRepository(db)
        avgDistanceCm = stats.avgDistanceCmToday()?.toInt() ?: 0
        safeDurationMinutes = stats.safeMinutesToday()
        safeSharePercent = stats.safeSharePercentToday()
        hourlyAvg = stats.hourlyAvgDistanceToday()
    }

    // M23/T13: chip zona dari rata-rata jarak hari ini vs zona ambang aktif.
    val safeCm = closeCm + Thresholds.SAFE_ZONE_MARGIN_CM
    val statusText: String
    val statusColor: Color
    when {
        avgDistanceCm <= 0 -> {
            statusText = stringResource(R.string.status_no_data)
            statusColor = SurfaceAlt
        }
        avgDistanceCm < closeCm -> {
            statusText = stringResource(R.string.zone_danger)
            statusColor = Coral
        }
        avgDistanceCm < safeCm -> {
            statusText = stringResource(R.string.zone_warn)
            statusColor = Sunny
        }
        else -> {
            statusText = stringResource(R.string.zone_safe)
            statusColor = Mint
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp),
        ) {
            StatsHeader(dateLabel = todayLabel)
            Spacer(modifier = Modifier.height(14.dp))
            ChartCard(hourlyAvg = hourlyAvg, closeCm = closeCm, safeCm = safeCm)
            Spacer(modifier = Modifier.height(14.dp))
            StatCardRow(
                avgDistanceCm = avgDistanceCm,
                safeDurationMinutes = safeDurationMinutes,
                statusText = statusText,
                statusColor = statusColor,
            )
            Spacer(modifier = Modifier.height(14.dp))
            InsightCard(safeSharePercent = safeSharePercent)
            Spacer(modifier = Modifier.weight(1f))
        }
        BottomNavBar(
            items = jagaBottomNavItems(),
            selectedIndex = initialTab,
            onSelect = onTab,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Header 06: judul + tanggal hari ini (dari LocalDate.now, bukan statis) + 2 kilau kuning. */
@Composable
private fun StatsHeader(dateLabel: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(R.string.s06_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Ink.copy(alpha = 0.72f),
            )
        }
        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-22).dp, y = 12.dp)
                    .size(15.dp),
        )
        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-58).dp, y = 38.dp)
                    .size(10.dp),
        )
    }
}

/** Satu batang grafik per-jam: tinggi & warna dihitung dari data + zona ambang. */
private data class HourBar(
    val label: String,
    val avgCm: Float?,
)

/**
 * Batang jam → tinggi (dp) & warna zona. Tanpa data = batang stub tipis
 * berwarna Divider (celah jujur, bukan angka karangan).
 */
private fun barHeightDp(avgCm: Float?): Float = if (avgCm == null) 8f else (24f + (avgCm.coerceIn(0f, 60f) / 60f) * 82f)

private fun barColor(
    avgCm: Float?,
    closeCm: Float,
    safeCm: Float,
): Color =
    when {
        avgCm == null -> Divider
        avgCm < closeCm -> Coral
        avgCm < safeCm -> Sunny
        else -> Mint
    }

/** Kartu grafik batang "Jarak per jam" dengan legenda zona. */
@Composable
private fun ChartCard(
    hourlyAvg: Map<Int, Float>,
    closeCm: Float,
    safeCm: Float,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .background(Surface, RoundedCornerShape(24.dp))
                .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.s06_chart_title),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(stringResource(R.string.s06_legend_aman), Mint)
            LegendItem(stringResource(R.string.s06_legend_waspada), Sunny)
            LegendItem(stringResource(R.string.s06_legend_bahaya), Coral)
        }
        Spacer(modifier = Modifier.height(16.dp))
        BarChart(hourlyAvg = hourlyAvg, closeCm = closeCm, safeCm = safeCm)
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .background(color, CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Ink,
        )
    }
}

/**
 * Bar chart 7 batang (label 08..20 dari desain). Tiap batang = rata-rata jarak
 * snapshot hari itu pada bucket 2 jam [label, label+2) — zona warna dari
 * ambang aktif; hari/bucket tanpa data = batang stub (T13).
 */
@Composable
private fun BarChart(
    hourlyAvg: Map<Int, Float>,
    closeCm: Float,
    safeCm: Float,
) {
    val hourLabels = stringArrayResource(R.array.s06_hours)
    val bars =
        remember(hourLabels, hourlyAvg, closeCm, safeCm) {
            hourLabels.map { label ->
                val startHour = label.toIntOrNull() ?: return@map HourBar(label, null)
                val samples =
                    (startHour until startHour + 2).mapNotNull { hourlyAvg[it] }
                HourBar(label, samples.takeIf { it.isNotEmpty() }?.average()?.toFloat())
            }
        }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { bar ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier =
                        Modifier
                            .width(24.dp)
                            .height(barHeightDp(bar.avgCm).dp)
                            .background(barColor(bar.avgCm, closeCm, safeCm), RoundedCornerShape(12.dp)),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink.copy(alpha = 0.72f),
                )
            }
        }
    }
}

/** Dua kartu statistik (Jarak rata-rata & Durasi aman) — jendela hari ini (M10). */
@Composable
private fun StatCardRow(
    avgDistanceCm: Int = 0,
    safeDurationMinutes: Int = 0,
    statusText: String = stringResource(R.string.zone_safe),
    statusColor: Color = Mint,
) {
    val distanceText = if (avgDistanceCm > 0) "$avgDistanceCm cm" else "0 cm"
    val distanceProgress = if (avgDistanceCm > 0) (avgDistanceCm / 60f).coerceIn(0.1f, 1f) else 0f
    val durationText = if (safeDurationMinutes > 0) "$safeDurationMinutes menit" else "0 menit"
    val durationProgress = if (safeDurationMinutes > 0) (safeDurationMinutes / 60f).coerceIn(0.1f, 1f) else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        StatCard(
            label = stringResource(R.string.s06_stat_distance),
            value = distanceText,
            progress = distanceProgress,
            statusText = statusText,
            statusColor = statusColor,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.s06_stat_duration),
            value = durationText,
            progress = durationProgress,
            statusText = statusText,
            statusColor = statusColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    progress: Float,
    statusText: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .height(166.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .background(Surface, RoundedCornerShape(20.dp))
                .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .background(SurfaceAlt, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Ink.copy(alpha = 0.72f),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            color = Ink,
        )
        Spacer(modifier = Modifier.weight(1f))
        // Progress bar
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(Divider, RoundedCornerShape(5.dp)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .background(Mint, RoundedCornerShape(5.dp)),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        ZoneStatusChip(text = statusText, containerColor = statusColor)
    }
}

/** Chip status zona (desain: "✓ Aman") — warna & teks dari data hari ini. */
@Composable
private fun ZoneStatusChip(
    text: String,
    containerColor: Color,
) {
    Row(
        modifier =
            Modifier
                .background(containerColor, RoundedCornerShape(11.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = Ink,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Ink,
        )
    }
}

/**
 * Kartu insight — kalimat dihitung dari data hari ini (T13):
 * persentase siklus tanpa pelanggaran; tanpa data → pesan kosong yang jujur.
 */
@Composable
private fun InsightCard(safeSharePercent: Int?) {
    val (title, body) =
        when {
            safeSharePercent == null ->
                stringResource(R.string.daily_insight_title_empty) to
                    stringResource(R.string.daily_insight_empty)
            safeSharePercent >= 80 ->
                stringResource(R.string.s06_health_title) to
                    stringResource(R.string.daily_insight_safe, safeSharePercent)
            else ->
                stringResource(R.string.daily_insight_title_warn) to
                    stringResource(R.string.daily_insight_warn, safeSharePercent)
        }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .background(Surface, RoundedCornerShape(24.dp))
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KikoImage(
            expression = if (safeSharePercent != null && safeSharePercent >= 80) KikoExpression.SENANG else KikoExpression.NETRAL,
            modifier = Modifier.size(width = 85.dp, height = 90.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink.copy(alpha = 0.72f),
            )
        }
    }
}
