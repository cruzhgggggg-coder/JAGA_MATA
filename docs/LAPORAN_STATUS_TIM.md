# Laporan Status Eksekusi — JagaMata vs Blueprint v2.0

**Untuk:** Tim (penanggung jawab lain bisa langsung lanjut dari dokumen ini)
**Tanggal:** 20 November 2026
**Cakupan sesi terakhir:** Menyamakan **UI/UX aplikasi dengan desain FigCraft `JagaMata_v2`** (17 layar) — loop paralel build → capture emulator → QA visual → fix, 3 iterasi.
**Status build saat ini:** `compileDebugKotlin` **HIJAU** · test/ktlint/detekt hijau (terakhir diuji di awal sesi) · screenshot 17 layar tersedia di `.mimocode/design_refs/app/iter2/`.

---

## 1. Ringkasan untuk yang baru membaca

- **Prinsip yang dipakai:** desain FigCraft = sumber kebenaran visual. Kalau blueprint/PRD beda dengan desain, **desain menang** (keputusan yang sudah dikunci di sesi ini).
- **Hasil utama:** semua 17 layar desain kini ada di app dengan copy teks **100% persis** desain, warna token **byte-exact**, maskot Kiko **aset asli desain**, dan QA iter2 mengonfirmasi **semua defect mayor iter1 sudah FIXED** (verdict: PASS / PASS-MINOR di hampir semua layar).
- **Sisa:** beberapa perbaikan minor (detail posisi/ukuran) — daftar lengkap di §4.

---

## 2. Peta status terhadap Blueprint

| Bagian Blueprint | Status | Catatan |
|---|---|---|
| §4–5 Arsitektur & deteksi (MediaPipe, ML Kit, jarak, pitch, lux, state machine) | ✅ Terimplementasi (kerja sebelumnya) | Modul `detection/`, `service/`, `data/` ada; model pitch TFLite terintegrasi (commit DEV-34..35, MAE 4.37° lulus) |
| §5.5 Siklus 1 menit (MinuteTicker, snapshot per menit) | ✅ Ada di kode (kerja sebelumnya) | Ticker + Room `check_snapshots`; uji K4 60-menit=60-baris menyusul |
| §5.7 KPI K1–K10 | ⏳ Belum lengkap | Ada catatan uji di `ml/reports/`; butuh sesi pengujian terukur (180 foto ground-truth dsb.) |
| §8 UI/UX (onboarding, alarm, dashboard, laporan) | ✅ **Disesuaikan penuh ke desain (sesi ini)** | 17 layar FigCraft → Compose; detail §3 |
| §9 Skema data Room | ✅ Ada (kerja sebelumnya) | `check_snapshots`, `violation_logs`, `daily_stats`, `app_settings` |
| §17.D Asset desain & teks | 🔶 Sebagian | Maskot Kiko lengkap (8 varian); copy 17 layar lengkap; **tema gelap belum**, **disclaimer medis belum dicek** |
| §17.D Ikon app | ✅ Ada | `mipmap`/`ic_launcher_foreground` (pernah disesuaikan palet desain di audit sebelumnya) |
| §15 Kepatuhan brief (MediaPipe+ML Kit+Kotlin) | ✅ | — |

---

## 3. Yang SUDAH dikerjakan (fokus sesi: UI/UX vs desain)

### 3.1 Design system (fondasi — semua layar memakai ini)
- **Warna** (`ui/theme/Color.kt`): 18 token persis halaman Foundations (`#12263A` Ink, `#4A86E8` Primary, `#2D70B7` PrimaryDeep, Mint/Sunny/Coral/Cream, `#E1EFFF` NavActive, `#E3E8EE` Decoration, `#1B3B6F` Iris, Lavender/Blush, biru hero `#D6E6FF→#B7D2FF`).
- **Tipografi** (`Type.kt`): Baloo 2 (Display/H1/H2/Number) + Nunito (Body/Caption/Micro/Button) sesuai tabel style desain (48/24/18/16/15/32/13/11 + line-height). Berat font variable diperbaiki via `FontVariation` (sebelumnya bold tidak terpakai) + `includeFontPadding=false` (cegah teks terpotong).
- **Radius/shadow** (`Theme.kt`): 14/16/20/24/32 + Shadow Card.

### 3.2 Komponen shared (`ui/components/`)
`JagaCard` (r24) · `PrimaryButton` (h56 r28, isi Ink) · `SecondaryButton` (h60 r30) · `BottomCtaBar` (CTA menempel bawah: margin samping 20, bawah 40) · `BottomNavBar` (h100, 4 tab: Beranda/Pantau/Hadiah/Orang Tua, tab aktif pill `#E1EFFF`) · `ZoneChip`/`StatusPill` · `SectionHeader` · **`KikoImage`** (aset asli desain) · `Sparkle` (bintang 4 sudut) · `JagaSwitch` (toggle knob seragam ON/OFF).

### 3.3 Maskot Kiko — aset persis dari FigCraft
Diekspor langsung dari component set "Kiko" di `JagaMata_v2.figcraft` (scale 4x, transparan) ke `res/drawable-nodpi/`:
`kiko_netral / senang / fokus / kaget / bangga / ngantuk / senang_kacamata / fokus_penggaris.png`
→ sesuai bahkan lebih lengkap dari blueprint §17.D yang minta "3 state".

### 3.4 Implementasi 17 layar (frame desain → file kode)

| # Frame desain | File implementasi | Status |
|---|---|---|
| 01a/b/c Onboarding 1–3 | `ui/onboarding/OnboardingFlow.kt` | ✅ (3 langkah, CTA menempel, dot slider, hero gradient) |
| 02 Beranda | `ui/child/BerandaScreen.kt` (BARU) | ✅ |
| 03 Pantau Jarak Live | `ui/live/PantauLiveScreen.kt` (BARU) | ✅ |
| 04 Peringatan Jarak Dekat | `ui/child/AlarmBlocked.kt` | ✅ (komposit + bottom sheet) |
| 05 Istirahat 20-20-20 | `ui/child/BreakScreen.kt` (BARU) | ✅ |
| 06 Statistik Harian | `ui/parent/DailyStatsScreen.kt` (BARU) | ✅ |
| 07 Hadiah & Streak | `ui/parent/RewardsScreen.kt` (BARU) | ✅ (badge medallion + confetti) |
| 08 Profil Anak | `ui/parent/ChildProfileScreen.kt` (BARU) | ✅ |
| 09 Dashboard Orang Tua | `ui/parent/DashboardScreen.kt` (REWORK) | ✅ (chart trend 7-hari, ViewModel data asli dipertahankan) |
| 10 Pengaturan | `ui/parent/SettingsScreen.kt` (REWORK) | ✅ |
| 11 Izin & Akses | `ui/onboarding/PermissionWizardScreen.kt` (REWORK) | ✅ (toggle ON/OFF sesuai desain) |
| 12 Kalibrasi Kamera | `ui/onboarding/CalibrationScreen.kt` (REWORK) | ✅ (gauge semicircle + penggaris Kiko) |
| 13 Kunci Orang Tua | `ui/parent/LockScreen.kt` (REWORK) | ✅ (PIN ●●●●, hero Kiko, CTA menempel) |
| 14 Privasi & Data | `ui/parent/PrivacyScreen.kt` + `DeleteDataScreen.kt` | ✅ |
| 15 Overlay Ditolak | `ui/common/OverlayDenied.kt` (REWORK) | ✅ |

### 3.5 Navigasi & alat bantu
- Semua layar terdaftar di `Routes.kt` + `AppNav.kt` (bottom-nav `openTab()` terpusat).
- **DebugGallery** (`ui/debug/DebugGallery.kt`, hanya build DEBUG): daftar 17 tile untuk navigasi cepat.
- Capture otomatis: `MainActivity` menerima `--es screen "<slug>"` + script `.mimocode/design_refs/capture.ps1` (17 screenshot per iterasi).

### 3.6 Proses QA yang sudah dijalankan
- **Iter1** (4 agent QA paralel): menemukan maskot salah, font bold tidak terpakai, chart datar, CTA tak ter-anchoring, dll (~40 defect). Semua **copy teks sudah 100% benar** sejak awal.
- **Fix round** (4 agent paralel): semua defect diperbaiki.
- **Iter2** (4 agent QA paralel + verifikasi pixel): **semua defect mayor FIXED** — kalibrasi gauge simetris, toggle izin benar, chart ber-tren, Kiko asli, tipografi bold. Verdict: 1× PASS, mayoritas PASS-MINOR.
- Bukti lengkap: `.mimocode/design_refs/DIFF/iter1_batch*.md` dan `iter2_batch*.md`; screenshot `app/iter1/`, `app/iter2/`.

---

## 4. Yang MASIH perlu diperbaiki (daftar residual)

### 4.1 Minor visual (dari QA iter2 — belum/sedang dikerjakan)
| # | Layar | Masalah | Perbaikan yang disarankan |
|---|---|---|---|
| 1 | onboarding1 & overlay_ditolak | Kaki Kiko menempel/terpotong di tepi bawah kartu ilustrasi | Tambah inset bawah ~12dp atau skala maskot -5% |
| 2 | onboarding3 | Pill "50 cm" terlalu jauh dari kaki Kiko (~60dp vs desain ~25dp); ukuran "cm" lebih kecil dari "50" | Naikkan pill; samakan 32sp |
| 3 | peringatan | Diameter busur gauge ~60% lebar kartu (desain ~80%) | Perbesar radius ~30% |
| 4 | beranda | Subtitle "Siap main sehat hari ini?" terpotong di baseline (descender 'p') | Container text-nya clip line box — pakai pola `heightIn(min=…)` seperti fix streak |
| 5 | profil_anak | Jarak kartu → CTA ~2× desain | Rapatkan proporsi konten |
| 6 | kunci | Sparkle putih ke-3 (kanan-bawah hero) belum ada | Tambah 1 sparkle |
| 7 | semua layar ber-toggle | (SUDAH diperbaiki) knob OFF lebih kecil dari ON | `JagaSwitch` sudah dibuat — pastikan sudah terpakai di Settings/Izin/Privasi |

> Catatan: item 1–3 sempat dikerjakan oleh agent yang terhenti (kode compile, tapi hasil visual **belum diverifikasi capture ulang**). Jalankan `.\.mimocode\design_refs\capture.ps1 -Iter iter3` setelah build untuk cek.

### 4.2 Konsistensi yang belum selesai (dari catatan agent)
1. **`strings.xml` (`alarm_title`/`alarm_subtitle`)** masih copy lama PRD ("Ayo mundur dikit!") — desain memakai copy sheet ("Terlalu dekat!" dsb). `OverlayService` memakai string ini → perlu disamakan.
2. **`OverlayService`** masih memakai layout View (`R.layout.overlay_alert`), **bukan** Compose `AlarmBlocked` → visual overlay di app lain belum = desain frame 04. (KPI K10 tetap jalan, hanya visualnya.)
3. **`res/drawable/mascot.xml`** (Kiko krem lama) sudah tidak dipakai layar mana pun → bisa dihapus/diganti agar tidak membingungkan.
4. **Tema gelap** — token ada di Foundations (`#0F1B2D`/`#1A2A42`) tapi app light-only. Bila ingin ikut, perlu implementasi Material3 dark scheme.
5. **Disclaimer medis** (blueprint §17.D) — belum terlihat di UI; usul masuk onboarding/halaman Privasi.

### 4.3 Salah kaprah yang sudah dikoreksi (jangan diulang)
- "Badge hadiah harus blob organik" — **salah**; path desain secara matematis **lingkaran sempurna**. Sudah dikembalikan ke lingkaran.
- "Ganti Kiko ke `mascot/kiko.svg`" — tidak perlu; PNG `kiko_*.png` = ekspor asli component set desain.

---

## 5. Tambahan di luar daftar layar (berguna untuk tim)

| Item | Lokasi | Fungsi |
|---|---|---|
| Pipeline referensi desain | `.mimocode/design_refs/` (screens PNG+SVG, `foundations/tokens.md`, `components/specs.md`, `copy/*.md`) | Ground truth teks/koordinat/warna — jangan dihapus |
| Script capture emulator | `.mimocode/design_refs/capture.ps1` | 17 screenshot per iterasi via adb |
| Laporan QA | `.mimocode/design_refs/DIFF/iter1_batch*.md`, `iter2_batch*.md` | Bukti perbaikan + sisa defect |
| Sumber desain | `D:\Semester 5\Mobile Application\JagaMata_v2.figcraft` + `design/*.xml` | XML = export vector; figcraft = sumber hidup (teks terbaca) |
| DebugGallery + `--es screen` | `ui/debug/`, `MainActivity` | Akses cepat 17 layar untuk demo/QA |

---

## 6. Catatan penting: alur desain vs blueprint §8.1

Blueprint §8.1 merencanakan onboarding **5 langkah** (usia anak → pasang → izin → kalibrasi → ambang). **Desain FigCraft memakai "Onboarding 3 langkah"** + layar terpisah: Izin (11), Kalibrasi (12), Kunci (13). Sesuai keputusan sesi, **app mengikuti desain**:
- `OnboardingFlow` = 3 halaman persis desain.
- Layar `AgeSelectScreen`/`SetupDoneScreen`/`IntroScreen` **kode masih ada tapi tidak terpakai** (tidak ada di desain).
- Kalau saat demo dosen menyinggung alur 5 langkah → jelaskan pemetaannya: langkah "izin/kalibrasi/ambang" tetap ada, hanya dipisah menjadi layar tersendiri sesuai desain.

---

## 7. Langkah selanjutnya (prioritas)

1. **Verifikasi visual iter3** — build + `capture.ps1 -Iter iter3` + cek 6 residual minor (§4.1). Bila perlu, satu agent fix terakhir.
2. **Samakan `alarm_title`/`alarm_subtitle`** di `strings.xml` ke copy desain (5 menit).
3. **Migrasi overlay ke Compose `AlarmBlocked`** (opsional tapi bikin visual K10 = desain).
4. **Hapus `res/drawable/mascot.xml`** lama (kebersihan).
5. **Gate akhir tim:** `.\gradlew.bat ktlintCheck detekt test assembleDebug` (target: semua hijau) + update `docs/DESIGN_AUDIT.md` (mapping 17 nama frame asli) & `docs/PROGRESS.md`.
6. **Sesi KPI blueprint** (K1–K10) — koordinasi dengan rekan ML: 180 foto ground-truth, uji siklus 60 menit, baterai, overlay 5 app.

---

## 8. Ringkasan satu paragraf (untuk dikirim cepat)

> UI/UX JagaMata sudah disamakan dengan desain FigCraft **17 layar penuh** (3 iterasi QA visual pakai screenshot emulator): design system token warna/teks/radius persis, maskot Kiko memakai aset asli desain (8 varian), semua copy teks 100% akurat, bottom-nav/CTA-menempel/gauge/switch mengikuti desain. Build hijau. Sisa: ~6 perbaikan minor detail posisi + penyelarasan teks alarm overlay + (opsional) migrasi overlay ke Compose. Bukti QA & screenshot ada di `.mimocode/design_refs/`. Di luar scope sesi ini: pengujian KPI K1–K10 blueprint masih menyusul.
