package com.mustfaibra.roffu.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PaymentCardsScreen(
    onBackPressed: () -> Unit,
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val bankCards = paymentViewModel.bankCards.value
    val isLoadingCards = paymentViewModel.isLoadingCards.value
    val error = paymentViewModel.error.value
    
    // Lấy danh sách thẻ khi màn hình được hiển thị
    LaunchedEffect(Unit) {
        paymentViewModel.getBankCards(context)
    }
    
    // Biến state để kiểm soát hiển thị dialog thêm thẻ
    var showAddCardDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thẻ thanh toán") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCardDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Card")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoadingCards) {
                // Hiển thị loading
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                // Hiển thị lỗi
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { paymentViewModel.getBankCards(context) }) {
                        Text("Thử lại")
                    }
                }
            } else if (bankCards.isEmpty()) {
                // Hiển thị thông báo khi không có thẻ nào
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bạn chưa có thẻ thanh toán nào",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { showAddCardDialog = true }) {
                        Text("Thêm thẻ mới")
                    }
                }
            } else {
                // Hiển thị danh sách thẻ
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bankCards) { card ->
                        VisaCardDisplay(
                            cardNumber = card.cardNumber,
                            cardHolder = card.cardHolderName,
                            expiryMonth = card.expiryMonth,
                            expiryYear = card.expiryYear
                        )
                        
                        // Hiển thị thông tin thêm (nếu là thẻ mặc định)
                        if (card.isDefault) {
                            Text(
                                text = "Thẻ mặc định",
                                color = Color.Green,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
    
    // Dialog thêm thẻ mới
    if (showAddCardDialog) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Thêm thẻ thanh toán") },
            text = {
                AddVirtualCardScreen(
                    onCardAdded = { cardNumber, month, year, cvv, cardHolder ->
                        showAddCardDialog = false
                    },
                    onCancel = { showAddCardDialog = false }
                )
            },
            buttons = {}
        )
    }
}
