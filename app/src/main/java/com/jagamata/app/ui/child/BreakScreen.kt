package com.jagamata.app.ui.child

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.BottomCtaBar
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTint
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Lavender
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Surface

/**
 * Layar 05 — Istirahat 20-20-20.
 * CTA "Selesai Istirahat" menempel di bawah via [BottomCtaBar].
 */
@Composable
fun BreakScreen(
    onDone: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.jagamata.app.service.KikoVoiceHelper.speakBreak202020(context)
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
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 112.dp),
        ) {
            BreakHeader()
            Spacer(modifier = Modifier.height(36.dp))
            BreakHeroCard()
            Spacer(modifier = Modifier.height(28.dp))
            BreakTipCard()
            Spacer(modifier = Modifier.weight(1f))
        }
        BottomCtaBar(
            text = stringResource(R.string.s05_cta),
            onClick = onDone,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Header 05: judul + pill "20-20-20" lavender. */
@Composable
private fun BreakHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.s05_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier =
                Modifier
                    .background(Lavender, RoundedCornerShape(13.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = stringResource(R.string.s05_chip),
                style = MaterialTheme.typography.labelSmall,
                color = Ink,
            )
        }
    }
}

/** Hero card gradasi BlueTint→BlueTintDeep: ring hitung mundur + Kiko + kartu "Segar lagi!". */
@Composable
private fun BreakHeroCard() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(410.dp)
                .shadow(8.dp, RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(listOf(BlueTint, BlueTintDeep)),
                    RoundedCornerShape(32.dp),
                ),
    ) {
        // Dekorasi awan & kilau
        DesignCloud(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-18).dp, y = 4.dp)
                    .size(width = 54.dp, height = 32.dp),
        )
        DesignCloud(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 22.dp, y = (-12).dp)
                    .size(width = 54.dp, height = 32.dp),
        )
        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-20).dp, y = 12.dp)
                    .size(13.3.dp),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 26.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BreakTimerRing()
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 49.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                KikoImage(
                    expression = KikoExpression.NGANTUK,
                    modifier = Modifier.size(width = 124.dp, height = 135.dp),
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(24.dp),
                )
                FreshCard()
            }
        }
    }
}

/** Ring progres putih + busur Mint (14/20 sisa) dengan angka & satuan di tengah. */
@Composable
private fun BreakTimerRing() {
    Box(
        modifier = Modifier.size(216.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val arcSize = Size(radius * 2, radius * 2)
            val topLeft = Offset((size.width - arcSize.width) / 2f, (size.height - arcSize.height) / 2f)
            // Track putih penuh
            drawArc(
                color = Surface,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )
            // Busur hijau 70 % (14 sisa dari 20 detik)
            drawArc(
                color = Mint,
                startAngle = -90f,
                sweepAngle = 252f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.s05_seconds),
                style = MaterialTheme.typography.displayLarge,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.s05_unit),
                style = MaterialTheme.typography.bodySmall,
                color = Ink.copy(alpha = 0.72f),
            )
        }
    }
}

/** Kartu kecil putih: Kiko segar + label "Segar lagi!". */
@Composable
private fun FreshCard() {
    Column(
        modifier =
            Modifier
                .size(width = 87.dp, height = 107.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .background(Surface, RoundedCornerShape(20.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        KikoImage(
            expression = KikoExpression.SENANG,
            modifier = Modifier.size(width = 60.dp, height = 64.dp),
        )
        Text(
            text = stringResource(R.string.s05_fresh),
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
        )
    }
}

/** Kartu instruksi putih: ikon mata hijau + tip 20-20-20. */
@Composable
private fun BreakTipCard() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .background(Surface, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .background(Mint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.s05_tip),
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
        )
    }
}
