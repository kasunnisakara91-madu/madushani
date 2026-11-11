package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisIndigo
import com.example.voice.VoiceState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AiOrbComposable(
    voiceState: VoiceState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_rotation")

    val rotationAngleCW by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cw_spin"
    )

    val rotationAngleCCW by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ccw_spin"
    )

    val basePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Dynamic scale influenced by audio volume
    val dynamicPulse = when (voiceState) {
        is VoiceState.Listening -> basePulse * (1f + amplitude * 0.35f)
        is VoiceState.Speaking -> basePulse * 1.12f
        is VoiceState.Processing -> basePulse * 1.05f
        else -> basePulse
    }

    val primaryColor = when (voiceState) {
        is VoiceState.Listening -> JarvisCyanBright
        is VoiceState.Processing -> JarvisElectricBlue
        is VoiceState.Speaking -> JarvisIndigo
        is VoiceState.Error -> Color(0xFFEF4444)
        else -> JarvisCyan
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.85f

            // 1. Outer Neon Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f * dynamicPulse),
                        primaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.25f
                ),
                radius = baseRadius * 1.25f,
                center = center
            )

            // 2. Outermost dashed orbital ring (Clockwise)
            rotate(degrees = rotationAngleCW, pivot = center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.45f),
                    radius = baseRadius * 0.98f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f)
                    )
                )

                // Perimeter tick marks
                val tickCount = 24
                for (i in 0 until tickCount) {
                    val angle = (i * 360f / tickCount) * (Math.PI / 180.0)
                    val r1 = baseRadius * 0.94f
                    val r2 = baseRadius * 1.02f
                    val start = Offset(
                        center.x + (r1 * cos(angle)).toFloat(),
                        center.y + (r1 * sin(angle)).toFloat()
                    )
                    val end = Offset(
                        center.x + (r2 * cos(angle)).toFloat(),
                        center.y + (r2 * sin(angle)).toFloat()
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.3f),
                        start = start,
                        end = end,
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // 3. Middle Sci-Fi Segmented Ring (Counter-Clockwise)
            rotate(degrees = rotationAngleCCW, pivot = center) {
                drawCircle(
                    color = JarvisCyan.copy(alpha = 0.6f),
                    radius = baseRadius * 0.78f,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(45f, 30f, 15f, 30f), 0f)
                    )
                )
            }

            // 4. Inner Concentric Geometric Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = baseRadius * 0.62f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 5. Pulsing Glowing Core
            val coreRadius = baseRadius * 0.45f * dynamicPulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        JarvisCyanBright,
                        primaryColor,
                        primaryColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 6. Arc Reactor Crosshairs
            val crosshairLen = baseRadius * 0.25f
            drawLine(
                color = primaryColor.copy(alpha = 0.7f),
                start = Offset(center.x - crosshairLen, center.y),
                end = Offset(center.x + crosshairLen, center.y),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.7f),
                start = Offset(center.x, center.y - crosshairLen),
                end = Offset(center.x, center.y + crosshairLen),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}
