package com.mustfaibra.roffu.screens.barcode

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mustfaibra.roffu.components.CameraPreview
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.utils.BarcodeAnalyzer
import com.mustfaibra.roffu.models.Product

@Composable
fun BarcodeScannerScreen(
    navController: NavController,
    viewModel: BarcodeScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    ) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Cần quyền camera để quét mã vạch", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processBarcodeFromImage(context, it, viewModel) }
    }

    if (!hasCameraPermission) {
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Ứng dụng cần quyền truy cập camera!")
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Cấp quyền camera")
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CameraPreview(
            modifier = Modifier.weight(1f),
            imageAnalyzer = BarcodeAnalyzer { barcode ->
                viewModel.getProductByBarcode(barcode)
            }
        )

        val scannedProduct by viewModel.scannedProduct.collectAsState()
        scannedProduct?.let { product ->
            ProductDisplay(
                product = product,
                onProductClicked = {
                    navController.navigate(
                        Screen.ProductDetails.route.replace("{productId}", "${product.id}")
                    )
                }
            )
        } ?: Text(text = "Đang quét...", modifier = Modifier.padding(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { pickImageLauncher.launch("image/*") }) {
                Text("Chọn ảnh từ thư viện")
            }
            Button(onClick = { navController.popBackStack() }) {
                Text("Quay lại")
            }
        }
    }
}

@Composable
fun ProductDisplay(
    product: Product,
    onProductClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .clickable { onProductClicked() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = product.image,
            contentDescription = "Product Image",
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.name, style = MaterialTheme.typography.body1)
        }
    }
}

fun processBarcodeFromImage(
    context: android.content.Context,
    imageUri: Uri,
    viewModel: BarcodeScannerViewModel
) {
    val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()

    try {
        val inputImage = com.google.mlkit.vision.common.InputImage.fromFilePath(context, imageUri)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcodeValue = barcodes.first().rawValue ?: "Không đọc được mã vạch"
                    viewModel.getProductByBarcode(barcodeValue)
                } else {
                    Toast.makeText(context, "Không tìm thấy mã vạch", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi khi quét mã vạch", Toast.LENGTH_SHORT).show()
            }
    } catch (e: Exception) {
        Toast.makeText(context, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show()
    }
}