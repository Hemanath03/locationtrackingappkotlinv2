package com.example.locationtrackingappv2.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/** Linear interpolation between two LatLng points */
fun lerpLatLng(start: LatLng, end: LatLng, t: Float): LatLng {
    return LatLng(
        start.latitude + (end.latitude - start.latitude) * t,
        start.longitude + (end.longitude - start.longitude) * t
    )
}

/** Computes bearing angle between two GPS points */
fun computeBearing(start: LatLng, end: LatLng): Float {
    val lat1 = Math.toRadians(start.latitude)
    val lon1 = Math.toRadians(start.longitude)
    val lat2 = Math.toRadians(end.latitude)
    val lon2 = Math.toRadians(end.longitude)

    val dLon = lon2 - lon1
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

    var brng = Math.toDegrees(atan2(y, x))
    brng = (brng + 360) % 360
    return brng.toFloat()
}

fun vectorToBitmap(context: Context, @DrawableRes resId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, resId)!!
    val h = vectorDrawable.intrinsicHeight
    val w = vectorDrawable.intrinsicWidth
    vectorDrawable.setBounds(0, 0, w, h)

    val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bm)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bm)
}