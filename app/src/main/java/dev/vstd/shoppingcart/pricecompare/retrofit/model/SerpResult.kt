package dev.vstd.shoppingcart.pricecompare.retrofit.model


import com.google.gson.annotations.SerializedName

data class SerpResult(
    @SerializedName("search_metadata")
    val searchMetadata: SearchMetadata,
    @SerializedName("search_parameters")
    val searchParameters: SearchParameters,
    @SerializedName("search_information")
    val searchInformation: SearchInformation,
    val filters: List<Filter>,
    @SerializedName("shopping_results")
    val shoppingResults: List<ShoppingResult>,
    val categories: List<Category>,
    val pagination: Pagination,
    @SerializedName("serpapi_pagination")
    val serpapiPagination: SerpapiPagination,
)

data class SearchMetadata(
    val id: String,
    val status: String,
    @SerializedName("json_endpoint")
    val jsonEndpoint: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("processed_at")
    val processedAt: String,
    @SerializedName("google_shopping_url")
    val googleShoppingUrl: String,
    @SerializedName("raw_html_file")
    val rawHtmlFile: String,
    @SerializedName("total_time_taken")
    val totalTimeTaken: Double,
)

data class SearchParameters(
    val engine: String,
    val q: String,
    @SerializedName("location_requested")
    val locationRequested: String,
    @SerializedName("location_used")
    val locationUsed: String,
    @SerializedName("google_domain")
    val googleDomain: String,
    val hl: String,
    val gl: String,
    val device: String,
)

data class SearchInformation(
    @SerializedName("query_displayed")
    val queryDisplayed: String,
    @SerializedName("shopping_results_state")
    val shoppingResultsState: String,
)

data class Filter(
    val type: String,
    var options: List<FilterOption>,
)

data class FilterOption(
    val text: String,
    val tbs: String,
)

data class ShoppingResult(
    val position: Long,
    val title: String,
    val link: String,
    val thumbnail: String,
    val rating: Double?,
    val reviews: Long?,
    val source: String,
    val price: String,
    @SerializedName("extracted_price")
    val extractedPrice: Long,
    @SerializedName("number_of_comparisons")
    val numberOfComparisons: Long?,
    @SerializedName("product_link")
    val productLink: String,
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("serpapi_product_api")
    val serpapiProductApi: String,
    val delivery: String,
    @SerializedName("second_hand_condition")
    val secondHandCondition: String?,
    @SerializedName("store_rating")
    val storeRating: Double?,
    @SerializedName("store_reviews")
    val storeReviews: Long?,
    val badge: String?,
    val extensions: List<String>?,
    @SerializedName("old_price")
    val oldPrice: String?,
    @SerializedName("extracted_old_price")
    val extractedOldPrice: Long?,
    val tag: String?,
)

data class Category(
    val title: String,
    val filters: List<Filter2>,
)

data class Filter2(
    val title: String,
    val link: String,
    val thumbnail: String,
    @SerializedName("serpapi_link")
    val serpapiLink: String,
)

data class Pagination(
    val current: Long,
    val next: String,
    @SerializedName("other_pages")
    val otherPages: OtherPages,
)

data class OtherPages(
    @SerializedName("2")
    val n2: String,
    @SerializedName("3")
    val n3: String,
    @SerializedName("4")
    val n4: String,
    @SerializedName("5")
    val n5: String,
)

data class SerpapiPagination(
    val current: Long,
    @SerializedName("next_link")
    val nextLink: String,
    val next: String,
    @SerializedName("other_pages")
    val otherPages: OtherPages2,
)

data class OtherPages2(
    @SerializedName("2")
    val n2: String,
    @SerializedName("3")
    val n3: String,
    @SerializedName("4")
    val n4: String,
    @SerializedName("5")
    val n5: String,
)

