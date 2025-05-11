package com.mustfaibra.roffu.screens.order

import retrofit2.http.GET
import retrofit2.http.Query

interface OrderApiService {
    @GET("api/v1/orders/")
    suspend fun getOrders(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): List<com.mustfaibra.roffu.models.dto.OrderWithItemsAndProducts>
}