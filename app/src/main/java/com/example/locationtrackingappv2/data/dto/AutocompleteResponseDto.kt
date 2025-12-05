package com.example.locationtrackingappv2.data.dto

import com.google.gson.annotations.SerializedName

data class AutocompleteResponseDto(
    @SerializedName("predictions")
    val predictions: List<PredictionDto> = emptyList(),

    @SerializedName("status")
    val status: String? = null
)

data class PredictionDto(
    @SerializedName("description")
    val description: String? = null,

    @SerializedName("place_id")
    val placeId: String? = null,

    // minimal structured formatting (optional)
    @SerializedName("structured_formatting")
    val structuredFormatting: StructuredFormattingDto? = null
)

data class StructuredFormattingDto(
    @SerializedName("main_text")
    val mainText: String? = null,

    @SerializedName("secondary_text")
    val secondaryText: String? = null
)
