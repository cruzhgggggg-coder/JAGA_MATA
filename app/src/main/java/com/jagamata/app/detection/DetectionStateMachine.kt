package com.jagamata.app.detection

/**
 * State machine risiko (PRD §2.3–6, §5.4, US-04).
 *
 * - BLOCKED : jarak < 30 cm bertahan 500 ms. Hysteresis: keluar hanya setelah
 *             > 35 cm bertahan 2.000 ms (US-04) — atau wajah hilang 2.000 ms.
 * - WARNING : zona Waspada 30–50 cm (§2.23) ATAU pitch > 30° bertahan 3.000 ms
 *             ATAU lux < 150 bertahan 10.000 ms ATAU baru turun dari BLOCKED.
 * - OK      : jarak ≥ 50 cm (zona Aman §2.23) bertahan 2.000 ms tanpa warning lain.
 * - Prioritas BLOCKED > WARNING > OK (§2.6). `null` = fitur tak terdeteksi,
 *   bukan 0 (US-03) — kondisi terkait dianggap tidak aktif.
 *
 * Catatan: syarat keluar BLOCKED memakai angka §5.4 (>35 cm / 2 dtk); syarat
 * state OK memakai zona Aman ≥50 cm (§2.23) supaya banner WARNING benar-benar
 * tampil di rentang 30–50 cm. Terdokumentasi di ASSUMPTION LOG.
 */
class DetectionStateMachine(
    private val config: Config = Config(),
    private val clock: () -> Long = System::currentTimeMillis
) {

    data class Config(
        val closeEnterCm: Float = 30f,
        val closeExitCm: Float = 35f,
        val safeZoneCm: Float = 50f,
        val pitchThresholdDeg: Float = 30f,
        val luxThreshold: Float = 150f,
        val closeDebounceMs: Long = 500L,
        val poseDebounceMs: Long = 3_000L,
        val darkDebounceMs: Long = 10_000L,
        val okDebounceMs: Long = 2_000L
    )

    private var state: RiskLevel = RiskLevel.OK

    private var closeSinceMs: Long? = null
    private var exitSinceMs: Long? = null
    private var faceLostSinceMs: Long? = null
    private var poseSinceMs: Long? = null
    private var darkSinceMs: Long? = null
    private var safeSinceMs: Long? = null

    fun current(): RiskLevel = state

    /** Evaluasi satu sampel dan kembalikan state baru. */
    fun onResult(result: DetectionResult): RiskLevel {
        val now = result.timestampMs
        val distance = result.distanceCm
        val pitch = result.pitchDeg
        val lux = result.lux

        closeSinceMs = tick(closeSinceMs, distance != null && distance < config.closeEnterCm, now)
        exitSinceMs = tick(exitSinceMs, distance != null && distance > config.closeExitCm, now)
        faceLostSinceMs = tick(faceLostSinceMs, distance == null, now)
        poseSinceMs = tick(poseSinceMs, pitch != null && pitch > config.pitchThresholdDeg, now)
        darkSinceMs = tick(darkSinceMs, lux != null && lux < config.luxThreshold, now)
        safeSinceMs = tick(
            safeSinceMs,
            distance != null && distance >= config.safeZoneCm && pitch != null && pitch <= config.pitchThresholdDeg,
            now
        )

        val closeSustained = sustained(closeSinceMs, config.closeDebounceMs, now)
        val exitedClose = sustained(exitSinceMs, config.okDebounceMs, now) ||
            sustained(faceLostSinceMs, config.okDebounceMs, now)
        val poseSustained = sustained(poseSinceMs, config.poseDebounceMs, now)
        val darkSustained = sustained(darkSinceMs, config.darkDebounceMs, now)
        val safeSustained = sustained(safeSinceMs, config.okDebounceMs, now)
        val inWarnZone = distance != null && distance >= config.closeEnterCm && distance < config.safeZoneCm

        state = when {
            closeSustained -> RiskLevel.BLOCKED
            state == RiskLevel.BLOCKED && !exitedClose -> RiskLevel.BLOCKED
            poseSustained || darkSustained -> RiskLevel.WARNING
            inWarnZone -> RiskLevel.WARNING
            state == RiskLevel.BLOCKED && !safeSustained -> RiskLevel.WARNING
            safeSustained && !poseSustained && !darkSustained -> RiskLevel.OK
            else -> state
        }
        return state
    }

    fun reset() {
        state = RiskLevel.OK
        closeSinceMs = null
        exitSinceMs = null
        faceLostSinceMs = null
        poseSinceMs = null
        darkSinceMs = null
        safeSinceMs = null
    }

    private fun tick(since: Long?, active: Boolean, now: Long): Long? = when {
        active -> since ?: now
        else -> null
    }

    private fun sustained(since: Long?, windowMs: Long, now: Long): Boolean =
        since != null && now - since >= windowMs

    companion object {
        /** Latensi maksimum alarm dari masuk kondisi ke status BLOCKED (KPI K5). */
        const val MAX_ALARM_LATENCY_MS = 1_500L
    }
}
