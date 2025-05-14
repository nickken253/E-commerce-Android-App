package com.mustfaibra.roffu.screens.checkout

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.*
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.IconButton
import com.mustfaibra.roffu.components.SecondaryTopBar
import com.mustfaibra.roffu.components.SummaryRow
import com.mustfaibra.roffu.models.dto.BankCardListResponse
import com.mustfaibra.roffu.models.dto.CartItemWithProductDetails
import com.mustfaibra.roffu.screens.profile.AddVirtualCardScreen
import com.mustfaibra.roffu.screens.profile.ProfileViewModel
import com.mustfaibra.roffu.screens.profile.VisaCardDisplay
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.encryptCardNumber
import com.skydoves.whatif.whatIfNotNull
import kotlinx.serialization.json.Json
import java.net.URLDecoder

// Hàm định dạng số thành chuỗi tiền tệ Việt Nam
private fun formatVietnamCurrency(amount: Long): String {
    return amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₫"
}

@Composable
fun CheckoutScreen(
    navController: NavHostController,
    itemsJson: String,
    totalAmount: Double,
    onChangeLocationRequested: () -> Unit,
    onNavigationRequested: (route: String, popBackStack: Boolean) -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    checkoutViewModel: CheckoutViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val appContext = LocalContext.current
    var selectedItems by remember { mutableStateOf<List<CartItemWithProductDetails>>(emptyList()) }
    var decodeError by remember { mutableStateOf<String?>(null) }

    // Giải mã itemsJson
    LaunchedEffect(itemsJson) {
        try {
            val decodedItemsJson = URLDecoder.decode(itemsJson, "UTF-8")
            val items = Json.decodeFromString<List<CartItemWithProductDetails>>(decodedItemsJson)
            selectedItems = items
            checkoutViewModel.updateSelectedCartItems(items)
            checkoutViewModel.setUserCart(items)
            checkoutViewModel.getBankCards(appContext)
            Log.d("CheckoutScreen", "Successfully decoded ${items.size} items")
        } catch (e: Exception) {
            decodeError = "Lỗi tải dữ liệu giỏ hàng: ${e.message}"
            Log.e("CheckoutScreen", "Error decoding itemsJson: ${e.message}")
        }
    }

    val checkoutUiState by checkoutViewModel.checkoutState
    val isVirtualCardAdded by profileViewModel.isVirtualCardAdded.collectAsState()
    val bankCards by checkoutViewModel.bankCards
    val selectedCardId by checkoutViewModel.selectedCardId
    val isLoadingCards by checkoutViewModel.isLoadingCards
    val error by checkoutViewModel.error
    var showAddCardDialog by remember { mutableStateOf(false) }
    val isLoadingSelectedCartItems by checkoutViewModel.isLoadingSelectedCartItems

    if (showAddCardDialog) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Thêm thẻ thanh toán") },
            text = {
                AddVirtualCardScreen(
                    onCardAdded = { cardNumber, month, year, cvvValue, cardHolder ->
                        showAddCardDialog = false
                        checkoutViewModel.getBankCards(appContext)
                    },
                    onCancel = { showAddCardDialog = false }
                )
            },
            buttons = {}
        )
    }

    if (checkoutUiState is UiState.Loading) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
            )
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colors.surface)
                    .fillMaxWidth()
                    .padding(Dimension.pagePadding.times(2))
            ) {
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(R.raw.world_rounding),
                )
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = LottieConstants.IterateForever,
                    speed = 1f,
                    restartOnPlay = true,
                )
                LottieAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    progress = { progress },
                    composition = composition,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        SecondaryTopBar(
            title = stringResource(id = R.string.checkout),
            onBackClicked = { onNavigationRequested(com.mustfaibra.roffu.sealed.Screen.Cart.route, true) },
        )
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = rememberScrollState()),
        ) {
            val selectedPaymentMethodId by checkoutViewModel.selectedPaymentMethodId
            val subTotal by checkoutViewModel.subTotalPrice
            
            // Sử dụng DeliveryLocationSection mới với API địa chỉ
            DeliveryLocationSection(
                viewModel = checkoutViewModel,
                onToastRequested = onToastRequested
            )
            PaymentMethodsSection(
                bankCards = bankCards,
                selectedCardId = selectedCardId,
                isLoadingCards = isLoadingCards,
                error = error,
                selectedPayment = selectedPaymentMethodId,
                onPaymentSelected = { checkoutViewModel.updateSelectedPaymentMethod(it) },
                onCardSelected = { checkoutViewModel.updateSelectedCard(it) },
                onAddCardRequested = { showAddCardDialog = true },
                viewModel = checkoutViewModel
            )
            Text(
                text = "Sản phẩm đã chọn",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(horizontal = Dimension.pagePadding, vertical = 8.dp)
            )

            when {
                decodeError != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = decodeError ?: "Lỗi không xác định",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onNavigationRequested(com.mustfaibra.roffu.sealed.Screen.Cart.route, true) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary,
                                    contentColor = MaterialTheme.colors.onPrimary
                                )
                            ) {
                                Text("Quay lại giỏ hàng")
                            }
                        }
                    }
                }
                isLoadingSelectedCartItems -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                selectedItems.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Không có sản phẩm nào được chọn",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onNavigationRequested(com.mustfaibra.roffu.sealed.Screen.Cart.route, true) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary,
                                    contentColor = MaterialTheme.colors.onPrimary
                                )
                            ) {
                                Text("Quay lại giỏ hàng")
                            }
                        }
                    }
                }
                else -> {
                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
                        contentPadding = PaddingValues(Dimension.pagePadding),
                    ) {
                        items(selectedItems) { item ->
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colors.surface)
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = item.productImage.takeIf { it.isNotEmpty() }
                                            ?: R.drawable.ic_shopping_bag
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.subtitle1,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatVietnamCurrency((item.unitPrice.toLong() * item.quantity)),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Text(
                                        text = "x${item.quantity}",
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(Dimension.pagePadding)
                )
            }
            Column(
                modifier = Modifier
                    .shadow(
                        elevation = Dimension.elevation.div(2),
                        shape = RoundedCornerShape(
                            topStartPercent = 15,
                            topEndPercent = 15,
                        ),
                        spotColor = MaterialTheme.colors.primary,
                    )
                    .clip(shape = MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colors.background)
                    .padding(all = Dimension.pagePadding),
                verticalArrangement = Arrangement.spacedBy(Dimension.sm)
            ) {
                SummaryRow(
                    title = stringResource(id = R.string.sub_total),
                    value = formatVietnamCurrency(checkoutViewModel.subTotalPrice.value.toLong()),
                    valueColor = Color(0xFF0052CC)
                )
                SummaryRow(
                    title = stringResource(id = R.string.shipping),
                    value = formatVietnamCurrency(checkoutViewModel.shippingFee.value.toLong()),
                    valueColor = Color(0xFF0052CC)
                )
                Divider()
                SummaryRow(
                    title = stringResource(id = R.string.total),
                    value = formatVietnamCurrency(checkoutViewModel.totalOrderAmount.value.toLong()),
                    valueColor = Color(0xFF0052CC)
                )
                CustomButton(
                    modifier = Modifier.fillMaxWidth(),
                    buttonColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    text = "Thanh toán",
                    onButtonClicked = {
                        val selectedPaymentMethod = checkoutViewModel.selectedPaymentMethodId.value
                        if (selectedPaymentMethod == "visa") {
                            // Xử lý thanh toán qua thẻ Visa - gọi API thật
                            onToastRequested("Đang xử lý thanh toán qua thẻ Visa...", Color.Blue)
                            try {
                                // CVV đã được nhập vào và lưu trong ViewModel
                                checkoutViewModel.processCardPayment(
                                    context = appContext,
                                    onSuccess = {
                                        checkoutViewModel.clearCart()
                                        onToastRequested("Thanh toán thành công!", Color(0xFF4CAF50))
                                        // Chuyển đến màn hình lịch sử đơn hàng sau khi thanh toán thành công
                                        onNavigationRequested(com.mustfaibra.roffu.sealed.Screen.OrderHistory.route, true)
                                    },
                                    onError = { errorMessage ->
                                        onToastRequested(errorMessage, Color.Red)
                                    }
                                )
                            } catch (e: Exception) {
                                onToastRequested("Lỗi: ${e.message}", Color.Red)
                            }
                        } else {
                            // Xử lý thanh toán bằng tiền mặt
                            checkoutViewModel.makeTransactionPayment(
                                items = checkoutViewModel.selectedCartItems,
                                total = checkoutViewModel.subTotalPrice.value,
                                onCheckoutSuccess = {
                                    checkoutViewModel.clearCart()
                                    onToastRequested("Thanh toán thành công!", Color(0xFF4CAF50))
                                    // Chuyển đến màn hình lịch sử đơn hàng sau khi thanh toán thành công
                                    onNavigationRequested(com.mustfaibra.roffu.sealed.Screen.OrderHistory.route, true)
                                },
                                onCheckoutFailed = { message ->
                                    onToastRequested(
                                        appContext.getString(message),
                                        Color.Red,
                                    )
                                }
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DeliveryLocationSection(
    viewModel: CheckoutViewModel,
    onToastRequested: (String, Color) -> Unit
) {
    val context = LocalContext.current
    val deliveryAddresses = viewModel.deliveryAddresses
    val selectedAddress by viewModel.selectedDeliveryAddress
    val isLoadingAddresses by viewModel.isLoadingAddresses
    val addressError by viewModel.addressError
    
    // Dialog states
    var showAddressDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var currentAddressId by remember { mutableStateOf<Int?>(null) }
    
    // Form fields
    var addressType by remember { mutableStateOf("shipping") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    
    // Load addresses when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.getUserAddresses(context)
    }
    
    // Show error toast if there's an error
    LaunchedEffect(addressError) {
        if (addressError != null) {
            onToastRequested(addressError!!, Color.Red)
        }
    }
    
    Column(
        modifier = Modifier.padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        Text(
            text = stringResource(R.string.delivery_address),
            style = MaterialTheme.typography.button,
        )
        
        if (isLoadingAddresses) {
            // Show loading indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colors.primary)
            }
        } else {
            // Trạng thái mở dropdown
            var showAddressDropdown by remember { mutableStateOf(false) }
            
            // Hiển thị dòng đầu tiên (luôn hiển thị)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colors.surface)
                    .clickable { showAddressDropdown = !showAddressDropdown }
                    .padding(Dimension.pagePadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
            ) {
                DrawableButton(
                    painter = painterResource(id = R.drawable.ic_map_pin),
                    onButtonClicked = {},
                    backgroundColor = MaterialTheme.colors.surface,
                    iconTint = MaterialTheme.colors.onSurface,
                    paddingValue = PaddingValues(Dimension.sm),
                )
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    if (selectedAddress != null) {
                        Text(
                            text = selectedAddress?.address ?: "",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedAddress?.city ?: "",
                            style = MaterialTheme.typography.caption,
                        )
                        if (selectedAddress?.isDefault == true) {
                            Text(
                                text = "Mặc định",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.primary
                            )
                        }
                    } else {
                        Text(
                            text = "Chọn địa chỉ giao hàng",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
                IconButton(
                    icon = if (showAddressDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    backgroundColor = MaterialTheme.colors.background,
                    iconTint = MaterialTheme.colors.onBackground,
                    onButtonClicked = { showAddressDropdown = !showAddressDropdown },
                    iconSize = Dimension.mdIcon,
                    paddingValue = PaddingValues(Dimension.sm),
                    shape = MaterialTheme.shapes.medium,
                )
            }
            
            // Dropdown menu cho danh sách địa chỉ
            AnimatedVisibility(visible = showAddressDropdown) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(10f) // Đảm bảo hiển thị trên các phần tử khác
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surface)
                        ) {
                            if (deliveryAddresses.isEmpty()) {
                                // Hiển thị thông báo khi không có địa chỉ
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimension.pagePadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Bạn chưa có địa chỉ nào",
                                        style = MaterialTheme.typography.body1,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            } else {
                                // Danh sách địa chỉ
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 250.dp),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    items(deliveryAddresses) { address ->
                                        val isSelected = selectedAddress?.id == address.id
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface)
                                                .clickable { 
                                                    viewModel.selectDeliveryAddress(address.id) 
                                                    showAddressDropdown = false
                                                }
                                                .padding(Dimension.pagePadding),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
                                        ) {
                                            // Selection indicator
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { 
                                                    viewModel.selectDeliveryAddress(address.id) 
                                                    showAddressDropdown = false
                                                },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = MaterialTheme.colors.primary
                                                )
                                            )
                                            
                                            // Address name/street
                                            Column(
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(
                                                    text = address.address,
                                                    style = MaterialTheme.typography.body1,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = address.city,
                                                    style = MaterialTheme.typography.caption,
                                                )
                                                if (address.isDefault) {
                                                    Text(
                                                        text = "Mặc định",
                                                        style = MaterialTheme.typography.caption,
                                                        color = MaterialTheme.colors.primary
                                                    )
                                                }
                                            }
                                            
                                            // Edit button
                                            IconButton(
                                                icon = Icons.Default.Edit,
                                                backgroundColor = MaterialTheme.colors.background,
                                                iconTint = MaterialTheme.colors.onBackground,
                                                onButtonClicked = { 
                                                    // Prepare for edit mode
                                                    isEditMode = true
                                                    currentAddressId = address.id
                                                    street = address.address
                                                    val cityParts = address.city.split(", ")
                                                    district = cityParts.firstOrNull() ?: ""
                                                    city = cityParts.getOrNull(1) ?: ""
                                                    postalCode = ""
                                                    isDefault = address.isDefault
                                                    showAddressDialog = true
                                                    showAddressDropdown = false
                                                },
                                                iconSize = Dimension.smIcon,
                                                paddingValue = PaddingValues(Dimension.xs),
                                                shape = MaterialTheme.shapes.medium,
                                            )
                                        }
                                        
                                        // Divider between addresses
                                        Divider(color = MaterialTheme.colors.background)
                                    }
                                }
                            }
                            
                            // Nút thêm địa chỉ mới
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        isEditMode = false
                                        street = ""
                                        city = ""
                                        district = ""
                                        postalCode = ""
                                        isDefault = false
                                        showAddressDialog = true 
                                        showAddressDropdown = false
                                    }
                                    .padding(Dimension.pagePadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Thêm địa chỉ mới",
                                    tint = MaterialTheme.colors.primary
                                )
                                Text(
                                    text = "Thêm địa chỉ mới",
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Address Dialog
    if (showAddressDialog) {
        Dialog(
            onDismissRequest = { showAddressDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.background
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isEditMode) "Cập nhật địa chỉ" else "Thêm địa chỉ mới",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Address type
                    OutlinedTextField(
                        value = addressType,
                        onValueChange = { addressType = it },
                        label = { Text("Loại địa chỉ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Street
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Đường") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // District
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("Quận/Huyện") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // City
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Thành phố") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Postal code
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        label = { Text("Mã bưu điện (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Default address checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDefault,
                            onCheckedChange = { newValue ->
                                isDefault = newValue
                            }
                        )
                        Text(
                            text = "Đặt làm địa chỉ mặc định",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddressDialog = false }
                        ) {
                            Text("Hủy")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (street.isBlank() || city.isBlank() || district.isBlank()) {
                                    onToastRequested("Vui lòng điền đầy đủ thông tin", Color.Red)
                                    return@Button
                                }
                                
                                // Tìm địa chỉ mặc định hiện tại
                                val currentDefaultAddress = deliveryAddresses.find { it.isDefault }
                                
                                // Kiểm tra nếu đây là địa chỉ đầu tiên
                                val isFirstAddress = deliveryAddresses.isEmpty()
                                
                                // Nếu đây là địa chỉ đầu tiên, luôn đặt làm mặc định
                                val shouldBeDefault = isFirstAddress || isDefault
                                
                                if (isEditMode && currentAddressId != null) {
                                    // Update existing address
                                    viewModel.updateAddress(
                                        context = context,
                                        addressId = currentAddressId!!,
                                        addressType = addressType,
                                        street = street,
                                        city = city,
                                        district = district,
                                        postalCode = postalCode.takeIf { it.isNotBlank() },
                                        isDefault = shouldBeDefault,
                                        onSuccess = {
                                            // Nếu đánh dấu là địa chỉ mặc định và có một địa chỉ mặc định khác
                                            if (shouldBeDefault && currentDefaultAddress != null && currentDefaultAddress.id != currentAddressId) {
                                                // Cập nhật địa chỉ mặc định cũ thành không mặc định
                                                viewModel.updateAddress(
                                                    context = context,
                                                    addressId = currentDefaultAddress.id,
                                                    addressType = "Home", // Giữ nguyên loại
                                                    street = currentDefaultAddress.address,
                                                    city = currentDefaultAddress.city.split(", ").getOrNull(1) ?: "",
                                                    district = currentDefaultAddress.city.split(", ").firstOrNull() ?: "",
                                                    isDefault = false,
                                                    onSuccess = {
                                                        // Không cần thông báo cho việc cập nhật địa chỉ mặc định cũ
                                                    },
                                                    onError = { _ -> }
                                                )
                                            }
                                            showAddressDialog = false
                                            onToastRequested("Cập nhật địa chỉ thành công", Color.Green)
                                        },
                                        onError = { error ->
                                            onToastRequested(error, Color.Red)
                                        }
                                    )
                                } else {
                                    // Create new address
                                    viewModel.createAddress(
                                        context = context,
                                        addressType = addressType,
                                        street = street,
                                        city = city,
                                        district = district,
                                        postalCode = postalCode.takeIf { it.isNotBlank() },
                                        isDefault = shouldBeDefault,
                                        onSuccess = {
                                            // Nếu đánh dấu là địa chỉ mặc định và có một địa chỉ mặc định khác
                                            if (shouldBeDefault && currentDefaultAddress != null) {
                                                // Cập nhật địa chỉ mặc định cũ thành không mặc định
                                                viewModel.updateAddress(
                                                    context = context,
                                                    addressId = currentDefaultAddress.id,
                                                    addressType = "Home", // Giữ nguyên loại
                                                    street = currentDefaultAddress.address,
                                                    city = currentDefaultAddress.city.split(", ").getOrNull(1) ?: "",
                                                    district = currentDefaultAddress.city.split(", ").firstOrNull() ?: "",
                                                    isDefault = false,
                                                    onSuccess = {
                                                        // Không cần thông báo cho việc cập nhật địa chỉ mặc định cũ
                                                    },
                                                    onError = { _ -> }
                                                )
                                            }
                                            showAddressDialog = false
                                            onToastRequested("Thêm địa chỉ mới thành công", Color.Green)
                                        },
                                        onError = { error ->
                                            onToastRequested(error, Color.Red)
                                        }
                                    )
                                }
                            }
                        ) {
                            Text(if (isEditMode) "Cập nhật" else "Thêm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodsSection(
    bankCards: List<BankCardListResponse>,
    selectedCardId: Int?,
    isLoadingCards: Boolean,
    error: String?,
    selectedPayment: String?,
    onPaymentSelected: (String) -> Unit,
    onCardSelected: (Int) -> Unit,
    onAddCardRequested: () -> Unit,
    viewModel: CheckoutViewModel
) {
    // State để lưu giá trị CVV
    var cvvValue by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        Text(
            text = "Phương thức thanh toán",
            style = MaterialTheme.typography.button,
        )

        // Card chứa các phương thức thanh toán
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = 2.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Phương thức thanh toán Visa
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable(enabled = bankCards.isNotEmpty()) { 
                                if (bankCards.isNotEmpty()) {
                                    onPaymentSelected("visa") 
                                }
                            }
                            .background(
                                if (selectedPayment == "visa") 
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                                else if (bankCards.isEmpty())
                                    Color.LightGray.copy(alpha = 0.3f)
                                else 
                                    MaterialTheme.colors.surface
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = if (bankCards.isEmpty()) Color.Gray else MaterialTheme.colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        
                        if (bankCards.isEmpty()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Thẻ Visa",
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Bạn chưa thêm thẻ nào",
                                    style = MaterialTheme.typography.caption,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Text(
                                text = "Thẻ Visa",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        RadioButton(
                            selected = selectedPayment == "visa",
                            onClick = { 
                                if (bankCards.isNotEmpty()) {
                                    onPaymentSelected("visa") 
                                }
                            },
                            enabled = bankCards.isNotEmpty(),
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colors.primary,
                                unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                disabledColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                    }
                    
                    // Đã xóa nút thêm thẻ mới
                    
                    // Hiển thị ô nhập CVV khi chọn phương thức thanh toán Visa
                    AnimatedVisibility(visible = selectedPayment == "visa") {
                        OutlinedTextField(
                            value = cvvValue,
                            onValueChange = { newValue ->
                                // Chỉ cho phép nhập số và giới hạn 3-4 chữ số
                                if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                                    cvvValue = newValue
                                    // Cập nhật CVV vào ViewModel để sử dụng khi thanh toán
                                    viewModel.setCvv(cvvValue.toIntOrNull() ?: 0)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            label = { Text("Mã CVV") },
                            placeholder = { Text("Nhập mã bảo mật 3-4 số ở mặt sau thẻ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.primary,
                                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
                
                // Divider giữa các phương thức thanh toán
                Divider(color = MaterialTheme.colors.background)
                
                // Phương thức thanh toán tiền mặt
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable { onPaymentSelected("cash") }
                        .background(if (selectedPayment == "cash") MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Tiền mặt (thanh toán khi nhận hàng)",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    RadioButton(
                        selected = selectedPayment == "cash",
                        onClick = { onPaymentSelected("cash") },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colors.primary,
                            unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        )
                    )
                }
            }
        }
    }
}