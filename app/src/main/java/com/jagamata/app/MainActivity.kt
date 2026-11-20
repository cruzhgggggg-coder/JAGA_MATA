package com.jagamata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jagamata.app.ui.navigation.AppNav
import com.jagamata.app.ui.navigation.Routes
import com.jagamata.app.ui.theme.JagaMataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Capture desain: adb shell am start ... --es screen "<slug>"
        val startDestination =
            if (BuildConfig.DEBUG) {
                Routes.routeForSlug(intent.getStringExtra(EXTRA_SCREEN)) ?: Routes.SPLASH
            } else {
                Routes.SPLASH
            }
        setContent {
            JagaMataTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNav(startDestination = startDestination)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
    }
}
