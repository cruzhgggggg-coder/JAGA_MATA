package com.jagamata.app.ui.common

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.BottomCtaBar
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.components.Sparkle
import com.jagamata.app.ui.onboarding.Cloud
import com.jagamata.app.ui.onboarding.SpeechBubble
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTint
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.PrimaryDark
import com.jagamata.app.ui.theme.Sunny

/**
 * Layar recovery saat izin overlay ditolak (desain 15 · 3518_overlay_ditolak).
 * "Buka Pengaturan" membuka setelan overlay Android; "Nanti" = jalur fallback
 * notifikasi ([onFallbackNotification]).
 */
@Composable
fun OverlayDenied(onFallbackNotification: () -> Unit = {}) {
    val context = LocalContext.current
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        // Bar atas: dekorasi + "Nanti"
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
                text = stringResource(R.string.od_skip),
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryDark,
                modifier = Modifier.clickable(onClick = onFallbackNotification),
            )
        }

        // Hero card (20,158.5) 353x360 r24 + gelembung + Kiko
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
        ) {
            SpeechBubble(
                text = stringResource(R.string.od_bubble),
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

        // Judul + deskripsi
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(20.dp, 575.5.dp)
                    .width(353.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.od_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.od_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = Ink.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        // Dots wizard (langkah 1 dari 3)
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
                            .size(width = if (i == 0) 24.dp else 8.dp, height = 8.dp)
                            .background(
                                color = if (i == 0) Primary else Divider,
                                shape = RoundedCornerShape(4.dp),
                            ),
                )
            }
        }

        BottomCtaBar(
            text = stringResource(R.string.od_cta),
            onClick = {
                val intent =
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    )
                runCatching { context.startActivity(intent) }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
