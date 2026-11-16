package com.jagamata.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
fun WeeklyReportScreen(onBack: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.report_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.report_empty),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onBack) {
            Text(stringResource(R.string.close))
        }
    }
}
