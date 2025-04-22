package dev.vstd.shoppingcart.model

data class SearchFilter(
    var keyword: String = "",
    var categories: List<String> = emptyList(),
    var locations: List<String> = emptyList(),
    var minPrice: Double? = null,
    var maxPrice: Double? = null,
    var shopTypes: List<String> = emptyList(),
    var minRating: Float = 0f,
    var hasPromotion: Boolean = false,
    var sortBy: SortType = SortType.RELEVANCE
)

enum class SortType {
    RELEVANCE,
    NEWEST,
    BEST_SELLING,
    PRICE_ASC,
    PRICE_DESC
} 