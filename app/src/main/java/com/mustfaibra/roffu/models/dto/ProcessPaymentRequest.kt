package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO để gửi yêu cầu xử lý thanh toán bằng thẻ
 */
data class ProcessPaymentRequest(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("idempotency_key")
    val idempotencyKey: String,
    
    @SerializedName("total_amount")
    val totalAmount: Int,
    
    @SerializedName("status")
    val status: String = "pending",
    
    @SerializedName("shipping_address_id")
    val shippingAddressId: Int,
    
    @SerializedName("items")
    val items: List<PaymentItem>,
    
    @SerializedName("cvv")
    val cvv: Int
)

data class PaymentItem(
    @SerializedName("product_id")
    val productId: Int,
    
    @SerializedName("quantity")
    val quantity: Int
)
