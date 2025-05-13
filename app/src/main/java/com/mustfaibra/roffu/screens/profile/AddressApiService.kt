package com.mustfaibra.roffu.screens.profile

import com.mustfaibra.roffu.models.dto.AddressListResponse
import com.mustfaibra.roffu.models.dto.AddressRequest
import com.mustfaibra.roffu.models.dto.AddressResponse
import retrofit2.Response
import retrofit2.http.*

interface AddressApiService {
    @GET("/api/v1/users/me/addresses")
    suspend fun getUserAddresses(
        @Header("Authorization") token: String
    ): Response<AddressListResponse>

    @POST("/api/v1/users/me/addresses")
    suspend fun createAddress(
        @Header("Authorization") token: String,
        @Body request: AddressRequest
    ): Response<AddressResponse>

    @PUT("/api/v1/users/me/addresses/{address_id}")
    suspend fun updateAddress(
        @Path("address_id") addressId: Int,
        @Header("Authorization") token: String,
        @Body request: AddressRequest
    ): Response<AddressResponse>
}
