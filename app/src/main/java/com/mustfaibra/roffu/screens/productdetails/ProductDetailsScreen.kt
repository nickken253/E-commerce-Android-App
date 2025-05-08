package com.mustfaibra.roffu.screens.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.ReactiveBookmarkIcon
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.addMoveAnimation
import kotlinx.coroutines.launch

@Composable
fun ProductDetailsScreen(
    productId: Int,
    cartItemsCount: Int,
    isOnBookmarksStateProvider: () -> Boolean,
    onUpdateBookmarksState: (productId: Int) -> Unit,
    onBackRequested: () -> Unit,
    productDetailsViewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        productDetailsViewModel.getProductDetails(productId = productId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(Dimension.pagePadding)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
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
                            duration = androidx.compose.material.SnackbarDuration.Short
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
                is UiState.Loading -> {
                    Text(
                        text = "Đang tải sản phẩm...",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
                is UiState.Success -> {
                    product?.let {
                        ProductContent(
                            product = it,
                            isInCart = isInCart,
                            isOnBookmarksStateProvider = isOnBookmarksStateProvider,
                            onUpdateCartState = { productDetailsViewModel.toggleCartState(productId) },
                            onUpdateBookmarksState = { onUpdateBookmarksState(productId) },
                        )
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Lỗi khi tải sản phẩm. Vui lòng thử lại.",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
                is UiState.Idle -> {}
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
            .fillMaxWidth()
            .padding(Dimension.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        AsyncImage(
            model = product.images.find { it.isPrimary }?.imageUrl
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
        Text(
            text = product.productName,
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Black),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .addMoveAnimation(
                    orientation = com.mustfaibra.roffu.sealed.Orientation.Vertical,
                    from = 100.dp,
                    to = 0.dp,
                    duration = 700,
                ),
        )
        Text(
            text = product.description,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Justify,
            modifier = Modifier
                .fillMaxWidth()
                .addMoveAnimation(
                    orientation = com.mustfaibra.roffu.sealed.Orientation.Vertical,
                    from = 200.dp,
                    to = 0.dp,
                    duration = 700,
                ),
        )
        Text(
            text = "Giá: ${product.price} VNĐ",
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .addMoveAnimation(
                    orientation = com.mustfaibra.roffu.sealed.Orientation.Vertical,
                    from = 200.dp,
                    to = 0.dp,
                    duration = 700,
                ),
        )
        Text(
            text = "Số lượng còn: ${product.quantity}",
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .fillMaxWidth()
                .addMoveAnimation(
                    orientation = com.mustfaibra.roffu.sealed.Orientation.Vertical,
                    from = 200.dp,
                    to = 0.dp,
                    duration = 700,
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimension.pagePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomButton(
                modifier = Modifier
                    .weight(1f)
                    .addMoveAnimation(
                        orientation = com.mustfaibra.roffu.sealed.Orientation.Vertical,
                        from = 200.dp,
                        to = 0.dp,
                        duration = 700,
                    ),
                text = if (isInCart) "Xóa khỏi giỏ" else "Thêm vào giỏ",
                onButtonClicked = onUpdateCartState,
                buttonColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                shape = RoundedCornerShape(50),
                textStyle = MaterialTheme.typography.button,
            )
            Spacer(modifier = Modifier.width(Dimension.pagePadding))
            ReactiveBookmarkIcon(
                modifier = Modifier
                    .addMoveAnimation(
                        orientation = com.mustfaibra.roffu.sealed.Orientation.Horizontal,
                        from = 60.dp,
                        to = 0.dp,
                        duration = 700,
                    ),
                iconSize = Dimension.smIcon,
                isOnBookmarks = isOnBookmarksStateProvider(),
                onBookmarkChange = onUpdateBookmarksState,
            )
        }
    }
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
            onButtonClicked = onBackRequested,
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