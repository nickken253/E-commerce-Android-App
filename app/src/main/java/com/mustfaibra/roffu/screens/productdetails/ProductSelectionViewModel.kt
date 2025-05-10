package com.mustfaibra.roffu.screens.productdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.repositories.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductSelectionViewModel @Inject constructor(
    private val productsRepository: ProductsRepository
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    fun loadProducts(currentProductId: Int) {
        viewModelScope.launch {
            // Lấy danh sách tất cả sản phẩm từ repository
//            val allProducts = productsRepository.getAllProducts()
//            _products.value = allProducts
        }
    }
} 