package com.jagamata.app.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jagamata.app.BuildConfig
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.navigation.Routes
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.InkSoft

/** Satu tile galeri = satu frame desain FigCraft. */
data class DesignScreenEntry(
    val slug: String,
    val title: String,
    val route: String,
)

/** 17 frame halaman Screens (urut nama frame). */
val DesignScreenEntries: List<DesignScreenEntry> =
    listOf(
        DesignScreenEntry("onboarding1", "01a Onboarding 1", Routes.ONBOARDING),
        DesignScreenEntry("onboarding2", "01b Onboarding 2", Routes.ONBOARDING),
        DesignScreenEntry("onboarding3", "01c Onboarding 3", Routes.ONBOARDING),
        DesignScreenEntry("beranda", "02 Beranda", Routes.BERANDA),
        DesignScreenEntry("pantau_live", "03 Pantau Jarak Live", Routes.PANTAU_LIVE),
        DesignScreenEntry("peringatan", "04 Peringatan Jarak Dekat", Routes.PERINGATAN),
        DesignScreenEntry("istirahat", "05 Istirahat 20-20-20", Routes.ISTIRAHAT),
        DesignScreenEntry("statistik_harian", "06 Statistik Harian", Routes.STATISTIK),
        DesignScreenEntry("hadiah", "07 Hadiah & Streak", Routes.HADIAH),
        DesignScreenEntry("profil_anak", "08 Profil Anak", Routes.PROFIL_ANAK),
        DesignScreenEntry("dashboard_ortu", "09 Dashboard Orang Tua", Routes.DASHBOARD),
        DesignScreenEntry("pengaturan", "10 Pengaturan", Routes.SETTINGS),
        DesignScreenEntry("izin", "11 Izin & Akses", Routes.IZIN),
        DesignScreenEntry("kalibrasi", "12 Kalibrasi Kamera", Routes.KALIBRASI),
        DesignScreenEntry("kunci", "13 Kunci Orang Tua", Routes.KUNCI),
        DesignScreenEntry("privasi", "14 Privasi & Data", Routes.PRIVACY),
        DesignScreenEntry("overlay_ditolak", "15 Overlay Ditolak", Routes.OVERLAY_DITOLAK),
    )

/**
 * Galeri layar desain (hanya BuildConfig.DEBUG): daftar 17 tile untuk
 * navigasi cepat & capture screenshot via adb.
 */
@Composable
fun DebugGallery(onOpen: (String) -> Unit) {
    if (!BuildConfig.DEBUG) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Background),
        )
        return
    }
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Design Gallery — 17 layar desain",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        items(DesignScreenEntries) { entry ->
            JagaCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(entry.route) },
                contentPadding =
                    PaddingValues(
                        horizontal = 16.dp,
                        vertical = 14.dp,
                    ),
            ) {
                Column {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                    Text(
                        text = entry.slug,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft,
                    )
                }
            }
        }
    }
}
