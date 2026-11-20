package com.jagamata.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Primary

/**
 * Toggle custom sesuai desain: track 52×26 r13, knob putih 22dp dengan elevasi halus.
 * Ukuran knob SAMA di kedua state (ON: track [Primary] + knob kanan,
 * OFF: track [Divider] + knob kiri).
 */
@Composable
fun JagaSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val trackColor = if (checked) Primary else Divider
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 29.dp else 1.dp,
        label = "jagaSwitchKnob",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
            modifier
                .size(width = 52.dp, height = 26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(trackColor.copy(alpha = if (enabled) 1f else 0.4f))
                .clickable(
                    enabled = enabled,
                    role = Role.Switch,
                    indication = null,
                    interactionSource = interactionSource,
                ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(start = knobOffset)
                    .size(22.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
        )
    }
}
