package com.mustfaibra.roffu.repositories

import android.util.Log
import com.mustfaibra.roffu.api.RetrofitClient
import com.mustfaibra.roffu.models.Manufacturer
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.models.dto.Image
import com.mustfaibra.roffu.screens.home.ApiResponse
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BrandsRepository @Inject constructor(
    private val client: HttpClient,
    private val json: Json,
    private val apiService: com.mustfaibra.roffu.api.ApiService
) {
    suspend fun getBrandsWithProducts(): DataResponse<List<Manufacturer>> {
        // Giả định triển khai
        return DataResponse.Success(emptyList())
    }
    
    suspend fun getProductsByCategory(categoryId: Int, token: String): DataResponse<ApiResponse> {
        return try {
            Log.d("BrandsRepository", "Getting products by category_id=$categoryId")
            
            // Sử dụng client.get để gọi API products/category/{category_id}
            val response = client.get("http://103.90.226.131:8000/api/v1/products/category/$categoryId") {
                header("accept", "application/json")
                header("Authorization", "Bearer $token")
            }
            
            Log.d("BrandsRepository", "API response status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val responseBody = response.bodyAsText()
                    Log.d("BrandsRepository", "API response body: $responseBody")
                    
                    // API trả về cấu trúc JSON có trường "products" thay vì "data"
                    // Sử dụng data class tạm thời để deserialize
                    @Serializable
                    data class CategoryProductsResponse(val products: List<Product>)
                    
                    val categoryResponse = json.decodeFromString<CategoryProductsResponse>(responseBody)
                    Log.d("BrandsRepository", "Deserialized to CategoryProductsResponse with ${categoryResponse.products.size} products")
                    
                    if (categoryResponse.products.isNotEmpty()) {
                        val firstProduct = categoryResponse.products[0]
                        Log.d("BrandsRepository", "First product: ${firstProduct.product_name}")
                    } else {
                        Log.d("BrandsRepository", "No products returned for category $categoryId")
                    }
                    
                    // Chuyển đổi sang ApiResponse để tương thích với phần còn lại của ứng dụng
                    val apiResponse = ApiResponse(
                        data = categoryResponse.products,
                        page = 1,  // Giá trị mặc định vì API không trả về thông tin phân trang
                        limit = categoryResponse.products.size,
                        total = categoryResponse.products.size,
                        pages = 1
                    )
                    
                    DataResponse.Success(apiResponse)
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e("BrandsRepository", "Unauthorized access (401)")
                    DataResponse.Error(Error.Unknown)
                }
                HttpStatusCode.BadRequest -> {
                    Log.e("BrandsRepository", "Bad request (400)")
                    DataResponse.Error(Error.Unknown)
                }
                else -> {
                    Log.e("BrandsRepository", "Unexpected status code: ${response.status}")
                    DataResponse.Error(Error.Network)
                }
            }
        } catch (e: SerializationException) {
            Log.e("BrandsRepository", "Serialization error: ${e.message}")
            DataResponse.Error(Error.Unknown)
        } catch (e: Exception) {
            Log.e("BrandsRepository", "Error getting products by category: ${e.message}")
            Log.e("BrandsRepository", "Exception type: ${e.javaClass.name}")
            e.printStackTrace()
            DataResponse.Error(Error.Network)
        }
    }
    
suspend fun getAllProducts(
    page: Int,
    limit: Int,
    token: String,
    brandId: Int? = null,
    categoryId: Int? = null
): DataResponse<ApiResponse> {
    return try {
        val skip = (page - 1) * limit // Tính skip từ page và limit
        val response = client.get("http://103.90.226.131:8000/api/v1/products/") {
            parameter("skip", skip)
            parameter("limit", limit)
            brandId?.let { parameter("brand_id", it) }
            categoryId?.let { parameter("category_id", it) }
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