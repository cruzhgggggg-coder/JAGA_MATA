package com.jagamata.app.detection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Ticker siklus pemantauan (PRD §2.1, §5.4, US-08, KPI K4).
 *
 * `delay(tickIntervalMs)` berulang selama service hidup. Setiap
 * `evaluateEveryTicks` kali tick: aggregator dievaluasi -> tepat 1
 * [CycleSummary] (1 baris `check_snapshots`) lewat [onCycle].
 *
 * `cycleDurationMs` = waktu nyata antar siklus (dipakai pengukuran drift K4,
 * target ±2 detik pada siklus 60 detik).
 *
 * Interval bisa diinjeksi (100 ms) untuk uji deterministik.
 */
class MinuteTicker(
    private val scope: CoroutineScope,
    private val aggregator: CycleAggregator = CycleAggregator(),
    private val tickIntervalMs: Long = TICK_INTERVAL_MS,
    private val evaluateEveryTicks: Int = 1,
    private val now: () -> Long = System::currentTimeMillis,
    private val onCycle: suspend (CycleSummary) -> Unit,
) {
    private var job: Job? = null
    private var tickCount = 0
    private var lastCycleStartMs: Long? = null

    /** Timestamp awal setiap siklus yang dievaluasi — pengukuran drift K4. */
    val cycleTimestampsMs = mutableListOf<Long>()

    /** Sampel agregat yang masuk ke siklus berikutnya. */
    val aggregatorSink: CycleAggregator get() = aggregator

    fun start() {
        if (job?.isActive == true) return
        tickCount = 0
        job =
            scope.launch {
                while (isActive) {
                    delay(tickIntervalMs)
                    tickCount++
                    if (tickCount % evaluateEveryTicks != 0) continue

                    val cycleStart = now()
                    val previousStart = lastCycleStartMs
                    lastCycleStartMs = cycleStart
                    val durationMs = previousStart?.let { cycleStart - it } ?: 0L

                    val summary =
                        aggregator.evaluate(
                            startedAtMs = cycleStart,
                            cycleDurationMs = durationMs,
                        )
                    cycleTimestampsMs.add(cycleStart)
                    onCycle(summary)
                }
            }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun isRunning(): Boolean = job?.isActive == true

    /** Selisih antar awal siklus berurutan (ms). Target K4: ±2.000 ms untuk siklus 60 detik. */
    fun driftBetweenCyclesMs(): List<Long> = cycleTimestampsMs.zipWithNext { a, b -> b - a }

    companion object {
        const val TICK_INTERVAL_MS = 60_000L
        const val LIGHT_MODE_TICK_INTERVAL_MS = 10_000L
        const val LIGHT_MODE_TICKS_PER_CYCLE = 6
    }
}
