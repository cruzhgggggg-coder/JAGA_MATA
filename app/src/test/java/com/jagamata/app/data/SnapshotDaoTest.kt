package com.jagamata.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jagamata.app.data.entity.CheckSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Insert / query / prune retensi 30 hari (PRD §2.9). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SnapshotDaoTest {
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

    private fun snapshot(
        ts: Long,
        violations: String = "",
    ) = CheckSnapshot(
        timestampMs = ts,
        cycleDurationMs = 60_000,
        sampleCount = 5,
        noFaceCount = 0,
        minDistanceCm = 25f,
        avgDistanceCm = 40f,
        maxDistanceCm = 60f,
        avgLux = 300f,
        maxPitchDeg = 10f,
        violations = violations,
        dayEpoch = ts / 86_400_000L,
    )

    @Test
    fun insertDanQuery() =
        runTest {
            db.snapshotDao().insert(snapshot(1_000))
            db.snapshotDao().insert(snapshot(2_000))
            assertEquals(2, db.snapshotDao().count())
            assertEquals(2, db.snapshotDao().getAll().size)
        }

    @Test
    fun getSinceMemfilterTimestamp() =
        runTest {
            db.snapshotDao().insert(snapshot(1_000))
            db.snapshotDao().insert(snapshot(50_000))
            val result = db.snapshotDao().getSince(10_000)
            assertEquals(1, result.size)
            assertEquals(50_000, result.first().timestampMs)
        }

    @Test
    fun prune30HariMenghapusDataLama() =
        runTest {
            val dayMs = 86_400_000L
            val now = 31 * dayMs
            db.snapshotDao().insert(snapshot(now - 31 * dayMs)) // 31 hari lalu
            db.snapshotDao().insert(snapshot(now - 10 * dayMs)) // 10 hari lalu
            val deleted = db.snapshotDao().deleteOlderThan(now - 30 * dayMs)
            assertEquals(1, deleted)
            assertEquals(1, db.snapshotDao().count())
        }

    @Test
    fun deleteAllMembersihkanTabel() =
        runTest {
            db.snapshotDao().insert(snapshot(1_000))
            db.snapshotDao().deleteAll()
            assertEquals(0, db.snapshotDao().count())
        }

    @Test
    fun kolomViolationsTersimpanBenar() =
        runTest {
            db.snapshotDao().insert(snapshot(1_000, violations = "TOO_CLOSE|BAD_POSTURE"))
            val loaded = db.snapshotDao().getAll().first()
            assertTrue(loaded.violations.contains("TOO_CLOSE"))
            assertTrue(loaded.violations.contains("BAD_POSTURE"))
        }
}
