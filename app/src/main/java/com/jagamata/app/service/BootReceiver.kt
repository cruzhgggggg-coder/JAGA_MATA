package com.jagamata.app.service

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jagamata.app.MainActivity
import com.jagamata.app.R
import com.jagamata.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * T5: setelah HP menyala ulang, lanjutkan pemantauan bila sebelumnya diinginkan
 * ([SettingsRepository.monitoringDesired]) — flag satu-satunya yang dipersist
 * karena proses mati saat reboot.
 *
 * Android 14+ (API 34) melarang FGS bertipe `camera` di-start dari
 * BOOT_COMPLETED; sebagai gantinya tampilkan notifikasi pengingat agar orang tua
 * membuka aplikasi dan memulai pemantauan lagi.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val desired =
                    runCatching {
                        SettingsRepository(context.applicationContext).monitoringDesired.first()
                    }.getOrDefault(false)
                if (!desired) return@launch
                val hasCamera =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                if (!hasCamera) {
                    notifyRestartReminder(context)
                    return@launch
                }
                if (Build.VERSION.SDK_INT >= 34) {
                    // FGS tipe kamera dibatasi dari BOOT_COMPLETED — langsung fallback.
                    Log.i(TAG, "Android 14+: start dari boot dibatasi, tampilkan pengingat")
                    notifyRestartReminder(context)
                    return@launch
                }
                try {
                    DetectionForegroundService.start(context)
                    Log.i(TAG, "Pemantauan dilanjutkan setelah boot")
                } catch (t: Throwable) {
                    Log.w(TAG, "start dari boot gagal: ${t.message}")
                    notifyRestartReminder(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyRestartReminder(context: Context) {
        runCatching {
            NotificationHelper.ensureChannel(context)
            val pending =
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val notification =
                NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(context.getString(R.string.boot_reminder_title))
                    .setContentText(context.getString(R.string.boot_reminder_body))
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()
            NotificationManagerCompat.from(context).notify(NOTIF_ID_REMINDER, notification)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
        private const val NOTIF_ID_REMINDER = 1004
    }
}
