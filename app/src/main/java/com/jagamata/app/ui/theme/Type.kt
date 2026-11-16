package com.jagamata.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jagamata.app.R

/**
 * Design token PRD §2.22: Baloo 2 (judul/angka) + Nunito (isi).
 * Skala ukuran 11/13/15/16/18/24/32/48.
 */
val DisplayFontFamily =
    FontFamily(
        Font(R.font.baloo2_variable, FontWeight.Normal),
        Font(R.font.baloo2_variable, FontWeight.Medium),
        Font(R.font.baloo2_variable, FontWeight.SemiBold),
        Font(R.font.baloo2_variable, FontWeight.Bold),
    )

val BodyFontFamily =
    FontFamily(
        Font(R.font.nunito_variable, FontWeight.Normal),
        Font(R.font.nunito_variable, FontWeight.Medium),
        Font(R.font.nunito_variable, FontWeight.SemiBold),
        Font(R.font.nunito_variable, FontWeight.Bold),
    )

val JagaMataTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            ),
    )
