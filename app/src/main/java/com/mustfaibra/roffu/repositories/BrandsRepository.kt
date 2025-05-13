package com.mustfaibra.roffu.repositories

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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BrandsRepository @Inject constructor(
    private val client: HttpClient,
    private val json: Json,
) {
    suspend fun getBrandsWithProducts(): DataResponse<List<Manufacturer>> {
        // Giả định triển khai
        return DataResponse.Success(emptyList())
    }

    suspend fun getAllProducts(page: Int, limit: Int, token: String): DataResponse<ApiResponse> {
        return try {
            val skip = (page - 1) * limit // Tính skip từ page và limit
            val response = client.get("http://103.90.226.131:8000/api/v1/products/") {
                parameter("skip", skip)
                parameter("limit", limit)
                header("accept", "application/json")
                header("Authorization", "Bearer $token") // Thêm header Authorization
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = json.decodeFromString<ApiResponse>(response.bodyAsText())
                    DataResponse.Success(apiResponse)
                }
                HttpStatusCode.Unauthorized -> DataResponse.Error(Error.Unknown)
                HttpStatusCode.BadRequest -> DataResponse.Error(Error.Unknown)
                else -> DataResponse.Error(Error.Network)
            }
        } catch (e: SerializationException) {
            DataResponse.Error(Error.Unknown)
        } catch (e: Exception) {
            DataResponse.Error(Error.Network)
        }
    }
}