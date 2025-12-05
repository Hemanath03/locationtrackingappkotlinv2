package com.example.locationtrackingappv2.domain.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.locationtrackingappv2.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelperImpl @Inject constructor(
    private val context: Context
) : INotificationHelper {

    companion object {
        const val CHANNEL_ID = "location_track_channel"
        const val NOTIF_ID = 44
    }

    override fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Navigation Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            manager?.createNotificationChannel(channel)
        }
    }

    override fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Trip Active")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    override fun updateNotification(id: Int, notification: Notification) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(id, notification)
    }
}
