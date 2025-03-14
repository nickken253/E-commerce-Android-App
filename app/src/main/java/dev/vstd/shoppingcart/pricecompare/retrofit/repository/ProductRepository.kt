package dev.vstd.shoppingcart.pricecompare.retrofit.repository

import android.util.Log
import dev.vstd.shoppingcart.pricecompare.retrofit.model.SerpProduct
import dev.vstd.shoppingcart.pricecompare.retrofit.model.SerpResult
import dev.vstd.shoppingcart.pricecompare.retrofit.service.ProductService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductRepository (private val productService: ProductService) {
    suspend fun search(q: String) : Response<SerpResult> {
        val resp = productService.search(q)
        return if (resp.isSuccessful) {
            Response.success(resp.body()!!, resp.raw())
        } else {
            Response.error(resp.code(), resp.errorBody()!!)
        }
    }


    suspend fun getSerpProduct(url: String) : Response<SerpProduct> {
        val resp = productService.getSerpProduct(url + "")
        return if (resp.isSuccessful) {
            Response.success(resp.body()!!, resp.raw())
        } else {
            Response.error(resp.code(), resp.errorBody()!!)
        }
    }

    suspend fun searchWithFilter(q: String, filter: String) : Response<SerpResult> {
        val resp = productService.searchWithFilter(q, filter)
        return if (resp.isSuccessful) {
            Response.success(resp.body()!!, resp.raw())
        } else {
            Response.error(resp.code(), resp.errorBody()!!)
        }
    }
}