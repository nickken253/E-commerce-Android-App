package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class CartItemReponse(
    val id: Int,
    val product_id: Int,
    val quantity: Int,
    val unit_price: Double
)