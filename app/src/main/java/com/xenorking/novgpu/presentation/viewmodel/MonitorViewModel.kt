package com.xenorking.novgpu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenorking.novgpu.data.repository.SystemStatsRepository
import com.xenorking.novgpu.domain.usecase.GetSystemStatsUseCase
import com.xenorking.novgpu.presentation.state.MonitorIntent
import com.xenorking.novgpu.presentation.state.MonitorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val getSystemStatsUseCase: GetSystemStatsUseCase,
    private val repository: SystemStatsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MonitorState())
    val state: StateFlow<MonitorState> = _state.asStateFlow()

    private var monitoringJob: Job? = null

    init {
        processIntent(MonitorIntent.StartMonitoring)
    }

    fun processIntent(intent: MonitorIntent) {
        when (intent) {
            is MonitorIntent.StartMonitoring -> startMonitoring()
            is MonitorIntent.StopMonitoring -> stopMonitoring()
            is MonitorIntent.SelectTab -> _state.update { it.copy(selectedTab = intent.index) }
            is MonitorIntent.UpdateConfig -> _state.update { it.copy(config = intent.config) }
            is MonitorIntent.SetGpuInfo -> repository.setGpuInfo(intent.renderer, intent.vendor, intent.version)
        }
    }

    private fun startMonitoring() {
        if (monitoringJob?.isActive == true) return
        _state.update { it.copy(isMonitoring = true, isLoading = true) }

        monitoringJob = viewModelScope.launch {
            getSystemStatsUseCase(intervalMs = _state.value.config.refreshIntervalMs)
                .catch { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { stats ->
                    _state.update {
                        it.copy(
                            stats = stats,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _state.update { it.copy(isMonitoring = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
