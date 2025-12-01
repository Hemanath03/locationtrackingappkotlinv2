package com.example.locationtrackingappv2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.locationtrackingappv2.utils.Constants.ACTION_LOCATION_UPDATE

object BroadcastHelper {

    fun registerReceiver(context: Context, receiver: BroadcastReceiver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(ACTION_LOCATION_UPDATE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(ACTION_LOCATION_UPDATE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    fun unregisterReceiver(context: Context, receiver: BroadcastReceiver) {
        context.unregisterReceiver(receiver)
    }
}
