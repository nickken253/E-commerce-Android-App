package com.mustfaibra.roffu.screens.order

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.mutableStateListOf
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

    private val tabStatus = listOf("Tất cả", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
    private val statusMapping = mapOf(
        "pending" to "Chờ lấy hàng",
        "processing" to "Đang giao",
        "shipped" to "Đã giao",
        "cancelled" to "Đã hủy"
    )

    // Tạo dữ liệu mẫu để kiểm tra UI
    /*
    private fun createSampleOrderData(): List<OrderWithItemsAndProducts> {
        android.util.Log.d("OrderViewModel", "Creating sample order data for testing")
        return listOf(
            OrderWithItemsAndProducts(
                orderId = 1,
                userId = 1,
                total = 250000.0,
                status = "pending",
                shippingAddressId = 1,
                paymentMethod = "COD",
                shippingCarrier = "GHN",
                trackingNumber = "GHN123456",
                createdAt = "2025-05-15T07:30:00Z",
                updatedAt = "2025-05-15T07:30:00Z",
                paymentStatus = "pending",
                orderDate = "2025-05-15T07:30:00Z",
                orderItems = listOf(
                    OrderItem(
                        id = 1,
                        orderId = 1,
                        productId = 101,
                        quantity = 2,
                        subtotal = 150000.0,
                        productName = "Áo thun nam cổ tròn",
                        productImage = "https://picsum.photos/200",
                        createdAt = "2025-05-15T07:30:00Z",
                        updatedAt = "2025-05-15T07:30:00Z"
                    ),
                    OrderItem(
                        id = 2,
                        orderId = 1,
                        productId = 102,
                        quantity = 1,
                        subtotal = 100000.0,
                        productName = "Quần jean nam",
                        productImage = "https://picsum.photos/200",
                        createdAt = "2025-05-15T07:30:00Z",
                        updatedAt = "2025-05-15T07:30:00Z"
                    )
                ),
                shippingAddress = null
            ),
            OrderWithItemsAndProducts(
                orderId = 2,
                userId = 1,
                total = 350000.0,
                status = "processing",
                shippingAddressId = 1,
                paymentMethod = "COD",
                shippingCarrier = "GHN",
                trackingNumber = "GHN789012",
                createdAt = "2025-05-14T10:15:00Z",
                updatedAt = "2025-05-14T10:15:00Z",
                paymentStatus = "completed",
                orderDate = "2025-05-14T10:15:00Z",
                orderItems = listOf(
                    OrderItem(
                        id = 3,
                        orderId = 2,
                        productId = 103,
                        quantity = 1,
                        subtotal = 200000.0,
                        productName = "Giày thể thao nam",
                        productImage = "https://picsum.photos/200",
                        createdAt = "2025-05-14T10:15:00Z",
                        updatedAt = "2025-05-14T10:15:00Z"
                    ),
                    OrderItem(
                        id = 4,
                        orderId = 2,
                        productId = 104,
                        quantity = 3,
                        subtotal = 150000.0,
                        productName = "Tất nam cổ ngắn",
                        productImage = "https://picsum.photos/200",
                        createdAt = "2025-05-14T10:15:00Z",
                        updatedAt = "2025-05-14T10:15:00Z"
                    )
                ),
                shippingAddress = null
            )
        )
    }
    */
    
    fun getOrdersWithProducts() {
        viewModelScope.launch {
            android.util.Log.d("OrderViewModel", "Calling getOrdersWithProducts from repository")
            
            // Thử lấy dữ liệu từ API
            productsRepository.getOrdersWithProducts().let { response ->
                when (response) {
                    is DataResponse.Success -> {
                        android.util.Log.d("OrderViewModel", "API Success: Got orders data")
                        response.data?.let { data ->
                            android.util.Log.d("OrderViewModel", "Orders count: ${data.size}")
                            data.forEach { order ->
                                android.util.Log.d("OrderViewModel", "Order ID: ${order.orderId}, Status: ${order.status}, Items: ${order.orderItems?.size ?: 0}")
                                order.orderItems?.forEach { item ->
                                    android.util.Log.d("OrderViewModel", "  Item: ${item.productName}, Image: ${item.productImage}")
                                }
                            }
                            _ordersWithProducts.clear()
                            _ordersWithProducts.addAll(data)
                            android.util.Log.d("OrderViewModel", "Updated _ordersWithProducts, new size: ${_ordersWithProducts.size}")
                            _errorMessage.value = null // Xóa thông báo lỗi
                            
                            // Log dữ liệu của OrderWithItemsAndProducts để kiểm tra
                            data.forEach { order ->
                                android.util.Log.d("OrderViewModel", "Order: ${order.orderId}, Status: ${order.status}, Total: ${order.total}, PaymentMethod: ${order.paymentMethod}, PaymentStatus: ${order.paymentStatus}")
                                
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
                                                                    
                                                                    // Log thông tin đã cập nhật
                                                                    android.util.Log.d("OrderViewModel", "  - Updated Item: ${item.id}, ProductName: ${item.productName}, ProductImage: ${item.productImage}, Quantity: ${item.quantity}, Subtotal: ${item.subtotal}")
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
                        
                        // Sử dụng dữ liệu mẫu để kiểm tra UI khi API trả về lỗi
                        /*
                        android.util.Log.d("OrderViewModel", "Using sample data for testing UI")
                        val sampleData = createSampleOrderData()
                        _ordersWithProducts.clear()
                        _ordersWithProducts.addAll(sampleData)
                        android.util.Log.d("OrderViewModel", "Added ${sampleData.size} sample orders to UI")
                        */
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