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
import com.xenorking.novgpu.domain.model.GpuStats
import com.xenorking.novgpu.domain.model.RamStats
import com.xenorking.novgpu.presentation.ui.components.*
import com.xenorking.novgpu.presentation.ui.theme.*

@Composable
fun GpuRamScreen(gpu: GpuStats, ram: RamStats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // GPU Section
        Text(
            "[ GPU ]",
            color = GpuColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            NeonGauge(value = gpu.usagePercent, color = GpuColor, label = "GPU LOAD", size = 140.dp)
        }

        NeonStatCard(accentColor = GpuColor) {
            Text("GPU HISTORY", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            NeonLineChart(data = gpu.history, color = GpuColor, height = 80.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                "RENDERER: ${gpu.renderer}",
                color = GpuColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                "VENDOR: ${gpu.vendor}",
                color = GpuColor.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // RAM Section
        Text(
            "[ RAM ]",
            color = RamColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        NeonStatCard(accentColor = RamColor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonGauge(value = ram.usagePercent, color = RamColor, label = "USED", size = 110.dp)
                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RamBarItem("USED", ram.usedMb, ram.totalMb, RamColor)
                    RamBarItem("FREE", ram.availableMb, ram.totalMb, Color(0xFF00CCFF))
                    if (ram.swapTotalMb > 0) {
                        RamBarItem("SWAP", ram.swapUsedMb, ram.swapTotalMb, Color(0xFFAA00FF))
                    }
                }
            }
        }

        NeonStatCard(accentColor = RamColor) {
            Text("RAM HISTORY", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            NeonLineChart(data = ram.history, color = RamColor, height = 80.dp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MemItem("TOTAL", "${ram.totalMb} MB", RamColor)
                MemItem("USED", "${ram.usedMb} MB", RamColor)
                MemItem("FREE", "${ram.availableMb} MB", RamColor)
                if (ram.swapTotalMb > 0) MemItem("SWAP", "${ram.swapTotalMb} MB", Color(0xFFAA00FF))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun RamBarItem(label: String, value: Long, total: Long, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            Text("$value MB", color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(3.dp))
        NeonProgressBar(progress = if (total > 0) value.toFloat() / total else 0f, color = color, barHeight = 6.dp)
    }
}

@Composable
private fun MemItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, letterSpacing = 1.sp)
        Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
