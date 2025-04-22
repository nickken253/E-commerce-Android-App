package dev.vstd.shoppingcart.model

data class Shop(
    val id: String,
    val name: String,
    val imageUrl: String,
    val location: String,
    val followers: Int,
    val rating: Float,
    val createdAt: String,
    val isFavorite: Boolean = false
) 