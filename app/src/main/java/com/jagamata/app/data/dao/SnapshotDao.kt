package com.jagamata.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jagamata.app.data.entity.CheckSnapshot

@Dao
interface SnapshotDao {
    @Insert
    suspend fun insert(snapshot: CheckSnapshot): Long

    @Query("SELECT * FROM check_snapshots ORDER BY timestampMs DESC")
    suspend fun getAll(): List<CheckSnapshot>

    @Query("SELECT * FROM check_snapshots WHERE timestampMs >= :sinceMs ORDER BY timestampMs ASC")
    suspend fun getSince(sinceMs: Long): List<CheckSnapshot>

    @Query("SELECT COUNT(*) FROM check_snapshots")
    suspend fun count(): Int

    /** Prune retensi 30 hari (PRD §2.9). @return jumlah baris terhapus. */
    @Query("DELETE FROM check_snapshots WHERE timestampMs < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long): Int

    @Query("DELETE FROM check_snapshots")
    suspend fun deleteAll()
}
