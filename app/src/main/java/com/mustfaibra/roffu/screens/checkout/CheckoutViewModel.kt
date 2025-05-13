package com.mustfaibra.roffu.screens.checkout


import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.Location
import com.mustfaibra.roffu.models.UserPaymentProviderDetails
import com.mustfaibra.roffu.models.dto.BankCardListResponse
import com.mustfaibra.roffu.models.dto.PaymentItem
import com.mustfaibra.roffu.models.dto.ProcessPaymentRequest
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.getDiscountedValue
import com.skydoves.whatif.whatIfNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _deliveryAddress = mutableStateOf<Location?>(null)
    val deliveryAddress: State<Location?> = _deliveryAddress

    private val _paymentProviders: MutableList<UserPaymentProviderDetails> = mutableStateListOf()
    val paymentProviders: List<UserPaymentProviderDetails> = _paymentProviders
    
    // Danh sách thẻ ngân hàng của người dùng
    private val _bankCards = mutableStateOf<List<BankCardListResponse>>(emptyList())
    val bankCards: State<List<BankCardListResponse>> = _bankCards
    
    // Thẻ được chọn để thanh toán
    private val _selectedCardId = mutableStateOf<Int?>(null)
    val selectedCardId: State<Int?> = _selectedCardId
    
    // Mã CVV được nhập
    private val _cvvText = mutableStateOf("")
    val cvvText: State<String> = _cvvText
    
    // Request thanh toán được tạo sẵn
    private val _paymentRequest = mutableStateOf<ProcessPaymentRequest?>(null)
    val paymentRequest: State<ProcessPaymentRequest?> = _paymentRequest

    private val _selectedPaymentMethodId = mutableStateOf<String?>(null)
    val selectedPaymentMethodId: State<String?> = _selectedPaymentMethodId
    val subTotalPrice = mutableStateOf(0.0)
    
    // Trạng thái của việc lấy danh sách thẻ
    private val _isLoadingCards = mutableStateOf(false)
    val isLoadingCards: State<Boolean> = _isLoadingCards
    
    // Thông báo lỗi
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _checkoutState = mutableStateOf<UiState>(UiState.Idle)
    val checkoutState: State<UiState> = _checkoutState

    init {
        getUserPaymentProviders()
        getUserLocations()
    }
    
    /**
     * Lấy danh sách thẻ ngân hàng của người dùng
     */
    fun getBankCards(context: Context) {
        _isLoadingCards.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                // Lấy token xác thực
                val token = UserPref.getToken(context)
                
                if (token == null) {
                    _error.value = "Bạn cần đăng nhập để xem danh sách thẻ"
                    _isLoadingCards.value = false
                    return@launch
                }
                
                // Gọi API
                val response = RetrofitClient.paymentApiService.getBankCards(
                    token = "Bearer $token"
                )
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        _bankCards.value = it
                        // Nếu có thẻ mặc định, chọn nó
                        val defaultCard = it.find { card -> card.isDefault }
                        if (defaultCard != null) {
                            _selectedCardId.value = defaultCard.id
                        } else if (it.isNotEmpty()) {
                            _selectedCardId.value = it.first().id
                        }
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
    
    /**
     * Cập nhật thẻ được chọn để thanh toán
     */
    fun updateSelectedCard(cardId: Int) {
        _selectedCardId.value = cardId
    }
    
    /**
     * Cập nhật phương thức thanh toán được chọn
     */
    fun updateSelectedPaymentMethod(id: String) {
        _selectedPaymentMethodId.value = id
    }
    
    /**
     * Cập nhật mã CVV được nhập
     */
    fun setCvvText(text: String) {
        _cvvText.value = text
    }

    private fun getUserPaymentProviders() {
        viewModelScope.launch {
            userRepository.getUserPaymentProviders().let {
                _paymentProviders.addAll(it)
            }
        }
    }

    private fun getUserLocations() {
        viewModelScope.launch {
            userRepository.getUserLocations().let {
                if (it.isNotEmpty()) {
                    _deliveryAddress.value = it.first()
                }
            }
        }
    }

    fun setUserCart(cartItems: List<CartItem>) {
        subTotalPrice.value = 0.0
        cartItems.forEach { cartItem ->
            /** Now should update the sub total price */
            subTotalPrice.value += cartItem.product?.price?.times(cartItem.quantity)
                ?.getDiscountedValue(cartItem.product?.discount ?: 0) ?: 0.0
        }
        
        // Tạo sẵn request thanh toán với idempotency_key
        createPaymentRequest(cartItems)
    }
    
    /**
     * Cập nhật trực tiếp tổng tiền thanh toán (dùng khi nhận từ navigation arguments)
     */
    fun setTotalAmount(amount: Double) {
        subTotalPrice.value = amount
        
        // Tạo sẵn request thanh toán với idempotency_key nhưng không có danh sách sản phẩm
        createEmptyPaymentRequest(amount)
    }
    
    /**
     * Tạo request thanh toán không có danh sách sản phẩm, chỉ có tổng tiền
     */
    private fun createEmptyPaymentRequest(amount: Double) {
        // Tạo idempotency key ngẫu nhiên
        val idempotencyKey = generateIdempotencyKey()
        
        // Tạo request
        _paymentRequest.value = ProcessPaymentRequest(
            userId = 0, // Sẽ được lấy từ token trên server
            idempotencyKey = idempotencyKey,
            totalAmount = amount.toInt(),
            status = "pending",
            shippingAddressId = _deliveryAddress.value?.id ?: 0,
            items = emptyList(), // Sẽ được cập nhật sau khi gọi API lấy thông tin sản phẩm
            cvv = 0 // Sẽ được cập nhật sau khi người dùng nhập
        )
        
        Log.d("CheckoutViewModel", "Tạo sẵn request thanh toán rỗng với idempotency_key: $idempotencyKey")
    }
    
    /**
     * Tạo sẵn request thanh toán với idempotency_key
     */
    private fun createPaymentRequest(items: List<CartItem>) {
        // Tạo idempotency key ngẫu nhiên
        val idempotencyKey = generateIdempotencyKey()
        
        // Chuyển đổi danh sách sản phẩm sang định dạng yêu cầu
        val paymentItems = items.map { cartItem ->
            PaymentItem(
                productId = cartItem.product?.id ?: 0,
                quantity = cartItem.quantity
            )
        }
        
        // Tạo request
        _paymentRequest.value = ProcessPaymentRequest(
            userId = 0, // Sẽ được lấy từ token trên server
            idempotencyKey = idempotencyKey,
            totalAmount = subTotalPrice.value.toInt(),
            status = "pending",
            shippingAddressId = _deliveryAddress.value?.id ?: 0,
            items = paymentItems,
            cvv = 0 // Sẽ được cập nhật sau khi người dùng nhập
        )
        
        Log.d("CheckoutViewModel", "Tạo sẵn request thanh toán với idempotency_key: $idempotencyKey")
    }

    fun makeTransactionPayment(
        items: List<CartItem>,
        total: Double,
        onCheckoutSuccess: () -> Unit,
        onCheckoutFailed: (message: Int) -> Unit,
    ) {
        _checkoutState.value = UiState.Idle
        _selectedPaymentMethodId.value.whatIfNotNull(
            whatIf = {
                _checkoutState.value = UiState.Loading
                viewModelScope.launch {
                    delay(5000)
                    /** Now clear the cart */
                    productsRepository.saveOrders(
                        items = items,
                        providerId = _selectedPaymentMethodId.value,
                        total = total,
                        deliveryAddressId = _deliveryAddress.value?.id,
                        onFinished = {
                            _checkoutState.value = UiState.Success
                            onCheckoutSuccess()
                        }
                    )
                }
            },
            whatIfNot = {
                _checkoutState.value = UiState.Error(error = Error.Unknown)
                onCheckoutFailed(R.string.please_select_payment)
            },
        )
    }
    
    /**
     * Thanh toán bằng thẻ Visa
     * @param cvv Mã bảo mật CVV của thẻ
     * @param context Context để lấy token xác thực
     * @param onSuccess Callback khi thanh toán thành công
     * @param onError Callback khi có lỗi
     */
    fun processCardPayment(
        cvv: Int,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _checkoutState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                // Kiểm tra xem đã chọn thẻ chưa
                val cardId = _selectedCardId.value
                if (cardId == null) {
                    _checkoutState.value = UiState.Error(error = Error.Unknown)
                    onError("Vui lòng chọn thẻ thanh toán")
                    return@launch
                }
                
                // Kiểm tra xem đã tạo request chưa
                val request = _paymentRequest.value
                if (request == null) {
                    _checkoutState.value = UiState.Error(error = Error.Unknown)
                    onError("Không tìm thấy thông tin thanh toán")
                    return@launch
                }
                
                // Lấy token xác thực
                val token = UserPref.getToken(context)
                if (token == null) {
                    _checkoutState.value = UiState.Error(error = Error.Unauthorized)
                    onError("Bạn cần đăng nhập để thanh toán")
                    return@launch
                }
                
                // Cập nhật CVV vào request
                val updatedRequest = request.copy(cvv = cvv)
                
                // Gọi API xử lý thanh toán
                val response = RetrofitClient.paymentApiService.processCardPayment(
                    cardId = cardId,
                    request = updatedRequest,
                    token = "Bearer $token"
                )
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success) {
                            _checkoutState.value = UiState.Success
                            onSuccess()
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
    
    /**
     * Tạo idempotency key ngẫu nhiên
     * @return Chuỗi ngẫu nhiên từ 12-18 ký tự
     */
    private fun generateIdempotencyKey(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val length = (12..18).random()
        return uuid.take(length)
    }
}