package com.mustfaibra.roffu.screens.productdetails

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.models.ProductSize
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class ProductDetailsViewModel @Inject constructor() : ViewModel() {
    private val _detailsUiState = mutableStateOf<UiState>(UiState.Loading)
    val detailsUiState: State<UiState> = _detailsUiState

    private val _product = mutableStateOf<Product?>(null)
    val product: State<Product?> = _product

    private val _selectedSize = mutableStateOf(0)
    val selectedSize: State<Int> = _selectedSize

    private val _sizeScale = mutableStateOf(1f)
    val sizeScale: State<Float> = _sizeScale

    private val _selectedColor = mutableStateOf("")
    val selectedColor: State<String> = _selectedColor
    
    // Trạng thái số lượng sản phẩm
    private val _quantity = mutableStateOf(1)
    val quantity: State<Int> = _quantity

    /**
     * Gọi API để lấy thông tin chi tiết sản phẩm theo ID
     */
    fun getProductDetails(productId: Int) {
        _detailsUiState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("ProductDetailsVM", "Fetching product details for ID: $productId")
                
                // Gọi API để lấy chi tiết sản phẩm
                val productDto = RetrofitClient.productApiService.getProductDetails(productId)
                Log.d("ProductDetailsVM", "API response: $productDto")
                
                // Chuyển đổi từ DTO sang model UI
                val product = Product(
                    id = productDto.id,
                    name = productDto.product_name,
                    image = 0, // Không dùng resource ID nữa
                    price = productDto.price.toDouble(),
                    description = productDto.description,
                    imagePath = productDto.images.firstOrNull()?.image_url,
                    discount = 0, // Có thể cập nhật nếu API trả về
                    manufacturerId = productDto.brand_id,
                    basicColorName = "Default", // Mặc định
                    barcode = productDto.barcode
                )
                
                Log.d("ProductDetailsVM", "Converted to UI model: $product")
                
                // Cập nhật UI
                _detailsUiState.value = UiState.Success
                _product.value = product
                _selectedColor.value = product.basicColorName
                _selectedSize.value = product.sizes?.maxOf { size -> size.size } ?: 0
                
            } catch (e: HttpException) {
                // Xử lý lỗi HTTP
                Log.e("ProductDetailsVM", "HTTP error: ${e.code()} - ${e.message()}")
                _detailsUiState.value = UiState.Error(error = when(e.code()) {
                    401 -> Error.Unauthorized
                    403 -> Error.Forbidden
                    404 -> Error.Unknown
                    else -> Error.Network
                })
            } catch (e: IOException) {
                // Xử lý lỗi kết nối
                Log.e("ProductDetailsVM", "IO error: ${e.message}")
                _detailsUiState.value = UiState.Error(error = Error.Network)
            } catch (e: Exception) {
                // Xử lý các lỗi khác
                Log.e("ProductDetailsVM", "Unexpected error: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                _detailsUiState.value = UiState.Error(error = Error.Unknown)
            }
        }
    }

    /**
     * Update the current product's color.
     * @param color The new color name
     */
    fun updateSelectedColor(color: String) {
        _selectedColor.value = color
    }

    /**
     * Update the current product's size.
     * @param size the new selected size.
     */
    fun updateSelectedSize(size: Int) {
        /** Check when user click again on the same size ! */
        if(size == _selectedSize.value) return
        /** Update the product's image scale depending on the new size */
        _sizeScale.value = if (size < _selectedSize.value) {
            _sizeScale.value.minus(0.1f)
        } else {
            _sizeScale.value.plus(0.1f)
        }
        _selectedSize.value = size
    }

    /**
     * Tăng số lượng sản phẩm
     */
    fun increaseQuantity() {
        _quantity.value = _quantity.value + 1
    }
    
    /**
     * Giảm số lượng sản phẩm (tối thiểu là 1)
     */
    fun decreaseQuantity() {
        if (_quantity.value > 1) {
            _quantity.value = _quantity.value - 1
        }
    }
    
    /**
     * Add the current product to cart.
     * @param productId the product id
     * @param size the selected size
     * @param color the selected color
     * @param quantity the quantity to add
     */
    fun addToCart(productId: Int, size: String, color: String) {
        // Không sử dụng Room Database nữa, chỉ log thông tin
        Log.d("ProductDetailsVM", "Simulating adding product $productId to cart with size: $size, color: $color, quantity: ${_quantity.value}")
        // UI sẽ hiển thị dialog thành công mà không cần thực sự lưu dữ liệu
    }
}
