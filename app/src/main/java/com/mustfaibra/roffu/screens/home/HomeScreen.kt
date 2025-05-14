package com.mustfaibra.roffu.screens.home

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomInputField
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.ProductItemLayout
import com.mustfaibra.roffu.models.Advertisement
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DecimalFormat

// Hàm định dạng giá với dấu chấm sau mỗi 3 chữ số
fun formatPrice(price: Int): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(price).replace(",", ".")
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    cartOffset: IntOffset,
    cartProductsIds: List<Int>,
    bookmarkProductsIds: List<Int>,
    onProductClicked: (productId: Int) -> Unit,
    onCartStateChanged: (productId: Int) -> Unit,
    onBookmarkStateChanged: (productId: Int) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    LaunchedEffect(key1 = Unit) {
        homeViewModel.getBrandsWithProducts()
        homeViewModel.getAllProducts()
    }

    val scope = rememberCoroutineScope()
    val searchQuery by homeViewModel.searchQuery

    val brandsUiState by homeViewModel.brandsUiState
    val brands = homeViewModel.brands

    val currentSelectedBrandIndex by homeViewModel.currentSelectedBrandIndex

    // Trạng thái cho filter
    val showFilterOptions by homeViewModel.showFilterOptions
    val filterType by homeViewModel.filterType

    // Danh sách brands và categories từ API
    val apiBrands = homeViewModel.apiBrands
    val apiCategories = homeViewModel.apiCategories
    val brandsApiState by homeViewModel.brandsApiState
    val categoriesApiState by homeViewModel.categoriesApiState

    // Filter đã chọn
    val selectedBrandId by homeViewModel.selectedBrandId
    val selectedCategoryId by homeViewModel.selectedCategoryId

    val gridState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisibleItemIndex != null && lastVisibleItemIndex >= homeViewModel.allProducts.size - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            homeViewModel.loadMoreProducts()
        }
    }

    LazyVerticalGrid(
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        contentPadding = PaddingValues(horizontal = Dimension.pagePadding),
    ) {
        item(
            span = { GridItemSpan(2) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.discover),
                    style = MaterialTheme.typography.h2,
                )
            }
        }

        item(
            span = { GridItemSpan(2) }
        ) {
            SearchField(
                value = searchQuery,
                onValueChange = { homeViewModel.updateSearchInputValue(it) },
                onFocusChange = {
                    if (it) {
                        onNavigateToSearch()
                    }
                },
                onImeActionClicked = {}
            )
        }

        // Hiển thị danh sách brands hoặc categories nếu đã chọn filter type
        if (filterType != null) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (filterType == HomeViewModel.FilterType.BRAND) "Chọn thương hiệu" else "Chọn danh mục",
                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        when {
                            filterType == HomeViewModel.FilterType.BRAND && brandsApiState == UiState.Loading -> {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Text(
                                    "Đang tải thương hiệu...", 
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.body2
                                )
                            }
                            filterType == HomeViewModel.FilterType.CATEGORY && categoriesApiState == UiState.Loading -> {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Text(
                                    "Đang tải danh mục...", 
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.body2
                                )
                            }
                            filterType == HomeViewModel.FilterType.BRAND -> {
                                if (apiBrands.isEmpty()) {
                                    Text(
                                        "Không có thương hiệu nào",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        item {
                                            FilterChip(
                                                text = "Tất cả",
                                                selected = selectedBrandId == null,
                                                onClick = { homeViewModel.selectBrand(null) }
                                            )
                                        }
                                        items(apiBrands.size) { index ->
                                            val brand = apiBrands[index]
                                            FilterChip(
                                                text = brand.name,
                                                selected = selectedBrandId == brand.id,
                                                onClick = { homeViewModel.selectBrand(brand.id) }
                                            )
                                        }
                                    }
                                }
                            }
                            filterType == HomeViewModel.FilterType.CATEGORY -> {
                                if (apiCategories.isEmpty()) {
                                    Text(
                                        "Không có danh mục nào",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        item {
                                            FilterChip(
                                                text = "Tất cả",
                                                selected = selectedCategoryId == null,
                                                onClick = { homeViewModel.selectCategory(null) }
                                            )
                                        }
                                        items(apiCategories.size) { index ->
                                            val category = apiCategories[index]
                                            FilterChip(
                                                text = category.name,
                                                selected = selectedCategoryId == category.id,
                                                onClick = { homeViewModel.selectCategory(category.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /** Handling what to show depending on brands ui state */
        when (brandsUiState) {
            is UiState.Loading -> {}
            is UiState.Success -> {
                /** Loading finished successfully, Shoes brands row first! */
                item(
                    span = { GridItemSpan(2) }
                ) {
                    ManufacturersSection(
                        brands = brands.map { Triple(it.id, it.name, it.icon) },
                        activeBrandIndex = currentSelectedBrandIndex,
                        onBrandClicked = { homeViewModel.updateCurrentSelectedBrandId(it) }
                    )
                }

                when {
                    currentSelectedBrandIndex == -1 -> {
                        when (homeViewModel.allProductsUiState.value) {
                            is UiState.Loading -> {
                                item(span = { GridItemSpan(2) }) {
                                    Text(
                                        text = "Đang tải sản phẩm...",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                            is UiState.Success -> {
                                items(homeViewModel.allProducts) { product ->
                                    val primaryImage = product.images.find { it.is_primary }?.image_url
                                    ProductItemLayout(
                                        modifier = Modifier.fillMaxWidth(),
                                        cartOffset = cartOffset,
                                        imageUrl = primaryImage ?: "",
                                        price = formatPrice(Integer.parseInt(product.price.toString())),
                                        title = product.product_name,
                                        onBookmark = product.id in bookmarkProductsIds,
                                        onProductClicked = { onProductClicked(product.id) },
                                        onChangeCartState = { onCartStateChanged(product.id) },
                                        onChangeBookmarkState = { onBookmarkStateChanged(product.id) },
                                    )
                                }
                            }
                            is UiState.Error -> {
                                item(span = { GridItemSpan(2) }) {
                                    Text(
                                        text = "Lỗi tải sản phẩm",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        color = androidx.compose.ui.graphics.Color.Red
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                    else -> {
                        val selectedBrandId = brands[currentSelectedBrandIndex].id
                        val filteredProducts = homeViewModel.allProducts.filter { it.brand_id == selectedBrandId }
                        if (filteredProducts.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text = "Không có sản phẩm nào cho thương hiệu này",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            items(filteredProducts) { product ->
                                val primaryImage = product.images.find { it.is_primary }?.image_url
                                ProductItemLayout(
                                    modifier = Modifier.fillMaxWidth(),
                                    cartOffset = cartOffset,
                                    imageUrl = primaryImage ?: "",
                                    price = formatPrice(Integer.parseInt(product.price.toString())),
                                    title = product.product_name,
                                    onBookmark = product.id in bookmarkProductsIds,
                                    onProductClicked = { onProductClicked(product.id) },
                                    onChangeCartState = { onCartStateChanged(product.id) },
                                    onChangeBookmarkState = { onBookmarkStateChanged(product.id) },
                                )
                            }
                        }
                    }
                }
            }
            is UiState.Error -> {}
            else -> {}
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (value: String) -> Unit,
    onFocusChange: (hadFocus: Boolean) -> Unit,
    onImeActionClicked: KeyboardActionScope.() -> Unit,
) {
    CustomInputField(
        modifier = Modifier
            .fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        placeholder = "Bạn đang tìm kiếm gì?",
        textStyle = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
        padding = PaddingValues(
            horizontal = Dimension.pagePadding,
            vertical = Dimension.pagePadding.times(0.7f),
        ),
        backgroundColor = MaterialTheme.colors.surface,
        textColor = MaterialTheme.colors.onBackground,
        imeAction = ImeAction.Search,
        shape = MaterialTheme.shapes.large,
        leadingIcon = {
            Icon(
                modifier = Modifier
                    .padding(end = Dimension.pagePadding.div(2))
                    .size(Dimension.mdIcon.times(0.7f)),
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
            )
        },
        onFocusChange = onFocusChange,
        onKeyboardActionClicked = onImeActionClicked,
    )
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() },
        backgroundColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        elevation = if (selected) 4.dp else 1.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.body2.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
        )
    }
}

@Composable
fun ManufacturersSection(
    brands: List<Triple<Int, String, Int>>,
    activeBrandIndex: Int,
    onBrandClicked: (index: Int) -> Unit,
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding.div(2)),
    ) {
        // Thêm mục "Tất cả"
        item {
            val backgroundColor = if (activeBrandIndex == -1) MaterialTheme.colors.primary
            else MaterialTheme.colors.background

            val contentColor = if (activeBrandIndex == -1) MaterialTheme.colors.onPrimary
            else MaterialTheme.colors.onBackground

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimension.xs),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable { onBrandClicked(-1) }
                    .padding(
                        horizontal = Dimension.md,
                        vertical = Dimension.sm,
                    )
            ) {
                AsyncImage(
                    model = R.drawable.ic_all,
                    contentDescription = null,
                    modifier = Modifier.size(Dimension.smIcon),
                    colorFilter = ColorFilter.tint(contentColor),
                )
                if (activeBrandIndex == -1) {
                    Text(
                        text = "Tất cả",
                        style = MaterialTheme.typography.body1,
                        color = contentColor,
                    )
                }
            }
        }
        
        // Thêm nút Filter ngay sau nút All
        item {
            val showFilterOptions by homeViewModel.showFilterOptions
            
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimension.xs),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .clickable { homeViewModel.toggleFilterOptions() }
                        .padding(
                            horizontal = Dimension.md,
                            vertical = Dimension.sm,
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filtering_slidebars),
                        contentDescription = "Filter",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(Dimension.smIcon)
                    )
                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.primary
                    )
                }
                
                // Dropdown menu cho filter options
                DropdownMenu(
                    expanded = showFilterOptions,
                    onDismissRequest = { homeViewModel.toggleFilterOptions() },
                    properties = PopupProperties(focusable = true)
                ) {
                    DropdownMenuItem(onClick = {
                        homeViewModel.selectFilterType(HomeViewModel.FilterType.BRAND)
                        homeViewModel.toggleFilterOptions()
                    }) {
                        Text("Brand")
                    }
                    DropdownMenuItem(onClick = {
                        homeViewModel.selectFilterType(HomeViewModel.FilterType.CATEGORY)
                        homeViewModel.toggleFilterOptions()
                    }) {
                        Text("Category")
                    }
                }
            }
        }
        // Các thương hiệu khác
        itemsIndexed(brands) { index, (_, name, icon) ->
            val backgroundColor = if (activeBrandIndex == index) MaterialTheme.colors.primary
            else MaterialTheme.colors.background

            val contentColor = if (activeBrandIndex == index) MaterialTheme.colors.onPrimary
            else MaterialTheme.colors.onBackground

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimension.xs),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable { onBrandClicked(index) }
                    .padding(
                        horizontal = Dimension.md,
                        vertical = Dimension.sm,
                    )
            ) {
                AsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Dimension.smIcon),
                    colorFilter = ColorFilter.tint(contentColor),
                )
                if (activeBrandIndex == index) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.body1,
                        color = contentColor,
                    )
                }
            }
        }
    }
}