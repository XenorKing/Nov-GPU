package com.xenorking.novgpu.domain.model

data class SystemStats(
    val cpu: CpuStats         = CpuStats(),
    val gpu: GpuStats         = GpuStats(),
    val ram: RamStats         = RamStats(),
    val temperature: TemperatureStats = TemperatureStats(),
    val network: NetworkStats = NetworkStats(),
    val battery: BatteryStats = BatteryStats(),
    val timestamp: Long       = System.currentTimeMillis()
)

/** Источник: /proc/stat — поля user, nice, system, idle, iowait, irq, softirq */
data class CpuStats(
    val usagePercent: Float = 0f,
    val coreUsages: List<Float> = emptyList(),
    val frequencyMhz: Long = 0L,
    val maxFrequencyMhz: Long = 0L,
    val architecture: String = "",
    val coreCount: Int = Runtime.getRuntime().availableProcessors(),
    val history: List<Float> = emptyList()
)

/** Источник: /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage (Adreno) или
 *  /sys/class/misc/mali0/device/utilization (Mali), OpenGL renderer string */
data class GpuStats(
    val usagePercent: Float = 0f,
    val renderer: String = "Нет данных",
    val vendor: String = "Нет данных",
    val version: String = "",
    val history: List<Float> = emptyList()
)

/** Источник: ActivityManager.getMemoryInfo() — системный API Android */
data class RamStats(
    val totalMb: Long = 0L,
    val usedMb: Long = 0L,
    val availableMb: Long = 0L,
    val usagePercent: Float = 0f,
    val swapTotalMb: Long = 0L,
    val swapUsedMb: Long = 0L,
    val history: List<Float> = emptyList()
)

/** Источник: /sys/class/thermal/thermal_zone*/temp (тепловые зоны),
 *  Intent.ACTION_BATTERY_CHANGED → BatteryManager.EXTRA_TEMPERATURE */
data class TemperatureStats(
    val batteryTemp: Float = 0f,
    val cpuTemp: Float = 0f,
    val thermalZones: Map<String, Float> = emptyMap()
)

/** Источник: TrafficStats.getTotalRxBytes() / TotalTxBytes() —
 *  разница значений за интервал обновления (1 сек), делённая на время */
data class NetworkStats(
    val downloadSpeedKbps: Long = 0L,
    val uploadSpeedKbps: Long = 0L,
    val totalDownloadMb: Long = 0L,
    val totalUploadMb: Long = 0L,
    val downloadHistory: List<Long> = emptyList(),
    val uploadHistory: List<Long> = emptyList(),
    val connectionType: String = "Неизвестно"
)

/** Источник: BatteryManager + Intent.ACTION_BATTERY_CHANGED */
data class BatteryStats(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val voltage: Float = 0f,
    val technology: String = "",
    val health: String = ""
)
