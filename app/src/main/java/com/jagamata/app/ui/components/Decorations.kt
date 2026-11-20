package com.jagamata.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.jagamata.app.ui.theme.Sunny

/**
 * Sparkle desain (Dekorasi/Sparkle): bintang 4 sudut cekung, default #FFD166.
 * Pengganti tanda plus/dot yang tidak sesuai desain.
 */
@Composable
fun Sparkle(
    modifier: Modifier = Modifier,
    color: Color = Sunny,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rx = w / 2f
        val ry = h / 2f
        // kontrol cekungan: 0.25 dari setengah sisi → khas sparkle runcing
        val k = 0.22f
        val path =
            Path().apply {
                moveTo(cx, cy - ry)
                quadraticBezierTo(cx + rx * k, cy - ry * k, cx + rx, cy)
                quadraticBezierTo(cx + rx * k, cy + ry * k, cx, cy + ry)
                quadraticBezierTo(cx - rx * k, cy + ry * k, cx - rx, cy)
                quadraticBezierTo(cx - rx * k, cy - ry * k, cx, cy - ry)
                close()
            }
        drawPath(path, color = color)
    }
}
