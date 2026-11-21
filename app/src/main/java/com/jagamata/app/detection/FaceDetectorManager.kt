package com.jagamata.app.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manajer deteksi wajah (PRD §5.1, DEV-13, fallback F1).
 *
 * Utama: MediaPipe FaceLandmarker (LIVE_STREAM, 1 wajah, model
 * `assets/face_landmarker.task`). Bila model gagal dimuat -> fallback
 * ML Kit Face Detection ("degraded mode": deteksi jarak + cahaya tetap jalan,
 * pitch dihitung dari crop wajah lewat [PitchBackend] bila model tersedia).
 */
class FaceDetectorManager(
    private val context: Context,
    private val pitchBackend: PitchBackend = LandmarkPitchBackend(),
    private val onResult: (DetectionResult) -> Unit,
) {
    private var landmarker: FaceLandmarker? = null
    private var mlkitDetector: com.google.mlkit.vision.face.FaceDetector? = null

    var activeBackend: DetectorBackend = DetectorBackend.NONE
        private set

    val isDegraded: Boolean get() = activeBackend == DetectorBackend.MLKIT

    private val closed = AtomicBoolean(false)

    /** Frame terakhir — dipakai untuk crop wajah ke backend pitch (PRD §5.5). */
    @Volatile
    private var lastFrame: Bitmap? = null

    @Volatile
    private var lastFrameWidth: Int = 480

    @Volatile
    private var lastFrameHeight: Int = 640

    @Volatile
    private var lastTimestampMs: Long = -1L

    private val distanceEstimator = DistanceEstimator()

    fun setChildAge(age: Int) {
        distanceEstimator.faceHeightCm = DistanceEstimator.getChildFaceHeightCm(age)
    }

    @Volatile
    var focalPx: Float = com.jagamata.app.data.SettingsRepository.DEFAULT_FOCAL_PX

    /** Inisialisasi backend. @return true bila siap menerima frame. */
    fun initialize(): Boolean {
        if (initMediaPipe()) {
            activeBackend = DetectorBackend.MEDIAPIPE
            Log.i(TAG, "MediaPipe FaceLandmarker aktif")
            return true
        }
        if (initMlKit()) {
            activeBackend = DetectorBackend.MLKIT
            Log.w(TAG, "degraded mode: MediaPipe gagal, memakai ML Kit")
            return true
        }
        activeBackend = DetectorBackend.NONE
        Log.e(TAG, "Tidak ada backend deteksi wajah yang tersedia")
        return false
    }

    private fun initMediaPipe(): Boolean {
        if (!modelExists(context)) {
            Log.w(TAG, "File $MODEL_ASSET tidak ada — lihat assets/MODEL_README.txt")
            return false
        }
        return try {
            val options =
                FaceLandmarker.FaceLandmarkerOptions.builder()
                    .setBaseOptions(
                        BaseOptions.builder()
                            .setModelAssetPath(MODEL_ASSET)
                            .build(),
                    )
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setNumFaces(1)
                    .setOutputFaceBlendshapes(false)
                    .setOutputFacialTransformationMatrixes(false)
                    .setResultListener { result, _ -> handleMediaPipeResult(result) }
                    .setErrorListener { e -> Log.e(TAG, "MediaPipe error: ${e.message}", e) }
                    .build()
            landmarker = FaceLandmarker.createFromOptions(context, options)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "Gagal memuat MediaPipe: ${t.message}")
            landmarker = null
            false
        }
    }

    private fun initMlKit(): Boolean {
        return try {
            val options =
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build()
            mlkitDetector = FaceDetection.getClient(options)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "Gagal memuat ML Kit: ${t.message}")
            mlkitDetector = null
            false
        }
    }

    /** Proses satu frame kamera. [timestampMs] harus monoton naik untuk LIVE_STREAM. */
    fun detect(
        imageProxy: ImageProxy,
        timestampMs: Long,
    ) {
        if (closed.get()) {
            imageProxy.close()
            return
        }
        val bitmap =
            try {
                ImageConverters.toBitmap(imageProxy)
            } finally {
                imageProxy.close()
            }
        if (bitmap == null) return
        detectBitmap(bitmap, timestampMs)
    }

    fun detectBitmap(
        bitmap: Bitmap,
        timestampMs: Long,
    ) {
        lastFrame = bitmap
        lastFrameWidth = bitmap.width
        lastFrameHeight = bitmap.height
        val monotonicTs =
            synchronized(this) {
                val next = if (timestampMs <= lastTimestampMs) lastTimestampMs + 1 else timestampMs
                lastTimestampMs = next
                next
            }
        when (activeBackend) {
            DetectorBackend.MEDIAPIPE -> {
                try {
                    val mpImage = BitmapImageBuilder(bitmap).build()
                    landmarker?.detectAsync(mpImage, monotonicTs)
                } catch (t: Throwable) {
                    Log.e(TAG, "detectAsync gagal: ${t.message}")
                }
            }
            DetectorBackend.MLKIT -> detectMlKit(bitmap, monotonicTs)
            DetectorBackend.NONE -> Unit
        }
    }

    private fun handleMediaPipeResult(result: com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult) {
        try {
            val landmarks = result.faceLandmarks().firstOrNull()
            if (landmarks == null) {
                onResult(
                    DetectionResult(
                        timestampMs = result.timestampMs(),
                        backend = DetectorBackend.MEDIAPIPE,
                    ),
                )
                return
            }
            val points = landmarks.map { LandmarkPoint(it.x(), it.y(), it.z()) }
            val frame = lastFrame
            val fw = frame?.width ?: lastFrameWidth
            val fh = frame?.height ?: lastFrameHeight
            // Landmark MediaPipe ternormalisasi [0,1] → skala ke piksel frame.
            val box = ModelPitchBackend.toPixelBox(points, fw, fh)
            val faceHeightPx = (box.bottom - box.top).toFloat()
            val faceWidthPx = (box.right - box.left).toFloat()
            val crop = frame?.let { ModelPitchBackend.cropFace(it, box) }
            val pitch =
                try {
                    pitchBackend.pitchDeg(points, crop)
                } finally {
                    crop?.recycle()
                }
            val faceHeight = faceHeightPx.takeIf { it > 0f }
            val faceWidth = faceWidthPx.takeIf { it > 0f }
            val distance = distanceEstimator.update(faceHeight, focalPx, pitch, faceWidth)
            onResult(
                DetectionResult(
                    timestampMs = result.timestampMs(),
                    faceHeightPx = faceHeight,
                    faceWidthPx = faceWidth,
                    pitchDeg = pitch,
                    distanceCm = distance,
                    backend = DetectorBackend.MEDIAPIPE,
                ),
            )
        } catch (t: Throwable) {
            Log.e(TAG, "handleMediaPipeResult error: ${t.message}", t)
        }
    }

    private fun detectMlKit(
        bitmap: Bitmap,
        timestampMs: Long,
    ) {
        val detector = mlkitDetector ?: return
        val input = InputImage.fromBitmap(bitmap, 0)
        detector.process(input)
            .addOnSuccessListener { faces ->
                val face = faces.firstOrNull()
                if (face == null) {
                    onResult(
                        DetectionResult(
                            timestampMs = timestampMs,
                            backend = DetectorBackend.MLKIT,
                        ),
                    )
                    return@addOnSuccessListener
                }
                val box: Rect = face.boundingBox
                val crop = bitmap.let { ModelPitchBackend.cropFace(it, box) }
                val pitch =
                    try {
                        pitchBackend.pitchDeg(emptyList(), crop)
                    } finally {
                        crop?.recycle()
                    }
                val faceHeight = box.height().toFloat().takeIf { it > 0f }
                val faceWidth = box.width().toFloat().takeIf { it > 0f }
                val distance = distanceEstimator.update(faceHeight, focalPx, pitch, faceWidth)
                onResult(
                    DetectionResult(
                        timestampMs = timestampMs,
                        faceHeightPx = faceHeight,
                        faceWidthPx = faceWidth,
                        pitchDeg = pitch,
                        distanceCm = distance,
                        backend = DetectorBackend.MLKIT,
                    ),
                )
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "ML Kit gagal: ${e.message}")
            }
    }

    fun close() {
        closed.set(true)
        lastFrame = null
        try {
            landmarker?.close()
        } catch (_: Throwable) {
        }
        landmarker = null
        try {
            mlkitDetector?.close()
        } catch (_: Throwable) {
        }
        mlkitDetector = null
        activeBackend = DetectorBackend.NONE
    }

    companion object {
        private const val TAG = "FaceDetectorManager"
        const val MODEL_ASSET = "face_landmarker.task"

        /** Ketersediaan file model di assets/ (assets/MODEL_README.txt bila false). */
        fun modelExists(context: Context): Boolean =
            try {
                context.assets.open(MODEL_ASSET).use { true }
            } catch (_: Throwable) {
                false
            }

        fun boundingBoxOf(points: List<LandmarkPoint>): Rect {
            if (points.isEmpty()) return Rect(0, 0, 0, 0)
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var maxY = -Float.MAX_VALUE
            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
            return Rect(minX.toInt(), minY.toInt(), maxX.toInt(), maxY.toInt())
        }
    }
}

/** Konversi frame kamera -> Bitmap (dipisah supaya bisa diuji tanpa hardware). */
object ImageConverters {
    /**
     * Konversi [ImageProxy] (YUV_420_888) menjadi Bitmap yang sudah dirotasi
     * sesuai [ImageProxy.imageInfo.rotationDegrees].
     */
    fun toBitmap(imageProxy: ImageProxy): Bitmap? {
        if (imageProxy.image == null) return null
        val bitmap = imageProxy.toBitmapOrNull() ?: yuvToBitmapFallback(imageProxy)
        if (bitmap == null) return null
        return rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
    }

    private fun ImageProxy.toBitmapOrNull(): Bitmap? =
        try {
            // CameraX 1.3+ menyediakan konversi ini untuk format yang didukung.
            val method = ImageProxy::class.java.methods.firstOrNull { it.name == "toBitmap" && it.parameterCount == 0 }
            method?.invoke(this) as? Bitmap
        } catch (_: Throwable) {
            null
        }

    private fun yuvToBitmapFallback(imageProxy: ImageProxy): Bitmap? =
        try {
            val planes = imageProxy.planes
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
            val yuvImage =
                android.graphics.YuvImage(
                    nv21,
                    android.graphics.ImageFormat.NV21,
                    imageProxy.width,
                    imageProxy.height,
                    null,
                )
            val out = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(android.graphics.Rect(0, 0, imageProxy.width, imageProxy.height), 90, out)
            val bytes = out.toByteArray()
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (t: Throwable) {
            Log.w("ImageConverters", "Konversi YUV gagal: ${t.message}")
            null
        }

    fun rotateBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int,
    ): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    /** Ukuran output analisis yang disyaratkan PRD §2.2 (480p pada sisi terpanjang). */
    fun targetAnalysisSize(longEdge: Int = 480): android.util.Size {
        return android.util.Size(480, longEdge)
    }
}
