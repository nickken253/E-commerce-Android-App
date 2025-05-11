package com.mustfaibra.roffu.screens.login

import com.mustfaibra.roffu.models.dto.LoginRequest
import com.mustfaibra.roffu.models.dto.LoginResponse
<<<<<<< HEAD
import com.mustfaibra.roffu.models.dto.RegisterRequest
import com.mustfaibra.roffu.models.dto.RegisterResponse
import com.mustfaibra.roffu.models.dto.ResetPasswordRequest
import com.mustfaibra.roffu.models.dto.ResetPasswordResponse
=======
>>>>>>> hieuluu2
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

<<<<<<< HEAD
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
=======
>>>>>>> hieuluu2

    @GET("api/v1/users/me")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserResponse

<<<<<<< HEAD
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
=======

>>>>>>> hieuluu2
}