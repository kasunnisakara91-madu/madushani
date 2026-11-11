package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisElectricBlue
import com.example.voice.VoiceState
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    voiceState: VoiceState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    height: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val isActive = voiceState is VoiceState.Listening || voiceState is VoiceState.Speaking

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val normalizedIdx = i.toFloat() / barCount
            // Sine wave height modulated by index and phase
            val sineVal = (sin((normalizedIdx * 4 * Math.PI + phase).toDouble()).toFloat() + 1f) / 2f
            val baseHeightFraction = if (isActive) {
                (0.15f + sineVal * 0.4f + amplitude * 0.45f).coerceIn(0.1f, 1f)
            } else {
                0.08f + sineVal * 0.08f
            }

            val barBrush = Brush.verticalGradient(
                colors = listOf(
                    JarvisCyanBright,
                    JarvisCyan,
                    JarvisElectricBlue
                )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(baseHeightFraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isActive) barBrush else Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))))
            )

            if (i < barCount - 1) {
                Box(modifier = Modifier.width(3.dp))
            }
        }
    }
}
