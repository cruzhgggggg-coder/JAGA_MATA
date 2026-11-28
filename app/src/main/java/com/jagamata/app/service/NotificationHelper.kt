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
 * Channel notifikasi:
 * - `jagamata_service`: Senyap, prioritas rendah untuk status background aktif
 * - `jagamata_alerts`: Peringatan kesehatan mata anak, throttled hanya beberapa menit sekali atau saat krusial
 */
object NotificationHelper {
    const val CHANNEL_ID = "jagamata_alerts"
    const val CHANNEL_ID_SERVICE = "jagamata_service"
    const val NOTIF_ID_ALERT = 1001
    const val NOTIF_ID_SERVICE = 1002

    private const val THROTTLE_WINDOW_CRUCIAL_MS = 60_000L // 1 menit untuk kondisi darurat (alarm bahaya)
    private const val THROTTLE_WINDOW_NORMAL_MS = 180_000L // 3 menit untuk peringatan rutin

    @Volatile
    private var lastCrucialAtMs: Long = 0L

    @Volatile
    private var lastNormalAtMs: Long = 0L

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        // 1. Channel silent untuk background service agar tidak membunyikan notifikasi setiap saat
        val serviceChannel =
            NotificationChannel(
                CHANNEL_ID_SERVICE,
                context.getString(R.string.detection_active),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Status pemantauan perlindungan mata aktif"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
        manager.createNotificationChannel(serviceChannel)

        // 2. Channel peringatan penting
        val alertChannel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notif_channel_desc)
            }
        manager.createNotificationChannel(alertChannel)
    }

    /** Notifikasi persisten untuk foreground service "JagaMata aktif" (senyap, hemat daya). */
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
        return NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.detection_active))
            .setContentText(context.getString(R.string.app_tagline))
            .setContentIntent(pending)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /** Peringatan adaptif: beberapa menit sekali untuk peringatan biasa, atau cepat pada saat krusial. */
    fun throttledAlert(
        context: Context,
        title: String,
        body: String,
        nowMs: Long = System.currentTimeMillis(),
        isCrucial: Boolean = false,
    ): Boolean {
        val window = if (isCrucial) THROTTLE_WINDOW_CRUCIAL_MS else THROTTLE_WINDOW_NORMAL_MS
        // Timestamp terpisah: alert biasa tidak boleh menelan alarm krusial (dan sebaliknya).
        if (isCrucial) {
            if (nowMs - lastCrucialAtMs < window) return false
            lastCrucialAtMs = nowMs
        } else {
            if (nowMs - lastNormalAtMs < window) return false
            lastNormalAtMs = nowMs
        }

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
                .setPriority(if (isCrucial) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
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
        lastCrucialAtMs = 0L
        lastNormalAtMs = 0L
    }
}
