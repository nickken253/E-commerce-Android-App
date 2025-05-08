package com.mustfaibra.roffu.screens.login

import com.mustfaibra.roffu.models.dto.LoginRequest
import com.mustfaibra.roffu.models.dto.LoginResponse
import com.mustfaibra.roffu.models.dto.RegisterRequest
import com.mustfaibra.roffu.models.dto.RegisterResponse
import com.mustfaibra.roffu.models.dto.ResetPasswordRequest
import com.mustfaibra.roffu.models.dto.ResetPasswordResponse
import com.mustfaibra.roffu.models.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
    @GET("api/v1/users/me")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserResponse
    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/v1/auth/forgot-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResetPasswordResponse>
}