package com.mustfaibra.roffu.screens.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderScreen(
    orderViewModel: OrderViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    LaunchedEffect(Unit) { orderViewModel.getOrdersWithProducts() }
    val orders = orderViewModel.ordersWithProducts
    val errorMessage by orderViewModel.errorMessage.collectAsState()
    val showNotifications by orderViewModel.showNotifications.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val tabTitles = listOf("Tất cả", "Chờ xác nhận", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
    val statusMapping = mapOf(
        "pending" to "Chờ xác nhận",
        "processing" to "Chờ lấy hàng",
        "shipped" to "Đang giao",
        "delivered" to "Đã giao",
        "cancelled" to "Đã hủy"
    )
    var selectedTab by remember { mutableStateOf(0) }
    val filteredOrders = if (selectedTab == 0) {
        orders // Hiển thị tất cả đơn hàng
    } else {
        orders.filter { statusMapping[it.status] == tabTitles[selectedTab] }
    }

    var showShippingDetail by remember { mutableStateOf(false) }

    // Hiển thị Snackbar khi có lỗi
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage!!,
                    actionLabel = "OK",
                    duration = SnackbarDuration.Short
                )
                orderViewModel.clearErrorMessage()
            }
        }
    }

    // Hiển thị Dialog thông báo
    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { orderViewModel.hideNotifications() },
            title = { Text("Thông báo") },
            text = { Text("Bạn có thông báo mới! Vui lòng kiểm tra chi tiết.") },
            confirmButton = {
                TextButton(onClick = { orderViewModel.hideNotifications() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            if (showShippingDetail) {
                ShippingDetailScreen(
                    onBack = { showShippingDetail = false }
                )
            } else {
                TopAppBar(
                    title = { Text("Đơn hàng", style = MaterialTheme.typography.h6) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /*TODO: Search*/ }) { Icon(Icons.Default.Search, null) }
                        IconButton(onClick = { orderViewModel.showNotifications() }) {
                            Icon(Icons.Default.Notifications, null)
                        }
                    },
                    backgroundColor = Color.White,
                    elevation = 4.dp
                )
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
                            onClick = {
                                selectedTab = idx
                                orderViewModel.selectTab(idx)
                            },
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
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF8F8F8)),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(filteredOrders) { orderWithProducts ->
                        val orderItems = orderWithProducts.orderItems
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            elevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                        ) {
                            Column(Modifier.background(Color.White)) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        orderWithProducts.createdAt,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        statusMapping[orderWithProducts.status] ?: orderWithProducts.status,
                                        color = MaterialTheme.colors.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Divider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                                val expandedOrders = remember { mutableStateMapOf<String, Boolean>() }
                                orderItems.let { items ->
                                    val orderId = orderWithProducts.orderId.toString()
                                    val expanded = expandedOrders[orderId] == true
                                    val showItems = if (expanded || items.size <= 2) items else items.take(2)
                                    showItems.forEach { orderItem ->
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.placeholder_image), // Thay bằng Coil để tải từ orderItem.productImage
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    orderItem.productName ?: "Unknown Product",
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 15.sp,
                                                    maxLines = 1
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "₫" + String.format("%,.0f", orderItem.subtotal * 25000),
                                                        color = MaterialTheme.colors.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        "x${orderItem.quantity}",
                                                        color = Color.Gray,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (items.size > 2) {
                                        TextButton(
                                            onClick = { expandedOrders[orderId] = !expanded },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Text(
                                                if (expanded) "Thu gọn" else "Xem thêm (${items.size - 2}) sản phẩm",
                                                color = MaterialTheme.colors.primary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                                Divider(
                                    color = Color(0xFFF2F2F2),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(Modifier.weight(1f))
                                    Text("Tổng cộng: ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        "₫" + String.format("%,.0f", orderWithProducts.total * 25000),
                                        color = MaterialTheme.colors.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    when (statusMapping[orderWithProducts.status] ?: orderWithProducts.status) {
                                        "Chờ xác nhận", "Chờ lấy hàng" -> {
                                            TextButton(
                                                onClick = { /* TODO: Hủy đơn */ },
                                                border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.textButtonColors(backgroundColor = Color.White),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                            ) {
                                                Text("Hủy đơn", color = MaterialTheme.colors.primary, fontSize = 14.sp)
                                            }
                                        }
                                        "Đang giao" -> {
                                            Button(
                                                onClick = { showShippingDetail = true },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                                            ) {
                                                Text(
                                                    "Chi tiết vận chuyển",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                        "Đã giao" -> {
                                            Text(
                                                "Đã giao hàng",
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        "Đã hủy" -> {
                                            Text(
                                                "Đơn đã hủy",
                                                color = Color.Red,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}