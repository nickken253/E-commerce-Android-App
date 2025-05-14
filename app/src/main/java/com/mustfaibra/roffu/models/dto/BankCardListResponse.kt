package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO cho phản hồi từ API khi lấy danh sách thẻ ngân hàng
 */
data class BankCardListResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("card_holder_name")
    val cardHolderName: String,
    
    @SerializedName("card_number")
    val cardNumber: String,
    
    @SerializedName("expiry_month")
    val expiryMonth: String,
    
    @SerializedName("expiry_year")
    val expiryYear: String,
    
    @SerializedName("is_default")
    val isDefault: Boolean
)
