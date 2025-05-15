package com.mustfaibra.roffu.screens.search

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mustfaibra.roffu.models.SearchFilters
import com.mustfaibra.roffu.models.SortOptions
import com.mustfaibra.roffu.models.SortOrderOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.mustfaibra.roffu.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showFilters by viewModel.showFilters.collectAsState()
    val currentFilters by viewModel.currentFilters.collectAsState()
    val hasMoreData by viewModel.hasMoreData.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val recommendations by viewModel.recommendations.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        // Search bar với thiết kế mới
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF333333)
                )
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isSearchFocused = it.isFocused },
                placeholder = { Text("Tìm kiếm sản phẩm...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF0052CC),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    backgroundColor = Color(0xFFF5F5F5)
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF666666)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.resetSearch()
                        viewModel.onSearch(searchQuery)
                    }
                )
            )

            // Thêm nút filter
            if (searchResults.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.toggleFilters() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFF333333)
                    )
                }
            }
        }

        // Search suggestions and history với thiết kế mới
        AnimatedVisibility(
            visible = isSearchFocused || (searchResults.isEmpty() && !isLoading && searchQuery.isEmpty()),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                if (searchQuery.isEmpty()) {
                    // Gợi ý tìm kiếm
                    if (recommendations.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Gợi ý cho bạn",
                                    style = MaterialTheme.typography.subtitle1.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )
                                )
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = "Suggestions",
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        items(recommendations.take(5)) { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        searchQuery = suggestion
                                        focusManager.clearFocus()
                                        viewModel.onSearch(suggestion)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.body1.copy(
                                        color = Color(0xFF333333)
                                    )
                                )
                            }
                        }
                        
                        item { 
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }

                    // Lịch sử tìm kiếm
                    if (searchHistory.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lịch sử tìm kiếm",
                                    style = MaterialTheme.typography.subtitle1.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )
                                )
                            }
                        }
                        
                        items(searchHistory.take(5)) { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        searchQuery = historyItem
                                        focusManager.clearFocus()
                                        viewModel.onSearch(historyItem)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = historyItem,
                                    style = MaterialTheme.typography.body1.copy(
                                        color = Color(0xFF333333)
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // Gợi ý tìm kiếm khi đang nhập
                    items(searchSuggestions.take(5)) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    searchQuery = suggestion
                                    focusManager.clearFocus()
                                    viewModel.onSearch(suggestion)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.body1.copy(
                                    color = Color(0xFF333333)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Filters
        AnimatedVisibility(
            visible = showFilters && searchResults.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Categories
                var expandedCategories by remember { mutableStateOf(false) }
                val categories by viewModel.categories.collectAsState(initial = emptyList())
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categories.find { it.id == currentFilters.categoryId }?.name ?: "Chọn danh mục",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Danh mục") },
                        trailingIcon = {
                            IconButton(onClick = { expandedCategories = !expandedCategories }) {
                                Icon(
                                    if (expandedCategories) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = if (expandedCategories) "Thu gọn" else "Mở rộng"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedCategories,
                        onDismissRequest = { expandedCategories = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                viewModel.updateFilters(
                                    currentFilters.copy(categoryId = null)
                                )
                                expandedCategories = false
                            }
                        ) {
                            Text("Tất cả danh mục")
                        }
                        categories.forEach { category ->
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.updateFilters(
                                        currentFilters.copy(categoryId = category.id)
                                    )
                                    expandedCategories = false
                                }
                            ) {
                                Text(category.name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Brands
                var expandedBrands by remember { mutableStateOf(false) }
                val brands by viewModel.brands.collectAsState(initial = emptyList())
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = brands.find { it.id == currentFilters.brandId }?.name ?: "Chọn thương hiệu",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Thương hiệu") },
                        trailingIcon = {
                            IconButton(onClick = { expandedBrands = !expandedBrands }) {
                                Icon(
                                    if (expandedBrands) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = if (expandedBrands) "Thu gọn" else "Mở rộng"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedBrands,
                        onDismissRequest = { expandedBrands = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                viewModel.updateFilters(
                                    currentFilters.copy(brandId = null)
                                )
                                expandedBrands = false
                            }
                        ) {
                            Text("Tất cả thương hiệu")
                        }
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.updateFilters(
                                        currentFilters.copy(brandId = brand.id)
                                    )
                                    expandedBrands = false
                                }
                            ) {
                                Text(brand.name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price range
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentFilters.minPrice.toString(),
                        onValueChange = { 
                            val newValue = it.toFloatOrNull() ?: 0f
                            if (newValue >= 0) {
                                viewModel.updateFilters(
                                    currentFilters.copy(
                                        minPrice = newValue
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Giá từ") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = currentFilters.maxPrice.toString(),
                        onValueChange = { 
                            val newValue = it.toFloatOrNull() ?: 10000000f
                            if (newValue >= currentFilters.minPrice) {
                                viewModel.updateFilters(
                                    currentFilters.copy(
                                        maxPrice = newValue
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Đến") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }

                // Sort options and order
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var expandedSortBy by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = SortOptions.options.find { it.second == currentFilters.sortBy }?.first ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Sắp xếp theo") },
                            trailingIcon = {
                                IconButton(onClick = { expandedSortBy = !expandedSortBy }) {
                                    Icon(
                                        if (expandedSortBy) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = if (expandedSortBy) "Thu gọn" else "Mở rộng"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expandedSortBy,
                            onDismissRequest = { expandedSortBy = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            SortOptions.options.forEach { (label, value) ->
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.updateFilters(
                                            currentFilters.copy(sortBy = value)
                                        )
                                        expandedSortBy = false
                                    }
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }

                    var expandedSortOrder by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = SortOrderOptions.options.find { it.second == currentFilters.sortOrder }?.first ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Thứ tự") },
                            trailingIcon = {
                                IconButton(onClick = { expandedSortOrder = !expandedSortOrder }) {
                                    Icon(
                                        if (expandedSortOrder) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = if (expandedSortOrder) "Thu gọn" else "Mở rộng"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expandedSortOrder,
                            onDismissRequest = { expandedSortOrder = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            SortOrderOptions.options.forEach { (label, value) ->
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.updateFilters(
                                            currentFilters.copy(sortOrder = value)
                                        )
                                        expandedSortOrder = false
                                    }
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                }

                // Apply filters button
                Button(
                    onClick = {
                        viewModel.resetSearch()
                        viewModel.onSearch(searchQuery)
                        viewModel.toggleFilters()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Áp dụng")
                }
            }
        }

        // Loading indicator
        if (isLoading && searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Error message
        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Search results
        if (!isSearchFocused && searchQuery.isNotEmpty()) {
            if (searchResults.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Không tìm thấy sản phẩm nào",
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentFilters.categoryId != null || currentFilters.brandId != null || 
                                 currentFilters.minPrice != null || currentFilters.maxPrice != null) {
                            "Hãy thử xóa bộ lọc hoặc tìm kiếm với từ khóa khác"
                        } else {
                            "Hãy thử tìm kiếm với từ khóa khác"
                        },
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (currentFilters.categoryId != null || currentFilters.brandId != null || 
                        currentFilters.minPrice != null || currentFilters.maxPrice != null) {
                        // Nút xóa bộ lọc
                        Button(
                            onClick = {
                                viewModel.updateFilters(SearchFilters())
                                viewModel.resetSearch()
                                viewModel.onSearch(searchQuery)
                            }
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Xóa bộ lọc",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Xóa bộ lọc")
                        }
                    } else {
                        // Nút tìm kiếm lại
                        Button(
                            onClick = {
                                viewModel.resetSearch()
                                viewModel.onSearch(searchQuery)
                            }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Tìm kiếm lại",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tìm kiếm lại")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Log.d("DEBUG", "Product clicked: ${product.id}")
                                    onProductClick(product.id)
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Product image
                            AsyncImage(
                                model = product.images.firstOrNull()?.image_url,
                                contentDescription = product.product_name,
                                modifier = Modifier
                                    .size(80.dp),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.ic_placeholder)
                            )
                            
                            // Product details
                            Column {
                                Text(
                                    text = product.product_name,
                                    style = MaterialTheme.typography.subtitle1
                                )
                                Text(
                                    text = "${product.price}đ",
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }

                    // Loading more indicator
                    if (isLoading && searchResults.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // No more data message
                    if (!hasMoreData && searchResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Không còn sản phẩm nào khác",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                style = MaterialTheme.typography.caption,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}