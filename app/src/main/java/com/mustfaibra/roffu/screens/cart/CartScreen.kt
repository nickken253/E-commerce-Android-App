package com.mustfaibra.roffu.screens.cart

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.IconButton as CustomIconButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.PopupOptionsMenu
import com.mustfaibra.roffu.components.SimpleLoadingDialog
import com.mustfaibra.roffu.components.SummaryRow
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.models.dto.CartItemWithProductDetails
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.screens.holder.HolderViewModel
import com.mustfaibra.roffu.sealed.MenuOption
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.getDp
import com.skydoves.whatif.whatIfNotNull
import kotlinx.coroutines.launch
import java.text.DecimalFormat
private const val USD_TO_VND = 25_000
// Hàm định dạng số thành chuỗi tiền tệ Việt Nam
private fun formatVietnamCurrency(amount: Long): String {
    return amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₫"
}

@Composable
fun CartScreen(
    navController: NavHostController,
    user: User?,
    cartViewModel: CartViewModel = hiltViewModel(),
    holderViewModel: HolderViewModel = hiltViewModel(),
    onProductClicked: (productId: Int) -> Unit,
    onCheckoutRequest: () -> Unit,
    onNavigationRequested: (route: String, removePreviousRoute: Boolean) -> Unit,
    onUserNotAuthorized: () -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
) {
    val cartItemsWithDetails by cartViewModel.cartItemsWithDetails
    val isLoading by cartViewModel.isLoading
    val error by cartViewModel.error
    val context = androidx.compose.ui.platform.LocalContext.current

    // Gọi API để lấy giỏ hàng
    LaunchedEffect(Unit) {
        cartViewModel.fetchCart(context)
    }

    // Quản lý trạng thái checkbox
    val checkedStates = remember { mutableStateMapOf<Int, Boolean>() }
    LaunchedEffect(cartItemsWithDetails) {
        cartItemsWithDetails.forEach { item ->
            if (item.id !in checkedStates) {
                checkedStates[item.id] = true // Mặc định chọn tất cả
            }
        }
    }

    // Tính tổng giá từ API
    val totalPrice by remember(cartItemsWithDetails, checkedStates) {
        derivedStateOf {
            cartItemsWithDetails
                .filter { checkedStates[it.id] == true }
                .sumOf { it.unitPrice.toDouble() * it.quantity }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomIconButton(
                    icon = Icons.Default.ArrowBack,
                    backgroundColor = Color.White,
                    iconTint = Color.Black,
                    onButtonClicked = { onNavigationRequested(Screen.Home.route, false) },
                    elevation = 1.dp,
                )
                Text(
                    text = "Giỏ hàng",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                )
                CustomIconButton(
                    icon = Icons.Default.Delete,
                    backgroundColor = Color.White,
                    iconTint = Color.Black,
                    onButtonClicked = {
                        cartViewModel.clearCart(context)
                    },
                    elevation = 1.dp,
                )
            }
            // Items
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hiển thị loading
                if (isLoading) {
                    item {
                        androidx.compose.material.CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .wrapContentSize(Alignment.Center),
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
                // Hiển thị lỗi
                else if (error != null) {
                    item {
                        Text(
                            text = "Lỗi: $error",
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                // Hiển thị dữ liệu từ API
                else if (cartItemsWithDetails.isNotEmpty()) {
                    items(cartItemsWithDetails, key = { it.id }) { item ->
                        CartItemModern(
                            cartId = item.id,
                            productName = item.productName,
                            productImage = item.productImage,
                            productPrice = item.unitPrice.toDouble(),
                            productColor = "Brand: ${item.productBrand}",
                            currentQty = item.quantity,
                            isChecked = checkedStates[item.id] == true,
                            onCheckedChange = { checked -> checkedStates[item.id] = checked },
                            onQuantityChanged = { qty ->
                                cartViewModel.updateQuantity(item.id, qty, context)
                            },
                            onProductRemoved = {
                                cartViewModel.removeCartItem(item.id, context)
                            }
                        )
                    }
                }
                // Hiển thị giỏ hàng trống
                else {
                    item {
                        Text(
                            text = "Giỏ hàng trống",
                            style = MaterialTheme.typography.body1,
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Total & Checkout
            if (cartItemsWithDetails.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = formatVietnamCurrency(totalPrice.toLong()),
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colors.primary
                        )
                    }
                    CustomButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        text = "Thanh toán",
                        textStyle = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                        buttonColor = Color(0xFF0052CC),
                        shape = RoundedCornerShape(16.dp),
                        padding = PaddingValues(0.dp),
                        onButtonClicked = {
                            user.whatIfNotNull(
                                whatIf = {
                                    val selectedItems = cartItemsWithDetails.filter { item ->
                                        checkedStates[item.id] == true
                                    }
                                    Log.d("CartScreen", "Selected items count: ${selectedItems.size}, items: $selectedItems")
                                    Log.d("CartScreen", "Checkbox states: $checkedStates")
                                    if (selectedItems.isEmpty()) {
                                        onToastRequested("Vui lòng chọn ít nhất một sản phẩm", Color.Red)
                                        return@whatIfNotNull
                                    }
                                    holderViewModel.selectedCartItems.clear()
                                    holderViewModel.selectedCartItems.addAll(
                                        selectedItems.map { item ->
                                            com.mustfaibra.roffu.models.CartItem(
                                                cartId = item.id,
                                                productId = item.productId,
                                                quantity = item.quantity
                                            ).apply {
                                                product = com.mustfaibra.roffu.models.dto.Product(
                                                    id = item.productId,
                                                    barcode = "",
                                                    product_name = item.productName,
                                                    description = item.productDescription,
                                                    price = item.unitPrice,
                                                    category_id = 0,
                                                    brand_id = 0,
                                                    created_at = "",
                                                    updated_at = "",
                                                    quantity = item.quantity,
                                                    variants = emptyList(),
                                                    images = listOf(
                                                        com.mustfaibra.roffu.models.dto.Image(
                                                            id = 0,
                                                            product_id = item.productId,
                                                            image_url = item.productImage,
                                                            is_primary = true,
                                                            upload_date = ""
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    )
                                    Log.d("CartScreen", "HolderViewModel selectedCartItems: ${holderViewModel.selectedCartItems}")
                                    cartViewModel.syncCartItems(
                                        navController = navController,
                                        selectedItems = selectedItems,
                                        onSyncFailed = {
                                            onToastRequested("Không thể đồng bộ giỏ hàng", Color.Red)
                                        },
                                        onSyncSuccess = {
                                            // Không cần gọi onNavigationRequested vì điều hướng được xử lý trong syncCartItems
                                        }
                                    )
                                },
                                whatIfNot = onUserNotAuthorized
                            )
                        },
                        contentColor = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemModern(
    cartId: Int,
    productName: String,
    productImage: Any,
    productPrice: Double,
    productColor: String,
    currentQty: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onQuantityChanged: (Int) -> Unit,
    onProductRemoved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val decimalFormat = DecimalFormat("#,###")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(22.dp))
            .padding(vertical = 20.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(top = 14.dp)
        )
        Spacer(Modifier.width(12.dp))
        // Xử lý hiển thị hình ảnh
        val imagePainter = when (productImage) {
            is String -> {
                if (productImage.isNotEmpty()) {
                    rememberAsyncImagePainter(productImage)
                } else {

                }
            }
            is Int -> rememberAsyncImagePainter(productImage)
            else -> {}
        }

        Image(
            painter = imagePainter as Painter,
            contentDescription = null,
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF5F5F5))
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = productName,
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222),
                maxLines = 1
            )
            // Đã loại bỏ phần hiển thị Brand (productColor)
            Spacer(Modifier.height(8.dp))
            // Quantity and delete controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { if (currentQty > 1) onQuantityChanged(currentQty - 1) else onProductRemoved() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Giảm", modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "$currentQty",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF222222),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChanged(currentQty + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Tăng", modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .size(width = 72.dp, height = 32.dp)
                ) {
                    IconButton(
                        onClick = onProductRemoved,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Xóa", modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatVietnamCurrency((productPrice * currentQty).toLong()),
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222)
            )
        }
    }
}
