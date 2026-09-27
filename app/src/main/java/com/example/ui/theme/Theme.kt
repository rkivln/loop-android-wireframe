package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LoopShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val LoopLightColorScheme = lightColorScheme(
    primary = BrandDark,
    onPrimary = Color.White,
    primaryContainer = SurfaceSecondary,
    onPrimaryContainer = TextPrimary,
    secondary = BrandBlue,
    onSecondary = Color.White,
    secondaryContainer = CategoryElectronicsBg,
    onSecondaryContainer = BrandBlue,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfacePureWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSecondary,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderDefault,
    error = BrandRed,
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
