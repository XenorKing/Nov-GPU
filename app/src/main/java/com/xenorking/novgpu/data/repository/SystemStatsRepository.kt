package com.xenorking.novgpu.data.repository

import com.xenorking.novgpu.data.monitor.CpuMonitor
import com.xenorking.novgpu.data.monitor.GpuMonitor
import com.xenorking.novgpu.data.monitor.NetworkMonitor
import com.xenorking.novgpu.data.monitor.RamMonitor
import com.xenorking.novgpu.data.monitor.TemperatureMonitor
import com.xenorking.novgpu.domain.model.SystemStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemStatsRepository @Inject constructor(
    private val cpuMonitor: CpuMonitor,
    private val gpuMonitor: GpuMonitor,
    private val ramMonitor: RamMonitor,
    private val temperatureMonitor: TemperatureMonitor,
    private val networkMonitor: NetworkMonitor
) {
    fun getStatsFlow(intervalMs: Long = 1000L): Flow<SystemStats> = flow {
        while (true) {
            val stats = SystemStats(
                cpu = cpuMonitor.getStats(),
                gpu = gpuMonitor.getStats(),
                ram = ramMonitor.getStats(),
                temperature = temperatureMonitor.getStats(),
                network = networkMonitor.getStats()
            )
            emit(stats)
            delay(intervalMs)
        }
    }

    fun setGpuInfo(renderer: String, vendor: String, version: String) {
        gpuMonitor.setGpuInfo(renderer, vendor, version)
    }
}
