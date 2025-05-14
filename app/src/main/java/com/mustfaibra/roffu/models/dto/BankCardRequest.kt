package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO để gửi yêu cầu lưu thông tin thẻ ngân hàng
 */
data class BankCardRequest(
    @SerializedName("card_number")
    val cardNumber: String,
    
    @SerializedName("card_holder_name")
    val cardHolder: String,
    
    @SerializedName("expiry_month")
    val expiryMonth: String,
    
    @SerializedName("expiry_year")
    val expiryYear: String,
    
    @SerializedName("cvv")
    val cvv: String
)
