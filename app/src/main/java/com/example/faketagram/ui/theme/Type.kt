package com.example.faketagram.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.faketagram.R

val helveticaNeue = FontFamily(
    Font(R.font.helvetica_neue_black, FontWeight.Black),
    Font(R.font.helvetica_neue_black_italic, FontWeight.Black, FontStyle.Italic),
    Font(R.font.helvetica_neue_bold, FontWeight.Bold),
    Font(R.font.helvetica_neue_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.helvetica_neue_heavy, FontWeight.ExtraBold),
    Font(R.font.helvetica_neue_heavy_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.helvetica_neue_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.helvetica_neue_light, FontWeight.Light),
    Font(R.font.helvetica_neue_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.helvetica_neue_medium, FontWeight.Medium),
    Font(R.font.helvetica_neue_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.helvetica_neue_roman, FontWeight.Normal),
    Font(R.font.helvetica_neue_thin, FontWeight.Thin),
    Font(R.font.helvetica_neue_thin_italic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.helvetica_neue_ultra_light, FontWeight.ExtraLight),
    Font(R.font.helvetica_neue_ultra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = helveticaNeue,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = helveticaNeue,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = helveticaNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    titleLarge = TextStyle(
        fontFamily = helveticaNeue,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = helveticaNeue,
        fontWeight = FontWeight.Thin,
        fontSize = 27.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )
)

