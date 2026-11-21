package com.jagamata.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jagamata.app.service.DetectionForegroundService
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

/** Navigasi tab bottom nav: 0 Beranda · 1 Pantau · 2 Hadiah · 3 Orang Tua (via Kunci PIN). */
fun NavHostController.openTab(index: Int) {
    val route =
        when (index) {
            0 -> Routes.BERANDA
            1 -> Routes.PANTAU_LIVE
            2 -> Routes.HADIAH
            else -> Routes.KUNCI
        }
    navigate(route) {
        popUpTo(Routes.BERANDA) { inclusive = false }
        launchSingleTop = true
    }
}

@Composable
fun AppNav(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH,
) {
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
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
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
            PrivacyScreen(onBack = { navController.popBackStack() })
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
            PantauLiveScreen(
                onTab = { navController.openTab(it) },
                onPauseToggle = {
                    if (DetectionForegroundService.isRunning.value) {
                        DetectionForegroundService.stop(context)
                    } else {
                        val hasCamera =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                PackageManager.PERMISSION_GRANTED
                        if (!hasCamera) {
                            navController.navigate(Routes.IZIN)
                        } else {
                            DetectionForegroundService.start(context)
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
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
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
            LockScreen(
                verifyPin = { pinInput, onResult ->
                    // PIN default 1234 atau valid 4 digit
                    onResult(pinInput.length == 4)
                },
                onUnlocked = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.BERANDA) { inclusive = false }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.DELETE_DATA) {
            DeleteDataScreen(
                onDeleteAll = {},
                onDone = { navController.popBackStack() },
            )
        }
        composable(Routes.OVERLAY_DITOLAK) { OverlayDenied() }
        composable(Routes.DEBUG_GALLERY) {
            DebugGallery(onOpen = { route -> navController.navigate(route) })
        }
    }
}
