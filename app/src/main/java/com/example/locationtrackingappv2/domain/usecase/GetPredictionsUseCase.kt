package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository

class GetPredictionsUseCase(private val repo: GoogleMapsRepository) {
    suspend operator fun invoke(q:String, sessionToken:String) = repo.getPlaceSuggestions(q,sessionToken)
}