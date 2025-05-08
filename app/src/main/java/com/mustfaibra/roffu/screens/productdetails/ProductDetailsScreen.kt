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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.AddedToCartDialog
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.ReactiveBookmarkIcon
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.addMoveAnimation
import com.mustfaibra.roffu.utils.getValidColor
import com.mustfaibra.roffu.sealed.UiState
import java.text.NumberFormat
import java.util.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.Button
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.PageSize

@Composable
fun ProductDetailsScreen(
    productId: Int,
    navController: NavHostController,
    viewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.getProductDetails(productId = productId)
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                    modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .padding(8.dp),
                tint = MaterialTheme.colors.onBackground
            )
        }

        when (val uiState = viewModel.uiState.value) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Có lỗi xảy ra khi tải thông tin sản phẩm",
                        color = MaterialTheme.colors.error
                    )
                }
            }
            is UiState.Success -> {
                viewModel.product.value?.let { product ->
                    val images = product.images
                    val pagerState = rememberPagerState(pageCount = { images.size })
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Slider ảnh
                        if (images.isNotEmpty()) {
                            HorizontalPager(
                                pageSize = PageSize.Fill,
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) { page ->
                                AsyncImage(
                                    model = images[page].image_url,
                                    contentDescription = product.product_name,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            // Ảnh nhỏ chọn chuyển trang
                            Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                images.forEachIndexed { idx, img ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(56.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .border(
                                                width = if (pagerState.currentPage == idx) 2.dp else 1.dp,
                                                color = if (pagerState.currentPage == idx) MaterialTheme.colors.primary else Color.Gray,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(idx)
                                                }
                                            }
                                    ) {
                                        AsyncImage(
                                            model = img.image_url,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                        // Tên sản phẩm
                        Text(
                            text = product.product_name,
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Khuyến mãi (nếu có)
                        if (product.price <  product.variants.getOrNull(0)?.let { (it as? Map<*, *>)?.get("original_price") as? Double ?: product.price } ?: product.price) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                    Text(
                                    text = "Khuyến mãi",
                                    color = Color.White,
                        modifier = Modifier
                                        .background(Color.Red, MaterialTheme.shapes.small)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                // Giả lập thời gian kết thúc khuyến mãi
                                Text(
                                    text = "Kết thúc trong 00:11",
                                    color = Color.Red,
                        modifier = Modifier
                                        .background(Color(0xFFFFE0E0), MaterialTheme.shapes.small)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        // Giá bán & giá gốc
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₫${product.price.toInt()}",
                                style = MaterialTheme.typography.h5.copy(
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val originalPrice = product.variants.getOrNull(0)?.let { (it as? Map<*, *>)?.get("original_price") as? Double }
                            if (originalPrice != null && originalPrice > product.price) {
                                Text(
                                    text = "₫${originalPrice.toInt()}",
                                    style = MaterialTheme.typography.body2.copy(
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                )
                            }
                        }
                        // Đánh giá (nếu có)
                        if (product.variants.getOrNull(0)?.let { (it as? Map<*, *>)?.get("rating") as? Double } != null) {
                            val rating = product.variants[0].let { (it as? Map<*, *>)?.get("rating") as? Double } ?: 0.0
                            val reviewCount = product.variants[0].let { (it as? Map<*, *>)?.get("review_count") as? Int } ?: 0
                            Text(
                                text = "${rating}⭐ Đánh giá sản phẩm ($reviewCount)",
                                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFA000)
                            )
                        }
                        // Mô tả sản phẩm
                        if (product.description.isNotBlank()) {
                            Text(
                                text = "MÔ TẢ SẢN PHẨM",
                                style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.body2
                            )
                        }
                        // Hashtag (nếu có)
                        if (product.variants.getOrNull(0)?.let { (it as? Map<*, *>)?.get("hashtags") as? String } != null) {
                            val hashtags = product.variants[0].let { (it as? Map<*, *>)?.get("hashtags") as? String } ?: ""
                            Text(
                                text = hashtags,
                                style = MaterialTheme.typography.caption.copy(color = Color.Gray)
                            )
                        }
                        // Ảnh mô tả thêm (nếu có)
                        if (product.images.size > 1) {
                            product.images.drop(1).forEach { image ->
                                AsyncImage(
                                    model = image.image_url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(vertical = 4.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        // Nút chức năng
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.addToCart(product.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Thêm vào giỏ hàng")
                            }
                            Button(
                                onClick = { /* TODO: Xử lý thanh toán */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Thanh toán ngay")
                            }
                            Button(
                                onClick = { /* TODO: So sánh sản phẩm */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("So sánh")
                            }
                        }
                    }
                }
            }
            is UiState.Idle -> {}
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
        Box {
            DrawableButton(
                painter = painterResource(id = R.drawable.ic_shopping_bag),
                iconTint = MaterialTheme.colors.onBackground,
                backgroundColor = MaterialTheme.colors.background,
                onButtonClicked = {
//                    onNavigateToCartRequested()
                },
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
