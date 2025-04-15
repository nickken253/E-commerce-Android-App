package com.mustfaibra.roffu.screens.admin

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.models.Product
@Composable
fun EditProductScreen(
    productId: Int,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onToastRequested: (String, Color) -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var product by remember { mutableStateOf<Product?>(null) }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(0.0) }
    var description by remember { mutableStateOf("") }
    var manufacturerId by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf(0) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        Log.d("EditProductScreen", "Fetching product with ID: $productId")
        viewModel.getProductById(productId) { foundProduct ->
            isLoading = false
            if (foundProduct != null) {
                Log.d("EditProductScreen", "Product found: ${foundProduct.name}, ID: ${foundProduct.id}")
                product = foundProduct
                name = foundProduct.name
                price = foundProduct.price
                description = foundProduct.description
                manufacturerId = foundProduct.manufacturerId
                color = foundProduct.basicColorName
                barcode = foundProduct.barcode
                discount = foundProduct.discount
                imageUri = foundProduct.imagePath?.let { Uri.parse(it) }
            } else {
                Log.d("EditProductScreen", "Product not found for ID: $productId")
                isError = true
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (isError) {
        LaunchedEffect(Unit) {
            onToastRequested("❌ Product is not exist", Color.Red)
            onBack()
        }
        return
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Edit Product", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name") },
            isError = name.isBlank()
        )
        OutlinedTextField(
            value = price.toString(),
            onValueChange = { price = it.toDoubleOrNull() ?: price },
            label = { Text("Price") },
            isError = price <= 0.0
        )
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả") })
        OutlinedTextField(
            value = manufacturerId.toString(),
            onValueChange = { manufacturerId = it.toIntOrNull() ?: manufacturerId },
            label = { Text("NSX ID") }
        )
        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Màu sắc") })
        OutlinedTextField(
            value = barcode,
            onValueChange = { barcode = it },
            label = { Text("Barcode") },
            isError = barcode.isBlank()
        )
        OutlinedTextField(
            value = discount.toString(),
            onValueChange = { discount = it.toIntOrNull() ?: discount },
            label = { Text("Discount") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .clickable { imageLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Choose image")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (name.isBlank() || price <= 0.0 || barcode.isBlank()) {
                onToastRequested("❗ Điền đầy đủ thông tin hợp lệ", Color.Red)
            } else {
                val updated = product!!.copy(
                    name = name,
                    price = price,
                    description = description,
                    discount = discount,
                    manufacturerId = manufacturerId,
                    basicColorName = color,
                    barcode = barcode,
                    imagePath = imageUri?.toString()
                )
                viewModel.updateProduct(updated) { msg, success ->
                    onToastRequested(msg, if (success) Color.Green else Color.Red)
                    if (success) onDone()
                }
            }
        }, modifier = Modifier.align(Alignment.End)) {
            Text("Save change")
        }
    }
}