package com.mustfaibra.roffu.models
import com.google.gson.annotations.SerializedName

data class CategoryProductsResponse(
    @SerializedName("products")
    val products: List<ProductResponse>?,
    @SerializedName("category")
    val category: Category,
    @SerializedName("total")
    val total: Int
) 