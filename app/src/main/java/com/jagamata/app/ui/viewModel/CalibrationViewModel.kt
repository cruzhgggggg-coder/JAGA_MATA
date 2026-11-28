package com.jagamata.app.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.detection.CalibrationManager
import com.jagamata.app.detection.DetectionResult
import com.jagamata.app.service.DetectionForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Batas wajar hasil kalibrasi (T6c) — murni Kotlin, teruji di JVM unit test.
 * Menolak focal di luar [MIN_FOCAL_PX]..[MAX_FOCAL_PX] atau yang menyimpang lebih
 * dari [MAX_DEVIATION_RATIO] dari focal tersimpan sebelumnya (bila ada).
 */
object CalibrationBounds {
    const val MIN_FOCAL_PX = 350f
    const val MAX_FOCAL_PX = 900f
    const val MAX_DEVIATION_RATIO = 0.30f

    enum class Reject {
        /** Focal di luar rentang wajar 350–900 px. */
        OUT_OF_RANGE,

        /** Focal menyimpang > 30% dari hasil kalibrasi sebelumnya. */
        DEVIATION,
    }

    /** Alasan penolakan, atau null bila [focalPx] wajar dan boleh disimpan. */
    fun rejectReason(
        focalPx: Float,
        previousFocalPx: Float?,
    ): Reject? {
        if (focalPx < MIN_FOCAL_PX || focalPx > MAX_FOCAL_PX) return Reject.OUT_OF_RANGE
        val prev = previousFocalPx?.takeIf { it > 0f }
        if (prev != null && kotlin.math.abs(focalPx - prev) / prev > MAX_DEVIATION_RATIO) {
            return Reject.DEVIATION
        }
        return null
    }

    fun isPlausible(
        focalPx: Float,
        previousFocalPx: Float?,
    ): Boolean = rejectReason(focalPx, previousFocalPx) == null
}

/**
 * ViewModel untuk alur kalibrasi jarak layar (PRD §2.8, US-02).
 * Menghubungkan frame kamera aktif dari DetectionForegroundService ke CalibrationManager,
 * menghitung stabilitas 1,5 detik pada jarak target 30 cm, dan menyimpan f_px ke SettingsRepository.
 */
class CalibrationViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = SettingsRepository(application)
    private val calibrationManager = CalibrationManager()

    data class UiState(
        val faceHeightPx: Float? = null,
        val distanceCm: Float? = null,
        val progress: Float = 0f,
        val isCalibrating: Boolean = false,
        val isCalibrated: Boolean = false,
        val calibratedFocalPx: Float? = null,
        /** T6c: alasan penolakan hasil kalibrasi; null bila tidak ada/tidak ditolak. */
        val focalReject: CalibrationBounds.Reject? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var sampleStartTimeMs: Long = 0L

    /** True bila anchor waktu progress sudah diambil dari sampel pertama. */
    private var progressAnchored: Boolean = false

    /** Tinggi wajah acuan sesuai usia — harus sama dengan yang dipakai runtime. */
    private var childFaceHeightCm: Float = com.jagamata.app.detection.DistanceEstimator.DEFAULT_FACE_HEIGHT_CM

    /** Focal tersimpan sebelum kalibrasi ini (null bila belum pernah) — acuan batas T6c. */
    private var previousFocalPx: Float? = null

    init {
        viewModelScope.launch {
            settings.childAge.collect { age ->
                childFaceHeightCm = com.jagamata.app.detection.DistanceEstimator.getChildFaceHeightCm(age)
            }
        }
        viewModelScope.launch {
            previousFocalPx = settings.currentFocalPx().takeIf { it > 0f }
        }
        viewModelScope.launch {
            DetectionForegroundService.latest.collect { result ->
                onDetectionResult(result)
            }
        }
    }

    fun startCalibration() {
        calibrationManager.reset()
        sampleStartTimeMs = 0L
        progressAnchored = false
        viewModelScope.launch {
            previousFocalPx = settings.currentFocalPx().takeIf { it > 0f }
        }
        _uiState.value =
            _uiState.value.copy(
                isCalibrating = true,
                isCalibrated = false,
                progress = 0f,
                focalReject = null,
            )
    }

    fun retryCalibration() {
        startCalibration()
    }

    fun skipCalibration() {
        viewModelScope.launch {
            settings.setFocalPx(SettingsRepository.DEFAULT_FOCAL_PX)
            settings.setOnboardingDone(true)
        }
    }

    private fun onDetectionResult(result: DetectionResult?) {
        val height = result?.faceHeightPx
        val distance = result?.distanceCm
        val currentState = _uiState.value

        if (!currentState.isCalibrating || currentState.isCalibrated) {
            _uiState.value =
                currentState.copy(
                    faceHeightPx = height,
                    distanceCm = distance,
                )
            return
        }

        if (height == null || height <= 0f) {
            _uiState.value =
                currentState.copy(
                    faceHeightPx = null,
                    distanceCm = distance,
                )
            return
        }

        // height != null menyiratkan result != null (height berasal dari result).
        val detection = result ?: return
        // Waktu monoton dari pipeline deteksi (elapsedRealtime, M4) —
        // bukan System.currentTimeMillis yang bisa mundur dan memecahkan window stabil.
        val now = detection.timestampMs
        if (!progressAnchored) {
            sampleStartTimeMs = now
            progressAnchored = true
        }
        // T10: teruskan pitch saat ini supaya kalibrasi memakai tinggi efektif
        // yang sama dengan rumus jarak runtime.
        val focalPx = calibrationManager.addSample(height, now, childFaceHeightCm, detection.pitchDeg)
        val elapsed = (now - sampleStartTimeMs).toFloat()
        val prog = (elapsed / CalibrationManager.STABILITY_MS.toFloat()).coerceIn(0f, 1f)

        if (focalPx != null) {
            // T6c: tolak hasil di luar batas wajar — jangan simpan, minta ulangi.
            val reject = CalibrationBounds.rejectReason(focalPx, previousFocalPx)
            if (reject != null) {
                calibrationManager.reset()
                sampleStartTimeMs = 0L
                progressAnchored = false
                _uiState.value =
                    currentState.copy(
                        faceHeightPx = height,
                        distanceCm = distance,
                        progress = 0f,
                        isCalibrating = false,
                        isCalibrated = false,
                        focalReject = reject,
                    )
                return
            }
            _uiState.value =
                currentState.copy(
                    faceHeightPx = height,
                    distanceCm = distance,
                    progress = 1f,
                    isCalibrating = false,
                    isCalibrated = true,
                    calibratedFocalPx = focalPx,
                )
            previousFocalPx = focalPx
            viewModelScope.launch {
                settings.setFocalPx(focalPx)
                settings.setOnboardingDone(true)
            }
        } else {
            _uiState.value =
                currentState.copy(
                    faceHeightPx = height,
                    distanceCm = distance,
                    progress = prog,
                )
        }
    }
}
