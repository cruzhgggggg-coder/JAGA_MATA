package com.jagamata.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jagamata.app.ui.theme.Ink

/**
 * CTA menempel di bawah layar: x=20 (padding samping), bottom margin 40,
 * tombol 353×56 — mengikuti geometri CTA pada layar desain (y=756 dari 852).
 */
@Composable
fun BottomCtaBar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Ink,
    enabled: Boolean = true,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 40.dp),
    ) {
        PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = containerColor,
            enabled = enabled,
        )
    }
}
