# Draft Listing Google Play Store — JagaMata v1.0.0

Status: **DRAFT JUJUR** — seluruh klaim diselaraskan dengan implementasi nyata (audit 28 Nov 2026, T15).
Sebelum publikasi: jalankan checklist §Verifikasi Pra-Rilis di bawah.

---

## Nama Aplikasi
**JagaMata: Jaga Jarak & Postur Layar Anak**

## Subjudul (Maks. 80 Karakter)
Pengingat kebiasaan layar sehat untuk anak — jarak, cahaya, postur.

## Deskripsi Singkat (Maks. 80 Karakter)
Pantau kebiasaan layar anak secara offline, bersama maskot Kiko.

## Deskripsi Lengkap

**JagaMata** adalah pengingat kebiasaan layar untuk anak. Aplikasi mendeteksi kebiasaan berisiko — terlalu dekat ke layar, ruangan gelap, dan postur menunduk — lalu mengingatkan lewat maskot Kiko. Semua pemrosesan berjalan **di perangkat (on-device)**; tidak ada foto/video yang disimpan atau dikirim.

### Fitur

1. **Pengingat Jarak Layar Real-Time**
   Estimasi jarak mata ke layar dari kamera (tanpa LiDAR/ToF). Saat jarak melewati batas yang diatur orang tua (default 30 cm), Kiko muncul mengingatkan anak untuk mundur.

2. **Deteksi Pencahayaan Ruangan**
   Mengingatkan saat ruangan terlalu gelap (default < 150 lux) agar anak menyalakan lampu.

3. **Pemantau Postur Kepala**
   Model di perangkat memperkirakan sudut kepala; postur menunduk berulang memicu peringatan.

4. **Maskot Kiko**
   Peringatan disampaikan dengan ramah lewat karakter burung hantu Kiko — bukan nada menyalahkan.

5. **Mode Orang Tua (terproteksi PIN)**
   Ambang batas jarak/lux/postur, kalibrasi jarak, statistik, dan laporan dilindungi PIN 4 digit yang dibuat saat onboarding (bukan PIN default).

6. **Statistik & Laporan Mingguan**
   Ringkasan kebiasaan layar 7 hari, grafik, dan ekspor laporan (PDF/PNG) untuk dibagikan ke WhatsApp keluarga.

### Batas yang Perlu Diketahui (jujur)
- Estimasi jarak berbasis ukuran wajah — presisi tergantung kalibrasi & pencahayaan; **bukan alat ukur medis**.
- Fitur istirahat 20-20-20, batas waktu bermain harian, dan target usia 3+ **belum** berfungsi penuh pada versi ini.
- Aplikasi ini **bukan** alat diagnosis/pencegah miopia — murni pengingat kebiasaan layar sehat.

### Privasi
- **Pemrosesan on-device:** frame kamera diproses di memori perangkat; tidak ada foto/video yang disimpan atau dikirim.
- **Izin INTERNET tidak dipakai** untuk fungsi inti (deteksi berjalan offline).
- Data statistik disimpan lokal (Room/DataStore) di HP anak.
- Anda dapat menghapus seluruh data kapan saja dari menu Privasi.

---

## Kategori Aplikasi
Parenting / Kesehatan & Kebugaran

## Target Usia (Age Rating)
Teen (13+) atau sesuai kebijakan Play — aplikasi memantau perangkat anak; wajib menyiapkan kebijakan privasi yang akurat sebelum publikasi.

## Kata Kunci Penelusuran (Keywords)
pengingat jarak layar, screen time anak, postur anak, kebiasaan layar, offline, maskot kiko

---

## Verifikasi Pra-Rilis (wajib sebelum submit)
- [ ] `aapt dump permissions` merged manifest → **tanpa INTERNET** (sudah `tools:node="remove"`)
- [ ] Data Safety form: deklarasikan "tidak ada data dikumpulkan/di-share"; kamera diproses on-device
- [ ] URL kebijakan privasi akurat (pemrosesan on-device + SDK pihak ketiga bila ada)
- [ ] Klaim medis dihapus (sudah: "pengingat kebiasaan, bukan alat medis")
- [ ] Tidak ada fitur yang diklaim belum berfungsi (sudah ditandai di "Batas yang Perlu Diketahui")
- [ ] Uji offline penuh (mode pesawat) seluruh sesi (KPI K8)
