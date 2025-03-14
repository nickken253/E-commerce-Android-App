package dev.vstd.shoppingcart.shopping.domain

data class Product(
    val id: Long,
    val title: String,
    val price: Long,
    val image: String,
    val description: String,
    val seller: String
)