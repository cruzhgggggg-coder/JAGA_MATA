package com.jagamata.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit test gerbang jeda (T5) — [PauseGate] murni Kotlin, tanpa Compose/Android. */
class PauseGateTest {
    private val gate = PauseGate()

    // ——— (a) stop harus PIN: hold saat berjalan + terkunci → minta PIN ———

    @Test
    fun `hold saat berjalan dan terkunci meminta PIN dan menahan jeda`() {
        val decision = gate.onToggle(isRunning = true, unlocked = false)

        assertEquals(PauseDecision.REQUEST_PIN, decision)
        assertTrue(gate.pausePending)
    }

    @Test
    fun `hold saat berjalan dan sesi terbuka langsung menjeda tanpa PIN`() {
        val decision = gate.onToggle(isRunning = true, unlocked = true)

        assertEquals(PauseDecision.PAUSE_NOW, decision)
        assertFalse(gate.pausePending)
    }

    // ——— (b) resume tetap bebas: tidak pernah meminta PIN ———

    @Test
    fun `hold saat tidak berjalan langsung melanjutkan tanpa PIN`() {
        val decision = gate.onToggle(isRunning = false, unlocked = false)

        assertEquals(PauseDecision.RESUME_NOW, decision)
        assertFalse(gate.pausePending)
    }

    @Test
    fun `melanjutkan saat terkunci tidak meninggalkan jeda tertunda`() {
        gate.onToggle(isRunning = true, unlocked = false)

        assertEquals(PauseDecision.RESUME_NOW, gate.onToggle(isRunning = false, unlocked = false))
        assertFalse(gate.pausePending)
    }

    // ——— eksekusi jeda tertunda setelah PIN ———

    @Test
    fun `consumePendingPause mengeksekusi tepat sekali`() {
        gate.onToggle(isRunning = true, unlocked = false)

        assertTrue(gate.consumePendingPause())
        assertFalse(gate.pausePending)
        assertFalse(gate.consumePendingPause())
    }

    @Test
    fun `consumePendingPause tanpa permintaan mengembalikan false`() {
        assertFalse(gate.consumePendingPause())
    }

    @Test
    fun `cancelPendingPause membatalkan jeda tertunda`() {
        gate.onToggle(isRunning = true, unlocked = false)

        gate.cancelPendingPause()

        assertFalse(gate.pausePending)
        assertFalse(gate.consumePendingPause())
    }

    @Test
    fun `permintaan PIN kedua menimpa yang pertama tetap satu jeda tertunda`() {
        gate.onToggle(isRunning = true, unlocked = false)
        gate.onToggle(isRunning = true, unlocked = false)

        assertTrue(gate.pausePending)
        assertTrue(gate.consumePendingPause())
    }
}
