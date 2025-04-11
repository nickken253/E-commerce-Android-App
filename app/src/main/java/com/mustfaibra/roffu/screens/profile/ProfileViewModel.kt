package com.mustfaibra.roffu.screens.profile


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.utils.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            userSessionManager.logout()
            onLoggedOut()
        }
    }
}