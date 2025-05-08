package com.mustfaibra.roffu.models

data class SearchFilters(
    var minPrice: Float = 0f,
    var maxPrice: Float = 100000000f, // 100 triệu
    var sortBy: String = "name",
    var sortOrder: String = "asc"
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