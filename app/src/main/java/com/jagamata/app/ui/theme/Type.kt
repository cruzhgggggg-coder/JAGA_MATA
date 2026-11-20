@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.jagamata.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jagamata.app.R

/**
 * Design tokens — teks dari foundations (variabel "Tipografi"):
 * Baloo 2 (Display/H1/H2/Number) + Nunito (Body/Caption/Micro/Button).
 * Variable font: weight via FontVariation; includeFontPadding=false agar
 * line box sesuai lineHeight desain (tanpa padding font ekstra Android).
 */
val DisplayFontFamily =
    FontFamily(
        Font(
            R.font.baloo2_variable,
            FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
        Font(
            R.font.baloo2_variable,
            FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
        Font(
            R.font.baloo2_variable,
            FontWeight.SemiBold,
            variationSettings = FontVariation.Settings(FontVariation.weight(600)),
        ),
        Font(
            R.font.baloo2_variable,
            FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        ),
        Font(
            R.font.baloo2_variable,
            FontWeight.ExtraBold,
            variationSettings = FontVariation.Settings(FontVariation.weight(800)),
        ),
    )

val BodyFontFamily =
    FontFamily(
        Font(
            R.font.nunito_variable,
            FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
        Font(
            R.font.nunito_variable,
            FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
        Font(
            R.font.nunito_variable,
            FontWeight.SemiBold,
            variationSettings = FontVariation.Settings(FontVariation.weight(600)),
        ),
        Font(
            R.font.nunito_variable,
            FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        ),
    )

private fun jagaStyle(
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    sizeSp: Int,
    lineHeightSp: Int,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle =
    TextStyle(
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = sizeSp.sp,
        lineHeight = lineHeightSp.sp,
        letterSpacing = letterSpacing,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

val JagaMataTypography =
    Typography(
        // Display — Baloo 2 ExtraBold 48/56
        displayLarge = jagaStyle(DisplayFontFamily, FontWeight.ExtraBold, 48, 56),
        // Number — Baloo 2 Bold 32/40
        displayMedium = jagaStyle(DisplayFontFamily, FontWeight.Bold, 32, 40),
        // H1 — Baloo 2 Bold 24/32
        headlineMedium = jagaStyle(DisplayFontFamily, FontWeight.Bold, 24, 32),
        // H2 — Baloo 2 Bold 18/26
        titleLarge = jagaStyle(DisplayFontFamily, FontWeight.Bold, 18, 26),
        titleMedium = jagaStyle(DisplayFontFamily, FontWeight.SemiBold, 16, 22),
        titleSmall = jagaStyle(DisplayFontFamily, FontWeight.SemiBold, 15, 20),
        // Body — Nunito Medium 16/24
        bodyLarge = jagaStyle(BodyFontFamily, FontWeight.Medium, 16, 24),
        // Body Small — Nunito Medium 15/22
        bodyMedium = jagaStyle(BodyFontFamily, FontWeight.Medium, 15, 22),
        // Caption — Nunito SemiBold 13/18
        bodySmall = jagaStyle(BodyFontFamily, FontWeight.SemiBold, 13, 18),
        // Button — Nunito Bold 16/24
        labelLarge = jagaStyle(BodyFontFamily, FontWeight.Bold, 16, 24),
        labelMedium = jagaStyle(BodyFontFamily, FontWeight.SemiBold, 13, 18),
        // Micro — Nunito Bold 11/14, letter-spacing +2%
        labelSmall = jagaStyle(BodyFontFamily, FontWeight.Bold, 11, 14, 0.02.em),
    )
