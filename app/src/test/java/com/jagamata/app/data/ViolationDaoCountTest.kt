package com.jagamata.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jagamata.app.data.entity.ViolationLog
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Hitungan sesi dijeda (PAUSED_BY_CHILD) untuk transparansi Dashboard (DEV-38). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ViolationDaoCountTest {
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java,
            ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun log(
        ts: Long,
        type: String,
    ) = ViolationLog(
        timestampMs = ts,
        type = type,
        distanceCm = null,
        lux = null,
        pitchDeg = null,
    )

    @Test
    fun countByTypeSinceMenghitungHanyaTipeYangDiminta() =
        runTest {
            val todayStart = 1_000_000L
            db.violationDao().insert(log(todayStart + 1, "PAUSED_BY_CHILD"))
            db.violationDao().insert(log(todayStart + 2, "PAUSED_BY_CHILD"))
            db.violationDao().insert(log(todayStart + 3, "TOO_CLOSE"))
            assertEquals(2, db.violationDao().countByTypeSince("PAUSED_BY_CHILD", todayStart))
        }

    @Test
    fun countByTypeSinceTidakMenghitungBarisKemarin() =
        runTest {
            val todayStart = 1_000_000L
            db.violationDao().insert(log(todayStart - 1, "PAUSED_BY_CHILD"))
            assertEquals(0, db.violationDao().countByTypeSince("PAUSED_BY_CHILD", todayStart))
        }
}
