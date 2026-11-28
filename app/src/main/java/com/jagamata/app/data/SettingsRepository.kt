package com.jagamata.app.data

import android.content.Context
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

/**
 * Status percobaan PIN yang dipersist (T3): jumlah kegagalan beruntun dan sisa
 * durasi lockout. [lockoutRemainingMs] memakai semantik sisa-durasi, bukan
 * timestamp absolut — lihat [SettingsRepository.effectiveLockoutRemainingMs].
 */
data class PinLockoutState(
    val failedAttempts: Int,
    val lockoutRemainingMs: Long,
) {
    val isLockedOut: Boolean get() = lockoutRemainingMs > 0L
}

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
    val voiceEnabled: Flow<Boolean> = read(KEY_VOICE_ENABLED, true)
    val childName: Flow<String> = read(KEY_CHILD_NAME, "Sahabat")
    val childAge: Flow<Int> = read(KEY_CHILD_AGE, 8)
    val childGrade: Flow<String> = read(KEY_CHILD_GRADE, "Kelas 3 SD")
    val dailyLimitMinutes: Flow<Int> = read(KEY_DAILY_LIMIT_MINUTES, 60)

    suspend fun setCloseThresholdCm(value: Float) = write(KEY_CLOSE_CM, value)

    suspend fun setLuxThreshold(value: Float) = write(KEY_LUX, value)

    suspend fun setPitchThresholdDeg(value: Float) = write(KEY_PITCH, value)

    suspend fun setLightModeEnabled(value: Boolean) = write(KEY_LIGHT_MODE, value)

    suspend fun setSafeBannerEnabled(value: Boolean) = write(KEY_SAFE_BANNER, value)

    suspend fun setFocalPx(value: Float) = write(KEY_FOCAL_PX, value)

    suspend fun setOnboardingDone(value: Boolean) = write(KEY_ONBOARDING_DONE, value)

    suspend fun setVoiceEnabled(value: Boolean) = write(KEY_VOICE_ENABLED, value)

    suspend fun setChildName(value: String) = write(KEY_CHILD_NAME, value)

    suspend fun setChildAge(value: Int) = write(KEY_CHILD_AGE, value)

    suspend fun setChildGrade(value: String) = write(KEY_CHILD_GRADE, value)

    suspend fun setDailyLimitMinutes(value: Int) = write(KEY_DAILY_LIMIT_MINUTES, value)

    suspend fun setParentPin(pin: String) {
        val salt = System.currentTimeMillis().toString()
        val hash = hashPin(pin, salt)
        // Salt + hash harus satu transaksi: dua write terpisah bisa menyisakan
        // salt baru + hash lama jika proses mati di tengah (PIN tidak bisa diverifikasi).
        context.dataStore.edit {
            it[KEY_PIN_SALT] = salt
            it[KEY_PIN_HASH] = hash
        }
    }

    /**
     * Verifikasi PIN orang tua. Tanpa hash tersimpan (belum dibuat, atau baru
     * "Hapus Semua Data") SEMUA PIN ditolak — tidak ada PIN default (T1).
     * Pembuatan PIN pertama lewat [setParentPin] yang tidak memverifikasi PIN lama.
     */
    suspend fun verifyParentPin(pin: String): Boolean {
        val hash = parentPinHash.first()
        val salt = context.dataStore.data.first()[KEY_PIN_SALT]
        return pinMatches(pin, salt, hash)
    }

    /** Status onboarding untuk routing splash (T2). */
    suspend fun isOnboardingDone(): Boolean = onboardingDone.first()

    /** true bila PIN orang tua sudah pernah dibuat (T2). */
    suspend fun hasParentPin(): Boolean = parentPinHash.first().isNotEmpty()

    /** Nilai focalPx hasil kalibrasi; 0 = belum dikalibrasi (T2). */
    suspend fun currentFocalPx(): Float = focalPx.first()

    /**
     * Baca status lockout persisten (T3). Sekaligus "decrement-on-read": sisa
     * durasi dikurangi selisih monotonic sejak penulisan lalu di-stamp ulang,
     * sehingga sisa waktu tidak hilang antar-sesi dan tetap kebal rollback jam dinding.
     */
    suspend fun pinLockoutState(nowElapsedMs: Long = SystemClock.elapsedRealtime()): PinLockoutState {
        var attempts = 0
        var remaining = 0L
        context.dataStore.edit { prefs ->
            attempts = prefs[KEY_PIN_FAILED_ATTEMPTS] ?: 0
            remaining =
                effectiveLockoutRemainingMs(
                    prefs[KEY_PIN_LOCKOUT_REMAINING_MS] ?: 0L,
                    prefs[KEY_PIN_LOCKOUT_STAMP_ELAPSED_MS] ?: 0L,
                    nowElapsedMs,
                )
            prefs[KEY_PIN_LOCKOUT_REMAINING_MS] = remaining
            prefs[KEY_PIN_LOCKOUT_STAMP_ELAPSED_MS] = nowElapsedMs
        }
        return PinLockoutState(attempts, remaining)
    }

    /**
     * Catat satu kegagalan verifikasi PIN (T3). Counter naik permanen (hanya
     * direset saat sukses) dan lockout eksponensial dipasang begitu mencapai
     * ambang [lockoutDurationMs].
     */
    suspend fun registerFailedPinAttempt(nowElapsedMs: Long = SystemClock.elapsedRealtime()): PinLockoutState {
        var attempts = 0
        var remaining = 0L
        context.dataStore.edit { prefs ->
            val prevRemaining =
                effectiveLockoutRemainingMs(
                    prefs[KEY_PIN_LOCKOUT_REMAINING_MS] ?: 0L,
                    prefs[KEY_PIN_LOCKOUT_STAMP_ELAPSED_MS] ?: 0L,
                    nowElapsedMs,
                )
            attempts = (prefs[KEY_PIN_FAILED_ATTEMPTS] ?: 0) + 1
            prefs[KEY_PIN_FAILED_ATTEMPTS] = attempts
            // Eskalasi jadwal tidak boleh memperpendek lockout yang masih berjalan.
            remaining = maxOf(prevRemaining, lockoutDurationMs(attempts))
            prefs[KEY_PIN_LOCKOUT_REMAINING_MS] = remaining
            prefs[KEY_PIN_LOCKOUT_STAMP_ELAPSED_MS] = nowElapsedMs
        }
        return PinLockoutState(attempts, remaining)
    }

    /** Reset counter percobaan + lockout setelah verifikasi sukses (T3). */
    suspend fun resetPinAttempts() {
        context.dataStore.edit {
            it[KEY_PIN_FAILED_ATTEMPTS] = 0
            it[KEY_PIN_LOCKOUT_REMAINING_MS] = 0L
            it[KEY_PIN_LOCKOUT_STAMP_ELAPSED_MS] = 0L
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    private fun <T> read(
        key: Preferences.Key<T>,
        default: T,
    ): Flow<T> = context.dataStore.data.map { it[key] ?: default }

    private suspend fun <T> write(
        key: Preferences.Key<T>,
        value: T,
    ) {
        context.dataStore.edit { it[key] = value }
    }

    companion object {
        const val DEFAULT_CLOSE_CM = 30f
        const val DEFAULT_LUX = 150f
        const val DEFAULT_PITCH = 30f
        const val DEFAULT_FOCAL_PX = 560f

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
        private val KEY_PIN_FAILED_ATTEMPTS = intPreferencesKey("pin_failed_attempts")
        private val KEY_PIN_LOCKOUT_REMAINING_MS = longPreferencesKey("pin_lockout_remaining_ms")
        private val KEY_PIN_LOCKOUT_STAMP_ELAPSED_MS = longPreferencesKey("pin_lockout_stamp_elapsed_ms")
        private val KEY_VOICE_ENABLED = booleanPreferencesKey("kiko_voice_enabled")
        private val KEY_CHILD_NAME = stringPreferencesKey("child_name")
        private val KEY_CHILD_AGE = intPreferencesKey("child_age")
        private val KEY_CHILD_GRADE = stringPreferencesKey("child_grade")
        private val KEY_DAILY_LIMIT_MINUTES = intPreferencesKey("daily_limit_minutes")

        /**
         * Lockout eksponensial (T3): kegagalan ke-5 → 1 menit, ke-6 → 5 menit,
         * ke-7 → 15 menit, ke-8 dan seterusnya → 60 menit. Di bawah 5 kegagalan
         * beruntun tidak ada lockout. Counter TIDAK direset saat lockout habis —
         * hanya verifikasi sukses yang mereset.
         */
        fun lockoutDurationMs(failedAttempts: Int): Long =
            when {
                failedAttempts >= 8 -> 60 * 60_000L
                failedAttempts == 7 -> 15 * 60_000L
                failedAttempts == 6 -> 5 * 60_000L
                failedAttempts == 5 -> 1 * 60_000L
                else -> 0L
            }

        /**
         * Sisa lockout efektif dari semantik sisa-durasi (T3).
         * [storedRemainingMs] = sisa durasi saat penulisan, [stampElapsedMs] =
         * `SystemClock.elapsedRealtime()` saat penulisan. Dihitung dari selisih
         * monotonic sehingga rollback jam dinding tidak memendekkan lockout.
         * Bila elapsedRealtime turun (perangkat baru di-boot, delta < 0) sisa
         * waktu tetap penuh — fail-safe, restart tidak membobol lockout;
         * pemanggil (lihat [SettingsRepository.pinLockoutState]) men-stamp ulang
         * sehingga hitung mundur berjalan normal dari sisa penuh tersebut.
         */
        fun effectiveLockoutRemainingMs(
            storedRemainingMs: Long,
            stampElapsedMs: Long,
            nowElapsedMs: Long,
        ): Long {
            if (storedRemainingMs <= 0L) return 0L
            val delta = nowElapsedMs - stampElapsedMs
            return if (delta < 0L) {
                storedRemainingMs
            } else {
                (storedRemainingMs - delta).coerceAtLeast(0L)
            }
        }

        /**
         * Keputusan verifikasi PIN murni (T1): hash kosong (PIN belum dibuat atau
         * baru dihapus) menolak SEMUA PIN — tidak ada PIN default. Salt hilang
         * juga menolak. Selebihnya cocokkan [hashPin] == hash.
         */
        fun pinMatches(
            pin: String,
            salt: String?,
            hash: String,
        ): Boolean {
            if (hash.isEmpty() || salt == null) return false
            return hashPin(pin, salt) == hash
        }

        /** SHA-256(pin + salt) hex (US-11, implementasi minimal). */
        fun hashPin(
            pin: String,
            salt: String,
        ): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest((pin + salt).toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
