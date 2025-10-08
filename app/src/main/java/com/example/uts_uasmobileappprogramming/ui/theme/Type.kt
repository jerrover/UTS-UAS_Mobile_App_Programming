package com.example.uts_uasmobileappprogramming.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.uts_uasmobileappprogramming.R

val NunitoSans = FontFamily(
    Font(
        resId = R.font.nunitosans_variable,
        weight = FontWeight.Normal
    )
)

// Buat objek Typography dengan penyesuaian ketebalan
val Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.SemiBold, // Sedikit tebal agar terbaca jelas
        fontSize = 12.sp
    )

)