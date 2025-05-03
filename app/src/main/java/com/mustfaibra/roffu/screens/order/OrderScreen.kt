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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.models.OrderDetails
import com.mustfaibra.roffu.models.Location
import com.mustfaibra.roffu.models.PaymentProvider
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderScreen(
    orderViewModel: OrderViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    LaunchedEffect(Unit) { orderViewModel.getOrdersWithProducts() }
    val orders = orderViewModel.ordersWithProducts

    val tabTitles = listOf("Chờ xác nhận", "Chờ lấy hàng", "Đang giao", "Đã giao", "Đã hủy")
    var selectedTab by remember { mutableStateOf(0) }
    val filteredOrders = orders.filter { it.order.status == tabTitles[selectedTab] }

    var showShippingDetail by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (showShippingDetail) {
            ShippingDetailScreen(
                onBack = { showShippingDetail = false }
            )
        } else {
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8F8F8)),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(filteredOrders) { orderWithProducts ->
                    val order = orderWithProducts.order
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
                                Text(order.createdAt, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.weight(1f))
                                Text(
                                    order.status,
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Divider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                            val expandedOrders = remember { mutableStateMapOf<String, Boolean>() }
                            orderItems.let { items ->
                                val orderId = order.orderId
                                val expanded = expandedOrders[orderId] == true
                                val showItems = if (expanded || items.size <= 2) items else items.take(2)
                                showItems.forEach { orderItemWithProduct ->
                                    val product = orderItemWithProduct.product
                                    if (product != null) {
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
                                                Text(listOfNotNull(product.type.takeIf { it.isNotBlank() }, product.basicColorName.takeIf { it.isNotBlank() }, product.size.takeIf { it.isNotBlank() }).joinToString(", "), color = Color.Gray, fontSize = 12.sp)
                                                Spacer(Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "₫" + String.format("%,.0f", product.price * 25000),
                                                        color = MaterialTheme.colors.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        "x${orderItemWithProduct.orderItem.quantity}",
                                                        color = Color.Gray,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (items.size > 2) {
                                    TextButton(
                                        onClick = { expandedOrders[orderId] = !expanded },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(if (expanded) "Thu gọn" else "Xem thêm (${items.size - 2}) sản phẩm", color = MaterialTheme.colors.primary, fontSize = 13.sp)
                                    }
                                }
                            }
                            Divider(color = Color(0xFFF2F2F2), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.weight(1f))
                                Text("Tổng cộng: ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "₫" + String.format("%,.0f", order.total * 25000),
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
                                when (order.status) {
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
                                            Text("Chi tiết vận chuyển", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

// UI Model cho đơn hàng và sản phẩm
private data class OrderUiModel(
    val id: String,
    val createdAt: String,
    val status: String,
    val products: List<ProductUiModel>,
    val total: String,
)
private data class ProductUiModel(
    val image: Int,
    val name: String,
    val variant: String,
    val priceOrigin: Double?,
    val price: Double,
    val quantity: Int,
)
