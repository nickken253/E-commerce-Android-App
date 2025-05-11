package com.mustfaibra.roffu.api

import com.mustfaibra.roffu.models.Brand
import com.mustfaibra.roffu.models.Category
import com.mustfaibra.roffu.models.ProductResponse
import com.mustfaibra.roffu.models.SearchResponse
import com.mustfaibra.roffu.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    companion object {
        const val ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDY5Nzc5MzksInN1YiI6IjIifQ.uFBKTDTwlenDuYF3EmMFetzoWXEwUCOhtaMp1nIlodo"
    }

    @GET("api/v1/products/{product_id}")
    suspend fun getProductDetails(
        @Path("product_id") productId: Int,
        @Header("Authorization") authorization: String = "Bearer $ACCESS_TOKEN"
    ): Response<ProductResponse>

    @GET("api/v1/products/")
    suspend fun searchProducts(
        @Query("search") search: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 10,
        @Query("category_id") categoryId: Int? = null,
        @Query("brand_id") brandId: Int? = null,
        @Query("min_price") minPrice: Float? = null,
        @Query("max_price") maxPrice: Float? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Header("Authorization") authorization: String = "Bearer $ACCESS_TOKEN"
    ): SearchResponse

    @GET("api/v1/products/brands")
    suspend fun getBrands(
        @Header("Authorization") authorization: String = "Bearer $ACCESS_TOKEN"
    ): List<Brand>

    @GET("api/v1/products/categories")
    suspend fun getCategories(
        @Header("Authorization") authorization: String = "Bearer $ACCESS_TOKEN"
    ): List<Category>
} 