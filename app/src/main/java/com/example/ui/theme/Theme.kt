package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicDarkScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = SpaceDarkBg,
    secondary = SafeGreen,
    onSecondary = SpaceDarkBg,
    tertiary = AlertRed,
    background = SpaceDarkBg,
    onBackground = TextPrimary,
    surface = SpaceDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = SpaceDarkCard,
    onSurfaceVariant = TextPrimary,
    outline = OutlineColor
)

private val CosmicLightScheme = lightColorScheme(
    primary = SpaceDarkBg,
    onPrimary = TextPrimary,
    secondary = SafeGreen,
    onSecondary = SpaceDarkBg,
    tertiary = AlertRed,
    background = TextPrimary,
    onBackground = SpaceDarkBg,
    surface = Color.White,
    onSurface = SpaceDarkBg,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = SpaceDarkBg,
    outline = Color(0xFFD1D5DB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme or permit light
    dynamicColor: Boolean = false, // Disable dynamic colors to keep cyber neon vibe uniform
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) CosmicDarkScheme else CosmicLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
