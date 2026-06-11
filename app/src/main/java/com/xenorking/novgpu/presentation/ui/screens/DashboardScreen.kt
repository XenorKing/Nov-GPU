package com.xenorking.novgpu.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenorking.novgpu.domain.model.SystemStats
import com.xenorking.novgpu.presentation.ui.components.*
import com.xenorking.novgpu.presentation.ui.theme.*

@Composable
fun DashboardScreen(stats: SystemStats, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        AnimatedHeader()

        // Главные индикаторы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NeonGauge(value = stats.cpu.usagePercent, color = NeonCyan,  label = "ПРОЦЕССОР", size = 110.dp)
            NeonGauge(value = stats.gpu.usagePercent, color = NeonGreen, label = "GPU",       size = 110.dp)
            NeonGauge(value = stats.ram.usagePercent, color = NeonPurple,label = "ОЗУ",       size = 110.dp)
        }

        // Карточки статистики
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonStatCard(
                title    = "ЗАРЯД",
                value    = "${stats.battery.level}%",
                subtitle = if (stats.battery.isCharging) "⚡ Заряжается" else "Напряжение: ${"%.2f".format(stats.battery.voltage)} В",
                color    = NeonYellow,
                modifier = Modifier.weight(1f)
            )
            NeonStatCard(
                title    = "ТЕМП. БАТАРЕИ",
                value    = "${"%.1f".format(stats.temperature.batteryTemp)}°C",
                subtitle = stats.battery.health,
                color    = tempColor(stats.temperature.batteryTemp),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonStatCard(
                title    = "СЕТЬ ↓",
                value    = formatSpeed(stats.network.downloadSpeedKbps),
                subtitle = "Получено: ${stats.network.totalDownloadMb} МБ",
                color    = NeonBlue,
                modifier = Modifier.weight(1f)
            )
            NeonStatCard(
                title    = "СЕТЬ ↑",
                value    = formatSpeed(stats.network.uploadSpeedKbps),
                subtitle = "Отправлено: ${stats.network.totalUploadMb} МБ",
                color    = NeonPink,
                modifier = Modifier.weight(1f)
            )
        }

        // Графики
        SectionLabel("ИСТОРИЯ НАГРУЗКИ")
        NeonChartCard(title = "Процессор %", data = stats.cpu.history.map { it }, color = NeonCyan)
        NeonChartCard(title = "ОЗУ %", data = stats.ram.history, color = NeonPurple)

        // ОЗУ детали
        SectionLabel("ОПЕРАТИВНАЯ ПАМЯТЬ")
        RamDetailCard(stats)

        // Частота CPU
        if (stats.cpu.frequencyMhz > 0) {
            SectionLabel("ЧАСТОТА ПРОЦЕССОРА")
            FreqCard(stats)
        }

        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun AnimatedHeader() {
    val offset by rememberInfiniteTransition(label = "header").animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "headerFloat"
    )
    Column(modifier = Modifier.offset(y = offset.dp)) {
        Text(
            text = "novGPU",
            color = NeonCyan,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp
        )
        Text(
            text = "Мониторинг системы в реальном времени",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.4f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
    )
}

@Composable
private fun NeonChartCard(title: String, data: List<Float>, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.07f), Color(0xFF080810))))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        NeonLineChart(data = data, color = color, label = title, height = 70.dp)
    }
}

@Composable
private fun RamDetailCard(stats: com.xenorking.novgpu.domain.model.SystemStats) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(NeonPurple.copy(alpha = 0.07f), Color(0xFF080810))))
            .border(1.dp, NeonPurple.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Занято: ${stats.ram.usedMb} МБ", color = NeonPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Всего: ${stats.ram.totalMb} МБ", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            NeonProgressBar(progress = stats.ram.usagePercent / 100f, color = NeonPurple, modifier = Modifier.fillMaxWidth())
            Text("Свободно: ${stats.ram.availableMb} МБ", color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun FreqCard(stats: com.xenorking.novgpu.domain.model.SystemStats) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(NeonCyan.copy(alpha = 0.07f), Color(0xFF080810))))
            .border(1.dp, NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Текущая: ${stats.cpu.frequencyMhz} МГц", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Макс: ${stats.cpu.maxFrequencyMhz} МГц", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
            }
            val freqProgress = if (stats.cpu.maxFrequencyMhz > 0) stats.cpu.frequencyMhz.toFloat() / stats.cpu.maxFrequencyMhz else 0f
            NeonProgressBar(progress = freqProgress, color = NeonCyan, modifier = Modifier.fillMaxWidth())
            Text("Ядер: ${stats.cpu.coreCount}  •  ${stats.cpu.architecture}", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
        }
    }
}

private fun tempColor(temp: Float): Color = when {
    temp >= 60f -> Color(0xFFFF3D3D)
    temp >= 45f -> NeonYellow
    else        -> NeonGreen
}

private fun formatSpeed(kbps: Long): String = when {
    kbps >= 1024 -> "${"%.1f".format(kbps / 1024f)} МБ/с"
    else         -> "$kbps КБ/с"
}
