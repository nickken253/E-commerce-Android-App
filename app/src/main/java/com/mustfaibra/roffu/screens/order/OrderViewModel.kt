package com.mustfaibra.roffu.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Simple data model for order and notification

data class OrderProduct(
    val name: String,
    val image: Int,
    val variant: String,
    val quantity: Int,
    val price: Int,
    val priceOrigin: Int? = null
)
data class Order(
    val id: String,
    val shopName: String,
    val shopLabel: String?, // ví dụ: "Mall", "Yêu thích+"
    val status: String,
    val date: String,
    val total: String,
    val products: List<OrderProduct>
)

data class Notification(
    val id: String,
    val content: String
)

data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val showNotificationDialog: Boolean = false
)

class OrderViewModel : ViewModel() {
    private val _orderUiState = MutableStateFlow(OrderUiState())
    val orderUiState: StateFlow<OrderUiState> = _orderUiState

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    private val tabStatus = listOf("Tất cả", "Chờ xác nhận", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")

    init {
        val shoeImages = listOf(
            R.drawable.air_huarache_le_gold_black,
            R.drawable.pegasus_trail_3_gore_tex_dark_green,
            R.drawable.blazer_low_black,
            R.drawable.blazer_low_pink,
            R.drawable.blazer_low_light_green,
            R.drawable.defiant_generation_green,
            R.drawable.defiant_generation_red,
            R.drawable.solarthon_primegreen_gray,
            R.drawable.solarthon_primegreen_black,
            R.drawable.solarthon_primegreen_red
        )
        val shopList = listOf(
            Pair("Ancol Official", "Mall"),
            Pair("amorshoes.vn", "Yêu thích+"),
            Pair("Sneaker House", null)
        )
        val productNames = listOf(
            "Giày sneaker thể thao, giày thời trang nam nữ",
            "Giày chạy bộ Pegasus Trail 3",
            "Giày Blazer Low Black Pink",
            "Giày Defiant Generation Red",
            "Giày Solarthon Primegreen"
        )
        val variants = listOf("Size 42, Trắng", "Size 38, Đen", "Size 40, Xanh", "Size 39, Hồng", "Size 41, Đỏ")

        val fakeOrders = List(20) { i ->
            val shopIdx = i % shopList.size
            val (shopName, shopLabel) = shopList[shopIdx]
            // Tạo số sản phẩm cho từng đơn: đơn chẵn >2 sản phẩm, đơn lẻ <=2
            val numProducts = if (i % 2 == 0) (3..5).random() else (1..2).random()
            val products = List(numProducts) { j ->
                val imgIdx = (i + j) % shoeImages.size
                val priceOrigin = (800_000..2_500_000).random()
                val price = priceOrigin - (50_000..300_000).random()
                OrderProduct(
                    name = productNames[imgIdx % productNames.size],
                    image = shoeImages[imgIdx],
                    variant = variants[imgIdx % variants.size],
                    quantity = (1..2).random(),
                    price = price,
                    priceOrigin = priceOrigin
                )
            }
            val total = products.sumOf { it.price * it.quantity }
            val statusList = listOf("Chờ xác nhận", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
            val status = statusList[i % statusList.size]
            val day = 21 - (i % 20)
            Order(
                id = "#${1001 + i}",
                shopName = shopName,
                shopLabel = shopLabel,
                status = status,
                date = "2025-04-${if (day < 10) "0$day" else "$day"}",
                total = "₫${"%,d".format(total)}",
                products = products
            )
        }
        val fakeNotifications = listOf(
            Notification("1", "Đơn hàng #1001 đã được xác nhận."),
            Notification("2", "Đơn hàng #1002 đang được giao."),
            Notification("3", "Đơn hàng #1003 đã giao thành công.")
        )
        _orderUiState.value = OrderUiState(
            orders = fakeOrders,
            filteredOrders = fakeOrders,
            notifications = fakeNotifications
        )
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        val status = tabStatus.getOrNull(index)
        _orderUiState.update { state ->
            state.copy(
                filteredOrders = if (status == null || status == "Tất cả") state.orders
                    else state.orders.filter { it.status == status }
            )
        }
    }

    fun showNotifications() {
        _orderUiState.update { it.copy(showNotificationDialog = true) }
    }
    fun hideNotifications() {
        _orderUiState.update { it.copy(showNotificationDialog = false) }
    }
}
