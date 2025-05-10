package com.mustfaibra.roffu.repositories

import com.mustfaibra.roffu.models.Advertisement
import com.mustfaibra.roffu.models.Manufacturer
import com.mustfaibra.roffu.screens.home.ApiResponse
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BrandsRepository @Inject constructor(
    private val client: HttpClient,
    private val json: Json,
) {
    suspend fun getBrandsAdvertisements(): DataResponse<List<Advertisement>> {
        // Giả định triển khai, không liên quan đến lỗi hiện tại
        return DataResponse.Success(emptyList())
    }

    suspend fun getBrandsWithProducts(): DataResponse<List<Manufacturer>> {
        // Giả định triển khai
        return DataResponse.Success(emptyList())
    }

    suspend fun getAllProducts(page: Int, limit: Int): DataResponse<ApiResponse> {
        return try {
            val response = client.get("http://103.90.226.131:8000/api/v1/products/") {
                parameter("page", page)
                parameter("limit", limit)
                header("accept", "application/json")
            }
            if (response.status == HttpStatusCode.OK) {
                val apiResponse = json.decodeFromString<ApiResponse>(response.bodyAsText())
                DataResponse.Success(apiResponse)
            } else {
                DataResponse.Error(Error.Network)
            }
        } catch (e: Exception) {
            DataResponse.Error(Error.Network)
        }
    }
}