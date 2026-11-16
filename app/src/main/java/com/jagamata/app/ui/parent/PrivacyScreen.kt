package com.jagamata.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.privacy_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.privacy_desc),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.privacy_delete_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.privacy_delete_desc),
            style = MaterialTheme.typography.bodySmall,
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = onBack) {
            Text(stringResource(R.string.close))
        }
    }
}
