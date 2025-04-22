package dev.vstd.shoppingcart.repository

import dev.vstd.shoppingcart.model.Product
import dev.vstd.shoppingcart.model.SearchFilter
import dev.vstd.shoppingcart.model.Shop
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun searchProducts(filter: SearchFilter, page: Int, pageSize: Int): Flow<List<Product>>
    suspend fun searchShops(filter: SearchFilter, page: Int, pageSize: Int): Flow<List<Shop>>
    suspend fun getPopularCategories(): Flow<List<String>>
    suspend fun getPopularSearchTags(): Flow<List<String>>
    suspend fun getSuggestedLocations(): Flow<List<String>>
    suspend fun getShopTypes(): Flow<List<String>>
} 