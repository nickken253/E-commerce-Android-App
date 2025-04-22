package dev.vstd.shoppingcart.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vstd.shoppingcart.model.Product
import dev.vstd.shoppingcart.model.SearchFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    val searchFilter by viewModel.searchFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val popularCategories by viewModel.popularCategories.collectAsState()
    val popularTags by viewModel.popularTags.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SearchTopBar(
                searchFilter = searchFilter,
                onNavigateBack = onNavigateBack,
                onFilterClick = { showFilterSheet = true },
                onSearchQueryChange = { query ->
                    viewModel.updateFilter { it.copy(keyword = query) }
                    viewModel.search()
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Popular Categories
                item {
                    Text(
                        text = "Danh mục phổ biến",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(popularCategories) { category ->
                            CategoryChip(
                                category = category,
                                selected = category in searchFilter.categories,
                                onClick = {
                                    viewModel.updateFilter { filter ->
                                        val updatedCategories = if (category in filter.categories) {
                                            filter.categories - category
                                        } else {
                                            filter.categories + category
                                        }
                                        filter.copy(categories = updatedCategories)
                                    }
                                    viewModel.search()
                                }
                            )
                        }
                    }
                }

                // Popular Tags
                item {
                    Text(
                        text = "Gợi ý tìm kiếm",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(popularTags) { tag ->
                            AssistChip(
                                onClick = {
                                    viewModel.updateFilter { it.copy(keyword = tag) }
                                    viewModel.search()
                                },
                                label = { Text(tag) }
                            )
                        }
                    }
                }

                // Search Results
                items(searchResults) { product ->
                    ProductItem(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }

                // Loading indicator
                if (isLoading) {
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
            }
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                searchFilter = searchFilter,
                onDismiss = { showFilterSheet = false },
                onApply = { filter ->
                    viewModel.updateFilter { filter }
                    viewModel.search()
                    showFilterSheet = false
                },
                onReset = {
                    viewModel.resetFilter()
                    showFilterSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchFilter: SearchFilter,
    onNavigateBack: () -> Unit,
    onFilterClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            OutlinedTextField(
                value = searchFilter.keyword,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm sản phẩm...") },
                leadingIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = onFilterClick) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                singleLine = true
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(
    category: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(category) },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductItem(
    product: Product,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Product image placeholder
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .align(Alignment.CenterVertically)
            ) {
                // TODO: Load actual image
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₫${product.price}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.discount > 0) {
                        Text(
                            text = "-${product.discount}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = product.rating.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "Đã bán ${product.soldCount}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            IconButton(
                onClick = { /* TODO: Toggle favorite */ },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (product.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
} 