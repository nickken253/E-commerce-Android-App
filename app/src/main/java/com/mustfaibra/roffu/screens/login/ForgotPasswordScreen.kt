package com.mustfaibra.roffu.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.CustomInputField
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import kotlinx.coroutines.launch
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
    onToastRequested: (message: String, color: Color) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    val uiState by remember { viewModel.uiState }
    var emailError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimension.pagePadding)
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Đặt lại mật khẩu",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.anh_minh_hoa),
            contentDescription = "Forgot Password Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )


        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Email *",
            style = MaterialTheme.typography.body2.copy(
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        CustomInputField(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp)),
            value = email,
            onValueChange = { email = it },
            placeholder = "Nhập email của bạn",
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
            padding = PaddingValues(Dimension.pagePadding),
            backgroundColor = MaterialTheme.colors.surface,
            textColor = MaterialTheme.colors.onBackground,
            imeAction = ImeAction.Done,
            shape = RoundedCornerShape(8.dp),
            onFocusChange = {},
            onKeyboardActionClicked = {}
        )

        emailError?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tìm tài khoản của bạn bằng cách xác minh email của bạn",
                        style = MaterialTheme.typography.body2.copy(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Xác minh OTP của bạn bằng cách sử dụng điện thoại di động của bạn",
                        style = MaterialTheme.typography.caption.copy(
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
                TextButton(onClick = { /* Add your OTP navigation */ }) {
                    Text("Xác minh", color = MaterialTheme.colors.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Quay lại Đăng nhập",
                    style = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
                )
            }

            Button(
                onClick = {
                    if (email.isBlank()) {
                        emailError = "Vui lòng nhập email!"
                        return@Button
                    }
                    coroutineScope.launch {
                        viewModel.sendOtp(
                            email = email,
                            onOtpSent = {
                                navController.navigate("otp-verification/$email") {
                                    popUpTo("forgot-password") { inclusive = false }
                                }
                            },
                            onOtpFailed = { errorMessage ->
                                emailError = errorMessage
                                onToastRequested(errorMessage, Color.Red)
                            }
                        )
                    }
                },
                enabled = uiState !is UiState.Loading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Tiếp tục",
                        style = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
                    )
                }
            }
        }
    }
}
