package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan

@Composable
fun RotatingVinylArtwork(
    albumArtRes: Int?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing)
        ),
        label = "angle"
    )

    val currentRotation = if (isPlaying) angle else 0f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(24.dp, shape = CircleShape, spotColor = NeonCyan),
        contentAlignment = Alignment.Center
    ) {
        // Outer Vinyl Disc
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .rotate(currentRotation),
            color = Color(0xFF0F111A),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(NeonCyan.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.6f), NeonCyan.copy(alpha = 0.6f))
                        ),
                        shape = CircleShape
                    )
            ) {
                // Vinyl grooves
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.dp, Color(0xFF22273D), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .border(1.dp, Color(0xFF1B1E30), CircleShape)
                )
            }
        }

        // Center Album Artwork Label
        Box(
            modifier = Modifier
                .fillMaxSize(0.66f)
                .clip(CircleShape)
                .rotate(currentRotation)
                .border(3.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = albumArtRes ?: R.drawable.ic_app_icon_1788818206748),
                contentDescription = "Album Artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Center spindle hole
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(DarkBackground)
                    .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            )
        }
    }
}
