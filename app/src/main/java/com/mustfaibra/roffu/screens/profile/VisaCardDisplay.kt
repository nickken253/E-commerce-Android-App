package com.mustfaibra.roffu.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VisaCardDisplay(
    cardNumber: String,
    cardHolder: String,
    expiryMonth: String,
    expiryYear: String
) {
    val formattedNumber = formatCardNumber(cardNumber)
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.7f)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF8E24AA), Color(0xFF2E003E))
                    )
                )
                .padding(24.dp)
        ) {
            // Top row: Chip + NFC
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    Modifier
                        .size(40.dp, 28.dp)
                        .background(Color(0xFFFFD600), RoundedCornerShape(6.dp))
                ) {}
                Spacer(Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            // Card number (centered, single line)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedNumber,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    maxLines = 1
                )
            }
            // VALID FROM & VALID THRU (same row, left side)
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 38.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.padding(end = 32.dp)) {
                    Text(
                        "VALID FROM",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Text(
                        "",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "VALID THRU",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Text(
                        "${expiryMonth.padStart(2, '0')}/$expiryYear",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            // Cardholder bottom left, VISA bottom right
            Box(
                Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = cardHolder.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 4.dp, bottom = 10.dp)
                )
                Text(
                    text = "VISA",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
}

fun formatCardNumber(cardNumber: String): String {
    // Hiển thị 4 số đầu, 4 số cuối, giữa là dấu *
    val clean = cardNumber.replace(" ", "")
    return if (clean.length == 16) {
        val first = clean.take(4)
        val last = clean.takeLast(4)
        "$first **** **** $last"
    } else cardNumber
}