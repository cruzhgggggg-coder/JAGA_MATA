# ASSUMPTION LOG — JagaMata

Keputusan agent sesuai PRD §0.2.c (hierarki keputusan). Semua angka teknis
mengikuti PRD BAB 2; di bawah hanya penafsiran yang perlu dicatat.

## Lingkungan

1. `JagaMata_v2.figcraft` TIDAK ada di folder project (PRD §2.21 menyebutnya
   "folder yang sama"). Referensi visual dipenuhi lewat export PNG di
   `design/referensi/` (01 Foundations/Tokens, 02 Mascot, 03 Screens 15 layar,
   04 App Icon, 05 Components, 06 Cover). Warna dari gambar 01 dikonfirmasi
   cocok dengan token PRD §2.22 (#F2F7FF, #FF7B6B, #5FD4A7, #FFD166).
2. `compileSdk = 37` (satu-satunya platform Android yang terpasang) dengan
   `targetSdk = 35` sesuai PRD. Angka PRD tidak berubah.
3. Gradle 8.9 + AGP 8.7.3 + Kotlin 1.9.24 + Compose BOM 2024.09.00 sesuai
   §2.16. Detekt 1.23.6 + ktlint (jlleitschuh 12.1.1) aktif dan lulus.
4. Git identitas per-commit via `git -c user.name/-c user.email` — config user
   tidak diubah.

## Logika deteksi

5. **Zona jarak vs state machine.** §5.4 menetapkan `OK = distanceCm > 35 cm
   selama 2000 ms`, sedangkan §2.23 menetapkan zona tampilan Aman ≥50 cm /
   Waspada 30–50 cm / Bahaya <30 cm dan "banner WARNING tampil di 30–50 cm".
   Keputusan: syarat *keluar dari BLOCKED* memakai §5.4 (hysteresis >35 cm
   selama 2 dtk — juga US-04), sedangkan *state OK* memakai zona Aman ≥50 cm
   sehingga banner WARNING benar-benar tampil di rentang 30–50 cm. Semua angka
   PRD tetap terpakai (30 / 35 / 50 / 500 ms / 2000 ms / 3000 ms / 10000 ms).
6. **Wajah hilang (`distanceCm == null`).** US-03: null, bukan 0. Kondisi close
   tidak aktif. Setelah 2.000 ms tanpa wajah, perlakuan sama dengan syarat
   keluar (analog ">35 cm selama 2 dtk") agar overlay tidak macet saat anak
   pergi. Tercatat sebagai interpretasi engineering.
7. **Pitch dalam fallback ML Kit = null (degraded mode).** ML Kit hanya
   memberi bounding box + ~5 landmark, bukan 478 titik seperti MediaPipe.
   Jarak dan cahaya tetap berfungsi (F1). Log "degraded mode" dicatat.
8. **Rumus pitch = atan2(dz, dy)** dengan v = chin − nose (nose landmark 1,
   chin landmark 152), persis frasa PRD §5.5 "geometri nose-chin atan2".
   Konvensi: positif = menunduk. Fixture sintetis 10° dan 40° dibuat
   konsisten dengan rumus ini (US-07).
9. `face_landmarker.task` BELUM dipasang → `assets/MODEL_README.txt` berisi
   URL unduhan; kode otomatis fallback ke ML Kit (F9 tetap terpenuhi:
   platform berjalan penuh tanpa file model).

## UI

10. Font **Baloo 2** + **Nunito** (PRD §2.22) dipasang sebagai
    `res/font/baloo2_variable.ttf` dan `res/font/nunito_variable.ttf`
    (variable font dari repositori google/fonts). Weight dirender lewat
    parameter `FontWeight` Compose.
11. Maskot Kiko (PRD §2.19) dibuat sebagai **placeholder geometris**
    (`res/drawable/mascot.xml`, ikon launcher vector) karena export dari file
    `.figcraft` tidak mungkin — persis izin yang diberikan PRD
    ("placeholder geometris HANYA jika export gagal").
12. Semua string UI di `strings.xml` Bahasa Indonesia (PRD BAB 6).

## Data & privasi

13. File laporan PNG ditulis ke `cacheDir/reports/` (bukan DCIM/Pictures) dan
    dibagikan lewat `FileProvider` — mematuhi K9 (tidak ada foto kamera ke disk).
14. Tabel `app_settings` di Room disediakan sesuai §2.10 untuk kebutuhan worker;
    sumber utama pengaturan tetap DataStore (§5.3 `SettingsRepository`).
15. PIN orang tua disimpan SHA-256 + salt (US-11, "implementasi minimal").

## Monetisasi & backlog

16. IAP billing (`premium_monthly` / `premium_yearly`) TIDAK dikerjakan —
    PRD §7 Fase 6 menyebutnya NICE-TO-HAVE dan Backlog mengecualikan
    "IAP billing sungguhan". Harga dicatat di draft Play Store.
