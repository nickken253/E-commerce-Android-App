package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO cho phản hồi từ API khi xử lý thanh toán
 */
data class ProcessPaymentResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("payment_status")
    val paymentStatus: String
)
