package com.jagamata.app.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val REPORT = "report"
    const val PRIVACY = "privacy"

    // Layar desain FigCraft (17 frame halaman Screens)
    const val BERANDA = "beranda" // 02
    const val PANTAU_LIVE = "pantau_live" // 03
    const val PERINGATAN = "peringatan" // 04 (state statis alarm)
    const val ISTIRAHAT = "istirahat" // 05
    const val STATISTIK = "statistik" // 06 Statistik Harian
    const val HADIAH = "hadiah" // 07
    const val PROFIL_ANAK = "profil_anak" // 08
    const val IZIN = "izin" // 11 Izin & Akses
    const val KALIBRASI = "kalibrasi" // 12
    const val KUNCI = "kunci" // 13 Kunci Orang Tua
    const val DELETE_DATA = "delete_data" // bagian dari 14 Privasi & Data
    const val OVERLAY_DITOLAK = "overlay_ditolak" // 15
    const val DEBUG_GALLERY = "debug_gallery"

    /** Pemetaan slug capture (adb --es screen) → route. */
    private val slugRoutes =
        mapOf(
            "onboarding1" to ONBOARDING,
            "onboarding2" to ONBOARDING,
            "onboarding3" to ONBOARDING,
            "beranda" to BERANDA,
            "pantau_live" to PANTAU_LIVE,
            "peringatan" to PERINGATAN,
            "istirahat" to ISTIRAHAT,
            "statistik_harian" to STATISTIK,
            "hadiah" to HADIAH,
            "profil_anak" to PROFIL_ANAK,
            "dashboard_ortu" to DASHBOARD,
            "pengaturan" to SETTINGS,
            "izin" to IZIN,
            "kalibrasi" to KALIBRASI,
            "kunci" to KUNCI,
            "privasi" to PRIVACY,
            "overlay_ditolak" to OVERLAY_DITOLAK,
        )

    fun routeForSlug(slug: String?): String? = slug?.let(slugRoutes::get)
}
