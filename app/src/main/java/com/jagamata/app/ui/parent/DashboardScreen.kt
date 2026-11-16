package com.jagamata.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R

@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit,
    onOpenReport: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_week_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StatCell(stringResource(R.string.stat_close), "0")
                    StatCell(stringResource(R.string.stat_dark), "0")
                    StatCell(stringResource(R.string.stat_posture), "0")
                }
            }
        }
        Text(
            text = stringResource(R.string.dashboard_no_data),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onOpenReport) {
                Text(stringResource(R.string.nav_report))
            }
            Button(onClick = onOpenSettings) {
                Text(stringResource(R.string.nav_settings))
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
) {
    Column {
        Text(text = value, style = MaterialTheme.typography.displaySmall)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
