package com.mustfaibra.roffu.api

import com.mustfaibra.roffu.models.ProductResponse
import com.mustfaibra.roffu.models.SearchResponse
import com.mustfaibra.roffu.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/v1/products/{product_id}")
    suspend fun getProductDetails(
        @Path("product_id") productId: Int
    ): Response<ProductResponse>

    @GET("api/v1/products/search/")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("min_price") minPrice: Float? = null,
        @Query("max_price") maxPrice: Float? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): SearchResponse
} 