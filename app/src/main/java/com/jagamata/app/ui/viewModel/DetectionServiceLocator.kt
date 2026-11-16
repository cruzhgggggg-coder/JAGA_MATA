package com.jagamata.app.ui.viewModel

import android.content.Context
import com.jagamata.app.data.AppDatabase

/**
 * Service locator minimal untuk ViewModel tanpa framework DI
 * (proyek v1 sederhana, tanpa Hilt — mencukupi kebutuhan DoD).
 */
object DetectionServiceLocator {
    @Volatile
    private var databaseOverride: AppDatabase? = null

    fun database(context: Context? = null): AppDatabase {
        databaseOverride?.let { return it }
        val ctx = requireNotNull(context) { "Context diperlukan untuk pertama kali" }
        return AppDatabase.getInstance(ctx).also { databaseOverride = it }
    }

    /** Dipakai test untuk injeksi database in-memory. */
    fun overrideDatabase(db: AppDatabase?) {
        databaseOverride = db
    }
}
