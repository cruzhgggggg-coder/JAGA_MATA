package com.jagamata.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jagamata.app.data.entity.ViolationLog

@Dao
interface ViolationDao {
    @Insert
    suspend fun insert(log: ViolationLog): Long

    @Query("SELECT * FROM violation_logs WHERE timestampMs >= :sinceMs ORDER BY timestampMs ASC")
    suspend fun getSince(sinceMs: Long): List<ViolationLog>

    @Query("SELECT COUNT(*) FROM violation_logs WHERE type = :type AND timestampMs >= :sinceMs")
    suspend fun countByTypeSince(
        type: String,
        sinceMs: Long,
    ): Int

    @Query("SELECT * FROM violation_logs ORDER BY timestampMs DESC")
    suspend fun getAll(): List<ViolationLog>

    @Query("DELETE FROM violation_logs WHERE timestampMs < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long): Int

    @Query("DELETE FROM violation_logs")
    suspend fun deleteAll()
}
