package com.example.locationtrackingappv2.domain.util

import android.app.Notification

interface INotificationHelper {
    fun createNotificationChannelIfNeeded()
    fun buildNotification(text: String): Notification
    fun updateNotification(id: Int, notification: Notification)
}
