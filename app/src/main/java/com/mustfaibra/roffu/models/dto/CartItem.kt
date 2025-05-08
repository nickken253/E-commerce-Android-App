package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val id: Int? = null,
    val product_id: Int,
    val quantity: Int,
    val product: Product? = null // Có thể không cần nếu API không trả về product
)