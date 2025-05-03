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
import com.skydoves.whatif.whatIfNotNull

private const val USD_TO_VND = 25_000

@Composable
fun CartScreen(
    user: User?,
    cartViewModel: CartViewModel = hiltViewModel(),
    onProductClicked: (productId: Int) -> Unit,
    onCheckoutRequest: () -> Unit,
    onUserNotAuthorized: () -> Unit,
) {
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

    val totalVnd by remember(selectedItems) {
        derivedStateOf { selectedItems.sumOf { it.quantity * (it.product?.price ?: 0.0) * USD_TO_VND } }
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
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomIconButton(
                    icon = Icons.Filled.ArrowBack,
                    onButtonClicked = { /* TODO */ },
                    backgroundColor = Color.Transparent,
                    iconTint = Color.Black,
                    iconSize = 22.dp,
                    shape = CircleShape,
                    paddingValue = PaddingValues(0.dp)
                )
                Text(
                    text = "Giỏ hàng",
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF222222),
                    modifier = Modifier.padding(start = 8.dp)
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
                items(cartItems, key = { it.cartId ?: 0 }) { item ->
                    item.product?.let { product ->
                        CartItemModern(
                            cartId = item.cartId!!,
                            productName = product.name,
                            productImage = product.image,
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
                            onCheckedChange = { c -> checkedStates[item.cartId!!] = c },
                            onQuantityChanged = { q -> cartViewModel.updateQuantity(item.cartId!!, q) },
                            onProductRemoved = { cartViewModel.removeCartItem(item.cartId!!) }
                        )
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
                        Text(
                            text = String.format("%,.0fđ", totalVnd),
                            style = MaterialTheme.typography.h5.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color(0xFF0052CC)
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
        Image(
            painter = rememberAsyncImagePainter(productImage),
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
                text = String.format("%,.0fđ", productPrice * USD_TO_VND),
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222)
            )
        }
    }
}
