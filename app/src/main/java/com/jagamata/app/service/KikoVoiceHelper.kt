package com.jagamata.app.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import com.jagamata.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Helper suara ramah maskot Kiko untuk anak (Blueprint D1 & §4.1).
 * Menggunakan engine TextToSpeech bawaan Android (tanpa menambah MB binary).
 * Diberi throttling (minimal 25 detik) agar tidak mengganggu anak saat bermain ponsel.
 */
object KikoVoiceHelper {
    private const val TAG = "KikoVoiceHelper"
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var lastSpokenMs: Long = 0L
    private const val THROTTLE_INTERVAL_MS = 25_000L // Min 25 detik antar ucapan

    fun initialize(context: Context) {
        if (tts != null) return
        val appContext = context.applicationContext
        tts =
            TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val localeId = Locale("id", "ID")
                    val langResult = tts?.setLanguage(localeId)
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Bahasa Indonesia tidak tersedia pada engine TTS, menggunakan bahasa default")
                        tts?.language = Locale.getDefault()
                    }
                    tts?.setSpeechRate(0.95f) // Tempo ramah anak sedikit santai
                    tts?.setPitch(1.15f) // Nada maskot sedikit lebih ceria/tinggi
                    isReady = true
                    Log.i(TAG, "Kiko Voice TTS siap digunakan")
                } else {
                    Log.e(TAG, "Gagal menginisialisasi TextToSpeech: status=$status")
                }
            }
    }

    fun speak(
        context: Context,
        message: String,
        force: Boolean = false,
    ) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastSpokenMs < THROTTLE_INTERVAL_MS)) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val settings = SettingsRepository(context.applicationContext)
            val enabled = settings.voiceEnabled.first()
            if (!enabled) return@launch

            Handler(Looper.getMainLooper()).post {
                if (isReady && tts != null) {
                    lastSpokenMs = now
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "kiko_utterance_$now")
                }
            }
        }
    }

    /** Peringatan jarak terlalu dekat. */
    fun speakWarningClose(context: Context) {
        speak(context, "Ups! Mundur sedikit yuk, layarnya terlalu dekat dengan matamu!")
    }

    /** Peringatan ruangan terlalu redup/gelap. */
    fun speakWarningDark(context: Context) {
        speak(context, "Wah, ruangannya agak redup nih! Nyalakan lampu yang terang ya!")
    }

    /** Peringatan istirahat 20-20-20. */
    fun speakBreak202020(context: Context) {
        speak(context, "Hebat! Sudah 20 menit, yuk istirahat sebentar dan lihat benda yang jauh!", force = true)
    }

    /** Konfirmasi kembali ke posisi aman. */
    fun speakSafe(context: Context) {
        speak(context, "Nah, pintar! Jarak matamu sekarang sudah aman.")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
