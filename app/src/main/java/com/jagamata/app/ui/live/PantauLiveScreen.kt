package com.jagamata.app.ui.live

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
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
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp),
        ) {
            PantauHeader()
            Spacer(modifier = Modifier.height(14.dp))
            PreviewCard()
            Spacer(modifier = Modifier.height(14.dp))
            GaugeCard()
            Spacer(modifier = Modifier.height(14.dp))
            TimeCard(onPauseToggle = onPauseToggle)
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

/** Header 03: judul + kilau + pill "Sedang memantau". */
@Composable
private fun PantauHeader() {
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
        MonitoringPill()
    }
}

@Composable
private fun MonitoringPill() {
    Row(
        modifier =
            Modifier
                .background(Mint, RoundedCornerShape(13.dp))
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
            text = stringResource(R.string.s03_monitoring),
            style = MaterialTheme.typography.labelSmall,
            color = Ink,
        )
    }
}

/** Kartu pratinjau kamera (353×240, biru): pill, wajah, badge jarak, Kiko. */
@Composable
private fun PreviewCard() {
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
                text = stringResource(R.string.s03_distance_badge),
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
            expression = KikoExpression.FOKUS_PENGARIS,
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
private fun GaugeCard() {
    JagaCard(
        contentPadding = PaddingValues(16.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(129.dp),
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
                    startAngle = 249f,
                    sweepAngle = 33f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = Mint,
                    startAngle = 295f,
                    sweepAngle = 65f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                // penanda putus di posisi 52 cm (320°)
                val angle = Math.toRadians(320.0)
                val knobX = cx + (r * cos(angle)).toFloat()
                val knobY = cy + (r * sin(angle)).toFloat()
                val knobCenter = Offset(knobX, knobY)
                drawCircle(Color.White, radius = 13.dp.toPx(), center = knobCenter)
                drawCircle(
                    Ink,
                    radius = 15.dp.toPx(),
                    center = knobCenter,
                    style = Stroke(4.dp.toPx()),
                )
            }
            Column(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 68.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.s03_distance_value),
                    style = MaterialTheme.typography.displayMedium,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.s03_distance_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.72f),
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(17.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            StatusPillChecked()
        }
        Spacer(modifier = Modifier.height(10.dp))
        ScaleRow()
    }
}

@Composable
private fun StatusPillChecked() {
    Row(
        modifier =
            Modifier
                .background(Mint, RoundedCornerShape(17.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Ink,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.s03_status_pill),
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
        )
    }
}

/** Baris skala: wajah · garis · penggaris · garis · ponsel. */
@Composable
private fun ScaleRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScaleCircle(
            size = 40.dp,
            background = SurfaceAlt,
            icon = Icons.Outlined.SentimentSatisfiedAlt,
            iconSize = 20.dp,
        )
        DottedConnector(width = 76.5.dp)
        ScaleCircle(
            size = 36.dp,
            background = Sunny,
            icon = Icons.Outlined.Edit,
            iconSize = 18.dp,
        )
        DottedConnector(width = 76.5.dp)
        ScaleCircle(
            size = 40.dp,
            background = SurfaceAlt,
            icon = Icons.Outlined.Smartphone,
            iconSize = 20.dp,
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
private fun DottedConnector(width: Dp) {
    Canvas(
        modifier = Modifier.size(width = width, height = 4.dp),
    ) {
        drawLine(
            color = Primary,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect =
                PathEffect.dashPathEffect(
                    floatArrayOf(2.3.dp.toPx(), 10.4.dp.toPx()),
                ),
        )
    }
}

/** Kartu waktu bermain 03 (353×82) + tombol jeda. */
@Composable
private fun TimeCard(onPauseToggle: () -> Unit) {
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
                    text = stringResource(R.string.s03_time_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.72f),
                )
                Text(
                    text = stringResource(R.string.s03_time_value),
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
                    imageVector = Icons.Outlined.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Surface,
                )
            }
        }
    }
}
