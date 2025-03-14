package dev.vstd.shoppingcart.auth.service

data class UserInfoRespDto(
    val username: String,
    val email: String,
    val address: String?,
)