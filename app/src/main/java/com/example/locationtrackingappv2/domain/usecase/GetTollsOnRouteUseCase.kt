package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.repository.ITollRepository
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetTollsOnRouteUseCase @Inject constructor(
    private val tollRepo: ITollRepository
) {
    operator fun invoke(points: List<LatLng>) =
        tollRepo.getTollsIntersecting(points)
}
