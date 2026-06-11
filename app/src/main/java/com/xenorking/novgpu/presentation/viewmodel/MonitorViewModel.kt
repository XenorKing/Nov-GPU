package com.xenorking.novgpu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenorking.novgpu.data.monitor.*
import com.xenorking.novgpu.domain.model.SystemStats
import com.xenorking.novgpu.presentation.state.MonitorIntent
import com.xenorking.novgpu.presentation.state.MonitorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val cpuMonitor:   CpuMonitor,
    private val gpuMonitor:   GpuMonitor,
    private val ramMonitor:   RamMonitor,
    private val tempMonitor:  TemperatureMonitor,
    private val netMonitor:   NetworkMonitor,
    private val battMonitor:  BatteryMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(MonitorState())
    val state: StateFlow<MonitorState> = _state.asStateFlow()

    private var monitorJob: Job? = null

    init { startMonitoring() }

    fun handleIntent(intent: MonitorIntent) {
        when (intent) {
            is MonitorIntent.StartMonitoring -> startMonitoring()
            is MonitorIntent.StopMonitoring  -> stopMonitoring()
            is MonitorIntent.SelectTab       -> _state.update { it.copy(selectedTab = intent.index) }
            is MonitorIntent.UpdateConfig    -> _state.update { it.copy(config = intent.config) }
            is MonitorIntent.SetGpuInfo      -> _state.update {
                it.copy(stats = it.stats.copy(
                    gpu = it.stats.gpu.copy(
                        renderer = intent.renderer,
                        vendor   = intent.vendor,
                        version  = intent.version
                    )
                ))
            }
        }
    }

    private fun startMonitoring() {
        if (monitorJob?.isActive == true) return
        _state.update { it.copy(isMonitoring = true, isLoading = false) }
        monitorJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val cpu  = async { cpuMonitor.getStats() }
                    val gpu  = async { gpuMonitor.getStats() }
                    val ram  = async { ramMonitor.getStats() }
                    val temp = async { tempMonitor.getStats() }
                    val net  = async { netMonitor.getStats() }
                    val bat  = async { withContext(Dispatchers.IO) { battMonitor.getStats() } }

                    val newStats = SystemStats(
                        cpu         = cpu.await(),
                        gpu         = gpu.await().let {
                            val current = _state.value.stats.gpu
                            if (current.renderer.isNotEmpty() && current.renderer != "Нет данных")
                                it.copy(renderer = current.renderer, vendor = current.vendor, version = current.version)
                            else it
                        },
                        ram         = ram.await(),
                        temperature = temp.await(),
                        network     = net.await(),
                        battery     = bat.await()
                    )

                    _state.update { it.copy(stats = newStats, isLoading = false, error = null) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message) }
                }
                delay(1000L)
            }
        }
    }

    private fun stopMonitoring() {
        monitorJob?.cancel()
        _state.update { it.copy(isMonitoring = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
