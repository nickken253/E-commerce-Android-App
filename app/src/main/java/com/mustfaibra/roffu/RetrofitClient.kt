package com.mustfaibra.roffu

import com.google.gson.GsonBuilder
import com.mustfaibra.roffu.api.ApiService
import com.mustfaibra.roffu.screens.cart.CartApiService
import com.mustfaibra.roffu.screens.order.OrderApiService
import com.mustfaibra.roffu.screens.product.ProductApiService
import com.mustfaibra.roffu.screens.profile.AddressApiService
import com.mustfaibra.roffu.screens.profile.PaymentApiService
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://103.90.226.131:8000/"
    private var authToken: String? = null
    
    fun init(token: String) {
        authToken = token
    }
    
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("accept", "application/json")
                
                authToken?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
    }
    
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
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
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}