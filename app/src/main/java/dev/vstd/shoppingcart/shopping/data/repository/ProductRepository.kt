package dev.vstd.shoppingcart.shopping.data.repository

import dev.vstd.shoppingcart.shopping.data.service.ProductDto
import dev.vstd.shoppingcart.shopping.data.service.ProductService
import retrofit2.Response

class ProductRepository(private val productService: ProductService) {
    suspend fun getProducts(): Response<List<ProductDto>> {
        return productService.getProducts()
    }
}