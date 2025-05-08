package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable
@Serializable
data class CartResponse(
    val id: Int,
    val user_id: Int,
    val status: String,
    val items: List<CartItem>
)
