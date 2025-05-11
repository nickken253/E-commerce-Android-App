package com.mustfaibra.roffu.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@Composable
fun AddVirtualCardScreen(
    onCardAdded: (cardNumber: String, month: String, year: String, cvv: String, cardHolder: String) -> Unit,
    onCancel: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    var cardNumberTouched by remember { mutableStateOf(false) }
    var monthTouched by remember { mutableStateOf(false) }
    var yearTouched by remember { mutableStateOf(false) }
    var cvvTouched by remember { mutableStateOf(false) }
    var cardHolderTouched by remember { mutableStateOf(false) }

    val cardNumberFocusRequester = remember { FocusRequester() }
    val monthFocusRequester = remember { FocusRequester() }
    val yearFocusRequester = remember { FocusRequester() }
    val cvvFocusRequester = remember { FocusRequester() }
    val cardHolderFocusRequester = remember { FocusRequester() }

    var cardNumberError by remember { mutableStateOf("") }
    var monthError by remember { mutableStateOf("") }
    var yearError by remember { mutableStateOf("") }
    var cvvError by remember { mutableStateOf("") }
    var cardHolderError by remember { mutableStateOf("") }

    fun validateCardNumber(): String = when {
        cardNumber.isBlank() -> "Required"
        !cardNumber.matches(Regex("\\d{16}")) -> "Mã số thẻ phải có 16 chữ số"
        else -> ""
    }
    fun validateMonth(): String = when {
        month.isBlank() -> "Required"
        month.toIntOrNull() == null || month.toInt() !in 1..12 -> "Tháng không hợp lệ"
        else -> ""
    }
    
    fun formatMonth(input: String): String {
        // Chỉ lấy tối đa 2 ký tự số
        val filtered = input.filter { it.isDigit() }.take(2)
        val monthValue = filtered.toIntOrNull()
        
        // Nếu là số hợp lệ từ 1-12, định dạng thành 2 chữ số
        return if (monthValue != null && monthValue in 1..12) {
            monthValue.toString().padStart(2, '0')
        } else {
            filtered
        }
    }
    fun validateYear(): String = when {
        year.isBlank() -> "Required"
        year.toIntOrNull() == null || year.length != 4 || year.toInt() < 2025 -> "Năm không hợp lệ"
        else -> ""
    }
    fun validateCVV(): String = when {
        cvv.isBlank() -> "Required"
        !cvv.matches(Regex("\\d{3,4}")) -> "CVV phải có 3 hoặc 4 số"
        else -> ""
    }
    fun validateCardHolder(): String = when {
        cardHolder.isBlank() -> "Required"
        !cardHolder.matches(Regex("^[A-Za-z ]{2,30}$")) -> "Tên không hợp lệ"
        else -> ""
    }

    fun validateAllFieldsAndSetErrors(): Boolean {
        cardNumberError = validateCardNumber()
        monthError = validateMonth()
        yearError = validateYear()
        cvvError = validateCVV()
        cardHolderError = validateCardHolder()
        return listOf(cardNumberError, monthError, yearError, cvvError, cardHolderError).all { it.isEmpty() }
    }

    LaunchedEffect(Unit) {
        cardNumberTouched = false
        monthTouched = false
        yearTouched = false
        cvvTouched = false
        cardHolderTouched = false
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = 8.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(340.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Credit card detail", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = {
                        cardNumber = it
                        if (cardNumberTouched) cardNumberError = validateCardNumber()
                    },
                    label = { Text("Card Number") },
                    isError = cardNumberTouched && cardNumberError.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(cardNumberFocusRequester)
                        .onFocusChanged {
                            if (!it.isFocused && !cardNumberTouched) cardNumberTouched = true
                            if (!it.isFocused) cardNumberError = validateCardNumber()
                        },
                    singleLine = true,
                )
                if (cardNumberTouched && cardNumberError.isNotEmpty()) {
                    Text(cardNumberError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = month,
                            onValueChange = {
                                // Giới hạn chỉ nhập số và tối đa 2 ký tự
                                if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.length <= 2)) {
                                    month = it
                                    if (monthTouched) monthError = validateMonth()
                                }
                            },
                            label = { Text("Month") },
                            isError = monthTouched && monthError.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(monthFocusRequester)
                                .onFocusChanged {
                                    if (!it.isFocused && !monthTouched) monthTouched = true
                                    if (!it.isFocused) monthError = validateMonth()
                                },
                            singleLine = true,
                        )
                        if (monthTouched && monthError.isNotEmpty()) {
                            Text(monthError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = year,
                            onValueChange = {
                                year = it
                                if (yearTouched) yearError = validateYear()
                            },
                            label = { Text("Year") },
                            isError = yearTouched && yearError.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(yearFocusRequester)
                                .onFocusChanged {
                                    if (!it.isFocused && !yearTouched) yearTouched = true
                                    if (!it.isFocused) yearError = validateYear()
                                },
                            singleLine = true,
                        )
                        if (yearTouched && yearError.isNotEmpty()) {
                            Text(yearError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = cvv,
                    onValueChange = {
                        cvv = it
                        if (cvvTouched) cvvError = validateCVV()
                    },
                    label = { Text("CVV") },
                    isError = cvvTouched && cvvError.isNotEmpty(),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(cvvFocusRequester)
                        .onFocusChanged {
                            if (!it.isFocused && !cvvTouched) cvvTouched = true
                            if (!it.isFocused) cvvError = validateCVV()
                        },
                    placeholder = { Text("CVV thường có 3 hoặc 4 ký tự") },
                    singleLine = true,
                )
                if (cvvTouched && cvvError.isNotEmpty()) {
                    Text(cvvError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = cardHolder,
                    onValueChange = {
                        cardHolder = it
                        if (cardHolderTouched) cardHolderError = validateCardHolder()
                    },
                    label = { Text("Card Holder Name") },
                    isError = cardHolderTouched && cardHolderError.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(cardHolderFocusRequester)
                        .onFocusChanged {
                            if (!it.isFocused && !cardHolderTouched) cardHolderTouched = true
                            if (!it.isFocused) cardHolderError = validateCardHolder()
                        },
                    singleLine = true,
                )
                if (cardHolderTouched && cardHolderError.isNotEmpty()) {
                    Text(cardHolderError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = {
                            val allValid = validateAllFieldsAndSetErrors()
                            cardNumberTouched = true
                            monthTouched = true
                            yearTouched = true
                            cvvTouched = true
                            cardHolderTouched = true
                            if (allValid) {
                                error = ""
                                onCardAdded(cardNumber, month, year, cvv, cardHolder)
                            } else {
                                error = "Please fix all errors above"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6FCF97)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Proceed", color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.Black)
                    }
                }
            }
        }
    }
}
