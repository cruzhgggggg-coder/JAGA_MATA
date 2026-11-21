package com.jagamata.app.ui.live

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jagamata.app.R
import com.jagamata.app.detection.RiskLevel
import com.jagamata.app.service.DetectionForegroundService
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.jagamata.app.ui.child.DesignSparkle
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.child.jagaBottomNavItems
import com.jagamata.app.ui.components.BottomNavBar
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt
import kotlin.math.cos
import kotlin.math.sin

/**
 * 03 Pantau Jarak Live — pratinjau kamera, gauge jarak, status zona,
 * waktu bermain, bottom nav (tab Pantau).
 */
@Composable
fun PantauLiveScreen(
    onTab: (Int) -> Unit = {},
    initialTab: Int = 1,
    onPauseToggle: () -> Unit = {},
) {
    val context = LocalContext.current
    val isRunning by DetectionForegroundService.isRunning.collectAsState()
    val latest by DetectionForegroundService.latest.collectAsState()
    val riskLevel by DetectionForegroundService.riskLevel.collectAsState()

    val sessionStart = DetectionForegroundService.sessionStartEpochMs
    var elapsedSeconds by remember {
        mutableLongStateOf(
            if (isRunning && sessionStart > 0L) {
                ((System.currentTimeMillis() - sessionStart) / 1000L).coerceAtLeast(0L)
            } else 0L,
        )
    }
    LaunchedEffect(isRunning, sessionStart) {
        if (isRunning) {
            val startEpoch = if (sessionStart > 0L) sessionStart else System.currentTimeMillis()
            while (isActive) {
                elapsedSeconds = ((System.currentTimeMillis() - startEpoch) / 1000L).coerceAtLeast(0L)
                delay(1000L)
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    LaunchedEffect(Unit) {
        val hasCamera =
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasCamera && !DetectionForegroundService.isRunning.value) {
            DetectionForegroundService.start(context)
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
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp),
        ) {
            PantauHeader(isRunning = isRunning)
            Spacer(modifier = Modifier.height(14.dp))
            PreviewCard(distanceCm = latest?.distanceCm, riskLevel = riskLevel)
            Spacer(modifier = Modifier.height(14.dp))
            GaugeCard(distanceCm = latest?.distanceCm, riskLevel = riskLevel)
            Spacer(modifier = Modifier.height(14.dp))
            LightConditionCard(lux = latest?.lux)
            Spacer(modifier = Modifier.height(14.dp))
            TimeCard(
                isRunning = isRunning,
                elapsedSeconds = elapsedSeconds,
                onPauseToggle = onPauseToggle,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        BottomNavBar(
            items = jagaBottomNavItems(),
            selectedIndex = initialTab,
            onSelect = onTab,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Header 03: judul + kilau + pill status memantau. */
@Composable
private fun PantauHeader(isRunning: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.s03_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        Spacer(modifier = Modifier.width(32.dp))
        DesignSparkle(
            modifier = Modifier.offset(y = 5.dp).size(11.7.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        MonitoringPill(isRunning = isRunning)
    }
}

@Composable
private fun MonitoringPill(isRunning: Boolean) {
    val bg = if (isRunning) Mint else Sunny
    val label = if (isRunning) stringResource(R.string.s03_monitoring) else "Dijeda"
    Row(
        modifier =
            Modifier
                .background(bg, RoundedCornerShape(13.dp))
                .padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(Ink, CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Ink,
        )
    }
}

/** Kartu pratinjau kamera (353×240, biru): pill, wajah, badge jarak, Kiko. */
@Composable
private fun PreviewCard(
    distanceCm: Float?,
    riskLevel: RiskLevel,
) {
    val distText =
        distanceCm?.let { String.format(Locale.US, "%.0f cm", it) }
            ?: "-- cm"
    val kikoExpr =
        when (riskLevel) {
            RiskLevel.OK -> KikoExpression.FOKUS_PENGARIS
            RiskLevel.WARNING -> KikoExpression.KAGET
            RiskLevel.BLOCKED -> KikoExpression.KAGET
        }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(BlueTintDeep, RoundedCornerShape(24.dp)),
    ) {
        Row(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp, y = 12.dp)
                    .background(Surface, RoundedCornerShape(14.dp))
                    .padding(start = 12.dp, end = 13.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Ink,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.s03_camera),
                style = MaterialTheme.typography.labelSmall,
                color = Ink,
            )
        }
        // bingkai putus-putus mengelilingi wajah
        Box(
            modifier =
                Modifier
                    .offset(x = 26.dp, y = 56.dp)
                    .size(96.dp, 96.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.White,
                            cornerRadius = CornerRadius(20.dp.toPx()),
                            style =
                                Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect =
                                        PathEffect.dashPathEffect(
                                            floatArrayOf(8.dp.toPx(), 8.dp.toPx()),
                                        ),
                                ),
                        )
                    },
        )
        Box(
            modifier =
                Modifier
                    .offset(x = 44.dp, y = 74.dp)
                    .size(60.dp)
                    .background(Surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.SentimentSatisfiedAlt,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = Ink,
            )
        }
        // garis putus biru: wajah — badge — ponsel
        Canvas(
            modifier =
                Modifier
                    .offset(x = 129.dp, y = 102.dp)
                    .size(width = 106.dp, height = 4.dp),
        ) {
            drawLine(
                color = Primary,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect =
                    PathEffect.dashPathEffect(
                        floatArrayOf(2.14.dp.toPx(), 9.64.dp.toPx()),
                    ),
            )
        }
        Box(
            modifier =
                Modifier
                    .offset(x = 150.dp, y = 89.dp)
                    .size(width = 61.dp, height = 26.dp)
                    .background(Surface, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = distText,
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
            )
        }
        Box(
            modifier =
                Modifier
                    .offset(x = 248.dp, y = 76.dp)
                    .size(56.dp)
                    .background(Surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Smartphone,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Ink,
            )
        }
        KikoImage(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-11).dp)
                    .size(width = 88.5.dp, height = 94.25.dp),
            expression = kikoExpr,
        )
        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 317.5.dp, y = 29.5.dp)
                    .size(15.dp),
            color = Color.White,
        )
    }
}

/** Kartu gauge 03 (353×262): busur 3 zona + nilai jarak + pill + baris skala. */
@Composable
private fun GaugeCard(
    distanceCm: Float?,
    riskLevel: RiskLevel,
) {
    val distText =
        distanceCm?.let { String.format(Locale.US, "%.0f cm", it) }
            ?: "-- cm"
    val (statusText, statusBg, statusIcon) =
        when {
            distanceCm == null ->
                Triple("Mencari wajah...", SurfaceAlt, Icons.Outlined.Face)
            riskLevel == RiskLevel.OK ->
                Triple(stringResource(R.string.s03_status_pill), Mint, Icons.Outlined.Check)
            riskLevel == RiskLevel.WARNING ->
                Triple("Mulai terlalu dekat! ⚠️", Sunny, Icons.Outlined.WarningAmber)
            else ->
                Triple("Terlalu dekat! Mundur ya ✋", Coral, Icons.Outlined.Close)
        }
    val targetAngleDeg =
        when {
            distanceCm == null -> 314.0
            distanceCm < 30f -> 180.0 + ((distanceCm.coerceIn(10f, 30f) - 10.0) / 20.0) * 60.0
            distanceCm < 45f -> 253.0 + ((distanceCm - 30.0) / 15.0) * 34.0
            else -> 300.0 + ((distanceCm.coerceAtMost(75f) - 45.0) / 30.0) * 60.0
        }
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngleDeg.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "gaugeKnobAngle",
    )

    JagaCard(
        contentPadding = PaddingValues(16.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(132.dp),
        ) {
            Canvas(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 11.dp)
                        .size(width = 218.dp, height = 118.dp),
            ) {
                val cx = 109.dp.toPx()
                val cy = 109.dp.toPx()
                val r = 100.dp.toPx()
                val stroke = 18.dp.toPx()
                val arcSize = Size(r * 2, r * 2)
                val arcTopLeft = Offset(cx - r, cy - r)
                drawArc(
                    color = Coral,
                    startAngle = 180f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = Sunny,
                    startAngle = 253f,
                    sweepAngle = 34f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = Mint,
                    startAngle = 300f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                // penanda knob halus di posisi dinamis
                val angle = Math.toRadians(animatedAngle.toDouble())
                val knobX = cx + (r * cos(angle)).toFloat()
                val knobY = cy + (r * sin(angle)).toFloat()
                val knobCenter = Offset(knobX, knobY)
                drawCircle(Color.White, radius = 12.dp.toPx(), center = knobCenter)
                drawCircle(
                    Ink,
                    radius = 14.dp.toPx(),
                    center = knobCenter,
                    style = Stroke(4.dp.toPx()),
                )
            }
            Column(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 66.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = distText,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.s03_distance_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.72f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            StatusPillChecked(
                text = statusText,
                background = statusBg,
                icon = statusIcon,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ScaleRow()
    }
}

@Composable
private fun StatusPillChecked(
    text: String = stringResource(R.string.s03_status_pill),
    background: Color = Mint,
    icon: ImageVector = Icons.Outlined.Check,
) {
    Row(
        modifier =
            Modifier
                .background(background, RoundedCornerShape(17.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Ink,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Ink,
        )
    }
}

/** Baris skala: wajah · garis · penggaris · garis · ponsel. */
@Composable
private fun ScaleRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScaleCircle(
            size = 40.dp,
            background = SurfaceAlt,
            icon = Icons.Outlined.SentimentSatisfiedAlt,
            iconSize = 22.dp,
        )
        DottedConnector(modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
        ScaleCircle(
            size = 36.dp,
            background = Sunny,
            icon = Icons.Outlined.Straighten,
            iconSize = 18.dp,
        )
        DottedConnector(modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
        ScaleCircle(
            size = 40.dp,
            background = SurfaceAlt,
            icon = Icons.Outlined.Smartphone,
            iconSize = 22.dp,
        )
    }
}

@Composable
private fun ScaleCircle(
    size: Dp,
    background: Color,
    icon: ImageVector,
    iconSize: Dp,
) {
    Box(
        modifier =
            Modifier
                .size(size)
                .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = Ink,
        )
    }
}

@Composable
private fun DottedConnector(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.height(4.dp),
    ) {
        drawLine(
            color = Primary,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect =
                PathEffect.dashPathEffect(
                    floatArrayOf(2.5.dp.toPx(), 8.dp.toPx()),
                ),
        )
    }
}

/** Kartu waktu bermain 03 (353×82) + tombol jeda. */
@Composable
private fun TimeCard(
    isRunning: Boolean,
    elapsedSeconds: Long = 0L,
    onPauseToggle: () -> Unit,
) {
    val icon = if (isRunning) Icons.Outlined.Pause else Icons.Filled.PlayArrow
    val subtitle = if (isRunning) stringResource(R.string.s03_time_label) else "Status pemantauan"
    val mainText =
        if (isRunning) {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        } else {
            "Dijeda"
        }

    JagaCard(
        modifier = Modifier.fillMaxWidth().height(82.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(SurfaceAlt, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Ink,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.72f),
                )
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.displayMedium,
                    color = Ink,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .background(Ink, CircleShape)
                        .clickable(onClick = onPauseToggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Surface,
                )
            }
        }
    }
}

/** Kartu kondisi cahaya ruangan (Lux Sensor). */
@Composable
private fun LightConditionCard(lux: Float?) {
    val (statusLabel, statusDesc, statusColor, icon) =
        when {
            lux == null ->
                LightStatus(
                    "Mendeteksi Cahaya...",
                    "Menyesuaikan sensor lux ruangan",
                    SurfaceAlt,
                    Icons.Outlined.LightMode,
                )
            lux < 150f ->
                LightStatus(
                    "Ruangan Terlalu Gelap ⚠️",
                    "Cahaya < 150 lux berisiko bagi mata anak. Nyalakan lampu ya!",
                    Coral,
                    Icons.Outlined.DarkMode,
                )
            lux < 300f ->
                LightStatus(
                    "Cahaya Cukup 💡",
                    "Penerangan standar (150–300 lux). Disarankan lampu meja belajar.",
                    Sunny,
                    Icons.Outlined.LightMode,
                )
            lux <= 600f ->
                LightStatus(
                    "Cahaya Ideal & Nyaman ✨",
                    "Pencahayaan sangat baik dan sehat untuk membaca (300–500 lux).",
                    Mint,
                    Icons.Outlined.Check,
                )
            else ->
                LightStatus(
                    "Ruangan Sangat Terang ☀️",
                    "Kondisi pencahayaan terang dan aman untuk mata.",
                    Mint,
                    Icons.Outlined.LightMode,
                )
        }

    val luxValueText = lux?.let { String.format(Locale.US, "%.0f lux", it) } ?: "-- lux"

    JagaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(statusColor.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Ink,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Cahaya Sekitar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.72f),
                    )
                    Text(
                        text = luxValueText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
                Text(
                    text = statusDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.72f),
                )
            }
        }
    }
}

private data class LightStatus(
    val label: String,
    val desc: String,
    val color: Color,
    val icon: ImageVector,
)
