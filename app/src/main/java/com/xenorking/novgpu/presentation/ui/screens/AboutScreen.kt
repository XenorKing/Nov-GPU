package com.xenorking.novgpu.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenorking.novgpu.presentation.ui.theme.*

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    val rotation by rememberInfiniteTransition(label = "logo").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "spin"
    )

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // Логотип
        Box(
            modifier = Modifier
                .size(90.dp)
                .rotate(rotation)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(listOf(NeonCyan, NeonGreen, NeonPurple, NeonCyan))
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape)
                    .background(Color(0xFF080810)),
                contentAlignment = Alignment.Center
            ) {
                Text("⬡", color = NeonCyan, fontSize = 32.sp)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("novGPU", color = NeonCyan, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            Text("Версия 1.0.0", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Мониторинг системы в реальном времени:\nCPU, GPU, ОЗУ, температура, сеть, батарея",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Источники данных
        SectionCard(title = "ИСТОЧНИКИ ДАННЫХ", color = NeonCyan) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                DataSourceItem(
                    emoji = "🖥️",
                    title = "Загрузка CPU",
                    source = "/proc/stat",
                    detail = "Накопленные тики: user, nice, system, idle, iowait, irq, softirq\nФормула: Δactive / Δtotal × 100%\nОбновление: каждые 1000 мс"
                )
                DataSourceItem(
                    emoji = "⚡",
                    title = "Частота CPU",
                    source = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq",
                    detail = "Текущая частота в кГц, делится на 1000 → МГц\nПоддерживается на всех Android без root"
                )
                DataSourceItem(
                    emoji = "🎮",
                    title = "Загрузка GPU",
                    source = "sysfs (драйвер GPU)",
                    detail = "Qualcomm Adreno: /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage\nARM Mali: /sys/class/misc/mali0/device/utilization\nПримечание: доступность зависит от прошивки"
                )
                DataSourceItem(
                    emoji = "💾",
                    title = "Оперативная память",
                    source = "ActivityManager.getMemoryInfo()",
                    detail = "Официальный Android API\ntotalMem — весь объём\navailMem — свободная память\nЗанято = totalMem − availMem"
                )
                DataSourceItem(
                    emoji = "🌡️",
                    title = "Температура",
                    source = "Тепловые зоны + BatteryManager",
                    detail = "CPU: /sys/class/thermal/thermal_zone*/temp\nЗначение в мс°C, делится на 1000 → °C\nБатарея: Intent.ACTION_BATTERY_CHANGED (EXTRA_TEMPERATURE / 10)"
                )
                DataSourceItem(
                    emoji = "🌐",
                    title = "Скорость сети",
                    source = "android.net.TrafficStats",
                    detail = "getTotalRxBytes() — суммарно получено\ngetTotalTxBytes() — суммарно отправлено\nСкорость = Δбайт / Δвремя (КБ/с)\nУчитывает весь трафик всех приложений"
                )
                DataSourceItem(
                    emoji = "🔋",
                    title = "Батарея",
                    source = "Intent.ACTION_BATTERY_CHANGED",
                    detail = "Sticky broadcast — Android обновляет автоматически\nЗаряд: EXTRA_LEVEL / EXTRA_SCALE × 100%\nНапряжение: EXTRA_VOLTAGE (мВ) / 1000 → В\nТемпература: EXTRA_TEMPERATURE / 10 → °C"
                )
            }
        }

        // Root не нужен
        SectionCard(title = "БЕЗ ROOT", color = NeonGreen) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "novGPU работает полностью без root-прав и без специальных разрешений.\n" +
                    "Все данные берутся из публичных псевдофайлов Linux (/proc, /sys) " +
                    "и стандартных API Android.",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PermBadge("✅ Без root")
                    PermBadge("✅ Без разрешений")
                    PermBadge("✅ Офлайн")
                }
            }
        }

        // Ограничения
        SectionCard(title = "ОГРАНИЧЕНИЯ", color = NeonYellow) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LimitItem("GPU загрузка", "Доступна не на всех устройствах — зависит от драйвера и прошивки")
                LimitItem("Температура CPU", "Число тепловых зон и их название различаются у разных SoC")
                LimitItem("/proc/stat", "На некоторых прошивках (MIUI, OneUI) файл может быть закрыт")
                LimitItem("Имя GPU", "Для получения строки рендерера нужен кратковременный GL-контекст")
            }
        }

        // Стек технологий
        SectionCard(title = "ТЕХНОЛОГИИ", color = NeonPurple) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TechRow("Язык", "Kotlin 2.1.0")
                TechRow("UI", "Jetpack Compose + Material3")
                TechRow("Архитектура", "MVI (Model-View-Intent)")
                TechRow("DI", "Hilt (Dagger)")
                TechRow("Многопоточность", "Kotlin Coroutines + Flow")
                TechRow("Сборка", "Gradle 8.10.2 + AGP 8.7.3")
                TechRow("CI/CD", "GitHub Actions (автосборка APK)")
                TechRow("Min Android", "Android 8.0 (API 26)")
            }
        }

        Text(
            "novGPU v1.0.0  •  2026",
            color = Color.White.copy(alpha = 0.2f),
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun DataSourceItem(emoji: String, title: String, source: String, detail: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(emoji, fontSize = 18.sp)
            Column {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(source, color = NeonCyan.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            detail,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(start = 26.dp)
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
    }
}

@Composable
private fun PermBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NeonGreen.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LimitItem(title: String, desc: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("⚠", color = NeonYellow, fontSize = 12.sp, modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = NeonYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun TechRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp)
        Text(value, color = NeonPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
