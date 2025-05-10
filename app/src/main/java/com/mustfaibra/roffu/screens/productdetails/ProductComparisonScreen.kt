package com.mustfaibra.roffu.screens.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mustfaibra.roffu.ui.theme.Dimension

@Composable
fun ProductComparisonScreen(
    productId1: Int,
    productId2: Int,
    onNavigateBack: () -> Unit,
    viewModel: ProductComparisonViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadProducts(productId1, productId2)
    }

    val product1 by viewModel.product1.collectAsState()
    val product2 by viewModel.product2.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(Dimension.pagePadding)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "So sánh sản phẩm",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.width(48.dp)) // Để căn giữa tiêu đề
        }

        // Nội dung so sánh
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hình ảnh sản phẩm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                product1?.let {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = it.image,
                            contentDescription = "Product 1",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(8.dp)
                        )
                        Text(text = it.name)
                        Text(text = "$${it.price}")
                    }
                }
                
                product2?.let {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = it.image,
                            contentDescription = "Product 2",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(8.dp)
                        )
                        Text(text = it.name)
                        Text(text = "$${it.price}")
                    }
                }
            }

            // Thông tin chi tiết
            product1?.let { p1 ->
                product2?.let { p2 ->
                    // Màu sắc
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Màu sắc:")
                            p1.colors?.forEach { color ->
                                Text("- ${color.colorName}")
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Màu sắc:")
                            p2.colors?.forEach { color ->
                                Text("- ${color.colorName}")
                            }
                        }
                    }

                    // Kích thước
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kích thước:")
                            p1.sizes?.forEach { size ->
                                Text("- Size ${size.size}")
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kích thước:")
                            p2.sizes?.forEach { size ->
                                Text("- Size ${size.size}")
                            }
                        }
                    }
                }
            }
        }
    }
} 