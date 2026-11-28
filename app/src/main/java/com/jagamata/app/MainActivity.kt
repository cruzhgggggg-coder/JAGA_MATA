package com.jagamata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jagamata.app.ui.navigation.AppNav
import com.jagamata.app.ui.navigation.ParentSession
import com.jagamata.app.ui.navigation.Routes
import com.jagamata.app.ui.theme.JagaMataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Capture desain: adb shell am start ... --es screen "<slug>"
        val screenSlug = intent.getStringExtra(EXTRA_SCREEN)
        // Hanya debug: bypass gerbang PIN agar pipeline screenshot (--es screen) tetap
        // bisa menangkap layar sensitif tanpa PIN (DEV-37, deviasi yang didokumentasikan).
        if (BuildConfig.DEBUG && screenSlug != null) {
            ParentSession.unlocked = true
        }
        val startDestination =
            if (BuildConfig.DEBUG) {
                Routes.routeForSlug(screenSlug) ?: Routes.SPLASH
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

    override fun onStart() {
        super.onStart()
        // DEV-37: auto-relock bila sesi lebih dari 5 menit di latar-belakang.
        ParentSession.onAppForeground()
    }

    override fun onStop() {
        super.onStop()
        ParentSession.onAppBackground()
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
    }
}
