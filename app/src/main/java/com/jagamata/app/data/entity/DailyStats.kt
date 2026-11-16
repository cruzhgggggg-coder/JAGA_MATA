package com.jagamata.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Rollup harian untuk grafik 7 hari dashboard (PRD US-10, DEV-17). */
@Entity(tableName = "daily_stats")
data class DailyStats(
    /** Epoch day (hari sejak 1970-01-01) — kunci unik per tanggal lokal. */
    @PrimaryKey val dayEpoch: Long,
    val closeViolations: Int = 0,
    val darkMinutes: Int = 0,
    val postureViolations: Int = 0,
    val totalCycles: Int = 0,
)
