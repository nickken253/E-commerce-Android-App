package com.mustfaibra.roffu.screens.cart

import com.mustfaibra.roffu.models.dto.AddToCartRequest
import com.mustfaibra.roffu.models.dto.AddToCartResponse
import com.mustfaibra.roffu.models.dto.CartItem
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.Response
import retrofit2.http.GET

interface CartApiService {
    @POST("api/v1/carts/items")
    suspend fun addToCart(
        @Body request: AddToCartRequest,
        @Header("Authorization") token: String? = null // Nếu cần token
    ): Response<AddToCartResponse>
    @GET("api/v1/carts/items")
    suspend fun getCartItems(): Response<List<CartItem>>
}