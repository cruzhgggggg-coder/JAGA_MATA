package com.jagamata.app.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jagamata.app.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Onboarding orang tua (PRD US-01/US-02): state wizard 5 layar,
 * hasil kalibrasi f_px, pilihan usia — semua disimpan di DataStore.
 */
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = SettingsRepository(application)

    data class State(
        val stepIndex: Int = 0,
        val ageGroup: String = "",
        val focalPx: Float = 0f,
        val calibrated: Boolean = false,
        val closeThresholdCm: Float = SettingsRepository.DEFAULT_CLOSE_CM,
        val luxThreshold: Float = SettingsRepository.DEFAULT_LUX,
        val pitchThresholdDeg: Float = SettingsRepository.DEFAULT_PITCH,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun selectAge(group: String) {
        _state.value = _state.value.copy(ageGroup = group)
    }

    fun onCalibrated(focalPx: Float) {
        _state.value = _state.value.copy(focalPx = focalPx, calibrated = true)
        viewModelScope.launch { settings.setFocalPx(focalPx) }
    }

    fun setThresholds(
        closeCm: Float,
        lux: Float,
        pitchDeg: Float,
    ) {
        _state.value =
            _state.value.copy(
                closeThresholdCm = closeCm,
                luxThreshold = lux,
                pitchThresholdDeg = pitchDeg,
            )
        viewModelScope.launch {
            settings.setCloseThresholdCm(closeCm)
            settings.setLuxThreshold(lux)
            settings.setPitchThresholdDeg(pitchDeg)
        }
    }

    fun markDone() {
        viewModelScope.launch { settings.setOnboardingDone(true) }
    }

    fun nextStep() {
        _state.value = _state.value.copy(stepIndex = _state.value.stepIndex + 1)
    }

    fun previousStep() {
        _state.value = _state.value.copy(stepIndex = (_state.value.stepIndex - 1).coerceAtLeast(0))
    }
}
