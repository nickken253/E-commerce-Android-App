package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddToCartRequest(
    val items: List<Item>
) {
    @Serializable
    data class Item(
        val product_id: Int,
        val quantity: Int
    )
}