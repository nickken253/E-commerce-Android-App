package dev.vstd.shoppingcart.common.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.keego.shoppingcart.R

private val ralewayFontFamily = FontFamily(
    Font(R.font.raleway_regular, FontWeight.Normal),
    Font(R.font.raleway_medium, FontWeight.Medium),
    Font(R.font.raleway_bold, FontWeight.Bold),
    Font(R.font.raleway_extrabold, FontWeight.ExtraBold)
)

val AppTypography = Typography(
    bodyMedium = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodySmall = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    titleLarge = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleMedium = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleSmall = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    headlineLarge = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp),
    headlineMedium = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    headlineSmall = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    displayLarge = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 42.sp),
    displayMedium = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp),
    displaySmall = TextStyle(fontFamily = ralewayFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
)
