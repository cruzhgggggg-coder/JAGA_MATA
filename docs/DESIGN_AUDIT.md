# DESIGN_AUDIT.md — Audit UI/UX vs Referensi Desain FigCraft v2.0

**Sumber Kebenaran Visual:** `JagaMata_v2.figcraft` (17 Layar Desain)
**Metode:** Analisis Komponen, Inspeksi Pixel Emulator, dan Verifikasi Token
**Status:** **100% SELARAS DESAIN (ALL DEFECTS FIXED)**

---

## 1. Inventaris & Keselarasan 17 Layar

| # | Layar FigCraft | File Implementasi Jetpack Compose | Status Visual & Fungsional |
|---|---|---|---|
| 01a | Onboarding 1 (Jarak) | `ui/onboarding/OnboardingFlow.kt` | ✅ Sempurna (Kiko aman tidak terpotong) |
| 01b | Onboarding 2 (Cahaya) | `ui/onboarding/OnboardingFlow.kt` | ✅ Sempurna (Ilustrasi ruangan & dot slider) |
| 01c | Onboarding 3 (Postur) | `ui/onboarding/OnboardingFlow.kt` | ✅ Sempurna (Pill 50 cm terpasang rapat) |
| 02 | Beranda Anak | `ui/child/BerandaScreen.kt` | ✅ Sempurna (Sapaan dinamis, statistik 0 fresh) |
| 03 | Pantau Jarak Live | `ui/live/PantauLiveScreen.kt` | ✅ Sempurna (Busur gauge simetris, kamera depan) |
| 04 | Peringatan Terlalu Dekat | `ui/child/AlarmBlocked.kt` | ✅ Sempurna (Copy resmi "Terlalu dekat!") |
| 05 | Istirahat 20-20-20 | `ui/child/BreakScreen.kt` | ✅ Sempurna (Timer relaksasi mata anak) |
| 06 | Statistik Harian | `ui/parent/DailyStatsScreen.kt` | ✅ Sempurna (Grafik jam & ringkasan aman) |
| 07 | Hadiah & Streak | `ui/parent/RewardsScreen.kt` | ✅ Sempurna (Medali lingkaran & streak harian) |
| 08 | Profil Anak | `ui/parent/ChildProfileScreen.kt` | ✅ Sempurna (Dialog edit profil & pengaturan sensitif) |
| 09 | Dashboard Orang Tua | `ui/parent/DashboardScreen.kt` | ✅ Sempurna (Tren 7 hari & edit ambang jarak) |
| 10 | Pengaturan | `ui/parent/SettingsScreen.kt` | ✅ Sempurna (Menu ganti PIN & menu jarak minimum) |
| 11 | Izin & Akses Sistem | `ui/onboarding/PermissionWizardScreen.kt` | ✅ Sempurna (Toggle switch seragam) |
| 12 | Kalibrasi Kamera | `ui/onboarding/CalibrationScreen.kt` | ✅ Sempurna (Semicircle gauge + Kiko penggaris) |
| 13 | Kunci PIN Orang Tua | `ui/parent/LockScreen.kt` | ✅ Sempurna (PIN 4-digit aman, 3 sparkle putih) |
| 14 | Privasi & Kebijakan Data | `ui/parent/PrivacyScreen.kt` | ✅ Sempurna (Komitmen 100% on-device & disclaimer) |
| 15 | Overlay Ditolak | `ui/common/OverlayDenied.kt` | ✅ Sempurna (Panduan membuka izin overlay) |

---

## 2. Audit Token Desain & Komponen

1. **Sistem Warna Token (Byte-Exact):**
   - Background: `#F2F7FF`
   - Ink: `#12263A` (Teks utama)
   - Primary: `#4A86E8` (Tombol & aksen aktif)
   - PrimaryDark: `#2D70B7` (Bayangan & aksen kontras)
   - Mint: `#5FD4A7` (Zona Jarak Aman)
   - Sunny: `#FFD166` (Zona Waspada)
   - Coral: `#FF7B6B` (Zona Bahaya Terlalu Dekat)
   - Cream: `#FFF6E9` (Warna badan asli Kiko)
   - BlueTint: `#D6E6FF→#B7D2FF` (Gradasi kartu hero)

2. **Tipografi & Geometri:**
   - Font: Baloo 2 (Header, judul, angka besar) dan Nunito (Body, label, tombol).
   - Pengaturan `includeFontPadding = false` memastikan tidak ada teks yang terpotong garis batas kartu.
   - Radius sudut kartu standar: 24dp (`JagaCard`), tombol: 28–30dp.

3. **Maskot Kiko Asli:**
   - Seluruh placeholder lama telah dihapus dan diganti dengan 8 aset resmi FigCraft resolusi tinggi.\n