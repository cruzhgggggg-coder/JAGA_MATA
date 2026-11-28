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
 *
 * Ambang/zona tidak hardcoded: `closeExit`/`safeZoneCm` diturunkan dari ambang
 * jarak pengguna lewat [Thresholds] (T9 + M25) — default 30/35/50 = desain.
 */
class DetectionStateMachine(
    config: Config = Config(),
) {
    var config: Config = config
        private set

    fun updateThresholds(
        closeEnterCm: Float? = null,
        closeExitCm: Float? = null,
        pitchThresholdDeg: Float? = null,
        luxThreshold: Float? = null,
    ) {
        config =
            config.copy(
                closeEnterCm = closeEnterCm ?: config.closeEnterCm,
                closeExitCm = closeExitCm ?: config.closeExitCm,
                pitchThresholdDeg = pitchThresholdDeg ?: config.pitchThresholdDeg,
                luxThreshold = luxThreshold ?: config.luxThreshold,
            )
    }

    /**
     * Terapkan seluruh ambang/zona dari satu sumber [Thresholds] (T9 + M25):
     * `closeExit`/`safeZoneCm` ikut turun dari ambang jarak pengguna
     * (exit = close+5, safe = close+20 — lihat [Thresholds.fromClose]),
     * sehingga tidak ada zona kedua yang terpisah.
     */
    fun updateThresholds(thresholds: Thresholds) {
        config =
            config.copy(
                closeEnterCm = thresholds.closeEnterCm,
                closeExitCm = thresholds.closeExitCm,
                safeZoneCm = thresholds.safeZoneCm,
                pitchThresholdDeg = thresholds.pitchDeg,
                luxThreshold = thresholds.lux,
            )
    }

    data class Config(
        val closeEnterCm: Float = 30f,
        val closeExitCm: Float = 35f,
        val safeZoneCm: Float = 50f,
        val pitchThresholdDeg: Float = 30f,
        val luxThreshold: Float = 150f,
        val closeDebounceMs: Long = 500L,
        val poseDebounceMs: Long = 3_000L,
        val darkDebounceMs: Long = 10_000L,
        val okDebounceMs: Long = 2_000L,
        /** Masa tenggang wajah hilang sebelum alarm dekat boleh jatuh ke OK (T7). */
        val faceLostGraceMs: Long = 10_000L,
        /** Toleransi frame berturut-turut tanpa wajah sebelum debounce close direset (M3). */
        val missingFrameTolerance: Int = 2,
    )

    private var state: RiskLevel = RiskLevel.OK

    private var closeSinceMs: Long? = null
    private var exitSinceMs: Long? = null
    private var faceLostSinceMs: Long? = null
    private var poseSinceMs: Long? = null
    private var darkSinceMs: Long? = null
    private var safeSinceMs: Long? = null

    /** Jumlah frame berturut-turut tanpa wajah sejak close terakhir (M3). */
    private var closeMissingFrames: Int = 0

    /** True bila state saat ini/terakhir didorong kondisi terlalu dekat (T7). */
    private var closeAlarmActive: Boolean = false

    /** Apakah frame terakhir memuat wajah terdeteksi (dipakai gerbang gelap M7). */
    private var lastFacePresent: Boolean = false

    fun current(): RiskLevel = state

    /** Evaluasi satu sampel dan kembalikan state baru. */
    fun onResult(result: DetectionResult): RiskLevel {
        val now = result.timestampMs
        updateTimers(result, now)
        state = resolveState(now)
        return state
    }

    private fun updateTimers(
        result: DetectionResult,
        now: Long,
    ) {
        val distance = result.distanceCm
        val pitch = result.pitchDeg
        val lux = result.lux
        lastDistanceCm = distance
        lastFacePresent = distance != null || result.faceHeightPx != null

        // M3: satu-dua frame hilang tidak boleh mereset debounce close 500 ms.
        val closeCond = distance != null && distance < config.closeEnterCm
        when {
            closeCond -> {
                closeMissingFrames = 0
                closeSinceMs = closeSinceMs ?: now
            }
            !lastFacePresent && closeSinceMs != null && closeMissingFrames < config.missingFrameTolerance -> {
                closeMissingFrames++
                // closeSinceMs dipertahankan lintas frame hilang
            }
            else -> {
                closeMissingFrames = 0
                closeSinceMs = null
            }
        }
        exitSinceMs = tick(exitSinceMs, distance != null && distance > config.closeExitCm, now)
        faceLostSinceMs = tick(faceLostSinceMs, !lastFacePresent, now)
        poseSinceMs = tick(poseSinceMs, pitch != null && pitch > config.pitchThresholdDeg, now)
        // M7: kondisi gelap hanya dihitung bila ada wajah terdeteksi —
        // ponsel telungkup / kamera tertutup di malam hari bukan pelanggaran.
        darkSinceMs = tick(darkSinceMs, lastFacePresent && lux != null && lux < config.luxThreshold, now)
        val pitchSafe = pitch == null || pitch <= config.pitchThresholdDeg
        safeSinceMs =
            tick(
                safeSinceMs,
                distance != null && distance >= config.safeZoneCm && pitchSafe,
                now,
            )
    }

    private data class Flags(
        val closeSustained: Boolean,
        val exitedClose: Boolean,
        val poseSustained: Boolean,
        val darkSustained: Boolean,
        val safeSustained: Boolean,
        val inWarnZone: Boolean,
        val faceLostSustained: Boolean,
    )

    private fun evaluateFlags(now: Long): Flags {
        val distance = lastDistanceCm
        val inWarnZone =
            distance != null &&
                distance >= config.closeEnterCm &&
                distance < config.safeZoneCm
        return Flags(
            closeSustained = sustained(closeSinceMs, config.closeDebounceMs, now),
            // "Keluar zona dekat" analog: jarak melewati ambang ATAU wajah hilang
            // melewati masa tenggang (T7) - bukan sekadar wajah hilang 2 dtk.
            exitedClose =
                sustained(exitSinceMs, config.okDebounceMs, now) ||
                    sustained(faceLostSinceMs, config.faceLostGraceMs, now),
            poseSustained = sustained(poseSinceMs, config.poseDebounceMs, now),
            darkSustained = sustained(darkSinceMs, config.darkDebounceMs, now),
            safeSustained = sustained(safeSinceMs, config.okDebounceMs, now),
            inWarnZone = inWarnZone,
            faceLostSustained = sustained(faceLostSinceMs, config.okDebounceMs, now),
        )
    }

    private fun resolveState(now: Long): RiskLevel {
        val f = evaluateFlags(now)
        val faceLost = faceLostSinceMs != null
        val faceLostGraceOver = sustained(faceLostSinceMs, config.faceLostGraceMs, now)

        val next =
            when {
                f.closeSustained -> RiskLevel.BLOCKED
                // T7: wajah hilang saat alarm dekat aktif (BLOCKED / WARNING zona
                // dekat) - tahan state selama grace. Menutup kamera dengan jari
                // tidak boleh mematikan alarm.
                faceLost && closeAlarmActive && !faceLostGraceOver &&
                    (state == RiskLevel.BLOCKED || state == RiskLevel.WARNING) -> state
                // T7: OK via wajah hilang hanya setelah grace (saat alarm dekat
                // aktif) atau okDebounce (peringatan gelap/postur saja - aturan lama).
                faceLost &&
                    (if (closeAlarmActive) faceLostGraceOver else f.faceLostSustained) &&
                    !f.poseSustained && !f.darkSustained -> RiskLevel.OK
                state == RiskLevel.BLOCKED && !f.exitedClose -> RiskLevel.BLOCKED
                f.poseSustained || f.darkSustained -> RiskLevel.WARNING
                f.inWarnZone -> RiskLevel.WARNING
                state == RiskLevel.BLOCKED && !f.safeSustained -> RiskLevel.WARNING
                f.safeSustained && !f.poseSustained && !f.darkSustained -> RiskLevel.OK
                else -> state
            }
        closeAlarmActive =
            when (next) {
                RiskLevel.OK -> false
                RiskLevel.BLOCKED -> true
                RiskLevel.WARNING ->
                    // Sekali dipicu konteks dekat, bertahan sampai OK (T7: WARNING
                    // zona dekat juga ditahan saat wajah hilang - bukan hanya BLOCKED).
                    closeAlarmActive || f.inWarnZone || f.closeSustained || state == RiskLevel.BLOCKED
            }
        return next
    }

    private var lastDistanceCm: Float? = null

    fun reset() {
        state = RiskLevel.OK
        closeSinceMs = null
        exitSinceMs = null
        faceLostSinceMs = null
        poseSinceMs = null
        darkSinceMs = null
        safeSinceMs = null
        closeMissingFrames = 0
        closeAlarmActive = false
        lastFacePresent = false
        lastDistanceCm = null
    }

    private fun tick(
        since: Long?,
        active: Boolean,
        now: Long,
    ): Long? =
        when {
            active -> since ?: now
            else -> null
        }

    private fun sustained(
        since: Long?,
        windowMs: Long,
        now: Long,
    ): Boolean = since != null && now - since >= windowMs

    companion object {
        /** Latensi maksimum alarm dari masuk kondisi ke status BLOCKED (KPI K5). */
        const val MAX_ALARM_LATENCY_MS = 1_500L
    }
}
