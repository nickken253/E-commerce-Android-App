package com.mustfaibra.roffu.screens.product

import com.mustfaibra.roffu.models.dto.Product
import retrofit2.Response
import retrofit2.http.*

interface ProductApiService {
    @GET("api/v1/products/{id}")
    suspend fun getProductDetails(@Path("id") id: Int): Product

    @GET("api/v1/products/barcode/{barcode}")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Product
}
