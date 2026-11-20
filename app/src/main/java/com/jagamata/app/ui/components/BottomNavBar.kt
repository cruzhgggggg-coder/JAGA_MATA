package com.jagamata.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.PrimaryDark
import com.jagamata.app.ui.theme.SurfaceAlt

/** Item tab pada BottomNavBar desain (Beranda · Pantau · Hadiah · Orang Tua). */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
)

/**
 * Bottom nav desain: tinggi 100, full-width 393, radius atas 24, putih;
 * label Nunito Bold 11. Tab aktif: label #2D70B7 + pill 56×32 r16 #E1EFFF.
 */
@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = com.jagamata.app.ui.theme.Surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .clickable { onSelect(index) }
                            .padding(horizontal = 4.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(56.dp)
                                .height(32.dp)
                                .background(
                                    color = if (selected) SurfaceAlt else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(20.dp),
                            tint = if (selected) PrimaryDark else Ink,
                        )
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) PrimaryDark else Ink,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
