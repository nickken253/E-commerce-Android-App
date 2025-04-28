package com.mustfaibra.roffu.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Simple data model for order and notification

data class Order(
    val id: String,
    val status: String,
    val date: String,
    val total: String,
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
        val fakeOrders = List(60) { i ->
            val statusList = listOf("Chờ xác nhận", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
            val status = statusList[i % statusList.size]
            val day = 21 - (i % 20)
            Order(
                id = "#${1001 + i}",
                status = status,
                date = "2025-04-${if (day < 10) "0$day" else "$day"}",
                total = "${(800_000..2_500_000).random()}đ"
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
