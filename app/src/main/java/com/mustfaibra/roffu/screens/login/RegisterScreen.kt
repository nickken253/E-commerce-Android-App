package com.mustfaibra.roffu.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.CustomInputField
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.LocationData
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManHinhDangKy(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onQuayLaiDangNhap: () -> Unit,
    onDangKyThanhCong: () -> Unit,
    onYeuCauToast: (String, Color) -> Unit,
) {
    val emailOrPhone by remember { loginViewModel.emailOrPhone }
    val matKhau by remember { loginViewModel.password }
    val trangThaiUi by remember { loginViewModel.uiState }

    val matKhauXacNhan = remember { mutableStateOf("") }
    val selectedProvince = remember { mutableStateOf("Thành phố") }
    val selectedDistrict = remember { mutableStateOf("") }
    val soNha = remember { mutableStateOf("") }
    val tenNguoiDung = remember { mutableStateOf("") }


    val categories = listOf(
        "Máy tính & laptop", "Đồ gia dụng", "Quần áo",
        "Làm đẹp và sức khỏe", "Đồ ăn", "Đồ chơi", "Khác"
    )
    val selectedCategory = remember { mutableStateOf<String?>(null) }

    var isProvinceDialogOpen by remember { mutableStateOf(false) }
    var isDistrictDialogOpen by remember { mutableStateOf(false) }

    val provinceList = LocationData.provinces.keys.toList()
    val districtList = LocationData.provinces[selectedProvince.value] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimension.pagePadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            ),
            modifier = Modifier.align(Alignment.Start)
        )
        // Tên người dùng
        CustomInputField(
            modifier = Modifier.fillMaxWidth(),
            value = tenNguoiDung.value,
            onValueChange = { tenNguoiDung.value = it },
            placeholder = "Tên của bạn",
            textStyle = MaterialTheme.typography.body1,
            padding = PaddingValues(Dimension.pagePadding),
            backgroundColor = MaterialTheme.colors.surface,
            textColor = MaterialTheme.colors.onBackground,
            imeAction = ImeAction.Next,
            shape = RoundedCornerShape(8.dp),
            onFocusChange = {},
            onKeyboardActionClicked = {},
        )


        // Email có @gmail.com
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                .padding(horizontal = Dimension.pagePadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomInputField(
                modifier = Modifier.weight(1f),
                value = emailOrPhone ?: "",
                onValueChange = { loginViewModel.updateEmailOrPhone(it.ifBlank { null }) },
                placeholder = "Nhập email",
                textStyle = MaterialTheme.typography.body1,
                imeAction = ImeAction.Next,
                backgroundColor = Color.Transparent,
                padding = PaddingValues(0.dp),
                shape = RoundedCornerShape(0.dp),
                textColor = MaterialTheme.colors.onBackground,
                onFocusChange = {},
                onKeyboardActionClicked = {},
                trailingIcon = null,
            )
            Text(text = "@gmail.com", style = MaterialTheme.typography.body1, color = MaterialTheme.colors.onBackground)
        }

        // Mật khẩu
        CustomInputField(
            modifier = Modifier.fillMaxWidth(),
            value = matKhau ?: "",
            onValueChange = { loginViewModel.updatePassword(it.ifBlank { null }) },
            placeholder = "Nhập mật khẩu của bạn",
            visualTransformation = PasswordVisualTransformation(),
            textStyle = MaterialTheme.typography.body1,
            padding = PaddingValues(Dimension.pagePadding),
            backgroundColor = MaterialTheme.colors.surface,
            textColor = MaterialTheme.colors.onBackground,
            imeAction = ImeAction.Next,
            shape = RoundedCornerShape(8.dp),
            onFocusChange = {},
            onKeyboardActionClicked = {},
        )

        // Xác nhận mật khẩu
        CustomInputField(
            modifier = Modifier.fillMaxWidth(),
            value = matKhauXacNhan.value,
            onValueChange = { matKhauXacNhan.value = it },
            placeholder = "Nhập lại mật khẩu của bạn",
            visualTransformation = PasswordVisualTransformation(),
            textStyle = MaterialTheme.typography.body1,
            padding = PaddingValues(Dimension.pagePadding),
            backgroundColor = MaterialTheme.colors.surface,
            textColor = MaterialTheme.colors.onBackground,
            imeAction = ImeAction.Next,
            shape = RoundedCornerShape(8.dp),
            onFocusChange = {},
            onKeyboardActionClicked = {},
        )

        // Thành phố + Change
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding)
        ) {
            CustomButton(
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(8.dp),
                padding = PaddingValues(Dimension.pagePadding.div(2)),
                buttonColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.onBackground,
                text = selectedProvince.value,
                enabled = false,
                textStyle = MaterialTheme.typography.body1,
                onButtonClicked = {},
            )
            CustomButton(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                padding = PaddingValues(Dimension.pagePadding.div(2)),
                buttonColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colors.primary,
                text = "Change",
                enabled = true,
                textStyle = MaterialTheme.typography.body1,
                onButtonClicked = {
                    isProvinceDialogOpen = true
                },
            )
        }

        // Quận huyện + chọn
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding)
        ) {
            CustomButton(
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(8.dp),
                padding = PaddingValues(Dimension.pagePadding.div(2)),
                buttonColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.onBackground,
                text = selectedDistrict.value.ifBlank { "Quận/Huyện" },
                enabled = false,
                textStyle = MaterialTheme.typography.body1,
                onButtonClicked = {},
            )
            CustomButton(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                padding = PaddingValues(Dimension.pagePadding.div(2)),
                buttonColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colors.primary,
                text = "Change",
                enabled = true,
                textStyle = MaterialTheme.typography.body1,
                onButtonClicked = {
                    isDistrictDialogOpen = true
                },
            )
        }

        // Số nhà
        CustomInputField(
            modifier = Modifier.fillMaxWidth(),
            value = soNha.value,
            onValueChange = { soNha.value = it },
            placeholder = "Tên đường/Số nhà",
            textStyle = MaterialTheme.typography.body1,
            padding = PaddingValues(Dimension.pagePadding),
            backgroundColor = MaterialTheme.colors.surface,
            textColor = MaterialTheme.colors.onBackground,
            imeAction = ImeAction.Next,
            shape = RoundedCornerShape(8.dp),
            onFocusChange = {},
            onKeyboardActionClicked = {},
        )

        // Danh mục
        Text("Bạn muốn tìm kiếm cái gì?", style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium))
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            categories.forEach { category ->
                val selected = selectedCategory.value == category
                CustomButton(
                    shape = RoundedCornerShape(50),
                    padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    buttonColor = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface,
                    contentColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground,
                    text = category,
                    textStyle = MaterialTheme.typography.caption,
                    onButtonClicked = {
                        selectedCategory.value = if (selected) null else category
                    },
                )
            }
        }

        // Đăng ký
        CustomButton(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            padding = PaddingValues(Dimension.pagePadding.div(2)),
            buttonColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            text = "Next",
            enabled = trangThaiUi !is UiState.Loading,
            textStyle = MaterialTheme.typography.button,
            onButtonClicked = {
                val rawEmail = emailOrPhone ?: ""
                val fullEmail = "$rawEmail@gmail.com"

                if (tenNguoiDung.value.isBlank() || rawEmail.isBlank() || (matKhau ?: "").isBlank() || matKhauXacNhan.value.isBlank()
                    || selectedProvince.value == "Thành phố" || selectedDistrict.value.isBlank() || soNha.value.isBlank()
                ) {
                    onYeuCauToast("Vui lòng điền đầy đủ thông tin!", Color.Red)
                    return@CustomButton
                }


                if (matKhau != matKhauXacNhan.value) {
                    onYeuCauToast("Mật khẩu xác nhận không khớp!", Color.Red)
                    return@CustomButton
                }

                loginViewModel.registerUser(
                    email = fullEmail,
                    password = matKhau ?: "",
                    confirmPassword = matKhauXacNhan.value,
                    name = tenNguoiDung.value,
                    address = "${soNha.value}, ${selectedDistrict.value}, ${selectedProvince.value}",
                    onRegistered = {
                        onYeuCauToast("Register successful!", Color.Green)
                        onDangKyThanhCong()
                    },
                    onRegistrationFailed = { loi ->
                        onYeuCauToast(loi, Color.Red)
                    }
                )

            },
            leadingIcon = {
                if (trangThaiUi is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = Dimension.pagePadding)
                            .size(Dimension.smIcon),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = Dimension.xs
                    )
                }
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = "Next",
                    tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.size(Dimension.smIcon)
                )
            }
        )
    }

    // Hộp thoại chọn tỉnh
    if (isProvinceDialogOpen) {
        Dialog(onDismissRequest = { isProvinceDialogOpen = false }) {
            Surface(shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Chọn Tỉnh / Thành phố", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    provinceList.forEach {
                        TextButton(onClick = {
                            selectedProvince.value = it
                            selectedDistrict.value = ""
                            isProvinceDialogOpen = false
                        }) {
                            Text(it)
                        }
                    }
                }
            }
        }
    }

    // Hộp thoại chọn quận/huyện
    if (isDistrictDialogOpen) {
        Dialog(onDismissRequest = { isDistrictDialogOpen = false }) {
            Surface(shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Chọn Quận / Huyện", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    districtList.forEach {
                        TextButton(onClick = {
                            selectedDistrict.value = it
                            isDistrictDialogOpen = false
                        }) {
                            Text(it)
                        }
                    }
                }
            }
        }
    }
}
