package com.mustfaibra.roffu.screens.order

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.dto.OrderItem
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

    private val _productPrices = mutableStateMapOf<Int, Double>()
    val productPrices: Map<Int, Double> = _productPrices

    private val tabStatus = listOf("Tất cả", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
    private val statusMapping = mapOf(
        "pending" to "Chờ lấy hàng",
        "processing" to "Đang giao",
        "shipped" to "Đã giao",
        "cancelled" to "Đã hủy"
    )
    
    fun getOrdersWithProducts() {
        viewModelScope.launch {
            android.util.Log.d("OrderViewModel", "Calling getOrdersWithProducts from repository")
            
            // Thử lấy dữ liệu từ API
            productsRepository.getOrdersWithProducts().let { response ->
                when (response) {
                    is DataResponse.Success -> {
                        response.data?.let { data ->
                            data.forEach { order ->
                                order.orderItems?.forEach { item ->
                                }
                            }
                            _ordersWithProducts.clear()
                            _ordersWithProducts.addAll(data)
                            _errorMessage.value = null // Xóa thông báo lỗi
                            
                            // Log dữ liệu của OrderWithItemsAndProducts để kiểm tra
                            data.forEach { order ->
                                // Gọi API lấy chi tiết sản phẩm trong đơn hàng
                                viewModelScope.launch {
                                    val orderItemsResponse = productsRepository.getOrderItems(order.orderId)
                                    when (orderItemsResponse) {
                                        is DataResponse.Success -> {
                                            orderItemsResponse.data?.let { items ->
                                                // Gán danh sách sản phẩm vào đơn hàng
                                                order.orderItems = items
                                                
                                                // Log chi tiết sản phẩm
                                                android.util.Log.d("OrderViewModel", "Fetched ${items.size} items for order ${order.orderId}")
                                                
                                                // Lặp qua từng sản phẩm trong đơn hàng để lấy thông tin chi tiết
                                                items.forEach { item ->
                                                    // Log thông tin ban đầu của item
                                                    android.util.Log.d("OrderViewModel", "  - Item: ${item.id}, ProductName: ${item.productName}, ProductImage: ${item.productImage}, Quantity: ${item.quantity}, Subtotal: ${item.subtotal}")
                                                    
                                                    // Gọi API lấy chi tiết sản phẩm
                                                    viewModelScope.launch {
                                                        val productResponse = productsRepository.getProductResponseDetails(item.productId)
                                                        when (productResponse) {
                                                            is DataResponse.Success -> {
                                                                productResponse.data?.let { product ->
                                                                    // Cập nhật thông tin sản phẩm vào item
                                                                    item.productName = product.product_name
                                                                    if (product.images.isNotEmpty()) {
                                                                        item.productImage = product.images.firstOrNull()?.image_url
                                                                    }
                                                                    // Cập nhật giá sản phẩm vào map
                                                                    _productPrices[item.productId] = product.price.toDouble()
                                                                }
                                                            }
                                                            is DataResponse.Error -> {
                                                                android.util.Log.e("OrderViewModel", "Error fetching product details for product ${item.productId}: ${productResponse.error}")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        is DataResponse.Error -> {
                                            val errorMsg = when (orderItemsResponse.error) {
                                                is Error.Network -> "Lỗi kết nối mạng"
                                                is Error.Empty -> "Không có sản phẩm nào"
                                                else -> "Đã xảy ra lỗi không xác định"
                                            }
                                            android.util.Log.e("OrderViewModel", "API Error: $errorMsg, Error type: ${orderItemsResponse.error}")
                                            _errorMessage.value = errorMsg
                                        }
                                    }
                                }
                            }
                        } ?: run {
                            _errorMessage.value = "Không có đơn hàng nào"
                        }
                    }
                    is DataResponse.Error -> {
                        val errorMsg = when (response.error) {
                            is Error.Network -> "Lỗi kết nối mạng"
                            is Error.Empty -> "Không có đơn hàng nào"
                            is Error.Unauthorized -> "Vui lòng đăng nhập lại"
                            is Error.Forbidden -> "Bạn không có quyền truy cập"
                            is Error.Unknown -> "Đã xảy ra lỗi không xác định"
                            is Error.Custom -> "Lỗi: ${(response.error as Error.Custom).message}"
                            null -> "Lỗi không xác định"
                        }
                        android.util.Log.e("OrderViewModel", "API Error: $errorMsg, Error type: ${response.error}")
                        _errorMessage.value = errorMsg
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

    suspend fun getProductPrice(productId: Int): Double? {
        return try {
            val productResponse = productsRepository.getProductResponseDetails(productId)
            when (productResponse) {
                is DataResponse.Success -> {
                    productResponse.data?.price?.toDouble()
                }
                is DataResponse.Error -> {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}