package dev.vstd.shoppingcart.auth

import dev.vstd.shoppingcart.auth.service.LoginResponseDto
import kotlinx.coroutines.flow.MutableStateFlow

object Session {
    var userEntity = MutableStateFlow<LoginResponseDto?>(null)
}