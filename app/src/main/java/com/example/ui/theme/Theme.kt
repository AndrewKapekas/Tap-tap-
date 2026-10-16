package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TikTokPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1020),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = TikTokCyan,
    onSecondary = Color(0xFF003635),
    secondaryContainer = Color(0xFF004F4E),
    onSecondaryContainer = Color(0xFF6FF7F1),
    tertiary = TikTokPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3B1E5C),
    onTertiaryContainer = Color(0xFFE9D5FF),
    background = DarkBackground,
    onBackground = OnSurfaceWhite,
    surface = DarkSurface,
    onSurface = OnSurfaceWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnSurfaceMuted,
    outline = Color(0xFF423B5C),
    outlineVariant = Color(0xFF2C2744)
)

private val LightColorScheme = DarkColorScheme // Default to high-contrast sleek dark theme for TikTok aesthetic

@Composable
fun TapTapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
