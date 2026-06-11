package com.xenorking.novgpu.domain.model

data class MonitorConfig(
    val refreshIntervalMs: Long = 1000L,
    val historySize: Int = 60,
    val showCpuCores: Boolean = true,
    val showGpu: Boolean = true,
    val showNetwork: Boolean = true,
    val showTemperature: Boolean = true,
    val keepScreenOn: Boolean = false,
    val backgroundMonitoring: Boolean = false,
    val selectedTab: Int = 0
)
