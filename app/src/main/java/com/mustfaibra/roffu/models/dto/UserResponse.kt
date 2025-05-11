package com.mustfaibra.roffu.models.dto

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val full_name: String,
    val phone_number: String,
    val address: String?
)