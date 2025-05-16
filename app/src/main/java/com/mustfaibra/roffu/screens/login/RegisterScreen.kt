package com.mustfaibra.roffu.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.CustomInputField
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.LocationData
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManHinhDangKy(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onQuayLaiDangNhap: () -> Unit,
    onDangKyThanhCong: () -> Unit,
    onYeuCauToast: (String, Color) -> Unit,
) {
    // Sử dụng googleEmail từ ViewModel để điền sẵn email
    val googleEmail by remember { loginViewModel.googleEmail }
    val emailOrPhone by remember { loginViewModel.emailOrPhone }
    val matKhau by remember { loginViewModel.password }
    val trangThaiUi by remember { loginViewModel.uiState }

    // Khởi tạo email với googleEmail nếu có, nếu không thì dùng emailOrPhone
    val initialEmail = googleEmail?.removeSuffix("@gmail.com") ?: emailOrPhone ?: ""
    val emailState = remember { mutableStateOf(initialEmail) }

    // Cập nhật emailOrPhone trong ViewModel khi màn hình được tạo
    LaunchedEffect(googleEmail) {
        if (googleEmail != null) {
            loginViewModel.updateEmailOrPhone(googleEmail!!.removeSuffix("@gmail.com"))
            emailState.value = googleEmail!!.removeSuffix("@gmail.com")
        }
    }

    val matKhauXacNhan = remember { mutableStateOf("") }
    val selectedProvince = remember { mutableStateOf("Chọn tỉnh/thành phố") }
    val selectedDistrict = remember { mutableStateOf("") }
    val soNha = remember { mutableStateOf("") }
    val tenNguoiDung = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }

    // Trạng thái lỗi cho từng trường
    val errorTenNguoiDung = remember { mutableStateOf<String?>(null) }
    val errorEmail = remember { mutableStateOf<String?>(null) }
    val errorMatKhau = remember { mutableStateOf<String?>(null) }
    val errorMatKhauXacNhan = remember { mutableStateOf<String?>(null) }
    val errorProvince = remember { mutableStateOf<String?>(null) }
    val errorDistrict = remember { mutableStateOf<String?>(null) }
    val errorSoNha = remember { mutableStateOf<String?>(null) }
    val errorPhoneNumber = remember { mutableStateOf<String?>(null) }

    // Trạng thái hiển thị/ẩn mật khẩu
    val showPassword = remember { mutableStateOf(false) }
    val showConfirmPassword = remember { mutableStateOf(false) }

    val categories = listOf(
        "Máy tính & laptop", "Đồ gia dụng", "Quần áo",
        "Làm đẹp và sức khỏe", "Đồ ăn", "Đồ chơi", "Khác"
    )
    val selectedCategory = remember { mutableStateOf<String?>(null) }

    var isProvinceDialogOpen by remember { mutableStateOf(false) }
    var isDistrictDialogOpen by remember { mutableStateOf(false) }

    val provinceList = LocationData.provinces.keys.toList()
    val districtList = LocationData.provinces[selectedProvince.value] ?: emptyList()

    // Thêm state mới để lưu username đã chuyển đổi
    val convertedUsername = remember { mutableStateOf("") }

    // Thêm state để theo dõi focus
    val isNameFieldFocused = remember { mutableStateOf(false) }

    // Thêm hàm chuyển đổi họ tên thành username
    fun convertToUsername(fullName: String): String {
        return fullName.lowercase()
            .trim()
            .replace(" ", "")
            .replace("đ", "d")
            .replace("Đ", "d")
            .replace("á", "a")
            .replace("à", "a")
            .replace("ả", "a")
            .replace("ã", "a")
            .replace("ạ", "a")
            .replace("ă", "a")
            .replace("ắ", "a")
            .replace("ằ", "a")
            .replace("ẳ", "a")
            .replace("ẵ", "a")
            .replace("ặ", "a")
            .replace("â", "a")
            .replace("ấ", "a")
            .replace("ầ", "a")
            .replace("ẩ", "a")
            .replace("ẫ", "a")
            .replace("ậ", "a")
            .replace("é", "e")
            .replace("è", "e")
            .replace("ẻ", "e")
            .replace("ẽ", "e")
            .replace("ẹ", "e")
            .replace("ê", "e")
            .replace("ế", "e")
            .replace("ề", "e")
            .replace("ể", "e")
            .replace("ễ", "e")
            .replace("ệ", "e")
            .replace("í", "i")
            .replace("ì", "i")
            .replace("ỉ", "i")
            .replace("ĩ", "i")
            .replace("ị", "i")
            .replace("ó", "o")
            .replace("ò", "o")
            .replace("ỏ", "o")
            .replace("õ", "o")
            .replace("ọ", "o")
            .replace("ô", "o")
            .replace("ố", "o")
            .replace("ồ", "o")
            .replace("ổ", "o")
            .replace("ỗ", "o")
            .replace("ộ", "o")
            .replace("ơ", "o")
            .replace("ớ", "o")
            .replace("ờ", "o")
            .replace("ở", "o")
            .replace("ỡ", "o")
            .replace("ợ", "o")
            .replace("ú", "u")
            .replace("ù", "u")
            .replace("ủ", "u")
            .replace("ũ", "u")
            .replace("ụ", "u")
            .replace("ư", "u")
            .replace("ứ", "u")
            .replace("ừ", "u")
            .replace("ử", "u")
            .replace("ữ", "u")
            .replace("ự", "u")
            .replace("ý", "y")
            .replace("ỳ", "y")
            .replace("ỷ", "y")
            .replace("ỹ", "y")
            .replace("ỵ", "y")
    }

    // Hàm kiểm tra mật khẩu hợp lệ
    fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$"
        return password.matches(passwordRegex.toRegex())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimension.pagePadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Đăng ký",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        // Tên người dùng
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Họ và tên",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground
                    )
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            CustomInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (errorTenNguoiDung.value != null) 1.dp else 0.dp,
                        color = if (errorTenNguoiDung.value != null) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                value = tenNguoiDung.value,
                onValueChange = {
                    tenNguoiDung.value = it
                    convertedUsername.value = convertToUsername(it)
                    errorTenNguoiDung.value = null
                },
                placeholder = "Nhập họ và tên",
                textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                padding = PaddingValues(Dimension.pagePadding),
                backgroundColor = MaterialTheme.colors.surface,
                textColor = MaterialTheme.colors.onBackground,
                imeAction = ImeAction.Next,
                shape = RoundedCornerShape(8.dp),
                onFocusChange = { isFocused ->
                    isNameFieldFocused.value = isFocused
                    if (isFocused) {
                        convertedUsername.value = convertToUsername(tenNguoiDung.value)
                    }
                },
                onKeyboardActionClicked = {},
            )
            errorTenNguoiDung.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (isNameFieldFocused.value || convertedUsername.value.isNotEmpty()) {
                Text(
                    text = "Username của bạn sẽ là: ${convertedUsername.value}",
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Email
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground
                    )
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                    .border(
                        width = if (errorEmail.value != null) 1.dp else 0.dp,
                        color = if (errorEmail.value != null) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = Dimension.pagePadding, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CustomInputField(
                    modifier = Modifier.weight(1f),
                    value = emailState.value,
                    onValueChange = {
                        emailState.value = it
                        loginViewModel.updateEmailOrPhone(it.ifBlank { null })
                        errorEmail.value = null
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
                    trailingIcon = null
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
            errorEmail.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Mật khẩu
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Mật khẩu",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground
                    )
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            CustomInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (errorMatKhau.value != null) 1.dp else 0.dp,
                        color = if (errorMatKhau.value != null) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                value = matKhau ?: "",
                onValueChange = {
                    loginViewModel.updatePassword(it.ifBlank { null })
                    errorMatKhau.value = null
                },
                placeholder = "Nhập mật khẩu",
                visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
                textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                padding = PaddingValues(Dimension.pagePadding),
                backgroundColor = MaterialTheme.colors.surface,
                textColor = MaterialTheme.colors.onBackground,
                imeAction = ImeAction.Next,
                shape = RoundedCornerShape(8.dp),
                onFocusChange = {},
                onKeyboardActionClicked = {},
                trailingIcon = {
                    IconButton(onClick = { showPassword.value = !showPassword.value }) {
                        Icon(
                            painter = painterResource(id = if (showPassword.value) R.drawable.ic_eye else R.drawable.ic_eye_off),
                            contentDescription = if (showPassword.value) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.size(Dimension.mdIcon)
                        )
                    }
                }
            )
            errorMatKhau.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Xác nhận mật khẩu
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Xác nhận mật khẩu",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground
                    )
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            CustomInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (errorMatKhauXacNhan.value != null) 1.dp else 0.dp,
                        color = if (errorMatKhauXacNhan.value != null) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                value = matKhauXacNhan.value,
                onValueChange = {
                    matKhauXacNhan.value = it
                    errorMatKhauXacNhan.value = null
                },
                placeholder = "Nhập lại mật khẩu",
                visualTransformation = if (showConfirmPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
                textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                padding = PaddingValues(Dimension.pagePadding),
                backgroundColor = MaterialTheme.colors.surface,
                textColor = MaterialTheme.colors.onBackground,
                imeAction = ImeAction.Next,
                shape = RoundedCornerShape(8.dp),
                onFocusChange = {},
                onKeyboardActionClicked = {},
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword.value = !showConfirmPassword.value }) {
                        Icon(
                            painter = painterResource(id = if (showConfirmPassword.value) R.drawable.ic_eye else R.drawable.ic_eye_off),
                            contentDescription = if (showConfirmPassword.value) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.size(Dimension.mdIcon)
                        )
                    }
                }
            )
            errorMatKhauXacNhan.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Số điện thoại
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Số điện thoại",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground
                    )
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            CustomInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (errorPhoneNumber.value != null) 1.dp else 0.dp,
                        color = if (errorPhoneNumber.value != null) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                value = phoneNumber.value,
                onValueChange = {
                    phoneNumber.value = it
                    errorPhoneNumber.value = null
                },
                placeholder = "Nhập số điện thoại",
                textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                padding = PaddingValues(Dimension.pagePadding),
                backgroundColor = MaterialTheme.colors.surface,
                textColor = MaterialTheme.colors.onBackground,
                imeAction = ImeAction.Next,
                shape = RoundedCornerShape(8.dp),
                onFocusChange = {},
                onKeyboardActionClicked = {},
            )
            errorPhoneNumber.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Thành phố
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tỉnh/Thành phố",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground
                    )
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (errorProvince.value != null) 1.dp else 0.dp,
                        color = if (errorProvince.value != null) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
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
                    textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                    onButtonClicked = {},
                )
                CustomButton(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    padding = PaddingValues(Dimension.pagePadding.div(2)),
                    buttonColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colors.primary,
                    text = "Chọn",
                    enabled = true,
                    textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                    onButtonClicked = {
                        isProvinceDialogOpen = true
                    },
                )
            }
            errorProvince.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Quận huyện
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Quận/Huyện",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground
                    )
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (errorDistrict.value != null) 1.dp else 0.dp,
                        color = if (errorDistrict.value != null) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding)
            ) {
                CustomButton(
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(8.dp),
                    padding = PaddingValues(Dimension.pagePadding.div(2)),
                    buttonColor = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.onBackground,
                    text = selectedDistrict.value.ifBlank { "Chọn quận/huyện" },
                    enabled = false,
                    textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                    onButtonClicked = {},
                )
                CustomButton(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    padding = PaddingValues(Dimension.pagePadding.div(2)),
                    buttonColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colors.primary,
                    text = "Chọn",
                    enabled = true,
                    textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                    onButtonClicked = {
                        isDistrictDialogOpen = true
                    },
                )
            }
            errorDistrict.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Số nhà
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Địa chỉ cụ thể",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground
                    )
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            CustomInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (errorSoNha.value != null) 1.dp else 0.dp,
                        color = if (errorSoNha.value != null) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                value = soNha.value,
                onValueChange = {
                    soNha.value = it
                    errorSoNha.value = null
                },
                placeholder = "Nhập tên đường, số nhà",
                textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                padding = PaddingValues(Dimension.pagePadding),
                backgroundColor = MaterialTheme.colors.surface,
                textColor = MaterialTheme.colors.onBackground,
                imeAction = ImeAction.Next,
                shape = RoundedCornerShape(8.dp),
                onFocusChange = {},
                onKeyboardActionClicked = {},
            )
            errorSoNha.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Danh mục
        Column {
            Text(
                text = "Danh mục quan tâm",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(modifier = Modifier.fillMaxWidth()) {
                categories.forEach { category ->
                    val selected = selectedCategory.value == category
                    CustomButton(
                        shape = RoundedCornerShape(50),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        buttonColor = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface,
                        contentColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground,
                        text = category,
                        textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp),
                        onButtonClicked = {
                            selectedCategory.value = if (selected) null else category
                        },
                    )
                }
            }
        }

        // Đăng ký
        CustomButton(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            padding = PaddingValues(Dimension.pagePadding.div(2)),
            buttonColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            text = "Đăng ký",
            enabled = trangThaiUi !is UiState.Loading,
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
            onButtonClicked = {
                var hasError = false

                // Kiểm tra tên người dùng
                if (tenNguoiDung.value.isBlank()) {
                    errorTenNguoiDung.value = "Trường này là bắt buộc"
                    hasError = true
                } else {
                    errorTenNguoiDung.value = null
                }

                // Kiểm tra email
                val rawEmail = emailState.value
                if (rawEmail.isBlank()) {
                    errorEmail.value = "Trường này là bắt buộc"
                    hasError = true
                } else {
                    errorEmail.value = null
                }

                // Kiểm tra mật khẩu
                val password = matKhau ?: ""
                if (password.isBlank()) {
                    errorMatKhau.value = "Trường này là bắt buộc"
                    hasError = true
                } else if (!isValidPassword(password)) {
                    errorMatKhau.value = "Mật khẩu dài từ 8-20 ký tự và chứa kết hợp các chữ cái tiếng Anh, số và ký hiệu đặc biệt"
                    hasError = true
                } else {
                    errorMatKhau.value = null
                }


                // Kiểm tra xác nhận mật khẩu
                if (matKhauXacNhan.value.isBlank()) {
                    errorMatKhauXacNhan.value = "Trường này là bắt buộc"
                    hasError = true
                } else if (matKhauXacNhan.value != password) {
                    errorMatKhauXacNhan.value = "Mật khẩu không trùng khớp"
                    hasError = true
                } else {
                    errorMatKhauXacNhan.value = null
                }

                // Kiểm tra số điện thoại
                if (phoneNumber.value.isBlank()) {
                    errorPhoneNumber.value = "Trường này là bắt buộc"
                    hasError = true
                } else {
                    errorPhoneNumber.value = null
                }

                // Kiểm tra tỉnh/thành phố
                if (selectedProvince.value == "Chọn tỉnh/thành phố") {
                    errorProvince.value = "Trường này là bắt buộc"
                    hasError = true
                } else {
                    errorProvince.value = null
                }

                // Kiểm tra quận/huyện
                if (selectedDistrict.value.isBlank()) {
                    errorDistrict.value = "Trường này là bắt buộc"
                    hasError = true
                } else {
                    errorDistrict.value = null
                }

                // Kiểm tra số nhà
                if (soNha.value.isBlank()) {
                    errorSoNha.value = "Trường này là bắt buộc"
                    hasError = true
                } else {
                    errorSoNha.value = null
                }

                if (!hasError) {
                    val fullEmail = "$rawEmail@gmail.com"
                    loginViewModel.registerUser(
                        username = convertedUsername.value,
                        email = fullEmail,
                        password = password,
                        confirmPassword = matKhauXacNhan.value,
                        name = tenNguoiDung.value,
                        address = "${soNha.value}, ${selectedDistrict.value}, ${selectedProvince.value}",
                        phoneNumber = phoneNumber.value,
                        onRegistered = {
                            onYeuCauToast("Đăng ký thành công!", Color.Green)
                            onDangKyThanhCong()
                        },
                        onRegistrationFailed = { loi ->
                            onYeuCauToast(loi, Color.Red)
                        }
                    )
                }
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
                    contentDescription = "Đăng ký",
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
                    Text(
                        text = "Chọn Tỉnh/Thành phố",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    provinceList.forEach {
                        TextButton(onClick = {
                            selectedProvince.value = it
                            selectedDistrict.value = ""
                            errorProvince.value = null
                            isProvinceDialogOpen = false
                        }) {
                            Text(
                                text = it,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp
                            )
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
                    Text(
                        text = "Chọn Quận/Huyện",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    districtList.forEach {
                        TextButton(onClick = {
                            selectedDistrict.value = it
                            errorDistrict.value = null
                            isDistrictDialogOpen = false
                        }) {
                            Text(
                                text = it,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}