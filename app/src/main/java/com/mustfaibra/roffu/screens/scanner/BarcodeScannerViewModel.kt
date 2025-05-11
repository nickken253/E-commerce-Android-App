package com.mustfaibra.roffu.screens.barcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.sealed.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val productRepository: ProductsRepository
) : ViewModel() {

    private val _scannedProduct = MutableStateFlow<Product?>(null)
    val scannedProduct: StateFlow<Product?> = _scannedProduct

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun getProductByBarcode(barcode: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                Timber.d("Fetching product for barcode: $barcode")
                val product = productRepository.getProductByBarcode(barcode)
                _scannedProduct.value = product
                if (product != null) {
                    Timber.d("Product found: ${product.product_name}")
                    Timber.d("Product images: ${product.images}")
                    product.images.forEachIndexed { index, image ->
                        Timber.d("Image $index: url=${image.image_url}, isPrimary=${image.is_primary}")
                    }
                    if (product.images.isEmpty()) {
                        Timber.w("No images found for product: ${product.product_name}")
                        _uiState.value ;
                    } else {
                        _uiState.value = UiState.Success
                    }
                } else {
                    Timber.w("No product found for barcode: $barcode")
                    _uiState.value;
                }
            } catch (e: Exception) {
                Timber.e("Error fetching product: ${e.message}")
                _uiState.value;
            }
        }
    }
}