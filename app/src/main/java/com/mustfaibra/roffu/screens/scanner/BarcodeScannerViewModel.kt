package com.mustfaibra.roffu.screens.barcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.repositories.ProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val productRepository: ProductsRepository
) : ViewModel() {

    val scannedProduct = MutableStateFlow<Product?>(null)

    val bookmarkedProductsIds = productRepository.getBookmarksProductsIdsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getProductByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByBarcode(barcode)
            scannedProduct.value = product
        }
    }

    fun isProductBookmarked(productId: Int): Boolean {
        return bookmarkedProductsIds.value.contains(productId)
    }
}
