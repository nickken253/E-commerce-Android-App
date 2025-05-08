package com.mustfaibra.roffu.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tìm kiếm...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.resetSearch()
                        viewModel.onSearch(searchQuery)
                    }
                )
            )
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.resetSearch()
                    viewModel.onSearch(searchQuery)
                }
            ) {
                Text("Tìm kiếm")
            }
        }

        // Filters (chỉ hiển thị sau khi có kết quả tìm kiếm)
        if (searchResults.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter by price
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sort options
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

                // Sort order
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

                // Apply filters button
                Button(
                    onClick = {
                        viewModel.resetSearch()
                        viewModel.onSearch(searchQuery)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Lọc")
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
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(searchResults) { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(product.id) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Product image
                    AsyncImage(
                        model = product.imagePath,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(80.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Product details
                    Column {
                        Text(
                            text = product.name,
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

        // Search suggestions when no results
        if (searchResults.isEmpty() && !isLoading && error == null) {
            if (searchQuery.isEmpty()) {
                // Show search history
                if (searchHistory.isNotEmpty()) {
                    Text(
                        text = "Lịch sử tìm kiếm",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyColumn {
                        items(searchHistory) { historyItem ->
                            Text(
                                text = historyItem,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = historyItem
                                        viewModel.onSearch(historyItem)
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Show search suggestions
                LazyColumn {
                    items(searchSuggestions) { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = suggestion
                                    viewModel.onSearch(suggestion)
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}