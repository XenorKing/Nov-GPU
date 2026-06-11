package com.xenorking.novgpu.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenorking.novgpu.domain.model.NetworkStats
import com.xenorking.novgpu.domain.model.TemperatureStats
import com.xenorking.novgpu.presentation.ui.components.*
import com.xenorking.novgpu.presentation.ui.theme.*

@Composable
fun NetworkTempScreen(network: NetworkStats, temperature: TemperatureStats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Network section
        Text("[ NETWORK ]", color = NetDownColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

        // Speed cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeonStatCard(accentColor = NetDownColor, modifier = Modifier.weight(1f)) {
                Text("DOWNLOAD", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    formatNetSpeed(network.downloadSpeedKbps),
                    color = NetDownColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            NeonStatCard(accentColor = NetUpColor, modifier = Modifier.weight(1f)) {
                Text("UPLOAD", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    formatNetSpeed(network.uploadSpeedKbps),
                    color = NetUpColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Download chart
        NeonStatCard(accentColor = NetDownColor) {
            Text("DOWNLOAD HISTORY", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            NeonLineChart(
                data = network.downloadHistory.map { it.toFloat() },
                color = NetDownColor,
                height = 80.dp,
                maxValue = network.downloadHistory.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
            )
        }

        // Upload chart
        NeonStatCard(accentColor = NetUpColor) {
            Text("UPLOAD HISTORY", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            NeonLineChart(
                data = network.uploadHistory.map { it.toFloat() },
                color = NetUpColor,
                height = 80.dp,
                maxValue = network.uploadHistory.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NetStatItem("TOTAL DOWN", "${network.totalDownloadMb} MB", NetDownColor)
            NetStatItem("TOTAL UP", "${network.totalUploadMb} MB", NetUpColor)
            NetStatItem("TYPE", network.connectionType, Color.White)
        }

        Spacer(Modifier.height(8.dp))

        // Temperature section
        Text("[ TEMPERATURE ]", color = TempColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NeonGauge(
                value = temperature.cpuTemp,
                maxValue = 100f,
                color = tempColor(temperature.cpuTemp),
                label = "CPU",
                unit = "°C",
                size = 120.dp
            )
            NeonGauge(
                value = temperature.batteryTemp,
                maxValue = 60f,
                color = tempColor(temperature.batteryTemp),
                label = "BATTERY",
                unit = "°C",
                size = 120.dp
            )
        }

        // Thermal zones
        if (temperature.thermalZones.isNotEmpty()) {
            NeonStatCard(accentColor = TempColor) {
                Text("THERMAL ZONES", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(12.dp))
                temperature.thermalZones.entries.forEach { (zone, temp) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            zone.take(16).uppercase(),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.width(100.dp)) {
                            NeonProgressBar(progress = (temp / 100f).coerceIn(0f, 1f), color = tempColor(temp), barHeight = 6.dp)
                        }
                        Text(
                            "%.1f°C".format(temp),
                            color = tempColor(temp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(55.dp).padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun tempColor(temp: Float): Color = when {
    temp > 75f -> Color(0xFFFF2200)
    temp > 60f -> Color(0xFFFF6600)
    temp > 45f -> Color(0xFFFFCC00)
    else -> Color(0xFF00CC88)
}

private fun formatNetSpeed(kbps: Long): String = when {
    kbps >= 1024 -> "%.1f MB/s".format(kbps / 1024f)
    else -> "$kbps KB/s"
}

@Composable
private fun NetStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, letterSpacing = 1.sp)
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
