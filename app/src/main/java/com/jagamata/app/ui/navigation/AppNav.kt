package com.jagamata.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jagamata.app.R
import com.jagamata.app.data.AppDatabase
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.service.DetectionForegroundService
import com.jagamata.app.service.NotificationHelper
import com.jagamata.app.service.PauseSessionManager
import com.jagamata.app.ui.child.AlarmBlocked
import com.jagamata.app.ui.child.BerandaScreen
import com.jagamata.app.ui.child.BreakScreen
import com.jagamata.app.ui.common.OverlayDenied
import com.jagamata.app.ui.debug.DebugGallery
import com.jagamata.app.ui.live.PantauLiveScreen
import com.jagamata.app.ui.onboarding.CalibrationScreen
import com.jagamata.app.ui.onboarding.OnboardingFlow
import com.jagamata.app.ui.onboarding.PermissionWizardScreen
import com.jagamata.app.ui.onboarding.SplashScreen
import com.jagamata.app.ui.parent.ChildProfileScreen
import com.jagamata.app.ui.parent.DailyStatsScreen
import com.jagamata.app.ui.parent.DashboardScreen
import com.jagamata.app.ui.parent.DeleteDataScreen
import com.jagamata.app.ui.parent.LockScreen
import com.jagamata.app.ui.parent.PrivacyScreen
import com.jagamata.app.ui.parent.RewardsScreen
import com.jagamata.app.ui.parent.SettingsScreen
import com.jagamata.app.ui.parent.WeeklyReportScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Navigasi tab bottom nav: 0 Beranda · 1 Pantau · 2 Hadiah · 3 Orang Tua (via gerbang PIN, DEV-37). */
fun NavHostController.openTab(index: Int) {
    val route =
        when (index) {
            0 -> Routes.BERANDA
            1 -> Routes.PANTAU_LIVE
            2 -> Routes.HADIAH
            else -> Routes.DASHBOARD
        }
    // Tab 0/1/2 lolos apa adanya (rute non-sensitif); hanya DASHBOARD yang bisa
    // diarahkan ke KUNCI saat sesi masih terkunci.
    navigate(ParentSession.tryNavigate(route)) {
        popUpTo(Routes.BERANDA) { inclusive = false }
        launchSingleTop = true
    }
}

/**
 * Navigasi ter-gerbang (DEV-37): rute sensitif ([ParentGate.GATED_ROUTES]) hanya bisa
 * dibuka langsung saat [ParentSession.unlocked]; selain itu tujuan disimpan di [pending]
 * dan pengguna diarahkan ke [Routes.KUNCI] lebih dulu. Rute non-sensitif lolos apa adanya.
 */
private fun NavHostController.navigateGated(
    route: String,
    pending: MutableState<String?>,
) {
    val target = ParentSession.tryNavigate(route)
    // target != route = dialihkan ke KUNCI (route == KUNCI sendiri bukan pengalihan).
    if (target != route) {
        pending.value = route
    }
    navigate(target)
}

@Composable
fun AppNav(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH,
) {
    // Tujuan tertunda saat gerbang PIN menahan rute sensitif (DEV-37).
    val pending = ParentSession.pendingState
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SPLASH) {
            val context = LocalContext.current
            val hasCamera =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
            SplashScreen(
                onFinished = {
                    if (!hasCamera) {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.BERANDA) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingFlow(
                onDone = {
                    navController.navigate(Routes.IZIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onOpenSettings = { navController.navigateGated(Routes.SETTINGS, pending) },
                onOpenReport = { navController.navigate(Routes.REPORT) },
                onOpenChildProfile = { navController.navigate(Routes.PROFIL_ANAK) },
                onOpenDailyStats = { navController.navigate(Routes.STATISTIK) },
                onTab = { navController.openTab(it) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                onOpenCalibration = { navController.navigate(Routes.KALIBRASI) },
                onOpenChildProfile = { navController.navigate(Routes.PROFIL_ANAK) },
                onOpenPermissions = { navController.navigate(Routes.IZIN) },
            )
        }
        composable(Routes.REPORT) {
            WeeklyReportScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(
                onBack = { navController.popBackStack() },
                onDeleteData = { navController.navigateGated(Routes.DELETE_DATA, pending) },
            )
        }

        // ——— Layar desain FigCraft (17 frame) ———
        composable(Routes.BERANDA) {
            val context = LocalContext.current
            BerandaScreen(
                onStartPantau = {
                    val hasCamera =
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                    if (!hasCamera) {
                        navController.navigate(Routes.IZIN)
                    } else {
                        DetectionForegroundService.start(context)
                        navController.navigate(Routes.PANTAU_LIVE)
                    }
                },
                onTab = { navController.openTab(it) },
                onOpenDailyStats = { navController.navigate(Routes.STATISTIK) },
            )
        }
        composable(Routes.PANTAU_LIVE) {
            val context = LocalContext.current
            val pauseManager =
                remember {
                    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
                    PauseSessionManager(
                        scope = appScope,
                        recordPause = { log ->
                            appScope.launch {
                                AppDatabase.getInstance(context.applicationContext)
                                    .violationDao().insert(log)
                            }
                        },
                        notifyParent = {
                            NotificationHelper.throttledAlert(
                                context.applicationContext,
                                context.getString(R.string.pause_notify_title),
                                context.getString(R.string.pause_notify_body),
                                isCrucial = false,
                            )
                        },
                        stopMonitoring = { DetectionForegroundService.stop(context) },
                        startMonitoring = { DetectionForegroundService.start(context) },
                    )
                }
            PantauLiveScreen(
                onTab = { navController.openTab(it) },
                onPauseToggle = {
                    if (DetectionForegroundService.isRunning.value) {
                        pauseManager.pause()
                    } else {
                        val hasCamera =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                PackageManager.PERMISSION_GRANTED
                        if (!hasCamera) {
                            navController.navigate(Routes.IZIN)
                        } else {
                            pauseManager.resume()
                        }
                    }
                },
            )
        }
        composable(Routes.PERINGATAN) { AlarmBlocked() }
        composable(Routes.ISTIRAHAT) {
            BreakScreen(onDone = { navController.popBackStack() })
        }
        composable(Routes.STATISTIK) {
            DailyStatsScreen(
                onBack = { navController.popBackStack() },
                onTab = { navController.openTab(it) },
            )
        }
        composable(Routes.HADIAH) {
            RewardsScreen(onTab = { navController.openTab(it) })
        }
        composable(Routes.PROFIL_ANAK) {
            ChildProfileScreen(
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigateGated(Routes.SETTINGS, pending) },
            )
        }
        composable(Routes.IZIN) {
            PermissionWizardScreen(
                onNext = {
                    navController.navigate(Routes.KALIBRASI) {
                        popUpTo(Routes.IZIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.KALIBRASI) {
            val context = LocalContext.current
            val viewModel: com.jagamata.app.ui.viewModel.CalibrationViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                val hasCamera =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                if (hasCamera && !DetectionForegroundService.isRunning.value) {
                    DetectionForegroundService.start(context)
                }
                viewModel.startCalibration()
            }

            CalibrationScreen(
                faceHeightPx = state.faceHeightPx,
                distanceCm = state.distanceCm,
                progress = state.progress,
                isCalibrated = state.isCalibrated,
                onStart = { viewModel.startCalibration() },
                onRetry = { viewModel.retryCalibration() },
                onNext = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.BERANDA) {
                            popUpTo(Routes.KALIBRASI) { inclusive = true }
                        }
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.BERANDA) {
                            popUpTo(Routes.KALIBRASI) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Routes.KUNCI) {
            val context = LocalContext.current
            val settingsRepo = remember { SettingsRepository(context) }
            val coroutineScope = rememberCoroutineScope()
            LockScreen(
                verifyPin = { pinInput, onResult ->
                    coroutineScope.launch {
                        val ok = settingsRepo.verifyParentPin(pinInput)
                        onResult(ok)
                    }
                },
                onUnlocked = {
                    // DEV-37: buka sesi, lalu lanjutkan ke tujuan yang tertunda bila ada.
                    ParentSession.unlocked = true
                    val target = pending.value.also { pending.value = null }
                    if (target != null) {
                        navController.popBackStack()
                        navController.navigate(target)
                    } else if (!navController.popBackStack()) {
                        // KUNCI berada di root back stack (mis. start destination capture):
                        // tidak ada yang bisa di-pop — jatuhkan ke Beranda agar tidak macet.
                        navController.navigate(Routes.BERANDA) {
                            popUpTo(Routes.KUNCI) { inclusive = true }
                        }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.DELETE_DATA) {
            val context = LocalContext.current
            val settingsRepo = remember { SettingsRepository(context) }
            DeleteDataScreen(
                onDeleteAll = {
                    val db = AppDatabase.getInstance(context)
                    db.snapshotDao().deleteAll()
                    db.violationDao().deleteAll()
                    db.dailyDao().deleteAll()
                    db.dailyDao().deleteAppSettings()
                    settingsRepo.clearAll()
                },
                onDone = { navController.popBackStack() },
            )
        }
        composable(Routes.OVERLAY_DITOLAK) { OverlayDenied() }
        composable(Routes.DEBUG_GALLERY) {
            DebugGallery(onOpen = { route -> navController.navigateGated(route, pending) })
        }
    }
}
