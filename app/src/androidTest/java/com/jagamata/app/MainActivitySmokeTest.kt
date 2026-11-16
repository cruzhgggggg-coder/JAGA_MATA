package com.jagamata.app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test (PRD DEV-18, DoD BAB 9): MainActivity launch tanpa crash.
 */
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {
    @Test
    fun mainActivityLaunchTanpaCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
                assertNotNull(activity.window)
            }
        }
    }
}
