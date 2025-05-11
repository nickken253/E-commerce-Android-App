package com.mustfaibra.roffu.models.dto

/**
 * Model kết hợp thông tin của CartItemResponse và Product
 */
data class CartItemWithProductDetails(
    val id: Int,                // ID của item trong giỏ hàng
    val productId: Int,         // ID của sản phẩm
    val quantity: Int,          // Số lượng
    val unitPrice: Long,        // Đơn giá
    
    // Thông tin chi tiết sản phẩm
    val productName: String,    // Tên sản phẩm
    val productImage: String,   // URL hình ảnh sản phẩm
    val productDescription: String, // Mô tả sản phẩm
    val productCategory: String,    // Danh mục sản phẩm
    val productBrand: String        // Thương hiệu sản phẩm
)
