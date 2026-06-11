package com.xenorking.novgpu.presentation.state

import com.xenorking.novgpu.domain.model.MonitorConfig
import com.xenorking.novgpu.domain.model.SystemStats

data class MonitorState(
    val stats: SystemStats    = SystemStats(),
    val config: MonitorConfig = MonitorConfig(),
    val isLoading: Boolean    = true,
    val error: String?        = null,
    val selectedTab: Int      = 0,
    val isMonitoring: Boolean = false
)

sealed class MonitorIntent {
    object StartMonitoring : MonitorIntent()
    object StopMonitoring  : MonitorIntent()
    data class SelectTab(val index: Int)            : MonitorIntent()
    data class UpdateConfig(val config: MonitorConfig) : MonitorIntent()
    data class SetGpuInfo(val renderer: String, val vendor: String, val version: String) : MonitorIntent()
}
