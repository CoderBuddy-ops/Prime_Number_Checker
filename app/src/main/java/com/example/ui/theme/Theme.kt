package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonMagenta,
    background = CosmicBackground,
    surface = CosmicSurface,
    onBackground = OnCosmicBackground,
    onSurface = OnCosmicSurface,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = OnCosmicSurfaceMuted,
    outline = CosmicBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonMagenta,
    background = CosmicBackground,
    surface = CosmicSurface,
    onBackground = OnCosmicBackground,
    onSurface = OnCosmicSurface,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = OnCosmicSurfaceMuted,
    outline = CosmicBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark theme for neon math glow!
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our tailored brand identity
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
