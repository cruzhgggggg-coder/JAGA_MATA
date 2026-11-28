package com.jagamata.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.jagamata.app.BuildConfig
import com.jagamata.app.MainActivity
import com.jagamata.app.R
import com.jagamata.app.data.AppDatabase
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.data.entity.CheckSnapshot
import com.jagamata.app.data.entity.ViolationLog
import com.jagamata.app.detection.CycleAggregator
import com.jagamata.app.detection.DetectionResult
import com.jagamata.app.detection.DetectionStateMachine
import com.jagamata.app.detection.FaceDetectorManager
import com.jagamata.app.detection.MinuteTicker
import com.jagamata.app.detection.ModelPitchBackend
import com.jagamata.app.detection.PitchBackendFactory
import com.jagamata.app.detection.RiskLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Service latar depan yang menghost kamera + ticker + agregator
 * (PRD §5.2, DEV-15). `foregroundServiceType="camera"`, `START_STICKY`.
 */
class DetectionForegroundService : LifecycleService() {
    private lateinit var database: AppDatabase
    private lateinit var settings: SettingsRepository
    private lateinit var detector: FaceDetectorManager
    private lateinit var aggregator: CycleAggregator
    private lateinit var stateMachine: DetectionStateMachine
    private lateinit var lightMonitor: com.jagamata.app.detection.LightMonitor
    private var ticker: MinuteTicker? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var lastFrameTimeMs: Long = 0L

    /** Use case analisis yang sedang terikat (dipakai listener rotasi, T12). */
    private var imageAnalysis: ImageAnalysis? = null

    /** T12: memperbarui [ImageAnalysis.targetRotation] saat orientasi berubah. */
    private var orientationListener: OrientationEventListener? = null

    /** M18: gate analisis per frame — false saat layar mati/terkunci. */
    private val analysisEnabled = java.util.concurrent.atomic.AtomicBoolean(true)

    /** M18: receiver konteks (bukan manifest) untuk SCREEN_OFF/SCREEN_ON. */
    private val screenReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        analysisEnabled.set(false)
                        Log.i(TAG, "Layar mati — analisis kamera dijeda (M18)")
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        analysisEnabled.set(true)
                        Log.i(TAG, "Layar menyala — analisis kamera dilanjut (M18)")
                    }
                }
            }
        }

    /** M20: observer state kamera (LiveData di CameraX 1.3.4). */
    private var cameraStateObserver: Observer<CameraState>? = null
    private var observedCameraState: LiveData<CameraState>? = null

    /** M20: job retry bind kamera dengan backoff kecil. */
    private var rebindJob: Job? = null
    private var rebindAttempts = 0

    /** Referensi backend model pitch agar interpreter bisa di-close (M6). */
    private var modelPitchBackend: ModelPitchBackend? = null
    private val isAnalyzing = java.util.concurrent.atomic.AtomicBoolean(false)
    private var initialized = false

    /** Kunci jenis pelanggaran terakhir yang sudah di-alert (logika onset). */
    private var lastAlertedViolationKey: String? = null

    private var lastViolationAlertAtMs: Long = 0L

    override fun onCreate() {
        super.onCreate()
        if (!hasCameraPermission()) {
            // FGS bertipe kamera tanpa izin memicu crash-loop
            // (ForegroundServiceDidNotStartInTimeException) — berhenti sebelum init apa pun.
            notifyCameraPermissionNeeded()
            stopSelf()
            return
        }
        database = AppDatabase.getInstance(this)
        settings = SettingsRepository(this)
        aggregator = CycleAggregator()
        stateMachine = DetectionStateMachine()
        val modelPitch = ModelPitchBackend(this)
        modelPitchBackend = modelPitch
        val pitchBackend =
            PitchBackendFactory.resolve(
                useTfliteFlag = BuildConfig.USE_TFLITE_PITCH,
                tfliteModelAvailable = ModelPitchBackend.modelExists(this),
                tflite = modelPitch,
            )
        detector = FaceDetectorManager(this, pitchBackend) { result -> onDetection(result) }
        lightMonitor = com.jagamata.app.detection.LightMonitor(this)
        lifecycleScope.launch {
            settings.focalPx.collect { saved ->
                detector.focalPx = if (saved > 0f) saved else SettingsRepository.DEFAULT_FOCAL_PX
            }
        }
        lifecycleScope.launch {
            settings.closeThresholdCm.collect { threshold ->
                stateMachine.updateThresholds(
                    closeEnterCm = threshold,
                    closeExitCm = threshold + 5f,
                )
            }
        }
        lifecycleScope.launch {
            settings.luxThreshold.collect { lux ->
                stateMachine.updateThresholds(luxThreshold = lux)
            }
        }
        lifecycleScope.launch {
            settings.pitchThresholdDeg.collect { pitch ->
                stateMachine.updateThresholds(pitchThresholdDeg = pitch)
            }
        }
        lifecycleScope.launch {
            settings.childAge.collect { age ->
                detector.setChildAge(age)
            }
        }
        if (sessionStartEpochMs == 0L) {
            sessionStartEpochMs = System.currentTimeMillis()
        }
        registerScreenReceiver()
        registerOrientationListener()
        // M20: sesi baru dimulai dalam kondisi sehat sampai terbukti sebaliknya.
        _cameraHealthy.value = true
        _isRunning.value = true
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        if (!hasCameraPermission()) {
            notifyCameraPermissionNeeded()
            stopSelf()
            return Service.START_NOT_STICKY
        }
        if (!this::detector.isInitialized) {
            // onCreate berhenti dini — state belum dibuat, jangan lanjut.
            stopSelf()
            return Service.START_NOT_STICKY
        }
        startForegroundWithNotification()
        if (initialized) {
            // Idempoten: intent start() berulang tidak boleh me-re-init detector
            // (bocor landmarker) atau membuat MinuteTicker ganda.
            return Service.START_STICKY
        }
        initialized = true
        detector.initialize()
        lightMonitor.start()
        startCamera()
        startTicker()
        return Service.START_STICKY
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    private fun notifyCameraPermissionNeeded() {
        // Throttle: jangan post berulang kali (service start spam / restart loop).
        val now = System.currentTimeMillis()
        if (now - lastPermNotifAtMs < PERM_NOTIF_INTERVAL_MS) return
        lastPermNotifAtMs = now
        NotificationHelper.ensureChannel(this)
        val pending =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Butuh izin kamera")
                .setContentText("JagaMata butuh izin kamera untuk memantau kesehatan mata. Buka aplikasi untuk memberikan izin kembali.")
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        runCatching {
            NotificationManagerCompat.from(this).notify(NOTIF_ID_PERMISSION, notification)
        }
    }

    private fun startForegroundWithNotification() {
        val notification: Notification = NotificationHelper.serviceNotification(this)
        val hasCamera =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type =
                if (hasCamera) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                } else {
                    0
                }
            try {
                startForeground(
                    NotificationHelper.NOTIF_ID_SERVICE,
                    notification,
                    type,
                )
            } catch (t: Throwable) {
                Log.w(TAG, "startForeground camera gagal, fallback tanpa type: ${t.message}")
                try {
                    startForeground(NotificationHelper.NOTIF_ID_SERVICE, notification)
                } catch (t2: Throwable) {
                    Log.e(TAG, "startForeground gagal: ${t2.message}")
                }
            }
        } else {
            startForeground(NotificationHelper.NOTIF_ID_SERVICE, notification)
        }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider
                val analysis =
                    ImageAnalysis.Builder()
                        .setTargetResolution(android.util.Size(480, 640))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                // T12: rotasi target analisis mengikuti rotasi display saat ini,
                // bukan rotasi saat service dibuat (frame landscape jadi miring 90°).
                analysis.targetRotation = currentDisplayRotation()
                analysis.setAnalyzer(analysisExecutor) { proxy ->
                    // M18: layar mati/terkunci → frame dibuang sebelum pipeline.
                    if (!analysisEnabled.get()) {
                        proxy.close()
                        return@setAnalyzer
                    }
                    // M4: waktu monoton (elapsedRealtime) untuk timeline deteksi/throttle -
                    // System.currentTimeMillis bisa mundur dan membekukan debounce.
                    val ts = SystemClock.elapsedRealtime()
                    // Optimasi ringan & hemat baterai (Blueprint §3.1 & §5.2): Batasi ke 5 FPS (200 ms)
                    // dan buang frame lama jika proses sebelumnya belum tuntas (Cegah antrean/lag)
                    if (ts - lastFrameTimeMs < FRAME_INTERVAL_MS || !isAnalyzing.compareAndSet(false, true)) {
                        proxy.close()
                        return@setAnalyzer
                    }
                    lastFrameTimeMs = ts
                    try {
                        detector.detect(proxy, ts)
                    } finally {
                        isAnalyzing.set(false)
                    }
                }
                imageAnalysis = analysis
                provider.unbindAll()
                val selector =
                    if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                val camera =
                    provider.bindToLifecycle(
                        this,
                        selector,
                        analysis,
                    )
                observeCameraState(camera)
                Log.i(TAG, "Kamera berhasil diikat ke foreground service ($selector)")
            } catch (t: Throwable) {
                Log.e(TAG, "Kamera gagal diikat: ${t.message}")
                _cameraHealthy.value = false
                scheduleCameraRebind()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun currentDisplayRotation(): Int =
        @Suppress("DEPRECATION")
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation

    /**
     * T12: picu pembacaan rotasi display saat perangkat berotasi dan
     * perbarui `targetRotation` analisis. Ringan: hanya menulis saat berubah.
     */
    private fun registerOrientationListener() {
        val listener =
            object : OrientationEventListener(this) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return
                    val analysis = imageAnalysis ?: return
                    val rotation = currentDisplayRotation()
                    if (analysis.targetRotation != rotation) {
                        analysis.targetRotation = rotation
                        Log.i(TAG, "targetRotation analisis kamera -> $rotation")
                    }
                }
            }
        if (listener.canDetectOrientation()) {
            listener.enable()
            orientationListener = listener
        }
    }

    /** M18: daftarkan receiver SCREEN_OFF/SCREEN_ON secara konteks (tanpa manifest). */
    private fun registerScreenReceiver() {
        val filter =
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
        ContextCompat.registerReceiver(this, screenReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    /**
     * M20: pantau state kamera — saat error (kamera diambil app lain, konfigurasi
     * gagal, dll.) status diekspos lewat [cameraHealthy] dan bind dicoba ulang.
     */
    private fun observeCameraState(camera: Camera) {
        // Hapus observer lama dulu: rebind bisa menghasilkan instance Camera yang
        // LiveData-nya sudah membawa state lama (sticky) — tanpa ini observer menumpuk.
        cameraStateObserver?.let { obs -> observedCameraState?.removeObserver(obs) }
        val observer =
            Observer<CameraState> { state ->
                val error = state.error
                if (error != null) {
                    Log.e(TAG, "CameraState ${state.type} error code=${error.code}: ${error.cause?.message}")
                    _cameraHealthy.value = false
                    scheduleCameraRebind()
                } else if (state.type == CameraState.Type.OPEN) {
                    _cameraHealthy.value = true
                    rebindAttempts = 0
                }
            }
        cameraStateObserver = observer
        observedCameraState = camera.cameraInfo.cameraState
        camera.cameraInfo.cameraState.observe(this, observer)
    }

    /** M20: retry bind dengan backoff kecil (3x, jeda 2 detik). */
    private fun scheduleCameraRebind() {
        if (rebindJob?.isActive == true) return
        if (rebindAttempts >= CAMERA_REBIND_MAX_ATTEMPTS) {
            Log.e(TAG, "Rebind kamera menyerah setelah $rebindAttempts percobaan")
            return
        }
        rebindAttempts++
        val attempt = rebindAttempts
        rebindJob =
            lifecycleScope.launch {
                delay(CAMERA_REBIND_BACKOFF_MS)
                if (!initialized) return@launch
                Log.i(TAG, "Rebind kamera (percobaan $attempt/$CAMERA_REBIND_MAX_ATTEMPTS)")
                startCamera()
            }
    }

    private fun startTicker() {
        lifecycleScope.launch {
            val lightMode = settings.lightModeEnabled.first()
            val interval =
                if (lightMode) {
                    MinuteTicker.LIGHT_MODE_TICK_INTERVAL_MS
                } else {
                    MinuteTicker.TICK_INTERVAL_MS
                }
            val every = if (lightMode) MinuteTicker.LIGHT_MODE_TICKS_PER_CYCLE else 1
            ticker?.stop()
            val newTicker =
                MinuteTicker(
                    scope = lifecycleScope,
                    aggregator = aggregator,
                    tickIntervalMs = interval,
                    evaluateEveryTicks = every,
                ) { summary -> persistCycle(summary) }
            ticker = newTicker
            newTicker.start()
        }
    }

    private fun onDetection(result: DetectionResult) {
        val withLux = if (result.lux == null) result.copy(lux = lightMonitor.lastLux()) else result
        aggregator.add(withLux)
        val level = stateMachine.onResult(withLux)
        _riskLevel.value = level
        _latest.value = withLux
        OverlayService.requestLevel(this, level)
    }

    private suspend fun persistCycle(summary: com.jagamata.app.detection.CycleSummary) {
        try {
            // M18: siklus tanpa satu pun wajah (termasuk siklus kosong saat layar
            // mati / kamera dijeda) tidak disimpan — "menit kosong" bukan siklus
            // (juga menutup celah farming lencana via kamera tertutup, M24).
            if (shouldSkipCycle(summary.sampleCount, summary.noFaceCount)) {
                Log.i(TAG, "Siklus dilewati (tanpa wajah terdeteksi): ${summary.sampleCount} sampel")
                return
            }
            val dayEpoch = summary.startedAtMs / DAY_MS
            database.snapshotDao().insert(
                CheckSnapshot(
                    timestampMs = summary.startedAtMs,
                    cycleDurationMs = summary.cycleDurationMs,
                    sampleCount = summary.sampleCount,
                    noFaceCount = summary.noFaceCount,
                    minDistanceCm = summary.minDistanceCm,
                    avgDistanceCm = summary.avgDistanceCm,
                    maxDistanceCm = summary.maxDistanceCm,
                    avgLux = summary.avgLux,
                    maxPitchDeg = summary.maxPitchDeg,
                    violations = summary.violationsCsv(),
                    dayEpoch = dayEpoch,
                ),
            )
            if (summary.hasViolation) {
                summary.violations.forEach { type ->
                    database.violationDao().insert(
                        ViolationLog(
                            timestampMs = summary.startedAtMs,
                            type = type.name,
                            distanceCm = summary.avgDistanceCm,
                            lux = summary.avgLux,
                            pitchDeg = summary.maxPitchDeg,
                        ),
                    )
                }
                // Alert hanya saat ONSET (jenis pelanggaran berubah) atau re-alert
                // berkala — BUKAN tiap siklus. Ini menghentikan "notif looping"
                // saat kondisi salah (mis. jarak meleset) berlangsung terus.
                val key = summary.violationsCsv()
                val now = System.currentTimeMillis()
                val onset = key != lastAlertedViolationKey
                val reAlertDue = now - lastViolationAlertAtMs >= ONGOING_VIOLATION_REALERT_MS
                if (onset || reAlertDue) maybeNotifyViolation(summary, key, now)
            } else {
                lastAlertedViolationKey = null
            }
            _lastCycle.value = summary
        } catch (t: Throwable) {
            // Satu kegagalan DB (mis. SQLiteFullException) tidak boleh membunuh ticker siklus.
            Log.e(TAG, "persistCycle gagal: ${t.message}", t)
        }
    }

    /** Kirim alert pelanggaran (dipanggil hanya saat onset / re-alert berkala). */
    private fun maybeNotifyViolation(
        summary: com.jagamata.app.detection.CycleSummary,
        key: String,
        now: Long,
    ) {
        val title = getString(R.string.notif_title, key)
        val body =
            when (summary.violations.first()) {
                com.jagamata.app.detection.ViolationType.TOO_CLOSE ->
                    getString(R.string.notif_body, summary.minDistanceCm ?: 0f)
                com.jagamata.app.detection.ViolationType.TOO_DARK ->
                    getString(R.string.notif_body_dark, summary.avgLux ?: 0f)
                com.jagamata.app.detection.ViolationType.BAD_POSTURE ->
                    getString(R.string.notif_body_posture, summary.maxPitchDeg ?: 0f)
            }
        val isCrucial =
            summary.violations.contains(com.jagamata.app.detection.ViolationType.TOO_CLOSE) &&
                (summary.minDistanceCm ?: 30f) < 25f
        if (NotificationHelper.throttledAlert(this, title, body, isCrucial = isCrucial)) {
            lastAlertedViolationKey = key
            lastViolationAlertAtMs = now
        }
    }

    override fun onDestroy() {
        ticker?.stop()
        if (this::detector.isInitialized) detector.close()
        // M6: tutup interpreter TFLite pitch (idempoten) agar tidak bocor per sesi.
        modelPitchBackend?.close()
        modelPitchBackend = null
        if (this::lightMonitor.isInitialized) lightMonitor.stop()
        // T12/M18/M20: lepas listener rotasi, receiver layar, dan observer kamera.
        orientationListener?.disable()
        orientationListener = null
        runCatching { unregisterReceiver(screenReceiver) }
        rebindJob?.cancel()
        rebindJob = null
        cameraStateObserver?.let { obs -> observedCameraState?.removeObserver(obs) }
        cameraStateObserver = null
        observedCameraState = null
        imageAnalysis = null
        cameraProvider?.unbindAll()
        analysisExecutor.shutdown()
        _isRunning.value = false
        sessionStartEpochMs = 0L
        OverlayService.requestLevel(this, RiskLevel.OK)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    companion object {
        private const val TAG = "DetectionFgService"
        private const val DAY_MS = 86_400_000L
        private const val FRAME_INTERVAL_MS = 200L // 5 FPS (Blueprint §3.1 & §5.2)
        private const val NOTIF_ID_PERMISSION = 1003

        /** Re-alert pelanggaran berkelanjutan maksimal tiap 5 menit (bukan tiap siklus). */
        private const val ONGOING_VIOLATION_REALERT_MS = 300_000L

        /** Notifikasi "butuh izin kamera" maksimal tiap 5 menit. */
        private const val PERM_NOTIF_INTERVAL_MS = 300_000L

        /** M20: jumlah percobaan rebind kamera setelah error (mis. diambil app lain). */
        private const val CAMERA_REBIND_MAX_ATTEMPTS = 3

        /** M20: jeda antar percobaan rebind kamera. */
        private const val CAMERA_REBIND_BACKOFF_MS = 2_000L

        /**
         * M18: siklus yang tidak memuat satu pun wajah — termasuk siklus kosong
         * (0 sampel, mis. saat layar mati) — tidak boleh disimpan sebagai baris
         * snapshot. `noFaceCount >= sampleCount` mencakup kasus 0 == 0.
         */
        fun shouldSkipCycle(
            sampleCount: Int,
            noFaceCount: Int,
        ): Boolean = sampleCount <= 0 || noFaceCount >= sampleCount

        @Volatile
        private var lastPermNotifAtMs: Long = 0L

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        // ——— T5: status keinginan memantau ———

        /**
         * T5: keinginan memantau — true saat pemantauan harus berjalan (termasuk
         * saat jeda singkat yang akan auto-resume, DEV-36). Di-mirror ke DataStore
         * ([SettingsRepository.monitoringDesired]) supaya BOOT_COMPLETED bisa
         * melanjutkan setelah proses mati.
         */
        @Volatile
        var monitoringDesired: Boolean = false
            private set

        private val _pausedSinceEpochMs = MutableStateFlow(0L)

        /** T5: waktu jeda dimulai (epoch ms); 0 = tidak sedang dijeda. Dipakai Dashboard. */
        val pausedSinceEpochMs: StateFlow<Long> = _pausedSinceEpochMs

        private val persistScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private fun persistMonitoringDesired(
            context: Context,
            desired: Boolean,
        ) {
            val app = context.applicationContext
            persistScope.launch {
                runCatching { SettingsRepository(app).setMonitoringDesired(desired) }
            }
        }

        @Volatile
        var sessionStartEpochMs: Long = 0L

        private val _riskLevel = MutableStateFlow(RiskLevel.OK)
        val riskLevel: StateFlow<RiskLevel> = _riskLevel

        private val _latest = MutableStateFlow<DetectionResult?>(null)
        val latest: StateFlow<DetectionResult?> = _latest

        private val _lastCycle = MutableStateFlow<com.jagamata.app.detection.CycleSummary?>(null)
        val lastCycle: StateFlow<com.jagamata.app.detection.CycleSummary?> = _lastCycle

        private val _cameraHealthy = MutableStateFlow(true)

        /**
         * M20: kesehatan kamera untuk status UI. `false` = kamera error / diambil
         * aplikasi lain sehingga deteksi berhenti diam-diam. Sengaja diekspos
         * tanpa konsumen UI dulu (gelombang status Dashboard).
         */
        val cameraHealthy: StateFlow<Boolean> = _cameraHealthy

        val sequence = AtomicLong(0)

        fun start(context: Context) {
            monitoringDesired = true
            _pausedSinceEpochMs.value = 0L
            persistMonitoringDesired(context, true)
            val intent = Intent(context, DetectionForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            monitoringDesired = false
            _pausedSinceEpochMs.value = 0L
            persistMonitoringDesired(context, false)
            context.stopService(Intent(context, DetectionForegroundService::class.java))
        }

        /**
         * T5: jeda singkat — service dihentikan sementara tetapi keinginan memantau
         * tetap true (auto-resume DEV-36 / restart via BootReceiver).
         */
        fun pauseMonitoring(context: Context) {
            _pausedSinceEpochMs.value = System.currentTimeMillis()
            context.stopService(Intent(context, DetectionForegroundService::class.java))
        }

        fun pendingIntent(context: Context): PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
    }
}
