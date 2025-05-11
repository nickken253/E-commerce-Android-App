package com.mustfaibra.roffu.screens.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
<<<<<<< HEAD
import androidx.compose.foundation.clickable
=======
>>>>>>> hieuluu2
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
<<<<<<< HEAD
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
=======
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
>>>>>>> hieuluu2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
<<<<<<< HEAD
import androidx.compose.ui.draw.shadow
=======
>>>>>>> hieuluu2
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
<<<<<<< HEAD
=======
import androidx.compose.ui.unit.sp
>>>>>>> hieuluu2
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.IconButton as CustomIconButton
import com.mustfaibra.roffu.models.User
<<<<<<< HEAD
import com.mustfaibra.roffu.sealed.MenuOption
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.getDp
import com.skydoves.whatif.whatIfNotNull
import kotlinx.coroutines.launch
import java.text.DecimalFormat
=======
import com.skydoves.whatif.whatIfNotNull

private const val USD_TO_VND = 25_000
>>>>>>> hieuluu2

@Composable
fun CartScreen(
    user: User?,
    cartViewModel: CartViewModel = hiltViewModel(),
    onProductClicked: (productId: Int) -> Unit,
    onCheckoutRequest: () -> Unit,
    onUserNotAuthorized: () -> Unit,
) {
<<<<<<< HEAD
    val cartItems by cartViewModel.cartItems
    val cartUiState by cartViewModel.cartUiState
    val totalPrice by cartViewModel.totalPrice
    val cartOptionsMenuExpanded by cartViewModel.cartOptionsMenuExpanded
    val isSyncingCart by cartViewModel.isSyncingCart

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val decimalFormat = DecimalFormat("#,###")

    LaunchedEffect(cartUiState) {
        if (cartUiState is UiState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Lỗi khi tải giỏ hàng")
            }
        }
    }

    if (isSyncingCart) {
        SimpleLoadingDialog(title = "Đang đồng bộ giỏ hàng...")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            var cardHeight by remember { mutableStateOf(0) }

            when (cartUiState) {
                is UiState.Loading -> {
                    Text(
                        text = "Đang tải giỏ hàng...",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    if (cartItems.isEmpty()) {
                        Text(
                            text = "Giỏ hàng trống",
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.background),
                            verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding.div(2)),
                            contentPadding = PaddingValues(bottom = cardHeight.getDp())
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colors.background)
                                        .padding(horizontal = Dimension.pagePadding),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = stringResource(id = R.string.cart),
                                        style = MaterialTheme.typography.h3,
                                    )
                                    PopupOptionsMenu(
                                        icon = painterResource(id = R.drawable.ic_more_vertical),
                                        iconSize = Dimension.smIcon,
                                        iconBackgroundColor = MaterialTheme.colors.background,
                                        menuContentColor = MaterialTheme.colors.onBackground.copy(alpha = 0.8f),
                                        options = listOf(MenuOption.ClearCart),
                                        onOptionsMenuExpandChanges = { cartViewModel.toggleOptionsMenuExpandState() },
                                        onMenuOptionSelected = {
                                            cartViewModel.toggleOptionsMenuExpandState()
                                            when (it) {
                                                is MenuOption.ClearCart -> cartViewModel.clearCart()
                                                else -> {}
                                            }
                                        },
                                        optionsMenuExpanded = cartOptionsMenuExpanded
                                    )
                                }
                            }
                            items(cartItems, key = { it.id ?: it.product_id }) { cartItem ->
                                cartItem.product?.let { product ->
                                    CartItemLayout(
                                        productName = product.product_name,
                                        productImage = product.images.find { it.is_primary }?.image_url,
                                        productPrice = product.price.toDouble(),
                                        currentQty = cartItem.quantity,
                                        onProductClicked = { onProductClicked(product.id) },
                                        onQuantityChanged = { newQuantity ->
                                            cartViewModel.updateQuantity(
                                                productId = product.id,
                                                quantity = newQuantity
                                            )
                                        },
                                        onProductRemoved = {
                                            cartViewModel.removeCartItem(productId = product.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Lỗi khi tải giỏ hàng",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    )
                }
                is UiState.Idle -> {}
            }

            if (cartItems.isNotEmpty() && cartUiState is UiState.Success) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onGloballyPositioned { cardHeight = it.size.height }
                        .shadow(
                            elevation = Dimension.elevation.div(2),
                            shape = RoundedCornerShape(topStartPercent = 15, topEndPercent = 15),
                            spotColor = Color.Blue
                        )
                        .clip(shape = RoundedCornerShape(topStartPercent = 15, topEndPercent = 15))
                        .background(MaterialTheme.colors.background)
                        .padding(all = Dimension.pagePadding),
                    verticalArrangement = Arrangement.spacedBy(Dimension.sm)
                ) {
                    SummaryRow(
                        title = stringResource(id = R.string.total),
                        value = "${decimalFormat.format(totalPrice.toInt())} VND",
                        valueColor = Color.Blue
                    )
                    CustomButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.proceed_to_checkout),
                        textStyle = MaterialTheme.typography.body1,
                        buttonColor = Color(0xFF0052CC),
                        shape = RoundedCornerShape(percent = 35),
                        padding = PaddingValues(all = Dimension.md.times(0.8f)),
                        onButtonClicked = {
                            user.whatIfNotNull(
                                whatIf = {
                                    cartViewModel.syncCartItems(
                                        onSyncFailed = { reason ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Đồng bộ giỏ hàng thất bại: $reason")
                                            }
                                        },
                                        onSyncSuccess = onCheckoutRequest
                                    )
                                },
                                whatIfNot = onUserNotAuthorized
                            )
                        },
                        contentColor = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
=======
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
                            text = String.format("%,.0fđ", totalPrice * 25000),
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
>>>>>>> hieuluu2
    }
}

@Composable
fun CartItemModern(
    cartId: Int,
    productName: String,
<<<<<<< HEAD
    productImage: String?,
=======
    productImage: Any,
>>>>>>> hieuluu2
    productPrice: Double,
    productColor: String,
    currentQty: Int,
<<<<<<< HEAD
    onProductClicked: () -> Unit,
    onQuantityChanged: (qty: Int) -> Unit,
=======
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onQuantityChanged: (Int) -> Unit,
>>>>>>> hieuluu2
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
        Image(
<<<<<<< HEAD
            painter = rememberAsyncImagePainter(model = productImage ?: "https://example.com/placeholder.jpg"),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5E8D7))
=======
            painter = rememberAsyncImagePainter(productImage),
            contentDescription = null,
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF5F5F5))
>>>>>>> hieuluu2
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
<<<<<<< HEAD
                text = "${decimalFormat.format(productPrice.toInt())} VND",
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Bold,
                color = Color.Blue
            )
        }

        /** Quantity Selector */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            DrawableButton(
                painter = painterResource(id = R.drawable.ic_round_remove_24),
                enabled = currentQty > 1,
                onButtonClicked = { onQuantityChanged(currentQty.dec()) },
                backgroundColor = if (currentQty > 1) Color.White else MaterialTheme.colors.background,
                iconTint = if (currentQty > 1) Color.Black else MaterialTheme.colors.onBackground,
                iconSize = Dimension.smIcon.times(0.8f),
                shape = CircleShape
            )
            Text(
                text = "$currentQty",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color.Blue,
                fontWeight = FontWeight.Normal
            )
            IconButton(
                icon = Icons.Rounded.Add,
                onButtonClicked = { onQuantityChanged(currentQty.inc()) },
                backgroundColor = Color.White,
                iconSize = Dimension.smIcon.times(0.8f),
                iconTint = Color.Black,
                shape = CircleShape
            )
            IconButton(
                icon = Icons.Rounded.Delete,
                onButtonClicked = { onProductRemoved() },
                backgroundColor = Color.White,
                iconSize = Dimension.smIcon.times(0.8f),
                iconTint = Color.Black,
                shape = CircleShape
=======
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
>>>>>>> hieuluu2
            )
        }
    }
}
