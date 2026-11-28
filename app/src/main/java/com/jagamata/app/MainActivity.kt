package com.jagamata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.ui.navigation.AppNav
import com.jagamata.app.ui.navigation.ParentSession
import com.jagamata.app.ui.navigation.Routes
import com.jagamata.app.ui.theme.JagaMataTheme
import kotlinx.coroutines.launch

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
        // T6: cermin status "pernah terkalibrasi" dari DataStore ke gerbang rute —
        // kalibrasi ulang (focal tersimpan) wajib PIN, kalibrasi pertama tidak.
        lifecycleScope.launch {
            SettingsRepository(applicationContext).focalPx.collect { focal ->
                ParentSession.hasSavedFocal = focal > 0f
            }
        }
        // T4: setelah process death, back stack yang di-restore `rememberNavController`
        // tidak boleh langsung memperlihatkan layar sensitif tanpa PIN — buang seluruh
        // back stack yang di-restore dan mulai dari BERANDA.
        val restoreWithoutSession = savedInstanceState != null && !ParentSession.unlocked
        setContent {
            JagaMataTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNav(
                        startDestination = startDestination,
                        resetBackStackToBeranda = restoreWithoutSession,
                    )
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
