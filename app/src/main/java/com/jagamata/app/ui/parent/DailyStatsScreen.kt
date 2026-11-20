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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
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

/** Tinggi batang grafik per jam (dp) — urutan 08..20 sesuai desain. */
private val barHeights = listOf(92f, 70f, 106f, 40f, 88f, 62f, 98f)

/** Warna batang per jam: Aman / Waspada / Aman / Bahaya / Aman / Waspada / Aman. */
private val barColors = listOf(Mint, Sunny, Mint, Coral, Mint, Sunny, Mint)

/**
 * Layar 06 — Statistik Harian.
 * Tab aktif Pantau (index 1) mengikuti PNG desain.
 */
@Composable
fun DailyStatsScreen(
    onBack: () -> Unit = {},
    onTab: (Int) -> Unit = {},
    initialTab: Int = 1,
) {
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
            StatsHeader()
            Spacer(modifier = Modifier.height(14.dp))
            ChartCard()
            Spacer(modifier = Modifier.height(14.dp))
            StatCardRow()
            Spacer(modifier = Modifier.height(14.dp))
            InsightCard()
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

/** Header 06: judul + tanggal + 2 kilau kuning. */
@Composable
private fun StatsHeader() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(R.string.s06_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.s06_date),
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

/** Kartu grafik batang "Jarak per jam" dengan legenda zona. */
@Composable
private fun ChartCard() {
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
        BarChart()
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

/** Bar chart 7 batang (08–20) dengan tinggi & warna zona sesuai desain. */
@Composable
private fun BarChart() {
    val hours = stringArrayResource(R.array.s06_hours)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        hours.forEachIndexed { i, hour ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier =
                        Modifier
                            .width(24.dp)
                            .height(barHeights[i].dp)
                            .background(barColors[i], RoundedCornerShape(12.dp)),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = hour,
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink.copy(alpha = 0.72f),
                )
            }
        }
    }
}

/** Dua kartu statistik (Jarak rata-rata & Durasi aman). */
@Composable
private fun StatCardRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        StatCard(
            label = stringResource(R.string.s06_stat_distance),
            value = stringResource(R.string.s06_value_distance),
            progress = 0.81f,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.s06_stat_duration),
            value = stringResource(R.string.s06_value_duration),
            progress = 0.81f,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    progress: Float,
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
        AmanChip()
    }
}

/** Chip hijau "✓ Aman". */
@Composable
private fun AmanChip() {
    Row(
        modifier =
            Modifier
                .background(Mint, RoundedCornerShape(11.dp))
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
            text = stringResource(R.string.s06_status_aman),
            style = MaterialTheme.typography.labelSmall,
            color = Ink,
        )
    }
}

/** Kartu insight "Mata kamu sehat!" + Kiko. */
@Composable
private fun InsightCard() {
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
            expression = KikoExpression.SENANG,
            modifier = Modifier.size(width = 85.dp, height = 90.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(R.string.s06_health_title),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.s06_health_body),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink.copy(alpha = 0.72f),
            )
        }
    }
}
