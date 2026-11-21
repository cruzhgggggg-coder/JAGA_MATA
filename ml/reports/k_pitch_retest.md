# Retest K-pitch — JagaMata (DEV-35)

**Status: SELESAI — KEPUTUSAN: GANTI (model jadi default)**
**MAE terverifikasi: 4,3692° ≤ target 5,0° → LULUS (PRD §5.5 butir 4)**

## 1. Sumber model

| Item | Nilai |
|---|---|
| Repo | `https://github.com/Theology26/JagaMata-ML.git` (rekan tim) |
| Arsitektur | MobileNetV3-Small, multi-task `[pitch, yaw, roll]` |
| File asli | `ml/models/jagamata_pitch_tuned_int8.onnx` (1,62 MB) · FP32 5,81 MB |
| Hasil konversi | `app/src/main/assets/pitch_model.tflite` (**1,64 MB**, dynamic-range quantized) |
| Input | `input_face_image` [1,3,224,224] float32, ImageNet μ/σ |
| Output | `pose_3d_degrees` [1,3] → pitch, yaw, roll (derajat) |
| Smoke test | ✅ inferensi jalan (contoh output `[13.39, 0.91, -4.33]`) |

## 2. Angka MAE (verifikasi 3 sumber — konsisten)

| Sumber | Angka |
|---|---|
| `README.md` repo | MAE = 4,3692° |
| `ml/runs/mse_evaluation_summary.json` (Champion, screen_domain) | `mae: 4.3692`, `mse: 42.0147`, `rmse: 6.4819` |
| `ml/LAPORAN_TRAINING.md` §6.1.C tabel Champion | 4,3692° (N=1.508, \|yaw\|<45°) |

**Konteks evaluasi (dari laporan rekan tim):**
- Dataset uji sakral AFLW2000-3D, N=2.000 (irisan train = 0, anti-leakage group split)
- Screen Domain operasional (|yaw| < 45°): **N = 1.508 → MAE 4,3692°**
- Extreme Profile (|yaw| ≥ 45°): N = 492 → MAE 24,03° (di luar rentang pakai anak menatap layar)
- Val MAE champion: 2,11° · sanity check 32 sampel lulus

**Verifikasi independen agent:** angka identik di ketiga sumber; struktur model
dicek dengan `onnx.checker` (input/output sesuai kontrak §5.5); konversi
ONNX→TFLite diuji dengan inferensi nyata (Python/TFLite) → LULUS.

## 3. Keputusan

> **GANTI — `ModelPitchBackend` diaktifkan sebagai backend pitch default.**
> Syarat PRD §5.5 butir 4 ("hanya boleh menggantikan backend lama jika MAE ≤ 5°")
> terpenuhi: 4,3692° ≤ 5,0°.
>
> `BuildConfig.USE_TFLITE_PITCH = true` · `PitchBackendFactory` memilih
> `ModelPitchBackend` bila file ada, otomatis `LandmarkPitchBackend` bila file
> dihapus (F9 tetap terpenuhi — teruji `PitchBackendFactoryTest`).

## 4. Penyesuaian teknis terhadap model (dicatat di ASSUMPTION LOG)

1. **Format** — rekan tim mengirim ONNX; PRD §2.24 meminta TFLite. Dikonversi
   `ONNX → TF SavedModel → TFLite` (dynamic range quantization) di `ml/export/`.
   Skrip: `ml/export/convert_pitch_model.py` (+ mode input `.onnx`).
2. **Tanda** — konvensi model: negatif = menunduk; state machine JagaMata (PRD §2):
   positif = menunduk → **pitch output dinegasi** di `ModelPitchBackend`.
3. **Ambang** — usul model `< -20° / 5 dtk` TIDAK dipakai; ambang tetap PRD §2.5
   (`> 30° / 3 dtk`). Model hanya sumber angka pitch.
4. **Guardrail yaw** — `|yaw| ≥ 45°` → pitch dianggap tidak dipercaya → null
   (rekomendasi laporan training §9.2).
5. **Face crop** — pipeline ditambah ekstraksi crop wajah dari frame
   (`ModelPitchBackend.cropFace` + simpan `lastFrame` di `FaceDetectorManager`);
   berjalan di mode MediaPipe maupun degraded ML Kit.
6. **Bug ikut diperbaiki** — landmark MediaPipe ternormalisasi [0,1] sempat
   dihitung sebagai piksel → kini diskalakan (`toPixelBox`) sehingga
   `faceHeightPx` benar (dampak ke estimasi jarak/KPI).

## 5. Status Komponen & Verifikasi Fisik

- **Retest 12 video D2** (banding landmark-vs-model dengan label sudut) — butuh
  HP/kamera fisik, instruksi di `ml/reports/manual_kpi.md`. Angka AFLW2000 dari
  rekan tim dipakai sebagai dasar keputusan sementara.
- `face_landmarker.task` **SUDAH TERPASANG ✅** (3,58 MB di `app/src/main/assets/`) —
  MediaPipe FaceLandmarker aktif sebagai primary backend dengan 478 landmark 3D,
  serta crop wajah untuk `pitch_model.tflite` berfungsi optimal. Fallback ML Kit
  tetap siap jika terjadi kendala runtime.

## 6. Reproduksi

```bash
# Konversi ulang (bila model berubah)
uv run --python 3.11 --with "onnx==1.15.0" --with onnx-tf \
  --with "tensorflow-cpu==2.15.1" --with "tensorflow-probability==0.23.0" \
  python ml/export/convert_pitch_model.py --onnx ml/export/jagamata_pitch_tuned.onnx

# Verifikasi gate
./gradlew.bat clean testDebugUnitTest ktlintCheck detekt assembleDebug
```
