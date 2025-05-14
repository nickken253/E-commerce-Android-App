package com.mustfaibra.roffu.api

import android.content.Context
import com.mustfaibra.roffu.models.Brand
import com.mustfaibra.roffu.models.Category
import com.mustfaibra.roffu.models.ProductResponse
import com.mustfaibra.roffu.models.SearchResponse
import com.mustfaibra.roffu.models.dto.LoginRequest
import com.mustfaibra.roffu.models.dto.LoginResponse
import com.mustfaibra.roffu.models.dto.RegisterRequest
import com.mustfaibra.roffu.models.dto.RegisterResponse
import com.mustfaibra.roffu.models.dto.ResetPasswordRequest
import com.mustfaibra.roffu.models.dto.ResetPasswordResponse
import com.mustfaibra.roffu.models.dto.UserResponse
import com.mustfaibra.roffu.utils.Constants
import com.mustfaibra.roffu.utils.UserPref
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    companion object {
        const val ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDY5Nzc5MzksInN1YiI6IjIifQ.uFBKTDTwlenDuYF3EmMFetzoWXEwUCOhtaMp1nIlodo"

        fun init(context: Context) {
            val token = UserPref.getToken(context)
            if (token != null) {
                RetrofitClient.init(token)
            }
        }
    }

    // Auth endpoints
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("api/v1/users/me")
    suspend fun getUserProfile(): UserResponse

    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/v1/auth/forgot-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResetPasswordResponse>

    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/v1/auth/google")
    suspend fun loginWithGoogle(
        @Body body: Map<String, String>
    ): LoginResponse

    // Product endpoints
    @GET("api/v1/products/{product_id}")
    suspend fun getProductDetails(
        @Path("product_id") productId: Int,
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
    ): SearchResponse
    
    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @GET("api/v1/products/category/{category_id}")
    suspend fun getProductsByCategory(
        @Path("category_id") categoryId: Int,
        //@Header("Authorization") token: String = "Bearer ${ACCESS_TOKEN}"
    ): SearchResponse

    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @GET("api/v1/products/brands")
    suspend fun getBrands(@Header("Authorization") token: String = "Bearer ${ACCESS_TOKEN}"): List<Brand>

    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @GET("api/v1/products/categories")
    suspend fun getCategories(@Header("Authorization") token: String = "Bearer ${ACCESS_TOKEN}"): List<Category>
} 