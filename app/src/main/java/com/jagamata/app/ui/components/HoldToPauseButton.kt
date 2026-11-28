package com.jagamata.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Detektor tekan-tahan murni dan dapat diuji.
 * [press] memulai hitungan mundur [holdMs]; [release] membatalkan.
 * Saat waktu tahan tercapai, [onActivated] dipanggil.
 */
class HoldDetector(
    private val scope: CoroutineScope,
    private val holdMs: Long = DEFAULT_HOLD_MS,
    private val onActivated: () -> Unit,
) {
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private var holdJob: Job? = null
    private var progressJob: Job? = null

    fun press() {
        release()
        holdJob =
            scope.launch {
                delay(holdMs)
                onActivated()
            }
        progressJob =
            scope.launch {
                val steps = (holdMs / PROGRESS_TICK_MS).toInt().coerceAtLeast(1)
                for (i in 1..steps) {
                    delay(PROGRESS_TICK_MS)
                    _progress.value = i.toFloat() / steps
                }
            }
    }

    fun release() {
        holdJob?.cancel()
        progressJob?.cancel()
        holdJob = null
        progressJob = null
        _progress.value = 0f
    }

    companion object {
        const val DEFAULT_HOLD_MS = 3000L
        private const val PROGRESS_TICK_MS = 50L
    }
}

/**
 * Tombol jeda dengan cincin progres animasi.
 * Saat ditekan-tahan, progres berputar dan hint muncul di bawah.
 */
@Composable
fun HoldToPauseButton(
    progress: Float,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Pause,
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "holdProgress")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(52.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onPressStart()
                                tryAwaitRelease()
                                onPressEnd()
                            },
                        )
                    },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(52.dp)) {
                val strokeWidth = 4.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
                val arcSize = Size(radius * 2, radius * 2)

                drawArc(
                    color = Ink.copy(alpha = 0.15f),
                    startAngle = START_ANGLE,
                    sweepAngle = FULL_CIRCLE,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                if (animatedProgress > 0f) {
                    drawArc(
                        color = Sunny,
                        startAngle = START_ANGLE,
                        sweepAngle = FULL_CIRCLE * animatedProgress.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
            }
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(Ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Surface,
                )
            }
        }
        if (progress > 0f) {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = Ink.copy(alpha = 0.72f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private const val START_ANGLE = -90f
private const val FULL_CIRCLE = 360f
