package com.mustfaibra.roffu

import com.google.gson.GsonBuilder
import com.mustfaibra.roffu.screens.order.OrderApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://103.90.226.131:8000/"

    val orderApiService: OrderApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(OrderApiService::class.java)
    }
}