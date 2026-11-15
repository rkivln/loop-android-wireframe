package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.models.AppearanceMode

private val LightColorScheme = lightColorScheme(
    primary = LightInkCharcoal,
    onPrimary = LightInkWhite,
    primaryContainer = LightAccentMint,
    onPrimaryContainer = LightAccentForestGreen,
    secondary = LightAccentForestGreen,
    onSecondary = LightInkWhite,
    secondaryContainer = LightAccentBeigeOat,
    onSecondaryContainer = LightInkCharcoal,
    tertiary = LightAccentWarmYellow,
    onTertiary = LightInkWhite,
    tertiaryContainer = LightAccentYellowSoft,
    onTertiaryContainer = LightInkCharcoal,
    background = LightPaperWarm,
    onBackground = LightInkCharcoal,
    surface = LightPaperPureWhite,
    onSurface = LightInkCharcoal,
    surfaceVariant = LightPaperIvory,
    onSurfaceVariant = LightInkSecondary,
    outline = LightPaperBorder,
    outlineVariant = LightPaperBorderSubtle,
    error = LightAlertCrimson,
    onError = LightInkWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkInkCharcoal,
    onPrimary = DarkInkWhite,
    primaryContainer = DarkAccentMint,
    onPrimaryContainer = DarkAccentForestGreen,
    secondary = DarkAccentForestGreen,
    onSecondary = DarkInkWhite,
    secondaryContainer = DarkAccentBeigeOat,
    onSecondaryContainer = DarkInkCharcoal,
    tertiary = DarkAccentWarmYellow,
    onTertiary = DarkInkWhite,
    tertiaryContainer = DarkAccentYellowSoft,
    onTertiaryContainer = DarkInkCharcoal,
    background = DarkPaperWarm,
    onBackground = DarkInkCharcoal,
    surface = DarkPaperPureWhite,
    onSurface = DarkInkCharcoal,
    surfaceVariant = DarkPaperIvory,
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkPaperBorder,
    outlineVariant = DarkPaperBorderSubtle,
    error = DarkAlertCrimson,
    onError = DarkInkWhite
)

object LoopTheme {
    val colors: LoopColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLoopColors.current
}

@Composable
fun LoopTheme(
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val useDarkTheme = when (appearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> systemInDark
    }

    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    val loopPalette = if (useDarkTheme) LoopDarkPalette else LoopLightPalette

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                insetsController.isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    CompositionLocalProvider(LocalLoopColors provides loopPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
