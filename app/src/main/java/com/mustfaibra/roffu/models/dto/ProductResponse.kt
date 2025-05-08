package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val data: List<Product>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)
