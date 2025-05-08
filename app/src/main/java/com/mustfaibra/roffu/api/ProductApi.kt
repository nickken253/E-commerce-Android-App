package com.mustfaibra.roffu.api

import com.mustfaibra.roffu.models.SearchResponse
import com.mustfaibra.roffu.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApi {
    @GET(Constants.Api.SEARCH_ENDPOINT)
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