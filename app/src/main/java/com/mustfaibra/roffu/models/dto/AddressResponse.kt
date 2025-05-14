package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName

data class AddressResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("address_type") val addressType: String,
    @SerializedName("street") val street: String,
    @SerializedName("city") val city: String,
    @SerializedName("district") val district: String,
    @SerializedName("postal_code") val postalCode: String?,
    @SerializedName("country") val country: String,
    @SerializedName("is_default") val isDefault: Boolean,
    @SerializedName("user_id") val userId: Int
)

data class AddressRequest(
    @SerializedName("address_type") val addressType: String,
    @SerializedName("street") val street: String,
    @SerializedName("city") val city: String,
    @SerializedName("district") val district: String,
    @SerializedName("postal_code") val postalCode: String?,
    @SerializedName("country") val country: String,
    @SerializedName("is_default") val isDefault: Boolean
)

// Thay đổi kiểu dữ liệu trả về từ API để phù hợp với mảng JSON
typealias AddressListResponse = List<AddressResponse>
