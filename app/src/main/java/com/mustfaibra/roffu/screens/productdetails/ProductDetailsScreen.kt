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
import com.mustfaibra.roffu.sealed.Screen

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
    val product by productDetailsViewModel.product
    val selectedColor by productDetailsViewModel.selectedColor
    val selectedSize by productDetailsViewModel.selectedSize
    val scale by productDetailsViewModel.sizeScale
    val quantity by productDetailsViewModel.quantity
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            DetailsHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                cartItemsCount = cartItemsCount,
                onBackRequested = onBackRequested,
                onNavigateToCartRequested = {
                    navController.navigate("cart") {
                        launchSingleTop = true
                    }
                }
            )

            // Scrollable content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    product?.let { currentProduct ->
                        // Product Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF5F5F5))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = currentProduct.imagePath,
                                    error = painterResource(id = R.drawable.ic_placeholder)
                                ),
                                contentDescription = "Product Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }

                        // Product Name
                        Text(
                            text = currentProduct.name,
                            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Price
                        Text(
                            text = "${PriceFormatter.formatPrice(currentProduct.price)} VND",
                            style = MaterialTheme.typography.h4.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.primary
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Description
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Mô tả sản phẩm",
                                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = currentProduct.description,
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }

                        // Sizes Section
                        currentProduct.sizes?.let { sizes ->
                            if (sizes.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Kích thước",
                                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            sizes.forEach { size ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (selectedSize == size.size) 
                                                                MaterialTheme.colors.primary 
                                                            else 
                                                                Color(0xFFF5F5F5)
                                                        )
                                                        .clickable { 
                                                            productDetailsViewModel.updateSelectedSize(size.size)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${size.size}",
                                                        color = if (selectedSize == size.size) 
                                                            Color.White 
                                                        else 
                                                            Color.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Quantity Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Số lượng",
                                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
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
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    IconButton(
                                        onClick = { productDetailsViewModel.decreaseQuantity() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text(
                                            text = "-",
                                            style = MaterialTheme.typography.h6
                                        )
                                    }
                                    Text(
                                        text = "$quantity",
                                        style = MaterialTheme.typography.h6
                                    )
                                    IconButton(
                                        onClick = { productDetailsViewModel.increaseQuantity() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text(
                                            text = "+",
                                            style = MaterialTheme.typography.h6
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Thêm vào giỏ hàng",
                        style = MaterialTheme.typography.button
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = {
                        navController.navigate("product-selection/$productId")
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "So sánh",
                        style = MaterialTheme.typography.button
                    )
                }
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
