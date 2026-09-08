package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.ChromaStyle
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChromaVisualizerRing(
    albumArtRes: Int?,
    albumArtUri: String?,
    isPlaying: Boolean,
    chromaStyle: ChromaStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    frequencies: List<Float>? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chroma_anim")

    // Rotation animation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 7000 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chroma_rotation"
    )

    // Pulse animation (expands subtly when music is playing)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.05f else 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chroma_pulse"
    )

    // Glow intensity
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (isPlaying) 0.95f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chroma_glow"
    )

    val context = LocalContext.current

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(pulseScale)
            .testTag("chroma_visualizer_box"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Chroma Ring Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationAngle)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) - 16.dp.toPx()

            when (chromaStyle) {
                ChromaStyle.NEON_CYBER -> {
                    // Dual neon arcs in Cyan and Vibrant Orange with glowing ends
                    val strokeW = 10.dp.toPx()
                    val brush1 = Brush.sweepGradient(
                        listOf(
                            Color(0xFF00F0FF).copy(alpha = glowAlpha),
                            Color(0xFFFF8500).copy(alpha = glowAlpha),
                            Color(0xFF7928CA).copy(alpha = glowAlpha * 0.7f),
                            Color(0xFF00F0FF).copy(alpha = glowAlpha)
                        )
                    )
                    drawCircle(
                        brush = brush1,
                        radius = baseRadius + 4.dp.toPx(),
                        center = center,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Secondary inner orbit
                    drawCircle(
                        color = Color(0xFF00F0FF).copy(alpha = 0.35f),
                        radius = baseRadius - 6.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                ChromaStyle.RAINBOW_NEBULA -> {
                    // Full rainbow spectrum sweep
                    val rainbowColors = listOf(
                        Color(0xFFFF0055),
                        Color(0xFFFF9900),
                        Color(0xFFFFFF00),
                        Color(0xFF00FF66),
                        Color(0xFF00E5FF),
                        Color(0xFF7928CA),
                        Color(0xFFFF0055)
                    ).map { it.copy(alpha = glowAlpha) }

                    drawCircle(
                        brush = Brush.sweepGradient(rainbowColors),
                        radius = baseRadius + 3.dp.toPx(),
                        center = center,
                        style = Stroke(width = 11.dp.toPx())
                    )
                }

                ChromaStyle.SPECTRUM_WAVE -> {
                    // 36 dynamic radial bars radiating outward like audio equalizer
                    val barCount = 40
                    val angleStep = (2 * PI / barCount)
                    for (i in 0 until barCount) {
                        val angle = (i * angleStep).toFloat()
                        // dynamic wave height simulation using real frequency spectrum if available
                        val waveOffset = if (isPlaying) {
                            val freqVal = frequencies?.getOrElse(i % frequencies.size) { 0.4f } ?: 0.4f
                            val harmonic = sin((i * 3.5 + rotationAngle * 0.05).toDouble()).toFloat()
                            ((harmonic * 8.dp.toPx() + freqVal * 16.dp.toPx())).coerceAtLeast(2.dp.toPx())
                        } else {
                            4.dp.toPx()
                        }

                        val innerR = baseRadius - 4.dp.toPx()
                        val outerR = innerR + 12.dp.toPx() + waveOffset

                        val startX = center.x + innerR * cos(angle)
                        val startY = center.y + innerR * sin(angle)
                        val endX = center.x + outerR * cos(angle)
                        val endY = center.y + outerR * sin(angle)

                        val barColor = if (i % 2 == 0) Color(0xFF00F0FF) else Color(0xFFFF7A00)

                        drawLine(
                            color = barColor.copy(alpha = glowAlpha),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 3.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                ChromaStyle.GOLDEN_FIRE -> {
                    // Golden, fiery amber aura
                    val fireColors = listOf(
                        Color(0xFFFFD700),
                        Color(0xFFFF6A00),
                        Color(0xFFFF2200),
                        Color(0xFFFFD700)
                    ).map { it.copy(alpha = glowAlpha) }

                    drawCircle(
                        brush = Brush.sweepGradient(fireColors),
                        radius = baseRadius + 5.dp.toPx(),
                        center = center,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                ChromaStyle.COSMIC_PULSE -> {
                    // Cosmic deep galactic aura
                    val cosmicColors = listOf(
                        Color(0xFF7928CA),
                        Color(0xFF4A00E0),
                        Color(0xFF8E2DE2),
                        Color(0xFF00F0FF),
                        Color(0xFF7928CA)
                    ).map { it.copy(alpha = glowAlpha) }

                    drawCircle(
                        brush = Brush.sweepGradient(cosmicColors),
                        radius = baseRadius + 4.dp.toPx(),
                        center = center,
                        style = Stroke(width = 10.dp.toPx())
                    )
                }
            }
        }

        // Inner Album Cover Disc (Clickable: triggers lyrics toggle)
        Box(
            modifier = Modifier
                .fillMaxSize(0.78f)
                .shadow(28.dp, shape = CircleShape, spotColor = NeonCyan)
                .clip(CircleShape)
                .border(3.dp, Brush.sweepGradient(listOf(Color(0xFF00F0FF), Color(0xFFFF8500), Color(0xFF00F0FF))), CircleShape)
                .background(Color(0xFF0E1019))
                .clickable { onClick() }
                .testTag("album_cover_clickable_center"),
            contentAlignment = Alignment.Center
        ) {
            if (!albumArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Song Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = albumArtRes ?: R.drawable.ic_remo_brand),
                    contentDescription = "Song Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Subtle Vinyl Spindle Center Hole
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(DarkBackground)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            )

            // Tap for Lyrics Prompt Badge (Overlaid subtly at bottom)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated.copy(alpha = 0.88f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = " اضغط للكلمات",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
