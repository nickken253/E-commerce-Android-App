package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Product(
    val id: Int,
    val barcode: String,
    @SerialName("product_name") val productName: String,
    val description: String,
    val price: Long,
    @SerialName("category_id") val categoryId: Int,
    @SerialName("brand_id") val brandId: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val quantity: Int,
    val variants: List<Variant>,
    val images: List<Image>
)