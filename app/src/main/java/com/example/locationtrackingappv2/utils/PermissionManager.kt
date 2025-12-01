package com.example.locationtrackingappv2.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

object PermissionManager {

    val REQUIRED_FOREGROUND = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Background is requested separately
    val BACKGROUND = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    fun hasForegroundPermissions(activity: Activity): Boolean {
        return REQUIRED_FOREGROUND.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasBackgroundPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity, BACKGROUND) == PackageManager.PERMISSION_GRANTED
    }

    fun requestForegroundPermissions(launcher: ActivityResultLauncher<Array<String>>) {
        launcher.launch(REQUIRED_FOREGROUND)
    }

    fun requestBackgroundPermission(launcher: ActivityResultLauncher<String>) {
        launcher.launch(BACKGROUND)
    }
}
