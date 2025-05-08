package com.mustfaibra.roffu.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.CustomInputField
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    email: String,
    viewModel: LoginViewModel = hiltViewModel(),
    onToastRequested: (message: String, color: Color) -> Unit,
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val uiState by remember { viewModel.uiState }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Đặt lại mật khẩu",
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = MaterialTheme.colors.onBackground
        )

        Image(
            painter = painterResource(id = R.drawable.help), // thay bằng đúng id ảnh
            contentDescription = "Reset illustration",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(180.dp)
                .background(Color.White) // hoặc `MaterialTheme.colors.background`
        )

        Spacer(modifier = Modifier.height(26.dp))



        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Mật khẩu mới *",
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    newPasswordError = null
                },
                placeholder = { Text("Nhập mật khẩu của bạn") },
                isError = newPasswordError != null,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                if (isPasswordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
                            ),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (newPasswordError != null) {
                Text(
                    text = newPasswordError ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Mật khẩu phải dài từ 8 đến 20 ký tự và chứa kết hợp các chữ cái tiếng Anh, số và ký tự đặc biệt.",
                    style = MaterialTheme.typography.caption.copy(color = Color.Gray),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Xác nhận Mật khẩu mới *",
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = {
                    confirmNewPassword = it
                    confirmPasswordError = null
                },
                placeholder = { Text("Nhập lại mật khẩu của bạn") },
                isError = confirmPasswordError != null,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                if (isConfirmPasswordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
                            ),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            confirmPasswordError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                var hasError = false

                if (newPassword.isBlank()) {
                    newPasswordError = "Vui lòng nhập mật khẩu"
                    hasError = true
                } else if (!isValidPassword(newPassword)) {
                    newPasswordError = "Mật khẩu không hợp lệ"
                    hasError = true
                }

                if (confirmNewPassword.isBlank()) {
                    confirmPasswordError = "Vui lòng xác nhận mật khẩu"
                    hasError = true
                } else if (confirmNewPassword != newPassword) {
                    confirmPasswordError = "Mật khẩu không trùng khớp"
                    hasError = true
                }

                if (!hasError) {
                    viewModel.resetPassword(
                        email = email,
                        newPassword = newPassword,
                        onSuccess = { message ->
                            // Hiển thị thông báo thành công
                            onToastRequested(message, Color.Green)
                            // Quay lại màn hình đăng nhập
                            navController.navigate("login")
                        },
                        onFailure = { errorMessage ->
                            onToastRequested(errorMessage, Color.Red)
                        }
                    )
                }
            },
            enabled = uiState !is UiState.Loading,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState is UiState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Lưu", style = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.SansSerif))
            }
        }
    }
}

// Regex kiểm tra mật khẩu hợp lệ
private fun isValidPassword(password: String): Boolean {
    val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,20}$"
    return password.matches(passwordRegex.toRegex())
}
