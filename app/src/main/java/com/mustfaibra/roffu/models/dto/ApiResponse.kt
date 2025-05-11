package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val data: List<Product>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)