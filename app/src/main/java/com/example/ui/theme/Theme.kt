package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisNavyBlack,
    primaryContainer = JarvisCyanDark,
    onPrimaryContainer = JarvisCyanBright,
    secondary = JarvisBlue,
    onSecondary = JarvisNavyBlack,
    secondaryContainer = JarvisSurfaceElevated,
    onSecondaryContainer = JarvisCyanBright,
    tertiary = JarvisPurple,
    onTertiary = Color.White,
    background = JarvisNavyBlack,
    onBackground = JarvisTextPrimary,
    surface = JarvisDeepNavy,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurface,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorderGlow,
    outlineVariant = JarvisBorder,
    error = JarvisError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // JARVIS UI is intentionally locked to its signature dark navy/neon cyan cybernetic aesthetic
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
