# PROGRESS.md — JagaMata (agent checkpoint)

Protocol: PRD_JagaMata_AI_Agent_Build.pdf §0.3. File ini satu-sandinya memori antar sesi.

- Session: MiMoCode · mulai 2026-07-07
- Environment: Windows 11 · JDK 17.0.20.1 ✅ · Android SDK `C:/Users/Zaky/AppData/Local/Android/Sdk` (platform android-37.0, build-tools 36.0.0) · Gradle wrapper 8.9 + AGP 8.7.3 · Python via `uv` ✅
- `JagaMata_v2.figcraft` TIDAK ada → referensi visual = `design/referensi/*.png`. Token & angka teknis PRD BAB 2 MENANG.
- Build terakhir: `compileDebugKotlin` BUILD SUCCESSFUL · `ktlintCheck` ✅ · `detekt` ✅

## Status task (BAB 7)

FASE P0 — SCAFFOLD
- [x] DEV-01 Gradle project (com.jagamata.app, minSdk 24, targetSdk 35) + wrapper — `assembleDebug` BUILD SUCCESSFUL
- [x] DEV-02 libs.versions.toml + ktlint/detekt — `ktlintCheck` 0 error, `detekt` 0 issue
- [x] DEV-03 Manifest + permissions + 2 service + themes/colors/strings ID — assembleDebug lulus
- [x] DEV-04 Scaffold semua file §5.3 — compileDebugKotlin lulus (detection/, service/, data/, report/, ui/ lengkap)

FASE P1 — CORE DETECTION
- [x] DEV-05 DistanceEstimator + CalibrationManager + test (6 + 5 test lulus)
- [x] DEV-06 HeadPitchAnalyzer + PitchBackend + fixture test (4 test 10°/40°)
- [x] DEV-07 LightMonitor (Sensor.TYPE_LIGHT, handle null sensor)
- [x] DEV-08 DetectionResult + DetectionStateMachine + 14 skenario test
- [x] DEV-09 CycleAggregator + 6 test multi-sample & violations
- [x] DEV-10 MinuteTicker + 5 test interval 100 ms (K4 → ml/reports/k4.txt)
- [x] DEV-11 Room 4 entity + 3 DAO + SnapshotDaoTest (insert/query/prune 30 hari)
- [x] DEV-12 SettingsRepository (DataStore) + SHA-256 PIN + PitchBackendFactoryTest

(FASE P2–P6 — lihat PRD BAB 7; belum dimulai)

## ASSUMPTION LOG (keputusan agent)

1. `JagaMata_v2.figcraft` tidak ada → pakai PNG di `design/referensi/` (PRD §0.2.b). Layout UI mengikuti struktur fungsional US-01..US-15 + token §2.22.
2. compileSdk 37 (platform terpasang) + targetSdk 35 (angka PRD tetap).
3. Git identity per-commit (`git -c user.name/-c user.email`) — config user tidak diubah.
4. **Zona jarak vs state machine**: syarat keluar BLOCKED memakai §5.4 (>35 cm / 2 dtk); syarat state OK memakai zona Aman ≥50 cm (§2.23) supaya banner WARNING benar-benar tampil di 30–50 cm. Semua angka PRD terpakai.
5. Wajah hilang (distance null) ≥2 dtk diperlakukan seperti keluar dari BLOCKED (analog "keluar >35 cm selama 2 dtk") agar overlay tidak macet.
6. Pitch fallback ML Kit = null (degraded mode) — pitch butuh 478 landmark MediaPipe. Jarak + cahaya tetap jalan.
7. Font Baloo 2/Nunito belum dipasang (fallback sistem) — menyusul di DEV-19 bila unduhan berhasil.
8. Model `face_landmarker.task` belum ada → `assets/MODEL_README.txt` (URL unduh) + kode fallback ML Kit (F1).

## Bukti verifikasi

- `.\gradlew.bat assembleDebug` → BUILD SUCCESSFUL (DEV-01, DEV-03)
- `.\gradlew.bat ktlintCheck` → exit 0 (DEV-02)
- `.\gradlew.bat detekt` → exit 0 (DEV-02)
- `.\gradlew.bat compileDebugKotlin` → BUILD SUCCESSFUL (DEV-04)
- `.\gradlew.bat testDebugUnitTest` → **54 test lulus, 0 gagal** (DEV-05..12)
- `ml/reports/k4.txt` → bukti K4 (10 siklus × 100 ms = tepat 10 baris, drift 0)

## Log error / fallback

- `local.properties` belum ada di awal → SDK location not found → dibuat (sdk.dir).
- `gradle.properties` belum ada → `android.useAndroidX` tidak aktif → dibuat.
- `NotificationHelper` kehilangan import `android.app.Notification` → diperbaiki.
- Detekt `MatchingDeclarationName` pada AppNav.kt → `Routes` dipindah ke file sendiri.
