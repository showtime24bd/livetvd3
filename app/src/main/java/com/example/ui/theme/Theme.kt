package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvDarkColorScheme = darkColorScheme(
    primary = TvCyanAccent,
    onPrimary = Color.Black,
    primaryContainer = TvSurfaceElevated,
    onPrimaryContainer = TvCyanAccent,
    secondary = TvEmeraldAccent,
    onSecondary = Color.Black,
    background = TvBackgroundDark,
    onBackground = TvTextPrimary,
    surface = TvSurfaceDark,
    onSurface = TvTextPrimary,
    surfaceVariant = TvCardBackground,
    onSurfaceVariant = TvTextSecondary,
    error = TvLiveRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        typography = Typography,
        content = content
    )
}
