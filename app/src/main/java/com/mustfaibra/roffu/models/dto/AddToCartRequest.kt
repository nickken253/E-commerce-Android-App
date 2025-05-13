package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO để gửi yêu cầu thêm sản phẩm vào giỏ hàng
 */
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
