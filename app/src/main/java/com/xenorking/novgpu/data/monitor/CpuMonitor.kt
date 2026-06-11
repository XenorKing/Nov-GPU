package com.xenorking.novgpu.data.monitor

import android.os.SystemClock
import com.xenorking.novgpu.domain.model.CpuStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Источник данных: /proc/stat — системный псевдофайл Linux.
 * Содержит накопленное время CPU в единицах USER_HZ (обычно 1/100 сек).
 * Поля: user, nice, system, idle, iowait, irq, softirq, steal, guest, guest_nice
 *
 * Формула: usage = (ΔactiveTime / ΔtotalTime) × 100
 * где activeTime = total − idle − iowait
 */
@Singleton
class CpuMonitor @Inject constructor() {

    @Volatile private var prevTotals = LongArray(0)
    @Volatile private var prevIdles  = LongArray(0)
    private val history = ArrayDeque<Float>(60)

    suspend fun getStats(): CpuStats = withContext(Dispatchers.IO) {
        val (total, cores) = readCpuUsage()
        history.addLast(total)
        if (history.size > 60) history.removeFirst()
        CpuStats(
            usagePercent    = total,
            coreUsages      = cores,
            frequencyMhz    = readCpuFreq(0),
            maxFrequencyMhz = readMaxCpuFreq(0),
            architecture    = System.getProperty("os.arch") ?: "unknown",
            coreCount       = Runtime.getRuntime().availableProcessors(),
            history         = history.toList()
        )
    }

    @Synchronized
    private fun readCpuUsage(): Pair<Float, List<Float>> {
        return try {
            val lines = File("/proc/stat").bufferedReader().readLines()
            val cpuLines = lines.filter { it.startsWith("cpu") }

            // Инициализация при первом чтении
            if (prevTotals.size != cpuLines.size) {
                prevTotals = LongArray(cpuLines.size)
                prevIdles  = LongArray(cpuLines.size)
            }

            val usages = mutableListOf<Float>()
            var totalUsage = 0f

            cpuLines.forEachIndexed { idx, line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size < 5) return@forEachIndexed
                // parts[0] = "cpu" or "cpu0" …
                val vals     = parts.drop(1).mapNotNull { it.toLongOrNull() }
                if (vals.size < 4) return@forEachIndexed

                val user     = vals[0]
                val nice     = vals[1]
                val system   = vals[2]
                val idle     = vals[3]
                val iowait   = vals.getOrElse(4) { 0L }
                val irq      = vals.getOrElse(5) { 0L }
                val softirq  = vals.getOrElse(6) { 0L }
                val steal    = vals.getOrElse(7) { 0L }

                val totalNow  = user + nice + system + idle + iowait + irq + softirq + steal
                val idleNow   = idle + iowait
                val activeNow = totalNow - idleNow

                val dTotal  = totalNow  - prevTotals[idx]
                val dIdle   = idleNow   - prevIdles[idx]
                val dActive = activeNow - (prevTotals[idx] - prevIdles[idx])

                val usage = if (dTotal > 0L) (dActive.toFloat() / dTotal * 100f).coerceIn(0f, 100f) else 0f

                prevTotals[idx] = totalNow
                prevIdles[idx]  = idleNow

                if (idx == 0) totalUsage = usage else usages.add(usage)
            }
            Pair(totalUsage, usages)
        } catch (e: Exception) {
            Pair(0f, emptyList())
        }
    }

    private fun readCpuFreq(core: Int): Long = try {
        File("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq")
            .takeIf { it.exists() }?.readText()?.trim()?.toLong()?.div(1000L) ?: 0L
    } catch (e: Exception) { 0L }

    private fun readMaxCpuFreq(core: Int): Long = try {
        File("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq")
            .takeIf { it.exists() }?.readText()?.trim()?.toLong()?.div(1000L) ?: 0L
    } catch (e: Exception) { 0L }
}
