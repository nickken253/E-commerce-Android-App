package dev.vstd.shoppingcart.pricecompare.retrofit.service

import dev.vstd.shoppingcart.pricecompare.retrofit.model.SerpProduct
import dev.vstd.shoppingcart.pricecompare.retrofit.model.SerpResult
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

const val BASE_URL = "https://serpapi.com/"
const val API_KEY = "1d76dd932f774b1867acc87772b9a95fe293aa787a4894e93b02d30fc0c385a6"

interface ProductService {
    @GET("search.json?engine=google&location=Vietnam&google_domain=google.com.vn&gl=vn&hl=vi&tbm=shop&device=desktop&api_key=$API_KEY")
    suspend fun search(
        @Query("q") q: String,
    ): Response<SerpResult>

    @GET("search.json?engine=google_shopping&location=Vietnam&google_domain=google.com.vn&gl=vn&hl=vi&device=desktop")
    suspend fun searchWithFilter(
        @Query("q") q: String,
        @Query("tbs") filter: String,
        @Query("api_key") key: String = API_KEY
    ) : Response<SerpResult>

    @GET
    suspend fun getSerpProduct(
        @Url url: String,
        @Query("api_key") key: String = API_KEY
    ) : Response<SerpProduct>


    companion object {

        fun build(okHttpClient: OkHttpClient): ProductService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ProductService::class.java)
        }
    }
}