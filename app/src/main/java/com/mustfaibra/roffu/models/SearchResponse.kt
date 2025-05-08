package com.mustfaibra.roffu.models

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("products")
    val products: List<ProductResponse>,
    @SerializedName("total")
    val total: Int
)

data class ProductResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("barcode")
    val barcode: String,
    @SerializedName("product_name")
    val product_name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("category_id")
    val category_id: Int,
    @SerializedName("brand_id")
    val brand_id: Int,
    @SerializedName("created_at")
    val created_at: String,
    @SerializedName("updated_at")
    val updated_at: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("variants")
    val variants: List<Any>,
    @SerializedName("images")
    val images: List<ProductImage>
)

data class ProductImage(
    @SerializedName("product_id")
    val product_id: Int,
    @SerializedName("is_primary")
    val is_primary: Boolean,
    @SerializedName("image_url")
    val image_url: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("upload_date")
    val upload_date: String
) 