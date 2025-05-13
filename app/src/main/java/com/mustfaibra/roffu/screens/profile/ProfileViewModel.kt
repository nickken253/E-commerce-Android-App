package com.mustfaibra.roffu.screens.profile


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.VirtualCard
import com.mustfaibra.roffu.repositories.UserRepository
import com.mustfaibra.roffu.utils.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
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
    private val _virtualCard = MutableStateFlow<VirtualCard?>(null)
    val virtualCard: StateFlow<VirtualCard?> = _virtualCard
    val isVirtualCardAdded = virtualCard.map { it != null }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            userSessionManager.logout()
            onLoggedOut()
        }
    }

    fun loadVirtualCard(userId: Int) {
        viewModelScope.launch {
            _virtualCard.value = userRepository.getVirtualCardByUser(userId)
        }
    }

    fun addVirtualCard(card: VirtualCard) {
        viewModelScope.launch {
            userRepository.addVirtualCard(card)
            _virtualCard.value = card
        }
    }
}