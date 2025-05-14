package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable
import com.google.gson.annotations.SerializedName

/**
 * DTO để gửi yêu cầu thêm sản phẩm vào giỏ hàng
 */
@Serializable
data class AddToCartRequest(
    @SerializedName("items")
    val items: List<AddToCartItem>
)

data class AddToCartItem(
    @SerializedName("product_id")
    val productId: Int,

    @SerializedName("quantity")
    val quantity: Int
)
