package com.mustfaibra.roffu.screens.profile


import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.utils.LOGGED_USER_ID
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.UserSessionManager
import com.mustfaibra.roffu.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val context: Context // Inject Context để truy cập DataStore và SharedPreferences
) : ViewModel() {
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
}