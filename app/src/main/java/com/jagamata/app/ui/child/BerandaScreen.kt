package com.jagamata.app.ui.child

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.BottomNavItem
import com.jagamata.app.ui.components.BottomNavBar
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import java.util.Calendar
import kotlinx.coroutines.launch
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTint
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Blush
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Cream
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.InkSoft
import com.jagamata.app.ui.theme.Iris
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.PrimaryDark
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt
import com.jagamata.app.ui.theme.ZoneSafe

/**
 * Item tab bottom nav desain (Beranda · Pantau · Hadiah · Orang Tua).
 * Dipakai bersama oleh layar batch B (02, 03, 06).
 */
@Composable
fun jagaBottomNavItems(): List<BottomNavItem> =
    listOf(
        BottomNavItem(stringResource(R.string.bnav_beranda), Icons.Outlined.Home),
        BottomNavItem(stringResource(R.string.bnav_pantau), Icons.Outlined.Visibility),
        BottomNavItem(stringResource(R.string.bnav_hadiah), Icons.Outlined.CardGiftcard),
        BottomNavItem(stringResource(R.string.bnav_orang_tua), Icons.Outlined.People),
    )

/** Kilau bintang 4 sudut desain (path sparkle 11.667). */
@Composable
fun DesignSparkle(
    modifier: Modifier = Modifier,
    color: Color = Sunny,
) {
    Canvas(modifier = modifier) {
        val k = size.minDimension / 11.667f
        val path =
            Path().apply {
                moveTo(5.833f * k, 0f)
                cubicTo(6.3f * k, 3.208f * k, 8.458f * k, 5.367f * k, 11.667f * k, 5.833f * k)
                cubicTo(8.458f * k, 6.3f * k, 6.3f * k, 8.458f * k, 5.833f * k, 11.667f * k)
                cubicTo(5.367f * k, 8.458f * k, 3.208f * k, 6.3f * k, 0f, 5.833f * k)
                cubicTo(3.208f * k, 5.367f * k, 5.367f * k, 3.208f * k, 5.833f * k, 0f)
                close()
            }
        drawPath(path, color)
    }
}

/** Awan dekorasi desain (path 54×32). */
@Composable
fun DesignCloud(
    modifier: Modifier = Modifier,
    color: Color = Surface,
) {
    Canvas(modifier = modifier) {
        val k = size.width / 54f
        val path =
            Path().apply {
                moveTo(14f * k, 32f * k)
                cubicTo(6f * k, 32f * k, 0f, 27f * k, 0f, 20f * k)
                cubicTo(0f, 13.5f * k, 5f * k, 9f * k, 11f * k, 9f * k)
                cubicTo(13f * k, 3f * k, 18.5f * k, 0f, 25f * k, 0f)
                cubicTo(33f * k, 0f, 39f * k, 5.5f * k, 40f * k, 12f * k)
                cubicTo(41.5f * k, 11f * k, 43.5f * k, 10.5f * k, 45.5f * k, 10.5f * k)
                cubicTo(50.194f * k, 10.5f * k, 54f * k, 14.306f * k, 54f * k, 19f * k)
                cubicTo(54f * k, 25f * k, 49f * k, 32f * k, 42f * k, 32f * k)
                lineTo(14f * k, 32f * k)
                close()
            }
        drawPath(path, color)
    }
}

/**
 * Maskot Kiko — ilustrasi vektor mengikuti path SVG layar desain
 * (bbox desain 108×134). `sleepy` = mata setengah tertutup + Zzz;
 * `ruler` = pegang penggaris (layar Pantau Live).
 */
@Composable
fun KikoMascot(
    modifier: Modifier = Modifier,
    sleepy: Boolean = false,
    ruler: Boolean = false,
) {
    Canvas(modifier = modifier) {
        val k = size.width / 108f
        drawKikoBase(k)
        if (sleepy) drawKikoSleepy(k)
        if (ruler) drawKikoRuler(k)
    }
}

private fun DrawScope.drawKikoBase(k: Float) {
    // kaki (di belakang badan)
    val footSize = Size(20.4f * k, 10.8f * k)
    drawOval(
        Sunny,
        topLeft = Offset(28.8f * k, 122.25f * k),
        size = footSize,
    )
    drawOval(
        Sunny,
        topLeft = Offset(58.8f * k, 122.25f * k),
        size = footSize,
    )
    // telinga (kiri / kanan cermin)
    drawPath(kikoEarPath(k, mirror = false), Primary)
    drawPath(kikoEarPath(k, mirror = true), Primary)
    // badan
    drawPath(kikoBodyPath(k), Primary)
    // perut krem
    drawOval(
        Cream,
        topLeft = Offset(19.2f * k, 73.05f * k),
        size = Size(69.6f * k, 50.4f * k),
    )
    // pipi
    drawOval(
        Blush,
        topLeft = Offset(12f * k, 57.45f * k),
        size = Size(15.6f * k, 8.4f * k),
    )
    drawOval(
        Blush,
        topLeft = Offset(80.4f * k, 57.45f * k),
        size = Size(15.6f * k, 8.4f * k),
    )
    // mata + kilau
    drawCircle(Iris, radius = 11.4f * k, center = Offset(35.4f * k, 37.6f * k))
    drawCircle(Iris, radius = 11.4f * k, center = Offset(72.6f * k, 37.6f * k))
    drawCircle(Color.White, radius = 3.9f * k, center = Offset(38.1f * k, 34.3f * k))
    drawCircle(Color.White, radius = 1.8f * k, center = Offset(30f * k, 47.2f * k))
    drawCircle(Color.White, radius = 3.9f * k, center = Offset(75.3f * k, 34.3f * k))
    drawCircle(Color.White, radius = 1.8f * k, center = Offset(67.2f * k, 47.2f * k))
    // paruh
    drawPath(kikoBeakTopPath(k), Sunny)
    drawPath(kikoBeakBottomPath(k), Iris)
    drawOval(
        Coral,
        topLeft = Offset(50.4f * k, 70.65f * k),
        size = Size(7.2f * k, 3f * k),
    )
}

private fun DrawScope.drawKikoSleepy(k: Float) {
    // kelopak biru menyatu dengan kepala (setengah tertutup)
    drawOval(
        Primary,
        topLeft = Offset(21.6f * k, 20.25f * k),
        size = Size(27.6f * k, 19.2f * k),
    )
    drawOval(
        Primary,
        topLeft = Offset(58.8f * k, 20.25f * k),
        size = Size(27.6f * k, 19.2f * k),
    )
    val brow = Stroke(width = 3.6f * k, cap = StrokeCap.Round)
    val browLeft =
        Path().apply {
            moveTo(25.2f * k, 22.65f * k)
            cubicTo(29.2f * k, 23.05f * k, 33.6f * k, 22.85f * k, 38.4f * k, 22.05f * k)
        }
    drawPath(browLeft, Iris, style = brow)
    val browRight =
        Path().apply {
            moveTo(69.6f * k, 22.05f * k)
            cubicTo(74.4f * k, 22.85f * k, 78.8f * k, 23.05f * k, 82.8f * k, 22.65f * k)
        }
    drawPath(browRight, Iris, style = brow)
    // "Zzz"
    val zStyle = Stroke(width = 1.5f * k, cap = StrokeCap.Round)
    val z1 =
        Path().apply {
            moveTo(94.8f * k, 1.65f * k)
            lineTo(100.2f * k, 1.65f * k)
            lineTo(94.8f * k, 7.05f * k)
            lineTo(100.2f * k, 7.05f * k)
        }
    drawPath(z1, PrimaryDark, style = zStyle)
    val z2 =
        Path().apply {
            moveTo(103.2f * k, 9.45f * k)
            lineTo(106.8f * k, 9.45f * k)
            lineTo(103.2f * k, 13.05f * k)
            lineTo(106.8f * k, 13.05f * k)
        }
    drawPath(z2, PrimaryDark, style = zStyle)
}

private fun DrawScope.drawKikoRuler(k: Float) {
    // alis tegak
    val brow = Stroke(width = 3.6f * k, cap = StrokeCap.Round)
    val browLeft =
        Path().apply {
            moveTo(25.2f * k, 17.84f * k)
            cubicTo(30f * k, 18.64f * k, 34.8f * k, 20.44f * k, 39.6f * k, 23.24f * k)
        }
    drawPath(browLeft, Iris, style = brow)
    val browRight =
        Path().apply {
            moveTo(68.4f * k, 23.24f * k)
            cubicTo(73.2f * k, 20.44f * k, 78f * k, 18.64f * k, 82.8f * k, 17.84f * k)
        }
    drawPath(browRight, Iris, style = brow)
    // penggaris kuning miring ±6° + garis ukur
    withTransform({
        translate(54f * k, 97f * k)
        rotate(6f)
    }) {
        drawRoundRect(
            color = Sunny,
            topLeft = Offset(-42f * k, -7.2f * k),
            size = Size(84f * k, 14.4f * k),
            cornerRadius = CornerRadius(7.2f * k, 7.2f * k),
        )
        for (i in 0 until 8) {
            val tx = (-29.99f + 8.4f * i) * k
            val len = (if (i % 2 == 0) 6f else 3.6f) * k
            drawLine(
                Ink,
                Offset(tx, -5.99f * k),
                Offset(tx, -5.99f * k + len),
                strokeWidth = 1.2f * k,
                cap = StrokeCap.Round,
            )
        }
    }
    // sayap gelap memegang ujung penggaris
    withTransform({
        translate(7f * k, 88.18f * k)
        rotate(16f)
    }) {
        drawOval(
            PrimaryDark,
            topLeft = Offset(-9f * k, -16.8f * k),
            size = Size(18f * k, 33.6f * k),
        )
    }
    withTransform({
        translate(101f * k, 83.21f * k)
        rotate(-16f)
    }) {
        drawOval(
            PrimaryDark,
            topLeft = Offset(-9f * k, -16.8f * k),
            size = Size(18f * k, 33.6f * k),
        )
    }
}

private fun kikoEarPath(
    k: Float,
    mirror: Boolean,
): Path {
    fun x(v: Float): Float = (if (mirror) 108f - v else v) * k
    fun y(v: Float): Float = v * k
    return Path().apply {
        moveTo(x(20.4f), y(23.847f))
        cubicTo(x(13.2f), y(17.847f), x(10.8f), y(8.247f), x(13.2f), y(2.247f))
        cubicTo(x(14.4f), y(-0.153f), x(17.4f), y(-0.753f), x(19.8f), y(1.047f))
        cubicTo(x(25.2f), y(5.247f), x(28.8f), y(13.047f), x(30f), y(20.247f))
        lineTo(x(20.4f), y(23.847f))
        close()
    }
}

private fun kikoBodyPath(k: Float): Path =
    Path().apply {
        moveTo(54f * k, 10.647f * k)
        cubicTo(68.322f * k, 10.647f * k, 82.057f * k, 16.336f * k, 92.184f * k, 26.463f * k)
        cubicTo(102.311f * k, 36.59f * k, 108f * k, 50.325f * k, 108f * k, 64.647f * k)
        lineTo(108f * k, 80.247f * k)
        cubicTo(108f * k, 92.977f * k, 102.943f * k, 105.186f * k, 93.941f * k, 114.188f * k)
        cubicTo(84.939f * k, 123.19f * k, 72.73f * k, 128.247f * k, 60f * k, 128.247f * k)
        lineTo(48f * k, 128.247f * k)
        cubicTo(35.27f * k, 128.247f * k, 23.061f * k, 123.19f * k, 14.059f * k, 114.188f * k)
        cubicTo(5.057f * k, 105.186f * k, 0f, 92.977f * k, 0f, 80.247f * k)
        lineTo(0f, 64.647f * k)
        cubicTo(0f, 50.325f * k, 5.689f * k, 36.59f * k, 15.816f * k, 26.463f * k)
        cubicTo(25.943f * k, 16.336f * k, 39.678f * k, 10.647f * k, 54f * k, 10.647f * k)
        close()
    }

private fun kikoBeakTopPath(k: Float): Path =
    Path().apply {
        moveTo(50.4f * k, 56.25f * k)
        lineTo(57.6f * k, 56.25f * k)
        cubicTo(58.8f * k, 56.25f * k, 59.4f * k, 57.45f * k, 58.8f * k, 58.65f * k)
        lineTo(54.9f * k, 63.45f * k)
        cubicTo(54.3f * k, 64.23f * k, 53.7f * k, 64.23f * k, 53.1f * k, 63.45f * k)
        lineTo(49.2f * k, 58.65f * k)
        cubicTo(48.7f * k, 57.45f * k, 49.13f * k, 56.25f * k, 50.4f * k, 56.25f * k)
        close()
    }

private fun kikoBeakBottomPath(k: Float): Path =
    Path().apply {
        moveTo(46.2f * k, 65.85f * k)
        lineTo(61.8f * k, 65.85f * k)
        cubicTo(61.8f * k, 71.45f * k, 59.2f * k, 74.25f * k, 54f * k, 74.25f * k)
        cubicTo(48.8f * k, 74.25f * k, 46.2f * k, 71.45f * k, 46.2f * k, 65.85f * k)
        close()
    }

/** Chip zona dengan centang (desain: "✓ Aman", tinggi 22, r 11). */
@Composable
fun CheckedChip(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(containerColor, RoundedCornerShape(11.dp))
                .padding(start = 10.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Ink,
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Ink,
        )
    }
}

/** Kartu statistik desain (170×166, r 20): ikon, label, nilai, progres, chip. */
@Composable
fun JagaStatCard(
    label: String,
    value: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    JagaCard(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .background(SurfaceAlt, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Ink,
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
        Spacer(modifier = Modifier.height(10.dp))
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
                        .background(ZoneSafe, RoundedCornerShape(5.dp)),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        CheckedChip(
            text = stringResource(R.string.s02_status_aman),
            containerColor = ZoneSafe,
        )
    }
}

/**
 * 02 Beranda — beranda anak: sapaan, kartu maskot, kartu statistik,
 * streak, CTA "Mulai Pantau", bottom nav desain.
 */
@Composable
fun BerandaScreen(
    onStartPantau: () -> Unit = {},
    onTab: (Int) -> Unit = {},
    initialTab: Int = 0,
    onOpenDailyStats: () -> Unit = {},
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
            BerandaHeader()
            Spacer(modifier = Modifier.height(16.dp))
            HeroCard()
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.clickable(onClick = onOpenDailyStats)) {
                StatCardRow()
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.clickable(onClick = onOpenDailyStats)) {
                StreakCard()
            }
            Spacer(modifier = Modifier.height(16.dp))
            PlayCta(text = stringResource(R.string.s02_cta), onClick = onStartPantau)
            Spacer(modifier = Modifier.weight(1f))
        }
        DesignCloud(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 302.dp, y = (-138).dp)
                    .size(width = 54.dp, height = 32.dp),
        )
        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 45.dp, y = (-143).dp)
                    .size(13.3.dp),
        )
        BottomNavBar(
            items = jagaBottomNavItems(),
            selectedIndex = initialTab,
            onSelect = onTab,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Header 02: sapaan + kilau + lonceng (desain y=58..108). */
@Composable
private fun BerandaHeader() {
    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = stringResource(R.string.s02_greeting),
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.s02_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = Ink.copy(alpha = 0.72f),
            )
        }
        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 177.dp, y = 9.dp)
                    .size(11.7.dp),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 3.dp)
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Ink,
            )
        }
    }
}

/** Kartu hero 02: gradasi biru + Kiko + gelembung ucapan (353�154.4). */
@Composable
private fun HeroCard() {
    val scope = rememberCoroutineScope()

    // Sapaan kontekstual berdasarkan waktu lokal (Blueprint §4.3)
    val defaultGreeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Selamat pagi! ☀️ Kiko siap jaga matamu hari ini!"
            in 12..17 -> "Selamat siang! 🌤️ Jaga jarak mata minimal 30 cm ya!"
            in 18..21 -> "Selamat malam! 🌙 Pastikan lampu ruanganmu menyala terang!"
            else -> "Sudah larut malam! 😴 Jangan lupa istirahatkan matamu ya!"
        }
    }

    val quotes = remember(defaultGreeting) {
        listOf(
            defaultGreeting,
            "Jarak layar aman minimal 30 cm ya! 👀",
            "Ruangan terang bikin mata tidak cepat lelah! 💡",
            "Duduk tegak, jangan terlalu menunduk! 👍",
            "Ingat aturan 20-20-20: istirahat sejenak! ⏳",
            "Mata sehat bikin belajar & main makin asyik! ✨",
        )
    }

    var quoteIndex by remember { mutableStateOf(0) }

    // 1. Animasi halus nafas/mengapung Kiko
    val infiniteTransition = rememberInfiniteTransition(label = "kiko_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float_y",
    )

    // 2. Animasi masuk pop-in saat buka layar
    val scaleAnim = remember { Animatable(0.75f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    // 3. Animasi lompat riang saat Kiko disentuh anak
    val hopAnim = remember { Animatable(0f) }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(154.4.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(BlueTint, BlueTintDeep)),
                    shape = RoundedCornerShape(24.dp),
                ),
    ) {
        // Maskot Kiko dengan animasi sapaan dan interaksi sentuh
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 10.dp, y = 21.5.dp)
                    .size(width = 124.dp, height = 132.dp)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        translationY = floatY + hopAnim.value
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        scope.launch {
                            quoteIndex = (quoteIndex + 1) % quotes.size
                            hopAnim.animateTo(-14f, tween(140, easing = EaseInOutSine))
                            hopAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                        }
                    },
        ) {
            KikoImage(
                expression = if (quoteIndex % 2 == 1) KikoExpression.SENANG_KACAMATA else KikoExpression.SENANG,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Gelembung ucapan adaptif
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 146.dp, y = 18.2.dp)
                    .size(width = 187.dp, height = 94.dp)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                    }
                    .background(Surface, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        scope.launch {
                            quoteIndex = (quoteIndex + 1) % quotes.size
                            hopAnim.animateTo(-10f, tween(120, easing = EaseInOutSine))
                            hopAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                        }
                    },
        ) {
            Text(
                text = quotes[quoteIndex],
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                modifier = Modifier.padding(12.dp),
            )
        }

        // Ekor gelembung menunjuk ke Kiko
        Canvas(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 136.dp, y = 22.dp)
                    .size(width = 16.dp, height = 13.dp),
        ) {
            val path =
                Path().apply {
                    moveTo(14f, 0f)
                    cubicTo(9.4f, 6f, 4.4f, 9f, 0.4f, 10.5f)
                    cubicTo(0f, 11f, 0f, 11.5f, 0.6f, 11.8f)
                    cubicTo(4.4f, 12.5f, 9.4f, 12f, 15.4f, 9f)
                    lineTo(14f, 0f)
                    close()
                }
            drawPath(path, Surface)
        }

        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 19.3.dp, y = 17.3.dp)
                    .size(11.7.dp),
            color = Color.White,
        )
        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 125.dp, y = 31.dp)
                    .size(10.dp),
            color = Color.White,
        )
        DesignSparkle(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 323.dp, y = 15.2.dp)
                    .size(11.7.dp),
            color = Color.White,
        )
    }
}

/** Baris dua kartu statistik (170�170, gap 13). */
@Composable
private fun StatCardRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        JagaStatCard(
            label = stringResource(R.string.s02_stat_distance),
            value = stringResource(R.string.s02_value_distance),
            progress = 0.81f,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(13.dp))
        JagaStatCard(
            label = stringResource(R.string.s02_stat_time),
            value = stringResource(R.string.s02_value_time),
            progress = 0.81f,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Kartu streak 02 (353�90): api 5 hari + panah. */
@Composable
private fun StreakCard() {
    JagaCard(
        modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(Sunny, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Whatshot,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = Ink,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.s02_streak_value),
                    style = MaterialTheme.typography.displayMedium,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.s02_streak_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.72f),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = InkSoft,
            )
        }
    }
}

/** CTA utama dengan ikon play (PrimaryButton + ikon, r 28, h 56). */
@Composable
private fun PlayCta(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Ink,
                contentColor = Surface,
            ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
