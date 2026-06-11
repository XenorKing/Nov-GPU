package com.xenorking.novgpu.presentation.ui.screens

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

/**
 * Данные CPU:
 * • Загрузка: /proc/stat — разница активных/общих тиков за интервал
 * • Частота: /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq (делится на 1000 → МГц)
 * • Ядра: Runtime.availableProcessors()
 * • Архитектура: System.getProperty("os.arch")
 */
@Composable
fun CpuScreen(stats: SystemStats, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val cpu = stats.cpu

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle("ПРОЦЕССОР", "Данные из /proc/stat")

        // Большой индикатор
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            NeonGauge(value = cpu.usagePercent, color = NeonCyan, label = "НАГРУЗКА", size = 160.dp)
        }

        // Статистика
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            NeonStatCard(
                title    = "ЯДРА",
                value    = "${cpu.coreCount}",
                subtitle = cpu.architecture,
                color    = NeonCyan,
                modifier = Modifier.weight(1f)
            )
            NeonStatCard(
                title    = "ЧАСТОТА",
                value    = if (cpu.frequencyMhz > 0) "${cpu.frequencyMhz}" else "—",
                subtitle = if (cpu.maxFrequencyMhz > 0) "макс: ${cpu.maxFrequencyMhz} МГц" else "МГц",
                color    = NeonBlue,
                modifier = Modifier.weight(1f)
            )
            NeonStatCard(
                title    = "НАГРУЗКА",
                value    = "${"%.1f".format(cpu.usagePercent)}%",
                subtitle = loadLabel(cpu.usagePercent),
                color    = loadColor(cpu.usagePercent),
                modifier = Modifier.weight(1f)
            )
        }

        // Частота → прогрессбар
        if (cpu.maxFrequencyMhz > 0) {
            SectionCard(title = "ТАКТОВАЯ ЧАСТОТА", color = NeonCyan) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("${cpu.frequencyMhz} МГц", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${cpu.maxFrequencyMhz} МГц макс", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                    }
                    NeonProgressBar(
                        progress = if (cpu.maxFrequencyMhz > 0) cpu.frequencyMhz.toFloat() / cpu.maxFrequencyMhz else 0f,
                        color = NeonCyan,
                        modifier = Modifier.fillMaxWidth()
                    )
                    InfoText("Источник: /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
                }
            }
        }

        // Загрузка по ядрам
        if (cpu.coreUsages.isNotEmpty()) {
            SectionCard(title = "ЯДРА (${cpu.coreUsages.size} шт.)", color = NeonPurple) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cpu.coreUsages.forEachIndexed { i, usage ->
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                            Text("CPU$i", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, modifier = Modifier.width(34.dp))
                            NeonProgressBar(progress = usage / 100f, color = coreColor(usage), modifier = Modifier.weight(1f))
                            Text("${"%.0f".format(usage)}%", color = coreColor(usage), fontSize = 10.sp, modifier = Modifier.width(36.dp))
                        }
                    }
                }
            }
        }

        // График истории
        SectionCard(title = "ИСТОРИЯ ЗАГРУЗКИ", color = NeonCyan) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                NeonLineChart(data = cpu.history, color = NeonCyan, height = 100.dp, modifier = Modifier.fillMaxWidth())
                InfoText("60 точек • обновление каждые 1000 мс • источник: /proc/stat")
            }
        }

        // Как вычисляется
        SectionCard(title = "КАК ВЫЧИСЛЯЕТСЯ", color = NeonGreen) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FormulaText("Источник", "/proc/stat — псевдофайл Linux с тиками CPU")
                FormulaText("Формат", "cpu user nice system idle iowait irq softirq steal")
                FormulaText("Активное время", "total − idle − iowait")
                FormulaText("Нагрузка", "ΔactiveTime / ΔtotalTime × 100%")
                FormulaText("Интервал", "1000 мс между измерениями")
            }
        }

        Spacer(Modifier.height(72.dp))
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String) {
    Column {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        Text(subtitle, color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
fun SectionCard(title: String, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.07f), Color(0xFF080810))))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = color.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            content()
        }
    }
}

@Composable
fun InfoText(text: String) {
    Text(text, color = Color.White.copy(alpha = 0.25f), fontSize = 9.sp, letterSpacing = 0.3.sp)
}

@Composable
fun FormulaText(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text("$label:  ", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, modifier = Modifier.width(110.dp))
        Text(value, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
    }
}

private fun loadLabel(v: Float) = when {
    v >= 80f -> "Высокая"
    v >= 50f -> "Средняя"
    v >= 20f -> "Низкая"
    else     -> "Простой"
}

private fun loadColor(v: Float): Color = when {
    v >= 80f -> Color(0xFFFF4444)
    v >= 50f -> NeonYellow
    else     -> NeonCyan
}

private fun coreColor(v: Float): Color = when {
    v >= 80f -> Color(0xFFFF4444)
    v >= 60f -> NeonYellow
    v >= 30f -> NeonCyan
    else     -> NeonGreen
}
