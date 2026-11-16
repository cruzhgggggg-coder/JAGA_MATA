# Instruksi Verifikasi Manual KPI K5–K10 — JagaMata

PRD BAB 4 · ditulis agent (MiMoCode) · status: **menunggu verifikasi user**
K1–K4 sudah diverifikasi otomatis (lihat `k1_k2_k3.txt` dan `k4.txt`).

Semua pengukuran dilakukan pada HP fisik (PRD: hardware uji tidak diasumsikan
tersedia untuk agent). Siapkan: minimal 1 HP Android (idealnya 3 merek berbeda
untuk dataset D1), penggaris 30 cm, meteran cahaya (opsional, bisa pakai app
lux meter), stopwatch.

---

## K5 — Latensi alarm ≤ 1,5 detik

**Target:** dari saat wajah masuk jarak <30 cm sampai overlay BLOCKED tampil,
maksimal 1,5 detik.

**Alat:** 10 video percobaan (PRD D2/K5) — rekam layar + subjek.

**Langkah:**
1. Aktifkan pemantauan (tombol Mulai di dasbor).
2. Rekam layar dengan `adb shell screenrecord /sdcard/k5.mp4` atau perekam
   bawaan (aktifkan "tampilkan sentuhan" untuk sinkron).
3. Bawa wajah anak masuk ke <30 cm dari layar; tandai momen masuk (frame saat
   jarak terdeteksi <30 cm di layar debug).
4. Hitung frame sampai overlay merah "Ayo mundur dikit!" muncul penuh.
5. Ulangi 10 kali. Catat tiap hasil di tabel.

**Kriteria lulus:** ≥9 dari 10 percobaan ≤1,5 detik.
**Bukti:** 10 video + tabel latensi (detik per percobaan).

---

## K6 — FPS analisis ≥5 di HP RAM 3 GB

**Target:** pipeline kamera memproses minimal 5 frame/detik.

**Langkah:**
1. Pakai HP kelas bawah (RAM 3 GB) — misal Samsung A-series lawas / Redmi entry.
2. Buka layar debug (angka cm bergerak) dan amati kehalusan update angka selama
   60 detik.
3. Alternatif kuantitatif: `adb shell dumpsys gfxinfo com.jagamata.app` atau
   logcat filter `FaceDetectorManager` — hitung jumlah `detectAsync` per detik.
4. Konfigurasi yang harus terlihat di log build: analisis 5 fps @480p dengan
   `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST` (kode sudah membatasi ini).

**Kriteria lulus:** rata-rata ≥5 frame/detik.
**Bukti:** screenshot layar debug + catatan device model + hasil hitungan.

---

## K7 — Baterai ≤8%/jam (Normal) dan ≤3%/jam (Mode Ringan)

**Target:** konsumsi baterai selama pemantauan aktif.

**Langkah (2 × 30 menit):**
1. Isi baterai ke 100%, aktifkan Mode Normal. Catat % awal + jam awal.
2. Jalankan pemantauan 30 menit dengan layar sesuai pemakaian nyata
   (screen-on untuk uji K7 sesuai PRD).
3. Catat % akhir. Ekstrapolasikan ke per jam: `persen_per_jam = (awal - akhir) × 2`.
4. Ulangi dengan Mode Ringan aktif (Settings → toggle Mode Ringan).
5. Pastikan log mencatat "pause saat layar mati" (kamera berhenti saat
   lifecycle STOP).

**Kriteria lulus:** Normal ≤8%/jam · Mode Ringan ≤3%/jam.
**Bukti:** screenshot pengaturan baterai sebelum/sesudah (2 set) + tabel.

---

## K8 — Offline (mode pesan)

**Target:** seluruh fitur jalan tanpa internet.

**Langkah:**
1. Aktifkan Mode Pesawat (Settings → Jaringan → Mode pesan).
2. Jalankan: onboarding, kalibrasi, pemantauan 5 menit, buka dasbor, buat
   laporan mingguan, bagikan (WhatsApp tetap butuh internet untuk *mengirim*,
   tapi pembuatan PNG harus sukses offline).
3. Verifikasi otomatis yang sudah ada: unit test K8 grep `http://`/`https://`
   di kode sumber — hanya boleh muncul di BuildConfig/dokumentasi.

**Kriteria lulus:** semua langkah di atas berfungsi.
**Bukti:** screenshot mode pesan + layar yang berfungsi.

---

## K9 — 0 foto tersimpan

**Target:** tidak ada gambar kamera yang ditulis ke disk.

**Langkah:**
1. Jalankan pemantauan 10 menit.
2. `adb shell run-as com.jagamata.app find . -type f \( -name '*.jpg' -o -name '*.png' -o -name '*.jpeg' \)`
   — harus kosong kecuali file laporan yang Anda buat sendiri via tombol Bagikan
   (`cacheDir/reports/laporan_mingguan_jagamata.png` — itu output laporan, bukan
   foto kamera).
3. Periksa juga `adb shell ls /sdcard/DCIM /sdcard/Pictures` — tidak ada tambahan
   dari JagaMata.

**Kriteria lulus:** tidak ada file foto kamera.
**Bukti:** output perintah `find` di atas.

---

## K10 — Overlay tampil di atas 5 aplikasi

**Target:** overlay BLOCKED muncul di atas aplikasi lain.

**Langkah:**
1. Izinkan "Tampilan di atas aplikasi lain" untuk JagaMata
   (Settings → Aplikasi → JagaMata → Izin khusus).
2. Aktifkan pemantauan.
3. Buka 5 aplikasi berbeda (misal: YouTube, WhatsApp, Chrome, Gallery, Game),
   satu per satu. Untuk tiap aplikasi, bawa wajah anak <30 cm dari layar.
4. Amati overlay merah menutupi aplikasi tersebut.

**Kriteria lulus:** overlay muncul di ke-5 aplikasi.
**Bukti:** 5 screenshot overlay di atas masing-masing aplikasi.

---

## Dataset D4–D7 (PRD BAB 4.5)

| Dataset | Isi | Status |
|---|---|---|
| D1 180 foto ground truth | 6 jarak × 3 cahaya × 10 · 3 merek HP · `ml/ground_truth/photos/*.jpg` + `labels.csv` | menunggu user (template CSV tersedia) |
| D2 12 video postur | tegak 15° / 30° / 45° × 4, label sudut ±5° | menunggu user |
| D3 log siklus 60 menit | export `check_snapshots` = tepat 60 baris, deviasi ±2 dtk | menunggu user (otomatisasi tersedia lewat K4 test) |
| D4 log baterai | 2 × 30 menit Normal & Mode Ringan | menunggu user (lihat K7) |
| D5 screenshot UI | semua layar ke `docs/screenshots/` | menunggu user |
| D6 tabel riwayat KPI | 10 baris K1–K10 di `docs/Laporan_Pengujian.md` | sebagian otomatis (K1–K4) |
| D7 riset pengguna | 8–10 wawancara ortu (opsional) | opsional |
