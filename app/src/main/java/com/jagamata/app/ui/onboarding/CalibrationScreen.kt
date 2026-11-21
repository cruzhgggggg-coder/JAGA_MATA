package com.jagamata.app.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.parent.HeroSparkle
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.InkSoft
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Kalibrasi jarak (PRD §2.8, US-02): pegang HP 30 cm dari wajah dengan
 * penggaris, tahan stabil >= 1,5 detik -> f_px = h_px x 30 / 15.
 * Visual mengikuti desain 3323 Kalibrasi Kamera.
 */
@Composable
fun CalibrationScreen(
    faceHeightPx: Float?,
    progress: Float,
    isCalibrated: Boolean,
    onStart: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    distanceCm: Float? = null,
    onBack: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
    ) {
        TitleRow(onBack = onBack)
        Spacer(modifier = Modifier.height(14.dp))
        CalibrationStageCard(distanceCm = distanceCm)
        Spacer(modifier = Modifier.height(14.dp))
        GaugeCard(distanceCm = distanceCm, isCalibrated = isCalibrated)
        Spacer(modifier = Modifier.height(18.dp))
        ProgressCard(
            faceHeightPx = faceHeightPx,
            distanceCm = distanceCm,
            progress = progress,
            isCalibrated = isCalibrated,
            onStart = onStart,
            onRetry = onRetry,
            onNext = onNext,
        )
        Spacer(modifier = Modifier.height(60.dp))
    }
}

/** Judul H1 + kilau kuning + pill instruksi yang menempel tepi kanan. */
@Composable
private fun TitleRow(onBack: () -> Unit = {}) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        com.jagamata.app.ui.parent.BackCircle(onBack = onBack)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.s12_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        HeroSparkle(
            color = Sunny,
            side = 13.dp,
            modifier = Modifier.padding(start = 6.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        InstructionPill()
    }
}

/** Pill mint "Pegang HP 30 cm dari wajah" — menempel tepi kanan layar. */
@Composable
private fun InstructionPill() {
    Row(
        modifier =
            Modifier
                .background(Mint, RoundedCornerShape(13.dp))
                .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
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
            text = stringResource(R.string.s12_pill),
            style = MaterialTheme.typography.labelSmall,
            color = Ink,
        )
    }
}

/** Kartu panggung biru #B7D2FF: penggaris, wajah–52 cm–HP, maskot. */
@Composable
private fun CalibrationStageCard(distanceCm: Float? = null) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BlueTintDeep),
    ) {
        RulerHintPill()
        HeroSparkle(
            color = Surface,
            side = 15.dp,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 317.dp, top = 28.dp),
        )
        MeasurementDial(distanceCm = distanceCm)
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp)
                    .size(width = 75.dp, height = 92.dp),
        ) {
            KikoImage(
                expression = KikoExpression.FOKUS,
                modifier = Modifier.fillMaxSize(),
            )
            // Penggaris kuning di perut Kiko (properti desain 3323).
            Canvas(modifier = Modifier.fillMaxSize()) {
                rotate(6f, pivot = Offset(19.dp.toPx(), 62.dp.toPx())) {
                    drawRoundRect(
                        color = Sunny,
                        topLeft = Offset(19.dp.toPx(), 62.dp.toPx()),
                        size = Size(49.dp.toPx(), 9.dp.toPx()),
                        cornerRadius = CornerRadius(4.5.dp.toPx()),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.RulerHintPill() {
    Row(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp)
                .background(Surface, RoundedCornerShape(14.dp))
                .padding(start = 12.dp, end = 14.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = null,
            tint = Ink,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.s12_ruler_hint),
            style = MaterialTheme.typography.labelSmall,
            color = Ink,
        )
    }
}

/** Kotak putus-putus + wajah + garis titik-titik "52 cm" + ikon HP. */
@Composable
private fun BoxScope.MeasurementDial(distanceCm: Float? = null) {
    Canvas(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 26.dp, y = 56.dp)
                .size(96.dp),
    ) {
        drawRoundRect(
            color = Surface,
            style =
                Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)),
                ),
            cornerRadius = CornerRadius(20.dp.toPx()),
        )
    }
    Box(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 44.dp, y = 74.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.SentimentSatisfiedAlt,
            contentDescription = null,
            tint = Ink,
            modifier = Modifier.size(36.dp),
        )
    }
    Canvas(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 129.dp, y = 102.dp)
                .size(width = 106.dp, height = 4.dp),
    ) {
        val path =
            Path().apply {
                moveTo(0f, size.height / 2f)
                lineTo(size.width, size.height / 2f)
            }
        drawPath(
            path = path,
            color = Primary,
            style =
                Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.3f, 10.4f), 1.2f),
                ),
        )
    }
    Box(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 150.dp, y = 89.dp)
                .size(width = 61.dp, height = 26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Surface),
        contentAlignment = Alignment.Center,
    ) {
        val distText =
            distanceCm?.let { String.format(Locale.US, "%.0f cm", it) }
                ?: stringResource(R.string.s12_distance)
        Text(
            text = distText,
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
        )
    }
    Box(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 248.dp, y = 76.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Smartphone,
            contentDescription = null,
            tint = Ink,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** Kartu gauge: busur 3 segmen + "Target 30 cm" + banner stabil + langkah. */
@Composable
private fun GaugeCard(
    distanceCm: Float? = null,
    isCalibrated: Boolean = false,
) {
    val displayDist =
        distanceCm?.let { String.format(Locale.US, "%.0f cm", it) }
            ?: stringResource(R.string.s12_target)
    val displaySub =
        if (distanceCm != null) {
            "target kalibrasi: 30 cm"
        } else {
            stringResource(R.string.s12_target_sub)
        }

    JagaCard(contentPadding = PaddingValues(0.dp)) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(146.dp),
        ) {
            GaugeCanvas(distanceCm = distanceCm)
            Text(
                text = displayDist,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 86.dp),
            )
            Text(
                text = displaySub,
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 128.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        StableBanner(isCalibrated = isCalibrated)
        Spacer(modifier = Modifier.height(10.dp))
        StepRow()
        Spacer(modifier = Modifier.height(12.dp))
    }
}

/** Dummy jarak pada pill hero ("52 cm") — sumber posisi knob busur. */
private const val DUMMY_DISTANCE_CM = 52f

/** Rentang skala busur jarak (cm) yang dipetakan ke 180° (kiri) .. 0° (kanan). */
private const val GAUGE_MIN_CM = 5f
private const val GAUGE_MAX_CM = 65f

/**
 * Busur gauge desain 3323: satu pita setengah lingkaran (r=100dp, tebal 18dp)
 * dengan tiga segmen pada lingkaran yang sama — koral 180°–240°, kuning
 * 253°–287°, mint 300°–360° (sudut Android, celah 13° antar segmen).
 */
@Composable
private fun GaugeCanvas(distanceCm: Float? = null) {
    val animatedDist by animateFloatAsState(
        targetValue = distanceCm ?: 30f,
        label = "calibKnob",
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
        val diameter = 200.dp.toPx()
        val center = Offset(size.width / 2f, 136.dp.toPx())
        val topLeft = Offset(center.x - diameter / 2f, center.y - diameter / 2f)
        val arcSize = Size(diameter, diameter)
        drawArc(
            color = Coral,
            startAngle = 180f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
        drawArc(
            color = Sunny,
            startAngle = 253f,
            sweepAngle = 34f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
        drawArc(
            color = Mint,
            startAngle = 300f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
        val knobCenter =
            gaugeKnobPosition(
                distanceCm = animatedDist,
                center = center,
                radiusPx = 100.dp.toPx(),
            )
        drawCircle(color = Surface, radius = 12.dp.toPx(), center = knobCenter)
        drawCircle(
            color = Ink,
            radius = 14.dp.toPx(),
            center = knobCenter,
            style = Stroke(width = 4.dp.toPx()),
        )
    }
}

@Composable
private fun StableBanner(isCalibrated: Boolean = false) {
    val (text, bg) =
        if (isCalibrated) {
            stringResource(R.string.s12_stable) to Mint
        } else {
            "Pegang stabil 30 cm selama 1,5 detik" to SurfaceAlt
        }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier =
                Modifier
                    .background(bg, RoundedCornerShape(17.dp))
                    .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
            )
        }
    }
}

@Composable
private fun StepRow() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepCircle(
            size = 40.dp,
            containerColor = SurfaceAlt,
            icon = Icons.Outlined.SentimentSatisfiedAlt,
            iconSize = 22.dp,
        )
        DottedConnector(modifier = Modifier.weight(1f))
        StepCircle(
            size = 36.dp,
            containerColor = Sunny,
            icon = Icons.Outlined.Straighten,
            iconSize = 18.dp,
        )
        DottedConnector(modifier = Modifier.weight(1f))
        StepCircle(
            size = 40.dp,
            containerColor = SurfaceAlt,
            icon = Icons.Outlined.Smartphone,
            iconSize = 20.dp,
        )
    }
}

/** Kartu progres: ikon jam, persentase, tombol aksi bulat 52 (play/pause/check). */
@Composable
private fun ProgressCard(
    faceHeightPx: Float?,
    progress: Float,
    isCalibrated: Boolean,
    onStart: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    distanceCm: Float? = null,
) {
    val percent = (progress.coerceIn(0f, 1f) * 100f).toInt()
    JagaCard(
        contentPadding =
            PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceAlt),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.calib_progress),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink,
                    )
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.displayMedium,
                        color = Ink,
                    )
                    if (faceHeightPx != null) {
                        val distInfo = distanceCm?.let { " · Jarak: %.0f cm".format(it) } ?: ""
                        Text(
                            text =
                                stringResource(R.string.calib_face_height) +
                                    ": " +
                                    "%.0f px".format(faceHeightPx) +
                                    distInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft,
                        )
                    } else {
                        Text(
                            text = "Arahkan kamera ke wajah...",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft,
                        )
                    }
                }
            }
            ActionCircle(
                progress = progress,
                isCalibrated = isCalibrated,
                onStart = onStart,
                onRetry = onRetry,
                onNext = onNext,
            )
        }
    }
}

/** Tombol aksi bulat 52: play (mulai), pause (ulangi), check (lanjut). */
@Composable
private fun ActionCircle(
    progress: Float,
    isCalibrated: Boolean,
    onStart: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
) {
    val icon =
        when {
            isCalibrated -> Icons.Filled.Check
            progress > 0f -> Icons.Filled.Pause
            else -> Icons.Filled.PlayArrow
        }
    val label =
        when {
            isCalibrated -> stringResource(R.string.onboarding_next)
            progress > 0f -> stringResource(R.string.calib_retry)
            else -> stringResource(R.string.calib_start)
        }
    val action =
        when {
            isCalibrated -> onNext
            progress > 0f -> onRetry
            else -> onStart
        }
    Box(
        modifier =
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Ink)
                .clickable(onClick = action),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Surface,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun StepCircle(
    size: Dp,
    containerColor: Color,
    icon: ImageVector,
    iconSize: Dp,
) {
    Box(
        modifier =
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Ink,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun DottedConnector(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.height(4.dp)) {
        val path =
            Path().apply {
                moveTo(0f, size.height / 2f)
                lineTo(size.width, size.height / 2f)
            }
        drawPath(
            path = path,
            color = Primary,
            style =
                Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.3f, 10.4f), 1.2f),
                ),
        )
    }
}

/**
 * Posisi knob pada busur: jarak (cm) dipetakan linear ke sudut setengah
 * lingkaran (180° kiri = terlalu dekat, 0° kanan = aman). Dummy 52 cm
 * mendarat di busur mint kanan-atas, konsisten dengan pill "52 cm".
 */
private fun gaugeKnobPosition(
    distanceCm: Float,
    center: Offset,
    radiusPx: Float,
): Offset {
    val t = ((distanceCm - GAUGE_MIN_CM) / (GAUGE_MAX_CM - GAUGE_MIN_CM)).coerceIn(0f, 1f)
    val angleRad = (180.0 * (1.0 - t)) * PI / 180.0
    return Offset(
        x = center.x + radiusPx * cos(angleRad).toFloat(),
        y = center.y - radiusPx * sin(angleRad).toFloat(),
    )
}
