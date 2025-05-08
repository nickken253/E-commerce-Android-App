package com.mustfaibra.roffu.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val new_password: String
)