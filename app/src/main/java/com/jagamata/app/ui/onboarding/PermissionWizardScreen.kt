package com.jagamata.app.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jagamata.app.R

/**
 * Wizard izin (PRD US-01, DEV-18): kamera, overlay (SYSTEM_ALERT_WINDOW),
 * notifikasi. Izin overlay tidak bisa diminta via dialog -> diarahkan ke
 * Pengaturan, dengan recovery [com.jagamata.app.ui.common.OverlayDenied].
 */
@Composable
fun PermissionWizardScreen(
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var notifGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var overlayGranted by remember {
        mutableStateOf(canDrawOverlays(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        cameraGranted = results[Manifest.permission.CAMERA] ?: cameraGranted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifGranted = results[Manifest.permission.POST_NOTIFICATIONS] ?: notifGranted
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.perm_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.perm_desc),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
        PermissionRow(
            title = stringResource(R.string.perm_camera),
            desc = stringResource(R.string.perm_camera_desc),
            granted = cameraGranted,
            onGrant = {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        )
        PermissionRow(
            title = stringResource(R.string.perm_overlay),
            desc = stringResource(R.string.perm_overlay_desc),
            granted = overlayGranted,
            onGrant = {
                overlayGranted = canDrawOverlays(context)
                if (!overlayGranted) {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${context.packageName}")
                    )
                    runCatching { context.startActivity(intent) }
                }
            }
        )
        PermissionRow(
            title = stringResource(R.string.perm_notification),
            desc = stringResource(R.string.perm_notification_desc),
            granted = notifGranted,
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            }
        )
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(stringResource(R.string.onboarding_next))
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    desc: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onGrant, enabled = !granted) {
            Text(
                if (granted) stringResource(R.string.perm_granted)
                else stringResource(R.string.perm_grant)
            )
        }
    }
}

private fun canDrawOverlays(context: android.content.Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
        android.provider.Settings.canDrawOverlays(context)
