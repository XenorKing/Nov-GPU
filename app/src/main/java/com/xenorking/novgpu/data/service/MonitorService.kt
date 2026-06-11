package com.xenorking.novgpu.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.xenorking.novgpu.R
import com.xenorking.novgpu.data.repository.SystemStatsRepository
import com.xenorking.novgpu.presentation.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@AndroidEntryPoint
class MonitorService : Service() {

    @Inject lateinit var repository: SystemStatsRepository

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val notificationId = 1337
    private val channelId = "novgpu_monitor"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(notificationId, buildNotification("CPU: -- | GPU: -- | RAM: --"))
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        scope.launch {
            repository.getStatsFlow(2000L)
                .catch { /* ignore errors in service */ }
                .collect { stats ->
                    val text = "CPU: ${stats.cpu.usagePercent.toInt()}% | RAM: ${stats.ram.usagePercent.toInt()}%"
                    updateNotification(text)
                }
        }
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("novGPU Monitor")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(channelId, "novGPU Monitor", NotificationManager.IMPORTANCE_LOW)
        channel.description = "System monitoring stats"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
