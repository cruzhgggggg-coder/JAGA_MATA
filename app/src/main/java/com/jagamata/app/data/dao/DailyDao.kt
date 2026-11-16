package com.jagamata.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jagamata.app.data.entity.DailyStats

@Dao
interface DailyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: DailyStats)

    @Query("SELECT * FROM daily_stats WHERE dayEpoch >= :sinceDayEpoch ORDER BY dayEpoch ASC")
    suspend fun getSince(sinceDayEpoch: Long): List<DailyStats>

    @Query("SELECT * FROM daily_stats ORDER BY dayEpoch ASC")
    suspend fun getAll(): List<DailyStats>

    @Query("SELECT * FROM daily_stats WHERE dayEpoch = :dayEpoch LIMIT 1")
    suspend fun getByDay(dayEpoch: Long): DailyStats?

    /** Rollup dari `check_snapshots` satu hari (dipanggil DailyRollupWorker). */
    @Query(
        """
        SELECT
            COUNT(*) AS totalCycles,
            COALESCE(SUM(CASE WHEN violations LIKE '%TOO_CLOSE%' THEN 1 ELSE 0 END), 0) AS closeViolations,
            COALESCE(SUM(CASE WHEN violations LIKE '%BAD_POSTURE%' THEN 1 ELSE 0 END), 0) AS postureViolations
        FROM check_snapshots
        WHERE timestampMs >= :fromMs AND timestampMs < :toMs
        """
    )
    suspend fun aggregateCycles(fromMs: Long, toMs: Long): CycleAggregate

    @Query("DELETE FROM daily_stats")
    suspend fun deleteAll()
}

data class CycleAggregate(
    val totalCycles: Int,
    val closeViolations: Int,
    val postureViolations: Int
)
