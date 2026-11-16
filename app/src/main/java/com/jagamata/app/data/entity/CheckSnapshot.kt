package com.jagamata.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 1 baris per siklus 60 detik (PRD §2.9–11).
 * Retensi 30 hari (prune harian). Tidak pernah berisi gambar.
 */
@Entity(tableName = "check_snapshots")
data class CheckSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long,
    val cycleDurationMs: Long,
    val sampleCount: Int,
    val noFaceCount: Int,
    val minDistanceCm: Float?,
    val avgDistanceCm: Float?,
    val maxDistanceCm: Float?,
    val avgLux: Float?,
    val maxPitchDeg: Float?,
    /** Dipisah dengan `|`: TOO_CLOSE|TOO_DARK|BAD_POSTURE */
    val violations: String,
    val dayEpoch: Long,
)
