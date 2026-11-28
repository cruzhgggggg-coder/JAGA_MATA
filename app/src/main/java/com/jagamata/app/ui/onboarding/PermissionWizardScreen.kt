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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jagamata.app.R
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.JagaSwitch
import com.jagamata.app.ui.components.PrimaryButton
import com.jagamata.app.ui.components.SecondaryButton
import com.jagamata.app.ui.parent.ChevronTrailing
import com.jagamata.app.ui.parent.FooterCaption
import com.jagamata.app.ui.parent.JagaTopBar
import com.jagamata.app.ui.parent.MascotHeroCard
import com.jagamata.app.ui.parent.RowIcon
import com.jagamata.app.ui.parent.ScreenDecorations
import com.jagamata.app.ui.parent.SettingsRow
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.Coral

/**
 * Wizard izin (PRD US-01, DEV-18): kamera, overlay (SYSTEM_ALERT_WINDOW),
 * notifikasi. Izin overlay tidak bisa diminta via dialog -> diarahkan ke
 * Pengaturan, dengan recovery [com.jagamata.app.ui.common.OverlayDenied].
 * Visual mengikuti desain 3135 Izin & Akses.
 *
 * T8: status overlay kini ditampilkan dan "Lanjut ke Beranda" hanya aktif bila
 * overlay diizinkan; tersedia jalur sadar-risiko "Lanjut tanpa peringatan layar".
 * ON_RESUME memuat ulang status izin (kembali dari Pengaturan, M21 parsial).
 */
@Composable
fun PermissionWizardScreen(
    onNext: () -> Unit,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val hasCamera =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    var cameraGranted by remember {
        mutableStateOf(hasCamera)
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

    // Muat ulang status izin nyata (T8/M21) — mis. setelah kembali dari Pengaturan.
    val refreshPermissionState = {
        cameraGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        notifGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        overlayGranted = canDrawOverlays(context)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) refreshPermissionState()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            cameraGranted = results[Manifest.permission.CAMERA] ?: (
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifGranted = results[Manifest.permission.POST_NOTIFICATIONS] ?: notifGranted
            }
        }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!hasCamera) {
            val perms = mutableListOf(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(perms.toTypedArray())
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
            overlayGranted = overlayGranted,
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
        Spacer(modifier = Modifier.height(24.dp))
        if (overlayGranted) {
            PrimaryButton(
                text = stringResource(R.string.s11_next_home),
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // T8: tanpa overlay, peringatan tidak tampil di atas aplikasi lain —
            // tombol utama dikunci, tapi tidak memblokir selamanya: ada jalur
            // sadar-risiko yang eksplisit (keputusan: soft-block, bukan hard-block).
            Text(
                text = stringResource(R.string.s11_overlay_warning),
                style = MaterialTheme.typography.bodySmall,
                color = Coral,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryButton(
                text = stringResource(R.string.s11_skip_without_overlay),
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        ScreenDecorations()
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionRows(
    cameraGranted: Boolean,
    notifGranted: Boolean,
    overlayGranted: Boolean,
    onRequestCamera: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestNotif: () -> Unit,
    onNext: () -> Unit,
) {
    JagaCard(contentPadding = PaddingValues(16.dp)) {
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.PhotoCamera) },
            title = stringResource(R.string.perm_camera),
            onClick = onRequestCamera,
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
            // T8: status izin overlay kini terlihat di wizard.
            value =
                stringResource(
                    if (overlayGranted) R.string.perm_granted else R.string.s11_overlay_missing,
                ),
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
            trailing = { ChevronTrailing() },
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
