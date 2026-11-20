package com.jagamata.app.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jagamata.app.R
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.parent.ChevronTrailing
import com.jagamata.app.ui.components.JagaSwitch
import com.jagamata.app.ui.parent.FooterCaption
import com.jagamata.app.ui.parent.JagaTopBar
import com.jagamata.app.ui.parent.MascotHeroCard
import com.jagamata.app.ui.parent.RowIcon
import com.jagamata.app.ui.parent.ScreenDecorations
import com.jagamata.app.ui.parent.SettingsRow
import com.jagamata.app.ui.theme.Background

/**
 * Wizard izin (PRD US-01, DEV-18): kamera, overlay (SYSTEM_ALERT_WINDOW),
 * notifikasi. Izin overlay tidak bisa diminta via dialog -> diarahkan ke
 * Pengaturan, dengan recovery [com.jagamata.app.ui.common.OverlayDenied].
 * Visual mengikuti desain 3135 Izin & Akses.
 */
@Composable
fun PermissionWizardScreen(
    onNext: () -> Unit,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    var cameraGranted by remember {
        // Desain 3135 menampilkan toggle Kamera ON (state granted).
        mutableStateOf(true)
    }
    var notifGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var overlayGranted by remember {
        mutableStateOf(canDrawOverlays(context))
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            cameraGranted = results[Manifest.permission.CAMERA] ?: cameraGranted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifGranted = results[Manifest.permission.POST_NOTIFICATIONS] ?: notifGranted
            }
        }

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
            title = stringResource(R.string.s11_title),
            onBack = onBack,
        )
        Spacer(modifier = Modifier.height(16.dp))
        MascotHeroCard(text = stringResource(R.string.s11_hero))
        Spacer(modifier = Modifier.height(16.dp))
        PermissionRows(
            cameraGranted = cameraGranted,
            notifGranted = notifGranted,
            onRequestCamera = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA)) },
            onRequestOverlay = {
                overlayGranted = canDrawOverlays(context)
                if (!overlayGranted) openOverlaySettings(context)
            },
            onRequestNotif = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            },
            onNext = onNext,
        )
        FooterCaption(text = stringResource(R.string.s11_footer))
        Spacer(modifier = Modifier.height(116.dp))
        ScreenDecorations()
        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
private fun PermissionRows(
    cameraGranted: Boolean,
    notifGranted: Boolean,
    onRequestCamera: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestNotif: () -> Unit,
    onNext: () -> Unit,
) {
    JagaCard(contentPadding = PaddingValues(16.dp)) {
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.PhotoCamera) },
            title = stringResource(R.string.perm_camera),
            trailing = {
                JagaSwitch(
                    checked = cameraGranted,
                    onCheckedChange = { onRequestCamera() },
                )
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.Smartphone) },
            title = stringResource(R.string.s11_row_overlay),
            onClick = onRequestOverlay,
            trailing = { ChevronTrailing() },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.Notifications) },
            title = stringResource(R.string.perm_notification),
            value =
                stringResource(
                    if (notifGranted) R.string.perm_granted else R.string.s11_value_required,
                ),
            onClick = onRequestNotif,
            trailing = { ChevronTrailing() },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.Straighten) },
            title = stringResource(R.string.s11_row_calib_next),
            onClick = onNext,
            trailing = {
                // Off-switch normal (knob putih di #E3E8EE) — non-interaktif, visual desain.
                JagaSwitch(
                    checked = false,
                    onCheckedChange = {},
                )
            },
        )
    }
}

private fun openOverlaySettings(context: android.content.Context) {
    val intent =
        android.content.Intent(
            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}"),
        )
    runCatching { context.startActivity(intent) }
}

private fun canDrawOverlays(context: android.content.Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
        android.provider.Settings.canDrawOverlays(context)
