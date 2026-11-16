package com.jagamata.app.detection

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Monitor cahaya ruangan (PRD US-06): Sensor.TYPE_LIGHT selama service hidup.
 * <150 lux selama 10 dtk -> WARNING (tidak menimpa BLOCKED).
 * Sensor absen -> fitur disembunyikan + dicatat di log, BUKAN crash.
 */
class LightMonitor(
    context: Context,
    private val onLuxChanged: (Float) -> Unit = {},
) : SensorEventListener {
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    /** False bila perangkat tidak punya sensor cahaya — fitur disembunyikan. */
    val isAvailable: Boolean = lightSensor != null

    private var lastLux: Float? = null

    fun start(): Boolean {
        val sensor = lightSensor
        if (sensor == null) {
            Log.w(TAG, "Sensor cahaya tidak tersedia — deteksi gelap dinonaktifkan")
            return false
        }
        val registered =
            sensorManager?.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL,
            ) ?: false
        if (!registered) {
            Log.w(TAG, "Gagal mendaftarkan listener sensor cahaya")
        }
        return registered
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    /** Nilai lux terakhir yang diketahui (null bila belum pernah / tidak ada sensor). */
    fun lastLux(): Float? = lastLux

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_LIGHT) return
        val lux = event.values.firstOrNull() ?: return
        lastLux = lux
        onLuxChanged(lux)
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) = Unit

    companion object {
        private const val TAG = "LightMonitor"
    }
}
