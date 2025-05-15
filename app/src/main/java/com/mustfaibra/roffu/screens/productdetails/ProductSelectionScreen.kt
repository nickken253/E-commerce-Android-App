package com.mustfaibra.roffu.screens.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.ui.theme.Dimension

@Composable
fun ProductSelectionScreen(
    currentProductId: Int,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    viewModel: ProductSelectionViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadProducts(currentProductId)
    }

    val products by viewModel.products.collectAsState()

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
                text = "Chọn sản phẩm để so sánh",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Danh sách sản phẩm
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                if (product.id != currentProductId) {
                    ProductItem(
                        product = product,
                        onClick = {
                            navController.navigate(
                                Screen.ProductComparison.route
                                    .replace("{productId1}", currentProductId.toString())
                                    .replace("{productId2}", product.id.toString())
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = product.imagePath,
            contentDescription = "Product image",
            modifier = Modifier
                .size(80.dp)
        )
        Column {
            Text(
                text = product.name,
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = "${String.format("%,d", product.price.toInt())} VND",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.primary
            )
        }
    }
} 