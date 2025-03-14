package dev.vstd.shoppingcart.shopping.data.service

import com.google.gson.GsonBuilder
import dev.vstd.shoppingcart.common.Constants
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderService {
    @POST("order/create")
    suspend fun makeOrder(@Query("userId") userId: Long, @Body body: CreateOrderBodyDto): Response<OrderRespDto>

    @GET("order/all")
    suspend fun getOrders(@Query("userId") userId: Long): Response<List<OrderRespDto>>

    companion object {
        fun create(okHttpClient: OkHttpClient): OrderService {
            return Retrofit.Builder()
                .baseUrl(Constants.backendUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .create()
                ))
                .build()
                .create(OrderService::class.java)
        }
    }
}