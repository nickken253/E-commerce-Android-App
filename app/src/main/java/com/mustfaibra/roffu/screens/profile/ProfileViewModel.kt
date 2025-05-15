package com.mustfaibra.roffu.screens.profile

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.RetrofitClient
import com.mustfaibra.roffu.models.VirtualCard
import com.mustfaibra.roffu.models.dto.BankCardListResponse
import com.mustfaibra.roffu.models.dto.BankCardRequest
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.utils.LOGGED_USER_ID
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val context: Context // Inject Context để truy cập DataStore và SharedPreferences
) : ViewModel() {
    // Lưu trữ thông tin thẻ ngân hàng từ API
    private val _bankCard = MutableStateFlow<BankCardListResponse?>(null)
    val bankCard: StateFlow<BankCardListResponse?> = _bankCard
    
    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Thông báo lỗi
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Giữ lại các trạng thái cũ để tương thích với ProfileScreen
    private val _virtualCard = MutableStateFlow<VirtualCard?>(null)
    val virtualCard: StateFlow<VirtualCard?> = _virtualCard
    val isVirtualCardAdded = virtualCard.map { it != null }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    /**
     * Đăng xuất khỏi ứng dụng
     */
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            // Xóa LOGGED_USER_ID từ DataStore
            context.dataStore.edit {
                it.remove(LOGGED_USER_ID)
            }
            // Xóa dữ liệu trong SharedPreferences và đặt lại UserPref
            UserPref.logout(context)
            // Log để kiểm tra
            Log.d("ProfileViewModel", "LOGGED_USER_ID after logout: ${context.dataStore.data.first()[LOGGED_USER_ID]}")
            Log.d("ProfileViewModel", "Token after logout: ${UserPref.getToken(context)}")
            Log.d("ProfileViewModel", "User after logout: ${UserPref.user.value}")
            // Gọi lambda để điều hướng
            onLoggedOut()
        }
    }

    /**
     * Lấy thông tin thẻ ngân hàng từ API
     */
    fun getBankCards() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val token = UserPref.getToken(context)
                if (token == null) {
                    _error.value = "Vui lòng đăng nhập lại"
                    _isLoading.value = false
                    return@launch
                }
                
                val response = RetrofitClient.paymentApiService.getBankCards("Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val cards = response.body()!!
                    if (cards.isNotEmpty()) {
                        _bankCard.value = cards[0] // Chỉ lấy thẻ đầu tiên
                        Log.d("ProfileViewModel", "Loaded bank card: ${cards[0].cardNumber}")
                        
                        // Cập nhật cả virtualCard để tương thích với ProfileScreen
                        _virtualCard.value = VirtualCard(
                            userId = UserPref.user.value?.userId ?: 0,
                            cardNumber = cards[0].cardNumber,
                            cardHolder = cards[0].cardHolderName,
                            expiryMonth = cards[0].expiryMonth,
                            expiryYear = cards[0].expiryYear,
                            cvv = ""
                        )
                    } else {
                        _bankCard.value = null
                        _virtualCard.value = null
                        Log.d("ProfileViewModel", "No bank cards found")
                    }
                } else {
                    _error.value = "Không thể tải thông tin thẻ: ${response.message()}"
                    Log.e("ProfileViewModel", "Error loading bank cards: ${response.code()} ${response.message()}")
                }
            } catch (e: HttpException) {
                _error.value = "Lỗi kết nối: ${e.message()}"
                Log.e("ProfileViewModel", "HttpException: ${e.message()}")
            } catch (e: IOException) {
                _error.value = "Lỗi kết nối mạng"
                Log.e("ProfileViewModel", "IOException: ${e.message}")
            } catch (e: Exception) {
                _error.value = "Đã xảy ra lỗi: ${e.message}"
                Log.e("ProfileViewModel", "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Phương thức cũ để tương thích với ProfileScreen, nhưng đã cập nhật để sử dụng API
     */
    fun loadVirtualCard(userId: Int) {
        // Gọi getBankCards() để lấy thông tin thẻ từ API
        getBankCards()
    }

    /**
     * Thêm thẻ ngân hàng mới qua API
     */
    fun addBankCard(cardNumber: String, cardHolder: String, expiryMonth: String, expiryYear: String, cvv: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val token = UserPref.getToken(context)
                if (token == null) {
                    _error.value = "Vui lòng đăng nhập lại"
                    _isLoading.value = false
                    return@launch
                }
                
                val cardRequest = BankCardRequest(
                    cardNumber = cardNumber,
                    cardHolder = cardHolder,
                    expiryMonth = expiryMonth,
                    expiryYear = expiryYear,
                    cvv = cvv
                )
                
                val response = RetrofitClient.paymentApiService.saveBankCard(cardRequest, "Bearer $token")
                
                if (response.isSuccessful) {
                    Log.d("ProfileViewModel", "Card added successfully")
                    // Sau khi thêm thẻ thành công, tải lại thông tin thẻ
                    getBankCards()
                } else {
                    _error.value = "Không thể thêm thẻ: ${response.message()}"
                    _isLoading.value = false
                    Log.e("ProfileViewModel", "Error adding card: ${response.code()} ${response.message()}")
                }
            } catch (e: HttpException) {
                _error.value = "Lỗi kết nối: ${e.message()}"
                _isLoading.value = false
                Log.e("ProfileViewModel", "HttpException: ${e.message()}")
            } catch (e: IOException) {
                _error.value = "Lỗi kết nối mạng"
                _isLoading.value = false
                Log.e("ProfileViewModel", "IOException: ${e.message}")
            } catch (e: Exception) {
                _error.value = "Đã xảy ra lỗi: ${e.message}"
                _isLoading.value = false
                Log.e("ProfileViewModel", "Exception: ${e.message}")
            }
        }
    }
    
    /**
     * Phương thức cũ để tương thích với ProfileScreen, nhưng đã cập nhật để sử dụng API
     */
    fun addVirtualCard(card: VirtualCard) {
        // Gọi addBankCard() để thêm thẻ qua API
        addBankCard(
            cardNumber = card.cardNumber,
            cardHolder = card.cardHolder,
            expiryMonth = card.expiryMonth,
            expiryYear = card.expiryYear,
            cvv = card.cvv
        )
    }
}