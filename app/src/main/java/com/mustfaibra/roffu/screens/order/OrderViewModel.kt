package com.mustfaibra.roffu.screens.order

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.models.OrderDetails
import com.mustfaibra.roffu.models.OrderWithItemsAndProducts
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
) : ViewModel() {
    private val _orders = mutableStateListOf<OrderDetails>()
    val orders: List<OrderDetails> = _orders

    private val _ordersWithProducts = mutableStateListOf<OrderWithItemsAndProducts>()
    val ordersWithProducts: List<OrderWithItemsAndProducts> = _ordersWithProducts

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    private val tabStatus = listOf("Tất cả", "Chờ xác nhận", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")

    fun getOrders() {
        viewModelScope.launch {
            productsRepository.getOrdersHistory().let {
                when (it) {
                    is DataResponse.Success -> {
                        it.data?.let { data ->
                            _orders.clear()
                            _orders.addAll(data)
                        }
                    }
                    is DataResponse.Error -> {
                        // handle error
                    }
                }
            }
        }
    }

    fun getOrdersWithProducts() {
        viewModelScope.launch {
            productsRepository.getOrdersWithProducts().let {
                when (it) {
                    is DataResponse.Success -> {
                        it.data?.let { data ->
                            _ordersWithProducts.clear()
                            _ordersWithProducts.addAll(data)
                        }
                    }
                    is DataResponse.Error -> {
                        // handle error
                    }
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        val status = tabStatus.getOrNull(index)
        // TODO: Update filtered orders logic
    }

    fun showNotifications() {
        // TODO: Update show notifications logic
    }
    fun hideNotifications() {
        // TODO: Update hide notifications logic
    }
}
