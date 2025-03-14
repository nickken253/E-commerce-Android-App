package dev.vstd.shoppingcart.common


sealed class UiStatus<T> {
    class Initial<T>: UiStatus<T>()
    class Loading<T>: UiStatus<T>()
    data class Success<T>(val data: T) : UiStatus<T>()
    data class Error<T>(var message: String): UiStatus<T>()
}