package dev.vstd.shoppingcart.pricecompare.retrofit.model

import com.google.gson.annotations.SerializedName

data class SerpProduct (
    @SerializedName("search_metadata")
    val searchMetadata: SearchMetadata,

    @SerializedName("search_parameters")
    val searchParameters: SearchParameters,

    @SerializedName("product_results")
    val productResults: ProductResults,

    @SerializedName("sellers_results")
    val sellersResults: SellersResults,

    @SerializedName("related_products")
    val relatedProducts: RelatedProducts,

    @SerializedName("reviews_results")
    val reviewsResults: ReviewsResults,

    @SerializedName("product_variations")
    val productVariations: List<ProductVariation>
)

data class ProductResults (
    @SerializedName("product_id")
    val productID: String,

    val title: String,
    val prices: List<String>,
    val conditions: List<String>,

    @SerializedName("typical_prices")
    val typicalPrices: TypicalPrices,

    val reviews: Long,
    val rating: Double,
    val extensions: List<String>,
    val description: String,
    val media: List<Media>,
    val sizes: Map<String, Size>
) {
    fun getFirstImage() : Media? {
        for (m : Media in media) {
            if (m.type == "image") {
                return m
            }
        }
        return null
    }

    fun getAllImage() : List<Media> {
        val list = mutableListOf<Media>()
        for (m : Media in media) {
            if (m.type == "image") {
                list.add(m)
            }
        }
        return list
    }
}

data class Media (
    val type: String,
    val link: String
)

data class Size (
    val link: String,

    @SerializedName("product_id")
    val productID: String,

    val selected: Boolean? = null,

    @SerializedName("serpapi_link")
    val serpapiLink: String
)

data class TypicalPrices (
    val low: String,
    val high: String,

    @SerializedName("shown_price")
    val shownPrice: String
)

data class ProductVariation (
    val thumbnail: String,
    val link: String? = null,

    @SerializedName("serpapi_link")
    val serpapiLink: String? = null
)

data class RelatedProducts (
    @SerializedName("different_brand")
    val differentBrand: List<DifferentBrand>
)

data class DifferentBrand (
    val title: String,
    val link: String,
    val thumbnail: String,
    val price: String,
    val rating: Double,
    val reviews: Long
)

data class ReviewsResults (
    val ratings: List<Rating>,
    val reviews: List<Review>
)

data class Rating (
    val stars: Long,
    val amount: Long
)

data class Review (
    val position: Long,
    val date: String,
    val rating: Long,
    val source: String,
    val content: String
)

data class SellersResults (
    @SerializedName("online_sellers")
    val onlineSellers: List<OnlineSeller>
)

data class OnlineSeller (
    val position: Long,
    val name: String,
    val link: String,

    @SerializedName("details_and_offers")
    val detailsAndOffers: List<DetailsAndOffer>,

    @SerializedName("original_price")
    val originalPrice: String?,

    @SerializedName("base_price")
    val basePrice: String,

    @SerializedName("additional_price")
    val additionalPrice: AdditionalPrice,

    @SerializedName("total_price")
    val totalPrice: String,

    @SerializedName("offer_id")
    val offerID: String? = null,

    @SerializedName("offer_link")
    val offerLink: String? = null,

    @SerializedName("serpapi_offer_link")
    val serpapiOfferLink: String? = null
)

data class AdditionalPrice (
    val shipping: String
)

data class DetailsAndOffer (
    val text: String
)

