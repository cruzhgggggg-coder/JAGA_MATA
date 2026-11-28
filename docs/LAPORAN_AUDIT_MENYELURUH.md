# Laporan Audit Menyeluruh — JagaMata

**Tanggal:** 28 November 2026
**Repo:** `cruzhgggggg-coder/JAGA_MATA` @ `305d553` (commit "fix: jarak tidak sesuai setelah kalibrasi + notifikasi looping")
**Metode:** audit kode statis dalam 2 putaran agent paralel.
- **Putaran 1:** 5 agent per domain fitur: (A) keamanan & mode ortu, (B) pipeline deteksi & kalibrasi, (C) service/overlay/notifikasi, (D) data/statistik/laporan, (E) onboarding/UI anak/hadiah.
- **Putaran 2:** 3 verifikator independen membuka ulang setiap temuan TINGGI/SEDANG di kode dan memberi verdict CONFIRMED / PLAUSIBLE / REJECTED, lalu mencari celah yang terlewat, termasuk matriks sinkronisasi lintas fitur.
- Temuan yang butuh angka dibuktikan dengan skrip replika (simulasi overlay S1, kalibrasi D2/D3, perhitungan tanggal R4).
- Gradle tidak dijalankan karena Android SDK tidak tersedia di lingkungan audit, dan repo tidak diubah.

**Batasan:** belum ada uji runtime di perangkat. Temuan bertanda *(PLAUSIBLE)* bergantung pada perilaku platform/OEM dan perlu dikonfirmasi di HP.

> **STATUS PERBAIKAN (terintegrasi di repo ini):** lihat bagian paling bawah dokumen ini — diperbarui seiring perbaikan berjalan.

---

## 1. Ringkasan

| Severity | Jumlah (unik, setelah deduplikasi) |
|---|---|
| 🔴 KRITIS | 1 |
| 🟠 TINGGI | 15 |
| 🟡 SEDANG | 27 |
| ⚪ RENDAH | 40 |
| **Total** | **83** |

Verifikasi putaran 2 tidak menolak satu pun temuan utama. Tiga sub-klaim dikoreksi (A8/U7, U6, D12 diturunkan ke RENDAH), dan lima dinaikkan (S1 → KRITIS; A5, D7, S6, S7 → TINGGI).

**Tiga kesimpulan utama:**
1. **Alarm inti praktis mati sejak commit terakhir.** Overlay "terlalu dekat" hanya muncul sekali, maksimal 5 detik, lalu tidak pernah kembali walau anak tetap terlalu dekat.
2. **Anak bisa mematikan atau membutakan pemantauan tanpa PIN dan tanpa jejak.** Caranya: tombol jeda, menutup kamera dengan jari, kalibrasi ulang, atau UI sistem Android. Orang tua tetap melihat laporan "bersih".
3. **Data tidak sinkron.** Alarm live memakai ambang dari pengaturan ortu, tetapi statistik, laporan, PDF, dan notifikasi memakai konstanta 30 cm / 150 lux / 30°. Beberapa layar ortu juga masih menampilkan data dummy.

### Status klaim perbaikan di `docs/LAPORAN_BUG.md`

| Bug | Status setelah audit | Catatan |
|---|---|---|
| B1 Hapus Semua Data | ⚠️ Sebagian | DB & DataStore terhapus, tetapi file PDF/PNG di cache tertinggal, PIN kembali 1234, dan proses tidak atomik |
| B2 PIN default & brute-force | ❌ Tidak tuntas | `1234` masih diterima, tidak ada pembuatan PIN, lockout bisa di-reset |
| B3 Ubah PIN butuh PIN lama | ✅ OK | — |
| B4 Salt+hash atomik | ✅ OK | — |
| B5 Rollup dijadwalkan | ✅ OK | — |
| B6 Rollup salah hari | ⚠️ Sebagian | Jendela waktu sudah benar, tetapi `dayEpoch` meleset 1 hari di WIB |
| B7 Cooldown menekan BLOCKED | ✅ OK lokal | Efek keselamatannya hilang karena regresi S1 |
| B8 onStartCommand idempoten | ✅ OK | — |
| B9 Izin kamera dicabut | ⚠️ Sebagian | Jalur `stopSelf` sebelum `startForeground` masih rawan crash |
| B10 Kegagalan Room mematikan logging | ⚠️ Sebagian | `aggregator.evaluate()` masih di luar try-catch dan diakses dari 2 thread |
| B11 ViewModel default param | ⚠️ Dashboard saja | Pola yang sama masih ada di SettingsScreen |
| B12 Grafik mingguan | ❌ Tidak | Masih menggambar `DummyDailyCm`, label ambang tetap "50 cm" |
| B13 PDF data palsu | ⚠️ Sebagian | Baris 0-siklus tiap malam membuat PDF "7 hari Aman" |
| B14 allowBackup | ✅ OK | — |
| B15 Vibrator API 26 | ✅ OK | — |
| B27 WARNING lengket | ✅ OK | Tetapi membuka celah bypass D7 |
| B30 Dedupe requestLevel | ⚠️ Diperbaiki → **regresi KRITIS S1** | — |
| B32 Guard NaN | ✅ OK | — |
| 305d553 "H kalibrasi = runtime" | ❌ Tidak efektif | H saling coret di rumus; akar masalah sebenarnya D2 |

---

## 2. 🔴 KRITIS

### K1. Overlay alarm "terlalu dekat" hanya tampil sekali (maks. 5 detik), lalu tidak pernah muncul lagi — regresi dari 305d553
- **Lokasi:** `service/OverlayService.kt:237-251` (`requestLevel`), `:155` (tap di mana saja menutup overlay), `:178-187` (auto-dismiss 5 dtk)
- **Bukti:** `if (prev == level) return` mendedupe semua intent selama level tetap BLOCKED. Overlay ditutup otomatis setelah 5 detik atau sekali ketuk, sedangkan `lastLevel` tetap BLOCKED. Tidak ada mekanisme yang menampilkan ulang.
- **Replika (60 dtk BLOCKED terus-menerus):**

  | Kondisi | Overlay tampil | Durasi terlihat |
  |---|---|---|
  | Sebelum 305d553 | 12× | 60 dtk |
  | Sesudah 305d553 | 1× | 5 dtk |
  | Sesudah 305d553, overlay diketuk | 1× | 0,6 dtk |

- **Repro:** Mulai pantau, tahan HP di 20 cm selama 1 menit. Alarm hanya muncul sekali di awal.
- **Dampak:** Fungsi inti app (mencegah anak terlalu dekat ke layar) tidak bekerja. Notifikasi tidak bisa menggantikan karena hanya muncul maksimal 1×/menit dan "krusial" hanya untuk jarak < 25 cm.
- **Fix:**
  - Dedupe hanya untuk OK/WARNING. Untuk BLOCKED, kirim ulang bila `overlayView == null` dan sudah lewat interval re-show (misal 3–5 dtk).
  - Atau OverlayService memeriksa `currentLevel()` setelah auto-dismiss dan menampilkan ulang selama masih BLOCKED.
  - Ketuk di area kosong jangan menutup overlay. Tutup hanya lewat tombol, dengan cooldown pendek.
  - Tambahkan unit test "BLOCKED 60 dtk ⇒ overlay terlihat ≥ X%".

---

## 3. 🟠 TINGGI

### T1. PIN default "1234" masih diterima selamanya; tidak ada langkah buat PIN (B2 tidak tuntas)
- **Lokasi:** `data/SettingsRepository.kt:75-76,98`; satu-satunya pemanggil `setParentPin` adalah dialog Ubah PIN (`SettingsScreen.kt:431`)
- **Masalah:**
  - `if (hash.isEmpty()) return pin == DEFAULT_PIN`, dan onboarding tidak pernah meminta PIN.
  - "Hapus Semua Data" (`clearAll()`) menghapus hash, sehingga PIN kembali ke 1234 sementara sesi ortu masih terbuka (V-A4).
- **Fix:** Langkah wajib "Buat PIN Orang Tua" di onboarding. Hapus fallback `DEFAULT_PIN`; bila hash kosong, paksa ke layar buat PIN. Setelah hapus data, `navigate(ONBOARDING){ popUpTo(0) }`.

### T2. Status onboarding tidak pernah dibaca — seluruh onboarding bisa dilewati
- **Lokasi:** `ui/navigation/AppNav.kt:64-77` (Splash hanya cek izin kamera); `onboardingDone` tidak punya pembaca
- **Masalah:** Kalibrasi bisa di-skip lewat back, sehingga `focalPx=0` dan service memakai default 560 px. Usia tidak pernah ditanya, jadi selalu 8. `AgeSelectScreen`, `IntroScreen`, `SetupDoneScreen`, dan `OnboardingViewModel` adalah kode mati.
- **Fix:** Splash merutekan berdasarkan `onboardingDone && pinSet && focalPx > 0`. Pasang AgeSelect ke alur.

### T3. Lockout PIN tidak persisten — brute-force tanpa batas
- **Lokasi:** `ui/parent/LockScreen.kt:74-76,83-105,361-362`
- **Masalah:** `failedAttempts` dan `lockoutUntilMs` hanya disimpan di `rememberSaveable`. Hitungan hilang saat tombol Batal/back ditekan atau app dimatikan. Waktu memakai jam dinding yang bisa dimundurkan anak. Lockout maksimal hanya 150 dtk. Ruang PIN 4 digit = 10.000 kombinasi.
- **Fix:** Simpan counter dan lockout di DataStore. Gunakan `SystemClock.elapsedRealtime()` plus penanda boot, atau simpan "sisa durasi" agar tidak terpengaruh jam. Lockout eksponensial (1, 5, 15, 60 menit). Pakai counter yang sama untuk dialog Ubah PIN (A11).

### T4. Mode ortu tidak pernah terkunci ulang (background / process death / notifikasi)
- **Lokasi:** `AppNav.kt:236-240` (unlock = sekadar navigate); `MainActivity.kt` (tanpa observer lifecycle); `NotificationHelper.kt:69-70,110-111` (`CLEAR_TOP` ke `singleTop`)
- **Repro:** Ortu membuka Dashboard, lalu menekan Home. Anak membuka app dari Recents dan langsung masuk Dashboard/Pengaturan tanpa PIN. Hal yang sama terjadi setelah process death karena `rememberNavController` me-restore back stack.
- **Fix:** Simpan state `parentUnlocked` dengan TTL (misal 5 menit). Kunci di `ProcessLifecycleOwner` ON_STOP. Buat route guard terpusat untuk semua route `ui/parent/*`. Setelah process death, mulai dari BERANDA.

### T5. Anak bisa menghentikan pemantauan tanpa PIN, dan orang tua tidak pernah tahu
- **Lokasi:** `AppNav.kt:141-144` (`onPauseToggle` → `DetectionForegroundService.stop()`); `PantauLiveScreen.kt:663-668`
- **Jalur bypass:**
  1. Tombol jeda di tab Pantau (satu ketukan).
  2. Tombol Stop di "Aplikasi aktif" Android 13+, Paksa berhenti, atau cabut izin kamera/overlay/notifikasi *(PLAUSIBLE per versi)*.
  3. Restart HP: tidak ada `BroadcastReceiver` BOOT_COMPLETED walau izinnya dideklarasikan.
- **Masalah tambahan:** Tidak ada log event, heartbeat, atau penanda "seharusnya memantau". Dashboard tetap menulis "Siklus 1 menit — aktif" (V-S2).
- **Fix:**
  - Tombol jeda wajib PIN, atau jeda maksimal N menit lalu otomatis lanjut.
  - Simpan `monitoringDesired` dan heartbeat per siklus.
  - `PeriodicWorkRequest` 15 menit untuk mendeteksi celah, mencatat event `MONITORING_GAP`, dan memberi notifikasi.
  - Dashboard menampilkan "Dijeda/berhenti sejak HH:mm" dan total durasi tidak terpantau.
  - Tambahkan boot receiver.

### T6. Kalibrasi ulang bisa dijalankan anak tanpa PIN — deteksi jarak bisa dibutakan permanen
- **Lokasi:** jalur `BERANDA/PANTAU → IZIN` (bila kamera belum granted) `→ KALIBRASI` (`AppNav.kt:126-127,148-149,178-200`); auto-start `LaunchedEffect`; `CalibrationViewModel.kt:104-116` tanpa batas nilai wajar
- **Repro:** Cabut izin kamera, tap "Mulai Pantau", lewati wizard. Kalibrasi langsung mulai dan dilakukan dengan HP di 15 cm, sehingga focal tersimpan setengah nilai sebenarnya. Semua jarak kemudian terbaca ~2× dan alarm hampir tidak pernah bunyi. Bisa juga terjadi **tidak sengaja**, misalnya setelah izin "Hanya kali ini" kedaluwarsa.
- **Fix:** Kalibrasi hanya dari mode ortu (PIN). Jangan auto-start; minta konfirmasi "pegang HP di 30 cm". Tolak focal di luar rentang wajar (misal 350–900 px) atau yang menyimpang > 30% dari nilai sebelumnya.

### T7. Menutup kamera dengan jari → alarm padam dalam ~2 detik tanpa jejak
- **Lokasi:** `detection/DetectionStateMachine.kt:110-112,127,131-132`
- **Masalah:** Wajah hilang ≥ 2 dtk dianggap "keluar zona dekat", sehingga state turun WARNING → OK. Pelanggaran tidak tercatat karena `distanceCm == null`. Jarak sangat dekat (wajah melebihi frame) jatuh ke jalur yang sama *(PLAUSIBLE)*.
- **Fix:** Bila state sebelumnya BLOCKED/WARNING dan wajah hilang, tahan state (atau masuk state `OBSTRUCTED`). Bedakan "frame gelap/tertutup" (luminansi frame ≈ 0) dari "anak pergi". Catat event `CAMERA_OBSTRUCTED` untuk laporan ortu.

### T8. App bisa berjalan tanpa satu pun kanal peringatan
- **Lokasi:** `ui/onboarding/PermissionWizardScreen.kt:73-75,134-138,167-172`; `NotificationHelper.kt:101-107,127-133`; `OverlayService.kt:82-99,157-158`
- **Masalah:**
  - Tombol "Lanjut ke Beranda" selalu aktif. Status izin overlay tidak pernah ditampilkan, dan layar pemulihan OverlayDenied tidak terjangkau di release.
  - Di Android 13+ tanpa POST_NOTIFICATIONS, `notify()` di-drop tanpa exception, tetapi fungsi tetap mengembalikan `true` dan throttle tetap terpakai.
  - Suara dan getar hanya ada di jalur overlay.
  - Hasilnya, dengan overlay dan notifikasi ditolak, anak tidak menerima alarm apa pun dan ortu tidak diberi tahu.
- **Fix:** Wizard memblokir lanjut tanpa izin overlay (atau menampilkan peringatan keras ke ortu). Cek `NotificationManagerCompat.areNotificationsEnabled()` dan izin overlay di setiap `ON_RESUME` dan di service. Fallback TTS/getar saat overlay tidak tersedia. Tampilkan status "Peringatan nonaktif" di Dashboard.

### T9. Ambang dari pengaturan ortu tidak dipakai untuk pelanggaran, statistik, laporan, PDF, dan notifikasi
- **Lokasi:** `detection/CycleAggregator.kt:40-42,60-62` (konstanta 30 cm / 150 lux / 30°); `DetectionForegroundService.kt:76` (`CycleAggregator()` tanpa konfigurasi), `:338-340` (`isCrucial < 25f`); `DailyRollupWorker.kt:47,69`; `DashboardViewModel.kt:64`; `ReportGenerator.kt:196,278,345`
- **Masalah:** Hanya `DetectionStateMachine` yang membaca ambang dari Flow. Aturan `any{}` menjadikan 1 sampel saja sebagai pelanggaran per siklus.
- **Repro:** Ortu set ambang 45 cm, anak berkali-kali kena alarm di 40 cm, tetapi PDF menulis "0 pelanggaran, Aman". Sebaliknya, ambang 20 cm dengan anak di 25 cm: tidak ada alarm, tetapi laporan "Perhatian".
- **Fix:** Satu sumber ambang. `CycleAggregator` menerima `Thresholds` dari Flow yang sama. Pelanggaran = proporsi sampel (misal ≥ 30%), bukan satu sampel. Teks PDF diformat dari nilai aktual.

### T10. Kalibrasi memakai tinggi wajah mentah, runtime memakai tinggi terkoreksi pitch — jarak selalu meleset (fix 305d553 tidak menyentuh akar)
- **Lokasi:** `CalibrationViewModel.kt:100` (`f = h_raw·30/H`) vs `DistanceEstimator.kt:40-50` (`d = H·f·cos(p)/h`)
- **Rumus:** `d_tampil = D_nyata × cos(pitch saat kalibrasi)`. Contoh: pitch 15° → 28,98 cm; 20° → 28,19 cm; 25° → 27,19 cm, padahal HP di 30 cm. Dengan ambang 30 cm, posisi kalibrasi sendiri sudah memicu alarm, dan ini cocok dengan keluhan "jarak tidak sesuai setelah kalibrasi".
- **Fix:** Kalibrasi memakai tinggi wajah yang sudah dikompensasi pitch dengan rumus yang sama persis dengan runtime (satu fungsi bersama). Tambahkan unit test round-trip: kalibrasi di 30 cm pada pitch X ⇒ runtime 30 ± 1 cm.

### T11. Syarat stabil kalibrasi hanya lolos jika rentang sampel tepat 1500 ms
- **Lokasi:** `detection/CalibrationManager.kt:28` (buang bila span > 1500) dan `:41` (syarat span ≥ 1500)
- **Hasil simulasi (500 run, wajah diam sempurna):**

  | Kondisi | Selesai dalam 120 dtk | Median |
  |---|---|---|
  | 30 fps dengan jitter | 99–100% | 9–18 dtk |
  | 24 fps | 0% | — |
  | 15 fps (ruangan redup) | 0–7% | — |
  | Setelah logika diperbaiki | 100% | ~1,7 dtk |

  Unit test lulus hanya karena timestamp sintetis 0/500/1000/1500.
- **Dampak:** User menyerah lalu menekan Lewati, sehingga jarak tak terkalibrasi (focal 560).
- **Fix:** Pangkas dengan `while (size ≥ 2 && t − q[1].t ≥ 1500) removeFirst()`, lalu syarat `t − q[0].t ≥ 1500`. Tambahkan test dengan jitter dan fps 15/24/30.

### T12. Rotasi analisis kamera dikunci saat service dibuat — di landscape wajah miring 90° *(mekanisme CONFIRMED, besaran PLAUSIBLE)*
- **Lokasi:** `DetectionForegroundService.kt:218-223` (tanpa `setTargetRotation`, tanpa `OrientationEventListener`)
- **Dampak:** Anak menonton video landscape, yaitu skenario paling umum. Akibatnya wajah tidak terdeteksi (alarm padam via T7) atau jarak terbaca ~14% lebih jauh.
- **Fix:** `OrientationEventListener` → `imageAnalysis.targetRotation`, atau putar bitmap sesuai rotasi display saat ini. Uji di perangkat dalam portrait/landscape.

### T13. Layar "Statistik Hari Ini" hampir seluruhnya data dummy
- **Lokasi:** `ui/parent/DailyStatsScreen.kt:55,58,129,199-226,319,372`; `strings_screen02_06.xml:46,66`
- **Masalah:** Grafik per jam, tanggal "Minggu, 1 November", chip "Aman", dan kalimat "80% waktumu… aman" semuanya statis. Hanya 2 angka kartu yang berasal dari DB. Layar ini juga bisa dibuka anak dari Beranda.
- **Fix:** Query snapshot hari ini (zona lokal) dikelompokkan per jam. Tanggal dari `LocalDate.now()`. Chip dan insight dihitung dari data.

### T14. Grafik Dashboard masih menggambar data dummy dan jarak "estimasi" karangan (B12 tidak tuntas)
- **Lokasi:** `ui/parent/DashboardScreen.kt:311,322-331,422,437,452,504`
- **Masalah:** Bila data kosong, kurva `DummyDailyCm` tetap tergambar di bawah teks "Belum ada riwayat". Hari tanpa snapshot diisi `ambang + 5 − pelanggaran × 2,5`. Label "50 cm" dan label hari "S S R K J S M" statis.
- **Fix:** Hapus dummy dan estimasi. Hari tanpa data = celah di grafik. Label ambang = nilai aktif, label hari dari tanggal sebenarnya. Simpan rata-rata jarak harian di `daily_stats`.

### T15. Klaim draft listing Play Store tidak sesuai implementasi — risiko ditolak/suspend (Families Policy)
- **Lokasi:** `docs/Listing_PlayStore_Draft.md`
- **Klaim yang tidak benar:**

  | Klaim | Kenyataan |
  |---|---|
  | "Zero Data Collection / offline" | ML Kit Face Detection mengirim data diagnostik (lihat M14) |
  | "Aturan 20-20-20 setiap 20 menit" | Tidak pernah dipicu (M19) |
  | "Target waktu bermain harian" | Tanpa efek (M27) |
  | "PIN 4 digit yang aman" | Default 1234 (T1) |
  | "misal < 35 cm" | Default 30, onboarding menulis 50 |
  | "Cegah miopia", PDF "STATUS KLINIS / Validasi Klinis / Dokter Spesialis Mata" | Dibantah disclaimer di app sendiri: "Bukan alat diagnosis/pencegah miopia" |
  | Target usia 3+ | Tidak ada kebijakan privasi yang bisa dibuka (A14) |

- **Fix:** Sebelum rilis:
  - Verifikasi merged manifest (`aapt dump permissions`).
  - Hapus ML Kit, atau ungkapkan di Data Safety.
  - Implementasikan fitur yang diklaim, atau hapus klaimnya.
  - Ganti klaim medis dengan "pengingat kebiasaan, bukan alat medis".
  - Sediakan URL kebijakan privasi yang akurat (pemrosesan kamera on-device dan SDK pihak ketiga).

---

## 4. 🟡 SEDANG

| ID | Temuan | Lokasi utama | Fix singkat |
|---|---|---|---|
| M1 | Usia diubah setelah kalibrasi menggeser semua jarak (misal 8→12 th: ×1,07, 28 cm terbaca 30 → tidak BLOCKED). Usia tidak disimpan di onboarding. Tidak ada ambang per usia (usia hanya mengubah H) | `ChildProfileScreen.kt:205`, `DistanceEstimator` | Simpan H saat kalibrasi, lalu skala ulang focal saat usia berubah atau minta kalibrasi ulang |
| M2 | Banyak wajah di frame: tidak dipilih wajah terdekat, sehingga wajah ortu bisa menutupi anak *(PLAUSIBLE)* | `FaceDetectorManager.kt:95,184,235` | `setNumFaces(3)` + pilih bbox terbesar |
| M3 | Satu frame tanpa wajah mereset debounce BLOCKED 500 ms | `DetectionStateMachine.kt:79` | Toleransi 1–2 frame hilang |
| M4 | Jam sistem mundur: throttle frame wall-clock membuang **semua** frame, sehingga deteksi mati total selama selisih waktu. Debounce juga beku | `DetectionForegroundService.kt:225-232`, `FaceDetectorManager.kt:158-163` | `SystemClock.elapsedRealtime()` untuk semua timeline deteksi |
| M5 | Fallback ML Kit memakai definisi bbox berbeda dari kalibrasi MediaPipe, sehingga jarak bias *(PLAUSIBLE)* | `FaceDetectorManager.kt:99-104,245,253` | Faktor koreksi per backend atau kalibrasi per backend |
| M6 | Interpreter TFLite tidak pernah di-close (bocor tiap sesi). Gagal load → pitch `null` permanen tanpa fallback ke landmark | `ModelPitchBackend.kt:79-82,124-129` | Panggil `close()` di `onDestroy`; fallback ke `LandmarkPitchBackend` saat gagal |
| M7 | TOO_DARK/WARNING tetap dihitung walau tidak ada wajah atau HP telungkup, sehingga ada pelanggaran dan notifikasi palsu malam hari | `DetectionForegroundService.kt:282`, `DetectionStateMachine.kt:125` | Evaluasi gelap hanya bila ada wajah |
| M8 | `dayEpoch` = epoch/86.400.000 (UTC). Di WIB, rollup 27 Nov tersimpan sebagai 26 Nov, dan "hari" snapshot berjalan 07:00–06:59 (B6 tidak tuntas) | `DailyRollupWorker.kt:42`, `DetectionForegroundService.kt:292` | `LocalDate.now(zone).toEpochDay()` di semua tempat + migrasi data |
| M9 | Rollup menulis baris 0-siklus tiap malam, sehingga tombol PDF aktif tanpa pemantauan, PDF "7 hari Aman", dan streak = hari sejak instal (+1 ganda) | `DailyRollupWorker.kt:49-57`, `BerandaScreen.kt:511` | Lewati upsert bila 0 siklus; streak dihitung dari hari dengan siklus valid berturut-turut |
| M10 | Label sama, rumus beda: "Jarak rata-rata 7 hari" di Dashboard ternyata 1 siklus terakhir; Beranda/Statistik memakai `getAll()` 30 hari; "Waktu main" = jumlah snapshot all-time | `DashboardScreen.kt:339-341`, `BerandaScreen.kt:504-512` | `StatsRepository` tunggal dengan jendela waktu eksplisit |
| M11 | Hari yang terlewat tidak pernah di-rollup ulang; "7 Hari Terakhir" = 7 baris terakhir, bukan 7 hari kalender; hari ini tidak masuk laporan | `DailyRollupWorker.kt:27-44`, `WeeklyReportScreen.kt:84` | Backfill dari hari terakhir ter-rollup; query rentang tanggal |
| M12 | File PDF/PNG berisi nama, usia, dan statistik anak tertinggal di `cacheDir/reports` setelah "Hapus Semua Data" yang tetap menyatakan sukses | `ReportGenerator.kt:125-126`, `AppNav.kt:250-257` | Hapus `cacheDir/reports` + `cancelAll()` notifikasi; TTL file 24 jam |
| M13 | Isi PDF menyesatkan: ambang hardcoded, label "Kepatuhan Postur" berisi jumlah pelanggaran (terbalik), klaim "STATUS KLINIS", "p < 0.001", tanda tangan "Dokter Spesialis Mata" | `ReportGenerator.kt:196-221,343-356` | Hapus klaim klinis, tambah disclaimer, label sesuai isi |
| M14 | Klaim "tanpa internet": ML Kit 16.1.7 kemungkinan menggabungkan izin INTERNET dan mengirim telemetri *(PLAUSIBLE — cek merged manifest)* | `app/build.gradle.kts:145` | `tools:node="remove"` untuk INTERNET atau ganti ML Kit; perbarui teks privasi |
| M15 | Tidak ada boot receiver, dan izin battery optimization dideklarasikan tetapi tidak pernah diminta | `AndroidManifest.xml` | Receiver BOOT_COMPLETED (dengan aturan Android 14 untuk FGS kamera) + minta pengecualian baterai |
| M16 | Kegagalan `startForeground` ditelan, sehingga `isRunning=true` palsu. Jalur `stopSelf()` sebelum `startForeground` rawan crash (B9 tidak tuntas) | `DetectionForegroundService.kt:67-72,117,126-135,199-205` | Panggil `startForeground` paling awal; set `isRunning` hanya setelah sukses |
| M17 | `CycleAggregator.samples` (`mutableList`) di-`add` dari thread MediaPipe dan di-`clear` dari Main, sehingga bisa terjadi `ConcurrentModificationException` dan crash app (B10 tidak tuntas, = B28) | `CycleAggregator.kt:9`, `MinuteTicker.kt:136-140` | Mutex/synchronized atau channel; bungkus `evaluate()` dengan try |
| M18 | Kamera tetap menganalisis saat layar mati/terkunci: baterai boros, menit kosong dihitung siklus | grep `SCREEN_OFF` = 0 | Pause pipeline saat `SCREEN_OFF`; lewati snapshot `noFaceCount == sampleCount` |
| M19 | Layar Istirahat 20-20-20, AlarmBlocked, dan OverlayDenied hanya bisa dibuka dari DebugGallery; di release tidak pernah dipicu, padahal onboarding, lencana, PDF, dan listing menjanjikannya | `AppNav.kt:158`, `DebugGallery.kt:41-52` | Timer 20 menit di service → BreakScreen, atau hapus klaimnya |
| M20 | Kamera direbut app lain (video call) atau error CameraX: deteksi berhenti diam-diam, UI tetap "Memantau", snapshot kosong dihitung "bersih". Bind gagal tanpa retry | `DetectionForegroundService.kt:212-256` | Observe `cameraInfo.cameraState`; status "Tidak terpantau" + retry backoff |
| M21 | Wizard izin tidak menangani "Don't ask again" (tanpa jalur ke Pengaturan app); status izin basi setelah kembali dari Pengaturan | `PermissionWizardScreen.kt:63-98` | `shouldShowRequestPermissionRationale` + `ACTION_APPLICATION_DETAILS_SETTINGS`; re-check di ON_RESUME |
| M22 | Beranda anak: chip "Aman" tampil tanpa syarat, teks "minimal 30 cm" hardcoded, statistik all-time | `BerandaScreen.kt:476-479,502-512,624,634` | Chip dari status nyata hari ini; teks dari ambang aktif |
| M23 | Pesan ambang kontradiktif: onboarding "50 cm", default 30 cm, overlay "minimal 30 cm", label Dashboard "50 cm" | `strings_screen01_15.xml:21-24`, `overlay_warning.xml:26`, `strings_screen07_13.xml:44` | Format semua teks dari nilai ambang aktif |
| M24 | Lencana/hadiah bisa di-"farm" dengan menutup kamera atau meninggalkan HP, karena siklus tanpa wajah ikut dihitung | `RewardsScreen.kt:101-148`, `DetectionForegroundService.kt:290-306` | Hanya hitung siklus dengan wajah terdeteksi ≥ X% sampel |
| M25 | Empat definisi zona jarak berlaku bersamaan. Contoh di 47 cm (ambang 30): overlay WARNING "minimal 30 cm" sementara layar Pantau "Jarakmu sudah pas! 👍" | `DetectionStateMachine.kt:42,104-107` (`safeZoneCm=50` tetap), `PantauLiveScreen.kt:239-240,389-404`, `CycleAggregator.kt:60` | Satu kelas `DistanceZones(threshold)` dipakai semua layer |
| M26 | Dashboard tidak pernah menunjukkan pemantauan dijeda/berhenti; `isMonitoring` dikumpulkan tetapi tidak ditampilkan; state live tidak di-reset saat stop; jeda batal sendiri saat tab Pantau dibuka | `DashboardViewModel.kt:49-50`, `DashboardScreen.kt:548`, `PantauLiveScreen.kt:131-139` | Tampilkan status; reset `_riskLevel/_latest/_lastCycle` di `onDestroy`; simpan `monitoringPaused` |
| M27 | `dailyLimitMinutes` hanya tampil di Profil, tanpa enforcement apa pun; Beranda memakai skala 60 menit hardcoded (B25 masih ada) | `SettingsRepository.kt:37,61`, `BerandaScreen.kt:799` | Hitung menit hari ini di service → overlay "waktunya berhenti", atau hapus fiturnya |

---

## 5. ⚪ RENDAH (ringkas)

**Keamanan & privasi**
- **A7.** Hapus data tidak atomik dan bisa ditimpa siklus yang sedang berjalan; back saat proses berjalan menyebabkan hapus parsial (V-A4). Fix: `clearAllTables()` dalam scope aplikasi; stop service dulu.
- **A8/U7.** `SettingsViewModel` dibuat sebagai default parameter, sehingga bocor per buka layar (klaim "loop rekomposisi" ditolak).
- **A10.** Hash PIN SHA-256 satu iterasi, salt = timestamp, perbandingan non-constant-time; README menulis "terenkripsi" padahal tidak. Fix: PBKDF2 ≥ 100k iterasi + salt acak `SecureRandom` + `MessageDigest.isEqual`.
- **A11.** Dialog Ubah PIN: tanpa batas percobaan PIN lama dan validasi PIN lemah minimal.
- **A12.** Tidak ada route guard. Deep link Navigation via intent extras ke route ortu masuk akal lewat adb/app lain *(PLAUSIBLE)*.
- **A13.** Izin/konfigurasi berlebih: `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` tak dipakai, `RECEIVE_BOOT_COMPLETED` tanpa receiver, path FileProvider `export/` tak dipakai, password keystore default hardcoded.
- **A14.** Toggle "0 foto tersimpan" dan baris "Kebijakan privasi" di layar Privasi tidak berfungsi.
- **B37** (backlog). `FLAG_SECURE` belum ada di layar PIN dan data anak.
- **V-A2.** Dialog Ubah PIN menerima digit Unicode (Arab/fullwidth) lewat `isDigit()`, padahal keypad hanya 0-9, sehingga ortu bisa terkunci permanen. Fix: `c in '0'..'9'`.
- **V-A3.** Share laporan: nama file tetap + grant URI tidak dicabut, nama anak ikut di subject/text. Fix: nama unik, `revokeUriPermission`, minimalkan PII.
- **V-A5.** Layar Statistik (package ortu) bisa dibuka dari Beranda. Isinya read-only, tetapi tanpa guard.

**Deteksi & service**
- **D12.** Bias netral ±21–27° di `LandmarkPitchBackend` (jalur mati di produksi).
- **D15.** Callback deteksi terlambat setelah `close()` tetap memanggil `requestLevel`.
- **D16.** Crop untuk model pitch tidak sesuai kontrak training; MAE 4,37° belum divalidasi pada model int8 yang dibundel (model int8 berbeda 0,1–3,6° dari float).
- **D17.** `toPixelBox` meng-clamp bbox ke tepi frame, sehingga tinggi wajah mengecil saat wajah sebagian keluar.
- **D18.** EMA jarak tidak di-reset saat wajah hilang/focal berubah; parameter `faceWidthPx` mati.
- **D19.** `ImageProxy.toBitmap()` via refleksi rentan R8; fallback YUV naif.
- **D20.** `startedAtMs` siklus sebenarnya waktu akhir; `cycleTimestampsMs` tumbuh tanpa batas.
- **D21.** `lastFrame` dibaca dari callback async (bisa frame lain); bitmap bisa di-recycle saat dipakai.
- **S9.** Urutan cleanup `onDestroy` tidak tepat; TTS `shutdown()` tanpa pemanggil.
- **S12.** Re-alert masih berulang saat jarak naik-turun di sekitar ambang; judul notifikasi berisi enum mentah.
- **S13.** Isi visibilitas/thread kecil di jalur deteksi→overlay.
- **S14.** Banner WARNING bisa menelan sentuhan di Android 12+.
- **V-D3.** Mode Jangan Ganggu: channel `IMPORTANCE_DEFAULT` tanpa `CATEGORY_ALARM`, getar tanpa atribut alarm, TTS di stream media tanpa audio focus; banner WARNING tanpa suara/getar.
- **V-D4.** Spam Start/Stop: callback basi dari instance lama bisa membuat BLOCKED pertama di sesi berikutnya ter-dedupe (memperparah K1). Fix: token sesi + debounce tombol.
- **V-D5.** Doze tanpa WakeLock: siklus "1 menit" bisa molor puluhan menit.

**Data & laporan**
- **R10.** Ekspor PDF/PNG tanpa penanganan error dan dirender di main thread.
- **R11.** Room versi 1 + `fallbackToDestructiveMigration` + tanpa export schema (B33); tanpa index; tabel `violation_logs`/`app_settings` mati.
- **R12.** `getAll()` tanpa batas + diolah di Main + one-shot (bukan Flow).
- **R13.** Insert snapshot & violation tidak atomik; siklus parsial dibuang.
- **R14.** Tanpa penanganan error DataStore; default "Sahabat (8 Tahun)" masuk PDF.
- **R15.** Unit test mengunci perilaku UTC; tidak ada test rollup/zona waktu.

**UI & sinkronisasi**
- **U6.** Toggle "Mode Ringan" tidak menurunkan fps kamera (tetap 5 fps), padahal deskripsinya "1 frame tiap 10 detik"; "Banner saat aman" tanpa konsumen.
- **U15.** Back di layar Izin menghasilkan layar kosong; slide onboarding tanpa back antar halaman (B19).
- **U16.** PantauLive menampilkan data basi setelah dijeda; kartu cahaya memakai 150 lux hardcoded.
- **U17.** Validasi input profil lemah.
- **U18.** Collect tanpa lifecycle, string hardcoded, `contentDescription` kurang untuk kontrol anak.
- **V-S5.** README tidak sesuai kode (onboarding 30 cm, BreakScreen ✅, "DataStore terenkripsi").
- **V-S6.** Syarat lencana tidak sesuai labelnya ("7 hari jarak aman" terbuka setelah 1 menit, "5 kali istirahat" = 5 siklus).
- **V-S7.** Nilai default ("Sahabat", 8, "Kelas 3 SD", 30f) tersebar sebagai literal di 4–6 tempat; tap stepper sebelum DataStore emit bisa menimpa nilai tersimpan.

---

## 6. Matriks sinkronisasi (ringkas)

| Nilai | Sumber tunggal? | Realtime ke service/UI? | Masalah |
|---|---|---|---|
| Ambang jarak | DataStore ✓ | Alarm ✓ · statistik/PDF/notif ✗ | 4 definisi zona (M25), konstanta 30 (T9) |
| Ambang lux | DataStore ✓ | Alarm ✓ · lainnya ✗ | Konstanta 150 di 5 tempat |
| Ambang pitch | DataStore ✓ | Alarm ✓ · statistik ✗ | Konstanta 30° |
| Usia / tinggi wajah | DataStore ✓ | ✓ | Tidak ditanya di onboarding; ubah usia menggeser jarak (M1) |
| Nama anak | DataStore ✓ | ✓ | "Belum diisi" = "Sahabat" |
| Batas harian | DataStore ✓ | ✗ nol konsumen | Tanpa enforcement (M27) |
| Status pantau | In-memory saja | PantauLive ✓ · Dashboard ✗ | Ortu tidak tahu jeda (M26) |
| Pelanggaran hari ini | — | ✗ | 4 jendela waktu berbeda, tidak ada yang "hari ini" |
| Jarak rata-rata | — | Sebagian | 4 rumus untuk label yang sama (M10) |
| PIN / kunci | DataStore ✓ | — | Default 1234, lockout & sesi tidak persisten (T1, T3, T4) |
| Mode ringan / banner aman | DataStore ✓ | ✗ | Tidak berefek (U6) |

---

## 7. Urutan perbaikan yang disarankan

1. **K1:** kembalikan re-show overlay selama BLOCKED + unit test durasi tampil. *Hotfix, paling mendesak.*
2. **Anti-bypass:**
   - T5: PIN untuk jeda, heartbeat/gap log, status di Dashboard, boot receiver.
   - T7: kamera tertutup ≠ OK.
   - T6: kalibrasi hanya dari mode ortu + batas nilai wajar.
   - T8: blokir jalan tanpa kanal alarm.
3. **PIN & sesi:** T1 + T2 (buat PIN dan onboarding wajib), T3 (lockout persisten), T4 (re-lock + route guard).
4. **Akurasi deteksi:** T10 (rumus kalibrasi = runtime), T11 (syarat stabil), T12 (rotasi), M4 (`elapsedRealtime`).
5. **Sinkronisasi data:** T9 + M25 (`Thresholds`/`DistanceZones` tunggal), M8 (`dayEpoch` lokal), M9–M11, lalu hapus semua dummy (T13, T14, M22).
6. **Sebelum rilis Play Store:** T15, M13, M14, M12, A14 (kebijakan privasi), B37 (`FLAG_SECURE`).
7. **Validasi runtime di perangkat:** semua item *(PLAUSIBLE)*, terutama T12, M2, M5, M14 (merged manifest), V-A1 (Stop dari Task Manager).

---

## 8. Status perbaikan (diperbarui seiring kerja berjalan)

| Item | Status | Catatan |
|---|---|---|
| **K1** | ✅ FIXED (28 Nov) | `OverlayService.requestLevel` kini mengirim ulang untuk WARNING/BLOCKED saat overlay tidak tampil (interval 4 dtk, jeda 3 dtk setelah tombol tutup); tap area kosong tidak lagi menutup overlay; 5 unit test `OverlayReshowTest` (simulasi BLOCKED 60 dtk ⇒ visibilitas ≥ 90%) |
| T1–T15, M1–M27, rendah | ⏳ berjalan | Dikerjakan sesuai urutan §7 |

---
*Detail bukti per temuan (file:baris, trace, skrip replika) tersedia di berkas kerja audit: `A_keamanan.md`, `B_deteksi.md`, `C_service.md`, `D_data.md`, `E_ui.md` (putaran 1) dan `V1_keamanan_ui.md`, `V2_deteksi_service.md`, `V3_data_sinkron.md` (putaran 2).*
