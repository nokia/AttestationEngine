package com.example.mobileattester.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FONTSIZE_SM = 14.sp
val FONTSIZE_MD = 16.sp
val FONTSIZE_LG = 18.sp
val FONTSIZE_XL = 20.sp
val FONTSIZE_XXL = 24.sp

// Set of Material typography styles to start with
val Typography = Typography(body1 = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = FONTSIZE_MD,
),
    h2 = TextStyle(
        fontSize = FONTSIZE_LG,
        fontWeight = FontWeight.Bold,
        color = PrimaryDark,
    ),
    h3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = FONTSIZE_XXL,
        fontWeight = FontWeight.Normal,
        color = White,
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */)

