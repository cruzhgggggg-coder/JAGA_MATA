package com.jagamata.app.ui.child

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.BottomCtaBar
import com.jagamata.app.ui.components.BottomNavBar
import com.jagamata.app.ui.components.BottomNavItem
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.components.Sparkle
import com.jagamata.app.ui.components.StatusPill
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Iris
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.PrimaryDark
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt

/**
 * Layar alarm penuh saat BLOCKED (desain 04 · 1860_peringatan).
 *
 * Adegan "Pantau Jarak" dirender di atas dasar Coral dengan grup opacity 0.72
 * (persis komposit SVG desain), lalu bottom-sheet putih r32 berisi Kiko,
 * judul, subjudul, dan CTA "Aku Mundur!". Atas sheet ≈54% tinggi layar
 * (persis desain) sehingga pill status di bawah gauge tertutup penuh.
 *
 * Dipakai sebagai rute PERINGATAN dan overlay layar-penuh; [title]/[subtitle]
 * tetap menjadi teks alarm, haptic dipanggil dari OverlayService.
 */
@Composable
fun AlarmBlocked(
    title: String = stringResource(R.string.per_sheet_title),
    subtitle: String = stringResource(R.string.per_sheet_subtitle),
    onConfirm: () -> Unit = {},
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Coral),
    ) {
        // Adegan di belakang sheet — desain menggabungkannya pada opacity 0.72.
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(0.72f),
        ) {
            PantauJarakScene()
        }

        // Bottom sheet alarm (opacity penuh) — atas sheet di ~54% tinggi layar.
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.46f)
                    .background(Surface, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(9.dp))
            Box(
                modifier =
                    Modifier
                        .size(52.dp, 5.dp)
                        .background(Divider, RoundedCornerShape(2.5.dp)),
            )
            KikoImage(
                expression = KikoExpression.SENANG,
                modifier =
                    Modifier
                        .padding(top = 8.dp)
                        .size(147.dp, 154.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 9.dp),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Ink.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            Spacer(Modifier.weight(1f))
            BottomCtaBar(
                text = stringResource(R.string.per_cta),
                onClick = onConfirm,
            )
        }
    }
}

/** Adegan layar "Pantau Jarak" (bagian yang tampak di atas sheet, desain 04). */
@Composable
private fun PantauJarakScene() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        // Header
        Text(
            text = stringResource(R.string.per_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            modifier = Modifier.offset(20.dp, 58.dp),
        )
        Sparkle(modifier = Modifier.offset(190.dp, 62.dp).size(14.dp), color = Sunny)

        // Chip status "Terlalu dekat" (267,61) 106x26 r13
        Row(
            modifier =
                Modifier
                    .offset(267.dp, 61.dp)
                    .height(26.dp)
                    .background(Coral, RoundedCornerShape(13.dp))
                    .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(7.dp)
                        .background(Iris, RoundedCornerShape(3.5.dp)),
            )
            Text(
                text = stringResource(R.string.per_status_badge),
                style = MaterialTheme.typography.labelSmall,
                color = Ink,
            )
        }

        // Kartu kamera (20,104) 353x240 r24
        Box(
            modifier =
                Modifier
                    .offset(20.dp, 104.dp)
                    .size(353.dp, 240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BlueTintDeep),
        ) {
            Sparkle(modifier = Modifier.offset(315.dp, 32.dp).size(14.dp), color = Color.White)

            // Chip "Kamera depan" (32,116) 121x28 r14
            Row(
                modifier =
                    Modifier
                        .offset(12.dp, 12.dp)
                        .height(28.dp)
                        .background(Surface, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    null,
                    tint = Ink,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(R.string.per_camera_chip),
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink,
                )
            }

            // Bingkai deteksi wajah putus-putus (46,160) 96x96 r16 + lingkaran wajah (64,178) 60x60
            Box(
                modifier =
                    Modifier
                        .offset(26.dp, 56.dp)
                        .size(96.dp),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val inset = 1.5.dp.toPx()
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - 2 * inset, size.height - 2 * inset),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style =
                            Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect =
                                    PathEffect.dashPathEffect(
                                        floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
                                    ),
                            ),
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .offset(18.dp, 18.dp)
                            .size(60.dp),
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val r = 30.dp.toPx()
                        drawCircle(Color.White, radius = r)
                        drawCircle(
                            Iris.copy(alpha = 0.55f),
                            radius = r - 2.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx()),
                        )
                        // wajah smiley
                        drawCircle(
                            Iris.copy(alpha = 0.7f),
                            radius = 3.dp.toPx(),
                            center = Offset(size.width / 2 - 9.dp.toPx(), size.height / 2 - 6.dp.toPx()),
                        )
                        drawCircle(
                            Iris.copy(alpha = 0.7f),
                            radius = 3.dp.toPx(),
                            center = Offset(size.width / 2 + 9.dp.toPx(), size.height / 2 - 6.dp.toPx()),
                        )
                        val smile =
                            Path().apply {
                                moveTo(size.width / 2 - 11.dp.toPx(), size.height / 2 + 8.dp.toPx())
                                cubicTo(
                                    size.width / 2 - 5.dp.toPx(),
                                    size.height / 2 + 14.dp.toPx(),
                                    size.width / 2 + 5.dp.toPx(),
                                    size.height / 2 + 14.dp.toPx(),
                                    size.width / 2 + 11.dp.toPx(),
                                    size.height / 2 + 8.dp.toPx(),
                                )
                            }
                        drawPath(smile, Iris.copy(alpha = 0.7f), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }

            // Garis putus-putus penghubung + pill "24 cm"
            Canvas(
                modifier =
                    Modifier
                        .offset(120.dp, 102.dp)
                        .size(130.dp, 4.dp),
            ) {
                drawLine(
                    Iris.copy(alpha = 0.45f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
                )
            }
            Box(
                modifier =
                    Modifier
                        .offset(150.dp, 89.dp)
                        .size(61.dp, 26.dp)
                        .background(Surface, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.per_distance),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                )
            }

            // Ikon perangkat/HP (268,180) 56x56 — glyph smartphone (bukan gagang telepon)
            Box(
                modifier =
                    Modifier
                        .offset(248.dp, 76.dp)
                        .size(56.dp)
                        .background(Surface, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Smartphone,
                    null,
                    tint = Ink,
                    modifier = Modifier.size(26.dp),
                )
            }

            // Kiko kecil dengan penggaris (aset Kiko + overlay penggaris, desain 04)
            Box(
                modifier =
                    Modifier
                        .offset(260.dp, 145.dp)
                        .size(87.dp, 95.dp),
            ) {
                KikoImage(
                    expression = KikoExpression.SENANG,
                    modifier = Modifier.fillMaxSize(),
                )
                Canvas(Modifier.fillMaxSize()) {
                    val ox = 15.3.dp.toPx()
                    val oy = 61.8.dp.toPx()
                    rotate(6f, pivot = Offset(ox, oy)) {
                        drawRoundRect(
                            Sunny,
                            topLeft = Offset(ox, oy),
                            size = Size(58.8.dp.toPx(), 10.08.dp.toPx()),
                            cornerRadius = CornerRadius(5.dp.toPx()),
                        )
                        drawRoundRect(
                            Ink,
                            topLeft = Offset(ox, oy),
                            size = Size(58.8.dp.toPx(), 10.08.dp.toPx()),
                            cornerRadius = CornerRadius(5.dp.toPx()),
                            style = Stroke(width = 0.84.dp.toPx()),
                        )
                        for (i in 0..7) {
                            val x = ox + (8.266f + i * 5.88f).dp.toPx()
                            val y0 = oy + 1.714.dp.toPx()
                            val len = if (i % 2 == 0) 4.2 else 2.52
                            drawLine(
                                Ink,
                                start = Offset(x, y0),
                                end = Offset(x, y0 + len.dp.toPx()),
                                strokeWidth = 0.84.dp.toPx(),
                            )
                        }
                    }
                }
            }
        }

        // Kartu gauge (20,358) 353x262 r24 — busur setengah lingkaran di tengah
        Box(
            modifier =
                Modifier
                    .offset(20.dp, 358.dp)
                    .size(353.dp, 262.dp)
                    .background(Surface, RoundedCornerShape(24.dp)),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                val arcSize = Size(260.dp.toPx(), 260.dp.toPx())
                val arcTopLeft = Offset(46.5.dp.toPx(), 10.dp.toPx())
                // busur kiri (bahaya)
                drawArc(
                    Coral,
                    startAngle = 180f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = stroke,
                )
                // busur atas (waspada)
                drawArc(
                    Sunny,
                    startAngle = 253f,
                    sweepAngle = 34f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = stroke,
                )
                // busur kanan (aman)
                drawArc(
                    Mint,
                    startAngle = 300f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = stroke,
                )
                // penanda gelap di segmen merah (θ=210° pada busur)
                val knob = Offset(63.9.dp.toPx(), 75.dp.toPx())
                drawCircle(Surface, radius = 13.dp.toPx(), center = knob)
                drawCircle(
                    Ink,
                    radius = 12.dp.toPx(),
                    center = knob,
                    style = Stroke(width = 4.dp.toPx()),
                )
            }

            // Pill status di bawah gauge (69,520) 255x34 r17
            StatusPill(
                text = stringResource(R.string.per_status_pill),
                containerColor = Coral,
                modifier = Modifier.offset(49.dp, 162.dp),
            )

            // Kartu waktu bermain (20,634) 353x82 r20
            Row(
                modifier =
                    Modifier
                        .offset(0.dp, 276.dp)
                        .fillMaxWidth()
                        .height(82.dp)
                        .background(Surface, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .background(SurfaceAlt, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Home,
                        null,
                        tint = PrimaryDark,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.per_play_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.72f),
                    )
                    Text(
                        text = stringResource(R.string.per_play_value),
                        style = MaterialTheme.typography.displayMedium,
                        color = Ink,
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier =
                        Modifier
                            .size(52.dp)
                            .background(Ink, RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        null,
                        tint = Surface,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }

        // Bottom nav (0,752) h100 — tertutup sheet, dipertahankan agar sesuai desain.
        BottomNavBar(
            items =
                listOf(
                    BottomNavItem(stringResource(R.string.nav_beranda), Icons.Filled.Home),
                    BottomNavItem(stringResource(R.string.nav_pantau), Icons.Filled.Visibility),
                    BottomNavItem(stringResource(R.string.nav_hadiah), Icons.Filled.Star),
                    BottomNavItem(stringResource(R.string.nav_ortu), Icons.Filled.Person),
                ),
            selectedIndex = 1,
            onSelect = {},
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Maskot Kiko (burung hantu) — wrapper desain; visual Kiko penuh dari aset [KikoImage].
 */
@Composable
fun MascotKiko(modifier: Modifier = Modifier) {
    KikoImage(modifier = modifier, expression = KikoExpression.SENANG)
}
