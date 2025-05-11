package com.mustfaibra.roffu.screens.cart

import com.mustfaibra.roffu.models.dto.CartResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface CartApiService {
    /**
     * Lấy thông tin giỏ hàng của người dùng
     * Yêu cầu xác thực với JWT token
     */
    @GET("api/v1/carts")
    suspend fun getCart(@Header("Authorization") token: String): Response<CartResponse>
}
