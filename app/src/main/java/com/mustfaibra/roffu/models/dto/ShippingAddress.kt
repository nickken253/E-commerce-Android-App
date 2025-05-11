package com.mustfaibra.roffu.models.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ShippingAddress(
    @SerializedName("id") val id: Int,
    @SerializedName("address") val address: String
)