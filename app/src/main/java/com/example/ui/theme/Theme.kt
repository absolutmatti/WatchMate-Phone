package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = CyanNeon,
    onPrimaryContainer = Color.Black,
    secondary = AmberAccent,
    onSecondary = Color.Black,
    tertiary = GreenWatched,
    background = TvBackground,
    onBackground = TextPrimary,
    surface = TvSurface,
    onSurface = TextPrimary,
    surfaceVariant = TvSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = FocusBorderColor
)

@Composable
fun WatchMateTvTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // TV apps always prefer a rich, cinema-grade dark theme for optimal living-room viewing
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
