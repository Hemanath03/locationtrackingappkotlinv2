package com.example.locationtrackingappv2.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.locationtrackingappv2.services.LocationService

object ServiceController {

    fun startService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }
}
