package com.jagamata.app.ui.parent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.Surface

/**
 * 13 Kunci Orang Tua — layar PIN mode orang tua (PRD US-11).
 * Visual mengikuti desain FigCraft 3445: hero Kiko + bubble, hint PIN,
 * titik progres, CTA "Buka". [verifyPin] tetap dipanggil saat Buka ditekan.
 *
 * @param onCancel aksi "Batal" di header (opsional, default no-op).
 */
@Composable
fun LockScreen(
    verifyPin: (String, (Boolean) -> Unit) -> Unit,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit = {},
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                // Ketuk area mana pun di luar CTA → fokus input PIN tersembunyi
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusRequester.requestFocus()
                },
    ) {
        // Input PIN tersembunyi (keyboard angka) — desain tidak menampilkan keypad
        BasicTextField(
            value = pin,
            onValueChange = { new ->
                if (new.length <= 4 && new.all(Char::isDigit)) {
                    pin = new
                    error = false
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(Color.Transparent),
            modifier =
                Modifier
                    .focusRequester(focusRequester)
                    .size(1.dp)
                    .padding(0.dp),
            decorationBox = { inner -> Box(modifier = Modifier.size(0.dp)) { inner() } },
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 48.dp),
            ) {
                // Header: sparkle emas kiri + Batal kanan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Sparkle(modifier = Modifier.size(14.dp))
                    Text(
                        text = stringResource(R.string.s13_cancel),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    focusManager.clearFocus()
                                    onCancel()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                HeroPinCard(modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = stringResource(R.string.s13_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Titik PIN: ● terisi penuh (ink) sesuai copy desain
                val dots = List(4) { "●" }.joinToString(" ")
                Text(
                    text = stringResource(R.string.s13_pin_hint, dots),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { focusRequester.requestFocus() },
                )

                if (error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.lock_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = Coral,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Indikator halaman (3 titik, aktif pertama)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(24.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Primary),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Divider),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Divider),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            BottomCtaBar(
                text = stringResource(R.string.s13_submit),
                onClick = {
                    if (pin.length == 4 && !verifying) {
                        verifying = true
                        verifyPin(pin) { ok ->
                            verifying = false
                            if (ok) {
                                focusManager.clearFocus()
                                onUnlocked()
                            } else {
                                error = true
                                pin = ""
                            }
                        }
                    } else if (pin.length < 4) {
                        error = true
                        focusRequester.requestFocus()
                    }
                },
                enabled = !verifying,
            )
        }
    }
}

@Composable
private fun HeroPinCard(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(BlueTint, BlueTintDeep))),
    ) {
        // Sparkle emas kiri atas
        Sparkle(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 40.dp)
                    .size(16.dp),
        )
        // Sparkle putih di sekitar maskot
        Sparkle(
            color = Color.White,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 40.dp, top = 84.dp)
                    .size(12.dp),
        )
        Sparkle(
            color = Color.White,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 28.dp)
                    .size(14.dp),
        )
        Sparkle(
            color = Color.White,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .size(10.dp),
        )

        // Awan kanan bawah
        Canvas(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 40.dp)
                    .size(64.dp, 36.dp),
        ) {
            val w = size.width
            val h = size.height
            drawCircle(Color.White, radius = h * 0.42f, center = Offset(w * 0.35f, h * 0.55f))
            drawCircle(Color.White, radius = h * 0.36f, center = Offset(w * 0.65f, h * 0.5f))
            drawCircle(Color.White, radius = h * 0.3f, center = Offset(w * 0.5f, h * 0.32f))
        }

        // Speech bubble + ekor (ekor menempel di bawah bubble berapa pun tingginya)
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .fillMaxWidth(0.86f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.s13_bubble),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Canvas(modifier = Modifier.size(18.dp, 13.dp)) {
                val path =
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                drawPath(path, color = Surface)
            }
        }

        // Maskot Kiko (aset kanonik)
        KikoImage(
            expression = KikoExpression.NETRAL,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .fillMaxHeight(0.62f)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true),
        )
    }
}
