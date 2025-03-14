package dev.vstd.shoppingcart.auth.data

import dev.vstd.beshoppingcart.dto.SignupBodyDto
import dev.vstd.shoppingcart.auth.service.LoginBodyDto
import dev.vstd.shoppingcart.auth.service.LoginResponseDto
import dev.vstd.shoppingcart.auth.service.UserInfoRespDto
import dev.vstd.shoppingcart.auth.service.UserService
import dev.vstd.shoppingcart.shopping.data.service.CardService
import retrofit2.Response

class UserRepository(private val userService: UserService, private val cardService: CardService) {
    suspend fun login(email: String, password: String): Response<LoginResponseDto> {
        val body = LoginBodyDto(email, password)
        return userService.login(body)
    }

    suspend fun getUserInfo(userId: Long): UserInfoRespDto? {
        return userService.getUserInfo(userId)
    }

    suspend fun getAddress(userId: Long): String? {
        return userService.getUserInfo(userId)?.address
    }
    
    suspend fun updateAddress(userId: Long, address: String) {
        userService.updateAddress(userId, address)
    }

    suspend fun signUp(username: String, email: String, password: String): Response<LoginResponseDto> {
        val body = SignupBodyDto(username, email, password)
        return userService.signUp(body)
    }
}