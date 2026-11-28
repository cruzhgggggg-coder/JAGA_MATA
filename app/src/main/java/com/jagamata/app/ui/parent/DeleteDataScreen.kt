package com.jagamata.app.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.jagamata.app.R
import com.jagamata.app.report.ReportGenerator
import com.jagamata.app.ui.components.BottomCtaBar
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.SecondaryButton
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.Coral
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.InkSoft
import com.jagamata.app.ui.theme.Mint
import com.jagamata.app.ui.theme.SurfaceAlt
import kotlinx.coroutines.launch

/**
 * Hapus Semua Data (PRD US-14): menghapus seluruh tabel Room + DataStore,
 * dengan dialog konfirmasi. Mengikuti bahasa desain 14 Privasi & Data.
 */
@Composable
fun DeleteDataScreen(
    onDeleteAll: suspend () -> Unit,
    onDone: () -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .statusBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
        ) {
            JagaTopBar(
                title = stringResource(R.string.privacy_delete_title),
                onBack = onDone,
            )
            Spacer(modifier = Modifier.height(16.dp))
            JagaCard(contentPadding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceAlt),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = null,
                            tint = Coral,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.privacy_delete_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Ink,
                        )
                        Text(
                            text = stringResource(R.string.privacy_delete_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                if (deleted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Mint,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.privacy_delete_done),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Mint,
                        )
                    }
                }
            }
        }
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            SecondaryButton(
                text = stringResource(R.string.privacy_delete_cancel),
                onClick = onDone,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
            )
            BottomCtaBar(
                text = stringResource(R.string.privacy_delete_confirm),
                onClick = { showConfirm = true },
                containerColor = Coral,
                modifier = Modifier.padding(top = 12.dp),
            )
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
                        scope.launch {
                            // M12: urutan hapus menyeluruh — DB+DataStore (dari caller),
                            // lalu file laporan di cache, lalu notifikasi yang masih tampil.
                            onDeleteAll()
                            ReportGenerator.clearReportCache(context)
                            NotificationManagerCompat.from(context).cancelAll()
                            deleted = true
                        }
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
