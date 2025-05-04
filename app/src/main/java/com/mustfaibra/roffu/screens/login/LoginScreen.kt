package com.mustfaibra.roffu.screens.login

import androidx.compose.foundation.Image
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.CustomInputField
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension

@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    onUserAuthenticated: () -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val uiState by remember { loginViewModel.uiState }
    val emailOrPhone by remember { loginViewModel.emailOrPhone }
    val password by remember { loginViewModel.password }

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
            modifier = Modifier.clickable {
                navController.navigate("forgot-password")
            }
        )

        /** Divider + "Hoặc kết nối với" */
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
}