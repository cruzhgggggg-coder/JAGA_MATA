package com.jagamata.app.ui.navigation

/** Keputusan tombol jeda/lanjut pada Pantau Live (T5). */
enum class PauseDecision {
    /** Pemantauan berjalan + sesi terbuka → langsung jeda (hold 3 detik tetap jadi syarat UI). */
    PAUSE_NOW,

    /** Pemantauan berjalan tapi sesi terkunci → minta PIN; jeda dieksekusi setelah unlock. */
    REQUEST_PIN,

    /** Pemantauan tidak berjalan → lanjut instan, tanpa PIN. */
    RESUME_NOW,
}

/**
 * Gerbang jeda (T5) — murni Kotlin, tanpa dependensi Android/Compose supaya bisa
 * diuji di JVM unit test. Hanya menghasilkan keputusan; eksekusi (pause/resume/
 * navigasi [Routes.KUNCI]) tetap di caller.
 *
 * Aturannya:
 * - Menghentikan pemantauan (pause) wajib PIN — anak tidak bisa mematikan
 *   pemantauan sendiri.
 * - Melanjutkan (resume/start) selalu bebas.
 * - Jeda tertunda ([pausePending]) dieksekusi tepat sekali setelah PIN sukses,
 *   atau dibatalkan bila layar kunci ditinggalkan.
 */
class PauseGate {
    /** True bila ada permintaan jeda yang menunggu verifikasi PIN. */
    var pausePending: Boolean = false
        private set

    /** Hold tombol jeda selesai: tentukan apakah langsung jeda, minta PIN, atau lanjut. */
    fun onToggle(
        isRunning: Boolean,
        unlocked: Boolean,
    ): PauseDecision =
        when {
            !isRunning -> {
                pausePending = false
                PauseDecision.RESUME_NOW
            }
            unlocked -> PauseDecision.PAUSE_NOW
            else -> {
                pausePending = true
                PauseDecision.REQUEST_PIN
            }
        }

    /** Setelah PIN sukses: true bila jeda tertunda harus dieksekusi (flag ikut direset). */
    fun consumePendingPause(): Boolean = pausePending.also { pausePending = false }

    /** Meninggalkan layar kunci tanpa PIN membatalkan jeda tertunda. */
    fun cancelPendingPause() {
        pausePending = false
    }
}
