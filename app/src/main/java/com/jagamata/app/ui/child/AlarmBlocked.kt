package com.jagamata.app.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.theme.Coral

/**
 * Layar alarm penuh saat BLOCKED (PRD §2.12, US-05): maskot Kiko +
 * "Ayo mundur dikit!" + haptic 200 ms (haptic dipanggil dari OverlayService).
 */
@Composable
fun AlarmBlocked(
    title: String = stringResource(R.string.alarm_title),
    subtitle: String = stringResource(R.string.alarm_subtitle),
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Coral)
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MascotKiko(modifier = Modifier.size(180.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

/** Maskot Kiko (burung hantu) — placeholder geometris (PRD §2.19). */
@Composable
fun MascotKiko(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(R.drawable.mascot),
        contentDescription = stringResource(R.string.app_name),
        modifier = modifier.clip(RoundedCornerShape(24.dp)),
    )
}
