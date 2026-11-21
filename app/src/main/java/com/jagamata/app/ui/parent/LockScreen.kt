package com.jagamata.app.ui.parent

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jagamata.app.R
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.components.Sparkle
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTint
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 13 Kunci Orang Tua — Layar Masuk PIN bergaya M-Banking (Keypad Angka Kustom + Titik Animasi).
 * Sesuai panduan desain FigCraft: tanpa keyboard sistem, verifikasi otomatis saat 4 digit terisi.
 */
@Composable
fun LockScreen(
    verifyPin: (String, (Boolean) -> Unit) -> Unit,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit = {},
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Trigger verifikasi otomatis saat digit ke-4 ditekan
    LaunchedEffect(pin) {
        if (pin.length == 4 && !verifying) {
            verifying = true
            verifyPin(pin) { ok ->
                verifying = false
                if (ok) {
                    onUnlocked()
                } else {
                    isError = true
                    scope.launch {
                        delay(600L)
                        pin = ""
                        isError = false
                    }
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Bar: Sparkle kiri & Tombol Batal kanan
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Sparkle(modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(R.string.s13_cancel),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Primary,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Card Kiko Mini
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(BlueTint, BlueTintDeep))),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Area Khusus Orang Tua",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lindungi setelan & statistik dari anak",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.72f),
                    )
                }
                KikoImage(
                    expression = if (isError) KikoExpression.KAGET else KikoExpression.NETRAL,
                    modifier = Modifier.size(76.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Masukkan PIN Orang Tua",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isError) "PIN salah! Masukkan kembali (Default: 1234)" else "PIN 4-digit standar (Default: 1234)",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) Coral else Ink.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Indikator 4 Titik PIN bergaya M-Banking
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pin.length
                val dotColor by animateColorAsState(
                    targetValue = if (isError) Coral else if (isFilled) Primary else SurfaceAlt,
                    label = "dotColor_$i",
                )
                val dotScale = remember { Animatable(1f) }

                LaunchedEffect(isFilled) {
                    if (isFilled) {
                        dotScale.animateTo(
                            targetValue = 1.25f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        )
                        dotScale.animateTo(1f)
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .scale(dotScale.value)
                            .shadow(if (isFilled) 4.dp else 0.dp, CircleShape)
                            .clip(CircleShape)
                            .background(dotColor),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Keypad Angka Kustom M-Banking (3x4)
        NumericKeypad(
            onDigitClick = { digit ->
                if (pin.length < 4 && !verifying) {
                    pin += digit
                    isError = false
                }
            },
            onDeleteClick = {
                if (pin.isNotEmpty() && !verifying) {
                    pin = pin.dropLast(1)
                    isError = false
                }
            },
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/**
 * Keypad angka 3x4 bergaya M-Banking tanpa keyboard sistem Android.
 */
@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
) {
    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL"),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        for (row in keypadRows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (key in row) {
                    when (key) {
                        "" -> {
                            // Spacer kosong penyeimbang kolom kiri bawah
                            Box(modifier = Modifier.size(68.dp))
                        }
                        "DEL" -> {
                            // Tombol Backspace
                            Box(
                                modifier =
                                    Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceAlt)
                                        .clickable(onClick = onDeleteClick),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Hapus",
                                    tint = Ink,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                        else -> {
                            // Tombol Angka (1..9, 0)
                            Box(
                                modifier =
                                    Modifier
                                        .size(68.dp)
                                        .shadow(2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Surface)
                                        .clickable { onDigitClick(key) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = Ink,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
