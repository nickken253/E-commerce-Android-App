package dev.vstd.shoppingcart.shopping.data.service

import dev.vstd.shoppingcart.common.Constants
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ProductService {
    @GET("product/all")
    suspend fun getProducts(): Response<List<ProductDto>>

    companion object {
        fun create(okHttpClient: OkHttpClient): ProductService {
            return retrofit2.Retrofit.Builder()
                .baseUrl(Constants.backendUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ProductService::class.java)
        }
    }
}