package com.mustfaibra.roffu.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.utils.PriceFormatter

@Composable
fun AddedToCartDialog(
    productName: String,
    productImage: Any,
    productPrice: Double,
    productQuantity: Int = 1,
    onContinue: () -> Unit,
    onViewCart: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Color(0xFF23242A), shape = RoundedCornerShape(18.dp))
                .padding(20.dp)
                .width(340.dp)
        ) {
            Text(
                "Đã thêm vào giỏ hàng!",
                style = MaterialTheme.typography.h6.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = Color.White,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .background(Color(0xFF2D2E36), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(productImage),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        productName,
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = Color.White,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(8.dp))
                    // Giá tiền
                    Text(
                        "${PriceFormatter.formatPrice(productPrice)} VND",
                        style = MaterialTheme.typography.body1.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    
                    // Số lượng
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Số lượng: $productQuantity",
                        style = MaterialTheme.typography.body2,
                        color = Color.LightGray
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF44454C))
                ) {
                    Text("Tiếp tục mua sắm", color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = onViewCart,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF3A8DFF)
                    )
                ) {
                    Text("Xem giỏ hàng", color = Color.White)
                }
            }
        }
    }
}
