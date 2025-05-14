package com.mustfaibra.roffu

import com.google.gson.GsonBuilder
import com.mustfaibra.roffu.screens.cart.CartApiService
import com.mustfaibra.roffu.screens.order.OrderApiService
import com.mustfaibra.roffu.screens.product.ProductApiService
import com.mustfaibra.roffu.screens.profile.AddressApiService
import com.mustfaibra.roffu.screens.profile.PaymentApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://103.90.226.131:8000/"
    
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    val orderApiService: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }
    
    val productApiService: ProductApiService by lazy {
        retrofit.create(ProductApiService::class.java)
    }
    
    val cartApiService: CartApiService by lazy {
        retrofit.create(CartApiService::class.java)
    }
    
    val paymentApiService: PaymentApiService by lazy {
        retrofit.create(PaymentApiService::class.java)
    }
    
    val addressApiService: AddressApiService by lazy {
        retrofit.create(AddressApiService::class.java)
    }
}