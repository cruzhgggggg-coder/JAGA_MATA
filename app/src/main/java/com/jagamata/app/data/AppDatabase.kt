package com.jagamata.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jagamata.app.data.dao.DailyDao
import com.jagamata.app.data.dao.SnapshotDao
import com.jagamata.app.data.dao.ViolationDao
import com.jagamata.app.data.entity.AppSettings
import com.jagamata.app.data.entity.CheckSnapshot
import com.jagamata.app.data.entity.DailyStats
import com.jagamata.app.data.entity.ViolationLog

@Database(
    entities = [
        CheckSnapshot::class,
        ViolationLog::class,
        DailyStats::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun snapshotDao(): SnapshotDao
    abstract fun violationDao(): ViolationDao
    abstract fun dailyDao(): DailyDao

    companion object {
        const val DB_NAME = "jagamata.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
