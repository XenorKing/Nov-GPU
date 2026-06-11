package com.xenorking.novgpu.data.monitor

import com.xenorking.novgpu.domain.model.GpuStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpuMonitor @Inject constructor() {

    private val history = ArrayDeque<Float>(60)
    private var gpuInfo: Pair<String, String>? = null

    fun setGpuInfo(renderer: String, vendor: String, version: String) {
        gpuInfo = Pair(renderer, vendor)
    }

    suspend fun getStats(): GpuStats = withContext(Dispatchers.IO) {
        val usage = readGpuUsage()

        history.addLast(usage)
        if (history.size > 60) history.removeFirst()

        GpuStats(
            usagePercent = usage,
            renderer = gpuInfo?.first ?: readGpuRenderer(),
            vendor = gpuInfo?.second ?: "Qualcomm / ARM / IMG",
            history = history.toList()
        )
    }

    private fun readGpuUsage(): Float {
        // Try Qualcomm Adreno
        val adrenoPath = "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"
        if (File(adrenoPath).exists()) {
            return try {
                File(adrenoPath).readText().trim()
                    .replace("%", "").trim().toFloatOrNull() ?: 0f
            } catch (e: Exception) { 0f }
        }

        // Try Mali GPU
        val maliPath = "/sys/class/misc/mali0/device/utilization"
        if (File(maliPath).exists()) {
            return try {
                File(maliPath).readText().trim().toFloatOrNull() ?: 0f
            } catch (e: Exception) { 0f }
        }

        // Try PowerVR
        val pvrPath = "/sys/class/pvr_gpu/pvr_gpu0/utilization"
        if (File(pvrPath).exists()) {
            return try {
                File(pvrPath).readText().trim().toFloatOrNull() ?: 0f
            } catch (e: Exception) { 0f }
        }

        return 0f
    }

    private fun readGpuRenderer(): String {
        return try {
            val props = File("/system/build.prop").readLines()
            props.find { it.startsWith("ro.board.platform") }
                ?.split("=")?.getOrNull(1) ?: "Unknown GPU"
        } catch (e: Exception) { "Unknown GPU" }
    }
}
