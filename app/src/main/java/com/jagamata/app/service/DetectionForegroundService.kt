package com.jagamata.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
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

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        settings = SettingsRepository(this)
        aggregator = CycleAggregator()
        stateMachine = DetectionStateMachine()
        val pitchBackend =
            PitchBackendFactory.resolve(
                useTfliteFlag = BuildConfig.USE_TFLITE_PITCH,
                tfliteModelAvailable = ModelPitchBackend.modelExists(this),
                tflite = ModelPitchBackend(this),
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
        _isRunning.value = true
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        startForegroundWithNotification()
        detector.initialize()
        lightMonitor.start()
        startCamera()
        startTicker()
        return Service.START_STICKY
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
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                analysis.setAnalyzer(analysisExecutor) { proxy ->
                    val ts = System.currentTimeMillis()
                    // Optimasi ringan & hemat baterai (Blueprint §3.1 & §5.2): Batasi ke 5 FPS (200 ms)
                    if (ts - lastFrameTimeMs < FRAME_INTERVAL_MS) {
                        proxy.close()
                        return@setAnalyzer
                    }
                    lastFrameTimeMs = ts
                    detector.detect(proxy, ts)
                }
                provider.unbindAll()
                val selector =
                    if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                provider.bindToLifecycle(
                    this,
                    selector,
                    analysis,
                )
                Log.i(TAG, "Kamera berhasil diikat ke foreground service ($selector)")
            } catch (t: Throwable) {
                Log.e(TAG, "Kamera gagal diikat: ${t.message}")
            }
        }, ContextCompat.getMainExecutor(this))
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
            val title = getString(R.string.notif_title, summary.violationsCsv())
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
            NotificationHelper.throttledAlert(this, title, body, isCrucial = isCrucial)
        }
        _lastCycle.value = summary
    }

    override fun onDestroy() {
        ticker?.stop()
        detector.close()
        lightMonitor.stop()
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

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        @Volatile
        var sessionStartEpochMs: Long = 0L

        private val _riskLevel = MutableStateFlow(RiskLevel.OK)
        val riskLevel: StateFlow<RiskLevel> = _riskLevel

        private val _latest = MutableStateFlow<DetectionResult?>(null)
        val latest: StateFlow<DetectionResult?> = _latest

        private val _lastCycle = MutableStateFlow<com.jagamata.app.detection.CycleSummary?>(null)
        val lastCycle: StateFlow<com.jagamata.app.detection.CycleSummary?> = _lastCycle

        val sequence = AtomicLong(0)

        fun start(context: Context) {
            val intent = Intent(context, DetectionForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
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
