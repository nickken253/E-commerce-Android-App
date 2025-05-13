package com.mustfaibra.roffu.screens.checkout

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
            val location by checkoutViewModel.deliveryAddress
            location?.whatIfNotNull(
                whatIf = {
                    DeliveryLocationSection(
                        address = it.address,
                        city = "${it.city}, ${it.country}",
                        onChangeLocationRequested = onChangeLocationRequested,
                    )
                }
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
                            onToastRequested("Đang xử lý thanh toán qua thẻ Visa...", Color.Blue)
                            Handler(Looper.getMainLooper()).postDelayed({
                                checkoutViewModel.clearCart()
                                onToastRequested("Thanh toán thành công!", Color(0xFF4CAF50))
                                onNavigationRequested(com.mustfaibra.roffu.sealed.Screen.Home.route, true)
                            }, 2000)
                        } else {
                            checkoutViewModel.makeTransactionPayment(
                                items = checkoutViewModel.selectedCartItems,
                                total = checkoutViewModel.subTotalPrice.value,
                                onCheckoutSuccess = {
                                    checkoutViewModel.clearCart()
                                    onToastRequested("Thanh toán thành công!", Color(0xFF4CAF50))
                                    onNavigationRequested(com.mustfaibra.roffu.sealed.Screen.Home.route, true)
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
    address: String,
    city: String,
    onChangeLocationRequested: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        Text(
            text = stringResource(R.string.delivery_address),
            style = MaterialTheme.typography.button,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = address,
                    style = MaterialTheme.typography.body1,
                )
                Text(
                    text = city,
                    style = MaterialTheme.typography.caption,
                )
            }
            IconButton(
                icon = Icons.Rounded.KeyboardArrowRight,
                backgroundColor = MaterialTheme.colors.background,
                iconTint = MaterialTheme.colors.onBackground,
                onButtonClicked = onChangeLocationRequested,
                iconSize = Dimension.mdIcon,
                paddingValue = PaddingValues(Dimension.sm),
                shape = MaterialTheme.shapes.medium,
            )
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
    Column(
        modifier = Modifier.padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        Text(
            text = "Phương thức thanh toán",
            style = MaterialTheme.typography.button,
        )

        // Visa option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(if (selectedPayment == "visa") MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
                .clickable(enabled = bankCards.isNotEmpty()) { onPaymentSelected("visa") },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = if (bankCards.isNotEmpty()) MaterialTheme.colors.primary else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Thẻ Visa",
                    style = MaterialTheme.typography.body1,
                    color = if (bankCards.isNotEmpty()) MaterialTheme.colors.onSurface else Color.Gray
                )

                if (isLoadingCards) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(top = 4.dp),
                        color = MaterialTheme.colors.primary
                    )
                } else if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.caption,
                        color = Color.Red
                    )
                } else if (bankCards.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onAddCardRequested() }
                    ) {
                        Text(
                            text = "Thêm thẻ mới",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // Hiển thị thông tin thẻ được chọn
                    val selectedCard = bankCards.find { it.id == selectedCardId }
                    selectedCard?.let {
                        Text(
                            text = "**** **** **** ${it.cardNumber.takeLast(4)}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            RadioButton(
                selected = selectedPayment == "visa",
                onClick = { if (bankCards.isNotEmpty()) onPaymentSelected("visa") },
                enabled = bankCards.isNotEmpty(),
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colors.secondary,
                    unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                )
            )
        }

        // Hiển thị danh sách thẻ nếu đã chọn phương thức Visa
        if (selectedPayment == "visa" && bankCards.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(bankCards) { card ->
                    Box(
                        modifier = Modifier
                            .width(300.dp)
                            .clickable { onCardSelected(card.id) }
                            .border(
                                width = 2.dp,
                                color = if (selectedCardId == card.id) MaterialTheme.colors.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp)
                    ) {
                        VisaCardDisplay(
                            cardNumber = card.cardNumber,
                            cardHolder = card.cardHolderName,
                            expiryMonth = card.expiryMonth,
                            expiryYear = card.expiryYear
                        )

                        // Badge cho thẻ mặc định
                        if (card.isDefault) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(MaterialTheme.colors.secondary, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Mặc định",
                                    style = MaterialTheme.typography.caption,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Nút thêm thẻ mới
                item {
                    Box(
                        modifier = Modifier
                            .width(300.dp)
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colors.surface)
                            .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { onAddCardRequested() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Thêm thẻ mới",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
            }

            // Trường nhập CVV
            if (selectedCardId != null) {
                val (cvvText, setCvvText) = remember { mutableStateOf("") }
                OutlinedTextField(
                    value = cvvText,
                    onValueChange = { if (it.length <= 4) setCvvText(it) },
                    label = { Text("Mã bảo mật CVV") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = true
                )
                LaunchedEffect(cvvText) {
                    viewModel.setCvvText(cvvText)
                }
            }
        }
        // Cash option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(if (selectedPayment == "cash") MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
                .clickable { onPaymentSelected("cash") },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Tiền mặt (thanh toán khi nhận hàng)",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = selectedPayment == "cash",
                onClick = { onPaymentSelected("cash") },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colors.secondary,
                    unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                )
            )
        }
    }
}