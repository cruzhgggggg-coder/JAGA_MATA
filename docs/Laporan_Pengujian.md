# Laporan Pengujian — JagaMata v1.0.0

PRD BAB 4 & BAB 8 · disusun agent (MiMoCode) · terakhir diperbarui: sesi build

## Metode

| Lapis | Metode | Perintah |
|---|---|---|
| Logika deteksi | Unit test JUnit4 (deterministik, virtual clock) | `./gradlew.bat testDebugUnitTest` |
| Persistensi | Robolectric + Room in-memory | idem |
| Analisis statis | ktlint + detekt, ambang 0 isu | `./gradlew.bat ktlintCheck detekt` |
| Build | assembleDebug / bundleRelease | `./gradlew.bat assembleDebug` |
| KPI K1–K3 | 60 fixture sintetis + `ml/metrics.py` | lihat `ml/reports/k1_k2_k3.txt` |
| KPI K4 | ticker virtual 100 ms × 10 siklus | lihat `ml/reports/k4.txt` |
| KPI K5–K10 | manual di HP fisik | instruksi `ml/reports/manual_kpi.md` |

## Hasil otomatis

- **Unit test: 71 lulus / 0 gagal** (11 kelas test)
  - `DetectionStateMachineTest` 14 skenario (flapping, cepat-lewat, null face,
    hysteresis 35 cm, debounce 500 ms / 3 s / 10 s, prioritas)
  - `ModelPitchBackendTest` 9 skenario (TFLite loading, toPixelBox scale, cropFace clamp & min-size, requiresFaceCrop, backend factory, yaw guardrail 45°)
  - `ImageConvertersTest` 8 (pipeline konversi, rotasi, bounding box, verifikasi model MediaPipe di assets)
  - `DistanceEstimatorTest` 6 (rumus `d = H×f_px/h_px`, EMA, null handling, MAE)
  - `CalibrationManagerTest` 5 (`f_px = h_px×30/15`, stabilitas 1,5 s)
  - `CycleAggregatorTest` 6 (min/avg/max, violations, clear)
  - `MinuteTickerTest` 5 (K4: 10 siklus → 10 baris, drift 0)
  - `SnapshotDaoTest` 5 (insert/query/prune 30 hari)
  - `PitchBackendFactoryTest` 5 (pemilihan backend F9)
  - `HeadPitchAnalyzerTest` 4 (fixture 10°/40°)
  - `ReportGeneratorTest` 4 (ringkasan 7 hari)
- **ktlintCheck**: exit 0
- **detekt**: 0 issue
- **assembleDebug**: BUILD SUCCESSFUL (APK 120 MB)

## Tabel riwayat KPI (D6)

| KPI | Target | Hasil otomatis | Bukti | Status |
|---|---|---|---|---|
| K1 Sensitivitas <30 cm | ≥90 % | **96,7 %** (29/30) | `ml/reports/k1_k2_k3.txt` | ✅ otomatis |
| K2 FPR ≥35 cm | ≤10 % | **0,0 %** (0/30) | `ml/reports/k1_k2_k3.txt` | ✅ otomatis |
| K3 MAE jarak | ≤6 cm | **1,77 cm** | `ml/reports/k1_k2_k3.txt` | ✅ otomatis |
| K4 Siklus 60 dtk ±2 dtk | ±2 dtk | **drift 0** (10 siklus uji) | `ml/reports/k4.txt` | ✅ otomatis |
| K5 Latensi alarm | ≤1,5 dtk | uji state machine 500 ms debounce | `DetectionStateMachineTest.02` | 🕒 menunggu video |
| K6 FPS ≥5 di HP 3 GB | ≥5 fps | kode membatasi 5 fps @480p | konfig `ImageAnalysis` | 🕒 menunggu HP |
| K7 Baterai ≤8 %/jam | ≤8 %/jam | pause saat STOP + Mode Ringan 10 dtk | kode service | 🕒 menunggu HP |
| K8 Offline | mode pesan | 0 dependensi jaringan di kode | grep `http` di kode | 🕒 menunggu uji |
| K9 0 foto tersimpan | 0 foto | pipeline tidak pernah tulis gambar | review kode | 🕒 menunggu uji |
| K10 Overlay 5 app | 5 app | `TYPE_APPLICATION_OVERLAY` | `OverlayService` | 🕒 menunggu uji |

Legenda: ✅ = terverifikasi otomatis oleh agent · 🕒 = instruksi di
`ml/reports/manual_kpi.md`, menunggu verifikasi user di hardware.

## Catatan dataset

D1–D3 dapat dievaluasi dari fixture otomatis (sudah disediakan
`ml/ground_truth/fixture_*.csv` untuk K1–K3). D4–D7 membutuhkan hardware/akun
eksternal — instruksi lengkap di `ml/reports/manual_kpi.md`.

## Reproduksi

```bash
./gradlew.bat clean test ktlintCheck assembleDebug   # gate utama
uv run python ml/metrics.py \
    --labels ml/ground_truth/fixture_labels.csv \
    --predictions ml/ground_truth/fixture_predictions.csv
```
