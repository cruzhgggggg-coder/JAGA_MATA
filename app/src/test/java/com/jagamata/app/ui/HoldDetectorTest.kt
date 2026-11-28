package com.jagamata.app.ui

import com.jagamata.app.ui.components.HoldDetector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HoldDetectorTest {
    @Test
    fun `release sebelum holdMs tidak memanggil onActivated`() =
        runTest {
            var activated = false
            val detector =
                HoldDetector(
                    scope = this,
                    holdMs = 3000L,
                ) { activated = true }

            detector.press()
            advanceTimeBy(2000)
            runCurrent()
            detector.release()

            assertFalse(activated)
            assertEquals(0f, detector.progress.value)
        }

    @Test
    fun `tahan sampai holdMs memanggil onActivated`() =
        runTest {
            var activated = false
            val detector =
                HoldDetector(
                    scope = this,
                    holdMs = 3000L,
                ) { activated = true }

            detector.press()
            advanceTimeBy(3000)
            runCurrent()

            assertTrue(activated)
        }

    @Test
    fun `progress naik dari 0 ke 1 selama hold`() =
        runTest {
            val detector =
                HoldDetector(
                    scope = this,
                    holdMs = 1000L,
                ) { }

            detector.press()
            assertEquals(0f, detector.progress.value)

            advanceTimeBy(500)
            runCurrent()
            val midProgress = detector.progress.value
            assertTrue("mid progress=$midProgress should be > 0", midProgress > 0f)
            assertTrue("mid progress=$midProgress should be < 1", midProgress < 1f)

            advanceTimeBy(500)
            runCurrent()
            val endProgress = detector.progress.value
            assertTrue("end progress=$endProgress should be >= 1", endProgress >= 1f)
        }

    @Test
    fun `release mereset progress ke 0`() =
        runTest {
            val detector =
                HoldDetector(
                    scope = this,
                    holdMs = 3000L,
                ) { }

            detector.press()
            advanceTimeBy(1500)
            runCurrent()
            assertTrue(detector.progress.value > 0f)

            detector.release()
            assertEquals(0f, detector.progress.value)
        }
}
