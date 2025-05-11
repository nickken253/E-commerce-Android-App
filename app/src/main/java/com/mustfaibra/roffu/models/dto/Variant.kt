package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class Variant(
    val id: Int? = null,
    val name: String? = null,
    val value: String? = null
)