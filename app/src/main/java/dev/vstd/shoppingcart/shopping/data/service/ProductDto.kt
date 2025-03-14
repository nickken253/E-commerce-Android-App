package dev.vstd.shoppingcart.shopping.data.service

import dev.vstd.shoppingcart.shopping.domain.Product

data class ProductDto(
    val id: Long = 0,
    val name: String,
    val price: Long,
    val previewImage: String,
    val description: String,
    val seller: String,
) {
    fun toProduct(): Product {
        return Product(
            id = id,
            title = name,
            price = price,
            image = previewImage,
            description = description,
            seller = seller,
        )
    }
}