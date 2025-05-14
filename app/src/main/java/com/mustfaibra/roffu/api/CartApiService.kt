package com.mustfaibra.roffu.api

import com.mustfaibra.roffu.models.dto.AddToCartRequest
import com.mustfaibra.roffu.models.dto.CartResponse
import retrofit2.Response
import retrofit2.http.*

interface CartApiService {
    @GET("api/v1/carts")
    suspend fun getCart(
        @Header("Authorization") token: String
    ): Response<CartResponse>

    @POST("api/v1/carts")
    suspend fun addToCart(
        @Body request: AddToCartRequest,
        @Header("Authorization") token: String
    ): Response<CartResponse>

    @PUT("api/v1/carts/items/{cartItemId}")
    suspend fun updateCartItemQuantity(
        @Path("cartItemId") cartItemId: Int,
        @Body quantityData: Map<String, Int>,
        @Header("Authorization") token: String
    ): Response<CartResponse>

    @DELETE("api/v1/carts/items/{cartItemId}")
    suspend fun deleteCartItem(
        @Path("cartItemId") cartItemId: Int,
        @Header("Authorization") token: String
    ): Response<CartResponse>

    @DELETE("api/v1/carts")
    suspend fun clearCart(
        @Header("Authorization") token: String
    ): Response<CartResponse>
} 