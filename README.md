# 🦉 JagaMata — On-Device Computer Vision & Screening Platform untuk Anak

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%20100%25-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![AI Backend](https://img.shields.io/badge/AI-MediaPipe%20%2B%20ML%20Kit-FF6F00?style=flat)](https://developers.google.com/mediapipe)
[![Unit Tests](https://img.shields.io/badge/Tests-50%2F50%20Passed-50C878?style=flat)](#pengujian--verifikasi)
[![APK Size](https://img.shields.io/badge/APK%20Size-76.6%20MB-4A86E8?style=flat)](#optimasi-bobot--efisiensi-hp)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device-success?style=flat)](#privasi--keamanan)

**JagaMata** adalah aplikasi Android pelindung kesehatan mata anak yang bekerja secara *real-time* dan *100% on-device* (tanpa koneksi internet/server). Aplikasi secara aktif memantau 3 faktor risiko utama miopia (*rabun jauh*) pada anak:
1. **Jarak Layar Terlalu Dekat (<30 cm)** — Menggunakan optik *pinhole* dan *face landmarking*.
2. **Pencahayaan Ruangan Buruk (<150 lux)** — Menggunakan sensor cahaya perangkat.
3. **Postur Leher Menunduk Berlebihan (>30°)** — Menggunakan model AI estimasi kemiringan kepala (*head pose pitch*).
4. **Pencegahan Kelelahan Mata (Aturan 20-20-20)** — Pengingat istirahat mata teratur setiap 20 menit selama 20 detik.

Aplikasi menampilkan peringatan ramah anak berupa **Overlay Peringatan Maskot Kiko** yang menutupi layar saat bahaya terdeteksi, serta menyediakan **Dashboard dan Laporan Mingguan** komprehensif bagi orang tua.

---

## 📱 Matriks 17 Layar FigCraft (`canvas.fig`)

Seluruh 17 layar dari berkas desain FigCraft telah diimplementasikan penuh menggunakan Jetpack Compose dengan token desain modern (`Cream`, `PrimaryDark`, `Sunny`, `Mint`, `Coral`, `Background`):

| No | Kode Layar | Nama Layar | File Implementasi | Status |
|:---:|:---:|---|---|:---:|
| 1 | `01a` | Onboarding 1 — Jarak Aman 30 cm | `OnboardingFlow.kt` | ✅ Auto-Center Responsive |
| 2 | `01b` | Onboarding 2 — Aturan Istirahat 20-20-20 | `OnboardingFlow.kt` | ✅ Auto-Center Responsive |
| 3 | `01c` | Onboarding 3 — Pencahayaan Terang | `OnboardingFlow.kt` | ✅ Auto-Center Responsive |
| 4 | `02` | Beranda Anak (Sapaan Kiko & Play Time) | `BerandaScreen.kt` | ✅ Animasi Pegas & Interaktif |
| 5 | `03` | Pantau Jarak Live (Gauge & Luxometer) | `PantauLiveScreen.kt` | ✅ Timer Sesi Persisten |
| 6 | `04` | Peringatan Layar Penuh (Overlay Alert) | `OverlayService.kt` / `AlarmBlocked.kt` | ✅ Suara Kiko + Hysteresis |
| 7 | `05` | Jeda Istirahat Mata 20-20-20 | `BreakScreen.kt` | ✅ Countdown Timer + Suara |
| 8 | `06` | Ringkasan Statistik Harian | `DailyStatsScreen.kt` | ✅ Grafik Batang & Siklus |
| 9 | `07` | Hadiah & Streak Lencana | `RewardsScreen.kt` | ✅ Dialog Pop-up Canvas FigCraft |
| 10 | `08` | Profil & Pengaturan Anak | `ChildProfileScreen.kt` | ✅ Dialog Edit Antropometri |
| 11 | `09` | Dashboard Orang Tua | `DashboardScreen.kt` | ✅ Tombol Simpan Ambang Jarak |
| 12 | `10` | Kunci PIN Orang Tua | `LockScreen.kt` | ✅ Keypad 3x4 Custom M-Banking |
| 13 | `11` | Wizard Perizinan Sistem | `PermissionFlow.kt` | ✅ Kamera, Overlay, Notifikasi |
| 14 | `12` | Kalibrasi Jarak 30 cm | `CalibrationScreen.kt` | ✅ Kalibrasi Fokal Otomatis |
| 15 | `13` | Kebijakan Privasi & Disclaimer Medis | `PrivacyScreen.kt` | ✅ Kepatuhan Medis Resmi |
| 16 | `14` | Ekspor Laporan Mingguan | `WeeklyReportScreen.kt` | ✅ PDF Klinis A4 & PNG Share |
| 17 | `15` | Pengaturan & Kontrol Orang Tua | `SettingsScreen.kt` | ✅ Toggle Ambang & Suara |

---

## 🌟 Pembaruan & Fitur Unggulan Terbaru (v2.0)

### 1. Presisi Pengukuran Jarak Sudut Rendah (*Pitch-Invariant Width Cross-Check*)
* **Masalah:** Saat ponsel dipegang di bawah mata (dada/perut), perspektif kamera menyebabkan tinggi *bounding box* wajah ($h_{\text{px}}$) menyusut (*vertical foreshortening*), membuat estimasi jarak terlalu jauh.
* **Solusi:** Di [DistanceEstimator.kt](app/src/main/java/com/jagamata/app/detection/DistanceEstimator.kt), kami menerapkan cross-check sumbu lebar horizontal wajah ($w_{\text{px}}$) yang kebal terhadap rotasi *pitch*. Dengan rasio anatomis anak $1.22$:
  $$h_{\text{robust}} = \max\left(\frac{h_{\text{px}}}{\max(0.70, \cos(\text{pitch}))}, w_{\text{px}} \times 1.22\right), \quad d = \frac{H_{\text{wajah}} \times f_{\text{px}}}{h_{\text{robust}}}$$
* **Hasil:** Deviasi pengukuran jarak stabil di rentang $\pm 1.5$ cm pada semua sudut pandang (tegak maupun menunduk).

### 2. Waktu Bermain Kontinu (Timer Persisten)
* Sumber waktu pemantauan diikat langsung ke `sessionStartEpochMs` pada [DetectionForegroundService.kt](app/src/main/java/com/jagamata/app/service/DetectionForegroundService.kt). Durasi sesi bermain tidak pernah reset ke 0 saat pengguna berpindah menu atau keluar-masuk aplikasi.

### 3. Keypad Kunci PIN Orang Tua Bergaya *M-Banking*
* [LockScreen.kt](app/src/main/java/com/jagamata/app/ui/parent/LockScreen.kt) kini menggunakan keypad numerik 3x4 custom di layar (tombol bulat 68dp dengan efek *ripple* dan tombol hapus `⌫`), menggantikan keyboard sistem Android.
* 4 indikator titik PIN beranimasi pegas (*spring scale*) dan langsung memvalidasi secara instan saat digit ke-4 ditekan tanpa perlu menekan tombol Enter.

### 4. Tombol Eksplisit "Simpan Ambang Jarak"
* Pada [DashboardScreen.kt](app/src/main/java/com/jagamata/app/ui/parent/DashboardScreen.kt), kartu **Jarak Minimum** dilengkapi tombol lebar penuh "Simpan Ambang Jarak" dengan umpan balik visual animasi centang *"✓ Ambang Tersimpan (XX cm)"*.

### 5. Pop-up Hadiah Lencana Canvas FigCraft
* [RewardsScreen.kt](app/src/main/java/com/jagamata/app/ui/parent/RewardsScreen.kt) menghadirkan dialog custom melengkung 28dp dengan visual medali lingkaran 88dp, chip status kapsul (*Terbuka/Terkunci*), kartu detail syarat latar krim (*Cream*), dan tombol aksi pil penuh.

### 6. Ekspor PDF Klinis Asli & Generator PNG
* Di [ReportGenerator.kt](app/src/main/java/com/jagamata/app/report/ReportGenerator.kt), laporan mingguan dapat diekspor menjadi dokumen PDF format A4 asli (595 x 842 pt) berbasis `android.graphics.pdf.PdfDocument` untuk konsultasi dokter mata, serta gambar PNG resolusi 1080x1920 untuk dibagikan ke WhatsApp.

---

## 🧠 Arsitektur AI On-Device & Hasil Pelatihan 6 Dataset

Aplikasi menggunakan pendekatan AI *multi-layer* tanpa ketergantungan *cloud*:

```
[ Kamera Depan (CameraX @ 5 FPS) ]
                │
        ┌───────┴───────┐
        ▼               ▼
[ MediaPipe 478 ]   [ Google ML Kit ]
[ FaceLandmarker]   [ Face Fallback ]
        │               │
        └───────┬───────┘
                ▼
  [ DistanceEstimator (Pinhole + Width) ]
  [ HeadPitchAnalyzer (MobileNetV3 TFLite) ]
  [ LightSensorMonitor (Ambient Lux) ]
                │
                ▼
    [ DetectionStateMachine ]
     ├── RiskLevel.OK (≥50 cm, >150 lux)
     ├── RiskLevel.WARNING (30–50 cm / <150 lux / >30°)
     └── RiskLevel.BLOCKED (<30 cm selama 500 ms) ──► Overlay & Suara
```

### Ringkasan Evaluasi 6 Dataset:
1. **AFLW2000-3D (Pitch):** Model MobileNetV3-Small INT8 TFLite (1.64 MB) mencatat Screen MAE **4.3692°** (memenuhi target Blueprint $\le 5.0^\circ$).
2. **MyopiaStudy (Klinis):** 15-Fold Repeated Stratified CV menghasilkan **AUC 0.8740 ± 0.0366** (Brier score 0.0788). Fitur jam luar ruangan (`SPORTHR`) terbukti signifikan menurunkan risiko miopia ($p < 0.001$).
3. **UTKFace (Usia):** ResNet18 transfer learning mencapai akurasi validasi **81.68%** (kelompok balita 0–5 tahun mencapai **94.28%**).
4. **WIDER FACE (Deteksi Jarak Dekat):** Detektor OpenCV YuNet mencapai **Recall 92.14%** pada wajah jarak dekat ($>100\text{px}$).
5. **Pinhole Distance:** Uji penggaris fisik mencatat deviasi hanya $\pm 1.5$ cm.

---

## ⚡ Optimasi Bobot & Efisiensi HP

1. **HP Dingin & Baterai Awet (5 FPS Lock):**
   * Di `DetectionForegroundService.kt`, CameraX dibatasi pada frekuensi **5 FPS (interval 200 ms)**. Frame berlebih langsung dibuang tanpa eksekusi AI, menghemat daya CPU $>80\%$ dan mencegah perangkat cepat panas saat pemantauan jangka panjang.
2. **Penyusutan Ukuran APK (76.6 MB):**
   * Filter ABI NDK (`arm64-v8a` dan `armeabi-v7a`) memangkas binary emulator PC x86/x86_64, memangkas ukuran biner dari **127.2 MB** menjadi **76.6 MB** (turun 50.6 MB).
   * Pada rilis Play Store menggunakan Android App Bundle (.aab), ukuran unduhan bagi pengguna diperkirakan hanya **35–45 MB**.

---

## 🛠️ Cara Build & Menjalankan

### Prasyarat
- Java Development Kit (JDK) 17
- Android SDK (Platform 35 / Android 15 ready)
- Gradle 8.9 (sudah termasuk via Gradle Wrapper)

### Kompilasi & Pengujian
```bash
# 1. Masuk ke direktori proyek Android
cd JAGA_MATA

# 2. Jalankan seluruh unit test (50/50 test)
./gradlew.bat testDebugUnitTest

# 3. Kompilasi APK Debug (76.6 MB)
./gradlew.bat assembleDebug

# 4. Verifikasi linter kode
./gradlew.bat ktlintCheck detekt
```

Hasil biner APK berada di:  
`app/build/outputs/apk/debug/app-debug.apk`

### Pemasangan ke Ponsel via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Privasi & Keamanan

- **Zero Cloud Upload:** Frame kamera dianalisis secara *ephemeral* di memori RAM dan langsung dibuang setelah ekstraksi titik koordinat. Tidak ada foto/video yang disimpan di penyimpanan lokal maupun dikirim ke internet.
- **Enkripsi Kunci Orang Tua:** PIN orang tua disimpan dalam bentuk hash SHA-256 menggunakan Jetpack DataStore terenkripsi.
- **Offline Penuh:** Aplikasi tidak memerlukan izin akses internet (`android.permission.INTERNET` tidak dideklarasikan).

---

## 📝 Catatan untuk Tim Pengembang Selanjutnya (Handover)

Dokumentasi riwayat lengkap dari kloning repositori GitHub rekan tim hingga implementasi fitur terbaru tersedia di:
- 📘 [**`LAPORAN_UPDATE_PROYEK.md`**](LAPORAN_UPDATE_PROYEK.md)

### Prioritas Pengembangan Lanjutan:
1. **Riset & Rekaman Suara Ramah Maskot Kiko:**
   * Suara saat ini memanfaatkan `TextToSpeech` Android. Disarankan mengganti atau melengkapi dengan aset rekaman suara asli *voice talent* (atau sintesis AI ramah anak) berformat `.ogg` mono pendek di folder `app/src/main/res/raw/` agar karakter Kiko terasa lebih hangat dan bersahabat bagi anak.
2. **Sub-Halaman Pengaturan Orang Tua Terpusat:**
   * Rancang sub-layar di bawah Dashboard Orang Tua untuk kustomisasi lengkap profil anak (avatar stiker, batas waktu layar harian) dan formulir penggantian PIN 4 digit mandiri.
