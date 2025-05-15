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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.mustfaibra.roffu.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

/**
 * Định dạng ngày tháng năm từ chuỗi thời gian
 * @param dateString Chuỗi thời gian cần định dạng (ví dụ: "2025-05-13T15:30:35.744455Z")
 * @return Chuỗi ngày tháng năm đã định dạng (ví dụ: "13/05/2025")
 */
private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Chưa có ngày"
    
    return try {
        // Tách ngày tháng năm từ chuỗi thời gian
        val parts = dateString.split("T")[0].split("-")
        if (parts.size >= 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            "$day/$month/$year" // Định dạng ngày/tháng/năm
        } else {
            "Chưa có ngày"
        }
    } catch (e: Exception) {
        "Chưa có ngày"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderScreen(
    orderViewModel: OrderViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    refreshOrders: Boolean = true,
) {
    val orders = orderViewModel.ordersWithProducts
    val productPrices = remember { mutableStateMapOf<Int, Double>() }
    
    // Gọi API "read orders" mỗi khi màn hình được hiển thị hoặc khi refreshOrders thay đổi
    LaunchedEffect(refreshOrders) {
        orderViewModel.getOrdersWithProducts()
        // Log để debug
        android.util.Log.d("OrderScreen", "Loading orders data. RefreshOrders: $refreshOrders")
    }
    
    // Lấy giá sản phẩm từ API cho mỗi order item
    LaunchedEffect(orders) {
        orders.forEach { order ->
            order.orderItems?.forEach { item ->
                if (!productPrices.containsKey(item.productId)) {
                    val productResponse = orderViewModel.getProductPrice(item.productId)
                    productResponse?.let { price ->
                        productPrices[item.productId] = price
                    }
                }
            }
        }
    }
    
    // Debug log cho danh sách đơn hàng
    LaunchedEffect(orders) {
        orders.forEachIndexed { index, order ->
            order.orderItems?.forEachIndexed { itemIndex, item ->
                android.util.Log.d("OrderScreen", "  Item #$itemIndex - Name: ${item.productName}, Image: ${item.productImage}")
            }
        }
    }
    val errorMessage by orderViewModel.errorMessage.collectAsState()
    val showNotifications by orderViewModel.showNotifications.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val tabTitles =
        listOf("Tất cả", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
    val statusMapping = mapOf(
        "pending" to "Chờ lấy hàng",
        "processing" to "Đang giao",
        "shipped" to "Đã giao",
        "cancelled" to "Đã hủy"
    )
    var selectedTab by remember { mutableStateOf(0) }
    // Xử lý an toàn khi orders có thể là null hoặc rỗng
    val filteredOrders = if (orders.isEmpty()) {
        emptyList() // Trả về danh sách rỗng nếu không có đơn hàng
    } else if (selectedTab == 0) {
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
                        IconButton(onClick = { /*TODO: Search*/ }) {
                            Icon(
                                Icons.Default.Search,
                                null
                            )
                        }
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
                                        formatDate(
                                            orderWithProducts.orderDate
                                                ?: orderWithProducts.createdAt
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        statusMapping[orderWithProducts.status]
                                            ?: (orderWithProducts.status ?: "Không rõ trạng thái"),
                                        color = MaterialTheme.colors.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Divider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                                val expandedOrders =
                                    remember { mutableStateMapOf<String, Boolean>() }

                                // Debug log cho orderItems
                                android.util.Log.d("OrderScreen", "OrderItems for order ${orderWithProducts.orderId}: ${orderItems?.size ?: 0}")
                                
                                // Kiểm tra null và xử lý an toàn
                                if (orderItems != null && orderItems.isNotEmpty()) {
                                    val items = orderItems
                                    val orderId = orderWithProducts.orderId.toString()
                                    val expanded = expandedOrders[orderId] == true
                                    val showItems =
                                        if (expanded || items.size <= 2) items else items.take(2)

                                    // Debug log cho showItems
                                    android.util.Log.d("OrderScreen", "ShowItems count: ${showItems.size} for order ${orderId}")
                                    
                                    // Hiển thị các sản phẩm
                                    showItems.forEach { orderItem ->
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Hiển thị ảnh sản phẩm sử dụng Coil với xử lý lỗi
                                            AsyncImage(
                                                model = orderItem.productImage,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                error = painterResource(id = R.drawable.ic_placeholder),
                                                contentScale = ContentScale.Fit
                                            )

                                            Spacer(Modifier.width(12.dp))

                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    orderItem.productName
                                                        ?: "Sản phẩm không xác định",
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 15.sp,
                                                    maxLines = 2
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Hiển thị giá sản phẩm từ API
                                                    val displayPrice = orderViewModel.productPrices[orderItem.productId] ?: orderItem.price ?: 0.0
                                                    
                                                    Text(
                                                        text = "₫" + String.format(
                                                            "%,.0f",
                                                            displayPrice
                                                        ),
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

                                    // Hiển thị nút Xem thêm/Thu gọn nếu có nhiều hơn 2 sản phẩm
                                    if (items.size > 2) {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .clickable {
                                                    expandedOrders[orderId] = !expanded
                                                },
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                if (expanded) "Thu gọn" else "Xem thêm ${items.size - 2} sản phẩm",
                                                color = MaterialTheme.colors.primary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                            Icon(
                                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = MaterialTheme.colors.primary
                                            )
                                        }
                                    }

                                    Divider(color = Color(0xFFF2F2F2), thickness = 1.dp)

                                    // Hiển thị tổng tiền của đơn hàng
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            "Tổng cộng: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            "₫" + String.format(
                                                "%,.0f",
                                                (orderWithProducts.total ?: 0.0)
                                            ),
                                            color = MaterialTheme.colors.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                    }

                                    // Hiển thị thông tin thanh toán
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Trạng thái thanh toán: ",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            when (orderWithProducts.paymentStatus) {
                                                "completed" -> "Đã thanh toán"
                                                "pending" -> "Chưa thanh toán"
                                                else -> orderWithProducts.paymentStatus
                                                    ?: "Không rõ"
                                            },
                                            color = when (orderWithProducts.paymentStatus) {
                                                "completed" -> Color(0xFF4CAF50)
                                                "pending" -> Color(0xFFFFA000)
                                                else -> Color.Gray
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Hiển thị các nút tùy theo trạng thái
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        when (statusMapping[orderWithProducts.status]
                                            ?: orderWithProducts.status) {
                                            "Chờ lấy hàng" -> {
                                                TextButton(
                                                    onClick = { /* TODO: Hủy đơn */ },
                                                    border = BorderStroke(
                                                        1.dp,
                                                        MaterialTheme.colors.primary
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.textButtonColors(
                                                        backgroundColor = Color.White
                                                    ),
                                                    contentPadding = PaddingValues(
                                                        horizontal = 12.dp,
                                                        vertical = 0.dp
                                                    )
                                                ) {
                                                    Text(
                                                        "Hủy đơn",
                                                        color = MaterialTheme.colors.primary,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }

                                            "Đang giao" -> {
                                                Button(
                                                    onClick = { showShippingDetail = true },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        backgroundColor = MaterialTheme.colors.primary
                                                    )
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
                                                Row {
                                                    Button(
                                                        onClick = { /* TODO: Đánh giá */ },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            backgroundColor = MaterialTheme.colors.primary
                                                        )
                                                    ) {
                                                        Text(
                                                            "Đánh giá",
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    }

                                                    Spacer(Modifier.width(8.dp))

                                                    Button(
                                                        onClick = { /* TODO: Mua lại */ },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            backgroundColor = Color(0xFF4CAF50)
                                                        )
                                                    ) {
                                                        Text(
                                                            "Mua lại",
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            }

                                            "Đã hủy" -> {
                                                Button(
                                                    onClick = { /* TODO: Mua lại */ },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        backgroundColor = Color(0xFF4CAF50)
                                                    )
                                                ) {
                                                    Text(
                                                        "Mua lại",
                                                        color = Color.White,
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
    }
}


