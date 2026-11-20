package com.jagamata.app.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.InkSoft

/**
 * Placeholder untuk frame desain yang belum punya padanan di kode.
 * Batch layar akan menggantinya dengan implementasi asli.
 */
@Composable
fun DesignPlaceholder(title: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Belum dibuat: $title",
            style = MaterialTheme.typography.titleLarge,
            color = InkSoft,
        )
    }
}
