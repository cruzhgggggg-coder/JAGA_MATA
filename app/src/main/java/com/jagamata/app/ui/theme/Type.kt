package com.jagamata.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Skala ukuran PRD §2.22: 11/13/15/16/18/24/32/48
// Baloo 2 (judul/angka) & Nunito (isi) — dipasang sebagai res/font (DEV-19).
// Sementara memakai fallback sistem agar kompilabel.
val DisplayFontFamily = FontFamily.Serif
val BodyFontFamily = FontFamily.SansSerif

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
        labelSmall =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            ),
    )
