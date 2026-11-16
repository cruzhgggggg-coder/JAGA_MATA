package com.jagamata.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jagamata.app.MainActivity
import com.jagamata.app.R

/**
 * Channel notifikasi `jagamata_alerts` (PRD US-13) + throttle maksimal
 * 1 notifikasi per menit.
 */
object NotificationHelper {
    const val CHANNEL_ID = "jagamata_alerts"
    const val NOTIF_ID_ALERT = 1001
    const val NOTIF_ID_SERVICE = 1002

    private const val THROTTLE_WINDOW_MS = 60_000L

    @Volatile
    private var lastAlertAtMs: Long = 0L

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notif_channel_desc)
            }
        manager.createNotificationChannel(channel)
    }

    /** Notifikasi persisten untuk foreground service "JagaMata aktif". */
    fun serviceNotification(context: Context): Notification {
        ensureChannel(context)
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val pending =
            PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.detection_active))
            .setContentText(context.getString(R.string.app_tagline))
            .setContentIntent(pending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /** Maksimal 1 peringatan per menit (PRD US-13). */
    fun throttledAlert(
        context: Context,
        title: String,
        body: String,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean {
        if (nowMs - lastAlertAtMs < THROTTLE_WINDOW_MS) return false
        lastAlertAtMs = nowMs

        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val pending =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        return try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_ALERT, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    /** Dipakai test — reset throttle. */
    fun resetThrottleForTest() {
        lastAlertAtMs = 0L
    }
}
