package com.mustfaibra.roffu.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.CustomInputField
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onUserAuthenticated: () -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val uiState by remember { loginViewModel.uiState }
    val emailOrPhone by remember { loginViewModel.emailOrPhone }
    val password by remember { loginViewModel.password }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    val forgotPasswordEmail = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmNewPassword = remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf<String?>(null) }
    val newPasswordError = remember { mutableStateOf<String?>(null) }
    val confirmPasswordError = remember { mutableStateOf<String?>(null) }

    // Hàm kiểm tra mật khẩu hợp lệ
    fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$"
        return password.matches(passwordRegex.toRegex())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        /** Logo ứng dụng */
        Text(
            text = "Mapa.",
            style = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.height(Dimension.pagePadding.times(2)))

        /** Trường Email */
        CustomInputField(
            modifier = Modifier
                .shadow(Dimension.elevation, MaterialTheme.shapes.large)
                .fillMaxWidth(),
            value = emailOrPhone ?: "",
            onValueChange = { loginViewModel.updateEmailOrPhone(it.ifBlank { null }) },
            placeholder = "Nhập Email của bạn",
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
            padding = PaddingValues(Dimension.pagePadding, Dimension.pagePadding.times(0.7f)),
            backgroundColor = MaterialTheme.colors.surface,
            textColor = MaterialTheme.colors.onBackground,
            imeAction = ImeAction.Next,
            shape = MaterialTheme.shapes.large,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile_empty),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier
                        .padding(end = Dimension.pagePadding.div(2))
                        .size(Dimension.mdIcon.times(0.7f))
                )
            },
            onFocusChange = {},
            onKeyboardActionClicked = {},
        )

        Spacer(modifier = Modifier.height(Dimension.pagePadding))

        /** Trường mật khẩu */
        CustomInputField(
            modifier = Modifier
                .shadow(Dimension.elevation, MaterialTheme.shapes.large)
                .fillMaxWidth(),
            value = password ?: "",
            onValueChange = { loginViewModel.updatePassword(it.ifBlank { null }) },
            placeholder = "Nhập mật khẩu của bạn",
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
            padding = PaddingValues(Dimension.pagePadding, Dimension.pagePadding.times(0.7f)),
            backgroundColor = MaterialTheme.colors.surface,
            textColor = MaterialTheme.colors.onBackground,
            imeAction = ImeAction.Done,
            shape = MaterialTheme.shapes.large,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier
                        .padding(end = Dimension.pagePadding.div(2))
                        .size(Dimension.mdIcon.times(0.7f))
                )
            },
            onFocusChange = {},
            onKeyboardActionClicked = {},
        )

        /** Nút Đăng nhập */
        Spacer(modifier = Modifier.height(Dimension.pagePadding))
        CustomButton(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            padding = PaddingValues(Dimension.pagePadding.div(2)),
            buttonColor = Color(0xFF0050D8),
            contentColor = Color.White,
            text = "Đăng nhập",
            enabled = uiState !is UiState.Loading,
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
            onButtonClicked = {
                loginViewModel.authenticateUser(
                    emailOrPhone ?: "",
                    password ?: "",
                    onAuthenticated = { onUserAuthenticated() },
                    onAuthenticationFailed = {
                        onToastRequested("Vui lòng kiểm tra thông tin đăng nhập!", Color.Red)
                    }
                )
            },
            leadingIcon = {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = Dimension.pagePadding)
                            .size(Dimension.smIcon),
                        color = Color.White,
                        strokeWidth = Dimension.xs
                    )
                }
            }
        )

        /** Nút Đăng ký */
        Spacer(modifier = Modifier.height(Dimension.pagePadding.div(2)))
        CustomButton(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            padding = PaddingValues(Dimension.pagePadding.div(2)),
            buttonColor = Color.White,
            contentColor = MaterialTheme.colors.primary,
            text = "Đăng ký",
            enabled = uiState !is UiState.Loading,
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
            onButtonClicked = onNavigateToRegister
        )

        /** Quên mật khẩu */
        Spacer(modifier = Modifier.height(Dimension.pagePadding.div(1.5f)))
        Text(
            text = "Quên mật khẩu?",
            style = MaterialTheme.typography.caption.copy(
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.clickable { showForgotPasswordDialog = true }
        )

        /** Divider + “Hoặc kết nối với” */
        Spacer(modifier = Modifier.height(Dimension.pagePadding))
        Divider()
        Text(
            text = "Hoặc kết nối với",
            modifier = Modifier.padding(vertical = Dimension.pagePadding.div(2)),
            style = MaterialTheme.typography.caption.copy(
                color = Color.Gray,
                fontFamily = FontFamily.SansSerif
            )
        )

        /** Icon mạng xã hội */
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                R.drawable.ic_apple,
                R.drawable.ic_google,
                R.drawable.ic_facebook
            ).forEach { iconId ->
                DrawableButton(
                    paddingValue = PaddingValues(Dimension.sm),
                    elevation = Dimension.elevation,
                    painter = painterResource(id = iconId),
                    onButtonClicked = { /* TODO: handle auth */ },
                    backgroundColor = MaterialTheme.colors.background,
                    shape = MaterialTheme.shapes.medium,
                    iconSize = Dimension.mdIcon.times(0.8f),
                )
            }
        }
    }

    // Dialog nhập email để quên mật khẩu
    if (showForgotPasswordDialog) {
        Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quên mật khẩu",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.background, RoundedCornerShape(8.dp))
                            .border(
                                width = if (emailError.value != null) 1.dp else 0.dp,
                                color = if (emailError.value != null) Color.Red else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = Dimension.pagePadding, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CustomInputField(
                            modifier = Modifier.weight(1f),
                            value = forgotPasswordEmail.value,
                            onValueChange = {
                                forgotPasswordEmail.value = it
                                emailError.value = null
                            },
                            placeholder = "Nhập email của bạn",
                            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                            imeAction = ImeAction.Next,
                            backgroundColor = Color.Transparent,
                            padding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(0.dp),
                            textColor = MaterialTheme.colors.onBackground,
                            onFocusChange = {},
                            onKeyboardActionClicked = {},
                            trailingIcon = null,
                        )
                        Text(
                            text = "@gmail.com",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp,
                                color = MaterialTheme.colors.onBackground
                            )
                        )
                    }
                    emailError.value?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding)
                    ) {
                        CustomButton(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            padding = PaddingValues(Dimension.pagePadding.div(2)),
                            buttonColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colors.primary,
                            text = "Hủy",
                            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                            onButtonClicked = { showForgotPasswordDialog = false }
                        )
                        CustomButton(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            padding = PaddingValues(Dimension.pagePadding.div(2)),
                            buttonColor = MaterialTheme.colors.primary,
                            contentColor = Color.White,
                            text = "Tiếp tục",
                            enabled = uiState !is UiState.Loading,
                            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                            onButtonClicked = {
                                loginViewModel.checkEmailForPasswordReset(
                                    email = "${forgotPasswordEmail.value}@gmail.com",
                                    onEmailExists = {
                                        showForgotPasswordDialog = false
                                        showResetPasswordDialog = true
                                    },
                                    onEmailNotFound = { error ->
                                        emailError.value = error
                                    }
                                )
                            },
                            leadingIcon = {
                                if (uiState is UiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = Dimension.pagePadding)
                                            .size(Dimension.smIcon),
                                        color = Color.White,
                                        strokeWidth = Dimension.xs
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog nhập mật khẩu mới
    if (showResetPasswordDialog) {
        Dialog(onDismissRequest = { showResetPasswordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đặt lại mật khẩu",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "Mật khẩu mới *",
                            style = MaterialTheme.typography.body2.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.onBackground,
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CustomInputField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (newPasswordError.value != null) 1.dp else 0.dp,
                                    color = if (newPasswordError.value != null) Color.Red else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            value = newPassword.value,
                            onValueChange = {
                                newPassword.value = it
                                newPasswordError.value = null
                            },
                            placeholder = "Nhập mật khẩu mới",
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                            padding = PaddingValues(Dimension.pagePadding),
                            backgroundColor = MaterialTheme.colors.background,
                            textColor = MaterialTheme.colors.onBackground,
                            imeAction = ImeAction.Next,
                            shape = RoundedCornerShape(8.dp),
                            onFocusChange = {},
                            onKeyboardActionClicked = {},
                        )
                        newPasswordError.value?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "Xác nhận mật khẩu mới *",
                            style = MaterialTheme.typography.body2.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.onBackground,
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CustomInputField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (confirmPasswordError.value != null) 1.dp else 0.dp,
                                    color = if (confirmPasswordError.value != null) Color.Red else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            value = confirmNewPassword.value,
                            onValueChange = {
                                confirmNewPassword.value = it
                                confirmPasswordError.value = null
                            },
                            placeholder = "Nhập lại mật khẩu mới",
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                            padding = PaddingValues(Dimension.pagePadding),
                            backgroundColor = MaterialTheme.colors.background,
                            textColor = MaterialTheme.colors.onBackground,
                            imeAction = ImeAction.Done,
                            shape = RoundedCornerShape(8.dp),
                            onFocusChange = {},
                            onKeyboardActionClicked = {},
                        )
                        confirmPasswordError.value?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding)
                    ) {
                        CustomButton(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            padding = PaddingValues(Dimension.pagePadding.div(2)),
                            buttonColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colors.primary,
                            text = "Hủy",
                            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                            onButtonClicked = { showResetPasswordDialog = false }
                        )
                        CustomButton(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            padding = PaddingValues(Dimension.pagePadding.div(2)),
                            buttonColor = MaterialTheme.colors.primary,
                            contentColor = Color.White,
                            text = "Xác nhận",
                            enabled = uiState !is UiState.Loading,
                            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                            onButtonClicked = {
                                var hasError = false
                                if (newPassword.value.isBlank()) {
                                    newPasswordError.value = "Trường này là bắt buộc"
                                    hasError = true
                                } else if (!isValidPassword(newPassword.value)) {
                                    newPasswordError.value = "Mật khẩu dài từ 8-20 ký tự và chứa kết hợp các chữ cái tiếng Anh, số và ký hiệu đặc biệt"
                                    hasError = true
                                } else {
                                    newPasswordError.value = null
                                }

                                if (confirmNewPassword.value.isBlank()) {
                                    confirmPasswordError.value = "Trường này là bắt buộc"
                                    hasError = true
                                } else if (confirmNewPassword.value != newPassword.value) {
                                    confirmPasswordError.value = "Mật khẩu không trùng khớp"
                                    hasError = true
                                } else {
                                    confirmPasswordError.value = null
                                }

                                if (!hasError) {
                                    loginViewModel.resetPassword(
                                        email = "${forgotPasswordEmail.value}@gmail.com",
                                        newPassword = newPassword.value,
                                        onSuccess = {
                                            onToastRequested("Đặt lại mật khẩu thành công!", Color.Green)
                                            showResetPasswordDialog = false
                                            forgotPasswordEmail.value = ""
                                            newPassword.value = ""
                                            confirmNewPassword.value = ""
                                        },
                                        onFailure = { error ->
                                            onToastRequested(error, Color.Red)
                                        }
                                    )
                                }
                            },
                            leadingIcon = {
                                if (uiState is UiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = Dimension.pagePadding)
                                            .size(Dimension.smIcon),
                                        color = Color.White,
                                        strokeWidth = Dimension.xs
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}