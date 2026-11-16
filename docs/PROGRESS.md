# PROGRESS.md — JagaMata (agent checkpoint)

Protocol: PRD_JagaMata_AI_Agent_Build.pdf §0.3. File ini satu-satunya memori antar sesi.

- Session: MiMoCode · mulai 2026-07-07
- Environment: Windows 11 · JDK 17.0.20.1 ✅ · Android SDK `C:/Users/Zaky/AppData/Local/Android/Sdk` (platform android-37.0, build-tools 36.0.0) · Gradle wrapper 8.9 + AGP 8.7.3 · Python via `uv` ✅
- Gate terakhir: `clean testDebugUnitTest ktlintCheck detekt assembleDebug` → **BUILD SUCCESSFUL** · 62 test lulus · APK `app/build/outputs/apk/debug/app-debug.apk`

## Status task (BAB 7)

FASE P0 — SCAFFOLD
- [x] DEV-01..04 — Gradle + lint config + manifest + struktur §5.3 lengkap

FASE P1 — CORE DETECTION
- [x] DEV-05..12 — estimator, kalibrasi, pitch, state machine (14 skenario), aggregator, ticker, Room, settings · 54 test

FASE P2 — AI PIPELINE
- [x] DEV-13 FaceDetectorManager (MediaPipe LIVE_STREAM + fallback ML Kit F1)
- [x] DEV-14 CameraX 5 fps @480p KEEP_ONLY_LATEST + ImageConvertersTest (8 test)

FASE P3 — SERVICE & ALARM
- [x] DEV-15 DetectionForegroundService (camera type, START_STICKY)
- [x] DEV-16 OverlayService (BLOCKED full-screen + haptic, WARNING banner, fallback F5)
- [x] DEV-17 NotificationHelper (throttle 1/menit) + DailyRollupWorker (00:05, prune 30 hari)
- [x] DEV-18 PermissionWizard + OverlayDenied + MainActivitySmokeTest

FASE P4 — UI
- [x] DEV-19 Theme + AppNav + font Baloo 2/Nunito (variable, dari google/fonts)
- [x] DEV-20 Onboarding flow (Splash/Intro/AgeSelect/Permission/Calibration/SetupDone)
- [x] DEV-21 Dashboard (3 angka pekan, strip live, grafik 7 hari)
- [x] DEV-22 Settings (slider ambang 20–50/50–500/20–60, Mode Ringan, PIN SHA-256)
- [x] DEV-23 ReportGenerator PNG 1080×1920 + share ACTION_SEND
- [x] DEV-24 Child screens (AlarmBlocked, WarningBanner, SafeBanner, mascot)
- [x] DEV-25 Privacy + DeleteData + LockScreen

FASE P5 — KPI & LAPORAN
- [x] DEV-26 ml/metrics.py (K1/K2/K3 dari CSV)
- [x] DEV-27 K1–K3 fixture 60 sampel → ml/reports/k1_k2_k3.txt (96,7% / 0% / 1,77 cm = LULUS)
- [x] DEV-28 K4 → ml/reports/k4.txt (drift 0, 10 baris / 10 siklus)
- [x] DEV-29 ml/reports/manual_kpi.md (K5–K10 + D4–D7)
- [x] DEV-30 docs/Laporan_Pengujian.md + Listing_PlayStore_Draft.md

FASE P6 — BUILD AKHIR
- [ ] DEV-31 Signing config + bundleRelease
- [ ] DEV-32 Commit + tag v1.0.0 + README final
- [ ] DEV-33 BAB 8: verifikasi akhir → docs/LAPORAN_AKHIR.md
- [x] DEV-34 PitchBackend + PitchBackendFactory + ml/export/convert_pitch_model.py (model belum ada → fallback Landmark, F9)
- [ ] DEV-35 Retest K-pitch (menunggu model rekan tim)

## ASSUMPTION LOG
Lihat `ml/reports/assumptions.md` (16 butir). Inti: figcraft tidak ada → PNG referensi;
zona 50/30 cm vs OK >35 cm direkonsiliasi (semua angka terpakai); wajah hilang 2 dtk
= keluar alarm; pitch ML Kit = null (degraded); model belum ada → MODEL_README.txt.

## Bukti verifikasi
- `./gradlew.bat clean testDebugUnitTest ktlintCheck detekt assembleDebug` → exit 0
- **62 unit test lulus, 0 gagal** (10 kelas test)
- `ml/reports/k1_k2_k3.txt` → K1 96,7% · K2 0% · K3 1,77 cm (semua LULUS)
- `ml/reports/k4.txt` → K4 drift 0
- APK debug terbentuk: `app/build/outputs/apk/debug/app-debug.apk`

## Log error / fallback
(local.properties / gradle.properties awal, import Notification, detekt MatchingDeclarationName,
nama test dengan karakter ilegal, Robolectric sdk 34, TestScope terpisah, FontVariation API —
semua sudah diperbaiki; detail di riwayat commit)
