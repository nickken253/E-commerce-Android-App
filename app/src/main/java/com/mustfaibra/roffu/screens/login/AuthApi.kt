package com.mustfaibra.roffu.screens.login

import com.mustfaibra.roffu.models.dto.LoginRequest
import com.mustfaibra.roffu.models.dto.LoginResponse
import com.mustfaibra.roffu.models.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse


    @GET("api/v1/users/me")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserResponse


}