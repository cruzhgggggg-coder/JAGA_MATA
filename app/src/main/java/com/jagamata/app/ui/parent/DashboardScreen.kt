package com.jagamata.app.ui.parent

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import kotlinx.coroutines.launch
import com.jagamata.app.detection.RiskLevel
import com.jagamata.app.ui.components.BottomNavBar
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.components.Sparkle
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.Cream
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt
import com.jagamata.app.ui.viewModel.DashboardViewModel
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
    viewModel: DashboardViewModel = DashboardViewModel(),
    onTab: (Int) -> Unit = {},
    onOpenChildProfile: () -> Unit = {},
    onOpenDailyStats: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember { com.jagamata.app.data.SettingsRepository(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val minDistanceFloat by settings.closeThresholdCm.collectAsState(initial = 30f)
    var stagedDistance by remember(minDistanceFloat) { mutableIntStateOf(minDistanceFloat.toInt()) }
    var isSavedFeedback by remember { mutableStateOf(false) }

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
                onClick = onOpenDailyStats,
            )
            CycleCard(state = state)
            TipsCard()
            ShareButton(onClick = onOpenReport)
            MinDistanceCard(
                valueCm = stagedDistance,
                isSaved = isSavedFeedback,
                onDecrease = {
                    stagedDistance = (stagedDistance - 5).coerceIn(20, 50)
                },
                onIncrease = {
                    stagedDistance = (stagedDistance + 5).coerceIn(20, 50)
                },
                onSaveDistance = {
                    scope.launch {
                        settings.setCloseThresholdCm(stagedDistance.toFloat())
                        isSavedFeedback = true
                        delay(2000L)
                        isSavedFeedback = false
                    }
                },
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

/** Tren jarak harian dummy (cm) — desain 2669: rendah di S, puncak K, turun J, puncak S, turun M. */
private val DummyDailyCm = listOf(48f, 52f, 50f, 54f, 51f, 55f, 53f)

@Composable
private fun WeeklyReportCard(
    state: DashboardViewModel.UiState,
    onClick: (() -> Unit)? = null,
) {
    val avgCm =
        state.lastCycle?.avgDistanceCm?.let { String.format(Locale.US, "%.0f cm", it) }
            ?: stringResource(R.string.s09_report_threshold)
    val statusLabel =
        when (state.riskLevel) {
            RiskLevel.OK -> stringResource(R.string.s09_report_status_good)
            RiskLevel.WARNING -> stringResource(R.string.status_warning)
            RiskLevel.BLOCKED -> stringResource(R.string.status_blocked)
        }
    val statusColor =
        when (state.riskLevel) {
            RiskLevel.OK -> Mint
            RiskLevel.WARNING -> Sunny
            RiskLevel.BLOCKED -> com.jagamata.app.ui.theme.Coral
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

        // Grafik garis 7 hari + label ambang
        Box {
            WeeklyLineChart(
                values = DummyDailyCm,
                thresholdCm = 50f,
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
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padTop = 8f
        val padBottom = 8f
        val usableH = h - padTop - padBottom
        val vMin = 46f
        val vMax = 56f

        fun yOf(v: Float): Float = padTop + usableH * ((vMax - v) / (vMax - vMin))

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

        // Kurva data halus (cubic, kontrol horizontal di tengah langkah — gaya desain)
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
                    Text(
                        text = String.format(Locale.US, "%d cm", valueCm),
                        style = MaterialTheme.typography.displayMedium,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
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
                colors = ButtonDefaults.buttonColors(
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
