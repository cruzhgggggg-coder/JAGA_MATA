package com.jagamata.app

import android.app.Application
import com.jagamata.app.service.NotificationHelper
import com.jagamata.app.ui.viewModel.DetectionServiceLocator

class JagaMataApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        DetectionServiceLocator.init(this)
    }
}
