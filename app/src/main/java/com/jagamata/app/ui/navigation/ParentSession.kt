package com.jagamata.app.ui.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Flag unlock sesi orang tua (DEV-37) — singleton per-proses, TIDAK disimpan ke disk.
 * Reset otomatis saat cold start; bertahan saat rotasi. Auto-relock setelah
 * aplikasi berada di latar-belakang lebih dari 5 menit (lihat [onAppForeground]).
 */
object ParentSession {
    private val gate = ParentGate()
    private val pauseGate = PauseGate()
    private val unlockedState = mutableStateOf(gate.unlocked)

    /**
     * Status unlock sesi. Di-backing [mutableStateOf] agar composition yang membacanya
     * ikut recompose. Tidak dipersist — cukup hidup selama proses berjalan.
     */
    var unlocked: Boolean
        get() = unlockedState.value
        set(value) {
            gate.unlocked = value
            unlockedState.value = value
        }

    /** Waktu terakhir aplikasi masuk latar-belakang (0 = belum pernah). */
    var lastBackgroundAtMs: Long
        get() = gate.lastBackgroundAtMs
        set(value) {
            gate.lastBackgroundAtMs = value
        }

    /**
     * T6: true bila focal kalibrasi sudah pernah tersimpan — kalibrasi berikutnya
     * (kalibrasi-ulang) wajib PIN. Di-set dari collector DataStore di MainActivity.
     */
    var hasSavedFocal: Boolean
        get() = gate.hasSavedFocal
        set(value) {
            gate.hasSavedFocal = value
        }

    /** Apakah [route] wajib melewati PIN saat terkunci (lihat [ParentGate.requiresGate]). */
    fun requiresGate(route: String): Boolean = gate.requiresGate(route)

    /** Keputusan tombol jeda/lanjut (T5) — lihat [PauseGate.onToggle]. */
    fun pauseToggleDecision(isRunning: Boolean): PauseDecision = pauseGate.onToggle(isRunning, unlocked)

    /** Setelah PIN sukses: true bila permintaan jeda tertunda harus dieksekusi (T5). */
    fun consumePendingPause(): Boolean = pauseGate.consumePendingPause()

    /** Membatalkan permintaan jeda tertunda (T5) — mis. layar kunci ditinggalkan. */
    fun cancelPendingPause() {
        pauseGate.cancelPendingPause()
    }

    /**
     * Handle Compose untuk tujuan yang tertunda — satu storage dengan [ParentGate.pending]
     * (lihat [GatePendingState]) sehingga AppNav bisa memakai `MutableState<String?>`
     * sementara unit test tetap memakai [ParentGate] tanpa Compose.
     */
    val pendingState: MutableState<String?> = GatePendingState(gate)

    /** Catat waktu masuk latar-belakang (dipanggil dari lifecycle onStop). */
    fun onAppBackground() {
        gate.onAppBackground()
    }

    /** Kembali ke latar-depan: relock bila jeda > [timeoutMs] (dipanggil dari lifecycle onStart). */
    fun onAppForeground(
        nowMs: Long = System.currentTimeMillis(),
        timeoutMs: Long = ParentGate.DEFAULT_TIMEOUT_MS,
    ) {
        gate.onAppForeground(nowMs, timeoutMs)
        unlockedState.value = gate.unlocked
    }

    /** Keputusan gerbang: route tujuan, atau [Routes.KUNCI] + pending untuk rute sensitif yang terkunci. */
    fun tryNavigate(route: String): String = gate.tryNavigate(route)

    /** Tandai sesi terbuka dan kembalikan tujuan tertunda (null bila tidak ada). */
    fun onUnlocked(): String? {
        val target = gate.onUnlocked()
        unlockedState.value = gate.unlocked
        return target
    }
}

/**
 * [MutableState] di atas [ParentGate.pending] — storage tetap tunggal di gate,
 * hanya tampil sebagai `MutableState<String?>` untuk API AppNav.
 */
internal class GatePendingState(private val gate: ParentGate) : MutableState<String?> {
    override var value: String?
        get() = gate.pending
        set(value) {
            gate.pending = value
        }

    override fun component1(): String? = value

    override fun component2(): (String?) -> Unit = { value = it }
}
