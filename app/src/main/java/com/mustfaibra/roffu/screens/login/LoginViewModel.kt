package com.mustfaibra.roffu.screens.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.mustfaibra.roffu.BuildConfig
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.models.dto.LoginRequest
import com.mustfaibra.roffu.repositories.UserRepository
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
    val googleEmail = mutableStateOf<String?>(null)

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

    fun startGoogleSignIn(context: Context, onIntentReady: (Intent) -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        // Đăng xuất tài khoản cũ để buộc chọn lại tài khoản
        viewModelScope.launch {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                onIntentReady(signInIntent)
            }
        }
    }


    fun authenticateUser(
        username: String,
        password: String,
        onAuthenticated: () -> Unit,
        onAuthenticationFailed: (String) -> Unit,
        isGoogleLogin: Boolean = false
    ) {
        if (!isGoogleLogin && (username.isBlank() || password.isBlank())) {
            onAuthenticationFailed("Vui lòng nhập đầy đủ thông tin")
            return
        }

        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // Call login API
                val request = LoginRequest(username = username, password = password)
                val loginResponse = RetrofitClient.authApi.login(request)

                // Save access_token
                UserPref.updateUserToken(context, loginResponse.access_token)

                // Get user profile
                val userResponse = RetrofitClient.authApi.getUserProfile("Bearer ${loginResponse.access_token}")

                val user = User(
                    userId = userResponse.id,
                    name = userResponse.full_name,
                    email = userResponse.email,
                    phone = userResponse.phone_number,
                    password = password,
                    gender = 1,
                    role = "user",
                    profile = R.drawable.adidas_48,
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
                    401 -> if (isGoogleLogin) "Tài khoản chưa được đăng ký" else "Tên đăng nhập hoặc mật khẩu không đúng"
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
            context.dataStore.edit {
                it.remove(LOGGED_USER_ID)
            }
            context.dataStore.data.first().let {
                Log.d("DataStore", "LOGGED_USER_ID after logout: ${it[LOGGED_USER_ID]}")
            }
            UserPref.logout(context)
            username.value = null
            emailOrPhone.value = ""
            password.value = ""
            uiState.value = UiState.Idle
        }
    }

    private fun isValidOtpFormat(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
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

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$"
        return password.matches(passwordRegex.toRegex())
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = "^[0-9]{10,12}$"
        return phone.matches(phoneRegex.toRegex())
    }
}