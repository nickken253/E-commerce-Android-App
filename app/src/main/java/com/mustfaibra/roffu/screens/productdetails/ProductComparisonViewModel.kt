package com.mustfaibra.roffu.screens.productdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.RetrofitClient
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProducts(productId1: Int, productId2: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("ProductComparisonVM", "Bắt đầu loadProducts với productId1: $productId1, productId2: $productId2")
                
                // Lấy thông tin sản phẩm 1
                val response1 = RetrofitClient.apiService.getProductDetails(productId1)
                if (!response1.isSuccessful) {
                    Log.e("ProductComparisonVM", "Lỗi khi lấy thông tin sản phẩm 1: ${response1.code()}")
                    return@launch
                }
                val productDto1 = response1.body()
                _product1.value = productDto1?.let {
                    Product(
                        id = it.id,
                        name = it.product_name,
                        image = 0,
                        price = it.price.toDouble(),
                        description = it.description,
                        imagePath = it.images?.firstOrNull()?.image_url,
                        manufacturerId = it.brand_id,
                        basicColorName = "Default",
                        barcode = it.barcode
                    )
                }

                // Lấy thông tin sản phẩm 2
                val response2 = RetrofitClient.apiService.getProductDetails(productId2)
                if (!response2.isSuccessful) {
                    Log.e("ProductComparisonVM", "Lỗi khi lấy thông tin sản phẩm 2: ${response2.code()}")
                    return@launch
                }
                val productDto2 = response2.body()
                _product2.value = productDto2?.let {
                    Product(
                        id = it.id,
                        name = it.product_name,
                        image = 0,
                        price = it.price.toDouble(),
                        description = it.description,
                        imagePath = it.images?.firstOrNull()?.image_url,
                        manufacturerId = it.brand_id,
                        basicColorName = "Default",
                        barcode = it.barcode
                    )
                }

                Log.d("ProductComparisonVM", "Đã tải xong thông tin cả 2 sản phẩm")
            } catch (e: Exception) {
                Log.e("ProductComparisonVM", "Lỗi khi loadProducts: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
} 