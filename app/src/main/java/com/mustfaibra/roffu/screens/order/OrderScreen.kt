package com.mustfaibra.roffu.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.R

@Composable
fun OrderScreen(
    orderViewModel: OrderViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val tabTitles = listOf("Chờ xác nhận", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
    var selectedTab by remember { mutableStateOf(0) }
    val orderUiState by orderViewModel.orderUiState.collectAsState()

    // Lọc danh sách đơn hàng theo trạng thái tab đang chọn
    val filteredOrders = orderUiState.orders.filter { it.status == tabTitles[selectedTab] }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Header
        TopAppBar(
            title = { Text("Đơn hàng", style = MaterialTheme.typography.h6) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
            actions = {
                IconButton(onClick = { /*TODO: Search*/ }) { Icon(Icons.Default.Search, null) }
                IconButton(onClick = { orderViewModel.showNotifications() }) { Icon(Icons.Default.Notifications, null) }
            },
            backgroundColor = Color.White,
            elevation = 4.dp
        )

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = Color.White,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colors.primary
                )
            }
        ) {
            tabTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == idx) MaterialTheme.colors.primary else Color.Gray
                        )
                    }
                )
            }
        }

        // Danh sách đơn hàng
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8)),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(filteredOrders) { order ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        // TODO: Hiển thị ảnh sản phẩm nếu có liên kết với OrderItem/Product
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Mã đơn: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Ngày đặt: ${order.date}", color = Color.Gray, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFEE4D2D), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Tổng thanh toán: ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(order.total, color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text("Trạng thái: ${order.status}", color = Color(0xFFFF8800), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { /*TODO*/ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Xem chi tiết", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        if (orderUiState.showNotificationDialog) {
            NotificationDialog(
                notifications = orderUiState.notifications,
                onDismiss = { orderViewModel.hideNotifications() }
            )
        }
    }
}

@Composable
fun NotificationDialog(notifications: List<Notification>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thông báo") },
        text = {
            Column {
                notifications.forEach {
                    Text("- ${it.content}", style = MaterialTheme.typography.body2)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}
