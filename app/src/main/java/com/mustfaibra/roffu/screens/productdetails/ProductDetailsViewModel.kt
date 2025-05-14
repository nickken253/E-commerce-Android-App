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
import com.mustfaibra.roffu.models.dto.AddToCartItem
import com.mustfaibra.roffu.models.dto.AddToCartRequest
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.UserPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
) : ViewModel() {
    private val _detailsUiState = mutableStateOf<UiState>(UiState.Idle)
    val detailsUiState: State<UiState> = _detailsUiState

    private val _cartUiState = mutableStateOf<UiState>(UiState.Idle)
    val cartUiState: State<UiState> = _cartUiState

    private val _product = mutableStateOf<Product?>(null)
    val product: State<Product?> = _product

    private val _selectedSize = mutableStateOf(0)
    val selectedSize: State<Int> = _selectedSize

    private val _sizeScale = mutableStateOf(1f)
    val sizeScale: State<Float> = _sizeScale

    private val _selectedColor = mutableStateOf("")
    val selectedColor: State<String> = _selectedColor

    private val _quantity = mutableStateOf(1)
    val quantity: State<Int> = _quantity

    private val _isInCart = mutableStateOf(false)
    val isInCart: State<Boolean> = _isInCart

    fun getProductDetails(productId: Int) {
        _detailsUiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                Log.d("ProductDetailsVM", "Fetching product details for ID: $productId")

                val productDto = RetrofitClient.productApiService.getProductDetails(productId)
                Log.d("ProductDetailsVM", "API response: $productDto")

                val product = Product(
                    id = productDto.id,
                    name = productDto.product_name,
                    image = 0,
                    price = productDto.price.toDouble(),
                    description = productDto.description,
                    imagePath = productDto.images.firstOrNull()?.image_url,
                    manufacturerId = productDto.brand_id,
                    basicColorName = "Default",
                    barcode = productDto.barcode
                )

                Log.d("ProductDetailsVM", "Converted to UI model: $product")

                _detailsUiState.value = UiState.Success
                _product.value = product
                _selectedColor.value = product.basicColorName
                _selectedSize.value = product.sizes?.maxOf { size -> size.size } ?: 0

            } catch (e: HttpException) {
                Log.e("ProductDetailsVM", "HTTP error: ${e.code()} - ${e.message()}")
                _detailsUiState.value = UiState.Error(error = when(e.code()) {
                    401 -> Error.Unauthorized
                    403 -> Error.Forbidden
                    404 -> Error.Unknown
                    else -> Error.Network
                })
            } catch (e: IOException) {
                Log.e("ProductDetailsVM", "IO error: ${e.message}")
                _detailsUiState.value = UiState.Error(error = Error.Network)
            } catch (e: Exception) {
                Log.e("ProductDetailsVM", "Unexpected error: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                _detailsUiState.value = UiState.Error(error = Error.Unknown)
            }
        }
    }

    fun updateSelectedColor(color: String) {
        _selectedColor.value = color
    }

    fun updateSelectedSize(size: Int) {
        if(size == _selectedSize.value) return
        _sizeScale.value = if (size < _selectedSize.value) {
            _sizeScale.value.minus(0.1f)
        } else {
            _sizeScale.value.plus(0.1f)
        }
        _selectedSize.value = size
    }

    fun increaseQuantity() {
        _quantity.value = _quantity.value + 1
    }

    fun decreaseQuantity() {
        if (_quantity.value > 1) {
            _quantity.value = _quantity.value - 1
        }
    }

    fun addToCart(productId: Int, size: String, color: String, context: Context? = null) {
        Log.d("ProductDetailsVM", "Adding product $productId to cart with size: $size, color: $color, quantity: ${_quantity.value}")

        context?.let {
            addToCartWithApi(it, productId, _quantity.value)
        }
    }

    private fun addToCartWithApi(context: Context, productId: Int, quantity: Int) {
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context)

                if (token == null) {
                    Log.e("ProductDetailsVM", "Token is null, user not authenticated")
                    Toast.makeText(context, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val cartItem = AddToCartItem(productId = productId, quantity = quantity)
                val request = AddToCartRequest(items = listOf(cartItem))

                val authToken = "Bearer $token"
                val response = RetrofitClient.cartApiService.addToCart(request, authToken)

                if (response.isSuccessful) {
                    Log.d("ProductDetailsVM", "Thêm sản phẩm vào giỏ hàng thành công")
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
