package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationResponse(
    val suggestions: List<String>
) 