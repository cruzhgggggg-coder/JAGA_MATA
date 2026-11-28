package com.jagamata.app.ui.navigation

/**
 * Mesin keputusan gerbang rute orang tua (DEV-37) — murni Kotlin, tanpa dependensi
 * Android/Compose supaya bisa diuji di JVM unit test.
 *
 * Aturannya:
 * - Rute di [GATED_ROUTES] hanya bisa dibuka saat [unlocked]; jika belum, tujuan
 *   disimpan ke [pending] dan caller diarahkan ke [Routes.KUNCI].
 * - Unlock menandai sesi terbuka dan mengembalikan tujuan tertunda (jika ada).
 * - Auto-relock: gap latar-belakang lebih besar dari [DEFAULT_TIMEOUT_MS] mencabut unlock.
 */
class ParentGate {
    /** True setelah PIN terverifikasi; false saat terkunci. */
    var unlocked: Boolean = false

    /** Tujuan yang tertunda saat pengguna diarahkan ke [Routes.KUNCI]. */
    var pending: String? = null
        internal set

    /** Waktu terakhir aplikasi masuk latar-belakang (0 = belum pernah). */
    var lastBackgroundAtMs: Long = 0L

    /**
     * Mengembalikan route yang benar-benar dituju: [route] saat boleh, atau
     * [Routes.KUNCI] sambil menyimpan [pending] untuk rute sensitif yang masih terkunci.
     */
    fun tryNavigate(route: String): String {
        if (!unlocked && route in GATED_ROUTES) {
            pending = route
            return Routes.KUNCI
        }
        return route
    }

    /**
     * Dipanggil setelah PIN berhasil: menandai sesi terbuka dan mengembalikan
     * tujuan tertunda untuk dilanjutkan, atau null jika tidak ada.
     */
    fun onUnlocked(): String? {
        unlocked = true
        return pending.also { pending = null }
    }

    /** Mencatat waktu aplikasi masuk latar-belakang. */
    fun onAppBackground(nowMs: Long = System.currentTimeMillis()) {
        lastBackgroundAtMs = nowMs
    }

    /**
     * Dipanggil saat aplikasi kembali ke latar-depan: mencabut unlock bila jeda
     * latar-belakang melebihi [timeoutMs]. Sesi yang belum pernah di-background
     * ([lastBackgroundAtMs] == 0) tidak pernah di-relock.
     */
    fun onAppForeground(
        nowMs: Long = System.currentTimeMillis(),
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    ) {
        if (unlocked && lastBackgroundAtMs > 0L && nowMs - lastBackgroundAtMs > timeoutMs) {
            unlocked = false
            pending = null
        }
    }

    companion object {
        /** Rute yang wajib melewati PIN orang tua. */
        val GATED_ROUTES: Set<String> = setOf(Routes.SETTINGS, Routes.DELETE_DATA, Routes.DASHBOARD)

        /** Batas latar-belakang sebelum unlock dicabut (5 menit). */
        const val DEFAULT_TIMEOUT_MS: Long = 5 * 60_000L
    }
}
