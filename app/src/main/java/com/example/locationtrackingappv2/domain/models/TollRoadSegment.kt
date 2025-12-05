package com.example.locationtrackingappv2.domain.models

import com.google.android.gms.maps.model.LatLng

data class TollRoadSegment(
    val id: Int,
    val points: List<LatLng>
)

