package com.mustfaibra.roffu.models

import androidx.room.Embedded
import androidx.room.Relation

// Model mới để join Order -> OrderItem -> Product

data class OrderWithItemsAndProducts(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "orderId",
        entityColumn = "orderId",
        entity = OrderItem::class
    )
    val orderItems: List<OrderItemWithProduct>
)

// Join từng OrderItem với Product

data class OrderItemWithProduct(
    @Embedded val orderItem: OrderItem,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id",
        entity = Product::class
    )
    val product: Product?
)
