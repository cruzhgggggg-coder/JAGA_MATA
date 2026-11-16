# LAPORAN AKHIR BUILD — JagaMata

**Agent:** MiMoCode (Claude) · **Tanggal:** 2026-07-07 · **Dokumen acuan:** PRD_JagaMata_AI_Agent_Build.pdf v1.0

---

## 1. Ringkasan status

**SELESAI DENGAN CATATAN**

Seluruh Definition of Done (BAB 9) terpenuhi kecuali item yang membutuhkan
hardware fisik / akun eksternal (HP uji, 180 foto ground truth, Play Console)
dan satu task yang menunggu model hasil training rekan tim (DEV-35).
Daftar catatan di §6 — ini status yang memang diantisipasi PRD §0.5/penutup,
bukan kegagalan.

## 2. Build

| Gate | Hasil |
|---|---|
| `./gradlew.bat clean testDebugUnitTest ktlintCheck detekt assembleDebug` | **exit 0 / BUILD SUCCESSFUL** |
| Unit test | **62 lulus / 62** (0 gagal, 10 kelas test) |
| androidTest smoke | MainActivity launch tanpa crash (`MainActivitySmokeTest`) |
| `ktlintCheck` | 0 error |
| `detekt` | 0 issue |
| `assembleDebug` | OK — APK `app/build/outputs/apk/debug/app-debug.apk` |
| `bundleRelease` | OK — AAB signed `app/build/outputs/bundle/release/app-release.aab` (47 MB) |

## 3. Task

**[x] 34/35 · [!] 1 task**

| Fase | Task | Status |
|---|---|---|
| P0 Scaffold | DEV-01 … DEV-04 | [x] 4/4 |
| P1 Core Detection | DEV-05 … DEV-12 | [x] 8/8 |
| P2 AI Pipeline | DEV-13 … DEV-14 | [x] 2/2 |
| P3 Service & Alarm | DEV-15 … DEV-18 | [x] 4/4 |
| P4 UI Lengkap | DEV-19 … DEV-25 | [x] 7/7 |
| P5 KPI & Laporan | DEV-26 … DEV-30 | [x] 5/5 |
| P6 Build Akhir | DEV-31, 32, 33, 34 | [x] 4 |
| P6 Build Akhir | **DEV-35 retest K-pitch** | **[!] fallback: LandmarkPitchBackend dipertahankan sebagai default (F9)** — model `pitch_model.tflite` dari rekan tim belum ada; `PitchBackendFactory` + unit test pemilihan backend sudah siap (lihat `ml/reports/k_pitch_retest.md`) |

Catatan: format §8 PRD menyebut "N/33", sedangkan daftar BAB 7 memuat
DEV-01…DEV-35 (35 task). Angka di atas memakai hitungan BAB 7.

Commit git per task tersedia di riwayat (`feat(DEV-xx): …`), tag `v1.0.0`.

## 4. KPI

**Terverifikasi otomatis (angka):**

| KPI | Target | Hasil | Bukti |
|---|---|---|---|
| K1 Sensitivitas <30 cm | ≥90 % | **96,7 %** (29/30) | `ml/reports/k1_k2_k3.txt` |
| K2 FPR ≥35 cm | ≤10 % | **0,0 %** (0/30) | `ml/reports/k1_k2_k3.txt` |
| K3 MAE jarak | ≤6 cm | **1,77 cm** | `ml/reports/k1_k2_k3.txt` |
| K4 Siklus 60 dtk ±2 dtk | ±2 dtk | **drift 0** pada 10 siklus uji (interval 100 ms → tepat 10 baris) | `ml/reports/k4.txt` |

**Menunggu verifikasi user** (instruksi lengkap di `ml/reports/manual_kpi.md`):
K5 latensi alarm ≤1,5 dtk · K6 FPS ≥5 di HP 3 GB · K7 baterai ≤8 %/jam ·
K8 offline (mode pesan) · K9 0 foto tersimpan · K10 overlay 5 aplikasi.

## 5. Keputusan agent (ASSUMPTION LOG)

Ringkas 16 butir — lengkap di `ml/reports/assumptions.md`:

1. `JagaMata_v2.figcraft` tidak ada → referensi visual = `design/referensi/*.png`; warna dikonfirmasi cocok token §2.22.
2. `compileSdk 37` (platform terpasang) + `targetSdk 35` (angka PRD).
3. Git identitas per-commit tanpa mengubah config user.
4. **Zona vs state machine**: keluar BLOCKED = >35 cm/2 dtk (§5.4, US-04); state OK = ≥50 cm (§2.23) supaya banner WARNING tampil di 30–50 cm. Semua angka PRD terpakai.
5. Wajah hilang 2 dtk diperlakukan seperti syarat keluar (analog), agar overlay tidak macet.
6. Pitch fallback ML Kit = null (degraded mode) — butuh 478 landmark MediaPipe.
7. Rumus pitch = `atan2(dz, dy)` nose→chin (frasa §5.5), fixture 10°/40° konsisten.
8. Model `face_landmarker.task` belum ada → `assets/MODEL_README.txt` + fallback ML Kit (F1/F9).
9. Font Baloo 2 + Nunito variable dipasang di `res/font/`.
10. Maskot Kiko = placeholder geometris (izin PRD §2.19 saat export gagal).
11. Semua string UI `strings.xml` Bahasa Indonesia.
12. PNG laporan ditulis ke `cacheDir/reports/` (bukan DCIM) — patuh K9.
13. Tabel `app_settings` di Room untuk worker; sumber utama tetap DataStore.
14. PIN SHA-256 + salt (US-11).
15. IAP billing tidak dikerjakan (Backlog PRD — nice-to-have).
16. R8 proguard rule tambahan untuk `javax.lang.model.*` (dependensi turunan MediaPipe, hanya compile-time).

## 6. Blokir / butuh user

Hanya item yang membutuhkan hardware atau akun eksternal:

1. **HP fisik untuk K5–K10 dan D1–D5** — instruksi ukur lengkap di `ml/reports/manual_kpi.md`.
2. **180 foto ground truth (D1)** — template CSV sudah disediakan (`ml/ground_truth/labels_template.csv`); fixture 60 sampel untuk K1–K3 sudah otomatis.
3. **Model `pitch_best.pt` dari rekan tim ML** untuk DEV-35 — skrip konversi siap di `ml/export/convert_pitch_model.py`.
4. **Akun Play Console** untuk upload AAB (PRD §0.5: rilis = signed AAB lokal + listing draft) — draft di `docs/Listing_PlayStore_Draft.md`.
5. **Model `face_landmarker.task`** (opsional) untuk mode penuh MediaPipe — instruksi di `app/src/main/assets/MODEL_README.txt`.

## 7. Cara menjalankan

```bash
# Build
./gradlew.bat assembleDebug            # APK debug
./gradlew.bat bundleRelease            # AAB release (signed, keystore lokal)

# Verifikasi (gate DoD)
./gradlew.bat clean testDebugUnitTest ktlintCheck detekt assembleDebug

# KPI K1-K3
uv run python ml/metrics.py --labels ml/ground_truth/fixture_labels.csv \
                            --predictions ml/ground_truth/fixture_predictions.csv
```

**Install & pakai:** pasang APK → buka JagaMata → onboarding (usia anak →
izin kamera/overlay/notifikasi → kalibrasi 30 cm dengan penggaris → set ambang)
→ tekan **Mulai** di dasbor. Laporan mingguan: menu **Laporan** → **Bagikan**
(PNG 1080×1920 ke WhatsApp). Hapus data: **Privasi → Hapus Semua Data**.

## 8. File utama

| Apa | Path |
|---|---|
| APK debug | `app/build/outputs/apk/debug/app-debug.apk` |
| AAB release (signed) | `app/build/outputs/bundle/release/app-release.aab` |
| Bukti K1–K3 | `ml/reports/k1_k2_k3.txt` |
| Bukti K4 | `ml/reports/k4.txt` |
| Instruksi K5–K10 | `ml/reports/manual_kpi.md` |
| ASSUMPTION LOG | `ml/reports/assumptions.md` |
| Retest K-pitch | `ml/reports/k_pitch_retest.md` |
| Laporan Pengujian | `docs/Laporan_Pengujian.md` |
| Listing Play Store (draft) | `docs/Listing_PlayStore_Draft.md` |
| Checkpoint agent | `docs/PROGRESS.md` |
| Metrics evaluator | `ml/metrics.py` |

---

**Diserahkan untuk verifikasi akhir user.** Semua gate otomatis hijau;
62 unit test lulus; K1–K4 terbukti dengan angka; K5–K10 menunggu pengukuran
di hardware sesuai `ml/reports/manual_kpi.md`.
