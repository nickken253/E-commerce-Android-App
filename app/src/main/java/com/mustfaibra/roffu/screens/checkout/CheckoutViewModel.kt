package com.mustfaibra.roffu.screens.checkout

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.Location
import com.mustfaibra.roffu.models.dto.*
import com.mustfaibra.roffu.models.dto.AddressRequest
import com.mustfaibra.roffu.models.dto.AddressResponse
import com.mustfaibra.roffu.models.dto.BankCardListResponse
import com.mustfaibra.roffu.models.dto.CartItemWithProductDetails
import com.mustfaibra.roffu.models.dto.PaymentItem
import com.mustfaibra.roffu.models.dto.ProcessPaymentRequest
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.UserPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    // Biến lưu giá trị CVV
    private val _cvv = mutableStateOf(0)
    val cvv: State<Int> = _cvv

    // Địa chỉ giao hàng
    private val _deliveryAddresses = mutableStateListOf<Location>()
    val deliveryAddresses: List<Location> = _deliveryAddresses
    
    private val _selectedDeliveryAddress = mutableStateOf<Location?>(null)
    val selectedDeliveryAddress: State<Location?> = _selectedDeliveryAddress
    
    private val _isLoadingAddresses = mutableStateOf(false)
    val isLoadingAddresses: State<Boolean> = _isLoadingAddresses
    
    private val _addressError = mutableStateOf<String?>(null)
    val addressError: State<String?> = _addressError

    private val _paymentProviders: MutableList<com.mustfaibra.roffu.models.UserPaymentProviderDetails> = mutableStateListOf()
    val paymentProviders: List<com.mustfaibra.roffu.models.UserPaymentProviderDetails> = _paymentProviders

    private val _bankCards = mutableStateOf<List<BankCardListResponse>>(emptyList())
    val bankCards: State<List<BankCardListResponse>> = _bankCards

    private val _selectedCardId = mutableStateOf<Int?>(null)
    val selectedCardId: State<Int?> = _selectedCardId

    private val _cvvText = mutableStateOf("")
    val cvvText: State<String> = _cvvText

    private val _paymentRequest = mutableStateOf<ProcessPaymentRequest?>(null)
    val paymentRequest: State<ProcessPaymentRequest?> = _paymentRequest

    private val _selectedPaymentMethodId = mutableStateOf<String?>(null)
    val selectedPaymentMethodId: State<String?> = _selectedPaymentMethodId

    private val _subTotalPrice = mutableStateOf(0.0)
    val subTotalPrice: State<Double> = _subTotalPrice

    private val _isLoadingCards = mutableStateOf(false)
    val isLoadingCards: State<Boolean> = _isLoadingCards

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _checkoutState = mutableStateOf<UiState>(UiState.Idle)
    val checkoutState: State<UiState> = _checkoutState

    private val _selectedCartItems = mutableStateListOf<CartItemWithProductDetails>()
    val selectedCartItems: SnapshotStateList<CartItemWithProductDetails> = _selectedCartItems

    private val _totalOrderAmount = mutableStateOf(0.0)
    val totalOrderAmount: State<Double> = _totalOrderAmount

    private val _shippingFee = mutableStateOf(30000.0)
    val shippingFee: State<Double> = _shippingFee

    private val _isLoadingSelectedCartItems = mutableStateOf(false)
    val isLoadingSelectedCartItems: State<Boolean> = _isLoadingSelectedCartItems

    init {
        // Không gọi getUserAddresses ở đây vì cần context
        getUserPaymentProviders()
    }

    fun getBankCards(context: Context) {
        _isLoadingCards.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context) ?: run {
                    _error.value = "Bạn cần đăng nhập để xem danh sách thẻ"
                    _isLoadingCards.value = false
                    return@launch
                }
                val response = RetrofitClient.paymentApiService.getBankCards("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        _bankCards.value = it
                        val defaultCard = it.find { card -> card.isDefault }
                        _selectedCardId.value = defaultCard?.id ?: it.firstOrNull()?.id
                        Log.d("CheckoutViewModel", "Lấy danh sách thẻ thành công: ${it.size} thẻ")
                    } ?: run {
                        _bankCards.value = emptyList()
                        Log.d("CheckoutViewModel", "Không có thẻ nào")
                    }
                } else {
                    _error.value = "Lỗi: ${response.code()} - ${response.message()}"
                    Log.e("CheckoutViewModel", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
                Log.e("CheckoutViewModel", "Exception: ${e.message}")
            } finally {
                _isLoadingCards.value = false
            }
        }
    }

    fun updateSelectedCard(cardId: Int) {
        _selectedCardId.value = cardId
    }
    
    /**
     * Cập nhật giá trị CVV từ UI
     */
    fun setCvv(cvv: Int) {
        _cvv.value = cvv
    }

    fun updateSelectedPaymentMethod(id: String) {
        _selectedPaymentMethodId.value = id
    }

    fun setCvvText(text: String) {
        _cvvText.value = text
    }

    private fun getUserPaymentProviders() {
        viewModelScope.launch {
            try {
                val providers = userRepository.getUserPaymentProviders()
                _paymentProviders.clear()
                _paymentProviders.addAll(providers)
            } catch (e: Exception) {
                _error.value = "Lỗi lấy nhà cung cấp thanh toán: ${e.message}"
            }
        }
    }

    // Lấy danh sách địa chỉ của người dùng qua API
    fun getUserAddresses(context: Context) {
        _isLoadingAddresses.value = true
        _addressError.value = null
        _error.value = null
        
        viewModelScope.launch {
            try {
                // Lấy token từ UserPref
                val token = UserPref.getToken(context)
                if (token.isNullOrEmpty()) {
                    _addressError.value = "Bạn chưa đăng nhập"
                    return@launch
                }
                
                // Gọi API lấy danh sách địa chỉ
                val response = RetrofitClient.addressApiService.getUserAddresses("Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val addressList = response.body()!!
                    
                    // Chuyển đổi từ AddressResponse sang Location
                    val locations = addressList.map { address ->
                        Location(
                            id = address.id,
                            address = address.street,
                            city = "${address.district}, ${address.city}",
                            country = address.country ?: "Vietnam",
                            isDefault = address.isDefault ?: false
                        )
                    }
                    
                    _deliveryAddresses.clear()
                    _deliveryAddresses.addAll(locations)
                    
                    // Chọn địa chỉ mặc định hoặc địa chỉ đầu tiên
                    val defaultAddress = locations.find { it.isDefault } ?: locations.firstOrNull()
                    _selectedDeliveryAddress.value = defaultAddress
                    
                    Log.d("CheckoutViewModel", "Selected address: $defaultAddress")
                    
                    Log.d("CheckoutViewModel", "Lấy địa chỉ thành công: ${locations.size} địa chỉ")
                } else {
                    _addressError.value = "Lỗi lấy địa chỉ: ${response.message()}"
                    Log.e("CheckoutViewModel", "Lỗi lấy địa chỉ: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _addressError.value = "Lỗi lấy địa chỉ giao hàng: ${e.message}"
                Log.e("CheckoutViewModel", "Lỗi lấy địa chỉ: ${e.message}")
            } finally {
                _isLoadingAddresses.value = false
            }
        }
    }
    
    // Tạo địa chỉ mới qua API
    fun createAddress(
        context: Context,
        addressType: String,
        street: String,
        city: String,
        district: String,
        postalCode: String? = null,
        country: String = "Vietnam",
        isDefault: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoadingAddresses.value = true
        
        viewModelScope.launch {
            try {
                // Lấy token từ UserPref
                val token = UserPref.getToken(context)
                if (token.isNullOrEmpty()) {
                    onError("Bạn chưa đăng nhập")
                    return@launch
                }
                
                // Tạo request body
                val addressRequest = AddressRequest(
                    addressType = addressType,
                    street = street,
                    city = city,
                    district = district,
                    postalCode = postalCode,
                    country = country,
                    isDefault = isDefault
                )
                
                // Gọi API tạo địa chỉ
                val response = RetrofitClient.addressApiService.createAddress(
                    token = "Bearer $token",
                    request = addressRequest
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val newAddress = response.body()!!
                    
                    // Chuyển đổi từ AddressResponse sang Location
                    val location = Location(
                        id = newAddress.id,
                        address = newAddress.street,
                        city = "${newAddress.district}, ${newAddress.city}",
                        country = newAddress.country ?: "Vietnam",
                        isDefault = newAddress.isDefault ?: false
                    )
                    
                    // Xử lý địa chỉ mặc định
                    if (isDefault) {
                        _deliveryAddresses.forEach { address ->
                            if (address.id != location.id) {
                                val index = _deliveryAddresses.indexOf(address)
                                if (index >= 0) {
                                    _deliveryAddresses[index] = address.copy()
                                }
                            }
                        }
                    }
                    
                    // Thêm địa chỉ mới vào danh sách
                    _deliveryAddresses.add(location)
                    _selectedDeliveryAddress.value = location
                    
                    onSuccess()
                    Log.d("CheckoutViewModel", "Tạo địa chỉ mới thành công: $location")
                } else {
                    onError("Lỗi tạo địa chỉ: ${response.message()}")
                    Log.e("CheckoutViewModel", "Lỗi tạo địa chỉ: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Lỗi tạo địa chỉ: ${e.message}")
                Log.e("CheckoutViewModel", "Lỗi tạo địa chỉ: ${e.message}")
            } finally {
                _isLoadingAddresses.value = false
            }
        }
    }
    
    // Cập nhật địa chỉ qua API
    fun updateAddress(
        context: Context,
        addressId: Int,
        addressType: String,
        street: String,
        city: String,
        district: String,
        postalCode: String? = null,
        country: String = "Vietnam",
        isDefault: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoadingAddresses.value = true
        
        viewModelScope.launch {
            try {
                // Lấy token từ UserPref
                val token = UserPref.getToken(context)
                if (token.isNullOrEmpty()) {
                    onError("Bạn chưa đăng nhập")
                    return@launch
                }
                
                // Tạo request body
                val addressRequest = AddressRequest(
                    addressType = addressType,
                    street = street,
                    city = city,
                    district = district,
                    postalCode = postalCode,
                    country = country,
                    isDefault = isDefault
                )
                
                // Gọi API cập nhật địa chỉ
                val response = RetrofitClient.addressApiService.updateAddress(
                    addressId = addressId,
                    token = "Bearer $token",
                    request = addressRequest
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val updatedAddress = response.body()!!
                    
                    // Chuyển đổi từ AddressResponse sang Location
                    val location = Location(
                        id = updatedAddress.id,
                        address = updatedAddress.street,
                        city = "${updatedAddress.district}, ${updatedAddress.city}",
                        country = updatedAddress.country ?: "Vietnam",
                        isDefault = updatedAddress.isDefault ?: false
                    )
                    
                    // Tìm và cập nhật địa chỉ trong danh sách
                    val addressIndex = _deliveryAddresses.indexOfFirst { it.id == addressId }
                    if (addressIndex != -1) {
                        _deliveryAddresses[addressIndex] = location
                    }
                    
                    // Xử lý địa chỉ mặc định
                    if (isDefault) {
                        _deliveryAddresses.forEach { address ->
                            if (address.id != addressId) {
                                val index = _deliveryAddresses.indexOf(address)
                                if (index >= 0) {
                                    _deliveryAddresses[index] = address.copy()
                                }
                            }
                        }
                    }
                    
                    // Nếu địa chỉ đang được chọn được cập nhật, cập nhật selectedDeliveryAddress
                    if (_selectedDeliveryAddress.value?.id == addressId) {
                        _selectedDeliveryAddress.value = location
                    }
                    
                    onSuccess()
                    Log.d("CheckoutViewModel", "Cập nhật địa chỉ thành công: $location")
                } else {
                    onError("Lỗi cập nhật địa chỉ: ${response.message()}")
                    Log.e("CheckoutViewModel", "Lỗi cập nhật địa chỉ: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Lỗi cập nhật địa chỉ: ${e.message}")
                Log.e("CheckoutViewModel", "Lỗi cập nhật địa chỉ: ${e.message}")
            } finally {
                _isLoadingAddresses.value = false
            }
        }
    }
    
    // Chọn địa chỉ giao hàng
    fun selectDeliveryAddress(addressId: Int) {
        val address = _deliveryAddresses.find { it.id == addressId }
        _selectedDeliveryAddress.value = address
    }

    fun updateSelectedCartItems(items: List<CartItemWithProductDetails>) {
        viewModelScope.launch {
            _isLoadingSelectedCartItems.value = true
            if (items != _selectedCartItems) {
                _selectedCartItems.clear()
                _selectedCartItems.addAll(items)
                calculateSubTotal()
                calculateTotalOrderAmount()
                createPaymentRequest(items)
            }
            _isLoadingSelectedCartItems.value = false
        }
    }

    fun setUserCart(cartItems: List<CartItemWithProductDetails>) {
        Log.d("CheckoutViewModel", "Setting user cart: $cartItems")
        viewModelScope.launch {
            if (cartItems.isEmpty()) {
                _error.value = "Không có sản phẩm nào được chọn"
                _subTotalPrice.value = 0.0
                _totalOrderAmount.value = _shippingFee.value
                _selectedCartItems.clear()
                return@launch
            }
            if (cartItems != _selectedCartItems) {
                _selectedCartItems.clear()
                _selectedCartItems.addAll(cartItems)
                calculateSubTotal()
                calculateTotalOrderAmount()
                createPaymentRequest(cartItems)
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _selectedCartItems.clear()
            _subTotalPrice.value = 0.0
            _totalOrderAmount.value = 0.0
        }
    }

    private fun calculateSubTotal() {
        _subTotalPrice.value = _selectedCartItems.sumOf { item ->
            item.unitPrice.toDouble() * item.quantity
        }
    }

    private fun calculateTotalOrderAmount() {
        _totalOrderAmount.value = _subTotalPrice.value + _shippingFee.value
    }

    private fun createPaymentRequest(items: List<CartItemWithProductDetails>) {
        val idempotencyKey = generateIdempotencyKey()
        val paymentItems = items.map { cartItem ->
            PaymentItem(
                productId = cartItem.productId,
                quantity = cartItem.quantity
            )
        }
        
        // Lấy userId từ UserPref
        val currentUserId = UserPref.user.value?.userId ?: 0
        
        _paymentRequest.value = ProcessPaymentRequest(
            userId = currentUserId,
            idempotencyKey = idempotencyKey,
            totalAmount = _subTotalPrice.value.toInt(),
            status = "pending",
            shippingAddressId = _selectedDeliveryAddress.value?.id ?: 0, // Sẽ kiểm tra lại khi thanh toán
            items = paymentItems,
            cvv = 0
        )
        Log.d("CheckoutViewModel", "Tạo request thanh toán với idempotency_key: $idempotencyKey, userId: $currentUserId")
    }

//    private fun createEmptyPaymentRequest(amount: Double) {
//        val idempotencyKey = generateIdempotencyKey()
//        _paymentRequest.value = ProcessPaymentRequest(
//            userId = 0,
//            idempotencyKey = idempotencyKey,
//            totalAmount = amount.toInt(),
//            status = "pending",
//            shippingAddressId = _deliveryAddress.value?.id ?: 0,
//            items = emptyList(),
//            cvv = 0
//        )
//        Log.d("CheckoutViewModel", "Tạo request thanh toán rỗng với idempotency_key: $idempotencyKey")
//    }

    fun makeTransactionPayment(
        items: List<CartItemWithProductDetails>,
        total: Double,
        onCheckoutSuccess: () -> Unit,
        onCheckoutFailed: (message: Int) -> Unit,
    ) {
        _checkoutState.value = UiState.Idle
        _selectedPaymentMethodId.value?.let { providerId ->
            _checkoutState.value = UiState.Loading
            viewModelScope.launch {
                try {
                    productsRepository.saveOrders(
                        items = items.map { com.mustfaibra.roffu.models.CartItem(cartId = it.id, productId = it.productId, quantity = it.quantity) },
                        providerId = providerId,
                        total = total,
                        deliveryAddressId = _selectedDeliveryAddress.value?.id ?: run {
                            _checkoutState.value = UiState.Error(error = Error.Unknown)
                            onCheckoutFailed(R.string.please_select_delivery_address)
                            return@launch
                        },
                        onFinished = {
                            _checkoutState.value = UiState.Success
                            onCheckoutSuccess()
                        }
                    )
                } catch (e: Exception) {
                    _checkoutState.value = UiState.Error(error = Error.Unknown)
                    onCheckoutFailed(R.string.error_saving_order)
                }
            }
        } ?: run {
            _checkoutState.value = UiState.Error(error = Error.Unknown)
            onCheckoutFailed(R.string.please_select_payment)
        }
    }

    fun processCardPayment(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Sử dụng giá trị CVV đã được lưu trước đó
        val cvv = _cvv.value
        _checkoutState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val cardId = _selectedCardId.value ?: run {
                    _checkoutState.value = UiState.Error(error = Error.Unknown)
                    onError("Vui lòng chọn thẻ thanh toán")
                    return@launch
                }
                val request = _paymentRequest.value ?: run {
                    _checkoutState.value = UiState.Error(error = Error.Unknown)
                    onError("Không tìm thấy thông tin thanh toán")
                    return@launch
                }
                val token = UserPref.getToken(context) ?: run {
                    _checkoutState.value = UiState.Error(error = Error.Unauthorized)
                    onError("Bạn cần đăng nhập để thanh toán")
                    return@launch
                }
                
                // Kiểm tra xem đã chọn địa chỉ giao hàng chưa
                if (_selectedDeliveryAddress.value == null) {
                    _checkoutState.value = UiState.Error(error = Error.Unknown)
                    onError("Vui lòng chọn địa chỉ giao hàng")
                    return@launch
                }
                // Kiểm tra CVV có hợp lệ không
                if (cvv <= 0) {
                    _checkoutState.value = UiState.Error(error = Error.Unknown)
                    onError("Vui lòng nhập mã CVV hợp lệ")
                    return@launch
                }
                
                // Lấy ID của địa chỉ giao hàng đã chọn
                val shippingAddressId = _selectedDeliveryAddress.value?.id ?: run {
                    _checkoutState.value = UiState.Error(error = Error.Unknown)
                    onError("Không tìm thấy ID của địa chỉ giao hàng")
                    return@launch
                }
                
                // Cập nhật request theo đúng yêu cầu của API
                val updatedRequest = request.copy(
                    cvv = cvv,
                    shippingAddressId = shippingAddressId
                )
                
                // Log thông tin địa chỉ giao hàng để debug
                Log.d("CheckoutViewModel", "Selected Delivery Address: ${_selectedDeliveryAddress.value?.address}, ID: $shippingAddressId")
                
                // Log các trường được gửi đến BE
                Log.d("CheckoutViewModel", "======= PAYMENT REQUEST DETAILS =======")
                Log.d("CheckoutViewModel", "Card ID: $cardId")
                Log.d("CheckoutViewModel", "User ID: ${updatedRequest.userId}")
                Log.d("CheckoutViewModel", "Idempotency Key: ${updatedRequest.idempotencyKey}")
                Log.d("CheckoutViewModel", "Total Amount: ${updatedRequest.totalAmount}")
                Log.d("CheckoutViewModel", "Status: ${updatedRequest.status}")
                Log.d("CheckoutViewModel", "Shipping Address ID: ${updatedRequest.shippingAddressId}")
                Log.d("CheckoutViewModel", "CVV: ${updatedRequest.cvv}")
                Log.d("CheckoutViewModel", "Items Count: ${updatedRequest.items.size}")
                updatedRequest.items.forEachIndexed { index, item ->
                    Log.d("CheckoutViewModel", "Item $index - Product ID: ${item.productId}, Quantity: ${item.quantity}")
                }
                Log.d("CheckoutViewModel", "Token: Bearer ${token.take(10)}...")
                Log.d("CheckoutViewModel", "======= END PAYMENT REQUEST =======")
                
                // Gọi API processCardPayment
                val response = RetrofitClient.paymentApiService.processCardPayment(
                    cardId = cardId,
                    request = updatedRequest,
                    token = "Bearer $token"
                )
                // Xử lý kết quả trả về từ API
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success) {
                            // Lấy danh sách các sản phẩm đã thanh toán từ request
                            // Chúng ta sẽ sử dụng productId để xóa các sản phẩm này khỏi giỏ hàng
                            val itemsToRemove = updatedRequest.items
                            
                            // Cập nhật trạng thái thành công
                            _checkoutState.value = UiState.Success
                            
                            // Xóa các sản phẩm khỏi giỏ hàng
                            removeCartItemsAfterPayment(itemsToRemove, context) {
                                // Gọi callback thành công sau khi đã xử lý xóa giỏ hàng
                                onSuccess()
                            }
                        } else {
                            _checkoutState.value = UiState.Error(error = Error.Unknown)
                            onError(it.message)
                        }
                    } ?: run {
                        _checkoutState.value = UiState.Error(error = Error.Unknown)
                        onError("Không thể xử lý thanh toán")
                    }
                } else {
                    _checkoutState.value = UiState.Error(error = Error.Network)
                    onError("Lỗi: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _checkoutState.value = UiState.Error(error = Error.Unknown)
                onError("Lỗi: ${e.message}")
            }
        }
    }

    private fun generateIdempotencyKey(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val length = (12..18).random()
        return uuid.take(length)
    }

    /**
     * Xóa các sản phẩm đã thanh toán khỏi giỏ hàng
     * @param items Danh sách các sản phẩm cần xóa
     * @param context Context để lấy token
     * @param onComplete Callback khi hoàn thành
     */
    fun removeCartItemsAfterPayment(items: List<PaymentItem>, context: android.content.Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context) ?: run {
                    Log.e("CheckoutViewModel", "Token is null, user not authenticated")
                    onComplete()
                    return@launch
                }
                
                val authToken = "Bearer $token"
                val productIds = items.map { it.productId }
                
                Log.d("CheckoutViewModel", "Removing ${productIds.size} items from cart after successful payment")
                
                // Xóa từng sản phẩm khỏi giỏ hàng dựa trên productId
                // Cần gọi API lấy giỏ hàng trước để tìm cartItemId tương ứng với productId
                try {
                    val cartResponse = RetrofitClient.cartApiService.getCart(authToken)
                    if (cartResponse.isSuccessful && cartResponse.body() != null) {
                        val cartItems = cartResponse.body()!!.items
                        // Lọc các mục trong giỏ hàng có productId nằm trong danh sách cần xóa
                        val cartItemsToRemove = cartItems.filter { cartItem -> 
                            productIds.contains(cartItem.productId) 
                        }
                        
                        Log.d("CheckoutViewModel", "Found ${cartItemsToRemove.size} matching cart items to remove")
                        
                        // Xóa từng mục trong giỏ hàng
                        cartItemsToRemove.forEach { cartItem ->
                            try {
                                val response = RetrofitClient.cartApiService.deleteCartItem(cartItem.id, authToken)
                                if (response.isSuccessful) {
                                    Log.d("CheckoutViewModel", "Successfully removed cart item: ${cartItem.id} (productId: ${cartItem.productId})")
                                } else {
                                    Log.e("CheckoutViewModel", "Failed to remove cart item: ${cartItem.id}, Error: ${response.code()} - ${response.message()}")
                                }
                            } catch (e: Exception) {
                                Log.e("CheckoutViewModel", "Exception when removing cart item: ${cartItem.id}, Error: ${e.message}")
                            }
                        }
                    } else {
                        Log.e("CheckoutViewModel", "Failed to get cart: ${cartResponse.code()} - ${cartResponse.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("CheckoutViewModel", "Exception when getting cart: ${e.message}")
                }
                
                Log.d("CheckoutViewModel", "Completed cart cleanup after payment")
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Error cleaning up cart after payment: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}