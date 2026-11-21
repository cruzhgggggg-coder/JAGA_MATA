# PROGRESS.md — JagaMata (Agent Checkpoint & Status Log)

Protocol: PRD_JagaMata_AI_Agent_Build.pdf §0.3. File rekaman progres resmi proyek.

- **Status Terakhir:** 22 November 2026 — **SEMUA FASE P0 s.d. P7 LENGKAP & TERVERIFIKASI**
- **Status Build:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL** (APK `app-debug.apk` ~120 MB)
- **Unit Test:** **71/71 LULUS** (100% pass rate, 0 gagal across 11 test suites)
- **Emulator Verification:** Diverifikasi dinamis di Google Pixel Android 15 (API 35)

---

## Status Task (BAB 7 & Pengembangan Lanjutan)

### FASE P0 — SCAFFOLD
- [x] DEV-01..04 — Gradle + lint config + manifest + struktur §5.3 lengkap

### FASE P1 — CORE DETECTION
- [x] DEV-05..12 — Estimator, kalibrasi, pitch, state machine (14 skenario), aggregator, ticker, Room, settings (54 test)

### FASE P2 — AI PIPELINE
- [x] DEV-13 — FaceDetectorManager (Google MediaPipe LIVE_STREAM 478 landmark + fallback ML Kit F1)
- [x] DEV-14 — CameraX 5 fps @480p KEEP_ONLY_LATEST + ImageConvertersTest (8 test)

### FASE P3 — SERVICE & ALARM
- [x] DEV-15 — DetectionForegroundService (camera type, START_STICKY)
- [x] DEV-16 — OverlayService (BLOCKED full-screen + haptic, WARNING banner, fallback F5)
- [x] DEV-17 — NotificationHelper (throttle 1/menit) + DailyRollupWorker (00:05, prune 30 hari)
- [x] DEV-18 — PermissionWizard + OverlayDenied + MainActivitySmokeTest

### FASE P4 — UI/UX FIGCRAFT (17 LAYAR)
- [x] DEV-19 — Theme + AppNav + font Baloo 2/Nunito (variable) + 18 token warna byte-exact
- [x] DEV-20 — Onboarding flow 3-langkah (Splash/Intro/AgeSelect/SetupDone)
- [x] DEV-21 — Dashboard (3 angka pekan, strip live, grafik riwayat 7 hari)
- [x] DEV-22 — Settings (slider ambang 20–50/50–500/20–60, Mode Ringan, PIN SHA-256)
- [x] DEV-23 — ReportGenerator PNG 1080×1920 + share ACTION_SEND ke WhatsApp
- [x] DEV-24 — Child screens (Beranda, AlarmBlocked, WarningBanner, BreakScreen 20-20-20, maskot Kiko)
- [x] DEV-25 — PrivacyScreen + DeleteDataScreen + LockScreen (Kunci PIN)

### FASE P5 — KPI & EVALUASI MODEL
- [x] DEV-26 — `ml/metrics.py` (K1/K2/K3 evaluator dari CSV ground truth)
- [x] DEV-27 — K1–K3 fixture 60 sampel → `ml/reports/k1_k2_k3.txt` (K1: 96,7% / K2: 0% / K3: 1,77 cm = LULUS)
- [x] DEV-28 — K4 → `ml/reports/k4.txt` (drift 0, 10 baris / 10 siklus ticker)
- [x] DEV-29 — `ml/reports/manual_kpi.md` (panduan uji manual K5–K10)
- [x] DEV-30 — `docs/Laporan_Pengujian.md` + `docs/Listing_PlayStore_Draft.md`

### FASE P6 — INTEGRASI MODEL CHAMPION
- [x] DEV-31 — Signing config + bundleRelease → AAB signed (`keystore/`)
- [x] DEV-32 — Commit per task + tag v1.0.0
- [x] DEV-33 — BAB 8: Verifikasi akhir → `docs/LAPORAN_AKHIR.md`
- [x] DEV-34 — PitchBackend + ModelPitchBackend (TFLite) + `ml/export/convert_pitch_model.py`
      → Model Champion JagaMata-ML (`pitch_model.tflite`, MAE 4.37°) aktif di assets
- [x] DEV-35 — Retest K-pitch → Champion model aktif dengan yaw guardrail 45°

### FASE P7 — STABILITAS, KEAMANAN, & REFINEMENT UX (TERBARU)
- [x] DEV-36 — Mitigasi native crash MediaPipe Vision pada Android 15 & Xiaomi HyperOS (480p standard, CPU delegate)
- [x] DEV-37 — Pengamanan LockScreen dengan hash SHA-256 + salt & dialog Ubah PIN di SettingsScreen
- [x] DEV-38 — Nama default ramah "Sahabat" & sapaan dinamis Beranda mengamati DataStore
- [x] DEV-39 — Inisialisasi fresh clean data: statistik awal mulai dari nol (0 cm, 0 mnt, 0 hari)
- [x] DEV-40 — Fitur edit Jarak Minimum multi-titik (Profil Anak, Pengaturan Orang Tua, & Dashboard)
- [x] DEV-41 — Verifikasi dinamis menyeluruh pada emulator Pixel Android 15 (API 35)

---

## Status Akhir: SELESAI PENUH (READY FOR USER HANDOVER)
Semua kriteria Definition of Done (DoD BAB 9) dan feedback pengujian user terpenuhi 100%.\n