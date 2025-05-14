package com.mustfaibra.roffu.screens.product

import com.mustfaibra.roffu.models.dto.Product
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApiService {
    @GET("api/v1/products/{productId}")
    suspend fun getProductDetails(@Path("productId") productId: Int): Product
}
