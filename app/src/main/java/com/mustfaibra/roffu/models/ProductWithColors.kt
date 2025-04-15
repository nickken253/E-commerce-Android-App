package com.mustfaibra.roffu.models

import androidx.room.Embedded
import androidx.room.Relation

data class ProductWithColors(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId",
        entity = ProductColor::class
    )
    val colors: List<ProductColor>
)