    package com.mustfaibra.roffu.screens.productdetails

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.dto.AddToCartRequest
import com.mustfaibra.roffu.models.dto.CartItemReponse
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.CartEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class CartResponse(
    val id: Int,
    val user_id: Int,
    val status: String,
    val items: List<CartItemReponse>
)

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val client: HttpClient,
    private val context: Context,
    private val cartEventBus: CartEventBus
) : ViewModel() {
    private val _detailsUiState = mutableStateOf<UiState>(UiState.Idle)
    val detailsUiState: State<UiState> = _detailsUiState

    private val _cartUiState = mutableStateOf<UiState>(UiState.Idle)
    val cartUiState: State<UiState> = _cartUiState

    private val _product = mutableStateOf<Product?>(null)
    val product: State<Product?> = _product

    private val _isInCart = mutableStateOf(false)
    val isInCart: State<Boolean> = _isInCart

    fun getProductDetails(productId: Int) {
        _detailsUiState.value = UiState.Loading
        viewModelScope.launch {
            productsRepository.getProductDetails(productId = productId).let {
                when (it) {
                    is DataResponse.Success -> {
                        _detailsUiState.value = UiState.Success
                        _product.value = it.data
                        val (isInCart, _) = checkIfInCart(productId)
                        _isInCart.value = isInCart
                    }
                    is DataResponse.Error -> {
                        _detailsUiState.value = UiState.Error(error = it.error ?: Error.Network)
                        Log.e("ProductDetailsViewModel", "Error loading product: ${it.error}")
                    }
                }
            }
        }
    }

    fun toggleCartState(productId: Int) {
        _cartUiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                if (_isInCart.value) {
                    val token = UserPref.getToken(context)
                    if (token.isNullOrBlank()) {
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("ProductDetailsViewModel", "No token found, user not authenticated")
                        return@launch
                    }

                    val (isInCart, itemId) = checkIfInCart(productId)
                    if (!isInCart || itemId == null) {
                        _cartUiState.value = UiState.Error(error = Error.Unknown)
                        Log.e("ProductDetailsViewModel", "Cart item not found for productId=$productId")
                        _isInCart.value = false
                        return@launch
                    }

                    Log.d("ProductDetailsViewModel", "Attempting to delete cart item with itemId=$itemId")
                    val response = client.delete("http://34.9.68.100:8000/api/v1/carts/items/$itemId") {
                        header("accept", "application/json")
                        header("Authorization", "Bearer $token")
                    }
                    when (response.status) {
                        HttpStatusCode.OK -> {
                            _isInCart.value = false
                            _cartUiState.value = UiState.Success
                            cartEventBus.notifyCartChanged()
                            Log.d("ProductDetailsViewModel", "Cart item deleted successfully")
                        }
                        HttpStatusCode.NotFound -> {
                            _cartUiState.value = UiState.Error(error = Error.Unknown)
                            Log.e("ProductDetailsViewModel", "Delete failed: Cart item not found, body: ${response.bodyAsText()}")
                            _isInCart.value = false
                        }
                        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                            _cartUiState.value = UiState.Error(error = Error.Unknown)
                            Log.e("ProductDetailsViewModel", "Delete failed: Unauthorized, body: ${response.bodyAsText()}")
                        }
                        else -> {
                            _cartUiState.value = UiState.Error(error = Error.Network)
                            Log.e("ProductDetailsViewModel", "Delete cart item failed: ${response.status}, body: ${response.bodyAsText()}")
                        }
                    }
                } else {
                    Log.d("ProductDetailsViewModel", "Attempting to add cart item with productId=$productId")
                    val token = UserPref.getToken(context)
                    val response = client.post("http://34.9.68.100:8000/api/v1/carts/items") {
                        header("accept", "application/json")
                        header("Content-Type", "application/json")
                        if (!token.isNullOrBlank()) {
                            header("Authorization", "Bearer $token")
                        }
                        setBody(
                            AddToCartRequest(
                                items = listOf(
                                    AddToCartRequest.Item(
                                        product_id = productId,
                                        quantity = 1
                                    )
                                )
                            )
                        )
                    }
                    when (response.status) {
                        HttpStatusCode.OK, HttpStatusCode.Created -> {
                            Log.d("ProductDetailsViewModel", "Add to cart response: ${response.bodyAsText()}")
                            val (isInCart, _) = checkIfInCart(productId)
                            if (isInCart) {
                                _isInCart.value = true
                                _cartUiState.value = UiState.Success
                                cartEventBus.notifyCartChanged()
                                Log.d("ProductDetailsViewModel", "Added to cart successfully, confirmed in cart")
                            } else {
                                _cartUiState.value = UiState.Error(error = Error.Unknown)
                                Log.e("ProductDetailsViewModel", "Add to cart failed: CartItem not found in cart after POST")
                            }
                        }
                        else -> {
                            _cartUiState.value = UiState.Error(error = Error.Network)
                            Log.e("ProductDetailsViewModel", "Add to cart failed: ${response.status}, body: ${response.bodyAsText()}")
                        }
                    }
                }
            } catch (e: Exception) {
                _cartUiState.value = UiState.Error(error = Error.Network)
                Log.e("ProductDetailsViewModel", "Cart operation error: ${e.message}", e)
            }
        }
    }

    private suspend fun checkIfInCart(productId: Int): Pair<Boolean, Int?> {
        return try {
            val token = UserPref.getToken(context)
            if (token.isNullOrBlank()) {
                Log.e("ProductDetailsViewModel", "No token found, user not authenticated")
                return Pair(false, null)
            }

            Log.d("ProductDetailsViewModel", "Checking cart for productId=$productId")
            val response = client.get("http://34.9.68.100:8000/api/v1/carts/") {
                header("accept", "application/json")
                header("Authorization", "Bearer $token")
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    Log.d("ProductDetailsViewModel", "Cart response body: ${response.bodyAsText()}")
                    val cartResponse: CartResponse = response.body()
                    val cartItem = cartResponse.items.find { it.product_id == productId }
                    val isInCart = cartItem != null
                    val itemId = cartItem?.id
                    Log.d("ProductDetailsViewModel", "Check cart: productId=$productId, isInCart=$isInCart, itemId=$itemId")
                    Pair(isInCart, itemId)
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e("ProductDetailsViewModel", "Token expired or invalid")
                    _cartUiState.value = UiState.Error(error = Error.Unknown)
                    Pair(false, null)
                }
                else -> {
                    Log.e("ProductDetailsViewModel", "Check cart failed: ${response.status}, body: ${response.bodyAsText()}")
                    Pair(false, null)
                }
            }
        } catch (e: Exception) {
            Log.e("ProductDetailsViewModel", "Check cart error: ${e.message}", e)
            Pair(false, null)
        }
    }
}