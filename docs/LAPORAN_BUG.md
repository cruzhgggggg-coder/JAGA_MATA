# Laporan Stress Test & Bug Hunt — JagaMata

**Tanggal:** 27 November 2026
**Metode:** (1) audit kode paralel 4 domain (UI/nav, service/deteksi, data/keamanan, fitur baru hasil merge tim) · (2) static analysis penuh (Android Lint + ktlint + detekt + unit test) · (3) runtime stress di emulator: 17 layar × rotasi + rapid navigation + spam CTA + lifecycle home/resume.
**Hasil runtime:** **0 crash** — app selamat dari seluruh skenario stress (logcat hanya berisi noise sistem emulator, tanpa `FATAL EXCEPTION`).
**Hasil audit:** **40 temuan unik** — 1 kritis, 13 tinggi, 19 sedang, 7 rendah. Terperinci di bawah.

> **UPDATE STATUS (27 Nov 2026):** Bug **B1–B15 (kritis + tinggi) sudah DIPERBAIKI** — 3 agent fix paralel + verifikasi (`compileDebugKotlin` hijau, `testDebugUnitTest` lulus, smoke test 5 layar di emulator tanpa crash). Catatan: fix B5 sempat membuat 16 unit test gagal (WorkManager belum ter-init di Robolectric saat `JagaMataApp.onCreate`) — sudah diperbaiki dengan guard `WorkManager.isInitialized()`. Bug **B16–B41 (sedang + rendah) belum diperbaiki** — daftar backlog di bawah.

---

## 🔴 KRITIS (1) — ✅ SUDAH DIPERBAIKI

### B1. "Hapus Semua Data" tidak menghapus apa pun — tapi UI melaporkan sukses
- **Lokasi:** `ui/navigation/AppNav.kt` (pemanggilan `DeleteDataScreen(onDeleteAll = {})`) + `ui/parent/PrivacyScreen.kt` (`onDeleteData: () -> Unit = {}` default) + `DeleteDataScreen.kt:159`
- **Repro:** Pengaturan/Privasi → Hapus Semua Data → konfirmasi → centang sukses muncul → **Room + DataStore + PIN tetap ada**. Selain itu baris "Hapus data" di PrivacyScreen **tidak menavigasi ke mana pun** (route `DELETE_DATA` tak pernah dipanggil).
- **Dampak:** Melanggar US-14 & klaim privasi (UU PDP) — pengguna percaya data terhapus.
- **Fix:** (a) `onDeleteData = { navController.navigate(Routes.DELETE_DATA) }`; (b) isi `onDeleteAll` dengan `clearAllTables()` + `SettingsRepository.clearAll()` (termasuk tabel `app_settings` yang belum punya DAO) sebelum menampilkan status sukses.

---

## 🟠 TINGGI (13) — ✅ SUDAH DIPERBAIKI

### B2. PIN default "1234" dipajang di layar kunci + tanpa batas percobaan
- **Lokasi:** `ui/parent/LockScreen.kt:84-90,174` · `data/SettingsRepository.kt:71`
- **Repro:** Lihat subtitle layar kunci: "(Default: 1234)" — anak bisa membuka mode orang tua. Salah PIN bisa dicoba tanpa batas (keyspace 4 digit → brute-force hitungan menit).
- **Fix:** hapus petunjuk default; wajibkan buat PIN saat onboarding; tambah counter percobaan + lockout bertingkat.

### B3. Ubah PIN tanpa verifikasi PIN lama
- **Lokasi:** `ui/parent/SettingsScreen.kt:492-563` (ChangePinDialog)
- **Repro:** Setelah unlock, "Ubah PIN Orang Tua" langsung menerima PIN baru tanpa memasukkan PIN lama; juga menerima `1234`/PIN sama.
- **Fix:** wajibkan field PIN lama; tolak PIN lemah / sama dengan yang lama.

### B4. Penulisan salt+hash PIN tidak atomik → bisa terkunci permanen
- **Lokasi:** `data/SettingsRepository.kt:63-66`
- **Repro:** Crash/proses mati di antara 2 `write()` (salt lalu hash) → salt baru + hash lama → `verifyParentPin` selalu gagal, tidak ada jalan keluar.
- **Fix:** satu `dataStore.edit { put keduanya }`.

### B5. `DailyRollupWorker.schedule()` tidak pernah dipanggil — statistik harian mati
- **Lokasi:** `data/DailyRollupWorker.kt:64` (definisi tanpa call site)
- **Dampak:** rollup `daily_stats` + prune 30 hari tidak pernah jalan → laporan mingguan nyaris kosong selamanya, DB tumbuh tanpa batas.
- **Fix:** panggil `DailyRollupWorker.schedule(context)` di `JagaMataApp.onCreate()`.

### B6. Rollup salah hari — data hari ini (5 menit) ditulis sebagai "hari ini", hari kemarin tak pernah diagregasi
- **Lokasi:** `DailyRollupWorker.kt:33-50`
- **Repro:** Worker jalan ~00:05 → `dayStart` = tengah malam *hari ini* → hanya ~5 menit data yang diagregasi; full-day kemarin hilang.
- **Fix:** agregasi jendela `[kemarin 00:00, hari ini 00:00)` dan stempel hari = kemarin.

### B7. Cooldown auto-dismiss overlay 8 detik menekan alarm BLOCKED (celah keselamatan)
- **Lokasi:** `service/OverlayService.kt:75,176-184`
- **Repro:** Overlay WARNING auto-dismiss (5 dtk) → `userDismissedUntilMs = +8 dtk` → selama 8 dtk `applyLevel` return duluan → anak yang makin dekat (<25 cm) **tidak melihat alarm full-screen**.
- **Fix:** cooldown hanya untuk dismiss manual; jangan pernah menahan level BLOCKED.

### B8. `onStartCommand` tidak idempoten → ticker ganda + leak FaceLandmarker
- **Lokasi:** `service/DetectionForegroundService.kt:105-116`; pemicu: `AppNav.kt:125/147/193`, `PantauLiveScreen.kt:137`
- **Repro:** CTA Beranda → Pantau Live cepat → 2 intent → 2 `MinuteTicker` (job lama tak dibatalkan) + 2 baris snapshot/menit (**KPI K4 korup**) + native landmarker lama tak ditutup.
- **Fix:** guard idempoten di `onStartCommand`; close/cancel sebelum re-init.

### B9. Izin kamera dicabut saat jalan → crash-loop ForegroundService (F5)
- **Lokasi:** `DetectionForegroundService.kt:119-137` + `AndroidManifest.xml` (`foregroundServiceType="camera"`, targetSdk 35)
- **Repro:** Cabut izin kamera → service restart (STICKY) → `startForeground` type kamera tanpa izin → exception tertelan → `ForegroundServiceDidNotStartInTimeException` ~5 dtk.
- **Fix:** cek izin dulu; jika tidak ada → `stopSelf()` + notifikasi "butuh izin kamera" (wizard ulang).

### B10. Satu kegagalan Room mematikan logging siklus selamanya
- **Lokasi:** `DetectionForegroundService.kt:227` (`persistCycle`), `MinuteTicker.kt:70-82`
- **Repro:** `SQLiteFullException`/insert gagal sekali → exception keluar dari `scope.launch` → ticker mati diam-diam, tak ada snapshot/notifikasi sampai restart manual.
- **Fix:** bungkus `persistCycle`/`onCycle` dengan `try/catch` + log.

### B11. `DashboardViewModel()` dibuat sebagai default param — bocor & hilang saat rotasi
- **Lokasi:** `ui/parent/DashboardScreen.kt:90` (+ `DashboardViewModel.kt:36-52`)
- **Repro:** Buka Dashboard, rotasi 5× → 5 ViewModel yatim (masing-masing 3 collector permanen di flow companion service); state reset.
- **Fix:** pakai `androidx.lifecycle.viewmodel.compose.viewModel()` (pola yang sudah benar di CalibrationScreen).

### B12. Grafik mingguan selalu kosong + garis ambang hardcoded 50 cm
- **Lokasi:** `DashboardScreen.kt:396-403,409,482,509`
- **Repro:** `chartValues = listOf(dist)` (≤1 titik) → chart digambar hanya jika `n > 1`, empty-state hanya jika `values.isEmpty()` → area chart **selalu kosong**. Garis "Ambang" tetap `50f` meski pengaturan baru 20–50 cm bisa diubah.
- **Fix:** petakan `state.dailyStats` ke 7 nilai harian; treat `n < 2` = empty state; kirim `minDistanceFloat` ke chart.

### B13. Laporan PDF memakai data contoh palsu saat data kosong
- **Lokasi:** `ui/parent/WeeklyReportScreen.kt:88-95`
- **Repro:** `daily_stats` kosong → 3 baris hardcoded (15/22/30 siklus…) dirender sebagai "Laporan Kesehatan Mata" dan bisa dibagikan (dengan nama & usia anak asli) ke dokter.
- **Fix:** blokir export / tampilkan "belum ada data", jangan pernah mengarang angka.

### B14. `allowBackup=true` tanpa aturan backup → hash PIN & DB anak bisa diekstrak
- **Lokasi:** `AndroidManifest.xml:22`
- **Dampak:** `adb backup`/cloud/D2D mentransfer `jagamata_settings` (salt+hash PIN) + seluruh DB riwayat anak (sensitif, terkait COPPA/UU PDP).
- **Fix:** `allowBackup="false"` atau exclude via `dataExtractionRules`; idealnya PIN di EncryptedSharedPreferences/Keystore.

### B15. Crash API-level: `Vibrator#vibrate(VibrationEffect)` butuh API 26, minSdk 24
- **Lokasi:** `service/OverlayService.kt:206-207` (error Lint `NewApi`)
- **Repro:** Android 7.0/7.1 (API 24/25) → `NoSuchMethodError` saat alarm bergetar.
- **Fix:** cabang `Build.VERSION.SDK_INT >= 26` (VibrationEffect) vs `vibrate(long)` lama.

---

## 🟡 SEDANG (19) — ⏳ BACKLOG

| # | Lokasi | Masalah | Fix singkat |
|---|---|---|---|
| B16 | `SettingsScreen.kt:415-416` | Toggle "Mode Gelap"/"Notifikasi" dummy — state hilang saat kembali, tema gelap tak ada | Persist + dark scheme, atau hapus toggle |
| B17 | `PermissionWizardScreen.kt:73-75,121-123` | Status izin overlay stale setelah balik dari Settings | Re-check `canDrawOverlays` di ON_RESUME |
| B18 | `AppNav.kt` (navigate umum) | Tidak ada `launchSingleTop` selain `openTab` → double-tap CTA menumpuk 2 layar (back 2×) | Tambah `launchSingleTop = true` |
| B19 | `AppNav.kt:179` | Back di layar Izin saat onboarding = dead-end (popBackStack gagal tanpa fallback) | Tambah fallback `navigate(Routes.BERANDA)` seperti KALIBRASI |
| B20 | `BerandaScreen.kt:520-543` | Kolom Beranda tidak scrollable — font besar → CTA "Mulai Pantau" tak terjangkau (tertutup BottomNav) | Tambah `verticalScroll` |
| B21 | `ChildProfileScreen.kt:108,207` | `remember` basi menimpa threshold tersimpan (45 cm → 30 cm) saat simpan edit nama | Key `remember(closeThresholdCm)` / reset di LaunchedEffect |
| B22 | `SettingsRepository.kt` + UI 3 tempat | Ambang jarak punya 3 salinan lokal independen (Settings/Dashboard/Profil) — risiko tidak konsisten | Sumber tunggal + sinkronisasi |
| B23 | `CalibrationViewModel.kt:93-94` | Progres kalibrasi pakai wall-clock → lompat 100% padahal 1 sampel (wajah tertutup 5 dtk) | Progres dari rentang `recentSamples` |
| B24 | `DetectionForegroundService.kt:281` + `PantauLiveScreen.kt:110-128` | "Persistent timer" reset ke 0 tiap pause/restart service | Persist `sessionStartEpochMs` ke DataStore + StateFlow |
| B25 | fitur Batas Waktu Main | `dailyLimitMinutes` disimpan & ditampilkan, **tidak pernah dipakai** oleh service/timer | Enforce di detection/overlay |
| B26 | `overlay_warning.xml:27` | Teks banner hardcoded "minimal 30 cm" — bohong setelah ambang jadi bisa 20–50 cm | Bind teks dari ambang aktif |
| B27 | `DetectionStateMachine.kt:157-170` | WARNING lengket saat wajah hilang (`distance == null`) — banner tak pernah kembali OK | Exit OK saat wajah hilang >N dtk |
| B28 | `CycleAggregator.kt:9-12` | `mutableListOf` lintas-thread (tambah vs evaluate) → CME / sampel hilang | `synchronized` / CopyOnWrite |
| B29 | `NotificationHelper.kt:30,96-98` | 1 timestamp throttle dipakai bersama → notifikasi biasa menelan alarm krusial (dan sebaliknya) | Pisah `lastCrucialAtMs`/`lastNormalAtMs` |
| B30 | `OverlayService.kt:235` | `requestLevel` spam `startService` ~5×/detik saat WARNING (baterai) | Dedupe `if (prev == level) return` |
| B31 | `FaceDetectorManager.kt:99-129` | F1 fallback: landmarker/MLKit lama tidak ditutup (leak), `activeBackend` non-volatile | Close/null resource lama; `@Volatile` |
| B32 | `DistanceEstimator.kt:32-42,56-70` | NaN pitch lolos `coerceIn` → EMA jarak teracuni NaN selamanya (pelanggaran jarak mati) | Return null bila `!isFinite()`; pakai/buang `faceWidthPx` |
| B33 | `AppDatabase.kt:47` | `fallbackToDestructiveMigration()` — update skema = hapus semua data anak diam-diam | Migration sungguhan + `exportSchema=true` |
| B34 | `app/build.gradle.kts:30` | `abiFilters` tanpa `x86_64` (emulator/ChromeOS gagal load native lib) + `useLegacyPackaging` global + CPU delegate untuk semua perangkat (regresi baterai non-HyperOS) | Tambah x86_64 utk debug; gate CPU delegate by Build |

---

## 🟢 RENDAH (7) — ⏳ BACKLOG

| # | Lokasi | Masalah |
|---|---|---|
| B35 | `ReportGenerator.kt:284-287` | Label hari PDF selalu "Senin…" (dari indeks, bukan tanggal) |
| B36 | `WeeklyReportScreen.kt:301-326` | Render PDF/PNG + I/O di main thread (ANR); nama file cache tetap (race/overwrite); `ACTION_SEND` tanpa `ClipData` |
| B37 | `MainActivity.kt` | Tanpa `FLAG_SECURE` — PIN/statistik bisa di-screenshot & muncul di Recents |
| B38 | `BerandaScreen.kt:511`, `RewardsScreen.kt:107,385` | "Streak" = total hari aktif (bukan beruntun); week row bisa >7 lingkaran |
| B39 | `RewardsScreen.kt:118-119` | Progress siklus reset di kelipatan tepat 10 (`% 10 == 0`) |
| B40 | `LockScreen.kt:84-88` | Jeda 600 ms reset PIN menelan input keypad |
| B41 | `FaceDetectorManager.kt:324` | `imageProxy.image` tanpa `@OptIn(ExperimentalGetImage)` (error Lint) |

---

## 📋 Static analysis (ringkas)

| Pemeriksaan | Hasil |
|---|---|
| Unit test | ✅ lulus |
| detekt | ✅ lulus |
| ktlint | ❌ 9 pelanggaran style (import-ordering ×7, unused-import, multiline-wrapping) — file: `AppNav, DashboardScreen, SettingsScreen, BerandaScreen, ChildProfileScreen, DailyStatsScreen` |
| Android Lint | ❌ **3 error** (B15, B41) + **194 warning**: UnusedResources ×105, GradleDependency ×66 (dependensi usang — perlu di-upgrade), HardcodedText ×2, InflateParams ×2, ObsoleteSdkInt ×2, MonochromeLauncherIcon ×2, LockedOrientationActivity, Overdraw ×2, dll. |

## 📋 Runtime stress (emulator API 37)

| Skenario | Hasil |
|---|---|
| Jalan 17 layar + rotasi landscape/portrait | ✅ 0 crash |
| Rapid navigation bolak-balik 8× | ✅ 0 crash |
| Spam CTA onboarding 6× cepat | ✅ 0 crash |
| Lifecycle HOME→resume 5× | ✅ 0 crash |
| Logcat `FATAL EXCEPTION` | **0** (230 baris `E/` = noise `WindowOrganizerController`/`InputDispatcher` sistem emulator akibat force-stop berulang) |

## ✅ Area yang diperiksa dan bersih

Referensi `R.string`/`R.drawable`/`R.font` semua resolve (tak ada sisa `mascot.xml`); matematika canvas chart aman dari div-zero; verifikasi PIN async cancellation-safe; logika hash+salt PIN benar; DebugGallery ter-gate `BuildConfig.DEBUG`; share PDF/PNG via FileProvider path cocok; `!!` semua null-checked; format-arg strings cocok dengan call site (tanpa crash format); bounds input dialog ambang aman (20–50, ±5).

---

## 🎯 Rekomendasi perbaikan (urutan)

1. **Segera (demo/privasi):** B1 (hapus data), B2+B3+B4 (keamanan PIN), B5+B6 (rollup — laporan mingguan), B13 (data palsu di PDF), B15 (crash Android 7).
2. **Sebelum rilis:** B7 (celah alarm), B8+B9+B10 (ketahanan service), B11+B12 (dashboard), B14 (backup), B21 (timpa threshold).
3. **Sprint berikutnya:** item sedang (konsistensi ambang, timer persisten, dark mode, thread-safety) + upgrade dependensi (66 warning GradleDependency) + bereskan ktlint.
4. **Catatan jujur:** stub `onDeleteAll = {}` di `AppNav` berasal dari wiring DebugGallery awal (placeholder) yang lolos ke merge — sekarang sudah terdeteksi sebagai B1 dan wajib diisi.
