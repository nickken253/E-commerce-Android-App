package com.mustfaibra.roffu.screens.productdetails

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.*
import com.mustfaibra.roffu.sealed.Orientation
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductDetailsScreen(
    productId: Int,
    cartItemsCount: Int,
    isOnCartStateProvider: () -> Boolean,
    isOnBookmarksStateProvider: () -> Boolean, // Giữ lại để tránh lỗi khi gọi hàm
    onUpdateCartState: (productId: Int) -> Unit,
    onUpdateBookmarksState: (productId: Int) -> Unit, // Giữ lại để tránh lỗi khi gọi hàm
    onBackRequested: () -> Unit,
    navController: NavHostController,
    productDetailsViewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = Unit) {
        productDetailsViewModel.getProductDetails(productId = productId)
    }

    var showAddedDialog by remember { mutableStateOf(false) }
    val product by productDetailsViewModel.product
    val selectedColor by productDetailsViewModel.selectedColor
    val selectedSize by productDetailsViewModel.selectedSize
    val scale by productDetailsViewModel.sizeScale
    val quantity by productDetailsViewModel.quantity
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(Dimension.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        /** Details screen header */
        DetailsHeader(
            modifier = Modifier
                .addMoveAnimation(
                    orientation = Orientation.Vertical,
                    from = (-100).dp,
                    to = 0.dp,
                    duration = 700,
                ),
            cartItemsCount = cartItemsCount,
            onBackRequested = onBackRequested,
            // Xóa tham số isOnBookmarks và onBookmarkChange
            onNavigateToCartRequested = {
                navController.navigate("cart") {
                    launchSingleTop = true
                }
            }
        )

        product?.let { currentProduct ->
            /** Product's name */
            Text(
                text = currentProduct.name,
                style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Black),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimension.pagePadding)
                    .addMoveAnimation(
                        orientation = Orientation.Vertical,
                        from = 100.dp,
                        to = 0.dp,
                        duration = 700,
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimension.pagePadding)
                    .weight(1f),
            ) {
                // Product image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .align(Alignment.TopCenter)
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val animatedOffset by infiniteTransition.animateFloat(
                        initialValue = -10f,
                        targetValue = 10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1300,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = currentProduct.imagePath,
                            error = painterResource(id = R.drawable.ic_placeholder)
                        ),
                        contentDescription = "Product Image",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .offset { IntOffset(y = animatedOffset.toInt(), x = 0) }
                            .fillMaxSize()
                            .padding(16.dp)
                            .rotate(-(40f))
                    )
                }

                // Product description
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 16.dp)
                        .align(Alignment.Center)
                        .offset(y = 160.dp)
                        .background(MaterialTheme.colors.background.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Mô tả sản phẩm",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.5f))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val scrollState = rememberScrollState()
                    Text(
                        text = currentProduct.description,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    )
                }
            }

            /** Sizes section */
            currentProduct.sizes?.let { sizes ->
                if (sizes.isNotEmpty()) {
                    SizesSection(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .addMoveAnimation(
                                orientation = Orientation.Horizontal,
                                from = (-60).dp,
                                to = 0.dp,
                                duration = 700,
                            ),
                        sizes = sizes.map { it.size },
                        pickedSizeProvider = { selectedSize },
                        onSizePicked = productDetailsViewModel::updateSelectedSize,
                    )
                }
            }

            /** Price and quantity section */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .addMoveAnimation(
                        orientation = Orientation.Vertical,
                        from = 200.dp,
                        to = 0.dp,
                        duration = 700,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${PriceFormatter.formatPrice(currentProduct.price)} VND",
                    style = MaterialTheme.typography.h4,
                )

                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .clickable { productDetailsViewModel.decreaseQuantity() }
                            .padding(horizontal = 8.dp)
                    )

                    Text(
                        text = "$quantity",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Text(
                        text = "+",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .clickable { productDetailsViewModel.increaseQuantity() }
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            /** Add to cart button */
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                onClick = {
                    productDetailsViewModel.addToCart(
                        productId = productId,
                        size = selectedSize.toString(),
                        color = selectedColor,
                        context = context
                    )
                    showAddedDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                )
            ) {
                Text(
                    text = "Add to cart",
                    style = MaterialTheme.typography.button
                )
            }
        }

        if (showAddedDialog) {
            AddedToCartDialog(
                productName = product?.name ?: "",
                productImage = product?.imagePath ?: (product?.image ?: R.drawable.adidas_48),
                productPrice = product?.price ?: 0.0,
                productQuantity = quantity,
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

@Composable
fun DetailsHeader(
    modifier: Modifier = Modifier,
    cartItemsCount: Int,
    onBackRequested: () -> Unit,
    // Xóa tham số isOnBookmarks và onBookmarkChange
    onNavigateToCartRequested: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Xóa nút bookmark
            
            // Giỏ hàng
            Box {
                DrawableButton(
                    painter = painterResource(id = R.drawable.ic_shopping_bag),
                    iconTint = MaterialTheme.colors.onBackground,
                    backgroundColor = MaterialTheme.colors.background,
                    onButtonClicked = onNavigateToCartRequested,
                    shape = MaterialTheme.shapes.medium,
                    elevation = Dimension.elevation,
                    iconSize = Dimension.smIcon,
                    paddingValue = PaddingValues(Dimension.sm),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(Dimension.smIcon.times(0.85f))
                        .clip(CircleShape)
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
}

@Composable
fun SizesSection(
    modifier: Modifier,
    sizes: List<Int>,
    pickedSizeProvider: () -> Int,
    onSizePicked: (size: Int) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement
            .spacedBy(Dimension.pagePadding.div(2)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.size),
            style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.SemiBold),
        )
        sizes.forEach { size ->
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = Dimension.elevation,
                        shape = MaterialTheme.shapes.small,
                    )
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (pickedSizeProvider() == size) MaterialTheme.colors.primary
                        else MaterialTheme.colors.background,
                    )
                    .clickable { onSizePicked(size) }
                    .padding(Dimension.sm)
            ) {
                Text(
                    text = "$size",
                    style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.SemiBold),
                    color = if (pickedSizeProvider() == size) MaterialTheme.colors.onPrimary
                    else MaterialTheme.colors.onBackground,
                )
            }
        }
    }
}

@Composable
fun ColorsSection(
    modifier: Modifier,
    colors: List<String>,
    pickedColorProvider: () -> String,
    onColorPicked: (name: String) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement
            .spacedBy(Dimension.pagePadding.div(2)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(Dimension.smIcon)
                    .border(
                        width = 2.dp,
                        color = if (pickedColorProvider() == color) MaterialTheme.colors.primary else Color.Transparent,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(Dimension.elevation)
                    .clip(MaterialTheme.shapes.small)
                    .background(color = Color(color.getValidColor()))
                    .clickable { onColorPicked(color) }
            )
        }
        Text(
            text = stringResource(R.string.color),
            style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
