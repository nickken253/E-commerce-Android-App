package com.mustfaibra.roffu.screens.order

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.dto.OrderWithItemsAndProducts
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
) : ViewModel() {
    private val _ordersWithProducts = mutableStateListOf<OrderWithItemsAndProducts>()
    val ordersWithProducts: List<OrderWithItemsAndProducts> = _ordersWithProducts

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val tabStatus = listOf("Tất cả", "Chờ xác nhận", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
    private val statusMapping = mapOf(
        "pending" to "Chờ xác nhận",
        "processing" to "Chờ lấy hàng",
        "shipped" to "Đang giao",
        "delivered" to "Đã giao",
        "cancelled" to "Đã hủy"
    )

    fun getOrdersWithProducts() {
        viewModelScope.launch {
            productsRepository.getOrdersWithProducts().let { response ->
                when (response) {
                    is DataResponse.Success -> {
                        response.data?.let { data ->
                            _ordersWithProducts.clear()
                            _ordersWithProducts.addAll(data)
                            _errorMessage.value = null // Xóa thông báo lỗi
                        } ?: run {
                            _errorMessage.value = "Không có đơn hàng nào"
                        }
                    }
                    is DataResponse.Error -> {
                        when (response.error) {
                            is Error.Unauthorized -> _errorMessage.value = "Vui lòng đăng nhập lại"
                            is Error.Forbidden -> _errorMessage.value = "Bạn không có quyền truy cập"
                            is Error.Empty -> _errorMessage.value = "Không có đơn hàng nào"
                            is Error.Network -> _errorMessage.value = "Lỗi mạng: ${(response.error as Error.Network).message}"
                            is Error.Unknown -> _errorMessage.value = "Lỗi không xác định: ${(response.error as Error.Unknown).message}"
                            is Error.Custom -> TODO()
                            null -> TODO()
                        }
                    }
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun showNotifications() {
        _showNotifications.value = true
    }

    fun hideNotifications() {
        _showNotifications.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}