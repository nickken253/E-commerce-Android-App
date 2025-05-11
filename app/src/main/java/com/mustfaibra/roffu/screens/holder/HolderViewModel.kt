package com.mustfaibra.roffu.screens.holder

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 * Đã đơn giản hóa để không sử dụng Room Database nữa, chỉ sử dụng dữ liệu giả lập
 */
@HiltViewModel
class HolderViewModel @Inject constructor() : ViewModel() {

    val cartItems: MutableList<CartItem> = mutableStateListOf()
    val productsOnCartIds: MutableList<Int> = mutableStateListOf()
    val productsOnBookmarksIds: MutableList<Int> = mutableStateListOf()

    init {
        // Khởi tạo dữ liệu giả lập thay vì truy cập vào Room Database
        initializeMockData()
    }

    private fun initializeMockData() {
        // Giả lập một số sản phẩm trong giỏ hàng
        val mockCartItems = listOf(
            CartItem(cartId = 1, productId = 1, quantity = 2, size = "L", color = "Red"),
            CartItem(cartId = 2, productId = 3, quantity = 1, size = "M", color = "Blue")
        )
        cartItems.addAll(mockCartItems)
        
        // Cập nhật danh sách ID sản phẩm trong giỏ hàng
        // Xử lý trường hợp productId có thể là null
        productsOnCartIds.addAll(mockCartItems.mapNotNull { it.productId })
        
        // Giả lập một số sản phẩm đã bookmark
        productsOnBookmarksIds.addAll(listOf(2, 5, 8))
        
        Log.d("HolderViewModel", "Initialized mock data: "
            + "${cartItems.size} cart items, "
            + "${productsOnCartIds.size} products in cart, "
            + "${productsOnBookmarksIds.size} bookmarked products")
    }

    // Cập nhật giỏ hàng - đã đơn giản hóa để không sử dụng Room Database
    @RequiresApi(Build.VERSION_CODES.N)
    fun updateCart(productId: Int, currentlyOnCart: Boolean = false) {
        viewModelScope.launch {
            // Giả lập việc thêm/xóa sản phẩm khỏi giỏ hàng
            if (currentlyOnCart) {
                // Xóa sản phẩm khỏi giỏ hàng
                cartItems.removeIf { it.productId == productId }
                productsOnCartIds.remove(productId)
            } else {
                // Thêm sản phẩm vào giỏ hàng
                // Tìm cartId lớn nhất hiện tại, xử lý cả trường hợp null
                val maxCartId: Int = if (cartItems.isEmpty()) {
                    0 // Nếu danh sách trống, bắt đầu từ 0
                } else {
                    // Lấy giá trị lớn nhất, thay thế null bằng 0
                    cartItems.maxOf { it.cartId ?: 0 }
                }
                // Tăng lên 1 để tạo ID mới
                val newCartId: Int = maxCartId + 1
                cartItems.add(CartItem(
                    cartId = newCartId,
                    productId = productId,
                    quantity = 1,
                    size = "Default",
                    color = "Default"
                ))
                productsOnCartIds.add(productId)
            }
            
            // Giả lập delay để mô phỏng thời gian xử lý
            delay(300)
            Log.d("HolderViewModel", "Updated cart for product $productId, now has ${cartItems.size} items")
        }
    }

    // Cập nhật bookmark - đã đơn giản hóa để không sử dụng Room Database
    fun updateBookmarks(productId: Int, currentlyOnBookmarks: Boolean) {
        viewModelScope.launch {
            // Giả lập việc thêm/xóa sản phẩm khỏi bookmark
            if (currentlyOnBookmarks) {
                productsOnBookmarksIds.remove(productId)
            } else {
                productsOnBookmarksIds.add(productId)
            }
            
            // Giả lập delay để mô phỏng thời gian xử lý
            delay(200)
            Log.d("HolderViewModel", "Updated bookmarks for product $productId, now has ${productsOnBookmarksIds.size} items")
        }
    }
}
