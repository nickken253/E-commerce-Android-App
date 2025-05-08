package com.mustfaibra.roffu.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartEventBus @Inject constructor() {
    private val _cartEvents = MutableSharedFlow<Unit>(replay = 1)
    val cartEvents: SharedFlow<Unit> = _cartEvents.asSharedFlow()

    fun notifyCartChanged() {
        _cartEvents.tryEmit(Unit)
    }
}