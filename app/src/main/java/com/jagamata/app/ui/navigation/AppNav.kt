package com.jagamata.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jagamata.app.ui.onboarding.OnboardingFlow
import com.jagamata.app.ui.onboarding.SplashScreen
import com.jagamata.app.ui.parent.DashboardScreen
import com.jagamata.app.ui.parent.PrivacyScreen
import com.jagamata.app.ui.parent.SettingsScreen
import com.jagamata.app.ui.parent.WeeklyReportScreen

@Composable
fun AppNav(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
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
    }
}
