package com.mustfaibra.roffu.models

data class SearchFilters(
    val minPrice: Float = 0f,
    val maxPrice: Float = 10000000f,
    val sortBy: String? = null,
    val sortOrder: String? = null,
    val categoryId: Int? = null,
    val brandId: Int? = null
)

// Các tùy chọn sắp xếp
object SortOptions {
    const val NAME = "name"
    const val PRICE = "price"
    
    val options = listOf(
        "Tên sản phẩm" to NAME,
        "Giá" to PRICE
    )
}

// Các tùy chọn thứ tự sắp xếp
object SortOrderOptions {
    const val ASC = "asc"
    const val DESC = "desc"
    
    val options = listOf(
        "Tăng dần" to ASC,
        "Giảm dần" to DESC
    )
} 