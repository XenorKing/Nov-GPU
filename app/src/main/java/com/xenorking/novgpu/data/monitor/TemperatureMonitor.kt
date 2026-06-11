package com.xenorking.novgpu.data.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.xenorking.novgpu.domain.model.TemperatureStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getStats(): TemperatureStats = withContext(Dispatchers.IO) {
        val batteryTemp = readBatteryTemperature()
        val cpuTemp = readCpuTemperature()
        val thermalZones = readThermalZones()

        TemperatureStats(
            batteryTemp = batteryTemp,
            cpuTemp = cpuTemp,
            thermalZones = thermalZones
        )
    }

    private fun readBatteryTemperature(): Float {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            temp / 10f
        } catch (e: Exception) { 0f }
    }

    private fun readCpuTemperature(): Float {
        val paths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/class/hwmon/hwmon0/temp1_input"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val value = file.readText().trim().toLongOrNull() ?: continue
                    return if (value > 1000) value / 1000f else value.toFloat()
                }
            } catch (e: Exception) { continue }
        }
        return 0f
    }

    private fun readThermalZones(): Map<String, Float> {
        val zones = mutableMapOf<String, Float>()
        try {
            val thermalDir = File("/sys/class/thermal")
            if (!thermalDir.exists()) return zones

            thermalDir.listFiles()?.filter { it.name.startsWith("thermal_zone") }
                ?.take(8)
                ?.forEach { zone ->
                    try {
                        val type = File(zone, "type").readText().trim()
                        val tempFile = File(zone, "temp")
                        if (tempFile.exists()) {
                            val value = tempFile.readText().trim().toLongOrNull() ?: return@forEach
                            val temp = if (value > 1000) value / 1000f else value.toFloat()
                            if (temp in 1f..150f) {
                                zones[type] = temp
                            }
                        }
                    } catch (e: Exception) { /* skip */ }
                }
        } catch (e: Exception) { /* ignore */ }
        return zones
    }
}
