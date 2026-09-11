package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = PrimaryDark,
  onPrimary = OnPrimaryDark,
  primaryContainer = PrimaryContainerDark,
  onPrimaryContainer = OnPrimaryContainerDark,
  secondary = SecondaryDark,
  onSecondary = OnSecondaryDark,
  secondaryContainer = SecondaryContainerDark,
  onSecondaryContainer = OnSecondaryContainerDark,
  tertiary = TertiaryDark,
  onTertiary = OnTertiaryDark,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  surfaceTint = NeonCyan
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF007A8A),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFC7F3F9),
  onPrimaryContainer = Color(0xFF002025),
  secondary = Color(0xFF6C3B9E),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFEADBFF),
  onSecondaryContainer = Color(0xFF260548),
  tertiary = Color(0xFFBA1A5B),
  background = Color(0xFFF7F8FC),
  onBackground = Color(0xFF13151F),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF13151F),
  surfaceVariant = Color(0xFFE8EAF2),
  onSurfaceVariant = Color(0xFF4A4E63),
  outline = Color(0xFFCBD0E0),
  surfaceTint = Color(0xFF007A8A)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek music player dark mode
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
