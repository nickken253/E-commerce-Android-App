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

    Column {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isSearchFocused = it.isFocused },
                placeholder = { Text("Tìm kiếm...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        Log.d("DEBUG", "Search button clicked with query: $searchQuery")
                        focusManager.clearFocus()
                        viewModel.resetSearch()
                        viewModel.onSearch(searchQuery)
                    }
                )
            )
            IconButton(
                onClick = {
                    Log.d("DEBUG", "Search button clicked with query: $searchQuery")
                    focusManager.clearFocus()
                    viewModel.resetSearch()
                    viewModel.onSearch(searchQuery)
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
            }
            IconButton(
                onClick = viewModel::toggleFilters
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Bộ lọc")
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
                            viewModel.updateFilters(
                                currentFilters.copy(
                                    minPrice = it.toFloatOrNull() ?: 0f
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Giá từ") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = currentFilters.maxPrice.toString(),
                        onValueChange = { 
                            viewModel.updateFilters(
                                currentFilters.copy(
                                    maxPrice = it.toFloatOrNull() ?: Float.MAX_VALUE
                                )
                            )
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

        // Search suggestions and history
        AnimatedVisibility(
            visible = isSearchFocused && searchResults.isEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
        LazyColumn {
                if (searchQuery.isEmpty()) {
                    // Show search history
            if (searchHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Lịch sử tìm kiếm",
                                style = MaterialTheme.typography.subtitle1,
                                modifier = Modifier.padding(16.dp)
                    )
                }
                        items(searchHistory) { historyItem ->
                    Text(
                                text = historyItem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                        searchQuery = historyItem
                                        focusManager.clearFocus()
                                        viewModel.onSearch(historyItem)
                            }
                            .padding(16.dp)
                    )
                }
            }
                } else {
                    // Show search suggestions
            items(searchSuggestions) { suggestion ->
                Text(
                    text = suggestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            searchQuery = suggestion
                                    focusManager.clearFocus()
                                    viewModel.onSearch(suggestion)
                        }
                        .padding(16.dp)
                )
                    }
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
        if (!isSearchFocused) {
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
                            contentScale = ContentScale.Crop
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