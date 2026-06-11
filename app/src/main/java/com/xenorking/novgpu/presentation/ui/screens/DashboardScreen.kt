package com.xenorking.novgpu.presentation.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.xenorking.novgpu.domain.model.SystemStats
import com.xenorking.novgpu.presentation.ui.components.*
import com.xenorking.novgpu.presentation.ui.theme.*

@Composable
fun DashboardScreen(stats: SystemStats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top gauges row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NeonGauge(
                value = stats.cpu.usagePercent,
                color = CpuColor,
                label = "CPU",
                size = 110.dp
            )
            NeonGauge(
                value = stats.gpu.usagePercent,
                color = GpuColor,
                label = "GPU",
                size = 110.dp
            )
            NeonGauge(
                value = stats.ram.usagePercent,
                color = RamColor,
                label = "RAM",
                size = 110.dp
            )
        }

        // CPU Card
        NeonStatCard(accentColor = CpuColor) {
            SectionHeader("CPU", stats.cpu.usagePercent, "%", CpuColor)
            Spacer(Modifier.height(8.dp))
            NeonLineChart(
                data = stats.cpu.history,
                color = CpuColor,
                height = 64.dp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("CORES", "${stats.cpu.coreCount}", CpuColor)
                StatItem("FREQ", "${stats.cpu.frequencyMhz} MHz", CpuColor)
                StatItem("ARCH", stats.cpu.architecture.take(8).uppercase(), CpuColor)
            }
        }

        // GPU Card
        NeonStatCard(accentColor = GpuColor) {
            SectionHeader("GPU", stats.gpu.usagePercent, "%", GpuColor)
            Spacer(Modifier.height(8.dp))
            NeonLineChart(
                data = stats.gpu.history,
                color = GpuColor,
                height = 64.dp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stats.gpu.renderer.take(32),
                color = GpuColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        // RAM Card
        NeonStatCard(accentColor = RamColor) {
            SectionHeader("RAM", stats.ram.usagePercent, "%", RamColor)
            Spacer(Modifier.height(8.dp))
            NeonProgressBar(
                progress = stats.ram.usagePercent / 100f,
                color = RamColor
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("USED", "${stats.ram.usedMb} MB", RamColor)
                StatItem("FREE", "${stats.ram.availableMb} MB", RamColor)
                StatItem("TOTAL", "${stats.ram.totalMb} MB", RamColor)
            }
        }

        // Network Card
        NeonStatCard(accentColor = NetDownColor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NETWORK", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Text(
                    stats.network.connectionType,
                    color = NetDownColor.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    NeonLineChart(data = stats.network.downloadHistory.map { it.toFloat() }, color = NetDownColor, height = 50.dp, maxValue = stats.network.downloadHistory.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f)
                }
                Box(modifier = Modifier.weight(1f)) {
                    NeonLineChart(data = stats.network.uploadHistory.map { it.toFloat() }, color = NetUpColor, height = 50.dp, maxValue = stats.network.uploadHistory.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("DOWN", formatSpeed(stats.network.downloadSpeedKbps), NetDownColor)
                StatItem("UP", formatSpeed(stats.network.uploadSpeedKbps), NetUpColor)
                StatItem("TYPE", stats.network.connectionType, NetDownColor)
            }
        }

        // Temperature Card
        NeonStatCard(accentColor = TempColor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TEMPERATURE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TempGaugeItem("CPU", stats.temperature.cpuTemp)
                TempGaugeItem("BATTERY", stats.temperature.batteryTemp)
            }
            if (stats.temperature.thermalZones.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                stats.temperature.thermalZones.entries.take(4).chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { (zone, temp) ->
                            StatItem(zone.take(10).uppercase(), "%.1f°C".format(temp), TempColor)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(title: String, value: Float, unit: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = if (unit == "%") "${value.toInt()}" else "%.1f".format(value),
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                color = color.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, letterSpacing = 1.sp)
        Text(text = value, color = color.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TempGaugeItem(label: String, temp: Float) {
    NeonGauge(
        value = temp,
        maxValue = 100f,
        color = when {
            temp > 70f -> Color(0xFFFF2200)
            temp > 50f -> TempColor
            else -> Color(0xFF00CC88)
        },
        label = label,
        unit = "°C",
        size = 90.dp
    )
}

private fun formatSpeed(kbps: Long): String {
    return when {
        kbps >= 1024 -> "%.1f MB/s".format(kbps / 1024f)
        else -> "$kbps KB/s"
    }
}
