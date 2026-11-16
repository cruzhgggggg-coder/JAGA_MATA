package com.jagamata.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R

/**
 * Hapus Semua Data (PRD US-14): menghapus seluruh tabel Room + DataStore,
 * dengan dialog konfirmasi.
 */
@Composable
fun DeleteDataScreen(
    onDeleteAll: () -> Unit,
    onDone: () -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.privacy_delete_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.privacy_delete_desc),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (deleted) {
            Text(
                text = stringResource(R.string.privacy_delete_done),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Button(
            onClick = { showConfirm = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.privacy_delete_confirm))
        }
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.privacy_delete_cancel))
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.privacy_delete_title)) },
            text = { Text(stringResource(R.string.privacy_delete_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        deleted = true
                        onDeleteAll()
                    },
                ) {
                    Text(stringResource(R.string.privacy_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.privacy_delete_cancel))
                }
            },
        )
    }
}
