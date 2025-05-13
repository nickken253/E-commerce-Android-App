package com.mustfaibra.roffu.screens.productdetails

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.models.ProductSize
import com.mustfaibra.roffu.models.dto.AddToCartItem
import com.mustfaibra.roffu.models.dto.AddToCartRequest
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.UserPref
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
     * Thêm sản phẩm vào giỏ hàng thông qua API
     * @param productId ID của sản phẩm cần thêm
     * @param size Kích thước sản phẩm đã chọn
     * @param color Màu sắc sản phẩm đã chọn
     * @param context Context để lấy token xác thực (sẽ được truyền từ UI)
     */
    fun addToCart(productId: Int, size: String, color: String, context: Context? = null) {
        Log.d("ProductDetailsVM", "Adding product $productId to cart with size: $size, color: $color, quantity: ${_quantity.value}")
        
        // Nếu context được cung cấp, gọi API để thêm vào giỏ hàng
        context?.let {
            addToCartWithApi(it, productId, _quantity.value)
        }
    }
    
    /**
     * Gọi API để thêm sản phẩm vào giỏ hàng
     * @param context Context để lấy token xác thực
     * @param productId ID của sản phẩm cần thêm
     * @param quantity Số lượng sản phẩm cần thêm
     */
    private fun addToCartWithApi(context: Context, productId: Int, quantity: Int) {
        viewModelScope.launch {
            try {
                // Lấy token xác thực từ UserPref
                val token = UserPref.getToken(context)
                
                if (token == null) {
                    Log.e("ProductDetailsVM", "Token is null, user not authenticated")
                    Toast.makeText(context, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Tạo request body
                val cartItem = AddToCartItem(productId = productId, quantity = quantity)
                val request = AddToCartRequest(items = listOf(cartItem))
                
                // Gọi API với token xác thực
                val authToken = "Bearer $token"
                val response = RetrofitClient.cartApiService.addToCart(request, authToken)
                
                if (response.isSuccessful) {
                    Log.d("ProductDetailsVM", "Thêm sản phẩm vào giỏ hàng thành công")
                    // Dialog thành công sẽ được hiển thị bởi UI
                } else {
                    Log.e("ProductDetailsVM", "Error ${response.code()}: ${response.message()}")
                    Toast.makeText(context, "Lỗi khi thêm vào giỏ hàng: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ProductDetailsVM", "Exception when adding to cart: ${e.message}")
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
