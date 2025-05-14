package com.mustfaibra.roffu.screens.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.dto.BankCardListResponse
import com.mustfaibra.roffu.models.dto.BankCardRequest
import com.mustfaibra.roffu.models.dto.BankCardResponse
import com.mustfaibra.roffu.utils.UserPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor() : ViewModel() {
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    
    private val _savedCard = mutableStateOf<BankCardResponse?>(null)
    val savedCard: State<BankCardResponse?> = _savedCard
    
    private val _bankCards = mutableStateOf<List<BankCardListResponse>>(emptyList())
    val bankCards: State<List<BankCardListResponse>> = _bankCards
    
    private val _isLoadingCards = mutableStateOf(false)
    val isLoadingCards: State<Boolean> = _isLoadingCards
    
    /**
     * Định dạng số thẻ theo yêu cầu: thêm dấu cách sau mỗi 4 ký tự (trừ vị trí cuối)
     */
    private fun formatCardNumberForApi(cardNumber: String): String {
        // Lọc bỏ các ký tự không phải số
        val cleanNumber = cardNumber.replace("\\s".toRegex(), "").filter { it.isDigit() }
        if (cleanNumber.length != 16) return cleanNumber

        val formattedNumber = StringBuilder()

        for (i in cleanNumber.indices) {
            formattedNumber.append(cleanNumber[i])
            if ((i + 1) % 4 == 0 && i < cleanNumber.length - 1) {
                formattedNumber.append(" ")
            }
        }
        
        return formattedNumber.toString()
    }
    
    /**
     * Lưu thông tin thẻ ngân hàng thông qua API
     */
    fun saveBankCard(
        cardNumber: String,
        cardHolder: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                // Lấy token xác thực
                val token = UserPref.getToken(context)
                
                if (token == null) {
                    _error.value = "Bạn cần đăng nhập để lưu thông tin thẻ"
                    _isLoading.value = false
                    return@launch
                }
                
                // Định dạng số thẻ theo yêu cầu
                val formattedCardNumber = formatCardNumberForApi(cardNumber)
                
                // Tạo request
                val request = BankCardRequest(
                    cardNumber = formattedCardNumber,
                    cardHolder = cardHolder,
                    expiryMonth = expiryMonth,
                    expiryYear = expiryYear,
                    cvv = cvv
                )
                
                // Gọi API
                val response = RetrofitClient.paymentApiService.saveBankCard(
                    request = request,
                    token = "Bearer $token"
                )
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        _savedCard.value = it
                        Toast.makeText(context, "Đã lưu thông tin thẻ thành công", Toast.LENGTH_SHORT).show()
                        // Lấy lại danh sách thẻ sau khi thêm thành công
                        getBankCards(context)
                        onSuccess()
                    } ?: run {
                        _error.value = "Không thể lưu thông tin thẻ"
                    }
                } else {
                    _error.value = "Lỗi: ${response.code()} - ${response.message()}"
                    Log.e("PaymentViewModel", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
                Log.e("PaymentViewModel", "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
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
                        Log.d("PaymentViewModel", "Lấy danh sách thẻ thành công: ${it.size} thẻ")
                    } ?: run {
                        _bankCards.value = emptyList()
                        Log.d("PaymentViewModel", "Không có thẻ nào")
                    }
                } else {
                    _error.value = "Lỗi: ${response.code()} - ${response.message()}"
                    Log.e("PaymentViewModel", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
                Log.e("PaymentViewModel", "Exception: ${e.message}")
            } finally {
                _isLoadingCards.value = false
            }
        }
    }
}
