package com.example.thewatcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFA5D6A7)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B5E20),
    secondary = Color(0xFF00897B),
    tertiary = Color(0xFF2E7D32)
)

@Composable
fun TheWatcherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
