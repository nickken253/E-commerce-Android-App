package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Product(
    val id: Int,
    val barcode: String,
    val product_name: String,
    val description: String,
    val price: Long,
    val category_id: Int,
    val brand_id: Int,
    val created_at: String,
    val updated_at: String,
    val quantity: Int,
    val variants: List<EmptyVariant>,
    val images: List<Image>
)
@Serializable
object EmptyVariant