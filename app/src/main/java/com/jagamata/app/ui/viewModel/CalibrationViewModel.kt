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

    init {
        viewModelScope.launch {
            DetectionForegroundService.latest.collect { result ->
                onDetectionResult(result)
            }
        }
    }

    fun startCalibration() {
        calibrationManager.reset()
        sampleStartTimeMs = System.currentTimeMillis()
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

        val now = System.currentTimeMillis()
        val focalPx = calibrationManager.addSample(height, now)
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
