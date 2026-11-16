# Retest K-pitch — JagaMata (DEV-35)

**Status: MENUNGGU MODEL** — `ml/export/pitch_model.tflite` (hasil rekan tim ML)
belum tersedia di repositori, sehingga perbandingan MAE belum bisa dijalankan.

## Kontrak (PRD §5.5 butir 4)

Backend `TflitePitchBackend` hanya boleh menggantikan `LandmarkPitchBackend`
sebagai DEFAULT jika **MAE ≤ 5°** terhadap label referensi (AFLW2000 atau
banding landmark-vs-model pada 12 video D2).

## Cara menjalankan setelah model datang

1. Konversi model:
   ```bash
   python ml/export/convert_pitch_model.py --pt ml/export/pitch_best.pt
   ```
   Hasil: `app/src/main/assets/pitch_model.tflite`
2. Aktifkan flag di `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("boolean", "USE_TFLITE_PITCH", "true")
   ```
3. Ukur MAE pada 12 video D2 (label sudut pitch ±5°, lihat `ml/reports/manual_kpi.md`):
   ```
   MAE = (1/12) * Σ |pitch_model - pitch_label|
   ```
4. Isi tabel di bawah.

## Hasil

| Backend | MAE vs label | Keputusan |
|---|---|---|
| LandmarkPitchBackend (geometri nose–chin atan2) | belum diukur di hardware | **DIPERTAHANKAN sebagai default** |
| TflitePitchBackend (pitch_best.pt) | model belum ada | menunggu |

**Keputusan saat ini:** pertahankan `LandmarkPitchBackend` sebagai default
(F9: platform tetap berjalan penuh tanpa file model). `PitchBackendFactory`
sudah diuji unit test untuk memilih backend yang benar
(`PitchBackendFactoryTest` — 5 test lulus).

## Catatan untuk rekan tim ML

- Format masuk `TflitePitchBackend`: crop wajah **224×224 RGB float32**
  ternormalisasi mean/std ImageNet; keluaran **1 float = pitch derajat**.
- File konversi & script di `ml/export/` (di luar `app/`) supaya APK tidak
  bengkak saat model belum ada.
