package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Strict radius scale (Small 8dp, Medium 12dp, Large 16dp, ExtraLarge 20dp)
val LoopShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

private val LoopDarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextPrimary,
    tertiary = BrandAccent,
    onTertiary = Color.White,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    outlineVariant = BorderSubtle,
    error = StatusError,
    onError = Color.White
)

@Composable
fun LoopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LoopDarkColorScheme,
        typography = Typography,
        shapes = LoopShapes,
        content = content
    )
}
