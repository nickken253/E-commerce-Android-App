package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationRequest(
    val recent_searches: List<String>,
    val cart_item_ids: List<String>
) 