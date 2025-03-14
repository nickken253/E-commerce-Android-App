package dev.vstd.shoppingcart.shopping.data.service

import dev.vstd.shoppingcart.shopping.domain.Order
import dev.vstd.shoppingcart.shopping.domain.Product
import dev.vstd.shoppingcart.shopping.domain.ProductOfOrder
import dev.vstd.shoppingcart.shopping.domain.Status
import java.util.Date

data class OrderRespDto(
    val id: Long,
    val userId: Long,
    val date: Date,
    val shippingAddress: String,
    val status: Status,
    val purchaseMethodId: Long?,
    val purchaseMethod: PurchaseMethod,
    val products: List<ProductsOfOrderEntity>
) {
    enum class PurchaseMethod {
        COD,
        CARD,
        OTHER,
    }
    class ProductsOfOrderEntity(
        val id: Long = 0,
        val productEntity: ProductEntity,
        val quantity: Int,
        val snapshotPrice: Long,
    ) {
        data class ProductEntity(
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
                    seller = seller
                )
            }
        }
    }

    fun toOrder(): Order {
        return Order(
            id = id,
            status = status,
            sellerName = products.random().productEntity.seller,
            products = products.map {
                ProductOfOrder(
                    quantity = it.quantity,
                    product = it.productEntity.toProduct()
                )
            }
        )
    }
}