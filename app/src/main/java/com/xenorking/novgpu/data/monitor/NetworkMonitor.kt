package com.xenorking.novgpu.data.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import com.xenorking.novgpu.domain.model.NetworkStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var prevRxBytes = TrafficStats.getTotalRxBytes()
    private var prevTxBytes = TrafficStats.getTotalTxBytes()
    private var prevTime = System.currentTimeMillis()

    private val downloadHistory = ArrayDeque<Long>(60)
    private val uploadHistory = ArrayDeque<Long>(60)

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    suspend fun getStats(): NetworkStats = withContext(Dispatchers.IO) {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()
        val currentTime = System.currentTimeMillis()

        val elapsedMs = (currentTime - prevTime).coerceAtLeast(1L)
        val rxDelta = (currentRx - prevRxBytes).coerceAtLeast(0L)
        val txDelta = (currentTx - prevTxBytes).coerceAtLeast(0L)

        val downloadSpeedKbps = rxDelta * 1000L / elapsedMs / 1024L
        val uploadSpeedKbps = txDelta * 1000L / elapsedMs / 1024L

        prevRxBytes = currentRx
        prevTxBytes = currentTx
        prevTime = currentTime

        downloadHistory.addLast(downloadSpeedKbps)
        uploadHistory.addLast(uploadSpeedKbps)
        if (downloadHistory.size > 60) downloadHistory.removeFirst()
        if (uploadHistory.size > 60) uploadHistory.removeFirst()

        NetworkStats(
            downloadSpeedKbps = downloadSpeedKbps,
            uploadSpeedKbps = uploadSpeedKbps,
            totalDownloadMb = currentRx / (1024 * 1024),
            totalUploadMb = currentTx / (1024 * 1024),
            downloadHistory = downloadHistory.toList(),
            uploadHistory = uploadHistory.toList(),
            connectionType = getConnectionType()
        )
    }

    private fun getConnectionType(): String {
        val network = connectivityManager.activeNetwork ?: return "None"
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return "None"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
    }
}
