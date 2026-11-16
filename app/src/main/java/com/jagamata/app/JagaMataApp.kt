package com.jagamata.app

import android.app.Application
import com.jagamata.app.service.NotificationHelper

class JagaMataApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
