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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    var failedAttempts by rememberSaveable { mutableStateOf(0) }
    var lockoutCount by rememberSaveable { mutableStateOf(0) }
    var lockoutUntilMs by rememberSaveable { mutableStateOf(0L) }
    var lockSecondsLeft by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Hitung mundur lockout agar pesan & keypad pulih otomatis saat waktu habis
    LaunchedEffect(lockoutUntilMs) {
        while (true) {
            val remainingSec = ((lockoutUntilMs - System.currentTimeMillis()) / 1000L).toInt()
            lockSecondsLeft = remainingSec.coerceAtLeast(0)
            if (remainingSec <= 0) break
            delay(1_000L)
        }
    }

    // Trigger verifikasi otomatis saat digit ke-4 ditekan
    LaunchedEffect(pin) {
        if (pin.length == 4 && !verifying && System.currentTimeMillis() >= lockoutUntilMs) {
            verifying = true
            verifyPin(pin) { ok ->
                verifying = false
                if (ok) {
                    failedAttempts = 0
                    onUnlocked()
                } else {
                    isError = true
                    failedAttempts += 1
                    if (failedAttempts >= MAX_PIN_ATTEMPTS) {
                        lockoutCount += 1
                        lockoutUntilMs =
                            System.currentTimeMillis() +
                                (LOCKOUT_BASE_MS * lockoutCount)
                                    .coerceAtMost(LOCKOUT_BASE_MS * LOCKOUT_MAX_MULTIPLIER)
                        failedAttempts = 0
                    }
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
        val lockedOut = lockoutUntilMs > System.currentTimeMillis()
        val subtitleText =
            when {
                lockedOut ->
                    "Terlalu banyak percobaan. Coba lagi dalam ${lockSecondsLeft.coerceAtLeast(1)} detik."
                isError -> "PIN salah! Sisa percobaan: ${MAX_PIN_ATTEMPTS - failedAttempts}"
                else -> "Masukkan PIN 4 digit orang tua Anda."
            }
        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError || lockedOut) Coral else Ink.copy(alpha = 0.72f),
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
                    targetValue =
                        if (isError) {
                            Coral
                        } else if (isFilled) {
                            Primary
                        } else {
                            SurfaceAlt
                        },
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
                if (pin.length < 4 && !verifying && System.currentTimeMillis() >= lockoutUntilMs) {
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
    val keypadRows =
        listOf(
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
                                    style =
                                        MaterialTheme.typography.titleLarge.copy(
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

private const val MAX_PIN_ATTEMPTS = 5
private const val LOCKOUT_BASE_MS = 30_000L
private const val LOCKOUT_MAX_MULTIPLIER = 5
