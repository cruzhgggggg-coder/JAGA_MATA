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
import com.jagamata.app.ui.theme.Sunny

/**
 * Banner peringatan (PRD §2.12, US-05) — pill desain h34 r17
 * (seperti "Agak dekat, mundur sedikit ya") di atas Sunny, teks Caption.
 */
@Composable
fun WarningBanner(message: String = stringResource(R.string.status_warning)) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        StatusPill(
            text = message,
            containerColor = Sunny,
            textColor = Ink,
        )
    }
}
