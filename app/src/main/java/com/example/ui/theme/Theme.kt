package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LoopShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

private val LoopLightColorScheme = lightColorScheme(
    primary = CanvasDark,
    onPrimary = Color.White,
    primaryContainer = CanvasSubtle,
    onPrimaryContainer = InkPrimary,
    secondary = AccentCobalt,
    onSecondary = Color.White,
    secondaryContainer = AccentCobaltSubtle,
    onSecondaryContainer = AccentCobalt,
    background = CanvasGround,
    onBackground = InkPrimary,
    surface = CanvasWhite,
    onSurface = InkPrimary,
    surfaceVariant = CanvasSubtle,
    onSurfaceVariant = InkSecondary,
    outline = LineHairline,
    outlineVariant = LineHairline,
    error = AlertCrimson,
    onError = Color.White
)

@Composable
fun LoopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LoopLightColorScheme,
        typography = Typography,
        shapes = LoopShapes,
        content = content
    )
}
