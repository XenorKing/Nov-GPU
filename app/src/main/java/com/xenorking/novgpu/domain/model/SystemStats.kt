package com.xenorking.novgpu.domain.model

data class SystemStats(
    val cpu: CpuStats = CpuStats(),
    val gpu: GpuStats = GpuStats(),
    val ram: RamStats = RamStats(),
    val temperature: TemperatureStats = TemperatureStats(),
    val network: NetworkStats = NetworkStats(),
    val timestamp: Long = System.currentTimeMillis()
)

data class CpuStats(
    val usagePercent: Float = 0f,
    val coreUsages: List<Float> = emptyList(),
    val frequencyMhz: Long = 0L,
    val maxFrequencyMhz: Long = 0L,
    val architecture: String = "",
    val coreCount: Int = Runtime.getRuntime().availableProcessors(),
    val history: List<Float> = emptyList()
)

data class GpuStats(
    val usagePercent: Float = 0f,
    val renderer: String = "Unknown",
    val vendor: String = "Unknown",
    val version: String = "",
    val history: List<Float> = emptyList()
)

data class RamStats(
    val totalMb: Long = 0L,
    val usedMb: Long = 0L,
    val availableMb: Long = 0L,
    val usagePercent: Float = 0f,
    val swapTotalMb: Long = 0L,
    val swapUsedMb: Long = 0L,
    val history: List<Float> = emptyList()
)

data class TemperatureStats(
    val batteryTemp: Float = 0f,
    val cpuTemp: Float = 0f,
    val thermalZones: Map<String, Float> = emptyMap()
)

data class NetworkStats(
    val downloadSpeedKbps: Long = 0L,
    val uploadSpeedKbps: Long = 0L,
    val totalDownloadMb: Long = 0L,
    val totalUploadMb: Long = 0L,
    val downloadHistory: List<Long> = emptyList(),
    val uploadHistory: List<Long> = emptyList(),
    val connectionType: String = "Unknown"
)
