package com.jagamata.app.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.jagamata.app.R
import com.jagamata.app.detection.RiskLevel
import java.util.concurrent.atomic.AtomicReference

/**
 * Overlay peringatan (PRD §2.12, US-05, DEV-16).
 *
 * - BLOCKED: ComposeView/View full-screen di atas aplikasi lain
 *   (TYPE_APPLICATION_OVERLAY) + haptic 200 ms.
 * - WARNING: banner atas 48dp.
 * - AMAN  : banner tipis opsional.
 * Hilang otomatis setelah state AMAN 2 detik. Tanpa izin overlay ->
 * fallback notifikasi (layar recovery [com.jagamata.app.ui.common.OverlayDenied]).
 */
class OverlayService : android.app.Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentLevel: RiskLevel = RiskLevel.OK

    override fun onBind(intent: Intent?): android.os.IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val level =
            runCatching {
                RiskLevel.valueOf(intent?.getStringExtra(EXTRA_LEVEL) ?: RiskLevel.OK.name)
            }.getOrDefault(RiskLevel.OK)
        applyLevel(level)
        return START_NOT_STICKY
    }

    private fun applyLevel(level: RiskLevel) {
        currentLevel = level
        if (level == RiskLevel.OK) {
            removeOverlay()
            return
        }
        if (!canDrawOverlays()) {
            Log.w(TAG, "Izin overlay tidak ada — fallback notifikasi")
            NotificationHelper.throttledAlert(
                this,
                getString(R.string.notif_title, level.name),
                getString(R.string.overlay_denied_fallback),
            )
            return
        }
        showOverlay(level)
    }

    private fun canDrawOverlays(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    private fun showOverlay(level: RiskLevel) {
        removeOverlay()
        val inflater = android.view.LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_alert, null, false)
        val title = view.findViewById<TextView>(R.id.overlay_title)
        val subtitle = view.findViewById<TextView>(R.id.overlay_subtitle)
        when (level) {
            RiskLevel.BLOCKED -> {
                title.text = getString(R.string.alarm_title)
                subtitle.text = getString(R.string.alarm_subtitle)
                haptic(200)
            }
            RiskLevel.WARNING -> {
                title.text = getString(R.string.banner_warning, getString(R.string.status_warning))
                subtitle.text = getString(R.string.alarm_subtitle)
            }
            RiskLevel.OK -> Unit
        }
        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
        val params =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT,
            ).apply { gravity = Gravity.TOP }
        runCatching {
            windowManager?.addView(view, params)
            overlayView = view
        }.onFailure { Log.e(TAG, "Gagal menampilkan overlay: ${it.message}") }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        runCatching { windowManager?.removeView(view) }
        overlayView = null
    }

    private fun haptic(durationMs: Long) {
        runCatching {
            val vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(VIBRATOR_SERVICE) as? Vibrator
                }
            vibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE),
            )
        }
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OverlayService"
        private const val EXTRA_LEVEL = "level"

        private val lastLevel = AtomicReference(RiskLevel.OK)

        /** Dipanggil DetectionForegroundService setiap perubahan state. */
        fun requestLevel(
            context: Context,
            level: RiskLevel,
        ) {
            lastLevel.set(level)
            val intent =
                Intent(context, OverlayService::class.java).apply {
                    putExtra(EXTRA_LEVEL, level.name)
                }
            runCatching { context.startService(intent) }
                .onFailure { Log.w(TAG, "OverlayService tidak bisa dijalankan: ${it.message}") }
        }

        fun currentLevel(): RiskLevel = lastLevel.get()
    }
}
