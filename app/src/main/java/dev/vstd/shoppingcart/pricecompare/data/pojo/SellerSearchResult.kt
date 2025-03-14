package dev.vstd.shoppingcart.pricecompare.data.pojo

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import dev.vstd.shoppingcart.pricecompare.data.model.SellerInfo

class SellerSearchResult(val data: Data) {
    class Data(private val stores: List<JsonObject>) {
        fun getStores(): List<Store> {
            val parsedStores = mutableListOf<Store>()

            for (storeItem in stores) {
                var productStore = ""
                var productPrice = ""
                var productStoreUrl = ""
                var productStoreLogoUrl = ""
                val tmp = storeItem.entrySet().toList()[0]
                if (tmp.value.isJsonArray) continue
                tmp.value.asJsonObject.entrySet().forEach { (key, value) ->
                    when (key) {
                        "product_store" -> productStore = value.asString
                        "product_price" -> productPrice = value.asString
                        "product_store_url" -> productStoreUrl = value.asString
                        "product_store_logo" -> productStoreLogoUrl = value.asString
                    }
                }
                if (productStore.isNotBlank()) {
                    val store = Store(productStore, productPrice, productStoreUrl, productStoreLogoUrl)
                    parsedStores.add(store)
                }
            }

            return parsedStores
        }

        class Store(
            @SerializedName("product_store")
            val storeName: String,
            @SerializedName("product_price")
            val price: String,
            @SerializedName("product_store_url")
            val url: String,
            @SerializedName("product_store_logo")
            val storeUrl: String,
        ) {
            fun toSellerInfo(): SellerInfo {
                return SellerInfo(
                    name = storeName,
                    price = price,
                    url = url,
                    sellerLogo = storeUrl
                )
            }
        }
    }
}