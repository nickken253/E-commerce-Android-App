package com.mustfaibra.roffu.screens.login


import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.LOGGED_USER_ID
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
@SuppressLint("StaticFieldLeak")
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val context: Context,
) : ViewModel() {
    val uiState = mutableStateOf<UiState>(UiState.Idle)
    val emailOrPhone = mutableStateOf<String?>("mustfaibra@gmail.com")
    val password = mutableStateOf<String?>("12344321")

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
        onAuthenticationFailed: () -> Unit,
    ) {
        if (emailOrPhone.isBlank() || password.isBlank()) onAuthenticationFailed()
        else {
            uiState.value = UiState.Loading
            /** We will use the coroutine so that we don't block our dear : The UiThread */
            viewModelScope.launch {
                delay(3000)
                userRepository.signInUser(
                    email = emailOrPhone,
                    password = password,
                ).let {
                    when (it) {
                        is DataResponse.Success -> {
                            it.data?.let { user ->
                                /** Authenticated successfully */
                                uiState.value = UiState.Success
                                UserPref.updateUser(user = user)
                                /** save user id */
                                saveUserIdToPreferences(userId = user.userId)
                                onAuthenticated()
                            }
                        }
                        is DataResponse.Error -> {
                            /** An error occurred while authenticating */
                            uiState.value = UiState.Error(error = it.error ?: Error.Network)
                            onAuthenticationFailed()
                        }
                    }
                }
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
        // Kiểm tra dữ liệu đầu vào
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || name.isBlank()) {
            onRegistrationFailed("Vui lòng điền đầy đủ thông tin!")
            return
        }

        if (password != confirmPassword) {
            onRegistrationFailed("Mật khẩu xác nhận không khớp!")
            return
        }

        // Kiểm tra định dạng mật khẩu (8-20 ký tự, chứa chữ hoa, chữ thường, số, ký tự đặc biệt)
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
        if (!password.matches(passwordRegex.toRegex())) {
            onRegistrationFailed("Mật khẩu phải dài 8-20 ký tự, chứa chữ hoa, chữ thường, số và ký tự đặc biệt!")
            return
        }

        // Kiểm tra định dạng email
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        if (!email.matches(emailRegex.toRegex())) {
            onRegistrationFailed("Email không hợp lệ!")
            return
        }

        // Gọi repository để đăng ký
        uiState.value = UiState.Loading
        viewModelScope.launch {
            delay(2000) // Giả lập thời gian xử lý
            val newUser = User(
                userId = 0,
                name = name,
                email = email,
                phone = "",
                password = password,
                gender = 1,
                role = "user",
                profile = R.drawable.mustapha_profile,
                address = address
            )

            userRepository.registerUser(user = newUser).let { response ->
                when (response) {
                    is DataResponse.Success -> {
                        response.data?.let { user ->
                            uiState.value = UiState.Success
                            UserPref.updateUser(user = user)
                            //saveUserIdToPreferences(userId = user.userId)
                            onRegistered()
                        }
                    }
                    is DataResponse.Error -> {
                        uiState.value = UiState.Error(error = response.error ?: Error.Network)
                        onRegistrationFailed("Đăng ký thất bại: ${response.error?.message}")
                    }
                }
            }
        }
    }


    private suspend fun saveUserIdToPreferences(userId: Int) {
        context.dataStore.edit {
            it[LOGGED_USER_ID] = userId
        }
    }
}