package com.example.locationtrackingappv2.domain.models

import com.google.android.gms.maps.model.LatLng

data class TollMatch(
    val segmentId: Int,
    val segmentPoints: List<LatLng>
)