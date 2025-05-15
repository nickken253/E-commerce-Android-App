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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val isLoading by viewModel.isLoading.collectAsState()

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
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Bảng so sánh
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp)
            ) {
                // Header của bảng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.primary)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Tiêu chí",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Sản phẩm 1",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Sản phẩm 2",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Hình ảnh sản phẩm
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Hình ảnh",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        product1?.let {
                            AsyncImage(
                                model = it.imagePath,
                                contentDescription = "Product 1",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        product2?.let {
                            AsyncImage(
                                model = it.imagePath,
                                contentDescription = "Product 2",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // Tên sản phẩm
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Tên sản phẩm",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product1?.name ?: "",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = product2?.name ?: "",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Giá
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Giá",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${product1?.price?.toInt()?.let { String.format("%,d", it) }} VND",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary
                    )
                    Text(
                        text = "${product2?.price?.toInt()?.let { String.format("%,d", it) }} VND",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary
                    )
                }

                // Mô tả
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Mô tả",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product1?.description ?: "",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        text = product2?.description ?: "",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }

                // Mã vạch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Mã vạch",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product1?.barcode ?: "",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = product2?.barcode ?: "",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 