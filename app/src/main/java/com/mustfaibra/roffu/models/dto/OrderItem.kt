package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    @SerializedName("id") val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("product_name") var productName: String?,
    @SerializedName("product_image") var productImage: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)