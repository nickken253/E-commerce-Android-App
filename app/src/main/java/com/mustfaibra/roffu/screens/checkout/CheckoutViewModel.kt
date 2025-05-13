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

    private val _deliveryAddress = mutableStateOf<com.mustfaibra.roffu.models.Location?>(null)
    val deliveryAddress: State<com.mustfaibra.roffu.models.Location?> = _deliveryAddress

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
        getUserPaymentProviders()
        getUserLocations()
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

    private fun getUserLocations() {
        viewModelScope.launch {
            try {
                val locations = userRepository.getUserLocations()
                if (locations.isNotEmpty()) {
                    _deliveryAddress.value = locations.first()
                }
            } catch (e: Exception) {
                _error.value = "Lỗi lấy địa chỉ giao hàng: ${e.message}"
            }
        }
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
        _paymentRequest.value = ProcessPaymentRequest(
            userId = 0,
            idempotencyKey = idempotencyKey,
            totalAmount = _subTotalPrice.value.toInt(),
            status = "pending",
            shippingAddressId = _deliveryAddress.value?.id ?: 0,
            items = paymentItems,
            cvv = 0
        )
        Log.d("CheckoutViewModel", "Tạo request thanh toán với idempotency_key: $idempotencyKey")
    }

    private fun createEmptyPaymentRequest(amount: Double) {
        val idempotencyKey = generateIdempotencyKey()
        _paymentRequest.value = ProcessPaymentRequest(
            userId = 0,
            idempotencyKey = idempotencyKey,
            totalAmount = amount.toInt(),
            status = "pending",
            shippingAddressId = _deliveryAddress.value?.id ?: 0,
            items = emptyList(),
            cvv = 0
        )
        Log.d("CheckoutViewModel", "Tạo request thanh toán rỗng với idempotency_key: $idempotencyKey")
    }

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
                        deliveryAddressId = _deliveryAddress.value?.id,
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
        cvv: Int,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
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
                val updatedRequest = request.copy(cvv = cvv)
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

    private fun generateIdempotencyKey(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val length = (12..18).random()
        return uuid.take(length)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}