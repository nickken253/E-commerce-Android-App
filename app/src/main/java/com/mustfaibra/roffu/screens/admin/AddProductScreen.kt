package com.mustfaibra.roffu.screens.admin

import android.net.Uri
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
import com.mustfaibra.roffu.models.Manufacturer
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.sealed.UiState

@Composable
fun AddProductScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    onToastRequested: (String, Color) -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(0.0) }
    var description by remember { mutableStateOf("") }
    var manufacturerId by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }
    var showAddManufacturerDialog by remember { mutableStateOf(false) }

    val manufacturers by remember { derivedStateOf { viewModel.manufacturers } }
    var expanded by remember { mutableStateOf(false) }
    var selectedManufacturer by remember { mutableStateOf<Manufacturer?>(null) }


    LaunchedEffect(viewModel.addManufacturerState) {
        when (viewModel.addManufacturerState.value) {
            is UiState.Success -> {
                onToastRequested("Add manufacturer successful", Color.Green)
            }
            is UiState.Error -> {
                onToastRequested("Add manufacturer failed", Color.Red)
            }
            else -> {}
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Add Product", style = MaterialTheme.typography.h5)

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
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Describe") }
        )

        // Dropdown cho nhà sản xuất
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedManufacturer?.let { "${it.id} - ${it.name}" } ?: "Choose manufacturer",
                    onValueChange = {},
                    label = { Text("Manufacturer") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    enabled = false,
                    isError = selectedManufacturer == null
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    manufacturers.forEach { manufacturer ->
                        DropdownMenuItem(
                            onClick = {
                                selectedManufacturer = manufacturer
                                expanded = false
                            }
                        ) {
                            Text(text = "${manufacturer.id} - ${manufacturer.name}")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showAddManufacturerDialog = true },
                modifier = Modifier.wrapContentSize()
            ) {
                Text("Add manufacturer")
            }
        }

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") }
        )
        OutlinedTextField(
            value = barcode,
            onValueChange = { barcode = it },
            label = { Text("Barcode") },
            isError = barcode.isBlank()
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
            if (name.isBlank() || price <= 0.0 || barcode.isBlank() || selectedManufacturer == null) {
                onToastRequested("❗ Điền đầy đủ thông tin hợp lệ", Color.Red)
            } else {
                val product = Product(
                    name = name,
                    price = price,
                    description = description,
                    manufacturerId = selectedManufacturer!!.id,
                    basicColorName = color,
                    barcode = barcode,
                    image = 0,
                    imagePath = imageUri?.toString()
                )
                viewModel.addProduct(product) { msg, success ->
                    onToastRequested(msg, if (success) Color.Green else Color.Red)
                    if (success) onDone()
                }
            }
        }, modifier = Modifier.align(Alignment.End)) {
            Text("Save product")
        }
    }

    // Dialog để thêm nhà sản xuất mới
    if (showAddManufacturerDialog) {
        AddManufacturerDialog(
            onDismiss = { showAddManufacturerDialog = false },
            onAdd = { manufacturerName ->
                viewModel.addManufacturer(manufacturerName) { msg, success ->
                    onToastRequested(msg, if (success) Color.Green else Color.Red)
                    if (success) showAddManufacturerDialog = false
                }
            }
        )
    }
}
@Composable
fun AddManufacturerDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add new manufacturer") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Manufacturer name") },
                    isError = name.isBlank()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Giả lập DropdownMenuItem (nếu cần)
@Composable
fun DropdownMenuItem(onClick: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material.DropdownMenuItem(
        onClick = onClick,
        content = { content() }
    )
}