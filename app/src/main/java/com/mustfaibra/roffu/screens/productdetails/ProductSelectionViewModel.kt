package com.mustfaibra.roffu.screens.productdetails

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.models.dto.Product as ProductDto
import com.mustfaibra.roffu.models.dto.EmptyVariant
import com.mustfaibra.roffu.models.dto.Image
import com.mustfaibra.roffu.utils.UserPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductSelectionViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProducts(currentProductId: Int) {
        Log.d("ProductSelectionVM", "Bắt đầu loadProducts với productId: $currentProductId")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val token = UserPref.getToken(context)
                if (token == null) {
                    Log.e("ProductSelectionVM", "Token is null")
                    return@launch
                }
                val authToken = "Bearer $token"
                
                // Lấy thông tin sản phẩm hiện tại để lấy category_id
                Log.d("ProductSelectionVM", "Đang gọi API lấy thông tin sản phẩm hiện tại...")
                val currentProductResponse = RetrofitClient.apiService.getProductDetails(currentProductId)
                if (!currentProductResponse.isSuccessful) {
                    Log.e("ProductSelectionVM", "Lỗi khi lấy thông tin sản phẩm: ${currentProductResponse.code()}")
                    return@launch
                }
                
                val currentProduct = currentProductResponse.body()
                val categoryId = currentProduct?.category_id
                Log.d("ProductSelectionVM", "Lấy được category_id: $categoryId")
                
                if (categoryId == null) {
                    Log.e("ProductSelectionVM", "Không tìm thấy category_id")
                    return@launch
                }

                // Lấy danh sách sản phẩm cùng danh mục
                Log.d("ProductSelectionVM", "Đang gọi API lấy danh sách sản phẩm cùng danh mục...")
                val response = RetrofitClient.apiService.getProductsByCategory(categoryId, authToken)
                Log.d("ProductSelectionVM", "Response từ API: $response")
                Log.d("ProductSelectionVM", "Số lượng sản phẩm nhận được: ${response.products?.size ?: 0}")
                
                // Chuyển đổi dữ liệu từ API sang model Product và lọc bỏ sản phẩm hiện tại
                val productsList = response.products
                    ?.filter { it.id != currentProductId && it.category_id == categoryId }
                    ?.map { productDto ->
                        Log.d("ProductSelectionVM", "Đang xử lý sản phẩm: ${productDto.product_name}")
                        Product(
                            id = productDto.id,
                            name = productDto.product_name,
                            image = 0,
                            price = productDto.price.toDouble(),
                            description = productDto.description,
                            imagePath = productDto.images?.firstOrNull()?.image_url,
                            manufacturerId = productDto.brand_id,
                            basicColorName = "Default",
                            barcode = productDto.barcode
                        )
                    } ?: emptyList()
                
                Log.d("ProductSelectionVM", "Số lượng sản phẩm sau khi lọc: ${productsList.size}")
                _products.value = productsList
            } catch (e: Exception) {
                Log.e("ProductSelectionVM", "Lỗi khi loadProducts: ${e.message}")
                e.printStackTrace()
                _products.value = emptyList()
            } finally {
                _isLoading.value = false
                Log.d("ProductSelectionVM", "Kết thúc loadProducts")
            }
        }
    }
} 