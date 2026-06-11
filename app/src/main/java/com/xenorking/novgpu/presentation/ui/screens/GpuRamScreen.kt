package com.xenorking.novgpu.presentation.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

/**
 * GPU: /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage (Qualcomm Adreno)
 *      /sys/class/misc/mali0/device/utilization (ARM Mali)
 *      Название/вендор: OpenGL ES renderer string через GLSurfaceView
 *
 * ОЗУ: ActivityManager.getMemoryInfo() — официальный Android API
 *      totalMem, availMem, threshold — системные значения
 */
@Composable
fun GpuRamScreen(stats: SystemStats, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val gpu = stats.gpu
    val ram = stats.ram

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle("GPU / ОЗУ", "Видеопроцессор и оперативная память")

        // Индикаторы
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
            NeonGauge(value = gpu.usagePercent, color = NeonGreen, label = "GPU", size = 130.dp)
            NeonGauge(value = ram.usagePercent, color = NeonPurple, label = "ОЗУ", size = 130.dp)
        }

        // GPU инфо
        SectionCard(title = "ВИДЕОПРОЦЕССОР", color = NeonGreen) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    NeonStatCard(
                        title    = "НАГРУЗКА",
                        value    = "${"%.1f".format(gpu.usagePercent)}%",
                        subtitle = if (gpu.usagePercent >= 80) "Высокая" else if (gpu.usagePercent >= 40) "Средняя" else "Низкая",
                        color    = NeonGreen, modifier = Modifier.weight(1f)
                    )
                    NeonStatCard(
                        title    = "ВЕНДОР",
                        value    = if (gpu.vendor.length > 10) gpu.vendor.take(10) + "…" else gpu.vendor,
                        subtitle = gpu.version.take(16),
                        color    = NeonCyan, modifier = Modifier.weight(1f)
                    )
                }
                Text("Рендерер: ${gpu.renderer}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                NeonLineChart(data = gpu.history, color = NeonGreen, label = "История GPU %", height = 80.dp)
                FormulaText("Источник (Adreno)", "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage")
                FormulaText("Источник (Mali)",   "/sys/class/misc/mali0/device/utilization")
                InfoText("Если файл недоступен — вернётся 0% (ограничение Android без root)")
            }
        }

        // ОЗУ инфо
        SectionCard(title = "ОПЕРАТИВНАЯ ПАМЯТЬ", color = NeonPurple) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("${ram.usedMb} МБ занято", color = NeonPurple, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("${ram.totalMb} МБ всего", color = Color.White.copy(alpha = 0.45f), fontSize = 13.sp)
                }
                NeonProgressBar(progress = ram.usagePercent / 100f, color = NeonPurple, modifier = Modifier.fillMaxWidth(), height = 10.dp)

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    NeonStatCard(
                        title = "СВОБОДНО",  value = "${ram.availableMb} МБ",
                        color = NeonGreen, modifier = Modifier.weight(1f)
                    )
                    NeonStatCard(
                        title = "ЗАНЯТО",    value = "${ram.usedMb} МБ",
                        color = NeonPurple, modifier = Modifier.weight(1f)
                    )
                    NeonStatCard(
                        title = "ВСЕГО",     value = "${ram.totalMb} МБ",
                        color = NeonBlue, modifier = Modifier.weight(1f)
                    )
                }

                NeonLineChart(data = ram.history, color = NeonPurple, label = "История ОЗУ %", height = 80.dp)

                FormulaText("Источник",   "ActivityManager.getMemoryInfo()")
                FormulaText("totalMem",   "Весь объём RAM устройства")
                FormulaText("availMem",   "Доступная память прямо сейчас")
                FormulaText("Занято",     "totalMem − availMem")
                FormulaText("Загрузка %", "usedMem / totalMem × 100")
            }
        }

        // Как вычисляется GPU
        SectionCard(title = "КАК РАБОТАЕТ GPU МОНИТОРИНГ", color = NeonGreen) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Android не предоставляет официального API для загрузки GPU. " +
                    "Приложение читает системные файлы sysfs, которые публикует драйвер GPU.",
                    color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, lineHeight = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                FormulaText("Adreno (Qualcomm)", "gpu_busy_percentage — % занятости")
                FormulaText("Mali (ARM)",        "utilization — значение 0–100")
                FormulaText("Обновление",        "каждые 1000 мс")
                InfoText("На некоторых устройствах доступ закрыт — это ограничение производителя")
            }
        }

        Spacer(Modifier.height(72.dp))
    }
}
