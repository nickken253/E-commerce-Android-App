package dev.vstd.shoppingcart.repository

import dev.vstd.shoppingcart.model.Product
import dev.vstd.shoppingcart.model.SearchFilter
import dev.vstd.shoppingcart.model.Shop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepositoryImpl : SearchRepository {
    override suspend fun searchProducts(
        filter: SearchFilter,
        page: Int,
        pageSize: Int
    ): Flow<List<Product>> = flow {
        // TODO: Implement actual API call
        emit(emptyList())
    }

    override suspend fun searchShops(
        filter: SearchFilter,
        page: Int,
        pageSize: Int
    ): Flow<List<Shop>> = flow {
        // TODO: Implement actual API call
        emit(emptyList())
    }

    override suspend fun getPopularCategories(): Flow<List<String>> = flow {
        emit(listOf("Thời trang", "Điện tử", "Đồ gia dụng", "Sách", "Mỹ phẩm"))
    }

    override suspend fun getPopularSearchTags(): Flow<List<String>> = flow {
        emit(listOf("iPhone", "Laptop", "Áo thun", "Giày thể thao", "Sách tiếng Anh"))
    }

    override suspend fun getSuggestedLocations(): Flow<List<String>> = flow {
        emit(listOf("Hà Nội", "TP.HCM", "Đà Nẵng", "Cần Thơ"))
    }

    override suspend fun getShopTypes(): Flow<List<String>> = flow {
        emit(listOf("Shop yêu thích", "Shop chính hãng", "Shop Mall"))
    }
} 