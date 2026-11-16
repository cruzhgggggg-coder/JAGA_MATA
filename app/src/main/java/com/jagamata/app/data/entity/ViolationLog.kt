package com.jagamata.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Catatan pelanggaran per siklus (PRD US-08, US-09). */
@Entity(tableName = "violation_logs")
data class ViolationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long,
    val type: String,
    val distanceCm: Float?,
    val lux: Float?,
    val pitchDeg: Float?,
)
