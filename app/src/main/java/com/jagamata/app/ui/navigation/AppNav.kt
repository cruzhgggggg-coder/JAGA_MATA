package com.jagamata.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jagamata.app.ui.child.AlarmBlocked
import com.jagamata.app.ui.child.BerandaScreen
import com.jagamata.app.ui.child.BreakScreen
import com.jagamata.app.ui.common.OverlayDenied
import com.jagamata.app.ui.debug.DebugGallery
import com.jagamata.app.ui.debug.DesignPlaceholder
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

/** Navigasi tab bottom nav: 0 Beranda · 1 Pantau · 2 Hadiah · 3 Orang Tua. */
fun NavHostController.openTab(index: Int) {
    val route =
        when (index) {
            0 -> Routes.BERANDA
            1 -> Routes.PANTAU_LIVE
            2 -> Routes.HADIAH
            else -> Routes.DASHBOARD
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
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingFlow(
                onDone = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenReport = { navController.navigate(Routes.REPORT) },
                onTab = { navController.openTab(it) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
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
            BerandaScreen(
                onStartPantau = { navController.navigate(Routes.PANTAU_LIVE) },
                onTab = { navController.openTab(it) },
            )
        }
        composable(Routes.PANTAU_LIVE) {
            PantauLiveScreen(onTab = { navController.openTab(it) })
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
            PermissionWizardScreen(onNext = { navController.popBackStack() })
        }
        composable(Routes.KALIBRASI) {
            CalibrationScreen(
                faceHeightPx = null,
                progress = 0f,
                isCalibrated = false,
                onStart = {},
                onRetry = {},
                onNext = { navController.popBackStack() },
            )
        }
        composable(Routes.KUNCI) {
            LockScreen(
                verifyPin = { _, onResult -> onResult(true) },
                onUnlocked = { navController.popBackStack() },
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
