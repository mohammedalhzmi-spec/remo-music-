package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2400)
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_halo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF161A2E),
                        DarkBackground,
                        Color(0xFF05060A)
                    )
                )
            )
            .testTag("animated_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Skip Button in top corner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onDismiss() }
        ) {
            Text(
                text = "تخطي ❯",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(20.dp)
        ) {
            // Glowing Ambient Halos & Wide Banner Asset
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(200.dp)
            ) {
                // Outer Cyan & Orange ambient ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00F0FF).copy(alpha = haloAlpha * 0.4f),
                                    Color(0xFFFF8500).copy(alpha = haloAlpha * 0.45f),
                                    Color(0xFF7928CA).copy(alpha = haloAlpha * 0.35f),
                                    Color(0xFF00F0FF).copy(alpha = haloAlpha * 0.4f)
                                )
                            )
                        )
                )

                // Wide Banner Container with Logo Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(184.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .shadow(32.dp, RoundedCornerShape(26.dp), spotColor = NeonCyan)
                        .border(
                            2.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF00F0FF).copy(alpha = 0.85f),
                                    Color(0xFFFF8500).copy(alpha = 0.85f)
                                )
                            ),
                            RoundedCornerShape(26.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_remo_brand),
                        contentDescription = "REMO Splash Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Brand Typography
            Text(
                text = "REMO PLAYER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Arabic Subtitle with equalizer wave accents
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color(0xFFFF8500),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مشغل الفيديوهات والموسيقى المدمج",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Equalizer Wave
            AudioWaveVisualizer(
                isPlaying = true,
                modifier = Modifier
                    .width(180.dp)
                    .height(30.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = NeonCyan,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "جاري تهيئة المعادل الذكي والمكتبة...",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // Developer Credit at bottom of splash
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "تطوير: محمد الحزمي",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
