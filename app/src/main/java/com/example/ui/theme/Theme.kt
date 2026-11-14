package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = InkCharcoal,
    onPrimary = InkWhite,
    primaryContainer = AccentMint,
    onPrimaryContainer = AccentForestGreen,
    secondary = AccentForestGreen,
    onSecondary = InkWhite,
    secondaryContainer = AccentBeigeOat,
    onSecondaryContainer = InkCharcoal,
    tertiary = AccentWarmYellow,
    onTertiary = InkWhite,
    tertiaryContainer = AccentYellowSoft,
    onTertiaryContainer = InkCharcoal,
    background = PaperWarm,
    onBackground = InkCharcoal,
    surface = PaperPureWhite,
    onSurface = InkCharcoal,
    surfaceVariant = PaperIvory,
    onSurfaceVariant = InkSecondary,
    outline = PaperBorder,
    outlineVariant = PaperBorderSubtle
)

@Composable
fun LoopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
