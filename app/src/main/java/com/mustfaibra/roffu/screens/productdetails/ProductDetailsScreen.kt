package com.mustfaibra.roffu.screens.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

import coil.compose.AsyncImage

import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.AddedToCartDialog
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.ReactiveBookmarkIcon
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.addMoveAnimation
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductDetailsScreen(
    productId: Int,
    cartItemsCount: Int,
    isOnBookmarksStateProvider: () -> Boolean,
    onUpdateBookmarksState: (productId: Int) -> Unit,
    onBackRequested: () -> Unit,
    navController: NavHostController,
    productDetailsViewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val cartUiState by productDetailsViewModel.cartUiState

    LaunchedEffect(Unit) {
        productDetailsViewModel.getProductDetails(productId)
    }
    LaunchedEffect(cartUiState) {
        when (cartUiState) {
            is UiState.Error -> {
                val error = (cartUiState as UiState.Error).error
                coroutineScope.launch {
                    val message = when (error) {
                        com.mustfaibra.roffu.sealed.Error.Unknown -> "Vui lòng đăng nhập để thêm sản phẩm vào giỏ"
                        Error.Network -> "Lỗi mạng. Vui lòng kiểm tra kết nối và thử lại."
                        else -> "Lỗi khi xử lý giỏ hàng. Vui lòng thử lại."
                    }
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            else -> {} // Không làm gì cho các trạng thái khác
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(

            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(padding),
        ) {
            val detailsUiState by productDetailsViewModel.detailsUiState
            val cartUiState by productDetailsViewModel.cartUiState
            val product by productDetailsViewModel.product
            val isInCart by productDetailsViewModel.isInCart

            LaunchedEffect(cartUiState) {
                if (cartUiState is UiState.Error) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Lỗi khi xử lý giỏ hàng. Vui lòng thử lại.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            DetailsHeader(
                modifier = Modifier.addMoveAnimation(
                    orientation = com.mustfaibra.roffu.sealed.Orientation.Vertical,
                    from = (-100).dp,
                    to = 0.dp,
                    duration = 700,
                ),
                cartItemsCount = cartItemsCount,
                onBackRequested = onBackRequested,
            )

            when (detailsUiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Đang tải sản phẩm...", style = MaterialTheme.typography.body1)
                }
                is UiState.Success -> product?.let {
                    ProductContent(
                        product = it,
                        isInCart = isInCart,
                        isOnBookmarksStateProvider = isOnBookmarksStateProvider,
                        onUpdateCartState = { productDetailsViewModel.toggleCartState(productId) },
                        onUpdateBookmarksState = { onUpdateBookmarksState(productId) },
                    )
                }
                is UiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lỗi khi tải sản phẩm. Vui lòng thử lại.",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.error
                    )
                }
                UiState.Idle -> {}
            }
        }
    }
}

@Composable
fun ProductContent(
    product: Product,
    isInCart: Boolean,
    isOnBookmarksStateProvider: () -> Boolean,
    onUpdateCartState: () -> Unit,
    onUpdateBookmarksState: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        ) {
            item {
                AsyncImage(
                    model = product.images.find { it.is_primary }?.image_url
                        ?: "https://example.com/placeholder.jpg",
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(Dimension.pagePadding))
                        .addMoveAnimation(
                            orientation = com.mustfaibra.roffu.sealed.Orientation.Vertical,
                            from = 100.dp,
                            to = 0.dp,
                            duration = 700,
                        ),
                )
            }
            item {
                Text(
                    text = product.product_name,
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Black),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.body1.copy(lineHeight = 22.sp),
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )

            }
            item {
                Text(
                    text = "Giá: ${formatCurrencyVND(product.price)}",
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(
                    text = "Số lượng còn: ${product.quantity}",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.fillMaxWidth()

                )

                // Popup xác nhận đã thêm vào giỏ hàng
                if (showAddedDialog) {
                    val product = productDetailsViewModel.product.value
                    AddedToCartDialog(
                        productName = product?.name ?: "",
                        productImage = product?.image ?: 0,
                        productPrice = product?.price ?: 0.0,
                        onContinue = {
                            showAddedDialog = false
                            navController.popBackStack()
                        },
                        onViewCart = {
                            showAddedDialog = false
                            navController.navigate("cart") {
                                launchSingleTop = true
                            }
                        },
                        onDismiss = { showAddedDialog = false }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimension.pagePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomButton(
                modifier = Modifier.weight(1f),
                text = if (isInCart) "Xóa khỏi giỏ" else "Thêm vào giỏ",
                onButtonClicked = onUpdateCartState,
                buttonColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                shape = RoundedCornerShape(50),
                textStyle = MaterialTheme.typography.button,
            )
            Spacer(modifier = Modifier.width(Dimension.pagePadding))
            ReactiveBookmarkIcon(
                iconSize = Dimension.smIcon,
                isOnBookmarks = isOnBookmarksStateProvider(),
                onBookmarkChange = onUpdateBookmarksState,
            )
        }
    }
}
fun formatCurrencyVND(amount: Long): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)} VNĐ"
}

@Composable
fun DetailsHeader(
    modifier: Modifier = Modifier,
    cartItemsCount: Int,
    onBackRequested: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DrawableButton(
            painter = painterResource(id = R.drawable.ic_arrow_left),
            iconTint = MaterialTheme.colors.onBackground,
            backgroundColor = MaterialTheme.colors.background,
            onButtonClicked = { onBackRequested() },
            shape = MaterialTheme.shapes.medium,
            elevation = Dimension.elevation,
            iconSize = Dimension.smIcon,
            paddingValue = PaddingValues(Dimension.sm),
        )
        Box {
            DrawableButton(
                painter = painterResource(id = R.drawable.ic_shopping_bag),
                iconTint = MaterialTheme.colors.onBackground,
                backgroundColor = MaterialTheme.colors.background,
                onButtonClicked = {},
                shape = MaterialTheme.shapes.medium,
                elevation = Dimension.elevation,
                iconSize = Dimension.smIcon,
                paddingValue = PaddingValues(Dimension.sm),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(Dimension.smIcon.times(0.85f))
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colors.onBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$cartItemsCount",
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.background,
                )
            }
        }
    }
}
