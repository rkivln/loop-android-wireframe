package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LoopDarkColorScheme = darkColorScheme(
    primary = VividPurple,
    onPrimary = Color.White,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = NeonBlue,
    onSecondary = Color.White,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonMagenta,
    onTertiary = Color.White,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassBorderSubtle
)

@Composable
fun LoopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LoopDarkColorScheme,
        typography = Typography,
        content = content
    )
}
