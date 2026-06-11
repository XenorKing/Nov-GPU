package com.xenorking.novgpu.data.monitor

import com.xenorking.novgpu.domain.model.CpuStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CpuMonitor @Inject constructor() {

    private var prevTotal = LongArray(0)
    private var prevIdle = LongArray(0)
    private val history = ArrayDeque<Float>(60)

    suspend fun getStats(): CpuStats = withContext(Dispatchers.IO) {
        val (usage, coreUsages) = readCpuUsage()
        val freq = readCpuFrequency()
        val maxFreq = readMaxCpuFrequency()
        val arch = System.getProperty("os.arch") ?: "unknown"

        history.addLast(usage)
        if (history.size > 60) history.removeFirst()

        CpuStats(
            usagePercent = usage,
            coreUsages = coreUsages,
            frequencyMhz = freq,
            maxFrequencyMhz = maxFreq,
            architecture = arch,
            coreCount = Runtime.getRuntime().availableProcessors(),
            history = history.toList()
        )
    }

    private fun readCpuUsage(): Pair<Float, List<Float>> {
        return try {
            val lines = File("/proc/stat").readLines()
            val cpuLines = lines.filter { it.startsWith("cpu") }

            if (prevTotal.isEmpty()) {
                prevTotal = LongArray(cpuLines.size)
                prevIdle = LongArray(cpuLines.size)
            }

            val usages = mutableListOf<Float>()
            var totalUsage = 0f

            cpuLines.forEachIndexed { idx, line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size < 5) return@forEachIndexed

                val values = parts.drop(1).map { it.toLongOrNull() ?: 0L }
                val idle = values.getOrElse(3) { 0L } + values.getOrElse(4) { 0L }
                val total = values.sum()

                val deltaTotal = total - prevTotal.getOrElse(idx) { 0L }
                val deltaIdle = idle - prevIdle.getOrElse(idx) { 0L }

                val usage = if (deltaTotal > 0) {
                    ((deltaTotal - deltaIdle).toFloat() / deltaTotal * 100f).coerceIn(0f, 100f)
                } else 0f

                if (idx == 0) {
                    totalUsage = usage
                } else {
                    usages.add(usage)
                }

                if (idx < prevTotal.size) {
                    prevTotal[idx] = total
                    prevIdle[idx] = idle
                }
            }

            Pair(totalUsage, usages)
        } catch (e: Exception) {
            Pair(0f, emptyList())
        }
    }

    private fun readCpuFrequency(): Long {
        return try {
            val file = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (file.exists()) file.readText().trim().toLong() / 1000L else 0L
        } catch (e: Exception) { 0L }
    }

    private fun readMaxCpuFrequency(): Long {
        return try {
            val file = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (file.exists()) file.readText().trim().toLong() / 1000L else 0L
        } catch (e: Exception) { 0L }
    }
}
