package com.mustfaibra.roffu.screens.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.IconButton as CustomIconButton
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.models.dto.CartItemWithProductDetails
import com.mustfaibra.roffu.models.dto.CartResponse
import com.skydoves.whatif.whatIfNotNull
import com.mustfaibra.roffu.R


// Hàm định dạng số thành chuỗi tiền tệ Việt Nam
private fun formatVietnamCurrency(amount: Long): String {
    return amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₫"
}

@Composable
fun CartScreen(
    user: User?,
    cartViewModel: CartViewModel = hiltViewModel(),
    onProductClicked: (productId: Int) -> Unit,
    onCheckoutRequest: () -> Unit,
    onUserNotAuthorized: () -> Unit,
) {
    // Lấy context để truyền cho fetchCart
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Gọi API để lấy danh sách sản phẩm trong giỏ hàng khi màn hình được hiển thị
    LaunchedEffect(Unit) {
        cartViewModel.fetchCart(context)
    }
    
    // Lấy các trạng thái từ CartViewModel
    val isLoading = cartViewModel.isLoading.value
    val error = cartViewModel.error.value
    val cartItemsWithDetails = cartViewModel.cartItemsWithDetails.value
    val holderViewModel: com.mustfaibra.roffu.screens.holder.HolderViewModel = hiltViewModel()
    val cartItems = holderViewModel.cartItems

    val checkedStates = remember { mutableStateMapOf<Int, Boolean>() }
    cartItems.forEach { item ->
        val id = item.cartId ?: return@forEach
        if (id !in checkedStates) checkedStates[id] = true
    }

    val selectedItems by remember {
        derivedStateOf { cartItems.filter { it.cartId?.let { id -> checkedStates[id] } == true } }
    }

    val totalPrice by remember(selectedItems) {
        derivedStateOf { selectedItems.sumOf { it.quantity * (it.product?.price ?: 0.0) } }
    }

    LaunchedEffect(selectedItems) {
        cartViewModel.updateCart(items = selectedItems)
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
                    onButtonClicked = { /*TODO*/ },
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
                    onButtonClicked = { /*TODO*/ },
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
                // Hiển thị loading indicator nếu đang tải dữ liệu
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
                // Hiển thị thông báo lỗi nếu có
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
                // Hiển thị dữ liệu từ API nếu có
                else if (cartItemsWithDetails.isNotEmpty()) {
                    items(cartItemsWithDetails, key = { it.id }) { item ->
                        // Mặc định mọi item đều được chọn
                        val cartId = item.id
                        if (cartId !in checkedStates) checkedStates[cartId] = true
                        
                        CartItemModern(
                            cartId = cartId,
                            productName = item.productName,
                            productImage = if (item.productImage.isNotEmpty()) item.productImage else R.drawable.ic_shopping_bag,
                            productPrice = item.unitPrice.toDouble(), // Đã là tiền Việt
                            productColor = "Brand: ${item.productBrand}", // Hiển thị thương hiệu thay vì màu sắc
                            currentQty = item.quantity,
                            isChecked = checkedStates[cartId] == true,
                            onCheckedChange = { c -> checkedStates[cartId] = c },
                            onQuantityChanged = { q -> 
                                // Cập nhật số lượng sản phẩm thông qua API
                                if (cartId.toString().isNotEmpty()) {
                                    cartViewModel.updateQuantity(cartId, q, context)
                                }
                            },
                            onProductRemoved = { 
                                // Xóa sản phẩm khỏi giỏ hàng thông qua API
                                if (cartId.toString().isNotEmpty()) {
                                    cartViewModel.removeCartItem(cartId, context)
                                }
                            }
                        )
                    }
                } else if (!isLoading && error == null) {
                    // Hiển thị dữ liệu từ holderViewModel nếu không có dữ liệu từ API và không có lỗi
                    items(cartItems, key = { it.cartId ?: 0 }) { item ->
                        item.product?.let { product ->
                            CartItemModern(
                                cartId = item.cartId ?: 0,
                                productName = product.name,
                                productImage = product.image ?: R.drawable.ic_shopping_bag,
                                productPrice = product.price,
                                productColor = buildString {
                                    if (item.size.isNotBlank()) append("Size ${item.size}")
                                    if (item.color.isNotBlank()) {
                                        if (isNotEmpty()) append(", ")
                                        append(item.color)
                                    }
                                },
                                currentQty = item.quantity,
                                isChecked = checkedStates[item.cartId] == true,
                                onCheckedChange = { c -> 
                                    item.cartId?.let { id -> checkedStates[id] = c }
                                },
                                onQuantityChanged = { q -> 
                                    item.cartId?.let { id -> cartViewModel.updateQuantity(id, q, context) }
                                },
                                onProductRemoved = {
                                    item.cartId?.let { id -> cartViewModel.removeCartItem(id, context) }
                                }
                            )
                        }
                    }
                }
            }

            // Total & Checkout
            if (cartItems.isNotEmpty()) {
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
                                // Tính tổng tiền dựa trên dữ liệu từ API hoặc dữ liệu giả lập
                        val displayTotal = if (cartItemsWithDetails.isNotEmpty()) {
                            // Tính tổng tiền dựa trên các mục được chọn từ API
                            cartItemsWithDetails
                                .filter { item -> checkedStates[item.id] == true }
                                .sumOf { item -> item.unitPrice * item.quantity }
                        } else {
                            // Sử dụng tổng tiền từ các mục hiện tại
                            totalPrice.toLong()
                        }
                        
                        Text(
                            text = formatVietnamCurrency(displayTotal),
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
                                whatIf = { cartViewModel.syncCartItems(onSyncFailed = {}, onSyncSuccess = onCheckoutRequest) },
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
        // Xử lý hiển thị hình ảnh dựa trên loại dữ liệu
        val imagePainter = when (productImage) {
            is String -> {
                if (productImage.isNotEmpty()) {
                    rememberAsyncImagePainter(productImage)
                } else {
                    rememberAsyncImagePainter(R.drawable.ic_shopping_bag)
                }
            }
            is Int -> rememberAsyncImagePainter(productImage)
            else -> rememberAsyncImagePainter(R.drawable.ic_shopping_bag)
        }
        
        Image(
            painter = imagePainter,
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
            Spacer(Modifier.height(2.dp))
            Text(
                text = productColor,
                style = MaterialTheme.typography.body1.copy(fontSize = 16.sp),
                color = Color(0xFF888888),
                maxLines = 1
            )
            Spacer(Modifier.height(8.dp))
            // Quantity and delete separated with consistent sizing
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quantity controls (slightly shrunk)
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
                // Delete button matching quantity box height
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
