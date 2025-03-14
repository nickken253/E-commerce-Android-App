package dev.vstd.shoppingcart.shopping.domain

data class Order(
    val id: Long,
    val sellerName: String,
    val status: Status,
    val products: List<ProductOfOrder>
)
