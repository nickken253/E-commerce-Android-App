package dev.vstd.shoppingcart.pricecompare.data.model

data class ComparingProduct(
    val id: String,
    val title: String,
    val image: String,
    val lowestPrice: Int,
)