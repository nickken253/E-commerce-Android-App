package dev.vstd.shoppingcart.auth.ui

object SignUpValidator {
    fun validate(username: String, email: String, password: String, confirmPassword: String): Result {
        val result: Result
        when {
            username.isEmpty() -> {
                result = Result("Username is required")
            }
            email.isEmpty() -> {
                result = Result("Email is required")
            }
            password.isEmpty() -> {
                result = Result("Password is required")
            }
            confirmPassword.isEmpty() -> {
                result = Result("Confirm password is required")
            }
            password != confirmPassword -> {
                result = Result("Password and confirm password do not match")
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