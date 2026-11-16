# JagaMata

Aplikasi Android yang mendeteksi real-time 3 risiko kesehatan mata anak:
(1) jarak layar <30 cm, (2) ruangan gelap <150 lux, (3) postur menunduk >30° —
memberi peringatan lewat maskot Kiko yang menutup layar, plus dashboard dan
laporan mingguan untuk orang tua.

- **Package:** `com.jagamata.app`
- **Platform:** Android (minSdk 24, targetSdk 35) · 100% Kotlin · Jetpack Compose
- **AI on-device:** MediaPipe FaceLandmarker + ML Kit Face Detection (fallback)
- **Bahasa UI:** Indonesia · **Offline-first** · 0 gambar disimpan/dikirim

## Cara build

Prasyarat: JDK 17, Android SDK (platform 35+), Gradle wrapper sudah termasuk.

```bash
./gradlew.bat assembleDebug        # APK debug
./gradlew.bat testDebugUnitTest    # unit test
./gradlew.bat ktlintCheck detekt   # lint
./gradlew.bat bundleRelease        # AAB release (perlu keystore, lihat bawah)
```

APK hasil: `app/build/outputs/apk/debug/app-debug.apk`

## Cara menjalankan

1. Install APK ke HP (`adb install -r app-debug.apk`) atau jalankan dari
   Android Studio.
2. Buka JagaMata → ikuti onboarding: pilih usia anak → izinkan **Kamera**,
   **Tampilan di atas aplikasi lain**, dan **Notifikasi**.
3. **Kalibrasi jarak**: pegang HP ±30 cm dari wajah anak (bantu dengan
   penggaris 30 cm) dan tahan stabil ≥1,5 detik sampai progres penuh.
4. Tekan **Mulai** di dasbor — pemantauan berjalan sebagai foreground service.

### Model MediaPipe (opsional, tapi disarankan)
Tanpa model, aplikasi berjalan dalam *degraded mode* (fallback ML Kit —
deteksi jarak & cahaya tetap jalan, pitch menyusul). Untuk mode penuh,
pasang model seperti instruksi di
`app/src/main/assets/MODEL_README.txt`.

## Struktur project

```
app/src/main/java/com/jagamata/app/
├── detection/     # DistanceEstimator, HeadPitchAnalyzer, DetectionStateMachine,
│                  # CycleAggregator, MinuteTicker, CalibrationManager,
│                  # FaceDetectorManager, LightMonitor, PitchBackend
├── service/       # DetectionForegroundService, OverlayService,
│                  # NotificationHelper, DailyRollupWorker
├── data/          # Room (CheckSnapshot, ViolationLog, DailyStats, AppSettings)
│                  # + SettingsRepository (DataStore)
├── report/        # ReportGenerator (PNG 1080x1920)
└── ui/            # theme (Baloo 2/Nunito), navigation, onboarding, parent,
                   # child (alarm), common, viewModel
```

## Ambang & perilaku (PRD BAB 2)

| Parameter | Default | Bisa diubah di Settings |
|---|---|---|
| Jarak dekat (BLOCKED) | <30 cm | slider 20–50 cm |
| Cahaya (WARNING) | <150 lux | slider 50–500 lux |
| Postur (WARNING) | >30° | slider 20–60° |
| Hysteresis keluar | >35 cm selama 2 dtk | — |
| Debounce | close 500 ms · pose 3 dtk · dark 10 dtk | — |
| Siklus cek | 60 detik (±2 dtk) | Mode Ringan: analisis 1 frame/10 dtk |
| Retensi data | 30 hari (prune harian) | Hapus Semua Data di Privasi |

Zona tampilan: **Aman** ≥50 cm (hijau) · **Waspada** 30–50 cm (kuning) ·
**Bahaya** <30 cm (merah).

## Pengujian & bukti

- `./gradlew.bat clean test ktlintCheck assembleDebug` → gate utama (DoD BAB 9)
- 62 unit test lulus (lihat `docs/Laporan_Pengujian.md`)
- 4 file bukti KPI: `ml/reports/{k1_k2_k3.txt, k4.txt, manual_kpi.md, assumptions.md}`
- Instruksi verifikasi manual K5–K10 di `ml/reports/manual_kpi.md`

## Build release

`bundleRelease` / `assembleRelease` membutuhkan keystore lokal:

```bash
keytool -genkey -v -keystore release.jks -keyalg RSA \
        -keysize 2048 -validity 10000 -alias jagamata
```

Lalu isi `signingConfigs` di `app/build.gradle.kts` (atau pakai
`assembleRelease` unsigned + `zipalign`/`apksigner` manual — instruksi ada di
`docs/LAPORAN_AKHIR.md`).

## Privasi

- Tidak ada foto yang disimpan atau dikirim (KPI K9)
- Semua data di perangkat; hapus lewat **Privasi → Hapus Semua Data**
- Tanpa API key / token di kode (KPI K8 — offline penuh)

## Lisensi aset
Font Baloo 2 & Nunito (SIL OFL, google/fonts). Maskot Kiko saat ini
placeholder geometris — ganti dengan export desain resmi bila tersedia.
