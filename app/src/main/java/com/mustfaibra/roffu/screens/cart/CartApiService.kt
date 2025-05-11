package com.mustfaibra.roffu.screens.cart

import com.mustfaibra.roffu.models.dto.AddToCartRequest
import com.mustfaibra.roffu.models.dto.AddToCartResponse
import com.mustfaibra.roffu.models.dto.CartItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CartApiService {
    @POST("api/v1/carts/items")
    suspend fun addToCart(
        @Body request: AddToCartRequest
    ): Response<AddToCartResponse>

    @GET("api/v1/carts/items")
    suspend fun getCartItems(): Response<List<CartItem>>
}