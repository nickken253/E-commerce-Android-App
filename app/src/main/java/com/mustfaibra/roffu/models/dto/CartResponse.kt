package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName

/**
 * Model cho response từ API lấy giỏ hàng
 */
data class CartResponse(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("user_id")
    val userId: Int = 0,
    
    @SerializedName("status")
    val status: String = "",
    
    @SerializedName("items")
    val items: List<CartItemResponse> = emptyList()
) {
    data class CartItemResponse(
        @SerializedName("id")
        val id: Int = 0,
        
        @SerializedName("product_id")
        val productId: Int = 0,
        
        @SerializedName("quantity")
        val quantity: Int = 0,
        
        @SerializedName("unit_price")
        val unitPrice: Long = 0
    )
}
