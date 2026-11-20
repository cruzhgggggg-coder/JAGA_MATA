package com.jagamata.app.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.BottomCtaBar
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.components.Sparkle
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTint
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.InkSoft
import com.jagamata.app.ui.theme.Lavender
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.PrimaryDark
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface

/*
 * Onboarding 3 langkah (desain 01a/01b/01c) — 393x852, 1px = 1dp.
 * Hero card (20, 158.5) 353x360 r24 gradient BlueTint -> BlueTintDeep,
 * CTA y=756 h56 r28 Ink via [BottomCtaBar], dots y=728.
 * Kiko dirender dari aset PNG desain ([KikoImage]); hero card di-clip
 * agar maskot tidak meluber dari kartu.
 */

// ------------------------------------------------------------ Dekorasi

@Composable
internal fun Cloud(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val p = Path().apply {
            moveTo(16.71f, 38.222f)
            cubicTo(7.161f, 38.222f, 0f, 32.25f, 0f, 23.889f)
            cubicTo(0f, 16.125f, 5.968f, 10.75f, 13.129f, 10.75f)
            cubicTo(15.516f, 3.583f, 22.081f, 0f, 29.839f, 0f)
            cubicTo(39.387f, 0f, 46.548f, 6.569f, 47.742f, 14.333f)
            cubicTo(49.532f, 13.139f, 51.919f, 12.542f, 54.306f, 12.542f)
            cubicTo(62.065f, 12.542f, 69.226f, 18.514f, 69.226f, 25.681f)
            cubicTo(69.226f, 32.847f, 63.258f, 38.222f, 54.903f, 38.222f)
            lineTo(16.71f, 38.222f)
            close()
        }
        scale(size.width / 69.226f, size.height / 38.222f, pivot = Offset.Zero) {
            drawPath(p, Color.White)
        }
    }
}

/** Gelembung ucapan putih r16 + ekor — teks bodySmall 15 tengah. */
@Composable
internal fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(305.dp)) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Surface, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
        Canvas(modifier = Modifier.size(18.dp, 13.dp).align(Alignment.CenterHorizontally)) {
            val p = Path().apply {
                moveTo(0f, 0f)
                lineTo(18f, 0f)
                cubicTo(13f, 3f, 10f, 8f, 8f, 12.5f)
                cubicTo(7.8f, 13f, 7.1f, 13f, 7f, 12.5f)
                cubicTo(6.4f, 8f, 4.5f, 3f, 0f, 0f)
                close()
            }
            drawPath(p, Surface)
        }
    }
}

// ------------------------------------------------------------- Onboarding

@Composable
fun OnboardingFlow(onDone: () -> Unit) {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    when (pageIndex) {
        0 -> OnboardingPage1(onNext = { pageIndex = 1 }, onSkip = onDone)
        1 -> OnboardingPage2(onNext = { pageIndex = 2 }, onSkip = onDone)
        else -> OnboardingPage3(onNext = onDone, onSkip = onDone)
    }
}

@Composable
private fun OnboardingScaffold(
    activeDot: Int,
    title: String,
    desc: String,
    titleTopDp: Float,
    ctaText: String,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    hero: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        Row(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(20.dp, 64.dp)
                    .width(353.dp)
                    .height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Sparkle(modifier = Modifier.size(14.dp), color = Sunny)
            Text(
                text = stringResource(R.string.ob_skip),
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryDark,
                modifier = Modifier.clickable(onClick = onSkip),
            )
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(20.dp, 158.5.dp)
                    .size(353.dp, 360.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(listOf(BlueTint, BlueTintDeep)),
                        shape = RoundedCornerShape(24.dp),
                    ),
            content = hero,
        )

        Column(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(20.dp, titleTopDp.dp)
                    .width(353.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyLarge,
                color = Ink.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(0.dp, 728.dp)
                    .fillMaxWidth()
                    .height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            repeat(3) { i ->
                Box(
                    modifier =
                        Modifier
                            .size(width = if (i == activeDot) 24.dp else 8.dp, height = 8.dp)
                            .background(
                                color = if (i == activeDot) Primary else Divider,
                                shape = RoundedCornerShape(4.dp),
                            ),
                )
            }
        }

        BottomCtaBar(
            text = ctaText,
            onClick = onNext,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ------------------------------------------------------------- Halaman 01a

@Composable
private fun OnboardingPage1(onNext: () -> Unit, onSkip: () -> Unit) {
    OnboardingScaffold(
        activeDot = 0,
        title = stringResource(R.string.ob1_title),
        desc = stringResource(R.string.ob1_desc),
        titleTopDp = 559.5f,
        ctaText = stringResource(R.string.ob_next),
        onNext = onNext,
        onSkip = onSkip,
    ) {
        SpeechBubble(
            text = stringResource(R.string.ob1_bubble),
            modifier = Modifier.offset(24.dp, 24.dp),
        )
        KikoImage(
            expression = KikoExpression.SENANG,
            modifier = Modifier.offset(66.7.dp, 115.3.dp).size(220.9.dp, 231.7.dp),
        )
        Sparkle(modifier = Modifier.offset(35.8.dp, 151.9.dp).size(18.dp), color = Color.White)
        Sparkle(modifier = Modifier.offset(301.3.dp, 141.4.dp).size(13.dp), color = Color.White)
        Sparkle(modifier = Modifier.offset(287.2.dp, 291.2.dp).size(12.dp), color = Color.White)
        Cloud(Modifier.offset(282.4.dp, 313.9.dp).size(69.2.dp, 38.2.dp))
    }
}

// ------------------------------------------------------------- Halaman 01b

@Composable
private fun OnboardingPage2(onNext: () -> Unit, onSkip: () -> Unit) {
    OnboardingScaffold(
        activeDot = 1,
        title = stringResource(R.string.ob2_title),
        desc = stringResource(R.string.ob2_desc),
        titleTopDp = 576f,
        ctaText = stringResource(R.string.ob_next),
        onNext = onNext,
        onSkip = onSkip,
    ) {
        KikoImage(
            expression = KikoExpression.SENANG,
            modifier = Modifier.offset(97.dp, 19.5.dp).size(159.dp, 167.dp),
        )
        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 17.dp, end = 17.dp, bottom = 10.dp)
                    .fillMaxWidth()
                    .height(152.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Filled.Schedule, null, tint = Ink, modifier = Modifier.size(22.dp)) },
                iconColor = Sunny,
                value = stringResource(R.string.ob2_value_1),
                label = stringResource(R.string.ob2_label_1),
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Filled.Visibility, null, tint = Ink, modifier = Modifier.size(22.dp)) },
                iconColor = Mint,
                value = stringResource(R.string.ob2_value_2),
                label = stringResource(R.string.ob2_label_2),
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Filled.Pause, null, tint = Ink, modifier = Modifier.size(22.dp)) },
                iconColor = Lavender,
                value = stringResource(R.string.ob2_value_3),
                label = stringResource(R.string.ob2_label_3),
            )
        }
        Cloud(Modifier.offset(0.dp, 366.dp).size(69.2.dp, 38.2.dp))
        Sparkle(modifier = Modifier.offset(320.dp, 380.dp).size(14.dp), color = Sunny)
    }
}

@Composable
private fun StatCard(
    iconColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .height(152.dp)
                .background(Surface, RoundedCornerShape(16.dp))
                .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .background(iconColor, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
            content = { icon() },
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft,
            textAlign = TextAlign.Center,
        )
    }
}

// ------------------------------------------------------------- Halaman 01c

@Composable
private fun OnboardingPage3(onNext: () -> Unit, onSkip: () -> Unit) {
    OnboardingScaffold(
        activeDot = 2,
        title = stringResource(R.string.ob3_title),
        desc = stringResource(R.string.ob3_desc),
        titleTopDp = 553f,
        ctaText = stringResource(R.string.ob_start),
        onNext = onNext,
        onSkip = onSkip,
    ) {
        KikoImage(
            expression = KikoExpression.SENANG_KACAMATA,
            modifier = Modifier.offset(77.dp, 26.dp).size(199.dp, 212.dp),
        )
        Row(
            modifier =
                Modifier
                    .offset(57.dp, 255.5.dp)
                    .size(240.dp, 56.dp)
                    .background(Surface, RoundedCornerShape(28.dp))
                    .padding(start = 16.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Mint, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Straighten,
                    null,
                    tint = Ink,
                    modifier = Modifier.size(22.dp).rotate(-45f),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.ob3_badge_value),
                style = MaterialTheme.typography.displayMedium,
                color = Ink,
                maxLines = 1,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.ob3_badge_label),
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                maxLines = 1,
            )
        }
        Cloud(Modifier.offset(300.dp, 345.dp).size(69.2.dp, 38.2.dp))
        Sparkle(modifier = Modifier.offset(30.dp, 372.dp).size(14.dp), color = Sunny)
    }
}
