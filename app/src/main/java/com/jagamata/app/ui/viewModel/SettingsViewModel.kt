package com.jagamata.app.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jagamata.app.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Pengaturan orang tua (PRD US-11): 3 ambang, Mode Ringan, banner AMAN,
 * kalibrasi ulang, PIN. Perubahan langsung berlaku ke state machine
 * tanpa restart service (DetectionStateMachine membaca nilai dari
 * SettingsRepository saat inisialisasi service).
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = SettingsRepository(application)

    val closeThresholdCm: StateFlow<Float> =
        settings.closeThresholdCm
            .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsRepository.DEFAULT_CLOSE_CM)

    val luxThreshold: StateFlow<Float> =
        settings.luxThreshold
            .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsRepository.DEFAULT_LUX)

    val pitchThresholdDeg: StateFlow<Float> =
        settings.pitchThresholdDeg
            .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsRepository.DEFAULT_PITCH)

    val lightModeEnabled: StateFlow<Boolean> =
        settings.lightModeEnabled
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val safeBannerEnabled: StateFlow<Boolean> =
        settings.safeBannerEnabled
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setCloseThresholdCm(value: Float) {
        viewModelScope.launch { settings.setCloseThresholdCm(value) }
    }

    fun setLuxThreshold(value: Float) {
        viewModelScope.launch { settings.setLuxThreshold(value) }
    }

    fun setPitchThresholdDeg(value: Float) {
        viewModelScope.launch { settings.setPitchThresholdDeg(value) }
    }

    fun setLightModeEnabled(value: Boolean) {
        viewModelScope.launch { settings.setLightModeEnabled(value) }
    }

    fun setSafeBannerEnabled(value: Boolean) {
        viewModelScope.launch { settings.setSafeBannerEnabled(value) }
    }

    fun setParentPin(pin: String) {
        viewModelScope.launch { settings.setParentPin(pin) }
    }

    fun verifyParentPin(
        pin: String,
        onResult: (Boolean) -> Unit,
    ) {
        viewModelScope.launch { onResult(settings.verifyParentPin(pin)) }
    }
}
