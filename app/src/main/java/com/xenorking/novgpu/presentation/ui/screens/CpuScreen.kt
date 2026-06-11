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
import com.xenorking.novgpu.domain.model.CpuStats
import com.xenorking.novgpu.presentation.ui.components.*
import com.xenorking.novgpu.presentation.ui.theme.*

@Composable
fun CpuScreen(cpu: CpuStats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main gauge
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            NeonGauge(value = cpu.usagePercent, color = CpuColor, label = "CPU LOAD", size = 160.dp)
        }

        // History chart
        NeonStatCard(accentColor = CpuColor) {
            Text("USAGE HISTORY (60s)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            NeonLineChart(data = cpu.history, color = CpuColor, height = 100.dp)
        }

        // Specs card
        NeonStatCard(accentColor = CpuColor) {
            Text("PROCESSOR INFO", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(12.dp))
            SpecRow("Architecture", cpu.architecture.uppercase(), CpuColor)
            SpecRow("Core Count", "${cpu.coreCount} cores", CpuColor)
            SpecRow("Current Freq", "${cpu.frequencyMhz} MHz", CpuColor)
            SpecRow("Max Freq", "${cpu.maxFrequencyMhz} MHz", CpuColor)
        }

        // Per-core usage
        if (cpu.coreUsages.isNotEmpty()) {
            NeonStatCard(accentColor = CpuColor) {
                Text("PER-CORE USAGE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(12.dp))
                cpu.coreUsages.forEachIndexed { index, usage ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "CPU${index + 1}",
                            color = CpuColor.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            modifier = Modifier.width(50.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            NeonProgressBar(progress = usage / 100f, color = CpuColor)
                        }
                        Text(
                            "${usage.toInt()}%",
                            color = CpuColor,
                            fontSize = 11.sp,
                            modifier = Modifier.width(40.dp).padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SpecRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
