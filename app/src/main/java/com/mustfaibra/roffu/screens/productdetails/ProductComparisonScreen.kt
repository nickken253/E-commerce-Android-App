package com.mustfaibra.roffu.screens.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "So sánh sản phẩm",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Bảng so sánh
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header của bảng
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.primary)
                                .padding(16.dp)
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = 0.dp,
                            backgroundColor = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Hình ảnh",
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically),
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    product1?.let {
                                        Card(
                                            modifier = Modifier
                                                .size(140.dp)
                                                .padding(8.dp),
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            AsyncImage(
                                                model = it.imagePath,
                                                contentDescription = "Product 1",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    product2?.let {
                                        Card(
                                            modifier = Modifier
                                                .size(140.dp)
                                                .padding(8.dp),
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            AsyncImage(
                                                model = it.imagePath,
                                                contentDescription = "Product 2",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Tên sản phẩm
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            elevation = 0.dp,
                            backgroundColor = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Tên sản phẩm",
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically),
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
                        }

                        // Giá
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = 0.dp,
                            backgroundColor = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Giá",
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${product1?.price?.toInt()?.let { String.format("%,d", it) }} VND",
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${product2?.price?.toInt()?.let { String.format("%,d", it) }} VND",
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Mô tả
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            elevation = 0.dp,
                            backgroundColor = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Mô tả",
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.Top),
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
                        }

                        // Mã vạch
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = 0.dp,
                            backgroundColor = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Mã vạch",
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically),
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
        }
    }
} 