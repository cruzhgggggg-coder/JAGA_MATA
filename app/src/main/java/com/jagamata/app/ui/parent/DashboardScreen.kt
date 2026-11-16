package com.jagamata.app.ui.parent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.detection.RiskLevel
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.InkSoft
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.viewModel.DashboardViewModel

/**
 * Dasbor orang tua (PRD US-10): 3 angka pekan ini, strip siklus live,
 * grafik batang 7 hari dari `daily_stats`. Compose-only.
 */
@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit,
    onOpenReport: () -> Unit,
    viewModel: DashboardViewModel = DashboardViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.displayMedium,
        )
        WeeklyStatsCard(state)
        LiveCycleStrip(
            riskLevel = state.riskLevel,
            distanceCm = state.lastCycle?.avgDistanceCm,
            lux = state.lastCycle?.avgLux,
            pitchDeg = state.lastCycle?.maxPitchDeg,
            isMonitoring = state.isMonitoring,
        )
        WeeklyChartCard(state.dailyStats.map { it.closeViolations.toFloat() }, state.dailyStats.map { it.postureViolations.toFloat() })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onOpenReport, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.nav_report))
            }
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.nav_settings))
            }
        }
    }
}

@Composable
private fun WeeklyStatsCard(state: DashboardViewModel.UiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_week_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatCell(
                    label = stringResource(R.string.stat_close),
                    value = state.weeklyCloseViolations.toString(),
                    unit = stringResource(R.string.stat_unit_times),
                    color = Coral,
                )
                StatCell(
                    label = stringResource(R.string.stat_dark),
                    value = state.weeklyDarkMinutes.toString(),
                    unit = stringResource(R.string.stat_unit_minutes),
                    color = Sunny,
                )
                StatCell(
                    label = stringResource(R.string.stat_posture),
                    value = state.weeklyPostureViolations.toString(),
                    unit = stringResource(R.string.stat_unit_times),
                    color = Mint,
                )
            }
        }
    }
}

@Composable
private fun WeeklyChartCard(
    closeValues: List<Float>,
    postureValues: List<Float>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_chart_title),
                style = MaterialTheme.typography.titleMedium,
            )
            WeeklyBarChart(
                closeValues = closeValues,
                postureValues = postureValues,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    unit: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            color = color,
        )
        Text(text = unit, style = MaterialTheme.typography.labelSmall, color = InkSoft)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Ink)
    }
}

@Composable
private fun LiveCycleStrip(
    riskLevel: RiskLevel,
    distanceCm: Float?,
    lux: Float?,
    pitchDeg: Float?,
    isMonitoring: Boolean,
) {
    val (label, color) =
        when (riskLevel) {
            RiskLevel.OK -> stringResource(R.string.status_ok) to Mint
            RiskLevel.WARNING -> stringResource(R.string.status_warning) to Sunny
            RiskLevel.BLOCKED -> stringResource(R.string.status_blocked) to Coral
        }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(12.dp)
                        .height(48.dp)
                        .background(color, RoundedCornerShape(6.dp)),
            )
            Column(
                modifier =
                    Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
            ) {
                Text(
                    text =
                        if (isMonitoring) {
                            stringResource(R.string.dashboard_live_title) + ": " + label
                        } else {
                            stringResource(R.string.dashboard_no_data)
                        },
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text =
                        (
                            distanceCm?.let { stringResource(R.string.detection_distance, it) }
                                ?: stringResource(R.string.detection_unknown_distance)
                        ) +
                            " · " + (lux?.let { stringResource(R.string.detection_lux, it) } ?: "—") +
                            " · " + (pitchDeg?.let { stringResource(R.string.detection_pitch, it) } ?: "—"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(
    closeValues: List<Float>,
    postureValues: List<Float>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val n = 7
        val maxVal = (closeValues + postureValues + listOf(1f)).max()
        val barWidth = size.width / (n * 2f)
        val usableHeight = size.height - 8f

        for (i in 0 until n) {
            val close = closeValues.getOrElse(i) { 0f }
            val posture = postureValues.getOrElse(i) { 0f }
            val x = i * (size.width / n)

            val closeHeight = usableHeight * (close / maxVal)
            drawRect(
                color = Coral,
                topLeft = Offset(x + barWidth * 0.3f, usableHeight - closeHeight),
                size = Size(barWidth * 0.8f, closeHeight),
            )
            val postureHeight = usableHeight * (posture / maxVal)
            drawRect(
                color = Sunny,
                topLeft = Offset(x + barWidth * 1.3f, usableHeight - postureHeight),
                size = Size(barWidth * 0.8f, postureHeight),
            )
        }
        drawLine(
            color = InkSoft,
            start = Offset(0f, usableHeight),
            end = Offset(size.width, usableHeight),
            strokeWidth = 2f,
        )
    }
}
