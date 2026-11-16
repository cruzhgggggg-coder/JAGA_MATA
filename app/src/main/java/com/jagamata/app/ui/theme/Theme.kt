package com.jagamata.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors =
    lightColorScheme(
        primary = Primary,
        onPrimary = Surface,
        secondary = Mint,
        onSecondary = Ink,
        tertiary = Sunny,
        onTertiary = Ink,
        error = Coral,
        onError = Surface,
        background = Background,
        onBackground = Ink,
        surface = Surface,
        onSurface = Ink,
        surfaceVariant = SurfaceAlt,
        onSurfaceVariant = InkSoft,
        outline = Divider,
    )

// Radius PRD §2.22: card 20–24, sheet 32, pill 999 (diwakili 50)
val JagaMataShapes =
    Shapes(
        extraSmall = RoundedCornerShape(12.dp),
        small = RoundedCornerShape(16.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp),
    )

@Composable
fun JagaMataTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = JagaMataTypography,
        shapes = JagaMataShapes,
        content = content,
    )
}
