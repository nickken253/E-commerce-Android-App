package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddToCartResponse(
    val id: Int,
    val status: String,
    val user_id: Int,
    val created_at: String,
    val updated_at: String
)