# DESIGN_AUDIT.md — Audit UI/UX vs Referensi Desain

**Sumber:** `design/referensi/` (export dari `JagaMata_v2.figcraft` yang tidak tersedia di disk)
**Metode:** analisis program (palet warna, deteksi grid, bounding box kartu, deteksi elemen aksen)
**Tanggal:** 2026-07-07 · PRD §2.21–23 (DESIGN MENANG kecuali angka teknis §2.1–18)

---

## 1. Inventaris referensi

| File | Ukuran | Peran |
|---|---|---|
| `01_Foundations_Tokens.png` | 1440×2694 | Token warna/font/radius |
| `02_Mascot_Kiko.png` | 1522×1225 | Art maskot burung hantu |
| `03_Screens_15_Layar.png` | 5856×1802 | Grid 15 layar (393×852) |
| `04_App_Icon.png` | 1652×1024 | Ikon aplikasi |
| `05_Components.png` | 1600×3314 | Komponen (tombol/card/pill) |
| `06_Cover.png` | 1440×900 | Cover presentasi |

Grid `03_Screens_15_Layar.png` dipecah ke `design/referensi/screens/` (17 sel berkonten; 2 di antaranya duplikat → **15 layar unik**, sesuai PRD).

---

## 2. Temuan desain (programatik)

### 2.1 Sistem warna — KONFIRMASI KOKOH
Palet semua 15 layar jatuh ke token yang sama dengan implementasi:

| Token | Hex desain | Implementasi | Status |
|---|---|---|---|
| Background | `#F2F7FF` | `Background` | ✅ cocok |
| Ink (teks) | `#12263A` | `Ink` | ✅ cocok |
| Primary | `#4A86E8` | `Primary` | ✅ cocok |
| Mint (aman) | `#5FD4A7` | `Mint` / `ZoneSafe` | ✅ cocok |
| Sunny (waspada) | `#FFD166` | `Sunny` / `ZoneWarn` | ✅ cocok |
| Coral (bahaya) | `#FF7B6B` | `Coral` / `ZoneDanger` | ✅ cocok |
| SurfaceAlt | `#E8F0FE` | `SurfaceAlt` | ✅ cocok |
| **Cream (badan Kiko)** | **`#FFF6E9`** | **`Cream`** | ✅ **DITAMBAHKAN** |
| PrimaryDark (bayangan) | `#2D70B7` | **`PrimaryDark`** | ✅ **DITAMBAHKAN** |
| BlueTint | `#D4E5FF` | **`BlueTint`** | ✅ **DITAMBAHAN** |

### 2.2 Tata letak (dari bounding box)
- **Ukuran layar acuan:** 392×848 px ≈ 393×852 (PRD) → mapping 1 px ≈ 1 dp
- **Margin horizontal kartu:** 20–24 px (kartu utama x = 20…368 atau 24…372)
  → implementasi `padding(20.dp)` **masih dalam toleransi** (bisa dinaikkan ke 24 dp bila mau persis)
- **Action bar bawah:** pita `y ≈ 752–840` (~96 dp dari bawah) dengan tombol Primary
  di `y ≈ 732` pada banyak layar → pola **CTA utama menempel di bawah**
- **Kartu putih** mendominasi layar Settings/Laporan/Privasi (44%–38% piksel putih)
- **Layar berlatar Background dominan** (39%–43%): Splash, Intro, AgeSelect, Dashboard, Alarm

### 2.3 Maskot Kiko — SELISIH DITEMUKAN & DIPERBAIKI
Palet desain `02_Mascot_Kiko.png`: `#FFF6E9` (badan krem) · `#FFFFFF` (mata) ·
`#4A86E8` (sayap/telinga biru) · `#2D70B7` (bayangan) · `#FFD166` (paruh)

**Sebelum:** placeholder putih + telinga merah + sayap mint (tidak sesuai desain)
**Sesudah:** `res/drawable/mascot.xml` — badan krem, mata putih + pupil Ink,
sayap & telinga biru Primary, bayangan biru tua, paruh kuning

### 2.4 Ikon aplikasi — SELISIH DITEMUKAN & DIPERBAIKI
Palet desain `04_App_Icon.png`: latar `#4A86E8`, badan `#FFF6E9`, mata putih,
aksen `#2D70B7`

**Sebelum:** latar biru + badan putih + telinga merah
**Sesudah:** latar `Primary` + badan `Cream` + telinga/sayap biru + mata putih
(diperbarui di `ic_launcher_foreground.xml`, `mipmap/ic_launcher.xml`, `ic_launcher_round.xml`)

### 2.5 Zona jarak (§2.23) — KONFIRMASI
- Mint muncul dominan di layar "aman" (screen_07, _08, _11, _15)
- Coral muncul di layar alarm/waspada (screen_05, _06, _08, _15)
- Sunny tersebar sebagai banner peringatan
→ Implementasi (`ZoneSafe`/`ZoneWarn`/`ZoneDanger` di dashboard, live strip, banner) **konsisten**

---

## 3. Pemetaan 15 layar desain → file implementasi

| # sel desain | Karakter (dari palet/layout) | File implementasi |
|---|---|---|
| r1c1 | Latar Background + aksen Primary besar | `SplashScreen` / `IntroScreen` |
| r1c2 | Background + tombol di bawah | `AgeSelectScreen` |
| r1c3 | Background dominan | `CalibrationScreen` |
| r1c4 | Kartu putih bertumpuk | `PermissionWizardScreen` |
| r1c5 | Kartu + SurfaceAlt + aksen | `SetupDoneScreen` / onboarding akhir |
| r1c6 | Kartu putih besar (464–756) | `CalibrationScreen` (panel preview) |
| r1c7 | Background + kartu kecil bawah | `DashboardScreen` (strip) |
| r1c8 | Kartu bertumpuk + Mint | `DashboardScreen` (stat + chart) |
| r1c9 | Kartu bertumpuk multi | `SettingsScreen` |
| r1c10 | Kartu + tombol bawah | `SettingsScreen` (lanjutan) |
| r1c11 | Kartu + Mint dominan | `WeeklyReportScreen` |
| r1c12 | Background dominan + kartu tengah | `WeeklyReportScreen` (ringkasan) |
| r2c1 | Background + Primary besar | `PrivacyScreen` / `LockScreen` |
| r2c2 | Background + Primary besar | `DeleteDataScreen` |
| r2c5 | Kartu + SurfaceAlt | `LockScreen` (PIN) |
| r2c12 / r2c13 | **duplikat** → 1 layar | `AlarmBlocked` (overlay) |

> Catatan: pemetaan ini berdasarkan kemiripan palet + struktur layout, **bukan** pembacaan teks
> (tool baca gambar tidak tersedia di sesi ini). Copy Bahasa Indonesia mengikuti PRD US-01…US-15.

---

## 4. Selisih tersisa (belum dikerjakan — kandidat lanjutan)

1. **CTA bawah menempel (bottom bar)** — desain menaruh tombol Primary di `y≈732` (~96 dp dari bawah) pada banyak layar; implementasi menaruh tombol di akhir Column (bisa ter-scroll). Efek UX: jangkauan jempol. *Belum diubah agar tidak memicu regresi; kandidat perbaikan terpisah.*
2. **Margin 24 dp** — sebagian kartu desain memakai 24 px, implementasi 20 dp. Selisih 4 dp, masih dalam toleransi visual.
3. **6 ekspresi Kiko** (PRD §2.19) — maskot statis 1 pose; ekspresi (aman/waspada/bahaya) menyusul bila ekspresi desain teridentifikasi.
4. **Verifikasi visual manual** — pemetaan layar (§3) perlu dicek mata manusia terhadap `screens/*.png` (terutama urutan & copy).

---

## 5. Verifikasi

- `./gradlew.bat clean testDebugUnitTest ktlintCheck detekt assembleDebug` → exit 0 (62 test tetap lulus)
- Token warna baru (`Cream`, `PrimaryDark`, `BlueTint`) ditambahkan ke `ui/theme/Color.kt` tanpa mengubah angka PRD §2.1–18
- Perubahan visual: `drawable/mascot.xml`, `drawable/ic_launcher_foreground.xml`, `mipmap/ic_launcher*.xml`
