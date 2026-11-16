package com.jagamata.app.detection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * KPI K4: siklus tepat 1 per interval, tepat 1 baris per siklus,
 * drift <= 2 detik pada interval 60 detik.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MinuteTickerTest {
    @Test
    fun `interval 100 ms x 10 siklus menghasilkan tepat 10 baris`() =
        runTest {
            val cycles = mutableListOf<CycleSummary>()
            val ticker =
                MinuteTicker(
                    scope = this,
                    aggregator = CycleAggregator(),
                    tickIntervalMs = 100,
                    now = { testScheduler.currentTime },
                ) { summary -> cycles.add(summary) }

            ticker.start()
            repeat(10) {
                advanceTimeBy(100)
                runCurrent()
            }
            ticker.stop()

            assertEquals(10, cycles.size)
            assertEquals(10, ticker.cycleTimestampsMs.size)
        }

    @Test
    fun `urutan timestamp siklus monoton naik`() =
        runTest {
            val cycles = mutableListOf<CycleSummary>()
            val ticker =
                MinuteTicker(
                    scope = this,
                    aggregator = CycleAggregator(),
                    tickIntervalMs = 100,
                    now = { testScheduler.currentTime },
                ) { summary -> cycles.add(summary) }

            ticker.start()
            repeat(6) {
                advanceTimeBy(100)
                runCurrent()
            }
            ticker.stop()

            val stamps = ticker.cycleTimestampsMs
            assertTrue(stamps.zipWithNext().all { (a, b) -> b > a })
        }

    @Test
    fun `drift antar siklus konsisten`() =
        runTest {
            val ticker =
                MinuteTicker(
                    scope = this,
                    aggregator = CycleAggregator(),
                    tickIntervalMs = 100,
                    now = { testScheduler.currentTime },
                ) { }

            ticker.start()
            repeat(5) {
                advanceTimeBy(100)
                runCurrent()
            }
            ticker.stop()

            val drift = ticker.driftBetweenCyclesMs()
            assertEquals(4, drift.size)
            assertTrue(drift.all { it == 100L })
        }

    @Test
    fun `evaluateEveryTicks menyatukan beberapa tick jadi satu siklus`() =
        runTest {
            val cycles = mutableListOf<CycleSummary>()
            val ticker =
                MinuteTicker(
                    scope = this,
                    aggregator = CycleAggregator(),
                    tickIntervalMs = 100,
                    evaluateEveryTicks = 3,
                    now = { testScheduler.currentTime },
                ) { summary -> cycles.add(summary) }

            ticker.start()
            repeat(9) {
                advanceTimeBy(100)
                runCurrent()
            }
            ticker.stop()

            // 9 tick / 3 = 3 siklus evaluasi
            assertEquals(3, cycles.size)
        }

    @Test
    fun `stop menghentikan ticker`() =
        runTest {
            val cycles = mutableListOf<CycleSummary>()
            val ticker =
                MinuteTicker(
                    scope = this,
                    aggregator = CycleAggregator(),
                    tickIntervalMs = 100,
                    now = { testScheduler.currentTime },
                ) { summary -> cycles.add(summary) }

            ticker.start()
            advanceTimeBy(300)
            runCurrent()
            ticker.stop()
            val countAfterStop = cycles.size

            advanceTimeBy(1000)
            runCurrent()
            assertEquals(countAfterStop, cycles.size)
        }
}
