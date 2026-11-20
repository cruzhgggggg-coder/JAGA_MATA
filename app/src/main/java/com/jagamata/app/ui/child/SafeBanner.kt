package com.jagamata.app.ui.child

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.StatusPill
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Mint

/**
 * Banner tipis saat AMAN (PRD §2.12) — pill desain h34 r17
 * (seperti "Jarakmu sudah pas! 👍") di atas Mint. Bisa dimatikan di Settings (US-11).
 */
@Composable
fun SafeBanner() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        StatusPill(
            text = stringResource(R.string.banner_safe),
            containerColor = Mint,
            textColor = Ink,
        )
    }
}
