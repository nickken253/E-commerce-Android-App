package com.mustfaibra.roffu.screens.productdetails

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.api.ApiService
import com.mustfaibra.roffu.models.ProductResponse
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val productsRepository: ProductsRepository
) : ViewModel() {
    private val _uiState = mutableStateOf<UiState>(UiState.Loading)
    val uiState: State<UiState> = _uiState

    private val _product = mutableStateOf<ProductResponse?>(null)
    val product: State<ProductResponse?> = _product

    fun getProductDetails(productId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val response = apiService.getProductDetails(productId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _product.value = it
                        _uiState.value = UiState.Success
                    } ?: run {
                        _uiState.value = UiState.Error(error = Error.Network)
                    }
                } else {
                    _uiState.value = UiState.Error(error = Error.Network)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(error = Error.Network)
            }
        }
    }

    fun addToCart(productId: Int) {
        viewModelScope.launch {
            // Tạm thời truyền giá trị mặc định cho size và color vì chưa có trong API
            productsRepository.updateCartState(productId, "Default", "Default")
        }
    }
}