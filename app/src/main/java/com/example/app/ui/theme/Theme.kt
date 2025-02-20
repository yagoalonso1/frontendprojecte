package com.example.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RedLight,
    secondary = RedMedium,
    background = BlackDark,
    surface = GrayLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = BlackDark
)

private val LightColorScheme = lightColorScheme(
    primary = RedLight,
    secondary = RedMedium,
    background = Color.White,
    surface = GrayLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BlackDark,
    onSurface = BlackDark
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}