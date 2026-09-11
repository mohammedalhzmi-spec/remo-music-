package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import kotlinx.coroutines.delay

/**
 * Live Audio Frequency Spectrum Visualizer component.
 * Synchronizes in real time with the current audio playback frequencies,
 * featuring glowing neon spectrum bars, gravity-decay peak indicators,
 * and frequency spectrum band markers.
 */
@Composable
fun LiveAudioFrequencyVisualizer(
    frequencies: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(72.dp),
    barColorGradient: List<Color> = listOf(NeonCyan, ElectricViolet, NeonMagenta, AmberGold),
    showLabels: Boolean = true
) {
    val barCount = frequencies.size.coerceAtLeast(16)
    // Peak meter states that fall with gravity
    val peakHeights = remember { mutableStateListOf<Float>().apply { repeat(barCount) { add(0f) } } }

    LaunchedEffect(frequencies, isPlaying) {
        for (i in 0 until minOf(barCount, frequencies.size)) {
            val currentVal = if (isPlaying) frequencies[i] else 0.05f
            if (i < peakHeights.size) {
                if (currentVal > peakHeights[i]) {
                    peakHeights[i] = currentVal
                } else {
                    peakHeights[i] = (peakHeights[i] - 0.04f).coerceAtLeast(currentVal)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val barSpacing = 4f
            val totalSpacing = barSpacing * (barCount + 1)
            val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(3f)

            val verticalBrush = Brush.verticalGradient(
                colors = barColorGradient,
                startY = height,
                endY = 0f
            )

            for (i in 0 until barCount) {
                val amp = if (isPlaying) {
                    frequencies.getOrElse(i) { 0.05f }.coerceIn(0.04f, 1.0f)
                } else {
                    0.04f
                }

                val barHeight = (height * amp).coerceAtLeast(6f)
                val x = barSpacing + i * (barWidth + barSpacing)
                val y = height - barHeight

                // Draw glowing spectrum bar
                drawRoundRect(
                    brush = verticalBrush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )

                // Draw studio peak floating indicator (small neon dot on top)
                val peakAmp = peakHeights.getOrElse(i) { amp }
                val peakY = (height - (height * peakAmp) - 4f).coerceIn(0f, height - 6f)

                drawRoundRect(
                    color = AmberGold,
                    topLeft = Offset(x, peakY),
                    size = Size(barWidth, 3f),
                    cornerRadius = CornerRadius(1.5f, 1.5f)
                )
            }
        }

        if (showLabels) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "60Hz (Bass)",
                    color = NeonCyan.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "250Hz",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "1kHz (Vocals)",
                    color = ElectricViolet.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "4kHz",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "16kHz (Treble)",
                    color = AmberGold.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AudioWaveVisualizer(
    isPlaying: Boolean,
    barCount: Int = 28,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
) {
    val transition = rememberInfiniteTransition(label = "audio_bars")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val barWidth = (totalWidth / (barCount * 1.5f)).coerceAtLeast(3f)
        val spacing = barWidth * 0.5f
        val maxHeight = size.height

        val gradient = Brush.verticalGradient(
            colors = listOf(NeonCyan, ElectricViolet, NeonMagenta),
            startY = 0f,
            endY = maxHeight
        )

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount
            val wave = if (isPlaying) {
                val factor = kotlin.math.sin(normalizedIndex * Math.PI * 2.0 + phase * Math.PI).toFloat()
                val heightRatio = (kotlin.math.abs(factor) * 0.75f + 0.25f).coerceIn(0.15f, 1.0f)
                maxHeight * heightRatio
            } else {
                maxHeight * 0.12f
            }

            val x = i * (barWidth + spacing) + spacing
            val y = (maxHeight - wave) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, wave),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
