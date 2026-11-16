package com.jagamata.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Snapshot pengaturan yang dibutuhkan worker/rollup (PRD §2.10).
 * Sumber utama tetap DataStore ([com.jagamata.app.data.SettingsRepository]);
 * tabel ini menampung nilai yang dipakai perhitungan latar belakang.
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val closeThresholdCm: Float = 30f,
    val luxThreshold: Float = 150f,
    val pitchThresholdDeg: Float = 30f,
    val lightModeEnabled: Boolean = false,
    val safeBannerEnabled: Boolean = true,
    val focalPx: Float = 0f,
    val updatedAtMs: Long = 0L
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
