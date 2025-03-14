package dev.vstd.shoppingcart.shopping.data.service

import com.google.gson.GsonBuilder
import dev.vstd.shoppingcart.common.Constants
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CardService {
    @GET("user/card")
    suspend fun getCard(@Query("userId") userId: Long): Response<CardRespDto>

    @POST("user/card")
    suspend fun registerCard(@Query("userId") userId: Long): Response<CardRespDto>

    @POST("order/pay")
    suspend fun payByCard(
        @Query("userId") userId: Long,
        @Query("orderId") orderId: Long,
        @Query("cvv") cvv: String
    ): Response<String>

    companion object {
        fun create(okHttpClient: OkHttpClient): CardService {
            return retrofit2.Retrofit.Builder()
                .baseUrl(Constants.backendUrl)
                .client(okHttpClient)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .build()
                .create(CardService::class.java)
        }
    }
}