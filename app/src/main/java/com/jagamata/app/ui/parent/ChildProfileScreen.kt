package com.jagamata.app.ui.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.components.Sparkle
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTint
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Cream
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.InkSoft
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.PrimaryDark
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt

/**
 * 08 Profil Anak — kartu profil, target harian, pengaturan sensitif.
 * Tanpa bottom nav (ikut desain). Tombol pengaturan → [onOpenSettings].
 * CTA outline bottom-anchored mengikuti geometri desain (y698–758 dari 852).
 */
@Composable
fun ChildProfileScreen(
    onBack: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
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
                    .verticalScroll(rememberScrollState()),
        ) {
            // Header: tombol back + judul tengah
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 48.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Surface),
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.onboarding_back),
                        tint = Ink,
                    )
                }
                Text(
                    text = stringResource(R.string.s08_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 56.dp),
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 170.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProfileCard()
                TargetCard()
                SensitiveSettingsCard()
            }
        }

        // CTA bottom-anchored (desain: y698–758 → margin bawah 94, tepat di atas awan)
        OpenSettingsButton(
            onClick = onOpenSettings,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 20.dp, end = 20.dp, bottom = 94.dp),
        )

        // Awan dekorasi kanan bawah
        CloudDecoration(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun ProfileCard() {
    JagaCard(contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Sparkle emas kiri
            Sparkle(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 40.dp, top = 48.dp)
                        .size(12.dp),
            )
            // Sparkle emas kanan atas
            Sparkle(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 36.dp, top = 8.dp)
                        .size(16.dp),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Avatar R
                Box(
                    modifier =
                        Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Sunny)
                            .border(4.dp, SurfaceAlt, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "R",
                        style = MaterialTheme.typography.displayLarge,
                        color = Ink,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.s08_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoChip(text = stringResource(R.string.s08_age))
                    InfoChip(text = stringResource(R.string.s08_grade))
                }
            }
        }
    }
}

/**
 * Pill info: fill krem #FFF6E9 + bintang outline (stroke ink 2) — sesuai SVG 2581.
 */
@Composable
private fun InfoChip(text: String) {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(Cream)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlineStar(modifier = Modifier.size(14.dp, 13.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
        )
    }
}

/** Bintang 5 sudut outline (path SVG pill, stroke #12263A 2px). */
@Composable
private fun OutlineStar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sx = size.width / 13.2f
        val sy = size.height / 12.525f
        val path =
            Path().apply {
                moveTo(6.6f * sx, 0f)
                lineTo(8.625f * sx, 4.2f * sy)
                lineTo(13.2f * sx, 4.8f * sy)
                lineTo(9.9f * sx, 7.95f * sy)
                lineTo(10.725f * sx, 12.525f * sy)
                lineTo(6.6f * sx, 10.35f * sy)
                lineTo(2.475f * sx, 12.525f * sy)
                lineTo(3.3f * sx, 7.95f * sy)
                lineTo(0f, 4.8f * sy)
                lineTo(4.575f * sx, 4.2f * sy)
                close()
            }
        drawPath(path, color = Ink, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun TargetCard() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(BlueTint, BlueTintDeep))),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, end = 120.dp),
        ) {
            Text(
                text = stringResource(R.string.s08_target_label),
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.s08_target_value),
                style = MaterialTheme.typography.displayMedium,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.s08_target_desc),
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
            )
        }
        // Kiko free-standing di kanan (tanpa disc biru)
        KikoImage(
            expression = KikoExpression.SENANG,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(80.dp),
        )
    }
}

@Composable
private fun SensitiveSettingsCard() {
    JagaCard(
        modifier = Modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Ink),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Surface,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.s08_sensitive_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.s08_sensitive_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.72f),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        SettingsRow(
            label = stringResource(R.string.s08_row_limit),
            value = stringResource(R.string.s08_row_limit_value),
        )
        Spacer(modifier = Modifier.height(18.dp))
        SettingsRow(
            label = stringResource(R.string.s08_row_distance),
            value = stringResource(R.string.s08_row_distance_value),
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
        )
    }
}

@Composable
private fun OpenSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(60.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(2.dp, Primary),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = Surface,
                contentColor = PrimaryDark,
            ),
    ) {
        Text(
            text = stringResource(R.string.s08_open_settings),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun CloudDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(56.dp, 32.dp)) {
        val w = size.width
        val h = size.height
        drawCircle(color = Color.White, radius = h * 0.45f, center = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.55f))
        drawCircle(color = Color.White, radius = h * 0.38f, center = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.5f))
        drawCircle(color = Color.White, radius = h * 0.32f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.35f))
        drawRect(
            color = Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.55f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.4f),
        )
    }
}
