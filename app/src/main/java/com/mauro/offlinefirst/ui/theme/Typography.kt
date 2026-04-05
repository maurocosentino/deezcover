package com.mauro.offlinefirst.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mauro.offlinefirst.R


val BoldFontFree = FontFamily(
    Font(R.font.theboldfont_freeversion)
)

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = PlusJakartaSansFamily),
    displayMedium = TextStyle(fontFamily = PlusJakartaSansFamily),
    displaySmall = TextStyle(fontFamily = PlusJakartaSansFamily),
    headlineLarge = TextStyle(fontFamily = PlusJakartaSansFamily),
    headlineMedium = TextStyle(fontFamily = PlusJakartaSansFamily),
    headlineSmall = TextStyle(fontFamily = PlusJakartaSansFamily),
    titleLarge = TextStyle(fontFamily = PlusJakartaSansFamily),
    titleMedium = TextStyle(fontFamily = PlusJakartaSansFamily),
    titleSmall = TextStyle(fontFamily = PlusJakartaSansFamily),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(fontFamily = PlusJakartaSansFamily),
    bodySmall = TextStyle(fontFamily = PlusJakartaSansFamily),
    labelLarge = TextStyle(fontFamily = PlusJakartaSansFamily),
    labelMedium = TextStyle(fontFamily = PlusJakartaSansFamily),
    labelSmall = TextStyle(fontFamily = PlusJakartaSansFamily)
)
