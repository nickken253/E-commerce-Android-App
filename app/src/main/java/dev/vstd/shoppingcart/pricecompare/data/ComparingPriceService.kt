package dev.vstd.shoppingcart.pricecompare.data

import dev.keego.shoppingcart.BuildConfig
import dev.vstd.shoppingcart.common.Constants
import dev.vstd.shoppingcart.pricecompare.data.pojo.ProductSearchResult
import dev.vstd.shoppingcart.pricecompare.data.pojo.SellerSearchResult
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ComparingPriceService {

    @GET("search")
    suspend fun searchProduct(
        @Query("product", encoded = true) productName: String,
        @Query("api_key") apiKey: String = BuildConfig.APIKEY_PRICECOMPARE,
    ): Response<ProductSearchResult>

    @GET("detail")
    suspend fun getProductSeller(
        @Query("id", encoded = true) productId: String,
        @Query("api_key") apiKey: String = BuildConfig.APIKEY_PRICECOMPARE,
    ): Response<SellerSearchResult>

    companion object {
        fun build(okHttpClient: OkHttpClient): ComparingPriceService {
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.comparingServiceUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ComparingPriceService::class.java)
        }
    }
}