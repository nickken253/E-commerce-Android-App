package dev.vstd.shoppingcart.model

data class Product(
    val id: String,
    val name: String,
    val imageUrl: String,
    val price: Double,
    val originalPrice: Double,
    val discount: Int,
    val rating: Float,
    val soldCount: Int,
    val shop: Shop,
    val categories: List<String>,
    val isFavorite: Boolean = false
) 