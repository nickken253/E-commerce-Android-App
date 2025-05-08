package com.mustfaibra.roffu.models.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val full_name: String,
    val avatar_url: String,
    val phone_number: String,
    val password: String
)