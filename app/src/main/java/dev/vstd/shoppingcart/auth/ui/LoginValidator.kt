package dev.vstd.shoppingcart.auth.ui

object LoginValidator {
    fun validate(email: String, password: String): Result {
        val result: Result
        when {
            email.isEmpty() -> {
                result = Result("Email is required")
            }

            password.isEmpty() -> {
                result = Result("Password is required")
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                result = Result("Invalid email address")
            }

            password.length < 6 -> {
                result = Result("Password must be at least 6 characters long")
            }

            else -> {
                result = Result("Success", true)
            }
        }
        return result
    }

    data class Result(
        val message: String,
        val success: Boolean = false,
    )
}