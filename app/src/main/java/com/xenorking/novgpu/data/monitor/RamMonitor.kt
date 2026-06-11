package com.xenorking.novgpu.data.monitor

import android.app.ActivityManager
import android.content.Context
import com.xenorking.novgpu.domain.model.RamStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RamMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val history = ArrayDeque<Float>(60)

    suspend fun getStats(): RamStats = withContext(Dispatchers.IO) {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availableMb = memInfo.availMem / (1024 * 1024)
        val usedMb = totalMb - availableMb
        val usagePercent = if (totalMb > 0) (usedMb.toFloat() / totalMb * 100f) else 0f

        val (swapTotal, swapUsed) = readSwapInfo()

        history.addLast(usagePercent)
        if (history.size > 60) history.removeFirst()

        RamStats(
            totalMb = totalMb,
            usedMb = usedMb,
            availableMb = availableMb,
            usagePercent = usagePercent,
            swapTotalMb = swapTotal,
            swapUsedMb = swapUsed,
            history = history.toList()
        )
    }

    private fun readSwapInfo(): Pair<Long, Long> {
        return try {
            val memInfo = File("/proc/meminfo").readLines()
            val swapTotal = memInfo.find { it.startsWith("SwapTotal:") }
                ?.split("\\s+".toRegex())?.getOrNull(1)?.toLongOrNull()?.div(1024) ?: 0L
            val swapFree = memInfo.find { it.startsWith("SwapFree:") }
                ?.split("\\s+".toRegex())?.getOrNull(1)?.toLongOrNull()?.div(1024) ?: 0L
            Pair(swapTotal, swapTotal - swapFree)
        } catch (e: Exception) {
            Pair(0L, 0L)
        }
    }
}
