package com.mustfaibra.roffu.screens.cart

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.dto.AddToCartRequest
import com.mustfaibra.roffu.models.dto.CartItem
import com.mustfaibra.roffu.models.dto.CartResponse
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.CartEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class UpdateCartItemRequest(val quantity: Int)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val client: HttpClient,
    private val context: Context,
    private val cartEventBus: CartEventBus
) : ViewModel() {
    private val _cartItems = mutableStateOf<List<CartItem>>(emptyList())
    val cartItems: State<List<CartItem>> = _cartItems

    private val _cartUiState = mutableStateOf<UiState>(UiState.Idle)
    val cartUiState: State<UiState> = _cartUiState

    private val _totalPrice = mutableStateOf(0.0)
    val totalPrice: State<Double> = _totalPrice

    private val _isSyncingCart = mutableStateOf(false)
    val isSyncingCart: State<Boolean> = _isSyncingCart

    private val _cartOptionsMenuExpanded = mutableStateOf(false)
    val cartOptionsMenuExpanded: State<Boolean> = _cartOptionsMenuExpanded

    init {
        fetchCartItems()
        viewModelScope.launch {
            cartEventBus.cartEvents.collect {
                Log.d("CartViewModel", "Received cart change event, refreshing cart")
                fetchCartItems()
            }
        }
    }

<<<<<<< HEAD
    fun fetchCartItems() {
        _cartUiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context)
                if (token.isNullOrBlank()) {
                    _cartUiState.value = UiState.Error(error = Error.Unknown)
                    Log.e("CartViewModel", "No token found, user not authenticated")
                    return@launch
                }

                Log.d("CartViewModel", "Fetching cart items with token: $token")
                val response = client.get("http://103.90.226.131:8000/api/v1/carts/") {
                    header("accept", "application/json")
                    header("Authorization", "Bearer $token")
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val cartResponse: CartResponse = response.body()
                        val items = cartResponse.items
                        val updatedItems = items.map { item ->
                            if (item.product == null) {
                                try {
                                    val productResponse = client.get("http://103.90.226.131:8000/api/v1/products/${item.product_id}") {
                                        header("accept", "application/json")
                                    }
                                    if (productResponse.status == HttpStatusCode.OK) {
                                        item.copy(product = productResponse.body<Product>())
                                    } else {
                                        item
                                    }
                                } catch (e: Exception) {
                                    Log.e("CartViewModel", "Failed to fetch product ${item.product_id}: ${e.message}")
                                    item
                                }
                            } else {
                                item
                            }
                        }
                        _cartItems.value = updatedItems
                        updateTotalPrice(updatedItems)
                        _cartUiState.value = UiState.Success
                        Log.d("CartViewModel", "Fetched cart items: $updatedItems")
                    }
                    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("CartViewModel", "Fetch cart failed: Unauthorized, body: ${response.bodyAsText()}")
                    }
                    HttpStatusCode.InternalServerError -> {
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("CartViewModel", "Fetch cart failed: ${response.status}, body: ${response.bodyAsText()}")
                    }
                    else -> {
                        _cartUiState.value = UiState.Error(error = Error.Network)
                        Log.e("CartViewModel", "Fetch cart failed: ${response.status}, body: ${response.bodyAsText()}")
                    }
                }
            } catch (e: Exception) {
                _cartUiState.value = UiState.Error(error = Error.Network)
                Log.e("CartViewModel", "Fetch cart error: ${e.message}", e)
            }
        }
    }

    private fun updateTotalPrice(items: List<CartItem>) {
        _totalPrice.value = items.sumOf { cartItem ->
            val price = cartItem.product?.price?.toDouble() ?: 0.0
            price * cartItem.quantity
=======
    // XÓA hoặc SỬA HÀM removeCartItem để không dùng alreadyOnCart nữa
    fun removeCartItem(cartId: Int) {
        viewModelScope.launch {
            // Viết hàm xóa cart item theo cartId hoặc productId tuỳ logic bạn muốn
            // productRepository.deleteCartItem(cartId)
            productRepository.deleteCartItemById(cartId)
>>>>>>> hieuluu2
        }
    }

    fun updateQuantity(cartId: Int, quantity: Int) {
        viewModelScope.launch {
<<<<<<< HEAD
            try {
                val token = UserPref.getToken(context)
                if (token.isNullOrBlank()) {
                    _cartUiState.value = UiState.Error(error = Error.Unknown)
                    Log.e("CartViewModel", "No token found, user not authenticated")
                    return@launch
                }

                val cartItem = _cartItems.value.find { it.product_id == productId }
                if (cartItem == null) {
                    _cartUiState.value = UiState.Error(error = Error.Unknown)
                    Log.e("CartViewModel", "Cart item not found for productId=$productId")
                    return@launch
                }

                val itemId = cartItem.id
                Log.d("CartViewModel", "Attempting to update quantity for itemId=$itemId (productId=$productId) to $quantity")
                val response = client.put("http://103.90.226.131:8000/api/v1/carts/items/$itemId") {
                    header("accept", "application/json")
                    header("Content-Type", "application/json")
                    header("Authorization", "Bearer $token")
                    setBody(UpdateCartItemRequest(quantity = quantity))
                }
                Log.d("CartViewModel", "PUT response status: ${response.status}, body: ${response.bodyAsText()}")
                when (response.status) {
                    HttpStatusCode.OK -> {
                        _cartItems.value = _cartItems.value.map { item ->
                            if (item.id == itemId) item.copy(quantity = quantity) else item
                        }
                        updateTotalPrice(_cartItems.value)
                        cartEventBus.notifyCartChanged()
                        Log.d("CartViewModel", "Updated quantity for itemId=$itemId (productId=$productId) to $quantity")
                    }
                    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("CartViewModel", "Update quantity failed: Unauthorized, body: ${response.bodyAsText()}")
                    }
                    HttpStatusCode.NotFound -> {
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("CartViewModel", "Update quantity failed: Cart item not found, body: ${response.bodyAsText()}")
                    }
                    else -> {
                        _cartUiState.value = UiState.Error(error = Error.Network)
                        Log.e("CartViewModel", "Update quantity failed: ${response.status}, body: ${response.bodyAsText()}")
                    }
                }
            } catch (e: Exception) {
                _cartUiState.value = UiState.Error(error = Error.Network)
                Log.e("CartViewModel", "Update quantity error: ${e.message}", e)
                fetchCartItems() // Đồng bộ lại giỏ hàng khi có lỗi
            }
        }
    }

    fun removeCartItem(productId: Int) {
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context)
                if (token.isNullOrBlank()) {
                    _cartUiState.value = UiState.Error(error = Error.Unknown)
                    Log.e("CartViewModel", "No token found, user not authenticated")
                    return@launch
                }

                val cartItem = _cartItems.value.find { it.product_id == productId }
                if (cartItem == null) {
                    _cartUiState.value = UiState.Error(error = Error.Unknown)
                    Log.e("CartViewModel", "Cart item not found for productId=$productId")
                    return@launch
                }

                val itemId = cartItem.id
                Log.d("CartViewModel", "Attempting to delete itemId=$itemId (productId=$productId)")
                val response = client.delete("http://103.90.226.131:8000/api/v1/carts/items/$itemId") {
                    header("accept", "application/json")
                    header("Authorization", "Bearer $token")
                }
                Log.d("CartViewModel", "DELETE response status: ${response.status}, body: ${response.bodyAsText()}")
                when (response.status) {
                    HttpStatusCode.OK -> {
                        _cartItems.value = _cartItems.value.filter { it.product_id != productId }
                        updateTotalPrice(_cartItems.value)
                        cartEventBus.notifyCartChanged()
                        Log.d("CartViewModel", "Removed itemId=$itemId (productId=$productId) from cart")
                    }
                    HttpStatusCode.NotFound -> {
                        _cartItems.value = _cartItems.value.filter { it.product_id != productId }
                        updateTotalPrice(_cartItems.value)
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("CartViewModel", "Remove cart item failed: Cart item not found, body: ${response.bodyAsText()}")
                    }
                    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("CartViewModel", "Remove cart item failed: Unauthorized, body: ${response.bodyAsText()}")
                    }
                    else -> {
                        _cartUiState.value = UiState.Error(error = Error.Network)
                        Log.e("CartViewModel", "Remove cart item failed: ${response.status}, body: ${response.bodyAsText()}")
                    }
                }
            } catch (e: Exception) {
                _cartUiState.value = UiState.Error(error = Error.Network)
                Log.e("CartViewModel", "Remove cart item error: ${e.message}", e)
                fetchCartItems()
            }
=======
            productRepository.updateCartItemQuantity(cartId = cartId, quantity = quantity)
>>>>>>> hieuluu2
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context)
                if (token.isNullOrBlank()) {
                    _cartUiState.value = UiState.Error(error = Error.Unknown)
                    Log.e("CartViewModel", "No token found, user not authenticated")
                    return@launch
                }

                val response = client.delete("http://103.90.226.131:8000/api/v1/carts/") {
                    header("accept", "application/json")
                    header("Authorization", "Bearer $token")
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        _cartItems.value = emptyList()
                        _totalPrice.value = 0.0
                        cartEventBus.notifyCartChanged()
                        Log.d("CartViewModel", "Cleared cart successfully")
                    }
                    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("CartViewModel", "Clear cart failed: Unauthorized, body: ${response.bodyAsText()}")
                    }
                    else -> {
                        _cartUiState.value = UiState.Error(error = Error.Network)
                        Log.e("CartViewModel", "Clear cart failed: ${response.status}, body: ${response.bodyAsText()}")
                    }
                }
            } catch (e: Exception) {
                _cartUiState.value = UiState.Error(error = Error.Network)
                Log.e("CartViewModel", "Clear cart error: ${e.message}", e)
                fetchCartItems()
            }
        }
    }

    fun syncCartItems(
        onSyncSuccess: () -> Unit,
        onSyncFailed: (reason: Int) -> Unit,
    ) {
        _isSyncingCart.value = true
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context)
                if (token.isNullOrBlank()) {
                    _isSyncingCart.value = false
                    onSyncFailed(401)
                    Log.e("CartViewModel", "No token found, user not authenticated")
                    return@launch
                }

                val response = client.get("http://103.90.226.131:8000/api/v1/carts/") {
                    header("accept", "application/json")
                    header("Authorization", "Bearer $token")
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val cartResponse: CartResponse = response.body()
                        val items = cartResponse.items
                        val updatedItems = items.map { item ->
                            if (item.product == null) {
                                try {
                                    val productResponse = client.get("http://103.90.226.131:8000/api/v1/products/${item.product_id}") {
                                        header("accept", "application/json")
                                    }
                                    if (productResponse.status == HttpStatusCode.OK) {
                                        item.copy(product = productResponse.body<Product>())
                                    } else {
                                        item
                                    }
                                } catch (e: Exception) {
                                    Log.e("CartViewModel", "Failed to fetch product ${item.product_id}: ${e.message}")
                                    item
                                }
                            } else {
                                item
                            }
                        }
                        _cartItems.value = updatedItems
                        updateTotalPrice(updatedItems)
                        _isSyncingCart.value = false
                        onSyncSuccess()
                        Log.d("CartViewModel", "Synced cart successfully: $updatedItems")
                    }
                    else -> {
                        _isSyncingCart.value = false
                        onSyncFailed(response.status.value)
                        Log.e("CartViewModel", "Sync cart failed: ${response.status}, body: ${response.bodyAsText()}")
                    }
                }
            } catch (e: Exception) {
                _isSyncingCart.value = false
                onSyncFailed(-1)
                Log.e("CartViewModel", "Sync cart error: ${e.message}", e)
            }
        }
    }

    fun toggleOptionsMenuExpandState() {
        _cartOptionsMenuExpanded.value = !_cartOptionsMenuExpanded.value
    }
}