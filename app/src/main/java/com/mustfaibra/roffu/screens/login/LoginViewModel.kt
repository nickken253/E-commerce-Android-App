package com.mustfaibra.roffu.screens.login

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.BuildConfig
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.models.dto.LoginRequest
import com.mustfaibra.roffu.models.dto.RegisterRequest
import com.mustfaibra.roffu.models.dto.ResetPasswordRequest
import com.mustfaibra.roffu.models.dto.ResetPasswordResponse
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.screens.login.RetrofitClient.authApi
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.LOGGED_USER_ID
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.dataStore
import com.resend.Resend
import com.resend.services.emails.model.SendEmailRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val context: Context,
) : ViewModel() {
    val uiState = mutableStateOf<UiState>(UiState.Idle)
    val emailOrPhone = mutableStateOf<String?>("")
    val password = mutableStateOf<String?>("")
    val username = mutableStateOf<String?>(null)

    companion object {
        private const val TAG = "LoginViewModel"
        private const val OTP_EXPIRY_MINUTES = 5
    }

    fun updateUsername(username: String?) {
        this.username.value = username
    }

    fun updateEmailOrPhone(value: String?) {
        this.emailOrPhone.value = value
    }

    fun updatePassword(value: String?) {
        this.password.value = value
    }

    fun authenticateUser(
        username: String,
        password: String,
        onAuthenticated: () -> Unit,
        onAuthenticationFailed: (String) -> Unit
    ) {
        if (username.isBlank() || password.isBlank()) {
            onAuthenticationFailed("Vui lòng nhập đầy đủ thông tin")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // Đăng nhập để lấy token
                val request = LoginRequest(username = username, password = password)
                val loginResponse = RetrofitClient.authApi.login(request)

                // Lưu access_token
                UserPref.updateUserToken(context, loginResponse.access_token)

                // Lấy thông tin người dùng
                val userResponse = RetrofitClient.authApi.getUserProfile("Bearer ${loginResponse.access_token}")

                val user = User(
                    userId = userResponse.id,
                    name = userResponse.full_name,
                    email = userResponse.email,
                    phone = userResponse.phone_number,
                    password = password,
                    gender = 1,
                    role = "user",
                    profile = R.drawable.default_avatar,
                    address = userResponse.address ?: ""
                )

                uiState.value = UiState.Success
                UserPref.updateUser(user)
                UserPref.updateUserId(context, user.userId)
                onAuthenticated()
            } catch (e: retrofit2.HttpException) {
                uiState.value = UiState.Error(error = Error.Network)
                val errorBody = e.response()?.errorBody()?.string() ?: "Không nhận được chi tiết lỗi"
                val errorMessage = when (e.code()) {
                    401 -> "Tên đăng nhập hoặc mật khẩu không đúng"
                    422 -> "Thông tin không hợp lệ: $errorBody"
                    else -> "Đăng nhập thất bại: HTTP ${e.code()} - $errorBody"
                }
                onAuthenticationFailed(errorMessage)
            } catch (e: Exception) {
                uiState.value = UiState.Error(error = Error.Network)
                onAuthenticationFailed("Lỗi hệ thống: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Xóa LOGGED_USER_ID từ DataStore
            context.dataStore.edit {
                it.remove(LOGGED_USER_ID)
            }
            context.dataStore.data.first().let {
                Log.d("DataStore", "LOGGED_USER_ID after logout: ${it[LOGGED_USER_ID]}")
            }
            // Xóa dữ liệu trong SharedPreferences và đặt lại UserPref
            UserPref.logout(context)
            // Đặt lại trạng thái trong ViewModel
            username.value = null
            emailOrPhone.value = ""
            password.value = ""
            uiState.value = UiState.Idle
        }
    }

    fun checkEmailForPasswordReset(
        email: String,
        onEmailExists: () -> Unit,
        onEmailNotFound: (String) -> Unit
    ) {
        if (email.isBlank()) {
            onEmailNotFound("Vui lòng nhập email!")
            return
        }

        if (!isValidEmail(email)) {
            onEmailNotFound("Email không hợp lệ!")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                delay(1000) // Simulate network delay

                userRepository.checkEmailExists(email).let { response ->
                    uiState.value = UiState.Idle
                    when (response) {
                        is DataResponse.Success -> {
                            if (response.data == true) {
                                onEmailExists()
                            } else {
                                onEmailNotFound("Không tìm thấy tài khoản với email này!")
                            }
                        }
                        is DataResponse.Error -> {
                            onEmailNotFound("Lỗi khi kiểm tra email: ${response.error?.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                uiState.value = UiState.Error(error = Error.Network)
                onEmailNotFound("Lỗi hệ thống: ${e.message}")
            }
        }
    }

    fun resetPassword(
        email: String,
        newPassword: String,
        onSuccess: (message: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Validation
        when {
            email.isBlank() || newPassword.isBlank() -> {
                onFailure("Vui lòng điền đầy đủ thông tin!")
                return
            }
            !isValidEmail(email) -> {
                onFailure("Email không hợp lệ!")
                return
            }
            !isValidPassword(newPassword) -> {
                onFailure("Mật khẩu phải dài 8-20 ký tự, chứa chữ cái, số và ký tự đặc biệt!")
                return
            }
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val request = ResetPasswordRequest(
                    email = email,
                    new_password = newPassword
                )

                // Log request
                Log.d("API_REQUEST", "Sending: ${Json.encodeToString(request)}")

                val response = RetrofitClient.authApi.resetPassword(request)

                // Log response chi tiết
                Log.d("API_RESPONSE", "Code: ${response.code()}, Body: ${response.body()?.toString()}, Error: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    response.body()?.let { resetResponse ->
                        Log.d("API_SUCCESS", "Success message: ${resetResponse.message}")
                        uiState.value = UiState.Success
                        onSuccess(resetResponse.message)
                    } ?: run {
                        Log.w("API_WARNING", "Response body is null")
                        onFailure("Không nhận được phản hồi từ server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Error response: $errorBody")

                    val errorMessage = when (response.code()) {
                        400 -> "Yêu cầu không hợp lệ: ${errorBody ?: "Bad Request"}"
                        401 -> "Không có quyền truy cập: Vui lòng kiểm tra thông tin"
                        404 -> "Email không tồn tại trong hệ thống"
                        422 -> {
                            try {
                                val errorResponse = Json.decodeFromString<ResetPasswordResponse>(errorBody ?: "")
                                errorResponse.message ?: "Dữ liệu không hợp lệ (Mã lỗi: ${response.code()})"
                            } catch (e: Exception) {
                                Log.e("PARSE_ERROR", "Failed to parse error body: ${e.message}")
                                "Dữ liệu không hợp lệ (Mã lỗi: ${response.code()}, Chi tiết: $errorBody)"
                            }
                        }
                        500 -> "Lỗi server: Vui lòng thử lại sau (Mã lỗi: ${response.code()})"
                        else -> "Đặt lại mật khẩu thất bại (Mã lỗi: ${response.code()}, Chi tiết: $errorBody)"
                    }
                    onFailure(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Exception occurred: ${e.message}", e)
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Kết nối timeout, vui lòng thử lại"
                    is ConnectException -> "Không thể kết nối đến server, kiểm tra mạng"
                    is kotlinx.serialization.SerializationException -> "Lỗi phân tích dữ liệu từ server: ${e.message}"
                    else -> "Lỗi hệ thống: ${e.message ?: "Unknown error"}"
                }
                onFailure(errorMessage)
            } finally {
                uiState.value = UiState.Idle
            }
        }
    }

    fun registerUser(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        address: String,
        phoneNumber: String,
        onRegistered: () -> Unit,
        onRegistrationFailed: (String) -> Unit
    ) {
        // Kiểm tra các trường bắt buộc
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || name.isBlank() || phoneNumber.isBlank()) {
            onRegistrationFailed("Vui lòng điền đầy đủ thông tin!")
            return
        }

        // Kiểm tra mật khẩu xác nhận
        if (password != confirmPassword) {
            onRegistrationFailed("Mật khẩu xác nhận không khớp!")
            return
        }

        // Kiểm tra định dạng mật khẩu
        if (!isValidPassword(password)) {
            onRegistrationFailed("Mật khẩu phải dài 8-20 ký tự, chứa chữ hoa, chữ thường, số và ký tự đặc biệt!")
            return
        }

        // Kiểm tra định dạng email
        if (!isValidEmail(email)) {
            onRegistrationFailed("Email không hợp lệ!")
            return
        }

        // Kiểm tra định dạng số điện thoại
        if (!isValidPhoneNumber(phoneNumber)) {
            onRegistrationFailed("Số điện thoại không hợp lệ! Phải là số và dài 10-12 chữ số.")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    full_name = name,
                    avatar_url = "https://example.com/default-avatar.png",
                    phone_number = phoneNumber,
                    password = password
                )

                val response = RetrofitClient.authApi.register(request)

                val newUser = User(
                    userId = response.id,
                    name = response.full_name,
                    email = response.email,
                    phone = response.phone_number,
                    password = password,
                    gender = 1,
                    role = "user",
                    profile = R.drawable.default_avatar,
                    address = address
                )

                uiState.value = UiState.Success
                UserPref.updateUser(user = newUser)
                onRegistered()
            } catch (e: retrofit2.HttpException) {
                uiState.value = UiState.Error(error = Error.Network)
                val errorBody = e.response()?.errorBody()?.string() ?: "Không nhận được chi tiết lỗi"
                onRegistrationFailed("Đăng ký thất bại: HTTP ${e.code()} - $errorBody")
            } catch (e: Exception) {
                uiState.value = UiState.Error(error = Error.Network)
                onRegistrationFailed("Đăng ký thất bại: ${e.message}")
            }
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = "^[0-9]{10,12}$"
        return phone.matches(phoneRegex.toRegex())
    }

    fun sendOtp(
        email: String,
        onOtpSent: () -> Unit,
        onOtpFailed: (String) -> Unit
    ) {
        if (email.isBlank()) {
            onOtpFailed("Vui lòng nhập email!")
            return
        }

        if (!isValidEmail(email)) {
            onOtpFailed("Email không hợp lệ!")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // 1. Generate OTP (bỏ qua check email tồn tại)
                val otp = (100000..999999).random().toString()
                Log.d(TAG, "Generated OTP for $email: $otp")

                // 2. Send OTP via Resend API
                val sendResult = withContext(Dispatchers.IO) {
                    sendOtpViaResend(email, otp)
                }

                if (sendResult.isSuccess) {
                    // 3. Save OTP để xác thực sau này
                    saveOtpToPreferences(email, otp)
                    uiState.value = UiState.Success
                    onOtpSent()
                } else {
                    uiState.value = UiState.Error(error = Error.Network)
                    onOtpFailed(sendResult.exceptionOrNull()?.message ?: "Gửi OTP thất bại")
                }
            } catch (e: Exception) {
                uiState.value = UiState.Error(error = Error.Network)
                Log.e(TAG, "OTP sending failed", e)
                onOtpFailed("Lỗi hệ thống: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private suspend fun sendOtpViaResend(email: String, otp: String): Result<Unit> {
        return try {
            val resend = Resend(BuildConfig.RESEND_API_KEY)

            val htmlContent = """
                <h3>Xin chào,</h3>
                <p>Mã OTP của bạn để đặt lại mật khẩu là: <strong>$otp</strong></p>
                <p>Mã này có hiệu lực trong $OTP_EXPIRY_MINUTES phút.</p>
                <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                <p>Trân trọng,<br/>Đội ngũ Roffu</p>
            """.trimIndent()

            val request = SendEmailRequest.builder()
                .from("Roffu <onboarding@resend.dev>")
                .to(email)
                .subject("Mã OTP để đặt lại mật khẩu")
                .html(htmlContent)
                .build()

            val response = withContext(Dispatchers.IO) {
                resend.emails().send(request)
            }

            Log.d(TAG, "Resend response: ${response.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send OTP via Resend SDK", e)
            Result.failure(e)
        }
    }

    fun verifyOtp(
        email: String,
        otp: String,
        onOtpVerified: () -> Unit,
        onOtpFailed: (String) -> Unit
    ) {
        if (otp.isBlank()) {
            onOtpFailed("Vui lòng nhập mã OTP!")
            return
        }

        if (!isValidOtpFormat(otp)) {
            onOtpFailed("Mã OTP phải là 6 chữ số!")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // 1. Get stored OTP
                val storedOtp = withContext(Dispatchers.IO) {
                    context.dataStore.data.first()[stringPreferencesKey("otp_$email")]
                }

                if (storedOtp == null) {
                    uiState.value = UiState.Error(error = Error.Network)
                    onOtpFailed("Mã OTP đã hết hạn hoặc không tồn tại!")
                    return@launch
                }

                // 2. Verify OTP
                if (storedOtp == otp) {
                    // 3. Remove OTP after successful verification
                    context.dataStore.edit { it.remove(stringPreferencesKey("otp_$email")) }
                    uiState.value = UiState.Success
                    onOtpVerified()
                } else {
                    uiState.value = UiState.Error(error = Error.Network)
                    onOtpFailed("Mã OTP không đúng!")
                }
            } catch (e: Exception) {
                uiState.value = UiState.Error(error = Error.Network)
                onOtpFailed("Lỗi hệ thống: ${e.message}")
            }
        }
    }

    private suspend fun saveUserIdToPreferences(userId: Int) {
        context.dataStore.edit {
            it[LOGGED_USER_ID] = userId
        }
    }

    private suspend fun saveOtpToPreferences(email: String, otp: String) {
        context.dataStore.edit {
            it[stringPreferencesKey("otp_$email")] = otp
        }
    }

    // Validation helpers
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$"
        return password.matches(passwordRegex.toRegex())
    }

    private fun isValidOtpFormat(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }
}