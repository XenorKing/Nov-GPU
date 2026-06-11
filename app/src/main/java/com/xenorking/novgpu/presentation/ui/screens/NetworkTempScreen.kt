package com.xenorking.novgpu.presentation.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenorking.novgpu.domain.model.SystemStats
import com.xenorking.novgpu.presentation.ui.components.*
import com.xenorking.novgpu.presentation.ui.theme.*

/**
 * СЕТЬ: TrafficStats.getTotalRxBytes() / getTotalTxBytes()
 *   Разница двух измерений за 1 сек → скорость КБ/с
 *
 * ТЕМПЕРАТУРА: /sys/class/thermal/thermal_zone*\/temp — тепловые зоны SoC
 *   Батарея: Intent.ACTION_BATTERY_CHANGED → EXTRA_TEMPERATURE / 10 → °C
 */
@Composable
fun NetworkTempScreen(stats: SystemStats, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val net  = stats.network
    val temp = stats.temperature
    val bat  = stats.battery

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle("СЕТЬ / ТЕМПЕРАТУРА", "Трафик и тепловой мониторинг")

        // Карточки скорости
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            NeonStatCard(
                title    = "ЗАГРУЗКА ↓",
                value    = formatNetSpeed(net.downloadSpeedKbps),
                subtitle = "Получено: ${net.totalDownloadMb} МБ",
                color    = NeonBlue,
                modifier = Modifier.weight(1f)
            )
            NeonStatCard(
                title    = "ОТДАЧА ↑",
                value    = formatNetSpeed(net.uploadSpeedKbps),
                subtitle = "Отправлено: ${net.totalUploadMb} МБ",
                color    = NeonPink,
                modifier = Modifier.weight(1f)
            )
        }
        NeonStatCard(
            title    = "ТИП СОЕДИНЕНИЯ",
            value    = net.connectionType,
            color    = NeonCyan,
            modifier = Modifier.fillMaxWidth()
        )

        // Графики трафика
        SectionCard(title = "ИСТОРИЯ ТРАФИКА", color = NeonBlue) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonLineChart(
                    data = net.downloadHistory.map { it.toFloat() },
                    maxValue = maxOf(net.downloadHistory.maxOrNull()?.toFloat() ?: 1f, 1f),
                    color = NeonBlue, label = "Загрузка КБ/с", height = 75.dp, modifier = Modifier.fillMaxWidth()
                )
                NeonLineChart(
                    data = net.uploadHistory.map { it.toFloat() },
                    maxValue = maxOf(net.uploadHistory.maxOrNull()?.toFloat() ?: 1f, 1f),
                    color = NeonPink, label = "Отдача КБ/с", height = 75.dp, modifier = Modifier.fillMaxWidth()
                )
                InfoText("Источник: TrafficStats.getTotalRxBytes()/TxBytes() • интервал 1 сек")
            }
        }

        // Как считается сеть
        SectionCard(title = "КАК СЧИТАЕТСЯ СКОРОСТЬ СЕТИ", color = NeonBlue) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FormulaText("API",        "android.net.TrafficStats")
                FormulaText("getRxBytes", "Суммарно получено байт (с загрузки ОС)")
                FormulaText("getTxBytes", "Суммарно отправлено байт")
                FormulaText("Скорость",   "Δbytes / Δtime (КБ/с)")
                FormulaText("Интервал",   "1000 мс между замерами")
                InfoText("Счётчики включают весь трафик всех приложений")
            }
        }

        // Температуры
        SectionCard(title = "ТЕМПЕРАТУРА", color = tempColor(temp.cpuTemp.coerceAtLeast(temp.batteryTemp))) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    NeonStatCard(
                        title    = "ПРОЦЕССОР",
                        value    = if (temp.cpuTemp > 0) "${"%.1f".format(temp.cpuTemp)}°C" else "—",
                        subtitle = if (temp.cpuTemp > 0) tempLabel(temp.cpuTemp) else "Нет данных",
                        color    = tempColor(temp.cpuTemp),
                        modifier = Modifier.weight(1f)
                    )
                    NeonStatCard(
                        title    = "БАТАРЕЯ",
                        value    = if (temp.batteryTemp > 0) "${"%.1f".format(temp.batteryTemp)}°C" else "—",
                        subtitle = tempLabel(temp.batteryTemp),
                        color    = tempColor(temp.batteryTemp),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (temp.thermalZones.isNotEmpty()) {
                    Text("ТЕПЛОВЫЕ ЗОНЫ", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp, letterSpacing = 1.sp)
                    temp.thermalZones.entries.take(8).forEach { (zone, t) ->
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text(zone, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            Text("${"%.1f".format(t)}°C", color = tempColor(t), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                InfoText("CPU: /sys/class/thermal/thermal_zone*/temp • Батарея: EXTRA_TEMPERATURE/10")
            }
        }

        // Батарея
        SectionCard(title = "БАТАРЕЯ", color = NeonYellow) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    NeonStatCard(
                        title    = "ЗАРЯД",
                        value    = "${bat.level}%",
                        subtitle = if (bat.isCharging) "⚡ Заряжается" else "Разряжается",
                        color    = if (bat.level > 20) NeonYellow else Color(0xFFFF4444),
                        modifier = Modifier.weight(1f)
                    )
                    NeonStatCard(
                        title    = "НАПРЯЖЕНИЕ",
                        value    = "${"%.2f".format(bat.voltage)} В",
                        subtitle = bat.technology,
                        color    = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    NeonStatCard(
                        title    = "ЗДОРОВЬЕ",
                        value    = bat.health,
                        color    = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                NeonProgressBar(
                    progress = bat.level / 100f,
                    color = if (bat.level > 20) NeonYellow else Color(0xFFFF4444),
                    modifier = Modifier.fillMaxWidth(), height = 8.dp
                )
                InfoText("Источник: Intent.ACTION_BATTERY_CHANGED (Sticky broadcast)")
            }
        }

        Spacer(Modifier.height(72.dp))
    }
}

private fun formatNetSpeed(kbps: Long): String = when {
    kbps >= 1024 -> "${"%.1f".format(kbps / 1024f)} МБ/с"
    else         -> "$kbps КБ/с"
}

private fun tempColor(t: Float): Color = when {
    t >= 65f -> Color(0xFFFF3D3D)
    t >= 50f -> Color(0xFFFFB300)
    t >= 35f -> NeonYellow
    else     -> NeonGreen
}

private fun tempLabel(t: Float): String = when {
    t >= 65f -> "🔴 Критично"
    t >= 50f -> "🟡 Высокая"
    t >= 35f -> "🟢 Норма"
    else     -> "❄️ Холодно"
}
