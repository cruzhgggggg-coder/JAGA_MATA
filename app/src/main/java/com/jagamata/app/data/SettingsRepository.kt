package com.jagamata.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jagamata_settings")

/**
 * Pengaturan aplikasi (PRD §2.8, §2.17–18, US-11).
 * Ambang bisa diubah dari Settings dan langsung berlaku ke state machine
 * tanpa restart service. PIN disimpan sebagai SHA-256 + salt.
 */
class SettingsRepository(private val context: Context) {

    val closeThresholdCm: Flow<Float> = read(KEY_CLOSE_CM, DEFAULT_CLOSE_CM)
    val luxThreshold: Flow<Float> = read(KEY_LUX, DEFAULT_LUX)
    val pitchThresholdDeg: Flow<Float> = read(KEY_PITCH, DEFAULT_PITCH)
    val lightModeEnabled: Flow<Boolean> = read(KEY_LIGHT_MODE, false)
    val safeBannerEnabled: Flow<Boolean> = read(KEY_SAFE_BANNER, true)
    val focalPx: Flow<Float> = read(KEY_FOCAL_PX, 0f)
    val onboardingDone: Flow<Boolean> = read(KEY_ONBOARDING_DONE, false)
    val parentPinHash: Flow<String> = read(KEY_PIN_HASH, "")

    suspend fun setCloseThresholdCm(value: Float) = write(KEY_CLOSE_CM, value)
    suspend fun setLuxThreshold(value: Float) = write(KEY_LUX, value)
    suspend fun setPitchThresholdDeg(value: Float) = write(KEY_PITCH, value)
    suspend fun setLightModeEnabled(value: Boolean) = write(KEY_LIGHT_MODE, value)
    suspend fun setSafeBannerEnabled(value: Boolean) = write(KEY_SAFE_BANNER, value)
    suspend fun setFocalPx(value: Float) = write(KEY_FOCAL_PX, value)
    suspend fun setOnboardingDone(value: Boolean) = write(KEY_ONBOARDING_DONE, value)

    suspend fun setParentPin(pin: String) {
        val salt = System.currentTimeMillis().toString()
        write(KEY_PIN_SALT, salt)
        write(KEY_PIN_HASH, hashPin(pin, salt))
    }

    suspend fun verifyParentPin(pin: String): Boolean {
        val hash = parentPinHash.first()
        val salt = context.dataStore.data.first()[KEY_PIN_SALT] ?: return false
        if (hash.isEmpty()) return true
        return hashPin(pin, salt) == hash
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    private fun <T> read(key: Preferences.Key<T>, default: T): Flow<T> =
        context.dataStore.data.map { it[key] ?: default }

    private suspend fun <T> write(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    companion object {
        const val DEFAULT_CLOSE_CM = 30f
        const val DEFAULT_LUX = 150f
        const val DEFAULT_PITCH = 30f

        // Rentang slider Settings (PRD §2.17)
        const val CLOSE_CM_RANGE_MIN = 20f
        const val CLOSE_CM_RANGE_MAX = 50f
        const val LUX_RANGE_MIN = 50f
        const val LUX_RANGE_MAX = 500f
        const val PITCH_RANGE_MIN = 20f
        const val PITCH_RANGE_MAX = 60f

        private val KEY_CLOSE_CM = floatPreferencesKey("close_threshold_cm")
        private val KEY_LUX = floatPreferencesKey("lux_threshold")
        private val KEY_PITCH = floatPreferencesKey("pitch_threshold_deg")
        private val KEY_LIGHT_MODE = booleanPreferencesKey("light_mode_enabled")
        private val KEY_SAFE_BANNER = booleanPreferencesKey("safe_banner_enabled")
        private val KEY_FOCAL_PX = floatPreferencesKey("focal_px")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_PIN_HASH = stringPreferencesKey("parent_pin_hash")
        private val KEY_PIN_SALT = stringPreferencesKey("parent_pin_salt")

        /** SHA-256(pin + salt) hex (US-11, implementasi minimal). */
        fun hashPin(pin: String, salt: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest((pin + salt).toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
