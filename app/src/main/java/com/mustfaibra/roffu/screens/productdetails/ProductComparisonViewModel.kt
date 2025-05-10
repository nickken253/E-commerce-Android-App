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
class ProductComparisonViewModel @Inject constructor(
    private val productsRepository: ProductsRepository
) : ViewModel() {
    private val _product1 = MutableStateFlow<Product?>(null)
    val product1: StateFlow<Product?> = _product1

    private val _product2 = MutableStateFlow<Product?>(null)
    val product2: StateFlow<Product?> = _product2

    fun loadProducts(productId1: Int, productId2: Int) {
        viewModelScope.launch {
//            _product1.value = productsRepository.getProductById(productId1)
//            _product2.value = productsRepository.getProductById(productId2)
        }
    }
} 