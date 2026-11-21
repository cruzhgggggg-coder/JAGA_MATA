package com.jagamata.app.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Backend pitch berbasis model hasil training rekan tim (PRD §5.5 drop-in).
 *
 * Model: MobileNetV3-Small multi-task [pitch, yaw, roll] (JagaMata-ML),
 * masukan crop wajah 224×224 RGB float32 ternormalisasi ImageNet (NCHW).
 *
 * Dua penyesuaian penting terhadap model:
 * 1. **Tanda** — model memakai konvensi negatif = menunduk; state machine JagaMata
 *    (PRD §2) memakai positif = menunduk → output pitch DINEGASI.
 * 2. **Guardrail yaw** — bila |yaw| ≥ 45° kepala menoleh jauh dari layar dan pitch
 *    tidak dipercaya (rekomendasi laporan training) → kembalikan null.
 *
 * Tanpa file model → [pitchDeg] null, aplikasi otomatis jatuh ke
 * [LandmarkPitchBackend] (F9: platform tetap penuh tanpa model).
 */
class ModelPitchBackend(
    private val context: Context,
    private val modelAsset: String = MODEL_ASSET,
) : PitchBackend {
    private val lock = Any()
    private var interpreter: Interpreter? = null
    private var loadFailed = false

    override fun requiresFaceCrop(): Boolean = true

    override fun pitchDeg(
        landmarks: List<LandmarkPoint>,
        faceCrop: Bitmap?,
    ): Float? {
        if (faceCrop == null) return null
        val interp = ensureInterpreter() ?: return null
        return try {
            synchronized(lock) {
                val input = preprocess(faceCrop) ?: return null
                val output = Array(1) { FloatArray(3) }
                interp.run(input, output)
                val pitch = output[0][0]
                val yaw = output[0][1]
                if (abs(yaw) >= YAW_GUARDRAIL_DEG) {
                    // Kepala menoleh jauh dari layar — pitch tidak dipercaya.
                    null
                } else {
                    // Konversi tanda ke konvensi JagaMata (positif = menunduk).
                    -pitch
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Inferensi pitch gagal: ${t.message}")
            null
        }
    }

    private fun ensureInterpreter(): Interpreter? {
        synchronized(lock) {
            interpreter?.let { return it }
            if (loadFailed) return null
            return try {
                val buffer = loadModelFile(context, modelAsset)
                val options =
                    Interpreter.Options().apply {
                        setNumThreads(2)
                    }
                Interpreter(buffer, options).also {
                    interpreter = it
                    Log.i(TAG, "Model pitch dimuat: $modelAsset")
                }
            } catch (t: Throwable) {
                loadFailed = true
                Log.w(TAG, "Model pitch tidak bisa dimuat (${t.message}) — fallback landmark")
                null
            }
        }
    }

    /**
     * Crop wajah 224×224 → tensor NCHW float32 ternormalisasi ImageNet.
     * mean = [0.485, 0.456, 0.406], std = [0.229, 0.224, 0.225].
     */
    private fun preprocess(source: Bitmap): ByteBuffer? {
        val resized =
            if (source.width == INPUT_SIZE && source.height == INPUT_SIZE) {
                source
            } else {
                Bitmap.createScaledBitmap(source, INPUT_SIZE, INPUT_SIZE, true)
            }
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        val buffer =
            ByteBuffer
                .allocateDirect(BYTES_PER_FLOAT * 3 * INPUT_SIZE * INPUT_SIZE)
                .order(ByteOrder.nativeOrder())
        for (c in 0 until 3) {
            val mean = MEAN[c]
            val std = STD[c]
            for (i in pixels.indices) {
                val p = pixels[i]
                val channel =
                    when (c) {
                        0 -> ((p shr 16) and 0xFF) / 255f
                        1 -> ((p shr 8) and 0xFF) / 255f
                        else -> (p and 0xFF) / 255f
                    }
                buffer.putFloat((channel - mean) / std)
            }
        }
        buffer.rewind()
        if (resized !== source) resized.recycle()
        return buffer
    }

    fun close() {
        synchronized(lock) {
            interpreter?.close()
            interpreter = null
        }
    }

    companion object {
        private const val TAG = "ModelPitchBackend"
        const val MODEL_ASSET = "pitch_model.tflite"
        const val INPUT_SIZE = 224
        const val BYTES_PER_FLOAT = 4
        const val YAW_GUARDRAIL_DEG = 45f
        const val MIN_FACE_PX = 40

        private val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val STD = floatArrayOf(0.229f, 0.224f, 0.225f)

        fun modelExists(context: Context): Boolean =
            try {
                context.assets.open(MODEL_ASSET).use { true }
            } catch (_: Throwable) {
                false
            }

        private fun loadModelFile(
            context: Context,
            assetName: String,
        ): ByteBuffer {
            return try {
                val fileDescriptor = context.assets.openFd(assetName)
                val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
                val fileChannel = inputStream.channel
                fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.startOffset,
                    fileDescriptor.declaredLength,
                )
            } catch (_: Throwable) {
                // Fallback direct buffer untuk lingkungan JVM / Robolectric
                context.assets.open(assetName).use { input ->
                    val bytes = input.readBytes()
                    val direct = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
                    direct.put(bytes)
                    direct.rewind()
                    direct
                }
            }
        }

        private fun abs(v: Float): Float = if (v < 0) -v else v

        /** Potong wajah dari frame penuh memakai bounding box piksel. */
        fun cropFace(
            frame: Bitmap,
            box: Rect,
        ): Bitmap? {
            val left = box.left.coerceIn(0, frame.width - 1)
            val top = box.top.coerceIn(0, frame.height - 1)
            val right = (box.right).coerceIn(left + 1, frame.width)
            val bottom = (box.bottom).coerceIn(top + 1, frame.height)
            val w = right - left
            if (w < MIN_FACE_PX || (bottom - top) < MIN_FACE_PX) return null
            return try {
                Bitmap.createBitmap(frame, left, top, w, bottom - top)
            } catch (_: Throwable) {
                null
            }
        }

        /** Skala landmark ternormalisasi [0,1] ke koordinat piksel frame. */
        fun toPixelBox(
            points: List<LandmarkPoint>,
            frameWidth: Int,
            frameHeight: Int,
        ): Rect {
            if (points.isEmpty() || frameWidth <= 0 || frameHeight <= 0) return Rect(0, 0, 0, 0)
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
            return Rect(
                (minX * frameWidth).toInt().coerceIn(0, frameWidth),
                (minY * frameHeight).toInt().coerceIn(0, frameHeight),
                (maxX * frameWidth).toInt().coerceIn(0, frameWidth),
                (maxY * frameHeight).toInt().coerceIn(0, frameHeight),
            )
        }
    }
}
