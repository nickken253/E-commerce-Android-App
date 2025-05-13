package com.mustfaibra.roffu.screens.productdetails

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.AddedToCartDialog
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.ReactiveBookmarkIcon
import com.mustfaibra.roffu.sealed.Orientation
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.PriceFormatter
import com.mustfaibra.roffu.utils.addMoveAnimation
import com.mustfaibra.roffu.utils.getValidColor

@Composable
fun ProductDetailsScreen(
    productId: Int,
    cartItemsCount: Int,
    isOnCartStateProvider: () -> Boolean,
    isOnBookmarksStateProvider: () -> Boolean,
    onUpdateCartState: (productId: Int) -> Unit,
    onUpdateBookmarksState: (productId: Int) -> Unit,
    onBackRequested: () -> Unit,
    navController: NavHostController,
    productDetailsViewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = Unit) {
        productDetailsViewModel.getProductDetails(productId = productId)
    }

    var showAddedDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(Dimension.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        val product by remember { productDetailsViewModel.product }
        val color by remember { productDetailsViewModel.selectedColor }
        val size by remember { productDetailsViewModel.selectedSize }
        val scale by remember { productDetailsViewModel.sizeScale }
        val animatedScale by animateFloatAsState(
            targetValue = scale,
            animationSpec = TweenSpec(
                durationMillis = 500,
                easing = LinearEasing,
            )
        )

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
            isOnBookmarks = isOnBookmarksStateProvider(),
            onBookmarkChange = { onUpdateBookmarksState(productId) },
            onNavigateToCartRequested = {
                // Chuyển đến màn hình giỏ hàng
                navController.navigate("cart") {
                    launchSingleTop = true
                }
            }
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            product?.let {
                /** Product's name */
                Text(
                    modifier = Modifier
                        .padding(horizontal = Dimension.pagePadding)
                        .addMoveAnimation(
                            orientation = Orientation.Vertical,
                            from = 100.dp,
                            to = 0.dp,
                            duration = 700,
                        ),
                    text = it.name,
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Black),
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimension.pagePadding)
                        .weight(1f),
                ) {
                    // Phần hiển thị ảnh sản phẩm
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val animatedOffset by infiniteTransition.animateFloat(
                            initialValue = -10f, targetValue = 10f,
                            animationSpec = InfiniteRepeatableSpec(
                                animation = TweenSpec(
                                    durationMillis = 1300,
                                    easing = LinearEasing,
                                ),
                                repeatMode = RepeatMode.Reverse,
                            ),
                        )
                        
                        // Hiển thị ảnh sản phẩm từ URL với scale từ tâm
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = it.imagePath ?: R.drawable.adidas_48
                            ),
                            contentDescription = "Product Image",
                            contentScale = androidx.compose.ui.layout.ContentScale.Inside,
                            modifier = Modifier
                                .offset { IntOffset(y = animatedOffset.toInt(), x = 0) }
                                .fillMaxSize()
                                .padding(16.dp)
                                .rotate(-(40f))
                        )
                    }
                    
                    // Phần mô tả sản phẩm cố định
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
                        
                        // Kẻ ngang ngăn cách - sử dụng Box với background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colors.primary.copy(alpha = 0.5f))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val scrollState = rememberScrollState()
                        Text(
                            text = it.description,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState)
                        )
                    }
                }
                
                /** Sizes section */
                if (it.sizes != null && it.sizes!!.isNotEmpty()) {
                    SizesSection(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .addMoveAnimation(
                                orientation = Orientation.Horizontal,
                                from = (-60).dp,
                                to = 0.dp,
                                duration = 700,
                            ),
                        sizes = it.sizes!!.map { size -> size.size },
                        pickedSizeProvider = { size },
                        onSizePicked = productDetailsViewModel::updateSelectedSize,
                    )
                }
                
                /** Price section and quantity adjustment */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                        .addMoveAnimation(
                            orientation = Orientation.Vertical,
                            from = 200.dp,
                            to = 0.dp,
                            duration = 700,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Giá tiền
                    Text(
                        text = "${PriceFormatter.formatPrice(it.price)} VND",
                        style = MaterialTheme.typography.h4,
                    )
                    
                    // Điều chỉnh số lượng
                    val quantity = productDetailsViewModel.quantity.value
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
                        // Nút giảm
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                .clickable { productDetailsViewModel.decreaseQuantity() }
                                .padding(horizontal = 8.dp)
                        )
                        
                        // Số lượng
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        // Nút tăng
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                .clickable { productDetailsViewModel.increaseQuantity() }
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
                    /** colors section */
                    if (it.colors != null && it.colors!!.size > 1) {
                        ColorsSection(
                            modifier = Modifier
                                .align(Alignment.End)
                                .addMoveAnimation(
                                    orientation = Orientation.Vertical,
                                    from = 200.dp,
                                    to = 0.dp,
                                    duration = 700,
                                ),
                            colors = it.colors!!.map { color -> color.colorName },
                            pickedColorProvider = { color },
                            onColorPicked = productDetailsViewModel::updateSelectedColor,
                        )
                    }
                }
                /** Add / Remove from cart button */
                // Lấy context để truyền vào ViewModel
                val context = androidx.compose.ui.platform.LocalContext.current
                
                CustomButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    text = "Add to cart",
                    buttonColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    onButtonClicked = {
                        // Lấy thông tin biến thể đang chọn
                        val selectedSize = productDetailsViewModel.selectedSize.value.toString()
                        val selectedColor = productDetailsViewModel.selectedColor.value
                        // Gọi API để thêm sản phẩm vào giỏ hàng
                        productDetailsViewModel.addToCart(
                            productId = productId,
                            size = selectedSize,
                            color = selectedColor,
                            context = context
                        )
                        showAddedDialog = true
                    },
                )

                // Popup xác nhận đã thêm vào giỏ hàng
                if (showAddedDialog) {
                    val product = productDetailsViewModel.product.value
                    AddedToCartDialog(
                        productName = product?.name ?: "",
                        // Sử dụng imagePath từ API nếu có, nếu không thì dùng image cũ
                        productImage = product?.imagePath ?: (product?.image ?: R.drawable.adidas_48),
                        productPrice = product?.price ?: 0.0,
                        productQuantity = productDetailsViewModel.quantity.value,
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
    }


@Composable
fun DetailsHeader(
    modifier: Modifier = Modifier,
    cartItemsCount: Int,
    onBackRequested: () -> Unit,
    isOnBookmarks: Boolean = false,
    onBookmarkChange: () -> Unit = {},
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
            // Nút bookmark
            ReactiveBookmarkIcon(
                modifier = Modifier,
                iconSize = Dimension.smIcon,
                isOnBookmarks = isOnBookmarks,
                onBookmarkChange = { onBookmarkChange() }
            )
            
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
