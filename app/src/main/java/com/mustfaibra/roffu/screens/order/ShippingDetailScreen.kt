package com.mustfaibra.roffu.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustfaibra.roffu.R
import androidx.compose.material.icons.filled.LocalShipping

// Data model cho từng bước vận chuyển
 data class ShippingStep(
    val content: String,
    val time: String,
    val isActive: Boolean = false // Bước hiện tại
)

// Fake data mẫu
val sampleTrackingSteps = listOf(
    ShippingStep("[HCM HUB]Đơn hàng đã được giao thành công", "21-01-2020 14:19", true),
    ShippingStep("[HCM HUB]Nhân viên giao hàng đang tiến hành giao", "21-01-2020 13:59"),
    ShippingStep("[HCM HUB]Chờ giao hàng", "21-01-2020 13:43"),
    ShippingStep("[Ho Chi Minh DC]Đã chuyển tới trung tâm khai thác", "21-01-2020 10:49"),
    ShippingStep("Đơn hàng đang được giao đến bạn", "21-01-2020 10:23"),
    ShippingStep("Đơn hàng đang được giao đến bạn", "21-01-2020 10:19"),
    ShippingStep("Đơn hàng đã được đóng gói", "21-01-2020 10:16"),
    ShippingStep("Đơn hàng đã được đóng gói", "21-01-2020 10:14"),
    ShippingStep("Đơn hàng đã được đóng gói", "21-01-2020 10:12"),
    ShippingStep("Đơn hàng đang được chuẩn bị bởi Kho Smart", "21-01-2020 10:12")
)

@Composable
fun ShippingDetailScreen(
    trackingNumber: String = "VN209839115571",
    expressName: String = "Shopee Express",
    steps: List<ShippingStep> = sampleTrackingSteps,
    onBack: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        // Text test để debug hiển thị
        Text(
            text = "Shipping Detail Test",
            color = Color.Red,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Thông tin vận chuyển") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                backgroundColor = Color.White,
                elevation = 4.dp
            )

            // Đầu tracking
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thay icon này bằng icon vận chuyển của bạn nếu có
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = MaterialTheme.colors.primary
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(expressName, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Mã vận đơn: $trackingNumber", color = Color.Gray, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "SAO CHÉP",
                            color = MaterialTheme.colors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { /* TODO: copy to clipboard */ }
                        )
                    }
                }
            }

            Divider()

            // Timeline
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                itemsIndexed(steps) { idx, step ->
                    Row(verticalAlignment = Alignment.Top) {
                        // Dot
                        Box(
                            Modifier
                                .padding(top = 4.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step.isActive) MaterialTheme.colors.primary else Color.LightGray
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                step.content,
                                color = if (step.isActive) MaterialTheme.colors.primary else Color.Gray,
                                fontWeight = if (step.isActive) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                step.time,
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                    if (idx != steps.lastIndex) {
                        // Timeline line
                        Spacer(
                            Modifier
                                .padding(start = 5.dp)
                                .width(2.dp)
                                .height(22.dp)
                                .background(Color(0xFFE0E0E0))
                        )
                    }
                }
            }
        }
    }
}
