package com.mustfaibra.roffu.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor() : ViewModel() {
    private val _scannedProduct = MutableStateFlow<Product?>(null)
    val scannedProduct: StateFlow<Product?> = _scannedProduct

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private var lastScannedBarcode: String? = null

    fun getProductByBarcode(barcode: String) {
        if (barcode == lastScannedBarcode) {
            return
        }
        
        lastScannedBarcode = barcode
        _uiState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                val product = RetrofitClient.productApiService.getProductByBarcode(barcode)
                _scannedProduct.value = product
                _uiState.value = UiState.Success
                Timber.d("Found product: ${product.product_name}")
            } catch (e: Exception) {
                Timber.e("Error scanning barcode: ${e.message}")
                _uiState.value = UiState.Error(error = Error.Network)
                _scannedProduct.value = null
            }
        }
    }

    fun resetScanner() {
        lastScannedBarcode = null
        _scannedProduct.value = null
        _uiState.value = UiState.Idle
    }
}