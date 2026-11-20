package com.jagamata.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.ZoneDanger
import com.jagamata.app.ui.theme.ZoneSafe
import com.jagamata.app.ui.theme.ZoneWarn

/**
 * Chip status zona desain: tinggi 22, radius 11, label Nunito Bold 11.
 * Aman #5FD4A7 · Waspada #FFD166 · Bahaya #FF7B6B.
 */
@Composable
fun ZoneChip(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Ink,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier =
            modifier
                .background(containerColor, RoundedCornerShape(11.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/**
 * Pill status desain (toast/status message): tinggi 34, radius 17,
 * teks Nunito SemiBold 13 di atas warna zona.
 */
@Composable
fun StatusPill(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Ink,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Text(
        text = text,
        style = style,
        color = textColor,
        modifier =
            modifier
                .background(containerColor, RoundedCornerShape(17.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

/** Convenience: chip zona berdasar nama status. */
@Composable
fun ZoneChipForStatus(
    status: String,
    modifier: Modifier = Modifier,
) {
    val color =
        when (status) {
            "Aman" -> ZoneSafe
            "Waspada" -> ZoneWarn
            else -> ZoneDanger
        }
    ZoneChip(text = status, containerColor = color, modifier = modifier)
}
