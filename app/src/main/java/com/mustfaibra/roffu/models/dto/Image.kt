package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Image(
    val id: Int,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("product_id") val productId: Int,
    @SerialName("is_primary") val isPrimary: Boolean,
    @SerialName("upload_date") val uploadDate: String
)