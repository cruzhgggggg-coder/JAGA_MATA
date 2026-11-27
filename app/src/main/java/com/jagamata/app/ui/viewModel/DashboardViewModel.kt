package com.jagamata.app.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagamata.app.data.AppDatabase
import com.jagamata.app.data.entity.DailyStats
import com.jagamata.app.detection.CycleSummary
import com.jagamata.app.detection.RiskLevel
import com.jagamata.app.service.DetectionForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * State dasbor orang tua (PRD US-10): 3 angka pekan ini, strip siklus live,
 * grafik 7 hari dari `daily_stats`.
 */
class DashboardViewModel(
    private val database: AppDatabase,
) : ViewModel() {
    constructor() : this(DetectionServiceLocator.database())

    data class UiState(
        val weeklyCloseViolations: Int = 0,
        val weeklyDarkMinutes: Int = 0,
        val weeklyPostureViolations: Int = 0,
        val lastCycle: CycleSummary? = null,
        val riskLevel: RiskLevel = RiskLevel.OK,
        val dailyStats: List<DailyStats> = emptyList(),
        val dailyAvgDistanceCm: Map<Long, Float> = emptyMap(),
        val isMonitoring: Boolean = false,
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
                _uiState.value = _uiState.value.copy(isMonitoring = running)
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val since = System.currentTimeMillis() - WEEK_MS
            val snapshots = database.snapshotDao().getSince(since)
            val daily = database.dailyDao().getSince((since / DAY_MS))
            _uiState.value =
                _uiState.value.copy(
                    weeklyCloseViolations = snapshots.count { it.violations.contains("TOO_CLOSE") },
                    weeklyDarkMinutes = snapshots.count { (it.avgLux ?: Float.MAX_VALUE) < 150f },
                    weeklyPostureViolations = snapshots.count { it.violations.contains("BAD_POSTURE") },
                    dailyStats = daily,
                    dailyAvgDistanceCm =
                        snapshots
                            .filter { it.avgDistanceCm != null }
                            .groupBy { it.dayEpoch }
                            .mapValues { (_, rows) -> rows.mapNotNull { it.avgDistanceCm }.average().toFloat() },
                )
        }
    }

    companion object {
        private const val DAY_MS = 86_400_000L
        private const val WEEK_MS = 7 * DAY_MS
    }
}
