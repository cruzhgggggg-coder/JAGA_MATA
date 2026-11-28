package com.jagamata.app.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.jagamata.app.R
import com.jagamata.app.detection.RiskLevel
import java.util.concurrent.atomic.AtomicReference

/**
 * Overlay peringatan (PRD §2.12, US-05, DEV-16).
 *
 * - BLOCKED: Layar penuh peringatan + haptic 200 ms + tombol "Tutup" + auto-dismiss 5 detik.
 * - WARNING: Banner atas 56dp (FLAG_NOT_TOUCHABLE) sehingga tidak mengganggu sentuhan pengguna.
 * - OK: Overlay otomatis ditutup.
 */
class OverlayService : android.app.Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentLevel: RiskLevel = RiskLevel.OK
    private val mainHandler = Handler(Looper.getMainLooper())
    private var autoDismissRunnable: Runnable? = null

    override fun onBind(intent: Intent?): android.os.IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
        KikoVoiceHelper.initialize(this)
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
        if (level == RiskLevel.OK) {
            val wasBlocked = currentLevel == RiskLevel.BLOCKED
            currentLevel = RiskLevel.OK
            removeOverlay()
            if (wasBlocked) {
                KikoVoiceHelper.speakSafe(this)
            }
            return
        }

        // Jangan re-inflate jika level yang ditampilkan sama persis
        if (currentLevel == level && overlayView != null) {
            return
        }

        val now = System.currentTimeMillis()
        // Cooldown hanya berlaku setelah dismiss MANUAL (tombol tutup). Auto-dismiss tidak
        // boleh mengarminginya, dan level BLOCKED tidak pernah ditekan — keselamatan anak.
        if (level != RiskLevel.BLOCKED && now < userDismissedUntilMs) {
            // Sedang dalam masa jeda setelah pengguna menekan tutup (cooldown)
            return
        }

        if (!canDrawOverlays()) {
            Log.w(TAG, "Izin overlay tidak ada — fallback notifikasi")
            val isCrucial = level == RiskLevel.BLOCKED
            val (title, body) =
                when (level) {
                    RiskLevel.BLOCKED ->
                        "JagaMata: Jarak Terlalu Dekat! ✋" to "Jarak matamu terlalu dekat dengan layar. Ayo mundur sedikit ya!"
                    RiskLevel.WARNING ->
                        "JagaMata: Peringatan Kesehatan Mata 💡" to "Perhatikan jarak aman layar, posisi kepala, atau nyalakan lampu ruangan ya!"
                    RiskLevel.OK -> return
                }
            NotificationHelper.throttledAlert(
                this,
                title,
                body,
                isCrucial = isCrucial,
            )
            return
        }

        currentLevel = level
        showOverlay(level)
    }

    private fun canDrawOverlays(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    private fun showOverlay(level: RiskLevel) {
        removeOverlay()

        val wm = windowManager ?: return
        val inflater = android.view.LayoutInflater.from(this)
        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

        val density = resources.displayMetrics.density

        val (view, params) =
            when (level) {
                RiskLevel.WARNING -> {
                    val v = inflater.inflate(R.layout.overlay_warning, null, false)
                    val p =
                        WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            (56 * density).toInt(),
                            type,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                            PixelFormat.TRANSLUCENT,
                        ).apply { gravity = Gravity.TOP }
                    v to p
                }
                RiskLevel.BLOCKED -> {
                    val v = inflater.inflate(R.layout.overlay_alert, null, false)
                    val title = v.findViewById<TextView>(R.id.overlay_title)
                    val subtitle = v.findViewById<TextView>(R.id.overlay_subtitle)
                    val btnDismiss = v.findViewById<Button>(R.id.btn_dismiss_overlay)

                    title?.text = getString(R.string.alarm_title)
                    subtitle?.text = getString(R.string.alarm_subtitle)

                    // Tombol tutup eksplisit agar HP TIDAK PERNAH terkunci
                    val dismissAction =
                        View.OnClickListener {
                            userDismissedUntilMs = System.currentTimeMillis() + DISMISS_COOLDOWN_MS
                            onManualDismiss()
                            removeOverlay()
                        }
                    btnDismiss?.setOnClickListener(dismissAction)
                    // Tap di area kosong TIDAK menutup overlay (audit K1) — hanya tombol.

                    haptic(200)
                    KikoVoiceHelper.speakWarningClose(this)

                    val p =
                        WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT,
                            type,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                            PixelFormat.TRANSLUCENT,
                        ).apply { gravity = Gravity.CENTER }
                    v to p
                }
                RiskLevel.OK -> return
            }

        runCatching {
            wm.addView(view, params)
            overlayView = view
            onOverlayVisibilityChanged(true)

            // Auto-dismiss safety timer: maksimal 5 detik agar tidak pernah menutupi HP secara permanen
            autoDismissRunnable?.let { mainHandler.removeCallbacks(it) }
            val runnable =
                Runnable {
                    // Auto-dismiss keselamatan TIDAK memicu cooldown — hanya dismiss
                    // manual yang boleh menahan level berikutnya.
                    removeOverlay()
                }
            autoDismissRunnable = runnable
            mainHandler.postDelayed(runnable, AUTO_DISMISS_TIMEOUT_MS)
        }.onFailure { Log.e(TAG, "Gagal menampilkan overlay: ${it.message}") }
    }

    private fun removeOverlay() {
        autoDismissRunnable?.let { mainHandler.removeCallbacks(it) }
        autoDismissRunnable = null
        val view = overlayView ?: return
        runCatching { windowManager?.removeView(view) }
        overlayView = null
        onOverlayVisibilityChanged(false)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        }
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OverlayService"
        private const val EXTRA_LEVEL = "level"
        private const val AUTO_DISMISS_TIMEOUT_MS = 5_000L
        private const val DISMISS_COOLDOWN_MS = 8_000L

        /** Interval kirim ulang intent saat overlay sudah tidak tampil tapi level masih sama. */
        private const val LEVEL_RESHOW_MS = 4_000L

        /** Jeda singkat setelah tombol tutup ditekan sebelum overlay boleh tampil lagi. */
        private const val DISMISS_QUIET_MS = 3_000L

        @Volatile
        private var userDismissedUntilMs = 0L

        @Volatile
        private var overlayShowing = false

        @Volatile
        private var lastLevelSendAtMs = 0L

        @Volatile
        private var dismissQuietUntilMs = 0L

        private val lastLevel = AtomicReference(RiskLevel.OK)

        /** Dipanggil DetectionForegroundService setiap perubahan state. */
        fun requestLevel(
            context: Context,
            level: RiskLevel,
        ) {
            val prev = lastLevel.getAndSet(level)
            val now = android.os.SystemClock.elapsedRealtime()
            if (!shouldResendIntent(prev, level, overlayShowing, now, lastLevelSendAtMs, dismissQuietUntilMs)) {
                return
            }
            lastLevelSendAtMs = android.os.SystemClock.elapsedRealtime()

            val intent =
                Intent(context, OverlayService::class.java).apply {
                    putExtra(EXTRA_LEVEL, level.name)
                }
            runCatching { context.startService(intent) }
                .onFailure { Log.w(TAG, "OverlayService tidak bisa dijalankan: ${it.message}") }
        }

        /**
         * Keputusan pengiriman intent (murni, untuk unit test — regresi K1):
         * level berubah SELALU dikirim; level sama hanya dikirim ulang untuk
         * WARNING/BLOCKED saat overlay tidak tampil + lewat interval re-show + lewat
         * jeda setelah dismiss manual.
         */
        internal fun shouldResendIntent(
            prev: RiskLevel,
            level: RiskLevel,
            overlayShowing: Boolean,
            nowMs: Long,
            lastSendAtMs: Long,
            quietUntilMs: Long,
        ): Boolean {
            if (prev != level) return true
            val reshowable = level == RiskLevel.BLOCKED || level == RiskLevel.WARNING
            return reshowable &&
                !overlayShowing &&
                nowMs - lastSendAtMs >= LEVEL_RESHOW_MS &&
                nowMs >= quietUntilMs
        }

        fun currentLevel(): RiskLevel = lastLevel.get()

        internal fun onOverlayVisibilityChanged(showing: Boolean) {
            overlayShowing = showing
        }

        internal fun onManualDismiss() {
            dismissQuietUntilMs = android.os.SystemClock.elapsedRealtime() + DISMISS_QUIET_MS
        }
    }
}
