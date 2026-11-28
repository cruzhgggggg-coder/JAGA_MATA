package com.jagamata.app.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagamata.app.data.AppDatabase
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.data.StatsRepository
import com.jagamata.app.data.dayInitial
import com.jagamata.app.data.localDayEpoch
import com.jagamata.app.data.localDaysEndingAt
import com.jagamata.app.detection.CycleSummary
import com.jagamata.app.detection.RiskLevel
import com.jagamata.app.service.DetectionForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * State dasbor orang tua (PRD US-10): 3 angka pekan ini, strip siklus live,
 * grafik 7 hari dari rata-rata jarak snapshot (M10: satu rumus via [StatsRepository]).
 */
class DashboardViewModel(
    private val database: AppDatabase,
) : ViewModel() {
    constructor() : this(DetectionServiceLocator.database())

    private val stats = StatsRepository(database)

    data class UiState(
        val weeklyCloseViolations: Int = 0,
        val weeklyDarkMinutes: Int = 0,
        val weeklyPostureViolations: Int = 0,
        val lastCycle: CycleSummary? = null,
        val riskLevel: RiskLevel = RiskLevel.OK,
        /** Rata-rata jarak 7 hari kalender terakhir — label "Jarak rata-rata 7 hari" (M10). */
        val avgDistance7DaysCm: Float? = null,
        /**
         * Seri grafik 7 hari kalender terakhir (berakhir hari ini):
         * null = hari tanpa data → celah garis (T14), bukan angka karangan.
         */
        val weeklyDistanceSeries: List<Float?> = emptyList(),
        /** Label hari (inisial Indonesia) untuk 7 titik seri — dari tanggal nyata (T14). */
        val weeklyDayLabels: List<String> = emptyList(),
        val pausedSessionsToday: Int = 0,
        val isMonitoring: Boolean = false,
        /** T5: waktu jeda dimulai (epoch ms); 0 = tidak sedang dijeda. */
        val pausedSinceEpochMs: Long = 0L,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            DetectionForegroundService.lastCycle.collect { cycle ->
                _uiState.value = _uiState.value.copy(lastCycle = cycle)
            }
        }
        viewModelScope.launch {
            DetectionForegroundService.riskLevel.collect { level ->
                _uiState.value = _uiState.value.copy(riskLevel = level)
            }
        }
        viewModelScope.launch {
            DetectionForegroundService.isRunning.collect { running ->
                _uiState.value =
                    _uiState.value.copy(
                        isMonitoring = running,
                        pausedSinceEpochMs =
                            if (running) 0L else DetectionForegroundService.pausedSinceEpochMs.value,
                    )
            }
        }
        // T5: status jeda ikut berubah tanpa fluktuasi isRunning (mis. jeda dicatat).
        viewModelScope.launch {
            DetectionForegroundService.pausedSinceEpochMs.collect { since ->
                _uiState.value = _uiState.value.copy(pausedSinceEpochMs = since)
            }
        }
        refresh()
    }

    /**
     * Muat statistik dengan jendela eksplisit (M10).
     * [luxThresholdCm] ambang lux aktif dari pengaturan (T9) — dikirim dari layar
     * karena ViewModel tidak memegang Context.
     */
    fun refresh(luxThresholdCm: Float = SettingsRepository.DEFAULT_LUX) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val weekly = stats.weeklyCounts(luxThresholdCm, now)
            val pausedToday =
                database.violationDao().countByTypeSince(TYPE_PAUSED_BY_CHILD, dayStartMs())
            _uiState.value =
                _uiState.value.copy(
                    weeklyCloseViolations = weekly.closeViolations,
                    weeklyDarkMinutes = weekly.darkMinutes,
                    weeklyPostureViolations = weekly.postureViolations,
                    pausedSessionsToday = pausedToday,
                    avgDistance7DaysCm = stats.avgDistance7DaysCm(now),
                    weeklyDistanceSeries = stats.avgDistanceSeries(days = 7, nowMs = now),
                    weeklyDayLabels =
                        localDaysEndingAt(localDayEpoch(now), 7).map { dayInitial(it) },
                )
        }
    }

    companion object {
        private const val TYPE_PAUSED_BY_CHILD = "PAUSED_BY_CHILD"
    }
}

/** Awal hari kalender lokal (00:00.000) untuk [nowMs] — jendela hitungan "hari ini". */
internal fun dayStartMs(nowMs: Long = System.currentTimeMillis()): Long {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = nowMs
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
