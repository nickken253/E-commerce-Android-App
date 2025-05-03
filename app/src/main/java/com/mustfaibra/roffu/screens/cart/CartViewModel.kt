package com.mustfaibra.roffu.screens.cart


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.utils.getDiscountedValue
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
}