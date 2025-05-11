package com.mustfaibra.roffu.screens.cart

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.dto.CartItemWithProductDetails
import com.mustfaibra.roffu.models.dto.CartResponse
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.getDiscountedValue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val productRepository: ProductsRepository,
) : ViewModel() {

    val totalPrice = mutableStateOf(0.0)
    val isSyncingCart = mutableStateOf(false)
    private val _cartOptionsMenuExpanded = mutableStateOf(false)
    val cartOptionsMenuExpanded: State<Boolean> = _cartOptionsMenuExpanded
    
    // Các state riêng biệt thay thế cho UiState
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _cartData = mutableStateOf<CartResponse?>(null)
    val cartData: State<CartResponse?> = _cartData
    
    private val _cartItemsWithDetails = mutableStateOf<List<CartItemWithProductDetails>>(emptyList())
    val cartItemsWithDetails: State<List<CartItemWithProductDetails>> = _cartItemsWithDetails
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun updateCart(items: List<CartItem>){
        totalPrice.value = 0.0
        items.forEach { cartItem ->
            /** Now should update the totalPrice */
            totalPrice.value += cartItem.product?.price?.times(cartItem.quantity)
                ?.getDiscountedValue(cartItem.product?.discount ?: 0) ?: 0.0
        }
    }

    // XÓA hoặc SỬA HÀM removeCartItem để không dùng alreadyOnCart nữa
    fun removeCartItem(cartId: Int) {
        viewModelScope.launch {
            // Viết hàm xóa cart item theo cartId hoặc productId tuỳ logic bạn muốn
            // productRepository.deleteCartItem(cartId)
            productRepository.deleteCartItemById(cartId)
        }
    }

    fun updateQuantity(cartId: Int, quantity: Int) {
        viewModelScope.launch {
            productRepository.updateCartItemQuantity(cartId = cartId, quantity = quantity)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            productRepository.clearCart()
        }
    }

    fun syncCartItems(
        onSyncSuccess: () -> Unit,
        onSyncFailed: (reason: Int) -> Unit,
    ) {
        isSyncingCart.value = true
        viewModelScope.launch {
            delay(3000)
            isSyncingCart.value = false
            onSyncSuccess()
        }
    }

    fun toggleOptionsMenuExpandState() {
        _cartOptionsMenuExpanded.value = !_cartOptionsMenuExpanded.value
    }
    
    /**
     * Lấy danh sách sản phẩm trong giỏ hàng từ API
     * @param context Context để lấy token xác thực
     */
    fun fetchCart(context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _cartItemsWithDetails.value = emptyList()
            
            try {
                // Lấy token xác thực từ UserPref
                val token = UserPref.getToken(context)
                
                if (token == null) {
                    _error.value = "Bạn chưa đăng nhập"
                    Log.e("CartViewModel", "Token is null, user not authenticated")
                    return@launch
                }
                
                // Gọi API với token xác thực
                val authToken = "Bearer $token"
                val response = RetrofitClient.cartApiService.getCart(authToken)
                
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _cartData.value = data
                        // Cập nhật tổng giá trị giỏ hàng
                        totalPrice.value = data.items.sumOf { it.unitPrice * it.quantity }.toDouble() / 1000
                        Log.d("CartViewModel", "Successfully fetched cart with ${data.items.size} items")
                        
                        // Lấy thông tin chi tiết của từng sản phẩm trong giỏ hàng
                        fetchProductDetails(data)
                    } else {
                        _error.value = "Empty response body"
                        Log.e("CartViewModel", "Empty response body")
                    }
                } else {
                    _error.value = "Error ${response.code()}: ${response.message()}"
                    Log.e("CartViewModel", "Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("CartViewModel", "Exception when fetching cart: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Lấy thông tin chi tiết của từng sản phẩm trong giỏ hàng
     */
    private suspend fun fetchProductDetails(cartResponse: CartResponse) {
        try {
            // Sử dụng coroutines để gọi nhiều API song song
            val cartItemsWithDetailsDeferred = cartResponse.items.map { cartItem ->
                viewModelScope.async {
                    try {
                        // Gọi API để lấy thông tin chi tiết sản phẩm
                        val productDetails = RetrofitClient.productApiService.getProductDetails(cartItem.productId)
                        
                        // Tạo đối tượng CartItemWithProductDetails kết hợp thông tin giỏ hàng và sản phẩm
                        CartItemWithProductDetails(
                            id = cartItem.id,
                            productId = cartItem.productId,
                            quantity = cartItem.quantity,
                            unitPrice = cartItem.unitPrice,
                            productName = productDetails.product_name,
                            productImage = if (productDetails.images.isNotEmpty()) productDetails.images[0].image_url else "",
                            productDescription = productDetails.description,
                            productCategory = productDetails.category_id.toString(),
                            productBrand = productDetails.brand_id.toString()
                        )
                    } catch (e: Exception) {
                        // Nếu không lấy được thông tin sản phẩm, tạo đối tượng với thông tin mặc định
                        Log.e("CartViewModel", "Error fetching product details for product ${cartItem.productId}: ${e.message}")
                        CartItemWithProductDetails(
                            id = cartItem.id,
                            productId = cartItem.productId,
                            quantity = cartItem.quantity,
                            unitPrice = cartItem.unitPrice,
                            productName = "Sản phẩm #${cartItem.productId}",
                            productImage = "",
                            productDescription = "",
                            productCategory = "",
                            productBrand = ""
                        )
                    }
                }
            }
            
            // Đợi tất cả các request hoàn thành
            val cartItemsWithDetails = cartItemsWithDetailsDeferred.awaitAll()
            _cartItemsWithDetails.value = cartItemsWithDetails
            
            Log.d("CartViewModel", "Successfully fetched details for ${cartItemsWithDetails.size} products")
        } catch (e: Exception) {
            Log.e("CartViewModel", "Error fetching product details: ${e.message}")
            _error.value = "Lỗi khi lấy thông tin sản phẩm: ${e.message}"
        }
    }
}