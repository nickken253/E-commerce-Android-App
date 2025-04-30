package com.mustfaibra.roffu.screens.order

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut


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

    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    var showShippingDetail by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Header
        TopAppBar(
            title = { Text("Đơn hàng", style = MaterialTheme.typography.h6) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        null
                    )
                }
            },
            actions = {
                IconButton(onClick = { /*TODO: Search*/ }) { Icon(Icons.Default.Search, null) }
                IconButton(onClick = { orderViewModel.showNotifications() }) {
                    Icon(
                        Icons.Default.Notifications,
                        null
                    )
                }
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
                val isExpanded = expandedStates[order.id] == true
                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                ) {
                    Column(Modifier.background(Color.White)) {
                        // Header: Shop name + trạng thái
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (order.shopLabel != null) {
                                Box(
                                    Modifier
                                        .background(
                                            MaterialTheme.colors.primary,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        order.shopLabel,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(order.shopName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.weight(1f))
                            Text(
                                order.status,
                                color = MaterialTheme.colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Divider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                        // Danh sách sản phẩm
                        order.products.take(2).forEach { product ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(product.image),
                                    contentDescription = null,
                                    modifier = Modifier.size(68.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        product.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        maxLines = 1
                                    )
                                    Text(product.variant, color = Color.Gray, fontSize = 12.sp)
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (product.priceOrigin != null) {
                                            Text(
                                                text = "₫${"%,d".format(product.priceOrigin)}",
                                                style = TextStyle(textDecoration = TextDecoration.LineThrough),
                                                color = Color.Gray,
                                                fontSize = 13.sp
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = "₫${"%,d".format(product.price)}",
                                            color = MaterialTheme.colors.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "x${product.quantity}",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                        // Nếu có nhiều hơn 2 sản phẩm thì dùng AnimatedVisibility cho phần còn lại
                        if (order.products.size > 2) {
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    order.products.drop(2).forEach { product ->
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(product.image),
                                                contentDescription = null,
                                                modifier = Modifier.size(68.dp).clip(RoundedCornerShape(8.dp))
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    product.name,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 15.sp,
                                                    maxLines = 1
                                                )
                                                Text(product.variant, color = Color.Gray, fontSize = 12.sp)
                                                Spacer(Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (product.priceOrigin != null) {
                                                        Text(
                                                            text = "₫${"%,d".format(product.priceOrigin)}",
                                                            style = TextStyle(textDecoration = TextDecoration.LineThrough),
                                                            color = Color.Gray,
                                                            fontSize = 13.sp
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                    }
                                                    Text(
                                                        text = "₫${"%,d".format(product.price)}",
                                                        color = MaterialTheme.colors.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        "x${product.quantity}",
                                                        color = Color.Gray,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Nút Xem thêm/Thu gọn như hiện tại
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedStates[order.id] = !isExpanded }
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isExpanded) "Thu gọn" else "Xem thêm",
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colors.primary,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                        Divider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                        // Tổng số tiền
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "Tổng số tiền (${order.products.sumOf { it.quantity }} sản phẩm): ",
                                fontSize = 14.sp
                            )
                            Text(
                                order.total,
                                color = MaterialTheme.colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        // Nút chức năng (theo trạng thái)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            when (order.status) {
                                "Chờ xác nhận", "Chờ lấy hàng" -> {
                                    OutlinedButton(
                                        onClick = { /* TODO: Xử lý hủy đơn */ },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
                                    ) {
                                        Text("Hủy đơn", color = MaterialTheme.colors.primary, fontSize = 14.sp)
                                    }
                                }
                                "Đang giao" -> {
                                    OutlinedButton(
                                        onClick = { showShippingDetail = true },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
                                    ) {
                                        Text("Chi tiết vận chuyển", color = MaterialTheme.colors.primary, fontSize = 14.sp)
                                    }
                                }
                                "Đã giao" -> {
                                    OutlinedButton(
                                        onClick = { /* TODO: Đánh giá */ },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
                                    ) {
                                        Text("Đánh giá", color = MaterialTheme.colors.primary, fontSize = 14.sp)
                                    }
                                }
                                // "Đã hủy" hoặc trạng thái khác: Không hiện nút
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
                if (showShippingDetail) {
                    ShippingDetailScreen(
                        onBack = { showShippingDetail = false }
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
    }
}

@Composable
fun NotificationDialog(notifications: List<Notification>, onDismiss: () -> Unit) {

}
