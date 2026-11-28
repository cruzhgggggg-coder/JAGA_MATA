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
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var sampleStartTimeMs: Long = 0L

    /** True bila anchor waktu progress sudah diambil dari sampel pertama. */
    private var progressAnchored: Boolean = false

    /** Tinggi wajah acuan sesuai usia — harus sama dengan yang dipakai runtime. */
    private var childFaceHeightCm: Float = com.jagamata.app.detection.DistanceEstimator.DEFAULT_FACE_HEIGHT_CM

    init {
        viewModelScope.launch {
            settings.childAge.collect { age ->
                childFaceHeightCm = com.jagamata.app.detection.DistanceEstimator.getChildFaceHeightCm(age)
            }
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
        _uiState.value =
            _uiState.value.copy(
                isCalibrating = true,
                isCalibrated = false,
                progress = 0f,
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
            _uiState.value =
                currentState.copy(
                    faceHeightPx = height,
                    distanceCm = distance,
                    progress = 1f,
                    isCalibrating = false,
                    isCalibrated = true,
                    calibratedFocalPx = focalPx,
                )
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
