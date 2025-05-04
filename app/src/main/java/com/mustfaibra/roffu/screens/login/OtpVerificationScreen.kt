package com.mustfaibra.roffu.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.CustomInputField
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import kotlinx.coroutines.launch
@Composable
fun OtpVerificationScreen(
    navController: NavController,
    email: String,
    viewModel: LoginViewModel = hiltViewModel(),
    onToastRequested: (message: String, color: Color) -> Unit,
) {
    var otp by remember { mutableStateOf("") }
    val uiState by remember { viewModel.uiState }
    var otpError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Send OTP to the email
            viewModel.sendOtp(
                email = email,
                onOtpSent = {
                    onToastRequested("Mã OTP đã được gửi đến $email", Color.Green)
                },
                onOtpFailed = { error ->
                    onToastRequested(error, Color.Red)
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimension.pagePadding)
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Xác minh OTP",
            style = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.height(Dimension.pagePadding))

        // Description
        Text(
            text = "Vui lòng nhập mã OTP đã được gửi đến $email",
            style = MaterialTheme.typography.body2.copy(
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.padding(horizontal = Dimension.pagePadding)
        )

        Spacer(modifier = Modifier.height(Dimension.pagePadding))

        // OTP Input Field
        CustomInputField(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(8.dp)
                ),
            value = otp,
            onValueChange = { otp = it },
            placeholder = "Nhập mã OTP",
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
            padding = PaddingValues(Dimension.pagePadding),
            backgroundColor = MaterialTheme.colors.surface,
            textColor = MaterialTheme.colors.onBackground,
            imeAction = ImeAction.Done,
            shape = RoundedCornerShape(8.dp),
            onFocusChange = {},
            onKeyboardActionClicked = {}
        )

        // OTP Error
        otpError?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimension.pagePadding.times(2)))

        // Verify Button
        CustomButton(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            padding = PaddingValues(Dimension.pagePadding.div(2)),
            buttonColor = MaterialTheme.colors.primary,
            contentColor = Color.White,
            text = "Xác minh",
            enabled = uiState !is UiState.Loading,
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
            onButtonClicked = {
                if (otp.isBlank()) {
                    otpError = "Vui lòng nhập mã OTP"
                    return@CustomButton
                }

                viewModel.verifyOtp(
                    email = email,
                    otp = otp,
                    onOtpVerified = {
                        navController.navigate("reset-password/$email") {
                            popUpTo("otp-verification") { inclusive = true }
                        }
                    },
                    onOtpFailed = { error ->
                        otpError = error
                        onToastRequested(error, Color.Red)
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

        Spacer(modifier = Modifier.height(Dimension.pagePadding))

        // Retry OTP Button
        Text(
            text = "Gửi lại mã OTP",
            style = MaterialTheme.typography.caption.copy(
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.clickable {
                coroutineScope.launch {
                    viewModel.sendOtp(
                        email = email,
                        onOtpSent = {
                            onToastRequested("Đã gửi lại mã OTP đến $email", Color.Green)
                        },
                        onOtpFailed = { error ->
                            onToastRequested(error, Color.Red)
                        }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimension.pagePadding))

        // Back Button
        Text(
            text = "Quay lại",
            style = MaterialTheme.typography.caption.copy(
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.clickable { navController.popBackStack() }
        )
    }
}
