package com.jagamata.app

import android.app.Application
import android.util.Log
import androidx.work.WorkManager
import com.jagamata.app.service.DailyRollupWorker
import com.jagamata.app.service.NotificationHelper
import com.jagamata.app.ui.viewModel.DetectionServiceLocator

class JagaMataApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        DetectionServiceLocator.init(this)
        // WorkManager belum ter-init di unit test (Robolectric) — guard agar jangan crash.
        try {
            if (WorkManager.isInitialized()) {
                DailyRollupWorker.schedule(this)
            }
        } catch (t: Throwable) {
            Log.w("JagaMataApp", "DailyRollupWorker tidak terjadwalkan: ${t.message}")
        }
    }
}
