package com.mustfaibra.roffu.screens.order

import com.mustfaibra.roffu.models.dto.OrderItem
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {
    @GET("api/v1/orders/")
    suspend fun getOrders(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): List<com.mustfaibra.roffu.models.dto.OrderWithItemsAndProducts>
    
    @GET("api/v1/orders/{order_id}/items")
    suspend fun getOrderItems(
        @Path("order_id") orderId: Int
    ): List<OrderItem>
}