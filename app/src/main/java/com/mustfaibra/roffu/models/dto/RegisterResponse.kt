package com.mustfaibra.roffu.models.dto

data class RegisterResponse(
    val username: String,
    val email: String,
    val full_name: String,
    val avatar_url: String,
    val phone_number: String,
    val id: Int,
    val is_active: Boolean,
    val created_at: String,
    val last_login: String?
)