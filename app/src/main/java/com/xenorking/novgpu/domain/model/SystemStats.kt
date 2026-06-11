package com.xenorking.novgpu.domain.model

data class SystemStats(
    val cpu: CpuStats                 = CpuStats(),
    val gpu: GpuStats                 = GpuStats(),
    val ram: RamStats                 = RamStats(),
    val temperature: TemperatureStats = TemperatureStats(),
    val network: NetworkStats         = NetworkStats(),
    val battery: BatteryStats         = BatteryStats(),
    val timestamp: Long               = System.currentTimeMillis()
)

// Источник: /proc/stat — поля user, nice, system, idle, iowait, irq, softirq
// Формула: usage = ΔactiveTime / ΔtotalTime × 100%
data class CpuStats(
    val usagePercent: Float     = 0f,
    val coreUsages: List<Float> = emptyList(),
    val frequencyMhz: Long      = 0L,
    val maxFrequencyMhz: Long   = 0L,
    val architecture: String    = "",
    val coreCount: Int          = 0,
    val history: List<Float>    = emptyList()
)

// Источник: sysfs GPU driver (Adreno: kgsl gpu_busy_percentage, Mali: mali0 utilization)
// OpenGL renderer string через GLSurfaceView
data class GpuStats(
    val usagePercent: Float     = 0f,
    val renderer: String        = "Нет данных",
    val vendor: String          = "Нет данных",
    val version: String         = "",
    val history: List<Float>    = emptyList()
)

// Источник: ActivityManager.getMemoryInfo() — официальный Android API
// totalMem, availMem, threshold
data class RamStats(
    val totalMb: Long           = 0L,
    val usedMb: Long            = 0L,
    val availableMb: Long       = 0L,
    val usagePercent: Float     = 0f,
    val swapTotalMb: Long       = 0L,
    val swapUsedMb: Long        = 0L,
    val history: List<Float>    = emptyList()
)

// Источник: /sys/class/thermal/thermal_zone[N]/temp (тепловые зоны SoC)
// Батарея: Intent.ACTION_BATTERY_CHANGED -> EXTRA_TEMPERATURE / 10 -> celsius
data class TemperatureStats(
    val batteryTemp: Float               = 0f,
    val cpuTemp: Float                   = 0f,
    val thermalZones: Map<String, Float> = emptyMap()
)

// Источник: android.net.TrafficStats.getTotalRxBytes/TxBytes
// Скорость = разница двух измерений / интервал (КБ/с)
data class NetworkStats(
    val downloadSpeedKbps: Long     = 0L,
    val uploadSpeedKbps: Long       = 0L,
    val totalDownloadMb: Long       = 0L,
    val totalUploadMb: Long         = 0L,
    val downloadHistory: List<Long> = emptyList(),
    val uploadHistory: List<Long>   = emptyList(),
    val connectionType: String      = "Неизвестно"
)

// Источник: Intent.ACTION_BATTERY_CHANGED (Sticky broadcast)
// level/scale*100=%, voltage/1000=В, temperature/10=°C
data class BatteryStats(
    val level: Int         = 0,
    val isCharging: Boolean = false,
    val voltage: Float     = 0f,
    val technology: String = "",
    val health: String     = ""
)
