package com.mustfaibra.roffu.models.dto

/**
 * Model đơn giản để truyền thông tin cần thiết từ CartScreen sang CheckoutScreen
 */
data class CheckoutItem(
    val id: Int,                // ID của item trong giỏ hàng
    val productId: Int,         // ID của sản phẩm
    val quantity: Int,          // Số lượng
    val unitPrice: Long         // Đơn giá
)
