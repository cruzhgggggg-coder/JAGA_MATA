package com.jagamata.app.ui.parent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jagamata.app.R
import com.jagamata.app.data.AppDatabase
import com.jagamata.app.ui.components.BottomNavBar
import com.jagamata.app.ui.components.BottomNavItem
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.components.Sparkle
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTint
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Cream
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Lavender
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt

data class BadgeInfo(
    val title: String,
    val subtitle: String,
    val description: String,
    val criteria: String,
    val locked: Boolean,
)

/**
 * 07 Hadiah & Streak — kartu streak mingguan, progres Kiko, koleksi lencana.
 * Terkoneksi dinamis ke database Room untuk status pencapaian nyata anak.
 */
@Composable
fun RewardsScreen(onTab: (Int) -> Unit = {}) {
    val context = LocalContext.current
    var totalCycles by remember { mutableStateOf(0) }
    var closeViolations by remember { mutableStateOf(0) }
    var streakDays by remember { mutableStateOf(1) }
    var selectedBadge by remember { mutableStateOf<BadgeInfo?>(null) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context)
        val snapshots = db.snapshotDao().getAll()
        val daily = db.dailyDao().getAll()
        totalCycles = snapshots.size
        closeViolations = snapshots.count { it.violations?.contains("TOO_CLOSE") == true }
        streakDays = (daily.size + if (snapshots.isNotEmpty()) 1 else 0).coerceIn(1, 7)
    }

    val kikoStage =
        if (totalCycles >= 20) {
            3
        } else if (totalCycles >= 10) {
            2
        } else {
            1
        }
    val kikoStageProgress = ((totalCycles % 10) / 10f).coerceIn(0.15f, 1f)
    val nextTargetDays = (10 - (totalCycles % 10)).coerceAtLeast(1)

    // Logika pembukaan lencana gamifikasi berbasis riwayat pemantauan nyata (Blueprint §4.2)
    val badges =
        listOf(
            BadgeInfo(
                title = stringResource(R.string.s07_badge_mata),
                subtitle = if (totalCycles >= 1) "Terbuka 🎉" else stringResource(R.string.s07_badge_mata_desc),
                description = "Lencana penjaga mata setia yang telah mulai membiasakan jarak pandang layar yang sehat.",
                criteria = "Selesaikan minimal 1 sesi pemantauan jarak layar dengan Kiko.",
                locked = totalCycles < 1,
            ),
            BadgeInfo(
                title = stringResource(R.string.s07_badge_istirahat),
                subtitle = if (totalCycles >= 5) "Terbuka 🎉" else stringResource(R.string.s07_badge_istirahat_desc),
                description = "Lencana kepatuhan aturan 20-20-20 untuk memberi jeda istirahat mata dari radiasi layar.",
                criteria = "Pantau kesehatan mata hingga mencapai minimal 5 siklus pemantauan.",
                locked = totalCycles < 5,
            ),
            BadgeInfo(
                title = stringResource(R.string.s07_badge_jarak),
                subtitle = if (totalCycles >= 3 && closeViolations == 0) "Terbuka 🎉" else stringResource(R.string.s07_locked),
                description = "Lencana disiplin menjaga jarak HP selalu aman di atas 30 cm tanpa peringatan bahaya.",
                criteria = "Selesaikan 3 sesi berturut-turut tanpa pelanggaran jarak terlalu dekat.",
                locked = !(totalCycles >= 3 && closeViolations == 0),
            ),
            BadgeInfo(
                title = stringResource(R.string.s07_badge_super),
                subtitle = if (streakDays >= 3) "Terbuka 🎉" else stringResource(R.string.s07_locked),
                description = "Lencana konsistensi tingkat tinggi penjaga mata sejati!",
                criteria = "Pertahankan kebiasaan melihat layar sehat selama 3 hari berturut-turut.",
                locked = streakDays < 3,
            ),
        )

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.s07_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Sparkle(modifier = Modifier.size(16.dp))
                    Sparkle(modifier = Modifier.size(10.dp))
                }
            }

            StreakCard(streakDays = streakDays)

            KikoGrowthCard(stage = kikoStage, progress = kikoStageProgress, nextDays = nextTargetDays)

            Text(
                text = stringResource(R.string.s07_collection),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                modifier = Modifier.padding(top = 4.dp),
            )

            BadgeGrid(badges = badges, onBadgeClick = { selectedBadge = it })
        }

        BottomNavBar(
            items = defaultBottomNavItems(),
            selectedIndex = 2,
            onSelect = onTab,
        )
    }

    if (selectedBadge != null) {
        val b = selectedBadge!!
        Dialog(onDismissRequest = { selectedBadge = null }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Surface,
                tonalElevation = 6.dp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Badge Visual Circle dengan Star/Lock
                    Box(
                        modifier =
                            Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(
                                    if (b.locked) {
                                        Brush.linearGradient(listOf(SurfaceAlt, Divider))
                                    } else {
                                        Brush.radialGradient(listOf(Sunny, Cream))
                                    },
                                )
                                .border(
                                    width = 3.dp,
                                    color = if (b.locked) Divider else Sunny,
                                    shape = CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (b.locked) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Terkunci",
                                tint = Ink.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Terbuka",
                                tint = Ink,
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chip status bergaya FigCraft
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (b.locked) Coral.copy(alpha = 0.15f) else Mint.copy(alpha = 0.2f))
                                .border(
                                    width = 1.dp,
                                    color = if (b.locked) Coral else Mint,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = if (b.locked) "Terkunci 🔒" else "Lencana Terbuka 🎉",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (b.locked) Coral else Mint,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = b.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Detail Card (Krim Canvas FigCraft)
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Cream)
                                .padding(16.dp),
                    ) {
                        Text(
                            text = b.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink,
                            textAlign = TextAlign.Start,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Cara Mendapatkan:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = b.criteria,
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink.copy(alpha = 0.8f),
                            textAlign = TextAlign.Start,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { selectedBadge = null },
                        shape = RoundedCornerShape(24.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = if (b.locked) Primary else Mint,
                                contentColor = if (b.locked) Color.White else Ink,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                    ) {
                        Text(
                            text = if (b.locked) "Saya Mengerti! 💪" else "Keren Banget! 🎉",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(streakDays: Int) {
    JagaCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = stringResource(R.string.s07_streak_title),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlameIcon(modifier = Modifier.size(16.dp, 21.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$streakDays hari",
                    style = MaterialTheme.typography.displayMedium,
                    color = Ink,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val days = listOf("S", "S", "R", "K", "J", "S", "M")
        val states =
            List(7) { i ->
                when {
                    i < (streakDays - 1).coerceAtLeast(0) -> DayState.Done
                    i == (streakDays - 1).coerceAtLeast(0) -> DayState.Today
                    else -> DayState.Empty
                }
            }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            days.forEachIndexed { i, label ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DayCircle(state = states[i])
                }
            }
        }
    }
}

private enum class DayState { Done, Today, Empty }

@Composable
private fun DayCircle(state: DayState) {
    val bg =
        when (state) {
            DayState.Done -> Mint
            DayState.Today -> Sunny
            DayState.Empty -> Surface
        }
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bg)
                .then(
                    when (state) {
                        DayState.Empty -> Modifier.border(1.dp, Divider, CircleShape)
                        DayState.Today -> Modifier.border(2.dp, Ink, CircleShape)
                        DayState.Done -> Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            DayState.Done ->
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(18.dp),
                )
            DayState.Today ->
                FlameIcon(modifier = Modifier.size(11.dp, 15.dp))
            DayState.Empty -> Unit
        }
    }
}

/**
 * Flame line-art desain (fill #FFD166 + stroke #12263A 2px) — path dari SVG 2264.
 */
@Composable
private fun FlameIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sx = size.width / 15.167f
        val sy = size.height / 19.948f
        val path =
            Path().apply {
                moveTo(7.583f * sx, 19.948f * sy)
                cubicTo(2.917f * sx, 19.948f * sy, 0f, 16.798f * sy, 0f, 12.948f * sy)
                cubicTo(0f, 8.281f * sy, 4.083f * sx, 5.948f * sy, 4.667f * sx, 1.281f * sy)
                cubicTo(4.783f * sx, 0.231f * sy, 5.717f * sx, -0.352f * sy, 6.533f * sx, 0.231f * sy)
                cubicTo(10.5f * sx, 3.031f * sy, 15.167f * sx, 7.114f * sy, 15.167f * sx, 12.948f * sy)
                cubicTo(15.167f * sx, 16.798f * sy, 12.25f * sx, 19.948f * sy, 7.583f * sx, 19.948f * sy)
                close()
                // api dalam
                moveTo(7.583f * sx, 19.948f * sy)
                cubicTo(5.6f * sx, 19.948f * sy, 4.667f * sx, 18.431f * sy, 4.667f * sx, 16.798f * sy)
                cubicTo(4.667f * sx, 14.464f * sy, 6.417f * sx, 13.298f * sy, 7.583f * sx, 11.198f * sy)
                cubicTo(8.75f * sx, 13.298f * sy, 10.5f * sx, 14.464f * sy, 10.5f * sx, 16.798f * sy)
                cubicTo(10.5f * sx, 18.431f * sy, 9.567f * sx, 19.948f * sy, 7.583f * sx, 19.948f * sy)
                close()
            }
        drawPath(path, color = Sunny)
        drawPath(path, color = Ink, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun KikoGrowthCard(
    stage: Int,
    progress: Float,
    nextDays: Int,
) {
    val stageName =
        when (stage) {
            1 -> "Tahap 1 · Kiko Pemula"
            2 -> "Tahap 2 · Kiko Penjaga Berkacamata"
            else -> "Tahap 3 · Kiko Pahlawan Mata"
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(BlueTint, BlueTintDeep))),
    ) {
        // Kiko free-standing di kiri (tanpa disc biru, sesuai desain)
        KikoImage(
            expression = if (stage >= 2) KikoExpression.SENANG_KACAMATA else KikoExpression.SENANG,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
                    .size(80.dp),
        )
        Column(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 104.dp, end = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.s07_kiko_grow),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                text = stageName,
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Progress bar putih + isian Primary
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Surface),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(progress.coerceIn(0.1f, 1f))
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Primary),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$nextDays sesi lagi menuju tahap berikutnya",
                style = MaterialTheme.typography.labelSmall,
                color = Ink,
            )
        }
    }
}

@Composable
private fun BadgeGrid(
    badges: List<BadgeInfo>,
    onBadgeClick: (BadgeInfo) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            if (badges.size >= 2) {
                BadgeCard(
                    badge = badges[0],
                    onClick = { onBadgeClick(badges[0]) },
                    modifier = Modifier.weight(1f),
                )
                BadgeCard(
                    badge = badges[1],
                    onClick = { onBadgeClick(badges[1]) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            if (badges.size >= 4) {
                BadgeCard(
                    badge = badges[2],
                    onClick = { onBadgeClick(badges[2]) },
                    modifier = Modifier.weight(1f),
                )
                BadgeCard(
                    badge = badges[3],
                    onClick = { onBadgeClick(badges[3]) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BadgeCard(
    badge: BadgeInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JagaCard(
        modifier = modifier.clickable { onClick() },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 20.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(96.dp),
            contentAlignment = Alignment.Center,
        ) {
            BadgeMedallion(
                locked = badge.locked,
                modifier = Modifier.size(124.dp, 96.dp),
            )
            KikoImage(
                expression = if (badge.locked) KikoExpression.SENANG else KikoExpression.SENANG_KACAMATA,
                modifier =
                    Modifier
                        .size(62.dp)
                        .then(if (badge.locked) Modifier.graphicsLayer { alpha = 0.4f } else Modifier),
            )
            if (badge.locked) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 22.dp, bottom = 4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Ink),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Surface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = badge.title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badge.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Medallion lencana sesuai SVG desain (2264):
 * - terbuka: blob emas #FFD166 + ring krem #FFF6E9 (stroke 4) + konfeti bar/dot
 * - terkunci: blob abu #E3E8EE + ring #F2F7FF (stroke 4)
 * Koordinat path dari viewBox blob 80x80; kanvas 124x96 unit dengan blob box di offset (24,8).
 */
@Composable
private fun BadgeMedallion(
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val u = size.width / 124f
        val ox = 24f * u
        val oy = 8f * u

        if (!locked) {
            // Konfeti bar (10x4 r2) + dot, posisi dari SVG (di bawah blob)
            drawConfettiBar(ox, oy, u, dx = -16.33f, dy = 16.768f, rotation = -30f, color = Lavender)
            drawConfettiBar(ox, oy, u, dx = 85.314f, dy = 8.074f, rotation = 25f, color = Coral)
            drawConfettiBar(ox, oy, u, dx = 35.729f, dy = -3.101f, rotation = -10f, color = Sunny)
            drawCircle(
                color = Mint,
                radius = 3.5f * u,
                center = androidx.compose.ui.geometry.Offset(ox + -12f * u + 3.5f * u, oy + 66f * u + 3.5f * u),
            )
            drawCircle(
                color = Primary,
                radius = 4f * u,
                center = androidx.compose.ui.geometry.Offset(ox + 86f * u + 4f * u, oy + 58f * u + 4f * u),
            )
        }

        // Medali desain: path "blob" di SVG secara matematis lingkaran sempurna
        // (radius 40 pada viewBox 80×80, sisa fit <0.01 unit) — digambar lingkaran.
        drawCircle(
            color = if (locked) Divider else Sunny,
            radius = 40f * u,
            center = androidx.compose.ui.geometry.Offset(ox + 40f * u, oy + 40f * u),
        )

        // Ring (circle r38 stroke 4)
        drawCircle(
            color = if (locked) Background else Cream,
            radius = 38f * u,
            center = androidx.compose.ui.geometry.Offset(ox + 40f * u, oy + 40f * u),
            style = Stroke(width = 4f * u),
        )
    }
}

/** Bar konfeti 10x4 radius 2, dirotasi terhadap titik pusat bar. */
private fun DrawScope.drawConfettiBar(
    ox: Float,
    oy: Float,
    u: Float,
    dx: Float,
    dy: Float,
    rotation: Float,
    color: Color,
) {
    val cx = ox + (dx + 5f) * u
    val cy = oy + (dy + 2f) * u
    rotate(degrees = rotation, pivot = androidx.compose.ui.geometry.Offset(cx, cy)) {
        val rect =
            Path().apply {
                val x0 = ox + dx * u
                val y0 = oy + dy * u
                val w = 10f * u
                val h = 4f * u
                val r = 2f * u
                moveTo(x0 + r, y0)
                lineTo(x0 + w - r, y0)
                quadraticBezierTo(x0 + w, y0, x0 + w, y0 + r)
                lineTo(x0 + w, y0 + h - r)
                quadraticBezierTo(x0 + w, y0 + h, x0 + w - r, y0 + h)
                lineTo(x0 + r, y0 + h)
                quadraticBezierTo(x0, y0 + h, x0, y0 + h - r)
                lineTo(x0, y0 + r)
                quadraticBezierTo(x0, y0, x0 + r, y0)
                close()
            }
        drawPath(rect, color = color)
    }
}

/** Item bottom nav standar desain (Beranda · Pantau · Hadiah · Orang Tua) — ikon outline. */
@Composable
internal fun defaultBottomNavItems(): List<BottomNavItem> =
    listOf(
        BottomNavItem(stringResource(R.string.nav_beranda), Icons.Outlined.Home),
        BottomNavItem(stringResource(R.string.nav_pantau), Icons.Outlined.Visibility),
        BottomNavItem(stringResource(R.string.nav_hadiah), Icons.Outlined.CardGiftcard),
        BottomNavItem(stringResource(R.string.nav_ortu), Icons.Outlined.People),
    )
