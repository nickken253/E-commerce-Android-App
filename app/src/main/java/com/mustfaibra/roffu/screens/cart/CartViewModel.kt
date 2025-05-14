package com.mustfaibra.roffu.screens.cart

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.api.CartApiService
import com.mustfaibra.roffu.models.dto.AddToCartItem
import com.mustfaibra.roffu.models.dto.AddToCartRequest
import com.mustfaibra.roffu.models.dto.CartItemWithProductDetails
import com.mustfaibra.roffu.models.dto.CartResponse
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.models.dto.CartItem
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.utils.CartEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import retrofit2.Response
import kotlinx.serialization.Serializable
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val productRepository: ProductsRepository,
) : ViewModel() {
    val isSyncingCart = mutableStateOf(false)
    private val _cartOptionsMenuExpanded = mutableStateOf(false)
    val cartOptionsMenuExpanded: State<Boolean> = _cartOptionsMenuExpanded

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _cartData = mutableStateOf<CartResponse?>(null)
    val cartData: State<CartResponse?> = _cartData

    private val _cartItemsWithDetails = mutableStateOf<List<CartItemWithProductDetails>>(emptyList())
    val cartItemsWithDetails: State<List<CartItemWithProductDetails>> = _cartItemsWithDetails

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        Log.d("CartViewModel", "Init called - fetching cart")
    }

    fun updateQuantity(cartItemId: Int, quantity: Int, context: android.content.Context) {
        viewModelScope.launch {
            isSyncingCart.value = true
            _error.value = null

            try {
                val token = UserPref.getToken(context)
                if (token == null) {
                    _error.value = "Bạn chưa đăng nhập"
                    Log.e("CartViewModel", "Token is null, user not authenticated")
                    return@launch
                }

                val authToken = "Bearer $token"
                if (quantity <= 0) {
                    val response = RetrofitClient.cartApiService.deleteCartItem(cartItemId, authToken)
                    handleCartResponse(response, context, "Xóa sản phẩm thành công")
                } else {
                    val quantityData = mapOf("quantity" to quantity)
                    val response = RetrofitClient.cartApiService.updateCartItemQuantity(cartItemId, quantityData, authToken)
                    handleCartResponse(response, context, "Cập nhật số lượng thành công")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("CartViewModel", "Exception when updating cart: ${e.message}")
            } finally {
                isSyncingCart.value = false
            }
        }
    }

    fun removeCartItem(cartItemId: Int, context: android.content.Context) {
        viewModelScope.launch {
            isSyncingCart.value = true
            _error.value = null

            try {
                val token = UserPref.getToken(context)
                if (token == null) {
                    _error.value = "Bạn chưa đăng nhập"
                    Log.e("CartViewModel", "Token is null, user not authenticated")
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = RetrofitClient.cartApiService.deleteCartItem(cartItemId, authToken)
                handleCartResponse(response, context, "Xóa sản phẩm thành công")
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("CartViewModel", "Exception when removing cart item: ${e.message}")
            } finally {
                isSyncingCart.value = false
            }
        }
    }

    private suspend fun handleCartResponse(response: Response<CartResponse>, context: android.content.Context, successMessage: String) {
        if (response.isSuccessful) {
            fetchCart(context)
            Log.d("CartViewModel", successMessage)
        } else {
            _error.value = "Error ${response.code()}: ${response.message()}"
            Log.e("CartViewModel", "Error ${response.code()}: ${response.message()}")
        }
    }

    fun addToCart(
        productId: Int,
        quantity: Int,
        context: android.content.Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            isSyncingCart.value = true
            _error.value = null

            try {
                val token = UserPref.getToken(context)
                if (token == null) {
                    val errorMsg = "Bạn chưa đăng nhập"
                    _error.value = errorMsg
                    onError(errorMsg)
                    Log.e("CartViewModel", "Token is null, user not authenticated")
                    return@launch
                }

                val cartItem = AddToCartItem(productId = productId, quantity = quantity)
                val request = AddToCartRequest(items = listOf(cartItem))
                val authToken = "Bearer $token"
                val response = RetrofitClient.cartApiService.addToCart(request, authToken)

                if (response.isSuccessful) {
                    fetchCart(context)
                    Log.d("CartViewModel", "Thêm sản phẩm vào giỏ hàng thành công")
                    onSuccess()
                } else {
                    val errorMsg = "Error ${response.code()}: ${response.message()}"
                    _error.value = errorMsg
                    onError(errorMsg)
                    Log.e("CartViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                _error.value = errorMsg
                onError(errorMsg)
                Log.e("CartViewModel", "Exception when adding to cart: ${e.message}")
            } finally {
                isSyncingCart.value = false
            }
        }
    }

    fun clearCart(context: Context) {
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context)
                if (token == null) {
                    _error.value = "Bạn chưa đăng nhập"
                    return@launch
                }

                val authToken = "Bearer $token"
                // TODO: Implement clearCart API endpoint
                // val response = RetrofitClient.cartApiService.clearCart(authToken)
                // if (response.isSuccessful) {
                    _cartItemsWithDetails.value = emptyList()
                    Log.d("CartViewModel", "Cleared cart successfully")
                // } else {
                //     _error.value = "Error ${response.code()}: ${response.message()}"
                //     Log.e("CartViewModel", "Clear cart failed: ${response.message()}")
                // }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("CartViewModel", "Clear cart error: ${e.message}", e)
            }
        }
    }

    fun syncCartItems(
        navController: NavHostController,
        selectedItems: List<CartItemWithProductDetails>,
        onSyncSuccess: () -> Unit,
        onSyncFailed: (reason: Int) -> Unit,
    ) {
        isSyncingCart.value = true
        viewModelScope.launch {
            try {
                val totalAmount = selectedItems.sumOf { it.unitPrice.toDouble() * it.quantity }
                Log.d("CartViewModel", "Navigating to checkout with ${selectedItems.size} items, totalAmount: $totalAmount")
                navController.navigate(
                    Screen.CheckoutWithProducts.createRoute(
                        items = selectedItems,
                        totalAmount = totalAmount
                    )
                )
                onSyncSuccess()
            } catch (e: Exception) {
                Log.e("CartViewModel", "Sync failed: ${e.message}")
                onSyncFailed(-1)
            } finally {
                isSyncingCart.value = false
            }
        }
    }

    fun toggleOptionsMenuExpandState() {
        _cartOptionsMenuExpanded.value = !_cartOptionsMenuExpanded.value
    }

    fun fetchCart(context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _cartItemsWithDetails.value = emptyList()

            try {
                val token = UserPref.getToken(context)
                if (token == null) {
                    _error.value = "Bạn chưa đăng nhập"
                    Log.e("CartViewModel", "Token is null, user not authenticated")
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = RetrofitClient.cartApiService.getCart(authToken)

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _cartData.value = data
                        fetchProductDetails(data)
                        Log.d("CartViewModel", "Successfully fetched cart with ${data.items.size} items")
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

    private suspend fun fetchProductDetails(cartResponse: CartResponse) {
        try {
            val cartItemsWithDetailsDeferred = cartResponse.items.map { cartItem ->
                viewModelScope.async {
                    try {
                        val productDetails = RetrofitClient.productApiService.getProductDetails(cartItem.productId)
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
            val cartItemsWithDetails = cartItemsWithDetailsDeferred.awaitAll()
            _cartItemsWithDetails.value = cartItemsWithDetails
            Log.d("CartViewModel", "Successfully fetched details for ${cartItemsWithDetails.size} products")
        } catch (e: Exception) {
            Log.e("CartViewModel", "Error fetching product details: ${e.message}")
            _error.value = "Lỗi khi lấy thông tin sản phẩm: ${e.message}"
        }
    }
}