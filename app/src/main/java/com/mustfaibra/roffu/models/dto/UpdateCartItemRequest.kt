package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCartItemRequest(
    val quantity: Int // Dùng cho API PATCH nếu có
)