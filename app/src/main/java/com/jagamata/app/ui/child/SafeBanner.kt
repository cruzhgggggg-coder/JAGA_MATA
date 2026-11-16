package com.jagamata.app.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.theme.Mint

/**
 * Banner tipis saat AMAN (PRD §2.12) — opsional, bisa dimatikan di Settings (US-11).
 */
@Composable
fun SafeBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Mint)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.banner_safe),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}
