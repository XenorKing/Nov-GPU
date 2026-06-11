package com.xenorking.novgpu.data.monitor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.xenorking.novgpu.domain.model.BatteryStats
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Источник: Intent.ACTION_BATTERY_CHANGED (Sticky broadcast).
 * Заряд: BatteryManager.EXTRA_LEVEL / EXTRA_SCALE × 100
 * Напряжение: BatteryManager.EXTRA_VOLTAGE (мВ → В)
 * Температура: BatteryManager.EXTRA_TEMPERATURE (десятки градусов → °C)
 */
@Singleton
class BatteryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getStats(): BatteryStats = try {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level   = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale   = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status  = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val tech    = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""
        val health  = when (intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD          -> "Хорошее"
            BatteryManager.BATTERY_HEALTH_OVERHEAT      -> "Перегрев"
            BatteryManager.BATTERY_HEALTH_DEAD          -> "Разряжена"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE  -> "Перенапряжение"
            BatteryManager.BATTERY_HEALTH_COLD          -> "Холодно"
            else                                        -> "Норма"
        }
        BatteryStats(
            level      = if (scale > 0) (level * 100 / scale) else 0,
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                      || status == BatteryManager.BATTERY_STATUS_FULL,
            voltage    = voltage / 1000f,
            technology = tech,
            health     = health
        )
    } catch (e: Exception) {
        BatteryStats()
    }
}
