package dev.vstd.shoppingcart.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vstd.shoppingcart.model.SearchFilter
import dev.vstd.shoppingcart.model.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    searchFilter: SearchFilter,
    onDismiss: () -> Unit,
    onApply: (SearchFilter) -> Unit,
    onReset: () -> Unit
) {
    var currentFilter by remember { mutableStateOf(searchFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bộ lọc tìm kiếm",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sort options
            Text(
                text = "Sắp xếp theo",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SortType.values()) { sortType ->
                    FilterChip(
                        selected = currentFilter.sortBy == sortType,
                        onClick = {
                            currentFilter = currentFilter.copy(sortBy = sortType)
                        },
                        label = {
                            Text(
                                when (sortType) {
                                    SortType.RELEVANCE -> "Liên quan"
                                    SortType.NEWEST -> "Mới nhất"
                                    SortType.BEST_SELLING -> "Bán chạy"
                                    SortType.PRICE_ASC -> "Giá tăng dần"
                                    SortType.PRICE_DESC -> "Giá giảm dần"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price range
            Text(
                text = "Khoảng giá",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentFilter.minPrice?.toString() ?: "",
                    onValueChange = { value ->
                        currentFilter = currentFilter.copy(
                            minPrice = value.toDoubleOrNull()
                        )
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Từ") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = currentFilter.maxPrice?.toString() ?: "",
                    onValueChange = { value ->
                        currentFilter = currentFilter.copy(
                            maxPrice = value.toDoubleOrNull()
                        )
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Đến") },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating filter
            Text(
                text = "Đánh giá",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..5).toList()) { rating ->
                    FilterChip(
                        selected = currentFilter.minRating == rating.toFloat(),
                        onClick = {
                            currentFilter = currentFilter.copy(
                                minRating = if (currentFilter.minRating == rating.toFloat()) 0f else rating.toFloat()
                            )
                        },
                        label = {
                            Text("Từ $rating sao")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Promotion filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đang giảm giá",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = currentFilter.hasPromotion,
                    onCheckedChange = { checked ->
                        currentFilter = currentFilter.copy(hasPromotion = checked)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Đặt lại")
                }
                Button(
                    onClick = { onApply(currentFilter) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Áp dụng")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 