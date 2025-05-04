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
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.LOGGED_USER_ID
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.dataStore
import com.resend.Resend
import com.resend.*
import com.resend.services.emails.model.SendEmailRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    companion object {
        private const val TAG = "LoginViewModel"
        private const val OTP_EXPIRY_MINUTES = 5
    }

    fun updateEmailOrPhone(value: String?) {
        this.emailOrPhone.value = value
    }

    fun updatePassword(value: String?) {
        this.password.value = value
    }

    fun authenticateUser(
        emailOrPhone: String,
        password: String,
        onAuthenticated: () -> Unit,
        onAuthenticationFailed: (String) -> Unit,
    ) {
        if (emailOrPhone.isBlank() || password.isBlank()) {
            onAuthenticationFailed("Vui lòng nhập đầy đủ thông tin")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                delay(1000) // Simulate network delay

                userRepository.signInUser(
                    email = emailOrPhone,
                    password = password,
                ).let { response ->
                    when (response) {
                        is DataResponse.Success -> {
                            response.data?.let { user ->
                                uiState.value = UiState.Success
                                UserPref.updateUser(user = user)
                                saveUserIdToPreferences(userId = user.userId)
                                onAuthenticated()
                            } ?: run {
                                onAuthenticationFailed("Không nhận được thông tin người dùng")
                            }
                        }
                        is DataResponse.Error -> {
                            uiState.value = UiState.Error(error = response.error ?: Error.Network)
                            onAuthenticationFailed((response.error?.message ?: "Lỗi đăng nhập không xác định").toString())
                        }
                    }
                }
            } catch (e: Exception) {
                uiState.value = UiState.Error(error = Error.Network)
                onAuthenticationFailed("Lỗi hệ thống: ${e.message}")
            }
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
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (email.isBlank() || newPassword.isBlank()) {
            onFailure("Vui lòng điền đầy đủ thông tin!")
            return
        }

        if (!isValidEmail(email)) {
            onFailure("Email không hợp lệ!")
            return
        }

        if (!isValidPassword(newPassword)) {
            onFailure("Mật khẩu phải dài 8-20 ký tự, chứa chữ cái, số và ký tự đặc biệt!")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                delay(2000) // Simulate network delay

                userRepository.resetPassword(email, newPassword).let { response ->
                    when (response) {
                        is DataResponse.Success -> {
                            uiState.value = UiState.Success
                            onSuccess()
                        }
                        is DataResponse.Error -> {
                            uiState.value = UiState.Error(error = response.error ?: Error.Network)
                            onFailure("Đặt lại mật khẩu thất bại: ${response.error?.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                uiState.value = UiState.Error(error = Error.Network)
                onFailure("Lỗi hệ thống: ${e.message}")
            }
        }
    }

    fun registerUser(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        address: String,
        onRegistered: () -> Unit,
        onRegistrationFailed: (String) -> Unit,
    ) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || name.isBlank()) {
            onRegistrationFailed("Vui lòng điền đầy đủ thông tin!")
            return
        }

        if (password != confirmPassword) {
            onRegistrationFailed("Mật khẩu xác nhận không khớp!")
            return
        }

        if (!isValidPassword(password)) {
            onRegistrationFailed("Mật khẩu phải dài 8-20 ký tự, chứa chữ hoa, chữ thường, số và ký tự đặc biệt!")
            return
        }

        if (!isValidEmail(email)) {
            onRegistrationFailed("Email không hợp lệ!")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                delay(2000) // Simulate network delay

                val newUser = User(
                    userId = 0,
                    name = name,
                    email = email,
                    phone = "",
                    password = password,
                    gender = 1,
                    role = "user",
                    profile = R.drawable.default_avatar,
                    address = address
                )

                userRepository.registerUser(user = newUser).let { response ->
                    when (response) {
                        is DataResponse.Success -> {
                            response.data?.let { user ->
                                uiState.value = UiState.Success
                                UserPref.updateUser(user = user)
                                onRegistered()
                            } ?: run {
                                onRegistrationFailed("Không nhận được thông tin người dùng sau đăng ký")
                            }
                        }
                        is DataResponse.Error -> {
                            uiState.value = UiState.Error(error = response.error ?: Error.Network)
                            onRegistrationFailed("Đăng ký thất bại: ${response.error?.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                uiState.value = UiState.Error(error = Error.Network)
                onRegistrationFailed("Lỗi hệ thống: ${e.message}")
            }
        }
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
                // 1. Check if email exists
                val emailResponse = userRepository.checkEmailExists(email)

                when (emailResponse) {
                    is DataResponse.Success -> {
                        if (emailResponse.data != true) {
                            uiState.value = UiState.Error(error = Error.Network)
                            onOtpFailed("Không tìm thấy tài khoản với email này!")
                            return@launch
                        }

                        // 2. Generate OTP
                        val otp = (100000..999999).random().toString()
                        Log.d(TAG, "Generated OTP for $email: $otp")

                        // 3. Send OTP via Resend API
                        val sendResult = withContext(Dispatchers.IO) {
                            sendOtpViaResend(email, otp)
                        }

                        if (sendResult.isSuccess) {
                            // 4. Save OTP with timestamp
                            saveOtpToPreferences(email, otp)
                            uiState.value = UiState.Success
                            onOtpSent()
                        } else {
                            uiState.value = UiState.Error(error = Error.Network)
                            onOtpFailed(sendResult.exceptionOrNull()?.message ?: "Gửi OTP thất bại")
                        }
                    }
                    is DataResponse.Error -> {
                        uiState.value = UiState.Error(error = emailResponse.error ?: Error.Network)
                        onOtpFailed("Lỗi khi kiểm tra email: ${emailResponse.error?.message}")
                    }
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