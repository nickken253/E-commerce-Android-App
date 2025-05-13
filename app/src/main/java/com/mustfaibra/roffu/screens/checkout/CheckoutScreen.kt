package com.mustfaibra.roffu.screens.checkout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import android.os.Handler
import android.os.Looper
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.IconButton
import com.mustfaibra.roffu.components.SecondaryTopBar
import com.mustfaibra.roffu.components.SummaryRow
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.UserPaymentProviderDetails
import com.mustfaibra.roffu.models.VirtualCard
import com.mustfaibra.roffu.models.dto.BankCardListResponse
import com.mustfaibra.roffu.models.dto.Image
import com.mustfaibra.roffu.models.dto.Product as ApiProduct
import com.mustfaibra.roffu.screens.profile.AddVirtualCardScreen
import com.mustfaibra.roffu.screens.profile.ProfileViewModel
import com.mustfaibra.roffu.screens.profile.VisaCardDisplay
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.encryptCardNumber
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.skydoves.whatif.whatIf
import com.skydoves.whatif.whatIfNotNull

// Hàm định dạng số thành chuỗi tiền tệ Việt Nam
private fun formatVietnamCurrency(amount: Long): String {
    return amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₫"
}

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    onChangeLocationRequested: () -> Unit,
    onBackRequested: () -> Unit,
    onCheckoutSuccess: () -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    checkoutViewModel: CheckoutViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    selectedItems: List<CartItem> = emptyList() // Danh sách sản phẩm được chọn để thanh toán
) {
    val appContext = LocalContext.current
    
    // Sử dụng một LaunchedEffect duy nhất để xử lý tất cả các tác vụ khởi tạo
    LaunchedEffect(key1 = Unit) {
        // Lấy danh sách thẻ ngân hàng khi màn hình được hiển thị
        checkoutViewModel.getBankCards(appContext)
        
        // Cập nhật danh sách sản phẩm đã chọn để thanh toán
        checkoutViewModel.updateSelectedCartItems(selectedItems)
        
        // Sử dụng danh sách sản phẩm được chọn hoặc toàn bộ giỏ hàng
        val itemsToCheckout = if (selectedItems.isNotEmpty()) selectedItems else cartItems
        checkoutViewModel.setUserCart(itemsToCheckout)
        
        android.util.Log.d("CheckoutScreen", "Khởi tạo với ${selectedItems.size} sản phẩm được chọn, ${cartItems.size} sản phẩm trong giỏ hàng")
    }

    val checkoutUiState by remember { checkoutViewModel.checkoutState }
    val isVirtualCardAdded by profileViewModel.isVirtualCardAdded.collectAsState()
    
    // Danh sách thẻ ngân hàng
    val bankCards by remember { checkoutViewModel.bankCards }
    val selectedCardId by remember { checkoutViewModel.selectedCardId }
    val isLoadingCards by remember { checkoutViewModel.isLoadingCards }
    val error by remember { checkoutViewModel.error }
    
    // Biến state để kiểm soát hiển thị dialog thêm thẻ
    var showAddCardDialog by remember { mutableStateOf(false) }
    
    // Không cần biến CVV ở cấp độ này

    // Dialog thêm thẻ mới
    if (showAddCardDialog) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Thêm thẻ thanh toán") },
            text = {
                AddVirtualCardScreen(
                    onCardAdded = { cardNumber, month, year, cvvValue, cardHolder ->
                        showAddCardDialog = false
                        // Lấy lại danh sách thẻ sau khi thêm
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

                /** to control the animation speed */
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
        /** Secondary top bar */
        SecondaryTopBar(
            title = stringResource(id = R.string.checkout),
            onBackClicked = onBackRequested,
        )
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = rememberScrollState()),
        ) {
            val selectedPaymentMethodId by remember {
                checkoutViewModel.selectedPaymentMethodId
            }
            val subTotal by remember { checkoutViewModel.subTotalPrice }
            /** Delivery Location */
            val location by remember {
                checkoutViewModel.deliveryAddress
            }
            location?.whatIfNotNull(
                whatIf = {
                    DeliveryLocationSection(
                        address = it.address,
                        city = "${it.city}, ${it.country}",
                        onChangeLocationRequested = {
//                          onChangeLocationRequested()
                        },
                    )
                }
            )
            /** Payment methods */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimension.pagePadding)
            ) {
                Text(
                    text = "Phương thức thanh toán",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Lấy danh sách thẻ từ ViewModel
                val bankCards by remember { checkoutViewModel.bankCards }
                val isLoadingCards by remember { checkoutViewModel.isLoadingCards }
                
                // Hiển thị loading khi đang tải thông tin thẻ
                if (isLoadingCards) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Đang tải thông tin thẻ...")
                    }
                } else {
                    // Luôn hiển thị phương thức thanh toán Visa, nhưng mờ đi nếu không có thẻ
                    val hasVisaCard = bankCards.isNotEmpty()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .alpha(if (hasVisaCard) 1f else 0.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = checkoutViewModel.selectedPaymentMethodId.value == "visa",
                            onClick = { 
                                if (hasVisaCard) {
                                    val card = bankCards.find { it.isDefault } ?: bankCards.first()
                                    checkoutViewModel.updateSelectedPaymentMethod("visa")
                                    checkoutViewModel.updateSelectedCard(card.id)
                                }
                            },
                            enabled = hasVisaCard
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = if (hasVisaCard) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (hasVisaCard) {
                            val card = bankCards.find { it.isDefault } ?: bankCards.first()
                            val lastFourDigits = card.cardNumber.takeLast(4)
                            Text(text = "Visa (****$lastFourDigits)")
                        } else {
                            Text(
                                text = "Visa (Không có thẻ)",
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                // Luôn hiển thị phương thức thanh toán bằng tiền mặt
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = checkoutViewModel.selectedPaymentMethodId.value == "cash",
                        onClick = { checkoutViewModel.updateSelectedPaymentMethod("cash") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Tiền mặt (thanh toán khi nhận hàng)")
                }
            }
            /** Sản phẩm trong giỏ hàng */
            Text(
                text = "Sản phẩm đã chọn",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(horizontal = Dimension.pagePadding, vertical = 8.dp)
            )
            
            // Hiển thị trạng thái loading khi đang tải thông tin sản phẩm
            val isLoadingProductDetails = checkoutViewModel.isLoadingProductDetails.value
            if (isLoadingProductDetails) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Lấy danh sách sản phẩm đã chọn và chi tiết sản phẩm
                val selectedCartItems = checkoutViewModel.selectedCartItems
                val productDetails = checkoutViewModel.productDetails.value
                
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
                    contentPadding = PaddingValues(Dimension.pagePadding),
                ) {
                    items(selectedCartItems) { item ->
                        val productId = item.productId
                        val product = if (productId != null) productDetails[productId] else null
                        
                        Column(
                            modifier = Modifier
                                .width(150.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colors.surface)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Hiển thị hình ảnh sản phẩm từ API nếu có
                            val imageUrl = if (product is ApiProduct) {
                                product.images.find { it.is_primary }?.image_url
                            } else null
                                ?: item.product?.image
                                ?: R.drawable.ic_shopping_bag
                            
                            Image(
                                painter = if (imageUrl is Int) {
                                    painterResource(id = imageUrl)
                                } else {
                                    rememberAsyncImagePainter(model = imageUrl)
                                },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Hiển thị tên sản phẩm từ API nếu có
                            Text(
                                text = if (product is ApiProduct) product.product_name 
                                    else item.product?.name 
                                    ?: "Sản phẩm",
                                style = MaterialTheme.typography.subtitle1,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Hiển thị giá và số lượng
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatVietnamCurrency(
                                        (if (product is ApiProduct) product.price
                                            else item.product?.price?.toLong() 
                                            ?: 0) * item.quantity
                                    ),
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
            /** Checkout summary */
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
                /** sub total cost row */
                SummaryRow(
                    title = stringResource(id = R.string.sub_total),
                    value = formatVietnamCurrency(checkoutViewModel.subTotalPrice.value.toLong()),
                    valueColor = Color(0xFF0052CC)
                )
                /** shipping cost row */
                SummaryRow(
                    title = stringResource(id = R.string.shipping),
                    value = formatVietnamCurrency(checkoutViewModel.shippingFee.value.toLong()),
                    valueColor = Color(0xFF0052CC)
                )
                Divider()
                /** total cost row */
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
                            // Thanh toán bằng thẻ Visa - không yêu cầu CVV
                            onToastRequested("Đang xử lý thanh toán qua thẻ Visa...", Color.Blue)
                            
                            // Giả lập API call thành công
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                onCheckoutSuccess()
                                onToastRequested("Thanh toán thành công!", Color(0xFF4CAF50))
                            }, 2000)
                        }
                        else {
                            // Sử dụng phương thức thanh toán cũ
                            checkoutViewModel.makeTransactionPayment(
                                items = cartItems,
                                total = checkoutViewModel.subTotalPrice.value,
                                onCheckoutSuccess = onCheckoutSuccess,
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
fun DeliveryLocationSection(
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
fun PaymentMethodsSection(
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
                // Sử dụng biến cục bộ cho CVV
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
                
                // Lưu cvvText vào ViewModel khi giá trị thay đổi
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
