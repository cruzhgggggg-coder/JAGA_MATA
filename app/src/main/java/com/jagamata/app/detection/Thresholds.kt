package com.jagamata.app.detection

/**
 * Satu sumber ambang/zona untuk seluruh lapisan (T9 + M25).
 *
 * Pemetaan resmi dari ambang jarak pengguna (dokumentasi keputusan):
 * - [closeEnterCm] = ambang pengguna (default 30 cm, PRD §2.17).
 * - [closeExitCm]  = closeEnter + [CLOSE_EXIT_MARGIN_CM] (default 35 cm —
 *   hysteresis keluar BLOCKED, PRD §5.4).
 * - [safeZoneCm]   = closeEnter + [SAFE_ZONE_MARGIN_CM] (default 50 cm —
 *   zona Aman, PRD §2.23; juga batas atas zona Waspada).
 * - [lux] / [pitchDeg] = ambang pengguna (default 150 lux / 30°).
 *
 * Default konstruktor via [fromClose] menghasilkan 30/35/50/150/30 — identik
 * dengan nilai desain lama, sehingga perilaku state machine & agregator tidak
 * berubah sampai orang tua mengubah ambang.
 */
data class Thresholds(
    val closeEnterCm: Float,
    val closeExitCm: Float,
    val safeZoneCm: Float,
    val lux: Float,
    val pitchDeg: Float,
) {
    companion object {
        /** Margin keluar hysteresis BLOCKED (PRD §5.4: 30 → 35 cm). */
        const val CLOSE_EXIT_MARGIN_CM = 5f

        /** Margin zona Aman (PRD §2.23: 30 → 50 cm). */
        const val SAFE_ZONE_MARGIN_CM = 20f

        /**
         * Satu-satunya cara membangun zona dari ambang jarak pengguna —
         * `closeExit`/`safeZone` tidak pernah di-set terpisah agar tidak ada
         * definisi zona kedua (M25).
         */
        fun fromClose(
            closeEnterCm: Float = 30f,
            lux: Float = 150f,
            pitchDeg: Float = 30f,
        ): Thresholds =
            Thresholds(
                closeEnterCm = closeEnterCm,
                closeExitCm = closeEnterCm + CLOSE_EXIT_MARGIN_CM,
                safeZoneCm = closeEnterCm + SAFE_ZONE_MARGIN_CM,
                lux = lux,
                pitchDeg = pitchDeg,
            )

        /** Nilai desain (sebelum pengaturan orang tua terbaca). */
        val DEFAULT: Thresholds = fromClose()
    }
}
