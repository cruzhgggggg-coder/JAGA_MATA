MODEL MEDIAPIPE — face_landmarker.task
=====================================

File ini WAJIB ada di app/src/main/assets/face_landmarker.task (PRD §2.20).

Status saat ini: TERPASANG ✅ (Full Tracking Mode aktif)
- File model face_landmarker.task (~3.58 MB) sudah berada di assets.
- FaceDetectorManager otomatis memakai MediaPipe FaceLandmarker
  (LIVE_STREAM, 1 wajah, 478 landmark 3D) untuk estimasi jarak dan postur.

CARA MEMASANG MODEL:
1. Unduh dari Google MediaPipe model maker / storage resmi:
   https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task
   (atau dari https://ai.google.dev/edge/mediapipe/solutions/vision/face_landmarker)
2. Simpan file hasil unduh sebagai:
   app/src/main/assets/face_landmarker.task
3. Build ulang: ./gradlew assembleDebug

SETELAH MODEL TERPASANG:
- FaceDetectorManager otomatis memakai MediaPipe FaceLandmarker
  (LIVE_STREAM, 1 wajah, 478 landmark) — pitch & jarak jadi lebih akurat.
- Tidak ada perubahan kode yang diperlukan.

CATATAN PRIVASI:
Model berjalan sepenuhnya on-device. Tidak ada gambar yang disimpan
atau dikirim ke jaringan (PRD BAB 1, KPI K8/K9).
