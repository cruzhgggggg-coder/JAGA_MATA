package com.jagamata.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R

/**
 * Pilih usia anak (PRD US-01) — satu-satunya langkah yang boleh dilewati.
 */
@Composable
fun AgeSelectScreen(
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.age_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.age_desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        AgeOption(stringResource(R.string.age_5_7), onClick = { onSelect("5-7"); onNext() })
        AgeOption(stringResource(R.string.age_8_12), onClick = { onSelect("8-12"); onNext() })
        AgeOption(stringResource(R.string.age_13_plus), onClick = { onSelect("13+"); onNext() })
        TextButton(onClick = onSkip, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.onboarding_skip))
        }
    }
}

@Composable
private fun AgeOption(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
