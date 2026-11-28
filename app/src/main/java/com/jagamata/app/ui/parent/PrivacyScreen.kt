package com.jagamata.app.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.Mint

/**
 * Privasi & Data (desain 3229): status data, hapus semua data,
 * kebijakan privasi. Logika hapus ada di [DeleteDataScreen].
 */
@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onDeleteData: () -> Unit = {},
) {
    var policyExpanded by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
    ) {
        JagaTopBar(
            title = stringResource(R.string.s14_title),
            onBack = onBack,
        )
        Spacer(modifier = Modifier.height(16.dp))
        MascotHeroCard(text = stringResource(R.string.s14_hero))
        Spacer(modifier = Modifier.height(16.dp))
        JagaCard(contentPadding = PaddingValues(16.dp)) {
            // A14: toggle mati diganti info faktual (tanpa aksi yang menyesatkan).
            SettingsRow(
                icon = { RowIcon(Icons.Outlined.PhotoCamera) },
                title = stringResource(R.string.s14_row_photos),
                trailing = {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = Mint,
                    )
                },
            )
            Spacer(modifier = Modifier.height(24.dp))
            SettingsRow(
                icon = { RowIcon(Icons.Outlined.Language) },
                title = stringResource(R.string.s14_row_offline),
            )
            Spacer(modifier = Modifier.height(24.dp))
            SettingsRow(
                icon = { RowIcon(Icons.Outlined.Tune) },
                title = stringResource(R.string.s14_row_delete),
                value = stringResource(R.string.s14_value_pdp),
                onClick = onDeleteData,
                trailing = { ChevronTrailing() },
            )
            Spacer(modifier = Modifier.height(24.dp))
            SettingsRow(
                icon = { RowIcon(Icons.Outlined.Lock) },
                title = stringResource(R.string.s14_row_policy),
                onClick = { policyExpanded = !policyExpanded },
                trailing = { ChevronTrailing() },
            )
            if (policyExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.s14_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        FooterCaption(text = stringResource(R.string.s14_footer))
        Spacer(modifier = Modifier.height(116.dp))
        ScreenDecorations()
        Spacer(modifier = Modifier.height(110.dp))
    }
}
