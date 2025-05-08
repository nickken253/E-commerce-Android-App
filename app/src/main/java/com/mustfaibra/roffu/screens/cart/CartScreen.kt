package com.mustfaibra.roffu.screens.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.IconButton
import com.mustfaibra.roffu.components.PopupOptionsMenu
import com.mustfaibra.roffu.components.SimpleLoadingDialog
import com.mustfaibra.roffu.components.SummaryRow
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.sealed.MenuOption
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.getDp
import com.skydoves.whatif.whatIfNotNull
import kotlinx.coroutines.launch

@Composable
fun CartScreen(
    user: User?,
    cartViewModel: CartViewModel = hiltViewModel(),
    onProductClicked: (productId: Int) -> Unit,
    onCheckoutRequest: () -> Unit,
    onUserNotAuthorized: () -> Unit,
) {
    val cartItems by cartViewModel.cartItems
    val cartUiState by cartViewModel.cartUiState
    val totalPrice by cartViewModel.totalPrice
    val cartOptionsMenuExpanded by cartViewModel.cartOptionsMenuExpanded
    val isSyncingCart by cartViewModel.isSyncingCart

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                                        productName = product.productName,
                                        productImage = product.images.find { it.isPrimary }?.imageUrl,
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
                        value = "₹${totalPrice.toInt()}",
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
    }
}

@Composable
fun CartItemLayout(
    productName: String,
    productImage: String?,
    productPrice: Double,
    currentQty: Int,
    onProductClicked: () -> Unit,
    onQuantityChanged: (qty: Int) -> Unit,
    onProductRemoved: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        /** Product Image */
        Image(
            painter = rememberAsyncImagePainter(model = productImage ?: "https://example.com/placeholder.jpg"),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5E8D7))
        )

        /** Product Info */
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = productName,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "₹${productPrice.toInt()}",
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
            )
        }
    }
}