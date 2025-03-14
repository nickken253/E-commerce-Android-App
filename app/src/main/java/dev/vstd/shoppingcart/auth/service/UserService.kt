package dev.vstd.shoppingcart.auth.service

import dev.vstd.beshoppingcart.dto.SignupBodyDto
import dev.vstd.shoppingcart.common.Constants
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserService {
    @POST("auth/login")
    suspend fun login(@Body loginBodyDto: LoginBodyDto): Response<LoginResponseDto>

    @POST("auth/signup")
    suspend fun signUp(@Body body: SignupBodyDto): Response<LoginResponseDto>

    @GET("/user/detail")
    suspend fun getUserInfo(@Query("userId") userId: Long): UserInfoRespDto?

    @PUT("/user/address")
    suspend fun updateAddress(@Query("userId") userId: Long, @Query("address") address: String)

    companion object {
        fun create(okHttpClient: OkHttpClient): UserService {
            return retrofit2.Retrofit.Builder()
                .baseUrl(Constants.backendUrl)
                .client(okHttpClient)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
                .create(UserService::class.java)
        }
    }
}